package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.cat.CatLifeStore;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/** Runs under the world's session lock, before loading dimensions or accepting players. */
public final class WorldUpgrade {
    private static final int CURRENT_SCHEMA = 1;

    private WorldUpgrade() {}

    public static boolean snapshotIfNeeded(Path world, String release) {
        Path data = world.resolve("data");
        Path marker = data.resolve("absmod_save_version.txt");
        try {
            if (!Files.notExists(marker)) {
                List<String> stamp = Files.readAllLines(marker);
                if (stamp.size() != 2 || !stamp.get(1).matches("[0-9][A-Za-z0-9.+_-]*")) {
                    throw new IOException("Invalid save marker: " + marker);
                }
                int schema = Integer.parseInt(stamp.getFirst());
                String previous = stamp.get(1);
                if (schema < 1 || schema > CURRENT_SCHEMA ||
                        new DefaultArtifactVersion(previous).compareTo(new DefaultArtifactVersion(release)) > 0) {
                    throw new IOException("Unsupported save version " + stamp + ", running " + release);
                }
                if (schema == CURRENT_SCHEMA && previous.equals(release)) return false;
            }
            if (Files.exists(world.resolve("level.dat")) || Files.exists(world.resolve("level.dat_old"))
                    || Files.isDirectory(world.resolve("region")) || Files.isDirectory(world.resolve("playerdata"))
                    || Files.isDirectory(data)) {
                Path archive = WorldUpgradeBackup.create(world);
                LogUtils.getLogger().info("Ab's Mod world upgrade backup verified: {}", archive);
            }
            return true;
        } catch (IOException | RuntimeException ex) {
            throw failure(world, ex);
        }
    }

    public static void finish(Path world, HolderLookup.Provider registries, String release) {
        Path data = world.resolve("data");
        Path marker = data.resolve("absmod_save_version.txt");
        try {
            // Schema 1 adopts the existing item IDs, components, ledgers and handoff files in place.
            checkLedger(registries, data, "absmod_blade_souls", BladeSoulStore.CODEC);
            checkLedger(registries, data, "absmod_blade_growth", BladeGrowthStore.CODEC);
            checkLedger(registries, data, "absmod_cat_life", CatLifeStore.CODEC);
            SpiritTransferJournal.read(data.resolve("absmod_spirit_transfers"), registries);
            Files.createDirectories(data);
            Path pending = Files.createTempFile(data, ".absmod-save-", ".tmp");
            try {
                Files.writeString(pending, CURRENT_SCHEMA + "\n" + release + "\n");
                try (var channel = FileChannel.open(pending, StandardOpenOption.WRITE)) { channel.force(true); }
                Files.move(pending, marker, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(pending);
            }
        } catch (IOException | RuntimeException ex) {
            throw failure(world, ex);
        }
    }

    private static IllegalStateException failure(Path world, Exception cause) {
        return new IllegalStateException("Ab's Mod 无法安全升级此世界，请检查磁盘空间、备份和日志。World: " + world, cause);
    }

    private static void checkLedger(HolderLookup.Provider registries, Path data, String name, Codec<?> codec) throws IOException {
        Path file = data.resolve(name + ".dat");
        if (Files.notExists(file)) return;
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            codec.parse(registries.createSerializationContext(NbtOps.INSTANCE),
                    root.get("data")).getOrThrow();
        } catch (RuntimeException ex) {
            throw new IOException("Invalid ledger: " + file, ex);
        }
    }
}
