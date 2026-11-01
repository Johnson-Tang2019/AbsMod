package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.spirit.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ClickType;
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
    private com.abyssredemption.absmod.entity.BladeSpiritEntity spirit;
    private net.minecraft.world.entity.monster.Husk target;
    public CatGeneticsVisualTest(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!started && mc.screen instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);
            mc.getWindow().setWindowed(1280,900);
            mc.createWorldOpenFlows().createFreshLevel("absmod-cat-genetics-"+System.currentTimeMillis(),
                new LevelSettings("Progression verification",GameType.SURVIVAL,false,
                    net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET)
                    .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if(!started || mc.player==null || mc.getSingleplayerServer()==null)return;
        tick++;if(tick==40)mc.options.hideGui=true;var server=mc.getSingleplayerServer();
        if(tick==30)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();var level=p.serverLevel();
            for(int x=-4;x<21;x++)for(int z=-6;z<5;z++)level.setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.WHITE_CONCRETE.defaultBlockState(),3);
            var empty=java.util.Optional.<java.util.UUID>empty();
            int[][] loci={{0,3,0},{3,3,0},{0,0,3},{3,0,3},{0,3,3},{3,3,3}};
            String[] names={"巧克力","淡紫","海豹重点","蓝重点","巧克力重点","淡紫重点"};
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","field_guide"))));
            for(int i=0;i<6;i++) {
                var a=net.minecraft.world.entity.EntityType.CAT.create(level);var b=net.minecraft.world.entity.EntityType.CAT.create(level);
                var genome=new com.abyssredemption.absmod.cat.CatGenome(loci[i][0],"minecraft:all_black",empty,empty,0,loci[i][1],loci[i][2]);
                ((com.abyssredemption.absmod.cat.CatGenetics)a).absmod$setGenome(genome);((com.abyssredemption.absmod.cat.CatGenetics)b).absmod$setGenome(genome);
                for(int j=0;j<2;j++) {
                    var cat=a.getBreedOffspring(level,b);cat.setAge(j==0?0:-24000);cat.setNoAi(true);
                    cat.setPos(i*3+(j==0?-0.5:0.7),100,0);cat.setYRot(155);cat.setYHeadRot(155);cat.yBodyRot=155;
                    cat.setCustomName(net.minecraft.network.chat.Component.literal(names[i]+(j==0?"":"幼猫")));cat.setCustomNameVisible(true);level.addFreshEntity(cat);
                    com.abyssredemption.absmod.item.FieldGuideItem.observe(p,cat,InteractionHand.MAIN_HAND);
                    if(!cat.getVariant().unwrapKey().orElseThrow().location().toString().equals(genome.appearance()))throw new IllegalStateException("Coat variant mismatch");
                }
            }
            com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_SPAWN_PASS six coats with actual offspring; adult and baby; advancement observations");
        });
        if(tick>=50 && tick<=350 && (tick-50)%60==0) {
            int index=(tick-50)/60;
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(p.serverLevel(),index*3,100,-3.5,java.util.Set.of(),0,18);});
        }
        if(tick>=85 && tick<=385 && (tick-85)%60==0)Screenshot.grab(mc.gameDirectory,"cat-coat-"+((tick-85)/60)+".png",mc.getMainRenderTarget(),message->{});
        if(tick==400)mc.reloadResourcePacks();
        if(tick==450)Screenshot.grab(mc.gameDirectory,"cat-reload.png",mc.getMainRenderTarget(),message->{});
        if(tick==470)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            for(String goal:new String[]{"cat_chocolate","cat_lilac","cat_point","cat_lilac_point"}) {
                var advancement=server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("absmod","progression/"+goal));
                if(advancement==null || !p.getAdvancements().getOrStartProgress(advancement).isDone())throw new IllegalStateException("Missing goal "+goal);
            }
            var file=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;var chapter=file.getChapter(Long.parseUnsignedLong("1C4E810187B7DDC2",16));
            if(chapter==null || chapter.getQuests().size()!=29)throw new IllegalStateException("FTB chapter count");
            var data=file.getTeamData(p).orElseThrow();long completed=chapter.getQuests().stream().filter(data::isCompleted).count();
            if(completed<8)throw new IllegalStateException("FTB goals incomplete: "+completed);data.saveIfChanged();
            com.mojang.logging.LogUtils.getLogger().info("CAT_FTB_PASS quests=29 completed={}",completed);
            com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_GOALS_PASS all four goals complete");
        });
        if(tick==490)dev.ftb.mods.ftbquests.client.ClientQuestFile.openGui();
        if(tick==525)Screenshot.grab(mc.gameDirectory,"cat-ftb.png",mc.getMainRenderTarget(),message->{});
        if(tick==540) {
            mc.options.hideGui=false;mc.setScreen(null);
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();p.getMainHandItem().getItem().use(p.serverLevel(),p,InteractionHand.MAIN_HAND);});
        }
        if(tick==565 && !(mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen))throw new IllegalStateException("Guide failed to open");
        if(tick>=570 && tick<=630 && (tick-570)%20==0 && mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen book)book.setPage(16+(tick-570)/20);
        if(tick>=578 && tick<=638 && (tick-578)%20==0)Screenshot.grab(mc.gameDirectory,"cat-guide-"+(17+(tick-578)/20)+".png",mc.getMainRenderTarget(),message->{});
        if(tick==660){com.mojang.logging.LogUtils.getLogger().info("CAT_GENETICS_CLIENT_PASS");mc.stop();}
    }
}
