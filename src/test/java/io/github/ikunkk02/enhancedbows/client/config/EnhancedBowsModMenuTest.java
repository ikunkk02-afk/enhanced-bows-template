package io.github.ikunkk02.enhancedbows.client.config;

import net.minecraft.client.gui.screen.Screen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EnhancedBowsModMenuTest {
	@Test
	void modMenuOpensDedicatedHubInsteadOfInjectingAButtonIntoClothScreen() {
		Screen screen = new EnhancedBowsModMenu().getModConfigScreenFactory().create(null);

		assertEquals("EnhancedBowsConfigHubScreen", screen.getClass().getSimpleName());
		EnhancedBowsConfigHubScreen hub = assertInstanceOf(EnhancedBowsConfigHubScreen.class, screen);
		assertEquals("CustomSoundImportScreen", hub.createSoundImportScreen().getClass().getSimpleName());
	}
}
