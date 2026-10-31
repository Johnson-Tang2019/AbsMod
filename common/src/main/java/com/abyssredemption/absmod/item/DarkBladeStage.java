package com.abyssredemption.absmod.item;

/** Approved prototype candidates, not gameplay-validated final balance. */
public enum DarkBladeStage implements BladeStage {
    STAGE_1(1,14,1.10F),STAGE_2(2,17,1.15F),STAGE_3(3,21,1.20F),STAGE_4(4,26,1.25F),
    STAGE_5(5,31,1.30F),STAGE_6(6,37,1.40F),STAGE_7(7,43,1.50F),STAGE_8(8,50,1.60F);
    private final int number;
    private final float damage,speed;
    DarkBladeStage(int number,float damage,float speed){this.number=number;this.damage=damage;this.speed=speed;}
    public int number(){return number;}
    public String itemId(){return number==1?"dark_meow_blade":"dark_meow_blade_stage_"+number;}
    public float attackDamage(){return damage;}
    public float baseAttackSpeed(){return speed;}
    public int maxComboStacks(){return 0;}
    // No dark durability progression was specified; retain vanilla netherite durability.
    public int durability(){return 2031;}
    public BladeRoute route(){return BladeRoute.DARK;}
}
