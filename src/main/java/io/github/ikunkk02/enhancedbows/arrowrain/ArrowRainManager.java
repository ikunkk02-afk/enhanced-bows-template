package io.github.ikunkk02.enhancedbows.arrowrain;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ticks bounded Arrow Rain wave schedules and spawns ordinary server-owned arrows. */
public final class ArrowRainManager {
	private static final Map<ServerWorld, List<ActiveRain>> RAINS = new IdentityHashMap<>();

	private ArrowRainManager() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(ArrowRainManager::tickWorld);
	}

	static void add(ServerWorld world, Vec3d center, UUID ownerUuid, double sourceDamage,
			ServerScanConfig.Values config) {
		if (config.arrowRainArrowCount() <= 0 || config.arrowRainWaves() <= 0) {
			return;
		}
		RAINS.computeIfAbsent(world, ignored -> new ArrayList<>())
			.add(new ActiveRain(center, ownerUuid, sourceDamage, config));
	}

	private static void tickWorld(ServerWorld world) {
		List<ActiveRain> rains = RAINS.get(world);
		if (rains == null) {
			return;
		}
		rains.removeIf(rain -> rain.tick(world));
		if (rains.isEmpty()) {
			RAINS.remove(world);
		}
	}

	private static final class ActiveRain {
		private final Vec3d center;
		private final UUID ownerUuid;
		private final double sourceDamage;
		private final ServerScanConfig.Values config;
		private final ArrowRainSchedule schedule;

		private ActiveRain(Vec3d center, UUID ownerUuid, double sourceDamage,
				ServerScanConfig.Values config) {
			this.center = center;
			this.ownerUuid = ownerUuid;
			this.sourceDamage = sourceDamage;
			this.config = config;
			this.schedule = new ArrowRainSchedule(config.arrowRainArrowCount(),
				config.arrowRainWaves(), config.arrowRainDurationTicks());
		}

		private boolean tick(ServerWorld world) {
			int count = schedule.tick();
			for (int i = 0; i < count; i++) {
				spawnArrow(world);
			}
			return schedule.isComplete();
		}

		private void spawnArrow(ServerWorld world) {
			ArrowRainSpawnRules.SpawnOffset offset = ArrowRainSpawnRules.sampleOffset(
				world.random.nextDouble(), world.random.nextDouble(),
				config.arrowRainRadius(), config.arrowRainHeight());
			ArrowEntity arrow = new ArrowEntity(world,
				center.x + offset.x(), center.y + offset.y(), center.z + offset.z(),
				Items.ARROW.getDefaultStack(), (ItemStack) null);
			ServerPlayerEntity owner = world.getServer().getPlayerManager().getPlayer(ownerUuid);
			if (owner != null) {
				arrow.setOwner(owner);
			}
			arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
			arrow.setCritical(false);
			arrow.setDamage(ArrowRainSpawnRules.scaledDamage(sourceDamage,
				config.arrowRainDamageMultiplier()));
			arrow.setVelocity((world.random.nextDouble() - 0.5) * 0.08,
				ArrowRainSpawnRules.downwardSpeed(world.random.nextDouble()),
				(world.random.nextDouble() - 0.5) * 0.08);
			if (arrow instanceof ArrowRainArrowAccess rainArrow) {
				rainArrow.enhancedBows$markArrowRainChild(100);
			}
			world.spawnEntity(arrow);
		}
	}
}
