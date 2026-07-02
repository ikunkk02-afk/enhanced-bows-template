package io.github.ikunkk02.enhancedbows.client.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientScanConfigTest {
	@Test
	void defaultsMatchTextHudAndCustomSoundContract() {
		ClientScanConfig.Values values = ClientScanConfig.Values.defaults();

		assertTrue(values.enableScanSounds());
		assertFalse(values.useCustomScanStartSound());
		assertFalse(values.useCustomDetectedSound());
		assertFalse(values.useCustomBounceSound());
		assertTrue(values.enableScanTextHud());
		assertEquals(40, values.scanTextHudY());
		assertTrue(values.enableScanningArrowRedTrail());
		assertEquals(12, values.redTrailLength());
		assertEquals(14, values.redTrailParticleCount());
		assertEquals(1.0, values.redTrailParticleSize());
		assertTrue(values.enableLightningHud());
		assertEquals(8, values.lightningHudX());
		assertEquals(8, values.lightningHudY());
		assertEquals(0.8, values.lightningHudScale());
		assertTrue(values.showLightningHudOnlyWhenHoldingBow());
		assertTrue(values.enableArrowRainHud());
	}

	@Test
	void legacyAnimationFieldsAreIgnoredDuringMigration() {
		ClientScanConfig.Values values = ClientScanConfig.fromJson(JsonParser.parseString("""
			{
			  "enableScanSounds": false,
			  "enableScanHudAnimation": false,
			  "enableDetectedHudAnimation": false,
			  "scanHudY": 99,
			  "scanHudScale": 2.0
			}
			""").getAsJsonObject());

		assertFalse(values.enableScanSounds());
		assertTrue(values.enableScanTextHud());
		assertEquals(40, values.scanTextHudY());
	}

	@Test
	void textHudYIsClampedToSafeBounds() {
		ClientScanConfig.Values low = ClientScanConfig.fromJson(
			JsonParser.parseString("{\"scanTextHudY\":-10}").getAsJsonObject());
		ClientScanConfig.Values high = ClientScanConfig.fromJson(
			JsonParser.parseString("{\"scanTextHudY\":20000}").getAsJsonObject());

		assertEquals(0, low.scanTextHudY());
		assertEquals(10000, high.scanTextHudY());
	}

	@Test
	void trailAndLightningHudValuesAreClamped() {
		ClientScanConfig.Values values = ClientScanConfig.fromJson(JsonParser.parseString("""
			{
			  "redTrailLength": 0,
			  "redTrailParticleCount": 1000,
			  "redTrailParticleSize": 99.0,
			  "lightningHudX": -1,
			  "lightningHudY": 20000,
			  "lightningHudScale": 0.01
			}
			""").getAsJsonObject());

		assertEquals(1, values.redTrailLength());
		assertEquals(64, values.redTrailParticleCount());
		assertEquals(4.0, values.redTrailParticleSize());
		assertEquals(0, values.lightningHudX());
		assertEquals(10000, values.lightningHudY());
		assertEquals(0.25, values.lightningHudScale());
	}
}
