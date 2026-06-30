package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.scan.ScanRules;
import io.github.ikunkk02.enhancedbows.scan.ScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.scan.TrackedScanningArrowAccess;
import io.github.ikunkk02.enhancedbows.scan.SpectralScanController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Minimal hook required because Fabric API has no per-tick event carrying state
 * for an individual vanilla projectile. No general arrow behavior is replaced.
 */
@Mixin(SpectralArrowEntity.class)
public abstract class SpectralArrowEntityMixin extends PersistentProjectileEntity implements ScanningArrowAccess {
	protected SpectralArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> type, World world) {
		super(type, world);
	}

	@Unique
	private boolean enhancedBows$triggerChecked;
	@Unique
	private boolean enhancedBows$scanActive;
	@Unique
	private int enhancedBows$elapsedTicks;
	@Unique
	private int enhancedBows$bounceCount;
	@Unique
	private int enhancedBows$maxBounces = 3;
	@Unique
	private final Set<UUID> enhancedBows$notifiedTargets = new HashSet<>();

	/** Runs the authoritative scan before vanilla movement mutates the initial velocity. */
	@Inject(method = "tick", at = @At("HEAD"))
	private void enhancedBows$tickScan(CallbackInfo callbackInfo) {
		SpectralArrowEntity arrow = (SpectralArrowEntity) (Object) this;
		if (arrow.getWorld().isClient()) {
			return;
		}

		ServerScanConfig.Values config = ServerScanConfig.get();
		Entity rawOwner = arrow.getOwner();
		if (!enhancedBows$triggerChecked) {
			enhancedBows$triggerChecked = true;
			boolean playerOwned = rawOwner instanceof ServerPlayerEntity;
			enhancedBows$scanActive = ScanRules.shouldTrigger(config.enableSpectralArrowScan(), playerOwned);
			if (enhancedBows$scanActive) {
				enhancedBows$setScanningArrow(true);
				enhancedBows$maxBounces = config.scanningArrowMaxBounces();
				SpectralScanController.notifyStarted((ServerPlayerEntity) rawOwner);
			}
		}

		if (!enhancedBows$scanActive || inGround || !(rawOwner instanceof ServerPlayerEntity owner)) {
			enhancedBows$scanActive = false;
			enhancedBows$setScanningArrow(false);
			return;
		}

		if (ScanRules.shouldScanAt(enhancedBows$elapsedTicks, config.scanIntervalTicks())) {
			SpectralScanController.scan(arrow, owner, config, enhancedBows$notifiedTargets);
		}

		enhancedBows$elapsedTicks++;
	}

	/** Persists the one-shot gate and notification set across chunk saves. */
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void enhancedBows$writeScanState(NbtCompound nbt, CallbackInfo callbackInfo) {
		nbt.putBoolean("EnhancedBowsScanChecked", enhancedBows$triggerChecked);
		nbt.putBoolean("EnhancedBowsScanActive", enhancedBows$scanActive);
		nbt.putInt("EnhancedBowsScanElapsed", enhancedBows$elapsedTicks);
		nbt.putBoolean("enhancedbows:is_scanning_arrow", enhancedBows$isScanningArrow());
		nbt.putInt("enhancedbows:bounce_count", enhancedBows$bounceCount);
		nbt.putInt("enhancedbows:max_bounces", enhancedBows$maxBounces);
		NbtList notified = new NbtList();
		for (UUID uuid : enhancedBows$notifiedTargets) {
			notified.add(NbtHelper.fromUuid(uuid));
		}
		nbt.put("EnhancedBowsScanNotified", notified);
	}

	/** Restores persisted scan state without re-running the trigger check. */
	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void enhancedBows$readScanState(NbtCompound nbt, CallbackInfo callbackInfo) {
		enhancedBows$triggerChecked = nbt.getBoolean("EnhancedBowsScanChecked");
		enhancedBows$scanActive = nbt.getBoolean("EnhancedBowsScanActive");
		enhancedBows$elapsedTicks = nbt.getInt("EnhancedBowsScanElapsed");
		enhancedBows$setScanningArrow(nbt.contains("enhancedbows:is_scanning_arrow")
			? nbt.getBoolean("enhancedbows:is_scanning_arrow")
			: enhancedBows$scanActive);
		enhancedBows$bounceCount = Math.max(0, nbt.getInt("enhancedbows:bounce_count"));
		enhancedBows$maxBounces = nbt.contains("enhancedbows:max_bounces")
			? Math.max(0, nbt.getInt("enhancedbows:max_bounces"))
			: 3;
		enhancedBows$notifiedTargets.clear();
		NbtList notified = nbt.getList("EnhancedBowsScanNotified", 11);
		for (int index = 0; index < notified.size(); index++) {
			try {
				enhancedBows$notifiedTargets.add(NbtHelper.toUuid(notified.get(index)));
			} catch (IllegalArgumentException ignored) {
				// Ignore malformed third-party NBT rather than breaking entity loading.
			}
		}
	}

	@Override
	public boolean enhancedBows$isScanningArrow() {
		return ((TrackedScanningArrowAccess) (Object) this).enhancedBows$getTrackedScanning();
	}

	@Override
	public void enhancedBows$setScanningArrow(boolean scanning) {
		((TrackedScanningArrowAccess) (Object) this).enhancedBows$setTrackedScanning(scanning);
	}

	@Override
	public int enhancedBows$getBounceCount() {
		return enhancedBows$bounceCount;
	}

	@Override
	public int enhancedBows$getMaxBounces() {
		return enhancedBows$maxBounces;
	}

	@Override
	public void enhancedBows$recordBounce() {
		enhancedBows$bounceCount++;
	}
}
