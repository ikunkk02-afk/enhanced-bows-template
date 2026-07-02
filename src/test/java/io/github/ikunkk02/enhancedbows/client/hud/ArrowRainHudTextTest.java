package io.github.ikunkk02.enhancedbows.client.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainHudTextTest {
	@Test
	void hidesWithoutHeldArrowRainBow() {
		assertFalse(ArrowRainHudText.create(false, true, 100).visible());
	}

	@Test
	void reportsOffReadyAndRoundedCooldownStates() {
		ArrowRainHudText off = ArrowRainHudText.create(true, false, 200);
		ArrowRainHudText ready = ArrowRainHudText.create(true, true, 0);
		ArrowRainHudText cooldown = ArrowRainHudText.create(true, true, 121);

		assertTrue(off.visible());
		assertEquals("hud.enhanced-bows.arrow_rain.off", off.translationKey());
		assertEquals("hud.enhanced-bows.arrow_rain.on", ready.translationKey());
		assertEquals("hud.enhanced-bows.arrow_rain.cooldown", cooldown.translationKey());
		assertEquals(7, cooldown.remainingSeconds());
	}
}
