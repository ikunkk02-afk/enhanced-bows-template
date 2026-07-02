package io.github.ikunkk02.enhancedbows.client.datagen;

import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantmentTags;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

/** Generates vanilla source tags and the exact release compatibility matrix. */
public final class ModEnchantmentTagProvider extends FabricTagProvider.EnchantmentTagProvider {
	public ModEnchantmentTagProvider(FabricDataOutput output,
			CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
		getOrCreateTagBuilder(EnchantmentTags.IN_ENCHANTING_TABLE)
			.add(ModEnchantments.LIGHTNING, ModEnchantments.BURST, ModEnchantments.ARROW_RAIN);
		getOrCreateTagBuilder(EnchantmentTags.NON_TREASURE)
			.add(ModEnchantments.LIGHTNING, ModEnchantments.BURST, ModEnchantments.ARROW_RAIN);
		getOrCreateTagBuilder(EnchantmentTags.ON_RANDOM_LOOT)
			.add(ModEnchantments.LIGHTNING, ModEnchantments.BURST, ModEnchantments.ARROW_RAIN);
		getOrCreateTagBuilder(EnchantmentTags.TRADEABLE)
			.add(ModEnchantments.LIGHTNING, ModEnchantments.BURST, ModEnchantments.ARROW_RAIN);

		getOrCreateTagBuilder(ModEnchantmentTags.LIGHTNING_BURST)
			.add(ModEnchantments.LIGHTNING, ModEnchantments.BURST);
		getOrCreateTagBuilder(ModEnchantmentTags.BURST_CONFLICTS)
			.add(ModEnchantments.BURST, ModEnchantments.LIGHTNING, ModEnchantments.ARROW_RAIN);
		getOrCreateTagBuilder(ModEnchantmentTags.ARROW_RAIN_BURST)
			.add(ModEnchantments.ARROW_RAIN, ModEnchantments.BURST);
	}
}
