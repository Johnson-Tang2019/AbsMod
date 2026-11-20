package com.abyssredemption.absmod.item;

import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class CookeryFinish {
    public static final BooleanProperty ROSE = BooleanProperty.create("absmod_rose");
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> UTENSILS = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("absmod", "cookery_utensils"));
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> CUPS = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("absmod", "cookery_cups"));
    private static final Set<String> BLOCKS = Set.of("StoveBlock","PotBlock","StockpotBlock","ChoppingBoardBlock","BambooTrayBlock",
            "MillstoneBlock","SteamerBlock","TeapotBlock","TrashCanBlock","KitchenwareRacksBlock","FruitBasketBlock","EnamelBasinBlock",
            "OilPotBlock","ShawarmaSpitBlock","RecipeBlock","EmptyCupBlock","TeacupBlock","ClayPotMilkTeaBlock");
    private CookeryFinish() {}
    public static boolean supports(Block block) {
        for (Class<?> c=block.getClass(); c.getName().startsWith("com.github.ysbbbbbb.kaleidoscopecookery.block."); c=c.getSuperclass())
            if (BLOCKS.contains(c.getSimpleName())) return true;
        return false;
    }
    public static boolean isRose(BlockState state) { return state.hasProperty(ROSE) && state.getValue(ROSE); }
    public static boolean isRose(ItemStack stack) { return Boolean.TRUE.equals(stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(ROSE)); }
    public static ItemStack apply(ItemStack stack) {
        stack.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, state -> state.with(ROSE, true));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.absmod.rose_cookery", stack.getItem().getDescription()));
        return stack;
    }
}
