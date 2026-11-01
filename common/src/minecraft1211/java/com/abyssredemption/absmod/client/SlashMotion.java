package com.abyssredemption.absmod.client;

/** Visual-only swing planes and sweep directions, independent of attack timing. */
public enum SlashMotion {
    DIAGONAL_RIGHT(-40, 1),
    DIAGONAL_LEFT(40, -1),
    HORIZONTAL(8, 1),
    RISING(78, -1),
    STEEP_DIAGONAL(-70, 1),
    REVERSE_SWEEP(-12, -1);

    private static final SlashMotion[] SEQUENCE = values();
    private final double rollDegrees;
    private final int sweepDirection;

    SlashMotion(double rollDegrees, int sweepDirection) {
        this.rollDegrees = rollDegrees;
        this.sweepDirection = sweepDirection;
    }

    public double rollRadians(int handDirection) {
        return Math.toRadians(rollDegrees * handDirection);
    }

    public int direction(int handDirection) {
        return sweepDirection * handDirection;
    }

    public SlashMotion next() {
        return SEQUENCE[(ordinal() + 1) % SEQUENCE.length];
    }
}
