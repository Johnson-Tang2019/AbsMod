package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

/** Bond keepsakes are exchanged for a specified offering after a server-verified action. */
public final class SpiritKeepsake extends Item {
    public enum Kind {
        WHISKER_COMPASS, PETAL_ANKLET, RAINFISH_LOCKET;
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public Item item() { return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod", id())); }
    }

    private final Kind kind;

    public SpiritKeepsake(Kind kind, Properties properties) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("keepsake.absmod." + kind.id()));
        lines.add(Component.translatable("charm.absmod.equip"));
    }

    private static Optional<BladeSoulStore> readStore(ServerPlayer player) {
        try {
            return BladeSoulStore.find(player.server);
        } catch (IllegalStateException error) {
            if ("server_thread".equals(error.getMessage())) throw error;
            com.mojang.logging.LogUtils.getLogger().warn("Could not read the blade record for a keepsake exchange", error);
            player.sendSystemMessage(Component.translatable("resonance.absmod.store_unavailable"));
            return Optional.empty();
        }
    }

    public static boolean canTuneCompass(ServerPlayer player) {
        var blade = player.getMainHandItem();
        var ref = blade.get(BladeSoulComponents.REFERENCE);
        if (!(blade.getItem() instanceof MeowBladeItem item) || ref == null || item.stage().number() < 2) return false;
        return readStore(player).flatMap(store -> store.find(ref.soulId()))
                .filter(record -> record.form().equals("sword") && record.ownerId().filter(player.getUUID()::equals).isPresent()
                        && record.route() == item.stage().route() && record.stage() == item.stage().number()).isPresent();
    }

    public static void finishGame(ServerPlayer player, BladeSpiritEntity spirit) {
        if (!player.getOffhandItem().is(Items.RABBIT_FOOT) || !player.getUUID().equals(spirit.ownerId()) || !spirit.active()) return;
        if (readStore(player).flatMap(store -> store.find(spirit.soulId()))
                .filter(record -> record.stage() >= 3).isPresent()) exchange(player, Items.RABBIT_FOOT, Kind.PETAL_ANKLET);
    }

    public static void receiveGift(ServerPlayer player, BladeSpiritEntity spirit) {
        if (player.serverLevel().isRainingAt(spirit.blockPosition())) exchange(player, Items.GLASS_BOTTLE, Kind.RAINFISH_LOCKET);
    }

    public static void exchange(ServerPlayer player, Item ingredient, Kind result) {
        var offering = player.getOffhandItem();
        if (!offering.is(ingredient)) return;
        // This is a deliberate material exchange even in creative. The original sword is never an ingredient.
        offering.shrink(1);
        var keepsake = new ItemStack(result.item());
        player.getInventory().placeItemBackInInventory(keepsake);
        player.displayClientMessage(Component.translatable("keepsake.absmod.received", result.item().getDescription()), true);
    }

    public static void traceCompanion(ServerPlayer player) {
        Item compass = Kind.WHISKER_COMPASS.item();
        if (!player.isShiftKeyDown() || player.getCooldowns().isOnCooldown(compass)) return;
        player.getCooldowns().addCooldown(compass, 80);
        var nearby = player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24),
                e -> e.isAlive() && player.distanceToSqr(e) <= 24 * 24 &&
                        (e instanceof Cat cat && cat.isOwnedBy(player) || e instanceof BladeSpiritEntity spirit
                                && player.getUUID().equals(spirit.ownerId()) && spirit.active()));
        var target = nearby.stream().min(Comparator.comparingDouble(player::distanceToSqr));
        if (target.isEmpty()) {
            player.displayClientMessage(Component.translatable("keepsake.absmod.quiet"), true);
            return;
        }
        var pet = target.get();
        BladeAdvancements.award(player, Kind.WHISKER_COMPASS.id());
        Vec3 start = player.getEyePosition().add(0, -0.45, 0);
        Vec3 delta = pet.getEyePosition().subtract(start);
        int dots = Math.max(1, (int)Math.ceil(Math.min(delta.length(), 6) * 2));
        Vec3 step = delta.normalize().scale(0.5);
        for (int i = 1; i <= dots; i++) {
            Vec3 point = start.add(step.scale(i));
            player.serverLevel().sendParticles(player, ParticleTypes.CHERRY_LEAVES, true, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        player.displayClientMessage(Component.translatable("keepsake.absmod.trail", pet.getDisplayName(),
                Math.round(player.distanceTo(pet))), true);
    }

    public static void gatherLooseItems(ServerPlayer player) {
        var level = player.serverLevel();
        var drops = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4),
                drop -> drop.isAlive() && !drop.hasPickUpDelay() && player.distanceToSqr(drop) <= 16 && player.hasLineOfSight(drop));
        int collected = 0;
        for (var drop : drops) {
            int before = drop.getItem().getCount();
            drop.playerTouch(player);
            if (drop.isRemoved() || drop.getItem().getCount() < before) collected++;
            if (collected == 12) break;
        }
        if (collected != 0) {
            BladeAdvancements.award(player, Kind.PETAL_ANKLET.id());
            level.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 0.2, player.getZ(), 9, 0.7, 0.1, 0.7, 0);
            player.displayClientMessage(Component.translatable("keepsake.absmod.gathered", collected), true);
        }
    }
}
