package com.abyssredemption.absmod.item;

/** Route-neutral stage contract; existing normal item IDs remain unchanged. */
public interface BladeStage {
    int number();
    String itemId();
    float attackDamage();
    float baseAttackSpeed();
    int maxComboStacks();
    int durability();
    BladeRoute route();
    static BladeStage of(BladeRoute route,int number) {
        if(!route.accepts(number))throw new IllegalArgumentException("invalid_stage");
        return route==BladeRoute.NORMAL?MeowBladeStage.values()[number-1]:DarkBladeStage.values()[number-1];
    }
}
