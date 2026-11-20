package com.abyssredemption.absmod.compat;

import com.abyssredemption.absmod.item.CookeryFinish;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class CookeryKitchen {
    private CookeryKitchen() {}
    public static void register(IEventBus bus) {
        bus.addListener((AddPackFindersEvent event) -> event.addPackFinders(ResourceLocation.fromNamespaceAndPath("absmod", "resourcepacks/rose_cookery"),
                PackType.CLIENT_RESOURCES, Component.translatable("pack.absmod.rose_cookery"), PackSource.BUILT_IN, true, Pack.Position.TOP));
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey().location().equals(com.abyssredemption.absmod.item.ModCreativeTab.ID))
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(CookeryFinish.UTENSILS).ifPresent(items ->
                        items.forEach(item -> event.accept(CookeryFinish.apply(new net.minecraft.world.item.ItemStack(item)))));
        });
        NeoForge.EVENT_BUS.addListener(CookeryKitchen::drops);
    }
    private static void drops(BlockDropsEvent event) {
        var state = event.getState();
        if (!CookeryFinish.isRose(state)) return;
        for (var entity : event.getDrops()) {
            var stack = entity.getItem();
            if (stack.is(state.getBlock().asItem()) || (state.getBlock().asItem().getDefaultInstance().is(CookeryFinish.CUPS) && stack.is(CookeryFinish.CUPS))) CookeryFinish.apply(stack);
        }
    }
}
