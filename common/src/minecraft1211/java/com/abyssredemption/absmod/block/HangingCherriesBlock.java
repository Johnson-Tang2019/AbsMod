package com.abyssredemption.absmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import com.abyssredemption.absmod.spirit.BladeAdvancements;

public final class HangingCherriesBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final ResourceLocation FRUIT = ResourceLocation.fromNamespaceAndPath("absmod", "dew_cherries");
    private static final VoxelShape CLUSTER = box(3, 4, 3, 13, 16, 13);

    public HangingCherriesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return CLUSTER; }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return context.getLevel().isEmptyBlock(context.getClickedPos()) ? defaultBlockState() : null;
    }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(Blocks.CHERRY_LEAVES);
    }
    @Override protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos other) {
        return side == Direction.UP && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override protected boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < 3; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9 && random.nextInt(3) == 0) advance(level, pos, state);
    }
    private void advance(ServerLevel level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) level.setBlockAndUpdate(pos, state.setValue(AGE, age + 1));
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return state.getValue(AGE) < 3; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { advance(level, pos, state); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 3 || player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server && server.setBlockAndUpdate(pos, state.setValue(AGE, 0))) {
            popResource(server, pos, new ItemStack(BuiltInRegistries.ITEM.get(FRUIT), 2 + server.random.nextInt(2)));
            server.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1, 1.12F);
            server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (player instanceof ServerPlayer owner) BladeAdvancements.award(owner, "harvest_dew_cherries");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
