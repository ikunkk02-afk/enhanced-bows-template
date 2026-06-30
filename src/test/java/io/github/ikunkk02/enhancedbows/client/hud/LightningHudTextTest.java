package io.github.ikunkk02.enhancedbows.client.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LightningHudTextTest {
	@Test
	void fullChargesUseTheCompactHudLine() {
		LightningHudText value = LightningHudText.create(2, 2, 0);
		assertEquals("hud.enhanced-bows.lightning.full", value.translationKey());
		assertEquals(0, value.remainingSeconds());
	}

	@Test
	void partialAndEmptyChargesRoundRemainingTimeUpToSeconds() {
		LightningHudText partial = LightningHudText.create(1, 2, 259);
		LightningHudText empty = LightningHudText.create(0, 2, 400);

		assertEquals("hud.enhanced-bows.lightning.recharging", partial.translationKey());
		assertEquals(13, partial.remainingSeconds());
		assertEquals(20, empty.remainingSeconds());
	}
}
