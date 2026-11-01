package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.AbsMod;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** Shared title, icon and stable item ordering for both loader registrations. */
public final class ModCreativeTab {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(AbsMod.MOD_ID, "main");

    private ModCreativeTab() {}

    public static CreativeModeTab create(CreativeModeTab.Builder builder,
            Function<MeowBladeStage, Item> blades, Function<MilkshakeVariant, Item> milkshakes) {
        return builder.title(Component.translatable("itemGroup.absmod"))
                .icon(() -> blades.apply(MeowBladeStage.STAGE_1).getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(AbsMod.MOD_ID,"resonance_heart")));
                    output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("absmod","field_guide")));
                    for (var stage : MeowBladeStage.values()) { output.accept(blades.apply(stage)); }
                    for(var stage:DarkBladeStage.values())output.accept(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("absmod",stage.itemId())));
                    for (var variant : MilkshakeVariant.values()) { output.accept(milkshakes.apply(variant)); }
                }).build();
    }
}
