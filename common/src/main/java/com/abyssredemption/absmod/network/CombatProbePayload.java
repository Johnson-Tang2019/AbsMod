package com.abyssredemption.absmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Only the server operator can enable collection for a player. */
public record CombatProbePayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<CombatProbePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absmod", "combat_probe"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CombatProbePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CombatProbePayload::enabled, CombatProbePayload::new);

    @Override
    public Type<CombatProbePayload> type() { return TYPE; }
}
