package com.abyssredemption.absmod.block;

import com.abyssredemption.absmod.item.MilkshakeItem;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MilkshakeTrayBlock extends BaseEntityBlock {
    private static final MapCodec<MilkshakeTrayBlock> CODEC = simpleCodec(MilkshakeTrayBlock::new);
    private static final VoxelShape BASE = box(1, 0, 1, 15, 2.5, 15);
    private static final VoxelShape[] CUP_BOUNDS = new VoxelShape[MilkshakeTrayBlockEntity.CUP_COUNT];

    static {
        for (int slot = 0; slot < CUP_BOUNDS.length; slot++) {
            var x = cupX(slot) * 16;
            var z = cupZ(slot) * 16;
            CUP_BOUNDS[slot] = box(x - 2.6, 1.5, z - 2.6, x + 2.6, 4.75, z + 2.6);
        }
    }

    public MilkshakeTrayBlock(Properties properties) {
        super(properties);
    }

    public static double cupX(int slot) { return slot % 2 == 0 ? 0.29 : 0.71; }
    public static double cupZ(int slot) { return slot < 2 ? 0.29 : 0.71; }

    @Override protected MapCodec<MilkshakeTrayBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkshakeTrayBlockEntity(pos, state);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shape = BASE;
        if (level.getBlockEntity(pos) instanceof MilkshakeTrayBlockEntity tray) {
            for (int slot = 0; slot < CUP_BOUNDS.length; slot++) {
                if (!tray.cup(slot).isEmpty()) {
                    shape = Shapes.or(shape, CUP_BOUNDS[slot]);
                }
            }
        }
        return shape;
    }

    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                                     Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(held.getItem() instanceof MilkshakeItem)) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (player instanceof ServerPlayer owner && !owner.isSpectator()
                && level.getBlockEntity(pos) instanceof MilkshakeTrayBlockEntity tray) {
            if (tray.placeCup(slot(hit), held, owner.hasInfiniteMaterials())) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.65F, 1.1F);
            } else {
                owner.sendSystemMessage(Component.translatable("tray.absmod.occupied"), true);
            }
        }
        return net.minecraft.world.ItemInteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                          BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer guest && !guest.isSpectator()
                && level.getBlockEntity(pos) instanceof MilkshakeTrayBlockEntity tray) {
            var drink = tray.removeCup(slot(hit));
            if (drink.isEmpty()) {
                guest.sendSystemMessage(Component.translatable("tray.absmod.empty"), true);
            } else {
                guest.getInventory().placeItemBackInInventory(drink, guest.connection != null);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.65F, 1.1F);
                BladeAdvancements.award(guest, "milkshake_tray");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof MilkshakeTrayBlockEntity tray) {
            tray.dropCups();
        }
        super.onRemove(state, level, pos, next, moving);
    }

    private static int slot(BlockHitResult hit) {
        var local = hit.getLocation().subtract(hit.getBlockPos().getX(), 0, hit.getBlockPos().getZ());
        return (local.x >= 0.5 ? 1 : 0) + (local.z >= 0.5 ? 2 : 0);
    }
}
