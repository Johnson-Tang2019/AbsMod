package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.registry.RoseGold;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TieredItem.class)
abstract class RoseGoldEnchantmentMixin extends Item {
    private RoseGoldEnchantmentMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        // A grindstone can remove the stored Silk Touch entry, but the material still has it.
        return !(enchantment.is(Enchantments.FORTUNE) && stack.is(RoseGold.SILK_TOOLS))
                && super.supportsEnchantment(stack, enchantment);
    }
}
