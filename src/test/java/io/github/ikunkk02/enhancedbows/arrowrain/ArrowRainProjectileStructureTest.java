package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainProjectileStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");

	@Test
	void projectilePersistsIndependentArrowRainAndChildState() throws IOException {
		String access = read("arrowrain/ArrowRainArrowAccess.java");
		String mixin = read("mixin/PersistentProjectileEntityMixin.java");

		assertTrue(access.contains("enhancedBows$armArrowRain"));
		assertTrue(access.contains("enhancedBows$markArrowRainChild"));
		assertTrue(mixin.contains("implements LightningArrowAccess, BurstArrowAccess, ArrowRainArrowAccess"));
		for (String key : new String[] {
			"enhancedbows:arrow_rain_armed",
			"enhancedbows:arrow_rain_triggered",
			"enhancedbows:arrow_rain_owner_uuid",
			"enhancedbows:arrow_rain_base_damage",
			"enhancedbows:arrow_rain_child",
			"enhancedbows:arrow_rain_child_age",
			"enhancedbows:arrow_rain_child_lifetime"
		}) {
			assertTrue(mixin.contains(key), key);
		}
		assertTrue(mixin.contains("enhancedBows$arrowRainChildAge++"));
		assertTrue(mixin.contains("projectile.discard()"));
	}

	@Test
	void spawnHookArmsArrowRainIndependentlyAndRejectsIllegalBurstCombination() throws IOException {
		String ranged = read("mixin/RangedWeaponItemMixin.java");

		assertTrue(ranged.contains("ModEnchantments.getArrowRainLevel"));
		assertTrue(ranged.contains("ArrowRainComponents.ARROW_RAIN.get"));
		assertTrue(ranged.contains("ArrowRainArrowRules.shouldArm"));
		assertTrue(ranged.contains("arrowRainArrow.enhancedBows$armArrowRain"));
		assertTrue(ranged.contains("Bow has both Arrow Rain and Burst enchantments. "
			+ "This is not allowed. Arrow Rain is disabled."));
		assertTrue(ranged.indexOf("enhancedBows$armLightning()")
			< ranged.indexOf("enhancedBows$armArrowRain"));
	}

	private static String read(String relative) throws IOException {
		return Files.readString(MAIN.resolve(relative));
	}
}
