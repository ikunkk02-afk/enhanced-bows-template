package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Tells the firing client to start its scanning HUD for the authoritative duration. */
public record ScanStartedS2CPayload(int durationTicks) implements CustomPayload {
	public static final Id<ScanStartedS2CPayload> ID = new Id<>(EnhancedBows.id("scan_started"));
	public static final PacketCodec<RegistryByteBuf, ScanStartedS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT, ScanStartedS2CPayload::durationTicks, ScanStartedS2CPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
