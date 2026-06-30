package io.github.ikunkk02.enhancedbows.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

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
	}

	@Test
	void sanitizationEnforcesSafeRuntimeBounds() {
		ServerScanConfig.Values values = new ServerScanConfig.Values(
			true, -5.0, 500, 0, 0, true, false, -1.0,
			true, 99, 5.0, -1.0, true, true,
			true, 0, 0, "unknown", true, false
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
}
