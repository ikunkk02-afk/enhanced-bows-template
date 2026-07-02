package io.github.ikunkk02.enhancedbows.enchantment;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Data-driven compatibility sets shared by runtime definitions and datagen. */
public final class ModEnchantmentTags {
	public static final TagKey<Enchantment> LIGHTNING_BURST = of("exclusive_set/lightning_burst");
	public static final TagKey<Enchantment> BURST_CONFLICTS = of("exclusive_set/burst_conflicts");
	public static final TagKey<Enchantment> ARROW_RAIN_BURST = of("exclusive_set/arrow_rain_burst");

	private ModEnchantmentTags() {
	}

	private static TagKey<Enchantment> of(String path) {
		return TagKey.of(RegistryKeys.ENCHANTMENT, EnhancedBows.id(path));
	}
}
