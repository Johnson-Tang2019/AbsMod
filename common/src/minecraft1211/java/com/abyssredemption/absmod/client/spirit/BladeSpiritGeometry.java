package com.abyssredemption.absmod.client.spirit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Small resource-pack geometry format with per-face UVs and optional tapered volumes. */
public final class BladeSpiritGeometry {
    private BladeSpiritGeometry() {}

    public static ModelPart read(Reader reader) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        if (!json.get("format").getAsString().equals("absmod_model_v1")) {
            throw new IllegalArgumentException("Unsupported spirit geometry format");
        }
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (var element : json.getAsJsonArray("bones")) {
            var bone = element.getAsJsonObject();
            children.put(bone.get("name").getAsString(), part(bone, 0));
        }
        return new ModelPart(List.of(), children);
    }

    private static ModelPart part(JsonObject bone, int depth) {
        if (depth > 32) { throw new IllegalArgumentException("Spirit bone hierarchy too deep"); }
        List<ModelPart.Cube> cubes = new ArrayList<>();
        for (var entry : bone.getAsJsonArray("cubes")) { cubes.add(cube(entry.getAsJsonObject())); }
        if (bone.has("quads")) {
            for (var entry : bone.getAsJsonArray("quads")) { cubes.add(meshFace(entry.getAsJsonObject())); }
        }
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (var entry : bone.getAsJsonArray("children")) {
            var child = entry.getAsJsonObject();
            children.put(child.get("name").getAsString(), part(child, depth + 1));
        }
        var part = new ModelPart(cubes, children);
        var p = bone.getAsJsonArray("origin");
        var r = bone.getAsJsonArray("rotation");
        var pose = PartPose.offsetAndRotation(value(p, 0), value(p, 1), value(p, 2), value(r, 0), value(r, 1), value(r, 2));
        part.setInitialPose(pose);
        part.loadPose(pose);
        return part;
    }

    private static ModelPart.Cube cube(JsonObject json) {
        var box = json.getAsJsonArray("box");
        float x = value(box, 0), y = value(box, 1), z = value(box, 2);
        float w = value(box, 3), h = value(box, 4), d = value(box, 5);
        if (w <= 0 || h <= 0 || d <= 0) { throw new IllegalArgumentException("Non-positive spirit cube size"); }
        var cube = new ModelPart.Cube(0, 0, x, y, z, w, h, d, 0, 0, 0, false, 1, 1, Set.of(Direction.values()));
        var uv = json.getAsJsonArray("uv");
        var taper = json.getAsJsonArray("taper");
        for (int i = 0; i < cube.polygons.length; i++) {
            var old = cube.polygons[i];
            var rect = uv.get(i).getAsJsonArray();
            float u0 = value(rect, 0), v0 = value(rect, 1), u1 = value(rect, 2), v1 = value(rect, 3);
            if (u0 < 0 || v0 < 0 || u1 > 1 || v1 > 1 || u0 >= u1 || v0 >= v1) {
                throw new IllegalArgumentException("Spirit UV outside atlas");
            }
            var vertices = new ModelPart.Vertex[4];
            for (int j = 0; j < 4; j++) {
                var vertex = old.vertices[j];
                float scale = vertex.pos.y() < y + h / 2 ? value(taper, 0) : value(taper, 1);
                vertices[j] = new ModelPart.Vertex(x + w / 2 + (vertex.pos.x() - x - w / 2) * scale,
                        vertex.pos.y(), z + d / 2 + (vertex.pos.z() - z - d / 2) * scale,
                        j == 0 || j == 3 ? u1 : u0, j < 2 ? v0 : v1);
            }
            var a = vertices[0]; var b = vertices[1]; var c = vertices[2];
            var normal = new Vector3f(b.pos.x() - a.pos.x(), b.pos.y() - a.pos.y(), b.pos.z() - a.pos.z())
                    .cross(c.pos.x() - a.pos.x(), c.pos.y() - a.pos.y(), c.pos.z() - a.pos.z()).normalize();
            if (normal.dot(old.normal) < 0) { normal.negate(); }
            cube.polygons[i] = com.abyssredemption.absmod.bedrock.client.BedrockPolygon.create(vertices, normal);
        }
        return cube;
    }

    private static ModelPart.Cube meshFace(JsonObject face) {
        var points = face.getAsJsonArray("vertices");
        var coordinates = face.getAsJsonArray("uv");
        if (points.size() != 4 || coordinates.size() != 4) { throw new IllegalArgumentException("Expected quad"); }
        var vertices = new ModelPart.Vertex[4];
        float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
        float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (int i = 0; i < 4; i++) {
            var point = points.get(i).getAsJsonArray(); var uv = coordinates.get(i).getAsJsonArray();
            float x = value(point, 0), y = value(point, 1), z = value(point, 2), u = value(uv, 0), v = value(uv, 1);
            if (u < 0 || u > 1 || v < 0 || v > 1) { throw new IllegalArgumentException("Invalid mesh UV"); }
            vertices[i] = new ModelPart.Vertex(x, y, z, u, v);
            minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
        }
        var a = vertices[0]; var b = vertices[1]; var c = vertices[2];
        var normal = new Vector3f(b.pos.x()-a.pos.x(), b.pos.y()-a.pos.y(), b.pos.z()-a.pos.z())
                .cross(c.pos.x()-a.pos.x(), c.pos.y()-a.pos.y(), c.pos.z()-a.pos.z());
        if (normal.lengthSquared() < 1.0e-10F) { throw new IllegalArgumentException("Degenerate mesh face"); }
        var result = new ModelPart.Cube(0, 0, minX, minY, minZ, maxX-minX, maxY-minY, maxZ-minZ,
                0, 0, 0, false, 1, 1, Set.of(Direction.NORTH));
        result.polygons[0] = com.abyssredemption.absmod.bedrock.client.BedrockPolygon.create(vertices, normal.normalize());
        return result;
    }

    private static float value(JsonArray array, int index) {
        float value = array.get(index).getAsFloat();
        if (!Float.isFinite(value)) { throw new IllegalArgumentException("Non-finite spirit geometry value"); }
        return value;
    }
}
