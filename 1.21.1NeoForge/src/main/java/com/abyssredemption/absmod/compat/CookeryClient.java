package com.abyssredemption.absmod.compat;

import com.abyssredemption.absmod.item.CookeryFinish;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "absmod", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CookeryClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.registerGeneric(ResourceLocation.fromNamespaceAndPath("absmod","rose_finish"),
                (stack, level, entity, seed) -> CookeryFinish.isRose(stack) ? 1 : 0));
    }
}
