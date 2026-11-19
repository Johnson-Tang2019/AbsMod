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
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "resonance"),
                com.abyssredemption.absmod.spirit.ResonanceMenu.TYPE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID,"meow_blade_crafting"), com.abyssredemption.absmod.item.MeowBladeRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID,"dark_blade_crafting"), com.abyssredemption.absmod.item.MeowBladeRecipe.DARK_SERIALIZER);
        var guideKey=ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(AbsMod.MOD_ID,"field_guide"));
        var guide = Registry.register(BuiltInRegistries.ITEM, guideKey,
                new com.abyssredemption.absmod.item.FieldGuideItem(new Item.Properties().setId(guideKey)));
        registerCatLife(guide);
        var resonanceKey=ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID,"resonance_heart"));
        var resonance=Registry.register(BuiltInRegistries.ITEM,resonanceKey,new com.abyssredemption.absmod.item.ResonanceHeartItem(new Item.Properties().setId(resonanceKey)));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries->entries.accept(resonance));
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
        var darkBlades=Arrays.stream(com.abyssredemption.absmod.item.DarkBladeStage.values()).map(AbsModFabric::registerMeowBlade).toList();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries->darkBlades.forEach(entries::accept));
        for (var stage : java.util.stream.Stream.concat(Arrays.stream(MeowBladeStage.values()),Arrays.stream(com.abyssredemption.absmod.item.DarkBladeStage.values())).toList()) {
            var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "spectral_" + stage.itemId()));
            Registry.register(BuiltInRegistries.ITEM, key, new com.abyssredemption.absmod.item.SpectralBladeItem(new Item.Properties().setId(key)));
        }
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

    private static Item registerMeowBlade(com.abyssredemption.absmod.item.BladeStage stage) {
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
    private static void registerCatLife(Item guide) {
        var nestId = Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "cat_nest");
        var nestKey = ResourceKey.create(Registries.BLOCK, nestId);
        var nest = Registry.register(BuiltInRegistries.BLOCK, nestKey, new com.abyssredemption.absmod.cat.CatNestBlock(
                net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(nestKey)
                        .noCollision().strength(0.4F).sound(net.minecraft.world.level.block.SoundType.WOOL)
                        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
        var itemKey = ResourceKey.create(Registries.ITEM, nestId);
        var nestItem = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new net.minecraft.world.item.BlockItem(nest, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        var bellKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "bond_bell"));
        var bell = Registry.register(BuiltInRegistries.ITEM, bellKey,
                new com.abyssredemption.absmod.item.BondBellItem(new Item.Properties().setId(bellKey)));
        var brushKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "gentle_brush"));
        var brush = Registry.register(BuiltInRegistries.ITEM, brushKey,
                new com.abyssredemption.absmod.item.GentleBrushItem(new Item.Properties().setId(brushKey)));
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "cat_journal"),
                com.abyssredemption.absmod.cat.CatJournalMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "spirit_board_game"),
                com.abyssredemption.absmod.spirit.SpiritGameMenu.TYPE);
        var adornments = com.abyssredemption.absmod.spirit.SpiritAdornment.items().stream()
                .map(style -> {
                    var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, style.getSerializedName()));
                    return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)));
                }).toList();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(nestItem);
            entries.accept(bell);
            entries.accept(brush);
            entries.accept(guide);
            adornments.forEach(entries::accept);
        });
    }
}
