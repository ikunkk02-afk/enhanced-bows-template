package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponent;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponents;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Registers all common payload codecs without loading any client classes. */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(ToggleArrowRainModeC2SPayload.ID,
			ToggleArrowRainModeC2SPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScanStartedS2CPayload.ID, ScanStartedS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(PlayerDetectedS2CPayload.ID, PlayerDetectedS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScanSoundCueS2CPayload.ID, ScanSoundCueS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ArrowRainToggleResultS2CPayload.ID,
			ArrowRainToggleResultS2CPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ToggleArrowRainModeC2SPayload.ID,
			(payload, context) -> handleArrowRainToggle(context.player()));
	}

	private static void handleArrowRainToggle(net.minecraft.server.network.ServerPlayerEntity player) {
		HeldArrowRainBow held = inspectHeldBows(player.getMainHandStack(), player.getOffHandStack(), player);
		ArrowRainComponent component = ArrowRainComponents.ARROW_RAIN.get(player);
		ArrowRainToggleRules.ToggleDecision decision = ArrowRainToggleRules.decide(
			component.isArrowRainModeEnabled(), held.present(), held.hasBurst());
		if (decision == ArrowRainToggleRules.ToggleDecision.ENABLE
				|| decision == ArrowRainToggleRules.ToggleDecision.DISABLE) {
			component.toggleArrowRainMode();
		}
		ServerPlayNetworking.send(player, new ArrowRainToggleResultS2CPayload(decision));
	}

	private static HeldArrowRainBow inspectHeldBows(ItemStack mainHand, ItemStack offHand,
			net.minecraft.server.network.ServerPlayerEntity player) {
		boolean present = false;
		boolean cleanBowPresent = false;
		for (ItemStack stack : new ItemStack[] {mainHand, offHand}) {
			if (stack.getItem() instanceof BowItem
					&& ModEnchantments.getArrowRainLevel(player.getWorld(), stack) > 0) {
				present = true;
				if (ModEnchantments.getBurstLevel(player.getWorld(), stack) == 0) {
					cleanBowPresent = true;
				}
			}
		}
		return new HeldArrowRainBow(present, present && !cleanBowPresent);
	}

	private record HeldArrowRainBow(boolean present, boolean hasBurst) {
	}
}
