package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.arena.CherryArena;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
abstract class ArenaChunkMixin {
    @Inject(method="setBlockState", at=@At("HEAD"), cancellable=true)
    private void absmod$keepArena(BlockPos pos, BlockState state, boolean moving, CallbackInfoReturnable<BlockState> ci) {
        var level = ((LevelChunk)(Object)this).getLevel();
        if (!level.isClientSide && CherryArena.locked(level)) ci.setReturnValue(null);
    }
}
