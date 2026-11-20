package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.block.MilkshakeTrayBlock;
import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;

public final class MilkshakeTrayRenderer implements BlockEntityRenderer<MilkshakeTrayBlockEntity> {
    private final ItemRenderer items;
    public MilkshakeTrayRenderer(BlockEntityRendererProvider.Context context) { items = context.getItemRenderer(); }

    @Override public void render(MilkshakeTrayBlockEntity tray, float tick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        for (int slot = 0; slot < MilkshakeTrayBlockEntity.CUP_COUNT; slot++) {
            var cup = tray.cup(slot);
            if (cup.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(MilkshakeTrayBlock.cupX(slot), 0.181, MilkshakeTrayBlock.cupZ(slot));
            pose.mulPose(Axis.XP.rotationDegrees(-65));
            pose.scale(0.36F, 0.36F, 0.36F);
            items.renderStatic(cup, ItemDisplayContext.FIXED, light, overlay, pose, buffers, tray.getLevel(), slot);
            pose.popPose();
        }
    }
}
