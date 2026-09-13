package com.abyssredemption.absmod.gametest;

import com.abyssredemption.absmod.spirit.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public final class SpiritGameTests {
    @GameTest
    public void manifestationPreservesSwordAndRejectsRepeatedRecall(GameTestHelper helper) {
        var player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        var sword = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod",
                com.abyssredemption.absmod.item.MeowBladeStage.values()[0].itemId())));
        sword.setDamageValue(17);
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Retained sword"));
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), sword);
        var store = BladeSoulStore.forInitialization(helper.getLevel().getServer());
        var original = BladeSoulService.initialize(store, sword, player.getUUID());
        var expected = sword.copy();
        var spirit = SpiritTransformationService.manifest(player);
        helper.assertTrue(player.getMainHandItem().isEmpty() && spirit.active(), "Manifestation did not transfer the carrier");
        var ops = helper.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        var encoded = BladeSoulStore.CODEC.encodeStart(ops, store).getOrThrow();
        var decoded = BladeSoulStore.CODEC.parse(ops, encoded).getOrThrow();
        helper.assertTrue(ItemStack.matches(decoded.find(original.soulId()).orElseThrow().carrier().orElseThrow().sword(), expected),
                "Schema 2 lost the complete sword snapshot");
        player.getInventory().setItem(8, new ItemStack(Items.STONE));
        boolean refused = false;
        try { SpiritTransformationService.recall(player, spirit, 8); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && spirit.active() && player.getInventory().getItem(8).is(Items.STONE), "Occupied slot changed state");
        var stranger = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        refused = false;
        try { SpiritTransformationService.recall(stranger, spirit, 7); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && spirit.active() && stranger.getInventory().getItem(7).isEmpty(), "Another player recalled the spirit");
        var retained = store.find(original.soulId()).orElseThrow().carrier().orElseThrow();
        retained.sword().setCount(0);
        helper.assertTrue(!retained.sword().isEmpty(), "Snapshot leaked a mutable stack");
        SpiritTransformationService.recall(player, spirit, 7);
        helper.assertTrue(ItemStack.matches(player.getInventory().getItem(7), expected) && spirit.isRemoved(), "Recall lost components");
        refused = false;
        try { SpiritTransformationService.recall(player, spirit, 6); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && player.getInventory().getItem(6).isEmpty(), "Repeated recall duplicated a sword");
        helper.assertTrue(store.find(original.soulId()).orElseThrow().form().equals("sword"), "Soul did not return to sword form");
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), player.getInventory().removeItemNoUpdate(7));
        var current = store.find(original.soulId()).orElseThrow();
        var obstacle = player.blockPosition().offset(1, 0, 0);
        helper.getLevel().setBlockAndUpdate(obstacle, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        refused = false;
        try { SpiritTransformationService.manifest(player); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && ItemStack.matches(player.getMainHandItem(), expected)
                && store.find(original.soulId()).orElseThrow().equals(current), "Blocked manifestation changed the sword or record");
        helper.succeed();
    }
}
