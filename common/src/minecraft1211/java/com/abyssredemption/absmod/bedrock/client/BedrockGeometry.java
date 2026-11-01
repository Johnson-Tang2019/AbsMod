// SPDX-License-ResourceLocation: MIT
// Coordinate and cube UV conventions adapted from TouhouLittleMaid's embedded SimpleBedrockModel.
// Copyright (c) 2019-2024 tartaric_acid; adaptations (c) 2026 Ab's Mod contributors.
package com.abyssredemption.absmod.bedrock.client;

import com.google.gson.*;
import java.io.Reader;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Limited Bedrock 1.12 geometry adapter. See client-bedrock/UPSTREAM.md for provenance. */
public final class BedrockGeometry {
    private static final String[] FACES = {"down", "up", "north", "south", "west", "east"};
    private static final int[][] ORDER = {{5,4,0,1},{2,3,7,6},{1,0,3,2},{4,5,6,7},{0,4,7,3},{5,1,2,6}};
    private BedrockGeometry() {}

    public record Baked(ModelPart root, Map<String, ModelPart> bones, int width, int height) {}

    public static Baked read(Reader reader) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        if (!"1.12.0".equals(json.get("format_version").getAsString())) {
            throw new IllegalArgumentException("Expected Bedrock geometry 1.12.0");
        }
        var geometries = json.getAsJsonArray("minecraft:geometry");
        if (geometries.size() != 1) { throw new IllegalArgumentException("Exactly one geometry required"); }
        var geometry = geometries.get(0).getAsJsonObject();
        var description = geometry.getAsJsonObject("description");
        int width = dimension(description, "texture_width"), height = dimension(description, "texture_height");
        Map<String, JsonObject> definitions = new LinkedHashMap<>();
        var array = geometry.getAsJsonArray("bones");
        if (array.size() > 256) { throw new IllegalArgumentException("Too many bones"); }
        for (var entry : array) {
            var bone = entry.getAsJsonObject();
            String name = bone.get("name").getAsString();
            if (name.isBlank() || definitions.putIfAbsent(name, bone) != null) {
                throw new IllegalArgumentException("Empty or duplicate bone name");
            }
        }
        Map<String, ModelPart> parts = new LinkedHashMap<>(), roots = new LinkedHashMap<>();
        for (String name : definitions.keySet()) { bake(name, definitions, parts, new HashSet<>(), width, height); }
        definitions.forEach((name, bone) -> { if (!bone.has("parent")) { roots.put(name, parts.get(name)); } });
        if (roots.isEmpty()) { throw new IllegalArgumentException("No root bone"); }
        return new Baked(new ModelPart(List.of(), roots), Map.copyOf(parts), width, height);
    }

    private static ModelPart bake(String name, Map<String, JsonObject> definitions, Map<String, ModelPart> parts,
            Set<String> visiting, int width, int height) {
        if (parts.containsKey(name)) { return parts.get(name); }
        if (!visiting.add(name) || visiting.size() > 32) { throw new IllegalArgumentException("Cyclic or deep hierarchy"); }
        var bone = definitions.get(name);
        if (bone == null) { throw new IllegalArgumentException("Missing bone: " + name); }
        String parent = bone.has("parent") ? bone.get("parent").getAsString() : null;
        if (parent != null && !definitions.containsKey(parent)) { throw new IllegalArgumentException("Missing parent: " + parent); }
        float[] pivot = vector(bone, "pivot", 3), rotation = vector(bone, "rotation", 3);
        float[] parentPivot = parent == null ? new float[]{0,24,0} : vector(definitions.get(parent), "pivot", 3);
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (var entry : definitions.entrySet()) {
            if (entry.getValue().has("parent") && name.equals(entry.getValue().get("parent").getAsString())) {
                children.put(entry.getKey(), bake(entry.getKey(), definitions, parts, visiting, width, height));
            }
        }
        List<ModelPart.Cube> cubes = new ArrayList<>();
        if (bone.has("poly_mesh")) {
            cubes.addAll(BedrockPolyMesh.bake(bone.getAsJsonObject("poly_mesh"), pivot, width, height));
        }
        var source = bone.has("cubes") ? bone.getAsJsonArray("cubes") : new JsonArray();
        if (source.size() > 512) { throw new IllegalArgumentException("Too many cubes"); }
        int index = 0;
        for (var entry : source) {
            var cube = entry.getAsJsonObject();
            float[] cp = cube.has("pivot") ? vector(cube, "pivot", 3) : pivot;
            var mesh = cube(cube, cp, bone.has("mirror") && bone.get("mirror").getAsBoolean(), width, height);
            var part = new ModelPart(mesh, Map.of());
            float[] cr = vector(cube, "rotation", 3);
            pose(part, cp, pivot, cr);
            String cubeName = "absmod_cube_" + index++;
            if (children.putIfAbsent(cubeName, part) != null) { throw new IllegalArgumentException("Reserved cube bone name"); }
        }
        var result = new ModelPart(cubes, children);
        pose(result, pivot, parentPivot, rotation);
        parts.put(name, result);
        visiting.remove(name);
        return result;
    }

    private static void pose(ModelPart part, float[] p, float[] parent, float[] r) {
        float rad = (float) (Math.PI / 180);
        var pose = PartPose.offsetAndRotation(p[0]-parent[0], parent[1]-p[1], p[2]-parent[2], r[0]*rad, r[1]*rad, r[2]*rad);
        part.setInitialPose(pose);
        part.loadPose(pose);
    }

    private static List<ModelPart.Cube> cube(JsonObject cube, float[] pivot, boolean inheritedMirror, int width, int height) {
        if (!cube.has("origin") || !cube.has("size")) { throw new IllegalArgumentException("Cube origin and size required"); }
        float[] o = vector(cube,"origin",3), s = vector(cube,"size",3);
        for (float size : s) { if (size < 0 || size > 512) { throw new IllegalArgumentException("Invalid cube size"); } }
        float inflate = cube.has("inflate") ? number(cube.get("inflate")) : 0;
        float x = o[0]-pivot[0]-inflate, y = pivot[1]-o[1]-s[1]-inflate, z = o[2]-pivot[2]-inflate;
        float dx=s[0]+2*inflate, dy=s[1]+2*inflate, dz=s[2]+2*inflate;
        if (dx < 0 || dy < 0 || dz < 0) { throw new IllegalArgumentException("Negative inflated cube"); }
        float[][] points = {{x,y,z},{x+dx,y,z},{x+dx,y+dy,z},{x,y+dy,z},
                {x,y,z+dz},{x+dx,y,z+dz},{x+dx,y+dy,z+dz},{x,y+dy,z+dz}};
        var uv = cube.get("uv");
        if (uv == null) { throw new IllegalArgumentException("Cube UV missing"); }
        boolean mirror = cube.has("mirror") ? cube.get("mirror").getAsBoolean() : inheritedMirror;
        List<ModelPart.Cube> output = new ArrayList<>();
        for (int face = 0; face < 6; face++) {
            float[] rect; int turn = 0;
            if (uv.isJsonObject()) {
                var faces = uv.getAsJsonObject();
                if (!faces.has(FACES[face])) { continue; }
                var f = faces.getAsJsonObject(FACES[face]);
                float[] start=vector(f,"uv",2), size=vector(f,"uv_size",2);
                rect=new float[]{start[0],start[1],start[0]+size[0],start[1]+size[1]};
                if (f.has("uv_rotation")) {
                    float angle=number(f.get("uv_rotation"));
                    if (angle != 0 && angle != 90 && angle != 180 && angle != 270) { throw new IllegalArgumentException("Invalid UV rotation"); }
                    turn=(int)angle/90;
                }
            } else {
                float[] start = array(uv.getAsJsonArray(),2);
                float u=start[0], v=start[1], w=(float)Math.floor(s[0]), h=(float)Math.floor(s[1]), d=(float)Math.floor(s[2]);
                float[] us={u,u+d,u+d+w,u+d+w+w,u+d+w+d,u+d+w+d+w};
                int[][] order=mirror ? new int[][]{{2,1},{3,2},{2,1},{5,4},{4,2},{1,0}} : new int[][]{{1,2},{2,3},{1,2},{4,5},{0,1},{2,4}};
                rect=new float[]{us[order[face][0]],face<2?v:v+d,us[order[face][1]],face==1?v:face==0?v+d:v+d+h};
                if (face==1) { rect[1]=v+d; }
            }
            if (rect[0]==rect[2] || rect[1]==rect[3]) { continue; }
            float[][] coords={{rect[2]/width,rect[1]/height},{rect[0]/width,rect[1]/height},
                    {rect[0]/width,rect[3]/height},{rect[2]/width,rect[3]/height}};
            ModelPart.Vertex[] vertices=new ModelPart.Vertex[4];
            for (int j=0;j<4;j++) {
                float[] p=points[ORDER[face][j]], t=coords[(j+turn)%4];
                if (t[0]<0 || t[0]>1 || t[1]<0 || t[1]>1) { throw new IllegalArgumentException("UV outside texture"); }
                vertices[j]=new ModelPart.Vertex(p[0],p[1],p[2],t[0],t[1]);
            }
            var a=vertices[0]; var b=vertices[1]; var c=vertices[2];
            var normal=new Vector3f(b.pos.x()-a.pos.x(),b.pos.y()-a.pos.y(),b.pos.z()-a.pos.z()).cross(c.pos.x()-a.pos.x(),c.pos.y()-a.pos.y(),c.pos.z()-a.pos.z());
            if (normal.lengthSquared()<1e-10F) { continue; }
            var polygon=new ModelPart.Cube(0,0,x,y,z,dx,dy,dz,0,0,0,false,1,1,Set.of(Direction.NORTH));
            polygon.polygons[0]=com.abyssredemption.absmod.bedrock.client.BedrockPolygon.create(vertices,normal.normalize());
            output.add(polygon);
        }
        return output;
    }

    private static int dimension(JsonObject object, String key) {
        float value=number(object.get(key));
        if (value<1 || value>4096 || value!=(int)value) { throw new IllegalArgumentException("Invalid texture dimension"); }
        return (int)value;
    }
    private static float[] vector(JsonObject object,String key,int length) {
        return object.has(key) ? array(object.getAsJsonArray(key),length) : new float[length];
    }
    private static float[] array(JsonArray array,int length) {
        if (array.size()!=length) { throw new IllegalArgumentException("Invalid vector length"); }
        float[] values=new float[length];
        for (int i=0;i<length;i++) { values[i]=number(array.get(i)); }
        return values;
    }
    private static float number(JsonElement element) {
        float value=element.getAsFloat();
        if (!Float.isFinite(value) || Math.abs(value)>65536) { throw new IllegalArgumentException("Invalid geometry number"); }
        return value;
    }
}
