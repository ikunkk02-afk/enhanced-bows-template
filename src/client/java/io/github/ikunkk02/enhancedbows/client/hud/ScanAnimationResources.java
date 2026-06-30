package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Discovers contiguous animation sequences and degrades safely when frames are absent. */
public final class ScanAnimationResources implements SimpleSynchronousResourceReloadListener {
	public static final ScanAnimationResources INSTANCE = new ScanAnimationResources();
	public static final int FRAME_WIDTH = 640;
	public static final int FRAME_HEIGHT = 360;
	private static final Pattern FRAME_NAME = Pattern.compile(".*/frame_(\\d{3})\\.png");

	private volatile List<Identifier> scanningFrames = List.of();
	private volatile List<Identifier> detectedFrames = List.of();

	private ScanAnimationResources() {
	}

	@Override
	public Identifier getFabricId() {
		return EnhancedBows.id("scan_animation_frames");
	}

	@Override
	public void reload(ResourceManager manager) {
		scanningFrames = findContiguous(manager, "scanning");
		detectedFrames = findContiguous(manager, "detected");
	}

	public List<Identifier> scanningFrames() {
		return scanningFrames;
	}

	public List<Identifier> detectedFrames() {
		return detectedFrames;
	}

	private static List<Identifier> findContiguous(ResourceManager manager, String animation) {
		String prefix = "textures/gui/" + animation;
		Map<Identifier, ?> resources = manager.findResources(prefix, id -> FRAME_NAME.matcher(id.getPath()).matches());
		List<Identifier> sorted = new ArrayList<>(resources.keySet());
		sorted.sort(Comparator.comparingInt(ScanAnimationResources::frameNumber));

		if (sorted.isEmpty() || frameNumber(sorted.getFirst()) != 0) {
			EnhancedBows.LOGGER.warn("Missing HUD animation frames under assets/{}/{}", EnhancedBows.MOD_ID, prefix);
			return List.of();
		}

		List<Identifier> contiguous = new ArrayList<>();
		for (int expected = 0; expected < sorted.size(); expected++) {
			Identifier frame = sorted.get(expected);
			if (frameNumber(frame) != expected) {
				EnhancedBows.LOGGER.warn("HUD animation {} is missing frame_{}, using its contiguous prefix",
					animation, String.format("%03d", expected));
				break;
			}
			contiguous.add(frame);
		}
		return List.copyOf(contiguous);
	}

	private static int frameNumber(Identifier id) {
		Matcher matcher = FRAME_NAME.matcher(id.getPath());
		return matcher.matches() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
	}
}
