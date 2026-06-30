package io.github.ikunkk02.enhancedbows.client.screen;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.client.hud.ScanAnimationResources;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

/** Visual editor for independent scanning and detected animation placements. */
public final class ScanHudEditorScreen extends Screen {
	private final Supplier<Screen> returnScreen;
	private boolean detectedPreview;
	private boolean dragging;
	private int scanX;
	private int scanY;
	private double scanScale;
	private boolean scanCentered;
	private int detectedX;
	private int detectedY;
	private double detectedScale;
	private boolean detectedCentered;

	public ScanHudEditorScreen() {
		this(null);
	}

	/** Accepts a lazy return screen so Cloth Config is rebuilt with freshly saved positions. */
	public ScanHudEditorScreen(Supplier<Screen> returnScreen) {
		super(Text.translatable("screen.enhanced-bows.scan_hud_editor"));
		this.returnScreen = returnScreen;
		ClientScanConfig.Values config = ClientScanConfig.get();
		scanX = config.scanHudX();
		scanY = config.scanHudY();
		scanScale = config.scanHudScale();
		scanCentered = scanX == -1;
		detectedX = config.detectedHudX();
		detectedY = config.detectedHudY();
		detectedScale = config.detectedHudScale();
		detectedCentered = detectedX == -1;
	}

	@Override
	protected void init() {
		if (scanX == -1) {
			scanX = width / 2;
		}
		if (detectedX == -1) {
			detectedX = width / 2;
		}
		addDrawableChild(ButtonWidget.builder(previewLabel(), button -> {
			detectedPreview = !detectedPreview;
			button.setMessage(previewLabel());
		}).dimensions(width / 2 - 155, 36, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("−"), button -> adjustScale(-0.1))
			.dimensions(width / 2 + 5, 36, 30, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> adjustScale(0.1))
			.dimensions(width / 2 + 40, 36, 30, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		List<Identifier> frames = detectedPreview
			? ScanAnimationResources.INSTANCE.detectedFrames()
			: ScanAnimationResources.INSTANCE.scanningFrames();
		if (!frames.isEmpty()) {
			int index = (int) ((System.currentTimeMillis() / 50L) % frames.size());
			drawPreview(context, frames.get(index));
		} else {
			context.drawCenteredTextWithShadow(textRenderer,
				Text.translatable("screen.enhanced-bows.missing_frames"), width / 2, height / 2, 0xFFFF5555);
		}

		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer,
			Text.translatable("screen.enhanced-bows.scan_hud_values", currentX(), currentY(),
				String.format("%.2f", currentScale())), width / 2, 62, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer,
			Text.translatable("screen.enhanced-bows.scan_hud_help"), width / 2, height - 24, 0xFFDDDDDD);
		super.render(context, mouseX, mouseY, delta);
	}

	private void drawPreview(DrawContext context, Identifier frame) {
		context.getMatrices().push();
		context.getMatrices().translate(currentX(), currentY(), 0.0F);
		context.getMatrices().scale((float) currentScale(), (float) currentScale(), 1.0F);
		context.drawTexture(frame, -ScanAnimationResources.FRAME_WIDTH / 2, 0, 0.0F, 0.0F,
			ScanAnimationResources.FRAME_WIDTH, ScanAnimationResources.FRAME_HEIGHT,
			ScanAnimationResources.FRAME_WIDTH, ScanAnimationResources.FRAME_HEIGHT);
		context.getMatrices().pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && insidePreview(mouseX, mouseY)) {
			dragging = true;
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			setCurrentPosition(clamp((int) Math.round(currentX() + deltaX), 0, width),
				clamp((int) Math.round(currentY() + deltaY), 0, height));
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			dragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		adjustScale(Math.signum(verticalAmount) * 0.1);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_R) {
			if (detectedPreview) {
				detectedX = width / 2;
				detectedY = 40;
				detectedScale = 1.0;
				detectedCentered = true;
			} else {
				scanX = width / 2;
				scanY = 40;
				scanScale = 1.0;
				scanCentered = true;
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		ClientScanConfig.Values old = ClientScanConfig.get();
		ClientScanConfig.setAndSave(new ClientScanConfig.Values(
			old.enableScanHudAnimation(), old.enableDetectedHudAnimation(),
			scanCentered ? -1 : scanX, scanY, scanScale,
			detectedCentered ? -1 : detectedX, detectedY, detectedScale,
			old.enableScanSounds()
		));
		if (client != null) {
			client.setScreen(returnScreen == null ? null : returnScreen.get());
		}
	}

	private Text previewLabel() {
		return Text.translatable(detectedPreview
			? "screen.enhanced-bows.preview_detected"
			: "screen.enhanced-bows.preview_scanning");
	}

	private int currentX() {
		return detectedPreview ? detectedX : scanX;
	}

	private int currentY() {
		return detectedPreview ? detectedY : scanY;
	}

	private double currentScale() {
		return detectedPreview ? detectedScale : scanScale;
	}

	private void setCurrentPosition(int x, int y) {
		if (detectedPreview) {
			detectedX = x;
			detectedY = y;
			detectedCentered = false;
		} else {
			scanX = x;
			scanY = y;
			scanCentered = false;
		}
	}

	private void adjustScale(double delta) {
		if (detectedPreview) {
			detectedScale = clampScale(detectedScale + delta);
		} else {
			scanScale = clampScale(scanScale + delta);
		}
	}

	private boolean insidePreview(double mouseX, double mouseY) {
		double halfWidth = ScanAnimationResources.FRAME_WIDTH * currentScale() / 2.0;
		double previewHeight = ScanAnimationResources.FRAME_HEIGHT * currentScale();
		return mouseX >= currentX() - halfWidth && mouseX <= currentX() + halfWidth
			&& mouseY >= currentY() && mouseY <= currentY() + previewHeight;
	}

	private static double clampScale(double value) {
		return Math.max(0.3, Math.min(3.0, value));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
