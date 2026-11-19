package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.cat.CatJournalMenu;
import com.abyssredemption.absmod.cat.CatLife;
import com.abyssredemption.absmod.cat.CatNestBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class BondBellItem extends Item {
    public BondBellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer owner) {
            if (player.isShiftKeyDown()) {
                CatLife.callSelected(owner);
            } else {
                CatJournalMenu.openPartners(owner);
            }
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof CatNestBlock)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            if (player.isShiftKeyDown()) {
                CatLife.useNest(player, context.getClickedPos(), true);
            } else {
                CatLife.bind(player, context.getClickedPos());
            }
        }
        return InteractionResult.SUCCESS;
    }
}
