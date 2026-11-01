package com.abyssredemption.absmod.mixin;
import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Retain the fully damaged blade after vanilla enchantment and durability processing. */
@Mixin(ItemStack.class)
public abstract class BladeDurabilityMixin {
    @Inject(method="hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;shrink(I)V"),cancellable=true)
    private void preserveBrokenBlade(CallbackInfo ci) {
        var stack=(ItemStack)(Object)this;
        if(stack.getItem() instanceof MeowBladeItem){stack.setDamageValue(stack.getMaxDamage());ci.cancel();}
    }
}
