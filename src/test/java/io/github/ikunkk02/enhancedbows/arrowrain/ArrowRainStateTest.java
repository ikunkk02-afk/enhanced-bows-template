package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainStateTest {
	@Test
	void startsDisabledAndReadyThenTogglesIndependently() {
		ArrowRainState state = new ArrowRainState();

		assertFalse(state.isModeEnabled());
		assertEquals(0, state.getCooldownTicks());
		assertTrue(state.toggleMode());
		assertTrue(state.isModeEnabled());
		assertFalse(state.toggleMode());
	}

	@Test
	void cooldownStartsOnceAndTicksToCompletion() {
		ArrowRainState state = new ArrowRainState();

		assertTrue(state.tryStartCooldown(21));
		assertFalse(state.tryStartCooldown(200));
		assertEquals(ArrowRainState.TickResult.SECOND_BOUNDARY, state.tick());
		assertEquals(20, state.getCooldownTicks());
		assertEquals(ArrowRainState.TickResult.NONE, state.tick());
		for (int i = 0; i < 18; i++) {
			state.tick();
		}
		assertEquals(1, state.getCooldownTicks());
		assertEquals(ArrowRainState.TickResult.COMPLETE, state.tick());
		assertEquals(0, state.getCooldownTicks());
		assertEquals(ArrowRainState.TickResult.NONE, state.tick());
	}

	@Test
	void loadClampsNegativeCooldownAndPreservesMode() {
		ArrowRainState state = new ArrowRainState();
		state.load(true, -40);

		assertTrue(state.isModeEnabled());
		assertEquals(0, state.getCooldownTicks());
	}
}
