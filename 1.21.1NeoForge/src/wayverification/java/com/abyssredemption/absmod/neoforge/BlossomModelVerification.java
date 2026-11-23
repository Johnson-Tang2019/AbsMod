package com.abyssredemption.absmod.neoforge;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

/** Uses the running client's item renderer and model predicates on every delivered sprite. */
public final class BlossomModelVerification extends Screen {
    private static final List<String> FAMILIES = List.of("blossom_bow", "dark_blossom_bow", "blossom_staff", "dark_blossom_staff");
    private static final List<String> LABELS = List.of("樱花弓", "暗樱弓", "樱花权杖", "暗樱权杖");
    private static final int[] DRAW_TICKS = {3, 14, 20};
    private final ArcherPose archer;
    private int frames;

    private BlossomModelVerification(Minecraft client) {
        super(Component.literal("樱花职业武器"));
        archer = new ArcherPose(client);
    }

    public static void verify(Minecraft client) {
        var screen = new BlossomModelVerification(client);
        for (String family : FAMILIES) {
            for (int n = 1; n <= (family.startsWith("dark") ? 8 : 9); n++) {
                String id = family + (n == 1 ? "" : "_stage_" + n);
                var item = stack(id);
                screen.check(client, item, id, -1);
                if (family.endsWith("bow")) {
                    for (int i = 0; i < DRAW_TICKS.length; i++) screen.check(client, item, id + "_pulling_" + i, DRAW_TICKS[i]);
                    var other = item.copy();
                    screen.checkIdleOtherHand(client, other, id);
                }
            }
        }
        System.out.println("BLOSSOM_85_BAKED_MODELS_PASS");
        client.setScreen(screen);
    }

    private void check(Minecraft client, ItemStack item, String texture, int ticks) {
        archer.item = item;
        archer.ticks = ticks;
        checkIdleOtherHand(client, item, texture);
    }

    private void checkIdleOtherHand(Minecraft client, ItemStack stack, String expected) {
        var baked = client.getItemRenderer().getModel(stack, client.level, archer, 0);
        var actual = baked.getParticleIcon().contents().name();
        if (!actual.equals(ResourceLocation.fromNamespaceAndPath("absmod", "item/" + expected)))
            throw new IllegalStateException("Baked item " + expected + " resolved to " + actual);
    }

    private static ItemStack stack(String name) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("absmod", name)).orElseThrow().getDefaultInstance();
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int pitch = Math.min(32, (width - 72) / 9);
        int left = (width - (9 * pitch + 46)) / 2;
        int row = Math.min(30, (height - 60) / 5);
        int top = (height - (5 * row + 36)) / 2;
        graphics.fill(0, 0, width, height, 0xFF251D30);
        graphics.drawString(font, title, left, top, 0xFFFFDBE9);
        for (int n = 1; n <= 9; n++) graphics.drawCenteredString(font, String.valueOf(n), left + 50 + (n - 1) * pitch + 8, top + 16, 0xFFD4AEC6);
        for (int j = 0; j < FAMILIES.size(); j++) {
            String family = FAMILIES.get(j);
            int y = top + 30 + j * row;
            graphics.drawString(font, LABELS.get(j), left, y + 4, 0xFFFFDBE9);
            for (int n = 1; n <= (family.startsWith("dark") ? 8 : 9); n++)
                graphics.renderItem(stack(family + (n == 1 ? "" : "_stage_" + n)), left + 50 + (n - 1) * pitch, y);
        }
        int y = top + 30 + 4 * row;
        graphics.drawString(font, "拉弓", left, y + 4, 0xFFFFDBE9);
        for (int j = 0; j < 2; j++) {
            var bow = stack(FAMILIES.get(j) + "_stage_" + (j == 0 ? 9 : 8));
            for (int i = 0; i < DRAW_TICKS.length; i++) {
                archer.item = bow;
                archer.ticks = DRAW_TICKS[i];
                graphics.renderItem(archer, bow, left + 50 + (j * 4 + i) * pitch, y, 0);
            }
        }
        graphics.flush();
        if (++frames == 3)
            Screenshot.grab(minecraft.gameDirectory, "blossom-weapon-stages.png", minecraft.getMainRenderTarget(),
                    message -> System.out.println("BLOSSOM_SCREENSHOT " + message.getString()));
        if (frames >= 5) minecraft.setScreen(null);
    }

    private static final class ArcherPose extends ArmorStand {
        private ItemStack item = ItemStack.EMPTY;
        private int ticks = -1;

        ArcherPose(Minecraft client) { super(client.level, 0, 0, 0); }
        @Override public boolean isUsingItem() { return ticks >= 0; }
        @Override public ItemStack getUseItem() { return isUsingItem() ? item : ItemStack.EMPTY; }
        @Override public int getUseItemRemainingTicks() { return isUsingItem() ? item.getUseDuration(this) - ticks : 0; }
    }
}
