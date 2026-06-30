package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import io.github.ikunkk02.enhancedbows.lightning.LightningChargeComponent;
import io.github.ikunkk02.enhancedbows.lightning.LightningComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class LightningHudRenderer {
	private LightningHudRenderer() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientScanConfig.Values config = ClientScanConfig.get();
		if (!config.enableLightningHud() || client.player == null || client.world == null) {
			return;
		}
		if (config.showLightningHudOnlyWhenHoldingBow() && !isHoldingLightningBow(client)) {
			return;
		}

		LightningChargeComponent component = LightningComponents.LIGHTNING_CHARGE.get(client.player);
		LightningHudText hud = LightningHudText.create(component.getLightningCharges(),
			component.getLightningMaxCharges(), component.getRemainingRechargeTicks());
		Text text = hud.remainingSeconds() == 0
			? Text.translatable(hud.translationKey(), hud.charges(), hud.maxCharges())
			: Text.translatable(hud.translationKey(), hud.charges(), hud.maxCharges(), hud.remainingSeconds());

		float scale = (float) config.lightningHudScale();
		context.getMatrices().push();
		context.getMatrices().translate(config.lightningHudX(), config.lightningHudY(), 0.0F);
		context.getMatrices().scale(scale, scale, 1.0F);
		context.drawTextWithShadow(client.textRenderer, text, 0, 0, 0xFFFF5555);
		context.getMatrices().pop();
	}

	private static boolean isHoldingLightningBow(MinecraftClient client) {
		return isLightningBow(client, client.player.getMainHandStack())
			|| isLightningBow(client, client.player.getOffHandStack());
	}

	private static boolean isLightningBow(MinecraftClient client, ItemStack stack) {
		return stack.getItem() instanceof BowItem && ModEnchantments.getLightningLevel(client.world, stack) > 0;
	}
}
