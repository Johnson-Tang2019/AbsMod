package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.compat.WornCharms;
import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.RoseCharmItem;
import com.abyssredemption.absmod.item.SpiritKeepsake.Kind;
import com.abyssredemption.absmod.item.SpiritKeepsake;
import com.abyssredemption.absmod.registry.RoseEquipment;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
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
            server.serverLevel().sendParticles(ParticleTypes.CHERRY_LEAVES,
                    event.getPos().getX() + 0.5, event.getPos().getY() + 1, event.getPos().getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0);
        }
    }

    public static void tune(PlayerInteractEvent.RightClickBlock event) {
        var p = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || p.isShiftKeyDown() || p.isSpectator()
                || !(p.getMainHandItem().getItem() instanceof MeowBladeItem)
                || !p.getOffhandItem().is(Items.COMPASS) || p.getOffhandItem().has(DataComponents.LODESTONE_TRACKER)
                || !event.getLevel().getBlockState(event.getPos()).is(Blocks.CARTOGRAPHY_TABLE)
                || !event.getLevel().mayInteract(p, event.getPos())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(p.level().isClientSide));
        if (p instanceof ServerPlayer server) {
            if (SpiritKeepsake.canTuneCompass(server)) SpiritKeepsake.exchange(server, Items.COMPASS, Kind.WHISKER_COMPASS);
            else server.displayClientMessage(Component.translatable("keepsake.absmod.stage_required"), true);
        }
    }

    private static boolean worn(Player player, Kind kind) {
        return ModList.get().isLoaded("curios")
                ? WornCharms.contains(player, kind.item())
                : player.getOffhandItem().is(kind.item());
    }

    public static void land(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isSpectator() && player.isShiftKeyDown()
                && event.getDistance() >= 2.5F && worn(player, Kind.PETAL_ANKLET)) SpiritKeepsake.gatherLooseItems(player);
    }

    public static void catchRain(PlayerInteractEvent.RightClickItem event) {
        var p = event.getEntity();
        if (p.isSpectator() || event.getHand() != InteractionHand.MAIN_HAND || !p.getMainHandItem().is(Items.GLASS_BOTTLE)
                || p.getXRot() > -45 || !p.level().isRainingAt(p.blockPosition().above()) || !worn(p, Kind.RAINFISH_LOCKET)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(p.level().isClientSide));
        if (p instanceof ServerPlayer server) {
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemUtils.createFilledResult(p.getMainHandItem(), p,
                    PotionContents.createItemStack(Items.POTION, Potions.WATER)));
            BladeAdvancements.award(server, Kind.RAINFISH_LOCKET.id());
            server.displayClientMessage(Component.translatable("keepsake.absmod.rainwater"), true);
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.tickCount % 10 != 0) return;
        if (player.isShiftKeyDown() && worn(player, Kind.WHISKER_COMPASS)) SpiritKeepsake.traceCompanion(player);
        if (ModList.get().isLoaded("curios")) {
            WornCharms.apply(player);
        } else if (player.getOffhandItem().getItem() instanceof RoseCharmItem charm) {
            charm.apply(player);
        }
    }
}
