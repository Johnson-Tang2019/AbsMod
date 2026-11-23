package com.abyssredemption.absmod.neoforge;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;

/** Optional disposable-client fixture. Compiled outside the release source set. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class LanternVisualTest {
    private final BlockPos start = new BlockPos(0, 92, 0);
    private boolean started;
    private int tick;
    private RemotePlayer visitor;

    public LanternVisualTest() {
        if (!Boolean.getBoolean("absmod.lanternPeers")) NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            mc.options.hideGui = false;
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.getWindow().setWindowed(960, 600);
            mc.createWorldOpenFlows().createFreshLevel("lantern-fixture-" + System.currentTimeMillis(),
                    new LevelSettings("提灯测试", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                            new GameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(42, false, false),
                    lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)
                            .value().createWorldDimensions(), new TitleScreen());
        }
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        switch (++tick) {
            case 30 -> server(mc, player -> {
                var world = player.serverLevel();
                for (int x = -7; x <= 7; x++) for (int y = 90; y <= 96; y++) for (int z = -6; z <= 9; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    boolean shell = x == -7 || x == 7 || y == 90 || y == 96 || z == -6 || z == 9;
                    world.setBlockAndUpdate(p, (shell ? ((y == 90 || y == 93) ? Blocks.STRIPPED_CHERRY_WOOD
                            : Blocks.CALCITE) : Blocks.AIR).defaultBlockState());
                }
                for (int x : new int[]{-4, 4}) {
                    for (int y = 91; y <= 95; y++) world.setBlockAndUpdate(new BlockPos(x, y, 7), Blocks.CHERRY_LOG.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(x, 91, 5), Blocks.CHISELED_BOOKSHELF.defaultBlockState());
                    world.setBlockAndUpdate(new BlockPos(x, 92, 5), Blocks.POTTED_CHERRY_SAPLING.defaultBlockState());
                }
                player.teleportTo(world, .5, 91, .5, 0, 6);
                world.setDayTime(18000);
            });
            case 90 -> {
                light(mc, start, 0);
                shot(mc, "lantern-unlit.png");
                server(mc, p -> p.setItemInHand(InteractionHand.OFF_HAND, CherryLantern.LANTERN.toStack()));
            }
            case 99 -> server(mc, p -> p.serverLevel().setBlockAndUpdate(start.west(3), Blocks.SEA_LANTERN.defaultBlockState()));
            case 110 -> server(mc, p -> p.serverLevel().setBlockAndUpdate(start.west(3), Blocks.AIR.defaultBlockState()));
            case 125 -> {
                light(mc, start, 14);
                light(mc, start.south(3), 11);
                light(mc, start.west(4), 10);
                server(mc, p -> {
                    require(p.serverLevel().getBrightness(LightLayer.BLOCK, start) == 0, "server block light");
                    require(p.serverLevel().getBlockState(start).isAir(), "unmodified terrain");
                });
                shot(mc, "lantern-offhand.png");
                visitor = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), "LanternObserver"));
                visitor.setId(-3600);
                visitor.setPos(.5, 91, .5);
                visitor.setItemInHand(InteractionHand.OFF_HAND, CherryLantern.LANTERN.toStack());
                mc.level.addEntity(visitor);
                server(mc, p -> p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY));
            }
            case 150 -> {
                light(mc, start, 14);
                visitor.discard();
            }
            case 180 -> {
                light(mc, start, 0);
                server(mc, p -> CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler("belt").orElseThrow()
                        .getStacks().setStackInSlot(0, CherryLantern.LANTERN.toStack()));
                mc.options.hideGui = true;
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 220 -> {
                light(mc, start, 14);
                shot(mc, "lantern-worn.png");
                server(mc, p -> p.teleportTo(p.serverLevel(), 4.5, 91, .5, 0, 6));
            }
            case 245 -> {
                light(mc, start.east(4), 14);
                light(mc, start, 10);
                server(mc, p -> CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler("belt").orElseThrow()
                        .getStacks().setStackInSlot(0, ItemStack.EMPTY));
            }
            case 270 -> {
                light(mc, start.east(4), 0);
                server(mc, p -> {
                    p.setItemInHand(InteractionHand.OFF_HAND, CherryLantern.LANTERN.toStack());
                    var nether = p.server.getLevel(Level.NETHER);
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                        nether.setBlockAndUpdate(start.offset(dx, -2, dz), Blocks.STONE.defaultBlockState());
                        nether.setBlockAndUpdate(start.offset(dx, -1, dz), Blocks.AIR.defaultBlockState());
                        nether.setBlockAndUpdate(start.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                    }
                    p.teleportTo(nether, .5, 91, .5, 0, 0);
                });
            }
            case 330 -> {
                require(mc.level.dimension().equals(Level.NETHER), "travelled to nether");
                light(mc, start, 14);
                server(mc, p -> {
                    p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    p.teleportTo(p.server.overworld(), .5, 91, .5, 0, 0);
                });
            }
            case 390 -> {
                require(mc.level.dimension().equals(Level.OVERWORLD), "returned to overworld");
                light(mc, start, 0);
                if (Boolean.getBoolean("absmod.verifyShader")) {
                    try {
                        var api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                        require(Boolean.TRUE.equals(api.getMethod("isShaderPackInUse").invoke(
                                api.getMethod("getInstance").invoke(null))), "Iris pack enabled");
                    } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                }
                System.out.println("LANTERN_LIGHT_PASS shader=" + Boolean.getBoolean("absmod.verifyShader"));
                mc.stop();
            }
            default -> {}
        }
    }
    private static void server(Minecraft mc, Consumer<ServerPlayer> task) {
        var server = mc.getSingleplayerServer();
        var player = mc.player.getUUID();
        server.submit(() -> task.accept(server.getPlayerList().getPlayer(player))).join();
    }
    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), ignored -> {});
    }
    private static void light(Minecraft mc, BlockPos pos, int expected) {
        int light = mc.level.getBrightness(LightLayer.BLOCK, pos);
        require(light == expected, pos + " expected " + expected + " got " + light);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
