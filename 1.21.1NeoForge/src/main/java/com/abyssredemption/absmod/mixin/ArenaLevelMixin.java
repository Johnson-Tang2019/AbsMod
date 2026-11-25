package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.arena.CherryArena;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
abstract class ArenaLevelMixin {
    // Block drops precede the chunk write in Level.destroyBlock.
    @Inject(method="destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", at=@At("HEAD"), cancellable=true)
    private void absmod$preventArenaDrops(BlockPos pos, boolean drop, Entity cause, int recursion, CallbackInfoReturnable<Boolean> ci) {
        if (CherryArena.locked((Level)(Object)this)) ci.setReturnValue(false);
    }
}
