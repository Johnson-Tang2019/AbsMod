package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.bedrock.client.BedrockGeometry;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;

/** Cosmetic animation of a free bone hierarchy, independent of HumanoidModel. */
public final class BedrockSpiritModel extends EntityModel<BladeSpiritEntity> {
    private final Map<String, ModelPart> bones;
    private final boolean nurseFox;
    private final ModelPart root;
    public BedrockSpiritModel(BedrockGeometry.Baked baked) {
        super();
        root=baked.root();
        nurseFox = baked.bones().containsKey("GuiRoot") && baked.bones().containsKey("NurseCap");
        bones = new java.util.LinkedHashMap<>(baked.bones());
        if (nurseFox) {
            String[][] aliases = {{"head","Head"},{"body","UpBody"},{"left_arm","LeftArm"},
                {"right_arm","RightArm"},{"left_leg","LeftLeg"},{"right_leg","RightLeg"},
                {"left_ear","Left_ear"},{"right_ear","Right_ear"},{"tail","Tail"},{"tail_tip","Tail7"}};
            for (var alias : aliases) {
                var part = bones.get(alias[1]);
                if (part == null) { throw new IllegalArgumentException("Missing nurse fox bone: " + alias[1]); }
                bones.put(alias[0], part);
            }
        }
        for (String name : new String[]{"head","body","left_arm","right_arm","left_leg","right_leg","left_ear","right_ear","tail"}) {
            if (!bones.containsKey(name)) { throw new IllegalArgumentException("Required spirit bone missing: " + name); }
        }
    }
    @Override public void setupAnim(BladeSpiritEntity entity,float limbSwing,float limbAmount,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        if (nurseFox) {
            // Preserve all authored geometry; select the normal humanoid appearance.
            for (String name : new String[]{"FOX","Backgrounds","Expression","Mouth_surprise","Mouth_smile","Mouth_smile2"}) {
                bones.get(name).visible = false;
            }
            var root = bones.get("GuiRoot");
            root.xScale = root.yScale = root.zScale = 0.6F;
        }
        bones.get("head").yRot += (float)Math.toRadians(Math.clamp(yaw,-70,70));
        bones.get("head").xRot += (float)Math.toRadians(Math.clamp(pitch,-45,45));
        float step=(float)Math.cos(limbSwing*0.6662F)*Math.clamp(limbAmount,0,1)*0.7F;
        bones.get("left_leg").xRot += step;
        bones.get("right_leg").xRot -= step;
        bones.get("left_arm").xRot -= step;
        bones.get("right_arm").xRot += step;
        bones.get("tail").yRot += (float)Math.sin(age*0.075F)*0.25F;
        if (bones.containsKey("tail_tip")) { bones.get("tail_tip").zRot += (float)Math.sin(age*0.075F-0.7F)*0.15F; }
    }
    @Override public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer buffer,int light,int overlay,int color) { root.render(pose,buffer,light,overlay,color); }
}
