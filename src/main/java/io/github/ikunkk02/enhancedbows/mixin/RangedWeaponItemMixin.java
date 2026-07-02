package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.EnhancedBows;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainArrowAccess;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainArrowRules;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponent;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponents;
import io.github.ikunkk02.enhancedbows.burst.BurstArrowAccess;
import io.github.ikunkk02.enhancedbows.burst.BurstArrowRules;
import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public abstract class RangedWeaponItemMixin {
	@Redirect(method = "shootAll", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean enhancedBows$armLightningArrowBeforeSpawn(ServerWorld spawnWorld, Entity entity,
			ServerWorld world, LivingEntity shooter, Hand hand, ItemStack weaponStack,
			List<ItemStack> projectileStacks, float speed, float divergence, boolean critical, LivingEntity target) {
		if ((Object) this instanceof BowItem && entity instanceof PersistentProjectileEntity projectile) {
			ServerScanConfig.Values config = ServerScanConfig.get();
			int lightningLevel = ModEnchantments.getLightningLevel(spawnWorld, weaponStack);
			int burstLevel = ModEnchantments.getBurstLevel(spawnWorld, weaponStack);
			int arrowRainLevel = ModEnchantments.getArrowRainLevel(spawnWorld, weaponStack);
			if (lightningLevel > 0 && burstLevel > 0) {
				EnhancedBows.LOGGER.warn("Bow has both Lightning and Burst enchantments. This is not allowed. Lightning takes priority.");
			}
			if (arrowRainLevel > 0 && burstLevel > 0) {
				EnhancedBows.LOGGER.warn("Bow has both Arrow Rain and Burst enchantments. This is not allowed. Arrow Rain is disabled.");
			}
			BurstArrowRules.ArmDecision decision = BurstArrowRules.decideArming(
				config.enableBurstEnchantment(), config.enableLightningEnchantment(), true,
				shooter instanceof ServerPlayerEntity,
				entity instanceof SpectralArrowEntity, burstLevel, lightningLevel);
			if (decision == BurstArrowRules.ArmDecision.LIGHTNING
					&& projectile instanceof LightningArrowAccess lightningArrow
					&& LightningArrowRules.shouldArm(config.enableLightningEnchantment(), true, lightningLevel)) {
				lightningArrow.enhancedBows$armLightning();
			} else if (decision == BurstArrowRules.ArmDecision.BURST
					&& projectile instanceof BurstArrowAccess burstArrow) {
				burstArrow.enhancedBows$armBurst(shooter.getUuid());
			}

			if (shooter instanceof ServerPlayerEntity player
					&& projectile instanceof ArrowRainArrowAccess arrowRainArrow) {
				ArrowRainComponent component = ArrowRainComponents.ARROW_RAIN.get(player);
				if (ArrowRainArrowRules.shouldArm(config.enableArrowRainEnchantment(), true, true,
					component.isArrowRainModeEnabled(), component.getArrowRainCooldownTicks(),
					burstLevel > 0, entity instanceof SpectralArrowEntity,
					config.arrowRainAllowSpectralArrow(), arrowRainArrow.enhancedBows$isArrowRainChild(),
					arrowRainLevel)) {
					arrowRainArrow.enhancedBows$armArrowRain(player.getUuid(), projectile.getDamage());
				}
			}
		}
		return spawnWorld.spawnEntity(entity);
	}
}
