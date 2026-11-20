package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.block.MilkshakeTrayBlockEntity;
import com.abyssredemption.absmod.spirit.*;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import vectorwing.farmersdelight.common.block.entity.*;

@Mod(value = "absmod", dist = Dist.CLIENT)
public final class RoseWorkshopTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 90, 0);
    private boolean started;
    private int tick;
    private BladeSpiritEntity spirit;
    private static final String[] CHARMS = {"paw_signet", "dusk_ears", "quarry_mittens", "spring_slippers", "tide_pendant", "fishbone_sash", "cloud_mantle"};
    private static final String[] SLOTS = {"ring", "head", "hands", "feet", "necklace", "belt", "back"};

    public RoseWorkshopTest() { NeoForge.EVENT_BUS.addListener(this::tick); }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.getWindow().setWindowed(1100, 760);
            mc.createWorldOpenFlows().createFreshLevel("absmod-workshop-" + System.currentTimeMillis(),
                    new LevelSettings("Rose workshop", GameType.SURVIVAL, false, net.minecraft.world.Difficulty.PEACEFUL,
                            true, new GameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(42, false, false),
                    lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        tick++;
        if (tick == 35) server(mc, this::setup);
        if (tick == 94) {
            mc.getToasts().clear();
            mc.options.hideGui = true;
        }
        if (tick == 100) {
            for (String id : java.util.stream.Stream.concat(java.util.Arrays.stream(CHARMS), java.util.stream.Stream.of("whisker_compass","petal_anklet","rainfish_locket")).toList()) {
                var models = mc.getItemRenderer().getItemModelShaper().getModelManager();
                check(mc.getItemRenderer().getModel(stack(id), mc.level, mc.player, 0) != models.getMissingModel(), "missing icon " + id);
            }
            screenshot(mc, "rose-workshop-kitchen.png");
        }
        if (tick == 104) mc.options.hideGui = false;
        if (tick == 130) server(mc, this::verify);
        if (tick == 159 || tick == 235 || tick == 284) mc.getToasts().clear();
        if (tick == 165) {
            check(mc.screen instanceof vectorwing.farmersdelight.client.gui.CookingPotScreen, "pot screen did not stay open");
            screenshot(mc, "rose-workshop-pot.png");
            mc.player.closeContainer();
        }
        if (tick == 190) server(mc, p -> {
            p.setItemInHand(InteractionHand.MAIN_HAND, stack("meow_blade"));
            BladeSoulService.initialize(BladeSoulStore.forInitialization(p.server), p.getMainHandItem(), p.getUUID());
            var chart = ORIGIN.offset(0, 0, -2);
            p.serverLevel().setBlockAndUpdate(chart, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
            p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COMPASS));
            use(p, chart, 0.5);
            check(p.getOffhandItem().is(Items.COMPASS), "stage one consumed compass");
            var blade = stack("meow_blade_stage_3");
            blade.setDamageValue(17);
            var bound = BladeSoulService.initialize(BladeSoulStore.forInitialization(p.server), blade, p.getUUID());
            p.setItemInHand(InteractionHand.MAIN_HAND, blade);
            use(p, chart, 0.5);
            check(p.getOffhandItem().isEmpty() && p.getMainHandItem()==blade && blade.getDamageValue()==17
                    && BladeSoulService.requireRecord(BladeSoulStore.forInitialization(p.server),blade).equals(bound), "compass exchange");
            equipKeepsake(p, "charm", "whisker_compass");
            spirit = SpiritTransformationService.manifest(p);
            spirit.setPos(p.getX() + 1, p.getY(), p.getZ());
            p.setShiftKeyDown(true);
            p.tickCount += (10 - p.tickCount % 10) % 10;
            RoseCharms.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            check(complete(p, "whisker_compass"), "compass trail");
            p.setShiftKeyDown(false);
            var level = p.serverLevel();
            level.setWeatherParameters(0,6000,true,false);
            level.setRainLevel(1F);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COD, 2));
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_BOTTLE, 2));
            spirit.interact(p, InteractionHand.MAIN_HAND);
            check(p.getMainHandItem().getCount()==1 && p.getOffhandItem().getCount()==1, "rain gift");
            spirit.interact(p, InteractionHand.MAIN_HAND);
            check(p.getMainHandItem().getCount()==1 && p.getOffhandItem().getCount()==1, "daily gift duplicate");
            equipKeepsake(p, "necklace", "rainfish_locket");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));
            p.setXRot(-75);
            p.gameMode.useItem(p,level,p.getMainHandItem(),InteractionHand.MAIN_HAND);
            check(p.getMainHandItem().is(Items.POTION) && complete(p,"rainfish_locket"), "rain bottle");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.RABBIT_FOOT));
            p.getInventory().add(stack("bell_clip"));
            check(SpiritWardrobe.choose(p, spirit, SpiritAdornment.BELL), "wardrobe equip");
            p.setItemInHand(InteractionHand.MAIN_HAND, stack("gentle_brush"));
            spirit.interact(p, InteractionHand.MAIN_HAND);
            check(spirit.isGrooming(), "brush");
            SpiritGameMenu.open(p, spirit, SpiritGameType.GOMOKU, () -> {});
        });
        if (tick == 220) {
            check(mc.player.containerMenu instanceof SpiritGameMenu game && game.gameType() == SpiritGameType.GOMOKU, "board sync");
            mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId, 40);
        }
        if (tick == 240) {
            check(((SpiritGameMenu)mc.player.containerMenu).cell(40) == SpiritBoardGame.PLAYER, "board action");
            screenshot(mc, "rose-workshop-board.png");
            mc.player.closeContainer();
        }
        if (tick == 252) server(mc, p -> {
            for (var variant : SpiritGameType.values()) {
                SpiritGameMenu.open(p, spirit, variant, () -> {});
                var menu = (SpiritGameMenu)p.containerMenu;
                for (int moves = 0; menu.result() == 0 && moves <= variant.cellCount(); moves++) {
                    boolean played = false;
                    for (int cell = 0; cell < variant.cellCount() && !played; cell++)
                        if (menu.canSelect(cell)) played = menu.clickMenuButton(p, cell);
                    check(played, "stalled game " + variant);
                }
                check(menu.result() != 0 && menu.clickMenuButton(p, SpiritGameMenu.RESTART), "finish and replay " + variant);
                p.closeContainer();
            }
            check(p.getOffhandItem().isEmpty(), "game offering");
            equipKeepsake(p, "feet", "petal_anklet");
            p.setShiftKeyDown(true);
            var drop = new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX()+2,p.getY(),p.getZ(),new ItemStack(Items.FEATHER));
            var reserved = new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX()+2,p.getY(),p.getZ(),new ItemStack(Items.COAL));
            reserved.setPickUpDelay(100);
            var claimed = new net.minecraft.world.entity.item.ItemEntity(p.serverLevel(),p.getX()+2,p.getY(),p.getZ(),new ItemStack(Items.REDSTONE));
            claimed.setTarget(java.util.UUID.randomUUID());
            p.serverLevel().addFreshEntity(claimed);
            p.serverLevel().addFreshEntity(drop);
            p.serverLevel().addFreshEntity(reserved);
            p.causeFallDamage(2F,1F,p.damageSources().fall());
            check(!drop.isRemoved(), "small step collected loot");
            p.causeFallDamage(4F,1F,p.damageSources().fall());
            check(drop.isRemoved() && !reserved.isRemoved() && !claimed.isRemoved() && complete(p,"petal_anklet"),"anklet landing");
            p.setShiftKeyDown(false);
        });
        if (tick == 270) {
            mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        }
        if (tick == 290) screenshot(mc, "rose-workshop-equipment.png");
        if (tick == 295) {
            mc.player.closeContainer();
            mc.setScreen(null);
            mc.options.hideGui = true;
            server(mc,p -> {
                var level=p.serverLevel();
                level.setWeatherParameters(6000,0,false,false);
                level.setRainLevel(0);
                for (int x=9;x<=15;x++) for (int z=-5;z<=1;z++)
                    level.setBlockAndUpdate(new BlockPos(x,89,z),Blocks.CHERRY_PLANKS.defaultBlockState());
                String[] slots={"charm","feet","necklace"};
                for (int i=0;i<slots.length;i++) {
                    var wall=new BlockPos(11+i,91,0);
                    level.setBlockAndUpdate(wall,Blocks.STRIPPED_CHERRY_LOG.defaultBlockState());
                    var frame=new net.minecraft.world.entity.decoration.GlowItemFrame(level,wall.north(),Direction.NORTH);
                    var inventory=CuriosApi.getCuriosInventory(p).orElseThrow();
                    frame.setItem(inventory.getStacksHandler(slots[i]).orElseThrow().getStacks().getStackInSlot(0).copy());
                    level.addFreshEntity(frame);
                }
                p.teleportTo(level,12.5,90,-3.1,java.util.Set.of(),0,2);
                p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                spirit.setPos(5,90,0);
            });
        }
        if (tick == 316) screenshot(mc, "blush-keepsakes-world.png");
        if (tick == 320) {
            mc.options.hideGui=false;
            server(mc,p -> {
                var file=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
                var data=file.getTeamData(p).orElseThrow();
                for (long id : new long[]{0x5406ba805fadbdb3L, 0x1815871d23a3fe7bL, 0x1cb5786f600c5910L}) {
                    check(data.isCompleted(file.getQuest(id)), "FTB completion " + id);
                }
            });
            com.mojang.logging.LogUtils.getLogger().info("BLUSH_KEEPSAKES_CLIENT_PASS three exchanges, three active effects, daily gate, preserved soul, 60 quests");
            com.mojang.logging.LogUtils.getLogger().info("ROSE_WORKSHOP_CLIENT_PASS kitchen, cooking, rose tools, armor, Curios, FTB, tray, grooming, wardrobe and board");
            server(mc, AlloyKitchenTest::exercise);
            mc.options.hideGui=true;
        }
        if (tick == 350) {
            AlloyKitchenTest.checkModels(mc);
            screenshot(mc,"rose-gold-cookery-world.png");
        }
        if (tick == 385) {
            server(mc,AlloyKitchenTest::quests);
            mc.options.hideGui=false;
            mc.stop();
        }
    }

    private void setup(ServerPlayer p) {
        var level = p.serverLevel();
        for (int x=-7;x<=7;x++) for (int z=-5;z<=6;z++) level.setBlockAndUpdate(ORIGIN.offset(x,-1,z), Blocks.CHERRY_PLANKS.defaultBlockState());
        level.setDayTime(1000);
        p.teleportTo(level, 0.5, 90, -3.5, java.util.Set.of(), 0, 16);
        var table = ORIGIN.offset(0,0,-1);
        level.setBlockAndUpdate(table, Blocks.SMITHING_TABLE.defaultBlockState());
        var blade = stack("meow_blade");
        blade.setDamageValue(5);
        p.setItemInHand(InteractionHand.MAIN_HAND, blade);
        p.inventoryMenu.broadcastChanges();
        p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.AMETHYST_SHARD, 2));
        use(p, table, 0.5);
        check(p.getOffhandItem().getCount()==1 && p.getMainHandItem()==blade && blade.getDamageValue()==5
                && p.getInventory().contains(stack("paw_crystal")), "blade imprint settlement");
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var curio = CuriosApi.getCuriosInventory(p).orElseThrow();
        for (int i=0;i<CHARMS.length;i++) {
            var item = stack(CHARMS[i]);
            check(level.getRecipeManager().byKey(id(CHARMS[i])).isPresent(), "charm recipe "+CHARMS[i]);
            var slots=curio.getStacksHandler(SLOTS[i]).orElseThrow().getStacks();
            check(slots.insertItem(0,item,false).isEmpty(),"curio slot "+SLOTS[i]);
            p.getInventory().add(item.copy());
        }
        for (var equipment : new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            String part = switch(equipment){case HEAD->"helmet";case CHEST->"chestplate";case LEGS->"leggings";default->"boots";};
            p.setItemSlot(equipment,stack("rose_"+part));
        }
        var input = new SmithingRecipeInput(new ItemStack(Items.PINK_DYE),new ItemStack(Items.IRON_PICKAXE),stack("paw_crystal"));
        input.base().setDamageValue(23);
        var pick = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,level).orElseThrow().value().assemble(input,level.registryAccess());
        check(pick.is(stack("rose_pickaxe").getItem()) && pick.getDamageValue()==23, "smithing copied wear");
        p.getInventory().add(pick);
        String[] stations={"rose_bamboo_basket","rose_wooden_basket","rose_cabinet","rose_stove","rose_cutting_board","milkshake_tray"};
        for(int i=0;i<stations.length;i++){
            var pos=ORIGIN.offset(i-3,0,1);
            level.setBlockAndUpdate(pos,BuiltInRegistries.BLOCK.get(id(stations[i])).defaultBlockState());
            var be=level.getBlockEntity(pos);
            check(be!=null && be.getType().isValid(level.getBlockState(pos)),"block entity "+stations[i]);
        }
        var stove=ORIGIN.offset(0,0,1);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
        use(p,stove,0.5);
        level.setBlockAndUpdate(stove.above(),BuiltInRegistries.BLOCK.get(id("rose_cooking_pot")).defaultBlockState());
        var pot=(CookingPotBlockEntity)level.getBlockEntity(stove.above());
        check(pot.isHeated(),"pot heat");
        var tomato=BuiltInRegistries.ITEM.get(ResourceLocation.parse("farmersdelight:tomato"));
        pot.getInventory().setStackInSlot(0,new ItemStack(tomato));
        pot.getInventory().setStackInSlot(1,new ItemStack(tomato));
        pot.getInventory().setStackInSlot(7,new ItemStack(Items.BOWL));
        var skilletPos=stove.east(3);
        level.setBlockAndUpdate(skilletPos,BuiltInRegistries.BLOCK.get(id("rose_stove")).defaultBlockState());
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
        use(p,skilletPos,0.5);
        level.setBlockAndUpdate(skilletPos.above(),BuiltInRegistries.BLOCK.get(id("rose_skillet")).defaultBlockState());
        var skillet=(SkilletBlockEntity)level.getBlockEntity(skilletPos.above());
        check(skillet.isHeated(),"skillet heat");
        skillet.addItemToCook(new ItemStack(Items.BEEF),p);
        check(skillet.hasStoredStack(),"skillet input");
        var board=(CuttingBoardBlockEntity)level.getBlockEntity(stove.east());
        board.addItem(new ItemStack(Items.PORKCHOP));
        check(board.processStoredItemUsingTool(stack("rose_knife"),p),"pink knife cutting board recipe");
        p.setItemInHand(InteractionHand.MAIN_HAND,stack("pink_milkshake"));
        use(p,stove.east(2),0.25);
        check(!((MilkshakeTrayBlockEntity)level.getBlockEntity(stove.east(2))).cup(0).isEmpty(),"tray insert");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        level.removeBlock(table, false);
        p.teleportTo(level, -2.3, 90, -4.5, java.util.Set.of(), -19, 12);
    }

    private void verify(ServerPlayer p) {
        var level=p.serverLevel();
        check(p.getArmorValue()==15,"iron-equivalent armor");
        for(var effect:java.util.List.of(MobEffects.LUCK,MobEffects.NIGHT_VISION,MobEffects.DIG_SPEED,MobEffects.JUMP,
                MobEffects.WATER_BREATHING,MobEffects.MOVEMENT_SPEED,MobEffects.SLOW_FALLING)) check(p.hasEffect(effect),"worn effect "+effect);
        var chapter=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE.getChapter(Long.parseUnsignedLong("1C4E810187B7DDC2",16));
        check(chapter!=null && chapter.getQuests().size()==60,"FTB count");
        var pos=ORIGIN.offset(0,1,1);
        var pot=(CookingPotBlockEntity)level.getBlockEntity(pos);
        for(int i=0;i<150 && pot.getMeal().isEmpty() && pot.getInventory().getStackInSlot(8).isEmpty();i++)
            CookingPotBlockEntity.cookingTick(level,pos,pot.getBlockState(),pot);
        check(!pot.getMeal().isEmpty() || !pot.getInventory().getStackInSlot(8).isEmpty(),"tomato sauce cooking");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        p.teleportTo(level,0.5,90,-1.5,java.util.Set.of(),0,5);
        use(p,pos,0.5);
        check(p.containerMenu instanceof vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu
                && p.containerMenu.stillValid(p),"pink pot menu validity");
    }

    private static boolean complete(ServerPlayer p,String key) {
        var entry=p.server.getAdvancements().get(id("progression/"+key));
        return entry!=null && p.getAdvancements().getOrStartProgress(entry).isDone();
    }

    private static void equipKeepsake(ServerPlayer p,String slot,String key) {
        var stacks=CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler(slot).orElseThrow().getStacks();
        var prior=stacks.extractItem(0,1,false);
        if (!prior.isEmpty()) p.getInventory().add(prior);
        int index=p.getInventory().findSlotMatchingItem(stack(key));
        check(index>=0 && stacks.insertItem(0,p.getInventory().removeItem(index,1),false).isEmpty(),"equip "+key);
    }

    private static void use(ServerPlayer p,BlockPos pos,double xz) {
        p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atLowerCornerOf(pos).add(xz,0.15,xz),Direction.UP,pos,false));
    }
    private static ItemStack stack(String path){return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));}
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("absmod",path);}
    private static void server(Minecraft mc,java.util.function.Consumer<ServerPlayer> action){
        var server=mc.getSingleplayerServer();
        server.submit(()->action.accept(server.getPlayerList().getPlayers().getFirst())).join();
    }
    private static void screenshot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),m->{});}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
