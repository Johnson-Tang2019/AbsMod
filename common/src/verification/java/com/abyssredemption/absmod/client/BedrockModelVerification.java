package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.bedrock.client.BedrockGeometry;
import com.abyssredemption.absmod.client.spirit.BedrockSpiritModel;
import com.abyssredemption.absmod.client.spirit.SpiritModelResources;
import com.google.gson.JsonParser;
import java.io.*;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;

/** CPU tests of actual resource loading, recovery and interleaved deferred render poses. */
public final class BedrockModelVerification {
    private BedrockModelVerification() {}
    public static void verify() throws Exception {
        byte[] geometry;
        try (var stream=BedrockModelVerification.class.getResourceAsStream("/assets/absmod/models/entity/blade_spirit_test.geo.json")) {
            geometry=java.util.Objects.requireNonNull(stream).readAllBytes();
        }
        String json=new String(geometry,StandardCharsets.UTF_8);
        var baked=BedrockGeometry.read(new StringReader(json));
        var model=new BedrockSpiritModel(baked);
        require(baked.bones().size()==10 && baked.width()==128 && baked.height()==128,"authored resource");
        require(baked.bones().get("body").getChild("head")==baked.bones().get("head"),"hierarchy preserved");
        require(baked.bones().get("head").getChild("left_ear")==baked.bones().get("left_ear"),"ear parent");
        int[] faces={0};
        baked.root().visit(new com.mojang.blaze3d.vertex.PoseStack(),(pose,path,index,cube)->{
            for (var polygon:cube.polygons) {
                faces[0]++;
                require(Math.abs(polygon.normal().lengthSquared()-1)<0.001,"unit normal");
                for (var vertex:polygon.vertices()) {
                    require(Float.isFinite(vertex.x()+vertex.y()+vertex.z()) && vertex.u()>=0 && vertex.u()<=1
                            && vertex.v()>=0 && vertex.v()<=1,"finite vertices and bounded UV");
                }
            }
        });
        require(faces[0]==60,"ten visible authored cubes");
        var a=new LivingEntityRenderState(); a.ageInTicks=20; a.yRot=40; a.xRot=15; a.walkAnimationSpeed=0.8F; a.walkAnimationPos=2;
        var b=new LivingEntityRenderState(); b.ageInTicks=70; b.yRot=-35;
        model.setupAnim(a);
        float tail=baked.bones().get("tail").yRot, leg=baked.bones().get("left_leg").xRot;
        require(Math.abs(leg)>0.01 && Math.abs(tail)>0.01,"walk and tail active");
        model.setupAnim(b);
        require(baked.bones().get("head").yRot<0 && baked.bones().get("left_leg").xRot==0,"second entity resets first pose");
        model.setupAnim(a);
        require(baked.bones().get("tail").yRot==tail && baked.bones().get("left_leg").xRot==leg,"A B A interleaving deterministic");
        var second=BedrockGeometry.read(new StringReader(json));
        new BedrockSpiritModel(second).setupAnim(b);
        require(baked.bones().get("tail").yRot==tail && second.root()!=baked.root(),"separate renderer instances isolated");
        var invalid=JsonParser.parseString(json).getAsJsonObject();
        var bones=invalid.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
        bones.get(0).getAsJsonObject().addProperty("parent","head");
        reject(invalid.toString());
        bones.get(0).getAsJsonObject().addProperty("parent","missing"); reject(invalid.toString());
        reject(json.replace("1.12.0","1.99.0"));
        int[] mode={0};
        ResourceManager resources=(ResourceManager)Proxy.newProxyInstance(ResourceManager.class.getClassLoader(),new Class<?>[]{ResourceManager.class},(proxy,method,args)->{
            if (!method.getName().equals("open")) { throw new UnsupportedOperationException(method.getName()); }
            String path=args[0].toString().split(":",2)[1];
            if (mode[0]==1) { throw new FileNotFoundException("Injected missing resource"); }
            if (mode[0]==2 && path.endsWith("json")) { return new ByteArrayInputStream("{broken".getBytes(StandardCharsets.UTF_8)); }
            if (mode[0]==3 && path.endsWith("png")) { return new ByteArrayInputStream(new byte[]{0,1,2}); }
            return BedrockModelVerification.class.getResourceAsStream("/assets/absmod/"+path);
        });
        var loaded=SpiritModelResources.load(resources);
        require(!loaded.fallback(),"real JSON and PNG load");
        for (int fault=1;fault<=3;fault++) {
            mode[0]=fault;
            var failed=SpiritModelResources.load(resources);
            require(failed.fallback(),"resource failure selects placeholder");
            failed.model().setupAnim(a);
            mode[0]=0;
            var recovered=SpiritModelResources.load(resources);
            require(!recovered.fallback() && recovered.model()!=loaded.model(),"reload recovers fresh geometry");
        }
        System.out.println("Bedrock model checks passed: hierarchy, poses, fallback and reload recovery (CPU only).");
    }
    private static void reject(String json) {
        try { BedrockGeometry.read(new StringReader(json)); }
        catch (IllegalArgumentException exception) { return; }
        throw new AssertionError("Invalid geometry accepted");
    }
    private static void require(boolean result,String message) { if (!result) { throw new AssertionError(message); } }
}
