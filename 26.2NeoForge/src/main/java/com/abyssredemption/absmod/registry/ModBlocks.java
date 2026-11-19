package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.block.MilkshakeTrayBlock;
import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.abyssredemption.absmod.cat.CatNestBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AbsMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AbsMod.MOD_ID);
    public static final DeferredBlock<CatNestBlock> CAT_NEST = BLOCKS.registerBlock("cat_nest", CatNestBlock::new,
            properties -> properties.noCollision().strength(0.4F).sound(SoundType.WOOL)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredBlock<MilkshakeTrayBlock> MILKSHAKE_TRAY = BLOCKS.registerBlock("milkshake_tray", MilkshakeTrayBlock::new,
            properties -> properties.noOcclusion().dynamicShape().strength(0.5F).sound(SoundType.CHERRY_WOOD)
                    .pushReaction(PushReaction.BLOCK));

    static {
        BLOCK_ENTITIES.register("milkshake_tray", () -> new BlockEntityType<>(MilkshakeTrayBlockEntity::new, MILKSHAKE_TRAY.get()));
    }

    private ModBlocks() {}
}
