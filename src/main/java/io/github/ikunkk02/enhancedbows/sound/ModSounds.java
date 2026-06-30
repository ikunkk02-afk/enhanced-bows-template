package io.github.ikunkk02.enhancedbows.sound;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

/** Common sound registry; playback remains entirely client-side. */
public final class ModSounds {
	public static final SoundEvent SCANNING_START = register("scanning_start");
	public static final SoundEvent SCANNING_LOOP = register("scanning_loop");
	public static final SoundEvent DETECTED = register("detected");

	private ModSounds() {
	}

	private static SoundEvent register(String path) {
		return Registry.register(Registries.SOUND_EVENT, EnhancedBows.id(path),
			SoundEvent.of(EnhancedBows.id(path)));
	}

	/** Forces class initialization from the common mod entrypoint. */
	public static void register() {
	}
}
