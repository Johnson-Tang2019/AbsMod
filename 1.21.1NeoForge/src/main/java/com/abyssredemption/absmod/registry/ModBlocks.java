package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.cat.CatNestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AbsMod.MOD_ID);
    public static final DeferredBlock<CatNestBlock> CAT_NEST = BLOCKS.registerBlock("cat_nest", CatNestBlock::new,
            BlockBehaviour.Properties.of().noCollission().strength(0.4F).sound(SoundType.WOOL)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));

    public static final DeferredBlock<com.abyssredemption.absmod.block.MilkshakeTrayBlock> MILKSHAKE_TRAY =
            BLOCKS.registerBlock("milkshake_tray", com.abyssredemption.absmod.block.MilkshakeTrayBlock::new,
                    BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.CHERRY_WOOD).noOcclusion()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));

    private ModBlocks() {}
}
