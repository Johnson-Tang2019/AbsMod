package com.abyssredemption.absmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Accesses the active screen through this Minecraft version's client API. */
public final class ClientScreenAccess {
    private ClientScreenAccess() {}

    public static Screen current(Minecraft client) {
        return client.screen;
    }
}
