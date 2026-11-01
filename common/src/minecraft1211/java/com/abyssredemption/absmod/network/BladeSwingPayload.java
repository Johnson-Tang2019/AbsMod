package com.abyssredemption.absmod.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** One server-confirmed visual per authorized action, including misses. */
public record BladeSwingPayload(UUID playerId, UUID attackId, long sequence, long startedTick, float intervalTicks) implements CustomPacketPayload {
    public static final Type<BladeSwingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("absmod", "blade_swing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BladeSwingPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BladeSwingPayload::playerId, UUIDUtil.STREAM_CODEC, BladeSwingPayload::attackId,
            net.minecraft.network.codec.ByteBufCodecs.VAR_LONG, BladeSwingPayload::sequence,
            net.minecraft.network.codec.ByteBufCodecs.VAR_LONG, BladeSwingPayload::startedTick,
            net.minecraft.network.codec.ByteBufCodecs.FLOAT, BladeSwingPayload::intervalTicks, BladeSwingPayload::new);
    @Override public Type<BladeSwingPayload> type() { return TYPE; }
}
