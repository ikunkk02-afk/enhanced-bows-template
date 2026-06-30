package io.github.ikunkk02.enhancedbows.lightning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningArrowRulesTest {
	@Test
	void armsOnlyEnabledEnchantedBows() {
		assertTrue(LightningArrowRules.shouldArm(true, true, 1));
		assertFalse(LightningArrowRules.shouldArm(false, true, 1));
		assertFalse(LightningArrowRules.shouldArm(true, false, 1));
		assertFalse(LightningArrowRules.shouldArm(true, true, 0));
	}

	@Test
	void triggersOnlyOnceOnALiveLivingTargetWithAUsableCharge() {
		assertTrue(LightningArrowRules.shouldTrigger(true, false, true, true, true, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(false, false, true, true, true, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(true, true, true, true, true, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(true, false, false, true, true, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(true, false, true, false, true, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(true, false, true, true, false, true, false));
		assertFalse(LightningArrowRules.shouldTrigger(true, false, true, true, true, false, false));
		assertTrue(LightningArrowRules.shouldTrigger(true, false, true, true, true, false, true));
	}

	@Test
	void creativeInfiniteModeSkipsChargeConsumption() {
		assertFalse(LightningArrowRules.shouldConsumeCharge(true, true));
		assertTrue(LightningArrowRules.shouldConsumeCharge(true, false));
		assertTrue(LightningArrowRules.shouldConsumeCharge(false, true));
	}
}
