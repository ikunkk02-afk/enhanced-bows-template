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
import static org.junit.jupiter.api.Assertions.assertTrue;

class BurstEnchantmentResourcesTest {
	private static final Path RESOURCES = Path.of("src/main/generated");

	@Test
	void burstIsLevelOneAndSupportsOnlyBows() throws IOException {
		JsonObject burst = readObject("data/enhanced-bows/enchantment/burst.json");

		assertEquals("#minecraft:enchantable/bow", burst.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/bow", burst.get("primary_items").getAsString());
		assertEquals(1, burst.get("max_level").getAsInt());
		assertEquals("#enhanced-bows:exclusive_set/burst_conflicts",
			burst.get("exclusive_set").getAsString());
	}

	@Test
	void lightningAndBurstShareAnExclusiveSet() throws IOException {
		JsonObject lightning = readObject("data/enhanced-bows/enchantment/lightning.json");
		JsonArray values = readObject(
			"data/enhanced-bows/tags/enchantment/exclusive_set/lightning_burst.json")
			.getAsJsonArray("values");

		assertEquals("#enhanced-bows:exclusive_set/lightning_burst",
			lightning.get("exclusive_set").getAsString());
		assertEquals(Set.of("enhanced-bows:lightning", "enhanced-bows:burst"),
			values.asList().stream().map(element -> element.getAsString()).collect(Collectors.toSet()));
		assertEquals(2, values.size());
	}

	@Test
	void burstAppearsInAllRequestedVanillaSourcesAndCreativeBookCode() throws IOException {
		for (String tag : new String[] {"in_enchanting_table", "non_treasure", "tradeable", "on_random_loot"}) {
			JsonArray values = readObject("data/minecraft/tags/enchantment/" + tag + ".json")
				.getAsJsonArray("values");
			assertTrue(values.asList().stream()
				.anyMatch(value -> value.getAsString().equals("enhanced-bows:burst")), tag);
		}

		String source = Files.readString(Path.of(
			"src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java"));
		assertTrue(source.contains("RegistryKey<Enchantment> BURST"));
		assertTrue(source.contains("getOptional(BURST)"));
	}

	private static JsonObject readObject(String relative) throws IOException {
		return JsonParser.parseString(Files.readString(RESOURCES.resolve(relative))).getAsJsonObject();
	}
}
