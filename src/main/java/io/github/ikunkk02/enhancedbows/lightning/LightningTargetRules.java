package io.github.ikunkk02.enhancedbows.lightning;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** Pure filtering and nearest-first selection shared by immediate chains and storm scans. */
final class LightningTargetRules {
	private LightningTargetRules() {
	}

	static List<Candidate> selectNearest(List<Candidate> candidates, UUID ownerUuid,
			boolean strikeOwner, boolean strikePlayers, double radius, int limit,
			Predicate<UUID> canStrike) {
		if (limit <= 0) {
			return List.of();
		}
		return candidates.stream()
			.filter(candidate -> isEligible(candidate, ownerUuid, strikeOwner, strikePlayers,
				radius, canStrike))
			.sorted(Comparator.comparingDouble(Candidate::squaredDistance))
			.limit(limit)
			.toList();
	}

	static boolean isInsideRadius(double squaredDistance, double radius) {
		return squaredDistance <= radius * radius;
	}

	private static boolean isEligible(Candidate candidate, UUID ownerUuid, boolean strikeOwner,
			boolean strikePlayers, double radius, Predicate<UUID> canStrike) {
		if (!candidate.alive() || candidate.removed() || !isInsideRadius(candidate.squaredDistance(), radius)) {
			return false;
		}
		if (candidate.uuid().equals(ownerUuid) && !strikeOwner) {
			return false;
		}
		if (candidate.player() && (!strikePlayers || candidate.creative() || candidate.spectator())) {
			return false;
		}
		return canStrike.test(candidate.uuid());
	}

	record Candidate(UUID uuid, double squaredDistance, boolean alive, boolean removed,
		boolean player, boolean creative, boolean spectator) {
	}
}
