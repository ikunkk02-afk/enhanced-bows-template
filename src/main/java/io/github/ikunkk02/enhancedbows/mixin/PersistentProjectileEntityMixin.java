package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCue;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCueS2CPayload;
import io.github.ikunkk02.enhancedbows.scan.BouncePhysics;
import io.github.ikunkk02.enhancedbows.scan.ScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.scan.TrackedScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowRules;
import io.github.ikunkk02.enhancedbows.lightning.LightningChargeComponent;
import io.github.ikunkk02.enhancedbows.lightning.LightningComponents;
import io.github.ikunkk02.enhancedbows.lightning.LightningStrikeController;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels vanilla block embedding only for activated scanning spectral arrows.
 * Every other persistent projectile returns immediately and stays vanilla.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements LightningArrowAccess, TrackedScanningArrowAccess {
	@Unique
	private static final TrackedData<Boolean> ENHANCED_BOWS_SCANNING = DataTracker.registerData(
		PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	@Unique
	private boolean enhancedBows$lightningArmed;
	@Unique
	private boolean enhancedBows$lightningTriggered;

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void enhancedBows$trackScanningState(DataTracker.Builder builder, CallbackInfo callbackInfo) {
		builder.add(ENHANCED_BOWS_SCANNING, false);
	}

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
		for (var player : PlayerLookup.tracking(projectile)) {
			if (ServerPlayNetworking.canSend(player, ScanSoundCueS2CPayload.ID)) {
				ServerPlayNetworking.send(player, new ScanSoundCueS2CPayload(ScanSoundCue.BOUNCE));
			}
		}
		world.spawnParticles(ParticleTypes.END_ROD, releasePoint.x, releasePoint.y, releasePoint.z,
			6, 0.08, 0.08, 0.08, 0.01);
		callbackInfo.cancel();
	}

	@Inject(method = "onEntityHit", at = @At("TAIL"))
	private void enhancedBows$triggerLightning(EntityHitResult hit, CallbackInfo callbackInfo) {
		PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
		if (!(projectile.getWorld() instanceof ServerWorld world)
				|| !(hit.getEntity() instanceof LivingEntity target)
				|| !(projectile.getOwner() instanceof ServerPlayerEntity owner)) {
			return;
		}
		ServerScanConfig.Values config = ServerScanConfig.get();
		LightningChargeComponent component = LightningComponents.LIGHTNING_CHARGE.get(owner);
		boolean creativeInfinite = owner.isCreative() && config.lightningAllowCreativeInfinite();
		if (!LightningArrowRules.shouldTrigger(enhancedBows$lightningArmed, enhancedBows$lightningTriggered,
			true, target.isAlive(), true, component.getLightningCharges() > 0, creativeInfinite)) {
			return;
		}
		if (LightningArrowRules.shouldConsumeCharge(owner.isCreative(), config.lightningAllowCreativeInfinite())
				&& !component.consumeLightningCharge()) {
			return;
		}
		enhancedBows$lightningTriggered = true;
		LightningStrikeController.strike(world, target, config.lightningDamageMode());
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void enhancedBows$writeLightningState(NbtCompound nbt, CallbackInfo callbackInfo) {
		nbt.putBoolean("enhancedbows:lightning_armed", enhancedBows$lightningArmed);
		nbt.putBoolean("enhancedbows:lightning_triggered", enhancedBows$lightningTriggered);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void enhancedBows$readLightningState(NbtCompound nbt, CallbackInfo callbackInfo) {
		enhancedBows$lightningArmed = nbt.getBoolean("enhancedbows:lightning_armed");
		enhancedBows$lightningTriggered = nbt.getBoolean("enhancedbows:lightning_triggered");
	}

	@Override
	public void enhancedBows$armLightning() {
		enhancedBows$lightningArmed = true;
	}

	@Override
	public boolean enhancedBows$isLightningArmed() {
		return enhancedBows$lightningArmed;
	}

	@Override
	public boolean enhancedBows$hasTriggeredLightning() {
		return enhancedBows$lightningTriggered;
	}

	@Override
	public void enhancedBows$markLightningTriggered() {
		enhancedBows$lightningTriggered = true;
	}

	@Override
	public boolean enhancedBows$getTrackedScanning() {
		PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
		return projectile.getDataTracker().get(ENHANCED_BOWS_SCANNING);
	}

	@Override
	public void enhancedBows$setTrackedScanning(boolean scanning) {
		PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
		projectile.getDataTracker().set(ENHANCED_BOWS_SCANNING, scanning);
	}
}
