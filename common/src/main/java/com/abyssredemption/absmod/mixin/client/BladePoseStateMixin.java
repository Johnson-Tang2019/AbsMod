package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.animation.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class BladePoseStateMixin implements BladePoseState {
    @Unique private BladeMotionPreset.Pose absmod$pose;
    @Unique private boolean absmod$left;
    public BladeMotionPreset.Pose absmod$getBladePose() { return absmod$pose; }
    public void absmod$setBladePose(BladeMotionPreset.Pose pose,boolean left) { absmod$pose=pose;absmod$left=left; }
    public boolean absmod$isLeftHanded() { return absmod$left; }
}
