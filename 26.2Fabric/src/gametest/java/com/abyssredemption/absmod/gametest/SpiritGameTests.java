package com.abyssredemption.absmod.gametest;

import com.abyssredemption.absmod.spirit.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public final class SpiritGameTests {
    @GameTest
    public void catGenomeUsesBothParentsAndPersists(GameTestHelper helper) {
        var first=helper.spawn(net.minecraft.world.entity.EntityTypes.CAT,1,1,1);
        var second=helper.spawn(net.minecraft.world.entity.EntityTypes.CAT,3,1,1);
        var dense=new com.abyssredemption.absmod.cat.CatGenome(0,"minecraft:all_black",java.util.Optional.empty(),java.util.Optional.empty(),0);
        var dilute=new com.abyssredemption.absmod.cat.CatGenome(3,"minecraft:all_black",java.util.Optional.empty(),java.util.Optional.empty(),0);
        ((com.abyssredemption.absmod.cat.CatGenetics)first).absmod$setGenome(dense);
        ((com.abyssredemption.absmod.cat.CatGenetics)second).absmod$setGenome(dilute);
        helper.assertTrue(dense.diluteChancePercent(dilute)==0,"DD x dd prediction wrong");
        var child=first.getBreedOffspring(helper.getLevel(),second);
        var inherited=((com.abyssredemption.absmod.cat.CatGenetics)child).absmod$getGenome();
        helper.assertTrue(inherited.notation().equals("Dd") && inherited.firstParent().orElseThrow().equals(first.getUUID())
            && inherited.secondParent().orElseThrow().equals(second.getUUID()),"Offspring did not inherit both parents");
        helper.assertTrue(inherited.diluteChancePercent(inherited)==25,"Carrier prediction wrong");
        var encoded=com.abyssredemption.absmod.cat.CatGenome.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,inherited).getOrThrow();
        helper.assertTrue(com.abyssredemption.absmod.cat.CatGenome.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow().equals(inherited),"Genome persistence lost lineage");
        var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,helper.getLevel().registryAccess());
        child.saveWithoutId(output);
        var restored=net.minecraft.world.entity.EntityTypes.CAT.create(helper.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);
        restored.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,helper.getLevel().registryAccess(),output.buildResult()));
        helper.assertTrue(((com.abyssredemption.absmod.cat.CatGenetics)restored).absmod$getGenome().equals(inherited),"Entity save/load lost cat genome or parents");
        int diluteCount=0;var random=net.minecraft.util.RandomSource.create(42);
        for(int i=0;i<10000;i++)if(inherited.breed(inherited,first.getUUID(),second.getUUID(),random).dilute())diluteCount++;
        helper.assertTrue(diluteCount>2300 && diluteCount<2700,"Carrier inheritance distribution wrong: "+diluteCount);
        helper.assertTrue(second.getVariant().unwrapKey().orElseThrow().identifier().toString().equals("minecraft:british_shorthair"),"Black dilution not visible");
        helper.succeed();
    }
    @GameTest
    public void repairPreservesSoulAndRefusesSacrificingAnotherBlade(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        sword.setDamageValue(1000);
        var record=BladeSoulService.initialize(BladeSoulStore.forInitialization(player.level().getServer()),sword,player.getUUID());
        var anvil=new net.minecraft.world.inventory.AnvilMenu(1,player.getInventory());
        anvil.getSlot(0).set(sword.copy());anvil.getSlot(1).set(new ItemStack(Items.DIAMOND));
        var repaired=anvil.getSlot(2).getItem();
        helper.assertTrue(!repaired.isEmpty() && repaired.getDamageValue()==610
            && repaired.get(BladeSoulComponents.REFERENCE).soulId().equals(record.soulId()),"Diamond repair lost identity or quarter repair");
        anvil.getSlot(1).set(sword.copy());
        helper.assertTrue(anvil.getSlot(2).getItem().isEmpty(),"Anvil sacrificed second blade");
        var grindstone=new net.minecraft.world.inventory.GrindstoneMenu(2,player.getInventory());
        grindstone.getSlot(0).set(sword.copy());grindstone.getSlot(1).set(sword.copy());
        helper.assertTrue(grindstone.getSlot(2).getItem().isEmpty(),"Grindstone sacrificed second blade");
        helper.succeed();
    }
    @GameTest
    public void darkCraftUpgradeAndSpiritKeepTheirRoute(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var original=new ItemStack(Items.NETHERITE_SWORD);original.setDamageValue(501);
        var ingredients=new java.util.ArrayList<ItemStack>();ingredients.add(original);
        for(int i=0;i<4;i++)ingredients.add(new ItemStack(Items.CRYING_OBSIDIAN));
        ingredients.add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("purple_dye"))));
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,2,ingredients);
        var recipe=com.abyssredemption.absmod.item.MeowBladeRecipe.DARK;
        helper.assertTrue(recipe.matches(input,helper.getLevel()),"Dark recipe missing");
        var sword=recipe.assemble(input);
        var store=BladeSoulStore.forInitialization(player.level().getServer());var record=BladeSoulService.initialize(store,sword,player.getUUID());
        helper.assertTrue(record.route()==com.abyssredemption.absmod.item.BladeRoute.DARK,"Dark soul used normal route");
        BladeSoulService.setTestAffection(store,sword,100);
        for(int target=2;target<=8;target++) {
            var menu=new ResonanceMenu(target,player.getInventory());menu.getSlot(0).set(sword);
            int slot=1;for(var cost:ResonanceMenu.costs(record.route(),target))menu.getSlot(slot++).set(new ItemStack(cost.item(),cost.count()));
            menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);sword=menu.getSlot(0).getItem();
            helper.assertTrue(store.find(record.soulId()).orElseThrow().stage()==target && sword.getDamageValue()==501,"Dark migration changed durability or failed");
        }
        var stage=((com.abyssredemption.absmod.item.MeowBladeItem)sword.getItem()).stage();
        helper.assertTrue(stage.maxComboStacks()==0 && stage.attackDamage()==50,"Dark route inherited normal combos");
        var pos=helper.absolutePos(new net.minecraft.core.BlockPos(1,2,1));player.setPos(pos.getX(),pos.getY(),pos.getZ());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
        var spirit=SpiritTransformationService.manifest(player);
        helper.assertTrue(player.getMainHandItem().is(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","spectral_dark_meow_blade_stage_8"))),"Dark projection missing");
        helper.assertTrue(SpiritTransformationService.recallForAttack(player) && ItemStack.matches(sword,player.getMainHandItem()),"Dark recall lost sword");
        helper.succeed();
    }
    @GameTest
    public void everyNormalUpgradePreservesEnchantmentsAndLocksFinalTrial(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var stranger=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        var enchantment=helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        sword.enchant(enchantment,3);
        var store=BladeSoulStore.forInitialization(player.level().getServer());
        var before=BladeSoulService.initialize(store,sword,player.getUUID());
        for(int target=2;target<=8;target++) {
            var menu=new ResonanceMenu(target,player.getInventory());menu.getSlot(0).set(sword);
            int slot=1;for(var cost:ResonanceMenu.costs(target))menu.getSlot(slot++).set(new ItemStack(cost.item(),cost.count()));
            menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,stranger);
            helper.assertTrue(menu.getSlot(0).getItem()==sword,"Stranger upgraded owned sword");
            BladeSoulService.setTestAffection(store,sword,0);
            menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            helper.assertTrue(menu.getSlot(0).getItem()==sword && !menu.getSlot(1).getItem().isEmpty(),"Affection refusal changed escrow");
            BladeSoulService.setTestAffection(store,sword,300);
            menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            sword=menu.getSlot(0).getItem();
            helper.assertTrue(store.find(before.soulId()).orElseThrow().stage()==target
                && sword.getEnchantments().getLevel(enchantment)==3,"Upgrade lost identity/enchantment");
            helper.assertTrue(!menu.getSlot(10).mayPickup(player) && menu.quickMoveStack(player,10).isEmpty(),"Preview could be extracted");
        }
        var finalMenu=new ResonanceMenu(9,player.getInventory());finalMenu.getSlot(0).set(sword);
        finalMenu.getSlot(1).set(new ItemStack(Items.AMETHYST_BLOCK,16));
        finalMenu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
        helper.assertTrue(finalMenu.getSlot(0).getItem()==sword && finalMenu.getSlot(1).getItem().getCount()==16
            && store.find(before.soulId()).orElseThrow().stage()==8,"Unqualified final upgrade succeeded");
        helper.succeed();
    }
    @GameTest
    public void affectionRewardsAreCappedPerDay(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pos=helper.absolutePos(new net.minecraft.core.BlockPos(1,2,1));player.setPos(pos.getX(),pos.getY(),pos.getZ());
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
        var store=BladeSoulStore.forInitialization(player.level().getServer());
        var initial=BladeSoulService.initialize(store,sword,player.getUUID());
        var spirit=SpiritTransformationService.manifest(player);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        BladeAffection.interact(player,spirit,net.minecraft.world.InteractionHand.MAIN_HAND);
        BladeAffection.interact(player,spirit,net.minecraft.world.InteractionHand.MAIN_HAND);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.COD,2));
        BladeAffection.interact(player,spirit,net.minecraft.world.InteractionHand.MAIN_HAND);
        BladeAffection.interact(player,spirit,net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount()==1,"Daily duplicate gift consumed fish");
        for(int i=0;i<3000;i++)BladeAffection.follow(player,spirit,true);
        helper.assertTrue(store.find(initial.soulId()).orElseThrow().affection().orElseThrow()==7D,"Daily talk, gift or following cap failed");
        spirit.discard();helper.succeed();
    }
    @GameTest
    public void resonancePreservesIdentityAndRejectsReplay(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        sword.setDamageValue(780);
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keepsake"));
        var store=BladeSoulStore.forInitialization(player.level().getServer());
        var before=BladeSoulService.initialize(store,sword,player.getUUID());
        BladeSoulService.setTestAffection(store,sword,300);
        var menu=new ResonanceMenu(7,player.getInventory());
        menu.slots.get(0).set(sword);
        menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
        helper.assertTrue(menu.slots.get(0).getItem()==sword,"Missing materials changed sword");
        menu.slots.get(1).set(new ItemStack(Items.AMETHYST_SHARD,16));
        menu.slots.get(2).set(new ItemStack(Items.COPPER_INGOT,16));
        menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
        var result=menu.slots.get(0).getItem();
        helper.assertTrue(result.is(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade_stage_2")))
            && result.get(BladeSoulComponents.REFERENCE).soulId().equals(before.soulId())
            && result.getHoverName().getString().equals("Keepsake") && result.getDamageValue()==900,"Upgrade lost data or repaired damage");
        helper.assertTrue(menu.slots.get(1).getItem().getCount()==8 && menu.slots.get(2).getItem().getCount()==8,"Incorrect material debit");
        menu.clicked(22,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
        helper.assertTrue(menu.slots.get(0).getItem()==result && store.find(before.soulId()).orElseThrow().stage()==2,"Duplicate confirmation upgraded twice");
        // Return-to-inventory networking is covered by the real-client fixture.
        helper.succeed();
    }
    @GameTest
    public void growthLedgerPersistsDailyBudgets(GameTestHelper helper) {
        var store=new BladeGrowthStore();var id=java.util.UUID.randomUUID();
        store.put(new BladeGrowthStore.Day(id,100,5990,3,9,3,true,true,java.util.List.of(java.util.UUID.randomUUID())));
        var ops=helper.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        var decoded=BladeGrowthStore.CODEC.parse(ops,BladeGrowthStore.CODEC.encodeStart(ops,store).getOrThrow()).getOrThrow();
        helper.assertTrue(decoded.day(id,100).equals(store.day(id,100)),"Reload lost growth budget");
        helper.assertTrue(decoded.day(id,99).talked(),"Clock rollback reset daily budget");
        helper.assertTrue(!decoded.day(id,101).talked() && decoded.day(id,101).walking()==0,"Next day did not reset budget");
        helper.succeed();
    }
    @GameTest
    public void craftingKeepsDataAndBrokenBladeSurvives(GameTestHelper helper) {
        var sword=new ItemStack(Items.DIAMOND_SWORD);sword.setDamageValue(500);
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Original"));
        var items=new java.util.ArrayList<ItemStack>();items.add(sword);
        for(int i=0;i<4;i++)items.add(new ItemStack(Items.AMETHYST_SHARD));
        items.add(new ItemStack(Items.COD));items.add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("pink_dye"))));
        items.add(ItemStack.EMPTY);items.add(ItemStack.EMPTY);
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,items);
        var recipe=com.abyssredemption.absmod.item.MeowBladeRecipe.INSTANCE;
        helper.assertTrue(recipe.matches(input,helper.getLevel()),"Initial recipe did not match");
        var result=recipe.assemble(input);
        helper.assertTrue(result.getDamageValue()==500 && result.getHoverName().getString().equals("Original"),"Crafting lost original data");
        result.hurtAndBreak(99999,helper.getLevel(),(ServerPlayer)null,item->{});
        helper.assertTrue(!result.isEmpty() && result.isBroken(),"Broken blade disappeared");
        var repairInput=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(result,result.copy()));
        helper.assertTrue(!net.minecraft.world.item.crafting.RepairItemRecipe.INSTANCE.matches(repairInput,helper.getLevel()),"Craft repair consumed a soul blade");
        helper.succeed();
    }
    @GameTest
    public void invulnerableSpiritRecallsBeforeAttack(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pos=helper.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        player.setPos(pos.getX(),pos.getY(),pos.getZ());
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        sword.setDamageValue(31);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
        BladeSoulService.initialize(BladeSoulStore.forInitialization(player.level().getServer()),sword,player.getUUID());
        var spirit=SpiritTransformationService.manifest(player);
        float health=spirit.getHealth();
        helper.assertTrue(!spirit.hurtServer(helper.getLevel(),spirit.damageSources().generic(),1000)
                && !spirit.hurtServer(helper.getLevel(),spirit.damageSources().fellOutOfWorld(),1000)
                && spirit.getHealth()==health && spirit.active(),"Spirit accepted damage");
        var stranger=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        stranger.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,player.getMainHandItem().copy());
        helper.assertTrue(!SpiritTransformationService.recallForAttack(stranger) && spirit.active(),"Stranger recalled a copied link");
        helper.assertTrue(SpiritTransformationService.recallForAttack(player) && spirit.isRemoved()
                && ItemStack.matches(player.getMainHandItem(),sword),"Attack recall did not restore the exact sword");
        helper.assertTrue(!SpiritTransformationService.recallForAttack(player),"Repeated recall succeeded");
        helper.succeed();
    }
    @GameTest
    public void sneakUseSwitchesThroughSpectralLink(GameTestHelper helper) {
        var player=(ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pos=helper.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        player.setPos(pos.getX(),pos.getY(),pos.getZ());
        var sword=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod","meow_blade")));
        sword.setDamageValue(23);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
        var item=sword.getItem();
        item.use(player.level(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(!sword.has(BladeSoulComponents.REFERENCE),"Ordinary use initialized a soul");
        player.setShiftKeyDown(true);
        item.use(player.level(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
        var id=sword.get(BladeSoulComponents.REFERENCE).soulId();
        var record=BladeSoulStore.find(player.level().getServer()).orElseThrow().find(id).orElseThrow();
        var spirit=(com.abyssredemption.absmod.entity.BladeSpiritEntity)player.level().getEntity(record.carrier().orElseThrow().entityId());
        helper.assertTrue(com.abyssredemption.absmod.item.SpectralBladeItem.represents(player.getMainHandItem(),id),"No spectral link");
        helper.assertTrue(!(player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem),"Projection is a usable blade");
        spirit.interact(player,net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
        helper.assertTrue(ItemStack.matches(player.getMainHandItem(),sword) && spirit.isRemoved(),"Sneak interaction did not restore the original sword");
        helper.succeed();
    }
    @GameTest
    public void manifestationPreservesSwordAndRejectsRepeatedRecall(GameTestHelper helper) {
        var player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        var sword = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod",
                com.abyssredemption.absmod.item.MeowBladeStage.values()[0].itemId())));
        sword.setDamageValue(17);
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Retained sword"));
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), sword);
        var store = BladeSoulStore.forInitialization(helper.getLevel().getServer());
        var original = BladeSoulService.initialize(store, sword, player.getUUID());
        var expected = sword.copy();
        var spirit = SpiritTransformationService.manifest(player);
        helper.assertTrue(com.abyssredemption.absmod.item.SpectralBladeItem.represents(player.getMainHandItem(), original.soulId()) && spirit.active(), "Manifestation did not transfer the carrier");
        var ops = helper.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        var encoded = BladeSoulStore.CODEC.encodeStart(ops, store).getOrThrow();
        var decoded = BladeSoulStore.CODEC.parse(ops, encoded).getOrThrow();
        helper.assertTrue(ItemStack.matches(decoded.find(original.soulId()).orElseThrow().carrier().orElseThrow().sword(), expected),
                "Schema 2 lost the complete sword snapshot");
        player.getInventory().setItem(8, new ItemStack(Items.STONE));
        boolean refused = false;
        try { SpiritTransformationService.recall(player, spirit, 8); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && spirit.active() && player.getInventory().getItem(8).is(Items.STONE), "Occupied slot changed state");
        var stranger = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        refused = false;
        try { SpiritTransformationService.recall(stranger, spirit, 7); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && spirit.active() && stranger.getInventory().getItem(7).isEmpty(), "Another player recalled the spirit");
        var retained = store.find(original.soulId()).orElseThrow().carrier().orElseThrow();
        retained.sword().setCount(0);
        helper.assertTrue(!retained.sword().isEmpty(), "Snapshot leaked a mutable stack");
        SpiritTransformationService.recall(player, spirit, 7);
        helper.assertTrue(ItemStack.matches(player.getInventory().getItem(7), expected) && spirit.isRemoved(), "Recall lost components");
        refused = false;
        try { SpiritTransformationService.recall(player, spirit, 6); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && player.getInventory().getItem(6).isEmpty(), "Repeated recall duplicated a sword");
        helper.assertTrue(store.find(original.soulId()).orElseThrow().form().equals("sword"), "Soul did not return to sword form");
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), player.getInventory().removeItemNoUpdate(7));
        var current = store.find(original.soulId()).orElseThrow();
        var obstacle = player.blockPosition().offset(1, 0, 0);
        helper.getLevel().setBlockAndUpdate(obstacle, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        refused = false;
        try { SpiritTransformationService.manifest(player); } catch (IllegalStateException exception) { refused = true; }
        helper.assertTrue(refused && ItemStack.matches(player.getMainHandItem(), expected)
                && store.find(original.soulId()).orElseThrow().equals(current), "Blocked manifestation changed the sword or record");
        helper.succeed();
    }
}
