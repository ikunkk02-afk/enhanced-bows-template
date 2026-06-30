package io.github.ikunkk02.enhancedbows.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Registers all common payload codecs without loading any client classes. */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(ScanStartedS2CPayload.ID, ScanStartedS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(PlayerDetectedS2CPayload.ID, PlayerDetectedS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScanSoundCueS2CPayload.ID, ScanSoundCueS2CPayload.CODEC);
	}
}
