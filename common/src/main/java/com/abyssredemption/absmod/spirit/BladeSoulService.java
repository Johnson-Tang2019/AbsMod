package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.BladeRoute;
import com.abyssredemption.absmod.item.MeowBladeItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;

/** Called by explicit server commands. No getters, item defaults or tick hooks initialize souls. */
public final class BladeSoulService {
    private BladeSoulService() {}

    public static BladeSoulRecord initialize(BladeSoulStore store, ItemStack stack, UUID testOwner) {
        MeowBladeItem blade = requireBlade(stack);
        if (stack.has(BladeSoulComponents.REFERENCE)) { return requireRecord(store, stack); }
        UUID id;
        do { id = UUID.randomUUID(); } while (store.find(id).isPresent());
        BladeSoulRecord record = new BladeSoulRecord(id, Optional.of(testOwner), blade.stage().route(),
                blade.stage().number(), Optional.of(0D), 0, 1, "sword");
        store.insert(record);
        stack.set(BladeSoulComponents.REFERENCE, new BladeSoulReference(id));
        return record;
    }

    public static BladeSoulRecord requireRecord(BladeSoulStore store, ItemStack stack) {
        MeowBladeItem blade = requireBlade(stack);
        BladeSoulReference reference = stack.get(BladeSoulComponents.REFERENCE);
        if (reference == null) { throw new IllegalStateException("uninitialized"); }
        BladeSoulRecord record = store.find(reference.soulId()).orElseThrow(() -> new IllegalStateException("missing_record"));
        if (record.route() != blade.stage().route() || record.stage() != blade.stage().number() || !record.form().equals("sword")) {
            throw new IllegalStateException("mismatch");
        }
        return record;
    }

    public static BladeSoulRecord setTestAffection(BladeSoulStore store, ItemStack stack, double value) {
        BladeSoulRecord before = requireRecord(store, stack);
        BladeSoulRecord after = before.withTestAffection(value);
        store.replace(before, after);
        return after;
    }

    /** Pure migration plan: not an inventory transaction or a playable upgrade entry point. */
    public static MigrationPlan planMigration(BladeSoulStore store, ItemStack source, MeowBladeItem target) {
        BladeSoulRecord before = requireRecord(store, source);
        if (target.stage().route()!=before.route() || target.stage().number() != before.stage() + 1) { throw new IllegalStateException("invalid_stage"); }
        ItemStack result = new ItemStack(target);
        // Only identity transfers. Combat defaults come from the target; other policies remain TBD.
        result.set(BladeSoulComponents.REFERENCE, source.get(BladeSoulComponents.REFERENCE));
        return new MigrationPlan(before, before.withStage(target.stage().number()), result);
    }

    public record MigrationPlan(BladeSoulRecord before, BladeSoulRecord after, ItemStack target) {}

    private static MeowBladeItem requireBlade(ItemStack stack) {
        if (stack.getCount() != 1 || !(stack.getItem() instanceof MeowBladeItem blade)) {
            throw new IllegalStateException("not_blade");
        }
        return blade;
    }
}
