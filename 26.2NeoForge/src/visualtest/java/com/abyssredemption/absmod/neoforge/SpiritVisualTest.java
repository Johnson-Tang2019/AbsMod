package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.entity.SpiritEntityType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Creates only a fresh disposable world; records actual in-world frames without altering mod gameplay. */
@Mod(value="absmod",dist=Dist.CLIENT)
public final class SpiritVisualTest {
    private boolean started;
    private int ticks;
    private BladeSpiritEntity testedSpirit;
    private final String worldId="absmod-visual-"+System.currentTimeMillis();
    public SpiritVisualTest() { NeoForge.EVENT_BUS.addListener(this::tick); }
    private void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if (!started && mc.gui.screen() instanceof TitleScreen) {
            started=true;
            mc.options.pauseOnLostFocus=false;
            mc.options.renderDistance().set(6);
            mc.options.fov().set(50);
            mc.getWindow().setWindowed(1280,960);
            mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);
            mc.createWorldOpenFlows().createFreshLevel(worldId,
                    new LevelSettings(worldId,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                    new WorldOptions(42,false,false),
                    lookup->lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());
        }
        if (!started || mc.level==null || mc.player==null || mc.getSingleplayerServer()==null) { return; }
        ticks++;
        var server=mc.getSingleplayerServer();
        if (ticks==40) {
            server.execute(()->{
                var level=server.overworld();
                for (int x=-8;x<=8;x++) { for (int z=-8;z<=8;z++) { level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.QUARTZ_BLOCK.defaultBlockState()); } }
                var player=server.getPlayerList().getPlayers().getFirst();
                player.setPos(-1,100,0);
                var sword=new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("absmod","meow_blade")));
                sword.setDamageValue(19);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
                player.setShiftKeyDown(true);
                sword.getItem().use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND);
                var record=com.abyssredemption.absmod.spirit.BladeSoulStore.find(server).orElseThrow().find(
                        sword.get(com.abyssredemption.absmod.spirit.BladeSoulComponents.REFERENCE).soulId()).orElseThrow();
                var spirit=(BladeSpiritEntity)level.getEntity(record.carrier().orElseThrow().entityId());
                testedSpirit=spirit;
                player.setShiftKeyDown(false);
                spirit.setPos(0,100,0); spirit.setYRot(180); spirit.yBodyRot=180; spirit.yHeadRot=180;
                spirit.setNoAi(true);
                LogUtils.getLogger().info("SPIRIT_VISUAL world={} entity={} manifested; held={}",worldId,spirit.getUUID(),player.getMainHandItem());
            });
        }
        int[] starts={60,180,300,420};
        double[][] cameras={{0,100,-2.7,0,15},{2.7,100,0,90,15},{0,100,2.7,180,15},{1.91,100,-1.91,45,15}};
        String[] views={"front","side","back","angle"};
        for (int i=0;i<starts.length;i++) {
            final int index=i;
            if (ticks==starts[i]) {
                server.execute(()->{
                    var player=server.getPlayerList().getPlayers().getFirst();var c=cameras[index];
                    player.teleportTo(server.overworld(),c[0],c[1],c[2],java.util.Set.of(),(float)c[3],(float)c[4],true);
                });
            }
            if (ticks==starts[i]+80) {
                Screenshot.grab(mc.gameDirectory,"spirit-"+views[i]+".png",mc.gameRenderer.mainRenderTarget(),1,
                        result->LogUtils.getLogger().info("SPIRIT_VISUAL screenshot {}",result.getString()));
            }
        }
        if (ticks==540) { mc.reloadResourcePacks(); }
        if (ticks==700) {
            Screenshot.grab(mc.gameDirectory,"spirit-after-reload.png",mc.gameRenderer.mainRenderTarget(),1,
                    result->LogUtils.getLogger().info("SPIRIT_VISUAL reload screenshot {}",result.getString()));
        }
        if (ticks==720) {
            server.execute(()->{
                var player=server.getPlayerList().getPlayers().getFirst();
                var combat=new com.abyssredemption.absmod.combat.BladeCombatServer();
                combat.receive(player,new com.abyssredemption.absmod.network.AttackIntentPayload(1,1,true,
                        player.getInventory().getSelectedSlot(),player.level().dimension().identifier()));
                combat.tick(server);
                if (!testedSpirit.isRemoved() || player.getMainHandItem().getDamageValue()!=19
                        || !(player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem)) {
                    throw new IllegalStateException("Visual test recall did not restore sword");
                }
                LogUtils.getLogger().info("SPIRIT_VISUAL attack-intent recall verified held={}",player.getMainHandItem());
            });
        }
        if (ticks==760) { Screenshot.grab(mc.gameDirectory,"spirit-recalled.png",mc.gameRenderer.mainRenderTarget(),1,
                result->LogUtils.getLogger().info("SPIRIT_VISUAL recall screenshot {}",result.getString())); }
        if (ticks==800) { mc.stop(); }
    }
}
