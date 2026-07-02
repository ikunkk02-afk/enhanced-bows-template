package io.github.ikunkk02.enhancedbows;

import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainManager;
import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import io.github.ikunkk02.enhancedbows.lightning.LightningEvents;
import io.github.ikunkk02.enhancedbows.lightning.LightningStormManager;
import io.github.ikunkk02.enhancedbows.network.ModNetworking;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedBows implements ModInitializer {
	public static final String MOD_ID = "enhanced-bows";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerScanConfig.load();
		ModNetworking.register();
		ModEnchantments.registerCreativeBook();
		LightningEvents.register();
		LightningStormManager.register();
		ArrowRainManager.register();
		LOGGER.info("Enhanced Bows spectral-arrow scanning initialized");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
