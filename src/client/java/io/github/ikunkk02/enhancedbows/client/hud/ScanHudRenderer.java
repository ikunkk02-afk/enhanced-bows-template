package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/** Draws one simple centered text message using only client-side classes. */
public final class ScanHudRenderer {
	private ScanHudRenderer() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		ClientScanConfig.Values config = ClientScanConfig.get();
		ScanHudState.Message message = ScanHudState.currentMessage();
		if (!config.enableScanTextHud() || message == null) {
			return;
		}

		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
			Text.translatable(message.translationKey()), context.getScaledWindowWidth() / 2,
			config.scanTextHudY(), 0xFFFFFFFF);
	}
}
