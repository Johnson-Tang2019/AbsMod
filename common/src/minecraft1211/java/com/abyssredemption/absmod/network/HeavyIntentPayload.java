package com.abyssredemption.absmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A request/cancel signal only; the server owns timing, target and damage. */
public record HeavyIntentPayload(long sequence,boolean active,int slot,ResourceLocation dimension) implements CustomPacketPayload {
    public static final Type<HeavyIntentPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("absmod","heavy_intent"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HeavyIntentPayload> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_LONG,HeavyIntentPayload::sequence,ByteBufCodecs.BOOL,HeavyIntentPayload::active,
        ByteBufCodecs.VAR_INT,HeavyIntentPayload::slot,ResourceLocation.STREAM_CODEC,HeavyIntentPayload::dimension,HeavyIntentPayload::new);
    @Override public Type<HeavyIntentPayload> type(){return TYPE;}
}
