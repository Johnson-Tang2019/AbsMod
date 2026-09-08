package com.abyssredemption.absmod.fabric;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.command.ModCommands;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.MeowBladeStage;
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
        List<Item> meowBlades = Arrays.stream(MeowBladeStage.values())
                .map(AbsModFabric::registerMeowBlade)
                .toList();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(entries -> meowBlades.forEach(item -> entries.accept(item)));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
                ModCommands.register(dispatcher, stage -> meowBlades.get(stage.number() - 1)));
    }

    private static Item registerMeowBlade(MeowBladeStage stage) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, stage.itemId()));
        return Registry.register(BuiltInRegistries.ITEM, key,
                new MeowBladeItem(new Item.Properties().setId(key), stage));
    }
}
