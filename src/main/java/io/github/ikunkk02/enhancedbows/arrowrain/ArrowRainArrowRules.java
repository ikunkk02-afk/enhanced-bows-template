package io.github.ikunkk02.enhancedbows.arrowrain;

/** Pure arming and one-shot impact gates for Arrow Rain source projectiles. */
public final class ArrowRainArrowRules {
	private ArrowRainArrowRules() {
	}

	public static boolean shouldArm(boolean enabled, boolean bow, boolean playerShooter,
			boolean modeEnabled, int cooldownTicks, boolean hasBurst, boolean spectral,
			boolean allowSpectral, boolean childArrow, int enchantmentLevel) {
		return enabled && bow && playerShooter && modeEnabled && cooldownTicks <= 0
			&& !hasBurst && (!spectral || allowSpectral) && !childArrow && enchantmentLevel > 0;
	}

	public static boolean shouldHandleImpact(boolean armed, boolean triggered,
			boolean entityImpact, boolean blockImpact, boolean childArrow,
			boolean triggerOnEntityHit, boolean triggerOnBlockHit) {
		return armed && !triggered && !childArrow
			&& ((entityImpact && triggerOnEntityHit) || (blockImpact && triggerOnBlockHit));
	}

	public static boolean canCreateRain(int arrowCount, int waves) {
		return arrowCount > 0 && waves > 0;
	}
}
