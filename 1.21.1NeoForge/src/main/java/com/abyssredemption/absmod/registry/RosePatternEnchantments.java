package com.abyssredemption.absmod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Keep the stored level intact for repairs, books and grindstones; boost gameplay queries on this equipment. */
public final class RosePatternEnchantments {
    public static final TagKey<Item> GEAR = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("absmod","rose_patterned_gear"));
    private RosePatternEnchantments() {}

    public static void boost(GetEnchantmentLevelEvent event) {
        if (!event.getStack().is(GEAR)) return;
        for (var entry : event.getStack().getEnchantments().entrySet()) {
            if (event.isTargetting(entry.getKey())) event.getEnchantments().upgrade(entry.getKey(), Math.min(255, entry.getIntValue() + 1));
        }
    }

    public static void tooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (!stack.is(GEAR)) return;
        for (var entry : stack.getEnchantments().entrySet()) {
            var base = Enchantment.getFullname(entry.getKey(), entry.getIntValue());
            int level = Math.min(255, entry.getIntValue() + 1);
            Component effective = level <= 10 ? Enchantment.getFullname(entry.getKey(), level)
                    : entry.getKey().value().description().copy().append(" " + level).withStyle(base.getStyle());
            event.getToolTip().replaceAll(line -> line.equals(base) ? effective : line);
        }
        event.getToolTip().add(Component.translatable("tooltip.absmod.rose_pattern").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
