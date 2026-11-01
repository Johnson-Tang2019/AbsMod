package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/** Ordinary player intents use the same authoritative transaction as operator commands. */
public final class SpiritInteraction {
    private SpiritInteraction() {}

    public static InteractionResult manifest(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) { return InteractionResult.PASS; }
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                if (!player.isAlive() || player.isSpectator()) { throw new IllegalStateException("owner_mismatch"); }
                var server = serverPlayer.level().getServer();
                var store = player.getMainHandItem().has(BladeSoulComponents.REFERENCE)
                        ? BladeSoulStore.find(server).orElseThrow(() -> new IllegalStateException("missing_record"))
                        : BladeSoulStore.forInitialization(server);
                BladeSoulService.initialize(store, player.getMainHandItem(), player.getUUID());
                SpiritTransformationService.manifest(serverPlayer);
                notify(serverPlayer, "message.absmod.spirit.manifested");
            } catch (IllegalStateException exception) { report(serverPlayer, exception); }
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult recall(Player player, InteractionHand hand, BladeSpiritEntity spirit) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) { return InteractionResult.PASS; }
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                SpiritTransformationService.recall(serverPlayer, spirit, player.getInventory().selected);
                notify(serverPlayer, "message.absmod.spirit.recalled");
            } catch (IllegalStateException exception) { report(serverPlayer, exception); }
        }
        return InteractionResult.SUCCESS;
    }

    private static void notify(ServerPlayer player, String key) {
        if (player.connection != null) { player.sendSystemMessage(Component.translatable(key), true); }
    }

    private static void report(ServerPlayer serverPlayer, IllegalStateException exception) {
        notify(serverPlayer, "message.absmod.spirit.error." + exception.getMessage());
    }
}
