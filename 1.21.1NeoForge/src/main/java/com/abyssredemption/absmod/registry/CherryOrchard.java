package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.block.*;
import com.abyssredemption.absmod.item.BlossomDrinkItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.*;

public final class CherryOrchard {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, AbsMod.MOD_ID);
    public static final DeferredBlock<HangingCherriesBlock> CLUSTER = ModBlocks.BLOCKS.registerBlock("hanging_cherries",
            HangingCherriesBlock::new, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.CROP).randomTicks()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    private static final DeferredBlock<BlossomSpringBlock> SPRING = ModBlocks.BLOCKS.registerBlock("blossom_spring_stone",
            BlossomSpringBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).lightLevel(s -> 5));

    private CherryOrchard() {}
    public static void register(IEventBus bus) {
        ModItems.ITEMS.registerItem("fragrant_cherry_blossom", p -> new BlockItem(CLUSTER.get(), p) {
            @Override public String getDescriptionId() { return getOrCreateDescriptionId(); }
        });
        ModItems.ITEMS.registerSimpleBlockItem(SPRING);
        food("dew_cherries", 2, .3F);
        food("blossom_dumplings", 5, .6F);
        food("cherry_tart", 7, .6F);
        drink("blossom_tea", 2, .4F);
        drink("dew_cherry_cordial", 4, .5F);
        FEATURES.register("blossom_spring", () -> new BlossomSpringFeature(() -> SPRING.get().defaultBlockState()));
        FEATURES.register(bus);
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity() instanceof ServerPlayer player) BlossomSpringBlock.bathe(player);
        });
    }

    private static void food(String id, int hunger, float saturation) {
        ModItems.ITEMS.registerSimpleItem(id, new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(hunger).saturationModifier(saturation).build()));
    }
    private static void drink(String id, int hunger, float saturation) {
        ModItems.ITEMS.registerItem(id, p -> new BlossomDrinkItem(p.stacksTo(16).food(new FoodProperties.Builder()
                .nutrition(hunger).saturationModifier(saturation).alwaysEdible().usingConvertsTo(Items.GLASS_BOTTLE).build())));
    }
}
