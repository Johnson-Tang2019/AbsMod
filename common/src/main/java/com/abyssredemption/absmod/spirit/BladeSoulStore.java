package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.AbsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;

public final class BladeSoulStore extends SavedData {
    private static final Codec<BladeSoulStore> RECORDS_CODEC = BladeSoulRecord.CODEC.listOf()
            .fieldOf("records").codec().flatXmap(BladeSoulStore::decode, store -> DataResult.success(store.records()));
    // Discard partial decoding results: one malformed record must not erase other identities on save.
    public static final Codec<BladeSoulStore> CODEC = new Codec<>() {
        @Override public <T> DataResult<com.mojang.datafixers.util.Pair<BladeSoulStore, T>> decode(
                com.mojang.serialization.DynamicOps<T> ops, T input) {
            var result = RECORDS_CODEC.decode(ops, input);
            return result.result().map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Invalid soul store; recovery required"));
        }
        @Override public <T> DataResult<T> encode(BladeSoulStore input,
                com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return RECORDS_CODEC.encode(input, ops, prefix);
        }
    };
    public static final SavedDataType<BladeSoulStore> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "blade_souls"), BladeSoulStore::new, CODEC,
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private final Map<UUID, BladeSoulRecord> records = new HashMap<>();

    public static Optional<BladeSoulStore> find(MinecraftServer server) {
        if (!server.isSameThread()) { throw new IllegalStateException("server_thread"); }
        BladeSoulStore store = server.getDataStorage().get(TYPE);
        if (store == null && !Files.notExists(TYPE.id().withSuffix(".dat").resolveAgainst(server.getWorldPath(LevelResource.DATA)))) {
            throw new IllegalStateException("store_unreadable");
        }
        return Optional.ofNullable(store);
    }

    public static BladeSoulStore forInitialization(MinecraftServer server) {
        return find(server).orElseGet(() -> {
            BladeSoulStore store = new BladeSoulStore();
            server.getDataStorage().set(TYPE, store);
            return store;
        });
    }

    private static DataResult<BladeSoulStore> decode(List<BladeSoulRecord> list) {
        BladeSoulStore store = new BladeSoulStore();
        for (BladeSoulRecord record : list) {
            if (!record.valid() || store.records.putIfAbsent(record.soulId(), record) != null) {
                return DataResult.error(() -> "Invalid or duplicate soul identity");
            }
        }
        return DataResult.success(store);
    }

    public List<BladeSoulRecord> records() { return List.copyOf(records.values()); }
    public Optional<BladeSoulRecord> find(UUID id) { return Optional.ofNullable(records.get(id)); }

    void insert(BladeSoulRecord record) {
        if (!record.valid() || records.containsKey(record.soulId())) { throw new IllegalStateException("duplicate"); }
        records.put(record.soulId(), record);
        setDirty();
    }

    void replace(BladeSoulRecord expected, BladeSoulRecord replacement) {
        if (!replacement.valid() || !expected.soulId().equals(replacement.soulId())
                || replacement.revision() != expected.revision() + 1
                || !records.replace(expected.soulId(), expected, replacement)) {
            throw new IllegalStateException("stale_revision");
        }
        setDirty();
    }
}
