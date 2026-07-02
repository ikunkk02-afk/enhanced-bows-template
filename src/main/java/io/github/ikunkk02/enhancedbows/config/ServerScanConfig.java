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
	private static final int CURRENT_CONFIG_VERSION = 7;
	private static final int SCAN_DEFAULTS_MIGRATION_VERSION = 3;
	private static final int ARROW_RAIN_DEFAULTS_MIGRATION_VERSION = 7;
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
				JsonObject json = GSON.toJsonTree(current).getAsJsonObject();
				json.addProperty("configVersion", CURRENT_CONFIG_VERSION);
				GSON.toJson(json, writer);
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
		boolean legacyScanDefaults = getInt(json, "configVersion", 1) < SCAN_DEFAULTS_MIGRATION_VERSION;
		boolean legacyArrowRainDefaults = getInt(json, "configVersion", 1)
			< ARROW_RAIN_DEFAULTS_MIGRATION_VERSION;
		double radius = getDouble(json, "scanRadius", defaults.scanRadius());
		double arrowRainRadius = getDouble(json, "arrowRainRadius", defaults.arrowRainRadius());
		int interval = getInt(json, "scanIntervalTicks", defaults.scanIntervalTicks());
		double threshold = getDouble(json, "upwardVelocityThreshold", defaults.upwardVelocityThreshold());
		if (legacyScanDefaults) {
			radius = radius == 10.0 ? defaults.scanRadius() : radius;
			interval = interval == 5 ? defaults.scanIntervalTicks() : interval;
			threshold = threshold == 0.35 ? defaults.upwardVelocityThreshold() : threshold;
		}
		if (legacyArrowRainDefaults && arrowRainRadius == 6.0) {
			arrowRainRadius = defaults.arrowRainRadius();
		}
		return new Values(
			getBoolean(json, "enableSpectralArrowScan", defaults.enableSpectralArrowScan()),
			radius,
			getInt(json, "scanDurationTicks", defaults.scanDurationTicks()),
			interval,
			getInt(json, "glowingDurationTicks", defaults.glowingDurationTicks()),
			getBoolean(json, "scanPlayers", defaults.scanPlayers()),
			getBoolean(json, "scanOwner", defaults.scanOwner()),
			threshold,
			getBoolean(json, "enableScanningArrowBounce", defaults.enableScanningArrowBounce()),
			getInt(json, "scanningArrowMaxBounces", defaults.scanningArrowMaxBounces()),
			getDouble(json, "scanningArrowBounceDamping", defaults.scanningArrowBounceDamping()),
			getDouble(json, "scanningArrowMinBounceVelocity", defaults.scanningArrowMinBounceVelocity()),
			getBoolean(json, "scanRequiresLineOfSight", defaults.scanRequiresLineOfSight()),
			getBoolean(json, "strictScanLineOfSight", defaults.strictScanLineOfSight()),
			getBoolean(json, "enableLightningEnchantment", defaults.enableLightningEnchantment()),
			getInt(json, "lightningMaxCharges", defaults.lightningMaxCharges()),
			getInt(json, "lightningRechargeTicks", defaults.lightningRechargeTicks()),
			getString(json, "lightningDamageMode", defaults.lightningDamageMode()),
			getBoolean(json, "lightningKeepChargeAfterDeath", defaults.lightningKeepChargeAfterDeath()),
			getBoolean(json, "lightningAllowCreativeInfinite", defaults.lightningAllowCreativeInfinite()),
			getDouble(json, "lightningChainRadius", defaults.lightningChainRadius()),
			getInt(json, "lightningMaxChainTargets", defaults.lightningMaxChainTargets()),
			getDouble(json, "lightningBonusDamage", defaults.lightningBonusDamage()),
			getDouble(json, "lightningChainBonusDamage", defaults.lightningChainBonusDamage()),
			getInt(json, "lightningStormDurationTicks", defaults.lightningStormDurationTicks()),
			getInt(json, "lightningStormScanIntervalTicks", defaults.lightningStormScanIntervalTicks()),
			getDouble(json, "lightningStormRadius", defaults.lightningStormRadius()),
			getBoolean(json, "lightningStrikeOwner", defaults.lightningStrikeOwner()),
			getBoolean(json, "lightningStrikePlayers", defaults.lightningStrikePlayers()),
			getInt(json, "lightningSetFireSeconds", defaults.lightningSetFireSeconds()),
			getBoolean(json, "lightningAllowRepeatStrikeInSameStorm",
				defaults.lightningAllowRepeatStrikeInSameStorm()),
			getBoolean(json, "enableBurstEnchantment", defaults.enableBurstEnchantment()),
			getDouble(json, "burstExplosionPower", defaults.burstExplosionPower()),
			getBoolean(json, "burstBreakBlocks", defaults.burstBreakBlocks()),
			getBoolean(json, "burstCreateFire", defaults.burstCreateFire()),
			getBoolean(json, "burstDamageOwner", defaults.burstDamageOwner()),
			getBoolean(json, "burstAffectPlayers", defaults.burstAffectPlayers()),
			getBoolean(json, "burstExcludeSpectralArrows", defaults.burstExcludeSpectralArrows()),
			getDouble(json, "burstKnockbackMultiplier", defaults.burstKnockbackMultiplier()),
			getDouble(json, "burstBaseDamage", defaults.burstBaseDamage()),
			getDouble(json, "burstMinDamage", defaults.burstMinDamage()),
			getBoolean(json, "enableArrowRainEnchantment", defaults.enableArrowRainEnchantment()),
			arrowRainRadius,
			getInt(json, "arrowRainArrowCount", defaults.arrowRainArrowCount()),
			getDouble(json, "arrowRainHeight", defaults.arrowRainHeight()),
			getInt(json, "arrowRainDurationTicks", defaults.arrowRainDurationTicks()),
			getInt(json, "arrowRainWaves", defaults.arrowRainWaves()),
			getInt(json, "arrowRainCooldownTicks", defaults.arrowRainCooldownTicks()),
			getDouble(json, "arrowRainDamageMultiplier", defaults.arrowRainDamageMultiplier()),
			getBoolean(json, "arrowRainTriggerOnBlockHit", defaults.arrowRainTriggerOnBlockHit()),
			getBoolean(json, "arrowRainTriggerOnEntityHit", defaults.arrowRainTriggerOnEntityHit()),
			getBoolean(json, "arrowRainTriggerOnMiss", defaults.arrowRainTriggerOnMiss()),
			getBoolean(json, "arrowRainAllowSpectralArrow", defaults.arrowRainAllowSpectralArrow())
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

	private static String getString(JsonObject json, String key, String fallback) {
		try {
			return json.has(key) ? json.get(key).getAsString() : fallback;
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
		boolean scanRequiresLineOfSight,
		boolean strictScanLineOfSight,
		boolean enableLightningEnchantment,
		int lightningMaxCharges,
		int lightningRechargeTicks,
		String lightningDamageMode,
		boolean lightningKeepChargeAfterDeath,
		boolean lightningAllowCreativeInfinite,
		double lightningChainRadius,
		int lightningMaxChainTargets,
		double lightningBonusDamage,
		double lightningChainBonusDamage,
		int lightningStormDurationTicks,
		int lightningStormScanIntervalTicks,
		double lightningStormRadius,
		boolean lightningStrikeOwner,
		boolean lightningStrikePlayers,
		int lightningSetFireSeconds,
		boolean lightningAllowRepeatStrikeInSameStorm,
		boolean enableBurstEnchantment,
		double burstExplosionPower,
		boolean burstBreakBlocks,
		boolean burstCreateFire,
		boolean burstDamageOwner,
		boolean burstAffectPlayers,
		boolean burstExcludeSpectralArrows,
		double burstKnockbackMultiplier,
		double burstBaseDamage,
		double burstMinDamage,
		boolean enableArrowRainEnchantment,
		double arrowRainRadius,
		int arrowRainArrowCount,
		double arrowRainHeight,
		int arrowRainDurationTicks,
		int arrowRainWaves,
		int arrowRainCooldownTicks,
		double arrowRainDamageMultiplier,
		boolean arrowRainTriggerOnBlockHit,
		boolean arrowRainTriggerOnEntityHit,
		boolean arrowRainTriggerOnMiss,
		boolean arrowRainAllowSpectralArrow
	) {
		/** Source-compatible constructor retained while existing callers omit Arrow Rain settings. */
		public Values(boolean enableSpectralArrowScan, double scanRadius, int scanDurationTicks,
				int scanIntervalTicks, int glowingDurationTicks, boolean scanPlayers, boolean scanOwner,
				double upwardVelocityThreshold, boolean enableScanningArrowBounce,
				int scanningArrowMaxBounces, double scanningArrowBounceDamping,
				double scanningArrowMinBounceVelocity, boolean scanRequiresLineOfSight,
				boolean strictScanLineOfSight, boolean enableLightningEnchantment,
				int lightningMaxCharges, int lightningRechargeTicks, String lightningDamageMode,
				boolean lightningKeepChargeAfterDeath, boolean lightningAllowCreativeInfinite,
				double lightningChainRadius, int lightningMaxChainTargets, double lightningBonusDamage,
				double lightningChainBonusDamage, int lightningStormDurationTicks,
				int lightningStormScanIntervalTicks, double lightningStormRadius,
				boolean lightningStrikeOwner, boolean lightningStrikePlayers,
				int lightningSetFireSeconds, boolean lightningAllowRepeatStrikeInSameStorm,
				boolean enableBurstEnchantment, double burstExplosionPower,
				boolean burstBreakBlocks, boolean burstCreateFire, boolean burstDamageOwner,
				boolean burstAffectPlayers, boolean burstExcludeSpectralArrows,
				double burstKnockbackMultiplier, double burstBaseDamage, double burstMinDamage) {
			this(enableSpectralArrowScan, scanRadius, scanDurationTicks, scanIntervalTicks,
				glowingDurationTicks, scanPlayers, scanOwner, upwardVelocityThreshold,
				enableScanningArrowBounce, scanningArrowMaxBounces, scanningArrowBounceDamping,
				scanningArrowMinBounceVelocity, scanRequiresLineOfSight, strictScanLineOfSight,
				enableLightningEnchantment, lightningMaxCharges, lightningRechargeTicks,
				lightningDamageMode, lightningKeepChargeAfterDeath, lightningAllowCreativeInfinite,
				lightningChainRadius, lightningMaxChainTargets, lightningBonusDamage,
				lightningChainBonusDamage, lightningStormDurationTicks,
				lightningStormScanIntervalTicks, lightningStormRadius, lightningStrikeOwner,
				lightningStrikePlayers, lightningSetFireSeconds,
				lightningAllowRepeatStrikeInSameStorm, enableBurstEnchantment, burstExplosionPower,
				burstBreakBlocks, burstCreateFire, burstDamageOwner, burstAffectPlayers,
				burstExcludeSpectralArrows, burstKnockbackMultiplier, burstBaseDamage, burstMinDamage,
				true, 8.0, 24, 14.0, 40, 4, 200, 0.7, true, true, false, false);
		}

		/** Source-compatible constructor retained while older callers omit Burst settings. */
		public Values(boolean enableSpectralArrowScan, double scanRadius, int scanDurationTicks,
				int scanIntervalTicks, int glowingDurationTicks, boolean scanPlayers, boolean scanOwner,
				double upwardVelocityThreshold, boolean enableScanningArrowBounce,
				int scanningArrowMaxBounces, double scanningArrowBounceDamping,
				double scanningArrowMinBounceVelocity, boolean scanRequiresLineOfSight,
				boolean strictScanLineOfSight, boolean enableLightningEnchantment,
				int lightningMaxCharges, int lightningRechargeTicks, String lightningDamageMode,
				boolean lightningKeepChargeAfterDeath, boolean lightningAllowCreativeInfinite,
				double lightningChainRadius, int lightningMaxChainTargets, double lightningBonusDamage,
				double lightningChainBonusDamage, int lightningStormDurationTicks,
				int lightningStormScanIntervalTicks, double lightningStormRadius,
				boolean lightningStrikeOwner, boolean lightningStrikePlayers,
				int lightningSetFireSeconds, boolean lightningAllowRepeatStrikeInSameStorm) {
			this(enableSpectralArrowScan, scanRadius, scanDurationTicks, scanIntervalTicks,
				glowingDurationTicks, scanPlayers, scanOwner, upwardVelocityThreshold,
				enableScanningArrowBounce, scanningArrowMaxBounces, scanningArrowBounceDamping,
				scanningArrowMinBounceVelocity, scanRequiresLineOfSight, strictScanLineOfSight,
				enableLightningEnchantment, lightningMaxCharges, lightningRechargeTicks,
				lightningDamageMode, lightningKeepChargeAfterDeath, lightningAllowCreativeInfinite,
				lightningChainRadius, lightningMaxChainTargets, lightningBonusDamage,
				lightningChainBonusDamage, lightningStormDurationTicks,
				lightningStormScanIntervalTicks, lightningStormRadius, lightningStrikeOwner,
				lightningStrikePlayers, lightningSetFireSeconds,
				lightningAllowRepeatStrikeInSameStorm,
				true, 2.5, false, false, false, true, true, 1.0, 8.0, 2.0,
				true, 8.0, 24, 14.0, 40, 4, 200, 0.7, true, true, false, false);
		}

		/** Source-compatible constructor retained for existing config-screen drafts. */
		public Values(boolean enableSpectralArrowScan, double scanRadius, int scanDurationTicks,
				int scanIntervalTicks, int glowingDurationTicks, boolean scanPlayers, boolean scanOwner,
				double upwardVelocityThreshold, boolean enableScanningArrowBounce,
				int scanningArrowMaxBounces, double scanningArrowBounceDamping,
				double scanningArrowMinBounceVelocity, boolean scanRequiresLineOfSight,
				boolean strictScanLineOfSight, boolean enableLightningEnchantment,
				int lightningMaxCharges, int lightningRechargeTicks, String lightningDamageMode,
				boolean lightningKeepChargeAfterDeath, boolean lightningAllowCreativeInfinite) {
			this(enableSpectralArrowScan, scanRadius, scanDurationTicks, scanIntervalTicks,
				glowingDurationTicks, scanPlayers, scanOwner, upwardVelocityThreshold,
				enableScanningArrowBounce, scanningArrowMaxBounces, scanningArrowBounceDamping,
				scanningArrowMinBounceVelocity, scanRequiresLineOfSight, strictScanLineOfSight,
				enableLightningEnchantment, lightningMaxCharges, lightningRechargeTicks,
				lightningDamageMode, lightningKeepChargeAfterDeath, lightningAllowCreativeInfinite,
				8.0, 6, 10.0, 8.0, 100, 10, 8.0, false, true, 3, false,
				true, 2.5, false, false, false, true, true, 1.0, 8.0, 2.0,
				true, 8.0, 24, 14.0, 40, 4, 200, 0.7, true, true, false, false);
		}

		/** Returns the feature defaults requested by the mod specification. */
		public static Values defaults() {
			return new Values(true, 80.0, 100, 2, 200, true, false, -999.0,
				true, 3, 0.75, 0.15, true, true,
				true, 2, 400, "vanilla_lightning", true, false,
				8.0, 6, 10.0, 8.0, 100, 10, 8.0, false, true, 3, false,
				true, 2.5, false, false, false, true, true, 1.0, 8.0, 2.0,
				true, 8.0, 24, 14.0, 40, 4, 200, 0.7, true, true, false, false);
		}

		/** Enforces runtime-safe bounds while preserving valid administrator values. */
		public Values sanitized() {
			double radius = Double.isFinite(scanRadius) && scanRadius > 0.0 ? Math.min(scanRadius, 80.0) : 80.0;
			int duration = Math.max(1, Math.min(scanDurationTicks, 100));
			int interval = Math.max(1, scanIntervalTicks);
			int glowing = Math.max(1, glowingDurationTicks);
			double threshold = Double.isFinite(upwardVelocityThreshold) ? upwardVelocityThreshold : -999.0;
			int maxBounces = Math.max(0, Math.min(scanningArrowMaxBounces, 16));
			double damping = Double.isFinite(scanningArrowBounceDamping)
				? Math.max(0.0, Math.min(scanningArrowBounceDamping, 1.0))
				: 0.75;
			double minBounceVelocity = Double.isFinite(scanningArrowMinBounceVelocity)
				? Math.max(0.0, scanningArrowMinBounceVelocity)
				: 0.15;
			int maxCharges = Math.max(1, Math.min(lightningMaxCharges, 20));
			int rechargeTicks = Math.max(1, lightningRechargeTicks);
			String damageMode = "vanilla_lightning".equals(lightningDamageMode)
				? lightningDamageMode
				: "vanilla_lightning";
			double chainRadius = sanitizeRadius(lightningChainRadius, 8.0);
			int maxChainTargets = Math.max(0, Math.min(lightningMaxChainTargets, 64));
			double bonusDamage = sanitizeFinite(lightningBonusDamage, 10.0, 0.0, 2048.0);
			double chainBonusDamage = sanitizeFinite(lightningChainBonusDamage, 8.0, 0.0, 2048.0);
			int stormDuration = Math.max(1, Math.min(lightningStormDurationTicks, 12000));
			int stormInterval = Math.max(1, Math.min(lightningStormScanIntervalTicks, 1200));
			double stormRadius = sanitizeRadius(lightningStormRadius, 8.0);
			int fireSeconds = Math.max(0, Math.min(lightningSetFireSeconds, 300));
			double burstPower = sanitizeFinite(burstExplosionPower, 2.5, 0.1, 16.0);
			double burstKnockback = sanitizeFinite(burstKnockbackMultiplier, 1.0, 0.0, 10.0);
			double burstBase = sanitizeFinite(burstBaseDamage, 8.0, 0.0, 2048.0);
			double burstMinimum = Math.min(burstBase,
				sanitizeFinite(burstMinDamage, 2.0, 0.0, 2048.0));
			double rainRadius = sanitizeFinite(arrowRainRadius, 8.0, 0.0, 32.0);
			int rainCount = Math.max(0, Math.min(arrowRainArrowCount, 256));
			double rainHeight = sanitizeFinite(arrowRainHeight, 14.0, 0.0, 64.0);
			int rainDuration = Math.max(1, Math.min(arrowRainDurationTicks, 1200));
			int rainWaves = rainCount == 0 ? 0 : Math.max(1,
				Math.min(Math.min(Math.min(arrowRainWaves, 64), rainCount), rainDuration));
			int rainCooldown = Math.max(0, Math.min(arrowRainCooldownTicks, 72000));
			double rainDamageMultiplier = sanitizeFinite(arrowRainDamageMultiplier, 0.7, 0.0, 10.0);
			return new Values(enableSpectralArrowScan, radius, duration, interval, glowing,
				scanPlayers, scanOwner, threshold, enableScanningArrowBounce, maxBounces,
				damping, minBounceVelocity, scanRequiresLineOfSight, strictScanLineOfSight,
				enableLightningEnchantment, maxCharges, rechargeTicks, damageMode,
				lightningKeepChargeAfterDeath, lightningAllowCreativeInfinite,
				chainRadius, maxChainTargets, bonusDamage, chainBonusDamage, stormDuration,
				stormInterval, stormRadius, lightningStrikeOwner, lightningStrikePlayers,
				fireSeconds, lightningAllowRepeatStrikeInSameStorm,
				enableBurstEnchantment, burstPower, burstBreakBlocks, burstCreateFire,
				burstDamageOwner, burstAffectPlayers, true, burstKnockback, burstBase, burstMinimum,
				enableArrowRainEnchantment, rainRadius, rainCount, rainHeight, rainDuration,
				rainWaves, rainCooldown, rainDamageMultiplier, arrowRainTriggerOnBlockHit,
				arrowRainTriggerOnEntityHit, arrowRainTriggerOnMiss, arrowRainAllowSpectralArrow);
		}

		private static double sanitizeFinite(double value, double fallback, double min, double max) {
			return Double.isFinite(value) ? Math.max(min, Math.min(value, max)) : fallback;
		}

		private static double sanitizeRadius(double value, double fallback) {
			return Double.isFinite(value) && value > 0.0 ? Math.min(value, 64.0) : fallback;
		}
	}
}
