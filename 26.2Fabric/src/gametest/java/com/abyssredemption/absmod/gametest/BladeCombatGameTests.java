package com.abyssredemption.absmod.gametest;

import com.abyssredemption.absmod.combat.BladeAttackScope;
import com.abyssredemption.absmod.item.ModCreativeTab;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** Exercises transformed Player/LivingEntity methods in a real server world, without client input. */
public final class BladeCombatGameTests {
    @GameTest(maxTicks=130)
    public void heavyStunExpiresAndRejectsImmuneTargets(GameTestHelper helper) {
        var target=helper.spawn(EntityTypes.HUSK,1,1,1);
        helper.assertTrue(com.abyssredemption.absmod.combat.BladeStun.apply(target),"Ordinary hostile refused stun");
        helper.assertTrue(com.abyssredemption.absmod.combat.BladeStun.active(target),"Stun not active");
        helper.assertTrue(!com.abyssredemption.absmod.combat.BladeStun.apply(target),"Repeated stun bypassed immunity");
        helper.assertTrue(!com.abyssredemption.absmod.combat.BladeStun.apply(player(helper)),"Player accepted stun");
        var boss=helper.spawn(EntityTypes.WITHER,3,1,3);boss.setNoAi(true);
        helper.assertTrue(!com.abyssredemption.absmod.combat.BladeStun.apply(boss),"Boss accepted stun");boss.discard();
        helper.runAfterDelay(13,()->{
            helper.assertTrue(!com.abyssredemption.absmod.combat.BladeStun.active(target),"Stun did not expire");
            helper.assertTrue(!com.abyssredemption.absmod.combat.BladeStun.apply(target),"Post-stun immunity missing");
        });
        helper.runAfterDelay(113,()->{
            helper.assertTrue(com.abyssredemption.absmod.combat.BladeStun.apply(target),"Immunity never expired");helper.succeed();
        });
    }
    @GameTest
    public void onlyActualPrimaryLossConfirmsHit(GameTestHelper helper) {
        Player player = player(helper);
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.setInvulnerable(true);
        helper.assertTrue(!BladeAttackScope.runTracked(player, target, () -> player.attack(target)),
                "Immune target confirmed a hit");
        target.setInvulnerable(false);
        target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);
        target.setAbsorptionAmount(20);
        float health = target.getHealth();
        helper.assertTrue(BladeAttackScope.runTracked(player, target, () -> player.attack(target)),
                "Absorption damage did not confirm a hit");
        near(helper, target.getHealth(), health, "Absorption should protect health");
        near(helper, target.getAbsorptionAmount(), 13, "Absorption loss");
        helper.assertTrue(!BladeAttackScope.runTracked(player, target, () -> {}), "Miss confirmed a hit");
        helper.succeed();
    }

    @GameTest(maxTicks = 230)
    public void sixAttacksPerSecondDealSixtyFullHits(GameTestHelper helper) {
        Player player = player(helper);
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.setNoAi(true);
        target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.setHealth(1000);
        var cadence = new com.abyssredemption.absmod.combat.AttackCadence();
        int[] elapsed = {0};
        int[] hits = {0};
        helper.onEachTick(() -> {
            if (elapsed[0] >= 200) { return; }
            if (cadence.tick(elapsed[0], true, 6)) {
                player.resetAttackStrengthTicker();
                float before = target.getHealth();
                BladeAttackScope.run(player, target, () -> player.attack(target));
                near(helper, before - target.getHealth(), 7, "Sustained primary damage");
                hits[0]++;
            }
            if (++elapsed[0] == 200) {
                helper.assertTrue(hits[0] == 60, "Expected 60 actual hits, got " + hits[0]);
                near(helper, target.getHealth(), 580, "Total damage over 200 ticks");
                helper.succeed();
            }
        });
    }

    @GameTest
    public void otherDamageRetainsItsCooldownHistory(GameTestHelper helper) {
        Player player = player(helper);
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        var source = target.damageSources().generic();
        helper.assertTrue(target.hurtServer(helper.getLevel(), source, 6), "Initial ordinary damage rejected");
        BladeAttackScope.run(player, target, () -> player.attack(target));
        near(helper, target.getHealth(), 87, "Mixed primary damage");
        helper.assertTrue(!target.hurtServer(helper.getLevel(), source, 3), "Smaller ordinary hit bypassed its cooldown");
        helper.assertTrue(target.hurtServer(helper.getLevel(), source, 8), "Larger ordinary hit was rejected");
        near(helper, target.getHealth(), 85, "Ordinary damage should retain its previous 6-point history");
        helper.succeed();
    }

    @GameTest
    public void armorStillReducesAuthorizedDamage(GameTestHelper helper) {
        Player player = player(helper);
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.getAttribute(Attributes.ARMOR).setBaseValue(20);
        float before = target.getHealth();
        BladeAttackScope.run(player, target, () -> player.attack(target));
        float loss = before - target.getHealth();
        helper.assertTrue(loss > 0 && loss < 7, "Armor was bypassed: " + loss);
        helper.succeed();
    }

    @GameTest
    public void authorizedHitsBypassChargeAndPreserveCooldown(GameTestHelper helper) {
        Player player = player(helper);
        player.setItemSlot(EquipmentSlot.MAINHAND, BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("absmod", "meow_blade")).getDefaultInstance());
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        target.invulnerableTime = 17;
        player.resetAttackStrengthTicker();
        BladeAttackScope.run(player, target, () -> player.attack(target));
        near(helper, target.getHealth(), 93, "First authorized damage");
        BladeAttackScope.run(player, target, () -> player.attack(target));
        near(helper, target.getHealth(), 86, "Second authorized damage inside hurt window");
        helper.assertTrue(target.invulnerableTime == 17, "Primary hit rewrote shared cooldown");
        helper.assertTrue(player.getAttackStrengthScale(0) < 1, "Full strength leaked outside authorization");
        helper.succeed();
    }

    @GameTest
    public void vanillaAttackStillUsesCharge(GameTestHelper helper) {
        Player player = player(helper);
        player.setItemSlot(EquipmentSlot.MAINHAND, Items.DIAMOND_SWORD.getDefaultInstance());
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.setNoAi(true);
        player.resetAttackStrengthTicker();
        float before = target.getHealth();
        player.attack(target);
        float loss = before - target.getHealth();
        helper.assertTrue(loss > 0 && loss < 7, "Vanilla uncharged sword dealt full damage or no damage: " + loss);
        helper.succeed();
    }

    @GameTest
    public void immunityIsPreserved(GameTestHelper helper) {
        Player player = player(helper);
        var target = helper.spawn(EntityTypes.COW, 1, 1, 1);
        target.setInvulnerable(true);
        float before = target.getHealth();
        BladeAttackScope.run(player, target, () -> player.attack(target));
        near(helper, target.getHealth(), before, "Invulnerability");
        helper.succeed();
    }

    @GameTest
    public void creativeTabIsRegistered(GameTestHelper helper) {
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(ModCreativeTab.ID);
        helper.assertTrue(tab != null, "Missing Ab's Mod creative tab");
        helper.assertTrue(tab.getIconItem().is(BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("absmod", "meow_blade"))), "Incorrect creative tab icon");
        helper.succeed();
    }

    private static Player player(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("absmod", "meow_blade")).getDefaultInstance());
        // Explicit fixture attributes isolate damage scaling from equipment tick synchronization.
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
        player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(5);
        player.setOnGround(true);
        return player;
    }

    private static void near(GameTestHelper helper, float actual, float expected, String label) {
        helper.assertTrue(Math.abs(actual - expected) < 0.001F, label + ": expected " + expected + ", got " + actual);
    }
}
