package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.animation.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class BladePoseExtractionMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void extract(Avatar entity,AvatarRenderState state,float partial,CallbackInfo ci) {
        ((BladePoseState)state).absmod$setBladePose(entity instanceof Player p?BladeMotionPlayer.pose(p,partial):null,
                entity.getMainArm()==HumanoidArm.LEFT);
    }
}
