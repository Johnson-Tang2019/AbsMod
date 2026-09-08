package com.abyssredemption.absmod.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public enum MilkshakeVariant {
    PLAIN("milkshake"),
    CHORUS("chorus_milkshake"),
    GOLD("gold_milkshake"),
    PINK("pink_milkshake");

    private final String itemId;

    MilkshakeVariant(String itemId) {
        this.itemId = itemId;
    }

    public String itemId() {
        return itemId;
    }

    public List<MobEffectInstance> effects() {
        List<MobEffectInstance> effects = new ArrayList<>();
        effects.add(new MobEffectInstance(MobEffects.HEALTH_BOOST, 2400, 1));
        if (this == GOLD) {
            effects.add(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 1));
            effects.add(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
            effects.add(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 1));
            effects.add(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
        } else if (this == PINK) {
            effects.add(new MobEffectInstance(MobEffects.SPEED, 6000, 3));
        }
        return List.copyOf(effects);
    }
}
