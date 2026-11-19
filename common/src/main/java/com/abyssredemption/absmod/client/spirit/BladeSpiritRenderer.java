package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Recreated by EntityRenderDispatcher on every resource reload on both loaders. */
public final class BladeSpiritRenderer extends MobRenderer<BladeSpiritEntity, LivingEntityRenderState, BedrockSpiritModel> {
    private final Identifier texture;
    private final net.minecraft.client.renderer.item.ItemModelResolver items;
    public BladeSpiritRenderer(EntityRendererProvider.Context context) {
        this(context, SpiritModelResources.load(context.getResourceManager()));
    }
    private BladeSpiritRenderer(EntityRendererProvider.Context context, SpiritModelResources.Loaded loaded) {
        super(context, loaded.model(), 0.35F);
        texture = loaded.texture();
        items = context.getItemModelResolver();
        addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<>(this) {
            @Override public void submit(com.mojang.blaze3d.vertex.PoseStack poses,
                    net.minecraft.client.renderer.SubmitNodeCollector collector, int light,
                    LivingEntityRenderState state, float yRot, float xRot) {
                var adornment = ((BladeSpiritRenderState) state).adornment;
                if (adornment.isEmpty() || state.isInvisibleToPlayer) {
                    return;
                }
                poses.pushPose();
                getParentModel().positionAdornment(poses, state);
                adornment.submit(poses, collector, light, getOverlayCoords(state, 0), state.outlineColor);
                poses.popPose();
            }
        });
    }
    @Override public LivingEntityRenderState createRenderState() { return new BladeSpiritRenderState(); }

    @Override public void extractRenderState(BladeSpiritEntity spirit, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(spirit, state, partialTick);
        ((BladeSpiritRenderState) state).groomingProgress = spirit.groomingProgress(partialTick);
        items.updateForLiving(((BladeSpiritRenderState) state).adornment, spirit.adornment().stack(),
                net.minecraft.world.item.ItemDisplayContext.FIXED, spirit);
    }

    @Override public Identifier getTextureLocation(LivingEntityRenderState state) { return texture; }
}
