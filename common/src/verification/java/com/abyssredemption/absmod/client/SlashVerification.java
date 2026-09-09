package com.abyssredemption.absmod.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Verifies swing boundaries and render geometry without requiring a GPU or game world. */
public final class SlashVerification {
    public static void verify() {
        SlashSwingTracker sequence = new SlashSwingTracker();
        SlashSwingTracker otherPlayer = new SlashSwingTracker();
        for (int cycle = 0; cycle < 3; cycle++) {
            for (SlashMotion expected : SlashMotion.values()) {
                for (int idle = 0; idle < 100; idle++) {
                    sequence.update(false, 0);
                }
                check(sequence.takeMotion() == expected, "Idle time must not reset the six-motion sequence");
                check(expected.rollRadians(1) == -expected.rollRadians(-1), "Motion plane must mirror for the other hand");
                check(expected.direction(1) == -expected.direction(-1), "Sweep direction must mirror for the other hand");
            }
        }
        check(otherPlayer.takeMotion() == SlashMotion.DIAGONAL_RIGHT, "Players must have independent sequences");
        System.out.println("PASS: six swing motions, idle persistence, independent players and hand mirroring");
        SlashSwingTracker tracker = new SlashSwingTracker();
        check(!tracker.update(false, 0), "Idle must not emit a slash");
        check(tracker.update(true, -1), "Local swing must start immediately");
        for (int time = 0; time < 4; time++) {
            check(!tracker.update(true, time), "An ongoing swing must not duplicate effects");
        }
        check(tracker.update(true, 0), "An early swing restart must produce a new effect");
        check(!tracker.update(false, 0), "Ending an animation must not emit an effect");
        check(tracker.update(true, 1), "Remote swing first observed after ticking must be detected");

        List<Vertex> right = vertices(3, 1, Vec3.ZERO);
        List<Vertex> left = vertices(3, -1, Vec3.ZERO);
        List<Vertex> translated = vertices(3, 1, new Vec3(50, -20, 100));
        check(!right.isEmpty() && right.size() % 4 == 0 && right.size() <= 10000,
                "Geometry must contain complete quads within the per-effect vertex budget");
        check(right.size() == left.size() && right.size() == translated.size(), "Handedness must preserve topology");
        int maxEarlyAlpha = 0;
        for (int i = 0; i < right.size(); i++) {
            Vertex a = right.get(i);
            Vertex b = left.get(i);
            Vertex c = translated.get(i);
            check(Double.isFinite(a.x) && Double.isFinite(a.y) && Double.isFinite(a.z), "Non-finite vertex");
            check(Math.abs(a.x) < 3 && Math.abs(a.y) < 1 && Math.abs(a.z) < 3, "Unbounded geometry");
            check(Math.abs((c.x - a.x) - 50) < 1e-6 && Math.abs((c.z - a.z) - 100) < 1e-6,
                    "Camera-relative translation must preserve geometry");
            // The ribbons mirror; the final camera-facing mote quads keep their facing.
            if (i < right.size() - MeowSlashGeometry.MOTE_COUNT * 4) {
                check(Math.abs(a.x + b.x) < 1e-6 && Math.abs(a.z - b.z) < 1e-6, "Left-hand slash must mirror");
            }
            maxEarlyAlpha = Math.max(maxEarlyAlpha, a.color >>> 24);
        }
        int maxLateAlpha = vertices(MeowSlashGeometry.LIFETIME - 1, 1, Vec3.ZERO)
                .stream().mapToInt(v -> v.color >>> 24).max().orElse(0);
        check(maxEarlyAlpha > maxLateAlpha && maxLateAlpha > 0, "The glow must fade over its lifetime");
        check(vertices(MeowSlashGeometry.LIFETIME, 1, Vec3.ZERO).isEmpty() && vertices(30, 1, Vec3.ZERO).isEmpty(),
                "Expired effects must emit no geometry");
        System.out.println("PASS: swing deduplication/restarts, slash geometry budget, handedness, translation and fading");
    }

    private static List<Vertex> vertices(float age, int direction, Vec3 origin) {
        List<Vertex> result = new ArrayList<>();
        MeowSlashGeometry.emit(new MeowSlashGeometry.Slash(origin, new Vec3(1, 0, 0),
                        new Vec3(0, 0, 1), new Vec3(0, 1, 0), age, 1, direction),
                new Vec3(1, 0, 0), new Vec3(0, 1, 0),
                (x, y, z, color) -> result.add(new Vertex(x, y, z, color)));
        return result;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record Vertex(double x, double y, double z, int color) {}
}
