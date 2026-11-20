package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.item.SpiritKeepsake;
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
    public static final int RESTART = 100;
    public static final int BACK = 101;
    private static final int CELLS_PER_SLOT = 5;
    private static final int CELL_BITS = 3;
    private static final int LEGAL_MOVE = 4;
    private final DataSlot[] cells = new DataSlot[(SpiritGameType.MAX_CELLS + CELLS_PER_SLOT - 1) / CELLS_PER_SLOT];
    private final DataSlot result = DataSlot.standalone();
    private final DataSlot type = DataSlot.standalone();
    private final Player viewer;
    private final BladeSpiritEntity opponent;
    private SpiritBoardGame game;
    private final Runnable back;

    private SpiritGameMenu(int id, Inventory inventory) {
        this(id, inventory, null, SpiritGameType.TIC_TAC_TOE, null);
    }

    private SpiritGameMenu(int id, Inventory inventory, BladeSpiritEntity opponent, SpiritGameType gameType, Runnable back) {
        super(TYPE, id);
        this.viewer = inventory.player;
        this.opponent = opponent;
        this.game = opponent == null ? null : gameType.newGame();
        this.back = back;
        for (int i = 0; i < cells.length; i++) {
            cells[i] = addDataSlot(DataSlot.standalone());
        }
        addDataSlot(result);
        addDataSlot(type);
        type.set(gameType.ordinal());
        if (game != null) {
            sync(gameType);
        }
    }

    public static void open(ServerPlayer player, BladeSpiritEntity spirit, SpiritGameType type, Runnable back) {
        player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> new SpiritGameMenu(id, inventory, spirit, type, back),
                type.title()));
    }

    public SpiritGameType gameType() { return SpiritGameType.fromNetwork(type.get()); }
    public int cell(int slot) { return readCell(slot) & 3; }
    public boolean canSelect(int slot) { return readCell(slot) == LEGAL_MOVE; }
    public int result() { return result.get(); }

    private int readCell(int slot) {
        return cells[slot / CELLS_PER_SLOT].get() >> (CELL_BITS * (slot % CELLS_PER_SLOT)) & 7;
    }

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
            game = gameType().newGame();
        } else {
            if (!game.play(button, player.getRandom())) {
                return false;
            }
            if (game.result() != 0) {
                BladeAdvancements.award(serverPlayer, gameType().advancement());
                SpiritKeepsake.finishGame(serverPlayer, opponent);
            }
        }
        sync(gameType());
        return true;
    }

    private void sync(SpiritGameType variant) {
        // The vanilla data-slot packet uses signed shorts. Pack five three-bit cells in each slot.
        for (int group = 0; group < cells.length; group++) {
            int packed = 0;
            for (int offset = 0; offset < CELLS_PER_SLOT; offset++) {
                int slot = group * CELLS_PER_SLOT + offset;
                if (slot >= variant.cellCount()) {
                    break;
                }
                int value = game.canPlay(slot) ? LEGAL_MOVE : game.cell(slot);
                packed |= value << (offset * CELL_BITS);
            }
            cells[group].set(packed);
        }
        result.set(game.result());
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
