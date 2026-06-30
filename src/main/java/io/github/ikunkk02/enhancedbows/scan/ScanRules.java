package io.github.ikunkk02.enhancedbows.scan;

/**
 * Pure scan predicates kept separate from Minecraft objects so the authority
 * rules can be unit-tested without booting a game instance.
 */
public final class ScanRules {
	private ScanRules() {
	}

	/** Returns whether a newly spawned spectral arrow may enter scan mode. */
	public static boolean shouldTrigger(boolean enabled, boolean playerOwned) {
		return enabled && playerOwned;
	}

	/** Returns whether an active arrow scans on this zero-based elapsed flight tick. */
	public static boolean shouldScanAt(int elapsedTicks, int intervalTicks) {
		return elapsedTicks >= 0
			&& intervalTicks > 0
			&& elapsedTicks % intervalTicks == 0;
	}

	/** Combines the eye and body-center raycast samples using the configured policy. */
	public static boolean hasLineOfSight(boolean eyeClear, boolean centerClear, boolean strict) {
		return strict ? eyeClear && centerClear : eyeClear || centerClear;
	}

	/** Prevents strict scans from crossing between exposed outdoor and covered indoor spaces. */
	public static boolean hasCompatibleSkyExposure(boolean arrowExposed, boolean targetEyeExposed,
			boolean targetCenterExposed) {
		return arrowExposed == targetEyeExposed && arrowExposed == targetCenterExposed;
	}

	/** Applies all target-selection policy that does not require world access. */
	public static boolean shouldIncludeTarget(boolean player, boolean owner, boolean spectator, boolean alive,
			boolean scanPlayers, boolean scanOwner) {
		if (!alive || spectator) {
			return false;
		}
		if (player && !scanPlayers) {
			return false;
		}
		return !owner || scanOwner;
	}
}
