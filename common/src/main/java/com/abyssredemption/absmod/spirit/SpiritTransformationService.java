package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Operator-only prototype. Interrupted operations freeze; recovery never invents a replacement sword. */
public final class SpiritTransformationService {
    private SpiritTransformationService() {}

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
        store.replace(before, pending);
        inventory.setItem(slot, ItemStack.EMPTY);
        try {
            if (!level.addFreshEntity(spirit)) { throw failure("spawn_failed"); }
            store.replace(pending, pending.transition("spirit", carrier));
            inventory.setChanged();
            return spirit;
        } catch (RuntimeException exception) {
            spirit.discard();
            if (inventory.getItem(slot).isEmpty()) {
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
        if (destinationSlot < 0 || destinationSlot > 35 || !player.getInventory().getItem(destinationSlot).isEmpty()) {
            throw failure("slot_occupied");
        }
        var pending = before.transition("recalling", new SpiritCarrier(carrier.entityId(),
                UUID.randomUUID(), carrier.sword(), destinationSlot));
        store.replace(before, pending);
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
