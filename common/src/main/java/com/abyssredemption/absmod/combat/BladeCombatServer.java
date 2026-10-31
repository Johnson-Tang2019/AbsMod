package com.abyssredemption.absmod.combat;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import com.abyssredemption.absmod.spirit.BladeSoulComponents;
import com.abyssredemption.absmod.spirit.BladeSoulReference;
import com.abyssredemption.absmod.spirit.BladeSoulService;
import com.abyssredemption.absmod.spirit.BladeSoulStore;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** One authoritative attack budget per connected player; optional tracing shares the same pipeline. */
public final class BladeCombatServer {
    private final Map<UUID, State> players = new HashMap<>();
    private long tick;
    private final java.util.function.BiPredicate<ServerPlayer, net.minecraft.world.phys.EntityHitResult> attackAllowed;
    private final java.util.function.BiConsumer<ServerPlayer, com.abyssredemption.absmod.network.BladeSwingPayload> send;

    public BladeCombatServer(java.util.function.BiConsumer<ServerPlayer, com.abyssredemption.absmod.network.BladeSwingPayload> send) {
        this(send, (player, hit) -> true);
    }

    public BladeCombatServer(java.util.function.BiConsumer<ServerPlayer, com.abyssredemption.absmod.network.BladeSwingPayload> send,
            java.util.function.BiPredicate<ServerPlayer, net.minecraft.world.phys.EntityHitResult> attackAllowed) {
        this.send = send;
        this.attackAllowed = attackAllowed;
    }

    public BladeCombatServer() { this((player, payload) -> {}); }

    public void enable(ServerPlayer player, boolean enabled) {
        State state = players.computeIfAbsent(player.getUUID(), id -> new State());
        state.tracing = enabled;
    }

    public void disconnect(ServerPlayer player) { BladeComboAttributes.update(player,0); players.remove(player.getUUID()); }
    public void clear() { players.clear(); BladeStun.clear(); tick = 0; }

    public void receiveHeavy(ServerPlayer player,com.abyssredemption.absmod.network.HeavyIntentPayload payload) {
        var state=players.computeIfAbsent(player.getUUID(),id->new State());
        if(payload.sequence()<=state.heavySequence)return;
        state.heavySequence=payload.sequence();
        if(!payload.active()){state.heavyPending=false;return;}
        if(state.heavyPending || tick<state.nextHeavy || !eligible(player)
            || payload.slot()!=player.getInventory().getSelectedSlot()
            || !payload.dimension().equals(player.level().dimension().identifier())
            || !(player.getMainHandItem().getItem() instanceof MeowBladeItem blade)
            || blade.stage().route()!=com.abyssredemption.absmod.item.BladeRoute.DARK)return;
        state.heavyPending=true;state.heavyAt=tick+12;state.nextHeavy=tick+160;
        state.heavyWeapon=player.getMainHandItem();state.heavySoul=state.heavyWeapon.get(BladeSoulComponents.REFERENCE);
        state.heavyLevel=player.level();state.heavySlot=payload.slot();
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("combat.absmod.heavy_windup"),true);
    }

    public void receive(ServerPlayer player, AttackIntentPayload payload) {
        State state = players.computeIfAbsent(player.getUUID(), id -> new State());
        state.session.active(tick);
        if (payload.slot() < 0 || payload.slot() > 8) { return; }
        boolean newEpoch = payload.epoch() > state.session.epoch();
        if (!state.session.accept(payload.epoch(), payload.sequence(), payload.held(), tick)) { return; }
        if (!payload.held()) { state.weapon = null; return; }
        if (payload.dimension().equals(player.level().dimension().identifier())
                && payload.slot() == player.getInventory().getSelectedSlot()
                && player.isAlive() && !player.isSpectator() && !player.isSleeping() && !player.isUsingItem()
                && !player.isChangingDimension() && player.connection.hasClientLoaded()
                && player.containerMenu == player.inventoryMenu
                && player.getMainHandItem().getItem() instanceof com.abyssredemption.absmod.item.SpectralBladeItem) {
            if (!com.abyssredemption.absmod.spirit.SpiritTransformationService.recallForAttack(player)) {
                state.session.stop(); return;
            }
        }
        if (!payload.dimension().equals(player.level().dimension().identifier())
                || !eligible(player) || payload.slot() != player.getInventory().getSelectedSlot()) {
            state.session.stop();
            return;
        }
        if (newEpoch) {
            state.weapon = player.getMainHandItem();
            state.soul = state.weapon.get(BladeSoulComponents.REFERENCE);
            state.level = player.level();
            state.slot = payload.slot();
        }
        if (!sameWeapon(player, state)) { state.session.stop(); }
    }

    public void tick(MinecraftServer server) {
        tick++;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            State state = players.get(player.getUUID());
            if (state == null) { continue; }
            updateCombo(player, state);
            if(state.heavyPending && (!eligible(player) || state.heavyWeapon!=player.getMainHandItem()
                || state.heavyLevel!=player.level() || state.heavySlot!=player.getInventory().getSelectedSlot()
                || !Objects.equals(state.heavySoul,player.getMainHandItem().get(BladeSoulComponents.REFERENCE))))state.heavyPending=false;
            boolean heavy=state.heavyPending && tick>=state.heavyAt;
            boolean active = state.session.active(tick) && eligible(player) && sameWeapon(player, state);
            if (!active) { state.session.stop(); state.weapon = null; }
            if(state.heavyPending)active=heavy;
            // The authoritative effective attribute already includes any external modifier once.
            double speed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            if (!Double.isFinite(speed) || speed <= 0) { state.session.stop(); active = false; }
            if (active && speed > CombatProbeSettings.MAX_ACTIONS_PER_SECOND && !state.capReported) {
                LogUtils.getLogger().info("AbsMod combat probe caps action budget at {} actions/s for {}; actual attribute={}",
                        CombatProbeSettings.MAX_ACTIONS_PER_SECOND, player.getUUID(), speed);
                state.capReported = true;
            }
            net.minecraft.world.phys.HitResult hit = active ? BladeAttackExecutor.pick(player) : null;
            if (hit != null && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                active = false;
                state.heavyPending=false;
                state.session.stop();
            }
            if (state.cadence.tick(tick, active, speed, player.getMainHandItem())) {
                state.authorized++;
                state.lastAttackId = UUID.randomUUID();
                var blade = (MeowBladeItem) player.getMainHandItem().getItem();
                int stacksBefore = state.combo.stacks();
                boolean confirmedHit;
                var damage=player.getAttribute(Attributes.ATTACK_DAMAGE);
                var heavyId=net.minecraft.resources.Identifier.fromNamespaceAndPath("absmod","heavy_damage");
                try {
                    if(heavy)damage.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        heavyId,blade.stage().attackDamage()*.8,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                    confirmedHit = (!(hit instanceof net.minecraft.world.phys.EntityHitResult entityHit)
                            || attackAllowed.test(player, entityHit)) && BladeAttackExecutor.attack(player, hit);
                } finally { if(heavy){damage.removeModifier(heavyId);state.heavyPending=false;} }
                if(heavy && confirmedHit && hit instanceof net.minecraft.world.phys.EntityHitResult heavyHit) {
                    BladeStun.apply(heavyHit.getEntity());
                    com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"dark_heavy");
                }
                updateCombo(player, state);
                if (confirmedHit && eligible(player) && (heavy || sameWeapon(player, state))) {
                    state.combo.recordHit(tick, state.authorized, blade.stage().maxComboStacks());
                    if(hit instanceof net.minecraft.world.phys.EntityHitResult entityHit)
                        com.abyssredemption.absmod.spirit.BladeAffection.hit(player,entityHit.getEntity());
                }
                if (state.tracing) {
                    LogUtils.getLogger().info("AbsMod blade attack player={} tick={} attackId={} epoch={} stage={} speed={} primaryHit={} stacksBefore={} stacksAfter={}",
                            player.getUUID(), tick, state.lastAttackId, state.session.epoch(), blade.stage().number(), speed, confirmedHit, stacksBefore, state.combo.stacks());
                }
                if(heavy)LogUtils.getLogger().info("AbsMod heavy attack player={} attackId={} hit={}",player.getUUID(),state.lastAttackId,confirmedHit);
                var swing = new com.abyssredemption.absmod.network.BladeSwingPayload(player.getUUID(), state.lastAttackId,
                        state.authorized,player.level().getGameTime(),(float)(20.0/speed));
                for (var viewer : server.getPlayerList().getPlayers()) {
                    if (viewer.level() == player.level() && viewer.distanceToSqr(player) <= 32 * 32) { send.accept(viewer, swing); }
                }
            }
        }
    }

    public String inspect(ServerPlayer player) {
        State state = players.get(player.getUUID());
        return state == null ? "idle" : "tracing=" + state.tracing + ", held=" + state.session.active(tick)
                + ", epoch=" + state.session.epoch() + ", sequence=" + state.session.sequence()
                + ", comboStacks=" + state.combo.stacks() + ", comboBonusesApplied=true"
                + ", authorized=" + state.authorized + ", lastAttackId=" + state.lastAttackId + ", automaticDamage=true";
    }

    private void updateCombo(ServerPlayer player, State state) {
        var stack = player.getMainHandItem();
        boolean bound = player.isAlive() && !player.isSpectator() && !player.isChangingDimension()
                && stack.getCount() == 1 && stack.getItem() instanceof MeowBladeItem;
        state.combo.bind(bound ? stack : null, stack.get(BladeSoulComponents.REFERENCE),
                player.level(), player.getInventory().getSelectedSlot());
        state.combo.advance(tick);
        BladeComboAttributes.update(player,state.combo.stacks());
    }

    private static boolean sameWeapon(ServerPlayer player, State state) {
        return state.weapon == player.getMainHandItem() && state.level == player.level()
                && state.slot == player.getInventory().getSelectedSlot() && state.weapon != null
                && Objects.equals(state.soul, state.weapon.get(BladeSoulComponents.REFERENCE));
    }

    private static boolean eligible(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isSleeping() || player.isUsingItem() || player.getMainHandItem().isBroken()
                || player.getCooldowns().isOnCooldown(player.getMainHandItem())
                || !player.getMainHandItem().isItemEnabled(player.level().enabledFeatures())
                || player.isChangingDimension() || !player.connection.hasClientLoaded()
                || player.containerMenu != player.inventoryMenu
                || player.getMainHandItem().getCount() != 1 || !(player.getMainHandItem().getItem() instanceof MeowBladeItem)) {
            return false;
        }
        // Existing soul references must remain valid; probing a new blade never initializes it.
        if (player.getMainHandItem().has(BladeSoulComponents.REFERENCE)) {
            try {
                var store = BladeSoulStore.find(player.level().getServer());
                if (store.isEmpty()) { return false; }
                BladeSoulService.requireRecord(store.get(), player.getMainHandItem());
            } catch (IllegalStateException exception) { return false; }
        }
        return true;
    }

    private static final class State {
        final AttackInputSession session = new AttackInputSession();
        final BladeComboState combo = new BladeComboState();
        final AttackCadence cadence = new AttackCadence();
        boolean tracing;
        boolean capReported;
        ItemStack weapon;
        BladeSoulReference soul;
        Object level;
        int slot;
        long authorized;
        UUID lastAttackId;
        long heavySequence=-1,heavyAt,nextHeavy;
        boolean heavyPending;
        ItemStack heavyWeapon;
        BladeSoulReference heavySoul;
        Object heavyLevel;
        int heavySlot;
    }
}
