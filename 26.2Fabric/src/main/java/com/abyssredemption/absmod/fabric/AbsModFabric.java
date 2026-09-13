package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.command.ModCommands;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.MeowBladeStage;
import com.abyssredemption.absmod.item.MilkshakeItem;
import com.abyssredemption.absmod.item.MilkshakeVariant;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class AbsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricCombatProbe.register();
        var spiritType = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                com.abyssredemption.absmod.entity.SpiritEntityType.KEY,
                com.abyssredemption.absmod.entity.SpiritEntityType.create());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(spiritType,
                com.abyssredemption.absmod.entity.BladeSpiritEntity.attributes());
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                com.abyssredemption.absmod.spirit.BladeSoulComponents.ID,
                com.abyssredemption.absmod.spirit.BladeSoulComponents.REFERENCE);
        List<Item> meowBlades = Arrays.stream(MeowBladeStage.values())
                .map(AbsModFabric::registerMeowBlade)
                .toList();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(entries -> meowBlades.forEach(item -> entries.accept(item)));
        List<Item> milkshakes = Arrays.stream(MilkshakeVariant.values())
                .map(AbsModFabric::registerMilkshake).toList();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> milkshakes.forEach(item -> entries.accept(item)));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                com.abyssredemption.absmod.item.ModCreativeTab.ID,
                com.abyssredemption.absmod.item.ModCreativeTab.create(
                        net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder(),
                        stage -> meowBlades.get(stage.number() - 1), variant -> milkshakes.get(variant.ordinal())));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
                ModCommands.register(dispatcher, stage -> meowBlades.get(stage.number() - 1),
                        variant -> milkshakes.get(variant.ordinal())));
    }

    private static Item registerMeowBlade(MeowBladeStage stage) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, stage.itemId()));
        return Registry.register(BuiltInRegistries.ITEM, key,
                new MeowBladeItem(new Item.Properties().setId(key), stage));
    }

    private static Item registerMilkshake(MilkshakeVariant variant) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, variant.itemId()));
        return Registry.register(BuiltInRegistries.ITEM, key,
                new MilkshakeItem(new Item.Properties().setId(key), variant));
    }
}
