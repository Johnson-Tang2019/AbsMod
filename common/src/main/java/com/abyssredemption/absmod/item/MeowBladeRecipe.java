package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.spirit.BladeComponentTransfer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Seven occupied slots; vanilla crafting consumes one unit from each. */
public final class MeowBladeRecipe extends CustomRecipe {
    public static final MeowBladeRecipe INSTANCE=new MeowBladeRecipe(false);
    public static final RecipeSerializer<MeowBladeRecipe> SERIALIZER=new RecipeSerializer<>(MapCodec.unit(INSTANCE),StreamCodec.unit(INSTANCE));
    public static final MeowBladeRecipe DARK=new MeowBladeRecipe(true);
    public static final RecipeSerializer<MeowBladeRecipe> DARK_SERIALIZER=new RecipeSerializer<>(MapCodec.unit(DARK),StreamCodec.unit(DARK));
    private final boolean dark;
    private MeowBladeRecipe(boolean dark){this.dark=dark;}
    @Override public boolean matches(CraftingInput input,Level level) {
        int sword=0,crystal=0,fish=0,dye=0;
        for(int i=0;i<input.size();i++) {
            var stack=input.getItem(i);if(stack.isEmpty())continue;
            if(stack.is(dark?Items.NETHERITE_SWORD:Items.DIAMOND_SWORD))sword++;
            else if(stack.is(dark?Items.CRYING_OBSIDIAN:Items.AMETHYST_SHARD))crystal++;
            else if(stack.is(Items.COD))fish++;
            else if(stack.is(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(dark?"purple_dye":"pink_dye"))))dye++;
            else return false;
        }
        return sword==1 && crystal==4 && fish==(dark?0:1) && dye==1;
    }
    @Override public ItemStack assemble(CraftingInput input) {
        if(!matches(input,null))return ItemStack.EMPTY;
        for(int i=0;i<input.size();i++)if(input.getItem(i).is(dark?Items.NETHERITE_SWORD:Items.DIAMOND_SWORD))
            return BladeComponentTransfer.transfer(input.getItem(i),BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod",dark?"dark_meow_blade":"meow_blade")));
        return ItemStack.EMPTY;
    }
    @Override public RecipeSerializer<MeowBladeRecipe> getSerializer(){return dark?DARK_SERIALIZER:SERIALIZER;}
}
