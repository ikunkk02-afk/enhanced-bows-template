package io.github.ikunkk02.enhancedbows.arrowrain;

/** Pure wave timing and exact arrow-count distribution for one rain event. */
final class ArrowRainSchedule {
	private final int totalArrows;
	private final int waves;
	private final int durationTicks;
	private int age;
	private int nextWave;

	ArrowRainSchedule(int totalArrows, int waves, int durationTicks) {
		this.totalArrows = Math.max(0, totalArrows);
		this.waves = this.totalArrows == 0 ? 0 : Math.max(1, Math.min(waves, this.totalArrows));
		this.durationTicks = Math.max(1, durationTicks);
	}

	int tick() {
		if (isComplete()) {
			return 0;
		}
		int count = 0;
		int dueTick = nextWave * durationTicks / waves;
		if (age >= dueTick) {
			count = totalArrows / waves + (nextWave < totalArrows % waves ? 1 : 0);
			nextWave++;
		}
		age++;
		return count;
	}

	boolean isComplete() {
		return nextWave >= waves;
	}
}
