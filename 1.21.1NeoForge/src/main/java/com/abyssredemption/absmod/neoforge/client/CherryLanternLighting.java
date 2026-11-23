package com.abyssredemption.absmod.neoforge.client;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.compat.CherryLanternRenderer;
import com.abyssredemption.absmod.compat.WornCharms;
import com.abyssredemption.absmod.neoforge.CherryLantern;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Add light to the client's transient block-light field. Server chunks never acquire a light block. */
@Mod(value = AbsMod.MOD_ID, dist = Dist.CLIENT)
public final class CherryLanternLighting {
    private static final int BRIGHTNESS = 14;
    private static LayerLightEventListener activeEngine;
    private static LongSet emitters = LongSet.of();
    private static boolean packetReplacedLight;
    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    public CherryLanternLighting(IEventBus bus) {
        if (CURIOS) bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(CherryLanternRenderer::register));
        NeoForge.EVENT_BUS.addListener(CherryLanternLighting::tick);
    }

    public static int emission(LayerLightEventListener engine, long pos, int existing) {
        return engine == activeEngine && existing < BRIGHTNESS && emitters.contains(pos) ? BRIGHTNESS : existing;
    }

    public static void lightPacket(int chunkX, int chunkZ) {
        for (var it = emitters.iterator(); it.hasNext();) {
            long pos = it.nextLong();
            if (Math.abs((BlockPos.getX(pos) >> 4) - chunkX) <= 1
                    && Math.abs((BlockPos.getZ(pos) >> 4) - chunkZ) <= 1) {
                packetReplacedLight = true;
                return;
            }
        }
    }

    private static boolean carrying(Player player) {
        var lantern = CherryLantern.LANTERN.get();
        return player.getOffhandItem().is(lantern) || (CURIOS && WornCharms.contains(player, lantern));
    }

    private static void tick(ClientTickEvent.Post event) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            activeEngine = null;
            emitters = LongSet.of();
            packetReplacedLight = false;
            return;
        }
        var engine = level.getLightEngine().getLayerListener(LightLayer.BLOCK);
        if (engine != activeEngine) {
            activeEngine = engine;
            emitters = LongSet.of();
            packetReplacedLight = false;
        }
        // The server can replace only one half of the light field at a chunk boundary.
        // Withdraw the old sources first so the next propagation crosses that boundary again.
        if (packetReplacedLight) {
            packetReplacedLight = false;
            var old = emitters;
            emitters = LongSet.of();
            for (var it = old.iterator(); it.hasNext();) engine.checkBlock(BlockPos.of(it.nextLong()));
            level.getLightEngine().runLightUpdates();
        }
        var current = new LongOpenHashSet();
        for (var player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || !carrying(player)) continue;
            var pos = BlockPos.containing(player.getX(), player.getY() + player.getBbHeight() * 0.58, player.getZ());
            if (level.hasChunkAt(pos) && level.getBlockState(pos).getLightBlock(level, pos) < 15) current.add(pos.asLong());
        }
        var previous = emitters;
        emitters = current;
        for (var it = previous.iterator(); it.hasNext();) {
            long pos = it.nextLong();
            if (!current.contains(pos)) engine.checkBlock(BlockPos.of(pos));
        }
        for (var it = current.iterator(); it.hasNext();) {
            long pos = it.nextLong();
            var block = BlockPos.of(pos);
            // A server light packet can replace a stationary player's local light section.
            if (!previous.contains(pos) || engine.getLightValue(block) < BRIGHTNESS) engine.checkBlock(block);
        }
    }
}
