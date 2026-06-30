package io.github.ikunkk02.enhancedbows.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScanSoundCueTest {
	@Test
	void wireIdsRemainStableAndInvalidIdsAreIgnored() {
		assertEquals(0, ScanSoundCue.SCAN_START.wireId());
		assertEquals(1, ScanSoundCue.DETECTED.wireId());
		assertEquals(2, ScanSoundCue.BOUNCE.wireId());
		assertEquals(3, ScanSoundCue.MARK_SUCCESS.wireId());

		assertEquals(ScanSoundCue.SCAN_START, ScanSoundCue.fromWireId(0));
		assertEquals(ScanSoundCue.DETECTED, ScanSoundCue.fromWireId(1));
		assertEquals(ScanSoundCue.BOUNCE, ScanSoundCue.fromWireId(2));
		assertEquals(ScanSoundCue.MARK_SUCCESS, ScanSoundCue.fromWireId(3));
		assertNull(ScanSoundCue.fromWireId(-1));
		assertNull(ScanSoundCue.fromWireId(999));
	}
}
