package com.abyssredemption.absmod.client;

import net.minecraft.world.phys.Vec3;

/** Original, texture-free curved ribbons. All inputs are immutable frame snapshots. */
public final class MeowSlashGeometry {
    public static final int LIFETIME = 18;
    public static final int MOTE_COUNT = 32;
    private static final int SEGMENTS = 48;
    private static final int WIDTH_STEPS = 4;

    private MeowSlashGeometry() {}

    public record Slash(Vec3 origin, Vec3 right, Vec3 forward, Vec3 up,
                        float age, float scale, int direction) {}

    @FunctionalInterface
    public interface VertexSink {
        void vertex(double x, double y, double z, int color);
    }

    public static void emit(Slash slash, Vec3 cameraRight, Vec3 cameraUp, VertexSink sink) {
        float progress = Math.clamp(slash.age() / LIFETIME, 0.0F, 1.0F);
        if (progress >= 1.0F || slash.age() < 0.0F) {
            return;
        }
        // Hold the luminous peak through the first half of the swing, then fade smoothly.
        float tail = Math.clamp((progress - 0.45F) / 0.55F, 0.0F, 1.0F);
        float fade = (0.85F + 0.15F * Math.min(slash.age(), 1.0F))
                * (1.0F - tail * tail * (3.0F - 2.0F * tail));
        // Saturated cherry-blossom pink remains distinct beneath the pale luminous edge.
        ribbon(slash, progress, fade * 0.36F, 1.60, 0.48, 0.0, 0xFF52A0, sink);
        ribbon(slash, progress, fade * 0.72F, 1.80, 0.28, 0.6, 0xFF70B5, sink);
        ribbon(slash, progress, fade, 1.96, 0.085, 0.0, 0xFFD1E8, sink);
        // An expanding halo and two raised crossing ribbons give the sweep volume.
        ribbon(slash, progress, fade * 0.24F, 2.05 + progress * 0.35, 0.55, 0.4, 0xFF4599, sink);
        ribbon(slash, progress, fade * 0.72F, 2.16, 0.045, 2.2, 0xFFB8DA, sink);
        ribbon(slash, progress, fade * 0.48F, 1.76, 0.15, 4.8, 0xFF77BA, sink);
        for (int i = 0; i < 6; i++) {
            ribbon(slash, progress, fade * 0.65F, 1.30 + i * 0.18,
                    0.035 + i * 0.012, 1.3 + i * 1.2, 0xFF8EC7, sink);
        }
        // Rotating petal-shaped motes use the camera basis; the slash stays world-oriented.
        for (int i = 0; i < MOTE_COUNT; i++) {
            double t = (i + 0.5) / MOTE_COUNT;
            double angle = -1.3 + 2.6 * t + progress * 0.35;
            Vec3 point = point(slash, angle, 1.65 + 0.50 * Math.sin(i * 9.7) + progress * 0.40,
                    Math.sin(i * 4.1) * 0.28 + progress * (0.25 + t * 0.55));
            double size = (0.025 + 0.030 * (0.5 + 0.5 * Math.sin(i * 7.3))) * (1.0 - progress);
            double spin = i * 2.4 + progress * 3.0;
            Vec3 petalUp = cameraUp.scale(Math.cos(spin)).add(cameraRight.scale(Math.sin(spin)));
            Vec3 petalRight = cameraRight.scale(Math.cos(spin)).subtract(cameraUp.scale(Math.sin(spin)));
            int color = color(0xFFBBDD, fade * (float) (0.65 + 0.35 * Math.sin(Math.PI * t)));
            vertex(point.add(petalUp.scale(size * 2.1)), color, sink);
            vertex(point.add(petalRight.scale(size)), color, sink);
            vertex(point.add(petalUp.scale(-size * 1.3)), color, sink);
            vertex(point.add(petalRight.scale(-size)), color, sink);
        }
    }

    private static void ribbon(Slash slash, float progress, float opacity, double radius,
                               double width, double wave, int rgb, VertexSink sink) {
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double t0 = segment / (double) SEGMENTS;
            double t1 = (segment + 1.0) / SEGMENTS;
            for (int row = 0; row < WIDTH_STEPS; row++) {
                double v0 = row / (double) WIDTH_STEPS;
                double v1 = (row + 1.0) / WIDTH_STEPS;
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, t0, v0, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, t1, v0, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, t1, v1, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, t0, v1, sink);
            }
        }
    }

    private static void ribbonVertex(Slash slash, float progress, float opacity, double radius,
                                     double width, double wave, int rgb, double t, double v,
                                     VertexSink sink) {
        double taper = Math.sin(Math.PI * t);
        double angle = -1.35 + t * 2.70 + (progress - 0.25) * 0.85;
        double ripple = wave == 0.0 ? 0.0 : Math.sin(t * 9.0 + wave + progress * 3.0);
        double r = radius + (v - 0.5) * width * taper + ripple * 0.055;
        double height = ripple * (0.12 + progress * 0.24) + (v - 0.5) * width * 0.38;
        float alpha = opacity * (float) (taper * Math.sin(Math.PI * v));
        // Avoid allocating vectors for every vertex in the render hot path.
        double radial = r * (1.0 + progress * 0.10) * slash.scale();
        double x = Math.sin(angle) * radial * slash.direction();
        double z = Math.cos(angle) * radial;
        double y = height * slash.scale();
        sink.vertex(slash.origin().x + slash.right().x * x + slash.forward().x * z + slash.up().x * y,
                slash.origin().y + slash.right().y * x + slash.forward().y * z + slash.up().y * y,
                slash.origin().z + slash.right().z * x + slash.forward().z * z + slash.up().z * y,
                color(rgb, alpha));
    }

    private static Vec3 point(Slash slash, double angle, double radius, double height) {
        return slash.origin()
                .add(slash.right().scale(Math.sin(angle) * radius * slash.scale() * slash.direction()))
                .add(slash.forward().scale(Math.cos(angle) * radius * slash.scale()))
                .add(slash.up().scale(height * slash.scale()));
    }

    private static int color(int rgb, float alpha) {
        return (Math.clamp(Math.round(alpha * 255), 0, 255) << 24) | rgb;
    }

    private static void vertex(Vec3 point, int color, VertexSink sink) {
        sink.vertex(point.x, point.y, point.z, color);
    }
}
