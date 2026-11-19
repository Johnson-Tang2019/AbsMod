package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Per-server follow windows opened by login or dimension change. */
public final class SpiritCompanionTravel {
    private static final int POLL_INTERVAL = 10;
    private static final int FOLLOW_WINDOW = 200;
    private static final int MAX_SOURCE_TICKETS = 4;
    private static final TicketType SOURCE_TICKET = new TicketType(FOLLOW_WINDOW,
            TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
                    | TicketType.FLAG_CAN_EXPIRE_IF_UNLOADED);
    private final Map<UUID, Visit> visits = new HashMap<>();

    public void tick(MinecraftServer server) {
        int tick = server.getTickCount();
        if (tick % POLL_INTERVAL != 0) { return; }
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            visits.clear();
            return;
        }
        BladeSoulStore store;
        try {
            store = BladeSoulStore.find(server).orElse(null);
        } catch (IllegalStateException exception) {
            // Missing or damaged records must never authorize delivery.
            return;
        }
        if (store == null) { return; }
        visits.keySet().removeIf(owner -> server.getPlayerList().getPlayer(owner) == null);
        for (ServerPlayer player : players) {
            var dimension = player.level().dimension();
            Visit visit = visits.get(player.getUUID());
            if (visit == null || visit.player != player || !visit.dimension.equals(dimension)) {
                visit = openVisit(player, store, tick);
                visits.put(player.getUUID(), visit);
            }
            if (player.isAlive() && !player.isSpectator()) { processVisit(player, store, visit, tick); }
        }
    }

    public void clear() { visits.clear(); }

    private Visit openVisit(ServerPlayer player, BladeSoulStore store, int tick) {
        var visit = new Visit(player, player.level().dimension(), tick + FOLLOW_WINDOW);
        for (BladeSoulRecord record : store.records()) {
            if (record.ownerId().filter(player.getUUID()::equals).isEmpty() || record.carrier().isEmpty()) { continue; }
            if (!record.form().equals("spirit")) {
                try {
                    if (SpiritTransformationService.recoverInterrupted(player, record.soulId())) {
                        message(player, "recovered");
                    }
                } catch (IllegalStateException exception) {
                    message(player, "error." + exception.getMessage());
                }
                continue;
            }
            var carrier = record.carrier().orElseThrow();
            if (carrier.lastLocation().filter(pos -> !pos.dimension().equals(visit.dimension)).isPresent()) {
                visit.following.add(record.soulId());
            }
        }
        return visit;
    }

    private void processVisit(ServerPlayer player, BladeSoulStore store, Visit visit, int tick) {
        if (visit.following.isEmpty()) { return; }
        if (tick >= visit.deadline) {
            visit.following.clear();
            message(player, "follow_waiting");
            return;
        }
        var level = SpiritTransformationService.levelOf(player);
        for (var iterator = visit.following.iterator(); iterator.hasNext();) {
            UUID soul = iterator.next();
            var record = store.find(soul).orElse(null);
            if (record == null || !record.form().equals("spirit")
                    || record.ownerId().filter(player.getUUID()::equals).isEmpty()) {
                iterator.remove();
                continue;
            }
            var carrier = record.carrier().orElseThrow();
            var position = carrier.lastLocation().orElse(null);
            if (position == null || position.dimension().equals(level.dimension())) {
                iterator.remove();
                continue;
            }
            var source = level.getServer().getLevel(position.dimension());
            if (source == null) { continue; }
            if (source.getEntity(carrier.entityId()) instanceof BladeSpiritEntity spirit) {
                var feet = spirit.findSafeArrival(level, player.blockPosition());
                if (feet == null) { continue; }
                try {
                    if (SpiritTransformationService.followAcrossDimension(player, store, record, spirit, feet)) {
                        iterator.remove();
                        message(player, "followed");
                        return; // Each transfer checkpoints the world, so move one companion per pass.
                    }
                } catch (IllegalStateException exception) {
                    message(player, "error." + exception.getMessage());
                    iterator.remove();
                }
            } else if (visit.tickets.size() < MAX_SOURCE_TICKETS && visit.tickets.add(position)) {
                requestSource(source, position.pos());
            }
        }
    }

    private static void requestSource(ServerLevel source, net.minecraft.core.BlockPos pos) {
        var chunk = new ChunkPos(net.minecraft.core.SectionPos.blockToSectionCoord(pos.getX()),
                net.minecraft.core.SectionPos.blockToSectionCoord(pos.getZ()));
        source.getChunkSource().addTicketWithRadius(SOURCE_TICKET, chunk, 2);
    }

    private static void message(ServerPlayer player, String key) {
        if (player.connection != null) {
            player.sendSystemMessage(Component.translatable("message.absmod.spirit." + key), true);
        }
    }

    private static final class Visit {
        private final ServerPlayer player;
        private final ResourceKey<Level> dimension;
        private final int deadline;
        private final List<UUID> following = new ArrayList<>();
        private final Set<GlobalPos> tickets = new HashSet<>();

        private Visit(ServerPlayer player, ResourceKey<Level> dimension, int deadline) {
            this.player = player;
            this.dimension = dimension;
            this.deadline = deadline;
        }
    }
}
