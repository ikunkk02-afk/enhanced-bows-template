package io.github.ikunkk02.enhancedbows.client.trail;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedTrailSamplerTest {
	@Test
	void samplesFourteenPointsAcrossTwelveBlocksBehindTheArrow() {
		var points = RedTrailSampler.sample(new Vec3d(10.0, 5.0, 2.0), new Vec3d(2.0, 0.0, 0.0), 12, 14);

		assertEquals(14, points.size());
		assertTrue(points.getFirst().x < 10.0);
		assertEquals(-2.0, points.getLast().x, 0.00001);
		assertEquals(5.0, points.getLast().y, 0.00001);
	}

	@Test
	void stationaryArrowProducesNoTrailPoints() {
		assertTrue(RedTrailSampler.sample(Vec3d.ZERO, Vec3d.ZERO, 12, 14).isEmpty());
	}
}
