package com.abyssredemption.absmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class MeowBladeItem extends Item {
    private final BladeStage stage;

    public MeowBladeItem(Properties properties) {
        this(properties, MeowBladeStage.STAGE_1);
    }

    public MeowBladeItem(Properties properties, BladeStage stage) {
        // Remove the player base and material bonus to obtain the sword-specific bonus.
        super(properties.sword(material(stage),
                stage.attackDamage() - 1.0F - material(stage).attackDamageBonus(), stage.baseAttackSpeed() - 4.0F).durability(stage.durability()));
        this.stage = stage;
    }

    private static ToolMaterial material(BladeStage stage) { return stage.route()==BladeRoute.DARK?ToolMaterial.NETHERITE:ToolMaterial.DIAMOND; }

    public BladeStage stage() {
        return stage;
    }

    @Override
    public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        var result = com.abyssredemption.absmod.spirit.SpiritInteraction.manifest(player, hand);
        return result == net.minecraft.world.InteractionResult.PASS ? super.use(level, player, hand) : result;
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<net.minecraft.network.chat.Component> lines,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(net.minecraft.network.chat.Component.translatable("item.absmod.meow_blade.prototype")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.accept(net.minecraft.network.chat.Component.translatable("item.absmod.meow_blade.manifest_hint")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
