package io.github.ikunkk02.enhancedbows.burst;

/** Pure arming priority and one-shot impact gates for Burst projectiles. */
public final class BurstArrowRules {
	private BurstArrowRules() {
	}

	public static ArmDecision decideArming(boolean burstEnabled, boolean lightningEnabled,
			boolean bow, boolean playerShooter, boolean spectral, int burstLevel, int lightningLevel) {
		if (!bow || !playerShooter) {
			return ArmDecision.NONE;
		}
		if (burstLevel > 0 && lightningLevel > 0) {
			return lightningEnabled ? ArmDecision.LIGHTNING : ArmDecision.NONE;
		}
		if (lightningEnabled && lightningLevel > 0) {
			return ArmDecision.LIGHTNING;
		}
		if (burstEnabled && burstLevel > 0 && !spectral) {
			return ArmDecision.BURST;
		}
		return ArmDecision.NONE;
	}

	public static boolean shouldTrigger(boolean armed, boolean triggered,
			boolean entityImpact, boolean blockImpact) {
		return armed && !triggered && (entityImpact || blockImpact);
	}

	public enum ArmDecision {
		NONE,
		LIGHTNING,
		BURST
	}
}
