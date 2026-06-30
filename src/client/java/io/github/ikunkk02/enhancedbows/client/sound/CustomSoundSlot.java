package io.github.ikunkk02.enhancedbows.client.sound;

/** Fixed custom sound slots and their generated resource-pack names. */
public enum CustomSoundSlot {
	SCAN_START("scan_start.ogg", "custom.scan_start"),
	DETECTED("detected.ogg", "custom.detected"),
	BOUNCE("bounce.ogg", "custom.bounce");

	private final String fileName;
	private final String eventPath;

	CustomSoundSlot(String fileName, String eventPath) {
		this.fileName = fileName;
		this.eventPath = eventPath;
	}

	public String fileName() {
		return fileName;
	}

	public String eventPath() {
		return eventPath;
	}
}
