package io.github.ikunkk02.enhancedbows.scan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanRulesTest {
	@Test
	void triggerAcceptsEveryPlayerOwnedSpectralArrowDirection() {
		assertTrue(ScanRules.shouldTrigger(true, true));
		assertFalse(ScanRules.shouldTrigger(false, true));
		assertFalse(ScanRules.shouldTrigger(true, false));
	}

	@Test
	void scanRunsEveryTwoTicksForTheWholeFlight() {
		assertTrue(ScanRules.shouldScanAt(0, 2));
		assertFalse(ScanRules.shouldScanAt(1, 2));
		assertTrue(ScanRules.shouldScanAt(2, 2));
		assertTrue(ScanRules.shouldScanAt(100, 2));
		assertTrue(ScanRules.shouldScanAt(10_000, 2));
	}

	@Test
	void strictLineOfSightRequiresBothTargetSamplesToBeClear() {
		assertTrue(ScanRules.hasLineOfSight(true, true, true));
		assertFalse(ScanRules.hasLineOfSight(true, false, true));
		assertFalse(ScanRules.hasLineOfSight(false, true, true));
		assertFalse(ScanRules.hasLineOfSight(false, false, true));
		assertTrue(ScanRules.hasLineOfSight(true, false, false));
		assertTrue(ScanRules.hasLineOfSight(false, true, false));
		assertFalse(ScanRules.hasLineOfSight(false, false, false));
	}

	@Test
	void strictScanSpaceRejectsOutdoorToIndoorDetectionThroughOpenings() {
		assertTrue(ScanRules.hasCompatibleSkyExposure(true, true, true));
		assertTrue(ScanRules.hasCompatibleSkyExposure(false, false, false));
		assertFalse(ScanRules.hasCompatibleSkyExposure(true, false, false));
		assertFalse(ScanRules.hasCompatibleSkyExposure(false, true, true));
		assertFalse(ScanRules.hasCompatibleSkyExposure(true, true, false));
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
