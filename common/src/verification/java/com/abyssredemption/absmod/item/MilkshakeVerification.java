package com.abyssredemption.absmod.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/** Regression checks for the effect durations and levels in the original MilkShakeMod. */
public final class MilkshakeVerification {
    public static void verify() {
        for (MilkshakeVariant variant : MilkshakeVariant.values()) {
            requireEffect(variant, MobEffects.HEALTH_BOOST, 2400, 1);
        }
        requireEffect(MilkshakeVariant.GOLD, MobEffects.FIRE_RESISTANCE, 6000, 1);
        requireEffect(MilkshakeVariant.GOLD, MobEffects.REGENERATION, 400, 1);
        requireEffect(MilkshakeVariant.GOLD, MobEffects.RESISTANCE, 6000, 1);
        requireEffect(MilkshakeVariant.GOLD, MobEffects.ABSORPTION, 2400, 3);
        requireEffect(MilkshakeVariant.PINK, MobEffects.SPEED, 6000, 3);
        int[] expectedCounts = {1, 1, 5, 2};
        for (MilkshakeVariant variant : MilkshakeVariant.values()) {
            if (variant.effects().size() != expectedCounts[variant.ordinal()]) {
                throw new AssertionError("Unexpected effects for " + variant);
            }
        }
        System.out.println("PASS: original milkshake effect durations, amplifiers and counts");
    }

    private static void requireEffect(MilkshakeVariant variant, Holder<MobEffect> effect,
            int ticks, int amplifier) {
        boolean matches = variant.effects().stream().anyMatch(instance ->
                instance.getEffect().equals(effect) && instance.getDuration() == ticks
                        && instance.getAmplifier() == amplifier);
        if (!matches) {
            throw new AssertionError("Milkshake effect mismatch: " + variant + " / " + effect);
        }
    }
}
