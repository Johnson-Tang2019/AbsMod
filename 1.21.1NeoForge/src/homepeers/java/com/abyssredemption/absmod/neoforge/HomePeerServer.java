package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.block.*;
import com.abyssredemption.absmod.cat.*;
import com.abyssredemption.absmod.entity.*;
import com.abyssredemption.absmod.spirit.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Network logins, separate chunk lifetimes and a disconnect on the lethal hit. */
@Mod(value="absmod",dist=Dist.DEDICATED_SERVER)
public final class HomePeerServer {
    private static final BlockPos NEST=new BlockPos(8008,92,8008), AWAY=NEST.east(384), MIRROR=NEST.west(3);
    private int ticks, phase, idle;
    private UUID catId, soul, guestSoul;
    private ServerPlayer oldOwner;
    private BladeSpiritEntity spirit;
    private MilkshakeTrayBlockEntity tray;
    public HomePeerServer() { NeoForge.EVENT_BUS.addListener(this::tick); }
    private void tick(ServerTickEvent.Post e) {
        var server=e.getServer();var level=server.overworld();
        var owner=server.getPlayerList().getPlayerByName("HomeOwner");
        var guest=server.getPlayerList().getPlayerByName("HomeGuest");
        check(++ticks<5400,"timeout, phase="+phase);
        if (owner==null || guest==null) return;
        if (phase==0) {
            level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true,server);
            System.out.println("HOME_PEERS_CONNECTED "+owner.getGameProfile().getName()+" "+guest.getGameProfile().getName());
            for(var origin:List.of(NEST,AWAY)) {
                level.getChunkAt(origin);
                for(int x=-6;x<=6;x++) for(int z=-5;z<=5;z++) level.setBlockAndUpdate(origin.offset(x,-1,z),Blocks.CHERRY_PLANKS.defaultBlockState());
            }
            move(owner,NEST.south(2));move(guest,NEST.south(3));
            level.setBlockAndUpdate(NEST,block("cat_nest"));
            var c=EntityType.CAT.create(level);c.tame(owner);c.setPos(Vec3.atBottomCenterOf(NEST.north(2)));level.addFreshEntity(c);catId=c.getUUID();
            var store=CatLife.store(owner);store.place(GlobalPos.of(level.dimension(),NEST),owner.getUUID());
            CatLife.select(owner,c);CatLife.bind(owner,NEST);CatLife.useNest(guest,NEST,true);
            check(store.home(catId).owner().equals(owner.getUUID()),"guest cannot unbind");
            var table=NEST.east(2);
            level.setBlockAndUpdate(table,block("cherry_tea_table"));
            hold(owner,stack("milkshake_tray"));click(owner,table);
            tray=(MilkshakeTrayBlockEntity)level.getBlockEntity(table);
            hold(owner,stack("pink_milkshake"));click(owner,table);
            hold(owner,stack("meow_blade"));BladeSoulService.initialize(BladeSoulStore.forInitialization(server),owner.getMainHandItem(),owner.getUUID());
            spirit=SpiritTransformationService.manifest(owner);
            owner.getInventory().selected=1;
            phase++;idle=0;
        } else if (phase==1 && ++idle>28) {
            var c=(Cat)level.getEntity(catId);
            if(!c.isLying()) return;
            var table=tray.getBlockPos();
            hold(owner,stack("bond_bell"));owner.interactOn(spirit,InteractionHand.MAIN_HAND);click(owner,table);
            hold(guest,owner.getMainHandItem().copy());guest.interactOn(spirit,InteractionHand.MAIN_HAND);click(guest,table);
            hold(guest,ItemStack.EMPTY);click(guest,table);
            check(guest.getInventory().countItem(stack("pink_milkshake").getItem())==1,"race cup taken once");
            phase++;idle=0;
        } else if (phase==2 && ++idle>45) {
            check(owner.getInventory().countItem(Items.BUCKET)==0,"no settlement for withdrawn cup");
            var visit=CatLife.store(owner).visit(owner.getUUID(),catId);
            check(visit!=null && visit.spot().pos().equals(NEST),"completed rest");
            owner.getInventory().selected=0;
            SpiritTransformationService.recall(owner,spirit,0);
            check(owner.getMainHandItem().is(stack("meow_blade").getItem()),"withdrawal preserved original");
            CatLife.callSelected(owner);
            var c=(Cat)level.getEntity(catId);c.setOrderedToSit(true);
            move(guest,AWAY.south(2));
            phase=8;
            idle=0;
        } else if (phase==8 && ++idle>40 && level.isPositionEntityTicking(AWAY)) {
            var c=(Cat)level.getEntity(catId);
            check(c!=null,"cat still present before moving to loaded destination");
            c.teleportTo(level,AWAY.getX()+.5,AWAY.getY(),AWAY.getZ()+.5,Set.of(),0,0);
            move(owner,AWAY.south(1));
            phase=3;
        } else if (phase==3 && !level.hasChunkAt(NEST) && level.getEntity(catId) instanceof Cat) {
            check(level.getEntity(catId) instanceof Cat && CatLife.store(owner).home(catId).spot().pos().equals(NEST),"nest unloaded separately");
            move(owner,NEST);move(guest,NEST.south(2));
            phase++;
        } else if (phase==4 && !level.hasChunkAt(AWAY)) {
            check(level.getEntity(catId)==null && CatLife.store(owner).visit(owner.getUUID(),catId)!=null,"cat unloaded separately");
            level.destroyBlock(NEST,true,owner);
            check(CatLife.store(owner).home(catId)==null,"move nest while cat unloaded");
            move(guest,AWAY);
            phase++;
        } else if (phase==5 && level.getEntity(catId) instanceof Cat c) {
            move(owner,AWAY.south(1));level.setBlockAndUpdate(AWAY.east(1),block("cherry_cat_cushion"));
            CatLife.store(owner).place(GlobalPos.of(level.dimension(),AWAY.east(1)),owner.getUUID());
            CatLife.select(owner,c);CatLife.bind(owner,AWAY.east(1));
            check(CatLife.store(owner).home(catId).spot().pos().equals(AWAY.east(1)),"moved nest rebound to original cat");
            move(owner,MIRROR.south(2));move(guest,MIRROR.south(3));
            idle=0;
            phase=10;
        } else if (phase==10 && ++idle>30 && level.isPositionEntityTicking(MIRROR)) {
            level.setBlockAndUpdate(MIRROR,block("heart_mirror"));
            var store=BladeSoulStore.forInitialization(server);
            hold(owner,stack("meow_blade_stage_8"));soul=BladeSoulService.initialize(store,owner.getMainHandItem(),owner.getUUID()).soulId();
            hold(guest,stack("meow_blade_stage_8"));guestSoul=BladeSoulService.initialize(store,guest.getMainHandItem(),guest.getUUID()).soulId();
            click(guest,MIRROR);click(owner,MIRROR);
            check(echoes(level).size()==1,"first challenger owns mirror: count="+echoes(level).size()+" guest="+guest.position());
            move(owner,NEST.east(5));
            guest.hurt(guest.damageSources().genericKill(),Float.MAX_VALUE);
            phase=9;
        } else if (phase==9 && guest.isAlive() && echoes(level).isEmpty() && guest.distanceToSqr(Vec3.atCenterOf(MIRROR))>144) {
            var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(MIRROR).inflate(7),
                    item -> item.getItem().has(BladeSoulComponents.REFERENCE)
                            && item.getItem().get(BladeSoulComponents.REFERENCE).soulId().equals(guestSoul));
            check(drops.size()==1 && !BladeSoulStore.forInitialization(server).find(guestSoul).orElseThrow().mirrorCleared(),"defeat drops one unqualified original");
            var drop=drops.getFirst();
            move(guest,drop.blockPosition());
            drop.setNoPickUpDelay();
            drop.playerTouch(guest);
            for(int i=0;i<9;i++) {
                var ref=guest.getInventory().getItem(i).get(BladeSoulComponents.REFERENCE);
                if(ref!=null && ref.soulId().equals(guestSoul)) guest.getInventory().selected=i;
            }
            check(guest.getMainHandItem().has(BladeSoulComponents.REFERENCE),"defeated player retrieved sword");
            move(owner,MIRROR.south(2));move(guest,MIRROR.south(3));
            click(owner,MIRROR);click(guest,MIRROR);
            check(echoes(level).size()==1,"two challengers one mirror");
            phase=6;
        } else if (phase==6) {
            var echo=echoes(level).getFirst();
            if(echo.phase()!=2) return;
            check(!echo.hurt(guest.damageSources().playerAttack(guest),1000),"foreign lethal hit rejected");
            check(echo.hurt(owner.damageSources().playerAttack(owner),1000),"owner lethal hit");
            oldOwner=owner;
            owner.connection.disconnect(Component.literal("HOME_PEERS_REJOIN"));
            phase++;
        } else if (phase==7 && owner!=oldOwner) {
            var store=BladeSoulStore.forInitialization(server);
            var won=BladeSoulService.requireRecord(store,owner.getMainHandItem());
            check(won.soulId().equals(soul) && won.mirrorCleared() && !BladeSoulService.requireRecord(store,guest.getMainHandItem()).mirrorCleared(),"winner survived disconnect; guest unqualified");
            check(CatLife.store(owner).visit(owner.getUUID(),catId)!=null,"owner rest survives relog");
            server.saveEverything(true,true,true);
            System.out.println("HOME_PEERS_PASS real clients, tea race, independent chunk unload, relocation, defeat drop, winner relog");
            owner.connection.disconnect(Component.literal("HOME_PEERS_DONE"));guest.connection.disconnect(Component.literal("HOME_PEERS_DONE"));
            server.halt(false);phase++;
        }
    }
    private static List<MirrorEchoEntity> echoes(ServerLevel level) {return level.getEntitiesOfClass(MirrorEchoEntity.class,new AABB(MIRROR).inflate(6));}
    private static void move(ServerPlayer p,BlockPos pos){p.teleportTo(p.serverLevel(),pos.getX()+.5,pos.getY(),pos.getZ()+.5,Set.of(),0,0);}
    private static void hold(ServerPlayer p,ItemStack s){p.setItemInHand(InteractionHand.MAIN_HAND,s);}
    private static ItemStack stack(String key){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:"+key)));}
    private static net.minecraft.world.level.block.state.BlockState block(String key){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("absmod:"+key)).defaultBlockState();}
    private static void click(ServerPlayer p,BlockPos pos){
        p.gameMode.useItemOn(p,p.level(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atLowerCornerOf(pos).add(.3,.75,.3),Direction.UP,pos,false));
    }
    private static void check(boolean pass,String detail){if(!pass)throw new IllegalStateException("Home peers: "+detail);}
}
