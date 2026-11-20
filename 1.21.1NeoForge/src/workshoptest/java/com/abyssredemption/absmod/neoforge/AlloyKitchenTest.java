package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.item.CookeryFinish;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ChoppingBoardBlockEntity;

final class AlloyKitchenTest {
    private static final String COOK = "kaleidoscope_cookery:";
    private static final TagKey<Item> UTENSILS = TagKey.create(Registries.ITEM,id("absmod:cookery_utensils"));
    private static ResourceLocation id(String path) { return ResourceLocation.parse(path); }
    private static ItemStack item(String path) {return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));}
    private static void check(boolean ok,String label) { if(!ok) throw new IllegalStateException("Rose gold test: "+label); }
    private static ItemStack smith(ServerPlayer p, ItemStack base, boolean take) {
        var menu=new SmithingMenu(12,p.getInventory());
        menu.getSlot(0).set(item("absmod:rose_gold_upgrade_template"));
        menu.getSlot(1).set(base.copyWithCount(1));
        menu.getSlot(2).set(item("absmod:rose_gold_ingot"));
        menu.createResult();
        var result=menu.getSlot(3).getItem().copy();
        if(take && !result.isEmpty()) {
            menu.getSlot(3).onTake(p,result);
            check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty() && menu.getSlot(2).getItem().isEmpty(),"smithing payment");
        }
        return result;
    }
    private static void use(ServerPlayer p, BlockPos pos) {
        p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0,.49,0),Direction.UP,pos,false));
    }

    static void exercise(ServerPlayer p) {
        var level=p.serverLevel();var access=level.registryAccess();
        var input=CraftingInput.of(3,3,List.of(new ItemStack(Items.GOLD_INGOT),new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.COPPER_INGOT),item("absmod:paw_crystal"),ItemStack.EMPTY,ItemStack.EMPTY));
        var alloy=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input,access);
        check(alloy.is(item("absmod:rose_gold_ingot").getItem()) && alloy.getCount()==2,"alloy ingredients");
        p.setItemInHand(InteractionHand.MAIN_HAND, alloy);
        p.inventoryMenu.broadcastChanges();
        var template = item("absmod:rose_gold_upgrade_template");
        var first = CraftingInput.of(2, 2, List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.COPPER_BLOCK),
                item("absmod:paw_crystal"), new ItemStack(Items.PINK_DYE)));
        check(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, first, level).orElseThrow().value().assemble(first, access).is(template.getItem()), "first template");
        var gold = new ItemStack(Items.GOLD_INGOT);
        var copy = CraftingInput.of(3, 3, List.of(gold, template, gold, gold, new ItemStack(Items.COPPER_BLOCK), gold, gold, gold, gold));
        var copies = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, copy, level).orElseThrow().value().assemble(copy, access);
        check(copies.is(template.getItem()) && copies.getCount() == 2, "duplicate template");
        var enchants=access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack pick=ItemStack.EMPTY;
        for(String part:List.of("pickaxe","axe","shovel","hoe","sword","helmet","chestplate","leggings","boots")) {
            var base=item("minecraft:diamond_"+part);
            base.setDamageValue(29);
            base.set(DataComponents.CUSTOM_NAME,Component.literal("打磨试样"));
            base.enchant(enchants.getOrThrow(Enchantments.UNBREAKING),2);
            var out=smith(p,base,true);
            check(out.is(item("absmod:rose_gold_"+part).getItem()) && out.getDamageValue()==29 &&
                    out.getHoverName().getString().equals("打磨试样") && out.getEnchantmentLevel(enchants.getOrThrow(Enchantments.UNBREAKING))==2,"gear "+part);
            check(out.getItem().isValidRepairItem(out,item("absmod:rose_gold_ingot")),"repair "+part);
            if (part.equals("pickaxe")) pick=out.copy();
            if (part.equals("chestplate") || part.equals("pickaxe")) p.getInventory().add(out);
        }
        var fortune=new ItemStack(Items.DIAMOND_PICKAXE);
        fortune.enchant(enchants.getOrThrow(Enchantments.FORTUNE),2);
        check(smith(p,fortune,false).isEmpty() && fortune.getEnchantments().getLevel(enchants.getOrThrow(Enchantments.FORTUNE))==2,"fortune protection");
        var at=new BlockPos(12,91,0);
        level.setBlockAndUpdate(at,Blocks.GLASS.defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,pick);
        check(p.gameMode.destroyBlock(at),"break glass");
        check(!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(at).inflate(1),
                e->e.getItem().is(Items.GLASS)).isEmpty(),"silk glass drop");
        var fresh=item("absmod:rose_gold_pickaxe");
        check(fresh.getEnchantmentLevel(enchants.getOrThrow(Enchantments.SILK_TOUCH))==1,"intrinsic silk");

        var wheel = new net.minecraft.world.inventory.GrindstoneMenu(13, p.getInventory());
        wheel.getSlot(0).set(pick.copy());
        var polished = wheel.getSlot(2).getItem().copy();
        var silk = enchants.getOrThrow(Enchantments.SILK_TOUCH);
        var luck = enchants.getOrThrow(Enchantments.FORTUNE);
        check(polished.is(pick.getItem()) && polished.getEnchantments().getLevel(silk) == 0
                && polished.getEnchantmentLevel(silk) == 1, "ground tool retains intrinsic silk");
        check(!polished.isPrimaryItemFor(luck) && !polished.supportsEnchantment(luck), "Fortune blocked after grinding");
        var anvil = new net.minecraft.world.inventory.AnvilMenu(14, p.getInventory());
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(luck, 3);
        anvil.getSlot(0).set(polished);
        anvil.getSlot(1).set(book);
        anvil.createResult();
        check(anvil.getSlot(2).getItem().isEmpty(), "grinding cannot allow an incompatible Fortune book");

        var patterned=item("absmod:rose_pickaxe");
        var efficiency=enchants.getOrThrow(Enchantments.EFFICIENCY);
        patterned.enchant(efficiency,5);
        check(patterned.getEnchantmentLevel(efficiency)==6 && patterned.getEnchantments().getLevel(efficiency)==5,"Efficiency VI");
        double[] speed={0};
        net.minecraft.world.item.enchantment.EnchantmentHelper.forEachModifier(patterned,net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                (attribute,modifier)->{if(attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY))speed[0]+=modifier.amount();});
        check(speed[0]==37,"Efficiency VI mining attribute "+speed[0]);
        p.getInventory().add(patterned.copy());
        var patternedArmor=item("absmod:rose_chestplate");
        patternedArmor.enchant(enchants.getOrThrow(Enchantments.PROTECTION),4);
        p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,patternedArmor);
        check(net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageProtection(level,p,p.damageSources().generic())==5,"Protection V damage calculation");
        check(item("absmod:rose_axe").getAllEnchantments(enchants).isEmpty(),"empty patterned tool");
        var vanilla=new ItemStack(Items.DIAMOND_PICKAXE);
        vanilla.enchant(efficiency,5);
        check(vanilla.getEnchantmentLevel(efficiency)==5 && item("absmod:rose_gold_pickaxe").getEnchantmentLevel(efficiency)==0,"enchantment scope");
        var vanillaLines=patterned.getTooltipLines(Item.TooltipContext.of(level),p,TooltipFlag.Default.NORMAL);
        check(vanillaLines.stream().anyMatch(line->line.equals(net.minecraft.world.item.enchantment.Enchantment.getFullname(efficiency,6))),"effective tooltip: " + vanillaLines);
        wheel.getSlot(0).set(patterned.copy());
        var cleared = wheel.getSlot(2).getItem();
        check(cleared.is(patterned.getItem()) && cleared.getEnchantmentLevel(efficiency) == 0, "ground pattern has no phantom level");
        var utensils=BuiltInRegistries.ITEM.getTag(UTENSILS).orElseThrow();
        check(utensils.size()==33,"utensil manifest "+utensils.size());
        for(var entry:utensils) {
            var base=new ItemStack(entry);if(base.is(item(COOK+"oil_pot").getItem())) OilPotItem.setOilCount(base,6);
            var out=smith(p,base,false);
            check(out.is(base.getItem()) && CookeryFinish.isRose(out) && smith(p,out,false).isEmpty(),"finish "+entry);
            if(base.is(item(COOK+"oil_pot").getItem())) check(OilPotItem.getOilCount(out)==6,"stored oil");
        }
        var board=smith(p,item(COOK+"chopping_board"),true);
        p.getInventory().add(board.copy());
        for(int x=15;x<=26;x++)for(int z=-8;z<=3;z++)level.setBlockAndUpdate(new BlockPos(x,89,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        var basePos=new BlockPos(21,90,0);
        p.teleportTo(level,21.5,90,-3.5,java.util.Set.of(),0,14);
        String[] stations={"stove","pot","chopping_board","steamer","teapot","millstone","stockpot","trash_can"};
        for(int i=0;i<stations.length;i++) {
            var pos=stations[i].equals("millstone") ? basePos.offset(4,0,2) : basePos.offset(i-4,0,0);
            p.setItemInHand(InteractionHand.MAIN_HAND,smith(p,item(COOK+stations[i]),false));
            p.setShiftKeyDown(true);
            use(p,pos.below());
            p.setShiftKeyDown(false);
            check(CookeryFinish.isRose(level.getBlockState(pos)), "placed "+stations[i]);
        }
        var stove = basePos.west(4);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        use(p, stove);
        check(level.getBlockState(stove).getValue(BlockStateProperties.LIT) && CookeryFinish.isRose(level.getBlockState(stove)), "lit pink stove");
        var stockpotPos = basePos.east(2);
        var stockpot = (com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity) level.getBlockEntity(stockpotPos);
        p.setItemInHand(InteractionHand.MAIN_HAND, smith(p, item(COOK + "stockpot_lid"), false));
        use(p, stockpotPos);
        check(stockpot.hasLid() && CookeryFinish.isRose(stockpot.getLidItem()), "stockpot accepts pink lid");
        use(p, stockpotPos);
        check(!stockpot.hasLid() && CookeryFinish.isRose(p.getMainHandItem()), "stockpot returns pink lid");
        var hit = new BlockHitResult(Vec3.atCenterOf(stove), Direction.UP, stove, false);
        check(CookeryFinish.isRose(level.getBlockState(stove).getCloneItemStack(hit, level, stove, p)), "pick pink stove");
        var chop=basePos.west(2);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COD));use(p,chop);
        var be=(ChoppingBoardBlockEntity)level.getBlockEntity(chop);
        check(!be.getCurrentCutStack().isEmpty(),"loaded board");
        p.setItemInHand(InteractionHand.MAIN_HAND,smith(p,item(COOK+"iron_kitchen_knife"),false));
        for(int i=0;i<18 && !be.getCurrentCutStack().isEmpty();i++)use(p,chop);
        check(be.getCurrentCutStack().isEmpty(),"rose knife sliced cod");
        var tea=smith(p,item(COOK+"barley_tea"),false);
        var returned=tea.finishUsingItem(level,p);
        check(returned.is(item(COOK+"empty_cup").getItem()) && CookeryFinish.isRose(returned),"drinking returns finished cup");
        var cupPos=basePos.offset(0,0,2);
        p.setItemInHand(InteractionHand.MAIN_HAND,returned);use(p,cupPos.below());
        p.setItemInHand(InteractionHand.MAIN_HAND,item(COOK+"empty_cup"));use(p,cupPos);
        check(p.getMainHandItem().getCount()==1,"mixed cup finishes");
        p.setItemInHand(InteractionHand.MAIN_HAND,smith(p,item(COOK+"barley_tea"),false));
        use(p,cupPos);
        check(CookeryFinish.isRose(level.getBlockState(cupPos)) && level.getBlockState(cupPos).getBlock().asItem()==item(COOK+"barley_tea").getItem(),"filled tea state");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);use(p,cupPos);
        check(CookeryFinish.isRose(level.getBlockState(cupPos)) && level.getBlockState(cupPos).getBlock().asItem()==item(COOK+"empty_cup").getItem(),"empty tea state");
        var plain=basePos.offset(-4,0,1);
        p.setItemInHand(InteractionHand.MAIN_HAND,item(COOK+"stove"));use(p,plain.below());
        check(!CookeryFinish.isRose(level.getBlockState(plain)),"plain stove");
        var sample=basePos.offset(0,0,-2);
        p.setItemInHand(InteractionHand.MAIN_HAND,board.copy());use(p,sample.below());
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.gameMode.destroyBlock(sample);
        check(!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(sample).inflate(1),
                e->CookeryFinish.isRose(e.getItem())).isEmpty(),"finish survives breaking");
        p.teleportTo(level,21,90,-5.2,java.util.Set.of(),0,12);
        p.setItemInHand(InteractionHand.MAIN_HAND,pick);
    }

    static void checkModels(Minecraft mc) {
        var manager=mc.getItemRenderer().getItemModelShaper().getModelManager();
        for(var entry:BuiltInRegistries.ITEM.getTag(UTENSILS).orElseThrow()) {
            var stack=new ItemStack(entry);
            var plain=mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);
            var rose=mc.getItemRenderer().getModel(CookeryFinish.apply(stack),mc.level,mc.player,0);
            check(plain!=rose && rose!=manager.getMissingModel(),"item model "+entry);
            if (stack.getItem() instanceof BlockItem blockItem) {
                var state=blockItem.getBlock().defaultBlockState();
                check(state.hasProperty(CookeryFinish.ROSE) && !CookeryFinish.isRose(state),"finish states "+entry);
                var baked=mc.getBlockRenderer().getBlockModel(state.setValue(CookeryFinish.ROSE,true));
                check(baked!=manager.getMissingModel(), "placed model "+entry);
            }
        }
    }
    static void quests(ServerPlayer p) {
        var ftb=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
        var team=ftb.getTeamData(p).orElseThrow();
        for(long quest:new long[]{0x79667abb4706871aL,0x47bd3124d2193a99L,0x168f83fdf865767dL,0x6c33725dcf2bbf5L,0x3aa11e663bd2f070L})check(team.isCompleted(ftb.getQuest(quest)),"FTB "+quest);
        com.mojang.logging.LogUtils.getLogger().info("ROSE_ALLOY_CLIENT_PASS 9 gear upgrades, glass drop, 33 finishes and models, board cutting, 5 new quests");
    }
}
