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
public final class ProgressionVisualTest {
    private boolean started;
    private int tick;
    private net.minecraft.world.entity.monster.zombie.Husk target;
    public ProgressionVisualTest(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!started && mc.gui.screen() instanceof TitleScreen) {
            started=true;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);
            mc.getWindow().setWindowed(1280,900);
            mc.createWorldOpenFlows().createFreshLevel("absmod-progression-"+System.currentTimeMillis(),
                new LevelSettings("Progression verification",GameType.SURVIVAL,
                    new LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(42,false,false),lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET)
                    .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if(!started || mc.player==null || mc.getSingleplayerServer()==null)return;
        tick++;var server=mc.getSingleplayerServer();
        if(tick==30)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            var heart=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","resonance_heart")));
            p.setItemInHand(InteractionHand.MAIN_HAND,heart);
            heart.getItem().use(p.level(),p,InteractionHand.MAIN_HAND);
            if(!(p.containerMenu instanceof ResonanceMenu menu))throw new IllegalStateException("Heart did not open resonance");
            var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
            sword.setDamageValue(780);
            var store=BladeSoulStore.forInitialization(server);BladeSoulService.initialize(store,sword,p.getUUID());
            BladeSoulService.setTestAffection(store,sword,25);
            menu.getSlot(0).set(sword);menu.getSlot(1).set(new ItemStack(Items.AMETHYST_SHARD,16));
            menu.getSlot(2).set(new ItemStack(Items.COPPER_INGOT,16));menu.broadcastChanges();
        });
        if(tick==80 || tick==130)Screenshot.grab(mc.gameDirectory,"progression-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==100)mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,22,0,ContainerInput.PICKUP,mc.player);
        if(tick==150)mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,22,0,ContainerInput.PICKUP,mc.player);
        if(tick==170)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();var menu=p.containerMenu;
            if(!menu.getSlot(0).getItem().is(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade_stage_2")))
                || menu.getSlot(0).getItem().getDamageValue()!=900 || menu.getSlot(1).getItem().getCount()!=8)
                throw new IllegalStateException("Progression transaction failed");
            p.closeContainer();
            boolean restored=false;
            for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(
                BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade_stage_2"))))restored=true;
            if(!restored)throw new IllegalStateException("Closing menu lost blade");
            com.mojang.logging.LogUtils.getLogger().info("PROGRESSION_VISUAL passed actual heart use, packet upgrade, replay rejection and close return");
        });
        if(tick==200)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","dark_meow_blade")));
            p.setItemInHand(InteractionHand.MAIN_HAND,sword);
            var store=BladeSoulStore.forInitialization(server);BladeSoulService.initialize(store,sword,p.getUUID());BladeSoulService.setTestAffection(store,sword,100);
            p.openMenu(new SimpleMenuProvider((id,inventory,player)->new ResonanceMenu(id,inventory),net.minecraft.network.chat.Component.literal("Dark resonance")));
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            p.containerMenu.getSlot(0).set(sword);p.containerMenu.getSlot(1).set(new ItemStack(Items.OBSIDIAN,8));
        });
        if(tick==230 || tick==270)Screenshot.grab(mc.gameDirectory,"progression-dark-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==240)mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,22,0,ContainerInput.PICKUP,mc.player);
        if(tick==280)server.execute(()->{
            var p=server.getPlayerList().getPlayers().getFirst();
            if(!p.containerMenu.getSlot(0).getItem().is(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","dark_meow_blade_stage_2"))))throw new IllegalStateException("Dark upgrade failed");
            p.closeContainer();p.setGameMode(GameType.CREATIVE);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","dark_meow_blade_stage_8"))));
            var level=p.level();
            for(int x=-3;x<=3;x++)for(int z=-3;z<=4;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.defaultBlockState());
            p.teleportTo(level,0,100,0,java.util.Set.of(),0,0,true);
            level.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
            target=net.minecraft.world.entity.EntityTypes.HUSK.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            target.setPos(0,100,2);target.setNoAi(true);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);level.addFreshEntity(target);
        });
        if(tick==320 || tick==350) {
            var packet=new com.abyssredemption.absmod.network.HeavyIntentPayload(tick,true,mc.player.getInventory().getSelectedSlot(),mc.level.dimension().identifier());
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
        }
        if(tick==326)server.execute(()->{if(target.getHealth()!=200)throw new IllegalStateException("Heavy struck before windup");});
        if(tick>=330 && tick<=344 && tick%2==0)Screenshot.grab(mc.gameDirectory,"progression-heavy-"+tick+".png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==375)server.execute(()->{
            if(Math.abs(target.getHealth()-110)>0.01)throw new IllegalStateException("Heavy damage/replay/cooldown mismatch: "+target.getHealth());
            var p=server.getPlayerList().getPlayers().getFirst();
            for(var name:java.util.List.of("normal_stage_2","dark_stage_2","dark_heavy")) {
                var advancement=server.getAdvancements().get(Identifier.fromNamespaceAndPath("absmod","progression/"+name));
                if(advancement==null || !p.getAdvancements().getOrStartProgress(advancement).isDone())throw new IllegalStateException("Missing advancement "+name);
            }
            com.mojang.logging.LogUtils.getLogger().info("PROGRESSION_DARK passed dark upgrade, 12-tick windup, one 90-damage primary hit, duplicate/cooldown rejection and real advancement grants");
        });
        if(tick==395)mc.gui.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(mc.getConnection().getAdvancements()));
        if(tick==420)Screenshot.grab(mc.gameDirectory,"progression-advancements.png",mc.gameRenderer.mainRenderTarget(),1,message->{});
        if(tick==440)mc.stop();
    }
}
