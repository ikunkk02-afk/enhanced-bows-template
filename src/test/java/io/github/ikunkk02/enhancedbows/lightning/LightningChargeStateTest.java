package io.github.ikunkk02.enhancedbows.lightning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningChargeStateTest {
	@Test
	void startsFullConsumesOnlyAvailableCharges() {
		LightningChargeState state = new LightningChargeState(2, 400);

		assertEquals(2, state.getCharges());
		assertTrue(state.consume());
		assertTrue(state.consume());
		assertFalse(state.consume());
		assertEquals(0, state.getCharges());
	}

	@Test
	void restoresOneChargeEveryFourHundredTicks() {
		LightningChargeState state = new LightningChargeState(2, 400);
		state.consume();
		state.consume();

		for (int tick = 0; tick < 399; tick++) {
			assertFalse(state.tick());
		}
		assertTrue(state.tick());
		assertEquals(1, state.getCharges());
		assertEquals(0, state.getRechargeTicks());

		for (int tick = 0; tick < 400; tick++) {
			state.tick();
		}
		assertEquals(2, state.getCharges());
		assertEquals(0, state.getRemainingRechargeTicks());
	}

	@Test
	void remainingTicksStopAtZeroWhenFull() {
		LightningChargeState state = new LightningChargeState(2, 400);
		assertEquals(0, state.getRemainingRechargeTicks());
		state.consume();
		state.tick();
		assertEquals(399, state.getRemainingRechargeTicks());
	}
}
