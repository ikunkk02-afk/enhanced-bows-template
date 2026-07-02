package io.github.ikunkk02.enhancedbows.lightning;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningTargetRulesTest {
	@Test
	void filtersOwnerPlayersInvalidAndAlreadyStruckTargets() {
		UUID owner = UUID.randomUUID();
		UUID mob = UUID.randomUUID();
		UUID player = UUID.randomUUID();
		UUID creative = UUID.randomUUID();
		UUID spectator = UUID.randomUUID();
		UUID dead = UUID.randomUUID();
		UUID removed = UUID.randomUUID();
		Set<UUID> struck = Set.of(mob);
		List<LightningTargetRules.Candidate> candidates = List.of(
			candidate(owner, 1.0, true, false, true, false, false),
			candidate(mob, 2.0, true, false, false, false, false),
			candidate(player, 3.0, true, false, true, false, false),
			candidate(creative, 4.0, true, false, true, true, false),
			candidate(spectator, 5.0, true, false, true, false, true),
			candidate(dead, 6.0, false, false, false, false, false),
			candidate(removed, 7.0, true, true, false, false, false)
		);

		List<LightningTargetRules.Candidate> none = LightningTargetRules.selectNearest(
			candidates, owner, false, false, 8.0, 6, uuid -> !struck.contains(uuid));
		assertTrue(none.isEmpty());

		List<LightningTargetRules.Candidate> playersEnabled = LightningTargetRules.selectNearest(
			candidates, owner, false, true, 8.0, 6, uuid -> !struck.contains(uuid));
		assertEquals(List.of(player), playersEnabled.stream().map(LightningTargetRules.Candidate::uuid).toList());

		List<LightningTargetRules.Candidate> ownerEnabled = LightningTargetRules.selectNearest(
			candidates, owner, true, true, 8.0, 6, uuid -> !struck.contains(uuid));
		assertEquals(List.of(owner, player), ownerEnabled.stream().map(LightningTargetRules.Candidate::uuid).toList());
	}

	@Test
	void selectsNearestTargetsInsideExactRadiusAndCapsAtSix() {
		UUID owner = UUID.randomUUID();
		List<LightningTargetRules.Candidate> candidates = new ArrayList<>();
		for (int distance = 9; distance >= 1; distance--) {
			candidates.add(candidate(UUID.randomUUID(), distance * distance, true, false,
				false, false, false));
		}

		List<LightningTargetRules.Candidate> selected = LightningTargetRules.selectNearest(
			candidates, owner, false, true, 8.0, 6, uuid -> true);

		assertEquals(6, selected.size());
		assertEquals(List.of(1.0, 4.0, 9.0, 16.0, 25.0, 36.0),
			selected.stream().map(LightningTargetRules.Candidate::squaredDistance).toList());
		assertTrue(LightningTargetRules.isInsideRadius(64.0, 8.0));
		assertFalse(LightningTargetRules.isInsideRadius(64.01, 8.0));
	}

	private static LightningTargetRules.Candidate candidate(UUID uuid, double squaredDistance,
			boolean alive, boolean removed, boolean player, boolean creative, boolean spectator) {
		return new LightningTargetRules.Candidate(uuid, squaredDistance, alive, removed,
			player, creative, spectator);
	}
}
