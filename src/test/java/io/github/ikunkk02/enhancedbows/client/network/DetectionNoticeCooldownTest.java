package io.github.ikunkk02.enhancedbows.client.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectionNoticeCooldownTest {
	@Test
	void detectedNoticePlaysAtMostOnceEveryTwoSeconds() {
		DetectionNoticeCooldown cooldown = new DetectionNoticeCooldown(2_000L);

		assertTrue(cooldown.tryAcquire(10_000L));
		assertFalse(cooldown.tryAcquire(11_999L));
		assertTrue(cooldown.tryAcquire(12_000L));
	}
}
