package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.mojang.serialization.Codec;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.LevelResource;

/** Cosmetic selections are independent of the sword and carrier transactions. */
public final class SpiritWardrobe extends SavedData {
    static final Codec<SpiritWardrobe> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, SpiritAdornment.CODEC)
            .fieldOf("choices").codec().xmap(SpiritWardrobe::new, store -> Map.copyOf(store.styles));
    private static final String STORAGE_ID = "absmod_spirit_wardrobe";
    private static final SavedData.Factory<SpiritWardrobe> TYPE = new SavedData.Factory<>(SpiritWardrobe::new,
            (tag, lookup) -> CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow());

    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        return (CompoundTag) CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }
    private final Map<UUID, SpiritAdornment> styles;

    private SpiritWardrobe() { this(Map.of()); }
    private SpiritWardrobe(Map<UUID, SpiritAdornment> styles) { this.styles = new HashMap<>(styles); }

    private static Optional<SpiritWardrobe> find(MinecraftServer server) {
        var store = server.overworld().getDataStorage().get(TYPE, STORAGE_ID);
        if (store == null && !Files.notExists(server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(STORAGE_ID + ".dat"))) {
            throw new IllegalStateException("spirit_wardrobe_unreadable");
        }
        return Optional.ofNullable(store);
    }

    public static SpiritAdornment read(MinecraftServer server, UUID soul) {
        return find(server).map(store -> store.styles.getOrDefault(soul, SpiritAdornment.NONE)).orElse(SpiritAdornment.NONE);
    }

    public static boolean canChoose(ServerPlayer player, SpiritAdornment style) {
        if (style == SpiritAdornment.NONE) {
            return true;
        }
        var item = style.stack().getItem();
        return player.getInventory().hasAnyMatching(stack -> stack.is(item));
    }

    public static boolean choose(ServerPlayer player, BladeSpiritEntity spirit, SpiritAdornment style) {
        if (player.level() != spirit.level() || !player.isAlive() || player.isSpectator() || !spirit.isAlive()
                || player.distanceToSqr(spirit) > 256 || !player.getUUID().equals(spirit.ownerId()) || !spirit.active()
                || !canChoose(player, style)) {
            return false;
        }
        var server = player.level().getServer();
        if (read(server, spirit.soulId()) == style) {
            return true;
        }
        var store = find(server).orElseGet(() -> {
            var created = new SpiritWardrobe();
            server.overworld().getDataStorage().set(STORAGE_ID, created);
            return created;
        });
        if (style == SpiritAdornment.NONE) {
            store.styles.remove(spirit.soulId());
        } else {
            store.styles.put(spirit.soulId(), style);
        }
        store.setDirty();
        spirit.setAdornment(style);
        if (style != SpiritAdornment.NONE) {
            BladeAdvancements.award(player, "spirit_adornment");
        }
        return true;
    }
}
