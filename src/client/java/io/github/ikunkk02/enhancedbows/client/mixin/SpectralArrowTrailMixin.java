package io.github.ikunkk02.enhancedbows.client.mixin;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.client.trail.RedTrailSampler;
import io.github.ikunkk02.enhancedbows.scan.ScanningArrowAccess;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectralArrowEntity.class)
public abstract class SpectralArrowTrailMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancedBows$renderRedTrail(CallbackInfo callbackInfo) {
		SpectralArrowEntity arrow = (SpectralArrowEntity) (Object) this;
		World world = arrow.getWorld();
		ClientScanConfig.Values config = ClientScanConfig.get();
		if (!world.isClient() || !config.enableScanningArrowRedTrail()
				|| !(arrow instanceof ScanningArrowAccess scanningArrow)
				|| !scanningArrow.enhancedBows$isScanningArrow()) {
			return;
		}

		DustParticleEffect particle = new DustParticleEffect(DustParticleEffect.RED,
			(float) config.redTrailParticleSize());
		for (var point : RedTrailSampler.sample(arrow.getPos(), arrow.getVelocity(),
			config.redTrailLength(), config.redTrailParticleCount())) {
			world.addParticle(particle, point.x, point.y, point.z, 0.0, 0.0, 0.0);
		}
	}
}
