package com.abyssredemption.absmod.entity;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Manual spirit prototype. Final health, death and automatic recall rules remain unconfigured. */
public final class BladeSpiritEntity extends PathfinderMob {
    private UUID ownerId;
    private UUID soulId;

    public BladeSpiritEntity(EntityType<? extends BladeSpiritEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    public void bind(UUID owner, UUID soul) { ownerId = owner; soulId = soul; }
    public UUID soulId() { return soulId; }
    public UUID ownerId() { return ownerId; }

    /** Spirit form is immune to damage, including sources that bypass the vanilla invulnerable flag. */
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        var result = com.abyssredemption.absmod.spirit.SpiritInteraction.recall(player, hand, this);
        return result == net.minecraft.world.InteractionResult.PASS ? super.mobInteract(player, hand) : result;
    }

    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 10 == 0) {
            var owner = ownerId == null ? null : level().getPlayerByUUID(ownerId);
            if (owner == null || !owner.isAlive() || owner.isSpectator() || !active()) {
                getNavigation().stop();
            } else {
                getLookControl().setLookAt(owner, 30, 30);
                double distance = distanceToSqr(owner);
                if (distance > 9 && distance < 24 * 24) { getNavigation().moveTo(owner, 1.0); }
                else if (distance < 4 || distance >= 24 * 24) { getNavigation().stop(); }
            }
        }
    }

    public boolean active() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || soulId == null) { return false; }
        try {
            return com.abyssredemption.absmod.spirit.BladeSoulStore.find(serverLevel.getServer())
                    .flatMap(store -> store.find(soulId))
                    .filter(record -> record.form().equals("spirit") && record.ownerId().filter(id -> id.equals(ownerId)).isPresent())
                    .flatMap(com.abyssredemption.absmod.spirit.BladeSoulRecord::carrier)
                    .filter(carrier -> carrier.entityId().equals(getUUID())).isPresent();
        } catch (IllegalStateException exception) { return false; }
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (ownerId != null) { output.store("absmod_owner", UUIDUtil.CODEC, ownerId); }
        if (soulId != null) { output.store("absmod_soul", UUIDUtil.CODEC, soulId); }
    }

    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ownerId = input.read("absmod_owner", UUIDUtil.CODEC).orElse(null);
        soulId = input.read("absmod_soul", UUIDUtil.CODEC).orElse(null);
        setInvulnerable(true);
    }
}
