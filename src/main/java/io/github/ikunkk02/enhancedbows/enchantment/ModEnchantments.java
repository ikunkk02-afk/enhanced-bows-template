package io.github.ikunkk02.enhancedbows.enchantment;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Optional;

/** Registry key and lookup helpers for the data-driven Lightning enchantment. */
public final class ModEnchantments {
	public static final RegistryKey<Enchantment> LIGHTNING = RegistryKey.of(
		RegistryKeys.ENCHANTMENT, EnhancedBows.id("lightning"));

	private ModEnchantments() {
	}

	public static int getLightningLevel(World world, ItemStack stack) {
		Optional<Registry<Enchantment>> registry = world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT);
		return registry.flatMap(enchantments -> enchantments.getEntry(LIGHTNING))
			.map(entry -> EnchantmentHelper.getLevel(entry, stack))
			.orElse(0);
	}

	public static void registerCreativeBook() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries ->
			entries.getContext().lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT)
				.flatMap(registry -> registry.getOptional(LIGHTNING))
				.ifPresent(enchantment -> entries.addAfter(Items.BOW,
					EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, 1))))
		);
	}
}
