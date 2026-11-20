package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.network.BladeSwingStyle;
import net.minecraft.world.phys.Vec3;

/** Original, texture-free curved ribbons. All inputs are immutable frame snapshots. */
public final class MeowSlashGeometry {
    public static final int LIFETIME = 18;
    public static final int MOTE_COUNT = 32;
    private static final int SEGMENTS = 48;
    private static final int WIDTH_STEPS = 4;

    private MeowSlashGeometry() {}

    public record Slash(Vec3 origin, Vec3 right, Vec3 forward, Vec3 up,
                        float age, float scale, int direction, BladeSwingStyle style) {}

    public static int lifetime(BladeSwingStyle style) {
        return switch (style) {
            case NORMAL -> LIFETIME;
            case DARK -> 10;
            case HEAVY, HEAVY_IMPACT -> 15;
        };
    }

    @FunctionalInterface
    public interface VertexSink {
        void vertex(double x, double y, double z, int color);
    }

    public static void emit(Slash slash, Vec3 cameraRight, Vec3 cameraUp, VertexSink sink) {
        float progress = Math.clamp(slash.age() / lifetime(slash.style()), 0.0F, 1.0F);
        if (progress >= 1.0F || slash.age() < 0.0F) {
            return;
        }
        // Camera-facing paw prints follow the world-space arc and dissolve from heel to toes.
        pawTrail(slash, progress, cameraRight, cameraUp, sink);
        if (slash.style() != BladeSwingStyle.NORMAL) {
            emitDark(slash, progress, sink);
            return;
        }
        // Hold the luminous peak through the first half of the swing, then fade smoothly.
        float tail = Math.clamp((progress - 0.45F) / 0.55F, 0.0F, 1.0F);
        float fade = (0.85F + 0.15F * Math.min(slash.age(), 1.0F))
                * (1.0F - tail * tail * (3.0F - 2.0F * tail));
        // Saturated cherry-blossom pink remains distinct beneath the pale luminous edge.
        ribbon(slash, progress, fade * 0.24F, 1.60, 0.78, 0.0, 0xFF52A0, sink);
        ribbon(slash, progress, fade * 0.72F, 1.80, 0.28, 0.6, 0xFF70B5, sink);
        ribbon(slash, progress, fade, 1.96, 0.085, 0.0, 0xFFD1E8, sink);
        // An expanding halo and two raised crossing ribbons give the sweep volume.
        ribbon(slash, progress, fade * 0.16F, 2.05 + progress * 0.35, 0.88, 0.4, 0xFF4599, sink);
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

    private static void pawTrail(Slash slash, float progress, Vec3 right, Vec3 up, VertexSink sink) {
        int count = slash.style().isHeavy() ? 5 : 3;
        int rgb = slash.style() == BladeSwingStyle.NORMAL ? 0xFFC9E3 : 0xD3AAFF;
        for (int i = 0; i < count; i++) {
            float age = progress - i * 0.075F;
            if (age <= 0 || age >= 0.8F) continue;
            Vec3 pad = point(slash, -0.85 + i * 0.44 + age * 0.45, 1.85 + age * 0.45, 0.12 + age * 0.68);
            double size = 0.078 * (1 - age * 0.5);
            int color = color(rgb, (float)Math.sin(Math.PI * age / 0.8F) * 0.87F);
            pawPad(pad, right, up, size, color, sink);
            for (int toe = -1; toe <= 1; toe++) {
                Vec3 center = pad.add(right.scale(toe * size * 0.95)).add(up.scale(size * (1.5 - Math.abs(toe) * 0.35)));
                pawPad(center, right, up, size * 0.43, color, sink);
            }
        }
    }

    private static void pawPad(Vec3 center, Vec3 right, Vec3 up, double size, int color, VertexSink sink) {
        vertex(center.add(up.scale(size)), color, sink);
        vertex(center.add(right.scale(size)), color, sink);
        vertex(center.subtract(up.scale(size * 0.8)), color, sink);
        vertex(center.subtract(right.scale(size)), color, sink);
    }

    private static void emitDark(Slash slash, float progress, VertexSink sink) {
        float fade = 1 - progress * progress;
        if (!slash.style().isHeavy()) {
            ribbon(slash, progress, fade * 0.55F, 1.36, 0.42, 0.0, 0x4830DC, sink);
            ribbon(slash, progress, fade * 0.90F, 1.53, 0.15, 0.0, 0x683DF7, sink);
            ribbon(slash, progress, fade * 0.86F, 1.63, 0.075, 0.0, 0xB8ACFF, sink);
            return;
        }
        ribbon(slash, progress, fade * 0.36F, 1.78, 0.69, 0.0, 0x7750EC, sink);
        ribbon(slash, progress, fade * 0.80F, 2.02, 0.28, 0.0, 0x9F68FF, sink);
        ribbon(slash, progress, fade, 2.17, 0.095, 0.0, 0xDBCCFF, sink);
        if (slash.style() == BladeSwingStyle.HEAVY_IMPACT && progress < 0.60F) {
            impactRing(slash, progress / 0.60F, sink);
        }
    }

    private static void impactRing(Slash slash, float progress, VertexSink sink) {
        Vec3 flatForward = new Vec3(slash.forward().x, 0, slash.forward().z).normalize();
        Vec3 origin = slash.origin().add(flatForward.scale(1.7)).add(0, -1.02, 0);
        double radius = 0.34 + 1.26 * progress;
        double width = 0.13 * (1 - progress) + 0.035;
        int edge = color(0xA366FF, 0);
        int center = color(0xC1A8FF, 0.82F * (1 - progress));
        for (int segment = 0; segment < 32; segment++) {
            double first = segment * (Math.PI / 16);
            double next = (segment + 1) * (Math.PI / 16);
            for (int half = 0; half < 2; half++) {
                double inner = radius + (half - 1) * width;
                double outer = inner + width;
                int innerColor = half == 0 ? edge : center;
                int outerColor = half == 0 ? center : edge;
                vertex(origin.add(Math.cos(first) * inner, 0, Math.sin(first) * inner), innerColor, sink);
                vertex(origin.add(Math.cos(next) * inner, 0, Math.sin(next) * inner), innerColor, sink);
                vertex(origin.add(Math.cos(next) * outer, 0, Math.sin(next) * outer), outerColor, sink);
                vertex(origin.add(Math.cos(first) * outer, 0, Math.sin(first) * outer), outerColor, sink);
            }
        }
    }

    private static void ribbon(Slash slash, float progress, float opacity, double radius,
                               double width, double wave, int rgb, VertexSink sink) {
        double sweep = slash.style() == BladeSwingStyle.DARK ? 1.48 : 2.70;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double t0 = segment / (double) SEGMENTS;
            double t1 = (segment + 1.0) / SEGMENTS;
            for (int row = 0; row < WIDTH_STEPS; row++) {
                double v0 = row / (double) WIDTH_STEPS;
                double v1 = (row + 1.0) / WIDTH_STEPS;
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, sweep, t0, v0, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, sweep, t1, v0, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, sweep, t1, v1, sink);
                ribbonVertex(slash, progress, opacity, radius, width, wave, rgb, sweep, t0, v1, sink);
            }
        }
    }

    private static void ribbonVertex(Slash slash, float progress, float opacity, double radius,
                                     double width, double wave, int rgb, double sweep, double t, double v,
                                     VertexSink sink) {
        double taper = Math.sin(Math.PI * t);
        double angle = (t - 0.5) * sweep + (progress - 0.25) * 0.85;
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
