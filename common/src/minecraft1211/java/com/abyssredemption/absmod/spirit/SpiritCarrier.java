package com.abyssredemption.absmod.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

/** Durable transaction evidence; callers cannot mutate the retained sword. */
public record SpiritCarrier(UUID entityId, UUID operationId, ItemStack sword, int sourceSlot, Optional<GlobalPos> lastLocation) {
    public static final Codec<SpiritCarrier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("entity_id").forGetter(SpiritCarrier::entityId),
            UUIDUtil.CODEC.fieldOf("operation_id").forGetter(SpiritCarrier::operationId),
            ItemStack.CODEC.fieldOf("sword").forGetter(SpiritCarrier::sword),
            Codec.intRange(0, 35).fieldOf("source_slot").forGetter(SpiritCarrier::sourceSlot),
            GlobalPos.CODEC.optionalFieldOf("last_location").forGetter(SpiritCarrier::lastLocation)
    ).apply(instance, SpiritCarrier::new));
    public SpiritCarrier(UUID entityId, UUID operationId, ItemStack sword, int sourceSlot) {
        this(entityId, operationId, sword, sourceSlot, Optional.empty());
    }

    public SpiritCarrier withLocation(GlobalPos location) {
        return new SpiritCarrier(entityId, operationId, sword, sourceSlot, Optional.of(location));
    }

    public SpiritCarrier { sword = sword.copy(); }
    @Override public ItemStack sword() { return sword.copy(); }
}
