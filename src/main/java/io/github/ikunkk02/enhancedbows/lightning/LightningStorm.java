package io.github.ikunkk02.enhancedbows.lightning;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** One bounded, server-owned Lightning storm region. */
final class LightningStorm {
	private static final int REPEAT_STRIKE_COOLDOWN_TICKS = 40;

	private final ServerWorld world;
	private final Vec3d center;
	private final UUID ownerUuid;
	private final ServerScanConfig.Values config;
	private final LightningStormState state;

	LightningStorm(ServerWorld world, Vec3d center, UUID ownerUuid, ServerScanConfig.Values config) {
		this.world = world;
		this.center = center;
		this.ownerUuid = ownerUuid;
		this.config = config;
		this.state = new LightningStormState(config.lightningStormDurationTicks(),
			config.lightningStormScanIntervalTicks(),
			config.lightningAllowRepeatStrikeInSameStorm(), REPEAT_STRIKE_COOLDOWN_TICKS);
	}

	void startAtBlock() {
		LightningStrikeController.visual(world, center);
	}

	void startAtEntity(LivingEntity primaryTarget) {
		if (primaryTarget.isAlive()) {
			strikeSelected(List.of(primaryTarget), config.lightningChainRadius(), 1,
				(float) config.lightningBonusDamage());
		} else {
			LightningStrikeController.visual(world, center);
		}
		strikeNearby(config.lightningChainRadius(), config.lightningMaxChainTargets(),
			(float) config.lightningChainBonusDamage());
	}

	/** Returns true when the storm has expired and should be removed. */
	boolean tick() {
		boolean shouldScan = state.tickAndShouldScan();
		if (shouldScan) {
			strikeNearby(config.lightningStormRadius(), config.lightningMaxChainTargets(),
				(float) config.lightningChainBonusDamage());
		}
		return state.isExpired();
	}

	private void strikeNearby(double radius, int limit, float damage) {
		Box box = new Box(center, center).expand(radius);
		strikeSelected(world.getEntitiesByClass(LivingEntity.class, box, entity -> true),
			radius, limit, damage);
	}

	private void strikeSelected(List<? extends LivingEntity> entities, double radius, int limit, float damage) {
		Map<UUID, LivingEntity> byUuid = new HashMap<>();
		List<LightningTargetRules.Candidate> candidates = entities.stream().map(entity -> {
			byUuid.put(entity.getUuid(), entity);
			boolean player = entity instanceof PlayerEntity;
			boolean creative = player && ((PlayerEntity) entity).isCreative();
			boolean spectator = player && ((PlayerEntity) entity).isSpectator();
			return new LightningTargetRules.Candidate(entity.getUuid(), entity.squaredDistanceTo(center),
				entity.isAlive(), entity.isRemoved(), player, creative, spectator);
		}).toList();

		for (LightningTargetRules.Candidate candidate : LightningTargetRules.selectNearest(
			candidates, ownerUuid, config.lightningStrikeOwner(), config.lightningStrikePlayers(),
			radius, limit, state::canStrike)) {
			LivingEntity target = byUuid.get(candidate.uuid());
			if (target == null) {
				continue;
			}
			state.recordStrike(candidate.uuid());
			LightningStrikeController.strike(world, target, damage, config.lightningSetFireSeconds());
		}
	}
}
