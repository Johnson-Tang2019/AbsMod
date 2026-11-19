package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** One compact line above the hotbar, based on the owning server's latest snapshot. */
public final class BladeCombatHud {
    private BladeCombatHud() {}

    public static void draw(GuiGraphics graphics) {
        var minecraft = Minecraft.getInstance();
        var status = BladeCombatClient.visibleStatus(minecraft);
        if (status.slot() < 0) { return; }
        Component text;
        int color;
        if (status.dark()) {
            color = 0xFFE3CAFF;
            if (status.windupTicks() > 0) {
                text = Component.translatable("combat.absmod.hud.heavy_preparing");
            } else if (status.cooldownSeconds() > 0) {
                text = Component.translatable("combat.absmod.hud.heavy_cooldown", status.cooldownSeconds());
            } else {
                text = Component.translatable("combat.absmod.hud.heavy_ready", BladeKeys.HEAVY.getTranslatedKeyMessage());
            }
        } else {
            if (status.comboStacks() <= 0) { return; }
            var blade = (MeowBladeItem) minecraft.player.getMainHandItem().getItem();
            int maximum = blade.stage().maxComboStacks();
            String label = status.decaying() ? "combo_decaying"
                    : status.comboStacks() >= maximum ? "combo_full" : "combo";
            text = Component.translatable("combat.absmod.hud." + label, status.comboStacks(), maximum);
            color = status.decaying() ? 0xFFD6BDD0 : 0xFFFFDBEB;
        }
        int width = minecraft.font.width(text);
        int x = (graphics.guiWidth() - width) / 2;
        int y = Math.max(4, graphics.guiHeight() - 90);
        graphics.fill(x - 4, y - 3, x + width + 4, y + 11, 0xA63C2338);
        graphics.drawString(minecraft.font, text, x, y, color, false);
    }
}
