package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.item.MeowBladeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

/** Client-only visual history; vanilla swing packets also cover other tracked players. */
public final class MeowSlashEffects {
    public static final int MAX_EFFECTS = 32;
    public static final double MAX_DISTANCE = 32.0;
    private final Map<UUID, SlashSwingTracker> swings = new HashMap<>();
    private final List<ActiveSlash> active = new ArrayList<>();
    private ClientLevel level;
    private int ticks;

    public void tick(Minecraft client) {
        if (level != client.level) {
            level = client.level;
            active.clear();
            swings.clear();
            ticks = 0;
        }
        if (level == null || client.isPaused()) {
            return;
        }
        ticks++;
        active.removeIf(effect -> ticks - effect.started() >= MeowSlashGeometry.LIFETIME);
        var present = new HashSet<UUID>();
        for (var player : level.players()) {
            present.add(player.getUUID());
            SlashSwingTracker tracker = swings.computeIfAbsent(player.getUUID(), key -> new SlashSwingTracker());
            boolean started = tracker.update(player.swinging, player.swingTime);
            if (!started || player.isSpectator() || !player.isAlive()) {
                continue;
            }
            InteractionHand hand = player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm;
            if (!(player.getItemInHand(hand).getItem() instanceof MeowBladeItem blade)) {
                continue;
            }
            // Advance even when culled so moving the camera does not restart the sequence.
            SlashMotion motion = tracker.takeMotion();
            if (client.getCameraEntity() == null
                    || player.distanceToSqr(client.getCameraEntity()) > MAX_DISTANCE * MAX_DISTANCE) {
                continue;
            }
            int direction = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (hand == InteractionHand.OFF_HAND) {
                direction = -direction;
            }
            Vec3 forward = player.getViewVector(1.0F).normalize();
            double yaw = Math.toRadians(player.getYRot());
            Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
            Vec3 up = forward.cross(right).normalize();
            double roll = motion.rollRadians(direction);
            Vec3 tiltedRight = right.scale(Math.cos(roll)).add(up.scale(Math.sin(roll)));
            Vec3 tiltedUp = up.scale(Math.cos(roll)).subtract(right.scale(Math.sin(roll)));
            Vec3 origin = player.getEyePosition().add(0, -0.40, 0).add(forward.scale(0.12));
            if (active.size() >= MAX_EFFECTS) {
                active.removeFirst();
            }
            active.add(new ActiveSlash(origin, tiltedRight, forward, tiltedUp,
                    1.20F + blade.stage().number() * 0.025F, motion.direction(direction), ticks));
        }
        swings.keySet().retainAll(present);
    }

    public List<MeowSlashGeometry.Slash> extract(Vec3 camera, float partialTick) {
        List<MeowSlashGeometry.Slash> frame = new ArrayList<>();
        for (ActiveSlash effect : active) {
            if (effect.origin().distanceToSqr(camera) <= MAX_DISTANCE * MAX_DISTANCE) {
                frame.add(new MeowSlashGeometry.Slash(effect.origin().subtract(camera), effect.right(),
                        effect.forward(), effect.up(), ticks - effect.started() + partialTick,
                        effect.scale(), effect.direction()));
            }
        }
        return List.copyOf(frame);
    }

    private record ActiveSlash(Vec3 origin, Vec3 right, Vec3 forward, Vec3 up,
                               float scale, int direction, int started) {}
}
