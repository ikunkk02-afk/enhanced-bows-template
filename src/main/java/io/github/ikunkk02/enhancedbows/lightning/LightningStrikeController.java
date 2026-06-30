package io.github.ikunkk02.enhancedbows.lightning;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

/** Creates the vanilla bolt visual while applying lightning damage only to the valid target. */
public final class LightningStrikeController {
	private LightningStrikeController() {
	}

	public static void strike(ServerWorld world, LivingEntity target, String damageMode) {
		LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
		if (lightning == null) {
			return;
		}
		lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
		lightning.setCosmetic(true);
		world.spawnEntity(lightning);
		if ("vanilla_lightning".equals(damageMode) && target.isAlive()) {
			target.damage(world.getDamageSources().lightningBolt(), 5.0F);
		}
	}
}
