package io.github.ikunkk02.enhancedbows.client.hud;

/** Chooses the localized Lightning HUD line and rounds remaining ticks up to seconds. */
public record LightningHudText(String translationKey, int charges, int maxCharges, int remainingSeconds) {
	public static LightningHudText create(int charges, int maxCharges, int remainingTicks) {
		int safeMax = Math.max(1, maxCharges);
		int safeCharges = Math.max(0, Math.min(charges, safeMax));
		int seconds = Math.max(0, (remainingTicks + 19) / 20);
		String key = safeCharges >= safeMax
			? "hud.enhanced-bows.lightning.full"
			: "hud.enhanced-bows.lightning.recharging";
		return new LightningHudText(key, safeCharges, safeMax, seconds);
	}
}
