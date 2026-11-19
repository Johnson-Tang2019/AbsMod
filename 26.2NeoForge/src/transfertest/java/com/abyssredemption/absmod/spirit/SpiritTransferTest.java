package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.command.SoulCommands;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import com.abyssredemption.absmod.item.SpectralBladeItem;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Uses only opt-in disposable worlds. Preparation deliberately stops without a final save. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class SpiritTransferTest {
    private final String worldName = "absmod-transfer-" + System.getProperty("absmod.transferWorld", "invalid");
    private final boolean prepare = "prepare".equals(System.getProperty("absmod.transferPhase"));
    private boolean opened;
    private volatile boolean finished;
    private int ticks;
    private int worldWait;
    private UUID activeSoul;
    private UUID firstCarrier;
    private static final int X = 320;

    public SpiritTransferTest() {
        require(worldName.matches("absmod-transfer-[0-9]{6,20}"), "A new numeric fixture ID is required");
        require(prepare || "recover".equals(System.getProperty("absmod.transferPhase")), "Bad fixture phase");
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::worldTick);
    }

    private void clientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!opened && mc.gui.screen() instanceof TitleScreen) {
            opened = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.renderDistance().set(4);
            var folder = mc.gameDirectory.toPath().resolve("saves").resolve(worldName);
            if (prepare) {
                require(Files.notExists(folder), "The fixture world already exists");
                mc.createWorldOpenFlows().createFreshLevel(worldName,
                        new LevelSettings(worldName, GameType.SURVIVAL,
                                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42, false, false),
                        lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                                .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                        new TitleScreen());
            } else {
                require(Files.exists(folder.resolve("level.dat")), "The fixture world does not exist");
                mc.createWorldOpenFlows().openWorld(worldName, () -> { throw new IllegalStateException("Fixture open cancelled"); });
            }
        }
        if (opened && !finished && mc.getSingleplayerServer() == null) {
            require(++worldWait < 1200, "Timed out opening fixture world");
        }
        if (finished) { mc.stop(); }
    }

    private void worldTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (!server.getWorldData().getLevelName().equals(worldName) || finished
                || server.getPlayerList().getPlayers().isEmpty()) { return; }
        var player = server.getPlayerList().getPlayers().getFirst();
        if (++ticks < 20) { return; }
        var nether = server.getLevel(Level.NETHER);
        require(nether != null, "Fixture has no Nether");
        if (prepare) {
            prepare(player, server.overworld(), nether);
        } else {
            recover(player, nether);
        }
        require(ticks < 720, "Fixture timed out");
    }

    private void prepare(ServerPlayer player, ServerLevel overworld, ServerLevel nether) {
        var server = overworld.getServer();
        var inv = player.getInventory();
        if (ticks == 20) {
            platform(overworld, true);
            teleport(player, overworld);
        }
        if (ticks == 60) {
            select(player, 0);
            var blade = newBlade(player, overworld, 0);
            activeSoul = blade.get(BladeSoulComponents.REFERENCE).soulId();
            firstCarrier = SpiritTransformationService.manifest(player).getUUID();
            platform(nether, false);
            teleport(player, nether);
        }
        if (ticks == 100) {
            var store = BladeSoulStore.find(server).orElseThrow();
            require(store.find(activeSoul).orElseThrow().carrier().orElseThrow().entityId().equals(firstCarrier),
                    "The spirit entered a blocked destination");
            platform(nether, true);
        }
        if (ticks <= 100) { return; }

        var store = BladeSoulStore.find(server).orElseThrow();
        var active = store.find(activeSoul).orElseThrow();
        var carrier = active.carrier().orElseThrow();
        if (!(nether.getEntity(carrier.entityId()) instanceof BladeSpiritEntity followed) || !followed.active()) { return; }
        require(!carrier.entityId().equals(firstCarrier) && overworld.getEntity(firstCarrier) == null
                && carrier.lastLocation().orElseThrow().dimension().equals(Level.NETHER)
                && followed.distanceToSqr(player) < 25 && active.affection().orElseThrow() == 42.0,
                "Cross-dimension transfer did not preserve authority");
        require(SoulCommands.findLoadedCopies(server, activeSoul).isEmpty(), "Following issued a sword");
        System.out.println("SPIRIT_TRANSFER_FOLLOW_PASS " + worldName);

        // Faults are injected at the same durable states the transaction service uses.
        for (int slot = 1; slot <= 4; slot++) {
            select(player, slot);
            var blade = newBlade(player, nether, slot);
            UUID soul = blade.get(BladeSoulComponents.REFERENCE).soulId();
            var materializing = slot == 1;
            var spirit = materializing ? new BladeSpiritEntity(SpiritEntityType.get(), nether)
                    : SpiritTransformationService.manifest(player);
            var before = store.find(soul).orElseThrow();
            if (materializing) {
                SpiritTransferJournal.write(server, before);
                SpiritTransferJournal.checkpointPlayer(player, false);
                spirit.bind(player.getUUID(), soul);
                spirit.setPos(player.getX() + 1, player.getY(), player.getZ());
            }
            var pending = before.transition(materializing ? "materializing" : slot == 4 ? "travelling" : "recalling",
                    new SpiritCarrier(spirit.getUUID(), UUID.randomUUID(), blade.copy(), slot,
                            Optional.of(GlobalPos.of(nether.dimension(), spirit.blockPosition())), true));
            store.commitHandoff(server, before, pending);
            if (materializing) {
                var stage = ((com.abyssredemption.absmod.item.MeowBladeItem) blade.getItem()).stage();
                inv.setItem(slot, SpectralBladeItem.create(stage, blade));
                require(nether.addFreshEntity(spirit), "Could not save the interrupted manifestation");
            } else if (slot == 2) {
                inv.setItem(slot, new ItemStack(Items.STONE, 7));
            } else if (slot == 3) {
                inv.setItem(slot, blade);
            } else {
                var arrival = new BladeSpiritEntity(SpiritEntityType.get(), overworld);
                arrival.restoreFrom(spirit);
                arrival.setUUID(UUID.randomUUID());
                arrival.setPos(X + 1, 100, 0);
                require(overworld.addFreshEntity(arrival), "Could not save the interrupted destination carrier");
            }
        }
        select(player, 0);
        teleport(player, overworld);
        SpiritTransferJournal.checkpointPlayer(player, true);
        var prepared = newBlade(player, overworld, 5);
        var before = store.find(prepared.get(BladeSoulComponents.REFERENCE).soulId()).orElseThrow();
        SpiritTransferJournal.write(server, before);
        SpiritTransferJournal.checkpointPlayer(player, false);
        // This record is deliberately newer than the world SavedData at process termination.
        var pending = before.transition("materializing", new SpiritCarrier(UUID.randomUUID(), UUID.randomUUID(),
                prepared, 5, Optional.of(GlobalPos.of(overworld.dimension(), player.blockPosition())), true));
        store.commitHandoff(server, before, pending);
        System.out.println("SPIRIT_TRANSFER_HALT_READY " + worldName);
        System.out.flush();
        Runtime.getRuntime().halt(86);
    }

    private void recover(ServerPlayer player, ServerLevel nether) {
        var server = nether.getServer();
        var inv = player.getInventory();
        var store = BladeSoulStore.find(server).orElseThrow();
        if (ticks < 65) { return; }
        require(player.level() == server.overworld() && inv.getItem(0).getItem() instanceof SpectralBladeItem,
                "The saved owner or active link was not restored");
        var soul = inv.getItem(0).get(BladeSoulComponents.REFERENCE).soulId();
        var active = store.find(soul).orElseThrow();
        require(active.form().equals("spirit") && active.ownerId().orElseThrow().equals(player.getUUID()),
                "Rejoin lost the committed companion");
        if (!(server.overworld().getEntity(active.carrier().orElseThrow().entityId()) instanceof BladeSpiritEntity companion)
                || !companion.active()) { return; }
        require(active.carrier().orElseThrow().lastLocation().orElseThrow().dimension().equals(Level.OVERWORLD),
                "Rejoin did not restore cross-dimension following");

        for (int slot : new int[]{1, 3, 4, 5}) { verifySword(player, store, inv.getItem(slot), slot); }
        require(inv.getItem(2).is(Items.STONE) && inv.getItem(2).getCount() == 7,
                "Recovery replaced an occupied slot");
        var waiting = store.records().stream().filter(r -> r.form().equals("recalling")).findFirst().orElseThrow();
        require(SoulCommands.findLoadedCopies(server, waiting.soulId()).isEmpty(), "Blocked recovery leaked a copy");
        inv.setItem(8, inv.getItem(2));
        inv.setItem(2, ItemStack.EMPTY);
        require(SpiritTransformationService.recoverInterrupted(player, waiting.soulId()), "Free slot did not allow recovery");
        verifySword(player, store, inv.getItem(2), 2);
        require(!SpiritTransformationService.recoverInterrupted(player, waiting.soulId()), "Repeated recovery delivered");
        select(player, 0);
        require(SpiritTransformationService.recallForAttack(player), "Could not recall after relog");
        verifySword(player, store, inv.getItem(0), 0);
        require(!SpiritTransformationService.recallForAttack(player), "Repeated recall delivered");
        for (var level : server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                require(!(entity instanceof BladeSpiritEntity old) || !old.active(),
                        "An old entity still owns a recovered soul");
            }
        }
        System.out.println("SPIRIT_TRANSFER_RESTART_PASS " + worldName + " identity, components, rejoin, blocked slot, no repeat");
        finished = true;
    }

    private static ItemStack newBlade(ServerPlayer player, ServerLevel level, int slot) {
        var blade = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", "meow_blade")));
        blade.set(DataComponents.CUSTOM_NAME, Component.literal("核对原剑 " + slot));
        blade.setDamageValue(101 + slot);
        blade.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 2);
        var store = BladeSoulStore.forInitialization(level.getServer());
        BladeSoulService.initialize(store, blade, player.getUUID());
        BladeSoulService.setTestAffection(store, blade, 42.0 + slot);
        player.getInventory().setItem(slot, blade);
        return blade;
    }

    private static void verifySword(ServerPlayer player, BladeSoulStore store, ItemStack stack, int slot) {
        var record = BladeSoulService.requireRecord(store, stack);
        require(record.ownerId().orElseThrow().equals(player.getUUID()) && record.affection().orElseThrow() == 42.0 + slot
                && stack.getDamageValue() == 101 + slot && stack.getHoverName().getString().equals("核对原剑 " + slot)
                && stack.isEnchanted() && SoulCommands.findLoadedCopies(player.level().getServer(), record.soulId()).size() == 1,
                "Recovery did not preserve exactly one original in slot " + slot);
    }

    private static void select(ServerPlayer player, int slot) { player.getInventory().setSelectedSlot(slot); }
    private static void teleport(ServerPlayer player, ServerLevel level) {
        player.teleportTo(level, X + .5, 100, .5, Set.of(), 0, 0, true);
    }

    private static void platform(ServerLevel level, boolean open) {
        for (int x = X - 4; x <= X + 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlockAndUpdate(new BlockPos(x, 99, z), Blocks.STONE.defaultBlockState());
                for (int y = 100; y <= 102; y++) {
                    boolean centre = x == X && z == 0;
                    level.setBlockAndUpdate(new BlockPos(x, y, z), (open || centre ? Blocks.AIR : Blocks.STONE).defaultBlockState());
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
