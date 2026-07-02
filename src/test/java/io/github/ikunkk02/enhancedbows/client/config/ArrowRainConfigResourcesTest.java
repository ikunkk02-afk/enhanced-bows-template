package io.github.ikunkk02.enhancedbows.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainConfigResourcesTest {
	private static final Path CLIENT = Path.of("src/client/java/io/github/ikunkk02/enhancedbows/client");

	@Test
	void localesContainArrowRainEnchantmentKeyHudMessagesAndAllConfigLabels() throws IOException {
		for (String locale : new String[] {"en_us", "zh_cn"}) {
			JsonObject lang = JsonParser.parseString(Files.readString(Path.of(
				"src/client/resources/assets/enhanced-bows/lang/" + locale + ".json"))).getAsJsonObject();
			for (String key : new String[] {
				"enchantment.enhanced_bows.arrow_rain",
				"key.enhanced-bows.toggle_arrow_rain",
				"key.categories.enhanced-bows",
				"message.enhanced-bows.arrow_rain.enabled",
				"message.enhanced-bows.arrow_rain.disabled",
				"message.enhanced-bows.arrow_rain.requires_bow",
				"message.enhanced-bows.arrow_rain.burst_conflict",
				"hud.enhanced-bows.arrow_rain.off",
				"hud.enhanced-bows.arrow_rain.on",
				"hud.enhanced-bows.arrow_rain.cooldown",
				"config.enhanced-bows.category.arrow_rain",
				"config.enhanced-bows.enable_arrow_rain_enchantment",
				"config.enhanced-bows.arrow_rain_radius",
				"config.enhanced-bows.arrow_rain_arrow_count",
				"config.enhanced-bows.arrow_rain_height",
				"config.enhanced-bows.arrow_rain_duration_ticks",
				"config.enhanced-bows.arrow_rain_waves",
				"config.enhanced-bows.arrow_rain_cooldown_ticks",
				"config.enhanced-bows.arrow_rain_damage_multiplier",
				"config.enhanced-bows.arrow_rain_trigger_on_block_hit",
				"config.enhanced-bows.arrow_rain_trigger_on_entity_hit",
				"config.enhanced-bows.arrow_rain_trigger_on_miss",
				"config.enhanced-bows.arrow_rain_allow_spectral_arrow",
				"config.enhanced-bows.enable_arrow_rain_hud"
			}) {
				assertTrue(lang.has(key), locale + ": " + key);
			}
		}
	}

	@Test
	void clothDraftAndClientOnlyRendererWireEveryArrowRainSetting() throws IOException {
		String screen = Files.readString(CLIENT.resolve("config/ScanConfigScreenFactory.java"));
		String renderer = Files.readString(CLIENT.resolve("hud/ArrowRainHudRenderer.java"));
		String initializer = Files.readString(CLIENT.resolve("EnhancedBowsClient.java"));

		for (String field : new String[] {
			"enableArrowRainEnchantment", "arrowRainRadius", "arrowRainArrowCount",
			"arrowRainHeight", "arrowRainDurationTicks", "arrowRainWaves",
			"arrowRainCooldownTicks", "arrowRainDamageMultiplier",
			"arrowRainTriggerOnBlockHit", "arrowRainTriggerOnEntityHit",
			"arrowRainTriggerOnMiss",
			"arrowRainAllowSpectralArrow", "enableArrowRainHud"
		}) {
			assertTrue(screen.contains(field), field);
		}
		assertTrue(screen.contains("setDefaultValue(8.0)"));
		assertTrue(renderer.contains("ArrowRainComponents.ARROW_RAIN.get"));
		assertTrue(renderer.contains("ModEnchantments.getArrowRainLevel"));
		assertTrue(renderer.contains("ModEnchantments.getLightningLevel"));
		assertTrue(renderer.contains("0.85F"));
		assertTrue(initializer.contains("HudRenderCallback.EVENT.register(ArrowRainHudRenderer::render)"));
	}
}
