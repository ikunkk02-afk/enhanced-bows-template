package io.github.ikunkk02.enhancedbows.arrowrain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowRainNetworkingStructureTest {
	private static final Path MAIN = Path.of("src/main/java/io/github/ikunkk02/enhancedbows");
	private static final Path CLIENT = Path.of("src/client/java/io/github/ikunkk02/enhancedbows/client");

	@Test
	void commonNetworkingValidatesHandsAndMutatesOnlyTheServerComponent() throws IOException {
		String networking = Files.readString(MAIN.resolve("network/ModNetworking.java"));
		String c2s = Files.readString(MAIN.resolve("network/ToggleArrowRainModeC2SPayload.java"));
		String s2c = Files.readString(MAIN.resolve("network/ArrowRainToggleResultS2CPayload.java"));

		assertTrue(c2s.contains("PacketCodec.unit"));
		assertTrue(s2c.contains("PacketCodecs.VAR_INT"));
		assertTrue(networking.contains("PayloadTypeRegistry.playC2S()"));
		assertTrue(networking.contains("PayloadTypeRegistry.playS2C()"));
		assertTrue(networking.contains("ServerPlayNetworking.registerGlobalReceiver"));
		assertTrue(networking.contains("getMainHandStack()"));
		assertTrue(networking.contains("getOffHandStack()"));
		assertTrue(networking.contains("instanceof BowItem"));
		assertTrue(networking.contains("ModEnchantments.getArrowRainLevel"));
		assertTrue(networking.contains("ModEnchantments.getBurstLevel"));
		assertTrue(networking.contains("toggleArrowRainMode()"));
		assertTrue(networking.contains("ServerPlayNetworking.send"));
	}

	@Test
	void clientRegistersRebindableVKeyAndServerResultFeedback() throws IOException {
		String key = Files.readString(CLIENT.resolve("input/ArrowRainKeyBinding.java"));
		String clientNetwork = Files.readString(CLIENT.resolve("network/ClientArrowRainNetworking.java"));
		String initializer = Files.readString(CLIENT.resolve("EnhancedBowsClient.java"));

		assertTrue(key.contains("KeyBindingHelper.registerKeyBinding"));
		assertTrue(key.contains("GLFW.GLFW_KEY_V"));
		assertTrue(key.contains("key.enhanced-bows.toggle_arrow_rain"));
		assertTrue(key.contains("category.enhanced-bows.enhanced_bows"));
		assertTrue(key.contains("while (TOGGLE.wasPressed())"));
		assertTrue(key.contains("ClientPlayNetworking.send"));
		assertTrue(clientNetwork.contains("ClientPlayNetworking.registerGlobalReceiver"));
		assertTrue(clientNetwork.contains("client.player.sendMessage"));
		assertTrue(clientNetwork.contains("SoundEvents.UI_BUTTON_CLICK"));
		assertTrue(initializer.contains("ArrowRainKeyBinding.register()"));
		assertTrue(initializer.contains("ClientArrowRainNetworking.register()"));
	}
}
