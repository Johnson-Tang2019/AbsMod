package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.BladeCombatClient;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import com.abyssredemption.absmod.client.BladeCombatHud;

/** Hide vanilla charge indicators only while the server-managed blade pipeline is available. */
@Mixin(Gui.class)
public abstract class BladeAttackIndicatorMixin {
    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void absmod$bladeStatus(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo callback) {
        BladeCombatHud.draw(graphics);
    }

    @ModifyExpressionValue(method = {"renderCrosshair", "renderItemHotbar"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object absmod$hideChargeIndicator(Object original) {
        var player = Minecraft.getInstance().player;
        return original instanceof AttackIndicatorStatus && BladeCombatClient.isAvailable()
                && player != null && player.getMainHandItem().getItem() instanceof MeowBladeItem
                ? AttackIndicatorStatus.OFF : original;
    }
}
