package io.github.ikunkk02.enhancedbows.lightning;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LightningStormIntegrationStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");

	@Test
	void projectileHandlesEntityAndFinalBlockImpactsThroughSharedController() throws IOException {
		String mixin = Files.readString(MAIN.resolve("mixin/PersistentProjectileEntityMixin.java"));

		assertTrue(mixin.contains("method = \"onEntityHit\""));
		assertTrue(mixin.contains("method = \"onBlockHit\""));
		assertTrue(mixin.contains("LightningImpactController.triggerEntityImpact"));
		assertTrue(mixin.contains("LightningImpactController.triggerBlockImpact"));
		assertTrue(mixin.indexOf("enhancedBows$bounceScanningArrow")
			< mixin.indexOf("enhancedBows$triggerBlockLightning"));

		String impact = Files.readString(MAIN.resolve("lightning/LightningImpactController.java"));
		assertFalse(impact.contains("primaryTarget != null && primaryTarget.isAlive()"));
		assertTrue(impact.contains("if (primaryTarget != null)"));
	}

	@Test
	void serverRegistersStormTicksAndExplicitLightningDamage() throws IOException {
		String initializer = Files.readString(MAIN.resolve("EnhancedBows.java"));
		String manager = Files.readString(MAIN.resolve("lightning/LightningStormManager.java"));
		String strikes = Files.readString(MAIN.resolve("lightning/LightningStrikeController.java"));

		assertTrue(initializer.contains("LightningStormManager.register()"));
		assertTrue(manager.contains("ServerTickEvents.END_WORLD_TICK"));
		assertTrue(strikes.contains("getDamageSources().lightningBolt()"));
		assertTrue(strikes.contains("setOnFireFor"));
		assertTrue(strikes.contains("setCosmetic(true)"));
	}
}
