package io.github.ikunkk02.enhancedbows.client.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Client-only file and FFmpeg service. No work is performed on the Minecraft render thread. */
public final class CustomSoundImportService {
	public static final String PACK_NAME = "EnhancedBows Custom Sounds";
	private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration CONVERSION_TIMEOUT = Duration.ofSeconds(120);
	private static final ExecutorService IMPORT_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
		Thread thread = new Thread(task, "enhanced-bows-sound-import");
		thread.setDaemon(true);
		return thread;
	});
	private static final String PACK_META = """
		{
		  "pack": {
		    "pack_format": 34,
		    "description": "EnhancedBows Custom Sounds"
		  }
		}
		""";
	private static final String SOUNDS_JSON = """
		{
		  "custom.scan_start": {"sounds": ["enhanced_bows:custom/scan_start"]},
		  "custom.detected": {"sounds": ["enhanced_bows:custom/detected"]},
		  "custom.bounce": {"sounds": ["enhanced_bows:custom/bounce"]}
		}
		""";

	private final Path gameDirectory;
	private final Path configDirectory;
	private final ProcessRunner processRunner;

	public CustomSoundImportService(Path gameDirectory, Path configDirectory) {
		this(gameDirectory, configDirectory, CustomSoundImportService::runProcess);
	}

	CustomSoundImportService(Path gameDirectory, Path configDirectory, ProcessRunner processRunner) {
		this.gameDirectory = gameDirectory.toAbsolutePath().normalize();
		this.configDirectory = configDirectory.toAbsolutePath().normalize();
		this.processRunner = processRunner;
	}

	public CompletableFuture<AudioImportResult> importAsync(CustomSoundSlot slot, Path input) {
		return CompletableFuture.supplyAsync(() -> importFile(slot, input), IMPORT_EXECUTOR);
	}

	AudioImportResult importFile(CustomSoundSlot slot, Path input) {
		if (!supports(input)) {
			return AudioImportResult.failure(AudioImportResult.Status.UNSUPPORTED_FORMAT, input.toString());
		}
		if (!Files.isRegularFile(input)) {
			return AudioImportResult.failure(AudioImportResult.Status.IO_ERROR, "Input file does not exist");
		}

		Path packRoot = gameDirectory.resolve("resourcepacks").resolve(PACK_NAME);
		Path packSoundDirectory = packRoot.resolve("assets/enhanced_bows/sounds/custom");
		Path packTarget = packSoundDirectory.resolve(slot.fileName());
		Path packTemporary = packSoundDirectory.resolve(slot.fileName() + ".import.ogg");
		Path backupTarget = configDirectory.resolve("enhanced_bows/custom_sounds").resolve(slot.fileName());
		Path backupTemporary = backupTarget.resolveSibling(slot.fileName() + ".import.ogg");

		try {
			Files.createDirectories(packSoundDirectory);
			Files.createDirectories(backupTarget.getParent());
			Files.deleteIfExists(packTemporary);
			Files.deleteIfExists(backupTemporary);

			String extension = extension(input);
			if (extension.equals(".ogg")) {
				Files.copy(input, packTemporary, StandardCopyOption.REPLACE_EXISTING);
			} else {
				String ffmpeg = locateFfmpeg();
				if (ffmpeg == null) {
					return AudioImportResult.failure(AudioImportResult.Status.FFMPEG_MISSING, "ffmpeg");
				}
				ProcessResult conversion = processRunner.run(conversionCommand(ffmpeg, input, packTemporary),
					CONVERSION_TIMEOUT);
				if (conversion.timedOut()) {
					return AudioImportResult.failure(AudioImportResult.Status.CONVERSION_TIMEOUT, conversion.output());
				}
				if (conversion.exitCode() != 0) {
					return AudioImportResult.failure(AudioImportResult.Status.CONVERSION_FAILED, conversion.output());
				}
			}

			if (!Files.isRegularFile(packTemporary) || Files.size(packTemporary) == 0L) {
				return AudioImportResult.failure(AudioImportResult.Status.CONVERSION_FAILED,
					"FFmpeg did not create a non-empty OGG file");
			}

			Files.copy(packTemporary, backupTemporary, StandardCopyOption.REPLACE_EXISTING);
			writePackMetadata(packRoot);
			replacePair(packTemporary, packTarget, backupTemporary, backupTarget);
			return AudioImportResult.success();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return AudioImportResult.failure(AudioImportResult.Status.IO_ERROR, "Audio import interrupted");
		} catch (IOException exception) {
			return AudioImportResult.failure(AudioImportResult.Status.IO_ERROR, exception.getMessage());
		} finally {
			deleteQuietly(packTemporary);
			deleteQuietly(backupTemporary);
		}
	}

	public static boolean supports(Path input) {
		String extension = extension(input);
		return extension.equals(".ogg") || extension.equals(".mp3") || extension.equals(".wav");
	}

	static List<String> conversionCommand(String ffmpeg, Path input, Path output) {
		return List.of(ffmpeg, "-y", "-i", input.toAbsolutePath().toString(), "-ac", "1", "-ar", "44100",
			"-c:a", "libvorbis", "-q:a", "4", output.toAbsolutePath().toString());
	}

	private String locateFfmpeg() throws InterruptedException {
		Path local = configDirectory.resolve("enhanced_bows/tools/ffmpeg.exe");
		if (Files.isRegularFile(local)) {
			return local.toAbsolutePath().toString();
		}

		try {
			ProcessResult probe = processRunner.run(List.of("ffmpeg", "-version"), PROBE_TIMEOUT);
			return !probe.timedOut() && probe.exitCode() == 0 ? "ffmpeg" : null;
		} catch (IOException exception) {
			return null;
		}
	}

	private static void writePackMetadata(Path packRoot) throws IOException {
		Files.createDirectories(packRoot);
		writeAtomically(packRoot.resolve("pack.mcmeta"), PACK_META);
		writeAtomically(packRoot.resolve("assets/enhanced_bows/sounds.json"), SOUNDS_JSON);
	}

	private static void writeAtomically(Path target, String content) throws IOException {
		Files.createDirectories(target.getParent());
		Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
		Files.writeString(temporary, content, StandardCharsets.UTF_8);
		moveReplacing(temporary, target);
	}

	private static void replacePair(Path firstTemporary, Path firstTarget,
			Path secondTemporary, Path secondTarget) throws IOException {
		Path firstPrevious = firstTarget.resolveSibling(firstTarget.getFileName() + ".previous");
		Path secondPrevious = secondTarget.resolveSibling(secondTarget.getFileName() + ".previous");
		Files.deleteIfExists(firstPrevious);
		Files.deleteIfExists(secondPrevious);
		boolean firstExisted = Files.exists(firstTarget);
		boolean secondExisted = Files.exists(secondTarget);

		try {
			if (firstExisted) {
				moveReplacing(firstTarget, firstPrevious);
			}
			if (secondExisted) {
				moveReplacing(secondTarget, secondPrevious);
			}
			moveReplacing(firstTemporary, firstTarget);
			moveReplacing(secondTemporary, secondTarget);
		} catch (IOException exception) {
			Files.deleteIfExists(firstTarget);
			Files.deleteIfExists(secondTarget);
			if (firstExisted && Files.exists(firstPrevious)) {
				moveReplacing(firstPrevious, firstTarget);
			}
			if (secondExisted && Files.exists(secondPrevious)) {
				moveReplacing(secondPrevious, secondTarget);
			}
			throw exception;
		} finally {
			deleteQuietly(firstPrevious);
			deleteQuietly(secondPrevious);
		}
	}

	private static void moveReplacing(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static String extension(Path input) {
		String name = input.getFileName() == null ? "" : input.getFileName().toString();
		int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot).toLowerCase(Locale.ROOT);
	}

	private static void deleteQuietly(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException ignored) {
			// A stale temporary file is harmless and will be replaced on the next import.
		}
	}

	private static ProcessResult runProcess(List<String> command, Duration timeout)
			throws IOException, InterruptedException {
		Process process = new ProcessBuilder(new ArrayList<>(command)).redirectErrorStream(true).start();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Thread drainer = Thread.ofVirtual().name("enhanced-bows-ffmpeg-output").start(() -> {
			try {
				process.getInputStream().transferTo(output);
			} catch (IOException ignored) {
				// Process exit status remains the authoritative conversion result.
			}
		});

		boolean finished;
		try {
			finished = process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (InterruptedException exception) {
			process.destroyForcibly();
			throw exception;
		}
		if (!finished) {
			process.destroyForcibly();
			process.waitFor();
		}
		drainer.join(2000L);
		return new ProcessResult(finished ? process.exitValue() : -1, !finished,
			output.toString(StandardCharsets.UTF_8));
	}

	@FunctionalInterface
	interface ProcessRunner {
		ProcessResult run(List<String> command, Duration timeout) throws IOException, InterruptedException;
	}

	record ProcessResult(int exitCode, boolean timedOut, String output) {
	}
}
