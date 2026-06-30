package io.github.ikunkk02.enhancedbows.client.network;

import io.github.ikunkk02.enhancedbows.client.hud.ScanHudState;
import io.github.ikunkk02.enhancedbows.client.sound.ClientScanSoundPlayer;
import io.github.ikunkk02.enhancedbows.network.PlayerDetectedS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCue;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCueS2CPayload;
import io.github.ikunkk02.enhancedbows.network.ScanStartedS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Util;

/** Registers the two S2C handlers and switches back to the render thread. */
public final class ClientScanNetworking {
	private static final DetectionNoticeCooldown DETECTION_NOTICE_COOLDOWN = new DetectionNoticeCooldown(2_000L);

	private ClientScanNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(ScanStartedS2CPayload.ID,
			(payload, context) -> context.client().execute(() -> {
				ScanHudState.startScanning();
				ClientScanSoundPlayer.play(ScanSoundCue.SCAN_START);
			}));
		ClientPlayNetworking.registerGlobalReceiver(PlayerDetectedS2CPayload.ID,
			(payload, context) -> context.client().execute(() -> {
				if (DETECTION_NOTICE_COOLDOWN.tryAcquire(Util.getMeasuringTimeMs())) {
					ScanHudState.startDetected();
					ClientScanSoundPlayer.play(ScanSoundCue.DETECTED);
				}
			}));
		ClientPlayNetworking.registerGlobalReceiver(ScanSoundCueS2CPayload.ID,
			(payload, context) -> context.client().execute(() -> ClientScanSoundPlayer.play(payload.cue())));
	}
}
