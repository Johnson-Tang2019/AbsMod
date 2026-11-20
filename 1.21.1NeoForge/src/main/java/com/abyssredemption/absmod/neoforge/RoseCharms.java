package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.RoseCharmItem;
import com.abyssredemption.absmod.registry.RoseEquipment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class RoseCharms {
    private RoseCharms() {}

    public static void engrave(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof MeowBladeItem)
                || !player.getOffhandItem().is(Items.AMETHYST_SHARD)
                || !event.getLevel().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)
                || !event.getLevel().mayInteract(player, event.getPos())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (player instanceof ServerPlayer server) {
            player.getOffhandItem().consume(1, player);
            server.getInventory().placeItemBackInInventory(RoseEquipment.PAW_CRYSTAL.toStack());
            server.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CHERRY_LEAVES,
                    event.getPos().getX() + 0.5, event.getPos().getY() + 1, event.getPos().getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0);
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.tickCount % 10 != 0) return;
        if (ModList.get().isLoaded("curios")) {
            com.abyssredemption.absmod.compat.WornCharms.apply(player);
        } else if (player.getOffhandItem().getItem() instanceof RoseCharmItem charm) {
            charm.apply(player);
        }
    }
}
