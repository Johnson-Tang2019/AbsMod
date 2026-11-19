package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public final class SpiritGameMenu extends AbstractContainerMenu {
    public static final MenuType<SpiritGameMenu> TYPE = new MenuType<>(SpiritGameMenu::new, FeatureFlags.DEFAULT_FLAGS);
    public static final int RESTART = 9;
    public static final int BACK = 10;
    private final DataSlot[] cells = new DataSlot[9];
    private final DataSlot result = DataSlot.standalone();
    private final Player viewer;
    private final BladeSpiritEntity opponent;
    private final SpiritTicTacToe game;
    private final Runnable back;

    private SpiritGameMenu(int id, Inventory inventory) {
        this(id, inventory, null, null);
    }

    private SpiritGameMenu(int id, Inventory inventory, BladeSpiritEntity opponent, Runnable back) {
        super(TYPE, id);
        this.viewer = inventory.player;
        this.opponent = opponent;
        this.game = opponent == null ? null : new SpiritTicTacToe();
        this.back = back;
        for (int i = 0; i < cells.length; i++) {
            cells[i] = addDataSlot(DataSlot.standalone());
        }
        addDataSlot(result);
    }

    public static void open(ServerPlayer player, BladeSpiritEntity spirit, Runnable back) {
        player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> new SpiritGameMenu(id, inventory, spirit, back),
                Component.translatable("leisure.absmod.board")));
    }

    public int cell(int slot) { return cells[slot].get(); }
    public int result() { return result.get(); }

    @Override public boolean stillValid(Player player) {
        return player.isAlive() && (opponent == null || (!player.isSpectator() && opponent.isAlive()
                && opponent.level() == player.level() && player.getUUID().equals(opponent.ownerId())
                && player.distanceToSqr(opponent) <= 256 && opponent.active()));
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || player != viewer || player.containerMenu != this
                || game == null || !stillValid(player)) {
            return false;
        }
        if (button == BACK) {
            back.run();
            return true;
        }
        if (button == RESTART) {
            if (game.result() == 0) {
                return false;
            }
            game.reset();
        } else {
            if (!game.play(button, player.getRandom())) {
                return false;
            }
            if (game.result() != 0) {
                BladeAdvancements.award(serverPlayer, "spirit_board_game");
            }
        }
        for (int i = 0; i < cells.length; i++) {
            cells[i].set(game.cell(i));
        }
        result.set(game.result());
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
