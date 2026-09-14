package com.abyssredemption.absmod.combat;

import com.abyssredemption.absmod.item.MeowBladeStage;
import com.abyssredemption.absmod.network.AttackIntentPayload;
import com.abyssredemption.absmod.network.CombatProbePayload;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** Runs production input/cadence logic; does not claim physical input, networking or damage acceptance. */
public final class CombatVerification {
    public static void verify() throws Exception {
        verifyStages();
        verifyCadence();
        verifySessions();
        verifyClientEdges();
        verifyPayloads();
        verifyAttackScope();
        verifyComboState();
        System.out.println("PASS: candidate stage attributes, fractional cadence, tap budgets, expiry/replay, client stop/rearm and packet codecs (no game runtime)");
    }

    private static void verifyAttackScope() {
        Object player = new Object(), target = new Object(), source = new Object();
        check(!BladeAttackScope.fullStrength(player), "Ordinary attacks inherited full strength");
        try {
            BladeAttackScope.run(player, target, () -> {
                BladeAttackScope.enterAttack();
                check(BladeAttackScope.fullStrength(player), "Primary attack not authorized");
                check(!BladeAttackScope.fullStrength(new Object()), "Authorization leaked to another player");
                BladeAttackScope.bindSource(player, source);
                BladeAttackScope.enterAttack();
                check(!BladeAttackScope.fullStrength(player), "Nested attack inherited charge bypass");
                check(!BladeAttackScope.claimPrimary(target, source), "Nested damage claimed primary");
                BladeAttackScope.exitAttack();
                check(!BladeAttackScope.claimPrimary(new Object(), source), "Sweep target inherited bypass");
                check(!BladeAttackScope.claimPrimary(target, new Object()), "Other source inherited bypass");
                check(BladeAttackScope.claimPrimary(target, source), "Primary damage not authorized");
                check(!BladeAttackScope.claimPrimary(target, source), "Duplicate damage reused authorization");
                throw new IllegalArgumentException("Expected cleanup test");
            });
        } catch (IllegalArgumentException expected) { }
        check(!BladeAttackScope.fullStrength(player) && !BladeAttackScope.claimPrimary(target, source), "Exception leaked scope");
        BladeAttackScope.run(player, target, () -> {});
        System.out.println("PASS: primary attack scope isolation, nested damage rejection and exception cleanup");
    }

    private static void verifyComboState() {
        var combo = new BladeComboState();
        Object blade = new Object(), soul = new Object(), world = new Object();
        combo.bind(blade, soul, world, 0);
        check(combo.stacks() == 0, "First attack must start at zero stacks");
        for (int action = 1; action <= 25; action++) { combo.recordHit(0, action, 20); }
        check(combo.stacks() == 20, "Stack cap failed");
        combo.advance(64);
        check(combo.stacks() == 20, "Decayed before tick 65");
        combo.advance(65);
        check(combo.stacks() == 19, "First decay missing");
        check(!combo.recordHit(69, 25, 20), "Duplicate hit refreshed grace");
        combo.advance(70);
        check(combo.stacks() == 18, "Duplicate result delayed decay");
        combo.recordHit(75, 26, 20);
        check(combo.stacks() == 18, "New hit failed to decay before stacking");
        combo.bind(blade, soul, world, 0);
        combo.advance(139);
        check(combo.stacks() == 18, "Release/same binding erased combo");
        combo.advance(140);
        check(combo.stacks() == 17, "New grace boundary incorrect");
        combo.bind(new Object(), soul, world, 0);
        check(combo.stacks() == 0, "Replacement weapon retained stacks");
        check(!combo.recordHit(141, 26, 20), "Old action replayed after swap");
        combo.recordHit(141, 27, 5);
        combo.bind(blade, new Object(), world, 0);
        check(combo.stacks() == 0, "Soul swap retained stacks");
        combo.recordHit(142, 28, 5);
        combo.bind(null, null, world, 0);
        check(combo.stacks() == 0 && !combo.recordHit(143, 29, 5), "Unbound state accepted hit");
        check(new BladeComboState().stacks() == 0, "Players share stacks");
        System.out.println("PASS: combo caps, deduplication, grace/decay and weapon identity clearing");
    }

    private static void verifyStages() throws Exception {
        var profile = JsonParser.parseString(Files.readString(Path.of(System.getProperty("absmod.balanceProfile"))))
                .getAsJsonObject().getAsJsonArray("normal");
        check(profile.size() == MeowBladeStage.values().length, "Stage count differs from design");
        for (var value : profile) {
            var row = value.getAsJsonObject();
            var stage = MeowBladeStage.values()[row.get("stage").getAsInt() - 1];
            check(stage.maxComboStacks() == row.get("max_combo_stacks").getAsInt(), "Combo cap differs from design");
            check(row.get("item_id").getAsString().equals("absmod:" + stage.itemId()), "Changed registered ID");
            near(stage.attackDamage(), row.get("base_total_damage").getAsDouble());
            near(stage.baseAttackSpeed(), row.get("base_attack_speed").getAsDouble());
            var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", stage.itemId())));
            var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            near(modifiers.compute(Attributes.ATTACK_DAMAGE, 1, EquipmentSlot.MAINHAND), stage.attackDamage());
            near(modifiers.compute(Attributes.ATTACK_SPEED, 4, EquipmentSlot.MAINHAND), stage.baseAttackSpeed());
            near(modifiers.compute(Attributes.ATTACK_SPEED, 4, EquipmentSlot.OFFHAND), 4);
        }
    }

    private static void verifyCadence() {
        for (double speed : new double[] {1.8, 2, 2.2, 2.5, 2.8, 3.2, 3.6, 4, 5, 6, 20, 100}) {
            var held = new AttackCadence();
            var tapped = new AttackCadence();
            Random random = new Random(913);
            int count = 0;
            int taps = 0;
            for (int tick = 0; tick < 2000; tick++) {
                if (held.tick(tick, true, speed)) { count++; }
                if (tapped.tick(tick, random.nextBoolean(), speed)) { taps++; }
                check(taps <= count, "Taps exceeded continuous budget at " + speed);
                check(!held.tick(tick, true, speed), "Repeated same-tick update issued another action");
            }
            check(Math.abs(count - Math.min(speed, 20) * 100) <= 1, "Fractional average lost at " + speed);
        }
        var six = new AttackCadence();
        var times = new ArrayList<Integer>();
        for (int tick = 0; tick < 200; tick++) { if (six.tick(tick, true, 6)) { times.add(tick); } }
        check(times.size() == 60, "Expected 60 authorizations in [0,200)");
        check(times.get(1) == 4 && times.get(2) == 7 && times.get(3) == 10, "Expected fractional 4/3/3 spacing");
        var paused = new AttackCadence();
        check(paused.tick(0, true, 5), "Initial ready action missing");
        check(!paused.tick(1, false, 5) && !paused.tick(2, true, 5), "Release gave a free opening action");
        check(!paused.tick(10000, true, 5), "Skipped game ticks caught up as a burst");
        check(paused.tick(10001, true, 5), "Current tick did not advance");
        for (int tick = 10002; tick < 10200; tick++) { check(!paused.tick(tick, false, 5), "Idle action issued"); }
        check(paused.tick(10200, true, 5) && !paused.tick(10201, true, 5), "Idle banked multiple actions");
        var invalid = new AttackCadence();
        var switched = new AttackCadence();
        Object slow = new Object();
        Object fast = new Object();
        check(switched.tick(0, true, 2, slow), "Slow opening missing");
        for (int tick = 1; tick < 10; tick++) {
            check(!switched.tick(tick, true, 6, fast), "Faster weapon bypassed outstanding recovery");
        }
        check(switched.tick(10, true, 6, fast), "Replacement did not recover");
        check(!invalid.tick(0, true, Double.NaN) && !invalid.tick(1, true, Double.POSITIVE_INFINITY)
                && !invalid.tick(2, true, -1) && !invalid.tick(3, true, 0), "Invalid speed authorized");
    }

    private static void verifySessions() {
        var session = new AttackInputSession();
        check(session.accept(1, 1, true, 0), "Start rejected");
        check(!session.accept(1, 1, true, 10), "Duplicate heartbeat refreshed session");
        check(session.active(14) && !session.active(15), "Timeout boundary wrong");
        check(!session.accept(1, 2, true, 16), "Expired epoch reopened");
        check(session.accept(2, 3, true, 16), "Fresh epoch rejected");
        check(!session.accept(1, 4, false, 17) && session.active(17), "Old stop killed new session");
        check(session.accept(2, 4, false, 17), "Stop rejected");
        check(!session.accept(2, 5, true, 18), "Stopped epoch reopened");
        check(session.accept(3, 6, false, 19) && !session.accept(3, 7, true, 20), "Stop-before-start replay accepted");
        check(!session.accept(-1, 8, true, 20) && !session.accept(4, -1, true, 20), "Malformed counters accepted");
        var other = new AttackInputSession();
        check(other.accept(1, 1, true, 0) && other.active(1), "Players share session tombstones");
    }

    private static void verifyClientEdges() {
        var input = new AttackInputTracker();
        Object blade = new Object();
        check(input.tick(0, true, true, blade, 0) == null, "Enable while held auto-started");
        check(input.tick(1, false, true, blade, 0) == null, "Release created request");
        var start = input.tick(2, true, true, blade, 0);
        check(start != null && start.held(), "Press missing");
        check(input.tick(3, true, true, blade, 0) == null, "Sent per-frame request");
        var heartbeat = input.tick(7, true, true, blade, 0);
        check(heartbeat != null && heartbeat.epoch() == start.epoch() && heartbeat.sequence() > start.sequence(), "Heartbeat invalid");
        var stop = input.tick(8, true, false, blade, 0);
        check(stop != null && !stop.held(), "GUI/focus/use/block did not stop");
        check(input.tick(9, true, true, blade, 0) == null, "Return without release resumed");
        input.tick(10, false, true, blade, 0);
        var restart = input.tick(11, true, true, blade, 0);
        check(restart.epoch() > start.epoch(), "New press reused closed epoch");
        check(!input.tick(12, true, true, new Object(), 0).held(), "Same-type replacement did not stop");
        input.tick(13, false, true, blade, 0);
        input.tick(14, true, true, blade, 0);
        check(!input.tick(15, true, true, blade, 1).held(), "Slot swap did not stop");
        input.tick(16, false, true, blade, 0);
        input.tick(17, true, true, blade, 0);
        check(!input.tick(18, false, true, blade, 0).held(), "Key release did not stop immediately");
        var projection = new Object();
        input.tick(19, false, true, projection, 0);
        var manifestAttack = input.tick(20, true, true, projection, 0);
        input.replaceWeapon(projection, blade);
        check(input.tick(21, true, true, blade, 0) == null, "Recall restarted or stopped the held attack");
        var continued = input.tick(27, true, true, blade, 0);
        check(continued != null && continued.held() && continued.epoch() == manifestAttack.epoch(), "Recall lost the original attack epoch");
    }

    private static void verifyPayloads() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), registries);
        try {
            var payload = new AttackIntentPayload(123, 456, true, 8, Identifier.withDefaultNamespace("overworld"));
            AttackIntentPayload.CODEC.encode(buffer, payload);
            check(payload.equals(AttackIntentPayload.CODEC.decode(buffer)), "Intent codec mismatch");
            CombatProbePayload.CODEC.encode(buffer, new CombatProbePayload(true));
            check(CombatProbePayload.CODEC.decode(buffer).enabled(), "Control codec mismatch");
            var swing = new com.abyssredemption.absmod.network.BladeSwingPayload(
                    java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 7654, 987654321L, 20F / 6);
            com.abyssredemption.absmod.network.BladeSwingPayload.CODEC.encode(buffer, swing);
            check(swing.equals(com.abyssredemption.absmod.network.BladeSwingPayload.CODEC.decode(buffer)), "Motion timing codec mismatch");
        } finally { buffer.release(); }
    }

    private static void near(double actual, double expected) { check(Math.abs(actual - expected) < 0.00001, actual + " != " + expected); }
    private static void check(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
}
