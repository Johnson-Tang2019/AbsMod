package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.client.ResonanceScreen;
import com.abyssredemption.absmod.client.BladeCombatClient;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulService;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.abyssredemption.absmod.spirit.ResonanceMenu;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** End-to-end 26.1.2 client and optional quest configuration check in a disposable world. */
public final class PortVisualTest implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long CHAPTER_ID = 0x1c4e810187b7ddc2L;
    private static final long RESONANCE_QUEST_ID = 0x14107493db5542a5L;
    private static final long STAGE_TWO_QUEST_ID = 0x3fa472d83cf0c5ffL;
    private boolean started;
    private volatile boolean verified;
    private int ticks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        if (!started && client.screen instanceof TitleScreen) {
            started = true;
            client.options.pauseOnLostFocus = false;
            client.options.renderDistance().set(4);
            client.getWindow().setWindowed(1280, 900);
            var settings = new LevelSettings("Ab's Mod 26.1.2 verification", GameType.SURVIVAL,
                    new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
            client.createWorldOpenFlows().createFreshLevel("absmod-port-" + System.currentTimeMillis(),
                    settings, new WorldOptions(42, false, false), lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                            .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
        }
        var server = client.getSingleplayerServer();
        if (!started || client.player == null || server == null) {
            return;
        }
        switch (++ticks) {
            case 15 -> server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.getInventory().setItem(2, new ItemStack(Items.AMETHYST_SHARD));
                player.setItemInHand(InteractionHand.MAIN_HAND, stack("meow_blade"));
                player.inventoryMenu.broadcastChanges();
            });
            case 30 -> server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var heart = stack("resonance_heart");
                var blade = player.getMainHandItem();
                player.setItemInHand(InteractionHand.MAIN_HAND, heart);
                heart.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                var menu = (ResonanceMenu) player.containerMenu;
                blade.setDamageValue(780);
                var store = BladeSoulStore.forInitialization(server);
                BladeSoulService.initialize(store, blade, player.getUUID());
                BladeSoulService.setTestAffection(store, blade, 25);
                menu.getSlot(0).set(blade);
                menu.getSlot(1).set(new ItemStack(Items.AMETHYST_SHARD, 16));
                menu.getSlot(2).set(new ItemStack(Items.COPPER_INGOT, 16));
                menu.broadcastChanges();
            });
            case 80 -> screenshot(client, "port2612-resonance-before.png");
            case 90 -> {
                if (!(client.screen instanceof ResonanceScreen screen)) {
                    throw new IllegalStateException("Custom resonance screen did not open");
                }
                var confirm = screen.children().stream().filter(Button.class::isInstance)
                        .map(Button.class::cast).findFirst().orElseThrow();
                if (!confirm.active || !screen.mouseClicked(new MouseButtonEvent(
                        confirm.getX() + 8, confirm.getY() + 8, new MouseButtonInfo(0, 0)), false)) {
                    throw new IllegalStateException("Pink resonance button did not accept the click");
                }
            }
            case 110 -> screenshot(client, "port2612-resonance-after.png");
            case 120 -> client.gameMode.handleInventoryButtonClick(client.player.containerMenu.containerId,
                    ResonanceMenu.CONFIRM_BUTTON);
            case 140 -> server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var menu = (ResonanceMenu) player.containerMenu;
                var blade = menu.getSlot(0).getItem();
                if (!menu.isComplete() || !blade.is(stack("meow_blade_stage_2").getItem())
                        || !blade.has(BladeSoulComponents.REFERENCE) || blade.getDamageValue() != 900
                        || menu.getSlot(1).getItem().getCount() != 8 || menu.getSlot(2).getItem().getCount() != 8) {
                    throw new IllegalStateException("Stage, blade identity, durability or transaction replay check failed");
                }
                var returned = blade.copy();
                player.closeContainer();
                if (!player.getInventory().contains(returned)) {
                    throw new IllegalStateException("Closing resonance did not return the sword");
                }
                var file = ServerQuestFile.getInstance();
                var chapter = file.getChapterOrThrow(CHAPTER_ID);
                if (chapter.getQuests().size() != 33 || !file.getTeamData(player).orElseThrow()
                        .isCompleted(file.getQuest(STAGE_TWO_QUEST_ID))) {
                    throw new IllegalStateException("FTB chapter or stage-two advancement task did not load and complete");
                }
                verified = true;
                LOGGER.info("PORT_2612_PASS pink screen action, retained blade, replay rejected, 33 FTB quests and stage two completed");
            });
            case 165 -> {
                if (!verified) {
                    throw new IllegalStateException("Server-side port verification did not finish successfully");
                }
                var quest = ClientQuestFile.getInstance().getQuest(RESONANCE_QUEST_ID);
                if (quest == null || quest.getDescription().size() < 3) {
                    throw new IllegalStateException("FTB resonance instructions did not load");
                }
                ClientQuestFile.openGui(quest, true);
            }
            case 190 -> screenshot(client, "port2612-ftb-resonance.png");
            case 205 -> {
                client.setScreen(null);
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack("dark_meow_blade"));
                });
            }
            case 230 -> {
                var combat = BladeCombatClient.visibleStatus(client);
                if (!combat.dark() || combat.stage() != 1 || combat.cooldownSeconds() != 0) {
                    throw new IllegalStateException("26.1.2 Fabric combat HUD did not receive the dark blade status");
                }
                screenshot(client, "port2612-combat-hud.png");
                LOGGER.info("PORT_2612_HUD_PASS");
            }
            case 248 -> client.stop();
            default -> {}
        }
    }

    private static ItemStack stack(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", name)));
    }

    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), 1, ignored -> {});
    }
}
