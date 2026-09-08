package com.abyssredemption.absmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class MeowBladeItem extends Item {
    private final MeowBladeStage stage;

    public MeowBladeItem(Properties properties) {
        this(properties, MeowBladeStage.STAGE_1);
    }

    public MeowBladeItem(Properties properties, MeowBladeStage stage) {
        // Remove the player base and material bonus to obtain the sword-specific bonus.
        super(properties.sword(ToolMaterial.DIAMOND,
                stage.attackDamage() - 1.0F - ToolMaterial.DIAMOND.attackDamageBonus(), -2.4F));
        this.stage = stage;
    }

    public MeowBladeStage stage() {
        return stage;
    }
}
