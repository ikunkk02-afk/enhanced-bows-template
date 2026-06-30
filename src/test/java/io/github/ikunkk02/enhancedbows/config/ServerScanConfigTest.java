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
		assertEquals(10.0, values.scanRadius());
		assertEquals(100, values.scanDurationTicks());
		assertEquals(5, values.scanIntervalTicks());
		assertEquals(200, values.glowingDurationTicks());
		assertTrue(values.scanPlayers());
		assertFalse(values.scanOwner());
		assertEquals(0.35, values.upwardVelocityThreshold());
		assertTrue(values.enableScanningArrowBounce());
		assertEquals(3, values.scanningArrowMaxBounces());
		assertEquals(0.75, values.scanningArrowBounceDamping());
		assertEquals(0.15, values.scanningArrowMinBounceVelocity());
		assertTrue(values.scanRequiresLineOfSight());
	}

	@Test
	void sanitizationEnforcesSafeRuntimeBounds() {
		ServerScanConfig.Values values = new ServerScanConfig.Values(
			true, -5.0, 500, 0, 0, true, false, -1.0,
			true, 99, 5.0, -1.0, true
		).sanitized();

		assertEquals(10.0, values.scanRadius());
		assertEquals(100, values.scanDurationTicks());
		assertEquals(1, values.scanIntervalTicks());
		assertEquals(1, values.glowingDurationTicks());
		assertEquals(0.0, values.upwardVelocityThreshold());
		assertEquals(16, values.scanningArrowMaxBounces());
		assertEquals(1.0, values.scanningArrowBounceDamping());
		assertEquals(0.0, values.scanningArrowMinBounceVelocity());
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
	}
}
