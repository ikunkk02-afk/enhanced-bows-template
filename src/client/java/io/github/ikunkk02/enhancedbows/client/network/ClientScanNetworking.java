package io.github.ikunkk02.enhancedbows.client.network;

import io.github.ikunkk02.enhancedbows.client.hud.ScanHudState;
import io.github.ikunkk02.enhancedbows.network.PlayerDetectedS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanStartedS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Registers the two S2C handlers and switches back to the render thread. */
public final class ClientScanNetworking {
	private ClientScanNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(ScanStartedS2CPayload.ID,
			(payload, context) -> context.client().execute(() -> ScanHudState.startScanning(payload.durationTicks())));
		ClientPlayNetworking.registerGlobalReceiver(PlayerDetectedS2CPayload.ID,
			(payload, context) -> context.client().execute(ScanHudState::startDetected));
	}
}
