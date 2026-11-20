package com.abyssredemption.absmod.cat;

import com.abyssredemption.absmod.block.CherryFurnitureBlock;
import com.abyssredemption.absmod.item.BondBellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.Shapes;

public final class ScratchingPostBlock extends CherryFurnitureBlock {
    public ScratchingPostBlock(Properties properties) {
        super(properties, Shapes.or(box(1,0,1,15,2,15), box(5,2,5,11,15,11)));
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof BondBellItem) || hand != InteractionHand.MAIN_HAND)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (player instanceof ServerPlayer owner) {
            var selected = CatLife.store(owner).selected(owner.getUUID());
            var cat = selected == null ? null : CatLife.nearby(owner, selected);
            if (cat == null || !cat.isOwnedBy(owner) || cat.isLying() || cat.distanceToSqr(Vec3.atCenterOf(pos)) > 6.25) {
                CatLife.tell(owner, "scratch_nearby");
            } else {
                cat.getLookControl().setLookAt(pos.getX()+0.5,pos.getY()+0.65,pos.getZ()+0.5);
                cat.swing(InteractionHand.MAIN_HAND,true);
                level.playSound(null,pos,SoundEvents.CAT_PURR,SoundSource.NEUTRAL,0.7F,1.1F);
                CatLife.tell(owner, "scratch");
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
