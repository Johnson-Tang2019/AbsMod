package com.abyssredemption.absmod.cat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** Read-only panel sent in the standard open-menu payload. Buttons use vanilla menu packets. */
public final class CatJournalMenu extends AbstractContainerMenu {
    public static final MenuType<CatJournalMenu> TYPE = new MenuType<>(CatJournalMenu::new, FeatureFlags.DEFAULT_FLAGS);
    public static final int PAGE_SIZE = 5;
    private final Player viewer;
    private final Map<Integer, Runnable> actions = new HashMap<>();

    public record Row(Component title, Component detail, Runnable action) {}
    public record Control(Component text, Runnable action) {}

    public CatJournalMenu(int id, Inventory inventory) {
        super(TYPE, id);
        viewer = inventory.player;
    }

    public static void openCollection(ServerPlayer player) {
        CatJournalViews.collection(player);
    }

    public static void openPartners(ServerPlayer player) {
        CatJournalViews.partners(player, 0);
    }

    static void show(ServerPlayer player, Component heading, Component summary, List<Row> rows, List<Control> controls) {
        var encodedRows = Component.empty();
        var encodedControls = Component.empty();
        var actions = new HashMap<Integer, Runnable>();
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            int id = row.action() == null ? -1 : i;
            if (id >= 0) {
                actions.put(id, row.action());
            }
            encodedRows.append(Component.empty().append(Component.literal(Integer.toString(id))).append(row.title()).append(row.detail()));
        }
        for (int i = 0; i < controls.size(); i++) {
            var control = controls.get(i);
            int id = control.action() == null ? -1 : PAGE_SIZE + i;
            if (id >= 0) {
                actions.put(id, control.action());
            }
            encodedControls.append(Component.empty().append(Component.literal(Integer.toString(id))).append(control.text()));
        }
        var data = Component.empty().append(heading).append(summary).append(encodedRows).append(encodedControls);
        player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> {
            var menu = new CatJournalMenu(id, inventory);
            menu.actions.putAll(actions);
            return menu;
        }, data));
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (player != viewer || player.containerMenu != this || !player.isAlive()) {
            return false;
        }
        var action = actions.get(button);
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    @Override public boolean stillValid(Player player) { return player.isAlive(); }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
