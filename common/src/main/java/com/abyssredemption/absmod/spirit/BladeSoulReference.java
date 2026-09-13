package com.abyssredemption.absmod.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** An immutable item reference, never a second copy of relationship data. */
public record BladeSoulReference(UUID soulId) {
    public static final Codec<BladeSoulReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("soul_id").forGetter(BladeSoulReference::soulId)
    ).apply(instance, BladeSoulReference::new));
}
