package com.abyssredemption.absmod.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.abyssredemption.absmod.spirit.BladeAdvancements;

/** A spring warms the connected surface water directly above it and its eight neighbors. */
public final class BlossomSpringBlock extends Block {
    public BlossomSpringBlock(Properties properties) { super(properties); }

    private static boolean source(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource();
    }

    @Nullable
    public static BlockPos waterAt(LivingEntity bather) {
        if (!bather.isAlive() || bather.isSpectator() || !bather.isInWater()) return null;
        var pos = bather.blockPosition();
        return source(bather.level(), pos) ? pos : null;
    }

    private static boolean connected(Level level, BlockPos from, BlockPos center) {
        int dx = center.getX() - from.getX();
        int dz = center.getZ() - from.getZ();
        return from.getY() == center.getY() && Math.abs(dx) <= 1 && Math.abs(dz) <= 1
                && (dx == 0 || dz == 0 || source(level, from.offset(dx, 0, 0)) || source(level, from.offset(0, 0, dz)));
    }

    @Nullable
    public static BlockPos springFor(Level level, BlockPos water, BlockPos companionWater) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            var center = water.offset(dx, 0, dz);
            if (source(level, center) && level.getBlockState(center.below()).getBlock() instanceof BlossomSpringBlock
                    && connected(level, water, center) && connected(level, companionWater, center)) return center;
        }
        return null;
    }

    public static void bathe(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        var water = waterAt(player);
        if (water == null) return;
        var spring = springFor(player.level(), water, water);
        if (spring == null) return;
        if (player.serverLevel().isEmptyBlock(spring.above())) {
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, spring.getX() + .5, spring.getY() + 1.04,
                    spring.getZ() + .5, 2, .5, .06, .5, .009);
        }
        warm(player, MobEffects.REGENERATION, 100);
        warm(player, MobEffects.DAMAGE_RESISTANCE, 300);
        BladeAdvancements.award(player, "blossom_bath");
    }

    private static void warm(ServerPlayer player, Holder<MobEffect> effect, int duration) {
        if (!player.hasEffect(effect)) player.addEffect(new MobEffectInstance(effect, duration, 0, true, false, true));
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!source(level, pos.above()) || !level.isEmptyBlock(pos.above(2))) return;
        double x = pos.getX() + random.nextDouble(), y = pos.getY() + 1.94, z = pos.getZ() + random.nextDouble();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.CLOUD, x + (random.nextDouble() - .5) * .55, y + i * .12,
                    z + (random.nextDouble() - .5) * .55, 0, .018 + random.nextDouble() * .018, 0);
        }
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.CHERRY_LEAVES, x, y + .3, z, 0, 0, 0);
    }
}
