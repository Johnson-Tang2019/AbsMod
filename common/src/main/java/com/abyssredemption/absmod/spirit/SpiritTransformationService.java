package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import com.abyssredemption.absmod.command.SoulCommands;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.SpectralBladeItem;
import java.util.Optional;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Shared, journaled handoffs between usable swords and authoritative spirits. */
public final class SpiritTransformationService {
    private SpiritTransformationService() {}

    /** Resolve only the authoritative, currently loaded entity; never recreate a missing carrier. */
    public static boolean recallForAttack(ServerPlayer player) {
        var link = player.getMainHandItem();
        if (!(link.getItem() instanceof SpectralBladeItem)
                || !player.isAlive() || player.isSpectator()) { return false; }
        var reference = link.get(BladeSoulComponents.REFERENCE);
        if (reference == null) { return false; }
        Optional<BladeSoulRecord> record;
        try {
            record = BladeSoulStore.find(levelOf(player).getServer()).flatMap(store -> store.find(reference.soulId()));
        } catch (IllegalStateException exception) { return false; }
        if (record.isEmpty() || !record.get().form().equals("spirit")
                || !record.get().ownerId().equals(Optional.of(player.getUUID()))) { return false; }
        var carrier = record.get().carrier();
        if (carrier.isEmpty() || !(levelOf(player).getEntity(carrier.get().entityId()) instanceof BladeSpiritEntity spirit)) { return false; }
        try { recall(player, spirit, player.getInventory().getSelectedSlot()); return true; }
        catch (IllegalStateException exception) { return false; }
    }

    public static BladeSpiritEntity manifest(ServerPlayer player) {
        var level = levelOf(player);
        var store = BladeSoulStore.find(level.getServer()).orElseThrow(() -> failure("uninitialized"));
        var inventory = player.getInventory();
        int slot = inventory.getSelectedSlot();
        ItemStack original = player.getMainHandItem();
        var before = BladeSoulService.requireRecord(store, original);
        requireOwner(player, before);
        // Loaded-copy diagnostics cannot prove uniqueness in unloaded containers.
        if (SoulCommands.findLoadedCopies(level.getServer(), before.soulId()).size() > 1) {
            throw failure("duplicate");
        }
        var spirit = new BladeSpiritEntity(SpiritEntityType.get(), level);
        spirit.bind(player.getUUID(), before.soulId());
        spirit.setPos(player.getX() + 1, player.getY(), player.getZ());
        if (!level.hasChunkAt(spirit.blockPosition()) || !level.getWorldBorder().isWithinBounds(spirit.blockPosition())
                || !level.noCollision(spirit)) { throw failure("blocked_space"); }
        var server = level.getServer();
        // A first-use blade must have a durable identity before its slot can be replaced.
        SpiritTransferJournal.write(server, before);
        SpiritTransferJournal.checkpointPlayer(player, false);
        var carrier = new SpiritCarrier(spirit.getUUID(), UUID.randomUUID(), original, slot,
                Optional.of(GlobalPos.of(level.dimension(), spirit.blockPosition())), true);
        var pending = before.transition("materializing", carrier);
        var projection = SpectralBladeItem.create(((MeowBladeItem) original.getItem()).stage(), original);
        store.commitHandoff(server, before, pending);
        inventory.setItem(slot, projection);
        try {
            if (!level.addFreshEntity(spirit)) { throw failure("spawn_failed"); }
            SpiritTransferJournal.checkpointManifestation(player, spirit);
            store.commitHandoff(server, pending, pending.transition("spirit", carrier));
        } catch (RuntimeException exception) {
            spirit.discard();
            // The journal owns the snapshot until the recovery delivery is committed.
            try { finishReturning(player, store, pending); }
            catch (IllegalStateException recoveryFailure) { exception.addSuppressed(recoveryFailure); }
            throw exception;
        }
        inventory.setChanged();
        BladeAdvancements.award(player, "manifest");
        return spirit;
    }

    public static void recall(ServerPlayer player, BladeSpiritEntity spirit, int destinationSlot) {
        var server = levelOf(player).getServer();
        var store = BladeSoulStore.find(server).orElseThrow(() -> failure("missing_record"));
        var before = Optional.ofNullable(spirit.soulId()).flatMap(store::find)
                .orElseThrow(() -> failure("missing_record"));
        requireOwner(player, before);
        if (!spirit.isAlive() || spirit.level() != player.level() || !isAuthoritative(before, spirit)) {
            throw failure("inactive");
        }
        if (!availableSlot(player, destinationSlot, before.soulId())) { throw failure("slot_occupied"); }
        if (!SoulCommands.findLoadedCopies(server, before.soulId()).isEmpty()) { throw failure("duplicate"); }

        var carrier = before.carrier().orElseThrow();
        var pending = before.transition("recalling", new SpiritCarrier(carrier.entityId(), UUID.randomUUID(),
                carrier.sword(), destinationSlot, carrier.lastLocation(), true));
        store.commitHandoff(server, before, pending);
        finishReturning(player, store, pending);
        spirit.discard();
    }

    /** Recovery is allowed only for recorded handoffs and is bound to the original owner and slot. */
    public static boolean recoverInterrupted(ServerPlayer player, UUID soul) {
        var server = levelOf(player).getServer();
        var store = BladeSoulStore.find(server).orElseThrow(() -> failure("missing_record"));
        var record = store.find(soul).orElseThrow(() -> failure("missing_record"));
        requireOwner(player, record);
        if (record.form().equals("sword") || record.form().equals("spirit")) { return false; }
        finishReturning(player, store, record);
        return true;
    }

    private static void finishReturning(ServerPlayer player, BladeSoulStore store, BladeSoulRecord pending) {
        var carrier = pending.carrier().orElseThrow();
        var inventory = player.getInventory();
        var sword = carrier.sword();
        var soul = pending.soulId();
        var server = levelOf(player).getServer();
        int originalSlot = -1;
        int linkSlot = -1;
        var previous = new ArrayList<ItemStack>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            previous.add(stack);
            var reference = stack.get(BladeSoulComponents.REFERENCE);
            if (reference == null || !reference.soulId().equals(soul)) { continue; }
            if (SpectralBladeItem.represents(stack, soul)) {
                if (slot < 36 && linkSlot == -1) { linkSlot = slot; }
            } else {
                if (originalSlot != -1 || !ItemStack.matches(stack, sword)) { throw failure("duplicate"); }
                originalSlot = slot;
            }
        }
        var ownSlot = player.getUUID() + "/inventory/" + originalSlot;
        if (SoulCommands.findLoadedCopies(server, soul).stream().anyMatch(location -> !location.equals(ownSlot))) {
            throw failure("duplicate");
        }
        if (!carrier.journaled() && originalSlot == -1 && linkSlot == -1) { throw failure("recovery_pending"); }
        int destination = originalSlot != -1 ? originalSlot : carrier.sourceSlot();
        if (originalSlot == -1 && !availableSlot(player, destination, soul)) {
            if (linkSlot == -1) { throw failure("slot_occupied"); }
            destination = linkSlot;
        }

        for (int slot = 0; slot < previous.size(); slot++) {
            if (SpectralBladeItem.represents(previous.get(slot), soul)) { inventory.setItem(slot, ItemStack.EMPTY); }
        }
        if (originalSlot == -1) { inventory.setItem(destination, sword); }
        try {
            SpiritTransferJournal.checkpointPlayer(player, false);
            store.commitHandoff(server, pending, pending.transition("sword", null));
        } catch (RuntimeException exception) {
            // Hold delivery until both the saved inventory and the journal have accepted it.
            for (int slot = 0; slot < previous.size(); slot++) { inventory.setItem(slot, previous.get(slot)); }
            inventory.setChanged();
            throw exception;
        }
        inventory.setChanged();
    }

    private static boolean availableSlot(ServerPlayer player, int slot, UUID soul) {
        if (slot < 0 || slot > 35) { return false; }
        var stack = player.getInventory().getItem(slot);
        return stack.isEmpty() || SpectralBladeItem.represents(stack, soul);
    }

    static boolean isAuthoritative(BladeSoulRecord record, BladeSpiritEntity spirit) {
        return record.form().equals("spirit") && record.soulId().equals(spirit.soulId())
                && record.ownerId().equals(Optional.ofNullable(spirit.ownerId()))
                && record.carrier().filter(value -> value.entityId().equals(spirit.getUUID()))
                        .filter(value -> value.lastLocation().map(GlobalPos::dimension)
                                .map(spirit.level().dimension()::equals).orElse(true)).isPresent();
    }

    /** Preserves the soul, but fences out the old physical UUID before activating the new one. */
    static boolean followAcrossDimension(ServerPlayer player, BladeSoulStore store, BladeSoulRecord before,
            BladeSpiritEntity source, BlockPos feet) {
        if (!isAuthoritative(before, source) || !source.isAlive() || source.isPassenger() || source.isVehicle()) {
            return false;
        }
        requireOwner(player, before);
        var level = levelOf(player);
        var server = level.getServer();
        var carried = before.carrier().orElseThrow();
        if (!SoulCommands.findLoadedCopies(server, before.soulId()).isEmpty()) { throw failure("duplicate"); }
        var arrival = new BladeSpiritEntity(SpiritEntityType.get(), level);
        arrival.restoreFrom(source);
        arrival.setUUID(UUID.randomUUID());
        arrival.setPos(feet.getX() + .5, feet.getY(), feet.getZ() + .5);
        arrival.setDeltaMovement(Vec3.ZERO);
        arrival.resetFallDistance();

        var savedSource = new SpiritCarrier(carried.entityId(), UUID.randomUUID(), carried.sword(),
                carried.sourceSlot(), carried.lastLocation(), true);
        var pending = before.transition("travelling", savedSource);
        store.commitHandoff(server, before, pending);
        try {
            if (!level.addFreshEntity(arrival)) {
                store.commitHandoff(server, pending, pending.transition("spirit", carried));
                return false;
            }
            var arrived = new SpiritCarrier(arrival.getUUID(), savedSource.operationId(), carried.sword(),
                    carried.sourceSlot(), Optional.of(GlobalPos.of(level.dimension(), feet)), true);
            SpiritTransferJournal.checkpointManifestation(player, arrival);
            store.commitHandoff(server, pending, pending.transition("spirit", arrived));
        } catch (RuntimeException exception) {
            arrival.discard();
            throw exception;
        }
        source.discard();
        return true;
    }

    /** Called at the first server tick and when an active carrier enters a new chunk. */
    public static boolean rememberLocation(ServerLevel level, BladeSpiritEntity spirit, GlobalPos location) {
        var store = BladeSoulStore.find(level.getServer()).orElse(null);
        if (store == null || spirit.soulId() == null) { return false; }
        var record = store.find(spirit.soulId()).orElse(null);
        if (record == null || !record.form().equals("spirit")
                || record.ownerId().isEmpty() || !record.ownerId().get().equals(spirit.ownerId())) {
            return false;
        }
        var carrier = record.carrier().orElseThrow();
        if (!isAuthoritative(record, spirit)) { return false; }
        if (!carrier.lastLocation().equals(Optional.of(location))) {
            store.replace(record, record.transition("spirit", carrier.withLocation(location)));
        }
        return true;
    }

    /** Reports a loaded or last known position, or retries a pending owner handoff. */
    public static Component locate(ServerPlayer player) {
        var link = player.getMainHandItem();
        var reference = link.get(BladeSoulComponents.REFERENCE);
        if (!(link.getItem() instanceof SpectralBladeItem) || reference == null) {
            throw failure("missing_record");
        }
        var level = levelOf(player);
        var record = BladeSoulStore.find(level.getServer()).flatMap(store -> store.find(reference.soulId()))
                .orElseThrow(() -> failure("missing_record"));
        requireOwner(player, record);
        if (record.form().equals("sword")) { throw failure("inactive"); }
        if (!record.form().equals("spirit")) {
            if (recoverInterrupted(player, record.soulId())) {
                return Component.translatable("message.absmod.spirit.recovered");
            }
            throw failure("recovery_pending");
        }

        var carrier = record.carrier().orElseThrow();
        if (level.getEntity(carrier.entityId()) instanceof BladeSpiritEntity spirit
                && spirit.isAlive() && reference.soulId().equals(spirit.soulId()) && spirit.active()) {
            return locationMessage("located", GlobalPos.of(level.dimension(), spirit.blockPosition()));
        }
        return carrier.lastLocation().map(location -> locationMessage("last_location", location))
                .orElseGet(() -> Component.translatable("message.absmod.spirit.location_unknown"));
    }

    private static Component locationMessage(String key, GlobalPos location) {
        var pos = location.pos();
        return Component.translatable("message.absmod.spirit." + key, location.dimension().identifier().toString(),
                pos.getX(), pos.getY(), pos.getZ());
    }

    static ServerLevel levelOf(ServerPlayer player) { return player.level(); }

    private static void requireOwner(ServerPlayer player, BladeSoulRecord record) {
        if (!record.ownerId().equals(Optional.of(player.getUUID())) || !player.isAlive() || player.isSpectator()) {
            throw failure("owner_mismatch");
        }
    }
    private static IllegalStateException failure(String message) { return new IllegalStateException(message); }
}
