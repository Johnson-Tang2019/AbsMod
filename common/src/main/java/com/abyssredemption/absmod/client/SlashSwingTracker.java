package com.abyssredemption.absmod.client;

/** Detects the beginning of a vanilla swing, including an early animation restart. */
public final class SlashSwingTracker {
    private SlashMotion nextMotion = SlashMotion.DIAGONAL_RIGHT;

    /** Advance only for a blade swing; idle time does not reset the visual sequence. */
    public SlashMotion takeMotion() {
        SlashMotion motion = nextMotion;
        nextMotion = motion.next();
        return motion;
    }

    private boolean swinging;
    private int time;

    public boolean update(boolean active, int swingTime) {
        boolean started = active && (!swinging || swingTime < time);
        swinging = active;
        time = swingTime;
        return started;
    }
}
