package com.abyssredemption.absmod.client.spirit;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;

/** Authored floral silhouette; all secondary motion is cosmetic and resets with the pose. */
public final class BladeSpiritModel extends HumanoidModel<BladeSpiritEntity> {
    private final ModelPart tail;
    private final ModelPart skirt;
    public BladeSpiritModel(ModelPart root) {
        super(root);
        tail = body.hasChild("tail") ? body.getChild("tail") : null;
        skirt = body.hasChild("skirt") ? body.getChild("skirt") : null;
    }
    @Override public void setupAnim(BladeSpiritEntity entity,float limbSwing,float limbAmount,float age,float yaw,float pitch) {
        super.setupAnim(entity,limbSwing,limbAmount,age,yaw,pitch);
        float idle = (float) Math.sin(age * 0.075F);
        if (tail != null) { tail.zRot += idle * 0.12F; }
        if (skirt != null) { skirt.zRot += (leftLeg.xRot - rightLeg.xRot) * 0.035F; }
        for (String side : new String[] {"left", "right"}) {
            if (head.hasChild(side + "_ear")) { head.getChild(side + "_ear").zRot += idle * 0.035F; }
            if (head.hasChild(side + "_hair")) { head.getChild(side + "_hair").xRot += idle * 0.03F; }
        }
        for (int i = 0; i < 3; i++) { if (head.hasChild("back_hair_" + i)) { head.getChild("back_hair_" + i).xRot += idle * 0.025F; } }
    }
}
