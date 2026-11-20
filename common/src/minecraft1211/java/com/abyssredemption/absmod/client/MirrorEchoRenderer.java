package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.entity.MirrorEchoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class MirrorEchoRenderer extends EntityRenderer<MirrorEchoEntity> {
    private final net.minecraft.client.renderer.entity.ItemRenderer items;
    public MirrorEchoRenderer(EntityRendererProvider.Context context) { super(context); items=context.getItemRenderer(); }
    @Override public ResourceLocation getTextureLocation(MirrorEchoEntity e) { return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(MirrorEchoEntity e,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light) {
        float age=e.tickCount+partial;
        poses.pushPose();
        poses.translate(0,0.9+Math.sin(age*0.12)*0.09,0);
        poses.mulPose(entityRenderDispatcher.cameraOrientation());
        poses.pushPose();
        float beat = e.phaseAge(partial);
        float turn;
        if (e.phase() == 0) {
            turn = -40 + 26 * Math.clamp(beat / 20, 0, 1) + 125 * Math.clamp((beat-22)/5, 0, 1);
        } else if (e.phase() == 1) {
            turn = 135;
            poses.translate(0,0.6 * Math.clamp(beat / 20, 0, 1) - 1.1 * Math.clamp((beat-25)/4, 0, 1),0);
        } else {
            turn = -25;
        }
        poses.mulPose(Axis.ZP.rotationDegrees(turn));
        poses.scale(1.7F,1.7F,1.7F);
        items.renderStatic(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:dark_meow_blade_stage_8"))),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,poses,buffers,e.level(),0);
        poses.popPose();
        for (int i=0;i<3;i++) {
            poses.pushPose();
            poses.mulPose(Axis.YP.rotationDegrees(age*2+i*120));
            poses.translate(e.phase()==2 ? 0.86 : 0.5,0,0);
            poses.scale(0.46F,0.46F,0.46F);
            items.renderStatic(new ItemStack(Items.AMETHYST_SHARD),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,poses,buffers,e.level(),i);
            poses.popPose();
        }
        poses.popPose();
        super.render(e,yaw,partial,poses,buffers,light);
    }
}
