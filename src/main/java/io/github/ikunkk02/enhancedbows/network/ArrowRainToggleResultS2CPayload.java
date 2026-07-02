package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainToggleRules;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ArrowRainToggleResultS2CPayload(int resultId) implements CustomPayload {
	public static final Id<ArrowRainToggleResultS2CPayload> ID =
		new Id<>(EnhancedBows.id("arrow_rain_toggle_result"));
	public static final PacketCodec<RegistryByteBuf, ArrowRainToggleResultS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT, ArrowRainToggleResultS2CPayload::resultId,
		ArrowRainToggleResultS2CPayload::new
	);

	public ArrowRainToggleResultS2CPayload(ArrowRainToggleRules.ToggleDecision decision) {
		this(decision.ordinal());
	}

	public ArrowRainToggleRules.ToggleDecision decision() {
		ArrowRainToggleRules.ToggleDecision[] values = ArrowRainToggleRules.ToggleDecision.values();
		return resultId >= 0 && resultId < values.length
			? values[resultId]
			: ArrowRainToggleRules.ToggleDecision.REQUIRES_BOW;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
