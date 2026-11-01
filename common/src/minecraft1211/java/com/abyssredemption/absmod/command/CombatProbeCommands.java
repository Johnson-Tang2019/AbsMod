package com.abyssredemption.absmod.command;

import com.abyssredemption.absmod.combat.BladeCombatServer;
import com.abyssredemption.absmod.network.CombatProbePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import java.util.function.BiConsumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CombatProbeCommands {
    private CombatProbeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, BladeCombatServer probe,
                                BiConsumer<ServerPlayer, CombatProbePayload> send) {
        dispatcher.register(Commands.literal("absmod").requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("combat")
                        .then(Commands.literal("probe").then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                    var targets = EntityArgument.getPlayers(context, "targets");
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    for (var player : targets) {
                                        probe.enable(player, enabled);
                                        // Tracing never toggles the combat pipeline or refreshes its action budget.
                                        player.sendSystemMessage(Component.translatable("commands.absmod.combat.probe", enabled));
                                    }
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.absmod.combat.probe", enabled), false);
                                    return targets.size();
                                }))))
                        .then(Commands.literal("inspect").then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    var player = EntityArgument.getPlayer(context, "target");
                                    context.getSource().sendSuccess(() -> Component.translatable("commands.absmod.combat.inspect",
                                            player.getDisplayName(), probe.inspect(player)), false);
                                    return 1;
                                })))));
    }
}
