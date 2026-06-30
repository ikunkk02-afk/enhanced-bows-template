package io.github.ikunkk02.enhancedbows.lightning;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public interface LightningChargeComponent extends ComponentV3, AutoSyncedComponent, ServerTickingComponent {
	int getLightningCharges();

	int getLightningMaxCharges();

	int getLightningRechargeTicks();

	int getLightningRechargeTimeTicks();

	int getRemainingRechargeTicks();

	boolean consumeLightningCharge();

	void resetLightningCharges();
}
