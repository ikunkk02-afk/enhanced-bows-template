package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Tells the firing client to show its fixed two-second scanning text. */
public record ScanStartedS2CPayload() implements CustomPayload {
	public static final Id<ScanStartedS2CPayload> ID = new Id<>(EnhancedBows.id("scan_started"));
	public static final PacketCodec<RegistryByteBuf, ScanStartedS2CPayload> CODEC =
		PacketCodec.unit(new ScanStartedS2CPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
