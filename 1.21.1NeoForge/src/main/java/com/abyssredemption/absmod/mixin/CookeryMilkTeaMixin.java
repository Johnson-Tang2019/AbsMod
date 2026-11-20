package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets="com.github.ysbbbbbb.kaleidoscopecookery.block.drink.ClayPotMilkTeaBlock",remap=false)
abstract class CookeryMilkTeaMixin {
    @ModifyExpressionValue(method="useWithoutItem",at=@At(value="NEW",target="net/minecraft/world/item/ItemStack"))
    private ItemStack absmod$takePot(ItemStack pot,@Local(argsOnly=true) BlockState state) {
        return CookeryFinish.isRose(state) ? CookeryFinish.apply(pot) : pot;
    }
}
