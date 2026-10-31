package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import java.util.Optional;
import java.util.UUID;
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
        if (carrier.isEmpty() || !(player.level().getEntity(carrier.get().entityId()) instanceof BladeSpiritEntity spirit)) { return false; }
        try { recall(player, spirit, player.getInventory().getSelectedSlot()); return true; }
        catch (IllegalStateException exception) { return false; }
    }

    public static BladeSpiritEntity manifest(ServerPlayer player) {
        var level = player.level();
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
        var carrier = new SpiritCarrier(spirit.getUUID(), UUID.randomUUID(), original, slot);
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
                || spirit.isRemoved() || spirit.level() != player.level()) { throw failure("inactive"); }
        if (destinationSlot < 0 || destinationSlot > 35 || (!player.getInventory().getItem(destinationSlot).isEmpty()
                && !com.abyssredemption.absmod.item.SpectralBladeItem.represents(player.getInventory().getItem(destinationSlot), before.soulId()))) {
            throw failure("slot_occupied");
        }
        var pending = before.transition("recalling", new SpiritCarrier(carrier.entityId(),
                UUID.randomUUID(), carrier.sword(), destinationSlot));
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

    private static void requireOwner(ServerPlayer player, BladeSoulRecord record) {
        if (!record.ownerId().equals(Optional.of(player.getUUID())) || !player.isAlive() || player.isSpectator()) {
            throw failure("owner_mismatch");
        }
    }
    private static IllegalStateException failure(String message) { return new IllegalStateException(message); }
}
