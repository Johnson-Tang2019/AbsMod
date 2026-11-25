package com.abyssredemption.absmod.arena;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

public final class CherryPortalBlock extends Block implements Portal {
    public static final ResourceLocation ID = ResourceLocation.parse("absmod:cherry_portal");
    private static final String RETURN = "absmod:cherry_gate_return";
    private static final VoxelShape X_PLANE = box(0,0,6,16,16,10), Z_PLANE = box(6,0,0,10,16,16);

    public CherryPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_AXIS);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? X_PLANE : Z_PLANE;
    }
    private static Direction along(Direction.Axis axis) { return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH; }

    private static boolean frame(LevelReader level, BlockPos bottom, Direction side) {
        for (int i = -1; i <= 2; i++) for (int h = 0; h <= 4; h++) {
            boolean end = i == -1 || i == 2;
            boolean cap = h == 0 || h == 4;
            if (end == cap) continue;
            if (!level.getBlockState(bottom.relative(side,i).above(h)).is(BlockTags.CHERRY_LOGS)) return false;
        }
        return true;
    }

    public boolean open(Level level, BlockPos clicked) {
        for (var axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            Direction side = along(axis);
            for (int offset = 0; offset <= 1; offset++) {
                var base = clicked.relative(side, -offset);
                if (!frame(level, base, side)) continue;
                boolean clear = true;
                for (int i=0;i<2;i++) for (int h=1;h<=3;h++)
                    clear &= level.isEmptyBlock(base.relative(side,i).above(h));
                if (!clear) continue;
                if (!level.isClientSide) {
                    var portal = defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
                    for (int i=0;i<2;i++) for (int h=1;h<=3;h++)
                        level.setBlock(base.relative(side,i).above(h),portal,Block.UPDATE_CLIENTS);
                }
                return true;
            }
        }
        return false;
    }

    @Override protected BlockState updateShape(BlockState state, Direction face, BlockState neighbor,
            LevelAccessor level, BlockPos pos, BlockPos other) {
        if (level instanceof Level world && world.dimension() != CherryArena.DIMENSION) {
            var side = along(state.getValue(BlockStateProperties.HORIZONTAL_AXIS));
            for (int i=0;i<2;i++) for (int h=1;h<=3;h++)
                if (frame(level,pos.relative(side,-i).below(h),side)) return state;
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide && level.dimension() != CherryArena.DIMENSION && !old.is(this))
            level.scheduleTick(pos, this, 5);
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        var side = along(state.getValue(BlockStateProperties.HORIZONTAL_AXIS));
        if (level.getBlockState(pos.below()).is(this) || level.getBlockState(pos.relative(side.getOpposite())).is(this)) return;
        // Dragons and stationary withers may skip ordinary inside-block callbacks.
        var opening = new net.minecraft.world.phys.AABB(pos).minmax(new net.minecraft.world.phys.AABB(pos.relative(side).above(2)));
        for (var e : level.getEntitiesOfClass(LivingEntity.class, opening,
                mob -> mob.getType() == net.minecraft.world.entity.EntityType.WITHER ||
                        mob.getType() == net.minecraft.world.entity.EntityType.ENDER_DRAGON)) {
            entityInside(state, level, pos, e);
        }
        level.scheduleTick(pos, this, 10);
    }

    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || !entity.isAlive()) return;
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this,pos);
        } else if (level instanceof ServerLevel from && from.dimension() != CherryArena.DIMENSION
                && !entity.isRemoved() && !entity.isPassenger() && !entity.isOnPortalCooldown()
                && (entity.getType() == net.minecraft.world.entity.EntityType.WITHER ||
                    (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon && dragon.getDragonFight() == null))) {
            // Vanilla bosses reject nether portals. Only this gate admits an unbound vanilla boss.
            var destination = getPortalDestination(from, entity, pos);
            if (destination != null && entity.canChangeDimensions(from, destination.newLevel())) {
                var arrived = entity.changeDimension(destination);
                if (arrived != null) arrived.setPortalCooldown();
            }
        }
    }

    @Override public DimensionTransition getPortalDestination(ServerLevel from, Entity entity, BlockPos pos) {
        var server = from.getServer();
        if (from.dimension() == CherryArena.DIMENSION) return returnTrip(server,entity);
        ServerLevel arena = server.getLevel(CherryArena.DIMENSION);
        if (arena == null) return null;
        CherryArena.prepare(arena);
        var axis = from.getBlockState(pos).getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        var source = new CompoundTag();
        source.putString("dimension",from.dimension().location().toString());
        source.putLong("position",pos.asLong());
        source.putBoolean("alongX",axis == Direction.Axis.X);
        return new DimensionTransition(arena,Vec3.atBottomCenterOf(CherryArena.SPAWN),Vec3.ZERO,180,0,e -> {
            e.getPersistentData().put(RETURN,source);
            DimensionTransition.PLACE_PORTAL_TICKET.onTransition(e);
        });
    }

    private DimensionTransition returnTrip(net.minecraft.server.MinecraftServer server, Entity entity) {
        var data = entity.getPersistentData().getCompound(RETURN);
        var id = ResourceLocation.tryParse(data.getString("dimension"));
        ServerLevel level = id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION,id));
        if (level != null && level.dimension() != CherryArena.DIMENSION && data.contains("position")) {
            var origin = BlockPos.of(data.getLong("position"));
            var side = data.getBoolean("alongX") ? Direction.SOUTH : Direction.EAST;
            if (level.isInWorldBounds(origin) && level.getWorldBorder().isWithinBounds(origin)) {
                for (int d=1;d<=3;d++) for (int sign : new int[]{1,-1}) for (int h=1;h>=-4;h--) {
                    var foot = origin.relative(side,d*sign).above(h);
                    var point = Vec3.atBottomCenterOf(foot);
                    if (level.getBlockState(foot.below()).isFaceSturdy(level,foot.below(),Direction.UP)
                            && level.getFluidState(foot).isEmpty() && level.getWorldBorder().isWithinBounds(foot)
                            && level.noCollision(entity,entity.getDimensions(entity.getPose()).makeBoundingBox(point))) {
                        return new DimensionTransition(level,point,Vec3.ZERO,entity.getYRot(),0,DimensionTransition.PLACE_PORTAL_TICKET);
                    }
                }
            }
        }
        return new DimensionTransition(server.overworld(),entity,DimensionTransition.PLACE_PORTAL_TICKET);
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (random.nextInt(3)==0) level.addParticle(net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES,
                pos.getX()+random.nextDouble(),pos.getY()+random.nextDouble(),pos.getZ()+random.nextDouble(),0,.02,0);
    }
}
