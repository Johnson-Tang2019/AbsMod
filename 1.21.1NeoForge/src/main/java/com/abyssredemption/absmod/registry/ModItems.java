package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.item.BladeRoute;
import com.abyssredemption.absmod.item.BlossomBowItem;
import com.abyssredemption.absmod.item.BlossomStaffItem;
import com.abyssredemption.absmod.item.BlossomWeaponStage;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.MeowBladeStage;
import com.abyssredemption.absmod.item.MilkshakeItem;
import com.abyssredemption.absmod.item.MilkshakeVariant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AbsMod.MOD_ID);
    public static final List<DeferredItem<BlossomBowItem>> BLOSSOM_BOWS;
    public static final List<DeferredItem<BlossomStaffItem>> BLOSSOM_STAVES;
    static {
        var bows = new ArrayList<DeferredItem<BlossomBowItem>>();
        var staves = new ArrayList<DeferredItem<BlossomStaffItem>>();
        for (BladeRoute route : BladeRoute.values()) {
            for (int tier = 1; route.accepts(tier); tier++) {
                var stage = new BlossomWeaponStage(route, tier);
                bows.add(ITEMS.registerItem(stage.itemId("bow"), properties -> new BlossomBowItem(properties, stage)));
                staves.add(ITEMS.registerItem(stage.itemId("staff"), properties -> new BlossomStaffItem(properties, stage)));
            }
        }
        BLOSSOM_BOWS = List.copyOf(bows);
        BLOSSOM_STAVES = List.copyOf(staves);
    }
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
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CAT_NEST =
            ITEMS.registerItem("cat_nest", properties -> new net.minecraft.world.item.BlockItem(ModBlocks.CAT_NEST.get(), properties));

    public static final DeferredItem<com.abyssredemption.absmod.item.FieldGuideItem> FIELD_GUIDE=ITEMS.registerItem("field_guide",com.abyssredemption.absmod.item.FieldGuideItem::new);
    public static final DeferredItem<com.abyssredemption.absmod.item.GentleBrushItem> GENTLE_BRUSH =
            ITEMS.registerItem("gentle_brush", com.abyssredemption.absmod.item.GentleBrushItem::new);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRAY = ITEMS.registerSimpleBlockItem(ModBlocks.MILKSHAKE_TRAY);
    static {
        for (var style : com.abyssredemption.absmod.spirit.SpiritAdornment.items())
            ITEMS.registerSimpleItem(style.getSerializedName(), new net.minecraft.world.item.Item.Properties().stacksTo(1));
    }
    private ModItems() {}
}
