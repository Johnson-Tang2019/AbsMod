package com.abyssredemption.absmod.neoforge;
import com.abyssredemption.absmod.client.*;
import com.abyssredemption.absmod.network.ClientPayloadBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
@Mod(value="absmod",dist=Dist.CLIENT)
public final class AbsModNeoForgeClient {
    private final MeowSlashEffects effects=new MeowSlashEffects();
    private final BladeCombatClient combat=new BladeCombatClient();
    public AbsModNeoForgeClient(IEventBus bus) {
        bus.addListener((RegisterColorHandlersEvent.Item e)->e.register((stack,index)->0x66FFD7EF,
            com.abyssredemption.absmod.registry.ModItems.SPECTRAL_BLADES.stream().map(java.util.function.Supplier::get).toArray(net.minecraft.world.item.Item[]::new)));
        bus.addListener((RegisterKeyMappingsEvent e)->e.register(BladeKeys.HEAVY));
        bus.addListener((EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(com.abyssredemption.absmod.entity.SpiritEntityType.get(),com.abyssredemption.absmod.client.spirit.BladeSpiritRenderer::new));
        ClientPayloadBridge.probe=p->combat.setEnabled(Minecraft.getInstance(),p.enabled());
        ClientPayloadBridge.swing=p->effects.confirm(Minecraft.getInstance(),p);
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::render);
    }
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();combat.tick(mc,p->PacketDistributor.sendToServer(p),p->PacketDistributor.sendToServer(p));effects.tick(mc);
    }
    private void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();var camera=event.getCamera();var rotation=camera.rotation();
        MeowSlashRenderer.render(effects.extract(camera.getPosition(),event.getPartialTick().getGameTimeDeltaPartialTick(false)),
            new Vec3(rotation.transform(new Vector3f(1,0,0))),new Vec3(rotation.transform(new Vector3f(0,1,0))),event.getPoseStack(),mc.renderBuffers().bufferSource());
    }
}
