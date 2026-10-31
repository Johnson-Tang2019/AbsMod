package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A broken soul blade retains its identity and can be repaired instead of disappearing. */
@Mixin(ItemStack.class)
public abstract class BladeDurabilityMixin {
    @Group(name="retainBlade", min=1)
    @Inject(method="applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",at=@At("HEAD"),cancellable=true,require=0)
    private void retainBrokenBlade(int damage,LivingEntity owner,Consumer<Item> callback,CallbackInfo ci) {
        var stack=(ItemStack)(Object)this;
        if(stack.getItem() instanceof MeowBladeItem && damage>=stack.getMaxDamage()) {
            stack.setDamageValue(stack.getMaxDamage());ci.cancel();
        }
    }

    @Group(name="retainBlade", min=1)
    @Inject(method="applyDamage(ILnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",at=@At("HEAD"),cancellable=true,require=0)
    private void retainVanillaBrokenBlade(int damage, ServerPlayer owner, Consumer<Item> callback, CallbackInfo ci) {
        retainBrokenBlade(damage, owner, callback, ci);
    }
}
