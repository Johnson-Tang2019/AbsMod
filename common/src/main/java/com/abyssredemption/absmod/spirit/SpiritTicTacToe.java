package com.abyssredemption.absmod.spirit;

import java.util.Arrays;

/** One server-owned match. The spirit takes a win, blocks a loss, or chooses a free square. */
public final class SpiritTicTacToe {
    public static final int PLAYER = 1;
    public static final int SPIRIT = 2;
    public static final int DRAW = 3;
    private static final int[][] LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}
    };
    private final int[] cells = new int[9];
    private int result;

    public int cell(int slot) { return cells[slot]; }
    public int result() { return result; }

    public boolean play(int slot, net.minecraft.util.RandomSource random) {
        if (slot < 0 || slot >= cells.length || result != 0 || cells[slot] != 0) {
            return false;
        }
        place(slot, PLAYER);
        if (result == 0) {
            int reply = winningMove(SPIRIT);
            if (reply == -1) {
                reply = winningMove(PLAYER);
            }
            if (reply == -1) {
                int[] open = new int[9];
                int count = 0;
                for (int i = 0; i < cells.length; i++) {
                    if (cells[i] == 0) {
                        open[count++] = i;
                    }
                }
                reply = open[random.nextInt(count)];
            }
            place(reply, SPIRIT);
        }
        return true;
    }

    public void reset() {
        Arrays.fill(cells, 0);
        result = 0;
    }

    private void place(int slot, int side) {
        cells[slot] = side;
        if (wins(side)) {
            result = side;
        } else if (Arrays.stream(cells).noneMatch(value -> value == 0)) {
            result = DRAW;
        }
    }

    private int winningMove(int side) {
        for (var line : LINES) {
            int occupied = 0;
            int open = -1;
            for (int index : line) {
                if (cells[index] == side) {
                    occupied++;
                } else if (cells[index] == 0) {
                    open = index;
                }
            }
            if (occupied == 2 && open >= 0) {
                return open;
            }
        }
        return -1;
    }

    private boolean wins(int side) {
        return Arrays.stream(LINES).anyMatch(line -> cells[line[0]] == side && cells[line[1]] == side && cells[line[2]] == side);
    }
}
