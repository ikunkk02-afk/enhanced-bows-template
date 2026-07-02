package io.github.ikunkk02.enhancedbows.burst;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurstDamageRulesTest {
	@Test
	void powerProducesVanillaSizedEntityRadiusAndLinearDamage() {
		double radius = BurstDamageRules.effectiveRadius(2.5);

		assertEquals(5.0, radius);
		assertEquals(8.0, BurstDamageRules.damageAtDistance(0.0, radius, 8.0, 2.0));
		assertEquals(5.0, BurstDamageRules.damageAtDistance(2.5, radius, 8.0, 2.0));
		assertEquals(2.0, BurstDamageRules.damageAtDistance(5.0, radius, 8.0, 2.0));
		assertEquals(0.0, BurstDamageRules.damageAtDistance(5.01, radius, 8.0, 2.0));
		assertEquals(0.0, BurstDamageRules.effectiveRadius(0.0));
	}

	@Test
	void targetPolicyExcludesOwnerPlayersAndInvalidEntities() {
		UUID owner = UUID.randomUUID();
		UUID mob = UUID.randomUUID();

		assertFalse(BurstDamageRules.shouldAffect(facts(owner, 1.0, true, false,
			false, false, false), owner, false, true, 5.0));
		assertTrue(BurstDamageRules.shouldAffect(facts(owner, 1.0, true, false,
			false, false, false), owner, true, true, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(UUID.randomUUID(), 1.0, true, false,
			true, false, false), owner, false, false, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(UUID.randomUUID(), 1.0, true, false,
			true, true, false), owner, false, true, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(UUID.randomUUID(), 1.0, true, false,
			true, false, true), owner, false, true, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(mob, 1.0, false, false,
			false, false, false), owner, false, true, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(mob, 1.0, true, true,
			false, false, false), owner, false, true, 5.0));
		assertFalse(BurstDamageRules.shouldAffect(facts(mob, 5.01, true, false,
			false, false, false), owner, false, true, 5.0));
		assertTrue(BurstDamageRules.shouldAffect(facts(mob, 5.0, true, false,
			false, false, false), owner, false, true, 5.0));
	}

	@Test
	void knockbackFallsOffToZeroAndUsesMultiplier() {
		assertEquals(2.0, BurstDamageRules.knockbackMagnitude(0.0, 5.0, 2.0));
		assertEquals(1.0, BurstDamageRules.knockbackMagnitude(2.5, 5.0, 2.0));
		assertEquals(0.0, BurstDamageRules.knockbackMagnitude(5.0, 5.0, 2.0));
		assertEquals(0.0, BurstDamageRules.knockbackMagnitude(6.0, 5.0, 2.0));
	}

	private static BurstDamageRules.TargetFacts facts(UUID uuid, double distance, boolean alive,
			boolean removed, boolean player, boolean creative, boolean spectator) {
		return new BurstDamageRules.TargetFacts(uuid, distance, alive, removed,
			player, creative, spectator);
	}
}
