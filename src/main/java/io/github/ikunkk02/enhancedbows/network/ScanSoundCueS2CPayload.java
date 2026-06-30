package io.github.ikunkk02.enhancedbows.network;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Requests a config-controlled scan sound on one client. */
public record ScanSoundCueS2CPayload(int cueId) implements CustomPayload {
	public static final Id<ScanSoundCueS2CPayload> ID = new Id<>(EnhancedBows.id("scan_sound_cue"));
	public static final PacketCodec<RegistryByteBuf, ScanSoundCueS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT, ScanSoundCueS2CPayload::cueId, ScanSoundCueS2CPayload::new
	);

	public ScanSoundCueS2CPayload(ScanSoundCue cue) {
		this(cue.wireId());
	}

	public ScanSoundCue cue() {
		return ScanSoundCue.fromWireId(cueId);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
