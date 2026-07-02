package io.github.ikunkk02.enhancedbows.arrowrain;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ArrowRainComponents implements EntityComponentInitializer {
	public static final ComponentKey<ArrowRainComponent> ARROW_RAIN =
		ComponentRegistryV3.INSTANCE.getOrCreate(EnhancedBows.id("arrow_rain"), ArrowRainComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(ARROW_RAIN, PlayerArrowRainComponent::new,
			RespawnCopyStrategy.ALWAYS_COPY);
	}
}
