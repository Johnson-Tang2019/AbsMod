package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.BladeRoute;
import com.abyssredemption.absmod.item.BladeStage;
import com.abyssredemption.absmod.item.MeowBladeItem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Four independent JVM launches against one opt-in disposable singleplayer world. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class ResonanceRecoveryTest {
    private final String worldName = "absmod-resonance-" + System.getProperty("absmod.resonanceWorld", "invalid");
    private final String phase = System.getProperty("absmod.resonancePhase", "");
    private boolean opened;
    private volatile boolean done;
    private int ticks;

    public ResonanceRecoveryTest() {
        require(worldName.matches("absmod-resonance-[0-9]{6,20}") && Set.of("seed", "prepare", "journal", "verify").contains(phase),
                "A fresh numeric world name and a fixture phase are required");
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::worldTick);
    }

    private void clientTick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (!opened && client.gui.screen() instanceof TitleScreen) {
            opened = true;
            client.options.pauseOnLostFocus = false;
            client.options.renderDistance().set(4);
            var path = client.gameDirectory.toPath().resolve("saves").resolve(worldName);
            if (phase.equals("seed")) {
                require(Files.notExists(path), "Fixture world exists");
                client.createWorldOpenFlows().createFreshLevel(worldName,
                        new LevelSettings(worldName, GameType.SURVIVAL,
                                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42, false, false), lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                                .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
            } else {
                require(Files.isRegularFile(path.resolve("level.dat")), "Fixture world is missing");
                client.createWorldOpenFlows().openWorld(worldName, () -> { throw new IllegalStateException("Could not open fixture"); });
            }
        }
        if (done) {
            client.stop();
        }
    }

    private void worldTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (done || !worldName.equals(server.getWorldData().getLevelName()) || server.getPlayerList().getPlayers().isEmpty()
                || ++ticks != 20) {
            return;
        }
        var player = server.getPlayerList().getPlayers().getFirst();
        switch (phase) {
            case "seed" -> {
                for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                    player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
                }
                var menu = open(player);
                insert(player, BladeRoute.NORMAL);
                menu.setCarried(new ItemStack(Items.PAPER, 7));
                SpiritTransferJournal.checkpointResonance(player);
                halt("unconfirmed");
            }
            case "prepare" -> {
                var menu = open(player);
                var escrow = ResonanceEscrow.of(player);
                verifySword(player, escrow.getItem(0), BladeRoute.NORMAL, 1);
                require(menu.getCarried().is(Items.PAPER) && menu.getCarried().getCount() == 7,
                        "Full inventory lost the retained cursor");
                require(escrow.getItem(1).getCount() == 11 && escrow.getItem(2).getCount() == 10,
                        "Unconfirmed costs changed");
                prepare(player, BladeRoute.NORMAL);
                SpiritTransferJournal.checkpointResonance(player);
                halt("before-soul-commit");
            }
            case "journal" -> {
                verifyAndRefund(player, BladeRoute.NORMAL);
                for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                    player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
                }
                open(player);
                insert(player, BladeRoute.DARK);
                var record = prepare(player, BladeRoute.DARK);
                SpiritTransferJournal.checkpointResonance(player);
                BladeSoulStore.find(server).orElseThrow().commitHandoff(server, record, record.withStage(2));
                halt("after-soul-commit");
            }
            case "verify" -> {
                verifyAndRefund(player, BladeRoute.DARK);
                System.out.println("RESONANCE_RECOVERY_PASS " + worldName);
                done = true;
            }
            default -> throw new IllegalStateException(phase);
        }
    }

    private static ResonanceMenu open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> new ResonanceMenu(id, inventory),
                Component.translatable("container.absmod.resonance")));
        return (ResonanceMenu) player.containerMenu;
    }

    private static void insert(ServerPlayer player, BladeRoute route) {
        var escrow = ResonanceEscrow.of(player);
        var sword = new ItemStack(blade(route, 1));
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Resonance recovery fixture"));
        sword.setDamageValue(101);
        sword.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 2);
        var server = player.level().getServer();
        var store = BladeSoulStore.forInitialization(server);
        BladeSoulService.initialize(store, sword, player.getUUID());
        var record = BladeSoulService.setTestAffection(store, sword, 42);
        SpiritTransferJournal.write(server, record);
        escrow.setItem(0, sword);
        int slot = 1;
        for (var cost : ResonanceMenu.costs(route, 2)) {
            escrow.setItem(slot, new ItemStack(cost.item(), cost.count() + (slot == 1 ? 3 : 2)));
            slot++;
        }
    }

    private static BladeSoulRecord prepare(ServerPlayer player, BladeRoute route) {
        var escrow = ResonanceEscrow.of(player);
        var input = escrow.getItem(0);
        var store = BladeSoulStore.find(player.level().getServer()).orElseThrow();
        var record = BladeSoulService.requireRecord(store, input);
        var output = new ArrayList<>(escrow.copyInputs());
        output.set(0, BladeComponentTransfer.transfer(input, blade(route, 2)));
        int slot = 1;
        for (var cost : ResonanceMenu.costs(route, 2)) {
            output.get(slot++).shrink(cost.count());
        }
        escrow.prepare(player, record, List.copyOf(output));
        return record;
    }

    private static void verifyAndRefund(ServerPlayer player, BladeRoute route) {
        var escrow = ResonanceEscrow.of(player);
        require(!escrow.locked(), "Login did not settle the pending upgrade");
        verifySword(player, escrow.getItem(0), route, 2);
        var first = blade(route, 1);
        var second = blade(route, 2);
        require(count(player, first) == 0 && count(player, second) == 0, "Full inventory received a copy");
        require(escrow.getItem(1).getCount() == 3 && escrow.getItem(2).getCount() == (route == BladeRoute.NORMAL ? 2 : 0),
                "Settlement charged a wrong amount");
        var advancement = player.level().getServer().getAdvancements().get(
                Identifier.fromNamespaceAndPath("absmod", "progression/" + route.getSerializedName() + "_stage_2"));
        require(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Recovery did not grant stage advancement");
        for (int slot = 0; slot < 8; slot++) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        ResonanceEscrow.onLogin(player);
        ResonanceEscrow.onLogin(player);
        require(count(player, second) == 1 && count(player, first) == 0 && escrow.getItem(0).isEmpty(),
                "Rerunning login duplicated or lost the blade");
        var costs = ResonanceMenu.costs(route, 2);
        require(count(player, costs.getFirst().item()) == 3, "Rerunning login changed the returned material balance");
        if (route == BladeRoute.NORMAL) {
            require(count(player, Items.COPPER_INGOT) == 2 && count(player, Items.PAPER) == 7,
                    "Rerunning login changed the remaining materials or cursor");
        }
        System.out.println("RESONANCE_RECOVERY_ROUTE_PASS " + route.getSerializedName());
    }

    private static void verifySword(ServerPlayer player, ItemStack sword, BladeRoute route, int stage) {
        var store = BladeSoulStore.find(player.level().getServer()).orElseThrow();
        var record = BladeSoulService.requireRecord(store, sword);
        int damage = blade(route, 2).getDefaultInstance().getMaxDamage();
        int initialMax = blade(route, 1).getDefaultInstance().getMaxDamage();
        int expected = stage == 1 ? 101 : (int) Math.ceil(101d * damage / initialMax);
        require(record.ownerId().orElseThrow().equals(player.getUUID()) && record.affection().orElseThrow() == 42
                && record.revision() == stage && sword.is(blade(route, stage)) && sword.isEnchanted()
                && sword.getHoverName().getString().equals("Resonance recovery fixture") && sword.getDamageValue() == expected,
                "Sword components, owner, affection, or revision changed");
    }

    private static MeowBladeItem blade(BladeRoute route, int stage) {
        return (MeowBladeItem) BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("absmod", BladeStage.of(route, stage).itemId()));
    }

    private static int count(ServerPlayer player, Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private void halt(String boundary) {
        System.out.println("RESONANCE_RECOVERY_HALT " + boundary + " " + worldName);
        System.out.flush();
        Runtime.getRuntime().halt(86);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
