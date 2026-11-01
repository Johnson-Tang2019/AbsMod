package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.spirit.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Isolated opt-in real-client progression fixture, excluded from release artifacts. */
@Mod(value="absmod",dist=Dist.CLIENT)
public final class CatGeneticsVisualTest {
    private boolean started;
    private int tick;
    private net.minecraft.world.entity.monster.zombie.Husk target;
    public CatGeneticsVisualTest(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!started && mc.gui.screen() instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);
            mc.getWindow().setWindowed(1280,900);
            mc.createWorldOpenFlows().createFreshLevel("absmod-cat-genetics-"+System.currentTimeMillis(),
                new LevelSettings("Progression verification",GameType.SURVIVAL,
                    new LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET)
                    .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if(!started || mc.player==null || mc.getSingleplayerServer()==null)return;
        tick++;var server=mc.getSingleplayerServer();
        if(tick==30)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();
            for(int x=-4;x<21;x++)for(int z=-6;z<5;z++)level.setBlock(new net.minecraft.core.BlockPos(x,99,z),BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_concrete")).defaultBlockState(),3);
            var empty=java.util.Optional.<java.util.UUID>empty();
            int[][] loci={{0,3,0},{3,3,0},{0,0,3},{3,0,3},{0,3,3},{3,3,3}};
            String[] names={"巧克力","淡紫","海豹重点","蓝重点","巧克力重点","淡紫重点"};
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","field_guide"))));
            for(int i=0;i<6;i++) {
                var a=net.minecraft.world.entity.EntityTypes.CAT.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);var b=net.minecraft.world.entity.EntityTypes.CAT.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                var genome=new com.abyssredemption.absmod.cat.CatGenome(loci[i][0],"minecraft:all_black",empty,empty,0,loci[i][1],loci[i][2]);
                ((com.abyssredemption.absmod.cat.CatGenetics)a).absmod$setGenome(genome);((com.abyssredemption.absmod.cat.CatGenetics)b).absmod$setGenome(genome);
                for(int j=0;j<2;j++) {
                    var cat=a.getBreedOffspring(level,b);cat.setAge(j==0?0:-24000);cat.setNoAi(true);
                    cat.setPos(i*3+(j==0?-0.5:0.7),100,0);cat.setYRot(155);cat.setYHeadRot(155);cat.yBodyRot=155;
                    cat.setCustomName(net.minecraft.network.chat.Component.literal(names[i]+(j==0?"":"幼猫")));cat.setCustomNameVisible(true);level.addFreshEntity(cat);
                    com.abyssredemption.absmod.item.FieldGuideItem.observe(p,cat,InteractionHand.MAIN_HAND);
                    if(!cat.getVariant().unwrapKey().orElseThrow().identifier().toString().equals(genome.appearance()))throw new IllegalStateException("Coat variant mismatch");
                }
            }
            com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_SPAWN_PASS six coats with actual offspring; adult and baby; advancement observations");
        });
        if(tick>=50 && tick<=350 && (tick-50)%60==0) {
            int index=(tick-50)/60;
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(p.level(),index*3,100,-3.5,java.util.Set.of(),0,18,true);});
        }
        if(tick>=85 && tick<=385 && (tick-85)%60==0)Screenshot.grab(mc.gameDirectory,"cat-coat-"+((tick-85)/60)+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==400)mc.reloadResourcePacks();
        if(tick==450)Screenshot.grab(mc.gameDirectory,"cat-reload.png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==470)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            for(String goal:new String[]{"cat_chocolate","cat_lilac","cat_point","cat_lilac_point"}) {
                var advancement=server.getAdvancements().get(Identifier.fromNamespaceAndPath("absmod","progression/"+goal));
                if(advancement==null || !p.getAdvancements().getOrStartProgress(advancement).isDone())throw new IllegalStateException("Missing goal "+goal);
            }

            com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_GOALS_PASS all four goals complete");
        });

        if(tick==540) {
            mc.gui.setScreen(null);
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();p.getMainHandItem().getItem().use(p.level(),p,InteractionHand.MAIN_HAND);});
        }
        if(tick==565 && !(mc.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen))throw new IllegalStateException("Guide failed to open");
        if(tick>=570 && tick<=630 && (tick-570)%20==0 && mc.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen book)book.setPage(16+(tick-570)/20);
        if(tick>=578 && tick<=638 && (tick-578)%20==0)Screenshot.grab(mc.gameDirectory,"cat-guide-"+(17+(tick-578)/20)+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==660){com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_CLIENT_PASS");mc.stop();}
    }
}
