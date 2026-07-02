package io.github.ikunkk02.enhancedbows.enchantment;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Optional;

/** Registry key and lookup helpers for the data-driven Lightning enchantment. */
public final class ModEnchantments {
	public static final RegistryKey<Enchantment> LIGHTNING = RegistryKey.of(
		RegistryKeys.ENCHANTMENT, EnhancedBows.id("lightning"));
	public static final RegistryKey<Enchantment> BURST = RegistryKey.of(
		RegistryKeys.ENCHANTMENT, EnhancedBows.id("burst"));
	public static final RegistryKey<Enchantment> ARROW_RAIN = RegistryKey.of(
		RegistryKeys.ENCHANTMENT, EnhancedBows.id("arrow_rain"));

	private ModEnchantments() {
	}

	/** Bootstraps the three data-driven release enchantments for Fabric datagen. */
	public static void bootstrap(Registerable<Enchantment> registerable) {
		RegistryEntryLookup<net.minecraft.item.Item> items = registerable.getRegistryLookup(RegistryKeys.ITEM);
		RegistryEntryLookup<Enchantment> enchantments = registerable.getRegistryLookup(RegistryKeys.ENCHANTMENT);
		var bows = items.getOrThrow(ItemTags.BOW_ENCHANTABLE);

		register(registerable, LIGHTNING, Enchantment.builder(Enchantment.definition(
			bows, bows, 5, 1, Enchantment.constantCost(18), Enchantment.constantCost(50), 4,
			AttributeModifierSlot.MAINHAND, AttributeModifierSlot.OFFHAND))
			.exclusiveSet(enchantments.getOrThrow(ModEnchantmentTags.LIGHTNING_BURST)));
		register(registerable, BURST, Enchantment.builder(Enchantment.definition(
			bows, bows, 5, 1, Enchantment.constantCost(15), Enchantment.constantCost(45), 4,
			AttributeModifierSlot.MAINHAND, AttributeModifierSlot.OFFHAND))
			.exclusiveSet(enchantments.getOrThrow(ModEnchantmentTags.BURST_CONFLICTS)));
		register(registerable, ARROW_RAIN, Enchantment.builder(Enchantment.definition(
			bows, bows, 5, 1, Enchantment.constantCost(16), Enchantment.constantCost(46), 4,
			AttributeModifierSlot.MAINHAND, AttributeModifierSlot.OFFHAND))
			.exclusiveSet(enchantments.getOrThrow(ModEnchantmentTags.ARROW_RAIN_BURST)));
	}

	private static void register(Registerable<Enchantment> registerable,
			RegistryKey<Enchantment> key, Enchantment.Builder builder) {
		Enchantment built = builder.build(key.getValue());
		registerable.register(key, new Enchantment(
			Text.translatable("enchantment.enhanced_bows." + key.getValue().getPath()),
			built.definition(), built.exclusiveSet(), built.effects()));
	}

	public static int getLightningLevel(World world, ItemStack stack) {
		return getLevel(world, stack, LIGHTNING);
	}

	public static int getBurstLevel(World world, ItemStack stack) {
		return getLevel(world, stack, BURST);
	}

	public static int getArrowRainLevel(World world, ItemStack stack) {
		return getLevel(world, stack, ARROW_RAIN);
	}

	private static int getLevel(World world, ItemStack stack, RegistryKey<Enchantment> key) {
		Optional<Registry<Enchantment>> registry = world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT);
		return registry.flatMap(enchantments -> enchantments.getEntry(key))
			.map(entry -> EnchantmentHelper.getLevel(entry, stack))
			.orElse(0);
	}

	public static void registerCreativeBook() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries ->
			entries.getContext().lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
				registry.getOptional(LIGHTNING).ifPresent(enchantment -> entries.addAfter(Items.BOW,
					EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, 1))));
				registry.getOptional(BURST).ifPresent(enchantment -> entries.addAfter(Items.BOW,
					EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, 1))));
				registry.getOptional(ARROW_RAIN).ifPresent(enchantment -> entries.addAfter(Items.BOW,
					EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, 1))));
			})
		);
	}
}
