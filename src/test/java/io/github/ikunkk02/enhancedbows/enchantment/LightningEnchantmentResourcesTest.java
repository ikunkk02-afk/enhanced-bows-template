package io.github.ikunkk02.enhancedbows.enchantment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningEnchantmentResourcesTest {
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void lightningIsLevelOneAndSupportsOnlyBows() throws IOException {
		JsonObject enchantment = read("data/enhanced-bows/enchantment/lightning.json");

		assertEquals("#minecraft:enchantable/bow", enchantment.get("supported_items").getAsString());
		assertEquals("#minecraft:enchantable/bow", enchantment.get("primary_items").getAsString());
		assertEquals(1, enchantment.get("max_level").getAsInt());
	}

	@Test
	void lightningAppearsInAllRequestedVanillaSources() throws IOException {
		for (String tag : new String[] {"in_enchanting_table", "non_treasure", "tradeable", "on_random_loot"}) {
			JsonArray values = read("data/minecraft/tags/enchantment/" + tag + ".json").getAsJsonArray("values");
			assertTrue(values.asList().stream().anyMatch(value -> value.getAsString().equals("enhanced-bows:lightning")));
		}
	}

	private static JsonObject read(String relative) throws IOException {
		return JsonParser.parseString(Files.readString(RESOURCES.resolve(relative))).getAsJsonObject();
	}
}
