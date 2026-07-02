package io.github.ikunkk02.enhancedbows.lightning;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Pure lifetime and per-target cooldown state for one Lightning storm. */
final class LightningStormState {
	private final int scanIntervalTicks;
	private final boolean allowRepeatStrike;
	private final int repeatCooldownTicks;
	private final Map<UUID, Integer> lastStrikeTicks = new HashMap<>();
	private int remainingTicks;
	private int ageTicks;

	LightningStormState(int durationTicks, int scanIntervalTicks, boolean allowRepeatStrike,
			int repeatCooldownTicks) {
		this.remainingTicks = Math.max(1, durationTicks);
		this.scanIntervalTicks = Math.max(1, scanIntervalTicks);
		this.allowRepeatStrike = allowRepeatStrike;
		this.repeatCooldownTicks = Math.max(1, repeatCooldownTicks);
	}

	boolean tickAndShouldScan() {
		if (isExpired()) {
			return false;
		}
		ageTicks++;
		remainingTicks--;
		return ageTicks % scanIntervalTicks == 0;
	}

	boolean isExpired() {
		return remainingTicks <= 0;
	}

	int ageTicks() {
		return ageTicks;
	}

	boolean canStrike(UUID uuid) {
		Integer lastStrikeTick = lastStrikeTicks.get(uuid);
		if (lastStrikeTick == null) {
			return true;
		}
		return allowRepeatStrike && ageTicks - lastStrikeTick >= repeatCooldownTicks;
	}

	void recordStrike(UUID uuid) {
		lastStrikeTicks.put(uuid, ageTicks);
	}
}
