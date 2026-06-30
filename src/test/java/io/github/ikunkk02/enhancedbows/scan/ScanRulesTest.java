package io.github.ikunkk02.enhancedbows.scan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanRulesTest {
	@Test
	void triggerRequiresEnabledPlayerOwnedUpwardSpectralArrow() {
		assertTrue(ScanRules.shouldTrigger(true, true, 0.351, 0.35));
		assertFalse(ScanRules.shouldTrigger(false, true, 1.0, 0.35));
		assertFalse(ScanRules.shouldTrigger(true, false, 1.0, 0.35));
		assertFalse(ScanRules.shouldTrigger(true, true, 0.35, 0.35));
	}

	@Test
	void scanRunsImmediatelyAndThenAtConfiguredIntervalWithinDuration() {
		assertTrue(ScanRules.shouldScanAt(0, 100, 5));
		assertFalse(ScanRules.shouldScanAt(1, 100, 5));
		assertTrue(ScanRules.shouldScanAt(5, 100, 5));
		assertTrue(ScanRules.shouldScanAt(95, 100, 5));
		assertFalse(ScanRules.shouldScanAt(100, 100, 5));
	}

	@Test
	void targetFilteringHonorsPlayerOwnerAndSpectatorSettings() {
		assertTrue(ScanRules.shouldIncludeTarget(false, false, false, true, true, false));
		assertTrue(ScanRules.shouldIncludeTarget(true, false, false, true, true, false));
		assertFalse(ScanRules.shouldIncludeTarget(true, false, false, true, false, false));
		assertFalse(ScanRules.shouldIncludeTarget(true, true, false, true, true, false));
		assertTrue(ScanRules.shouldIncludeTarget(true, true, false, true, true, true));
		assertFalse(ScanRules.shouldIncludeTarget(true, false, true, true, true, true));
		assertFalse(ScanRules.shouldIncludeTarget(false, false, false, false, true, true));
	}
}
