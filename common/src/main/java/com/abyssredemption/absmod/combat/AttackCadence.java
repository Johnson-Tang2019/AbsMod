package com.abyssredemption.absmod.combat;

/** One game-tick budget shared by every input session of a connected player. */
public final class AttackCadence {
    private double progress = 1;
    private long lastTick = Long.MIN_VALUE;
    private Object lastWeapon;
    private double lastActionSpeed;

    public boolean tick(long tick, boolean held, double speed) {
        return tick(tick, held, speed, this);
    }

    public boolean tick(long tick, boolean held, double speed, Object weapon) {
        if (tick <= lastTick) { return false; }
        boolean firstTick = lastTick == Long.MIN_VALUE;
        lastTick = tick;
        if (!Double.isFinite(speed) || speed <= 0) { return false; }
        // Never catch up skipped ticks or wall-clock time; only the current tick contributes.
        // A faster replacement weapon cannot shorten the previous weapon's outstanding recovery.
        double recoverySpeed = lastWeapon != null && (!held || weapon != lastWeapon) ? Math.min(speed, lastActionSpeed) : speed;
        if (!firstTick) { progress += Math.min(recoverySpeed, CombatProbeSettings.MAX_ACTIONS_PER_SECOND) / 20.0; }
        if (held && progress + 1.0e-9 >= 1) {
            progress = Math.max(0, progress - 1);
            lastWeapon = weapon;
            lastActionSpeed = speed;
            return true;
        }
        if (!held) { progress = Math.min(1, progress); }
        return false;
    }
}
