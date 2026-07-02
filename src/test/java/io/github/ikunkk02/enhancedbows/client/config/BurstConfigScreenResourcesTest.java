package io.github.ikunkk02.enhancedbows.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurstConfigScreenResourcesTest {
	private static final Path CLIENT = Path.of("src/client");
	private static final List<String> KEYS = List.of(
		"enchantment.enhanced_bows.burst",
		"config.enhanced-bows.category.burst",
		"config.enhanced-bows.enable_burst_enchantment",
		"config.enhanced-bows.burst_explosion_power",
		"config.enhanced-bows.burst_break_blocks",
		"config.enhanced-bows.burst_create_fire",
		"config.enhanced-bows.burst_damage_owner",
		"config.enhanced-bows.burst_affect_players",
		"config.enhanced-bows.burst_exclude_spectral_arrows",
		"config.enhanced-bows.burst_knockback_multiplier",
		"config.enhanced-bows.burst_base_damage",
		"config.enhanced-bows.burst_min_damage"
	);

	@Test
	void bothLocalesDescribeEveryBurstOption() throws IOException {
		for (String locale : List.of("zh_cn.json", "en_us.json")) {
			JsonObject json = JsonParser.parseString(Files.readString(CLIENT.resolve(
				"resources/assets/enhanced-bows/lang/" + locale))).getAsJsonObject();
			for (String key : KEYS) {
				assertTrue(json.has(key), locale + " missing " + key);
			}
		}
	}

	@Test
	void clothScreenExposesBurstSettingsButSpectralExclusionIsReadOnly() throws IOException {
		String source = Files.readString(CLIENT.resolve(
			"java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java"));
		for (String accessor : List.of("enableBurstEnchantment", "burstExplosionPower",
			"burstBreakBlocks", "burstCreateFire", "burstDamageOwner", "burstAffectPlayers",
			"burstExcludeSpectralArrows", "burstKnockbackMultiplier", "burstBaseDamage",
			"burstMinDamage")) {
			assertTrue(source.contains(accessor), "missing Burst draft accessor " + accessor);
		}
		assertTrue(source.contains("startTextDescription(Text.translatable(\"config.enhanced-bows.burst_exclude_spectral_arrows\"))"));
		assertFalse(source.contains("setSaveConsumer(value -> server.burstExcludeSpectralArrows = value)"));
	}
}
