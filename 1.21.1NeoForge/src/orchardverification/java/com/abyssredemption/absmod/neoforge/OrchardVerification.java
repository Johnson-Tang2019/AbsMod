package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.block.HangingCherriesBlock;
import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.spirit.*;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "absmod", dist = Dist.CLIENT)
public final class OrchardVerification {
    private final String world = System.getProperty("absmod.orchardWorld");
    private final boolean reload = Boolean.getBoolean("absmod.orchardReload");
    private static final BlockPos POOL = new BlockPos(72, 90, 72);
    private static final BlockPos LEAF = POOL.offset(-3, 3, 1);
    private BladeSpiritEntity spirit;
    private int phase, frames;
    private long began;
    private boolean starting, done, photo;
    private final String view = System.getProperty("absmod.orchardView");

    public OrchardVerification() {
        require(world.matches("absmod-orchard-[0-9]+"), "disposable fixture copy required");
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();
        if (!starting && mc.screen instanceof TitleScreen) {
            starting = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.hideGui = true;
            mc.options.renderDistance().set(6);
            mc.getWindow().setWindowed(1000, 650);
            mc.createWorldOpenFlows().openWorld(world, () -> { throw new AssertionError("Missing 0.7.0 fixture copy"); });
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || ++frames < 40) return;
        require(frames < 1100, "timeout phase " + phase);
        var server = mc.getSingleplayerServer();
        if (view != null) {
            if (frames == 40) server.submit(() -> frameSpring(server.getPlayerList().getPlayers().getFirst())).join();
            if (frames == 140) {
                mc.getToasts().clear();
                Screenshot.grab(mc.gameDirectory, "natural-blossom-spring.png", mc.getMainRenderTarget(), t -> {});
            }
            if (frames == 144) mc.setScreen(new OrchardPalette());
            if (frames == 155) {
                Screenshot.grab(mc.gameDirectory, "orchard-pink-kitchen.png", mc.getMainRenderTarget(), t -> {});
                System.out.println("ORCHARD_PALETTE_PASS");
            }
            if (frames == 160) mc.stop();
            return;
        }
        server.submit(() -> step(server.getPlayerList().getPlayers().getFirst())).join();
        if (!reload && phase == 1 && !photo && frames > 58) {
            mc.getToasts().clear();
            Screenshot.grab(mc.gameDirectory, "cherry-orchard-spring.png", mc.getMainRenderTarget(), t -> {});
            photo = true;
        }
        if (done) mc.stop();
    }

    private static final class OrchardPalette extends net.minecraft.client.gui.screens.Screen {
        private static final String[] ITEMS = {"fragrant_cherry_blossom", "dew_cherries", "blossom_tea",
                "dew_cherry_cordial", "blossom_dumplings", "cherry_tart"};
        private static final String[] STAGES = {"花蕾", "青果", "转色", "浅粉熟果"};

        OrchardPalette() { super(net.minecraft.network.chat.Component.literal("樱花果园")); }
        @Override public boolean isPauseScreen() { return false; }

        @Override public void render(net.minecraft.client.gui.GuiGraphics g, int x, int y, float delta) {
            g.fill(0, 0, width, height, 0xFF352933);
            int top = height / 4;
            int y2 = height * 2 / 3;
            for (int i = 0; i < ITEMS.length; i++) {
                int cx = width * (2 * i + 1) / (2 * ITEMS.length);
                var key = ResourceLocation.fromNamespaceAndPath("absmod", ITEMS[i]);
                var stack = BuiltInRegistries.ITEM.getOptional(key).orElseThrow().getDefaultInstance();
                var model = minecraft.getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
                require(model.getParticleIcon().contents().name().equals(key.withPrefix("item/")), "item atlas " + key);
                var pose = g.pose();
                pose.pushPose();
                pose.translate(cx - 16, top - 16, 0);
                pose.scale(2, 2, 1);
                g.renderItem(stack, 0, 0);
                pose.popPose();
                g.drawCenteredString(font, stack.getHoverName(), cx, top + 24, 0xFFFFDFE8);
            }
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("absmod", "hanging_cherries"));
            for (int age = 0; age < STAGES.length; age++) {
                int cx = width * (2 * age + 1) / 8;
                var state = block.defaultBlockState().setValue(HangingCherriesBlock.AGE, age);
                var pose = g.pose();
                pose.pushPose();
                pose.translate(cx, y2 + 20, 100);
                pose.scale(48, -48, 48);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(24));
                pose.translate(-.5, 0, -.5);
                minecraft.getBlockRenderer().renderSingleBlock(state, pose, g.bufferSource(), 15728880,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
                g.flush();
                pose.popPose();
                g.drawCenteredString(font, STAGES[age], cx, y2 + 26, 0xFFFFDFE8);
            }
        }
    }

    private void frameSpring(ServerPlayer p) {
        int[] xyz = Arrays.stream(view.split(",")).mapToInt(Integer::parseInt).toArray();
        var stone = new BlockPos(xyz[0], xyz[1], xyz[2]);
        var level = p.serverLevel();
        level.getChunkAt(stone);
        require(level.getBlockState(stone).getBlock() instanceof com.abyssredemption.absmod.block.BlossomSpringBlock, "generated spring at photo site");
        level.setDayTime(1600);
        level.setWeatherParameters(6000,0,false,false);
        var center = Vec3.atCenterOf(stone.above());
        p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        for (int angle = 0; angle < 360; angle += 20) {
            double a = Math.toRadians(angle+30);
            Vec3 eye = center.add(Math.cos(a)*4.2, 2.8, Math.sin(a)*4.2);
            var clip = level.clip(new net.minecraft.world.level.ClipContext(eye, center,
                    net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, p));
            if (clip.getType() != HitResult.Type.MISS ||
                    !level.isEmptyBlock(BlockPos.containing(eye)) || !level.isEmptyBlock(BlockPos.containing(eye.add(0,-.3,0)))) continue;
            p.teleportTo(level,eye.x,eye.y-p.getEyeHeight(),eye.z,Set.of(),0,0);
            p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,center);
            System.out.println("NATURAL_SPRING_CAMERA "+eye);
            return;
        }
        throw new AssertionError("No clear angle on generated pool");
    }

    private void step(ServerPlayer p) {
        var l = p.serverLevel();
        long now = l.getGameTime();
        if (phase == 0) {
            if (reload) {
                var list = l.getEntitiesOfClass(BladeSpiritEntity.class, new AABB(POOL).inflate(5), s -> p.getUUID().equals(s.ownerId()));
                require(list.size() == 1, "saved spirit"); spirit = list.getFirst();
                require(affection(p) == 1 && daily(p).bathed(), "saved daily affection");
                require(l.getBlockState(LEAF.below()).getValue(HangingCherriesBlock.AGE) == 1, "saved fruit age");
            } else {
                buildBath(p);
                harvestAndCook(p);
                hold(p, stack("meow_blade"));
                BladeSoulService.initialize(BladeSoulStore.forInitialization(l.getServer()), p.getMainHandItem(), p.getUUID());
                spirit = SpiritTransformationService.manifest(p);
                spirit.setNoAi(true);
                spirit.moveTo(Vec3.atBottomCenterOf(POOL.east()), 160, 0);
                p.teleportTo(l, POOL.getX()+.5, POOL.getY()+2, POOL.getZ()-4, Set.of(), 0, 25);
            }
            began = now; phase++;
        } else if (reload) {
            if (phase == 1 && now - began > 130) {
                require(affection(p) == 1, "relog cannot repeat the same day");
                l.setDayTime(l.getDayTime() + 24000); began = now; phase++;
            } else if (phase == 2 && now - began > 120) {
                require(affection(p) == 2 && daily(p).bathed(), "next day");
                System.out.println("ORCHARD_REOPEN_PASS"); done = true;
            }
        } else if (phase == 1 && now - began > 45) {
            require(affection(p) == 0 && !p.hasEffect(MobEffects.REGENERATION), "dry bank");
            enter(p); phase++; began = now;
        } else if (phase == 2 && now - began > 55) {
            require(affection(p) == 0 && p.hasEffect(MobEffects.REGENERATION) && p.hasEffect(MobEffects.DAMAGE_RESISTANCE), "partial bath");
            p.teleportTo(l, POOL.getX()+.5, POOL.getY()+1, POOL.getZ()-3, Set.of(), 0, 18);
            phase++; began = now;
        } else if (phase == 3 && now - began > 12) {
            enter(p); phase++; began = now;
        } else if (phase == 4 && now - began > 60) {
            require(affection(p) == 0, "leaving resets pair timer");
            phase++; began = now;
        } else if (phase == 5 && now - began > 165) {
            require(affection(p) == 1 && daily(p).bathed() && !daily(p).talked(), "one separate daily reward");
            verifyQuests(p);
            System.out.println("ORCHARD_BATH_AND_GUIDE_PASS"); done = true;
        }
    }

    private void enter(ServerPlayer p) { p.teleportTo(p.serverLevel(), POOL.getX()+.5, POOL.getY(), POOL.getZ()+.5, Set.of(), 0, 10); }
    private BladeSoulRecord record(ServerPlayer p) { return BladeSoulStore.find(p.server).orElseThrow().find(spirit.soulId()).orElseThrow(); }
    private double affection(ServerPlayer p) { return record(p).affection().orElseThrow(); }
    private BladeGrowthStore.Day daily(ServerPlayer p) { return BladeAffection.dailyStatus(p, record(p)).orElseThrow(); }

    private void buildBath(ServerPlayer p) {
        var level = p.serverLevel();
        p.getInventory().selected = 0;
        p.teleportTo(level, POOL.getX()+.5, POOL.getY()+1, POOL.getZ()-3, Set.of(), 0, 15);
        level.setDayTime(1400);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, p.server);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, p.server);
        for (int x=-10;x<=10;x++) for (int z=-8;z<=9;z++) {
            var top = POOL.offset(x,0,z);
            level.setBlockAndUpdate(top.below(), Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(top, Blocks.GRASS_BLOCK.defaultBlockState());
        }
        var features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var placed = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).get(id("blossom_spring"));
        var biome = level.registryAccess().registryOrThrow(Registries.BIOME).get(id("minecraft:cherry_grove"));
        require(biome.getGenerationSettings().hasFeature(placed), "biome placement");
        var feature = features.get(id("blossom_spring"));
        level.setBlockAndUpdate(POOL.north().above(), Blocks.CHEST.defaultBlockState());
        require(!feature.place(level, level.getChunkSource().getGenerator(), level.random, POOL.above()), "preserve occupied site");
        level.removeBlock(POOL.north().above(), false);
        require(feature.place(level, level.getChunkSource().getGenerator(), level.random, POOL.above()), "world generation");
        require(level.getBlockState(POOL.below()).is(BuiltInRegistries.BLOCK.get(id("blossom_spring_stone"))), "warm pool floor");
        level.setBlockAndUpdate(POOL.north(), Blocks.CALCITE.defaultBlockState());
        level.setBlockAndUpdate(POOL.west(), Blocks.CALCITE.defaultBlockState());
        require(com.abyssredemption.absmod.block.BlossomSpringBlock.springFor(level, POOL.north().west(), POOL) == null,
                "two solid banks separate diagonal water");
        level.setBlockAndUpdate(POOL.north(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(POOL.west(), Blocks.WATER.defaultBlockState());
        for (var pos : List.of(POOL.offset(-6,1,4),POOL.offset(4,1,6),POOL.offset(7,1,0))) {
            features.get(id("minecraft:cherry")).place(level, level.getChunkSource().getGenerator(), level.random, pos);
        }
    }

    private void harvestAndCook(ServerPlayer p) {
        var level = p.serverLevel();
        var leaf = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE,1);
        require(leaf.isRandomlyTicking(), "healthy cherry leaves tick");
        var silk = new ItemStack(Items.IRON_HOE);
        silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
        int flowers = 0, saplings = 0;
        for (int i=0;i<160;i++) {
            for (var drop : Block.getDrops(leaf,level,LEAF,null,p,ItemStack.EMPTY)) {
                if (drop.is(Items.CHERRY_SAPLING)) saplings++;
                if (drop.is(stack("fragrant_cherry_blossom").getItem())) flowers++;
            }
            for (var tool : List.of(silk,new ItemStack(Items.SHEARS))) {
                var result = Block.getDrops(leaf,level,LEAF,null,p,tool);
                require(result.size()==1 && result.getFirst().is(Items.CHERRY_LEAVES), "silk/shears");
            }
            require(Block.getDrops(leaf.setValue(LeavesBlock.PERSISTENT,true),level,LEAF,null,p,ItemStack.EMPTY).stream()
                    .noneMatch(s -> s.is(stack("fragrant_cherry_blossom").getItem())), "placed leaves");
        }
        require(flowers > 0 && saplings > 0, "additive leaf loot");
        BlockPos natural = LEAF.east(2);
        while ((Mth.getSeed(natural) & 7) != 0) natural = natural.east();
        level.setBlockAndUpdate(natural,leaf); level.removeBlock(natural.below(),false);
        level.getBlockState(natural).randomTick(level,natural,level.random);
        require(level.getBlockState(natural.below()).getBlock() instanceof HangingCherriesBlock, "natural branch");
        level.removeBlock(natural,false);
        require(level.isEmptyBlock(natural.below()), "detached branch");
        level.setBlockAndUpdate(LEAF.above(),Blocks.CHERRY_LOG.defaultBlockState());
        level.setBlockAndUpdate(LEAF,leaf);
        hold(p, stack("fragrant_cherry_blossom")); click(p, LEAF, Direction.DOWN);
        BlockPos cluster = LEAF.below();
        require(level.getBlockState(cluster).getBlock() instanceof HangingCherriesBlock && p.getMainHandItem().isEmpty(), "planted");
        hold(p, ItemStack.EMPTY); click(p,cluster,Direction.DOWN);
        require(level.getBlockState(cluster).getValue(HangingCherriesBlock.AGE)==0, "green branch");
        hold(p, new ItemStack(Items.BONE_MEAL,2)); click(p,cluster,Direction.DOWN);click(p,cluster,Direction.DOWN);
        require(level.getBlockState(cluster).getValue(HangingCherriesBlock.AGE)==2, "bone meal stages");
        for(int i=0;i<200 && level.getBlockState(cluster).getValue(HangingCherriesBlock.AGE)!=3;i++) {
            level.getBlockState(cluster).randomTick(level,cluster,level.random);
        }
        require(level.getBlockState(cluster).getValue(HangingCherriesBlock.AGE)==3, "natural ripening");
        hold(p,ItemStack.EMPTY); click(p,cluster,Direction.DOWN);click(p,cluster,Direction.DOWN);
        var cherries = level.getEntitiesOfClass(ItemEntity.class,new AABB(cluster).inflate(2)).stream()
                .filter(i->i.getItem().is(stack("dew_cherries").getItem())).mapToInt(i->i.getItem().getCount()).sum();
        require(cherries>=2 && cherries<=3 && level.getBlockState(cluster).getValue(HangingCherriesBlock.AGE)==0,"harvest once");
        hold(p,new ItemStack(Items.BONE_MEAL));click(p,cluster,Direction.DOWN);
        hold(p,stack("dew_cherries")); taste(p,2);
        craft(p,"blossom_spring_stone","","fragrant_cherry_blossom","","minecraft:calcite","minecraft:campfire","minecraft:calcite","","minecraft:calcite","");
        craft(p,"blossom_dumplings","fragrant_cherry_blossom","minecraft:sugar","minecraft:wheat","minecraft:stick");
        taste(p,5);
        craft(p,"cherry_tart","dew_cherries","fragrant_cherry_blossom","minecraft:wheat","minecraft:sugar","minecraft:egg");
        taste(p,7);
        craft(p,"dew_cherry_cordial","dew_cherries","dew_cherries","minecraft:sugar","minecraft:glass_bottle");
        drink(p); craft(p,"blossom_tea","fragrant_cherry_blossom","minecraft:honey_bottle","minecraft:glass_bottle"); drink(p);
        System.out.println("ORCHARD_LOOT_HARVEST_RECIPES_PASS flowers="+flowers+" saplings="+saplings);
    }

    private static void taste(ServerPlayer p, int hunger) {
        p.getFoodData().setFoodLevel(8);
        var food = p.getMainHandItem();
        require(food.finishUsingItem(p.level(),p).isEmpty() && p.getFoodData().getFoodLevel()==8+hunger, "ate food");
    }
    private static void drink(ServerPlayer p) {
        var drink = p.getMainHandItem();
        require(drink.getUseAnimation()==UseAnim.DRINK && drink.finishUsingItem(p.level(),p).is(Items.GLASS_BOTTLE), "sipped and returned bottle");
    }
    private static void craft(ServerPlayer p,String name,String...ingredients) {
        var cells=new ArrayList<ItemStack>();
        for(String k:ingredients)cells.add(k.isEmpty()?ItemStack.EMPTY:stack(k));
        while(cells.size()<9)cells.add(ItemStack.EMPTY);
        var input=CraftingInput.of(3,3,cells);
        var recipe=p.server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,p.level()).orElseThrow().value();
        var out=recipe.assemble(input,p.registryAccess());
        require(out.is(stack(name).getItem()),"recipe "+name);
        if(name.equals("blossom_tea")) require(recipe.getRemainingItems(input).stream().filter(s->s.is(Items.GLASS_BOTTLE)).count()==1,"honey container");
        hold(p,out);
    }
    private static void verifyQuests(ServerPlayer p) {
        var file=ServerQuestFile.INSTANCE;
        var team=file.getTeamData(p).orElseThrow();
        require(file.getChapter(0x1C4E810187B7DDC2L).getQuests().size()==111,"updated chapter");
        for(String k:List.of("fragrant_cherry_blossom","harvest_dew_cherries","blossom_tea","dew_cherry_cordial","blossom_dumplings","cherry_tart","blossom_bath","spirit_bath")) {
            try {
                var hash=java.security.MessageDigest.getInstance("SHA-256").digest(("absmod/orchard/"+k).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                long id=Long.parseLong("4"+HexFormat.of().formatHex(hash).substring(0,15),16);
                require(team.isCompleted(file.getQuest(id)), "FTB "+k);
            } catch(java.security.NoSuchAlgorithmException ex){ throw new AssertionError(ex); }
        }
    }
    private static void click(ServerPlayer p,BlockPos pos,Direction face) {
        p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.49)),face,pos,false));
    }
    private static void hold(ServerPlayer p,ItemStack stack) { p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.inventoryMenu.broadcastChanges(); }
    private static ResourceLocation id(String name) { return name.contains(":")?ResourceLocation.parse(name):ResourceLocation.fromNamespaceAndPath("absmod",name); }
    private static ItemStack stack(String name) { return new ItemStack(BuiltInRegistries.ITEM.get(id(name))); }
    private static void require(boolean ok,String detail){if(!ok)throw new AssertionError(detail);}
}
