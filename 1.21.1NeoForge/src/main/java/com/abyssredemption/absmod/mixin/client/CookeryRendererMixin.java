package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.item.CookeryFinish;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class CookeryRendererMixin {
    @Unique private static final Map<RenderType, RenderType> absmod$finishes = new IdentityHashMap<>();
    @WrapOperation(method = "setupAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private static <T extends BlockEntity> void absmod$finish(BlockEntityRenderer<T> renderer, T entity, float time,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay, Operation<Void> render) {
        MultiBufferSource selected = buffers;
        if (CookeryFinish.isRose(entity.getBlockState())) {
            if (absmod$finishes.isEmpty()) {
                for (String part : new String[]{"millstone", "teapot", "trash_can"}) {
                    String path = "textures/block/" + part + ".png";
                    absmod$finishes.put(RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", path)),
                            RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("absmod", "textures/cookery/block/" + part + ".png")));
                }
            }
            selected = type -> buffers.getBuffer(absmod$finishes.getOrDefault(type, type));
        }
        render.call(renderer, entity, time, pose, selected, light, overlay);
    }
}
