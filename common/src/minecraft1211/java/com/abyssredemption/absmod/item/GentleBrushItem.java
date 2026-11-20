package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import com.abyssredemption.absmod.spirit.BladeAffection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.Item;

/** The brush uses existing interaction rewards and never changes the target's saved appearance. */
public final class GentleBrushItem extends Item {
    public static final int RESPONSE_TICKS = 28;

    public GentleBrushItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean canGroom(ServerPlayer player, LivingEntity target) {
        return player.isAlive() && !player.isSpectator() && target.isAlive() && player.level() == target.level()
                && player.distanceToSqr(target) <= 16;
    }

    public static InteractionResult groomSpirit(ServerPlayer player, BladeSpiritEntity spirit, InteractionHand hand) {
        if (!canGroom(player, spirit) || !player.getUUID().equals(spirit.ownerId()) || !spirit.active()) {
            notifyUnavailable(player);
            return InteractionResult.FAIL;
        }
        if (spirit.startGrooming(player)) {
            showResponse(spirit);
            BladeAdvancements.award(player, "gentle_brush");
            // A brush is a non-gift interaction, so it uses the existing daily talk budget.
            BladeAffection.interact(player, spirit, hand);
        }
        return InteractionResult.SUCCESS;
    }

    public static void respondCat(ServerPlayer player, Cat cat, SoundEvent purr) {
        cat.getLookControl().setLookAt(player, 35F, 28F);
        cat.playSound(purr, 0.65F, 1.0F);
        showResponse(cat);
        BladeAdvancements.award(player, "gentle_brush");
    }

    public static void notifyUnavailable(ServerPlayer player) {
        if (player.connection != null) {
            player.sendSystemMessage(Component.translatable("brush.absmod.unavailable"), true);
        }
    }

    private static void showResponse(LivingEntity target) {
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight() + 0.15,
                    target.getZ(), 3, 0.18, 0.09, 0.18, 0);
        }
    }
}
