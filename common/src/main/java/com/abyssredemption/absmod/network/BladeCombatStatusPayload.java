package com.abyssredemption.absmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Owner-only combat display snapshot. Fields never authorize a swing. */
public record BladeCombatStatusPayload(int slot, boolean dark, int stage, int comboStacks, boolean decaying,
                                       int windupTicks, int cooldownSeconds) implements CustomPacketPayload {
    public static final BladeCombatStatusPayload EMPTY = new BladeCombatStatusPayload(-1, false, 0, 0, false, 0, 0);
    public static final Type<BladeCombatStatusPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absmod", "blade_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BladeCombatStatusPayload> CODEC = StreamCodec.of(
            (buffer, status) -> {
                buffer.writeVarInt(status.slot());
                buffer.writeBoolean(status.dark());
                buffer.writeVarInt(status.stage());
                buffer.writeVarInt(status.comboStacks());
                buffer.writeBoolean(status.decaying());
                buffer.writeVarInt(status.windupTicks());
                buffer.writeVarInt(status.cooldownSeconds());
            },
            buffer -> new BladeCombatStatusPayload(buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt()));

    @Override
    public Type<BladeCombatStatusPayload> type() {
        return TYPE;
    }
}
