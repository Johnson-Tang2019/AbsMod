package com.abyssredemption.absmod.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum BladeRoute implements StringRepresentable {
    NORMAL("normal", 9), DARK("dark", 8);

    public static final Codec<BladeRoute> CODEC = StringRepresentable.fromEnum(BladeRoute::values);
    private final String id;
    private final int maxStage;

    BladeRoute(String id, int maxStage) {
        this.id = id;
        this.maxStage = maxStage;
    }

    public boolean accepts(int stage) { return stage >= 1 && stage <= maxStage; }

    @Override
    public String getSerializedName() { return id; }
}
