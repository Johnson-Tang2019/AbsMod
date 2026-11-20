package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IBlockStateExtension.class)
interface CookeryPickMixin {
    @ModifyReturnValue(method = "getCloneItemStack", at = @At("RETURN"))
    private ItemStack absmod$pickFinish(ItemStack original) {
        if ((Object) this instanceof BlockState state && CookeryFinish.isRose(state) && original.is(CookeryFinish.UTENSILS)) {
            return CookeryFinish.apply(original.copy());
        }
        return original;
    }
}
