package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainScheduleTest {
	@Test
	void distributesCountsExactlyAcrossWaves() {
		assertEquals(List.of(6, 6, 6, 6), emitAll(new ArrowRainSchedule(24, 4, 40)));
		assertEquals(List.of(7, 6, 6, 6), emitAll(new ArrowRainSchedule(25, 4, 40)));
	}

	@Test
	void defaultWavesOccurAtEvenTicksAndCompleteAfterLastWave() {
		ArrowRainSchedule schedule = new ArrowRainSchedule(24, 4, 40);
		List<Integer> ticks = new ArrayList<>();
		for (int tick = 0; tick < 50 && !schedule.isComplete(); tick++) {
			if (schedule.tick() > 0) {
				ticks.add(tick);
			}
		}

		assertEquals(List.of(0, 10, 20, 30), ticks);
		assertTrue(schedule.isComplete());
	}

	private static List<Integer> emitAll(ArrowRainSchedule schedule) {
		List<Integer> counts = new ArrayList<>();
		while (!schedule.isComplete()) {
			int count = schedule.tick();
			if (count > 0) {
				counts.add(count);
			}
		}
		return counts;
	}
}
