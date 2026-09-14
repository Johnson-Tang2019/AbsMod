package com.abyssredemption.absmod.combat;

/** Client input edges and heartbeat policy, independently testable without a window. */
public final class AttackInputTracker {
    private long epoch;
    private long sequence;
    private long lastSentTick;
    private boolean active;
    private boolean requiresRelease = true;
    private Object weapon;
    private int slot;

    public Intent tick(long tick, boolean down, boolean eligible, Object currentWeapon, int currentSlot) {
        boolean changed = active && (weapon != currentWeapon || slot != currentSlot);
        if (!eligible || changed) { requiresRelease = true; }
        if (!down) { requiresRelease = false; }
        boolean next = eligible && down && !requiresRelease;
        if (active && !next) {
            active = false;
            return new Intent(epoch, ++sequence, false, slot);
        }
        if (!next) { return null; }
        if (!active) {
            active = true;
            weapon = currentWeapon;
            slot = currentSlot;
            epoch++;
            lastSentTick = tick;
            return new Intent(epoch, ++sequence, true, slot);
        }
        if (tick - lastSentTick >= CombatProbeSettings.HEARTBEAT_TICKS) {
            lastSentTick = tick;
            return new Intent(epoch, ++sequence, true, slot);
        }
        return null;
    }

    public void requireRelease() { requiresRelease = true; }

    /** Keep the same held intent across a server-confirmed spectral-to-sword replacement. */
    public void replaceWeapon(Object previous, Object replacement) {
        if (active && weapon == previous) { weapon = replacement; }
    }

    public record Intent(long epoch, long sequence, boolean held, int slot) {}
}
