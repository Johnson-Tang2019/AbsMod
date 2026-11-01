package com.abyssredemption.absmod.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class SpiritEntityType {
    public static final ResourceKey<EntityType<?>> KEY = ResourceKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("absmod", "blade_spirit"));
    private SpiritEntityType() {}
    public static EntityType<BladeSpiritEntity> create() {
        return EntityType.Builder.of(BladeSpiritEntity::new, MobCategory.CREATURE).sized(0.6F, 1.8F)
                .clientTrackingRange(8).build(KEY.location().toString());
    }
    @SuppressWarnings("unchecked")
    public static EntityType<BladeSpiritEntity> get() {
        return (EntityType<BladeSpiritEntity>) BuiltInRegistries.ENTITY_TYPE.get(KEY.location());
    }
}
