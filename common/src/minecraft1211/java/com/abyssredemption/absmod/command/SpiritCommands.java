package com.abyssredemption.absmod.command;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.spirit.SpiritTransformationService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class SpiritCommands {
    private SpiritCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("absmod")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spirit")
                    .then(Commands.literal("manifest")
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                            try {
                                var entity = SpiritTransformationService.manifest(EntityArgument.getPlayer(context, "player"));
                                context.getSource().sendSuccess(() -> Component.translatable(
                                        "commands.absmod.spirit.manifest", entity.getUUID()), true);
                                return 1;
                            } catch (IllegalStateException | IllegalArgumentException exception) {
                                return failure(context.getSource(), exception);
                            }
                        })))
                    .then(Commands.literal("recall")
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("spirit", EntityArgument.entity())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, 35)).executes(context -> {
                                    try {
                                        if (!(EntityArgument.getEntity(context, "spirit") instanceof BladeSpiritEntity spirit)) {
                                            throw new IllegalStateException("inactive");
                                        }
                                        SpiritTransformationService.recall(EntityArgument.getPlayer(context, "player"), spirit,
                                                IntegerArgumentType.getInteger(context, "slot"));
                                        context.getSource().sendSuccess(() -> Component.translatable("commands.absmod.spirit.recall"), true);
                                        return 1;
                                    } catch (IllegalStateException | IllegalArgumentException exception) {
                                        return failure(context.getSource(), exception);
                                    }
                                })))))));
    }
    private static int failure(CommandSourceStack source, RuntimeException exception) {
        source.sendFailure(Component.translatable("commands.absmod.spirit.error", exception.getMessage()));
        return 0;
    }
}
