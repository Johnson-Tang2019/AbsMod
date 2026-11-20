package com.abyssredemption.absmod.entity;

import com.abyssredemption.absmod.block.HeartMirrorBlock;
import com.abyssredemption.absmod.item.BladeRoute;
import com.abyssredemption.absmod.spirit.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** A transient encounter. Only a victory on the participating physical blade is persisted. */
public final class MirrorEchoEntity extends PathfinderMob {
    public static final ResourceLocation ID = ResourceLocation.parse("absmod:mirror_echo");
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(MirrorEchoEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PHASE_START = SynchedEntityData.defineId(MirrorEchoEntity.class,EntityDataSerializers.LONG);
    private final ServerBossEvent healthBar = new ServerBossEvent(Component.translatable("entity.absmod.mirror_echo"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private UUID owner, soul;
    private BlockPos anchor;
    private Vec3 aim = Vec3.ZERO;
    private boolean damaging;
    public MirrorEchoEntity(EntityType<? extends MirrorEchoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }
    public static EntityType<MirrorEchoEntity> create() {
        return EntityType.Builder.of(MirrorEchoEntity::new,MobCategory.MISC).sized(1,1.9F)
                .noSave().fireImmune().clientTrackingRange(8).build(ID.toString());
    }
    @SuppressWarnings("unchecked")
    public static EntityType<MirrorEchoEntity> type() { return (EntityType<MirrorEchoEntity>) BuiltInRegistries.ENTITY_TYPE.get(ID); }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.KNOCKBACK_RESISTANCE,1);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, 0);
        builder.define(PHASE_START, 0L);
    }
    public float phaseAge(float partial) { return level().getGameTime() - entityData.get(PHASE_START) + partial; }
    public int phase() { return entityData.get(PHASE); }
    public static void tell(ServerPlayer p,String key) {
        if (p.connection != null) p.displayClientMessage(Component.translatable("mirror.absmod." + key), true);
    }
    private static BladeSoulRecord blade(ServerPlayer p) {
        try {
            var store = BladeSoulStore.find(p.serverLevel().getServer());
            if (store.isEmpty()) return null;
            var record = BladeSoulService.requireRecord(store.get(),p.getMainHandItem());
            return record.stage() == 8 && record.route() == BladeRoute.NORMAL
                    && record.ownerId().filter(p.getUUID()::equals).isPresent() ? record : null;
        } catch (IllegalStateException invalid) { return null; }
    }
    public static void challenge(ServerPlayer p, BlockPos pos) {
        var level = p.serverLevel();
        if (!level.isPositionEntityTicking(pos) || !level.areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(pos).toLong())) {
            tell(p,"loading");
            return;
        }
        var sword = blade(p);
        if (sword == null || !p.isAlive() || p.isSpectator()) { tell(p,"blade"); return; }
        if (com.abyssredemption.absmod.command.SoulCommands.findLoadedCopies(level.getServer(),sword.soulId()).size() > 1) {
            p.displayClientMessage(Component.translatable("resonance.absmod.duplicate"),true);
            return;
        }
        if (sword.mirrorCleared()) {
            restoreVictory(p);
            tell(p,"qualified");
            return;
        }
        if (!level.getEntitiesOfClass(MirrorEchoEntity.class,new AABB(pos).inflate(25)).isEmpty()) { tell(p,"busy"); return; }
        var echo = type().create(level);
        if (echo == null) return;
        echo.moveTo(pos.getX()+0.5,pos.getY()+1.05,pos.getZ()+0.5,0,0);
        if (!level.noCollision(echo)) { tell(p,"space"); return; }
        echo.owner = p.getUUID();
        echo.soul = sword.soulId();
        echo.anchor = pos.immutable();
        if (level.addFreshEntity(echo)) {
            if (p.connection != null) echo.healthBar.addPlayer(p);
            BladeAdvancements.award(p,"mirror_trial");
            tell(p,"started");
        }
    }
    private boolean participating(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.level() != level() || !player.getUUID().equals(owner)
                || anchor == null || player.distanceToSqr(Vec3.atCenterOf(anchor)) > 144) return false;
        var ref = player.getMainHandItem().get(BladeSoulComponents.REFERENCE);
        return ref != null && soul.equals(ref.soulId()) && blade(player) != null;
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean hurt(DamageSource source, float damage) {
        if (!(source.getDirectEntity() instanceof ServerPlayer p) || source.getEntity() != p || !participating(p)
                || phase() != 2 || p.distanceToSqr(this) > 36) return false;
        damaging = true;
        try { return super.hurt(source, damage); }
        finally { damaging = false; }
    }
    public static void restoreVictory(ServerPlayer player) {
        BladeAdvancements.award(player,"mirror_trial");
        BladeAdvancements.award(player,"mirror_victory");
    }
    @Override public void die(DamageSource source) {
        if (damaging && source.getDirectEntity() instanceof ServerPlayer p && participating(p)) {
            try {
                if (BladeSoulService.winMirror(p, soul)) {
                    restoreVictory(p);
                    tell(p,"won");
                } else p.displayClientMessage(Component.translatable("resonance.absmod.duplicate"),true);
            } catch (IllegalStateException unavailable) {
                com.mojang.logging.LogUtils.getLogger().error("Could not save mirror victory for soul {}", soul, unavailable);
                p.sendSystemMessage(Component.translatable("resonance.absmod.store_unavailable"));
            }
        }
        healthBar.removeAllPlayers();
        super.die(source);
    }
    @Override public void remove(RemovalReason reason) { healthBar.removeAllPlayers(); super.remove(reason); }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        var ownerPlayer = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null || !participating(ownerPlayer) || !level.hasChunkAt(anchor)
                || !(level.getBlockState(anchor).getBlock() instanceof HeartMirrorBlock)) {
            if (ownerPlayer != null) tell(ownerPlayer,"ended");
            discard();
            return;
        }
        healthBar.setProgress(getHealth()/getMaxHealth());
        int beat = (tickCount - 1) % 108;
        Vec3 center = Vec3.atBottomCenterOf(anchor.above());
        // First paint the sweep lane, then mark the ground under the owner for a delayed impact.
        if (beat == 0 || beat == 38 || beat == 70) {
            int phase = beat == 0 ? 0 : beat == 38 ? 1 : 2;
            entityData.set(PHASE,phase);
            entityData.set(PHASE_START,level.getGameTime());
            aim = ownerPlayer.position();
            if (phase == 0) {
                var direction = new Vec3(aim.x-center.x,0,aim.z-center.z).normalize();
                if (direction.lengthSqr() < 0.1) direction = new Vec3(0,0,1);
                aim = center.add(direction.scale(6));
                setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
                setYBodyRot(getYRot());
            }
            tell(ownerPlayer,phase == 0 ? "sweep" : phase == 1 ? "slam" : "open");
        }
        if (phase() == 0 && beat <= 24 && beat % 3 == 0) {
            Vec3 direction = aim.subtract(center).normalize();
            for (int i = 0; i <= 12; i++) {
                var point = center.add(direction.scale(i * 0.5));
                level.sendParticles(ParticleTypes.END_ROD,point.x,center.y-0.55,point.z,1,0,0,0,0);
            }
            Vec3 relative = ownerPlayer.position().subtract(center).multiply(1,0,1);
            double along = relative.dot(direction);
            if (beat == 24 && along >= -0.6 && along <= 6.6 && relative.subtract(direction.scale(along)).lengthSqr() <= 1.6
                    && Math.abs(ownerPlayer.getY()-center.y) < 3) ownerPlayer.hurt(damageSources().mobAttack(this),5);
        }
        if (phase() == 1 && beat <= 65 && (beat-38)%3 == 0) {
            for (int i = 0; i < 20; i++) {
                double angle = i*Math.PI/10;
                level.sendParticles(ParticleTypes.WITCH,aim.x+2*Math.cos(angle),aim.y+0.12,aim.z+2*Math.sin(angle),1,0,0,0,0);
            }
            if (beat == 65 && ownerPlayer.distanceToSqr(aim) <= 4) ownerPlayer.hurt(damageSources().mobAttack(this),7);
        }
    }
}
