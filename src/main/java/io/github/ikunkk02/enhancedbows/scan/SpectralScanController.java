package io.github.ikunkk02.enhancedbows.scan;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.network.PlayerDetectedS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanStartedS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCue;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCueS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Set;
import java.util.UUID;

/** Performs all authoritative world queries, effects, and scan notifications. */
public final class SpectralScanController {
	private SpectralScanController() {
	}

	/** Sends the start notification only when the client advertised the channel. */
	public static void notifyStarted(ServerPlayerEntity owner) {
		if (ServerPlayNetworking.canSend(owner, ScanStartedS2CPayload.ID)) {
			ServerPlayNetworking.send(owner, new ScanStartedS2CPayload());
		}
	}

	/** Scans a sphere around the arrow and refreshes glowing on every valid target. */
	public static void scan(SpectralArrowEntity arrow, ServerPlayerEntity owner,
			ServerScanConfig.Values config, Set<UUID> notifiedTargets) {
		ServerWorld world = (ServerWorld) arrow.getWorld();
		double radius = config.scanRadius();
		Box candidates = arrow.getBoundingBox().expand(radius);

		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, candidates,
			entity -> isValidTarget(entity, owner, config))) {
			if (target.squaredDistanceTo(arrow) > radius * radius) {
				continue;
			}
			if (config.scanRequiresLineOfSight() && !hasLineOfSight(world, arrow, target,
				config.strictScanLineOfSight())) {
				continue;
			}

			target.addStatusEffect(new StatusEffectInstance(
				StatusEffects.GLOWING, config.glowingDurationTicks(), 0, false, false, true
			), owner);

			boolean firstDetection = notifiedTargets.add(target.getUuid());
			if (target instanceof ServerPlayerEntity player
					&& ServerPlayNetworking.canSend(player, PlayerDetectedS2CPayload.ID)) {
				ServerPlayNetworking.send(player, new PlayerDetectedS2CPayload());
			}
			if (firstDetection && ServerPlayNetworking.canSend(owner, ScanSoundCueS2CPayload.ID)) {
				ServerPlayNetworking.send(owner, new ScanSoundCueS2CPayload(ScanSoundCue.MARK_SUCCESS));
			}
		}
	}

	/** Requires clear collider raycasts to both the target eye and body center in strict mode. */
	private static boolean hasLineOfSight(ServerWorld world, SpectralArrowEntity arrow, LivingEntity target,
			boolean strict) {
		Vec3d start = arrow.getPos().add(0.0, 0.1, 0.0);
		Vec3d targetEye = target.getEyePos();
		Vec3d targetCenter = target.getBoundingBox().getCenter();
		boolean eyeClear = raycastClear(world, arrow, start, targetEye);
		boolean centerClear = raycastClear(world, arrow, start, targetCenter);
		if (!ScanRules.hasLineOfSight(eyeClear, centerClear, strict)) {
			return false;
		}
		if (!strict) {
			return true;
		}
		return ScanRules.hasCompatibleSkyExposure(
			isSkyExposed(world, start),
			isSkyExposed(world, targetEye),
			isSkyExposed(world, targetCenter)
		);
	}

	private static boolean isSkyExposed(ServerWorld world, Vec3d position) {
		return world.isSkyVisible(BlockPos.ofFloored(position.x, position.y, position.z));
	}

	private static boolean raycastClear(ServerWorld world, SpectralArrowEntity arrow, Vec3d start, Vec3d end) {
		return world.raycast(new RaycastContext(
			start, end, RaycastContext.ShapeType.COLLIDER,
			RaycastContext.FluidHandling.NONE, arrow
		)).getType() == HitResult.Type.MISS;
	}

	private static boolean isValidTarget(LivingEntity target, Entity owner, ServerScanConfig.Values config) {
		boolean player = target instanceof PlayerEntity;
		boolean spectator = player && ((PlayerEntity) target).isSpectator();
		return ScanRules.shouldIncludeTarget(player, target == owner, spectator, target.isAlive(),
			config.scanPlayers(), config.scanOwner());
	}
}
