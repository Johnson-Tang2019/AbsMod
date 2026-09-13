package com.abyssredemption.absmod.client.spirit;

import com.abyssredemption.absmod.bedrock.client.BedrockGeometry;
import com.mojang.logging.LogUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class SpiritModelResources {
    public static final Identifier MODEL = id("models/entity/blade_spirit_test.geo.json");
    public static final Identifier TEXTURE = id("textures/entity/blade_spirit/bedrock_test.png");
    private static final Identifier PLACEHOLDER = id("textures/entity/blade_spirit/placeholder.png");
    private SpiritModelResources() {}
    public record Loaded(BedrockSpiritModel model, Identifier texture, boolean fallback) {}
    public static Loaded load(ResourceManager resources) {
        try (var stream=resources.open(MODEL); var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
            var baked=BedrockGeometry.read(reader);
            try (var texture=resources.open(TEXTURE)) {
                var image=javax.imageio.ImageIO.read(texture);
                if (image==null || image.getWidth()!=baked.width() || image.getHeight()!=baked.height()) { throw new IOException("Model and PNG dimensions differ"); }
            }
            var loaded=new Loaded(new BedrockSpiritModel(baked),TEXTURE,false);
            LogUtils.getLogger().info("Ab's Mod Bedrock spirit loaded: {} ({} bones)",MODEL,baked.bones().size());
            return loaded;
        } catch (IOException | RuntimeException exception) {
            LogUtils.getLogger().warn("Cannot load {}. Using spirit placeholder until next resource reload.",MODEL,exception);
            return fallback();
        }
    }
    /** Built-in geometry survives a missing or malformed resource-pack model. */
    public static Loaded fallback() {
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);
        var root=mesh.getRoot();
        root.getChild("head").addOrReplaceChild("left_ear",CubeListBuilder.create().texOffs(64,0).addBox(-1.5F,-4,-1,3,4,2),PartPose.offset(2.5F,-7,0));
        root.getChild("head").addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(64,0).addBox(-1.5F,-4,-1,3,4,2),PartPose.offset(-2.5F,-7,0));
        root.getChild("body").addOrReplaceChild("tail",CubeListBuilder.create().texOffs(64,16).addBox(-1,0,0,2,10,2),PartPose.offsetAndRotation(0,9,2,0.5F,0,0));
        var part=LayerDefinition.create(mesh,128,64).bakeRoot();
        var bones=new java.util.LinkedHashMap<String,net.minecraft.client.model.geom.ModelPart>();
        for (String name:new String[]{"head","body","left_arm","right_arm","left_leg","right_leg"}) { bones.put(name,part.getChild(name)); }
        bones.put("left_ear",part.getChild("head").getChild("left_ear"));
        bones.put("right_ear",part.getChild("head").getChild("right_ear"));
        bones.put("tail",part.getChild("body").getChild("tail"));
        return new Loaded(new BedrockSpiritModel(new BedrockGeometry.Baked(part,Map.copyOf(bones),128,64)),PLACEHOLDER,true);
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("absmod",path); }
}
