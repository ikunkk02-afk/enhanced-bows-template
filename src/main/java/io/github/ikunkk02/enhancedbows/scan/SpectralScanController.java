package io.github.ikunkk02.enhancedbows.scan;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.network.PlayerDetectedS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanStartedS2CPayload;
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
import net.minecraft.world.RaycastContext;

import java.util.Set;
import java.util.UUID;

/** Performs all authoritative world queries, effects, and scan notifications. */
public final class SpectralScanController {
	private SpectralScanController() {
	}

	/** Sends the start notification only when the client advertised the channel. */
	public static void notifyStarted(ServerPlayerEntity owner, int durationTicks) {
		if (ServerPlayNetworking.canSend(owner, ScanStartedS2CPayload.ID)) {
			ServerPlayNetworking.send(owner, new ScanStartedS2CPayload(durationTicks));
		}
	}

	/** Scans a sphere around the arrow and refreshes glowing on every valid target. */
	public static void scan(SpectralArrowEntity arrow, ServerPlayerEntity owner,
			ServerScanConfig.Values config, Set<UUID> notifiedPlayers) {
		ServerWorld world = (ServerWorld) arrow.getWorld();
		double radius = config.scanRadius();
		Box candidates = arrow.getBoundingBox().expand(radius);

		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, candidates,
			entity -> isValidTarget(entity, owner, config))) {
			if (target.squaredDistanceTo(arrow) > radius * radius) {
				continue;
			}
			if (config.scanRequiresLineOfSight() && isOccluded(world, arrow, target)) {
				continue;
			}

			target.addStatusEffect(new StatusEffectInstance(
				StatusEffects.GLOWING, config.glowingDurationTicks(), 0, false, false, true
			), owner);

			if (target instanceof ServerPlayerEntity player && notifiedPlayers.add(player.getUuid())
					&& ServerPlayNetworking.canSend(player, PlayerDetectedS2CPayload.ID)) {
				ServerPlayNetworking.send(player, new PlayerDetectedS2CPayload());
			}
		}
	}

	/** Raycasts from the projectile itself so walls correctly divide scan spaces. */
	private static boolean isOccluded(ServerWorld world, SpectralArrowEntity arrow, LivingEntity target) {
		return world.raycast(new RaycastContext(
			arrow.getPos(), target.getEyePos(), RaycastContext.ShapeType.COLLIDER,
			RaycastContext.FluidHandling.NONE, arrow
		)).getType() != HitResult.Type.MISS;
	}

	private static boolean isValidTarget(LivingEntity target, Entity owner, ServerScanConfig.Values config) {
		boolean player = target instanceof PlayerEntity;
		boolean spectator = player && ((PlayerEntity) target).isSpectator();
		return ScanRules.shouldIncludeTarget(player, target == owner, spectator, target.isAlive(),
			config.scanPlayers(), config.scanOwner());
	}
}
