package com.abyssredemption.absmod.block;

import com.abyssredemption.absmod.item.MilkshakeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class MilkshakeTrayBlockEntity extends BlockEntity {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("absmod", "milkshake_tray");
    public static final int CUP_COUNT = 4;
    private final NonNullList<ItemStack> cups = NonNullList.withSize(CUP_COUNT, ItemStack.EMPTY);

    public MilkshakeTrayBlockEntity(BlockPos pos, BlockState state) {
        super(type(), pos, state);
    }

    @SuppressWarnings("unchecked")
    public static BlockEntityType<MilkshakeTrayBlockEntity> type() {
        return (BlockEntityType<MilkshakeTrayBlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ID);
    }

    /** The returned stack is a read-only view for hit detection and rendering. */
    public ItemStack cup(int slot) {
        return cups.get(slot);
    }

    boolean placeCup(int slot, ItemStack held, boolean creative) {
        if (!(level instanceof ServerLevel) || isRemoved() || held.isEmpty()
                || !(held.getItem() instanceof MilkshakeItem) || !cups.get(slot).isEmpty()) {
            return false;
        }
        cups.set(slot, creative ? held.copyWithCount(1) : held.split(1));
        contentsChanged();
        return true;
    }

    public ItemStack removeCup(int slot) {
        if (!(level instanceof ServerLevel) || isRemoved()) {
            return ItemStack.EMPTY;
        }
        var removed = cups.set(slot, ItemStack.EMPTY);
        if (!removed.isEmpty()) {
            contentsChanged();
        }
        return removed;
    }

    private void contentsChanged() {
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override protected void loadAdditional(CompoundTag input, HolderLookup.Provider lookup) {
        super.loadAdditional(input, lookup);
        cups.clear();
        ContainerHelper.loadAllItems(input, cups, lookup);
    }

    @Override protected void saveAdditional(CompoundTag output, HolderLookup.Provider lookup) {
        super.saveAdditional(output, lookup);
        ContainerHelper.saveAllItems(output, cups, lookup);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void dropCups() {
        if (level instanceof ServerLevel server) {
            Containers.dropContents(server, worldPosition, cups);
            cups.clear();
        }
    }
}
