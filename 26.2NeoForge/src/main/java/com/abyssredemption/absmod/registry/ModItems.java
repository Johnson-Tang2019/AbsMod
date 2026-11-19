package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import net.minecraft.world.item.Item;
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
    public static final List<DeferredItem<MeowBladeItem>> DARK_BLADES = Arrays.stream(com.abyssredemption.absmod.item.DarkBladeStage.values()).map(stage->ITEMS.registerItem(stage.itemId(),properties->new MeowBladeItem(properties,stage))).toList();
    public static final DeferredItem<MeowBladeItem> MEOW_BLADE = MEOW_BLADES.getFirst();
    public static final List<DeferredItem<com.abyssredemption.absmod.item.SpectralBladeItem>> SPECTRAL_BLADES =
            java.util.stream.Stream.concat(Arrays.stream(MeowBladeStage.values()),Arrays.stream(com.abyssredemption.absmod.item.DarkBladeStage.values())).map(stage -> ITEMS.registerItem("spectral_" + stage.itemId(),
                    com.abyssredemption.absmod.item.SpectralBladeItem::new)).toList();
    public static final List<DeferredItem<MilkshakeItem>> MILKSHAKES =
            Arrays.stream(MilkshakeVariant.values())
                    .map(variant -> ITEMS.registerItem(variant.itemId(),
                            properties -> new MilkshakeItem(properties, variant)))
                    .toList();

    public static final DeferredItem<com.abyssredemption.absmod.item.ResonanceHeartItem> RESONANCE_HEART = ITEMS.registerItem("resonance_heart",com.abyssredemption.absmod.item.ResonanceHeartItem::new);
    public static final DeferredItem<com.abyssredemption.absmod.item.BondBellItem> BOND_BELL =
            ITEMS.registerItem("bond_bell", com.abyssredemption.absmod.item.BondBellItem::new);
    public static final DeferredItem<com.abyssredemption.absmod.item.GentleBrushItem> GENTLE_BRUSH =
            ITEMS.registerItem("gentle_brush", com.abyssredemption.absmod.item.GentleBrushItem::new);
    public static final java.util.List<DeferredItem<Item>> SPIRIT_ADORNMENTS =
            com.abyssredemption.absmod.spirit.SpiritAdornment.items().stream()
                    .map(style -> ITEMS.registerItem(style.getSerializedName(), properties -> new Item(properties.stacksTo(1))))
                    .toList();
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CAT_NEST =
            ITEMS.registerItem("cat_nest", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.CAT_NEST.get(), properties));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MILKSHAKE_TRAY =
            ITEMS.registerItem("milkshake_tray", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.MILKSHAKE_TRAY.get(), properties));

    public static final DeferredItem<com.abyssredemption.absmod.item.FieldGuideItem> FIELD_GUIDE=ITEMS.registerItem("field_guide",com.abyssredemption.absmod.item.FieldGuideItem::new);
    private ModItems() {}
}
