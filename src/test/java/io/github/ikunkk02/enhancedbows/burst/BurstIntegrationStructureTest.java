package io.github.ikunkk02.enhancedbows.burst;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BurstIntegrationStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");

	@Test
	void projectilePersistsBurstStateAndForwardsBothImpactTypes() throws IOException {
		String access = Files.readString(MAIN.resolve("burst/BurstArrowAccess.java"));
		String mixin = Files.readString(MAIN.resolve("mixin/PersistentProjectileEntityMixin.java"));

		assertTrue(access.contains("enhancedBows$armBurst"));
		assertTrue(access.contains("enhancedBows$markBurstTriggered"));
		assertTrue(mixin.contains("implements LightningArrowAccess, BurstArrowAccess"));
		assertTrue(mixin.contains("enhancedbows:burst_arrow"));
		assertTrue(mixin.contains("enhancedbows:burst_triggered"));
		assertTrue(mixin.contains("enhancedbows:burst_owner_uuid"));
		assertTrue(mixin.contains("BurstExplosionController.triggerEntityImpact"));
		assertTrue(mixin.contains("BurstExplosionController.triggerBlockImpact"));
	}

	@Test
	void spawnHookExcludesSpectralArrowsAndMakesLightningWinIllegalCombinations() throws IOException {
		String ranged = Files.readString(MAIN.resolve("mixin/RangedWeaponItemMixin.java"));

		assertTrue(ranged.contains("entity instanceof SpectralArrowEntity"));
		assertTrue(ranged.contains("BurstArrowRules.decideArming"));
		assertTrue(ranged.contains("Bow has both Lightning and Burst enchantments. "
			+ "This is not allowed. Lightning takes priority."));
		assertTrue(ranged.contains("ArmDecision.LIGHTNING"));
		assertTrue(ranged.contains("ArmDecision.BURST"));
	}

	@Test
	void explosionUsesVanillaPresentationWithoutVanillaEntityEffects() throws IOException {
		String controller = Files.readString(MAIN.resolve("burst/BurstExplosionController.java"));

		assertTrue(controller.contains("new ExplosionBehavior()"));
		assertTrue(controller.contains("boolean shouldDamage"));
		assertTrue(controller.contains("return false"));
		assertTrue(controller.contains("float getKnockbackModifier"));
		assertTrue(controller.contains("createExplosion"));
		assertTrue(controller.contains("getDamageSources().explosion"));
		assertTrue(controller.contains("BurstDamageRules.damageAtDistance"));
	}
}
