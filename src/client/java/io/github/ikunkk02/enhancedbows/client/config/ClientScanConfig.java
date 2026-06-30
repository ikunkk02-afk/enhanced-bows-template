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
	private static volatile Values current = Values.defaults();

	private ClientScanConfig() {
	}

	/** Loads client preferences without allowing malformed files to break startup. */
	public static void load() {
		Path path = path();
		Values loaded = Values.defaults();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				JsonObject json = GSON.fromJson(reader, JsonObject.class);
				if (json != null) {
					loaded = fromJson(json);
				}
			} catch (Exception exception) {
				EnhancedBows.LOGGER.warn("Could not read {}; using defaults", path, exception);
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
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException exception) {
			EnhancedBows.LOGGER.warn("Could not write {}", path, exception);
		}
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("enhanced-bows-client.json");
	}

	/** Reads only the current text-HUD and sound fields so legacy animation settings disappear on save. */
	static Values fromJson(JsonObject json) {
		Values defaults = Values.defaults();
		return new Values(
			getBoolean(json, "enableScanSounds", defaults.enableScanSounds()),
			getBoolean(json, "useCustomScanStartSound", defaults.useCustomScanStartSound()),
			getBoolean(json, "useCustomDetectedSound", defaults.useCustomDetectedSound()),
			getBoolean(json, "useCustomBounceSound", defaults.useCustomBounceSound()),
			getBoolean(json, "enableScanTextHud", defaults.enableScanTextHud()),
			getInt(json, "scanTextHudY", defaults.scanTextHudY()),
			getBoolean(json, "enableScanningArrowRedTrail", defaults.enableScanningArrowRedTrail()),
			getInt(json, "redTrailLength", defaults.redTrailLength()),
			getInt(json, "redTrailParticleCount", defaults.redTrailParticleCount()),
			getDouble(json, "redTrailParticleSize", defaults.redTrailParticleSize()),
			getBoolean(json, "enableLightningHud", defaults.enableLightningHud()),
			getInt(json, "lightningHudX", defaults.lightningHudX()),
			getInt(json, "lightningHudY", defaults.lightningHudY()),
			getDouble(json, "lightningHudScale", defaults.lightningHudScale()),
			getBoolean(json, "showLightningHudOnlyWhenHoldingBow", defaults.showLightningHudOnlyWhenHoldingBow())
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

	/** Immutable client settings shared by the text renderer, sound player, importer, and config screen. */
	public record Values(
		boolean enableScanSounds,
		boolean useCustomScanStartSound,
		boolean useCustomDetectedSound,
		boolean useCustomBounceSound,
		boolean enableScanTextHud,
		int scanTextHudY,
		boolean enableScanningArrowRedTrail,
		int redTrailLength,
		int redTrailParticleCount,
		double redTrailParticleSize,
		boolean enableLightningHud,
		int lightningHudX,
		int lightningHudY,
		double lightningHudScale,
		boolean showLightningHudOnlyWhenHoldingBow
	) {
		public static Values defaults() {
			return new Values(true, false, false, false, true, 40,
				true, 12, 14, 1.0, true, 8, 8, 0.8, true);
		}

		public Values sanitized() {
			int trailLength = Math.max(1, Math.min(redTrailLength, 64));
			int particleCount = Math.max(1, Math.min(redTrailParticleCount, 64));
			double particleSize = Double.isFinite(redTrailParticleSize)
				? Math.max(0.1, Math.min(redTrailParticleSize, 4.0))
				: 1.0;
			double hudScale = Double.isFinite(lightningHudScale)
				? Math.max(0.25, Math.min(lightningHudScale, 2.0))
				: 0.8;
			return new Values(enableScanSounds, useCustomScanStartSound, useCustomDetectedSound,
				useCustomBounceSound, enableScanTextHud, Math.max(0, Math.min(scanTextHudY, 10000)),
				enableScanningArrowRedTrail, trailLength, particleCount, particleSize,
				enableLightningHud, Math.max(0, Math.min(lightningHudX, 10000)),
				Math.max(0, Math.min(lightningHudY, 10000)), hudScale,
				showLightningHudOnlyWhenHoldingBow);
		}
	}
}
