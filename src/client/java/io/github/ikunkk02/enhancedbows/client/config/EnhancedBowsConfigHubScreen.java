package io.github.ikunkk02.enhancedbows.client.config;

import io.github.ikunkk02.enhancedbows.client.screen.CustomSoundImportScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Stable Mod Menu landing page that routes to Cloth Config or the sound importer. */
public final class EnhancedBowsConfigHubScreen extends Screen {
	private final Screen parent;

	public EnhancedBowsConfigHubScreen(Screen parent) {
		super(Text.translatable("config.enhanced-bows.hub.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int left = width / 2 - 110;
		int top = height / 2 - 35;
		addDrawableChild(ButtonWidget.builder(Text.translatable("config.enhanced-bows.hub.open_settings"),
			button -> client.setScreen(ScanConfigScreenFactory.create(this)))
			.dimensions(left, top, 220, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("config.enhanced-bows.open_sound_import"),
			button -> client.setScreen(createSoundImportScreen()))
			.dimensions(left, top + 26, 220, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
			.dimensions(left, top + 58, 220, 20).build());
	}

	Screen createSoundImportScreen() {
		return new CustomSoundImportScreen(() -> this);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xFF101018);
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 68, 0xFFFFFFFF);
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}
}
