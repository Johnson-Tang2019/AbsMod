package com.abyssredemption.absmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CherryFloorLampBlock extends DoublePlantBlock {
    private static final VoxelShape BASE = Shapes.or(box(2, 0, 2, 14, 2, 14), box(6, 2, 6, 10, 16, 10));
    private static final VoxelShape SHADE = Shapes.or(box(6, 0, 6, 10, 3, 10), box(2, 3, 2, 14, 15, 14));

    public CherryFloorLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    protected boolean mayPlaceOn(BlockState support, BlockGetter level, BlockPos pos) {
        return support.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? SHADE : BASE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
            BlockState other = level.getBlockState(otherPos);
            boolean lit = !state.getValue(BlockStateProperties.LIT);
            if (other.is(this)) level.setBlockAndUpdate(otherPos, other.setValue(BlockStateProperties.LIT, lit));
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, lit));
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, lit ? 0.65F : 0.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
