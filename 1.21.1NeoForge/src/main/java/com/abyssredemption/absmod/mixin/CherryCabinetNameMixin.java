package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.block.CherryCabinetBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrelBlockEntity.class)
abstract class CherryCabinetNameMixin {
    @Inject(method = "getDefaultName", at = @At("HEAD"), cancellable = true)
    private void absmod$cabinetTitle(CallbackInfoReturnable<Component> callback) {
        var block = ((BarrelBlockEntity) (Object) this).getBlockState().getBlock();
        if (block instanceof CherryCabinetBlock) callback.setReturnValue(block.getName());
    }
}
