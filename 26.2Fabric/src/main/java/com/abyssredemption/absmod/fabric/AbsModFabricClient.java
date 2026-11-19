package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.client.MeowSlashEffects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class AbsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        net.minecraft.client.gui.screens.MenuScreens.register(com.abyssredemption.absmod.cat.CatJournalMenu.TYPE,
                com.abyssredemption.absmod.client.CatJournalScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(com.abyssredemption.absmod.spirit.ResonanceMenu.TYPE,
                com.abyssredemption.absmod.client.ResonanceScreen::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.abyssredemption.absmod.entity.SpiritEntityType.get(),
                com.abyssredemption.absmod.client.spirit.BladeSpiritRenderer::new);
        net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(com.abyssredemption.absmod.client.BladeKeys.HEAVY);
        net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(com.abyssredemption.absmod.client.BladeKeys.COMBAT_HUD);
        var probe = new com.abyssredemption.absmod.client.BladeCombatClient();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.abyssredemption.absmod.network.CombatProbePayload.TYPE,
                (payload, context) -> probe.setEnabled(context.client(), payload.enabled()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.abyssredemption.absmod.network.BladeCombatStatusPayload.TYPE,
                (payload, context) -> probe.acceptStatus(context.client(), payload));
        ClientTickEvents.END_CLIENT_TICK.register(client -> probe.tick(client,
                payload -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload),
                payload -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload)));
        MeowSlashEffects effects = new MeowSlashEffects();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.abyssredemption.absmod.network.BladeSwingPayload.TYPE,
                (payload, context) -> effects.confirm(context.client(), payload));
        ClientTickEvents.END_CLIENT_TICK.register(effects::tick);
        FabricSlashRenderHook.register(effects);
    }
}
