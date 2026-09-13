package com.abyssredemption.absmod.command;

import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulRecord;
import com.abyssredemption.absmod.spirit.BladeSoulReference;
import com.abyssredemption.absmod.spirit.BladeSoulService;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** Operator-only storage probes; these commands do not define final relationship gameplay. */
public final class SoulCommands {
    private SoulCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var soul = Commands.literal("soul")
                .executes(context -> help(context.getSource()));
        for (String operation : List.of("init", "inspect")) {
            soul.then(Commands.literal(operation).then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> execute(context.getSource(), EntityArgument.getPlayer(context, "target"), operation, 0))));
        }
        var affection = Commands.literal("affection");
        for (String operation : List.of("set", "add")) {
            affection.then(Commands.literal(operation).then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes(context ->
                            execute(context.getSource(), EntityArgument.getPlayer(context, "target"), operation,
                                    DoubleArgumentType.getDouble(context, "value"))))));
        }
        dispatcher.register(Commands.literal("absmod").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(soul.then(affection)));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.absmod.soul.help"), false);
        return 1;
    }

    public static int inspect(CommandSourceStack source, ServerPlayer player) {
        return execute(source, player, "inspect", 0);
    }

    private static int execute(CommandSourceStack source, ServerPlayer player, String operation, double value) {
        try {
            ItemStack stack = player.getMainHandItem();
            if (stack.getCount() != 1 || !(stack.getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem)) {
                throw new IllegalStateException("not_blade");
            }
            MinecraftServer server = source.getServer();
            BladeSoulReference reference = stack.get(BladeSoulComponents.REFERENCE);
            if (reference == null && !operation.equals("init")) { throw new IllegalStateException("uninitialized"); }
            if (reference != null) {
                List<String> locations = findLoadedCopies(server, reference.soulId());
                if (locations.size() > 1) {
                    source.sendFailure(Component.translatable("commands.absmod.soul.duplicate", reference.soulId(),
                            String.join(", ", locations)));
                    return 0;
                }
            }
            // A missing referenced record must never cause an empty replacement store to be written.
            BladeSoulStore store = reference == null && operation.equals("init")
                    ? BladeSoulStore.forInitialization(server)
                    : BladeSoulStore.find(server).orElseThrow(() -> new IllegalStateException("missing_record"));
            BladeSoulRecord record;
            if (operation.equals("init")) {
                record = BladeSoulService.initialize(store, stack, player.getUUID());
                player.containerMenu.broadcastChanges();
            } else {
                record = BladeSoulService.requireRecord(store, stack);
            }
            if (operation.equals("set") || operation.equals("add")) {
                if (!record.ownerId().equals(java.util.Optional.of(player.getUUID()))) {
                    throw new IllegalStateException("owner_mismatch");
                }
                double result = operation.equals("set") ? value : record.affection()
                        .orElseThrow(() -> new IllegalStateException("affection_unset")) + value;
                BladeSoulRecord before = record;
                record = BladeSoulService.setTestAffection(store, stack, result);
                LogUtils.getLogger().info("AbsMod test affection operation={} actor={} soul={} before={} after={} revision={}",
                        UUID.randomUUID(), source.getTextName(), record.soulId(), before.affection(), record.affection(), record.revision());
            }
            BladeSoulRecord output = record;
            source.sendSuccess(() -> Component.translatable("commands.absmod.soul.entry", player.getDisplayName(),
                    output.soulId().toString(), output.ownerId().map(UUID::toString).orElse("-"),
                    output.route().getSerializedName(), output.stage(),
                    output.affection().<Object>map(v -> v).orElse(Component.translatable("commands.absmod.soul.unset")),
                    output.form(), output.revision()), !operation.equals("inspect"));
            return 1;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            source.sendFailure(Component.translatable("commands.absmod.soul.error." + exception.getMessage()));
            return 0;
        }
    }

    /** Diagnostic scope: online inventories/ender chests and loaded dropped items, not unloaded containers. */
    public static List<String> findLoadedCopies(MinecraftServer server, UUID id) {
        List<String> locations = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scan(player.getInventory(), player.getUUID() + "/inventory/", id, locations);
            scan(player.getEnderChestInventory(), player.getUUID() + "/ender_chest/", id, locations);
        }
        for (var level : server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity item && item.isAlive()) {
                    BladeSoulReference reference = item.getItem().get(BladeSoulComponents.REFERENCE);
                    if (reference != null && reference.soulId().equals(id)) {
                        locations.add(level.dimension().identifier() + "/item/" + item.getUUID());
                    }
                }
            }
        }
        return locations;
    }

    private static void scan(Container container, String prefix, UUID id, List<String> locations) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            BladeSoulReference reference = container.getItem(slot).get(BladeSoulComponents.REFERENCE);
            if (reference != null && reference.soulId().equals(id)) { locations.add(prefix + slot); }
        }
    }
}
