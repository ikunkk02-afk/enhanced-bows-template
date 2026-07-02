package io.github.ikunkk02.enhancedbows.arrowrain;

import java.util.UUID;

public interface ArrowRainArrowAccess {
	void enhancedBows$armArrowRain(UUID ownerUuid, double baseDamage);

	boolean enhancedBows$isArrowRainArmed();

	boolean enhancedBows$hasArrowRainTriggered();

	void enhancedBows$markArrowRainTriggered();

	UUID enhancedBows$getArrowRainOwnerUuid();

	double enhancedBows$getArrowRainBaseDamage();

	void enhancedBows$markArrowRainChild(int lifetimeTicks);

	boolean enhancedBows$isArrowRainChild();
}
