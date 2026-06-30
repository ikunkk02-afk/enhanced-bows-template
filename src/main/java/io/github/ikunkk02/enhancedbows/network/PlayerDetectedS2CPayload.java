package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Tells one scanned player to play the detected warning once. */
public record PlayerDetectedS2CPayload() implements CustomPayload {
	public static final Id<PlayerDetectedS2CPayload> ID = new Id<>(EnhancedBows.id("player_detected"));
	public static final PacketCodec<RegistryByteBuf, PlayerDetectedS2CPayload> CODEC =
		PacketCodec.unit(new PlayerDetectedS2CPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
