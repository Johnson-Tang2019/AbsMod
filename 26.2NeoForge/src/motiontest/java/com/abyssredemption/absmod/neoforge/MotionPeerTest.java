package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.client.animation.BladeMotionPlayer;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Two real clients; inputs traverse the production server scheduler and tracking channel. */
final class MotionPeerTest {
    private boolean started;
    private int tick;
    private long sequence;
    void tick() {
        var mc=Minecraft.getInstance();
        boolean observer="observer".equals(System.getProperty("absmod.motionPeer"));
        if(!started && mc.gui.screen() instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);
            mc.getWindow().setWindowed(960,720);BladeMotionPlayer.hideEffects=true;
            if(observer) {
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new TitleScreen(),mc,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25583"),
                    new net.minecraft.client.multiplayer.ServerData("Motion host","127.0.0.1:25583",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);
            } else {
                mc.createWorldOpenFlows().createFreshLevel("motion-peers-"+System.currentTimeMillis(),
                    new LevelSettings("Motion peers",GameType.CREATIVE,new LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                    new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
            }
        }
        if(mc.level==null || mc.player==null) { return; }
        tick++;
        if(!observer && tick==40) {
            var server=mc.getSingleplayerServer();
            // Development identities are accepted only on the loopback-only test listener.
            server.setUsesAuthentication(false);
            try { server.getConnection().startTcpServerListener(java.net.InetAddress.getByName("127.0.0.1"),25583); }
            catch(java.io.IOException e) { throw new IllegalStateException("Cannot open local motion test listener",e); }
            server.execute(()->{
                var p=server.getPlayerList().getPlayers().getFirst();
                for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)p.level().setBlockAndUpdate(new BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.defaultBlockState());
            });
        }
        if(!observer && tick%40==0) {
            var server=mc.getSingleplayerServer();
            server.execute(()->{
                var players=server.getPlayerList().getPlayers();
                for(var p:players) {
                    boolean guest=p.getGameProfile().name().equals("MotionGuest");
                    p.teleportTo(p.level(),guest?0:0,100,guest?-3:0,java.util.Set.of(),guest?0:180,0,true);
                    if(!(p.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem))p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade"))));
                }
                com.mojang.logging.LogUtils.getLogger().info("MOTION_PEERS connected={}",players.size());
            });
        }
        if(mc.level.players().size()>1 && tick%5==0) {
            boolean held=(tick%160)<100;
            ClientPacketDistributor.sendToServer(new AttackIntentPayload(1000000,++sequence,held,0,mc.level.dimension().identifier()));
        }
        if(mc.level.players().size()>1 && tick%4==0)Screenshot.grab(mc.gameDirectory,"peer-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(observer && tick==220 || !observer && tick==2400)mc.stop();
    }
}
