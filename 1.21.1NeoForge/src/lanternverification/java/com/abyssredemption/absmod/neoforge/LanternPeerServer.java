package com.abyssredemption.absmod.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.Team;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(value = "absmod", dist = Dist.DEDICATED_SERVER)
public final class LanternPeerServer {
    private int expected;

    public LanternPeerServer() {
        if (Boolean.getBoolean("absmod.lanternPeers")) {
            NeoForge.EVENT_BUS.addListener(this::tick);
            NeoForge.EVENT_BUS.addListener(this::acknowledge);
        }
    }

    private void tick(ServerTickEvent.Post event) {
        if (expected != 0) return;
        var server = event.getServer();
        var carrier = server.getPlayerList().getPlayerByName("LanternCarrier");
        var witness = server.getPlayerList().getPlayerByName("LanternWitness");
        if (carrier == null || witness == null) return;
        var world = server.overworld();
        for (int x = -2; x < 9; x++) for (int z = -2; z <= 2; z++) {
            world.setBlockAndUpdate(new BlockPos(x, 90, z), Blocks.STONE.defaultBlockState());
        }
        var scoreboard = server.getScoreboard();
        var team = scoreboard.addPlayerTeam("lantern");
        team.setCollisionRule(Team.CollisionRule.NEVER);
        for (var player : java.util.List.of(carrier, witness)) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
            player.teleportTo(world, .5, 91, .5, 0, 0);
        }
        CuriosApi.getCuriosInventory(carrier).orElseThrow().getStacksHandler("belt").orElseThrow()
                .getStacks().setStackInSlot(0, CherryLantern.LANTERN.toStack());
        witness.setItemInHand(InteractionHand.OFF_HAND, CherryLantern.LANTERN.toStack());
        expected = 1;
    }

    private void acknowledge(ServerChatEvent event) {
        if (!event.getUsername().equals("LanternWitness") || !event.getRawText().equals("lantern_check_" + expected)) return;
        event.setCanceled(true);
        var server = event.getPlayer().server;
        var carrier = server.getPlayerList().getPlayerByName("LanternCarrier");
        switch (expected++) {
            case 1 -> event.getPlayer().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            case 2 -> carrier.teleportTo(server.overworld(), 6.5, 91, .5, 0, 0);
            case 3 -> carrier.connection.disconnect(Component.literal("Finished carrier checks"));
            case 4 -> {
                System.out.println("LANTERN_PEERS_PASS");
                server.halt(false);
            }
            default -> throw new IllegalStateException("Unexpected witness acknowledgement");
        }
    }
}
