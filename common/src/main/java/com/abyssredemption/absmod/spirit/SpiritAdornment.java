package com.abyssredemption.absmod.spirit;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum SpiritAdornment implements StringRepresentable {
    NONE("none"), BLOSSOM("blossom_clip"), MOON("moon_clip");

    static final Codec<SpiritAdornment> CODEC = StringRepresentable.fromEnum(SpiritAdornment::values);
    private static final SpiritAdornment[] VALUES = values();
    private final String id;

    SpiritAdornment(String id) { this.id = id; }

    public static SpiritAdornment fromNetwork(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : NONE;
    }

    public ItemStack stack() {
        return this == NONE ? ItemStack.EMPTY : new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", id)));
    }

    public Component displayName() {
        return Component.translatable(this == NONE ? "leisure.absmod.none" : "item.absmod." + id);
    }

    @Override public String getSerializedName() { return id; }
}
