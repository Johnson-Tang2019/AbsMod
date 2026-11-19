package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.cat.CatJournalMenu;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CatJournalScreen extends AbstractContainerScreen<CatJournalMenu> {
    private static final int WIDTH = 308;
    private static final int HEIGHT = 214;
    private static final int INK = 0xFF4D2640;
    private static final int MUTED = 0xFF78526C;

    private final List<Component> rows;
    private final List<Component> controls;

    public CatJournalScreen(CatJournalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        rows = group(title, 2);
        controls = group(title, 3);
    }

    @Override protected void init() {
        super.init();
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i).getSiblings();
            int id = action(row);
            if (id >= 0) {
                addButton(row.get(1), id, leftPos + 9, topPos + 41 + i * 24, 116, 19);
            }
        }
        for (int i = 0; i < controls.size(); i++) {
            var control = controls.get(i).getSiblings();
            addButton(control.get(1), action(control), leftPos + 9 + i % 3 * 97, topPos + 164 + i / 3 * 22, 93, 19);
        }
    }

    private void addButton(Component label, int id, int x, int y, int width, int height) {
        var button = Button.builder(fit(label, width - 8), widget -> {
            var game = Minecraft.getInstance().gameMode;
            if (game != null) {
                game.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(x, y, width, height).build();
        button.active = id >= 0;
        addRenderableWidget(button);
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFAD668D);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xFFFFF4FA);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + 23, 0xFFF1BCD7);
        for (int i = 0; i < rows.size(); i++) {
            int rowY = y + 39 + i * 24;
            graphics.fill(x + 7, rowY, x + WIDTH - 7, rowY + 23, i % 2 == 0 ? 0xFFFDE6F1 : 0xFFFFEDF8);
        }
    }

    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var parts = title.getSiblings();
        if (parts.size() < 4) {
            return;
        }
        graphics.text(font, fit(parts.get(0), WIDTH - 20), 11, 9, INK, false);
        graphics.text(font, fit(parts.get(1), WIDTH - 20), 11, 28, MUTED, false);
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i).getSiblings();
            int y = 47 + i * 24;
            if (action(row) < 0) {
                graphics.text(font, fit(row.get(1), 112), 12, y, INK, false);
            }
            var detail = font.split(row.get(2), 164);
            if (!detail.isEmpty()) {
                int lineY = detail.size() > 1 ? y - 4 : y;
                for (int line = 0; line < Math.min(2, detail.size()); line++) {
                    graphics.text(font, detail.get(line), 134, lineY + line * 9, MUTED, false);
                }
            }
        }
    }

    private Component fit(Component text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return Component.literal(font.plainSubstrByWidth(text.getString(), width - font.width("…")) + "…");
    }

    private static List<Component> group(Component title, int index) {
        var parts = title.getSiblings();
        return parts.size() > index ? parts.get(index).getSiblings() : List.of();
    }

    private static int action(List<Component> data) {
        return Integer.parseInt(data.getFirst().getString());
    }
}
