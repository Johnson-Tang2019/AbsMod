package com.abyssredemption.absmod.block;

import com.abyssredemption.absmod.entity.MirrorEchoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class HeartMirrorBlock extends CherryFurnitureBlock {
    public HeartMirrorBlock(Properties properties) { super(properties, net.minecraft.world.phys.shapes.Shapes.or(box(2,0,2,14,3,14), box(1,3,6,15,16,10))); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (player instanceof ServerPlayer owner) MirrorEchoEntity.challenge(owner, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer owner) MirrorEchoEntity.tell(owner, "blade");
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
