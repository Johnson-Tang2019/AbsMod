package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.client.MeowSlashEffects;
import com.abyssredemption.absmod.client.MeowSlashGeometry;
import com.abyssredemption.absmod.client.MeowSlashRenderer;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class AbsModFabricClient implements ClientModInitializer {
    private static final RenderStateDataKey<List<MeowSlashGeometry.Slash>> SLASHES =
            RenderStateDataKey.create(() -> "absmod:meow_slashes");

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.abyssredemption.absmod.entity.SpiritEntityType.get(),
                com.abyssredemption.absmod.client.spirit.BladeSpiritRenderer::new);
        var probe = new com.abyssredemption.absmod.client.BladeCombatClient();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.abyssredemption.absmod.network.CombatProbePayload.TYPE,
                (payload, context) -> probe.setEnabled(context.client(), payload.enabled()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> probe.tick(client,
                payload -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload)));
        MeowSlashEffects effects = new MeowSlashEffects();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.abyssredemption.absmod.network.BladeSwingPayload.TYPE,
                (payload, context) -> effects.confirm(context.client(), payload));
        ClientTickEvents.END_CLIENT_TICK.register(effects::tick);
        LevelExtractionEvents.END_EXTRACTION.register(context -> context.levelState().setData(SLASHES,
                effects.extract(context.camera().position(), context.deltaTracker().getGameTimeDeltaPartialTick(false))));
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            var rotation = context.levelState().cameraRenderState.orientation;
            MeowSlashRenderer.submit(context.levelState().getData(SLASHES),
                    new Vec3(rotation.transform(new Vector3f(1, 0, 0))),
                    new Vec3(rotation.transform(new Vector3f(0, 1, 0))), context.submitNodeCollector());
        });
    }
}
