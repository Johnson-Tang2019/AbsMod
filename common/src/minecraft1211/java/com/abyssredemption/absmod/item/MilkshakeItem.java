package com.abyssredemption.absmod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Rarity;


import net.minecraft.world.level.Level;

public final class MilkshakeItem extends Item {
    private final MilkshakeVariant variant;

    public MilkshakeItem(Properties properties, MilkshakeVariant variant) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON).craftRemainder(Items.BUCKET).food(food(variant)));
        this.variant = variant;
    }

    private static FoodProperties food(MilkshakeVariant variant) {
        var builder=new FoodProperties.Builder().nutrition(6).saturationModifier(1).alwaysEdible().usingConvertsTo(Items.BUCKET);
        for(var effect:variant.effects())builder.effect(()->new net.minecraft.world.effect.MobEffectInstance(effect),1);
        return builder.build();
    }
    @Override public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack){return net.minecraft.world.item.UseAnim.DRINK;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 60;}
    @Override public net.minecraft.sounds.SoundEvent getDrinkingSound(){return SoundEvents.HONEY_DRINK;}

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (variant == MilkshakeVariant.CHORUS && entity instanceof ServerPlayer player) {
            teleportRandomly(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private static void teleportRandomly(ServerPlayer player) {
        double originX = player.getX();
        double originY = player.getY();
        double originZ = player.getZ();
        spawnTeleportParticles(player);
        // Preserve the original fifteen attempts within eight blocks on each axis.
        for (int attempt = 0; attempt < 15; attempt++) {
            double x = originX + (player.getRandom().nextDouble() - 0.5) * 16.0;
            double y = originY + player.getRandom().nextInt(16) - 8;
            double z = originZ + (player.getRandom().nextDouble() - 0.5) * 16.0;
            if (player.randomTeleport(x, y, z, true)) {
                playTeleportSound(player, 1.0F);
                spawnTeleportParticles(player);
                return;
            }
        }
        playTeleportSound(player, 0.5F);
    }

    private static void playTeleportSound(ServerPlayer player, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void spawnTeleportParticles(ServerPlayer player) {
        player.serverLevel().sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(), 50,
                player.getBbWidth() * 0.5, player.getBbHeight() * 0.25, player.getBbWidth() * 0.5, 0.1);
    }
}
