package io.github.ikunkk02.enhancedbows;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseReadinessTest {
	private static final Path ROOT = Path.of("");
	private static final Path LANG = Path.of("src/client/resources/assets/enhanced-bows/lang");

	@Test
	void metadataMatchesTheOnePointZeroRelease() throws IOException {
		JsonObject metadata = JsonParser.parseString(Files.readString(
			Path.of("src/main/resources/fabric.mod.json"))).getAsJsonObject();

		assertEquals("enhanced-bows", metadata.get("id").getAsString());
		assertEquals("${version}", metadata.get("version").getAsString());
		assertEquals("Enhanced Bows", metadata.get("name").getAsString());
		assertEquals("shouyun", metadata.getAsJsonArray("authors").get(0).getAsString());
		assertEquals(1, metadata.getAsJsonArray("authors").size());
		assertEquals("MIT", metadata.get("license").getAsString());
		assertEquals("assets/enhanced-bows/icon.png", metadata.get("icon").getAsString());
		assertTrue(Files.isRegularFile(Path.of("src/main/resources/assets/enhanced-bows/icon.png")));
		String description = metadata.get("description").getAsString();
		for (String term : Set.of("spectral", "Lightning", "Burst", "Arrow Rain", "HUD", "sound")) {
			assertTrue(description.contains(term), term);
		}

		JsonObject contact = metadata.getAsJsonObject("contact");
		assertEquals("https://space.bilibili.com/1832031043", contact.get("homepage").getAsString());
		assertEquals("https://github.com/ikunkk02-afk/enhanced-bows-template", contact.get("sources").getAsString());
		assertEquals("https://v.douyin.com/75aDMY8LVM8/", contact.get("douyin").getAsString());
		assertEquals("https://space.bilibili.com/1832031043", contact.get("bilibili").getAsString());

		JsonObject depends = metadata.getAsJsonObject("depends");
		assertTrue(depends.has("cloth-config"));
		assertTrue(depends.has("modmenu"));
		assertFalse(metadata.has("suggests"));
	}

	@Test
	void languagesHaveMatchingKeysAndReleaseNames() throws IOException {
		JsonObject english = readLanguage("en_us");
		JsonObject chinese = readLanguage("zh_cn");
		assertEquals(english.keySet(), chinese.keySet());
		assertEquals("Lightning", english.get("enchantment.enhanced_bows.lightning").getAsString());
		assertEquals("Burst", english.get("enchantment.enhanced_bows.burst").getAsString());
		assertEquals("Arrow Rain", english.get("enchantment.enhanced_bows.arrow_rain").getAsString());
		assertEquals("闪电", chinese.get("enchantment.enhanced_bows.lightning").getAsString());
		assertEquals("爆裂", chinese.get("enchantment.enhanced_bows.burst").getAsString());
		assertEquals("箭雨", chinese.get("enchantment.enhanced_bows.arrow_rain").getAsString());
		assertEquals("Toggle Arrow Rain Mode", english.get("key.enhanced-bows.toggle_arrow_rain").getAsString());
		assertEquals("切换箭雨模式", chinese.get("key.enhanced-bows.toggle_arrow_rain").getAsString());
		assertEquals("Enhanced Bows", english.get("category.enhanced-bows.enhanced_bows").getAsString());
		assertEquals("加强弓", chinese.get("category.enhanced-bows.enhanced_bows").getAsString());
	}

	@Test
	void readmeLicenseAndIgnoreRulesAreReleaseReady() throws IOException {
		String readme = Files.readString(ROOT.resolve("README.md"));
		for (String section : Set.of("# Enhanced Bows", "## 功能介绍", "### 光灵箭探测",
			"### 闪电附魔", "### 爆裂附魔", "### 箭雨附魔", "### 自定义音效",
			"## 前置要求", "## 安装方法", "## 配置说明", "## 按键", "## 兼容性说明",
			"## 注意事项", "## 许可证", "## Credits")) {
			assertTrue(readme.contains(section), section);
		}
		assertTrue(readme.contains("作者：shouyun"));
		assertTrue(readme.contains("https://v.douyin.com/75aDMY8LVM8/"));
		assertTrue(readme.contains("https://space.bilibili.com/1832031043"));
		assertTrue(readme.contains("Fabric 1.21.1"));
		assertTrue(Files.readString(ROOT.resolve("LICENSE")).startsWith("MIT License"));

		String ignore = Files.readString(ROOT.resolve(".gitignore"));
		for (String entry : Set.of(".gradle/", "build/", ".idea/", ".vscode/", "run/")) {
			assertTrue(ignore.contains(entry), entry);
		}
		assertTrue(ignore.contains("src/main/generated/.cache/"));
	}

	private static JsonObject readLanguage(String locale) throws IOException {
		return JsonParser.parseString(Files.readString(LANG.resolve(locale + ".json"))).getAsJsonObject();
	}
}
