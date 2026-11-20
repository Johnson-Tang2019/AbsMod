package com.abyssredemption.absmod.registry;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RoseGold {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "absmod");
    public static final net.minecraft.tags.TagKey<Item> SILK_TOOLS = net.minecraft.tags.TagKey.create(Registries.ITEM, id("rose_gold_tools"));
    private RoseGold() {}

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("absmod", path); }
    private static Component hint(String path) { return Component.translatable("smithing.absmod.rose_gold." + path); }

    public static void register(IEventBus bus) {
        var ingot = ModItems.ITEMS.registerSimpleItem("rose_gold_ingot");
        var storage = ModBlocks.BLOCKS.register("rose_gold_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK)));
        ModItems.ITEMS.registerSimpleBlockItem(storage);
        ModItems.ITEMS.register("rose_gold_upgrade_template", () -> new SmithingTemplateItem(
                hint("applies"), ingot.get().getDescription(), hint("title"), hint("base"), hint("addition"),
                List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe"), ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate")),
                List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"))));
        var tier = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1800, 8.5F, 3, 22, () -> Ingredient.of(ingot.get()));
        ModItems.ITEMS.registerItem("rose_gold_pickaxe", p -> new PickaxeItem(tier, p.attributes(PickaxeItem.createAttributes(tier, 1, -2.8F))));
        ModItems.ITEMS.registerItem("rose_gold_axe", p -> new AxeItem(tier, p.attributes(AxeItem.createAttributes(tier, 5, -3F))));
        ModItems.ITEMS.registerItem("rose_gold_shovel", p -> new ShovelItem(tier, p.attributes(ShovelItem.createAttributes(tier, 1.5F, -3F))));
        ModItems.ITEMS.registerItem("rose_gold_hoe", p -> new HoeItem(tier, p.attributes(HoeItem.createAttributes(tier, -3, 0F))));
        ModItems.ITEMS.registerItem("rose_gold_sword", p -> new SwordItem(tier, p.attributes(SwordItem.createAttributes(tier, 3, -2.4F))));
        var armor = MATERIALS.register("rose_gold", () -> new ArmorMaterial(ArmorMaterials.DIAMOND.value().defense(), 22,
                ArmorMaterials.DIAMOND.value().equipSound(), () -> Ingredient.of(ingot.get()),
                List.of(new ArmorMaterial.Layer(id("rose_gold"))), 2, 0));
        MATERIALS.register(bus);
        for (var type : List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS)) {
            ModItems.ITEMS.registerItem("rose_gold_" + type.getName(), p -> new ArmorItem(armor, type, p.durability(type.getDurability(36))));
        }
    }

    public static void enchantments(GetEnchantmentLevelEvent event) {
        if (!event.getStack().is(SILK_TOOLS)) return;
        if (event.isTargetting(Enchantments.SILK_TOUCH))
            event.getHolder(Enchantments.SILK_TOUCH).ifPresent(holder -> event.getEnchantments().upgrade(holder, 1));
        if (event.isTargetting(Enchantments.FORTUNE))
            event.getHolder(Enchantments.FORTUNE).ifPresent(holder -> event.getEnchantments().set(holder, 0));
    }
}
