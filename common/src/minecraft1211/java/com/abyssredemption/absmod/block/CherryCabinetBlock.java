package com.abyssredemption.absmod.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CherryCabinetBlock extends BarrelBlock {
    public CherryCabinetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
