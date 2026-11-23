package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.mixin.ArrowPiercingInvoker;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class BlossomBowItem extends BowItem {
    private static final TagKey<Item> BOWS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("absmod", "blossom_bows"));
    private final BlossomWeaponStage stage;

    public BlossomBowItem(Properties properties, BlossomWeaponStage stage) {
        super(properties.durability(768));
        this.stage = stage;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack bow, ItemStack ammo, boolean fullDraw) {
        var projectile = super.createProjectile(level, shooter, bow, ammo, fullDraw);
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() + stage.arrowBonus());
            if (fullDraw && stage.dark() && arrow.getPierceLevel() < 1) ((ArrowPiercingInvoker)arrow).absmod$setPierceLevel((byte)1);
            if (fullDraw && !stage.dark()) arrow.setGlowingTag(true);
        }
        return projectile;
    }

    @Override
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon,
                         List<ItemStack> ammo, float velocity, float inaccuracy, boolean fullDraw, @Nullable LivingEntity target) {
        super.shoot(level, shooter, hand, weapon, ammo, velocity, inaccuracy, fullDraw, target);
        if (stage.dark() && shooter instanceof Player player) {
            level.registryAccess().registryOrThrow(Registries.ITEM).getTagOrEmpty(BOWS)
                    .forEach(item -> player.getCooldowns().addCooldown(item.value(), 10));
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack material) {
        return material.is(Items.CHERRY_PLANKS) || super.isValidRepairItem(stack, material);
    }
}
