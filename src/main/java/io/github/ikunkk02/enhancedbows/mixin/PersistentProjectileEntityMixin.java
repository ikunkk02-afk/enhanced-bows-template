package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.burst.BurstArrowAccess;
import io.github.ikunkk02.enhancedbows.burst.BurstExplosionController;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainArrowAccess;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainImpactController;
import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCue;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCueS2CPayload;
import io.github.ikunkk02.enhancedbows.scan.BouncePhysics;
import io.github.ikunkk02.enhancedbows.scan.ScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.scan.TrackedScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningImpactController;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Cancels vanilla block embedding only for activated scanning spectral arrows.
 * Every other persistent projectile returns immediately and stays vanilla.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements LightningArrowAccess, BurstArrowAccess, ArrowRainArrowAccess, TrackedScanningArrowAccess {
	@Unique
	private static final TrackedData<Boolean> ENHANCED_BOWS_SCANNING = DataTracker.registerData(
		PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	@Unique
	private boolean enhancedBows$lightningArmed;
	@Unique
	private boolean enhancedBows$lightningTriggered;
	@Unique
	private boolean enhancedBows$burstArmed;
	@Unique
	private boolean enhancedBows$burstTriggered;
	@Unique
	private UUID enhancedBows$burstOwnerUuid;
	@Unique
	private boolean enhancedBows$arrowRainArmed;
	@Unique
	private boolean enhancedBows$arrowRainTriggered;
	@Unique
	private UUID enhancedBows$arrowRainOwnerUuid;
	@Unique
	private double enhancedBows$arrowRainBaseDamage;
	@Unique
	private boolean enhancedBows$arrowRainChild;
	@Unique
	private int enhancedBows$arrowRainChildAge;
	@Unique
	private int enhancedBows$arrowRainChildLifetime;

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void enhancedBows$trackScanningState(DataTracker.Builder builder, CallbackInfo callbackInfo) {
		builder.add(ENHANCED_BOWS_SCANNING, false);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void enhancedBows$expireArrowRainChild(CallbackInfo callbackInfo) {
		if (!enhancedBows$arrowRainChild || ((PersistentProjectileEntity) (Object) this).getWorld().isClient()) {
			return;
		}
		enhancedBows$arrowRainChildAge++;
		if (enhancedBows$arrowRainChildAge >= enhancedBows$arrowRainChildLifetime) {
			PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
			projectile.discard();
			callbackInfo.cancel();
		}
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
		if (hit.getEntity() instanceof LivingEntity target) {
			LightningImpactController.triggerEntityImpact(projectile, target);
			BurstExplosionController.triggerEntityImpact(projectile, target);
			ArrowRainImpactController.triggerEntityImpact(projectile, target);
		}
	}

	@Inject(method = "onBlockHit", at = @At("TAIL"))
	private void enhancedBows$triggerBlockLightning(BlockHitResult hit, CallbackInfo callbackInfo) {
		PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
		LightningImpactController.triggerBlockImpact(projectile, hit.getPos());
		BurstExplosionController.triggerBlockImpact(projectile, hit.getPos());
		ArrowRainImpactController.triggerBlockImpact(projectile, hit.getPos());
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void enhancedBows$writeLightningState(NbtCompound nbt, CallbackInfo callbackInfo) {
		nbt.putBoolean("enhancedbows:lightning_armed", enhancedBows$lightningArmed);
		nbt.putBoolean("enhancedbows:lightning_triggered", enhancedBows$lightningTriggered);
		nbt.putBoolean("enhancedbows:burst_arrow", enhancedBows$burstArmed);
		nbt.putBoolean("enhancedbows:burst_triggered", enhancedBows$burstTriggered);
		if (enhancedBows$burstOwnerUuid != null) {
			nbt.putUuid("enhancedbows:burst_owner_uuid", enhancedBows$burstOwnerUuid);
		}
		nbt.putBoolean("enhancedbows:arrow_rain_armed", enhancedBows$arrowRainArmed);
		nbt.putBoolean("enhancedbows:arrow_rain_triggered", enhancedBows$arrowRainTriggered);
		if (enhancedBows$arrowRainOwnerUuid != null) {
			nbt.putUuid("enhancedbows:arrow_rain_owner_uuid", enhancedBows$arrowRainOwnerUuid);
		}
		nbt.putDouble("enhancedbows:arrow_rain_base_damage", enhancedBows$arrowRainBaseDamage);
		nbt.putBoolean("enhancedbows:arrow_rain_child", enhancedBows$arrowRainChild);
		nbt.putInt("enhancedbows:arrow_rain_child_age", enhancedBows$arrowRainChildAge);
		nbt.putInt("enhancedbows:arrow_rain_child_lifetime", enhancedBows$arrowRainChildLifetime);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void enhancedBows$readLightningState(NbtCompound nbt, CallbackInfo callbackInfo) {
		enhancedBows$lightningArmed = nbt.getBoolean("enhancedbows:lightning_armed");
		enhancedBows$lightningTriggered = nbt.getBoolean("enhancedbows:lightning_triggered");
		enhancedBows$burstArmed = nbt.getBoolean("enhancedbows:burst_arrow");
		enhancedBows$burstTriggered = nbt.getBoolean("enhancedbows:burst_triggered");
		enhancedBows$burstOwnerUuid = nbt.containsUuid("enhancedbows:burst_owner_uuid")
			? nbt.getUuid("enhancedbows:burst_owner_uuid")
			: null;
		enhancedBows$arrowRainArmed = nbt.getBoolean("enhancedbows:arrow_rain_armed");
		enhancedBows$arrowRainTriggered = nbt.getBoolean("enhancedbows:arrow_rain_triggered");
		enhancedBows$arrowRainOwnerUuid = nbt.containsUuid("enhancedbows:arrow_rain_owner_uuid")
			? nbt.getUuid("enhancedbows:arrow_rain_owner_uuid")
			: null;
		enhancedBows$arrowRainBaseDamage = nbt.getDouble("enhancedbows:arrow_rain_base_damage");
		enhancedBows$arrowRainChild = nbt.getBoolean("enhancedbows:arrow_rain_child");
		enhancedBows$arrowRainChildAge = nbt.getInt("enhancedbows:arrow_rain_child_age");
		enhancedBows$arrowRainChildLifetime = nbt.getInt("enhancedbows:arrow_rain_child_lifetime");
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
	public void enhancedBows$armBurst(UUID ownerUuid) {
		enhancedBows$burstArmed = true;
		enhancedBows$burstOwnerUuid = ownerUuid;
	}

	@Override
	public boolean enhancedBows$isBurstArmed() {
		return enhancedBows$burstArmed;
	}

	@Override
	public boolean enhancedBows$hasBurstTriggered() {
		return enhancedBows$burstTriggered;
	}

	@Override
	public UUID enhancedBows$getBurstOwnerUuid() {
		return enhancedBows$burstOwnerUuid;
	}

	@Override
	public void enhancedBows$markBurstTriggered() {
		enhancedBows$burstTriggered = true;
	}

	@Override
	public void enhancedBows$armArrowRain(UUID ownerUuid, double baseDamage) {
		enhancedBows$arrowRainArmed = true;
		enhancedBows$arrowRainOwnerUuid = ownerUuid;
		enhancedBows$arrowRainBaseDamage = Math.max(0.0, baseDamage);
	}

	@Override
	public boolean enhancedBows$isArrowRainArmed() {
		return enhancedBows$arrowRainArmed;
	}

	@Override
	public boolean enhancedBows$hasArrowRainTriggered() {
		return enhancedBows$arrowRainTriggered;
	}

	@Override
	public void enhancedBows$markArrowRainTriggered() {
		enhancedBows$arrowRainTriggered = true;
	}

	@Override
	public UUID enhancedBows$getArrowRainOwnerUuid() {
		return enhancedBows$arrowRainOwnerUuid;
	}

	@Override
	public double enhancedBows$getArrowRainBaseDamage() {
		return enhancedBows$arrowRainBaseDamage;
	}

	@Override
	public void enhancedBows$markArrowRainChild(int lifetimeTicks) {
		enhancedBows$arrowRainChild = true;
		enhancedBows$arrowRainChildAge = 0;
		enhancedBows$arrowRainChildLifetime = Math.max(1, lifetimeTicks);
	}

	@Override
	public boolean enhancedBows$isArrowRainChild() {
		return enhancedBows$arrowRainChild;
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
