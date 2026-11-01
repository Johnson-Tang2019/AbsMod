package com.abyssredemption.absmod.combat;

import java.util.Objects;

/** Short-lived server state using the PROPOSED balance profile; never stores affection. */
public final class BladeComboState {
    public static final int GRACE_TICKS = 60;
    public static final int DECAY_TICKS = 5;
    private Object weapon;
    private Object soul;
    private Object world;
    private int slot;
    private int stacks;
    private long lastAction = -1;
    private long nextDecay;

    public void bind(Object weapon, Object soul, Object world, int slot) {
        if (this.weapon != weapon || !Objects.equals(this.soul, soul) || this.world != world || this.slot != slot) {
            stacks = 0;
        }
        this.weapon = weapon;
        this.soul = soul;
        this.world = world;
        this.slot = slot;
        if (weapon == null) { stacks = 0; }
    }

    public void advance(long tick) {
        if (stacks > 0 && tick >= nextDecay) {
            long lost = 1 + (tick - nextDecay) / DECAY_TICKS;
            stacks -= (int) Math.min(stacks, lost);
            if (stacks > 0) { nextDecay += lost * DECAY_TICKS; }
        }
    }

    public boolean recordHit(long tick, long action, int maximum) {
        advance(tick);
        if (weapon == null || action <= lastAction || maximum <= 0) { return false; }
        lastAction = action;
        stacks = Math.min(stacks + 1, maximum);
        nextDecay = tick + GRACE_TICKS + DECAY_TICKS;
        return true;
    }

    public int stacks() { return stacks; }
}
