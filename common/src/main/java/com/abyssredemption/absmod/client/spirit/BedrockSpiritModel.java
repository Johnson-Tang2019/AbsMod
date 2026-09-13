package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.bedrock.client.BedrockGeometry;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Cosmetic animation of a free bone hierarchy, independent of HumanoidModel. */
public final class BedrockSpiritModel extends EntityModel<LivingEntityRenderState> {
    private final Map<String, ModelPart> bones;
    public BedrockSpiritModel(BedrockGeometry.Baked baked) {
        super(baked.root());
        bones = baked.bones();
        for (String name : new String[]{"head","body","left_arm","right_arm","left_leg","right_leg","left_ear","right_ear","tail"}) {
            if (!bones.containsKey(name)) { throw new IllegalArgumentException("Required spirit bone missing: " + name); }
        }
    }
    @Override public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
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
