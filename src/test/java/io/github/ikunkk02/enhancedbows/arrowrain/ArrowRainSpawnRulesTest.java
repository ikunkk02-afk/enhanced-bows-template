package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainSpawnRulesTest {
	@Test
	void samplesUniformCircularOffsetsAndConfiguredHeight() {
		ArrowRainSpawnRules.SpawnOffset center = ArrowRainSpawnRules.sampleOffset(0.0, 0.0, 8.0, 14.0);
		ArrowRainSpawnRules.SpawnOffset edge = ArrowRainSpawnRules.sampleOffset(1.0, 0.25, 8.0, 14.0);

		assertEquals(0.0, center.x(), 1.0E-9);
		assertEquals(0.0, center.z(), 1.0E-9);
		assertEquals(14.0, center.y());
		assertTrue(Math.hypot(edge.x(), edge.z()) <= 8.0 + 1.0E-9);
		assertEquals(14.0, edge.y());
	}

	@Test
	void scalesOnlyBaseDamageAndProvidesDownwardVelocity() {
		assertEquals(4.9, ArrowRainSpawnRules.scaledDamage(7.0, 0.7), 1.0E-9);
		assertEquals(0.0, ArrowRainSpawnRules.scaledDamage(-1.0, 0.7));
		assertTrue(ArrowRainSpawnRules.downwardSpeed(0.0) < 0.0);
		assertTrue(ArrowRainSpawnRules.downwardSpeed(1.0) < 0.0);
	}
}
