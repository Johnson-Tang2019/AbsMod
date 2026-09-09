package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.AbsMod;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class MeowSlashRenderer {
    private static final RenderType PINK_BODY = RenderType.create("absmod_meow_slash_body",
            RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "pipeline/meow_slash_body"))
                    .build()).createRenderSetup());
    // The snippet supplies depth testing without depth writes and disables face culling.
    private static final RenderType GLOW = RenderType.create("absmod_meow_slash",
            RenderSetup.builder(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "pipeline/meow_slash"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .build()).createRenderSetup());

    private MeowSlashRenderer() {}

    public static void submit(List<MeowSlashGeometry.Slash> frame, Vec3 cameraRight, Vec3 cameraUp,
                              SubmitNodeCollector collector) {
        if (frame == null || frame.isEmpty()) {
            return;
        }
        // Positions are already camera-relative. The render pipeline applies the view rotation.
        // Alpha blending anchors the pink hue even against bright sky or snow.
        collector.order(0).submitCustomGeometry(new PoseStack(), PINK_BODY, (pose, buffer) -> {
            for (var slash : frame) {
                MeowSlashGeometry.emit(slash, cameraRight, cameraUp, (x, y, z, color) ->
                        buffer.addVertex(pose, (float) x, (float) y, (float) z).setColor(color));
            }
        });
        // A separate additive pass supplies brightness without bleaching the base into white.
        collector.order(1).submitCustomGeometry(new PoseStack(), GLOW, (pose, buffer) -> {
            for (var slash : frame) {
                MeowSlashGeometry.emit(slash, cameraRight, cameraUp, (x, y, z, color) -> {
                    int alpha = Math.round((color >>> 24) * 0.65F);
                    buffer.addVertex(pose, (float) x, (float) y, (float) z)
                            .setColor((alpha << 24) | (color & 0xFFFFFF));
                });
            }
        });
    }
}
