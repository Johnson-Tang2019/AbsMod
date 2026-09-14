package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.bedrock.client.BedrockGeometry;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Cosmetic animation of a free bone hierarchy, independent of HumanoidModel. */
public final class BedrockSpiritModel extends EntityModel<LivingEntityRenderState> {
    private final Map<String, ModelPart> bones;
    private final boolean nurseFox;
    public BedrockSpiritModel(BedrockGeometry.Baked baked) {
        super(baked.root());
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
    @Override public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (nurseFox) {
            // Preserve all authored geometry; select the normal humanoid appearance.
            for (String name : new String[]{"FOX","Backgrounds","Expression","Mouth_surprise","Mouth_smile","Mouth_smile2"}) {
                bones.get(name).visible = false;
            }
            var root = bones.get("GuiRoot");
            root.xScale = root.yScale = root.zScale = 0.6F;
        }
        bones.get("head").yRot += (float)Math.toRadians(Math.clamp(state.yRot,-70,70));
        bones.get("head").xRot += (float)Math.toRadians(Math.clamp(state.xRot,-45,45));
        float step=(float)Math.cos(state.walkAnimationPos*0.6662F)*Math.clamp(state.walkAnimationSpeed,0,1)*0.7F;
        bones.get("left_leg").xRot += step;
        bones.get("right_leg").xRot -= step;
        bones.get("left_arm").xRot -= step;
        bones.get("right_arm").xRot += step;
        bones.get("tail").yRot += (float)Math.sin(state.ageInTicks*0.075F)*0.25F;
        if (bones.containsKey("tail_tip")) { bones.get("tail_tip").zRot += (float)Math.sin(state.ageInTicks*0.075F-0.7F)*0.15F; }
    }
}
