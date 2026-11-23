package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.AbsMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AbsMod.MOD_ID)
public final class CherryLantern {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AbsMod.MOD_ID);
    public static final DeferredItem<Item> LANTERN =
            ITEMS.registerSimpleItem("cherry_blossom_lantern", new Item.Properties().stacksTo(1));

    public CherryLantern(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) event.accept(LANTERN);
        });
    }
}
