package com.abyssredemption.absmod.client;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.spirit.ResonanceMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Pink resonance workshop with server-owned inputs and a standard container button. */
public final class ResonanceScreen extends AbstractContainerScreen<ResonanceMenu> {
    private static final int INK = 0xFF562342;
    private static final int PAPER = 0xFFFFEAF3;
    private static final int BORDER = 0xFFAD557E;
    private static final int SLOT_EDGE = 0xFFDA95B4;
    private static final int SLOT_FILL = 0xFFFFF6FA;
    private static final Component ACTION = label("action");
    private Button confirmButton;

    public ResonanceScreen(ResonanceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ResonanceMenu.WIDTH, ResonanceMenu.HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        confirmButton = addRenderableWidget(new ResonanceButton(leftPos + 130, topPos + 111, button -> {
            var mode = Minecraft.getInstance().gameMode;
            if (mode != null) {
                mode.handleInventoryButtonClick(menu.containerId, ResonanceMenu.CONFIRM_BUTTON);
            }
        }));
        updateConfirmButton();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateConfirmButton();
    }

    private void updateConfirmButton() {
        confirmButton.active = problem() == null;
    }

    private String problem() {
        if (menu.isComplete()) {
            return "close_to_continue";
        }
        if (!(menu.getSlot(0).getItem().getItem() instanceof MeowBladeItem blade)) {
            return "insert_blade";
        }
        var stage = blade.stage();
        int next = stage.number() + 1;
        if (next >= 9) {
            return "limit";
        }
        if (menu.currentAffection() < 0) {
            return "check_identity";
        }
        if (menu.currentAffection() < ResonanceMenu.affection(stage.route(), next)) {
            return "insufficient_bond";
        }
        if (!menu.hasMaterials(ResonanceMenu.costs(stage.route(), next))) {
            return "insufficient_items";
        }
        return null;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        drawPanel(graphics);
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x + 2, y + 3, x + imageWidth + 2, y + imageHeight + 3, 0xAA49243C);
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BORDER);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, PAPER);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 25, 0xFFF2B6CE);
        graphics.fill(x + 2, y + 24, x + imageWidth - 2, y + 25, SLOT_EDGE);
        graphics.fill(x + 10, y + 74, x + imageWidth - 10, y + 75, SLOT_EDGE);
        graphics.fill(x + 10, y + 132, x + imageWidth - 10, y + 133, SLOT_EDGE);
        for (var slot : menu.slots) {
            if (slot.isActive()) {
                int slotX = x + slot.x;
                int slotY = y + slot.y;
                graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, SLOT_EDGE);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, SLOT_FILL);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 16, 10, INK, false);
        graphics.text(font, label("blade"), 16, 34, INK, false);
        graphics.text(font, label("materials"), 53, 34, INK, false);
        graphics.text(font, label("preview"), 16, 77, INK, false);
        graphics.text(font, label("required"), 53, 77, INK, false);
        graphics.text(font, label("bond"), 121, 77, INK, false);
        graphics.text(font, playerInventoryTitle, 28, 135, INK, false);
        if (menu.getSlot(0).getItem().getItem() instanceof MeowBladeItem blade) {
            var stage = blade.stage();
            int next = stage.number() + 1;
            if (next < 9 && menu.currentAffection() >= 0) {
                graphics.text(font, label("bond_progress", menu.currentAffection(),
                        ResonanceMenu.affection(stage.route(), next)), 121, 92, INK, false);
            }
        }
        String problem = problem();
        if (problem != null) {
            graphics.text(font, label(problem), 16, 117, INK, false);
        }
    }

    private static Component label(String key, Object... args) {
        return Component.translatable("screen.absmod.resonance." + key, args);
    }

    private static final class ResonanceButton extends Button {
        private ResonanceButton(int x, int y, OnPress onPress) {
            super(x, y, 70, 19, ACTION, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int fill = active ? isHoveredOrFocused() ? 0xFFAA3D73 : 0xFF8E2C61 : 0xFFF0C9DC;
            graphics.fill(x, y, x + getWidth(), y + getHeight(), BORDER);
            graphics.fill(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1, fill);
            var font = Minecraft.getInstance().font;
            graphics.text(font, getMessage(), x + (getWidth() - font.width(getMessage())) / 2,
                    y + (getHeight() - 8) / 2, active ? 0xFFFFFFFF : INK, false);
        }
    }
}
