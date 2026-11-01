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
        try {
            var player=(Player)(Object)this;
            if(!player.level().isClientSide() && (player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem
                || player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem)
                && !BladeAttackScope.fullStrength(this))return;
            original.call(target);
        } finally { BladeAttackScope.exitAttack(); }
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void absmod$fullStrength(float partial, CallbackInfoReturnable<Float> callback) {
        if (BladeAttackScope.fullStrength(this)) { callback.setReturnValue(1.0F); }
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method="attack",at=@At("STORE"),ordinal=0)
    private DamageSource absmod$bindPrimarySource(DamageSource source) {
        BladeAttackScope.bindSource(this,source);
        return source;
    }
}
