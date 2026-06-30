package io.github.ikunkk02.enhancedbows.lightning;

/** Pure gates for arming and consuming a Lightning projectile. */
public final class LightningArrowRules {
	private LightningArrowRules() {
	}

	public static boolean shouldArm(boolean enabled, boolean bow, int enchantmentLevel) {
		return enabled && bow && enchantmentLevel > 0;
	}

	public static boolean shouldTrigger(boolean armed, boolean triggered, boolean livingTarget,
			boolean targetAlive, boolean playerOwner, boolean hasCharge, boolean creativeInfinite) {
		return armed && !triggered && livingTarget && targetAlive && playerOwner && (hasCharge || creativeInfinite);
	}

	public static boolean shouldConsumeCharge(boolean creative, boolean allowCreativeInfinite) {
		return !creative || !allowCreativeInfinite;
	}
}
