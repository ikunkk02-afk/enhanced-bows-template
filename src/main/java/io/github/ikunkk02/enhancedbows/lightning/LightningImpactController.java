package io.github.ikunkk02.enhancedbows.lightning;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Converts one qualifying projectile impact into one charge consumption and one storm. */
public final class LightningImpactController {
	private LightningImpactController() {
	}

	public static void triggerEntityImpact(PersistentProjectileEntity projectile, LivingEntity target) {
		trigger(projectile, target.getPos(), target, true, false);
	}

	public static void triggerBlockImpact(PersistentProjectileEntity projectile, Vec3d position) {
		trigger(projectile, position, null, false, true);
	}

	private static void trigger(PersistentProjectileEntity projectile, Vec3d center,
			LivingEntity primaryTarget, boolean entityImpact, boolean blockImpact) {
		if (!(projectile instanceof LightningArrowAccess lightningArrow)
				|| !(projectile.getWorld() instanceof ServerWorld world)
				|| !(projectile.getOwner() instanceof ServerPlayerEntity owner)) {
			return;
		}

		ServerScanConfig.Values config = ServerScanConfig.get();
		LightningChargeComponent component = LightningComponents.LIGHTNING_CHARGE.get(owner);
		boolean creativeInfinite = owner.isCreative() && config.lightningAllowCreativeInfinite();
		boolean hasChargeOrInfinite = component.getLightningCharges() > 0 || creativeInfinite;
		if (!LightningArrowRules.shouldTriggerImpact(lightningArrow.enhancedBows$isLightningArmed(),
			lightningArrow.enhancedBows$hasTriggeredLightning(), true, hasChargeOrInfinite,
			entityImpact, blockImpact)) {
			return;
		}

		if (LightningArrowRules.shouldConsumeCharge(owner.isCreative(),
			config.lightningAllowCreativeInfinite()) && !component.consumeLightningCharge()) {
			return;
		}
		lightningArrow.enhancedBows$markLightningTriggered();

		LightningStorm storm = new LightningStorm(world, center, owner.getUuid(), config);
		if (primaryTarget != null) {
			storm.startAtEntity(primaryTarget);
		} else {
			storm.startAtBlock();
		}
		LightningStormManager.add(storm, world);
	}
}
