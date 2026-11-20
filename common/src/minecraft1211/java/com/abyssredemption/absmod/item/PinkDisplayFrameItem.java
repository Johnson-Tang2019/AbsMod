package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.entity.PinkDisplayFrame;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

public final class PinkDisplayFrameItem extends Item {
    private final Supplier<EntityType<PinkDisplayFrame>> frameType;

    public PinkDisplayFrameItem(Properties properties, Supplier<EntityType<PinkDisplayFrame>> frameType) {
        super(properties);
        this.frameType = frameType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        var level = context.getLevel();
        var facing = context.getClickedFace();
        var pos = context.getClickedPos().relative(facing);
        if (player != null && !player.mayUseItemAt(pos, facing, stack)) return InteractionResult.FAIL;
        var frame = new PinkDisplayFrame(frameType.get(), level, pos, facing);
        var data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!data.isEmpty()) EntityType.updateCustomEntityTag(level, player, frame, data);
        if (!frame.survives()) return InteractionResult.CONSUME;
        if (!level.isClientSide) {
            if (!level.addFreshEntity(frame)) return InteractionResult.FAIL;
            frame.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, frame.position());
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
