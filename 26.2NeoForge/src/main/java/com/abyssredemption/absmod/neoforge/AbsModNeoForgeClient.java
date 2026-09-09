package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.client.MeowSlashEffects;
import com.abyssredemption.absmod.client.MeowSlashGeometry;
import com.abyssredemption.absmod.client.MeowSlashRenderer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

@Mod(value = AbsMod.MOD_ID, dist = Dist.CLIENT)
public final class AbsModNeoForgeClient {
    private static final ContextKey<List<MeowSlashGeometry.Slash>> SLASHES = new ContextKey<>(
            Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "meow_slashes"));
    private final MeowSlashEffects effects = new MeowSlashEffects();

    public AbsModNeoForgeClient() {
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::extract);
        NeoForge.EVENT_BUS.addListener(this::submit);
    }

    private void tick(ClientTickEvent.Post event) {
        effects.tick(Minecraft.getInstance());
    }

    private void extract(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(SLASHES, effects.extract(event.getCamera().position(),
                event.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
    }

    private void submit(SubmitCustomGeometryEvent event) {
        var rotation = event.getLevelRenderState().cameraRenderState.orientation;
        MeowSlashRenderer.submit(event.getLevelRenderState().getRenderData(SLASHES),
                new Vec3(rotation.transform(new Vector3f(1, 0, 0))),
                new Vec3(rotation.transform(new Vector3f(0, 1, 0))), event.getSubmitNodeCollector());
    }
}
