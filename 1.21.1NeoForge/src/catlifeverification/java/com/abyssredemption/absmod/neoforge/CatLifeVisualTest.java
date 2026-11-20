package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.cat.*;
import com.abyssredemption.absmod.client.CatJournalScreen;
import com.abyssredemption.absmod.item.FieldGuideItem;
import com.abyssredemption.absmod.spirit.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Opt-in client test driven through server item interactions and real menu buttons. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class CatLifeVisualTest {
    private static final BlockPos NEST = new BlockPos(3, 100, 1);
    private final String world = "absmod-cat-life-" + System.getProperty("absmod.catLifeWorld", "");
    private final boolean verify = "verify".equals(System.getProperty("absmod.catLifePhase"));
    private boolean opened;
    private volatile boolean roseOnly, family, recalled;
    private volatile boolean done;
    private int ticks;
    private volatile int clientStep;
    private static final Set<Long> CAT_QUESTS = Set.of(0x0c40fe16bd0d9954L, 0x36ee8561675fba69L, 0x748803b75d9c83a3L, 0x4f30df8d1ccf70d3L);
    private int phase;
    private Cat amber;

    public CatLifeVisualTest() {
        check(world.matches("absmod-cat-life-[0-9]{6,20}"), "Numeric fixture ID required");
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
    }

    private void clientTick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (!opened && client.screen instanceof TitleScreen) {
            opened = true;
            client.options.pauseOnLostFocus = false;
            client.options.renderDistance().set(4);
            var path = client.gameDirectory.toPath().resolve("saves").resolve(world);
            if (verify) {
                check(Files.isRegularFile(path.resolve("level.dat")), "Fixture world unavailable");
                client.createWorldOpenFlows().openWorld(world, () -> { throw new IllegalStateException("Open fixture cancelled"); });
            } else {
                check(Files.notExists(path), "Fixture world already exists");
                client.createWorldOpenFlows().createFreshLevel(world,
                        new LevelSettings(world, GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42, false, false), lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                                .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
            }
        }
        if (done) {
            client.stop();
            return;
        }
        if (verify || !(client.screen instanceof CatJournalScreen screen)) {
            return;
        }
        var parts = screen.getTitle().getSiblings();
        check(parts.size() == 4, "Menu data not received");
        var visible = parts.get(2).getString();
        var firstRow = parts.get(2).getSiblings().isEmpty() ? "" : parts.get(2).getSiblings().getFirst().getSiblings().get(1).getString();
        boolean partnerDetails = parts.get(1).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                && text.getKey().equals("cats.absmod.partner_details");
        if (clientStep == 0 && visible.contains("Amber") && visible.contains("Rose")) {
            screenshot(client, "cat-life-collection.png");
            press(screen, 2);
            clientStep++;
        } else if (clientStep == 1 && visible.contains("Amber") && !visible.contains("Rose")) {
            roseOnly = true;
            press(screen, 0);
            clientStep++;
        } else if (clientStep == 2 && visible.contains("Amber") && visible.contains("Rose")) {
            press(screen, 0);
            clientStep++;
        } else if (clientStep == 3 && firstRow.equals("Rose") && visible.contains("Amber")) {
            check(parts.get(2).getSiblings().size() == 3, "Unknown parent row missing");
            screenshot(client, "cat-life-family.png");
            family = true;
            client.player.closeContainer();
            clientStep++;
        } else if (clientStep == 4 && visible.contains("Fixture blade") && !partnerDetails) {
            press(screen, 2);
            clientStep++;
        } else if (clientStep == 5 && visible.contains("Fixture blade") && partnerDetails) {
            press(screen, 1);
            clientStep++;
            recalled = true;
        }
    }

    private void serverTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (done || !world.equals(server.getWorldData().getLevelName()) || server.getPlayerList().getPlayers().isEmpty()) {
            return;
        }
        var player = server.getPlayerList().getPlayers().getFirst();
        var level = (ServerLevel) player.level();
        check(++ticks < 650, "Fixture timed out at server phase " + phase + " and client step " + clientStep);
        if (ticks == 20) {
            if (verify) {
                verifySaved(player, level);
                done = true;
                return;
            }
            seed(player, level);
        }
        if (phase == 1 && family && ticks > 40) {
            equip(player, stack("bond_bell"));
            amber.mobInteract(player, InteractionHand.MAIN_HAND);
            useBlock(player, NEST, Direction.UP);
            check(CatLife.store(player).home(amber.getUUID()) != null, "Bell right-click did not bind nest");
            phase++;
        }
        if (phase == 2 && ticks % 100 == 0) {
            var goal = ((CatHomeGoal.Carrier) amber).absmod$homeGoal();
            var goals = ((com.abyssredemption.absmod.mixin.MobGoalAccessor) (Object) amber).absmod$goals();
            System.out.println("CAT_LIFE_PATH " + amber.position() + " sitting="
                    + amber.isInSittingPose() + " ordered=" + amber.isOrderedToSit() + " path=" + amber.getNavigation().getPath()
                    + " active=" + goals.getAvailableGoals().stream().filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning).map(g -> g.getGoal().getClass().getSimpleName()).toList()
                    + " custom=" + goal);
        }
        if (phase == 2 && amber.isInSittingPose() && amber.distanceToSqr(Vec3.atBottomCenterOf(NEST)) < 1) {
            screenshotFromServer("cat-life-nest.png");
            equip(player, stack("bond_bell"));
            player.setShiftKeyDown(true);
            player.getMainHandItem().getItem().use(level, player, InteractionHand.MAIN_HAND);
            player.setShiftKeyDown(false);
            phase++;
        }
        if (phase == 3 && amber.distanceToSqr(player) <= 4 && !amber.isInSittingPose()) {
            equip(player, stack("meow_blade"));
            var blade = player.getMainHandItem();
            var store = BladeSoulStore.forInitialization(server);
            BladeSoulService.initialize(store, blade, player.getUUID());
            blade.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Fixture blade"));
            SpiritTransformationService.manifest(player);
            player.getInventory().setItem(1, stack("bond_bell"));
            player.getInventory().selected = 1;
            player.getMainHandItem().getItem().use(level, player, InteractionHand.MAIN_HAND);
            phase++;
        }
        if (phase == 4 && recalled) {
            var blade = player.getInventory().getItem(0);
            var reference = blade.get(BladeSoulComponents.REFERENCE);
            if (reference == null || !blade.getHoverName().getString().equals("Fixture blade")
                    || blade.getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem) {
                return;
            }
            check(roseOnly && com.abyssredemption.absmod.command.SoulCommands.findLoadedCopies(server, reference.soulId()).size() == 1,
                    "Filter or single-sword recall failed");
            for (String task : new String[]{"cat_collection", "cat_pedigree", "cat_nest", "cat_bell"}) {
                var advancement = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("absmod", "progression/" + task));
                check(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(), "Advancement failed: " + task);
            }
            var file = dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            var chapter = file.getChapter(0x1C4E810187B7DDC2L);
            check(chapter != null && chapter.getQuests().size() == 51, "Cat FTB chapter did not load");
            var team = file.getTeamData(player).orElseThrow();
            long completed = chapter.getQuests().stream().filter(quest -> CAT_QUESTS.contains(quest.id))
                    .filter(team::isCompleted).count();
            if (completed < CAT_QUESTS.size()) {
                if (ticks % 100 == 0) {
                    var guide = server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("absmod", "progression/field_guide"));
                    System.out.println("CAT_LIFE_FTB_WAIT " + completed + " guide=" + player.getAdvancements().getOrStartProgress(guide).isDone());
                }
                return;
            }
            System.out.println("CAT_LIFE_FTB_1211_PASS " + completed);
            System.out.println("CAT_LIFE_GAME_PASS " + world);
            done = true;
        }
    }

    private void seed(ServerPlayer player, ServerLevel level) {
        for (int x = -5; x <= 12; x++) for (int z = -5; z <= 6; z++) {
            level.setBlock(new BlockPos(x, 99, z), Blocks.QUARTZ_BLOCK.defaultBlockState(), 3);
        }
        player.teleportTo(level, 7.5, 100, 2.5, Set.of(), 90, 18);
        amber = cat(player, level, "Amber", 7.5, false, CatGenome.founder("minecraft:all_black", player.getUUID()));
        var empty = Optional.<java.util.UUID>empty();
        ((CatGenetics) amber).absmod$setGenome(new CatGenome(0, "minecraft:all_black", empty, empty, 0,
                0, 0, Optional.of(new CatCoat(0, 0, 0, 0, false))));
        var rose = cat(player, level, "Rose", 6, true, new CatGenome(0, "minecraft:all_black",
                Optional.of(amber.getUUID()), Optional.of(java.util.UUID.randomUUID()), 1, 0, 0,
                Optional.of(new CatCoat(0, 3, 2, 3, true))));
        equip(player, stack("cat_nest"));
        useBlock(player, NEST.below(), Direction.UP);
        check(level.getBlockState(NEST).getBlock() instanceof CatNestBlock, "Nest item could not be placed");
        equip(player, stack("field_guide"));
        player.inventoryMenu.broadcastChanges();
        amber.mobInteract(player, InteractionHand.MAIN_HAND);
        rose.mobInteract(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(true);
        player.getMainHandItem().getItem().use(level, player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        phase = 1;
    }

    private static void verifySaved(ServerPlayer player, ServerLevel level) {
        var spot = GlobalPos.of(level.dimension(), NEST);
        var store = CatLife.store(player);
        var home = store.at(spot);
        check(home != null && home.owner().equals(player.getUUID()) && home.cat().isPresent()
                && home.cat().orElseThrow().equals(store.selected(player.getUUID())), "Binding or bell selection lost after restart");
        var records = store.observations(player.getUUID());
        check(records.size() == 2 && records.values().stream().anyMatch(row -> row.name().equals("Rose")
                && row.genome().firstParent().equals(home.cat())), "Collection or lineage lost after restart");
        var id = home.cat().orElseThrow();
        check(level.getEntity(id) instanceof Cat, "Original cat missing after restart");
        check(level.destroyBlock(NEST, true, player) && store.home(id) == null && level.getEntity(id) instanceof Cat,
                "Breaking the nest did not remove only the assignment");
        System.out.println("CAT_LIFE_RELOAD_PASS");
        // Explicit save keeps the verification complete if this disposable world is opened again.
        level.getServer().saveEverything(true, true, true);
    }

    private static Cat cat(ServerPlayer player, ServerLevel level, String name, double x, boolean still, CatGenome genome) {
        var cat = EntityType.CAT.create(level);
        cat.tame(player);
        cat.setPos(x, 100, 1.5);
        cat.setCustomName(Component.literal(name));
        cat.setNoAi(still);
        ((CatGenetics) cat).absmod$setGenome(genome);
        level.addFreshEntity(cat);
        return cat;
    }

    private static void equip(ServerPlayer player, ItemStack stack) {
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    private static void useBlock(ServerPlayer player, BlockPos pos, Direction face) {
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), face, pos, false);
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
    }

    private static ItemStack stack(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod", path)));
    }

    private static void press(CatJournalScreen screen, int index) {
        var button = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList().get(index);
        check(button.active, "Expected an active menu button");
        button.onPress();
    }

    private static void screenshotFromServer(String name) {
        var client = Minecraft.getInstance();
        client.execute(() -> screenshot(client, name));
    }

    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), ignored -> {});
    }

    private static void check(boolean condition, String failure) {
        if (!condition) {
            throw new IllegalStateException(failure);
        }
    }
}
