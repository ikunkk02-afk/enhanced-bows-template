package io.github.ikunkk02.enhancedbows.lightning;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Ticks short-lived Lightning storms without creating synchronized custom entities. */
public final class LightningStormManager {
	private static final Map<ServerWorld, List<LightningStorm>> STORMS = new IdentityHashMap<>();

	private LightningStormManager() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(LightningStormManager::tickWorld);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> STORMS.clear());
	}

	static void add(LightningStorm storm, ServerWorld world) {
		STORMS.computeIfAbsent(world, ignored -> new ArrayList<>()).add(storm);
	}

	private static void tickWorld(ServerWorld world) {
		List<LightningStorm> storms = STORMS.get(world);
		if (storms == null) {
			return;
		}
		storms.removeIf(LightningStorm::tick);
		if (storms.isEmpty()) {
			STORMS.remove(world);
		}
	}
}
