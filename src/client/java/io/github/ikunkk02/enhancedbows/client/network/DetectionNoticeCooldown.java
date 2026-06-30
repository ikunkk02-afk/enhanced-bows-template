package io.github.ikunkk02.enhancedbows.client.network;

/** Limits repeated detected HUD and sound notifications without affecting glowing refreshes. */
final class DetectionNoticeCooldown {
	private final long cooldownMillis;
	private long nextAllowedMillis = Long.MIN_VALUE;

	DetectionNoticeCooldown(long cooldownMillis) {
		this.cooldownMillis = Math.max(0L, cooldownMillis);
	}

	boolean tryAcquire(long nowMillis) {
		if (nowMillis < nextAllowedMillis) {
			return false;
		}
		nextAllowedMillis = nowMillis + cooldownMillis;
		return true;
	}
}
