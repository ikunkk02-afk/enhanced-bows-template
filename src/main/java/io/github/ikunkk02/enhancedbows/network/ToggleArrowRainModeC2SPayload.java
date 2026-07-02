package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ToggleArrowRainModeC2SPayload() implements CustomPayload {
	public static final Id<ToggleArrowRainModeC2SPayload> ID =
		new Id<>(EnhancedBows.id("toggle_arrow_rain_mode"));
	public static final PacketCodec<RegistryByteBuf, ToggleArrowRainModeC2SPayload> CODEC =
		PacketCodec.unit(new ToggleArrowRainModeC2SPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
