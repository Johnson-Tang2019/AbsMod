package com.abyssredemption.absmod.spirit;

import net.minecraft.util.RandomSource;

/** Standard eight-by-eight Reversi with forced passes handled on the server. */
final class SpiritReversi implements SpiritBoardGame {
    private static final int SIZE = 8;
    private final int[] cells = new int[SIZE * SIZE];
    private int result;

    SpiritReversi() {
        cells[28] = cells[35] = PLAYER;
        cells[27] = cells[36] = SPIRIT;
    }

    @Override public int cell(int slot) { return cells[slot]; }
    @Override public int result() { return result; }

    @Override public boolean canPlay(int slot) {
        return slot >= 0 && slot < cells.length && result == EMPTY && captures(slot, PLAYER, false) > 0;
    }

    @Override public boolean play(int slot, RandomSource random) {
        if (!canPlay(slot)) {
            return false;
        }
        place(slot, PLAYER);
        do {
            int reply = bestReply(random);
            if (reply >= 0) {
                place(reply, SPIRIT);
            }
            if (hasPlayerMove()) {
                return true;
            }
            if (reply < 0) {
                finish();
                return true;
            }
        } while (true);
    }

    private boolean hasPlayerMove() {
        for (int slot = 0; slot < cells.length; slot++) {
            if (canPlay(slot)) {
                return true;
            }
        }
        return false;
    }

    private int bestReply(RandomSource random) {
        int selected = -1;
        int best = -1;
        int ties = 0;
        for (int slot = 0; slot < cells.length; slot++) {
            int flipped = captures(slot, SPIRIT, false);
            if (flipped == 0) {
                continue;
            }
            int row = slot / SIZE;
            int column = slot % SIZE;
            boolean corner = (row == 0 || row == SIZE - 1) && (column == 0 || column == SIZE - 1);
            int score = flipped + (corner ? 16 : 0);
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

    private void place(int slot, int side) {
        captures(slot, side, true);
        cells[slot] = side;
    }

    private int captures(int slot, int side, boolean flip) {
        if (cells[slot] != EMPTY) {
            return 0;
        }
        int total = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dy != 0 || dx != 0) {
                    total += trace(slot / SIZE, slot % SIZE, dy, dx, side, flip);
                }
            }
        }
        return total;
    }

    private int trace(int row, int column, int dy, int dx, int side, boolean flip) {
        int count = 0;
        int y = row + dy;
        int x = column + dx;
        while (inside(y, x) && cells[y * SIZE + x] == (PLAYER + SPIRIT - side)) {
            count++;
            y += dy;
            x += dx;
        }
        if (!inside(y, x) || cells[y * SIZE + x] != side) {
            return 0;
        }
        if (flip) {
            for (int i = 1; i <= count; i++) {
                cells[(row + i * dy) * SIZE + column + i * dx] = side;
            }
        }
        return count;
    }

    private void finish() {
        int lead = 0;
        for (int cell : cells) {
            lead += cell == PLAYER ? 1 : cell == SPIRIT ? -1 : 0;
        }
        result = lead > 0 ? PLAYER : lead < 0 ? SPIRIT : DRAW;
    }

    private static boolean inside(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }
}
