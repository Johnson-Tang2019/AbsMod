package com.abyssredemption.absmod.client.animation;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Immutable imported samples. Every evaluation returns fresh pose values. */
public final class BladeMotionPreset {
    public static final ResourceLocation RESOURCE=ResourceLocation.fromNamespaceAndPath("absmod","animations/slashblade_baseline.json");
    public record Transform(Vector3f position, Quaternionf rotation) {
        public Transform blend(Transform next,float t) { return new Transform(new Vector3f(position).lerp(next.position,t),new Quaternionf(rotation).slerp(next.rotation,t)); }
    }
    public record Pose(Map<String,Transform> bones,float weight,float effectRoll) {
        public Transform bone(String name) { return bones.get(name); }
    }
    public record Clip(String id,List<Map<String,Transform>> strike,List<Map<String,Transform>> recovery,float roll) {}
    private final List<Clip> clips;
    private BladeMotionPreset(List<Clip> clips) { this.clips=List.copyOf(clips); }
    public static BladeMotionPreset load(ResourceManager resources) throws IOException {
        try(var reader=new InputStreamReader(resources.open(RESOURCE),StandardCharsets.UTF_8)) { return read(reader); }
    }
    public static BladeMotionPreset read(Reader reader) {
        var json=JsonParser.parseReader(reader).getAsJsonObject();
        if (!"absmod_motion_v1".equals(json.get("format").getAsString())) { throw new IllegalArgumentException("Unsupported motion preset"); }
        List<Clip> clips=new ArrayList<>();
        for(var entry:json.getAsJsonArray("clips")) {
            var c=entry.getAsJsonObject();clips.add(new Clip(c.get("id").getAsString(),frames(c.getAsJsonArray("strike")),frames(c.getAsJsonArray("recovery")),c.get("effect_roll").getAsFloat()));
        }
        if(clips.size()!=5) { throw new IllegalArgumentException("Baseline requires five single-strike clips"); }
        return new BladeMotionPreset(clips);
    }
    private static List<Map<String,Transform>> frames(JsonArray array) {
        if(array.size()<2 || array.size()>512) { throw new IllegalArgumentException("Invalid sample count"); }
        List<Map<String,Transform>> result=new ArrayList<>();
        for(var frame:array) {
            Map<String,Transform> parts=new HashMap<>();
            for(String name:new String[]{"blade","body","torso","head","right arm","left arm"}) {
                var values=frame.getAsJsonObject().getAsJsonArray(name);var p=values.get(0).getAsJsonArray();var q=values.get(1).getAsJsonArray();
                var position=new Vector3f(p.get(0).getAsFloat(),p.get(1).getAsFloat(),p.get(2).getAsFloat());
                var rotation=new Quaternionf(q.get(0).getAsFloat(),q.get(1).getAsFloat(),q.get(2).getAsFloat(),q.get(3).getAsFloat());
                if(!position.isFinite() || !rotation.isFinite() || rotation.lengthSquared()<.001F) { throw new IllegalArgumentException("Invalid motion transform"); }
                parts.put(name,new Transform(position,rotation.normalize()));
            }
            result.add(Map.copyOf(parts));
        }
        return List.copyOf(result);
    }
    public Pose sample(long sequence,float elapsed,float interval) {
        var clip=clips.get((int)Math.floorMod(sequence-1,clips.size()));
        float strikeTime=Math.clamp(interval*.85F,1.5F,6F);
        boolean recovering=elapsed>strikeTime;
        var frames=recovering?clip.recovery:clip.strike;
        // Recovery samples retain 30-fps source timing. A new attack replaces playback, never queues it.
        float cursor=recovering?(elapsed-strikeTime)*3:Math.max(0,elapsed)/strikeTime*(frames.size()-1);
        cursor=Math.clamp(cursor,0,frames.size()-1);
        int lo=(int)cursor,hi=Math.min(lo+1,frames.size()-1);float t=cursor-lo;
        Map<String,Transform> pose=new HashMap<>();
        for(var entry:frames.get(lo).entrySet()) {
            var value=entry.getValue().blend(frames.get(hi).get(entry.getKey()),t);
            // A3/A4 omit additional upstream hits. Blend into the selected recovery instead of snapping.
            if(recovering) { value=clip.strike.getLast().get(entry.getKey()).blend(value,Math.clamp((elapsed-strikeTime)/2F,0,1)); }
            pose.put(entry.getKey(),value);
        }
        float remaining=(frames.size()-1-cursor)/3F;
        float weight=recovering?Math.clamp(remaining/3F,0,1):1;
        return new Pose(Map.copyOf(pose),weight,clip.roll);
    }
}
