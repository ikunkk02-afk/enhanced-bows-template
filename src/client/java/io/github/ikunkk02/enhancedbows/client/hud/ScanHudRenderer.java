package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Draws a single centered HUD frame using only client-side classes. */
public final class ScanHudRenderer {
	private ScanHudRenderer() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		ScanHudState.ActiveFrame frame = ScanHudState.currentFrame();
		if (frame == null) {
			return;
		}

		ClientScanConfig.Values config = ClientScanConfig.get();
		int configuredX = frame.detected() ? config.detectedHudX() : config.scanHudX();
		int centerX = configuredX == -1 ? context.getScaledWindowWidth() / 2 : configuredX;
		int topY = frame.detected() ? config.detectedHudY() : config.scanHudY();
		float scale = (float) (frame.detected() ? config.detectedHudScale() : config.scanHudScale());

		context.getMatrices().push();
		context.getMatrices().translate(centerX, topY, 0.0F);
		context.getMatrices().scale(scale, scale, 1.0F);
		context.drawTexture(frame.texture(), -ScanAnimationResources.FRAME_WIDTH / 2, 0, 0.0F, 0.0F,
			ScanAnimationResources.FRAME_WIDTH, ScanAnimationResources.FRAME_HEIGHT,
			ScanAnimationResources.FRAME_WIDTH, ScanAnimationResources.FRAME_HEIGHT);
		context.getMatrices().pop();
	}
}
