package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.spirit.SpiritGameMenu;
import com.abyssredemption.absmod.spirit.SpiritTicTacToe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SpiritGameScreen extends AbstractContainerScreen<SpiritGameMenu> {
    private static final int WIDTH = 186;
    private static final int HEIGHT = 180;
    private final Button[] tiles = new Button[9];
    private Button replay;

    public SpiritGameScreen(SpiritGameMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    @Override protected void init() {
        super.init();
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = addButton(Component.empty(), i, 43 + 34 * (i % 3), 43 + 34 * (i / 3), 32, 32);
        }
        replay = addButton(label("again"), SpiritGameMenu.RESTART, 93, 153, 82, 18);
        addButton(label("back"), SpiritGameMenu.BACK, 11, 153, 76, 18);
        updateBoard();
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateBoard();
    }

    private Button addButton(Component text, int id, int x, int y, int w, int h) {
        return addRenderableWidget(Button.builder(text, ignored -> {
            var game = Minecraft.getInstance().gameMode;
            if (game != null) {
                game.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + x, topPos + y, w, h).build());
    }

    private void updateBoard() {
        for (int i = 0; i < tiles.length; i++) {
            int mark = menu.cell(i);
            tiles[i].setMessage(Component.literal(mark == SpiritTicTacToe.PLAYER ? "X" : mark == SpiritTicTacToe.SPIRIT ? "O" : ""));
            tiles[i].active = mark == 0 && menu.result() == 0;
        }
        replay.active = menu.result() != 0;
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFAD668D);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xFFFFF4FA);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + 22, 0xFFF1BCD7);
        graphics.fill(x + 40, y + 40, x + 146, y + 147, 0xFFF1D3E4);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 11, 8, 0xFF4D2640, false);
        Component status = label(switch (menu.result()) {
            case SpiritTicTacToe.PLAYER -> "win";
            case SpiritTicTacToe.SPIRIT -> "loss";
            case SpiritTicTacToe.DRAW -> "draw";
            default -> "turn";
        });
        graphics.text(font, status, (WIDTH - font.width(status)) / 2, 28, 0xFF4D2640, false);
    }

    private static Component label(String key) { return Component.translatable("leisure.absmod." + key); }
}
