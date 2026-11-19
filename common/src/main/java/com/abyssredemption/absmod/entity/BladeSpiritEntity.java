package com.abyssredemption.absmod.entity;

import com.abyssredemption.absmod.item.GentleBrushItem;
import com.abyssredemption.absmod.spirit.BladeAffection;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.abyssredemption.absmod.spirit.SpiritTransformationService;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The loaded companion of one authoritative blade soul. */
public final class BladeSpiritEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> GROOMING =
            SynchedEntityData.defineId(BladeSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private UUID ownerId;
    private UUID soulId;
    private static final int FOLLOW_INTERVAL = 10;
    private static final int CATCH_UP_DISTANCE = 24;
    private Vec3 lastGrowthPosition;
    private GlobalPos lastRecordedLocation;
    private int groomingEnd;
    private int groomingVisualTicks;

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

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROOMING, false);
    }

    public boolean startGrooming(ServerPlayer owner) {
        if (isGrooming() || tickCount < groomingEnd) {
            return false;
        }
        groomingEnd = tickCount + GentleBrushItem.RESPONSE_TICKS;
        entityData.set(GROOMING, true);
        getLookControl().setLookAt(owner, 30, 30);
        return true;
    }

    public boolean isGrooming() {
        return entityData.get(GROOMING);
    }

    public float groomingProgress(float partialTick) {
        return isGrooming() ? Math.clamp((groomingVisualTicks + partialTick) / GentleBrushItem.RESPONSE_TICKS, 0, 1) : 0;
    }

    /** Spirit form is immune to damage, including sources that bypass the vanilla invulnerable flag. */
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND && !player.isShiftKeyDown()
                && player.getItemInHand(hand).getItem() instanceof GentleBrushItem) {
            return player instanceof ServerPlayer serverPlayer ? GentleBrushItem.groomSpirit(serverPlayer, this, hand)
                    : net.minecraft.world.InteractionResult.SUCCESS;
        }
        if(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            var growth=com.abyssredemption.absmod.spirit.BladeAffection.interact(serverPlayer,this,hand);
            if(growth!=net.minecraft.world.InteractionResult.PASS)return growth;
        }
        var result = com.abyssredemption.absmod.spirit.SpiritInteraction.recall(player, hand, this);
        return result == net.minecraft.world.InteractionResult.PASS ? super.mobInteract(player, hand) : result;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassengers) {
        // The soul service controls dimension handoffs and their durable authority.
        return false;
    }

    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            groomingVisualTicks = isGrooming() ? Math.min(groomingVisualTicks + 1, GentleBrushItem.RESPONSE_TICKS) : 0;
        }
        if (!(level() instanceof ServerLevel level) || isRemoved()) { return; }
        if ((tickCount == 1 || tickCount % FOLLOW_INTERVAL == 0) && retireStaleCarrier(level)) { return; }
        if (isGrooming()) {
            var owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
            if (tickCount >= groomingEnd || !(owner instanceof ServerPlayer player)
                    || !GentleBrushItem.canGroom(player, this) || !active()) {
                entityData.set(GROOMING, false);
            }
        }
        rememberLocation(level);
        if (tickCount % FOLLOW_INTERVAL != 0 || isNoAi()) { return; }

        var owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
        if (!(owner instanceof ServerPlayer player) || !player.isAlive()
                || player.isSpectator() || isPassenger() || !active()) {
            lastGrowthPosition = null;
            getNavigation().stop();
            return;
        }

        boolean moved = lastGrowthPosition != null && player.position().distanceToSqr(lastGrowthPosition) > .01;
        BladeAffection.follow(player, this, moved);
        lastGrowthPosition = player.position();
        getLookControl().setLookAt(player, 30, 30);
        double distance = distanceToSqr(player);
        if (distance < 4) {
            getNavigation().stop();
        } else if (distance >= CATCH_UP_DISTANCE * CATCH_UP_DISTANCE && tryCatchUp(level, player.blockPosition())) {
            rememberLocation(level);
        } else if (distance > 9) {
            getNavigation().moveTo(player, 1.0);
        }
    }

    private void rememberLocation(ServerLevel level) {
        if (soulId == null) { return; }
        BlockPos position = blockPosition();
        if (lastRecordedLocation != null && lastRecordedLocation.dimension().equals(level.dimension())
                && chunkCoordinate(position.getX()) == chunkCoordinate(lastRecordedLocation.pos().getX())
                && chunkCoordinate(position.getZ()) == chunkCoordinate(lastRecordedLocation.pos().getZ())) {
            return;
        }
        // Retry unresolved records at a limited rate, including records being repaired by an operator.
        if (lastRecordedLocation == null && tickCount != 1 && tickCount % FOLLOW_INTERVAL != 0) { return; }
        GlobalPos observed = GlobalPos.of(level.dimension(), position);
        try {
            if (SpiritTransformationService.rememberLocation(level, this, observed)) {
                lastRecordedLocation = observed;
            }
        } catch (IllegalStateException exception) {
            // Keep the existing fail-closed behavior when the store cannot be read.
        }
    }

    private static int chunkCoordinate(int blockCoordinate) {
        return SectionPos.blockToSectionCoord(blockCoordinate);
    }

    private boolean tryCatchUp(ServerLevel level, BlockPos target) {
        BlockPos feet = findSafeArrival(level, target);
        if (feet == null) { return false; }
        getNavigation().stop();
        snapTo(feet.getX() + .5, feet.getY(), feet.getZ() + .5);
        setDeltaMovement(Vec3.ZERO);
        resetFallDistance();
        return true;
    }

    public BlockPos findSafeArrival(ServerLevel level, BlockPos target) {
        for (int dy : new int[]{0, -1, 1}) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) < 2) { continue; }
                    BlockPos feet = target.offset(dx, dy, dz);
                    if (canStandAt(level, feet)) { return feet; }
                }
            }
        }
        return null;
    }

    private boolean retireStaleCarrier(ServerLevel level) {
        if (soulId == null || ownerId == null) { return false; }
        try {
            var record = BladeSoulStore.find(level.getServer()).flatMap(store -> store.find(soulId)).orElse(null);
            if (record != null && record.ownerId().filter(ownerId::equals).isPresent()
                    && (record.form().equals("sword") || (record.form().equals("spirit")
                    && record.carrier().filter(carrier -> !carrier.entityId().equals(getUUID())
                            || carrier.lastLocation().filter(position -> !position.dimension().equals(level.dimension())).isPresent()).isPresent()))) {
                discard();
                return true;
            }
        } catch (IllegalStateException ignored) {
            // Without an authoritative record, retain the entity but deny transactions.
        }
        return false;
    }

    private boolean canStandAt(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        BlockPos head = feet.above();
        if (level.isOutsideBuildHeight(floor) || level.isOutsideBuildHeight(head)
                || !level.isPositionEntityTicking(feet)) {
            return false;
        }

        // Path type and collision checks also inspect adjacent blocks at chunk edges.
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                if (level.getChunkSource().getChunkNow(chunkCoordinate(feet.getX() + dx),
                        chunkCoordinate(feet.getZ() + dz)) == null) { return false; }
            }
        }
        if (!level.isEmptyBlock(feet) || !level.isEmptyBlock(head)
                || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                || WalkNodeEvaluator.getPathTypeStatic(
                        new net.minecraft.world.level.pathfinder.PathfindingContext(level, this), feet.mutable()) != PathType.WALKABLE) {
            return false;
        }
        var bounds = getBoundingBox().move(feet.getX() + .5 - getX(),
                feet.getY() - getY(), feet.getZ() + .5 - getZ());
        return level.getWorldBorder().isWithinBounds(bounds) && level.noCollision(this, bounds);
    }

    public boolean active() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || soulId == null) { return false; }
        try {
            return com.abyssredemption.absmod.spirit.BladeSoulStore.find(serverLevel.getServer())
                    .flatMap(store -> store.find(soulId))
                    .filter(record -> record.form().equals("spirit") && record.ownerId().filter(id -> id.equals(ownerId)).isPresent())
                    .flatMap(com.abyssredemption.absmod.spirit.BladeSoulRecord::carrier)
                    .filter(carrier -> carrier.entityId().equals(getUUID()))
                    .filter(carrier -> carrier.lastLocation().map(GlobalPos::dimension)
                            .map(serverLevel.dimension()::equals).orElse(true)).isPresent();
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
