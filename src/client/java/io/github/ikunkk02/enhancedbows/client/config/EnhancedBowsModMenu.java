package io.github.ikunkk02.enhancedbows.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Exposes the Cloth Config screen through Mod Menu on physical clients only. */
public final class EnhancedBowsModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return EnhancedBowsConfigHubScreen::new;
	}
}
