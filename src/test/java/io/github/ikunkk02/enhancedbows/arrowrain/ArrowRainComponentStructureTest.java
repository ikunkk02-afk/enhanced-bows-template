package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainComponentStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");

	@Test
	void componentPersistsSyncsAndSurvivesRespawn() throws IOException {
		String api = read("arrowrain/ArrowRainComponent.java");
		String implementation = read("arrowrain/PlayerArrowRainComponent.java");
		String registry = read("arrowrain/ArrowRainComponents.java");
		String metadata = Files.readString(Path.of("src/main/resources/fabric.mod.json"));

		assertTrue(api.contains("AutoSyncedComponent"));
		assertTrue(api.contains("ServerTickingComponent"));
		assertTrue(implementation.contains("arrowRainModeEnabled"));
		assertTrue(implementation.contains("arrowRainCooldownTicks"));
		assertTrue(implementation.contains("ArrowRainComponents.ARROW_RAIN.sync(player)"));
		assertTrue(implementation.contains("shouldSyncWith(ServerPlayerEntity recipient)"));
		assertTrue(implementation.contains("recipient == player"));
		assertTrue(implementation.contains("TickResult.SECOND_BOUNDARY"));
		assertTrue(implementation.contains("TickResult.COMPLETE"));
		assertTrue(registry.contains("RespawnCopyStrategy.ALWAYS_COPY"));
		assertTrue(metadata.contains("io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponents"));
		assertTrue(metadata.contains("enhanced-bows:arrow_rain"));
	}

	private static String read(String relative) throws IOException {
		return Files.readString(MAIN.resolve(relative));
	}
}
