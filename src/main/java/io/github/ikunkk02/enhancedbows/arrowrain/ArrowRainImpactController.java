package io.github.ikunkk02.enhancedbows.arrowrain;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/** Consumes one player's cooldown and creates one Arrow Rain schedule per armed source arrow. */
public final class ArrowRainImpactController {
	private ArrowRainImpactController() {
	}

	public static void triggerEntityImpact(PersistentProjectileEntity projectile, LivingEntity target) {
		trigger(projectile, target.getPos(), true, false);
	}

	public static void triggerBlockImpact(PersistentProjectileEntity projectile, Vec3d position) {
		trigger(projectile, position, false, true);
	}

	private static void trigger(PersistentProjectileEntity projectile, Vec3d center,
			boolean entityImpact, boolean blockImpact) {
		ServerScanConfig.Values config = ServerScanConfig.get();
		if (!(projectile instanceof ArrowRainArrowAccess rainArrow)
				|| !(projectile.getWorld() instanceof ServerWorld world)
				|| !ArrowRainArrowRules.shouldHandleImpact(rainArrow.enhancedBows$isArrowRainArmed(),
					rainArrow.enhancedBows$hasArrowRainTriggered(), entityImpact, blockImpact,
					rainArrow.enhancedBows$isArrowRainChild(), config.arrowRainTriggerOnEntityHit(),
					config.arrowRainTriggerOnBlockHit())
				|| !ArrowRainArrowRules.canCreateRain(config.arrowRainArrowCount(),
					config.arrowRainWaves())) {
			return;
		}

		rainArrow.enhancedBows$markArrowRainTriggered();
		UUID ownerUuid = rainArrow.enhancedBows$getArrowRainOwnerUuid();
		ServerPlayerEntity owner = ownerUuid == null
			? null
			: world.getServer().getPlayerManager().getPlayer(ownerUuid);
		if (owner == null) {
			return;
		}

		ArrowRainComponent component = ArrowRainComponents.ARROW_RAIN.get(owner);
		if (!component.tryStartArrowRainCooldown(config.arrowRainCooldownTicks())) {
			return;
		}
		ArrowRainManager.add(world, center, ownerUuid,
			rainArrow.enhancedBows$getArrowRainBaseDamage(), config);
	}
}
