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

class LightningConfigScreenResourcesTest {
	private static final Path CLIENT = Path.of("src/client");
	private static final List<String> KEYS = List.of(
		"config.enhanced-bows.lightning_chain_radius",
		"config.enhanced-bows.lightning_max_chain_targets",
		"config.enhanced-bows.lightning_bonus_damage",
		"config.enhanced-bows.lightning_chain_bonus_damage",
		"config.enhanced-bows.lightning_storm_duration_ticks",
		"config.enhanced-bows.lightning_storm_scan_interval_ticks",
		"config.enhanced-bows.lightning_storm_radius",
		"config.enhanced-bows.lightning_strike_owner",
		"config.enhanced-bows.lightning_strike_players",
		"config.enhanced-bows.lightning_set_fire_seconds",
		"config.enhanced-bows.lightning_allow_repeat_strike"
	);

	@Test
	void bothLocalesDescribeEveryStormOption() throws IOException {
		for (String locale : List.of("zh_cn.json", "en_us.json")) {
			JsonObject json = JsonParser.parseString(Files.readString(CLIENT.resolve(
				"resources/assets/enhanced-bows/lang/" + locale))).getAsJsonObject();
			for (String key : KEYS) {
				assertTrue(json.has(key), locale + " missing " + key);
			}
		}
	}

	@Test
	void clothScreenExposesStormOptionsAndHidesLegacyDamageMode() throws IOException {
		String source = Files.readString(CLIENT.resolve(
			"java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java"));
		for (String accessor : List.of("lightningChainRadius", "lightningMaxChainTargets",
			"lightningBonusDamage", "lightningChainBonusDamage", "lightningStormDurationTicks",
			"lightningStormScanIntervalTicks", "lightningStormRadius", "lightningStrikeOwner",
			"lightningStrikePlayers", "lightningSetFireSeconds",
			"lightningAllowRepeatStrikeInSameStorm")) {
			assertTrue(source.contains(accessor), "missing UI draft accessor " + accessor);
		}
		assertFalse(source.contains("startStrField(Text.translatable(\"config.enhanced-bows.lightning_damage_mode\")"));
	}
}
