package io.github.ikunkk02.enhancedbows.arrowrain;

/** Pure server-side validation outcomes for the Arrow Rain mode toggle. */
public final class ArrowRainToggleRules {
	private ArrowRainToggleRules() {
	}

	public static ToggleDecision decide(boolean currentlyEnabled,
			boolean holdingArrowRainBow, boolean heldBowHasBurst) {
		if (!holdingArrowRainBow) {
			return ToggleDecision.REQUIRES_BOW;
		}
		if (currentlyEnabled) {
			return ToggleDecision.DISABLE;
		}
		return heldBowHasBurst ? ToggleDecision.BURST_CONFLICT : ToggleDecision.ENABLE;
	}

	public enum ToggleDecision {
		ENABLE,
		DISABLE,
		REQUIRES_BOW,
		BURST_CONFLICT
	}
}
