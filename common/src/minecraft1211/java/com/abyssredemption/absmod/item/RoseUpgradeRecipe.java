package com.abyssredemption.absmod.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/** Transmute a single base, keeping its existing component patch. */
public record RoseUpgradeRecipe(Item base, Item result) implements SmithingRecipe {
    public static final RecipeSerializer<RoseUpgradeRecipe> SERIALIZER = new RecipeSerializer<>() {
        public MapCodec<RoseUpgradeRecipe> codec() {
            return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i -> i.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("base").forGetter(RoseUpgradeRecipe::base),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(RoseUpgradeRecipe::result)
            ).apply(i, RoseUpgradeRecipe::new));
        }
        public StreamCodec<RegistryFriendlyByteBuf, RoseUpgradeRecipe> streamCodec() {
            var items = net.minecraft.network.codec.ByteBufCodecs.registry(Registries.ITEM);
            return StreamCodec.composite(items, RoseUpgradeRecipe::base, items, RoseUpgradeRecipe::result, RoseUpgradeRecipe::new);
        }
    };
    private static Item known(String path) { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod", path)); }
    public boolean isTemplateIngredient(ItemStack stack) { return stack.is(known("rose_gold_upgrade_template")); }
    public boolean isAdditionIngredient(ItemStack stack) { return stack.is(known("rose_gold_ingot")); }
    public boolean isBaseIngredient(ItemStack stack) { return stack.is(base); }

    public boolean matches(SmithingRecipeInput in, Level level) {
        if (!isTemplateIngredient(in.template()) || !isAdditionIngredient(in.addition()) || !isBaseIngredient(in.base())) return false;
        if (base == result) return !CookeryFinish.isRose(in.base());
        return !(result instanceof net.minecraft.world.item.TieredItem) ||
                in.base().getEnchantments().getLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)) == 0;
    }

    public ItemStack assemble(SmithingRecipeInput in, HolderLookup.Provider registries) {
        var out = in.base().transmuteCopy(result, 1);
        if (base == result) return CookeryFinish.apply(out);
        if (result instanceof net.minecraft.world.item.TieredItem)
            out.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        return out;
    }
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return assemble(new SmithingRecipeInput(ItemStack.EMPTY, new ItemStack(base), ItemStack.EMPTY), registries);
    }
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
}
