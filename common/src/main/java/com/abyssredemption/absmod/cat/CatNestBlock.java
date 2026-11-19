package com.abyssredemption.absmod.cat;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CatNestBlock extends Block {
    private static final MapCodec<CatNestBlock> CODEC = simpleCodec(CatNestBlock::new);
    private static final VoxelShape OUTLINE = box(1, 0, 1, 15, 7, 15);

    public CatNestBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<CatNestBlock> codec() { return CODEC; }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player && level instanceof ServerLevel server) {
            CatLifeStore.get(server.getServer()).place(GlobalPos.of(server.dimension(), pos), player.getUUID());
        }
    }

    @Override protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        CatLifeStore.get(level.getServer()).remove(GlobalPos.of(level.dimension(), pos));
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer owner) {
            CatLife.useNest(owner, pos, player.isShiftKeyDown());
        }
        return InteractionResult.SUCCESS;
    }
}
