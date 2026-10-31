package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerAttackPacketMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;hasClientLoaded()Z"), cancellable = true)
    private void absmod$singleDamageEntry(ServerboundAttackPacket packet, CallbackInfo callback) {
        // After vanilla's thread handoff; raw packets cannot bypass the shared cadence.
        if (!(player.getMainHandItem().getItem() instanceof MeowBladeItem))com.abyssredemption.absmod.combat.BladeComboAttributes.update(player,0);
        if (player.getMainHandItem().getItem() instanceof MeowBladeItem
                || player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem) { callback.cancel(); }
    }
}
