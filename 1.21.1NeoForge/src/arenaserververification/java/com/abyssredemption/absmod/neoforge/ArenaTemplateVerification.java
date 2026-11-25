package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.arena.CherryArena;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
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
                check(level.getBlockState(pos) == palette.get(cell.getInt("state")),"world mismatch at "+pos);
            }
            check(level.isEmptyBlock(new BlockPos(0,70,19)) && level.isEmptyBlock(new BlockPos(0,80,-18)), "obsolete exit and roof removed");
            for (int x = -34; x <= 34; x++) for (int z = -34; z <= 34; z++) {
                if (x*x + z*z > 34*34) continue;
                var pos = new BlockPos(x, CherryArena.FLOOR, z);
                check(!level.isEmptyBlock(pos.below()), "gap in fighting floor " + pos);
                if (!pos.equals(CherryArena.MIRROR)) check(level.isEmptyBlock(pos), "obstructed floor " + pos);
                check(level.isEmptyBlock(pos.above()), "missing headroom " + pos);
            }
            check(!level.destroyBlock(CherryArena.MIRROR,true),"protected mirror");
            check(level.getChunk(15,15).getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,0,0)<0,"void");
            System.out.println("ARENA_DEDICATED_TEMPLATE_PASS cells="+blocks.size()+" palette="+palette.size());
            server.halt(false);
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    private static void check(boolean ok,String label) { if (!ok) throw new IllegalStateException(label); }
}
