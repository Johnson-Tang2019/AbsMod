package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Shared manual transitions. Interrupted operations freeze; recovery never invents a replacement sword. */
public final class SpiritTransformationService {
    private SpiritTransformationService() {}

    /** Resolve only the authoritative, currently loaded entity; never recreate a missing carrier. */
    public static boolean recallForAttack(ServerPlayer player) {
        var link = player.getMainHandItem();
        if (!(link.getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem)
                || !player.isAlive() || player.isSpectator()) { return false; }
        var reference = link.get(BladeSoulComponents.REFERENCE);
        if (reference == null) { return false; }
        var record = BladeSoulStore.find(player.level().getServer()).flatMap(store -> store.find(reference.soulId()));
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
        if (com.abyssredemption.absmod.command.SoulCommands.findLoadedCopies(level.getServer(), before.soulId()).size() > 1) {
            throw failure("duplicate");
        }
        var spirit = new BladeSpiritEntity(SpiritEntityType.get(), level);
        spirit.bind(player.getUUID(), before.soulId());
        spirit.setPos(player.getX() + 1, player.getY(), player.getZ());
        if (!level.hasChunkAt(spirit.blockPosition()) || !level.getWorldBorder().isWithinBounds(spirit.blockPosition())
                || !level.noCollision(spirit)) { throw failure("blocked_space"); }
        var carrier = new SpiritCarrier(spirit.getUUID(), UUID.randomUUID(), original, slot)
                .withLocation(GlobalPos.of(level.dimension(), spirit.blockPosition()));
        var pending = before.transition("materializing", carrier);
        var projection = com.abyssredemption.absmod.item.SpectralBladeItem.create(
                ((com.abyssredemption.absmod.item.MeowBladeItem) original.getItem()).stage(), original);
        store.replace(before, pending);
        inventory.setItem(slot, projection);
        try {
            if (!level.addFreshEntity(spirit)) { throw failure("spawn_failed"); }
            store.replace(pending, pending.transition("spirit", carrier));
            inventory.setChanged();
            BladeAdvancements.award(player,"manifest");
            return spirit;
        } catch (RuntimeException exception) {
            spirit.discard();
            if (inventory.getItem(slot).isEmpty() || inventory.getItem(slot) == projection) {
                inventory.setItem(slot, original);
                store.replace(pending, pending.transition("sword", null));
            } else {
                store.replace(pending, pending.transition("recovery_pending", carrier));
            }
            throw exception;
        }
    }

    public static void recall(ServerPlayer player, BladeSpiritEntity spirit, int destinationSlot) {
        var store = BladeSoulStore.find(player.level().getServer()).orElseThrow(() -> failure("missing_record"));
        var before = Optional.ofNullable(spirit.soulId()).flatMap(store::find)
                .orElseThrow(() -> failure("missing_record"));
        requireOwner(player, before);
        var carrier = before.carrier().orElseThrow(() -> failure("inactive"));
        if (!before.form().equals("spirit") || !carrier.entityId().equals(spirit.getUUID())
                || !player.getUUID().equals(spirit.ownerId()) || !spirit.isAlive() || spirit.level() != player.level()) { throw failure("inactive"); }
        if (destinationSlot < 0 || destinationSlot > 35 || (!player.getInventory().getItem(destinationSlot).isEmpty()
                && !com.abyssredemption.absmod.item.SpectralBladeItem.represents(player.getInventory().getItem(destinationSlot), before.soulId()))) {
            throw failure("slot_occupied");
        }
        var pending = before.transition("recalling", new SpiritCarrier(carrier.entityId(),
                UUID.randomUUID(), carrier.sword(), destinationSlot, carrier.lastLocation()));
        store.replace(before, pending);
        // Remove only this soul's inert links; other items and other spirits are untouched.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (com.abyssredemption.absmod.item.SpectralBladeItem.represents(player.getInventory().getItem(slot), before.soulId())) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
        player.getInventory().setItem(destinationSlot, carrier.sword());
        store.replace(pending, pending.transition("sword", null));
        spirit.discard();
        player.getInventory().setChanged();
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
        if (!carrier.entityId().equals(spirit.getUUID())) { return false; }
        if (!carrier.lastLocation().equals(Optional.of(location))) {
            store.replace(record, record.transition("spirit", carrier.withLocation(location)));
        }
        return true;
    }

    /** Reports a loaded carrier or its last observed location without loading chunks. */
    public static Component locate(ServerPlayer player) {
        var link = player.getMainHandItem();
        var reference = link.get(BladeSoulComponents.REFERENCE);
        if (!(link.getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem) || reference == null) {
            throw failure("missing_record");
        }
        var level = levelOf(player);
        var record = BladeSoulStore.find(level.getServer()).flatMap(store -> store.find(reference.soulId()))
                .orElseThrow(() -> failure("missing_record"));
        requireOwner(player, record);
        if (record.form().equals("sword")) { throw failure("inactive"); }
        if (!record.form().equals("spirit")) { throw failure("recovery_pending"); }

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

    private static ServerLevel levelOf(ServerPlayer player) { return player.level(); }

    private static void requireOwner(ServerPlayer player, BladeSoulRecord record) {
        if (!record.ownerId().equals(Optional.of(player.getUUID())) || !player.isAlive() || player.isSpectator()) {
            throw failure("owner_mismatch");
        }
    }
    private static IllegalStateException failure(String message) { return new IllegalStateException(message); }
}
