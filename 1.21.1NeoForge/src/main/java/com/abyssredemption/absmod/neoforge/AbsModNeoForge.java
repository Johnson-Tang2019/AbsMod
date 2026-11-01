package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.command.ModCommands;
import com.abyssredemption.absmod.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(AbsMod.MOD_ID)
public final class AbsModNeoForge {
    private static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITIES =
            net.neoforged.neoforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.ENTITY_TYPE, AbsMod.MOD_ID);
    static {
        ENTITIES.register("blade_spirit", com.abyssredemption.absmod.entity.SpiritEntityType::create);
    }
    private static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(com.abyssredemption.absmod.entity.SpiritEntityType.get(),
                com.abyssredemption.absmod.entity.BladeSpiritEntity.attributes().build());
    }
    private static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.item.CreativeModeTab> TABS =
            net.neoforged.neoforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, AbsMod.MOD_ID);
    static {
        TABS.register(com.abyssredemption.absmod.item.ModCreativeTab.ID.getPath(), () ->
                com.abyssredemption.absmod.item.ModCreativeTab.create(net.minecraft.world.item.CreativeModeTab.builder(),
                        stage -> ModItems.MEOW_BLADES.get(stage.number() - 1).get(),
                        variant -> ModItems.MILKSHAKES.get(variant.ordinal()).get()));
    }
    private static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS =
            net.neoforged.neoforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, AbsMod.MOD_ID);
    static {
        COMPONENTS.register("blade_soul", () -> com.abyssredemption.absmod.spirit.BladeSoulComponents.REFERENCE);
    }

    private static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES = net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER,AbsMod.MOD_ID);
    static { RECIPES.register("meow_blade_crafting",()->com.abyssredemption.absmod.item.MeowBladeRecipe.SERIALIZER); RECIPES.register("dark_blade_crafting",()->com.abyssredemption.absmod.item.MeowBladeRecipe.DARK_SERIALIZER); }
    public AbsModNeoForge(IEventBus modEventBus) {
        RECIPES.register(modEventBus);
        new NeoForgeCombatProbe(modEventBus);
        COMPONENTS.register(modEventBus);
        ENTITIES.register(modEventBus);
        modEventBus.addListener(AbsModNeoForge::attributes);
        ModItems.ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(AbsModNeoForge::addCreativeItems);
        NeoForge.EVENT_BUS.addListener(AbsModNeoForge::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(),
                stage -> ModItems.MEOW_BLADES.get(stage.number() - 1).get(),
                variant -> ModItems.MILKSHAKES.get(variant.ordinal()).get());
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            ModItems.MEOW_BLADES.forEach(item -> event.accept(item));
            ModItems.DARK_BLADES.forEach(item -> event.accept(item));
            event.accept(ModItems.RESONANCE_HEART);
        }
        if (event.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)) {
            ModItems.MILKSHAKES.forEach(item -> event.accept(item));
        }
    }
}
