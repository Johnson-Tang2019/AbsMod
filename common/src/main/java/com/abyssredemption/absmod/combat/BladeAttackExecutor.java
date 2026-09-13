package com.abyssredemption.absmod.combat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Uses the current server-side aim and normal melee pipeline; never searches off the aim ray. */
public final class BladeAttackExecutor {
    private BladeAttackExecutor() {}

    public static HitResult pick(ServerPlayer player) {
        var range = player.getMainHandItem().getOrDefault(DataComponents.ATTACK_RANGE, AttackRange.defaultFor(player));
        return range.getClosesetHit(player, 1.0F, entity -> entity != player && !entity.isSpectator() && entity.isPickable());
    }

    public static boolean attack(ServerPlayer player, HitResult hit) {
        player.resetLastActionTime();
        if (!(hit instanceof EntityHitResult entityHit)) { player.resetAttackStrengthTicker(); return false; }
        Entity target = entityHit.getEntity();
        if (!target.isAlive() || !target.isAttackable() || target instanceof ItemEntity || target instanceof ExperienceOrb
                || target.level() != player.level() || !player.hasLineOfSight(target)
                || !player.level().getWorldBorder().isWithinBounds(target.blockPosition())
                || !player.isWithinAttackRange(player.getMainHandItem(), target.getBoundingBox(), 0)) { return false; }
        return BladeAttackScope.runTracked(player, target, () -> player.attack(target));
    }
}
