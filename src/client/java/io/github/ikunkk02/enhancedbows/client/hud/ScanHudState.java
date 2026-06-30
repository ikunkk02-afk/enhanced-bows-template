package io.github.ikunkk02.enhancedbows.client.hud;

/** Owns two fixed two-second text timelines; detected warnings take priority. */
public final class ScanHudState {
	private static final int DISPLAY_TICKS = 40;
	private static int scanningTicksRemaining;
	private static int detectedTicksRemaining;

	private ScanHudState() {
	}

	public static void startScanning() {
		scanningTicksRemaining = DISPLAY_TICKS;
	}

	public static void startDetected() {
		detectedTicksRemaining = DISPLAY_TICKS;
	}

	public static void tick() {
		if (scanningTicksRemaining > 0) {
			scanningTicksRemaining--;
		}
		if (detectedTicksRemaining > 0) {
			detectedTicksRemaining--;
		}
	}

	public static Message currentMessage() {
		if (detectedTicksRemaining > 0) {
			return Message.DETECTED;
		}
		return scanningTicksRemaining > 0 ? Message.SCANNING : null;
	}

	public static void reset() {
		scanningTicksRemaining = 0;
		detectedTicksRemaining = 0;
	}

	public enum Message {
		SCANNING("hud.enhanced-bows.scanning"),
		DETECTED("hud.enhanced-bows.detected");

		private final String translationKey;

		Message(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return translationKey;
		}
	}
}
