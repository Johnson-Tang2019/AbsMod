package com.abyssredemption.absmod.spirit;

import com.mojang.logging.LogUtils;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.mixin.PlayerListSaveInvoker;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

/** The latest durable handoff per soul. An older world autosave cannot undo a committed handoff. */
final class SpiritTransferJournal {
    private SpiritTransferJournal() {}

    private static Path directory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("absmod_spirit_transfers");
    }

    static List<BladeSoulRecord> read(MinecraftServer server) {
        Path directory = directory(server);
        if (Files.notExists(directory)) { return List.of(); }
        try (var files = Files.newDirectoryStream(directory, "*.dat")) {
            var records = new ArrayList<BladeSoulRecord>();
            for (Path file : files) {
                var record = BladeSoulRecord.CODEC.parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap())).getOrThrow();
                if (!file.getFileName().toString().equals(record.soulId() + ".dat")) {
                    throw new IOException("Handoff file does not match its soul");
                }
                records.add(record);
            }
            return records;
        } catch (IOException | RuntimeException exception) {
            throw storageFailure("read handoffs", exception);
        }
    }

    static void write(MinecraftServer server, BladeSoulRecord record) {
        Path scratch = null;
        try {
            Path directory = Files.createDirectories(directory(server));
            Path destination = directory.resolve(record.soulId() + ".dat");
            scratch = Files.createTempFile(directory, "transfer-", ".tmp");
            var encoded = BladeSoulRecord.CODEC.encodeStart(
                    server.registryAccess().createSerializationContext(NbtOps.INSTANCE), record).getOrThrow();
            NbtIo.writeCompressed((CompoundTag) encoded, scratch);
            try (var channel = FileChannel.open(scratch, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            Files.move(scratch, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException exception) {
            throw storageFailure("write handoff for " + record.soulId(), exception);
        } finally {
            if (scratch != null) {
                try { Files.deleteIfExists(scratch); }
                catch (IOException exception) { LogUtils.getLogger().warn("Could not remove {}", scratch, exception); }
            }
        }
    }

    /** Game tests use disconnected mock players and have no player save to verify. */
    static void checkpointPlayer(ServerPlayer player, boolean saveCarriers) {
        if (player.connection == null) { return; }
        MinecraftServer server = player.level().getServer();
        try {
            var inventory = inventoryTag(player);
            boolean localOwner = server.isSingleplayerOwner(player.getGameProfile());
            if (saveCarriers) {
                server.saveEverything(true, true, true);
            } else {
                ((PlayerListSaveInvoker) server.getPlayerList()).absmod$savePlayer(player);
                if (localOwner) {
                    var storage = ((com.abyssredemption.absmod.mixin.ServerStorageAccessor) server).absmod$getLevelStorage();
                    storage.saveDataTag(server.registryAccess(), server.getWorldData(), server.getPlayerList().getSingleplayerData());
                }
            }
            Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID() + ".dat");
            var stored = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
            if (!java.util.Objects.equals(inventory, stored.get("Inventory"))) {
                throw new IOException("Saved inventory did not match the handoff");
            }
            if (localOwner) {
                var level = NbtIo.readCompressed(server.getWorldPath(LevelResource.ROOT).resolve("level.dat"),
                        NbtAccounter.unlimitedHeap());
                var local = level.getCompound("Data").getCompound("Player");
                if (!java.util.Objects.equals(inventory, local.get("Inventory"))) {
                    throw new IOException("Integrated host inventory did not match level.dat");
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw storageFailure("checkpoint player " + player.getUUID(), exception);
        }
    }

    /** Confirms the destination carrier really reached the entity region before granting authority. */
    static void checkpointManifestation(ServerPlayer player, BladeSpiritEntity carrier) {
        checkpointPlayer(player, true);
        if (player.connection == null) { return; }
        var server = player.level().getServer();
        var dimension = carrier.level().dimension();
        var chunk = carrier.chunkPosition();
        Path folder = DimensionType.getStorageFolder(dimension, server.getWorldPath(LevelResource.ROOT)).resolve("entities");
        Path file = folder.resolve("r." + chunk.getRegionX() + "." + chunk.getRegionZ() + ".mca");
        try {
            if (!Files.isRegularFile(file)) { throw new IOException("Destination entity region missing"); }
            var info = new RegionStorageInfo(server.getWorldData().getLevelName(), dimension, "entities");
            try (var region = new RegionFile(info, file, folder, false);
                    var input = region.getChunkDataInputStream(chunk)) {
                if (input == null) { throw new IOException("Destination entity chunk missing"); }
                var tag = NbtIo.read(input);
                for (var entity : tag.getList("Entities", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                    if (entity instanceof CompoundTag row && (row.hasUUID("UUID") && row.getUUID("UUID").equals(carrier.getUUID()))) {
                        return;
                    }
                }
                throw new IOException("Destination carrier not found on disk");
            }
        } catch (IOException | RuntimeException exception) {
            throw storageFailure("checkpoint carrier " + carrier.getUUID(), exception);
        }
    }

    private static net.minecraft.nbt.Tag inventoryTag(ServerPlayer player) {
        return player.saveWithoutId(new CompoundTag()).get("Inventory");
    }

    private static IllegalStateException storageFailure(String action, Exception exception) {
        LogUtils.getLogger().error("Could not {}. The blade handoff remains recoverable.", action, exception);
        return new IllegalStateException("transfer_storage", exception);
    }
}
