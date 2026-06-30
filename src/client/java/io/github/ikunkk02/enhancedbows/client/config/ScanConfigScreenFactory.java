package io.github.ikunkk02.enhancedbows.client.config;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.client.mixin.ScreenAccessor;
import io.github.ikunkk02.enhancedbows.client.screen.ScanHudEditorScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/** Builds the Mod Menu page for local server rules and client presentation settings. */
public final class ScanConfigScreenFactory {
	private ScanConfigScreenFactory() {
	}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.translatable("config.enhanced-bows.title"));
		ConfigEntryBuilder entries = builder.entryBuilder();
		ServerDraft server = new ServerDraft(ServerScanConfig.get());
		ClientDraft client = new ClientDraft(ClientScanConfig.get());

		ConfigCategory rules = builder.getOrCreateCategory(Text.translatable("config.enhanced-bows.category.rules"));
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_scan"), server.enabled)
			.setDefaultValue(true).setSaveConsumer(value -> server.enabled = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.radius"), server.radius)
			.setDefaultValue(10.0).setMin(0.1).setMax(64.0).setSaveConsumer(value -> server.radius = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.duration"), server.duration)
			.setDefaultValue(100).setMin(1).setMax(100).setSaveConsumer(value -> server.duration = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.interval"), server.interval)
			.setDefaultValue(5).setMin(1).setSaveConsumer(value -> server.interval = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.glowing"), server.glowing)
			.setDefaultValue(200).setMin(1).setSaveConsumer(value -> server.glowing = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.scan_players"), server.scanPlayers)
			.setDefaultValue(true).setSaveConsumer(value -> server.scanPlayers = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.scan_owner"), server.scanOwner)
			.setDefaultValue(false).setSaveConsumer(value -> server.scanOwner = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.threshold"), server.threshold)
			.setDefaultValue(0.35).setMin(0.0).setSaveConsumer(value -> server.threshold = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_bounce"), server.bounce)
			.setDefaultValue(true).setSaveConsumer(value -> server.bounce = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.max_bounces"), server.maxBounces)
			.setDefaultValue(3).setMin(0).setMax(16).setSaveConsumer(value -> server.maxBounces = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.bounce_damping"), server.damping)
			.setDefaultValue(0.75).setMin(0.0).setMax(1.0).setSaveConsumer(value -> server.damping = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.min_bounce_velocity"), server.minVelocity)
			.setDefaultValue(0.15).setMin(0.0).setSaveConsumer(value -> server.minVelocity = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.require_line_of_sight"), server.lineOfSight)
			.setDefaultValue(true).setSaveConsumer(value -> server.lineOfSight = value).build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("config.enhanced-bows.category.hud"));
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_scan_hud"), client.scanHud)
			.setDefaultValue(true).setSaveConsumer(value -> client.scanHud = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_detected_hud"), client.detectedHud)
			.setDefaultValue(true).setSaveConsumer(value -> client.detectedHud = value).build());
		hud.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.hud_x"), client.x)
			.setDefaultValue(-1).setSaveConsumer(value -> client.x = value).build());
		hud.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.hud_y"), client.y)
			.setDefaultValue(40).setSaveConsumer(value -> client.y = value).build());
		hud.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.hud_scale"), client.scale)
			.setDefaultValue(1.0).setMin(0.3).setMax(3.0).setSaveConsumer(value -> client.scale = value).build());
		hud.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.detected_hud_x"), client.detectedX)
			.setDefaultValue(-1).setSaveConsumer(value -> client.detectedX = value).build());
		hud.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.detected_hud_y"), client.detectedY)
			.setDefaultValue(40).setSaveConsumer(value -> client.detectedY = value).build());
		hud.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.detected_hud_scale"), client.detectedScale)
			.setDefaultValue(1.0).setMin(0.3).setMax(3.0).setSaveConsumer(value -> client.detectedScale = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_sounds"), client.sounds)
			.setDefaultValue(true).setSaveConsumer(value -> client.sounds = value).build());

		builder.setSavingRunnable(() -> {
			ServerScanConfig.setAndSave(server.toValues());
			ClientScanConfig.setAndSave(client.toValues());
		});
		builder.setAfterInitConsumer(screen -> ((ScreenAccessor) screen).enhancedBows$addDrawableChild(
			ButtonWidget.builder(Text.translatable("config.enhanced-bows.open_hud_editor"), button ->
				MinecraftClient.getInstance().setScreen(new ScanHudEditorScreen(() -> create(parent))))
				.dimensions(screen.width / 2 - 100, screen.height - 54, 200, 20)
				.build()
		));
		return builder.build();
	}

	private static final class ServerDraft {
		private boolean enabled;
		private double radius;
		private int duration;
		private int interval;
		private int glowing;
		private boolean scanPlayers;
		private boolean scanOwner;
		private double threshold;
		private boolean bounce;
		private int maxBounces;
		private double damping;
		private double minVelocity;
		private boolean lineOfSight;

		private ServerDraft(ServerScanConfig.Values values) {
			enabled = values.enableSpectralArrowScan();
			radius = values.scanRadius();
			duration = values.scanDurationTicks();
			interval = values.scanIntervalTicks();
			glowing = values.glowingDurationTicks();
			scanPlayers = values.scanPlayers();
			scanOwner = values.scanOwner();
			threshold = values.upwardVelocityThreshold();
			bounce = values.enableScanningArrowBounce();
			maxBounces = values.scanningArrowMaxBounces();
			damping = values.scanningArrowBounceDamping();
			minVelocity = values.scanningArrowMinBounceVelocity();
			lineOfSight = values.scanRequiresLineOfSight();
		}

		private ServerScanConfig.Values toValues() {
			return new ServerScanConfig.Values(enabled, radius, duration, interval, glowing,
				scanPlayers, scanOwner, threshold, bounce, maxBounces, damping, minVelocity, lineOfSight);
		}
	}

	private static final class ClientDraft {
		private boolean scanHud;
		private boolean detectedHud;
		private int x;
		private int y;
		private double scale;
		private int detectedX;
		private int detectedY;
		private double detectedScale;
		private boolean sounds;

		private ClientDraft(ClientScanConfig.Values values) {
			scanHud = values.enableScanHudAnimation();
			detectedHud = values.enableDetectedHudAnimation();
			x = values.scanHudX();
			y = values.scanHudY();
			scale = values.scanHudScale();
			detectedX = values.detectedHudX();
			detectedY = values.detectedHudY();
			detectedScale = values.detectedHudScale();
			sounds = values.enableScanSounds();
		}

		private ClientScanConfig.Values toValues() {
			return new ClientScanConfig.Values(scanHud, detectedHud, x, y, scale,
				detectedX, detectedY, detectedScale, sounds);
		}
	}
}
