package com.abyssredemption.absmod.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import java.util.List;
/** Two transparent passes using the 1.21.1 buffer submission API. */
public final class MeowSlashRenderer extends RenderStateShard {
    private MeowSlashRenderer() { super("absmod_slash",()->{},()->{}); }
    private static RenderType layer(String name,TransparencyStateShard blend) {
        return RenderType.create(name,DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,1536,false,true,
            RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(blend)
                .setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }
    private static final RenderType BODY=layer("absmod_slash_body",TRANSLUCENT_TRANSPARENCY);
    private static final RenderType GLOW=layer("absmod_slash_glow",LIGHTNING_TRANSPARENCY);
    public static void render(List<MeowSlashGeometry.Slash> frame,Vec3 right,Vec3 up,PoseStack pose,MultiBufferSource.BufferSource buffers) {
        for(RenderType type:new RenderType[]{BODY,GLOW}) {
            var buffer=buffers.getBuffer(type);
            for(var slash:frame)MeowSlashGeometry.emit(slash,right,up,(x,y,z,color)-> {
                int tint=type==GLOW ? (Math.round((color>>>24)*.65F)<<24)|(color&0xffffff):color;
                buffer.addVertex(pose.last(),(float)x,(float)y,(float)z).setColor(tint);
            });
            buffers.endBatch(type);
        }
    }
}
