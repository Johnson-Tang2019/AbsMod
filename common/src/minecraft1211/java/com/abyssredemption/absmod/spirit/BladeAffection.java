package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import java.util.*;

/** Growth rewards are separate from short-lived combo stacks and damage amounts. */
public final class BladeAffection {
    private BladeAffection(){}
    private static void award(ServerPlayer owner,UUID soul,int amount) {
        var store=BladeSoulStore.find(owner.level().getServer()).orElseThrow(()->new IllegalStateException("missing_store"));
        var record=store.find(soul).orElseThrow(()->new IllegalStateException("missing_record"));
        if(record.ownerId().filter(owner.getUUID()::equals).isEmpty())throw new IllegalStateException("not_owner");
        store.replace(record,record.withTestAffection(Math.clamp(record.affection().orElse(0D)+amount,0,300)));
        BladeAdvancements.award(owner,"bond");
    }
    private static long day(ServerPlayer player){return player.level().getServer().overworld().getDayTime()/24000;}
    public static void follow(ServerPlayer owner,BladeSpiritEntity spirit,boolean moved) {
        try { followUnchecked(owner,spirit,moved); }
        catch(IllegalStateException error) { reportUnavailable(error); }
    }
    private static void followUnchecked(ServerPlayer owner,BladeSpiritEntity spirit,boolean moved) {
        if(!moved || spirit.distanceToSqr(owner)>256 || !spirit.active())return;
        var store=BladeGrowthStore.get(owner.level().getServer());var d=store.day(spirit.soulId(),day(owner));
        if(d.follow()>=4)return;
        int walking=d.walking()+10;int earned=walking>=6000?1:0;
        if(earned>0)award(owner,spirit.soulId(),earned);
        store.put(new BladeGrowthStore.Day(d.soul(),d.day(),walking%6000,d.follow()+earned,d.hits(),d.combat(),d.talked(),d.gifted(),d.targets()));
    }
    public static InteractionResult interact(ServerPlayer owner,BladeSpiritEntity spirit,InteractionHand hand) {
        try { return interactUnchecked(owner,spirit,hand); }
        catch(IllegalStateException error) {
            reportUnavailable(error);
            if(owner.connection!=null)owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("resonance.absmod.store_unavailable"),true);
            return InteractionResult.FAIL;
        }
    }
    private static InteractionResult interactUnchecked(ServerPlayer owner,BladeSpiritEntity spirit,InteractionHand hand) {
        if(hand!=InteractionHand.MAIN_HAND || owner.isShiftKeyDown() || !owner.getUUID().equals(spirit.ownerId()) || !spirit.active())return InteractionResult.PASS;
        var store=BladeGrowthStore.get(owner.level().getServer());var d=store.day(spirit.soulId(),day(owner));
        boolean gift=owner.getItemInHand(hand).is(Items.COD);
        if(gift?d.gifted():d.talked()) {
            if(owner.connection!=null)owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("resonance.absmod.daily_limit"),true);return InteractionResult.SUCCESS;
        }
        award(owner,spirit.soulId(),gift?2:1);
        if(gift && !owner.hasInfiniteMaterials())owner.getItemInHand(hand).shrink(1);
        store.put(new BladeGrowthStore.Day(d.soul(),d.day(),d.walking(),d.follow(),d.hits(),d.combat(),d.talked()||!gift,d.gifted()||gift,d.targets()));
        if(owner.connection!=null)owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("resonance.absmod.affection_gained",gift?2:1),true);
        return InteractionResult.SUCCESS;
    }
    public static void hit(ServerPlayer owner,Entity target) {
        try { hitUnchecked(owner,target); }
        catch(IllegalStateException error) { reportUnavailable(error); }
    }
    private static void hitUnchecked(ServerPlayer owner,Entity target) {
        if(!(target instanceof net.minecraft.world.entity.monster.Monster) || owner.isAlliedTo(target))return;
        var ref=owner.getMainHandItem().get(BladeSoulComponents.REFERENCE);if(ref==null)return;
        var soulStore=BladeSoulStore.find(owner.level().getServer()).orElseThrow(()->new IllegalStateException("missing_store"));
        var record=BladeSoulService.requireRecord(soulStore,owner.getMainHandItem());
        if(record.ownerId().filter(owner.getUUID()::equals).isEmpty())return;
        var store=BladeGrowthStore.get(owner.level().getServer());var d=store.day(ref.soulId(),day(owner));
        // Conservative anti-farm policy: each hostile entity contributes only once per soul/day.
        if(d.combat()>=4 || d.targets().contains(target.getUUID()))return;
        var targets=new ArrayList<>(d.targets());targets.add(target.getUUID());
        int hits=d.hits()+1,earned=hits>=10?1:0;
        if(earned>0)award(owner,ref.soulId(),earned);
        store.put(new BladeGrowthStore.Day(d.soul(),d.day(),d.walking(),d.follow(),hits%10,d.combat()+earned,d.talked(),d.gifted(),List.copyOf(targets)));
    }
    private static final Set<String> REPORTED=new HashSet<>();
    private static void reportUnavailable(IllegalStateException error) {
        if(REPORTED.add(String.valueOf(error.getMessage())))
            com.mojang.logging.LogUtils.getLogger().error("Blade growth disabled for invalid state: {}",error.getMessage());
    }
}
