package com.abyssredemption.absmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Checks command parsing and permission boundaries without starting a game server. */
public final class CommandVerification {
    public static void main(String[] args) throws Exception {
        try {
            verify();
        } finally {
            net.minecraft.util.Util.shutdownExecutors();
        }
    }

    private static void verify() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        com.abyssredemption.absmod.item.MilkshakeVerification.verify();
        com.abyssredemption.absmod.client.SlashVerification.verify();
        com.abyssredemption.absmod.client.SpiritModelVerification.verify();
        com.abyssredemption.absmod.spirit.SoulVerification.verify();
        com.abyssredemption.absmod.combat.CombatVerification.verify();
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ModCommands.register(dispatcher, stage -> {
            throw new AssertionError("Parsing must not grant items");
        }, variant -> {
            throw new AssertionError("Parsing must not grant milkshakes");
        });
        CommandSourceStack operator = source(PermissionSet.ALL_PERMISSIONS);
        CombatProbeCommands.register(dispatcher, new com.abyssredemption.absmod.combat.BladeCombatServer(),
                (target, payload) -> { throw new AssertionError("Parsing must not send packets"); });
        CommandSourceStack player = source(PermissionSet.NO_PERMISSIONS);
        for (String command : new String[] {"absmod", "absmod help", "absmod stages",
                "absmod give TestPlayer 1", "absmod give TestPlayer 9",
                "absmod give @a 9", "absmod kit @s", "absmod inspect @a",
                "absmod milkshakes @s", "absmod milkshakes TestPlayer",
                "absmod kit TestPlayer", "absmod inspect TestPlayer", "absmod soul",
                "absmod soul init TestPlayer", "absmod soul inspect TestPlayer",
                "absmod soul affection set TestPlayer 12.5", "absmod soul affection add TestPlayer -2",
                "absmod spirit manifest TestPlayer", "absmod spirit recall TestPlayer TestSpirit 0",
                "absmod spirit recall TestPlayer TestSpirit 35", "absmod combat probe @s true", "absmod combat probe @a false", "absmod combat inspect TestPlayer"}) {
            check(parses(dispatcher, operator, command), "Expected valid command: " + command);
            check(!parses(dispatcher, player, command), "Permission bypass: " + command);
        }
        for (String command : new String[] {"absmod give TestPlayer 0", "absmod give TestPlayer 10",
                "absmod give TestPlayer 1.5", "absmod give TestPlayer", "absmod kit",
                "absmod inspect", "absmod milkshakes", "absmod unknown", "absmod stages extra",
                "absmod soul init", "absmod soul affection set TestPlayer", "absmod soul affection set TestPlayer NaN",
                "absmod spirit manifest", "absmod spirit recall TestPlayer TestSpirit -1",
                "absmod spirit recall TestPlayer TestSpirit 36", "absmod combat probe @s", "absmod combat probe @s yes", "absmod combat inspect"}) {
            check(!parses(dispatcher, operator, command), "Accepted invalid command: " + command);
        }
        check(dispatcher.execute("absmod stages", operator) == 9, "Expected nine stage entries");
        check(dispatcher.execute("absmod help", operator) == 1, "Help failed");
        System.out.println("PASS: command grammar, stage bounds, permissions and read-only output");
    }

    private static CommandSourceStack source(PermissionSet permissions) {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null,
                permissions, "Verification", Component.literal("Verification"), null, null);
    }

    private static boolean parses(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandSourceStack source, String command) {
        var result = dispatcher.parse(command, source);
        return !result.getReader().canRead() && result.getExceptions().isEmpty()
                && result.getContext().getCommand() != null;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
