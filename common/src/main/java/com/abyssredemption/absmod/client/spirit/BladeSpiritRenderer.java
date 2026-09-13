package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Recreated by EntityRenderDispatcher on every resource reload on both loaders. */
public final class BladeSpiritRenderer extends MobRenderer<BladeSpiritEntity, LivingEntityRenderState, BedrockSpiritModel> {
    private final Identifier texture;
    public BladeSpiritRenderer(EntityRendererProvider.Context context) {
        this(context, SpiritModelResources.load(context.getResourceManager()));
    }
    private BladeSpiritRenderer(EntityRendererProvider.Context context, SpiritModelResources.Loaded loaded) {
        super(context, loaded.model(), 0.35F);
        texture = loaded.texture();
    }
    @Override public LivingEntityRenderState createRenderState() { return new LivingEntityRenderState(); }
    @Override public Identifier getTextureLocation(LivingEntityRenderState state) { return texture; }
}
