package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.entity.*;
import com.abyssredemption.absmod.block.*;
import com.abyssredemption.absmod.cat.*;
import com.abyssredemption.absmod.spirit.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.Items;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "absmod", dist = Dist.CLIENT)
public final class HomeFinaleTest {

    private static final BlockPos TABLE = new BlockPos(1,90,0), NEST = TABLE.east(3), CUSHION = NEST.east(2), MIRROR = TABLE.west(5);
    private final String world = "absmod-finale-" + System.getProperty("absmod.homeWorld");
    private final boolean reload = "reload".equals(System.getProperty("absmod.homeFinale"));
    private boolean started, photoTea, photoRest, photoMirror;
    private int ticks, phase, cupFrames, echoFrames;
    private BladeSpiritEntity spirit;
    private Cat firstCat, secondCat;
    private UUID trialSoul;
    private int damageBefore, maxBefore;
    private ItemStack otherSword;

    public HomeFinaleTest() {
        check(world.matches("absmod-finale-[0-9]{6,20}"), "numeric fixture world required");
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started=true;
            mc.options.pauseOnLostFocus=false;
            mc.options.hideGui = true;
            mc.options.renderDistance().set(4);
            mc.getWindow().setWindowed(1200,760);
            if (reload) mc.createWorldOpenFlows().openWorld(world, () -> { throw new IllegalStateException("Fixture unavailable"); });
            else {
                check(!new java.io.File(mc.gameDirectory,"saves/"+world).exists(),"fresh test world");
                mc.createWorldOpenFlows().createFreshLevel(world,
                        new LevelSettings(world,GameType.SURVIVAL,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42,false,false), lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                                .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
            }
        }
        if (mc.player==null || mc.getSingleplayerServer()==null || mc.level==null) return;
        check(++ticks<1000,"timed out phase="+phase);
        if (ticks < 35) return;
        if (reload) {
            server(mc,this::verifyReload);
            System.out.println("HOME_FINALE_RELOAD_PASS");
            mc.stop();
            return;
        }
        if (phase==0) {
            server(mc,this::start);
            phase=-1;
        } else if (phase==-1 && ticks>55) {
            server(mc,this::invite);
            phase=1;
        } else if (phase==1) {
            if (!photoTea && mc.level.getEntitiesOfClass(BladeSpiritEntity.class,new AABB(TABLE).inflate(10)).stream().anyMatch(s->!s.teaCup().isEmpty()) && ++cupFrames == 17) {
                take(mc,"home-spirit-tea.png");
                photoTea=true;
            }
            server(mc,p->{
                if (ticks%200==0) System.out.println("HOME_TRACE t="+ticks+" game="+p.serverLevel().getGameTime()+" spirit="+spirit.position()
                        +" path="+spirit.getNavigation().getPath()+" served="+complete(p,"spirit_tea")+" cup="+spirit.teaCup()+" cats="+firstCat.position()+"/"+firstCat.isLying()+" "+secondCat.position()+"/"+secondCat.isLying());
                if (!complete(p,"spirit_tea") || !firstCat.isLying() || !secondCat.isLying()) return;
                var level=p.serverLevel();
                var tray=(MilkshakeTrayBlockEntity)level.getBlockEntity(TABLE);
                check(photoTea && tray.cup(0).isEmpty() && tray.cup(1).getHoverName().getString().equals("Guest cup")
                        && !tray.cup(2).isEmpty() && !tray.cup(3).isEmpty() && p.getInventory().countItem(Items.BUCKET)==1,"served exactly one cup");
                check(CatLife.store(p).visit(p.getUUID(),firstCat.getUUID()).spot().pos().equals(NEST)
                        && CatLife.store(p).visit(p.getUUID(),secondCat.getUUID()).spot().pos().equals(CUSHION),"distinct rest records");
                p.teleportTo(level,CUSHION.getX()-1,90,-3,Set.of(),0,20);
                phase=2;
            });
        } else if (phase==2 && ticks%20==0) {
            if (!photoRest) { take(mc,"home-resting-cats.png"); photoRest=true; }
            else { server(mc,this::beginTrial); phase=3; }
        } else if (phase==3) {
            if (!photoMirror && !mc.level.getEntitiesOfClass(MirrorEchoEntity.class,new AABB(MIRROR).inflate(6)).isEmpty()) {
                if (++echoFrames==19) {take(mc,"home-mirror-warning.png"); photoMirror=true;}
            }
            server(mc,p->{
                var echo=p.serverLevel().getEntitiesOfClass(MirrorEchoEntity.class,new AABB(MIRROR).inflate(5)).stream()
                        .filter(e->e.isAlive()).findFirst().orElse(null);
                if (echo==null) {
                    check(complete(p,"mirror_victory"),"fight ended without victory");
                    check(BladeSoulService.requireRecord(BladeSoulStore.forInitialization(p.serverLevel().getServer()),p.getMainHandItem()).mirrorCleared(),"durable victory");
                    var revision=BladeSoulService.requireRecord(BladeSoulStore.forInitialization(p.serverLevel().getServer()),p.getMainHandItem()).revision();
                    check(BladeSoulService.winMirror(p,trialSoul) && BladeSoulService.requireRecord(BladeSoulStore.forInitialization(p.serverLevel().getServer()),p.getMainHandItem()).revision()==revision,"idempotent victory");
                    openWorkshop(p);
                    phase=4;
                    return;
                }
                int beat=(echo.tickCount-1)%108;
                if (beat==7) p.teleportTo(p.serverLevel(),MIRROR.getX()+3.5,90,MIRROR.getZ()-.5,Set.of(),65,-5);
                if (beat==47 || beat==72) p.teleportTo(p.serverLevel(),MIRROR.getX()+.5,90,MIRROR.getZ()-2.2,Set.of(),0,-6);
                if (echo.phase()==2 && echo.invulnerableTime==0) {
                    var guest=guest(p);
                    check(!echo.hurt(guest.damageSources().playerAttack(guest),1000),"foreign attack rejected");
                    p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,echo.getBoundingBox().getCenter());
                    var hit=com.abyssredemption.absmod.combat.BladeAttackExecutor.pick(p);
                    if (hit instanceof EntityHitResult entity && entity.getEntity()==echo)
                        com.abyssredemption.absmod.combat.BladeAttackExecutor.attack(p,hit);
                }
                if (echo.phase()!=2) {
                    check(!echo.hurt(p.damageSources().playerAttack(p),1),"closed window blocks damage");
                    if (p.getHealth()<14) p.setHealth(p.getMaxHealth());
                }
            });
        } else if (phase==4 && mc.screen instanceof com.abyssredemption.absmod.client.ResonanceScreen && ticks%20==0) {
            take(mc,"home-final-resonance.png");
            server(mc,this::settle);
            phase=5;
        } else if (phase==5 && ticks%20==0) {
            server(mc,p->{
                for (String name:List.of("set_tea_table","spirit_tea","cat_rest","mirror_trial","mirror_victory","normal_stage_9"))
                    check(complete(p,name),"advancement "+name);
                var ftb=ServerQuestFile.INSTANCE;
                var chapter=ftb.getChapter(0x1C4E810187B7DDC2L);
                check(chapter!=null && chapter.getQuests().size() == 111,"111 FTB quests loaded");
                var team=ftb.getTeamData(p).orElseThrow();
                for (long id: NEW_QUESTS) if (!team.isCompleted(ftb.getQuest(id))) return;
                p.serverLevel().getServer().saveEverything(true,true,true);
                phase=6;
            });
            if (phase==6) {System.out.println("HOME_FINALE_GAME_PASS six FTB quests, tea race, two nests and stage IX"); mc.stop();}
        }
    }

    private static void take(Minecraft mc,String name) {
        mc.getToasts().clear();
        Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),message->{});
    }
    private static ServerPlayer guest(ServerPlayer owner) {
        var p=FakePlayerFactory.get(owner.serverLevel(),new GameProfile(UUID.fromString("7d92cee5-478c-41de-94a8-7185eea38280"),"TeaGuest"));
        p.setPos(owner.getX()+0.5,owner.getY(),owner.getZ());
        return p;
    }
    private static boolean complete(ServerPlayer p,String key) {
        var a=p.serverLevel().getServer().getAdvancements().get(id("progression/"+key));
        return a!=null && p.getAdvancements().getOrStartProgress(a).isDone();
    }
    private static void ring(ServerPlayer p, int slot) {
        var at=Vec3.atLowerCornerOf(TABLE).add(MilkshakeTrayBlock.cupX(slot),0.75,MilkshakeTrayBlock.cupZ(slot));
        p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at,Direction.UP,TABLE,false));
    }
    private void start(ServerPlayer p) {
        var level=p.serverLevel();
        for(int x=-11;x<=11;x++) for(int z=-9;z<=6;z++) level.setBlockAndUpdate(new BlockPos(x,89,z),Blocks.CHERRY_PLANKS.defaultBlockState());
        level.setDayTime(1000);
        p.teleportTo(level,1.5,90,-4,Set.of(),0,7);
        // Seed earlier chapter progress; the six new goals are earned by their runtime interactions.
        var newNames=Set.of("set_tea_table","spirit_tea","cat_rest","mirror_trial","mirror_victory","normal_stage_9");
        for(var adv:level.getServer().getAdvancements().getAllAdvancements())
            if (adv.id().getNamespace().equals("absmod") && adv.id().getPath().startsWith("progression/")
                    && !newNames.contains(adv.id().getPath().substring(12)))
                for(String criterion:adv.value().criteria().keySet()) p.getAdvancements().award(adv,criterion);
        craft(p,"heart_mirror",1,"","minecraft:amethyst_block","","minecraft:amethyst_shard","pink_glowing_glass","minecraft:amethyst_shard","","minecraft:crying_obsidian","");
        craft(p,"cherry_cat_cushion",1,"","minecraft:pink_petals","","minecraft:pink_wool","minecraft:cherry_slab","minecraft:pink_wool");
        craft(p,"cherry_scratching_post",1,"","minecraft:pink_wool","","","minecraft:string","","minecraft:cherry_slab","minecraft:cherry_slab","minecraft:cherry_slab");
        place(p,"cat_nest",NEST); place(p,"cherry_cat_cushion",CUSHION);
        firstCat=cat(p,NEST.south(2)); secondCat=cat(p,CUSHION.south(2));
        CatLife.select(p,firstCat);CatLife.bind(p,NEST);
        CatLife.select(p,secondCat);CatLife.bind(p,CUSHION);
        CatLife.useNest(guest(p),NEST,true);
        check(CatLife.store(p).home(firstCat.getUUID())!=null,"foreign unbind refused");
        place(p,"cherry_tea_table",TABLE);
        hold(p,stack("milkshake_tray"));click(p,TABLE,Direction.UP);
        check(level.getBlockEntity(TABLE) instanceof MilkshakeTrayBlockEntity && p.getMainHandItem().isEmpty(),"mounted tea tray");
        for(int i=0;i<4;i++) {
            var cup=stack(i==0?"pink_milkshake":"milkshake");
            if(i==1) cup.set(DataComponents.CUSTOM_NAME,Component.literal("Guest cup"));
            hold(p,cup);ring(p,i);
        }
        hold(p,stack("meow_blade"));
        BladeSoulService.initialize(BladeSoulStore.forInitialization(level.getServer()),p.getMainHandItem(),p.getUUID());
        spirit=SpiritTransformationService.manifest(p);
    }
    private void invite(ServerPlayer p) {
        var level=p.serverLevel();
        p.getInventory().selected=1;
        hold(p,stack("bond_bell"));
        p.interactOn(spirit,InteractionHand.MAIN_HAND);
        check(p.getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().hasUUID("absmod_tea_spirit"),"bell selected spirit");
        ring(p,0);
        check(spirit.getNavigation().getPath()!=null,"accepted path from "+spirit.position()+" grounded="+spirit.onGround());
        var guest=guest(p);
        hold(guest,ItemStack.EMPTY);ring(guest,0);
        check(guest.getInventory().countItem(stack("pink_milkshake").getItem())==1,"guest takes invited cup");
        var tray=(MilkshakeTrayBlockEntity)level.getBlockEntity(TABLE);
        spirit.tea.tick(spirit,level);
        check(tray.cup(0).isEmpty() && !complete(p,"spirit_tea"),"cancel before settlement");
        var bell=p.getMainHandItem();
        hold(p,stack("pink_milkshake"));ring(p,0);hold(p,bell);
        ring(p,0);
        hold(guest,ItemStack.EMPTY);
        ring(guest,0);
        hold(guest,stack("pink_milkshake"));
        ring(guest,0);
        spirit.tea.tick(spirit,level);
        check(!complete(p,"spirit_tea") && !tray.cup(0).isEmpty(), "replacing the same flavour cancels service");
        ring(p,0);
    }
    private static Cat cat(ServerPlayer p,BlockPos pos) {
        var c=EntityType.CAT.create(p.serverLevel());c.tame(p);
        c.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);p.serverLevel().addFreshEntity(c);return c;
    }

    private void beginTrial(ServerPlayer p) {
        var level=p.serverLevel();
        p.getInventory().selected=0;
        p.getInventory().selected=1;
        ring(p,1);
        p.getInventory().selected=0;
        SpiritTransformationService.recall(p,spirit,0);
        check(p.getMainHandItem().is(stack("meow_blade").getItem()),"original sword restored after tea");
        var tray=(MilkshakeTrayBlockEntity)level.getBlockEntity(TABLE);
        p.gameMode.destroyBlock(TABLE);
        check(tray.isRemoved() && dropped(p,TABLE,"cherry_tea_table")==1 && dropped(p,TABLE,"milkshake_tray")==1
                && dropped(p,TABLE,"milkshake")==3,"dismount returns table, tray and three drinks");
        p.teleportTo(level,-3.5,90,-2.3,Set.of(),0,-8);
        place(p,"heart_mirror",MIRROR);
        var store=BladeSoulStore.forInitialization(level.getServer());
        otherSword=stack("meow_blade_stage_8");BladeSoulService.initialize(store,otherSword,p.getUUID());
        var sword=stack("meow_blade_stage_8");
        trialSoul=BladeSoulService.initialize(store,sword,p.getUUID()).soulId();
        BladeSoulService.setTestAffection(store,sword,250);
        sword.set(DataComponents.CUSTOM_NAME,Component.literal("Moonlit original"));
        sword.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),3);
        sword.setDamageValue(123);
        hold(p,sword);click(p,MIRROR,Direction.UP);
        var echo=level.getEntitiesOfClass(MirrorEchoEntity.class,new AABB(MIRROR).inflate(3)).getFirst();
        hold(p,otherSword);echo.tick();
        check(echo.isRemoved() && !BladeSoulService.requireRecord(store,otherSword).mirrorCleared(),"changing blades cancels");
        hold(p,sword);click(p,MIRROR,Direction.UP);
        check(level.getEntitiesOfClass(MirrorEchoEntity.class,new AABB(MIRROR).inflate(3)).stream().filter(e->!e.isRemoved()).count()==1,"single active echo");
    }
    private void openWorkshop(ServerPlayer p) {
        var sword=p.getMainHandItem();damageBefore=sword.getDamageValue();maxBefore=sword.getMaxDamage();
        hold(p,stack("resonance_heart"));
        p.getMainHandItem().use(p.level(),p,InteractionHand.MAIN_HAND);
        var menu=(ResonanceMenu)p.containerMenu;
        menu.getSlot(0).set(otherSword);menu.getSlot(1).set(new ItemStack(Items.AMETHYST_BLOCK,16));menu.broadcastChanges();
        check(!menu.mirrorCleared(),"second blade shows its own lock");
        menu.clickMenuButton(p,0);
        check(menu.getSlot(0).getItem()==otherSword && menu.getSlot(1).getItem().getCount()==16 && !menu.isComplete(),"failed confirmation consumes nothing");
        p.getInventory().setItem(8,otherSword);menu.getSlot(0).set(sword);menu.broadcastChanges();
        check(menu.mirrorCleared(),"original blade unlocks");
    }
    private void settle(ServerPlayer p) {
        var menu=(ResonanceMenu)p.containerMenu;
        menu.clickMenuButton(p,0);check(!menu.clickMenuButton(p,0),"duplicate click refused");
        var sword=menu.getSlot(0).getItem();
        check(menu.isComplete() && menu.getSlot(1).getItem().isEmpty() && sword.is(stack("meow_blade_stage_9").getItem())
                && sword.getDamageValue()==(int)Math.ceil((double)damageBefore*sword.getMaxDamage()/maxBefore)
                && !sword.getEnchantments().isEmpty() && sword.getHoverName().getString().equals("Moonlit original")
                && sword.get(BladeSoulComponents.REFERENCE).soulId().equals(trialSoul),"IX identity, name, enchant, durability and single debit");
        p.closeContainer();
    }
    private void verifyReload(ServerPlayer p) {
        var level=p.serverLevel();var store=BladeSoulStore.forInitialization(level.getServer());
        int found=0;
        for(int slot=0;slot<p.getInventory().getContainerSize();slot++) {
            var sword=p.getInventory().getItem(slot);
            if (sword.is(stack("meow_blade_stage_9").getItem())) {
                var record=BladeSoulService.requireRecord(store,sword);
                check(record.mirrorCleared() && record.affection().orElseThrow()==250 && sword.getHoverName().getString().equals("Moonlit original"),"saved victory");
                found++;
            }
        }
        check(found==1 && !BladeSoulService.requireRecord(store,p.getInventory().getItem(8)).mirrorCleared(),"single IX, separate VIII after restart");
        var life=CatLife.store(p);
        for(var pos:List.of(NEST,CUSHION)) {
            var home=life.at(GlobalPos.of(level.dimension(),pos));
            check(home!=null && life.visit(p.getUUID(),home.cat().orElseThrow()).spot().equals(home.spot()),"saved home and visit");
        }
    }
    private static final long[] NEW_QUESTS = {0x1D4D2E8F48E7AA03L, 0x1846776214F76AB8L, 0x79F2310DDDF5539AL, 0x0CE33A72B13EAAD6L, 0x19EF437F13F31165L, 0x4ACE82CA4A3C572BL};
    private static int dropped(ServerPlayer p, BlockPos pos, String item) {
        return p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.25)).stream()
                .filter(entity -> entity.getItem().is(stack(item).getItem())).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static void place(ServerPlayer p, String name, BlockPos pos) {
        hold(p, stack(name));
        p.setShiftKeyDown(true);
        click(p, pos.below(), Direction.UP);
        p.setShiftKeyDown(false);
        check(p.serverLevel().getBlockState(pos).is(BuiltInRegistries.BLOCK.get(id(name))), "placed " + name);
        hold(p, ItemStack.EMPTY);
    }

    private static void click(ServerPlayer p, BlockPos support, Direction face) {
        var point = Vec3.atCenterOf(support).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.49));
        p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND, new BlockHitResult(point, face, support, false));
    }

    private static void craft(ServerPlayer p, String result, int count, String... ingredients) {
        var cells = new ArrayList<ItemStack>();
        for (var name : ingredients) cells.add(name.isEmpty() ? ItemStack.EMPTY : stack(name));
        while (cells.size() < 9) cells.add(ItemStack.EMPTY);
        var input = CraftingInput.of(3, 3, cells);
        var level = p.serverLevel();
        var output = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().value().assemble(input, level.registryAccess());
        check(output.is(stack(result).getItem()) && output.getCount() == count, "recipe " + result + " (got " + output + ")");
        hold(p, output);
    }

    private static void hold(ServerPlayer p, ItemStack item) {
        p.setItemInHand(InteractionHand.MAIN_HAND, item);
        p.inventoryMenu.broadcastChanges();
    }

    private static ResourceLocation id(String key) {
        return key.contains(":") ? ResourceLocation.parse(key) : ResourceLocation.fromNamespaceAndPath("absmod", key);
    }
    private static ItemStack stack(String key) { return new ItemStack(BuiltInRegistries.ITEM.get(id(key))); }
    private static void server(Minecraft mc, java.util.function.Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer();
        server.submit(() -> action.accept(server.getPlayerList().getPlayers().getFirst())).join();
    }
    private static void check(boolean condition, String subject) {
        if (!condition) throw new IllegalStateException("Home finale: " + subject);
    }
}
