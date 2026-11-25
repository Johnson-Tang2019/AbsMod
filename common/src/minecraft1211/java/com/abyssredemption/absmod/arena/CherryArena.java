package com.abyssredemption.absmod.arena;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;

public final class CherryArena {
    public static final ResourceLocation ID = ResourceLocation.parse("absmod:cherry_arena");
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);
    public static final BlockPos ORIGIN = new BlockPos(-56, 64, -56);
    public static final int FLOOR = 70;
    public static final BlockPos SPAWN = new BlockPos(0, FLOOR, 38);
    public static final int FIGHT_RADIUS = 34;
    public static final BlockPos EXIT = new BlockPos(0, FLOOR, 47);
    public static final BlockPos MIRROR = new BlockPos(0, FLOOR, -8);
    private static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.parse("absmod:arena_bosses"));
    private static final Map<ServerLevel, Boolean> READY = new WeakHashMap<>();
    private static final ThreadLocal<ServerLevel> PLACING = new ThreadLocal<>();

    // Boss bars owned by an entity are common in mods that supply no entity-type tag.
    private static final ClassValue<Boolean> OWNS_BOSS_BAR = new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            for (Class<?> c = type; c != null && c != LivingEntity.class; c = c.getSuperclass()) {
                try {
                    for (var field : c.getDeclaredFields())
                        if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                                ServerBossEvent.class.isAssignableFrom(field.getType())) return true;
                } catch (LinkageError missingOptionalType) {
                    // A mod may mention an absent optional API. Continue checking its superclasses and tags.
                }
            }
            return false;
        }
    };

    private CherryArena() {}

    public static boolean locked(Level level) {
        return level.dimension() == DIMENSION && PLACING.get() != level;
    }

    /** Reapply the immutable template once per loaded server level. Interrupted placement is retryable. */
    public static void prepare(ServerLevel level) {
        if (level.dimension() != DIMENSION || READY.containsKey(level)) return;
        var template = level.getStructureManager().get(ID).orElseThrow(() -> new IllegalStateException("Missing " + ID));
        if (!template.getSize().equals(new net.minecraft.core.Vec3i(113, 38, 113)))
            throw new IllegalStateException("Unexpected cherry arena template dimensions");
        PLACING.set(level);
        try {
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            var settings = new StructurePlaceSettings().setIgnoreEntities(true).setKnownShape(true);
            if (!template.placeInWorld(level, ORIGIN, ORIGIN, settings, level.random, flags))
                throw new IllegalStateException("Could not place " + ID);
            var portal = BuiltInRegistries.BLOCK.get(CherryPortalBlock.ID).defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X);
            for (int x = -3; x <= 3; x++) for (int y = 0; y < 4; y++)
                level.setBlock(EXIT.offset(x, y, 0), portal, flags);
            level.setBlock(MIRROR, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("absmod:heart_mirror")).defaultBlockState(), flags);
            if (!level.getBlockState(SPAWN.below()).isFaceSturdy(level, SPAWN.below(), Direction.UP))
                throw new IllegalStateException("Cherry arena landing is missing");
            READY.put(level, true);
        } finally {
            PLACING.remove();
        }
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.getType().is(BOSSES) || OWNS_BOSS_BAR.get(entity.getClass());
    }

    public static void contain(Entity entity, ServerLevel level) {
        if (entity instanceof ServerPlayer player) {
            if (player.getY() < FLOOR - 7) {
                player.stopRiding();
                player.teleportTo(level, SPAWN.getX()+.5, SPAWN.getY(), SPAWN.getZ()+.5, java.util.Set.of(), 180, 0);
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0;
            }
            return;
        }
        if (!(entity instanceof LivingEntity boss) || !isBoss(boss) || !boss.isAlive()) return;
        // Keep horizontal attacks and AI. Reserve room for the body of a full-size dragon.
        double radius = Math.max(1, FIGHT_RADIUS - boss.getBbWidth() / 2);
        double dx = boss.getX() - .5, dz = boss.getZ() - .5;
        double scale = Math.min(1, radius / Math.max(.001, Math.hypot(dx, dz)));
        double x = .5 + dx * scale, z = .5 + dz * scale;
        // The trial mirror is the only raised obstacle on the fighting floor.
        double half = boss.getBbWidth() / 2;
        double ground = (Math.abs(x - (MIRROR.getX()+.5)) < half+.5 &&
                         Math.abs(z - (MIRROR.getZ()+.5)) < half+.5) ? FLOOR + 1 : FLOOR;
        if (boss.isPassenger()) boss.stopRiding();
        if (scale < 1 || Math.abs(boss.getY() - ground) > .001)
            boss.teleportTo(x, ground, z);
        var motion = boss.getDeltaMovement();
        if (motion.y != 0) boss.setDeltaMovement(motion.x, 0, motion.z);
        boss.setOnGround(true);
        boss.fallDistance = 0;
    }
}
