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
public final class ProgressionVisualTest {
    private boolean started;
    private int tick;
    private com.abyssredemption.absmod.entity.BladeSpiritEntity spirit;
    private net.minecraft.world.entity.monster.Husk target;
    public ProgressionVisualTest(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!started && mc.screen instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);
            mc.getWindow().setWindowed(1280,900);
            mc.createWorldOpenFlows().createFreshLevel("absmod-progression-"+System.currentTimeMillis(),
                new LevelSettings("Progression verification",GameType.SURVIVAL,false,
                    net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET)
                    .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if(!started || mc.player==null || mc.getSingleplayerServer()==null)return;
        tick++;var server=mc.getSingleplayerServer();
        if(tick==30)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            var heart=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","resonance_heart")));
            p.setItemInHand(InteractionHand.MAIN_HAND,heart);
            heart.getItem().use(p.serverLevel(),p,InteractionHand.MAIN_HAND);
            if(!(p.containerMenu instanceof ResonanceMenu menu))throw new IllegalStateException("Heart did not open resonance");
            var sword=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","meow_blade")));
            sword.setDamageValue(780);
            var store=BladeSoulStore.forInitialization(server);BladeSoulService.initialize(store,sword,p.getUUID());
            BladeSoulService.setTestAffection(store,sword,25);
            menu.getSlot(0).set(sword);menu.getSlot(1).set(new ItemStack(Items.AMETHYST_SHARD,16));
            menu.getSlot(2).set(new ItemStack(Items.COPPER_INGOT,16));menu.broadcastChanges();
        });
        if(tick==80 || tick==130)Screenshot.grab(mc.gameDirectory,"progression-"+tick+".png",mc.getMainRenderTarget(),message->{});
        if(tick==100)mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,22,0,ClickType.PICKUP,mc.player);
        if(tick==150)mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,22,0,ClickType.PICKUP,mc.player);
        if(tick==170)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();var menu=p.containerMenu;
            if(!menu.getSlot(0).getItem().is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","meow_blade_stage_2")))
                || menu.getSlot(0).getItem().getDamageValue()!=900 || menu.getSlot(1).getItem().getCount()!=8)
                throw new IllegalStateException("Progression transaction failed");
            p.closeContainer();
            boolean restored=false;
            for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(
                BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","meow_blade_stage_2"))))restored=true;
            if(!restored)throw new IllegalStateException("Closing menu lost blade");
            com.mojang.logging.LogUtils.getLogger().info("PROGRESSION_VISUAL passed actual heart use, packet upgrade, replay rejection and close return");
        });
        if(tick==200)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            var sword=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","dark_meow_blade")));
            p.setItemInHand(InteractionHand.MAIN_HAND,sword);
            var store=BladeSoulStore.forInitialization(server);BladeSoulService.initialize(store,sword,p.getUUID());BladeSoulService.setTestAffection(store,sword,100);
            p.openMenu(new SimpleMenuProvider((id,inventory,player)->new ResonanceMenu(id,inventory),net.minecraft.network.chat.Component.literal("Dark resonance")));
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            p.containerMenu.getSlot(0).set(sword);p.containerMenu.getSlot(1).set(new ItemStack(Items.OBSIDIAN,8));
        });
        if(tick==230 || tick==270)Screenshot.grab(mc.gameDirectory,"progression-dark-"+tick+".png",mc.getMainRenderTarget(),message->{});
        if(tick==240)mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,22,0,ClickType.PICKUP,mc.player);
        if(tick==280)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            if(!p.containerMenu.getSlot(0).getItem().is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","dark_meow_blade_stage_2"))))throw new IllegalStateException("Dark upgrade failed");
            p.closeContainer();p.setGameMode(GameType.CREATIVE);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","dark_meow_blade_stage_8"))));
            var level=p.serverLevel();
            for(int x=-3;x<=3;x++)for(int z=-3;z<=4;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.defaultBlockState());
            p.teleportTo(level,0,100,0,java.util.Set.of(),0,0);
            level.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
            target=net.minecraft.world.entity.EntityType.HUSK.create(level);
            target.setPos(0,100,2);target.setNoAi(true);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);level.addFreshEntity(target);
        });
        if(tick==320 || tick==350) {
            var packet=new com.abyssredemption.absmod.network.HeavyIntentPayload(tick,true,mc.player.getInventory().selected,mc.level.dimension().location());
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
        }
        if(tick==326)server.execute(()->{if(target.getHealth()!=200)throw new IllegalStateException("Heavy struck before windup");});
        if(tick>=330 && tick<=344 && tick%2==0)Screenshot.grab(mc.gameDirectory,"progression-heavy-"+tick+".png",mc.getMainRenderTarget(),message->{});
        if(tick==375)server.execute(()->{
            if(Math.abs(target.getHealth()-110)>0.01)throw new IllegalStateException("Heavy damage/replay/cooldown mismatch: "+target.getHealth());
            var p=server.getPlayerList().getPlayers().getFirst();
            for(var name:java.util.List.of("normal_stage_2","dark_stage_2","dark_heavy")) {
                var advancement=server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("absmod","progression/"+name));
                if(advancement==null || !p.getAdvancements().getOrStartProgress(advancement).isDone())throw new IllegalStateException("Missing advancement "+name);
            }
            com.mojang.logging.LogUtils.getLogger().info("PROGRESSION_DARK passed dark upgrade, 12-tick windup, one 90-damage primary hit, duplicate/cooldown rejection and real advancement grants");
        });
        if(tick==395)mc.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(mc.getConnection().getAdvancements()));
        if(tick==420)Screenshot.grab(mc.gameDirectory,"progression-advancements.png",mc.getMainRenderTarget(),message->{});
        if(tick==440) {
            mc.setScreen(null);
            server.execute(()->{
                var p=server.getPlayerList().getPlayers().getFirst();
                var guide=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","field_guide")));
                p.setItemInHand(InteractionHand.MAIN_HAND,guide);guide.getItem().use(p.serverLevel(),p,InteractionHand.MAIN_HAND);
            });
        }
        if(tick==460 && !(mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen))throw new IllegalStateException("Field guide screen did not open");
        if(tick>=465 && tick<=615 && (tick-465)%10==0 && mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen book) {
            book.setPage((tick-465)/10);
        }
        if(tick>=468 && tick<=618 && (tick-468)%10==0)
            Screenshot.grab(mc.gameDirectory,"field-guide-page-"+((tick-468)/10+1)+".png",mc.getMainRenderTarget(),message->{});
        if(tick==630) {
            mc.setScreen(null);
            server.execute(()->{
                var p=server.getPlayerList().getPlayers().getFirst();target.discard();
                var a=net.minecraft.world.entity.EntityType.CAT.create(p.serverLevel());
                var b=net.minecraft.world.entity.EntityType.CAT.create(p.serverLevel());
                var empty=java.util.Optional.<java.util.UUID>empty();
                ((com.abyssredemption.absmod.cat.CatGenetics)a).absmod$setGenome(new com.abyssredemption.absmod.cat.CatGenome(1,"minecraft:all_black",empty,empty,0));
                ((com.abyssredemption.absmod.cat.CatGenetics)b).absmod$setGenome(new com.abyssredemption.absmod.cat.CatGenome(3,"minecraft:all_black",empty,empty,0));
                p.teleportTo(p.serverLevel(),0,100,-2,java.util.Set.of(),0,18);
                a.setPos(-1,100,2);b.setPos(1,100,2);a.setNoAi(true);b.setNoAi(true);p.serverLevel().addFreshEntity(a);p.serverLevel().addFreshEntity(b);
                com.abyssredemption.absmod.item.FieldGuideItem.observe(p,a,InteractionHand.MAIN_HAND);
                p.setShiftKeyDown(true);com.abyssredemption.absmod.item.FieldGuideItem.observe(p,a,InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);
                com.abyssredemption.absmod.item.FieldGuideItem.observe(p,b,InteractionHand.MAIN_HAND);
                com.mojang.logging.LogUtils.getLogger().info("CAT_GUIDE_VISUAL guide opened; dense/carrier and dilute variants spawned; 50 percent prediction requested");
            });
        }
        if(tick==655)Screenshot.grab(mc.gameDirectory,"cat-guide-observation.png",mc.getMainRenderTarget(),message->{});
        if(tick==660)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            var file=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            var chapter=file.getChapter(Long.parseUnsignedLong("1C4E810187B7DDC2",16));
            if(chapter==null || chapter.getQuests().size()!=29)throw new IllegalStateException("FTB chapter missing or wrong quest count");
            var data=file.getTeamData(p).orElseThrow();
            int completed=0;
            for(var quest:chapter.getQuests()) {
                if(data.isCompleted(quest))completed++;
                if(quest.getTitle().getString().isBlank())throw new IllegalStateException("FTB title empty");
            }
            if(completed<4)throw new IllegalStateException("FTB advancement detection incomplete: "+completed);
            data.saveIfChanged();
            com.mojang.logging.LogUtils.getLogger().info("FTB_PORT_CHECK chapter={} quests=29 completed={} team={}",chapter.getTitle().getString(),completed,p.getUUID());
        });
        if(tick==680)dev.ftb.mods.ftbquests.client.ClientQuestFile.openGui();
        if(tick==705)Screenshot.grab(mc.gameDirectory,"ftb-port-quests.png",mc.getMainRenderTarget(),message->{});
        if(tick==730) {
            mc.setScreen(null);
            server.execute(()->{
                var p=server.getPlayerList().getPlayers().getFirst();
                p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","meow_blade"))));
                BladeSoulService.initialize(BladeSoulStore.forInitialization(server),p.getMainHandItem(),p.getUUID());
                spirit=SpiritTransformationService.manifest(p);spirit.setNoAi(true);spirit.setPos(0,100,1);spirit.setYRot(180);spirit.setYHeadRot(180);
            });
        }
        if(tick==765)Screenshot.grab(mc.gameDirectory,"port-spirit-and-projection.png",mc.getMainRenderTarget(),message->{});
        if(tick==780)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            if(!SpiritTransformationService.recallForAttack(p))throw new IllegalStateException("Spirit attack recall failed");
        });
        if(tick==795)Screenshot.grab(mc.gameDirectory,"port-first-person-rest.png",mc.getMainRenderTarget(),message->{});
        if(tick>=800 && tick<880 && tick%16==0) {
            final long sequence=tick;
            server.execute(()->{var p=server.getPlayerList().getPlayers().getFirst();net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,
                new com.abyssredemption.absmod.network.BladeSwingPayload(p.getUUID(),java.util.UUID.randomUUID(),sequence,p.serverLevel().getGameTime(),4));});
        }
        if(tick>=802 && tick<880 && tick%2==0)Screenshot.grab(mc.gameDirectory,"port-motion-"+tick+".png",mc.getMainRenderTarget(),message->{});
        if(tick==900)mc.reloadResourcePacks();
        if(tick==950) {
            if(!com.abyssredemption.absmod.client.animation.BladeMotionPlayer.isReady())throw new IllegalStateException("Motion reload failed");
            com.mojang.logging.LogUtils.getLogger().info("PORT_CLIENT_PASS progression, guide, FTB, manifestation/recall, motion and reload fixture completed");mc.stop();
        }
    }
}
