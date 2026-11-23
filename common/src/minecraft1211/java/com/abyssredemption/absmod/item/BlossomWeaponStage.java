package com.abyssredemption.absmod.item;

/** Both ranged professions follow the ordinary nine stages and the dark eight stages. */
public record BlossomWeaponStage(BladeRoute route, int number) {
    public BlossomWeaponStage {
        if (!route.accepts(number)) throw new IllegalArgumentException("Invalid blossom weapon stage: " + number);
    }

    public boolean dark() {
        return route == BladeRoute.DARK;
    }

    public String itemId(String weapon) {
        return (dark() ? "dark_" : "") + "blossom_" + weapon + (number == 1 ? "" : "_stage_" + number);
    }

    // Prototype PvE tuning. A bow still uses the vanilla velocity, critical and enchantment calculation.
    public double arrowBonus() {
        return dark() ? 0.75 + 0.30 * (number - 1) : 0.22 * (number - 1);
    }

    public float spellDamage() {
        return dark() ? 6F + 1.2F * (number - 1) : 4F + 0.8F * (number - 1);
    }

    public int spellRecovery() {
        return dark() ? 36 : 20;
    }
}
