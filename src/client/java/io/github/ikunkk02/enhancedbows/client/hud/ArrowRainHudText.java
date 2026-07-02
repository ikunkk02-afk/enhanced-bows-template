package io.github.ikunkk02.enhancedbows.client.hud;

/** Pure localized HUD state for Arrow Rain mode and cooldown. */
public record ArrowRainHudText(boolean visible, String translationKey, int remainingSeconds) {
	public static ArrowRainHudText create(boolean holdingArrowRainBow,
			boolean modeEnabled, int cooldownTicks) {
		if (!holdingArrowRainBow) {
			return new ArrowRainHudText(false, "", 0);
		}
		if (!modeEnabled) {
			return new ArrowRainHudText(true, "hud.enhanced-bows.arrow_rain.off", 0);
		}
		int seconds = Math.max(0, (cooldownTicks + 19) / 20);
		return seconds > 0
			? new ArrowRainHudText(true, "hud.enhanced-bows.arrow_rain.cooldown", seconds)
			: new ArrowRainHudText(true, "hud.enhanced-bows.arrow_rain.on", 0);
	}
}
