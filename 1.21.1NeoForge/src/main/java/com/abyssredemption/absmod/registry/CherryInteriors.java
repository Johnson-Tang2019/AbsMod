package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.block.CherryFloorLampBlock;
import com.abyssredemption.absmod.block.CherryFurnitureBlock;
import com.abyssredemption.absmod.entity.PinkDisplayFrame;
import com.abyssredemption.absmod.item.PinkDisplayFrameItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CherryInteriors {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "absmod");
    public static final Supplier<EntityType<PinkDisplayFrame>> FRAME = ENTITIES.register("pink_display_frame", () ->
            EntityType.Builder.<PinkDisplayFrame>of(PinkDisplayFrame::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).eyeHeight(0).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("absmod:pink_display_frame"));

    private CherryInteriors() {}

    private static BlockBehaviour.Properties cherry() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).noOcclusion();
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Supplier<B> factory) {
        var block = ModBlocks.BLOCKS.register(name, factory);
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ModItems.ITEMS.registerItem("pink_display_frame", p -> new PinkDisplayFrameItem(p, FRAME));
        block("pink_glowing_glass", () -> new StainedGlassBlock(DyeColor.PINK,
                BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_STAINED_GLASS).lightLevel(s -> 12)));
        block("pink_glowing_glass_pane", () -> new StainedGlassPaneBlock(DyeColor.PINK,
                BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_STAINED_GLASS_PANE).lightLevel(s -> 12)));
        block("cherry_floor_lamp", () -> new CherryFloorLampBlock(cherry().lightLevel(s ->
                s.getValue(BlockStateProperties.LIT) && s.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? 15 : 0)));
        block("cherry_table_lamp", () -> new CherryFurnitureBlock(cherry().lightLevel(s -> 12), Shapes.or(
                Block.box(3, 0, 3, 13, 2, 13), Block.box(6, 2, 6, 10, 6, 10), Block.box(2, 6, 2, 14, 15, 14))));
        block("cherry_tea_table", () -> new com.abyssredemption.absmod.block.TeaTableBlock(cherry()));
        block("cherry_flower_stand", () -> new CherryFurnitureBlock(cherry(), Shapes.or(
                Block.box(1, 0, 1, 15, 2, 15), Block.box(5, 2, 5, 11, 14, 11), Block.box(0, 14, 0, 16, 16, 16))));
        block("heart_mirror", () -> new com.abyssredemption.absmod.block.HeartMirrorBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).noOcclusion().lightLevel(s -> 6)));
        block("cherry_cat_cushion", () -> new com.abyssredemption.absmod.cat.CatNestBlock(
                BlockBehaviour.Properties.of().noOcclusion().strength(0.4F).sound(SoundType.WOOL)
                        .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
        block("cherry_scratching_post", () -> new com.abyssredemption.absmod.cat.ScratchingPostBlock(cherry()));
        var cabinet = block("cherry_display_cabinet", () -> new com.abyssredemption.absmod.block.CherryCabinetBlock(cherry()));
        bus.addListener((BlockEntityTypeAddBlocksEvent event) -> event.modify(BlockEntityType.BARREL, cabinet.get()));
    }
}
