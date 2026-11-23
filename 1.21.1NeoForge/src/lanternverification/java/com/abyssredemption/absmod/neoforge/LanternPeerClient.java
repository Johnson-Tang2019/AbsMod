package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.compat.WornCharms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Two actual clients share a light cell, then the observer checks the remote carrier's departure. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class LanternPeerClient {
    private static final BlockPos START = new BlockPos(0, 92, 0);
    private boolean joined;
    private int phase;

    public LanternPeerClient() {
        if (Boolean.getBoolean("absmod.lanternPeers")) NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!joined && mc.screen instanceof TitleScreen) {
            joined = true;
            mc.options.pauseOnLostFocus = false;
            var address = "127.0.0.1:25591";
            ConnectScreen.startConnecting(new TitleScreen(), mc, ServerAddress.parseString(address),
                    new ServerData("Lantern test", address, ServerData.Type.OTHER), false, null);
        } else if (mc.screen instanceof DisconnectedScreen) {
            mc.stop();
        } else if (mc.level != null && mc.player != null && mc.player.getGameProfile().getName().equals("LanternWitness")) {
            var carrier = mc.level.players().stream().filter(p -> p.getGameProfile().getName().equals("LanternCarrier")).findFirst();
            var lamp = CherryLantern.LANTERN.get();
            boolean local = mc.player.getOffhandItem().is(lamp);
            boolean remote = carrier.isPresent() && WornCharms.contains(carrier.get(), lamp);
            int center = mc.level.getBrightness(LightLayer.BLOCK, START);
            int east = mc.level.getBrightness(LightLayer.BLOCK, START.east(6));
            if ((phase == 0 && local && remote && center == 14) || (phase == 1 && !local && remote && center == 14)
                    || (phase == 2 && remote && center == 8 && east == 14)
                    || (phase == 3 && carrier.isEmpty() && center == 0 && east == 0)) {
                mc.player.connection.sendChat("lantern_check_" + ++phase);
            }
        }
    }
}
