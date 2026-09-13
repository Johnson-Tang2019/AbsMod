package com.abyssredemption.absmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Input only: no target, damage, speed, permanent identity or reward supplied by the client. */
public record AttackIntentPayload(long epoch, long sequence, boolean held, int slot, Identifier dimension) implements CustomPacketPayload {
    public static final Type<AttackIntentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absmod", "attack_intent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttackIntentPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, AttackIntentPayload::epoch, ByteBufCodecs.VAR_LONG, AttackIntentPayload::sequence,
            ByteBufCodecs.BOOL, AttackIntentPayload::held, ByteBufCodecs.VAR_INT, AttackIntentPayload::slot,
            Identifier.STREAM_CODEC, AttackIntentPayload::dimension, AttackIntentPayload::new);

    @Override
    public Type<AttackIntentPayload> type() { return TYPE; }
}
