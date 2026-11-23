package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.neoforge.client.CherryLanternLighting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
abstract class LanternBlockLightMixin {
    @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true)
    private void absmod$lanternEmission(long pos, BlockState state, CallbackInfoReturnable<Integer> callback) {
        int existing = callback.getReturnValueI();
        int light = CherryLanternLighting.emission((BlockLightEngine) (Object) this, pos, existing);
        if (light != existing) callback.setReturnValue(light);
    }
}
