package com.abyssredemption.absmod.combat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Uses the current server-side aim and normal melee pipeline; never searches off the aim ray. */
public final class BladeAttackExecutor {
    private BladeAttackExecutor() {}

    public static HitResult pick(ServerPlayer player) {
        double range=player.entityInteractionRange();
        var start=player.getEyePosition();var end=start.add(player.getViewVector(1).scale(range));
        var block=player.level().clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
        double maxDistance=block.getType()==HitResult.Type.MISS?range*range:start.distanceToSqr(block.getLocation());
        var entity=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player,start,end,player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),target->target!=player && !target.isSpectator() && target.isPickable(),maxDistance);
        return entity==null?block:entity;
    }

    public static boolean attack(ServerPlayer player, HitResult hit) {
        player.resetLastActionTime();
        if (!(hit instanceof EntityHitResult entityHit)) { player.resetAttackStrengthTicker(); return false; }
        Entity target = entityHit.getEntity();
        if (!target.isAlive() || !target.isAttackable() || target instanceof ItemEntity || target instanceof ExperienceOrb
                || target.level() != player.level() || !player.hasLineOfSight(target)
                || !player.level().getWorldBorder().isWithinBounds(target.blockPosition())
                || !player.canInteractWithEntity(target,0)) { return false; }
        return BladeAttackScope.runTracked(player, target, () -> player.attack(target));
    }
}
