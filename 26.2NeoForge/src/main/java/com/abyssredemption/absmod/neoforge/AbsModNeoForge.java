package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(AbsMod.MOD_ID)
public final class AbsModNeoForge {
    public AbsModNeoForge(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(AbsModNeoForge::addCreativeItems);
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            ModItems.MEOW_BLADES.forEach(item -> event.accept(item));
        }
    }
}
