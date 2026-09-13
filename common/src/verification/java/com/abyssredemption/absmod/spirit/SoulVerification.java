package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.MeowBladeStage;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.SavedDataStorage;

/** Pure service/codec tests plus actual SavedDataStorage file I/O, without a running game server. */
public final class SoulVerification {
    public static void verify() throws Exception {
        // This standalone JVM has no loader registration phase. Reopen the fixture registries,
        // then refreeze them before exercising real codecs. This does not test loader lifecycle hooks.
        var frozen = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozen.setAccessible(true);
        frozen.setBoolean(BuiltInRegistries.DATA_COMPONENT_TYPE, false);
        frozen.setBoolean(BuiltInRegistries.ITEM, false);
        frozen.setBoolean(BuiltInRegistries.BLOCK, false);
        var intrusive = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        intrusive.setAccessible(true);
        intrusive.set(BuiltInRegistries.ITEM, new java.util.IdentityHashMap<>());
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, BladeSoulComponents.ID, BladeSoulComponents.REFERENCE);
        MeowBladeItem first = register(MeowBladeStage.STAGE_1);
        MeowBladeItem second = register(MeowBladeStage.STAGE_2);
        for (var stage : MeowBladeStage.values()) {
            if (stage.number() > 2) { register(stage); }
        }
        var tags = net.minecraft.core.MappedRegistry.class.getDeclaredField("allTags");
        tags.setAccessible(true);
        var unbound = tags.getType().getDeclaredMethod("unbound");
        unbound.setAccessible(true);
        for (var registry : java.util.List.of(BuiltInRegistries.DATA_COMPONENT_TYPE, BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK)) {
            tags.set(registry, unbound.invoke(null));
        }
        BuiltInRegistries.DATA_COMPONENT_TYPE.freeze();
        BuiltInRegistries.ITEM.freeze();
        BuiltInRegistries.BLOCK.freeze();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(net.minecraft.data.registries.VanillaRegistries.createLookup())
                .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        BladeSoulStore store = new BladeSoulStore();
        ItemStack source = new ItemStack(first);
        ItemStack other = new ItemStack(first);
        UUID owner = UUID.randomUUID();
        rejects(() -> BladeSoulService.requireRecord(store, source), "uninitialized");
        check(store.records().isEmpty() && !store.isDirty() && !source.has(BladeSoulComponents.REFERENCE), "Query mutated state");
        var record = BladeSoulService.initialize(store, source, owner);
        var otherRecord = BladeSoulService.initialize(store, other, owner);
        check(!record.soulId().equals(otherRecord.soulId()), "Independent blades share identity");
        check(record.affection().isEmpty(), "Invented default affection");
        store.setDirty(false);
        check(BladeSoulService.initialize(store, source, owner).equals(record) && !store.isDirty(), "Repeated init changed identity");
        check(BladeSoulService.requireRecord(store, source).equals(record) && !store.isDirty(), "Query dirtied store");
        BladeSoulStore empty = new BladeSoulStore();
        rejects(() -> BladeSoulService.initialize(empty, source, owner), "missing_record");
        check(empty.records().isEmpty() && !empty.isDirty(), "Missing archive silently recreated");

        var updated = BladeSoulService.setTestAffection(store, source, 37.5);
        check(updated.revision() == 1 && updated.affection().orElseThrow() == 37.5, "Affection update failed");
        rejects(() -> BladeSoulService.setTestAffection(store, source, Double.NaN), "non_finite");
        rejects(() -> BladeSoulService.setTestAffection(store, source, Double.POSITIVE_INFINITY), "non_finite");
        rejects(() -> store.replace(record, record.withTestAffection(2)), "stale_revision");
        check(store.find(record.soulId()).orElseThrow().equals(updated), "Rejected update changed archive");
        var itemTag = ItemStack.CODEC.encodeStart(ops, source).getOrThrow();
        ItemStack restoredItem = ItemStack.CODEC.parse(ops, itemTag).getOrThrow();
        check(restoredItem.get(BladeSoulComponents.REFERENCE).equals(source.get(BladeSoulComponents.REFERENCE)), "Item reference round trip failed");

        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), registries);
        try {
            BladeSoulComponents.REFERENCE.streamCodec().encode(buffer, source.get(BladeSoulComponents.REFERENCE));
            check(BladeSoulComponents.REFERENCE.streamCodec().decode(buffer).equals(source.get(BladeSoulComponents.REFERENCE)),
                    "Network reference round trip failed");
        } finally { buffer.release(); }

        source.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        var plan = BladeSoulService.planMigration(store, source, second);
        check(plan.after().soulId().equals(updated.soulId()) && plan.after().ownerId().equals(updated.ownerId())
                && plan.after().affection().equals(updated.affection()) && plan.after().stage() == 2, "Migration lost identity or affection");
        check(plan.target().get(DataComponents.ATTRIBUTE_MODIFIERS).equals(new ItemStack(second).get(DataComponents.ATTRIBUTE_MODIFIERS)),
                "Migration overwrote target combat defaults");
        check(BladeSoulService.requireRecord(store, source).stage() == 1, "Planning applied an upgrade");
        rejects(() -> BladeSoulService.requireRecord(store, plan.target()), "mismatch");
        rejects(() -> BladeSoulService.planMigration(store, source, first), "invalid_stage");

        var json = BladeSoulStore.CODEC.encodeStart(JsonOps.INSTANCE, store).getOrThrow().getAsJsonObject();
        var duplicate = json.deepCopy();
        duplicate.getAsJsonArray("records").add(duplicate.getAsJsonArray("records").get(0).deepCopy());
        check(BladeSoulStore.CODEC.parse(JsonOps.INSTANCE, duplicate).resultOrPartial(error -> {}).isEmpty(), "Duplicate ID partially decoded");
        var future = json.deepCopy();
        future.getAsJsonArray("records").get(0).getAsJsonObject().addProperty("schema_version", 99);
        check(BladeSoulStore.CODEC.parse(JsonOps.INSTANCE, future).resultOrPartial(error -> {}).isEmpty(), "Future schema partially decoded");
        var malformed = json.deepCopy();
        malformed.getAsJsonArray("records").get(0).getAsJsonObject().addProperty("stage", 0);
        check(BladeSoulStore.CODEC.parse(JsonOps.INSTANCE, malformed).resultOrPartial(error -> {}).isEmpty(), "Invalid stage partially decoded");

        var carrier = new SpiritCarrier(UUID.randomUUID(), UUID.randomUUID(), other, 0);
        var manifested = otherRecord.transition("spirit", carrier);
        store.replace(otherRecord, manifested);
        check(!updated.transition("spirit", carrier).valid(), "Mismatched retained identity accepted");
        rejects(() -> BladeSoulService.requireRecord(store, other), "mismatch");

        var directory = Files.createTempDirectory(java.nio.file.Path.of("."), "soul-store-");
        try (SavedDataStorage storage = new SavedDataStorage(directory, DataFixers.getDataFixer(), registries)) {
            storage.set(BladeSoulStore.TYPE, store);
            storage.saveAndJoin();
        }
        check(Files.isRegularFile(BladeSoulStore.TYPE.id().withSuffix(".dat").resolveAgainst(directory)), "SavedData file missing");
        try (SavedDataStorage storage = new SavedDataStorage(directory, DataFixers.getDataFixer(), registries)) {
            var loaded = storage.get(BladeSoulStore.TYPE);
            check(loaded != null && loaded.records().size() == 2 && !loaded.isDirty(), "SavedData reload failed");
            check(loaded.find(updated.soulId()).orElseThrow().equals(updated), "SavedData reload lost relationship data");
            check(BladeSoulService.requireRecord(loaded, restoredItem).equals(updated), "Reloaded item/archive disagree");
            var loadedSpirit = loaded.find(otherRecord.soulId()).orElseThrow();
            check(loadedSpirit.form().equals("spirit") && loadedSpirit.carrier().orElseThrow().entityId().equals(carrier.entityId())
                    && ItemStack.matches(loadedSpirit.carrier().orElseThrow().sword(), other), "SavedData lost spirit carrier or sword components");
        }
        System.out.println("PASS: soul identity, read-only queries, missing records, revision checks, item/network codecs, migration plans and SavedData file reload");
    }

    private static MeowBladeItem register(MeowBladeStage stage) {
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("absmod", stage.itemId()));
        return Registry.register(BuiltInRegistries.ITEM, key, new MeowBladeItem(new Item.Properties().setId(key), stage));
    }

    private static void rejects(Runnable action, String expected) {
        try { action.run(); } catch (IllegalStateException | IllegalArgumentException exception) {
            check(expected.equals(exception.getMessage()), "Unexpected rejection: " + exception);
            return;
        }
        throw new AssertionError("Expected rejection: " + expected);
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
