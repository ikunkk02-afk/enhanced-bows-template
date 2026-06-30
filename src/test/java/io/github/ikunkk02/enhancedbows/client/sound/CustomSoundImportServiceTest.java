package io.github.ikunkk02.enhancedbows.client.sound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSoundImportServiceTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void acceptsOnlyOggMp3AndWavCaseInsensitively() {
		assertTrue(CustomSoundImportService.supports(Path.of("voice.ogg")));
		assertTrue(CustomSoundImportService.supports(Path.of("voice.MP3")));
		assertTrue(CustomSoundImportService.supports(Path.of("voice.WaV")));
		assertFalse(CustomSoundImportService.supports(Path.of("voice.flac")));
		assertFalse(CustomSoundImportService.supports(Path.of("voice")));
	}

	@Test
	void oggIsCopiedToBackupAndGeneratedResourcePack() throws IOException {
		Path input = temporaryDirectory.resolve("source.ogg");
		byte[] audio = {79, 103, 103, 83, 1, 2, 3};
		Files.write(input, audio);
		CustomSoundImportService service = service((command, timeout) -> {
			throw new AssertionError("OGG import must not run FFmpeg");
		});

		AudioImportResult result = service.importFile(CustomSoundSlot.SCAN_START, input);

		assertEquals(AudioImportResult.Status.SUCCESS, result.status());
		assertArrayEquals(audio, Files.readAllBytes(configSound("scan_start.ogg")));
		assertArrayEquals(audio, Files.readAllBytes(packSound("scan_start.ogg")));
		assertTrue(Files.readString(packRoot().resolve("pack.mcmeta")).contains("\"pack_format\": 34"));
		String soundsJson = Files.readString(packRoot().resolve("assets/enhanced_bows/sounds.json"));
		assertTrue(soundsJson.contains("\"custom.scan_start\""));
		assertTrue(soundsJson.contains("enhanced_bows:custom/bounce"));
	}

	@Test
	void mp3AndWavUseRequiredMonoVorbisCommandAndFixedNames() throws IOException {
		for (CustomSoundSlot slot : List.of(CustomSoundSlot.DETECTED, CustomSoundSlot.BOUNCE)) {
			String extension = slot == CustomSoundSlot.DETECTED ? ".mp3" : ".wav";
			Path input = temporaryDirectory.resolve("input" + extension);
			Files.write(input, new byte[] {1});
			List<List<String>> commands = new ArrayList<>();
			CustomSoundImportService service = service((command, timeout) -> {
				commands.add(List.copyOf(command));
				if (command.size() == 2 && command.get(1).equals("-version")) {
					return new CustomSoundImportService.ProcessResult(0, false, "ffmpeg version test");
				}
				Files.write(Path.of(command.getLast()), new byte[] {79, 103, 103, 83, 9});
				return new CustomSoundImportService.ProcessResult(0, false, "converted");
			});

			AudioImportResult result = service.importFile(slot, input);

			assertEquals(AudioImportResult.Status.SUCCESS, result.status());
			List<String> conversion = commands.getLast();
			assertEquals("ffmpeg", conversion.getFirst());
			assertTrue(conversion.containsAll(List.of("-y", "-i", "-ac", "1", "-ar", "44100",
				"-c:a", "libvorbis", "-q:a", "4")));
			assertTrue(Files.exists(packSound(slot.fileName())));
			assertTrue(Files.exists(configSound(slot.fileName())));
		}
	}

	@Test
	void localWindowsFfmpegHasPriorityOverPathProbe() throws IOException {
		Path input = temporaryDirectory.resolve("input.mp3");
		Files.write(input, new byte[] {1});
		Path localFfmpeg = configRoot().resolve("enhanced_bows/tools/ffmpeg.exe");
		Files.createDirectories(localFfmpeg.getParent());
		Files.write(localFfmpeg, new byte[] {1});
		List<List<String>> commands = new ArrayList<>();
		CustomSoundImportService service = service((command, timeout) -> {
			commands.add(List.copyOf(command));
			Files.write(Path.of(command.getLast()), new byte[] {79, 103, 103, 83});
			return new CustomSoundImportService.ProcessResult(0, false, "converted");
		});

		assertEquals(AudioImportResult.Status.SUCCESS,
			service.importFile(CustomSoundSlot.SCAN_START, input).status());
		assertEquals(localFfmpeg.toAbsolutePath().toString(), commands.getFirst().getFirst());
		assertEquals(1, commands.size());
	}

	@Test
	void missingFfmpegDoesNotOverwriteExistingSound() throws IOException {
		Path input = temporaryDirectory.resolve("input.mp3");
		Files.write(input, new byte[] {1});
		Files.createDirectories(packSound("detected.ogg").getParent());
		byte[] existing = {79, 103, 103, 83, 7};
		Files.write(packSound("detected.ogg"), existing);
		CustomSoundImportService service = service((command, timeout) -> {
			throw new IOException("Cannot run program ffmpeg");
		});

		AudioImportResult result = service.importFile(CustomSoundSlot.DETECTED, input);

		assertEquals(AudioImportResult.Status.FFMPEG_MISSING, result.status());
		assertArrayEquals(existing, Files.readAllBytes(packSound("detected.ogg")));
	}

	@Test
	void conversionFailureAndTimeoutDoNotOverwriteExistingSound() throws IOException {
		for (boolean timedOut : List.of(false, true)) {
			Path input = temporaryDirectory.resolve("input-" + timedOut + ".wav");
			Files.write(input, new byte[] {1});
			Files.createDirectories(packSound("bounce.ogg").getParent());
			byte[] existing = {79, 103, 103, 83, 8};
			Files.write(packSound("bounce.ogg"), existing);
			CustomSoundImportService service = service((command, timeout) -> {
				if (command.size() == 2) {
					return new CustomSoundImportService.ProcessResult(0, false, "ffmpeg version test");
				}
				return new CustomSoundImportService.ProcessResult(timedOut ? -1 : 2, timedOut, "failed");
			});

			AudioImportResult result = service.importFile(CustomSoundSlot.BOUNCE, input);

			assertEquals(timedOut ? AudioImportResult.Status.CONVERSION_TIMEOUT
				: AudioImportResult.Status.CONVERSION_FAILED, result.status());
			assertArrayEquals(existing, Files.readAllBytes(packSound("bounce.ogg")));
		}
	}

	private CustomSoundImportService service(CustomSoundImportService.ProcessRunner runner) {
		return new CustomSoundImportService(temporaryDirectory, configRoot(), runner);
	}

	private Path configRoot() {
		return temporaryDirectory.resolve("config");
	}

	private Path configSound(String fileName) {
		return configRoot().resolve("enhanced_bows/custom_sounds").resolve(fileName);
	}

	private Path packRoot() {
		return temporaryDirectory.resolve("resourcepacks/EnhancedBows Custom Sounds");
	}

	private Path packSound(String fileName) {
		return packRoot().resolve("assets/enhanced_bows/sounds/custom").resolve(fileName);
	}
}
