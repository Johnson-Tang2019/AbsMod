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
        var kitchen = ResourceLocation.fromNamespaceAndPath("absmod", "rose_cooking_pot");
        if (BuiltInRegistries.BLOCK.containsKey(kitchen) || server.getRecipeManager().byKey(kitchen).isPresent()) throw new IllegalStateException("Unloaded kitchen");
        var player = FakePlayerFactory.getMinecraft(server.overworld());
        var charm = ResourceLocation.fromNamespaceAndPath("absmod", "paw_signet");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(BuiltInRegistries.ITEM.get(charm)));
        RoseCharms.tick(new PlayerTickEvent.Post(player));
        if (!player.hasEffect(MobEffects.LUCK)) throw new IllegalStateException("Missing offhand effect");
        com.mojang.logging.LogUtils.getLogger().info("ROSE_STANDALONE_PASS optional integrations absent, offhand charm active");
        server.halt(false);
    }
}
