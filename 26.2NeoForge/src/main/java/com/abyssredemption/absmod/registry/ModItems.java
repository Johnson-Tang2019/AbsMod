package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.MeowBladeStage;
import com.abyssredemption.absmod.item.MilkshakeItem;
import com.abyssredemption.absmod.item.MilkshakeVariant;
import java.util.Arrays;
import java.util.List;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AbsMod.MOD_ID);
    public static final List<DeferredItem<MeowBladeItem>> MEOW_BLADES =
            Arrays.stream(MeowBladeStage.values())
                    .map(stage -> ITEMS.registerItem(stage.itemId(),
                            properties -> new MeowBladeItem(properties, stage)))
                    .toList();
    public static final DeferredItem<MeowBladeItem> MEOW_BLADE = MEOW_BLADES.getFirst();
    public static final List<DeferredItem<MilkshakeItem>> MILKSHAKES =
            Arrays.stream(MilkshakeVariant.values())
                    .map(variant -> ITEMS.registerItem(variant.itemId(),
                            properties -> new MilkshakeItem(properties, variant)))
                    .toList();

    private ModItems() {}
}
