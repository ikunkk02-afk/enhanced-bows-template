package io.github.ikunkk02.enhancedbows.client.hud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScanHudStateTest {
	@AfterEach
	void resetState() {
		ScanHudState.reset();
	}

	@Test
	void scanningMessageLastsExactlyFortyTicks() {
		ScanHudState.startScanning();
		assertEquals(ScanHudState.Message.SCANNING, ScanHudState.currentMessage());

		for (int tick = 0; tick < 39; tick++) {
			ScanHudState.tick();
		}
		assertEquals(ScanHudState.Message.SCANNING, ScanHudState.currentMessage());

		ScanHudState.tick();
		assertNull(ScanHudState.currentMessage());
	}

	@Test
	void detectedMessageHasPriorityAndAlsoLastsFortyTicks() {
		ScanHudState.startScanning();
		ScanHudState.startDetected();
		assertEquals(ScanHudState.Message.DETECTED, ScanHudState.currentMessage());

		for (int tick = 0; tick < 40; tick++) {
			ScanHudState.tick();
		}
		assertNull(ScanHudState.currentMessage());
	}
}
