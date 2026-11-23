package com.abyssredemption.absmod.compat;

import com.abyssredemption.absmod.neoforge.CherryLantern;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class CherryLanternRenderer implements ICurioRenderer {
    public static void register() {
        CuriosRendererRegistry.register(CherryLantern.LANTERN.get(), CherryLanternRenderer::new);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slot,
            PoseStack pose, RenderLayerParent<T, M> parent, MultiBufferSource buffer, int light,
            float stride, float amplitude, float partialTicks, float age, float headYaw, float headPitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> model)) return;
        pose.pushPose();
        model.body.translateAndRotate(pose);
        pose.translate(0.295, 0.635, -0.16);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.XP.rotationDegrees((float) Math.sin(stride) * amplitude * 10));
        pose.translate(0, -0.23, 0);
        pose.scale(0.52F, 0.52F, 0.52F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, pose, buffer, slot.entity().level(), slot.entity().getId());
        pose.popPose();
    }
}
