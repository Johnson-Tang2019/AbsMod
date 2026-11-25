package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.block.BlossomSpringBlock;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Surveys generated chunks of a regular seeded overworld without placing any blocks. */
@Mod("absmod")
public final class SpringGenerationVerification {
    private final ArrayDeque<ChunkPos> candidates = new ArrayDeque<>();
    private int checked;
    public SpringGenerationVerification() {
        NeoForge.EVENT_BUS.addListener(this::start);
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void start(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        var unique = new LinkedHashSet<ChunkPos>();
        for (int step = 0; step < 5; step++) {
            var start = new BlockPos((step % 3)*1800, 120, (step / 3)*2200);
            var found = level.findClosestBiome3d(b -> b.is(Biomes.CHERRY_GROVE),start,5000,32,64);
            if (found == null) continue;
            var grove = new ChunkPos(found.getFirst());
            for(int r=0;r<=12;r++) for(int dx=-r;dx<=r;dx++) for(int dz=-r;dz<=r;dz++) {
                if (Math.max(Math.abs(dx),Math.abs(dz))!=r) continue;
                var pos=new ChunkPos(grove.x+dx,grove.z+dz);
                var gen=level.getChunkSource().getGenerator().getBiomeSource();
                if(gen.getNoiseBiome(QuartPos.fromBlock(pos.getMiddleBlockX()),QuartPos.fromBlock(found.getFirst().getY()),
                        QuartPos.fromBlock(pos.getMiddleBlockZ()),level.getChunkSource().randomState().sampler()).is(Biomes.CHERRY_GROVE))
                    unique.add(pos);
            }
        }
        candidates.addAll(unique);
        System.out.println("SPRING_SURVEY_CANDIDATES "+unique.size());
    }
    private void tick(ServerTickEvent.Post event) {
        var level=event.getServer().overworld();
        var candidate=candidates.poll();
        if(candidate==null) throw new IllegalStateException("No natural hot spring among "+checked+" generated chunks");
        checked++;
        var springs=new ArrayList<BlockPos>();
        level.getChunk(candidate.x,candidate.z).findBlocks(state->state.getBlock() instanceof BlossomSpringBlock,
                (pos,state)->springs.add(pos.immutable()));
        for(var stone:springs) {
            if (!level.getBiome(stone.above()).is(Biomes.CHERRY_GROVE)) continue;
            int sourceCount=0;
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
                var p=stone.offset(dx,1,dz);
                if(level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isSource())sourceCount++;
            }
            if(sourceCount!=9)continue;
            try{Files.writeString(Path.of("natural-spring.txt"),stone.getX()+" "+stone.getY()+" "+stone.getZ());}
            catch(java.io.IOException ex){throw new IllegalStateException(ex);}
            System.out.println("NATURAL_CHERRY_SPRING_PASS seed="+level.getSeed()+" stone="+stone+" checked="+checked);
            event.getServer().halt(false);
            return;
        }
    }
}
