package com.abyssredemption.absmod.spirit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

/** Runtime criteria are awarded only after the corresponding server transaction succeeds. */
public final class BladeAdvancements {
    private BladeAdvancements(){}
    public static void award(ServerPlayer player,String path) {
        if(player.connection==null)return;
        var advancement=player.level().getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath("absmod","progression/"+path));
        if(advancement!=null)player.getAdvancements().award(advancement,"complete");
    }
}
