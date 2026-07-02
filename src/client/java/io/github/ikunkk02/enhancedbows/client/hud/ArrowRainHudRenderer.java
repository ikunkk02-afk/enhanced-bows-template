package io.github.ikunkk02.enhancedbows.client.hud;

import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponent;
import io.github.ikunkk02.enhancedbows.arrowrain.ArrowRainComponents;
import io.github.ikunkk02.enhancedbows.client.config.ClientScanConfig;
import io.github.ikunkk02.enhancedbows.enchantment.ModEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class ArrowRainHudRenderer {
	private ArrowRainHudRenderer() {
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientScanConfig.Values config = ClientScanConfig.get();
		if (!config.enableArrowRainHud() || client.player == null || client.world == null) {
			return;
		}
		ItemStack heldBow = findArrowRainBow(client);
		ArrowRainComponent component = ArrowRainComponents.ARROW_RAIN.get(client.player);
		ArrowRainHudText hud = ArrowRainHudText.create(!heldBow.isEmpty(),
			component.isArrowRainModeEnabled(), component.getArrowRainCooldownTicks());
		if (!hud.visible()) {
			return;
		}

		Text text = hud.remainingSeconds() > 0
			? Text.translatable(hud.translationKey(), hud.remainingSeconds())
			: Text.translatable(hud.translationKey());
		int y = config.lightningHudY();
		if (config.enableLightningHud() && ModEnchantments.getLightningLevel(client.world, heldBow) > 0) {
			y += 12;
		}
		context.getMatrices().push();
		context.getMatrices().translate(config.lightningHudX(), y, 0.0F);
		context.getMatrices().scale(0.85F, 0.85F, 1.0F);
		context.drawTextWithShadow(client.textRenderer, text, 0, 0, 0xFF55FFFF);
		context.getMatrices().pop();
	}

	private static ItemStack findArrowRainBow(MinecraftClient client) {
		ItemStack mainHand = client.player.getMainHandStack();
		if (isArrowRainBow(client, mainHand)) {
			return mainHand;
		}
		ItemStack offHand = client.player.getOffHandStack();
		return isArrowRainBow(client, offHand) ? offHand : ItemStack.EMPTY;
	}

	private static boolean isArrowRainBow(MinecraftClient client, ItemStack stack) {
		return stack.getItem() instanceof BowItem
			&& ModEnchantments.getArrowRainLevel(client.world, stack) > 0;
	}
}
