package com.abyssredemption.absmod.gametest;

import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.abyssredemption.absmod.item.MilkshakeVariant;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MilkshakeTrayGameTests {
    @GameTest
    public void traySharesExactlyOnceAndReturnsRemainingDrinksWhenBroken(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(1, 2, 1);
        var pos = helper.absolutePos(relative);
        var state = BuiltInRegistries.BLOCK.getValue(MilkshakeTrayBlockEntity.ID).defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        var tray = (MilkshakeTrayBlockEntity) level.getBlockEntity(pos);
        var host = helper.makeMockServerPlayer(GameType.SURVIVAL);
        var guest = helper.makeMockServerPlayer(GameType.SURVIVAL);
        if (host.connection != null) host.setGameMode(GameType.SURVIVAL);
        if (guest.connection != null) guest.setGameMode(GameType.SURVIVAL);
        var plain = drink(MilkshakeVariant.PLAIN);
        var gold = drink(MilkshakeVariant.GOLD);
        var milkshakes = new ItemStack(plain, 2);
        var name = Component.literal("Shared by the host");
        milkshakes.set(DataComponents.CUSTOM_NAME, name);
        host.setItemInHand(InteractionHand.MAIN_HAND, milkshakes);
        var first = hit(pos, 0.28, 0.28);
        var second = hit(pos, 0.71, 0.28);
        state.useItemOn(milkshakes, level, host, InteractionHand.MAIN_HAND, first);
        state.useItemOn(milkshakes, level, host, InteractionHand.MAIN_HAND, first);
        helper.assertTrue(tray.cup(0).getCount() == 1 && milkshakes.getCount() == 1 && tray.cup(1).isEmpty(),
                "Placing twice on one coaster consumed extra bottles or touched a different coaster");
        state.useItemOn(new ItemStack(gold), level, host, InteractionHand.MAIN_HAND, second);
        var restored = new MilkshakeTrayBlockEntity(pos, state);
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(),
                tray.getUpdateTag(level.registryAccess())));
        helper.assertTrue(restored.cup(0).is(plain) && name.equals(restored.cup(0).get(DataComponents.CUSTOM_NAME))
                && restored.cup(1).is(gold), "Tray did not save distinct slots and bottle components");

        state.useWithoutItem(level, guest, first);
        var received = guest.getMainHandItem();
        helper.assertTrue(received.is(plain) && received.getCount() == 1 && name.equals(received.get(DataComponents.CUSTOM_NAME))
                && tray.cup(0).isEmpty() && tray.cup(1).is(gold), "Sharing should transfer the exact item to the guest only once");
        host.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        state.useWithoutItem(level, host, first);
        helper.assertTrue(host.getMainHandItem().isEmpty(), "An empty coaster returned a second copy");
        level.destroyBlock(pos, true);
        helper.assertItemEntityCountIs(gold, relative, 2, 1);
        helper.assertItemEntityCountIs(plain, relative, 2, 0);
        helper.assertItemEntityCountIs(BuiltInRegistries.ITEM.getValue(MilkshakeTrayBlockEntity.ID), relative, 2, 1);
        helper.succeed();
    }

    @GameTest
    public void trayRecipeMatchesTheGuide(GameTestHelper helper) {
        var plank = new ItemStack(Items.CHERRY_PLANKS);
        var wool = new ItemStack(Items.PINK_WOOL);
        var input = CraftingInput.of(3, 3, List.of(plank, ItemStack.EMPTY, plank,
                wool, new ItemStack(Items.AMETHYST_SHARD), wool, plank, plank, plank));
        var level = helper.getLevel();
        var result = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .orElseThrow().value().assemble(input);
        helper.assertTrue(result.is(BuiltInRegistries.ITEM.getValue(MilkshakeTrayBlockEntity.ID)) && result.getCount() == 1,
                "The documented crafting arrangement did not make one tray");
        helper.succeed();
    }

    private static Item drink(MilkshakeVariant variant) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", variant.itemId()));
    }

    private static BlockHitResult hit(BlockPos pos, double x, double z) {
        return new BlockHitResult(Vec3.atLowerCornerOf(pos).add(x, 0.15, z), Direction.UP, pos, false);
    }
}
