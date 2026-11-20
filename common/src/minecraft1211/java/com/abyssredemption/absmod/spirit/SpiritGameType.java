package com.abyssredemption.absmod.spirit;

import net.minecraft.network.chat.Component;

public enum SpiritGameType {
    TIC_TAC_TOE(3, "board", "spirit_board_game"),
    GOMOKU(9, "gomoku", "spirit_gomoku"),
    REVERSI(8, "reversi", "spirit_reversi");

    private static final SpiritGameType[] NETWORK_ORDER = values();
    public static final int MAX_CELLS = GOMOKU.cellCount();
    private final int side;
    private final String label;
    private final String advancement;

    SpiritGameType(int side, String label, String advancement) {
        this.side = side;
        this.label = label;
        this.advancement = advancement;
    }

    public static SpiritGameType fromNetwork(int number) {
        return number >= 0 && number < NETWORK_ORDER.length ? NETWORK_ORDER[number] : TIC_TAC_TOE;
    }

    public int side() { return side; }
    public int cellCount() { return side * side; }
    public Component title() { return Component.translatable("leisure.absmod." + label); }
    public Component rules() { return Component.translatable("leisure.absmod.rules." + label); }
    public String advancement() { return advancement; }

    public SpiritBoardGame newGame() {
        return switch (this) {
            case TIC_TAC_TOE -> new SpiritLineGame(side, 3);
            case GOMOKU -> new SpiritLineGame(side, 5);
            case REVERSI -> new SpiritReversi();
        };
    }
}
