package io.github.ikunkk02.enhancedbows.scan;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BouncePhysicsTest {
	@Test
	void onlyActiveScanningArrowsBelowTheLimitWithEnoughResultingSpeedBounce() {
		assertTrue(BouncePhysics.canBounce(true, true, 0, 3, 1.0, 0.75, 0.15));
		assertFalse(BouncePhysics.canBounce(false, true, 0, 3, 1.0, 0.75, 0.15));
		assertFalse(BouncePhysics.canBounce(true, false, 0, 3, 1.0, 0.75, 0.15));
		assertFalse(BouncePhysics.canBounce(true, true, 3, 3, 1.0, 0.75, 0.15));
		assertFalse(BouncePhysics.canBounce(true, true, 0, 3, 0.19, 0.75, 0.15));
	}

	@Test
	void reflectionFlipsOnlyTheCollisionAxisAndAppliesDamping() {
		Vec3d floorBounce = BouncePhysics.reflect(new Vec3d(1.0, -2.0, 3.0), Direction.UP, 0.75);
		assertEquals(0.75, floorBounce.x, 0.00001);
		assertEquals(1.5, floorBounce.y, 0.00001);
		assertEquals(2.25, floorBounce.z, 0.00001);

		Vec3d wallBounce = BouncePhysics.reflect(new Vec3d(2.0, 1.0, -1.0), Direction.EAST, 0.5);
		assertEquals(-1.0, wallBounce.x, 0.00001);
		assertEquals(0.5, wallBounce.y, 0.00001);
		assertEquals(-0.5, wallBounce.z, 0.00001);
	}
}
