package io.github.ikunkk02.enhancedbows.client.input;

import io.github.ikunkk02.enhancedbows.network.ToggleArrowRainModeC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ArrowRainKeyBinding {
	private static final KeyBinding TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key.enhanced-bows.toggle_arrow_rain",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_V,
		"category.enhanced-bows.enhanced_bows"
	));

	private ArrowRainKeyBinding() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE.wasPressed()) {
				if (client.player != null && ClientPlayNetworking.canSend(ToggleArrowRainModeC2SPayload.ID)) {
					ClientPlayNetworking.send(new ToggleArrowRainModeC2SPayload());
				}
			}
		});
	}
}
