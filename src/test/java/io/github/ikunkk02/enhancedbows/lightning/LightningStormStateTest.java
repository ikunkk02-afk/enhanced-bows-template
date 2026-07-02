package io.github.ikunkk02.enhancedbows.lightning;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningStormStateTest {
	@Test
	void scansEveryTenTicksAndExpiresAfterOneHundredTicks() {
		LightningStormState state = new LightningStormState(100, 10, false, 40);
		int scans = 0;
		for (int tick = 1; tick <= 100; tick++) {
			boolean shouldScan = state.tickAndShouldScan();
			assertEquals(tick % 10 == 0, shouldScan, "tick " + tick);
			if (shouldScan) {
				scans++;
			}
			assertEquals(tick == 100, state.isExpired(), "tick " + tick);
		}
		assertEquals(10, scans);
	}

	@Test
	void defaultStormNeverStrikesTheSameUuidTwice() {
		UUID target = UUID.randomUUID();
		LightningStormState state = new LightningStormState(100, 10, false, 40);

		assertTrue(state.canStrike(target));
		state.recordStrike(target);
		for (int tick = 0; tick < 100; tick++) {
			assertFalse(state.canStrike(target));
			state.tickAndShouldScan();
		}
	}

	@Test
	void repeatModeUsesFortyTickCooldown() {
		UUID target = UUID.randomUUID();
		LightningStormState state = new LightningStormState(100, 10, true, 40);
		state.recordStrike(target);

		for (int tick = 0; tick < 40; tick++) {
			assertFalse(state.canStrike(target), "age " + state.ageTicks());
			state.tickAndShouldScan();
		}
		assertTrue(state.canStrike(target));
	}
}
