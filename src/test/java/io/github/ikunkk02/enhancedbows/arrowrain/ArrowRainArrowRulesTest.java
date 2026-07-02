package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainArrowRulesTest {
	@Test
	void armsOnlyReadyEnabledPlayerBowShots() {
		assertTrue(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(false, true, true, true,
			0, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, false, true, true,
			0, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, false, true,
			0, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, false,
			0, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, true,
			1, false, false, false, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, false, false, false, false, 0));
	}

	@Test
	void burstAndChildrenAreAlwaysRejectedWhileSpectralIsConfigurable() {
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, true, false, true, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, false, true, false, false, 1));
		assertTrue(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, false, true, true, false, 1));
		assertFalse(ArrowRainArrowRules.shouldArm(true, true, true, true,
			0, false, false, true, true, 1));
	}

	@Test
	void handlesOnlyEnabledEntityOrBlockImpactsOnce() {
		assertTrue(handleImpact(true, false, true, false, false, true, true));
		assertTrue(handleImpact(true, false, false, true, false, true, true));
		assertFalse(handleImpact(true, false, true, false, false, false, true));
		assertFalse(handleImpact(true, false, false, true, false, true, false));
		assertFalse(handleImpact(true, false, false, false, false, true, true));
		assertFalse(handleImpact(true, true, true, false, false, true, true));
		assertFalse(handleImpact(false, false, true, false, false, true, true));
		assertFalse(handleImpact(true, false, true, false, true, true, true));
	}

	@Test
	void requiresAtLeastOneArrowAndWaveBeforeConsumingCooldown() {
		assertTrue(canCreateRain(24, 4));
		assertFalse(canCreateRain(0, 4));
		assertFalse(canCreateRain(24, 0));
	}

	private static boolean handleImpact(boolean armed, boolean triggered,
			boolean entityImpact, boolean blockImpact, boolean childArrow,
			boolean triggerOnEntityHit, boolean triggerOnBlockHit) {
		Method method = Arrays.stream(ArrowRainArrowRules.class.getDeclaredMethods())
			.filter(candidate -> candidate.getName().equals("shouldHandleImpact"))
			.filter(candidate -> candidate.getParameterCount() == 7)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Missing configurable impact-policy overload"));
		try {
			return (boolean) method.invoke(null, armed, triggered, entityImpact, blockImpact,
				childArrow, triggerOnEntityHit, triggerOnBlockHit);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static boolean canCreateRain(int arrowCount, int waves) {
		Method method = Arrays.stream(ArrowRainArrowRules.class.getDeclaredMethods())
			.filter(candidate -> candidate.getName().equals("canCreateRain"))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Missing successful-rain preflight rule"));
		try {
			return (boolean) method.invoke(null, arrowCount, waves);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}
}
