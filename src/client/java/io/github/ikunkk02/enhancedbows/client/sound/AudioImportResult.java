package io.github.ikunkk02.enhancedbows.client.sound;

/** Value returned to the client thread after a background import attempt. */
public record AudioImportResult(Status status, String detail) {
	public static AudioImportResult success() {
		return new AudioImportResult(Status.SUCCESS, "");
	}

	public static AudioImportResult failure(Status status, String detail) {
		return new AudioImportResult(status, detail == null ? "" : detail);
	}

	public boolean successful() {
		return status == Status.SUCCESS;
	}

	public enum Status {
		SUCCESS,
		UNSUPPORTED_FORMAT,
		FFMPEG_MISSING,
		CONVERSION_FAILED,
		CONVERSION_TIMEOUT,
		IO_ERROR
	}
}
