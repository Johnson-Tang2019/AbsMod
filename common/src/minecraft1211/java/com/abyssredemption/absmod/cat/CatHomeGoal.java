package com.abyssredemption.absmod.cat;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.Vec3;

/** Uses ordinary navigation for both a rest order and a nearby call. */
public final class CatHomeGoal extends Goal {
    public interface Carrier {
        CatHomeGoal absmod$homeGoal();
    }

    public static final int RANGE = 16;
    private final Cat cat;
    private UUID caller;
    private int callTicks;
    private int nextCheck;
    private int nextPath;
    private boolean resting;

    public CatHomeGoal(Cat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public boolean isResting() { return resting && cat.isLying(); }

    public void call(UUID owner) {
        caller = owner;
        callTicks = cat.tickCount + 120;
        nextCheck = 0;
        nextPath = 0;
    }

    public void cancelCall() {
        caller = null;
        nextCheck = 0;
    }

    @Override public boolean canUse() {
        if (cat.tickCount < nextCheck) {
            return false;
        }
        nextCheck = cat.tickCount + 20;
        return target() != null;
    }

    @Override public boolean canContinueToUse() {
        return target() != null;
    }

    @Override public void tick() {
        Vec3 target = target();
        if (target == null) {
            return;
        }
        if (cat.distanceToSqr(target) <= (caller == null ? 0.12 : 2.25)) {
            cat.getNavigation().stop();
            if (caller == null) {
                if (!resting && cat.level() instanceof ServerLevel level) {
                    var store = CatLifeStore.get(level.getServer());
                    var home = store.home(cat.getUUID());
                    store.arrived(home, cat.getUUID(), level.getGameTime());
                    var owner = level.getServer().getPlayerList().getPlayer(home.owner());
                    if (owner != null) com.abyssredemption.absmod.spirit.BladeAdvancements.award(owner, "cat_rest");
                }
                resting = true;
                cat.setInSittingPose(false);
                cat.setLying(true);
            } else {
                caller = null;
            }
            return;
        }
        if (resting) {
            cat.setInSittingPose(false);
            cat.setLying(false);
            resting = false;
        }
        if (caller == null && stepOntoCushion(target)) {
            return;
        }
        if (cat.tickCount >= nextPath) {
            nextPath = cat.tickCount + 15;
            cat.getNavigation().moveTo(target.x, target.y, target.z, 1.1);
        }
    }

    // Vanilla navigation can stop beside the nest because the target block is occupied.
    // Step the final short distance only across a clear, supported floor.
    private boolean stepOntoCushion(Vec3 target) {
        Vec3 step = target.subtract(cat.position());
        if (step.lengthSqr() > 2.25 || Math.abs(step.y) > 0.5
                || !cat.level().noCollision(cat, cat.getBoundingBox().move(0, Math.max(0,step.y), 0).expandTowards(step.x, 0, step.z))) {
            return false;
        }
        var floor = BlockPos.containing(target).below();
        if (!cat.level().getBlockState(floor).isFaceSturdy(cat.level(), floor, Direction.UP)) {
            return false;
        }
        cat.getNavigation().stop();
        cat.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.65);
        return true;
    }

    @Override public void stop() {
        cat.getNavigation().stop();
        if (resting) {
            cat.setInSittingPose(false);
            cat.setLying(false);
            resting = false;
        }
    }

    private Vec3 target() {
        if (!(cat.level() instanceof ServerLevel level) || cat.isOrderedToSit() || !cat.isTame()) {
            return null;
        }
        if (caller != null) {
            var player = level.getServer().getPlayerList().getPlayer(caller);
            if (cat.tickCount >= callTicks || player == null || !cat.isOwnedBy(player) || player.level() != level
                    || !player.isAlive() || cat.distanceToSqr(player) > RANGE * RANGE) {
                caller = null;
                return null;
            }
            return player.position();
        }
        var home = CatLifeStore.get(level.getServer()).home(cat.getUUID());
        if (home == null || !home.resting() || !CatLife.owns(cat, home.owner())) {
            return null;
        }
        GlobalPos spot = home.spot();
        Vec3 target = Vec3.atBottomCenterOf(spot.pos()).add(0, CatNestBlock.REST_HEIGHT, 0);
        return level.dimension().equals(spot.dimension()) && cat.distanceToSqr(target) <= RANGE * RANGE
                && level.hasChunkAt(spot.pos()) && level.getBlockState(spot.pos()).getBlock() instanceof CatNestBlock ? target : null;
    }
}
