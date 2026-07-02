package io.github.ikunkk02.enhancedbows.client.datagen;

import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/** Writes the three dynamic enchantment definitions. */
public final class ModEnchantmentProvider extends FabricDynamicRegistryProvider {
	public ModEnchantmentProvider(FabricDataOutput output,
			CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
		RegistryWrapper.Impl<Enchantment> enchantments = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
		entries.add(enchantments, ModEnchantments.LIGHTNING);
		entries.add(enchantments, ModEnchantments.BURST);
		entries.add(enchantments, ModEnchantments.ARROW_RAIN);
	}

	@Override
	public String getName() {
		return "Enhanced Bows enchantments";
	}
}
