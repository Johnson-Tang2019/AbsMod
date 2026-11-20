package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Items and pending settlement live in the same player snapshot as the inventory. */
public final class ResonanceEscrow extends SimpleContainer {
    public static final String KEY = "absmod_resonance";
    static final int INPUT_SLOTS = 9;
    static final int MENU_SLOTS = 27;

    public interface Holder {
        ResonanceEscrow absmod$resonance();
    }

    private static final Codec<List<ItemStack>> INPUTS = ItemStack.OPTIONAL_CODEC.listOf(INPUT_SLOTS, INPUT_SLOTS);

    private record Pending(BladeSoulRecord before, List<ItemStack> output) {
        private static final Codec<Pending> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BladeSoulRecord.CODEC.fieldOf("before").forGetter(Pending::before),
                INPUTS.fieldOf("output").forGetter(Pending::output)
        ).apply(instance, Pending::new));
    }

    private record Snapshot(List<ItemStack> inputs, ItemStack cursor, Optional<Pending> pending) {
        private static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                INPUTS.fieldOf("inputs").forGetter(Snapshot::inputs),
                ItemStack.OPTIONAL_CODEC.fieldOf("cursor").forGetter(Snapshot::cursor),
                Pending.CODEC.optionalFieldOf("pending").forGetter(Snapshot::pending)
        ).apply(instance, Snapshot::new));
    }

    private ItemStack cursor = ItemStack.EMPTY;
    private Pending pending;
    private CompoundTag unreadable;

    public ResonanceEscrow() {
        super(MENU_SLOTS);
    }

    static ResonanceEscrow of(ServerPlayer player) {
        return ((Holder) player).absmod$resonance();
    }

    public CompoundTag save(ServerPlayer player) {
        if (unreadable != null) {
            return unreadable;
        }
        ItemStack carried = player.containerMenu instanceof ResonanceMenu menu && !menu.getCarried().isEmpty()
                ? menu.getCarried() : cursor;
        var inputs = copyInputs();
        if (pending == null && carried.isEmpty() && inputs.stream().allMatch(ItemStack::isEmpty)) {
            return null;
        }
        var snapshot = new Snapshot(inputs, carried, Optional.ofNullable(pending));
        return (CompoundTag) Snapshot.CODEC.encodeStart(
                player.registryAccess().createSerializationContext(NbtOps.INSTANCE), snapshot).getOrThrow();
    }

    public void load(ServerPlayer player, CompoundTag tag) {
        unreadable = null;
        pending = null;
        cursor = ItemStack.EMPTY;
        clearContent();
        if (tag == null) {
            return;
        }
        // Only a complete decode can return items or settle a transaction.
        var decoded = Snapshot.CODEC.parse(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                .ifError(error -> LogUtils.getLogger().error("Invalid resonance escrow for {}: {}", player.getUUID(), error.message()))
                .result();
        if (decoded.isEmpty()) {
            unreadable = tag.copy();
            return;
        }
        var contents = decoded.orElseThrow();
        setInputs(contents.inputs());
        cursor = contents.cursor();
        pending = contents.pending().orElse(null);
    }

    public static boolean recover(ServerPlayer player) {
        var escrow = of(player);
        try {
            if (escrow.unreadable != null) {
                throw new IllegalStateException("unreadable");
            }
            escrow.settle(player);
            return true;
        } catch (IllegalStateException error) {
            LogUtils.getLogger().error("Could not settle resonance for {}", player.getUUID(), error);
            notify(player, "recovery_blocked");
            return false;
        }
    }

    public static void onLogin(ServerPlayer player) {
        var escrow = of(player);
        if (recover(player) && escrow.returnItems(player.getInventory())) {
            notify(player, "recovery_retained");
        }
    }

    static void notify(ServerPlayer player, String key) {
        if (player.connection != null) {
            player.sendSystemMessage(Component.translatable("resonance.absmod." + key), false);
        }
    }

    boolean locked() {
        return pending != null || unreadable != null;
    }

    ItemStack takeCursor() {
        ItemStack retained = cursor;
        cursor = ItemStack.EMPTY;
        return retained;
    }

    void retainCursor(ItemStack carried) {
        if (!carried.isEmpty()) {
            cursor = carried;
        }
    }

    List<ItemStack> copyInputs() {
        return IntStream.range(0, INPUT_SLOTS).mapToObj(slot -> getItem(slot).copy()).toList();
    }

    private void setInputs(List<ItemStack> inputs) {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            setItem(i, inputs.get(i).copy());
        }
    }

    void prepare(ServerPlayer player, BladeSoulRecord before, List<ItemStack> output) {
        if (locked()) {
            throw new IllegalStateException("recovery_blocked");
        }
        verifyCarriers(player, before, output.getFirst());
        SpiritTransferJournal.write(player.level().getServer(), before);
        pending = new Pending(before, output);
    }

    // Repeating settlement after a restart either advances the soul once or uses its committed record.
    void settle(ServerPlayer player) {
        if (pending == null) {
            return;
        }
        var before = pending.before();
        var sword = pending.output().getFirst();
        verifyCarriers(player, before, sword);
        var server = player.level().getServer();
        var store = BladeSoulStore.find(server).orElseThrow(() -> new IllegalStateException("missing_record"));
        var current = store.find(before.soulId()).orElseThrow(() -> new IllegalStateException("missing_record"));
        var after = before.withStage(before.stage() + 1);
        if (!current.equals(before) && !current.equals(after)) {
            throw new IllegalStateException("stale_revision");
        }
        SpiritTransferJournal.checkpointResonance(player);
        if (current.equals(before)) {
            store.commitHandoff(server, current, after);
        }
        if (after.stage() == 9 && after.mirrorCleared())
            com.abyssredemption.absmod.entity.MirrorEchoEntity.restoreVictory(player);
        BladeAdvancements.award(player, after.route().getSerializedName() + "_stage_" + after.stage());
        if (player.connection != null) {
            player.getAdvancements().save();
        }
        setInputs(pending.output());
        pending = null;
    }

    private void verifyCarriers(ServerPlayer player, BladeSoulRecord before, ItemStack output) {
        if (!before.form().equals("sword") || !before.ownerId().filter(player.getUUID()::equals).isPresent()
                || !matches(getItem(0), before, before.stage()) || !matches(output, before, before.stage() + 1)) {
            throw new IllegalStateException("mismatch");
        }
        var activeCursor = player.containerMenu instanceof ResonanceMenu menu ? menu.getCarried() : ItemStack.EMPTY;
        if (hasSoul(activeCursor, before) || hasSoul(cursor, before)) {
            throw new IllegalStateException("duplicate");
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (hasSoul(player.getInventory().getItem(slot), before)) {
                throw new IllegalStateException("duplicate");
            }
        }
        for (int slot = 1; slot < INPUT_SLOTS; slot++) {
            if (hasSoul(getItem(slot), before)) {
                throw new IllegalStateException("duplicate");
            }
        }
    }

    private static boolean matches(ItemStack stack, BladeSoulRecord before, int stage) {
        return stack.getCount() == 1 && stack.getItem() instanceof MeowBladeItem blade
                && blade.stage().route() == before.route() && blade.stage().number() == stage && hasSoul(stack, before);
    }

    private static boolean hasSoul(ItemStack stack, BladeSoulRecord record) {
        var reference = stack.get(BladeSoulComponents.REFERENCE);
        return reference != null && reference.soulId().equals(record.soulId());
    }

    /** Returns what fits. Backed-up stacks stay in the player save and can be retrieved by reopening. */
    boolean returnItems(Inventory inventory) {
        if (locked()) {
            return false;
        }
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            var stack = getItem(slot);
            insert(inventory, stack);
            if (stack.isEmpty()) {
                setItem(slot, ItemStack.EMPTY);
            }
        }
        insert(inventory, cursor);
        inventory.setChanged();
        return !cursor.isEmpty() || IntStream.range(0, INPUT_SLOTS).anyMatch(slot -> !getItem(slot).isEmpty());
    }

    private static void insert(Inventory inventory, ItemStack stack) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && !stack.isEmpty(); slot++) {
            var existing = inventory.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int count = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (count > 0) {
                    existing.grow(count);
                    stack.shrink(count);
                }
            }
        }
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && !stack.isEmpty(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.split(stack.getMaxStackSize()));
            }
        }
    }
}
