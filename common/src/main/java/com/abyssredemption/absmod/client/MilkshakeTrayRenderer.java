package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.block.MilkshakeTrayBlock;
import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class MilkshakeTrayRenderer implements BlockEntityRenderer<MilkshakeTrayBlockEntity, MilkshakeTrayRenderer.State> {
    private final ItemModelResolver resolver;

    public MilkshakeTrayRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }

    public static final class State extends BlockEntityRenderState {
        private final ItemStackRenderState[] cups = new ItemStackRenderState[MilkshakeTrayBlockEntity.CUP_COUNT];

        State() {
            for (int slot = 0; slot < cups.length; slot++) {
                cups[slot] = new ItemStackRenderState();
            }
        }
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(MilkshakeTrayBlockEntity tray, State state, float tick, Vec3 camera,
                                              ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(tray, state, tick, camera, overlay);
        for (int slot = 0; slot < state.cups.length; slot++) {
            resolver.updateForTopItem(state.cups[slot], tray.cup(slot), ItemDisplayContext.FIXED, tray.getLevel(), null, slot);
        }
    }

    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < state.cups.length; slot++) {
            var cup = state.cups[slot];
            if (cup.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(MilkshakeTrayBlock.cupX(slot), 0.181, MilkshakeTrayBlock.cupZ(slot));
            pose.mulPose(Axis.XP.rotationDegrees(-65));
            pose.scale(0.36F, 0.36F, 0.36F);
            cup.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
