package io.github.ikunkk02.enhancedbows.client.config;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Builds the Mod Menu page for local server rules and client presentation settings. */
public final class ScanConfigScreenFactory {
	private ScanConfigScreenFactory() {
	}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.translatable("config.enhanced-bows.title"))
			.solidBackground();
		ConfigEntryBuilder entries = builder.entryBuilder();
		ServerDraft server = new ServerDraft(ServerScanConfig.get());
		ClientDraft client = new ClientDraft(ClientScanConfig.get());

		ConfigCategory rules = builder.getOrCreateCategory(Text.translatable("config.enhanced-bows.category.rules"));
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_scan"), server.enabled)
			.setDefaultValue(true).setSaveConsumer(value -> server.enabled = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.radius"), server.radius)
			.setDefaultValue(80.0).setMin(0.1).setMax(80.0).setSaveConsumer(value -> server.radius = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.interval"), server.interval)
			.setDefaultValue(2).setMin(1).setSaveConsumer(value -> server.interval = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.glowing"), server.glowing)
			.setDefaultValue(200).setMin(1).setSaveConsumer(value -> server.glowing = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.scan_players"), server.scanPlayers)
			.setDefaultValue(true).setSaveConsumer(value -> server.scanPlayers = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.scan_owner"), server.scanOwner)
			.setDefaultValue(false).setSaveConsumer(value -> server.scanOwner = value).build());
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
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.strict_line_of_sight"), server.strictLineOfSight)
			.setDefaultValue(true).setSaveConsumer(value -> server.strictLineOfSight = value).build());
		rules.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_red_trail"), client.redTrail)
			.setDefaultValue(true).setSaveConsumer(value -> client.redTrail = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.red_trail_length"), client.redTrailLength)
			.setDefaultValue(12).setMin(1).setMax(64).setSaveConsumer(value -> client.redTrailLength = value).build());
		rules.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.red_trail_particle_count"), client.redTrailParticleCount)
			.setDefaultValue(14).setMin(1).setMax(64).setSaveConsumer(value -> client.redTrailParticleCount = value).build());
		rules.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.red_trail_particle_size"), client.redTrailParticleSize)
			.setDefaultValue(1.0).setMin(0.1).setMax(4.0).setSaveConsumer(value -> client.redTrailParticleSize = value).build());

		ConfigCategory lightning = builder.getOrCreateCategory(Text.translatable("config.enhanced-bows.category.lightning"));
		lightning.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_lightning_enchantment"), server.lightningEnabled)
			.setDefaultValue(true).setSaveConsumer(value -> server.lightningEnabled = value).build());
		lightning.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.lightning_max_charges"), server.lightningMaxCharges)
			.setDefaultValue(2).setMin(1).setMax(20).setSaveConsumer(value -> server.lightningMaxCharges = value).build());
		lightning.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.lightning_recharge_ticks"), server.lightningRechargeTicks)
			.setDefaultValue(400).setMin(1).setSaveConsumer(value -> server.lightningRechargeTicks = value).build());
		lightning.addEntry(entries.startStrField(Text.translatable("config.enhanced-bows.lightning_damage_mode"), server.lightningDamageMode)
			.setDefaultValue("vanilla_lightning").setSaveConsumer(value -> server.lightningDamageMode = value).build());
		lightning.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.lightning_keep_after_death"), server.lightningKeepAfterDeath)
			.setDefaultValue(true).setSaveConsumer(value -> server.lightningKeepAfterDeath = value).build());
		lightning.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.lightning_creative_infinite"), server.lightningCreativeInfinite)
			.setDefaultValue(false).setSaveConsumer(value -> server.lightningCreativeInfinite = value).build());
		lightning.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_lightning_hud"), client.lightningHud)
			.setDefaultValue(true).setSaveConsumer(value -> client.lightningHud = value).build());
		lightning.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.lightning_hud_x"), client.lightningHudX)
			.setDefaultValue(8).setMin(0).setMax(10000).setSaveConsumer(value -> client.lightningHudX = value).build());
		lightning.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.lightning_hud_y"), client.lightningHudY)
			.setDefaultValue(8).setMin(0).setMax(10000).setSaveConsumer(value -> client.lightningHudY = value).build());
		lightning.addEntry(entries.startDoubleField(Text.translatable("config.enhanced-bows.lightning_hud_scale"), client.lightningHudScale)
			.setDefaultValue(0.8).setMin(0.25).setMax(2.0).setSaveConsumer(value -> client.lightningHudScale = value).build());
		lightning.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.lightning_hud_holding_only"), client.lightningHudHoldingOnly)
			.setDefaultValue(true).setSaveConsumer(value -> client.lightningHudHoldingOnly = value).build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("config.enhanced-bows.category.hud"));
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_scan_text_hud"), client.textHud)
			.setDefaultValue(true).setSaveConsumer(value -> client.textHud = value).build());
		hud.addEntry(entries.startIntField(Text.translatable("config.enhanced-bows.scan_text_hud_y"), client.textHudY)
			.setDefaultValue(40).setMin(0).setMax(10000).setSaveConsumer(value -> client.textHudY = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.enable_sounds"), client.sounds)
			.setDefaultValue(true).setSaveConsumer(value -> client.sounds = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.use_custom_scan_start_sound"), client.customScanStart)
			.setDefaultValue(false).setSaveConsumer(value -> client.customScanStart = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.use_custom_detected_sound"), client.customDetected)
			.setDefaultValue(false).setSaveConsumer(value -> client.customDetected = value).build());
		hud.addEntry(entries.startBooleanToggle(Text.translatable("config.enhanced-bows.use_custom_bounce_sound"), client.customBounce)
			.setDefaultValue(false).setSaveConsumer(value -> client.customBounce = value).build());

		builder.setSavingRunnable(() -> {
			ServerScanConfig.setAndSave(server.toValues());
			ClientScanConfig.setAndSave(client.toValues());
		});
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
		private boolean strictLineOfSight;
		private boolean lightningEnabled;
		private int lightningMaxCharges;
		private int lightningRechargeTicks;
		private String lightningDamageMode;
		private boolean lightningKeepAfterDeath;
		private boolean lightningCreativeInfinite;

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
			strictLineOfSight = values.strictScanLineOfSight();
			lightningEnabled = values.enableLightningEnchantment();
			lightningMaxCharges = values.lightningMaxCharges();
			lightningRechargeTicks = values.lightningRechargeTicks();
			lightningDamageMode = values.lightningDamageMode();
			lightningKeepAfterDeath = values.lightningKeepChargeAfterDeath();
			lightningCreativeInfinite = values.lightningAllowCreativeInfinite();
		}

		private ServerScanConfig.Values toValues() {
			return new ServerScanConfig.Values(enabled, radius, duration, interval, glowing,
				scanPlayers, scanOwner, threshold, bounce, maxBounces, damping, minVelocity, lineOfSight,
				strictLineOfSight, lightningEnabled, lightningMaxCharges, lightningRechargeTicks,
				lightningDamageMode, lightningKeepAfterDeath, lightningCreativeInfinite);
		}
	}

	private static final class ClientDraft {
		private boolean sounds;
		private boolean customScanStart;
		private boolean customDetected;
		private boolean customBounce;
		private boolean textHud;
		private int textHudY;
		private boolean redTrail;
		private int redTrailLength;
		private int redTrailParticleCount;
		private double redTrailParticleSize;
		private boolean lightningHud;
		private int lightningHudX;
		private int lightningHudY;
		private double lightningHudScale;
		private boolean lightningHudHoldingOnly;

		private ClientDraft(ClientScanConfig.Values values) {
			sounds = values.enableScanSounds();
			customScanStart = values.useCustomScanStartSound();
			customDetected = values.useCustomDetectedSound();
			customBounce = values.useCustomBounceSound();
			textHud = values.enableScanTextHud();
			textHudY = values.scanTextHudY();
			redTrail = values.enableScanningArrowRedTrail();
			redTrailLength = values.redTrailLength();
			redTrailParticleCount = values.redTrailParticleCount();
			redTrailParticleSize = values.redTrailParticleSize();
			lightningHud = values.enableLightningHud();
			lightningHudX = values.lightningHudX();
			lightningHudY = values.lightningHudY();
			lightningHudScale = values.lightningHudScale();
			lightningHudHoldingOnly = values.showLightningHudOnlyWhenHoldingBow();
		}

		private ClientScanConfig.Values toValues() {
			return new ClientScanConfig.Values(sounds, customScanStart, customDetected,
				customBounce, textHud, textHudY, redTrail, redTrailLength, redTrailParticleCount,
				redTrailParticleSize, lightningHud, lightningHudX, lightningHudY, lightningHudScale,
				lightningHudHoldingOnly);
		}
	}
}
