package io.github.ikunkk02.enhancedbows.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persists visual and audio preferences that are meaningful only on a client. */
public final class ClientScanConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("enhanced-bows-client.json");
	private static volatile Values current = Values.defaults();

	private ClientScanConfig() {
	}

	/** Loads client preferences without allowing malformed files to break startup. */
	public static void load() {
		Values loaded = Values.defaults();
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				JsonObject json = GSON.fromJson(reader, JsonObject.class);
				if (json != null) {
					loaded = fromJson(json);
				}
			} catch (Exception exception) {
				EnhancedBows.LOGGER.warn("Could not read {}; using defaults", PATH, exception);
			}
		}
		current = loaded;
		save();
	}

	public static Values get() {
		return current;
	}

	/** Applies changes immediately and writes them for the next client launch. */
	public static void setAndSave(Values values) {
		current = values.sanitized();
		save();
	}

	private static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException exception) {
			EnhancedBows.LOGGER.warn("Could not write {}", PATH, exception);
		}
	}

	/** Preserves existing client choices while defaulting newly added detected placement fields. */
	private static Values fromJson(JsonObject json) {
		Values defaults = Values.defaults();
		return new Values(
			getBoolean(json, "enableScanHudAnimation", defaults.enableScanHudAnimation()),
			getBoolean(json, "enableDetectedHudAnimation", defaults.enableDetectedHudAnimation()),
			getInt(json, "scanHudX", defaults.scanHudX()),
			getInt(json, "scanHudY", defaults.scanHudY()),
			getDouble(json, "scanHudScale", defaults.scanHudScale()),
			getInt(json, "detectedHudX", defaults.detectedHudX()),
			getInt(json, "detectedHudY", defaults.detectedHudY()),
			getDouble(json, "detectedHudScale", defaults.detectedHudScale()),
			getBoolean(json, "enableScanSounds", defaults.enableScanSounds())
		).sanitized();
	}

	private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
		try {
			return json.has(key) ? json.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static int getInt(JsonObject json, String key, int fallback) {
		try {
			return json.has(key) ? json.get(key).getAsInt() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	private static double getDouble(JsonObject json, String key, double fallback) {
		try {
			return json.has(key) ? json.get(key).getAsDouble() : fallback;
		} catch (RuntimeException exception) {
			return fallback;
		}
	}

	/** Immutable client settings shared by the renderer, editor, and config screen. */
	public record Values(
		boolean enableScanHudAnimation,
		boolean enableDetectedHudAnimation,
		int scanHudX,
		int scanHudY,
		double scanHudScale,
		int detectedHudX,
		int detectedHudY,
		double detectedHudScale,
		boolean enableScanSounds
	) {
		public static Values defaults() {
			return new Values(true, true, -1, 40, 1.0, -1, 40, 1.0, true);
		}

		public Values sanitized() {
			double scanningScale = Double.isFinite(scanHudScale)
				? Math.max(0.25, Math.min(scanHudScale, 3.0))
				: 1.0;
			double detectedScale = Double.isFinite(detectedHudScale)
				? Math.max(0.3, Math.min(detectedHudScale, 3.0))
				: 1.0;
			return new Values(enableScanHudAnimation, enableDetectedHudAnimation,
				scanHudX, scanHudY, Math.max(0.3, scanningScale),
				detectedHudX, detectedHudY, detectedScale, enableScanSounds);
		}
	}
}
