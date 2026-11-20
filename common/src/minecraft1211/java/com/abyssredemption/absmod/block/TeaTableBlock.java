package com.abyssredemption.absmod.block;

import com.abyssredemption.absmod.spirit.BladeAdvancements;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class TeaTableBlock extends CherryFurnitureBlock {
    static final VoxelShape TABLE = Shapes.or(box(0,7,0,16,9,16), box(1,0,1,4,7,4), box(12,0,1,15,7,4),
            box(1,0,12,4,7,15), box(12,0,12,15,7,15));
    public TeaTableBlock(Properties properties) { super(properties, TABLE); }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP || !stack.is(BuiltInRegistries.ITEM.get(MilkshakeTrayBlockEntity.ID)))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!player.mayBuild() || player.isSpectator()) return ItemInteractionResult.FAIL;
        if (player instanceof ServerPlayer server && level.setBlockAndUpdate(pos,
                BuiltInRegistries.BLOCK.get(ResourceLocation.parse("absmod:laid_tea_table")).defaultBlockState())) {
            stack.consume(1, player);
            BladeAdvancements.award(server, "set_tea_table");
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static final class Laid extends MilkshakeTrayBlock {
        public Laid(Properties properties) { super(properties); }
        @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:cherry_tea_table")));
        }
    }
}
