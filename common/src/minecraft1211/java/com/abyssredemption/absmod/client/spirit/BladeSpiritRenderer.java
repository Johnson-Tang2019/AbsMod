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
    }
    @Override public ResourceLocation getTextureLocation(BladeSpiritEntity entity) { return texture; }
}
