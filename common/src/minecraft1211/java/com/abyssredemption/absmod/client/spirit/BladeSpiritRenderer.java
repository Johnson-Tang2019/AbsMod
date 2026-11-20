package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.resources.ResourceLocation;

/** Recreated by EntityRenderDispatcher on every resource reload on both loaders. */
public final class BladeSpiritRenderer extends MobRenderer<BladeSpiritEntity, BedrockSpiritModel> {
    private final ResourceLocation texture;
    public BladeSpiritRenderer(EntityRendererProvider.Context context) {
        this(context, SpiritModelResources.load(context.getResourceManager()));
    }
    private BladeSpiritRenderer(EntityRendererProvider.Context context, SpiritModelResources.Loaded loaded) {
        super(context, loaded.model(), 0.35F);
        texture = loaded.texture();
        addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<>(this) {
            @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers,
                    int light, BladeSpiritEntity spirit, float swing, float amount, float tick, float age, float yaw, float pitch) {
                var accessory = spirit.adornment().stack();
                if (accessory.isEmpty() || spirit.isInvisible()) return;
                pose.pushPose();
                getParentModel().positionAdornment(pose);
                context.getItemRenderer().renderStatic(accessory, net.minecraft.world.item.ItemDisplayContext.FIXED,
                        light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, pose, buffers, spirit.level(), spirit.getId());
                pose.popPose();
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(BladeSpiritEntity entity) { return texture; }
}
