package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.client.spirit.BladeSpiritGeometry;
import com.abyssredemption.absmod.client.spirit.BladeSpiritModel;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Bakes the actual authored resource; validates geometry and animation reset without a GPU. */
public final class SpiritModelVerification {
    private SpiritModelVerification() {}
    private static float[] bounds(net.minecraft.client.model.geom.ModelPart part) {
        float[] bounds = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!path.isEmpty()) { return; }
            for (var polygon : cube.polygons) {
                for (var v : polygon.vertices()) {
                    bounds[0] = Math.min(bounds[0], v.y()); bounds[1] = Math.max(bounds[1], v.y());
                    bounds[2] = Math.min(bounds[2], v.x()); bounds[3] = Math.max(bounds[3], v.x());
                }
            }
        });
        return bounds;
    }
    public static void verify() throws Exception {
        BedrockModelVerification.verify();
        try (var input = SpiritModelVerification.class.getResourceAsStream("/assets/absmod/models/entity/blade_spirit.json");
                var reader = new InputStreamReader(java.util.Objects.requireNonNull(input), StandardCharsets.UTF_8)) {
            var root = BladeSpiritGeometry.read(reader);
            var model = new BladeSpiritModel(root);
            int[] count = {0};
            root.visit(new PoseStack(), (pose, path, index, cube) -> {
                count[0]++;
                for (var polygon : cube.polygons) {
                    if (!Float.isFinite(polygon.normal().lengthSquared()) || Math.abs(polygon.normal().lengthSquared() - 1) > 0.001) {
                        throw new AssertionError("Invalid normal: " + path);
                    }
                    for (var vertex : polygon.vertices()) {
                        if (!Float.isFinite(vertex.x() + vertex.y() + vertex.z()) || vertex.u() < 0 || vertex.u() > 1
                                || vertex.v() < 0 || vertex.v() > 1) { throw new AssertionError("Invalid geometry/UV: " + path); }
                    }
                }
            });
            if (count[0] < 1000 || count[0] > 2500) { throw new AssertionError("Unexpected curved model budget: " + count[0]); }
            var state = new HumanoidRenderState();
            state.ageInTicks = 100;
            model.setupAnim(state);
            if (Math.abs(root.getChild("head").y - 6) > 0.0001F
                    || Math.abs(root.getChild("body").y - 6) > 0.0001F
                    || Math.abs(root.getChild("left_leg").y - 15) > 0.0001F) {
                throw new AssertionError("Vanilla animation overwrote measured spirit pivots");
            }
            var headBounds = bounds(root.getChild("head"));
            var legBounds = bounds(root.getChild("left_leg"));
            if (Math.abs(headBounds[1] - headBounds[0] - 8) > 0.001F
                    || Math.abs(legBounds[1] - legBounds[0] - 9) > 0.001F
                    || Math.abs(legBounds[3] - legBounds[2] - 3) > 0.001F) {
                throw new AssertionError("Measured head/leg proportions drifted");
            }
            float first = root.getChild("body").getChild("tail").zRot;
            model.setupAnim(state);
            if (Math.abs(root.getChild("body").getChild("tail").zRot - first) > 0.0001F) {
                throw new AssertionError("Secondary animation accumulated between frames");
            }
        }
        try (var input = SpiritModelVerification.class.getResourceAsStream("/assets/absmod/models/entity/borrowed_cat_spirit.json");
                var reader = new InputStreamReader(java.util.Objects.requireNonNull(input), StandardCharsets.UTF_8)) {
            var root = BladeSpiritGeometry.read(reader);
            var borrowed = new BladeSpiritModel(root);
            var state = new HumanoidRenderState();
            state.ageInTicks = 40;
            borrowed.setupAnim(state);
            borrowed.setupAnim(state);
            if (Math.abs(root.getChild("head").y - 6) > 0.001F) {
                throw new AssertionError("Borrowed model head pivot changed");
            }
        }
        System.out.println("PASS: borrowed maid geometry and optional animation bones");
        System.out.println("PASS: floral spirit geometry, finite normals, UV bounds, required bones and animation reset (no GPU)");
    }
}
