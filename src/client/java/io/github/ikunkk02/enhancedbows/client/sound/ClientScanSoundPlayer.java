package io.github.ikunkk02.enhancedbows.client.sound;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.network.ScanSoundCue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

/** Resolves optional local-resource-pack sounds and safely falls back to vanilla. */
public final class ClientScanSoundPlayer {
	private static final Identifier CUSTOM_SCAN_START = Identifier.of("enhanced_bows", "custom.scan_start");
	private static final Identifier CUSTOM_DETECTED = Identifier.of("enhanced_bows", "custom.detected");
	private static final Identifier CUSTOM_BOUNCE = Identifier.of("enhanced_bows", "custom.bounce");

	private ClientScanSoundPlayer() {
	}

	public static void play(ScanSoundCue cue) {
		ClientScanConfig.Values config = ClientScanConfig.get();
		if (!config.enableScanSounds() || cue == null) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		SoundManager manager = client.getSoundManager();
		Identifier customId = customId(cue);
		WeightedSoundSet customSounds = customId == null ? null : manager.get(customId);
		if (customId != null && useCustom(config, cue) && customSounds != null && customSounds.getWeight() > 0) {
			try {
				manager.play(PositionedSoundInstance.master(SoundEvent.of(customId), 1.0F));
				return;
			} catch (RuntimeException exception) {
				EnhancedBows.LOGGER.warn("Could not play custom scan sound {}; using vanilla", customId, exception);
			}
		}

		manager.play(PositionedSoundInstance.master(vanilla(cue), 1.0F));
	}

	private static boolean useCustom(ClientScanConfig.Values config, ScanSoundCue cue) {
		return switch (cue) {
			case SCAN_START -> config.useCustomScanStartSound();
			case DETECTED -> config.useCustomDetectedSound();
			case BOUNCE -> config.useCustomBounceSound();
			case MARK_SUCCESS -> false;
		};
	}

	private static Identifier customId(ScanSoundCue cue) {
		return switch (cue) {
			case SCAN_START -> CUSTOM_SCAN_START;
			case DETECTED -> CUSTOM_DETECTED;
			case BOUNCE -> CUSTOM_BOUNCE;
			case MARK_SUCCESS -> null;
		};
	}

	private static SoundEvent vanilla(ScanSoundCue cue) {
		return switch (cue) {
			case SCAN_START -> SoundEvents.BLOCK_BEACON_ACTIVATE;
			case DETECTED -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
			case BOUNCE -> SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
			case MARK_SUCCESS -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
		};
	}
}
