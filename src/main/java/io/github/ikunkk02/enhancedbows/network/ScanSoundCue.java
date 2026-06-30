package io.github.ikunkk02.enhancedbows.network;

/** Stable sound cue IDs shared by the common packet and client sound resolver. */
public enum ScanSoundCue {
	SCAN_START(0),
	DETECTED(1),
	BOUNCE(2),
	MARK_SUCCESS(3);

	private final int wireId;

	ScanSoundCue(int wireId) {
		this.wireId = wireId;
	}

	public int wireId() {
		return wireId;
	}

	public static ScanSoundCue fromWireId(int wireId) {
		for (ScanSoundCue cue : values()) {
			if (cue.wireId == wireId) {
				return cue;
			}
		}
		return null;
	}
}
