package io.github.ikunkk02.enhancedbows.burst;

import java.util.UUID;

/** Pure spherical target, damage, and knockback rules for Burst explosions. */
public final class BurstDamageRules {
	private BurstDamageRules() {
	}

	public static double effectiveRadius(double explosionPower) {
		return Math.max(0.0, explosionPower * 2.0);
	}

	public static double damageAtDistance(double distance, double radius,
			double baseDamage, double minDamage) {
		if (radius <= 0.0 || distance > radius) {
			return 0.0;
		}
		double normalizedDistance = Math.max(0.0, Math.min(distance / radius, 1.0));
		return baseDamage + (minDamage - baseDamage) * normalizedDistance;
	}

	public static boolean shouldAffect(TargetFacts target, UUID ownerUuid,
			boolean damageOwner, boolean affectPlayers, double radius) {
		if (!target.alive() || target.removed() || target.distance() > radius) {
			return false;
		}
		if (target.uuid().equals(ownerUuid) && !damageOwner) {
			return false;
		}
		return !target.player() || affectPlayers && !target.creative() && !target.spectator();
	}

	public static double knockbackMagnitude(double distance, double radius, double multiplier) {
		if (radius <= 0.0 || distance >= radius) {
			return 0.0;
		}
		return Math.max(0.0, 1.0 - distance / radius) * Math.max(0.0, multiplier);
	}

	public record TargetFacts(UUID uuid, double distance, boolean alive, boolean removed,
		boolean player, boolean creative, boolean spectator) {
	}
}
