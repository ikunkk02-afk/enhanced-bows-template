package io.github.ikunkk02.enhancedbows.enchantment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentDatagenReleaseTest {
	private static final Path GENERATED = Path.of("src/main/generated/data");
	private static final Path MANUAL = Path.of("src/main/resources/data");

	@Test
	void datagenIsTheOnlySourceForAllThreeEnchantments() {
		for (String id : List.of("lightning", "burst", "arrow_rain")) {
			assertTrue(Files.isRegularFile(GENERATED.resolve(
				"enhanced-bows/enchantment/" + id + ".json")), id);
			assertFalse(Files.exists(MANUAL.resolve(
				"enhanced-bows/enchantment/" + id + ".json")), "manual duplicate: " + id);
		}
	}

	@Test
	void generatedDefinitionsMatchReleaseContract() throws IOException {
		assertDefinition("lightning", "enchantment.enhanced_bows.lightning", 18, 50,
			"#enhanced-bows:exclusive_set/lightning_burst");
		assertDefinition("burst", "enchantment.enhanced_bows.burst", 15, 45,
			"#enhanced-bows:exclusive_set/burst_conflicts");
		assertDefinition("arrow_rain", "enchantment.enhanced_bows.arrow_rain", 16, 46,
			"#enhanced-bows:exclusive_set/arrow_rain_burst");
	}

	@Test
	void generatedSourceAndExclusiveTagsMatchReleaseMatrix() throws IOException {
		Set<String> all = Set.of("enhanced-bows:lightning", "enhanced-bows:burst",
			"enhanced-bows:arrow_rain");
		for (String tag : List.of("in_enchanting_table", "non_treasure", "on_random_loot", "tradeable")) {
			assertEquals(all, values("minecraft/tags/enchantment/" + tag + ".json"), tag);
		}
		assertEquals(Set.of("enhanced-bows:lightning", "enhanced-bows:burst"),
			values("enhanced-bows/tags/enchantment/exclusive_set/lightning_burst.json"));
		assertEquals(Set.of("enhanced-bows:burst", "enhanced-bows:lightning", "enhanced-bows:arrow_rain"),
			values("enhanced-bows/tags/enchantment/exclusive_set/burst_conflicts.json"));
		assertEquals(Set.of("enhanced-bows:arrow_rain", "enhanced-bows:burst"),
			values("enhanced-bows/tags/enchantment/exclusive_set/arrow_rain_burst.json"));
	}

	@Test
	void datagenEntryPointRegistersDynamicDefinitionsAndTags() throws IOException {
		String entrypoint = Files.readString(Path.of(
			"src/client/java/io/github/ikunkk02/enhancedbows/client/EnhancedBowsDataGenerator.java"));
		String enchantments = Files.readString(Path.of(
			"src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java"));
		String build = Files.readString(Path.of("build.gradle"));

		assertTrue(entrypoint.contains("ModEnchantmentProvider"));
		assertTrue(entrypoint.contains("ModEnchantmentTagProvider"));
		assertTrue(entrypoint.contains("ModEnchantments::bootstrap"));
		assertTrue(enchantments.contains("void bootstrap(Registerable<Enchantment>"));
		assertTrue(enchantments.contains("ItemTags.BOW_ENCHANTABLE"));
		assertTrue(Files.isRegularFile(Path.of(
			"src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantmentTags.java")));
		assertTrue(build.contains("configureDataGeneration"));
	}

	private static void assertDefinition(String id, String translation, int minCost, int maxCost,
			String exclusiveSet) throws IOException {
		JsonObject definition = read("enhanced-bows/enchantment/" + id + ".json");
		assertEquals(translation, definition.getAsJsonObject("description").get("translate").getAsString());
		assertEquals("#minecraft:enchantable/bow", definition.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/bow", definition.get("primary_items").getAsString());
		assertEquals(5, definition.get("weight").getAsInt());
		assertEquals(1, definition.get("max_level").getAsInt());
		assertEquals(minCost, definition.getAsJsonObject("min_cost").get("base").getAsInt());
		assertEquals(0, definition.getAsJsonObject("min_cost").get("per_level_above_first").getAsInt());
		assertEquals(maxCost, definition.getAsJsonObject("max_cost").get("base").getAsInt());
		assertEquals(0, definition.getAsJsonObject("max_cost").get("per_level_above_first").getAsInt());
		assertEquals(4, definition.get("anvil_cost").getAsInt());
		assertEquals(Set.of("mainhand", "offhand"), definition.getAsJsonArray("slots").asList().stream()
			.map(element -> element.getAsString()).collect(Collectors.toSet()));
		assertEquals(exclusiveSet, definition.get("exclusive_set").getAsString());
		assertTrue(!definition.has("effects") || definition.getAsJsonObject("effects").isEmpty());
	}

	private static Set<String> values(String relative) throws IOException {
		JsonArray values = read(relative).getAsJsonArray("values");
		return values.asList().stream().map(element -> element.getAsString()).collect(Collectors.toSet());
	}

	private static JsonObject read(String relative) throws IOException {
		return JsonParser.parseString(Files.readString(GENERATED.resolve(relative))).getAsJsonObject();
	}
}
