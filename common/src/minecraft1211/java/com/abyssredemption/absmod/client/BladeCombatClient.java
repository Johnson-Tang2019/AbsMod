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
    private net.minecraft.world.item.ItemStack previousWeapon = net.minecraft.world.item.ItemStack.EMPTY;
    private int previousSlot = -1;

    private static boolean accepts(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof MeowBladeItem
                || stack.getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem;
    }

    public BladeCombatClient() { instance = this; }

    public static boolean interceptPress(Minecraft minecraft) {
        if (instance == null || !instance.enabled || minecraft.player == null
                || !accepts(minecraft.player.getMainHandItem())
                || minecraft.player.isSpectator() || minecraft.hitResult == null
                || minecraft.hitResult.getType() == HitResult.Type.BLOCK) { return false; }
        instance.pendingClick = true;
        return true;
    }

    public static boolean isAvailable() { return instance != null && instance.enabled; }

    private AttackInputTracker tracker = new AttackInputTracker();
    private Object connection;
    private Object level;
    private net.minecraft.resources.ResourceLocation dimension;
    private boolean enabled;
    private long tick;

    public void setEnabled(Minecraft minecraft, boolean value) {
        if (connection != minecraft.getConnection()) { tracker = new AttackInputTracker(); }
        connection = minecraft.getConnection();
        enabled = value;
        tracker.requireRelease();
    }

    private long heavySequence;
    private boolean heavyRequested;
    public void tick(Minecraft minecraft, Consumer<AttackIntentPayload> send,
            Consumer<com.abyssredemption.absmod.network.HeavyIntentPayload> sendHeavy) {
        if (connection != minecraft.getConnection()) {
            connection = minecraft.getConnection();
            enabled = false;
            tracker = new AttackInputTracker();
            heavySequence=0;heavyRequested=false;
        }
        tick++;
        if (connection == null) { return; }
        if (level != minecraft.level) {
            level = minecraft.level;
            tracker.requireRelease();
        }
        var player = minecraft.player;
        if (minecraft.level != null) { dimension = minecraft.level.dimension().location(); }
        boolean eligible = enabled && player != null && minecraft.gameMode != null && minecraft.level != null
                && !minecraft.isPaused() && minecraft.screen == null && minecraft.isWindowActive()
                && player.isAlive() && !player.isSpectator() && !player.isSleeping() && !player.isHandsBusy()
                && !player.isUsingItem() && !minecraft.options.keyUse.isDown() && !minecraft.gameMode.isDestroying()
                && accepts(player.getMainHandItem())
                && (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK);
        boolean heavyClick=false;
        while(BladeKeys.HEAVY.consumeClick())heavyClick=true;
        if(player!=null && dimension!=null) {
            if(!eligible && heavyRequested) {
                sendHeavy.accept(new com.abyssredemption.absmod.network.HeavyIntentPayload(++heavySequence,false,player.getInventory().selected,dimension));
                heavyRequested=false;
            }
            if(eligible && heavyClick && player.getMainHandItem().getItem() instanceof MeowBladeItem blade
                && blade.stage().route()==com.abyssredemption.absmod.item.BladeRoute.DARK) {
                sendHeavy.accept(new com.abyssredemption.absmod.network.HeavyIntentPayload(++heavySequence,true,player.getInventory().selected,dimension));
                heavyRequested=true;
            }
        }
        if (player != null) {
            var current = player.getMainHandItem();
            var reference = previousWeapon.get(com.abyssredemption.absmod.spirit.BladeSoulComponents.REFERENCE);
            if (previousSlot == player.getInventory().selected
                    && previousWeapon.getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem
                    && current.getItem() instanceof MeowBladeItem && reference != null
                    && reference.equals(current.get(com.abyssredemption.absmod.spirit.BladeSoulComponents.REFERENCE))) {
                tracker.replaceWeapon(previousWeapon, current);
            }
            previousWeapon = current;
            previousSlot = player.getInventory().selected;
        }
        var intent = tracker.tick(tick, minecraft.options.keyAttack.isDown() || pendingClick, eligible,
                player == null ? null : player.getMainHandItem(), player == null ? -1 : player.getInventory().selected);
        pendingClick = false;
        if (intent != null && dimension != null) {
            send.accept(new AttackIntentPayload(intent.epoch(), intent.sequence(), intent.held(), intent.slot(), dimension));
        }
    }
}
