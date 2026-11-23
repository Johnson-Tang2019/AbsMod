package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.item.BlossomBowItem;
import com.abyssredemption.absmod.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Runs only in the opt-in copy of the old release's world. */
public final class BlossomWeaponVerification {
    private static final List<Husk> targets = new ArrayList<>();
    private static final List<Husk> arrowTargets = new ArrayList<>();
    private static Vec3 returnPosition;
    private static float returnPitch, returnYaw;
    private static GameType gameMode;
    private static ItemStack mainHand, offHand;
    private static BlockPos floor;

    private static void check(boolean condition, String detail) {
        if (!condition) throw new IllegalStateException("Blossom verification: " + detail);
    }

    private static Item weapon(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("absmod", id)).orElseThrow();
    }

    private static void verifyRecipes(ServerPlayer p) {
        var level = p.serverLevel();
        var normal = List.of(Items.COPPER_INGOT, Items.AMETHYST_BLOCK, Items.GOLD_INGOT, Items.DIAMOND,
                Items.BLAZE_ROD, Items.ECHO_SHARD, Items.NETHERITE_INGOT, Items.NETHER_STAR);
        var dark = List.of(Items.OBSIDIAN, Items.MAGMA_CREAM, Items.WITHER_SKELETON_SKULL, Items.SCULK_CATALYST,
                Items.ANCIENT_DEBRIS, Items.NETHERITE_INGOT, Items.NETHER_STAR);
        var unbreaking = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
        for (String kind : List.of("bow", "staff")) {
            var entry = CraftingInput.of(2, 2, List.of((kind.equals("bow") ? Items.BOW : Items.STICK).getDefaultInstance(),
                    Items.CHERRY_PLANKS.getDefaultInstance(), Items.PINK_PETALS.getDefaultInstance(), Items.AMETHYST_SHARD.getDefaultInstance()));
            var plain = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, entry, level).orElseThrow().value()
                    .assemble(entry, level.registryAccess());
            check(plain.is(weapon("blossom_" + kind)), "entry recipe " + kind);
            plain.set(DataComponents.CUSTOM_NAME, Component.literal("Blossom journey"));
            plain.setDamageValue(137);
            plain.enchant(unbreaking, 2);
            for (boolean shadow : List.of(false, true)) {
                var current = plain.copy();
                String family = (shadow ? "dark_" : "") + "blossom_" + kind;
                for (int n = shadow ? 1 : 2; n <= (shadow ? 8 : 9); n++) {
                    var prefix = n == 1 ? Items.ECHO_SHARD : Items.PINK_PETALS;
                    var material = n == 1 ? Items.CRYING_OBSIDIAN : (shadow ? dark : normal).get(n - 2);
                    var smith = new SmithingRecipeInput(prefix.getDefaultInstance(), current, material.getDefaultInstance());
                    current = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, smith, level).orElseThrow().value()
                            .assemble(smith, level.registryAccess());
                    check(current.is(weapon(family + (n == 1 ? "" : "_stage_" + n))) && current.getDamageValue() == 137
                            && current.getHoverName().equals(plain.getHoverName()) && current.getEnchantments().getLevel(unbreaking) == 2,
                            family + " " + n + " identity and components");
                }
                check(level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, new SmithingRecipeInput(
                        Items.PINK_PETALS.getDefaultInstance(), current, Items.NETHER_STAR.getDefaultInstance()), level).isEmpty(),
                        family + " cap");
            }
        }
    }

    private static Husk target(ServerLevel level, int x, int z) {
        var husk = new Husk(EntityType.HUSK, level);
        husk.setNoAi(true);
        husk.setNoGravity(true);
        husk.moveTo(floor.getX() + x + 0.5, floor.getY() + 1, floor.getZ() + z + 0.5);
        check(level.addFreshEntity(husk), "spawn test target");
        targets.add(husk);
        return husk;
    }

    private static void stand(ServerPlayer p, int x) {
        p.teleportTo(p.serverLevel(), floor.getX() + x + 0.5, floor.getY() + 1, floor.getZ() + 0.5, Set.of(), 0, 0);
    }

    private static void cast(ServerPlayer p, String id) {
        p.setItemInHand(InteractionHand.MAIN_HAND, weapon(id).getDefaultInstance());
        check(p.getMainHandItem().use(p.serverLevel(), p, InteractionHand.MAIN_HAND).getResult().consumesAction(), id + " cast");
        check(p.getMainHandItem().getDamageValue() == 1, id + " durability");
        for (var staff : ModItems.BLOSSOM_STAVES) {
            check(p.getCooldowns().isOnCooldown(staff.get()), id + " family cooldown");
            p.getCooldowns().removeCooldown(staff.get());
        }
    }

    private static void shoot(ServerPlayer p, boolean shadow) {
        var bow = (BlossomBowItem)weapon(shadow ? "dark_blossom_bow_stage_8" : "blossom_bow");
        var stack = bow.getDefaultInstance();
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 3));
        check(bow.use(p.serverLevel(), p, InteractionHand.MAIN_HAND).getResult().consumesAction(), "nock");
        bow.releaseUsing(stack, p.level(), p, stack.getUseDuration(p) - 20);
        p.stopUsingItem();
        check(stack.getDamageValue() == 1 && p.getOffhandItem().getCount() == 2, "one shot and one arrow");
        var shots = p.serverLevel().getEntitiesOfClass(AbstractArrow.class, p.getBoundingBox().inflate(2),
                arrow -> p.equals(arrow.getOwner()));
        check(shots.size() == 1 && (shadow ? shots.getFirst().getPierceLevel() == 1 : shots.getFirst().isCurrentlyGlowing()),
                "full draw special");
        check(p.getCooldowns().isOnCooldown(bow) == shadow, "bow recovery");
    }

    public static void verify(ServerPlayer p) {
        verifyRecipes(p);
        var level = p.serverLevel();
        returnPosition = p.position();
        returnPitch = p.getXRot();
        returnYaw = p.getYRot();
        gameMode = p.gameMode.getGameModeForPlayer();
        mainHand = p.getMainHandItem();
        offHand = p.getOffhandItem();
        p.setGameMode(GameType.SURVIVAL);
        floor = new BlockPos(p.blockPosition().getX(), level.getMaxBuildHeight() - 12, p.blockPosition().getZ());
        for (int lane : List.of(0, 6, 12, 18)) {
            var pos = floor.east(lane);
            check(level.getBlockState(pos).is(Blocks.AIR), "fixture air at elevated lane");
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        stand(p, 0);
        var single = target(level, 0, 5);
        var beside = target(level, 1, 5);
        cast(p, "blossom_staff");
        check(single.getHealth() < single.getMaxHealth() && beside.getHealth() == beside.getMaxHealth(), "focused spell");
        stand(p, 6);
        var bloom = target(level, 6, 5);
        var nearby = target(level, 7, 5);
        var blocked = floor.offset(6, 2, 3);
        check(level.getBlockState(blocked).is(Blocks.AIR), "wall fixture air");
        level.setBlockAndUpdate(blocked, Blocks.STONE.defaultBlockState());
        cast(p, "dark_blossom_staff_stage_8");
        check(bloom.getHealth() == bloom.getMaxHealth() && nearby.getHealth() == nearby.getMaxHealth(), "magic blocked by wall");
        level.removeBlock(blocked, false);
        cast(p, "dark_blossom_staff_stage_8");
        check(bloom.getHealth() < bloom.getMaxHealth() && nearby.getHealth() < nearby.getMaxHealth(), "dark blossom splash");
        stand(p, 12);
        arrowTargets.add(target(level, 12, 4));
        shoot(p, false);
        stand(p, 18);
        arrowTargets.add(target(level, 18, 4));
        arrowTargets.add(target(level, 18, 7));
        shoot(p, true);
        System.out.println("BLOSSOM_RECIPES_AND_CASTS_PASS");
    }

    public static void verifyImpact(ServerPlayer p) {
        check(arrowTargets.size() == 3 && arrowTargets.stream().allMatch(mob -> mob.getHealth() < mob.getMaxHealth()),
                "one ordinary impact and two piercing impacts");
        targets.forEach(Husk::discard);
        targets.clear();
        arrowTargets.clear();
        for (int lane : List.of(0, 6, 12, 18)) p.serverLevel().removeBlock(floor.east(lane), false);
        p.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        p.setItemInHand(InteractionHand.OFF_HAND, offHand);
        p.setGameMode(gameMode);
        p.teleportTo(p.serverLevel(), returnPosition.x, returnPosition.y, returnPosition.z, Set.of(), returnYaw, returnPitch);
        System.out.println("BLOSSOM_NORMAL_AND_PIERCING_ARROW_IMPACTS_PASS");
    }
}
