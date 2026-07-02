package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import static io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules.ToggleDecision.BURST_CONFLICT;
import static io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules.ToggleDecision.DISABLE;
import static io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules.ToggleDecision.ENABLE;
import static io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules.ToggleDecision.REQUIRES_BOW;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrowRainToggleRulesTest {
	@Test
	void requiresArrowRainBowBeforeChangingMode() {
		assertEquals(REQUIRES_BOW, ArrowRainToggleRules.decide(false, false, false));
		assertEquals(REQUIRES_BOW, ArrowRainToggleRules.decide(true, false, false));
	}

	@Test
	void validBowEnablesAndDisablesMode() {
		assertEquals(ENABLE, ArrowRainToggleRules.decide(false, true, false));
		assertEquals(DISABLE, ArrowRainToggleRules.decide(true, true, false));
	}

	@Test
	void burstBlocksEnablingButNeverBlocksDisabling() {
		assertEquals(BURST_CONFLICT, ArrowRainToggleRules.decide(false, true, true));
		assertEquals(DISABLE, ArrowRainToggleRules.decide(true, true, true));
	}
}
