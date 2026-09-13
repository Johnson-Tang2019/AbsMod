package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.combat.BladeAttackScope;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @WrapMethod(method = "attack")
    private void absmod$attackScope(Entity target, Operation<Void> original) {
        BladeAttackScope.enterAttack();
        try { original.call(target); } finally { BladeAttackScope.exitAttack(); }
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void absmod$fullStrength(float partial, CallbackInfoReturnable<Float> callback) {
        if (BladeAttackScope.fullStrength(this)) { callback.setReturnValue(1.0F); }
    }

    @Inject(method = "createAttackSource", at = @At("RETURN"))
    private void absmod$bindPrimarySource(ItemStack stack, CallbackInfoReturnable<DamageSource> callback) {
        BladeAttackScope.bindSource(this, callback.getReturnValue());
    }
}
