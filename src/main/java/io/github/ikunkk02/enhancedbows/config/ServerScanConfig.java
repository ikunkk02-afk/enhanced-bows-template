package io.github.ikunkk02.enhancedbows.config;

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

/** Loads and atomically publishes the server-authoritative spectral-arrow settings. */
public final class ServerScanConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile Values current = Values.defaults();

	private ServerScanConfig() {
	}

	/** Loads the configuration, falling back to safe defaults when the file is malformed. */
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

	/** Returns the immutable snapshot read by server ticks. */
	public static Values get() {
		return current;
	}

	/** Replaces the local snapshot and persists it for integrated or future servers. */
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
		return FabricLoader.getInstance().getConfigDir().resolve("enhanced-bows-server.json");
	}

	/** Merges a JSON object over defaults so older config files gain new options safely. */
	static Values fromJson(JsonObject json) {
		Values defaults = Values.defaults();
		return new Values(
			getBoolean(json, "enableSpectralArrowScan", defaults.enableSpectralArrowScan()),
			getDouble(json, "scanRadius", defaults.scanRadius()),
			getInt(json, "scanDurationTicks", defaults.scanDurationTicks()),
			getInt(json, "scanIntervalTicks", defaults.scanIntervalTicks()),
			getInt(json, "glowingDurationTicks", defaults.glowingDurationTicks()),
			getBoolean(json, "scanPlayers", defaults.scanPlayers()),
			getBoolean(json, "scanOwner", defaults.scanOwner()),
			getDouble(json, "upwardVelocityThreshold", defaults.upwardVelocityThreshold()),
			getBoolean(json, "enableScanningArrowBounce", defaults.enableScanningArrowBounce()),
			getInt(json, "scanningArrowMaxBounces", defaults.scanningArrowMaxBounces()),
			getDouble(json, "scanningArrowBounceDamping", defaults.scanningArrowBounceDamping()),
			getDouble(json, "scanningArrowMinBounceVelocity", defaults.scanningArrowMinBounceVelocity()),
			getBoolean(json, "scanRequiresLineOfSight", defaults.scanRequiresLineOfSight())
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

	/** Immutable wire-independent server settings. */
	public record Values(
		boolean enableSpectralArrowScan,
		double scanRadius,
		int scanDurationTicks,
		int scanIntervalTicks,
		int glowingDurationTicks,
		boolean scanPlayers,
		boolean scanOwner,
		double upwardVelocityThreshold,
		boolean enableScanningArrowBounce,
		int scanningArrowMaxBounces,
		double scanningArrowBounceDamping,
		double scanningArrowMinBounceVelocity,
		boolean scanRequiresLineOfSight
	) {
		/** Returns the feature defaults requested by the mod specification. */
		public static Values defaults() {
			return new Values(true, 10.0, 100, 5, 200, true, false, 0.35,
				true, 3, 0.75, 0.15, true);
		}

		/** Enforces runtime-safe bounds while preserving valid administrator values. */
		public Values sanitized() {
			double radius = Double.isFinite(scanRadius) && scanRadius > 0.0 ? Math.min(scanRadius, 64.0) : 10.0;
			int duration = Math.max(1, Math.min(scanDurationTicks, 100));
			int interval = Math.max(1, scanIntervalTicks);
			int glowing = Math.max(1, glowingDurationTicks);
			double threshold = Double.isFinite(upwardVelocityThreshold)
				? Math.max(0.0, upwardVelocityThreshold)
				: 0.35;
			int maxBounces = Math.max(0, Math.min(scanningArrowMaxBounces, 16));
			double damping = Double.isFinite(scanningArrowBounceDamping)
				? Math.max(0.0, Math.min(scanningArrowBounceDamping, 1.0))
				: 0.75;
			double minBounceVelocity = Double.isFinite(scanningArrowMinBounceVelocity)
				? Math.max(0.0, scanningArrowMinBounceVelocity)
				: 0.15;
			return new Values(enableSpectralArrowScan, radius, duration, interval, glowing,
				scanPlayers, scanOwner, threshold, enableScanningArrowBounce, maxBounces,
				damping, minBounceVelocity, scanRequiresLineOfSight);
		}
	}
}
