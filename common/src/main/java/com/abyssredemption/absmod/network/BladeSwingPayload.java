package com.abyssredemption.absmod.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** One server-confirmed visual per authorized action, including misses. */
public record BladeSwingPayload(UUID playerId, UUID attackId) implements CustomPacketPayload {
    public static final Type<BladeSwingPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absmod", "blade_swing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BladeSwingPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BladeSwingPayload::playerId, UUIDUtil.STREAM_CODEC, BladeSwingPayload::attackId, BladeSwingPayload::new);
    @Override public Type<BladeSwingPayload> type() { return TYPE; }
}
