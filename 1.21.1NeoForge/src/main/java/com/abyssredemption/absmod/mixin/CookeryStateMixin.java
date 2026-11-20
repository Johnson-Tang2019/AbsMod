package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
abstract class CookeryStateMixin {
    @ModifyVariable(method = "registerDefaultState", at = @At("HEAD"), argsOnly = true)
    private BlockState absmod$defaultFinish(BlockState state) {
        return state.hasProperty(CookeryFinish.ROSE) ? state.setValue(CookeryFinish.ROSE, false) : state;
    }
}
