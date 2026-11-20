package com.abyssredemption.absmod.compat;

import com.abyssredemption.absmod.registry.ModItems;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import vectorwing.farmersdelight.common.block.*;
import vectorwing.farmersdelight.common.item.*;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

/** Lazily loaded only when Farmer's Delight is present. Its block entities keep their inventories, menus and recipes. */
public final class RoseKitchen {
    private RoseKitchen() {}

    private static DeferredBlock<Block> station(String id, Supplier<Block> original, Function<BlockBehaviour.Properties, Block> factory) {
        return com.abyssredemption.absmod.registry.ModBlocks.BLOCKS.register("rose_" + id,
                () -> factory.apply(BlockBehaviour.Properties.ofFullCopy(original.get())));
    }

    public static void register(IEventBus bus) {
        var stove = station("stove", ModBlocks.STOVE, StoveBlock::new);
        ModItems.ITEMS.registerSimpleBlockItem(stove);
        var cuttingBoard = station("cutting_board", ModBlocks.CUTTING_BOARD, CuttingBoardBlock::new);
        ModItems.ITEMS.registerSimpleBlockItem(cuttingBoard);
        var cookingPot = station("cooking_pot", ModBlocks.COOKING_POT, properties -> new CookingPotBlock(properties) {
            @Override public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos,
                                                         net.minecraft.world.level.block.state.BlockState state) {
                return super.getCloneItemStack(level, pos, state).transmuteCopy(this);
            }
        });
        ModItems.ITEMS.registerItem("rose_cooking_pot", p -> new CookingPotItem(cookingPot.get(), p.stacksTo(1)));
        var skillet = station("skillet", ModBlocks.SKILLET, SkilletBlock::new);
        ModItems.ITEMS.registerItem("rose_skillet", p -> new SkilletItem(skillet.get(), p.durability(SkilletItem.SKILLET_TIER.getUses()).attributes(SkilletItem.createAttributes(SkilletItem.SKILLET_TIER, 5, -2.6F))));
        var woodenBasket = station("wooden_basket", ModBlocks.WOODEN_BASKET, BasketBlock::new);
        ModItems.ITEMS.registerSimpleBlockItem(woodenBasket);
        var bambooBasket = station("bamboo_basket", ModBlocks.BAMBOO_BASKET, BasketBlock::new);
        ModItems.ITEMS.registerSimpleBlockItem(bambooBasket);
        var cabinet = station("cabinet", ModBlocks.OAK_CABINET, CabinetBlock::new);
        ModItems.ITEMS.registerSimpleBlockItem(cabinet);
        ModItems.ITEMS.registerItem("rose_knife", p -> new KnifeItem(Tiers.IRON,
                p.attributes(DiggerItem.createAttributes(Tiers.IRON, 0.5F, -2F))));
        bus.addListener((BlockEntityTypeAddBlocksEvent event) -> {
            event.modify(ModBlockEntityTypes.STOVE.get(), stove.get());
            event.modify(ModBlockEntityTypes.CUTTING_BOARD.get(), cuttingBoard.get());
            event.modify(ModBlockEntityTypes.COOKING_POT.get(), cookingPot.get());
            event.modify(ModBlockEntityTypes.SKILLET.get(), skillet.get());
            event.modify(ModBlockEntityTypes.BASKET.get(), woodenBasket.get());
            event.modify(ModBlockEntityTypes.BASKET.get(), bambooBasket.get());
            event.modify(ModBlockEntityTypes.CABINET.get(), cabinet.get());
        });
    }
}
