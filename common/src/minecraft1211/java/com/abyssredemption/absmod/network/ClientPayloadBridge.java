package com.abyssredemption.absmod.network;

/** Client entrypoint installs handlers without loading client classes on a dedicated server. */
public final class ClientPayloadBridge {
    public static java.util.function.Consumer<CombatProbePayload> probe=payload->{};
    public static java.util.function.Consumer<BladeSwingPayload> swing=payload->{};
    private ClientPayloadBridge() {}
}
