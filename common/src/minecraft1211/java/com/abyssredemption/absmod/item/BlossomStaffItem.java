package com.abyssredemption.absmod.item;

import java.util.Comparator;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class BlossomStaffItem extends Item {
    private static final TagKey<Item> STAVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("absmod", "blossom_staves"));
    private static final DustParticleOptions BLOSSOM = new DustParticleOptions(new Vector3f(1F, 0.62F, 0.79F), 0.9F);
    private static final DustParticleOptions DUSK = new DustParticleOptions(new Vector3f(0.49F, 0.18F, 0.53F), 1.1F);
    private final BlossomWeaponStage stage;

    public BlossomStaffItem(Properties properties, BlossomWeaponStage stage) {
        super(properties.durability(768));
        this.stage = stage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var staff = player.getItemInHand(hand);
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(staff);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer caster) {
            cast(server, caster);
            level.registryAccess().registryOrThrow(Registries.ITEM).getTagOrEmpty(STAVES)
                    .forEach(item -> player.getCooldowns().addCooldown(item.value(), stage.spellRecovery()));
            staff.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(staff, level.isClientSide());
    }

    private void cast(ServerLevel level, ServerPlayer caster) {
        Vec3 start = caster.getEyePosition();
        Vec3 limit = start.add(caster.getLookAngle().scale(16 + stage.number()));
        Vec3 end = level.clip(new ClipContext(start, limit, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
        var hit = ProjectileUtil.getEntityHitResult(caster, start, end, new AABB(start, end).inflate(1),
                entity -> entity instanceof LivingEntity living && living.isAlive() && living.isPickable() && !living.isSpectator(),
                start.distanceToSqr(end));
        if (hit != null) {
            end = hit.getLocation();
            var target = (LivingEntity)hit.getEntity();
            if (canHarm(caster, target) && target.hurt(level.damageSources().indirectMagic(caster, caster), stage.spellDamage())
                    && stage.dark()) darkBloom(level, caster, target);
        }
        var trail = stage.dark() ? DUSK : BLOSSOM;
        Vec3 direction = end.subtract(start).normalize();
        for (double d = 0.6; d < start.distanceTo(end); d += 0.8) {
            Vec3 point = start.add(direction.scale(d));
            level.sendParticles(trail, point.x, point.y, point.z, 1, 0.025, 0.025, 0.025, 0);
        }
        ParticleOptions petals = stage.dark() ? ParticleTypes.WITCH : ParticleTypes.CHERRY_LEAVES;
        level.sendParticles(petals, end.x, end.y, end.z, 6, 0.18, 0.18, 0.18, 0);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                0.85F, stage.dark() ? 0.65F : 1.35F);
    }

    private boolean canHarm(ServerPlayer caster, LivingEntity target) {
        if (target == caster || target.isAlliedTo(caster) || caster.isAlliedTo(target)) return false;
        if (target instanceof Player player) return caster.server.isPvpAllowed() && caster.canHarmPlayer(player);
        return !(target instanceof TamableAnimal pet && pet.isOwnedBy(caster));
    }

    private void darkBloom(ServerLevel level, ServerPlayer caster, LivingEntity center) {
        Vec3 origin = center.getBoundingBox().getCenter();
        level.getEntitiesOfClass(LivingEntity.class, center.getBoundingBox().inflate(2.5),
                        mob -> mob != center && mob instanceof Enemy && mob.isAlive() && canHarm(caster, mob)
                                && mob.distanceToSqr(center) <= 6.25)
                .stream().sorted(Comparator.comparingDouble(center::distanceToSqr))
                .filter(mob -> level.clip(new ClipContext(origin, mob.getBoundingBox().getCenter(),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getType() == HitResult.Type.MISS)
                .limit(3).forEach(mob -> {
                    if (mob.hurt(level.damageSources().indirectMagic(caster, caster), stage.spellDamage() * 0.5F)) {
                        level.sendParticles(DUSK, mob.getX(), mob.getY(0.5), mob.getZ(), 8, 0.25, 0.25, 0.25, 0);
                    }
                });
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack material) {
        return material.is(Items.CHERRY_PLANKS) || super.isValidRepairItem(stack, material);
    }
}
