package com.abyssredemption.absmod.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod("absmod")
public final class RoseStandaloneTest {
    public RoseStandaloneTest() { NeoForge.EVENT_BUS.addListener(this::started); }
    private void started(ServerStartedEvent event) {
        var server = event.getServer();
        if (ModList.get().isLoaded("curios") || ModList.get().isLoaded("farmersdelight")) throw new IllegalStateException("Expected standalone environment");
        for (String path : java.util.List.of("cherry_blossom_lantern", "blossom_bow_stage_9", "blossom_staff_stage_9",
                "dark_blossom_bow_stage_8", "dark_blossom_staff_stage_8")) {
            var id = ResourceLocation.fromNamespaceAndPath("absmod", path);
            if (!BuiltInRegistries.ITEM.containsKey(id) || server.getRecipeManager().byKey(id).isEmpty())
                throw new IllegalStateException("Missing standalone blossom item or recipe: " + path);
        }
        com.mojang.logging.LogUtils.getLogger().info("BLOSSOM_STANDALONE_PASS lantern and professions available");
        var kitchen = ResourceLocation.fromNamespaceAndPath("absmod", "rose_cooking_pot");
        if (BuiltInRegistries.BLOCK.containsKey(kitchen) || server.getRecipeManager().byKey(kitchen).isPresent()) throw new IllegalStateException("Unloaded kitchen");
        if (ModList.get().isLoaded("kaleidoscope_cookery") ||
                server.getRecipeManager().byKey(ResourceLocation.parse("absmod:cookery_finish/stockpot")).isPresent())
            throw new IllegalStateException("Unexpected optional Cookery recipe");
        if (server.getRecipeManager().byKey(ResourceLocation.parse("absmod:rose_gold_pickaxe")).isEmpty())
            throw new IllegalStateException("Missing standalone rose gold upgrade");
        com.mojang.logging.LogUtils.getLogger().info("ROSE_ALLOY_STANDALONE_PASS rose gold present with optional kitchens absent");
        for (String id : java.util.List.of("pink_display_frame", "pink_glowing_glass", "pink_glowing_glass_pane",
                "cherry_floor_lamp", "cherry_table_lamp", "cherry_tea_table", "cherry_flower_stand", "cherry_display_cabinet")) {
            if (server.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("absmod", id)).isEmpty())
                throw new IllegalStateException("Missing standalone furniture recipe " + id);
        }
        com.mojang.logging.LogUtils.getLogger().info("CHERRY_INTERIORS_STANDALONE_PASS all eight recipes without optional mods");
        var player = FakePlayerFactory.getMinecraft(server.overworld());
        var charm = ResourceLocation.fromNamespaceAndPath("absmod", "paw_signet");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(BuiltInRegistries.ITEM.get(charm)));
        RoseCharms.tick(new PlayerTickEvent.Post(player));
        if (!player.hasEffect(MobEffects.LUCK)) throw new IllegalStateException("Missing offhand effect");
        var compass=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","whisker_compass"));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(compass));
        player.setShiftKeyDown(true);
        RoseCharms.tick(new PlayerTickEvent.Post(player));
        if (!player.getCooldowns().isOnCooldown(compass)) throw new IllegalStateException("Standalone companion search");
        com.mojang.logging.LogUtils.getLogger().info("BLUSH_STANDALONE_PASS offhand compass activated with Curios absent");
        com.mojang.logging.LogUtils.getLogger().info("ROSE_STANDALONE_PASS optional integrations absent, offhand charm active");
        server.halt(false);
    }
}
