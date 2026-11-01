package com.abyssredemption.absmod.bedrock.client;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
/** Keep authored per-vertex UV and normals on the 1.21.1 cube renderer. */
public final class BedrockPolygon {
    private BedrockPolygon() {}
    public static ModelPart.Polygon create(ModelPart.Vertex[] vertices, Vector3f normal) {
        var polygon=new ModelPart.Polygon(vertices.clone(),0,0,1,1,1,1,false,Direction.UP);
        System.arraycopy(vertices,0,polygon.vertices,0,vertices.length);
        polygon.normal.set(normal);
        return polygon;
    }
}
