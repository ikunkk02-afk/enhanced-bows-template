package io.github.ikunkk02.enhancedbows.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerScanConfigTest {
	@Test
	void defaultsMatchTheFeatureContract() {
		ServerScanConfig.Values values = ServerScanConfig.Values.defaults();

		assertTrue(values.enableSpectralArrowScan());
		assertEquals(80.0, values.scanRadius());
		assertEquals(100, values.scanDurationTicks());
		assertEquals(2, values.scanIntervalTicks());
		assertEquals(200, values.glowingDurationTicks());
		assertTrue(values.scanPlayers());
		assertFalse(values.scanOwner());
		assertEquals(-999.0, values.upwardVelocityThreshold());
		assertTrue(values.enableScanningArrowBounce());
		assertEquals(3, values.scanningArrowMaxBounces());
		assertEquals(0.75, values.scanningArrowBounceDamping());
		assertEquals(0.15, values.scanningArrowMinBounceVelocity());
		assertTrue(values.scanRequiresLineOfSight());
		assertTrue(values.strictScanLineOfSight());
		assertTrue(values.enableLightningEnchantment());
		assertEquals(2, values.lightningMaxCharges());
		assertEquals(400, values.lightningRechargeTicks());
		assertEquals("vanilla_lightning", values.lightningDamageMode());
		assertTrue(values.lightningKeepChargeAfterDeath());
		assertFalse(values.lightningAllowCreativeInfinite());
		assertEquals(8.0, values.lightningChainRadius());
		assertEquals(6, values.lightningMaxChainTargets());
		assertEquals(10.0, values.lightningBonusDamage());
		assertEquals(8.0, values.lightningChainBonusDamage());
		assertEquals(100, values.lightningStormDurationTicks());
		assertEquals(10, values.lightningStormScanIntervalTicks());
		assertEquals(8.0, values.lightningStormRadius());
		assertFalse(values.lightningStrikeOwner());
		assertTrue(values.lightningStrikePlayers());
		assertEquals(3, values.lightningSetFireSeconds());
		assertFalse(values.lightningAllowRepeatStrikeInSameStorm());
		assertTrue(values.enableBurstEnchantment());
		assertEquals(2.5, values.burstExplosionPower());
		assertFalse(values.burstBreakBlocks());
		assertFalse(values.burstCreateFire());
		assertFalse(values.burstDamageOwner());
		assertTrue(values.burstAffectPlayers());
		assertTrue(values.burstExcludeSpectralArrows());
		assertEquals(1.0, values.burstKnockbackMultiplier());
		assertEquals(8.0, values.burstBaseDamage());
		assertEquals(2.0, values.burstMinDamage());
		assertTrue(values.enableArrowRainEnchantment());
		assertEquals(8.0, values.arrowRainRadius());
		assertEquals(24, values.arrowRainArrowCount());
		assertEquals(14.0, values.arrowRainHeight());
		assertEquals(40, values.arrowRainDurationTicks());
		assertEquals(4, values.arrowRainWaves());
		assertEquals(200, values.arrowRainCooldownTicks());
		assertEquals(0.7, values.arrowRainDamageMultiplier());
		assertTrue(recordBoolean(values, "arrowRainTriggerOnBlockHit"));
		assertTrue(recordBoolean(values, "arrowRainTriggerOnEntityHit"));
		assertFalse(recordBoolean(values, "arrowRainTriggerOnMiss"));
		assertFalse(values.arrowRainAllowSpectralArrow());
	}

	@Test
	void sanitizationEnforcesSafeRuntimeBounds() {
		ServerScanConfig.Values values = new ServerScanConfig.Values(
			true, -5.0, 500, 0, 0, true, false, -1.0,
			true, 99, 5.0, -1.0, true, true,
			true, 0, 0, "unknown", true, false,
			-2.0, 999, Double.NaN, -3.0, 0, 0, Double.POSITIVE_INFINITY,
			false, true, -4, false,
			true, Double.NaN, true, true, true, false, false, -2.0, Double.NaN, 999.0
		).sanitized();

		assertEquals(80.0, values.scanRadius());
		assertEquals(100, values.scanDurationTicks());
		assertEquals(1, values.scanIntervalTicks());
		assertEquals(1, values.glowingDurationTicks());
		assertEquals(-1.0, values.upwardVelocityThreshold());
		assertEquals(16, values.scanningArrowMaxBounces());
		assertEquals(1.0, values.scanningArrowBounceDamping());
		assertEquals(0.0, values.scanningArrowMinBounceVelocity());
		assertEquals(1, values.lightningMaxCharges());
		assertEquals(1, values.lightningRechargeTicks());
		assertEquals("vanilla_lightning", values.lightningDamageMode());
		assertEquals(8.0, values.lightningChainRadius());
		assertEquals(64, values.lightningMaxChainTargets());
		assertEquals(10.0, values.lightningBonusDamage());
		assertEquals(0.0, values.lightningChainBonusDamage());
		assertEquals(1, values.lightningStormDurationTicks());
		assertEquals(1, values.lightningStormScanIntervalTicks());
		assertEquals(8.0, values.lightningStormRadius());
		assertEquals(0, values.lightningSetFireSeconds());
		assertEquals(2.5, values.burstExplosionPower());
		assertTrue(values.burstBreakBlocks());
		assertTrue(values.burstCreateFire());
		assertTrue(values.burstDamageOwner());
		assertFalse(values.burstAffectPlayers());
		assertTrue(values.burstExcludeSpectralArrows());
		assertEquals(0.0, values.burstKnockbackMultiplier());
		assertEquals(8.0, values.burstBaseDamage());
		assertEquals(8.0, values.burstMinDamage());
	}

	@Test
	void legacyConfigWithoutNewFieldsReceivesNewFeatureDefaults() {
		ServerScanConfig.Values values = ServerScanConfig.fromJson(
			JsonParser.parseString("{\"scanRadius\":12.0}").getAsJsonObject()
		);

		assertEquals(12.0, values.scanRadius());
		assertTrue(values.enableScanningArrowBounce());
		assertEquals(3, values.scanningArrowMaxBounces());
		assertTrue(values.scanRequiresLineOfSight());
		assertTrue(values.strictScanLineOfSight());
		assertEquals(8.0, values.lightningChainRadius());
		assertEquals(100, values.lightningStormDurationTicks());
		assertEquals("vanilla_lightning", values.lightningDamageMode());
	}

	@Test
	void legacyGeneratedDefaultsMigrateToFiveChunkDirectionIndependentScanningDefaults() {
		ServerScanConfig.Values values = ServerScanConfig.fromJson(
			JsonParser.parseString("""
				{
				  "scanRadius": 10.0,
				  "scanIntervalTicks": 5,
				  "upwardVelocityThreshold": 0.35,
				  "strictScanLineOfSight": true
				}
				""").getAsJsonObject()
		);

		assertEquals(80.0, values.scanRadius());
		assertEquals(2, values.scanIntervalTicks());
		assertEquals(-999.0, values.upwardVelocityThreshold());
		assertTrue(values.strictScanLineOfSight());
	}

	@Test
	void versionThreeConfigKeepsExplicitScanValuesWhileGainingStormDefaults() {
		ServerScanConfig.Values values = ServerScanConfig.fromJson(
			JsonParser.parseString("""
				{
				  "configVersion": 3,
				  "scanRadius": 10.0,
				  "scanIntervalTicks": 5,
				  "upwardVelocityThreshold": 0.35
				}
				""").getAsJsonObject()
		);

		assertEquals(10.0, values.scanRadius());
		assertEquals(5, values.scanIntervalTicks());
		assertEquals(0.35, values.upwardVelocityThreshold());
		assertEquals(8.0, values.lightningStormRadius());
	}

	@Test
	void versionFourConfigKeepsStormValuesWhileGainingBurstDefaults() {
		ServerScanConfig.Values values = ServerScanConfig.fromJson(
			JsonParser.parseString("""
				{
				  "configVersion": 4,
				  "scanRadius": 12.0,
				  "lightningStormRadius": 11.0,
				  "lightningBonusDamage": 14.0,
				  "burstExcludeSpectralArrows": false
				}
				""").getAsJsonObject()
		);

		assertEquals(12.0, values.scanRadius());
		assertEquals(11.0, values.lightningStormRadius());
		assertEquals(14.0, values.lightningBonusDamage());
		assertEquals(2.5, values.burstExplosionPower());
		assertTrue(values.burstExcludeSpectralArrows());
	}

	@Test
	void arrowRainValuesGainDefaultsAndAreBoundedForServerSafety() {
		ServerScanConfig.Values defaults = ServerScanConfig.fromJson(
			JsonParser.parseString("{\"configVersion\":5}").getAsJsonObject());
		assertEquals(24, defaults.arrowRainArrowCount());
		assertEquals(4, defaults.arrowRainWaves());
		assertEquals(8.0, defaults.arrowRainRadius());
		assertTrue(recordBoolean(defaults, "arrowRainTriggerOnBlockHit"));
		assertTrue(recordBoolean(defaults, "arrowRainTriggerOnEntityHit"));
		assertFalse(recordBoolean(defaults, "arrowRainTriggerOnMiss"));

		ServerScanConfig.Values bounded = ServerScanConfig.fromJson(JsonParser.parseString("""
			{
			  "arrowRainRadius": 999.0,
			  "arrowRainArrowCount": 3,
			  "arrowRainHeight": -5.0,
			  "arrowRainDurationTicks": 0,
			  "arrowRainWaves": 99,
			  "arrowRainCooldownTicks": -1,
			  "arrowRainDamageMultiplier": -2.0
			}
			""").getAsJsonObject());
		assertEquals(32.0, bounded.arrowRainRadius());
		assertEquals(3, bounded.arrowRainArrowCount());
		assertEquals(0.0, bounded.arrowRainHeight());
		assertEquals(1, bounded.arrowRainDurationTicks());
		assertEquals(1, bounded.arrowRainWaves());
		assertEquals(0, bounded.arrowRainCooldownTicks());
		assertEquals(0.0, bounded.arrowRainDamageMultiplier());
	}

	@Test
	void versionSixGeneratedArrowRainRadiusMigratesToEightBlocks() {
		ServerScanConfig.Values migrated = ServerScanConfig.fromJson(
			JsonParser.parseString("{\"configVersion\":6,\"arrowRainRadius\":6.0}").getAsJsonObject());
		assertEquals(8.0, migrated.arrowRainRadius());

		ServerScanConfig.Values currentExplicit = ServerScanConfig.fromJson(
			JsonParser.parseString("{\"configVersion\":7,\"arrowRainRadius\":6.0}").getAsJsonObject());
		assertEquals(6.0, currentExplicit.arrowRainRadius());
	}

	private static boolean recordBoolean(ServerScanConfig.Values values, String name) {
		RecordComponent component = Arrays.stream(ServerScanConfig.Values.class.getRecordComponents())
			.filter(candidate -> candidate.getName().equals(name))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Missing config field: " + name));
		try {
			return (boolean) component.getAccessor().invoke(values);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot read config field: " + name, exception);
		}
	}
}
