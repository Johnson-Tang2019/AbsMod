package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.animation.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerModel.class)
public abstract class BladePlayerPoseMixin {
    @WrapMethod(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V")
    private void animate(AvatarRenderState state,Operation<Void> original) {
        var data=(BladePoseState)state;var pose=data.absmod$getBladePose();
        float attack=state.attackTime;
        if(pose!=null) { state.attackTime=0; }
        try { original.call(state); }
        finally { state.attackTime=attack; }
        if(pose!=null) { BladeThirdPerson.apply((PlayerModel)(Object)this,pose,data.absmod$isLeftHanded()); }
    }
}
