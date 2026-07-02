package io.github.ikunkk02.enhancedbows.arrowrain;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public interface ArrowRainComponent extends ComponentV3, AutoSyncedComponent, ServerTickingComponent {
	boolean isArrowRainModeEnabled();

	int getArrowRainCooldownTicks();

	boolean toggleArrowRainMode();

	boolean tryStartArrowRainCooldown(int configuredTicks);
}
