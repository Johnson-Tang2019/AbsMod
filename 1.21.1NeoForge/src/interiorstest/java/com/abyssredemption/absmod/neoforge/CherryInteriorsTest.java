package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.entity.PinkDisplayFrame;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "absmod", dist = Dist.CLIENT)
public final class CherryInteriorsTest {
    private static final BlockPos ROOM = new BlockPos(0, 90, 0);
    private static final String GLASS = "pink_glowing_glass", PANE = "pink_glowing_glass_pane", FRAME = "pink_display_frame";
    private static final String FLOOR = "cherry_floor_lamp", LAMP = "cherry_table_lamp", TABLE = "cherry_tea_table",
            STAND = "cherry_flower_stand", CABINET = "cherry_display_cabinet";
    private boolean started;
    private int tick;

    public CherryInteriorsTest() { NeoForge.EVENT_BUS.addListener(this::tick); }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.getWindow().setWindowed(1200, 760);
            mc.createWorldOpenFlows().createFreshLevel("absmod-interiors-" + System.currentTimeMillis(),
                    new LevelSettings("Cherry interiors", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                            new GameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(42, false, false),
                    lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        tick++;
        if (tick == 40) {
            server(mc, CherryInteriorsTest::exercise);
            mc.options.hideGui = true;
        }
        if (tick == 100) {
            check(mc.level.getEntitiesOfClass(PinkDisplayFrame.class, new AABB(ROOM).inflate(12)).size() == 2, "two synced frames");
            check(mc.level.getBrightness(LightLayer.BLOCK, ROOM.offset(-4, 1, 0)) == 15, "floor lamp block light");
            for (var name : List.of(GLASS, PANE, FRAME, FLOOR, LAMP, TABLE, STAND, CABINET)) {
                check(mc.getItemRenderer().getModel(stack(name), mc.level, mc.player, 0) !=
                        mc.getItemRenderer().getItemModelShaper().getModelManager().getMissingModel(), "baked " + name);
            }
            mc.getToasts().clear();
            Screenshot.grab(mc.gameDirectory, "cherry-interiors-room.png", mc.getMainRenderTarget(), message -> {});
        }
        if (tick == 115) server(mc, p -> p.teleportTo(p.serverLevel(), 0.5, 90, -2.7, Set.of(), 0, -1));
        if (tick == 145) Screenshot.grab(mc.gameDirectory, "cherry-interiors-frames.png", mc.getMainRenderTarget(), message -> {});
        if (tick == 170) {
            server(mc, p -> {
                var ftb = ServerQuestFile.INSTANCE;
                var team = ftb.getTeamData(p).orElseThrow();
                for (long id : new long[]{0x2aa18b80e5226d11L, 0x563ae4f1ed152bcaL, 0x080ad3073c650462L, 0x4e879b6570e66fd8L})
                    check(team.isCompleted(ftb.getQuest(id)), "new FTB quest " + Long.toHexString(id));
            });
            com.mojang.logging.LogUtils.getLogger().info("CHERRY_INTERIORS_PASS eight recipes, glass and panes, lamp light/switch/break, frames, cabinet, four FTB quests");
            mc.options.hideGui = false;
            mc.stop();
        }
    }

    private static void exercise(ServerPlayer p) {
        var level = p.serverLevel();
        level.setDayTime(12700);
        for (int x = -6; x <= 6; x++) for (int z = -10; z <= 2; z++)
            level.setBlockAndUpdate(ROOM.offset(x, -1, z), Blocks.CHERRY_PLANKS.defaultBlockState());
        for (int x = -6; x <= 6; x++) for (int y = 0; y <= 4; y++) {
            var material = (x == -6 || x == 6 || x == -3 || x == 3) ? Blocks.STRIPPED_CHERRY_LOG :
                    (y == 0 || y == 4) ? Blocks.CHERRY_PLANKS : Blocks.SMOOTH_SANDSTONE;
            level.setBlockAndUpdate(ROOM.offset(x, y, 2), material.defaultBlockState());
        }
        p.teleportTo(level, 0.5, 90, -5.5, Set.of(), 0, 2);
        hold(p, stack("field_guide"));
        craft(p, GLASS, 4, "minecraft:pink_stained_glass", "minecraft:pink_stained_glass", "minecraft:pink_stained_glass",
                "minecraft:pink_stained_glass", "minecraft:glowstone_dust");
        craft(p, PANE, 16, GLASS, GLASS, GLASS, GLASS, GLASS, GLASS);
        craft(p, FRAME, 1, "minecraft:item_frame", PANE, "minecraft:pink_petals");
        String slab = "minecraft:cherry_slab", fence = "minecraft:cherry_fence", paper = "minecraft:paper";
        craft(p, FLOOR, 1, paper, GLASS, paper, "", fence, "", "", slab, "");
        craft(p, LAMP, 1, paper, GLASS, paper, "", slab, "");
        craft(p, TABLE, 1, slab, slab, slab, fence, "minecraft:pink_petals", fence);
        craft(p, STAND, 1, "", slab, "", "", fence, "", "", slab, "");
        craft(p, CABINET, 1, slab, slab, slab, "minecraft:pink_petals", "minecraft:barrel", "minecraft:pink_petals", slab, slab, slab);
        BlockPos lamp = ROOM.west(4);
        place(p, FLOOR, lamp);
        click(p, lamp.above(), Direction.NORTH);
        check(!level.getBlockState(lamp).getValue(BlockStateProperties.LIT) &&
                level.getBlockState(lamp.above()).getLightEmission(level, lamp.above()) == 0, "toggle off from shade");
        click(p, lamp, Direction.NORTH);
        check(level.getBlockState(lamp.above()).getValue(BlockStateProperties.LIT), "toggle on from base");
        var spare = ROOM.offset(-5, 0, -4);
        level.setBlockAndUpdate(spare.above(), Blocks.STONE.defaultBlockState());
        hold(p, stack(FLOOR));
        click(p, spare.below(), Direction.UP);
        check(level.isEmptyBlock(spare) && p.getMainHandItem().getCount() == 1, "blocked headroom");
        level.removeBlock(spare.above(), false);
        place(p, FLOOR, spare);
        p.gameMode.destroyBlock(spare.above());
        check(level.isEmptyBlock(spare) && level.isEmptyBlock(spare.above()) && dropped(p, spare, FLOOR) == 1, "single drop from upper half");
        var baseTest = ROOM.offset(5, 0, -7);
        place(p, FLOOR, baseTest);
        p.gameMode.destroyBlock(baseTest);
        check(level.isEmptyBlock(baseTest.above()) && dropped(p, baseTest, FLOOR) == 1, "single drop from base");
        place(p, TABLE, ROOM.offset(0, 0, -1));
        place(p, STAND, ROOM.east(4));
        place(p, "minecraft:flower_pot", ROOM.east(4).above());
        hold(p, stack("minecraft:pink_tulip"));
        click(p, ROOM.east(4).above(), Direction.UP);
        check(level.getBlockState(ROOM.east(4).above()).is(Blocks.POTTED_PINK_TULIP), "flower stand holds potted plant");
        var cupboard = ROOM.east(2);
        place(p, CABINET, cupboard);
        click(p, cupboard, Direction.NORTH);
        var container = (BarrelBlockEntity)level.getBlockEntity(cupboard);
        check(p.containerMenu instanceof net.minecraft.world.inventory.ChestMenu && container.getContainerSize() == 27
                && container.getDisplayName().getString().equals(stack(CABINET).getHoverName().getString()), "cabinet screen and name");
        container.setItem(13, stack("minecraft:apple"));
        p.closeContainer();
        var saved = container.saveWithFullMetadata(level.registryAccess());
        var loaded = (BarrelBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(cupboard,
                level.getBlockState(cupboard), saved, level.registryAccess());
        check(loaded != null && loaded.getItem(13).is(net.minecraft.world.item.Items.APPLE), "cabinet inventory roundtrip");
        place(p, LAMP, cupboard.above());
        for (int x = -5; x <= -4; x++) for (int y = 1; y <= 2; y++) {
            var pos = ROOM.offset(x, y, 2);
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(id(GLASS)).defaultBlockState());
            check(level.getBlockState(pos).getLightEmission(level, pos) == 12, "glass emission");
        }
        var window = ROOM.offset(-2, 1, 2);
        for (int i = 0; i < 2; i++) level.removeBlock(window.east(i), false);
        place(p, PANE, window);
        place(p, PANE, window.east());
        check(level.getBlockState(window).getValue(BlockStateProperties.EAST)
                && level.getBlockState(window.east()).getValue(BlockStateProperties.WEST), "pane connections");
        check(level.getBlockState(window).getLightEmission(level, window) == 12, "pane emission");
        var wet = ROOM.offset(-5, 0, -7);
        level.setBlockAndUpdate(wet, Blocks.WATER.defaultBlockState());
        place(p, PANE, wet);
        check(level.getBlockState(wet).getValue(BlockStateProperties.WATERLOGGED), "panes waterlog");
        p.gameMode.destroyBlock(wet);
        check(dropped(p, wet, PANE) == 1, "glass pane drop");
        var ceiling = ROOM.offset(5, 3, -2);
        level.setBlockAndUpdate(ceiling, Blocks.CHERRY_PLANKS.defaultBlockState());
        var mapFrame = hang(p, ceiling, Direction.DOWN);
        hold(p, net.minecraft.world.item.MapItem.create(level, 0, 0, (byte)0, false, false));
        mapFrame.interact(p, InteractionHand.MAIN_HAND);
        check(mapFrame.hasFramedMap() && mapFrame.getItem().has(DataComponents.MAP_ID), "ceiling map frame");
        mapFrame.hurt(p.damageSources().playerAttack(p), 1);
        mapFrame.hurt(p.damageSources().playerAttack(p), 1);
        level.removeBlock(ceiling, false);
        var exhibit = hang(p, ROOM.offset(1, 2, 2), Direction.NORTH);
        hold(p, stack("rose_gold_sword")); exhibit.interact(p, InteractionHand.MAIN_HAND);
        exhibit.interact(p, InteractionHand.MAIN_HAND);
        check(exhibit.getRotation() == 1 && exhibit.getItem().is(stack("rose_gold_sword").getItem()), "rotate item");
        var tag = new CompoundTag(); exhibit.save(tag);
        var reloaded = (PinkDisplayFrame)EntityType.loadEntityRecursive(tag, level, entity -> entity);
        check(reloaded != null && reloaded.getRotation() == 1 && reloaded.getItem().is(exhibit.getItem().getItem()), "frame save/load");
        hang(p, ROOM.offset(0, 2, 2), Direction.NORTH);
        var spareFrame = hang(p, ROOM.offset(5, -1, -4), Direction.UP);
        hold(p, stack("minecraft:amethyst_shard")); spareFrame.interact(p, InteractionHand.MAIN_HAND);
        spareFrame.hurt(p.damageSources().playerAttack(p), 1);
        check(spareFrame.getItem().isEmpty() && spareFrame.isAlive(), "recover exhibit separately");
        spareFrame.hurt(p.damageSources().playerAttack(p), 1);
        check(spareFrame.isRemoved() && dropped(p, ROOM.offset(5, 0, -4), FRAME) == 1, "recover correct frame");
        var spareCabinet = ROOM.offset(4, 0, -6);
        place(p, CABINET, spareCabinet);
        var stored = stack("minecraft:apple").copyWithCount(3);
        ((BarrelBlockEntity) level.getBlockEntity(spareCabinet)).setItem(5, stored);
        p.gameMode.destroyBlock(spareCabinet);
        check(dropped(p, spareCabinet, CABINET) == 1 && dropped(p, spareCabinet, "minecraft:apple") == 3, "cabinet drops its contents once");
        hold(p, ItemStack.EMPTY);
        p.teleportTo(level, .5, 90, -7.5, Set.of(), 0, 0);
    }

    private static PinkDisplayFrame hang(ServerPlayer p, BlockPos support, Direction face) {
        hold(p, stack(FRAME)); click(p, support, face);
        var pos = support.relative(face);
        var frames = p.serverLevel().getEntitiesOfClass(PinkDisplayFrame.class, new AABB(pos).inflate(.3),
                frame -> frame.getDirection() == face && frame.getPos().equals(pos));
        check(frames.size() == 1 && p.getMainHandItem().isEmpty(), "hang " + face);
        return frames.getFirst();
    }

    private static int dropped(ServerPlayer p, BlockPos pos, String item) {
        return p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.25)).stream()
                .filter(entity -> entity.getItem().is(stack(item).getItem())).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static void place(ServerPlayer p, String name, BlockPos pos) {
        hold(p, stack(name));
        p.setShiftKeyDown(true);
        click(p, pos.below(), Direction.UP);
        p.setShiftKeyDown(false);
        check(p.serverLevel().getBlockState(pos).is(BuiltInRegistries.BLOCK.get(id(name))), "placed " + name);
        hold(p, ItemStack.EMPTY);
    }

    private static void click(ServerPlayer p, BlockPos support, Direction face) {
        var point = Vec3.atCenterOf(support).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.49));
        p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND, new BlockHitResult(point, face, support, false));
    }

    private static void craft(ServerPlayer p, String result, int count, String... ingredients) {
        var cells = new ArrayList<ItemStack>();
        for (var name : ingredients) cells.add(name.isEmpty() ? ItemStack.EMPTY : stack(name));
        while (cells.size() < 9) cells.add(ItemStack.EMPTY);
        var input = CraftingInput.of(3, 3, cells);
        var level = p.serverLevel();
        var output = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().value().assemble(input, level.registryAccess());
        check(output.is(stack(result).getItem()) && output.getCount() == count, "recipe " + result + " (got " + output + ")");
        hold(p, output);
    }

    private static void hold(ServerPlayer p, ItemStack item) {
        p.setItemInHand(InteractionHand.MAIN_HAND, item);
        p.inventoryMenu.broadcastChanges();
    }

    private static ResourceLocation id(String key) {
        return key.contains(":") ? ResourceLocation.parse(key) : ResourceLocation.fromNamespaceAndPath("absmod", key);
    }
    private static ItemStack stack(String key) { return new ItemStack(BuiltInRegistries.ITEM.get(id(key))); }
    private static void server(Minecraft mc, java.util.function.Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer();
        server.submit(() -> action.accept(server.getPlayerList().getPlayers().getFirst())).join();
    }
    private static void check(boolean condition, String subject) {
        if (!condition) throw new IllegalStateException("Cherry interiors: " + subject);
    }
}
