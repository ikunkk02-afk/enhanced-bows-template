package io.github.ikunkk02.enhancedbows.lightning;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public final class LightningEvents {
	private LightningEvents() {
	}

	public static void register() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (!ServerScanConfig.get().lightningKeepChargeAfterDeath()) {
				LightningComponents.LIGHTNING_CHARGE.get(newPlayer).resetLightningCharges();
			}
		});
	}
}
