package com.abyssredemption.absmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PinkDisplayFrame extends ItemFrame {
    public PinkDisplayFrame(EntityType<? extends PinkDisplayFrame> type, Level level) {
        super(type, level);
    }

    public PinkDisplayFrame(EntityType<? extends PinkDisplayFrame> type, Level level, BlockPos pos, Direction facing) {
        super(type, level, pos, facing);
    }

    @Override
    protected ItemStack getFrameItemStack() {
        return new ItemStack(BuiltInRegistries.ITEM.get(BuiltInRegistries.ENTITY_TYPE.getKey(getType())));
    }
}
