package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.combat.AttackInputTracker;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;

/** Client intent collection; only server confirmations drive actual blade swings. */
public final class BladeCombatClient {
    public static BladeCombatClient instance;
    private boolean pendingClick;

    public BladeCombatClient() { instance = this; }

    public static boolean interceptPress(Minecraft minecraft) {
        if (instance == null || !instance.enabled || minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof MeowBladeItem)
                || minecraft.player.isSpectator() || minecraft.hitResult == null
                || minecraft.hitResult.getType() == HitResult.Type.BLOCK) { return false; }
        instance.pendingClick = true;
        return true;
    }

    public static boolean isAvailable() { return instance != null && instance.enabled; }

    private AttackInputTracker tracker = new AttackInputTracker();
    private Object connection;
    private Object level;
    private net.minecraft.resources.Identifier dimension;
    private boolean enabled;
    private long tick;

    public void setEnabled(Minecraft minecraft, boolean value) {
        if (connection != minecraft.getConnection()) { tracker = new AttackInputTracker(); }
        connection = minecraft.getConnection();
        enabled = value;
        tracker.requireRelease();
    }

    public void tick(Minecraft minecraft, Consumer<AttackIntentPayload> send) {
        if (connection != minecraft.getConnection()) {
            connection = minecraft.getConnection();
            enabled = false;
            tracker = new AttackInputTracker();
        }
        tick++;
        if (connection == null) { return; }
        if (level != minecraft.level) {
            level = minecraft.level;
            tracker.requireRelease();
        }
        var player = minecraft.player;
        if (minecraft.level != null) { dimension = minecraft.level.dimension().identifier(); }
        boolean eligible = enabled && player != null && minecraft.gameMode != null && minecraft.level != null
                && !minecraft.isPaused() && minecraft.gui.screen() == null && minecraft.isWindowActive()
                && player.isAlive() && !player.isSpectator() && !player.isSleeping() && !player.isHandsBusy()
                && !player.isUsingItem() && !minecraft.options.keyUse.isDown() && !minecraft.gameMode.isDestroying()
                && player.getMainHandItem().getItem() instanceof MeowBladeItem
                && (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK);
        var intent = tracker.tick(tick, minecraft.options.keyAttack.isDown() || pendingClick, eligible,
                player == null ? null : player.getMainHandItem(), player == null ? -1 : player.getInventory().getSelectedSlot());
        pendingClick = false;
        if (intent != null && dimension != null) {
            send.accept(new AttackIntentPayload(intent.epoch(), intent.sequence(), intent.held(), intent.slot(), dimension));
        }
    }
}
