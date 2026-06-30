package io.github.ikunkk02.enhancedbows.mixin;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowAccess;
import io.github.ikunkk02.enhancedbows.lightning.LightningArrowRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
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
		if ((Object) this instanceof BowItem && entity instanceof PersistentProjectileEntity projectile
				&& projectile instanceof LightningArrowAccess lightningArrow) {
			ServerScanConfig.Values config = ServerScanConfig.get();
			int level = ModEnchantments.getLightningLevel(spawnWorld, weaponStack);
			if (LightningArrowRules.shouldArm(config.enableLightningEnchantment(), true, level)) {
				lightningArrow.enhancedBows$armLightning();
			}
		}
		return spawnWorld.spawnEntity(entity);
	}
}
