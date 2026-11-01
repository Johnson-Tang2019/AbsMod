package com.abyssredemption.absmod.combat;

/** Monotonic epochs and sequences remain as tombstones after stop/expiry. */
public final class AttackInputSession {
    private long epoch;
    private long sequence;
    private long lastInputTick;
    private boolean held;

    public boolean accept(long incomingEpoch, long incomingSequence, boolean down, long tick) {
        if (incomingEpoch <= 0 || incomingSequence <= sequence || incomingEpoch < epoch) { return false; }
        if (incomingEpoch == epoch && down && !held) { return false; }
        epoch = incomingEpoch;
        sequence = incomingSequence;
        held = down;
        lastInputTick = tick;
        return true;
    }

    public boolean active(long tick) {
        if (tick - lastInputTick >= CombatProbeSettings.INPUT_TIMEOUT_TICKS) { held = false; }
        return held;
    }

    public void stop() { held = false; }
    public long epoch() { return epoch; }
    public long sequence() { return sequence; }
}
