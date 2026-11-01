package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.combat.BladeCombatServer;
import com.abyssredemption.absmod.command.CombatProbeCommands;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import com.abyssredemption.absmod.network.CombatProbePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForgeCombatProbe {
    private final BladeCombatServer probe = new BladeCombatServer((player, payload) -> PacketDistributor.sendToPlayer(player, payload));

    public NeoForgeCombatProbe(IEventBus modBus) {
        modBus.addListener(this::payloads);
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::commands);
        NeoForge.EVENT_BUS.addListener(this::disconnect);
        NeoForge.EVENT_BUS.addListener(this::connected);
        NeoForge.EVENT_BUS.addListener(this::stopped);
    }

    private void payloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("3");
        registrar.playToServer(AttackIntentPayload.TYPE, AttackIntentPayload.CODEC,
                (payload, context) -> probe.receive((ServerPlayer) context.player(), payload));
        registrar.playToServer(com.abyssredemption.absmod.network.HeavyIntentPayload.TYPE,com.abyssredemption.absmod.network.HeavyIntentPayload.CODEC,
            (payload,context)->probe.receiveHeavy((ServerPlayer)context.player(),payload));
        registrar.playToClient(CombatProbePayload.TYPE, CombatProbePayload.CODEC,(payload,context)->com.abyssredemption.absmod.network.ClientPayloadBridge.probe.accept(payload));
        registrar.playToClient(com.abyssredemption.absmod.network.BladeSwingPayload.TYPE, com.abyssredemption.absmod.network.BladeSwingPayload.CODEC,(payload,context)->com.abyssredemption.absmod.network.ClientPayloadBridge.swing.accept(payload));
    }

    private void tick(ServerTickEvent.Post event) { probe.tick(event.getServer()); }
    private void stopped(ServerStoppedEvent event) { probe.clear(); }
    private void connected(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { PacketDistributor.sendToPlayer(player, new CombatProbePayload(true)); }
    }
    private void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { probe.disconnect(player); }
    }
    private void commands(RegisterCommandsEvent event) {
        CombatProbeCommands.register(event.getDispatcher(), probe, (player, payload) -> PacketDistributor.sendToPlayer(player, payload));
    }
}
