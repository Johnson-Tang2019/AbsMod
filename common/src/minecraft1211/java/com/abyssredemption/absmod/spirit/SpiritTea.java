package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.abyssredemption.absmod.block.TeaTableBlock;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

/** An invitation reserves no inventory. Arrival and serving revalidate the exact table and cup. */
public final class SpiritTea {
    private static final String SELECTION = "absmod_tea_spirit";
    private MilkshakeTrayBlockEntity tray;
    private ItemStack drink;
    private BlockPos seat;
    private int slot, elapsed, sip;

    private static void tell(ServerPlayer player, String key) {
        com.mojang.logging.LogUtils.getLogger().debug("Spirit tea: {}", key);
        if (player.connection != null) player.sendSystemMessage(Component.translatable("tea.absmod." + key), true);
    }
    public void select(ServerPlayer owner, BladeSpiritEntity spirit, ItemStack bell) {
        if (!owner.getUUID().equals(spirit.ownerId()) || !spirit.active()) { tell(owner, "owner"); return; }
        if (tray != null) { finish(spirit); tell(owner, "cancelled"); return; }
        CustomData.update(DataComponents.CUSTOM_DATA, bell, tag -> tag.putUUID(SELECTION, spirit.getUUID()));
        tell(owner, "selected");
    }
    public static void invite(ServerPlayer owner, ItemStack bell, MilkshakeTrayBlockEntity tray, int slot) {
        var tag = bell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        var level = owner.serverLevel();
        if (!tag.hasUUID(SELECTION) || !(level.getEntity(tag.getUUID(SELECTION)) instanceof BladeSpiritEntity spirit)
                || !owner.getUUID().equals(spirit.ownerId()) || !spirit.active() || spirit.isGrooming()
                || spirit.distanceToSqr(Vec3.atCenterOf(tray.getBlockPos())) > 256) { tell(owner, "select_first"); return; }
        var tea = spirit.tea;
        if (tea.tray != null) { tell(owner, "busy"); return; }
        if (tray.cup(slot).isEmpty()) { tell(owner, "empty"); return; }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos seat = tray.getBlockPos().relative(side);
            if (!level.hasChunkAt(seat) || !level.getBlockState(seat.below()).isFaceSturdy(level,seat.below(),Direction.UP)
                    || !level.noCollision(spirit,spirit.getBoundingBox().move(Vec3.atBottomCenterOf(seat).subtract(spirit.position())))) continue;
            var path = spirit.getNavigation().createPath(seat, 0);
            if (spirit.distanceToSqr(Vec3.atBottomCenterOf(seat)) > 0.9
                    && (path == null || !path.canReach() || !spirit.getNavigation().moveTo(path, 1.1))) continue;
            tea.tray = tray;
            tea.slot = slot;
            tea.drink = tray.cup(slot);
            tea.seat = seat;
            tell(owner, "invited");
            return;
        }
        tell(owner, "no_path");
    }
    public boolean tick(BladeSpiritEntity spirit, ServerLevel level) {
        if (tray == null) return false;
        var owner = level.getPlayerByUUID(spirit.ownerId());
        var pos = tray.getBlockPos();
        if (!(owner instanceof ServerPlayer player) || !owner.isAlive() || owner.isSpectator()
                || owner.distanceToSqr(Vec3.atCenterOf(pos)) > 144 || ++elapsed > 180 || !spirit.active()
                || !level.hasChunkAt(pos) || level.getBlockEntity(pos) != tray || tray.isRemoved()
                || !(tray.getBlockState().getBlock() instanceof TeaTableBlock.Laid) || tray.cup(slot) != drink) {
            if (owner instanceof ServerPlayer present) tell(present, "cancelled");
            finish(spirit);
            return false;
        }
        if (spirit.distanceToSqr(Vec3.atBottomCenterOf(seat)) > 0.9) {
            if (sip > 0 || (spirit.getNavigation().isDone() && elapsed > 10)) {
                finish(spirit);
                tell(player, "cancelled");
            }
            return true;
        }
        spirit.getNavigation().stop();
        float targetYaw = (float)Math.toDegrees(Math.atan2(player.getZ()-spirit.getZ(), player.getX()-spirit.getX())) - 90;
        float turn = net.minecraft.util.Mth.approachDegrees(spirit.getYRot(), targetYaw, 18);
        spirit.setYRot(turn);
        spirit.setYBodyRot(turn);
        spirit.getLookControl().setLookAt(player, 40, 40);
        if (++sip == 1) spirit.showTeaCup(drink);
        if (sip >= 32) {
            if (!tray.removeCup(slot).isEmpty()) {
                player.getInventory().placeItemBackInInventory(new ItemStack(Items.BUCKET));
                level.playSound(null,pos,SoundEvents.GENERIC_DRINK,SoundSource.NEUTRAL,0.8F,1.1F);
                BladeAffection.tea(player, spirit);
                BladeAdvancements.award(player,"spirit_tea");
                tell(player,"served");
            }
            finish(spirit);
        }
        return true;
    }
    private void finish(BladeSpiritEntity spirit) {
        tray = null;
        drink = null;
        seat = null;
        sip = elapsed = 0;
        spirit.showTeaCup(ItemStack.EMPTY);
        spirit.getNavigation().stop();
    }
}
