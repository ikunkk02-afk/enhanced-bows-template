package io.github.ikunkk02.enhancedbows.client.trail;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Produces evenly spaced points behind a moving arrow without touching server state. */
public final class RedTrailSampler {
	private RedTrailSampler() {
	}

	public static List<Vec3d> sample(Vec3d position, Vec3d velocity, int length, int particleCount) {
		if (velocity.lengthSquared() < 1.0E-8 || length <= 0 || particleCount <= 0) {
			return List.of();
		}
		Vec3d backward = velocity.normalize().negate();
		List<Vec3d> points = new ArrayList<>(particleCount);
		for (int index = 1; index <= particleCount; index++) {
			double distance = length * (index / (double) particleCount);
			points.add(position.add(backward.multiply(distance)));
		}
		return points;
	}
}
