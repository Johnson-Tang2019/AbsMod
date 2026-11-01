package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.BladeCombatClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void absmod$routeAttackInput(CallbackInfoReturnable<Boolean> callback) {
        if (BladeCombatClient.interceptPress((Minecraft) (Object) this)) { callback.setReturnValue(false); }
    }
}
