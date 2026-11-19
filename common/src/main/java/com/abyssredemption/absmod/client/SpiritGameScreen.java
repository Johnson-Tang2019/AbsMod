package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.spirit.SpiritGameMenu;
import com.abyssredemption.absmod.spirit.SpiritBoardGame;
import com.abyssredemption.absmod.spirit.SpiritGameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SpiritGameScreen extends AbstractContainerScreen<SpiritGameMenu> {
    private static final int WIDTH = 192;
    private static final int HEIGHT = 228;
    private SpiritGameType layout;
    private Button[] tiles;
    private Button replay;

    public SpiritGameScreen(SpiritGameMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    @Override protected void init() {
        super.init();
        layout = menu.gameType();
        tiles = new Button[layout.cellCount()];
        int side = layout.side();
        int step = side == 3 ? 40 : 17;
        int gridWidth = side * step;
        int gridLeft = (WIDTH - gridWidth) / 2;
        int gridTop = 44 + (153 - gridWidth) / 2;
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = addButton(Component.empty(), i, gridLeft + step * (i % side),
                    gridTop + step * (i / side), step - 1, step - 1);
        }
        replay = addButton(label("again"), SpiritGameMenu.RESTART, 99, 201, 82, 18);
        addButton(label("back"), SpiritGameMenu.BACK, 11, 201, 82, 18);
        updateBoard();
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (layout != menu.gameType()) {
            rebuildWidgets();
        }
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
            boolean legal = menu.canSelect(i);
            boolean line = layout == SpiritGameType.TIC_TAC_TOE;
            String symbol = switch (mark) {
                case SpiritBoardGame.PLAYER -> line ? "X" : "●";
                case SpiritBoardGame.SPIRIT -> line ? "O" : "○";
                default -> legal && layout == SpiritGameType.REVERSI ? "·" : "";
            };
            tiles[i].setMessage(Component.literal(symbol));
            tiles[i].active = legal;
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
        graphics.fill(x + 16, y + 40, x + WIDTH - 16, y + 198, 0xFFF1D3E4);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 11, 8, 0xFF4D2640, false);
        Component status = label(switch (menu.result()) {
            case SpiritBoardGame.PLAYER -> "win";
            case SpiritBoardGame.SPIRIT -> "loss";
            case SpiritBoardGame.DRAW -> "draw";
            default -> layout == SpiritGameType.TIC_TAC_TOE ? "turn" : "turn_stones";
        });
        graphics.text(font, status, (WIDTH - font.width(status)) / 2, 27, 0xFF4D2640, false);
    }

    private static Component label(String key) { return Component.translatable("leisure.absmod." + key); }
}
