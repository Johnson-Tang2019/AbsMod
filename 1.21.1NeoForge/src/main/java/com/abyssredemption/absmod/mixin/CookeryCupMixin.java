package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"com.github.ysbbbbbb.kaleidoscopecookery.block.drink.EmptyCupBlock","com.github.ysbbbbbb.kaleidoscopecookery.block.drink.TeacupBlock"},remap=false)
abstract class CookeryCupMixin {
    @Inject(method="useItemOn",at=@At("HEAD"),cancellable=true)
    private void absmod$keepStackUniform(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit, CallbackInfoReturnable<ItemInteractionResult> callback) {
        if (stack.is(CookeryFinish.CUPS) && CookeryFinish.isRose(stack)!=CookeryFinish.isRose(state))
            callback.setReturnValue(ItemInteractionResult.CONSUME);
    }
    @ModifyArg(method="useItemOn",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"),index=1)
    private BlockState absmod$pour(BlockState result,@Local(argsOnly=true) BlockState previous) {
        return CookeryFinish.isRose(previous) && result.hasProperty(CookeryFinish.ROSE) ? result.setValue(CookeryFinish.ROSE,true) : result;
    }
    @ModifyExpressionValue(method="useItemOn",at=@At(value="NEW",target="net/minecraft/world/item/ItemStack"))
    private ItemStack absmod$takeCup(ItemStack cup,@Local(argsOnly=true) BlockState previous) {
        return CookeryFinish.isRose(previous) ? CookeryFinish.apply(cup) : cup;
    }
}
