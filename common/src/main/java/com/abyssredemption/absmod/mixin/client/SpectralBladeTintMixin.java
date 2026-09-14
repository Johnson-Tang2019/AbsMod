package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.item.SpectralBladeItem;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tint the original sprite at draw time; supplied sword artwork remains unchanged. */
@Mixin(Constant.class)
public abstract class SpectralBladeTintMixin {
    @Inject(method="calculate", at=@At("HEAD"), cancellable=true)
    private void spectralTint(ItemStack stack, ClientLevel level, LivingEntity owner, CallbackInfoReturnable<Integer> result) {
        if (stack.getItem() instanceof SpectralBladeItem) { result.setReturnValue(0x66FFD7EF); }
    }
}
