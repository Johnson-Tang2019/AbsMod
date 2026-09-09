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
        MeowSlashEffects effects = new MeowSlashEffects();
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
