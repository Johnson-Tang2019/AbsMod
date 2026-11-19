package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulService;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.abyssredemption.absmod.spirit.SpiritInteraction;
import com.abyssredemption.absmod.spirit.SpiritTransformationService;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Exercises normal entity unloading and reloading in a fresh local world. */
@Mod(value = "absmod", dist = Dist.CLIENT)
public final class SpiritRecoveryTest {
    private static final int START_X = 320;
    private static final int CATCH_UP_X = 352;
    private final String worldName = "absmod-recovery-" + System.currentTimeMillis();
    private boolean started;
    private volatile boolean captureLocation;
    private volatile boolean finished;
    private int tick;
    private int leftTick;
    private int returnTick;
    private BladeSpiritEntity originalCarrier;
    private ItemStack originalSword;
    private ItemStack link;

    public SpiritRecoveryTest() {
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
    }

    private void clientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!started && mc.gui.screen() instanceof TitleScreen) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.renderDistance().set(4);
            mc.createWorldOpenFlows().createFreshLevel(worldName,
                    new LevelSettings(worldName, GameType.SURVIVAL,
                            new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(42, false, false),
                    lookup -> lookup.lookupOrThrow(Registries.WORLD_PRESET)
                            .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
        }
        if (captureLocation) {
            captureLocation = false;
            Screenshot.grab(mc.gameDirectory, "spirit-last-location.png", mc.gameRenderer.mainRenderTarget(), 1,
                    result -> LogUtils.getLogger().info("SPIRIT_RECOVERY_SCREENSHOT {}", result.getString()));
        }
        if (finished) { mc.stop(); }
    }

    private void serverTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (!server.getWorldData().getLevelName().equals(worldName)
                || server.getPlayerList().getPlayers().isEmpty() || finished) { return; }
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        ServerLevel level = server.overworld();
        tick++;

        if (tick == 20) {
            platform(level, START_X);
            teleport(player, level, START_X, 0);
        } else if (tick == 60) {
            var sword = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", "meow_blade")));
            sword.set(DataComponents.CUSTOM_NAME, Component.literal("共鸣原剑"));
            sword.setDamageValue(173);
            sword.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 2);
            var store = BladeSoulStore.forInitialization(server);
            BladeSoulService.initialize(store, sword, player.getUUID());
            BladeSoulService.setTestAffection(store, sword, 42);
            originalSword = sword.copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
            originalCarrier = SpiritTransformationService.manifest(player);
            link = player.getMainHandItem().copy();
            level.setBlockAndUpdate(new BlockPos(CATCH_UP_X, 99, 0), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(CATCH_UP_X - 3, 99, -3), Blocks.MAGMA_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(CATCH_UP_X - 3, 99, -2), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(CATCH_UP_X - 3, 101, -2), Blocks.STONE.defaultBlockState());
            teleport(player, level, CATCH_UP_X, 0);
        } else if (tick == 105) {
            check(originalCarrier.distanceToSqr(player) > 24 * 24, "Caught up on a hazardous or obstructed floor");
            platform(level, CATCH_UP_X);
            level.setBlockAndUpdate(new BlockPos(CATCH_UP_X - 3, 99, -3), Blocks.MAGMA_BLOCK.defaultBlockState());
        }

        if (tick > 105 && leftTick == 0
                && (originalCarrier.distanceToSqr(player) < 36 || tick == 220)) {
            check(originalCarrier.distanceToSqr(player) < 36 && originalCarrier.active()
                    && !level.getBlockState(originalCarrier.blockPosition().below()).is(Blocks.MAGMA_BLOCK),
                    "The active spirit failed to reach a safe floor: spirit=" + originalCarrier.position()
                    + " owner=" + player.position() + " active=" + originalCarrier.active()
                    + " target ticking=" + level.isPositionEntityTicking(new BlockPos(CATCH_UP_X, 100, 0)));
            check(hasKey(SpiritTransformationService.locate(player), "message.absmod.spirit.located"),
                    "Loaded spirit did not report its live position");
            // Keep the carrier at its last position while the owner intentionally leaves it behind.
            originalCarrier.setNoAi(true);
            LogUtils.getLogger().info("SPIRIT_RECOVERY_FOLLOW_PASS id={} pos={}",
                    originalCarrier.getUUID(), originalCarrier.blockPosition());
            platform(level, 1024);
            teleport(player, level, 1024, 0);
            leftTick = tick;
        }

        if (leftTick != 0 && tick > leftTick && returnTick == 0 && level.getEntity(originalCarrier.getUUID()) == null) {
            check(!SpiritTransformationService.recallForAttack(player) && ItemStack.matches(link, player.getMainHandItem()),
                    "An unloaded carrier issued a sword");
            var status = SpiritTransformationService.locate(player);
            check(hasKey(status, "message.absmod.spirit.last_location"), "Last location unavailable");
            var soul = originalSword.get(BladeSoulComponents.REFERENCE).soulId();
            var saved = BladeSoulStore.find(server).orElseThrow().find(soul).orElseThrow();
            check(saved.carrier().orElseThrow().lastLocation().orElseThrow().pos().getX() > CATCH_UP_X - 5,
                    "Stored position did not follow the carrier across chunks");
            player.getMainHandItem().getItem().use(level, player, InteractionHand.MAIN_HAND);
            returnTick = tick + 40;
            LogUtils.getLogger().info("SPIRIT_RECOVERY_UNLOAD_PASS {}", status);
        }
        if (returnTick != 0 && tick == returnTick - 25) { captureLocation = true; }
        if (returnTick != 0 && tick == returnTick) { teleport(player, level, CATCH_UP_X, 0); }
        if (returnTick != 0 && tick > returnTick
                && level.getEntity(originalCarrier.getUUID()) instanceof BladeSpiritEntity restored) {
            check(restored != originalCarrier && restored.active(), "Expected a reloaded authoritative carrier");
            player.setShiftKeyDown(true);
            SpiritInteraction.recall(player, InteractionHand.MAIN_HAND, restored);
            player.setShiftKeyDown(false);
            check(ItemStack.matches(originalSword, player.getMainHandItem()), "The original sword's components changed");
            var soul = originalSword.get(BladeSoulComponents.REFERENCE).soulId();
            var record = BladeSoulStore.find(server).orElseThrow().find(soul).orElseThrow();
            long copies = com.abyssredemption.absmod.command.SoulCommands.findLoadedCopies(server, soul).size();
            check(copies == 1 && record.form().equals("sword") && record.ownerId().orElseThrow().equals(player.getUUID())
                    && record.affection().orElseThrow() == 42 && restored.isRemoved()
                    && !SpiritTransformationService.recallForAttack(player), "Recall failed the same-soul or replay checks");
            LogUtils.getLogger().info("SPIRIT_RECOVERY_PASS id={} copies={} affection={} damage={}",
                    soul, copies, record.affection().orElseThrow(), player.getMainHandItem().getDamageValue());
            finished = true;
        }
        check(tick < 1000, "Timed out waiting for normal chunk unload or reload");
    }

    private static void teleport(ServerPlayer player, ServerLevel level, int x, int z) {
        player.teleportTo(level, x + .5, 100, z + .5, Set.of(), 0, 0, true);
    }

    private static void platform(ServerLevel level, int centerX) {
        for (int x = centerX - 4; x <= centerX + 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlockAndUpdate(new BlockPos(x, 99, z), Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static boolean hasKey(Component component, String key) {
        return component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                && text.getKey().equals(key);
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
