package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.AbsMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;

public final class BladeSoulComponents {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "blade_soul");
    public static final DataComponentType<BladeSoulReference> REFERENCE =
            DataComponentType.<BladeSoulReference>builder().persistent(BladeSoulReference.CODEC).build();

    private BladeSoulComponents() {}
}
