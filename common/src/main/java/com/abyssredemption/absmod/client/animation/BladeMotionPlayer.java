package com.abyssredemption.absmod.client.animation;

import java.util.*;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.network.BladeSwingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/** Per-player latest authorized animation; no damage callbacks or playback queue. */
public final class BladeMotionPlayer {
    private static final Map<UUID,Playback> ACTIVE=new HashMap<>();
    private static final LinkedHashSet<UUID> SEEN=new LinkedHashSet<>();
    private static Object level;
    private static BladeMotionPreset preset;
    public static boolean hideEffects;
    private record Playback(long sequence,long start,float interval,Object item,BladeMotionPreset.Pose previous) {}
    private BladeMotionPlayer() {}
    public static boolean isReady() { return preset!=null; }
    private static boolean locallyBlocked(Player player) {
        var client=Minecraft.getInstance();
        return player==client.player && (client.gui.screen()!=null || client.gameMode==null
                || client.gameMode.isDestroying());
    }
    public static void reload(net.minecraft.server.packs.resources.ResourceManager resources) {
        clear();
        try { preset=BladeMotionPreset.load(resources); }
        catch(Exception e) { preset=null;com.mojang.logging.LogUtils.getLogger().error("Could not load slashblade_baseline; custom motion disabled",e); }
    }
    public static void clear() { ACTIVE.clear();SEEN.clear(); }
    public static void tick(Minecraft client) {
        if(level!=client.level) { level=client.level;clear(); }
        if(client.level==null) { return; }
        ACTIVE.entrySet().removeIf(entry->{var p=client.level.getPlayerByUUID(entry.getKey());return p==null || !eligible(p)
                || p.getMainHandItem().getItem()!=entry.getValue().item || client.level.getGameTime()-entry.getValue().start>50;});
        if(client.player!=null && locallyBlocked(client.player)) { ACTIVE.remove(client.player.getUUID()); }
    }
    private static boolean eligible(Player p) { return p.isAlive() && !p.isSpectator() && !p.isUsingItem() && p.getMainHandItem().getItem() instanceof MeowBladeItem; }
    public static boolean confirm(Minecraft client,BladeSwingPayload packet) {
        if(level!=client.level) { level=client.level;clear(); }
        if(client.level==null || !Float.isFinite(packet.intervalTicks()) || packet.intervalTicks()<=0 || packet.sequence()<1 || !SEEN.add(packet.attackId())) { return false; }
        if(SEEN.size()>512) { SEEN.removeFirst(); }
        var player=client.level.getPlayerByUUID(packet.playerId());
        if(player==null || !eligible(player) || locallyBlocked(player)) { return false; }
        var old=ACTIVE.get(packet.playerId());
        if(old!=null && packet.sequence()<=old.sequence) { return false; }
        var previous=pose(player,0);
        ACTIVE.put(packet.playerId(),new Playback(packet.sequence(),packet.startedTick(),packet.intervalTicks(),player.getMainHandItem().getItem(),previous));
        return true;
    }
    public static BladeMotionPreset.Pose pose(Player player,float partial) {
        if(preset==null || !eligible(player) || locallyBlocked(player)) { return null; }
        var active=ACTIVE.get(player.getUUID());
        if(active==null || active.item!=player.getMainHandItem().getItem()) { return null; }
        float elapsed=player.level().getGameTime()-active.start+partial;
        if(elapsed<0 || elapsed>50) { return null; }
        var pose=preset.sample(active.sequence,elapsed,active.interval);
        if(active.previous!=null && elapsed<.75F) {
            float blend=Math.clamp(elapsed/.75F,0,1);
            Map<String,BladeMotionPreset.Transform> bones=new HashMap<>();
            for(var entry:pose.bones().entrySet()) {
                bones.put(entry.getKey(),active.previous.bone(entry.getKey()).blend(entry.getValue(),blend));
            }
            pose=new BladeMotionPreset.Pose(Map.copyOf(bones),pose.weight(),pose.effectRoll());
        }
        return pose.weight()<=0?null:pose;
    }
}
