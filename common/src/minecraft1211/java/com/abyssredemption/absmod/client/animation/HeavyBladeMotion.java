package com.abyssredemption.absmod.client.animation;

import com.abyssredemption.absmod.client.animation.BladeMotionPreset.Pose;
import com.abyssredemption.absmod.client.animation.BladeMotionPreset.Transform;
import java.util.HashMap;
import java.util.Map;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Original downstroke sampled from the confirmed swing tick. */
public final class HeavyBladeMotion {
    public static final float EFFECT_ROLL = -64;
    private static final Map<String, Transform> RAISED = frame(
            blade(-12, 16, 3, 30, 12, -15),
            bone(149, 12, -24), bone(44, 16, 14), bone(-10, 19, 0));
    private static final Map<String, Transform> STRUCK = frame(
            blade(-3, 2, 14, -45, 14, 19),
            bone(58, -15, -10), bone(37, -12, 8), bone(16, -15, 0));
    private static final Map<String, Transform> LOWERED = frame(
            blade(-12, -1, 6, -18, 8, -38),
            bone(23, 5, -9), bone(15, 0, 12), bone(5, 0, 0));

    private HeavyBladeMotion() {}

    static Pose sample(float elapsed) {
        float progress = elapsed / 2.5F;
        Map<String, Transform> from = RAISED;
        Map<String, Transform> to = STRUCK;
        if (progress > 1) {
            progress = Math.clamp((elapsed - 2.5F) / 5, 0, 1);
            from = STRUCK;
            to = LOWERED;
        }
        float blend = progress * progress * (3 - 2 * progress);
        Map<String, Transform> bones = new HashMap<>();
        for (var entry : from.entrySet()) {
            bones.put(entry.getKey(), entry.getValue().blend(to.get(entry.getKey()), blend));
        }
        float weight = 1 - Math.clamp((elapsed - 4) / 5, 0, 1);
        return new Pose(Map.copyOf(bones), weight, EFFECT_ROLL);
    }

    private static Map<String, Transform> frame(Transform blade, Transform rightArm, Transform leftArm, Transform torso) {
        return Map.of("blade", blade, "body", bone(0, 0, 0), "torso", torso, "head", bone(0, 0, 0),
                "right arm", rightArm, "left arm", leftArm);
    }

    private static Transform blade(float x, float y, float z, float pitch, float yaw, float roll) {
        return new Transform(new Vector3f(x, y, z), rotation(pitch, yaw, roll));
    }

    private static Transform bone(float pitch, float yaw, float roll) {
        return new Transform(new Vector3f(), rotation(pitch, yaw, roll));
    }

    private static Quaternionf rotation(float pitch, float yaw, float roll) {
        return new Quaternionf().rotationZYX((float) Math.toRadians(roll),
                (float) Math.toRadians(yaw), (float) Math.toRadians(pitch));
    }
}
