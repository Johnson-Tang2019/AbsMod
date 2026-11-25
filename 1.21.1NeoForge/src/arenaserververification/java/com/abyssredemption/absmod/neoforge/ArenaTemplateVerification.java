package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.arena.CherryArena;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("absmod")
public final class ArenaTemplateVerification {
    private boolean checked;
    public ArenaTemplateVerification() { NeoForge.EVENT_BUS.addListener(this::verify); }

    private void verify(ServerTickEvent.Post event) {
        if (checked) return;
        checked = true;
        var server = event.getServer();
        var level = server.getLevel(CherryArena.DIMENSION);
        var name = ResourceLocation.parse("absmod:structure/cherry_arena.nbt");
        try (var in = server.getResourceManager().open(name)) {
            var root = NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());
            check(root.getInt("DataVersion") == 3955,"template version");
            var palette = new ArrayList<BlockState>();
            for (var entry : root.getList("palette",Tag.TAG_COMPOUND)) {
                var raw = (CompoundTag)entry;
                var state = NbtUtils.readBlockState(level.registryAccess().lookupOrThrow(Registries.BLOCK),raw);
                var encoded = NbtUtils.writeBlockState(state);
                check(encoded.getString("Name").equals(raw.getString("Name")),"palette name "+raw);
                var props = raw.getCompound("Properties");
                for (var key : props.getAllKeys())
                    check(encoded.getCompound("Properties").getString(key).equals(props.getString(key)),"palette property "+raw);
                palette.add(state);
            }
            var blocks = root.getList("blocks",Tag.TAG_COMPOUND);
            for (var entry : blocks) {
                var cell = (CompoundTag)entry;
                var xyz = cell.getList("pos",Tag.TAG_INT);
                BlockPos pos = CherryArena.ORIGIN.offset(xyz.getInt(0),xyz.getInt(1),xyz.getInt(2));
                if (palette.get(cell.getInt("state")).is(net.minecraft.world.level.block.Blocks.PINK_CONCRETE))
                    throw new IllegalStateException("saturated concrete at " + pos);
                check(level.getBlockState(pos) == palette.get(cell.getInt("state")),"world mismatch at "+pos);
            }
            check(level.isEmptyBlock(new BlockPos(0,70,19)) && level.isEmptyBlock(new BlockPos(0,80,-18)), "obsolete exit and roof removed");
            for (int x : new int[]{-44,44}) for (int z : new int[]{-44,44}) {
                for (int y = 64; y <= 100; y++)
                    check(level.isEmptyBlock(new BlockPos(x,y,z)), "former corner tower remains");
            }
            for (int x = -34; x <= 34; x++) for (int z = -34; z <= 34; z++) {
                if (x*x + z*z > 34*34) continue;
                var pos = new BlockPos(x, CherryArena.FLOOR, z);
                check(level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.CHERRY_PLANKS), "marked or incomplete playing surface " + pos);
                if (!pos.equals(CherryArena.MIRROR)) check(level.isEmptyBlock(pos), "obstructed floor " + pos);
                check(level.isEmptyBlock(pos.above()), "missing headroom " + pos);
            }
            checkGrove(level);
            check(level.isEmptyBlock(new BlockPos(16,95,-46)), "old gallery roof tip removed");
            check(level.getBlockState(new BlockPos(-7,84,-45)).is(net.minecraft.world.level.block.Blocks.GLASS), "upper lounge glazing");
            for (int step = 0; step < 6; step++) {
                var stair = new BlockPos(15 - step, 77 + step, -46);
                check(level.getBlockState(stair).is(net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ_STAIRS), "terrace stair");
                check(level.isEmptyBlock(stair.above()) && level.isEmptyBlock(stair.above(2)), "terrace stair headroom");
            }
            check(level.getBlockState(new BlockPos(0,94,50)).is(net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ), "south arch crown");
            check(level.getBlockState(new BlockPos(-46,85,-7)).is(net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS), "west clerestory");
            check(!level.destroyBlock(CherryArena.MIRROR,true),"protected mirror");
            check(level.getChunk(15,15).getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,0,0)<0,"void");
            System.out.println("ARENA_DEDICATED_TEMPLATE_PASS cells="+blocks.size()+" palette="+palette.size());
            server.halt(false);
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    private static void checkGrove(ServerLevel level) {
        var wood = new HashSet<BlockPos>();
        var foliage = new HashSet<BlockPos>();
        for (var p : BlockPos.betweenClosed(37, 77, -22, 56, 96, 20)) {
            var state = level.getBlockState(p);
            if (state.is(Blocks.CHERRY_WOOD)) wood.add(p.immutable());
            if (state.is(Blocks.CHERRY_LEAVES)) foliage.add(p.immutable());
        }
        var branches = new HashSet<>(wood);
        for (var root : new BlockPos[]{new BlockPos(49,77,-12), new BlockPos(51,77,0), new BlockPos(48,77,11)}) {
            check(branches.remove(root), "missing or joined tree at " + root);
            var queue = new ArrayDeque<BlockPos>();
            queue.add(root);
            while (!queue.isEmpty()) {
                var p = queue.removeFirst();
                for (var direction : Direction.values()) {
                    var next = p.relative(direction);
                    if (branches.remove(next)) queue.addLast(next);
                }
            }
        }
        check(branches.isEmpty(), "detached wood in planted trees");
        check(!foliage.isEmpty(), "missing cherry canopy");
        var queue = new ArrayDeque<>(wood);
        while (!queue.isEmpty()) {
            var p = queue.removeFirst();
            for (var direction : Direction.values()) {
                var next = p.relative(direction);
                if (foliage.remove(next)) queue.addLast(next);
            }
        }
        check(foliage.isEmpty(), "detached cherry canopy");
    }

    private static void check(boolean ok,String label) { if (!ok) throw new IllegalStateException(label); }
}
