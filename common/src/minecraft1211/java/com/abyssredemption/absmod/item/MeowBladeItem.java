package com.abyssredemption.absmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

public final class MeowBladeItem extends net.minecraft.world.item.SwordItem {
    private final BladeStage stage;

    public MeowBladeItem(Properties properties) {
        this(properties, MeowBladeStage.STAGE_1);
    }

    public MeowBladeItem(Properties properties, BladeStage stage) {
        // Remove the player base and material bonus to obtain the sword-specific bonus.
        super(material(stage), properties.attributes(net.minecraft.world.item.SwordItem.createAttributes(material(stage),
                stage.attackDamage() - 1.0F - material(stage).getAttackDamageBonus(), stage.baseAttackSpeed() - 4.0F)).durability(stage.durability()));
        this.stage = stage;
    }

    private static net.minecraft.world.item.Tier material(BladeStage stage) {
        var base=stage.route()==BladeRoute.DARK?Tiers.NETHERITE:Tiers.DIAMOND;
        return new net.minecraft.world.item.Tier() {
            public int getUses(){return stage.durability();}
            public float getSpeed(){return base.getSpeed();}
            public float getAttackDamageBonus(){return base.getAttackDamageBonus();}
            public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops(){return base.getIncorrectBlocksForDrops();}
            public int getEnchantmentValue(){return base.getEnchantmentValue();}
            public net.minecraft.world.item.crafting.Ingredient getRepairIngredient(){return base.getRepairIngredient();}
        };
    }

    public BladeStage stage() {
        return stage;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        var result = com.abyssredemption.absmod.spirit.SpiritInteraction.manifest(player, hand);
        return result == net.minecraft.world.InteractionResult.PASS ? super.use(level, player, hand) : new net.minecraft.world.InteractionResultHolder<>(result,player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, Item.TooltipContext context,
                                java.util.List<net.minecraft.network.chat.Component> lines,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(net.minecraft.network.chat.Component.translatable("item.absmod.meow_blade.prototype")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(net.minecraft.network.chat.Component.translatable("item.absmod.meow_blade.manifest_hint")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
