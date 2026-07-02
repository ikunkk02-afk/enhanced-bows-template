package io.github.ikunkk02.enhancedbows.burst;

import java.util.UUID;

public interface BurstArrowAccess {
	void enhancedBows$armBurst(UUID ownerUuid);

	boolean enhancedBows$isBurstArmed();

	boolean enhancedBows$hasBurstTriggered();

	UUID enhancedBows$getBurstOwnerUuid();

	void enhancedBows$markBurstTriggered();
}
