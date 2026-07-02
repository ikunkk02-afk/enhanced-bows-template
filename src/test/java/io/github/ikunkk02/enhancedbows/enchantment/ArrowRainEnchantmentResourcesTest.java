package io.github.ikunkk02.enhancedbows.enchantment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainEnchantmentResourcesTest {
	private static final Path RESOURCES = Path.of("src/main/generated");

	@Test
	void arrowRainIsLevelOneAndSupportsOnlyBows() throws IOException {
		JsonObject arrowRain = readObject("data/enhanced-bows/enchantment/arrow_rain.json");

		assertEquals("#minecraft:enchantable/bow", arrowRain.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/bow", arrowRain.get("primary_items").getAsString());
		assertEquals(1, arrowRain.get("max_level").getAsInt());
	}

	@Test
	void burstAndArrowRainAreMutuallyExclusiveWithoutBlockingLightning() throws IOException {
		JsonObject lightning = readObject("data/enhanced-bows/enchantment/lightning.json");
		JsonObject burst = readObject("data/enhanced-bows/enchantment/burst.json");
		JsonObject arrowRain = readObject("data/enhanced-bows/enchantment/arrow_rain.json");

		Set<String> lightningExclusions = valuesForReference(lightning.get("exclusive_set").getAsString());
		Set<String> burstExclusions = valuesForReference(burst.get("exclusive_set").getAsString());
		Set<String> arrowRainExclusions = valuesForReference(arrowRain.get("exclusive_set").getAsString());

		assertTrue(lightningExclusions.contains("enhanced-bows:burst"));
		assertTrue(burstExclusions.contains("enhanced-bows:lightning"));
		assertTrue(burstExclusions.contains("enhanced-bows:arrow_rain"));
		assertTrue(arrowRainExclusions.contains("enhanced-bows:burst"));
		assertFalse(lightningExclusions.contains("enhanced-bows:arrow_rain"));
		assertFalse(arrowRainExclusions.contains("enhanced-bows:lightning"));
	}

	@Test
	void arrowRainAppearsInAllRequestedSourcesAndCreativeBookCode() throws IOException {
		for (String tag : new String[] {"in_enchanting_table", "non_treasure", "tradeable", "on_random_loot"}) {
			JsonArray values = readObject("data/minecraft/tags/enchantment/" + tag + ".json")
				.getAsJsonArray("values");
			assertTrue(values.asList().stream()
				.anyMatch(value -> value.getAsString().equals("enhanced-bows:arrow_rain")), tag);
		}

		String source = Files.readString(Path.of(
			"src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java"));
		assertTrue(source.contains("RegistryKey<Enchantment> ARROW_RAIN"));
		assertTrue(source.contains("getArrowRainLevel"));
		assertTrue(source.contains("getOptional(ARROW_RAIN)"));
	}

	private static Set<String> valuesForReference(String reference) throws IOException {
		String id = reference.substring(1);
		String[] parts = id.split(":", 2);
		JsonArray values = readObject("data/" + parts[0] + "/tags/enchantment/" + parts[1] + ".json")
			.getAsJsonArray("values");
		return values.asList().stream().map(element -> element.getAsString()).collect(Collectors.toSet());
	}

	private static JsonObject readObject(String relative) throws IOException {
		return JsonParser.parseString(Files.readString(RESOURCES.resolve(relative))).getAsJsonObject();
	}
}
