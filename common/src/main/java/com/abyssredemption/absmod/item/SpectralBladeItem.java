package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import java.util.UUID;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An inert link to a manifested spirit, never a second usable sword. */
public final class SpectralBladeItem extends Item {
    public SpectralBladeItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<net.minecraft.network.chat.Component> lines,
            net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(net.minecraft.network.chat.Component.translatable("item.absmod.spectral_blade.hint")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }

    public static boolean represents(ItemStack stack, UUID soul) {
        var reference = stack.get(BladeSoulComponents.REFERENCE);
        return stack.getItem() instanceof SpectralBladeItem && reference != null && reference.soulId().equals(soul);
    }

    public static ItemStack create(MeowBladeStage stage, ItemStack original) {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("absmod", "spectral_" + stage.itemId()));
        if (!(item instanceof SpectralBladeItem)) { throw new IllegalStateException("missing_projection"); }
        var result = new ItemStack(item);
        result.set(BladeSoulComponents.REFERENCE, original.get(BladeSoulComponents.REFERENCE));
        return result;
    }
}
