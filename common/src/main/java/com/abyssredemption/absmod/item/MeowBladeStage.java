package com.abyssredemption.absmod.item;

/** Shared stage definitions used by both loaders. */
public enum MeowBladeStage {
    // Damage values are placeholders until the stage balance is supplied.
    STAGE_1(1, "meow_blade", 7.0F),
    STAGE_2(2, "meow_blade_stage_2", 7.0F),
    STAGE_3(3, "meow_blade_stage_3", 7.0F),
    STAGE_4(4, "meow_blade_stage_4", 7.0F),
    STAGE_5(5, "meow_blade_stage_5", 7.0F),
    STAGE_6(6, "meow_blade_stage_6", 7.0F),
    STAGE_7(7, "meow_blade_stage_7", 7.0F),
    STAGE_8(8, "meow_blade_stage_8", 7.0F),
    STAGE_9(9, "meow_blade_stage_9", 7.0F);

    private final int number;
    private final String itemId;
    private final float attackDamage;

    MeowBladeStage(int number, String itemId, float attackDamage) {
        this.number = number;
        this.itemId = itemId;
        this.attackDamage = attackDamage;
    }

    public int number() {
        return number;
    }

    public String itemId() {
        return itemId;
    }

    /** Total damage for a player with the vanilla base attack damage. */
    public float attackDamage() {
        return attackDamage;
    }
}
