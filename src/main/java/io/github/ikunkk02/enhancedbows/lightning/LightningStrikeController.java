package io.github.ikunkk02.enhancedbows.lightning;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Creates cosmetic vanilla bolts while applying explicit damage only to selected targets. */
public final class LightningStrikeController {
	private LightningStrikeController() {
	}

	public static void visual(ServerWorld world, Vec3d position) {
		LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
		if (lightning == null) {
			return;
		}
		lightning.refreshPositionAfterTeleport(position.x, position.y, position.z);
		lightning.setCosmetic(true);
		world.spawnEntity(lightning);
	}

	public static void strike(ServerWorld world, LivingEntity target, float bonusDamage, int fireSeconds) {
		visual(world, target.getPos());
		if (target.isAlive() && bonusDamage > 0.0F) {
			target.damage(world.getDamageSources().lightningBolt(), bonusDamage);
		}
		if (target.isAlive() && fireSeconds > 0) {
			target.setOnFireFor(fireSeconds);
		}
	}
}
