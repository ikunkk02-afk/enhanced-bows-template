package io.github.ikunkk02.enhancedbows.arrowrain;

/** Pure persistent player state for Arrow Rain mode and cooldown. */
final class ArrowRainState {
	private boolean modeEnabled;
	private int cooldownTicks;

	boolean isModeEnabled() {
		return modeEnabled;
	}

	int getCooldownTicks() {
		return cooldownTicks;
	}

	boolean toggleMode() {
		modeEnabled = !modeEnabled;
		return modeEnabled;
	}

	boolean tryStartCooldown(int configuredTicks) {
		if (cooldownTicks > 0) {
			return false;
		}
		cooldownTicks = Math.max(0, configuredTicks);
		return true;
	}

	TickResult tick() {
		if (cooldownTicks <= 0) {
			return TickResult.NONE;
		}
		cooldownTicks--;
		if (cooldownTicks == 0) {
			return TickResult.COMPLETE;
		}
		return cooldownTicks % 20 == 0 ? TickResult.SECOND_BOUNDARY : TickResult.NONE;
	}

	void load(boolean enabled, int savedCooldownTicks) {
		modeEnabled = enabled;
		cooldownTicks = Math.max(0, savedCooldownTicks);
	}

	enum TickResult {
		NONE,
		SECOND_BOUNDARY,
		COMPLETE
	}
}
