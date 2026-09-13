package com.abyssredemption.absmod.item;

/** Shared stage definitions used by both loaders. */
public enum MeowBladeStage {
    // PROPOSED PvE profile v1; not gameplay-validated final balance.
    STAGE_1(1, "meow_blade", 7.0F, 1.8F),
    STAGE_2(2, "meow_blade_stage_2", 8.0F, 2.0F),
    STAGE_3(3, "meow_blade_stage_3", 9.0F, 2.2F),
    STAGE_4(4, "meow_blade_stage_4", 10.0F, 2.5F),
    STAGE_5(5, "meow_blade_stage_5", 12.0F, 2.8F),
    STAGE_6(6, "meow_blade_stage_6", 14.0F, 3.2F),
    STAGE_7(7, "meow_blade_stage_7", 16.0F, 3.6F),
    STAGE_8(8, "meow_blade_stage_8", 18.0F, 4.0F),
    STAGE_9(9, "meow_blade_stage_9", 22.0F, 5.0F);

    private final int number;
    private final String itemId;
    private final float attackDamage;
    private final float baseAttackSpeed;

    MeowBladeStage(int number, String itemId, float attackDamage, float baseAttackSpeed) {
        this.number = number;
        this.itemId = itemId;
        this.attackDamage = attackDamage;
        this.baseAttackSpeed = baseAttackSpeed;
    }

    /** Candidate stack caps from BALANCE_PROFILE.json, not final balance. */
    public int maxComboStacks() {
        return switch (this) {
            case STAGE_1 -> 5;
            case STAGE_2 -> 6;
            case STAGE_3 -> 8;
            case STAGE_4 -> 10;
            case STAGE_5 -> 12;
            case STAGE_6 -> 14;
            case STAGE_7 -> 16;
            case STAGE_8 -> 18;
            case STAGE_9 -> 20;
        };
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
    /** Total attack speed including the vanilla player base of 4. */
    public float baseAttackSpeed() {
        return baseAttackSpeed;
    }
}
