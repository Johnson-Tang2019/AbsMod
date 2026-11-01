package com.abyssredemption.absmod.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

/** Durable transaction evidence; callers cannot mutate the retained sword. */
public record SpiritCarrier(UUID entityId, UUID operationId, ItemStack sword, int sourceSlot) {
    public static final Codec<SpiritCarrier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("entity_id").forGetter(SpiritCarrier::entityId),
            UUIDUtil.CODEC.fieldOf("operation_id").forGetter(SpiritCarrier::operationId),
            ItemStack.CODEC.fieldOf("sword").forGetter(SpiritCarrier::sword),
            Codec.intRange(0, 35).fieldOf("source_slot").forGetter(SpiritCarrier::sourceSlot)
    ).apply(instance, SpiritCarrier::new));
    public SpiritCarrier { sword = sword.copy(); }
    @Override public ItemStack sword() { return sword.copy(); }
}
