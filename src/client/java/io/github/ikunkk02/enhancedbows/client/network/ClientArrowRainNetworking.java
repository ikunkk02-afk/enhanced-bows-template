package io.github.ikunkk02.enhancedbows.client.network;

import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules;
import io.github.ikunkk02.enhancedbows.network.ArrowRainToggleResultS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class ClientArrowRainNetworking {
	private ClientArrowRainNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(ArrowRainToggleResultS2CPayload.ID,
			(payload, context) -> context.client().execute(() -> {
				var client = context.client();
				if (client.player == null) {
					return;
				}
				ArrowRainToggleRules.ToggleDecision decision = payload.decision();
				client.player.sendMessage(Text.translatable(messageKey(decision)), true);
				if (decision == ArrowRainToggleRules.ToggleDecision.ENABLE
						|| decision == ArrowRainToggleRules.ToggleDecision.DISABLE) {
					client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.0F);
				}
			}));
	}

	private static String messageKey(ArrowRainToggleRules.ToggleDecision decision) {
		return switch (decision) {
			case ENABLE -> "message.enhanced-bows.arrow_rain.enabled";
			case DISABLE -> "message.enhanced-bows.arrow_rain.disabled";
			case BURST_CONFLICT -> "message.enhanced-bows.arrow_rain.burst_conflict";
			case REQUIRES_BOW -> "message.enhanced-bows.arrow_rain.requires_bow";
		};
	}
}
