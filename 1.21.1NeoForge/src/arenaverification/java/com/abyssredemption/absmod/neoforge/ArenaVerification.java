package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.arena.CherryArena;
import com.abyssredemption.absmod.arena.CherryPortalBlock;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value="absmod", dist=Dist.CLIENT)
public final class ArenaVerification {
    private final String world=System.getProperty("absmod.arenaWorld");
    private final boolean reopen=Boolean.getBoolean("absmod.arenaReopen");
    private static final BlockPos BASE=new BlockPos(52,105,54);
    private boolean started, photo, entrancePhoto, galleryPhoto;
    private float trialHealth;
    private int frames, phase, ticks, openingTicks;
    private UUID wither, dragon;

    public ArenaVerification() {
        check(world.matches("absmod-arena-[0-9]+"),"disposable copy required");
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started=true;
            mc.options.pauseOnLostFocus=false;
            mc.options.hideGui=true;
            mc.options.renderDistance().set(12);
            mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
            mc.getWindow().setWindowed(1080,700);
            mc.createWorldOpenFlows().openWorld(world,()->{throw new AssertionError("missing fixture");});
        }
        if (started && mc.player == null) {
            if (++openingTicks % 100 == 0) System.out.println("ARENA_OPEN_SCREEN " + (mc.screen == null ? "-" : mc.screen.getClass().getSimpleName()));
            check(openingTicks < 950, "world opening timed out");
            if (mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen backup) {
                var save = net.minecraft.network.chat.Component.translatable("selectWorld.backupJoinConfirmButton").getString();
                for (var child : backup.children()) {
                    if (child instanceof net.minecraft.client.gui.components.Button button && button.getMessage().getString().equals(save)) {
                        button.onPress(); break;
                    }
                }
            }
        }
        if (mc.player==null || mc.level==null || mc.getSingleplayerServer()==null || ++frames<45) return;
        check(frames<1400,"timeout in phase "+phase);
        var server=mc.getSingleplayerServer();
        server.submit(()->step(server.getPlayerList().getPlayers().getFirst())).join();
        if (phase==4 && !photo && ticks>=150 && renderedCourt(mc)) {
            mc.getToasts().clear();
            Screenshot.grab(mc.gameDirectory,"cherry-arena-court.png",mc.getMainRenderTarget(),ignored->{});
            photo=true;
        }
        if (phase==12 && !entrancePhoto && ticks>=25) {
            mc.getToasts().clear();
            Screenshot.grab(mc.gameDirectory,"cherry-arena-entry.png",mc.getMainRenderTarget(),ignored->{});
            entrancePhoto=true;
        }
        if (phase==15 && !galleryPhoto && ticks>=50) {
            mc.getToasts().clear();
            Screenshot.grab(mc.gameDirectory,"cherry-arena-gallery.png",mc.getMainRenderTarget(),ignored->{});
            galleryPhoto=true;
        }
        if (phase==8) mc.stop();
    }
    private static boolean renderedCourt(Minecraft mc) {
        for (int x = -48; x <= 48; x += 16) for (int z = -48; z <= 48; z += 16) {
            if (!mc.levelRenderer.isSectionCompiled(new BlockPos(x,CherryArena.FLOOR,z))) return false;
        }
        return true;
    }
    private static void check(boolean condition,String why) { if(!condition) throw new AssertionError(why); }
    private static boolean portal(ServerLevel level,BlockPos pos) {return level.getBlockState(pos).getBlock() instanceof CherryPortalBlock;}
    private static void move(ServerPlayer p,double x,double y,double z) {
        p.teleportTo(p.serverLevel(),x,y,z,Set.of(),180,0);
        p.setDeltaMovement(Vec3.ZERO);
        p.fallDistance=0;
    }
    private void step(ServerPlayer p) {
        var level=p.serverLevel();
        var arena=p.server.getLevel(CherryArena.DIMENSION);
        check(arena!=null,"dimension registered");
        ticks++;
        if (phase==0 && reopen) {
            check(level==arena && portal(arena,CherryArena.EXIT),"reopened arena");
            check(arena.getBlockState(CherryArena.MIRROR).is(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("absmod:heart_mirror"))),"mirror");
            p.setPortalCooldown(0);
            move(p,.5,70,CherryArena.EXIT.getZ()+.5);
            phase=6; return;
        }
        if (phase==0) {
            p.server.getPlayerList().setViewDistance(12);
            check(level==p.server.overworld(),"start in overworld");
            p.setGameMode(GameType.SURVIVAL);
            p.server.setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
            for(int x=-5;x<6;x++) for(int z=-4;z<=4;z++) level.setBlockAndUpdate(BASE.offset(x,-1,z),Blocks.STONE.defaultBlockState());
            for(int i=-1;i<=2;i++) for(int h=0;h<=4;h++)
                level.setBlockAndUpdate(BASE.offset(i,h,0), (i==-1||i==2||h==0||h==4)?Blocks.CHERRY_LOG.defaultBlockState():Blocks.AIR.defaultBlockState());
            move(p,BASE.getX()+.5,BASE.getY(),BASE.getZ()+2);
            var flower=BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:fragrant_cherry_blossom"));
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(flower,2));
            click(p); check(portal(level,BASE.above(3)) && p.getMainHandItem().getCount()==1,"lit frame");
            level.removeBlock(BASE.offset(-1,2,0),false);check(!portal(level,BASE.above(3)),"frame collapse");
            level.setBlockAndUpdate(BASE.offset(-1,2,0),Blocks.CHERRY_LOG.defaultBlockState());
            click(p);check(p.getMainHandItem().isEmpty(),"second flower");
            var w=EntityType.WITHER.create(level);w.setNoAi(true);
            w.moveTo(BASE.getX()+.5,BASE.getY()+1,BASE.getZ()+.5);wither=w.getUUID();check(level.addFreshEntity(w),"wither spawn");
            phase=9;ticks=0;return;
        }
        if (phase==9) {
            if (ticks==80) System.out.println("ARENA_WITHER_TRACE origin="+level.getEntity(wither)+" arena="+arena.getEntity(wither));
            if (arena.getEntity(wither)==null) return;
            move(p,BASE.getX()+1.5,BASE.getY()+1,BASE.getZ()+.5);
            phase=1;return;
        }
        if (phase==1 && level==arena) {
            check(portal(level,CherryArena.EXIT.offset(-3,3,0)),"exit");
            for(int chunk:new int[]{-22,-5,7,19}) {
                var sample=level.getChunk(chunk,4);
                for(var section:sample.getSections()) check(section.hasOnlyAir(),"void-only chunk");
            }
            var pos=CherryArena.SPAWN.below();var floor=level.getBlockState(pos);
            check(!level.destroyBlock(pos,true,p) && !level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState()),"block writes and drops");
            check(!p.gameMode.destroyBlock(pos),"player break");
            p.setGameMode(GameType.CREATIVE);
            move(p,5.5,70,10.5);
            level.explode(null,pos.getX(),pos.getY()+1,pos.getZ(),5,Level.ExplosionInteraction.TNT);
            check(level.getBlockState(pos)==floor,"explosion");
            check(!level.setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState()),"tower placement");
            var w=(WitherBoss)level.getEntity(wither);check(w!=null,"wither crossed real portal");
            w.setNoAi(false);w.moveTo(2,91,2);w.setDeltaMovement(0,1.2,0);
            var d=EntityType.ENDER_DRAGON.create(level);d.moveTo(-4,93,2);dragon=d.getUUID();level.addFreshEntity(d);
            p.setGameMode(GameType.CREATIVE);
            phase=2;ticks=0;return;
        }
        if (phase==2 && ticks>=12) {
            var w=(WitherBoss)arena.getEntity(wither); var d=(LivingEntity)arena.getEntity(dragon);
            check(w!=null && d!=null && w.getY()>=70 && w.getY()<=71 && d.getY()>=70 && d.getY()<=71,"grounded flying bosses");
            check(!CherryArena.isBoss(EntityType.CAT.create(arena)),"ordinary cat");
            w.setNoAi(true);
            w.moveTo(42,92,.5);
            CherryArena.contain(w,arena);
            check(w.getX()>32 && w.getX()<35 && w.getY()==CherryArena.FLOOR,"expanded boss movement area");
            d.discard();
            phase=11;ticks=0;return;
        }
        if (phase==11 && ticks>=30) {
            var w=(WitherBoss)arena.getEntity(wither);
            float hp=w.getHealth();
            check(w.hurt(level.damageSources().playerAttack(p),5) && w.getHealth()<hp,"boss takes damage after its normal hurt cooldown");
            w.discard();
            p.setGameMode(GameType.SURVIVAL);
            p.removeAllEffects(); p.setHealth(p.getMaxHealth()); p.invulnerableTime=0;
            var sword=BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:meow_blade_stage_8")).getDefaultInstance();
            com.abyssredemption.absmod.spirit.BladeSoulService.initialize(
                    com.abyssredemption.absmod.spirit.BladeSoulStore.forInitialization(p.server),sword,p.getUUID());
            p.setItemInHand(InteractionHand.MAIN_HAND,sword);
            move(p,.5,70,-5.5);
            p.gameMode.useItemOn(p,arena,sword,InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(CherryArena.MIRROR),Direction.UP,CherryArena.MIRROR,false));
            trialHealth=p.getHealth();
            phase=10;ticks=0;return;
        }
        if (phase==10 && ticks>=34) {
            var echoes=arena.getEntitiesOfClass(com.abyssredemption.absmod.entity.MirrorEchoEntity.class,
                    new AABB(CherryArena.MIRROR).inflate(10));
            check(echoes.size()==1 && echoes.getFirst().onGround() && p.getHealth()<trialHealth,"grounded mirror sweep deals damage");
            move(p,22.5,70,.5);
            phase=16;ticks=0;return;
        }
        if (phase==16 && ticks>=6) {
            var echoes=arena.getEntitiesOfClass(com.abyssredemption.absmod.entity.MirrorEchoEntity.class,
                    new AABB(CherryArena.MIRROR).inflate(10));
            check(echoes.size()==1,"mirror trial continues in the extended court");
            move(p,37.5,70,.5);
            phase=17;ticks=0;return;
        }
        if (phase==17 && ticks>=6) {
            check(arena.getEntitiesOfClass(com.abyssredemption.absmod.entity.MirrorEchoEntity.class,
                    new AABB(CherryArena.MIRROR).inflate(10)).isEmpty(),"leaving the court ends the trial");
            System.out.println("ARENA_MIRROR_GROUND_ATTACK_PASS");
            move(p,90,57,60);
            phase=3;ticks=0;return;
        }
        if (phase==3 && ticks>=6) {
            check(p.position().distanceTo(Vec3.atBottomCenterOf(CherryArena.SPAWN))<1 && p.fallDistance==0,"safe void rescue");
            var file=ServerQuestFile.INSTANCE;var team=file.getTeamData(p).orElseThrow();
            check(file.getChapter(0x1C4E810187B7DDC2L).getQuests().size()==111,"quest count");
            check(team.isCompleted(file.getQuest(0x71E0E638CC9A8001L)) && team.isCompleted(file.getQuest(0x71E0E638CC9A8002L)),"both arena quests");
            System.out.println("ARENA_PORTAL_VOID_PROTECTION_BOSSES_FTB_PASS");
            p.setGameMode(GameType.SPECTATOR);
            move(p,58,146,70);p.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(.5,69,-1));
            phase=4;ticks=0;return;
        }
        if (phase==4 && ticks>=165 && photo) {
            move(p,31,88,38);p.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(41,78,-18));
            phase=15;ticks=0;return;
        }
        if (phase==15 && ticks>=65 && galleryPhoto) {
            p.setGameMode(GameType.SURVIVAL);move(p,.5,70,CherryArena.SPAWN.getZ()+.5);
            p.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(.5,81,-46));
            phase=12;ticks=0;return;
        }
        if (phase==12 && entrancePhoto && ticks>=32) {
            phase=8;return;
        }
        if (phase==6 && level==p.server.overworld()) {
            check(p.position().distanceTo(Vec3.atCenterOf(BASE))<6,"personal return after reopen");
            System.out.println("ARENA_REOPEN_RETURN_PASS");
            var base = BASE.east(25).above(35);
            for (int z=-1;z<=2;z++) for (int y=0;y<=4;y++)
                if ((z==-1 || z==2) != (y==0 || y==4))
                    level.setBlockAndUpdate(base.offset(0,y,z),Blocks.STRIPPED_CHERRY_LOG.defaultBlockState());
            move(p,base.getX()+1.5,base.getY()+1,base.getZ()+.5);
            var flower = BuiltInRegistries.ITEM.get(ResourceLocation.parse("absmod:fragrant_cherry_blossom")).getDefaultInstance();
            p.setItemInHand(InteractionHand.MAIN_HAND,flower);
            var cap=base.offset(0,4,0);
            level.removeBlock(cap,false);
            click(p,base);
            check(!portal(level,base.above()) && p.getMainHandItem().getCount()==1,"incomplete frame");
            level.setBlockAndUpdate(cap,Blocks.STRIPPED_CHERRY_LOG.defaultBlockState());
            click(p,base);
            check(portal(level,base.above()) && level.getBlockState(base.above()).getValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS)==Direction.Axis.Z,"rotated cornerless gate");
            p.setPortalCooldown(0);move(p,base.getX()+.5,base.getY()+1,base.getZ()+.5);
            phase=13; return;
        }
        if (phase==13 && level==arena) {
            p.setPortalCooldown(0);move(p,.5,70,CherryArena.EXIT.getZ()+.5);
            phase=14;return;
        }
        if (phase==14 && level==p.server.overworld()) {
            var spawn=level.getSharedSpawnPos();
            check(Math.abs(p.getX()-spawn.getX())<30 && Math.abs(p.getZ()-spawn.getZ())<30,"no safe return uses world spawn");
            System.out.println("ARENA_ROTATED_FRAME_SPAWN_FALLBACK_PASS");
            phase=8;
        }
    }
    private static void click(ServerPlayer p) { click(p,BASE); }
    private static void click(ServerPlayer p, BlockPos base) {
        p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(base).add(0,.5,0),Direction.UP,base,false));
    }
}
