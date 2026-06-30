package io.github.ikunkk02.enhancedbows.lightning;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class LightningComponents implements EntityComponentInitializer {
	public static final ComponentKey<LightningChargeComponent> LIGHTNING_CHARGE =
		ComponentRegistryV3.INSTANCE.getOrCreate(EnhancedBows.id("lightning_charge"), LightningChargeComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(LIGHTNING_CHARGE, PlayerLightningChargeComponent::new,
			RespawnCopyStrategy.ALWAYS_COPY);
	}
}
