package io.github.ikunkk02.enhancedbows.burst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurstArrowRulesTest {
	@Test
	void armsBurstOnlyForEnabledNonSpectralBowProjectiles() {
		assertEquals(BurstArrowRules.ArmDecision.BURST,
			BurstArrowRules.decideArming(true, true, true, true, false, 1, 0));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(false, true, true, true, false, 1, 0));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(true, true, false, true, false, 1, 0));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(true, true, true, true, true, 1, 0));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(true, true, true, true, false, 0, 0));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(true, true, true, false, false, 1, 0));
	}

	@Test
	void illegalCombinationAlwaysGivesLightningPriority() {
		assertEquals(BurstArrowRules.ArmDecision.LIGHTNING,
			BurstArrowRules.decideArming(true, true, true, true, false, 1, 1));
		assertEquals(BurstArrowRules.ArmDecision.LIGHTNING,
			BurstArrowRules.decideArming(false, true, true, true, false, 1, 1));
		assertEquals(BurstArrowRules.ArmDecision.NONE,
			BurstArrowRules.decideArming(true, false, true, true, false, 1, 1));
	}

	@Test
	void lightningCanStillArmSpectralArrows() {
		assertEquals(BurstArrowRules.ArmDecision.LIGHTNING,
			BurstArrowRules.decideArming(true, true, true, true, true, 0, 1));
	}

	@Test
	void triggersOnceOnEntityOrBlockButNeverOnMiss() {
		assertTrue(BurstArrowRules.shouldTrigger(true, false, true, false));
		assertTrue(BurstArrowRules.shouldTrigger(true, false, false, true));
		assertFalse(BurstArrowRules.shouldTrigger(true, false, false, false));
		assertFalse(BurstArrowRules.shouldTrigger(false, false, true, false));
		assertFalse(BurstArrowRules.shouldTrigger(true, true, true, false));
	}
}
