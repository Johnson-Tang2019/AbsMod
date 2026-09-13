package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.combat.BladeCombatServer;
import com.abyssredemption.absmod.command.CombatProbeCommands;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import com.abyssredemption.absmod.network.CombatProbePayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricCombatProbe {
    private FabricCombatProbe() {}

    public static void register() {
        var probe = new BladeCombatServer((player, payload) -> ServerPlayNetworking.send(player, payload),
                (player, hit) -> net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.invoker()
                        .interact(player, player.level(), net.minecraft.world.InteractionHand.MAIN_HAND, hit.getEntity(), hit)
                        == net.minecraft.world.InteractionResult.PASS);
        PayloadTypeRegistry.clientboundPlay().register(com.abyssredemption.absmod.network.BladeSwingPayload.TYPE,
                com.abyssredemption.absmod.network.BladeSwingPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(handler.player, new CombatProbePayload(true)));
        PayloadTypeRegistry.serverboundPlay().register(AttackIntentPayload.TYPE, AttackIntentPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CombatProbePayload.TYPE, CombatProbePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AttackIntentPayload.TYPE, (payload, context) -> probe.receive(context.player(), payload));
        ServerTickEvents.END_SERVER_TICK.register(probe::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> probe.disconnect(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> probe.clear());
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
                CombatProbeCommands.register(dispatcher, probe, (player, payload) -> ServerPlayNetworking.send(player, payload)));
    }
}
