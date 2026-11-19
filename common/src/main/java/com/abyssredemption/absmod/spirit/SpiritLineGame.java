package com.abyssredemption.absmod.spirit;

import net.minecraft.util.RandomSource;

/** Tic-tac-toe and freestyle Gomoku use the same straight-line win rule. */
final class SpiritLineGame implements SpiritBoardGame {
    private static final int[][] AXES = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
    private final int size;
    private final int required;
    private final int[] cells;
    private int available;
    private int result;

    SpiritLineGame(int size, int required) {
        this.size = size;
        this.required = required;
        cells = new int[size * size];
        available = cells.length;
    }

    @Override public int cell(int slot) { return cells[slot]; }
    @Override public int result() { return result; }
    @Override public boolean canPlay(int slot) {
        return slot >= 0 && slot < cells.length && result == EMPTY && cells[slot] == EMPTY;
    }

    @Override public boolean play(int slot, RandomSource random) {
        if (!canPlay(slot)) {
            return false;
        }
        place(slot, PLAYER);
        if (result == EMPTY) {
            place(reply(random), SPIRIT);
        }
        return true;
    }

    private void place(int slot, int side) {
        cells[slot] = side;
        --available;
        if (lineLength(slot, side) >= required) {
            result = side;
        } else if (available == 0) {
            result = DRAW;
        }
    }

    private int reply(RandomSource random) {
        int selected = -1;
        int best = -1;
        int ties = 0;
        for (int slot = 0; slot < cells.length; slot++) {
            if (cells[slot] != EMPTY) {
                continue;
            }
            int own = lineLength(slot, SPIRIT);
            if (own >= required) {
                return slot;
            }
            int threat = lineLength(slot, PLAYER);
            int score = threat >= required ? 1000 : own * own + threat * threat;
            if (score > best) {
                selected = slot;
                best = score;
                ties = 1;
            } else if (score == best && random.nextInt(++ties) == 0) {
                selected = slot;
            }
        }
        return selected;
    }

    private int lineLength(int slot, int side) {
        int longest = 1;
        for (int[] axis : AXES) {
            int row = slot / size;
            int column = slot % size;
            int length = 1 + count(row, column, axis[0], axis[1], side) + count(row, column, -axis[0], -axis[1], side);
            longest = Math.max(longest, length);
        }
        return longest;
    }

    private int count(int row, int column, int dy, int dx, int side) {
        int count = 0;
        for (int y = row + dy, x = column + dx;
                y >= 0 && y < size && x >= 0 && x < size && cells[y * size + x] == side; y += dy, x += dx) {
            count++;
        }
        return count;
    }
}
