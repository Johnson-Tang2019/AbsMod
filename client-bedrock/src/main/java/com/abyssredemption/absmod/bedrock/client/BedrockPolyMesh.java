package com.abyssredemption.absmod.bedrock.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Indexed Bedrock polygon subset, baked into the existing 26.2 model submission path. */
final class BedrockPolyMesh {
    private BedrockPolyMesh() {}
    static List<ModelPart.Cube> bake(JsonObject mesh, float[] pivot, int width, int height) {
        var positions=mesh.getAsJsonArray("positions");
        var normals=mesh.getAsJsonArray("normals");
        var uvs=mesh.getAsJsonArray("uvs");
        var polys=mesh.getAsJsonArray("polys");
        if (positions.size()>32768 || normals.size()>32768 || uvs.size()>32768 || polys.size()>8192) {
            throw new IllegalArgumentException("Polygon mesh budget exceeded");
        }
        boolean normalized=mesh.has("normalized_uvs") && mesh.get("normalized_uvs").getAsBoolean();
        List<ModelPart.Cube> output=new ArrayList<>();
        for (var entry:polys) {
            JsonArray indices=entry.getAsJsonArray();
            int count=indices.size();
            if (count!=3 && count!=4) { throw new IllegalArgumentException("Expected indexed triangle or quad"); }
            var vertices=new ModelPart.Vertex[4];
            Vector3f normal=new Vector3f();
            float minX=Float.POSITIVE_INFINITY,minY=minX,minZ=minX;
            float maxX=Float.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
            for (int j=0;j<count;j++) {
                // Reflect Y and reverse winding to preserve the outward-facing surface.
                var index=indices.get(count-1-j).getAsJsonArray();
                if (index.size()!=3) { throw new IllegalArgumentException("Expected position/normal/UV indices"); }
                var p=tuple(positions,index.get(0).getAsFloat(),3);
                var n=tuple(normals,index.get(1).getAsFloat(),3);
                var uv=tuple(uvs,index.get(2).getAsFloat(),2);
                float x=p[0]-pivot[0], y=pivot[1]-p[1], z=p[2]-pivot[2];
                float u=uv[0]/(normalized?1:width),v=uv[1]/(normalized?1:height);
                if (u<0 || u>1 || v<0 || v>1) { throw new IllegalArgumentException("Mesh UV outside atlas"); }
                normal.add(n[0],-n[1],n[2]);
                vertices[j]=new ModelPart.Vertex(x,y,z,u,v);
                minX=Math.min(minX,x); minY=Math.min(minY,y); minZ=Math.min(minZ,z);
                maxX=Math.max(maxX,x); maxY=Math.max(maxY,y); maxZ=Math.max(maxZ,z);
            }
            if (count==3) { vertices[3]=vertices[2]; }
            var a=vertices[0];var b=vertices[1];var c=vertices[2];
            var geometric=new Vector3f(b.x()-a.x(),b.y()-a.y(),b.z()-a.z()).cross(c.x()-a.x(),c.y()-a.y(),c.z()-a.z());
            if (geometric.lengthSquared()<1e-10F || normal.lengthSquared()<1e-10F) { throw new IllegalArgumentException("Degenerate polygon or normal"); }
            if (geometric.dot(normal)<=0) { throw new IllegalArgumentException("Mesh winding disagrees with normals"); }
            var cube=new ModelPart.Cube(0,0,minX,minY,minZ,maxX-minX,maxY-minY,maxZ-minZ,0,0,0,false,1,1,Set.of(Direction.NORTH));
            cube.polygons[0]=new ModelPart.Polygon(vertices,normal.normalize());
            output.add(cube);
        }
        return output;
    }
    private static float[] tuple(JsonArray source,float index,int length) {
        if (!Float.isFinite(index) || index!=(int)index || index<0 || index>=source.size()) { throw new IllegalArgumentException("Invalid mesh index"); }
        var tuple=source.get((int)index).getAsJsonArray();
        if (tuple.size()!=length) { throw new IllegalArgumentException("Invalid mesh tuple"); }
        float[] result=new float[length];
        for (int i=0;i<length;i++) {
            result[i]=tuple.get(i).getAsFloat();
            if (!Float.isFinite(result[i]) || Math.abs(result[i])>65536) { throw new IllegalArgumentException("Invalid mesh number"); }
        }
        return result;
    }
}
