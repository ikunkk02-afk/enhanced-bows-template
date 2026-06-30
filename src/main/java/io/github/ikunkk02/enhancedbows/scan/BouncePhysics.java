package io.github.ikunkk02.enhancedbows.scan;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Pure eligibility and reflection math for scanning-arrow block bounces. */
public final class BouncePhysics {
	private BouncePhysics() {
	}

	/** Returns whether the collision should replace vanilla block embedding. */
	public static boolean canBounce(boolean enabled, boolean scanningArrow, int bounceCount, int maxBounces,
			double speed, double damping, double minimumResultingSpeed) {
		return enabled
			&& scanningArrow
			&& bounceCount < maxBounces
			&& speed * damping >= minimumResultingSpeed;
	}

	/** Reflects the velocity across the hit face and applies energy loss. */
	public static Vec3d reflect(Vec3d velocity, Direction hitSide, double damping) {
		double x = hitSide.getAxis() == Direction.Axis.X ? -velocity.x : velocity.x;
		double y = hitSide.getAxis() == Direction.Axis.Y ? -velocity.y : velocity.y;
		double z = hitSide.getAxis() == Direction.Axis.Z ? -velocity.z : velocity.z;
		return new Vec3d(x, y, z).multiply(damping);
	}
}
