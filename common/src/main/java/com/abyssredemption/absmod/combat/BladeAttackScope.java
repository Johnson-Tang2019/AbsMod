package com.abyssredemption.absmod.combat;

/** Thread-local capability for exactly one primary attack; nested attacks never inherit it. */
public final class BladeAttackScope {
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();

    private BladeAttackScope() {}

    public static void run(Object player, Object target, Runnable attack) {
        runTracked(player, target, attack);
    }

    public static boolean runTracked(Object player, Object target, Runnable attack) {
        Frame previous = CURRENT.get();
        if (previous != null) { throw new IllegalStateException("Nested blade authorization"); }
        CURRENT.set(new Frame(player, target));
        try { attack.run(); return CURRENT.get().successful; } finally { CURRENT.remove(); }
    }

    public static void recordPrimaryLoss(Object target, Object source, boolean accepted, float loss) {
        Frame frame = CURRENT.get();
        if (frame != null && frame.claimed && frame.depth == 1 && frame.target == target && frame.source == source
                && accepted && Float.isFinite(loss) && loss > 0) { frame.successful = true; }
    }

    public static void enterAttack() { if (CURRENT.get() != null) { CURRENT.get().depth++; } }
    public static void exitAttack() { if (CURRENT.get() != null) { CURRENT.get().depth--; } }
    public static boolean fullStrength(Object player) {
        Frame frame = CURRENT.get();
        return frame != null && frame.player == player && frame.depth == 1;
    }
    public static void bindSource(Object player, Object source) {
        if (fullStrength(player)) { CURRENT.get().source = source; }
    }
    public static boolean claimPrimary(Object target, Object source) {
        Frame frame = CURRENT.get();
        if (frame == null || frame.depth != 1 || frame.claimed || frame.target != target || frame.source != source) { return false; }
        frame.claimed = true;
        return true;
    }
    private static final class Frame {
        final Object player;
        final Object target;
        Object source;
        int depth;
        boolean claimed;
        boolean successful;
        Frame(Object player, Object target) { this.player = player; this.target = target; }
    }
}
