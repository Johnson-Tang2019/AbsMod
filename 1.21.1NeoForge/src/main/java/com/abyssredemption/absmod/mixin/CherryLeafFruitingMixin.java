package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.registry.CherryOrchard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LeavesBlock.class)
abstract class CherryLeafFruitingMixin {
    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void absmod$cherryTicks(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (state.is(Blocks.CHERRY_LEAVES) && !state.getValue(LeavesBlock.PERSISTENT)) callback.setReturnValue(true);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void absmod$flower(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo callback) {
        if (!state.is(Blocks.CHERRY_LEAVES) || state.getValue(LeavesBlock.PERSISTENT)
                || state.getValue(LeavesBlock.DISTANCE) == 7 || (Mth.getSeed(pos) & 7) != 0) return;
        BlockPos fruit = pos.below();
        if (level.isEmptyBlock(fruit) && level.getRawBrightness(fruit, 0) >= 9) {
            level.setBlockAndUpdate(fruit, CherryOrchard.CLUSTER.get().defaultBlockState());
        }
    }
}
