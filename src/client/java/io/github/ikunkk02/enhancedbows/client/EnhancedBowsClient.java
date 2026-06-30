package io.github.ikunkk02.enhancedbows.client;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.client.hud.ScanAnimationResources;
import io.github.ikunkk02.enhancedbows.client.hud.ScanHudRenderer;
import io.github.ikunkk02.enhancedbows.client.hud.ScanHudState;
import io.github.ikunkk02.enhancedbows.client.network.ClientScanNetworking;
import io.github.ikunkk02.enhancedbows.client.screen.ScanHudEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import org.lwjgl.glfw.GLFW;

public class EnhancedBowsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientScanConfig.load();
		ClientScanNetworking.register();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(ScanAnimationResources.INSTANCE);
		HudRenderCallback.EVENT.register(ScanHudRenderer::render);

		KeyBinding editorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.enhanced-bows.open_scan_hud_editor",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_ALT,
			"category.enhanced-bows"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ScanHudState.tick();
			while (editorKey.wasPressed()) {
				client.setScreen(new ScanHudEditorScreen());
			}
		});
	}
}
