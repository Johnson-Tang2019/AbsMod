package com.abyssredemption.absmod.client;

import net.minecraft.client.KeyMapping;

public final class BladeKeys {
    public static final KeyMapping HEAVY=new KeyMapping("key.absmod.heavy",org.lwjgl.glfw.GLFW.GLFW_KEY_G,"key.categories.gameplay");
    public static final KeyMapping COMBAT_HUD = new KeyMapping("key.absmod.combat_hud",
            org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN, "key.categories.gameplay");
    private BladeKeys(){}
}
