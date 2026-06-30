package io.github.ikunkk02.enhancedbows.scan;

/**
 * Pure scan predicates kept separate from Minecraft objects so the authority
 * rules can be unit-tested without booting a game instance.
 */
public final class ScanRules {
	private ScanRules() {
	}

	/** Returns whether a newly spawned spectral arrow may enter scan mode. */
	public static boolean shouldTrigger(boolean enabled, boolean playerOwned, double upwardVelocity,
			double upwardVelocityThreshold) {
		return enabled && playerOwned && upwardVelocity > upwardVelocityThreshold;
	}

	/** Returns whether an active arrow scans on this zero-based elapsed tick. */
	public static boolean shouldScanAt(int elapsedTicks, int durationTicks, int intervalTicks) {
		return elapsedTicks >= 0
			&& elapsedTicks < durationTicks
			&& intervalTicks > 0
			&& elapsedTicks % intervalTicks == 0;
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
