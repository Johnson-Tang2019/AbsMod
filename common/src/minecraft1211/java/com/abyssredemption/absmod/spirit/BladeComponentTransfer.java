package com.abyssredemption.absmod.spirit;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Preserve user data while deriving combat defaults and durability from the new item. */
public final class BladeComponentTransfer {
    private BladeComponentTransfer() {}
    public static ItemStack transfer(ItemStack source, Item target) {
        ItemStack result=source.transmuteCopy(target,1);
        var defaults=new ItemStack(target);
        result.set(DataComponents.ATTRIBUTE_MODIFIERS,defaults.get(DataComponents.ATTRIBUTE_MODIFIERS));
        result.set(DataComponents.MAX_DAMAGE,defaults.get(DataComponents.MAX_DAMAGE));
        result.set(DataComponents.TOOL,defaults.get(DataComponents.TOOL));
        int damage=source.getMaxDamage()==0?0:(int)Math.ceil((double)source.getDamageValue()*result.getMaxDamage()/source.getMaxDamage());
        result.setDamageValue(Math.min(damage,result.getMaxDamage()));
        return result;
    }
}
