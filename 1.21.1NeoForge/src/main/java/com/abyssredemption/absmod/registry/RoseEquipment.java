package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.item.RoseCharmItem;
import com.abyssredemption.absmod.item.SpiritKeepsake;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RoseEquipment {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "absmod");
    public static final DeferredItem<Item> PAW_CRYSTAL = ModItems.ITEMS.registerSimpleItem("paw_crystal");
    private RoseEquipment() {}

    public static void register(IEventBus bus) {
        var rose = MATERIALS.register("rose_guard", () -> {
            var iron = ArmorMaterials.IRON.value();
            return new ArmorMaterial(iron.defense(), iron.enchantmentValue(), iron.equipSound(),
                    () -> Ingredient.of(PAW_CRYSTAL.get()), List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath("absmod", "rose_guard"))), iron.toughness(), iron.knockbackResistance());
        });
        MATERIALS.register(bus);
        for (var type : List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS)) {
            ModItems.ITEMS.registerItem("rose_" + type.getName(), properties -> new ArmorItem(rose, type,
                    properties.durability(type.getDurability(15))));
        }
        var tier = Tiers.IRON;
        ModItems.ITEMS.registerItem("rose_pickaxe", p -> new PickaxeItem(tier, p.attributes(PickaxeItem.createAttributes(tier, 1, -2.8F))));
        ModItems.ITEMS.registerItem("rose_axe", p -> new AxeItem(tier, p.attributes(AxeItem.createAttributes(tier, 6, -3.1F))));
        ModItems.ITEMS.registerItem("rose_shovel", p -> new ShovelItem(tier, p.attributes(ShovelItem.createAttributes(tier, 1.5F, -3F))));
        ModItems.ITEMS.registerItem("rose_hoe", p -> new HoeItem(tier, p.attributes(HoeItem.createAttributes(tier, -2, -1F))));
        for (var kind : SpiritKeepsake.Kind.values()) {
            ModItems.ITEMS.registerItem(kind.id(), p -> new SpiritKeepsake(kind, p));
        }
        for (var charm : RoseCharmItem.Kind.values()) {
            ModItems.ITEMS.registerItem(charm.id(), p -> new RoseCharmItem(charm, p));
        }
    }
}
