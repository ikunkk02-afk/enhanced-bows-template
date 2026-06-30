package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.scan.BouncePhysics;
import io.github.ikunkk02.enhancedbows.scan.ScanningArrowAccess;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels vanilla block embedding only for activated scanning spectral arrows.
 * Every other persistent projectile returns immediately and stays vanilla.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
	@Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
	private void enhancedBows$bounceScanningArrow(BlockHitResult hit, CallbackInfo callbackInfo) {
		PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
		if (!(projectile instanceof SpectralArrowEntity)
				|| !(projectile instanceof ScanningArrowAccess scanningArrow)
				|| projectile.getWorld().isClient()) {
			return;
		}

		ServerScanConfig.Values config = ServerScanConfig.get();
		double speed = projectile.getVelocity().length();
		if (!BouncePhysics.canBounce(config.enableScanningArrowBounce(), scanningArrow.enhancedBows$isScanningArrow(),
			scanningArrow.enhancedBows$getBounceCount(), scanningArrow.enhancedBows$getMaxBounces(), speed,
			config.scanningArrowBounceDamping(), config.scanningArrowMinBounceVelocity())) {
			return;
		}

		Vec3d reflected = BouncePhysics.reflect(projectile.getVelocity(), hit.getSide(),
			config.scanningArrowBounceDamping());
		Direction normal = hit.getSide();
		Vec3d releasePoint = hit.getPos().add(
			normal.getOffsetX() * 0.05,
			normal.getOffsetY() * 0.05,
			normal.getOffsetZ() * 0.05
		);

		projectile.setPos(releasePoint.x, releasePoint.y, releasePoint.z);
		projectile.setVelocity(reflected);
		projectile.velocityDirty = true;
		projectile.velocityModified = true;
		projectile.shake = 0;
		scanningArrow.enhancedBows$recordBounce();

		ServerWorld world = (ServerWorld) projectile.getWorld();
		world.playSound(null, releasePoint.x, releasePoint.y, releasePoint.z,
			SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 0.35F, 1.35F);
		world.spawnParticles(ParticleTypes.END_ROD, releasePoint.x, releasePoint.y, releasePoint.z,
			6, 0.08, 0.08, 0.08, 0.01);
		callbackInfo.cancel();
	}
}
