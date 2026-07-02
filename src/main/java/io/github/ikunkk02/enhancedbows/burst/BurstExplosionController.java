package io.github.ikunkk02.enhancedbows.burst;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.UUID;

/** Creates one vanilla-presented explosion with explicit server-owned entity effects. */
public final class BurstExplosionController {
	private static final ExplosionBehavior NO_VANILLA_ENTITY_EFFECTS = new ExplosionBehavior() {
		@Override
		public boolean shouldDamage(Explosion explosion, Entity entity) {
			return false;
		}

		@Override
		public float getKnockbackModifier(Entity entity) {
			return 0.0F;
		}
	};

	private BurstExplosionController() {
	}

	public static void triggerEntityImpact(PersistentProjectileEntity projectile, LivingEntity target) {
		trigger(projectile, target.getPos(), true, false);
	}

	public static void triggerBlockImpact(PersistentProjectileEntity projectile, Vec3d position) {
		trigger(projectile, position, false, true);
	}

	private static void trigger(PersistentProjectileEntity projectile, Vec3d center,
			boolean entityImpact, boolean blockImpact) {
		if (!(projectile instanceof BurstArrowAccess burstArrow)
				|| !(projectile.getWorld() instanceof ServerWorld world)
				|| !BurstArrowRules.shouldTrigger(burstArrow.enhancedBows$isBurstArmed(),
					burstArrow.enhancedBows$hasBurstTriggered(), entityImpact, blockImpact)) {
			return;
		}

		burstArrow.enhancedBows$markBurstTriggered();
		ServerScanConfig.Values config = ServerScanConfig.get();
		Entity owner = projectile.getOwner();
		UUID ownerUuid = burstArrow.enhancedBows$getBurstOwnerUuid();
		World.ExplosionSourceType sourceType = config.burstBreakBlocks()
			? World.ExplosionSourceType.BLOCK
			: World.ExplosionSourceType.NONE;
		world.createExplosion(owner, world.getDamageSources().explosion(owner, owner),
			NO_VANILLA_ENTITY_EFFECTS, center, (float) config.burstExplosionPower(),
			config.burstCreateFire(), sourceType);
		applyEntityEffects(world, center, owner, ownerUuid, config);
	}

	private static void applyEntityEffects(ServerWorld world, Vec3d center, Entity owner,
			UUID ownerUuid, ServerScanConfig.Values config) {
		double radius = BurstDamageRules.effectiveRadius(config.burstExplosionPower());
		if (radius <= 0.0) {
			return;
		}
		Box box = new Box(center, center).expand(radius);
		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box, entity -> true)) {
			double distance = target.getBoundingBox().getCenter().distanceTo(center);
			boolean player = target instanceof PlayerEntity;
			boolean creative = player && ((PlayerEntity) target).isCreative();
			boolean spectator = player && ((PlayerEntity) target).isSpectator();
			BurstDamageRules.TargetFacts facts = new BurstDamageRules.TargetFacts(
				target.getUuid(), distance, target.isAlive(), target.isRemoved(),
				player, creative, spectator);
			if (!BurstDamageRules.shouldAffect(facts, ownerUuid, config.burstDamageOwner(),
				config.burstAffectPlayers(), radius)) {
				continue;
			}

			float damage = (float) BurstDamageRules.damageAtDistance(distance, radius,
				config.burstBaseDamage(), config.burstMinDamage());
			if (damage > 0.0F) {
				target.damage(world.getDamageSources().explosion(owner, owner), damage);
			}
			applyKnockback(target, center, distance, radius, config.burstKnockbackMultiplier());
		}
	}

	private static void applyKnockback(LivingEntity target, Vec3d center, double distance,
			double radius, double multiplier) {
		Vec3d offset = target.getBoundingBox().getCenter().subtract(center);
		if (offset.lengthSquared() < 1.0E-8) {
			return;
		}
		double magnitude = BurstDamageRules.knockbackMagnitude(distance, radius, multiplier);
		if (magnitude > 0.0) {
			target.addVelocity(offset.normalize().multiply(magnitude));
		}
	}
}
