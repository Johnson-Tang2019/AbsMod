package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.client.ResonanceScreen;
import com.abyssredemption.absmod.client.CatJournalScreen;
import com.abyssredemption.absmod.client.SpiritGameScreen;
import com.abyssredemption.absmod.cat.CatGenetics;
import net.minecraft.world.entity.animal.feline.Cat;
import com.abyssredemption.absmod.item.FieldGuideItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import com.abyssredemption.absmod.client.BladeCombatClient;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulService;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.abyssredemption.absmod.spirit.ResonanceMenu;
import com.abyssredemption.absmod.spirit.SpiritTransformationService;
import com.abyssredemption.absmod.spirit.SpiritAdornment;
import com.abyssredemption.absmod.spirit.SpiritGameMenu;
import com.abyssredemption.absmod.spirit.SpiritWardrobe;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import java.util.Arrays;
import net.minecraft.network.chat.contents.TranslatableContents;
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
    private static final long CAT_COLLECTION_QUEST_ID = 0x0c40fe16bd0d9954L;
    private static final long BOND_QUEST_ID = 0x6b3c47d1aa6174d9L;
    private static final long BRUSH_QUEST_ID = 0x4a47756ba2b0d3f5L;
    private static final long ADORNMENT_QUEST_ID = 0x4c1b8cf84280f105L;
    private static final long BOARD_QUEST_ID = 0x03e3d6eff96763a4L;
    private volatile BladeSpiritEntity spirit;
    private volatile boolean verifiedLeisure;
    private Cat observedCat;
    private volatile boolean verifiedBrush;
    private int spiritMenuWait;
    private volatile long activityRevision;
    private boolean started;
    private volatile boolean verified;
    private volatile boolean verifiedCat;
    private volatile boolean verifiedSpirit;
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
                if (chapter.getQuests().size() != 40 || !file.getTeamData(player).orElseThrow()
                        .isCompleted(file.getQuest(STAGE_TWO_QUEST_ID))) {
                    throw new IllegalStateException("FTB chapter or stage-two advancement task did not load and complete");
                }
                verified = true;
                LOGGER.info("PORT_2612_PASS pink screen action, retained blade, replay rejected, 40 FTB quests and stage two completed");
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
            case 248 -> server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var cat = EntityType.CAT.create(player.level(), EntitySpawnReason.COMMAND);
                cat.tame(player);
                cat.setCustomName(Component.literal("Mochi"));
                observedCat = cat;
                cat.setPos(player.position().add(1, 0, 0));
                player.level().addFreshEntity(cat);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack("field_guide"));
                player.inventoryMenu.broadcastChanges();
                FieldGuideItem.observe(player, cat, InteractionHand.MAIN_HAND);
                player.setShiftKeyDown(true);
                player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                player.setShiftKeyDown(false);
            });
            case 270 -> {
                if (!(client.screen instanceof CatJournalScreen screen) || !screen.getTitle().getString().contains("Mochi")) {
                    throw new IllegalStateException("26.1.2 Fabric collection did not display the observed cat");
                }
                screenshot(client, "port2612-cat-collection.png");
                var filter = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList().get(1);
                screen.mouseClicked(new MouseButtonEvent(filter.getX() + 8, filter.getY() + 8, new MouseButtonInfo(0, 0)), false);
            }
            case 345 -> server.execute(() -> {
                var file = ServerQuestFile.getInstance();
                var player = server.getPlayerList().getPlayers().getFirst();
                if (!file.getTeamData(player).orElseThrow().isCompleted(file.getQuest(CAT_COLLECTION_QUEST_ID))) {
                    throw new IllegalStateException("Collection filter did not complete the FTB quest");
                }
                verifiedCat = true;
                LOGGER.info("CAT_LIFE_FTB_2612_PASS");
            });
            case 380 -> {
                if (!verifiedCat) {
                    throw new IllegalStateException("FTB collection verification failed");
                }
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.closeContainer();
                    var sword = stack("meow_blade");
                    sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Lumen"));
                    sword.setDamageValue(61);
                    player.getInventory().setSelectedSlot(0);
                    player.setItemInHand(InteractionHand.MAIN_HAND, sword);
                    BladeSoulService.initialize(BladeSoulStore.forInitialization(server), sword, player.getUUID());
                    spirit = SpiritTransformationService.manifest(player);
                    player.getInventory().setItem(1, stack("bond_bell"));
                    player.getInventory().setSelectedSlot(1);
                    player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                });
            }
            case 415 -> {
                if (!(client.screen instanceof CatJournalScreen screen) || !screen.getTitle().getString().contains("Lumen")) {
                    if (++spiritMenuWait >= 160) {
                        throw new IllegalStateException("Bell companion list did not open");
                    }
                    // Manifestation checkpoints the player and regions before it opens the bell menu.
                    ticks--;
                    return;
                }
                var chosen = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> button.getMessage().getString().contains("Lumen")).findFirst().orElseThrow();
                screenshot(client, "port2612-spirit-list.png");
                press(screen, chosen);
            }
            case 450 -> {
                checkSpiritDetails(client, "0", "10", "spirit_unrecorded");
                screenshot(client, "port2612-spirit-before.png");
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.getInventory().setSelectedSlot(3);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack("gentle_brush"));
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    if (!spirit.isGrooming()) {
                        throw new IllegalStateException("Own spirit did not start responding to the brush");
                    }
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD, 2));
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    if (player.getMainHandItem().getCount() != 1) {
                        throw new IllegalStateException("Daily gift was charged twice");
                    }
                    player.getInventory().setSelectedSlot(1);
                    activityRevision = BladeSoulStore.find(server).orElseThrow().find(spirit.soulId()).orElseThrow().revision();
                });
            }
            case 467 -> {
                if (!clientSpirit(client).isGrooming()) {
                    throw new IllegalStateException("Spirit grooming was not synchronized to the client");
                }
            }
            case 475 -> {
                if (!(client.screen instanceof CatJournalScreen screen)) {
                    throw new IllegalStateException("Spirit activity screen closed before refresh");
                }
                var refresh = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> button.getMessage().getContents() instanceof TranslatableContents text
                                && text.getKey().equals("cats.absmod.spirit_refresh")).findFirst().orElseThrow();
                press(screen, refresh);
            }
            case 515 -> {
                checkSpiritDetails(client, "3", "7", "spirit_recorded");
                screenshot(client, "port2612-spirit-after.png");
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    var record = BladeSoulStore.find(server).orElseThrow().find(spirit.soulId()).orElseThrow();
                    var quests = ServerQuestFile.getInstance();
                    if (record.revision() != activityRevision || !quests.getTeamData(player).orElseThrow()
                            .isCompleted(quests.getQuest(BOND_QUEST_ID))) {
                        throw new IllegalStateException("Refreshing status modified the soul or the FTB bond quest did not complete");
                    }
                    verifiedSpirit = true;
                    LOGGER.info("SPIRIT_ACTIVITY_2612_PASS");
                });
            }
            case 540 -> {
                if (!verifiedSpirit || clientSpirit(client).isGrooming()) {
                    throw new IllegalStateException("Spirit activity verification or normal pose reset failed");
                }
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.closeContainer();
                    player.getInventory().setSelectedSlot(3);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack("gentle_brush"));
                    spirit.setPos(player.position().add(0, 0, 2.4));
                    spirit.setYRot(180);
                    spirit.setYBodyRot(180);
                    spirit.setYHeadRot(180);
                    spirit.getNavigation().stop();
                    spirit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
                    var record = BladeSoulStore.find(server).orElseThrow().find(spirit.soulId()).orElseThrow();
                    if (!spirit.isGrooming() || record.affection().orElseThrow() != 3.0) {
                        throw new IllegalStateException("Daily limit blocked a repeat pose or awarded extra affection");
                    }
                });
            }
            case 551 -> {
                client.player.getInventory().setSelectedSlot(3);
                client.player.setYRot(0);
                client.player.setXRot(8);
                client.getToastManager().clear();
            }
            case 558 -> {
                if (!clientSpirit(client).isGrooming() || clientSpirit(client).groomingProgress(0) <= 0) {
                    throw new IllegalStateException("Repeat grooming pose was not visible");
                }
                screenshot(client, "port2612-gentle-brush.png");
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.teleportTo(player.level(), player.getX() + 7, player.getY(), player.getZ(),
                            java.util.Set.of(), 0, 0, true);
                });
            }
            case 564 -> {
                if (clientSpirit(client).isGrooming()) {
                    throw new IllegalStateException("Leaving reach did not cancel the synchronized pose");
                }
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    observedCat.setPos(player.position().add(0.5, 0, 1));
                    observedCat.setOrderedToSit(true);
                    var genetics = (CatGenetics) observedCat;
                    var before = genetics.absmod$getGenome();
                    observedCat.mobInteract(player, InteractionHand.MAIN_HAND);
                    var quests = ServerQuestFile.getInstance();
                    if (!genetics.absmod$getGenome().equals(before) || !observedCat.isOrderedToSit()
                            || !quests.getTeamData(player).orElseThrow().isCompleted(quests.getQuest(BRUSH_QUEST_ID))) {
                        throw new IllegalStateException("Brush altered cat state or failed to complete the FTB quest");
                    }
                    verifiedBrush = true;
                    LOGGER.info("GENTLE_BRUSH_2612_PASS");
                });
            }
            case 595 -> {
                if (!verifiedBrush) {
                    throw new IllegalStateException("Brush verification did not finish successfully");
                }
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.getInventory().setItem(10, stack("blossom_clip"));
                    player.getInventory().setItem(11, stack("moon_clip"));
                    player.getInventory().setSelectedSlot(1);
                    player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                });
            }
            case 625 -> {
                var screen = (CatJournalScreen) client.screen;
                var spiritButton = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> button.getMessage().getString().contains("Lumen")).findFirst().orElseThrow();
                press(screen, spiritButton);
            }
            case 645 -> pressLabel(client.screen, "leisure.absmod.wardrobe");
            case 665 -> pressLabel(client.screen, "item.absmod.blossom_clip");
            case 686 -> {
                if (clientSpirit(client).adornment() != SpiritAdornment.BLOSSOM) {
                    throw new IllegalStateException("Blossom clip did not reach client");
                }
                client.getToastManager().clear();
                screenshot(client, "port2612-spirit-wardrobe.png");
                pressLabel(client.screen, "item.absmod.moon_clip");
            }
            case 710 -> {
                if (clientSpirit(client).adornment() != SpiritAdornment.MOON) {
                    throw new IllegalStateException("Moon clip did not reach client");
                }
                pressLabel(client.screen, "cats.absmod.back");
            }
            case 730 -> pressLabel(client.screen, "leisure.absmod.play");
            case 827 -> {
                if (!(client.screen instanceof SpiritGameScreen) || !(client.player.containerMenu instanceof SpiritGameMenu game)
                        || game.result() == 0) {
                    throw new IllegalStateException("The board did not finish");
                }
                client.getToastManager().clear();
                screenshot(client, "port2612-spirit-board.png");
                pressLabel(client.screen, "leisure.absmod.again");
            }
            case 851 -> {
                var game = (SpiritGameMenu) client.player.containerMenu;
                if (game.result() != 0 || java.util.stream.IntStream.range(0, 9).anyMatch(i -> game.cell(i) != 0)) {
                    throw new IllegalStateException("Starting another game did not clear the board");
                }
                pressLabel(client.screen, "leisure.absmod.back");
            }
            case 874 -> server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var quests = ServerQuestFile.getInstance();
                var team = quests.getTeamData(player).orElseThrow();
                var before = spirit.soulId();
                if (player.getInventory().getItem(10).getCount() != 1 || player.getInventory().getItem(11).getCount() != 1
                        || SpiritWardrobe.read(server, before) != SpiritAdornment.MOON
                        || !team.isCompleted(quests.getQuest(ADORNMENT_QUEST_ID))
                        || !team.isCompleted(quests.getQuest(BOARD_QUEST_ID))) {
                    throw new IllegalStateException("Adornment inventory or leisure quest failed");
                }
                player.closeContainer();
                SpiritTransformationService.recall(player, spirit, 0);
                player.getInventory().setSelectedSlot(0);
                spirit = SpiritTransformationService.manifest(player);
                if (!spirit.soulId().equals(before)) {
                    throw new IllegalStateException("Sword identity changed");
                }
                spirit.setPos(player.position().add(0, 0, 2.4));
                spirit.setYRot(180);
                spirit.setYBodyRot(180);
                spirit.setYHeadRot(180);
                verifiedLeisure = true;
            });
            case 956 -> {
                if (!verifiedLeisure || clientSpirit(client).adornment() != SpiritAdornment.MOON) {
                    throw new IllegalStateException("Accessory did not survive the recall and manifestation");
                }
                client.player.setYRot(0);
                client.player.setXRot(8);
                client.getToastManager().clear();
            }
            case 965 -> {
                screenshot(client, "port2612-spirit-adornment.png");
                LOGGER.info("SPIRIT_LEISURE_2612_PASS");
                client.stop();
            }
            default -> {}
        }
        if (ticks >= 748 && ticks < 815 && ticks % 12 == 4
                && client.screen instanceof SpiritGameScreen screen
                && client.player.containerMenu instanceof SpiritGameMenu game && game.result() == 0) {
            var buttons = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
            for (int i = 0; i < 9; i++) {
                if (game.cell(i) == 0 && buttons.get(i).active) {
                    press(screen, buttons.get(i));
                    break;
                }
            }
        }
    }

    private BladeSpiritEntity clientSpirit(Minecraft client) {
        if (client.level.getEntity(spirit.getId()) instanceof BladeSpiritEntity shown) {
            return shown;
        }
        throw new IllegalStateException("Could not find the client-side spirit");
    }

    private static void checkSpiritDetails(Minecraft client, String affection, String shortage, String interaction) {
        if (!(client.screen instanceof CatJournalScreen screen)) {
            throw new IllegalStateException("Spirit detail screen did not open");
        }
        var rows = screen.getTitle().getSiblings().get(2).getSiblings();
        var overview = rowDetails(rows.get(0), "spirit_overview");
        var progress = rowDetails(rows.get(1), "spirit_requirement_short");
        var claims = rowDetails(rows.get(2), "spirit_claims");
        var budget = rowDetails(rows.get(3), "spirit_daily_counts");
        if (!Arrays.toString(progress).equals("[2, 10, " + shortage + "]")) {
            throw new IllegalStateException("Wrong next-stage requirement: " + Arrays.toString(progress));
        }
        if (!containsTranslation(overview[0], "spirit_normal", "1")
                || !containsTranslation(overview[1], "spirit_bond", affection)
                || !overview[2].toString().equals("1500") || !overview[3].toString().equals("1561")
                || !containsTranslation(claims[0], interaction, null) || !containsTranslation(claims[1], interaction, null)
                || !Arrays.toString(budget).equals("[0, 4, 0, 4]")) {
            throw new IllegalStateException("Unexpected spirit daily status: " + screen.getTitle().getString());
        }
    }

    private static Object[] rowDetails(Component row, String key) {
        var text = (TranslatableContents) row.getSiblings().get(2).getContents();
        if (!text.getKey().equals("cats.absmod." + key)) {
            throw new IllegalStateException("Wrong status row: " + text.getKey());
        }
        return text.getArgs();
    }

    private static boolean containsTranslation(Object value, String key, String argument) {
        return value instanceof Component label && label.getContents() instanceof TranslatableContents text
                && text.getKey().equals("cats.absmod." + key)
                && (argument == null || text.getArgs()[0].toString().equals(argument));
    }

    private static void pressLabel(net.minecraft.client.gui.screens.Screen screen, String key) {
        var button = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(widget -> widget.getMessage().getContents() instanceof TranslatableContents label && label.getKey().equals(key))
                .findFirst().orElseThrow(() -> new IllegalStateException("Missing button " + key));
        press(screen, button);
    }

    private static void press(net.minecraft.client.gui.screens.Screen screen, Button button) {
        var click = new MouseButtonEvent(button.getX() + button.getWidth() / 2,
                button.getY() + button.getHeight() / 2, new MouseButtonInfo(0, 0));
        if (!button.active || !screen.mouseClicked(click, false)) {
            throw new IllegalStateException("Spirit detail button did not accept the click");
        }
    }

    private static ItemStack stack(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", name)));
    }

    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), 1, ignored -> {});
    }
}
