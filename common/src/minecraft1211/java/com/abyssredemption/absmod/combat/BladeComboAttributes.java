package com.abyssredemption.absmod.combat;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;

/** Fixed transient IDs; only the stage base contributes to each linear bonus. */
public final class BladeComboAttributes {
    private static final ResourceLocation DAMAGE=ResourceLocation.fromNamespaceAndPath("absmod","combo_damage");
    private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath("absmod","combo_speed");
    private BladeComboAttributes(){}
    public static void update(ServerPlayer player,int stacks) {
        var item=player.getMainHandItem().getItem();
        double damage=0,speed=0;
        if(player.isAlive() && !player.isSpectator() && !(player.getMainHandItem().getDamageValue() >= player.getMainHandItem().getMaxDamage()) && item instanceof MeowBladeItem blade) {
            int n=Math.clamp(stacks,0,blade.stage().maxComboStacks());
            damage=blade.stage().attackDamage()*.03*n;speed=blade.stage().baseAttackSpeed()*.01*n;
        }
        set(player.getAttribute(Attributes.ATTACK_DAMAGE),DAMAGE,damage);
        set(player.getAttribute(Attributes.ATTACK_SPEED),SPEED,speed);
    }
    private static void set(AttributeInstance attribute,ResourceLocation id,double value) {
        if(attribute==null)return;
        var old=attribute.getModifier(id);
        if(old!=null && old.amount()==value)return;
        attribute.removeModifier(id);
        if(value!=0)attribute.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));
    }
}
