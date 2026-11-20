package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.entity.PinkDisplayFrame;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameRenderer.class)
abstract class PinkDisplayFrameModelMixin {
    @Unique private static final ModelResourceLocation absmod$rim =
            ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath("absmod", "pink_display_frame"));

    @Inject(method = "getFrameModelResourceLoc", at = @At("HEAD"), cancellable = true)
    private void absmod$rimModel(ItemFrame frame, ItemStack contents, CallbackInfoReturnable<ModelResourceLocation> callback) {
        if (frame instanceof PinkDisplayFrame) callback.setReturnValue(absmod$rim);
    }
}
