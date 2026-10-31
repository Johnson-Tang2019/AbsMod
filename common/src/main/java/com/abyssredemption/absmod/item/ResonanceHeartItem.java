package com.abyssredemption.absmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ResonanceHeartItem extends Item {
    public ResonanceHeartItem(Properties properties){super(properties.stacksTo(1));}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            serverPlayer.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->
                new com.abyssredemption.absmod.spirit.ResonanceMenu(id,inventory),
                net.minecraft.network.chat.Component.translatable("container.absmod.resonance")));
        return InteractionResult.SUCCESS;
    }
}
