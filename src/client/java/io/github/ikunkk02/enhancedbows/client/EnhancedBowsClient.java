package io.github.ikunkk02.enhancedbows.client;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.client.hud.ScanHudRenderer;
import io.github.ikunkk02.enhancedbows.client.hud.ScanHudState;
import io.github.ikunkk02.enhancedbows.client.hud.LightningHudRenderer;
import io.github.ikunkk02.enhancedbows.client.hud.ArrowRainHudRenderer;
import io.github.ikunkk02.enhancedbows.client.input.ArrowRainKeyBinding;
import io.github.ikunkk02.enhancedbows.client.network.ClientArrowRainNetworking;
import io.github.ikunkk02.enhancedbows.client.network.ClientScanNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class EnhancedBowsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientScanConfig.load();
		ClientScanNetworking.register();
		ClientArrowRainNetworking.register();
		ArrowRainKeyBinding.register();
		HudRenderCallback.EVENT.register(ScanHudRenderer::render);
		HudRenderCallback.EVENT.register(LightningHudRenderer::render);
		HudRenderCallback.EVENT.register(ArrowRainHudRenderer::render);
		ClientTickEvents.END_CLIENT_TICK.register(client -> ScanHudState.tick());
	}
}
