package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Combining two blades would discard a soul; use diamond repair or Mending instead. */
@Mixin(RepairItemRecipe.class)
public abstract class BladeCraftRepairMixin {
    @Inject(method="canCombine",at=@At("HEAD"),cancellable=true)
    private static void preserveSouls(ItemStack first,ItemStack second,CallbackInfoReturnable<Boolean> callback) {
        if(first.getItem() instanceof MeowBladeItem || second.getItem() instanceof MeowBladeItem)callback.setReturnValue(false);
    }
}
