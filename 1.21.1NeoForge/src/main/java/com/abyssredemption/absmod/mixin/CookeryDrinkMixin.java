package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets="com.github.ysbbbbbb.kaleidoscopecookery.item.TeacupItem",remap=false)
abstract class CookeryDrinkMixin {
    @Inject(method="finishUsingItem",at=@At("HEAD"))
    private void absmod$rememberCup(ItemStack stack, Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> callback,
            @Share("roseCup") LocalBooleanRef rose) { rose.set(CookeryFinish.isRose(stack)); }

    @WrapOperation(method="finishUsingItem",at=@At(value="INVOKE",target="Lcom/github/ysbbbbbb/kaleidoscopecookery/item/TeacupItem;returnContainerToEntity(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack absmod$returnCup(@Coerce Object item, ItemStack remainder, Level level, LivingEntity user,
            Operation<ItemStack> original,@Share("roseCup") LocalBooleanRef rose) {
        if (!rose.get()) return original.call(item,remainder,level,user);
        var cup=CookeryFinish.apply(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("kaleidoscope_cookery:empty_cup"))));
        if(remainder.isEmpty()) return cup;
        if (!level.isClientSide) {
            if(user instanceof Player p) ItemHandlerHelper.giveItemToPlayer(p,cup);
            else user.spawnAtLocation(cup);
        }
        return remainder;
    }
}
