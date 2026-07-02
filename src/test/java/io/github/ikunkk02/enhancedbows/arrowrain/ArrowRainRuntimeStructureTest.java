package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainRuntimeStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");

	@Test
	void managerSpawnsBoundedOrdinaryNonPickupChildArrowsInServerWaves() throws IOException {
		String manager = read("arrowrain/ArrowRainManager.java");
		String initializer = read("EnhancedBows.java");

		assertTrue(manager.contains("ServerTickEvents.END_WORLD_TICK"));
		assertTrue(manager.contains("Map<ServerWorld, List<ActiveRain>>"));
		assertTrue(manager.contains("new ArrowEntity"));
		assertTrue(manager.contains("Items.ARROW.getDefaultStack()"));
		assertTrue(manager.contains("(ItemStack) null"));
		assertTrue(manager.contains("PickupPermission.DISALLOWED"));
		assertTrue(manager.contains("setCritical(false)"));
		assertTrue(manager.contains("setDamage"));
		assertTrue(manager.contains("enhancedBows$markArrowRainChild(100)"));
		assertTrue(manager.contains("world.spawnEntity"));
		assertTrue(initializer.contains("ArrowRainManager.register()"));
	}

	@Test
	void impactMarksOnceConsumesOwnerCooldownAndRunsBesideLightning() throws IOException {
		String controller = read("arrowrain/ArrowRainImpactController.java");
		String mixin = read("mixin/PersistentProjectileEntityMixin.java");

		assertTrue(controller.contains("enhancedBows$markArrowRainTriggered()"));
		assertTrue(controller.contains("config.arrowRainTriggerOnEntityHit()"));
		assertTrue(controller.contains("config.arrowRainTriggerOnBlockHit()"));
		assertTrue(controller.contains("ArrowRainArrowRules.canCreateRain"));
		assertTrue(controller.indexOf("ArrowRainArrowRules.canCreateRain")
			< controller.indexOf("enhancedBows$markArrowRainTriggered()"));
		assertTrue(controller.indexOf("enhancedBows$markArrowRainTriggered()")
			< controller.indexOf("tryStartArrowRainCooldown"));
		assertTrue(controller.contains("ArrowRainManager.add"));
		assertTrue(mixin.contains("ArrowRainImpactController.triggerEntityImpact"));
		assertTrue(mixin.contains("ArrowRainImpactController.triggerBlockImpact"));
		assertTrue(mixin.indexOf("LightningImpactController.triggerEntityImpact")
			< mixin.indexOf("ArrowRainImpactController.triggerEntityImpact"));
	}

	private static String read(String relative) throws IOException {
		return Files.readString(MAIN.resolve(relative));
	}
}
