package com.abyssredemption.absmod.cat;

import com.abyssredemption.absmod.entity.BladeSpiritEntity;
import com.abyssredemption.absmod.item.SpectralBladeItem;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulRecord;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.abyssredemption.absmod.spirit.SpiritTransformationService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Inventory;

public final class CatLife {
    private CatLife() {}

    public static CatLifeStore store(ServerPlayer player) {
        return CatLifeStore.get(player.level().getServer());
    }

    static CatLifeStore.Home findHome(ServerPlayer player, UUID id) {
        var store = store(player);
        var home = store.home(id);
        if (home == null || !home.owner().equals(player.getUUID())) {
            return null;
        }
        var level = player.level().getServer().getLevel(home.spot().dimension());
        var pos = home.spot().pos();
        if (level != null && level.hasChunkAt(pos) && !(level.getBlockState(pos).getBlock() instanceof CatNestBlock)) {
            store.remove(home.spot());
            return null;
        }
        return home;
    }

    static boolean owns(Cat cat, UUID owner) {
        var reference = cat.getOwnerReference();
        return cat.isTame() && reference != null && owner.equals(reference.getUUID());
    }

    static Cat nearby(ServerPlayer player, UUID id) {
        if (id != null && player.level().getEntity(id) instanceof Cat cat && cat.isAlive() && cat.isOwnedBy(player)
                && player.distanceToSqr(cat) <= CatHomeGoal.RANGE * CatHomeGoal.RANGE) {
            return cat;
        }
        return null;
    }

    public static void select(ServerPlayer player, Cat cat) {
        if (!cat.isAlive() || !cat.isOwnedBy(player)) {
            tell(player, "not_owner");
            return;
        }
        var store = store(player);
        store.observe(player, cat);
        store.select(player.getUUID(), cat.getUUID());
        tell(player, "selected", cat.getDisplayName());
    }

    public static void bind(ServerPlayer player, BlockPos pos) {
        var store = store(player);
        var spot = GlobalPos.of(player.level().dimension(), pos);
        var current = store.at(spot);
        if (current != null && !current.owner().equals(player.getUUID())) {
            tell(player, "private_nest");
            return;
        }
        var cat = nearby(player, store.selected(player.getUUID()));
        if (cat == null || cat.distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos)) > CatHomeGoal.RANGE * CatHomeGoal.RANGE) {
            tell(player, "need_cat", CatHomeGoal.RANGE);
            return;
        }
        store.bind(spot, player.getUUID(), cat.getUUID());
        sendToNest(cat);
        store.observe(player, cat);
        BladeAdvancements.award(player, "cat_nest");
        tell(player, "bound", cat.getDisplayName());
    }

    public static void useNest(ServerPlayer player, BlockPos pos, boolean unbind) {
        var store = store(player);
        var spot = GlobalPos.of(player.level().dimension(), pos);
        var home = store.at(spot);
        if (home != null && !home.owner().equals(player.getUUID())) {
            tell(player, "private_nest");
            return;
        }
        if (home == null || home.cat().isEmpty()) {
            tell(player, "empty_nest");
            return;
        }
        if (unbind) {
            store.place(spot, player.getUUID());
            tell(player, "unbound");
            return;
        }
        UUID id = home.cat().orElseThrow();
        var cat = nearby(player, id);
        var name = name(store.observation(player.getUUID(), id));
        if (cat == null) {
            tell(player, "nest_away", name);
        } else {
            store.setResting(home, true);
            sendToNest(cat);
            store.observe(player, cat);
            tell(player, "bound", cat.getDisplayName());
        }
    }

    private static void sendToNest(Cat cat) {
        ((CatHomeGoal.Carrier) cat).absmod$homeGoal().cancelCall();
        cat.setOrderedToSit(false);
    }

    public static void callSelected(ServerPlayer player) {
        call(player, store(player).selected(player.getUUID()));
    }

    static boolean call(ServerPlayer player, UUID id) {
        var cat = nearby(player, id);
        if (cat == null) {
            tell(player, "need_cat", CatHomeGoal.RANGE);
            return false;
        }
        var store = store(player);
        store.observe(player, cat);
        if (cat.distanceToSqr(player) > 2.25) {
            var path = cat.getNavigation().createPath(player, 1);
            if (path == null || !path.canReach()) {
                tell(player, "no_path", cat.getDisplayName());
                return false;
            }
        }
        store.setResting(store.home(cat.getUUID()), false);
        cat.setOrderedToSit(false);
        ((CatHomeGoal.Carrier) cat).absmod$homeGoal().call(player.getUUID());
        BladeAdvancements.award(player, "cat_bell");
        tell(player, "called", cat.getDisplayName());
        return true;
    }

    static List<BladeSoulRecord> spirits(ServerPlayer player) {
        try {
            return BladeSoulStore.find(player.level().getServer()).stream().flatMap(store -> store.records().stream())
                    .filter(record -> isOwnedSpirit(player, record))
                    .sorted(java.util.Comparator.comparing(BladeSoulRecord::soulId)).toList();
        } catch (IllegalStateException unavailable) {
            return List.of();
        }
    }

    static java.util.Optional<BladeSoulRecord> spirit(ServerPlayer player, UUID id) {
        try {
            return BladeSoulStore.find(player.level().getServer()).flatMap(store -> store.find(id))
                    .filter(record -> isOwnedSpirit(player, record));
        } catch (IllegalStateException unavailable) {
            return java.util.Optional.empty();
        }
    }

    private static boolean isOwnedSpirit(ServerPlayer player, BladeSoulRecord record) {
        return record.carrier().isPresent() && record.ownerId().filter(player.getUUID()::equals).isPresent();
    }

    static BladeSpiritEntity nearbySpirit(ServerPlayer player, BladeSoulRecord record) {
        var carrier = record.carrier().orElseThrow();
        return record.form().equals("spirit") && player.level().getEntity(carrier.entityId()) instanceof BladeSpiritEntity spirit
                && spirit.isAlive() && spirit.active() && Objects.equals(spirit.soulId(), record.soulId())
                && player.distanceToSqr(spirit) <= CatHomeGoal.RANGE * CatHomeGoal.RANGE ? spirit : null;
    }

    static void recall(ServerPlayer player, BladeSoulRecord record) {
        var spirit = nearbySpirit(player, record);
        if (spirit == null) {
            tell(player, "spirit_away");
            return;
        }
        int destination = record.carrier().orElseThrow().sourceSlot();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            var stack = player.getInventory().getItem(slot);
            var identity = stack.get(BladeSoulComponents.REFERENCE);
            if (stack.getItem() instanceof SpectralBladeItem && identity != null && identity.soulId().equals(record.soulId())) {
                destination = slot;
                break;
            }
        }
        try {
            SpiritTransformationService.recall(player, spirit, destination);
            tell(player, "recalled");
        } catch (IllegalStateException error) {
            tell(player, "recall_blocked");
        }
    }

    public static Component name(CatLifeStore.Observation row) {
        return row == null ? label("unrecorded") : row.name().isBlank()
                ? Component.translatable("entity.minecraft.cat") : Component.literal(row.name());
    }

    static Component position(GlobalPos spot) {
        return label("position", spot.dimension().identifier().toString(), spot.pos().getX(), spot.pos().getY(), spot.pos().getZ());
    }

    static Component label(String key, Object... arguments) {
        return Component.translatable("cats.absmod." + key, arguments);
    }

    static void tell(ServerPlayer player, String key, Object... arguments) {
        if (player.connection != null) {
            player.sendSystemMessage(label(key, arguments), false);
        }
    }
}
