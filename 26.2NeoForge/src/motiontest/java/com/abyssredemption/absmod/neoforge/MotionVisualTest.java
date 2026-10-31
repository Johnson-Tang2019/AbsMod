package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.client.animation.BladeMotionPlayer;
import com.abyssredemption.absmod.network.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/** Opt-in visual fixtures send authoritative presentation packets; they never assert gameplay damage. */
@Mod(value="absmod",dist=Dist.CLIENT)
public final class MotionVisualTest {
    private final MotionPeerTest peer = new MotionPeerTest();
    private boolean started;
    private int tick;
    private long sequence;
    private net.minecraft.client.player.RemotePlayer remote;
    public MotionVisualTest() { NeoForge.EVENT_BUS.addListener(this::tick); }
    private void tick(ClientTickEvent.Post event) {
        if(System.getProperty("absmod.motionPeer")!=null) { peer.tick();return; }
        var mc=Minecraft.getInstance();
        if(!started && mc.gui.screen() instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.fov().set(65);mc.options.renderDistance().set(5);mc.getWindow().setWindowed(1280,900);
            mc.createWorldOpenFlows().createFreshLevel("absmod-motion-"+System.currentTimeMillis(),
                new LevelSettings("Motion visual",GameType.CREATIVE,new LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if(!started || mc.level==null || mc.player==null || mc.getSingleplayerServer()==null) { return; }
        tick++;var server=mc.getSingleplayerServer();
        if(tick==30) {
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();
                for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.QUARTZ_BLOCK.defaultBlockState());
                p.teleportTo(level,0,100,0,java.util.Set.of(),180,0,true);
                p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade"))));
            });
            BladeMotionPlayer.hideEffects=true;
        }
        if(tick==60)mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(tick==160)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if(tick==260) { BladeMotionPlayer.hideEffects=false;mc.options.setCameraType(CameraType.FIRST_PERSON); }
        if(tick==340) { server.execute(()->server.getPlayerList().getPlayers().getFirst().setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
            new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade_stage_9"))))); }
        if(tick>=60 && tick<400 && tick%16==0) {
            long seq=++sequence;
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();
                PacketDistributor.sendToPlayer(p,new BladeSwingPayload(p.getUUID(),java.util.UUID.randomUUID(),seq,p.level().getGameTime(),4));});
        }
        if(tick>=65 && tick<=245 && tick%2==0 || tick>=345 && tick<=385 && tick%2==0) {
            String name="motion-"+(tick<160?"first":tick<260?"third":"stage9")+"-"+tick+".png";
            Screenshot.grab(mc.gameDirectory,name,mc.gameRenderer.mainRenderTarget(),1,message->{});
        }
        if(tick==410) { BladeMotionPlayer.hideEffects=true;mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.LEFT);mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.LEFT); }
        if(tick==450)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if(tick==500)mc.reloadResourcePacks();
        if(tick==560) { mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.RIGHT);mc.player.setMainArm(net.minecraft.world.entity.HumanoidArm.RIGHT);mc.options.setCameraType(CameraType.FIRST_PERSON); }
        if(tick>=416 && tick<490 && tick%4==0 || tick>=540 && tick<600 && tick%3==0) {
            long seq=++sequence;
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();
                PacketDistributor.sendToPlayer(p,new BladeSwingPayload(p.getUUID(),java.util.UUID.randomUUID(),seq,p.level().getGameTime(),20F/6));});
        }
        if(tick==625)server.execute(()->server.getPlayerList().getPlayers().getFirst().setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)));
        if(tick>=418 && tick<490 && tick%2==0 || tick>=542 && tick<620 && tick%2==0 || tick==635) {
            Screenshot.grab(mc.gameDirectory,"motion-matrix-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        }
        if(tick==650) {
            remote=new net.minecraft.client.player.RemotePlayer(mc.level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MotionObserver"));
            remote.setId(990001);remote.setPos(0,100,-3);remote.setYRot(0);remote.setYHeadRot(0);
            remote.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade"))));
            mc.level.addEntity(remote);
        }
        if(tick>=654 && tick<716 && tick%8==0) {
            BladeMotionPlayer.confirm(mc,new BladeSwingPayload(remote.getUUID(),java.util.UUID.randomUUID(),++sequence,mc.level.getGameTime(),4));
        }
        if(tick>=656 && tick<740 && tick%2==0)Screenshot.grab(mc.gameDirectory,"motion-observer-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==750) { LogUtils.getLogger().info("MOTION_VISUAL sequences={} finished",sequence);mc.stop(); }
    }
}
