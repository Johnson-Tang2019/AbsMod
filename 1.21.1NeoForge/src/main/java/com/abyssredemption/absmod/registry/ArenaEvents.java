package com.abyssredemption.absmod.registry;

import com.abyssredemption.absmod.arena.CherryArena;
import com.abyssredemption.absmod.arena.CherryPortalBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ArenaEvents {
    public static final DeferredBlock<CherryPortalBlock> PORTAL = ModBlocks.BLOCKS.registerBlock("cherry_portal",
            CherryPortalBlock::new, BlockBehaviour.Properties.of().strength(-1,3600000).noCollission().noOcclusion()
                    .noLootTable().lightLevel(s -> 11).sound(SoundType.GLASS).pushReaction(PushReaction.BLOCK));

    private ArenaEvents() {}
    public static void register() {
        var bus = NeoForge.EVENT_BUS;
        bus.addListener(ArenaEvents::activate);
        bus.addListener((LevelTickEvent.Pre e) -> {
            if (e.getLevel() instanceof ServerLevel level && level.dimension() == CherryArena.DIMENSION)
                CherryArena.prepare(level);
        });
        bus.addListener((EntityTickEvent.Pre e) -> contain(e));
        bus.addListener((EntityTickEvent.Post e) -> contain(e));
        bus.addListener((BlockEvent.BreakEvent e) -> {
            if (e.getLevel() instanceof ServerLevel level && CherryArena.locked(level)) e.setCanceled(true);
        });
        bus.addListener((PlayerInteractEvent.LeftClickBlock e) -> {
            if (e.getLevel().dimension() == CherryArena.DIMENSION) e.setCanceled(true);
        });
        bus.addListener((ExplosionEvent.Detonate e) -> {
            if (e.getLevel().dimension() == CherryArena.DIMENSION) e.getAffectedBlocks().clear();
        });
    }

    private static void contain(EntityTickEvent e) {
        if (e.getEntity().level() instanceof ServerLevel level && level.dimension() == CherryArena.DIMENSION)
            CherryArena.contain(e.getEntity(), level);
    }

    private static void activate(PlayerInteractEvent.RightClickBlock e) {
        var level = e.getLevel();
        if (level.dimension() == CherryArena.DIMENSION || e.getFace() != Direction.UP ||
                !level.getBlockState(e.getPos()).is(BlockTags.CHERRY_LOGS) ||
                !BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem()).equals(ResourceLocation.parse("absmod:fragrant_cherry_blossom"))) return;
        if (!PORTAL.get().open(level, e.getPos())) return;
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (!player.hasInfiniteMaterials()) e.getItemStack().shrink(1);
            com.abyssredemption.absmod.spirit.BladeAdvancements.award(player, "open_cherry_gate");
        }
        e.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        e.setCanceled(true);
    }
}
