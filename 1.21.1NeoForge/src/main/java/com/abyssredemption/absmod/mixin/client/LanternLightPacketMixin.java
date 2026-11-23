package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.neoforge.client.CherryLanternLighting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class LanternLightPacketMixin {
    @Inject(method = "applyLightData", at = @At("TAIL"))
    private void absmod$refreshNearbyLanterns(int x, int z, ClientboundLightUpdatePacketData packet,
            CallbackInfo callback) {
        CherryLanternLighting.lightPacket(x, z);
    }
}
