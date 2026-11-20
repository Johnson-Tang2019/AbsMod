package com.abyssredemption.absmod.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Two isolated development clients join only the local disposable test server. */
@Mod(value="absmod",dist=Dist.CLIENT)
public final class HomePeerClient {
    private int wait;
    public HomePeerClient() { NeoForge.EVENT_BUS.addListener(this::tick); }
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if (!(mc.screen instanceof TitleScreen || mc.screen instanceof DisconnectedScreen)) { wait=0; return; }
        if (mc.screen instanceof DisconnectedScreen && mc.screen.getNarrationMessage().getString().contains("HOME_PEERS_DONE")) {
            mc.stop();
            return;
        }
        if (++wait!=30) return;
        mc.options.pauseOnLostFocus=false;
        mc.options.renderDistance().set(2);
        var address="127.0.0.1:25584";
        ConnectScreen.startConnecting(new TitleScreen(),mc,ServerAddress.parseString(address),
                new ServerData("Home test",address,ServerData.Type.OTHER),false,null);
    }
}
