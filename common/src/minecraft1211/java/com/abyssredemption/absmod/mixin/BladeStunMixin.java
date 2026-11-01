package com.abyssredemption.absmod.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class BladeStunMixin {
    @Inject(method="serverAiStep",at=@At("HEAD"),cancellable=true)
    private void interruptAi(CallbackInfo callback) {
        var mob=(Mob)(Object)this;
        if(com.abyssredemption.absmod.combat.BladeStun.active(mob)) {
            mob.setXxa(0);mob.setZza(0);mob.setJumping(false);
            var velocity=mob.getDeltaMovement();mob.setDeltaMovement(0,velocity.y,0);
            callback.cancel();
        }
    }
}
