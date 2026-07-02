package io.github.ikunkk02.enhancedbows.arrowrain;

/** Pure random-sample transforms for rain projectile placement and damage. */
final class ArrowRainSpawnRules {
	private ArrowRainSpawnRules() {
	}

	static SpawnOffset sampleOffset(double radialSample, double angleSample,
			double radius, double height) {
		double safeRadius = Math.max(0.0, radius);
		double radial = Math.sqrt(clamp01(radialSample)) * safeRadius;
		double angle = clamp01(angleSample) * Math.PI * 2.0;
		return new SpawnOffset(Math.cos(angle) * radial, Math.max(0.0, height),
			Math.sin(angle) * radial);
	}

	static double scaledDamage(double baseDamage, double multiplier) {
		return Math.max(0.0, baseDamage) * Math.max(0.0, multiplier);
	}

	static double downwardSpeed(double sample) {
		return -(1.5 + clamp01(sample) * 0.2);
	}

	private static double clamp01(double value) {
		return Math.max(0.0, Math.min(value, 1.0));
	}

	record SpawnOffset(double x, double y, double z) {
	}
}
