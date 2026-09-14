package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.client.animation.BladeMotionPreset;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Verifies actual imported resources, continuous sampling and snapshot isolation. */
public final class BladeMotionVerification {
    public static void verify() throws Exception {
        try (var reader = new InputStreamReader(BladeMotionVerification.class.getResourceAsStream(
                "/assets/absmod/animations/slashblade_baseline.json"), StandardCharsets.UTF_8)) {
            var preset = BladeMotionPreset.read(reader);
            for (int sequence = 1; sequence <= 5; sequence++) {
                for (float interval : new float[] {4, 20F / 6, 2}) {
                    var start = preset.sample(sequence, 0, interval).bone("blade");
                    var end = preset.sample(sequence, interval * .8F, interval).bone("blade");
                    check(start.position().distance(end.position()) > .01F
                            || Math.abs(start.rotation().dot(end.rotation())) < .999F, "Static weapon clip");
                    for (float elapsed = 0; elapsed < 45; elapsed += .1F) {
                        var pose = preset.sample(sequence, elapsed, interval);
                        for (var transform : pose.bones().values()) {
                            check(transform.position().isFinite() && transform.rotation().isFinite(), "Non-finite pose");
                        }
                    }
                    float boundary = interval * .85F;
                    var before = preset.sample(sequence, boundary, interval).bone("blade");
                    var after = preset.sample(sequence, boundary + .0001F, interval).bone("blade");
                    check(before.position().distance(after.position()) < .01F, "Recovery snapped");
                }
                check(preset.sample(sequence, 45, 4).weight() == 0, "Recovery never ended");
            }
            var expected = preset.sample(1, 2, 4).bone("blade").position();
            preset.sample(1, 2, 4).bone("blade").position().zero();
            preset.sample(4, 3, 4);
            check(preset.sample(1, 2, 4).bone("blade").position().equals(expected), "Pose mutation leaked between players");
        }
        System.out.println("PASS: five imported weapon clips, cadence sampling, recovery continuity and pose isolation");
    }
    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
