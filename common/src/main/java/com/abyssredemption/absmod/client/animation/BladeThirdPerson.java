package com.abyssredemption.absmod.client.animation;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

/** Upper-body adaptation keeps vanilla skin, walking legs, crouch and other players' poses. */
public final class BladeThirdPerson {
    private BladeThirdPerson() {}
    public static void apply(PlayerModel model,BladeMotionPreset.Pose pose,boolean left) {
        float mirror=left?-1:1,weight=pose.weight();
        rotate(model.body,pose.bone("torso"),weight,mirror,true);
        rotate(model.body,pose.bone("body"),weight,mirror,true);
        arm(model.rightArm,pose.bone(left?"left arm":"right arm"),weight,mirror);
        arm(model.leftArm,pose.bone(left?"right arm":"left arm"),weight,mirror);
        // Vanilla shoulders are siblings of the torso: carry their pivots with the authored torso turn.
        for(var arm:new ModelPart[]{model.leftArm,model.rightArm}) {
            var offset=new Vector3f(arm.x,arm.y,arm.z).rotateY(model.body.yRot);
            arm.x=offset.x;arm.z=offset.z;arm.yRot+=model.body.yRot;
        }
    }
    private static void arm(ModelPart part,BladeMotionPreset.Transform transform,float weight,float mirror) {
        var angles=transform.rotation().getEulerAnglesZYX(new Vector3f());
        part.xRot=part.xRot*(1-weight)-angles.x*weight;
        part.yRot=part.yRot*(1-weight)+angles.y*weight*mirror;
        part.zRot=part.zRot*(1-weight)-angles.z*weight*mirror;
        // MMD shoulder translations are not portable to Minecraft's rigid one-piece arms.
        // Keep the anatomical shoulder pivot; only the torso carries it through space.
    }
    private static void rotate(ModelPart part,BladeMotionPreset.Transform transform,float weight,float mirror,boolean body) {
        var angles=transform.rotation().getEulerAnglesZYX(new Vector3f());
        part.xRot+=angles.x*weight;part.yRot-=angles.y*weight*mirror;part.zRot-=angles.z*weight*mirror;
    }
}
