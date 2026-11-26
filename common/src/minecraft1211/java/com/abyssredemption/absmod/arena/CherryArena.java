package com.abyssredemption.absmod.arena;

import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;

public final class CherryArena {
    public static final ResourceLocation ID = ResourceLocation.parse("absmod:cherry_arena");
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);
    public static final BlockPos ORIGIN = new BlockPos(-56, 64, -56);
    public static final int FLOOR = 70;
    public static final BlockPos SPAWN = new BlockPos(0, FLOOR, 38);
    public static final int FIGHT_RADIUS = 34;
    private static final int GARDEN_RADIUS = 55;
    public static final BlockPos EXIT = new BlockPos(0, FLOOR, 47);
    public static final BlockPos MIRROR = new BlockPos(0, FLOOR, -8);
    private static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.parse("absmod:arena_bosses"));
    private static final Map<ServerLevel, Long> NEXT_SKY_CHECK = new WeakHashMap<>();
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
        if (level.dimension() != DIMENSION) return;
        Long nextCheck = NEXT_SKY_CHECK.get(level);
        if (nextCheck != null) {
            if (!level.players().isEmpty() && level.getGameTime() >= nextCheck) {
                NEXT_SKY_CHECK.put(level, level.getGameTime() + 40);
                refreshStaleCourtLight(level);
            }
            return;
        }
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
            // A saved chunk can retain the previous roof's shadows after bulk template placement.
            var limits = template.getSize();
            var firstChunk = new ChunkPos(ORIGIN);
            var lastChunk = new ChunkPos(ORIGIN.offset(limits.getX()-1, 0, limits.getZ()-1));
            var chunks = ChunkPos.rangeClosed(firstChunk, lastChunk).toList();
            for (var chunk : chunks) level.getChunk(chunk.x, chunk.z).initializeLightSources();
            for (var chunk : chunks) publishArenaLight(level, level.getChunk(chunk.x, chunk.z));
            NEXT_SKY_CHECK.put(level, 0L);
        } finally {
            PLACING.remove();
        }
    }

    private static void publishArenaLight(ServerLevel level, LevelChunk chunk) {
        var source = level.getChunkSource();
        // Bulk skylight propagation writes section arrays directly. Publish the finished arrays
        // to watching clients and mark the chunk for saving even if no block state changed.
        source.getLightEngine().lightChunk(chunk, false).thenRunAsync(() -> {
            chunk.setUnsaved(true);
            if (source.getChunkNow(chunk.getPos().x, chunk.getPos().z) != chunk) return;
            var watchers = source.chunkMap.getPlayers(chunk.getPos(), false);
            if (watchers.isEmpty()) return;
            var engine = source.getLightEngine();
            int lowest = SectionPos.blockToSectionCoord(ORIGIN.getY()) - 1;
            int highest = SectionPos.blockToSectionCoord(ORIGIN.getY() + 37) + 1;
            var skySections = new BitSet();
            skySections.set(lowest - engine.getMinLightSection(), highest - engine.getMinLightSection() + 1);
            // Vanilla light-change broadcasts reach the tracking border; a bulk relight also affects interior viewers.
            var packet = new ClientboundLightUpdatePacket(chunk.getPos(), engine, skySections, new BitSet());
            for (var player : watchers) player.connection.send(packet);
        }, level.getServer());
    }

    // Chunk lighting can finish asynchronously after a legacy roof has been removed.
    // Sample open sky above the actual garden surface so the moon and trees keep their natural shadows.
    private static void refreshStaleCourtLight(ServerLevel level) {
        var lighting = level.getChunkSource().getLightEngine();
        var pos = new BlockPos.MutableBlockPos();
        int radiusSquared = GARDEN_RADIUS * GARDEN_RADIUS;
        for (int cx = -GARDEN_RADIUS >> 4; cx <= GARDEN_RADIUS >> 4; cx++) {
            for (int cz = -GARDEN_RADIUS >> 4; cz <= GARDEN_RADIUS >> 4; cz++) {
                var chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                boolean stale = false;
                for (int x = cx * 16; x < cx * 16 + 16; x++) {
                    for (int z = cz * 16; z < cz * 16 + 16; z++) {
                        if (x*x + z*z > radiusSquared) continue;
                        pos.set(x, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1, z);
                        if (level.getBrightness(LightLayer.SKY,pos) < 15) {
                            if (!stale) chunk.initializeLightSources();
                            stale = true;
                            lighting.checkBlock(pos);
                        }
                    }
                }
                if (stale) publishArenaLight(level,chunk);
            }
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
