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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

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
    private static final String STORAGE_ID="absmod_blade_souls";
    private static final SavedData.Factory<BladeSoulStore> TYPE=new SavedData.Factory<>(BladeSoulStore::new,
        (tag,registries)->CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tag).getOrThrow());
    @Override public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries) {
        return (net.minecraft.nbt.CompoundTag)CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),this).getOrThrow();
    }
    private final Map<UUID, BladeSoulRecord> records = new HashMap<>();

    public static Optional<BladeSoulStore> find(MinecraftServer server) {
        if (!server.isSameThread()) { throw new IllegalStateException("server_thread"); }
        BladeSoulStore store = server.overworld().getDataStorage().get(TYPE,STORAGE_ID);
        if (store == null && !Files.notExists(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").resolve(STORAGE_ID+".dat"))) {
            throw new IllegalStateException("store_unreadable");
        }
        return Optional.ofNullable(store);
    }

    public static BladeSoulStore forInitialization(MinecraftServer server) {
        return find(server).orElseGet(() -> {
            BladeSoulStore store = new BladeSoulStore();
            server.overworld().getDataStorage().set(STORAGE_ID, store);
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
