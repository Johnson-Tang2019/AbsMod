package com.abyssredemption.absmod.command;

import com.abyssredemption.absmod.item.MeowBladeStage;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModCommands {
    private ModCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
            Function<MeowBladeStage, Item> itemLookup) {
        dispatcher.register(Commands.literal("absmod")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> help(context.getSource()))
                .then(Commands.literal("help").executes(context -> help(context.getSource())))
                .then(Commands.literal("stages").executes(context -> stages(context.getSource())))
                .then(Commands.literal("kit")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> kit(context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"), itemLookup))))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> inspect(context.getSource(),
                                        EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("stage", IntegerArgumentType.integer(1, 9))
                                        .executes(context -> give(context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                MeowBladeStage.values()[IntegerArgumentType.getInteger(
                                                        context, "stage") - 1], itemLookup))))));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.absmod.help"), false);
        return 1;
    }

    private static int stages(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.absmod.stages.header"), false);
        for (MeowBladeStage stage : MeowBladeStage.values()) {
            source.sendSuccess(() -> Component.translatable("commands.absmod.stages.entry",
                    stage.number(), "absmod:" + stage.itemId(), stage.attackDamage(), 1.6F), false);
        }
        return MeowBladeStage.values().length;
    }

    private static int kit(CommandSourceStack source, Collection<ServerPlayer> targets,
            Function<MeowBladeStage, Item> itemLookup) {
        for (ServerPlayer player : targets) {
            for (MeowBladeStage stage : MeowBladeStage.values()) {
                deliver(player, itemLookup.apply(stage));
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.absmod.kit.success", targets.size()), true);
        return targets.size();
    }

    private static int inspect(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int matches = 0;
        for (ServerPlayer player : targets) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof MeowBladeItem blade) {
                source.sendSuccess(() -> Component.translatable("commands.absmod.inspect.entry",
                        player.getDisplayName(), blade.stage().number(),
                        stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()), false);
                matches++;
            } else {
                source.sendSuccess(() -> Component.translatable(
                        "commands.absmod.inspect.empty", player.getDisplayName()), false);
            }
        }
        return matches;
    }

    private static int give(CommandSourceStack source, Collection<ServerPlayer> targets,
            MeowBladeStage stage, Function<MeowBladeStage, Item> itemLookup) {
        Item item = itemLookup.apply(stage);
        for (ServerPlayer player : targets) {
            deliver(player, item);
        }
        source.sendSuccess(() -> Component.translatable(
                "commands.absmod.give.success", stage.number(), targets.size()), true);
        return targets.size();
    }

    private static void deliver(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            // Match vanilla give behavior when the target inventory is full.
            ItemEntity dropped = player.drop(stack, false);
            if (dropped != null) {
                dropped.setNoPickUpDelay();
                dropped.setTarget(player.getUUID());
            }
        }
        player.containerMenu.broadcastChanges();
    }
}
