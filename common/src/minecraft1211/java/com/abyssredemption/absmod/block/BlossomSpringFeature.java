package com.abyssredemption.absmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import java.util.function.Supplier;

/** A shallow sealed bath on an existing level patch of cherry-grove soil. */
public final class BlossomSpringFeature extends Feature<NoneFeatureConfiguration> {
    private final Supplier<BlockState> spring;

    public BlossomSpringFeature(Supplier<BlockState> spring) {
        super(NoneFeatureConfiguration.CODEC);
        this.spring = spring;
    }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        BlockPos floor = context.origin().below();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            var pos = floor.offset(x, 0, z);
            if (!level.ensureCanWrite(pos) || !level.getBlockState(pos).is(BlockTags.DIRT)) return false;
            if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                var above = level.getBlockState(pos.above());
                if (!above.isAir() && !above.is(Blocks.PINK_PETALS) && !above.is(Blocks.SHORT_GRASS)) return false;
                if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) return false;
            }
        }
        var base = level.getBlockState(floor.below());
        if (!base.is(BlockTags.DIRT) && !base.is(BlockTags.BASE_STONE_OVERWORLD) && !base.is(Blocks.GRAVEL)) return false;
        level.setBlock(floor.below(), spring.get(), 2);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var cell = floor.offset(x, 0, z);
            if (!level.isEmptyBlock(cell.above())) level.setBlock(cell.above(), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(cell, Blocks.WATER.defaultBlockState(), 2);
        }
        for (var offset : java.util.List.of(new BlockPos(-2, 1, -1), new BlockPos(1, 1, 2),
                new BlockPos(2, 1, 1), new BlockPos(-1, 1, -2))) {
            var rim = floor.offset(offset);
            if (level.isEmptyBlock(rim)) level.setBlock(rim, Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState(), 2);
        }
        for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var bank = floor.relative(direction, 2).above();
            var petals = Blocks.PINK_PETALS.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT, 1 + context.random().nextInt(3));
            if (level.isEmptyBlock(bank) && petals.canSurvive(level, bank)) level.setBlock(bank, petals, 2);
        }
        return true;
    }
}
