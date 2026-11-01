package com.abyssredemption.absmod.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;

/** Calibration for the existing diagonal 2D Meow Blade sprites, without a sheath asset. */
public final class BladeGrip {
    private BladeGrip() {}
    public static void firstPerson(PoseStack stack,BladeMotionPreset.Pose pose,HumanoidArm arm) {
        float mirror=arm==HumanoidArm.RIGHT?1:-1;
        var position=new org.joml.Vector3f(.48F*mirror,-.60F,-1.25F);
        var rotation=Axis.ZP.rotationDegrees(72*mirror);
        if(pose!=null) {
            var transform=pose.bone("blade");var p=transform.position();var q=transform.rotation();
            position.lerp(new org.joml.Vector3f(-p.x()*.035F*mirror,p.y()*.035F-.65F,-1.15F-p.z()*.02F),pose.weight());
            rotation.slerp(new Quaternionf(-q.x(),q.y()*mirror,-q.z()*mirror,q.w()),pose.weight());
        }
        stack.translate(position.x,position.y,position.z);
        stack.mulPose(rotation);
        stack.mulPose(Axis.ZP.rotationDegrees(-45*mirror));
        // Thin the generated sprite edge geometry, not the supplied PNG artwork.
        stack.scale(.85F*mirror,.85F,.12F);
        stack.translate(.28F,.28F,0);
    }
}
