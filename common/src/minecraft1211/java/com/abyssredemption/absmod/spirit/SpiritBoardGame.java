package com.abyssredemption.absmod.spirit;

import net.minecraft.util.RandomSource;

public interface SpiritBoardGame {
    int EMPTY = 0;
    int PLAYER = 1;
    int SPIRIT = 2;
    int DRAW = 3;

    int cell(int slot);
    int result();
    boolean canPlay(int slot);
    boolean play(int slot, RandomSource random);
}
