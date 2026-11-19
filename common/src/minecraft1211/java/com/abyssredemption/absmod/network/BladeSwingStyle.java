package com.abyssredemption.absmod.network;

import com.abyssredemption.absmod.item.BladeRoute;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Server-selected presentation for one authorized attack. */
public enum BladeSwingStyle {
    NORMAL(0, BladeRoute.NORMAL),
    DARK(1, BladeRoute.DARK),
    HEAVY(2, BladeRoute.DARK),
    HEAVY_IMPACT(3, BladeRoute.DARK);

    public static final StreamCodec<ByteBuf, BladeSwingStyle> CODEC =
            ByteBufCodecs.VAR_INT.map(BladeSwingStyle::fromWireId, style -> style.wireId);

    private final int wireId;
    private final BladeRoute route;

    BladeSwingStyle(int wireId, BladeRoute route) {
        this.wireId = wireId;
        this.route = route;
    }

    private static BladeSwingStyle fromWireId(int id) {
        return switch (id) {
            case 0 -> NORMAL;
            case 1 -> DARK;
            case 2 -> HEAVY;
            case 3 -> HEAVY_IMPACT;
            default -> throw new DecoderException("Unknown blade swing style: " + id);
        };
    }

    public static BladeSwingStyle regular(BladeRoute route) {
        return route == BladeRoute.DARK ? DARK : NORMAL;
    }

    public boolean matches(BladeRoute route) {
        return this.route == route;
    }

    public boolean isHeavy() {
        return this == HEAVY || this == HEAVY_IMPACT;
    }
}
