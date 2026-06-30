package io.github.ikunkk02.enhancedbows.client.screen;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.client.sound.AudioImportResult;
import io.github.ikunkk02.enhancedbows.client.sound.CustomSoundImportService;
import io.github.ikunkk02.enhancedbows.client.sound.CustomSoundSlot;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/** Three-slot drag-and-drop importer. All file and FFmpeg work runs off the client thread. */
public final class CustomSoundImportScreen extends Screen {
	private static final int SLOT_WIDTH = 320;
	private static final int SLOT_HEIGHT = 34;
	private static final int SLOT_GAP = 8;
	private static final int SLOT_TOP = 38;
	private final Supplier<Screen> returnScreen;
	private CustomSoundImportService importService;
	private Text status = Text.translatable("screen.enhanced-bows.sound_import.drop_help");
	private boolean busy;
	private int lastMouseX;
	private int lastMouseY;

	public CustomSoundImportScreen(Supplier<Screen> returnScreen) {
		super(Text.translatable("screen.enhanced-bows.sound_import.title"));
		this.returnScreen = returnScreen;
	}

	@Override
	protected void init() {
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.enhanced-bows.sound_import.reload"),
			button -> reloadResources()).dimensions(width / 2 - 155, height - 34, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> close())
			.dimensions(width / 2 + 5, height - 34, 150, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		context.fill(0, 0, width, height, 0xFF101018);
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);

		for (CustomSoundSlot slot : CustomSoundSlot.values()) {
			drawSlot(context, slot, slotTop(slot), mouseX, mouseY);
		}

		context.drawCenteredTextWithShadow(textRenderer, status, width / 2, SLOT_TOP + 3 * (SLOT_HEIGHT + SLOT_GAP) + 8,
			statusColor());
		context.drawCenteredTextWithShadow(textRenderer,
			Text.translatable("screen.enhanced-bows.sound_import.enable_pack"), width / 2, height - 58, 0xFFFFCC55);
	}

	private void drawSlot(DrawContext context, CustomSoundSlot slot, int top, int mouseX, int mouseY) {
		int left = width / 2 - SLOT_WIDTH / 2;
		boolean hovered = inside(left, top, mouseX, mouseY);
		context.fill(left, top, left + SLOT_WIDTH, top + SLOT_HEIGHT, hovered ? 0xFF6A6A8A : 0xFF404050);
		context.fill(left + 2, top + 2, left + SLOT_WIDTH - 2, top + SLOT_HEIGHT - 2,
			hovered ? 0xCC30304A : 0xCC20202A);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable(slotTranslationKey(slot)),
			width / 2, top + 5, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer,
			Text.translatable("screen.enhanced-bows.sound_import.slot_hint"), width / 2, top + 19, 0xFFBBBBBB);
	}

	@Override
	public void filesDragged(List<Path> paths) {
		if (busy || paths.isEmpty()) {
			return;
		}

		CustomSoundSlot slot = slotAt(lastMouseX, lastMouseY);
		if (slot == null) {
			status = Text.translatable("screen.enhanced-bows.sound_import.choose_slot");
			return;
		}

		Path input = paths.getFirst();
		if (!CustomSoundImportService.supports(input)) {
			status = Text.translatable("screen.enhanced-bows.sound_import.unsupported");
			return;
		}

		busy = true;
		status = Text.translatable("screen.enhanced-bows.sound_import.processing", input.getFileName().toString());
		importService().importAsync(slot, input).whenComplete((result, throwable) ->
			MinecraftClient.getInstance().execute(() -> finishImport(slot, result, throwable)));
	}

	private CustomSoundImportService importService() {
		if (importService == null) {
			importService = new CustomSoundImportService(FabricLoader.getInstance().getGameDir(),
				FabricLoader.getInstance().getConfigDir());
		}
		return importService;
	}

	private void finishImport(CustomSoundSlot slot, AudioImportResult result, Throwable throwable) {
		busy = false;
		if (throwable != null) {
			EnhancedBows.LOGGER.warn("Unexpected custom sound import failure", throwable);
			status = Text.translatable("screen.enhanced-bows.sound_import.io_error", concise(throwable.getMessage()));
			return;
		}
		if (result == null) {
			status = Text.translatable("screen.enhanced-bows.sound_import.io_error", "unknown result");
			return;
		}

		if (result.successful()) {
			enableCustomSound(slot);
			status = Text.translatable("screen.enhanced-bows.sound_import.success");
			return;
		}

		status = switch (result.status()) {
			case UNSUPPORTED_FORMAT -> Text.translatable("screen.enhanced-bows.sound_import.unsupported");
			case FFMPEG_MISSING -> Text.translatable("screen.enhanced-bows.sound_import.ffmpeg_missing");
			case CONVERSION_TIMEOUT -> Text.translatable("screen.enhanced-bows.sound_import.timeout");
			case CONVERSION_FAILED -> Text.translatable("screen.enhanced-bows.sound_import.conversion_failed",
				concise(result.detail()));
			case IO_ERROR -> Text.translatable("screen.enhanced-bows.sound_import.io_error", concise(result.detail()));
			case SUCCESS -> Text.translatable("screen.enhanced-bows.sound_import.success");
		};
	}

	private void reloadResources() {
		if (busy || client == null) {
			return;
		}
		busy = true;
		status = Text.translatable("screen.enhanced-bows.sound_import.reloading");
		client.reloadResources().whenComplete((unused, throwable) -> client.execute(() -> {
			busy = false;
			if (throwable == null) {
				status = Text.translatable("screen.enhanced-bows.sound_import.reload_complete");
			} else {
				EnhancedBows.LOGGER.warn("Could not reload client resources", throwable);
				status = Text.translatable("screen.enhanced-bows.sound_import.reload_failed",
					concise(throwable.getMessage()));
			}
		}));
	}

	private static void enableCustomSound(CustomSoundSlot slot) {
		ClientScanConfig.Values old = ClientScanConfig.get();
		ClientScanConfig.setAndSave(new ClientScanConfig.Values(
			old.enableScanSounds(),
			old.useCustomScanStartSound() || slot == CustomSoundSlot.SCAN_START,
			old.useCustomDetectedSound() || slot == CustomSoundSlot.DETECTED,
			old.useCustomBounceSound() || slot == CustomSoundSlot.BOUNCE,
			old.enableScanTextHud(), old.scanTextHudY(),
			old.enableScanningArrowRedTrail(), old.redTrailLength(), old.redTrailParticleCount(),
			old.redTrailParticleSize(), old.enableLightningHud(), old.lightningHudX(), old.lightningHudY(),
			old.lightningHudScale(), old.showLightningHudOnlyWhenHoldingBow()
		));
	}

	private CustomSoundSlot slotAt(int mouseX, int mouseY) {
		int left = width / 2 - SLOT_WIDTH / 2;
		for (CustomSoundSlot slot : CustomSoundSlot.values()) {
			if (inside(left, slotTop(slot), mouseX, mouseY)) {
				return slot;
			}
		}
		return null;
	}

	private static int slotTop(CustomSoundSlot slot) {
		return SLOT_TOP + slot.ordinal() * (SLOT_HEIGHT + SLOT_GAP);
	}

	private static boolean inside(int left, int top, int mouseX, int mouseY) {
		return mouseX >= left && mouseX < left + SLOT_WIDTH && mouseY >= top && mouseY < top + SLOT_HEIGHT;
	}

	private static String slotTranslationKey(CustomSoundSlot slot) {
		return switch (slot) {
			case SCAN_START -> "screen.enhanced-bows.sound_import.scan_start";
			case DETECTED -> "screen.enhanced-bows.sound_import.detected";
			case BOUNCE -> "screen.enhanced-bows.sound_import.bounce";
		};
	}

	private int statusColor() {
		return busy ? 0xFFFFFF55 : 0xFFFFFFFF;
	}

	private static String concise(String detail) {
		if (detail == null || detail.isBlank()) {
			return "unknown error";
		}
		String singleLine = detail.replace('\r', ' ').replace('\n', ' ').trim();
		return singleLine.length() <= 180 ? singleLine : singleLine.substring(singleLine.length() - 180);
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(returnScreen == null ? null : returnScreen.get());
		}
	}
}
