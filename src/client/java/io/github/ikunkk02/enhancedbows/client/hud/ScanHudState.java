package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;

import java.util.List;

/** Owns the two one-shot HUD timelines; detected warnings take visual priority. */
public final class ScanHudState {
	private static int scanningFrame;
	private static int scanningTicksRemaining;
	private static int detectedFrame;
	private static boolean detectedActive;

	private ScanHudState() {
	}

	/** Starts the owner animation and both one-shot scan sounds. */
	public static void startScanning(int durationTicks) {
		scanningFrame = 0;
		scanningTicksRemaining = Math.max(0, Math.min(durationTicks, 100));
		if (ClientScanConfig.get().enableScanSounds()) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.SCANNING_START, 1.0F));
			client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.SCANNING_LOOP, 1.0F));
		}
	}

	/** Starts a complete detected warning and its sound. */
	public static void startDetected() {
		detectedFrame = 0;
		detectedActive = true;
		if (ClientScanConfig.get().enableScanSounds()) {
			MinecraftClient.getInstance().getSoundManager()
				.play(PositionedSoundInstance.master(ModSounds.DETECTED, 1.0F));
		}
	}

	/** Advances at the client tick rate, matching the exported 20 FPS sequences. */
	public static void tick() {
		if (scanningTicksRemaining > 0) {
			scanningTicksRemaining--;
			scanningFrame++;
		}
		if (detectedActive) {
			detectedFrame++;
			if (detectedFrame >= ScanAnimationResources.INSTANCE.detectedFrames().size()) {
				detectedActive = false;
			}
		}
	}

	/** Returns the highest-priority enabled frame, or null when nothing should render. */
	public static ActiveFrame currentFrame() {
		ClientScanConfig.Values config = ClientScanConfig.get();
		List<Identifier> detected = ScanAnimationResources.INSTANCE.detectedFrames();
		if (detectedActive && config.enableDetectedHudAnimation() && !detected.isEmpty()) {
			return new ActiveFrame(detected.get(Math.min(detectedFrame, detected.size() - 1)), true);
		}

		List<Identifier> scanning = ScanAnimationResources.INSTANCE.scanningFrames();
		if (scanningTicksRemaining > 0 && config.enableScanHudAnimation() && !scanning.isEmpty()) {
			return new ActiveFrame(scanning.get(Math.min(scanningFrame, scanning.size() - 1)), false);
		}
		return null;
	}

	/** Texture plus placement selector for the renderer. */
	public record ActiveFrame(Identifier texture, boolean detected) {
	}
}
