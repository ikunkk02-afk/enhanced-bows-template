package io.github.ikunkk02.enhancedbows.arrowrain;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

final class PlayerArrowRainComponent implements ArrowRainComponent {
	private static final String MODE_KEY = "arrowRainModeEnabled";
	private static final String COOLDOWN_KEY = "arrowRainCooldownTicks";

	private final PlayerEntity player;
	private final ArrowRainState state = new ArrowRainState();

	PlayerArrowRainComponent(PlayerEntity player) {
		this.player = player;
	}

	@Override
	public boolean isArrowRainModeEnabled() {
		return state.isModeEnabled();
	}

	@Override
	public int getArrowRainCooldownTicks() {
		return state.getCooldownTicks();
	}

	@Override
	public boolean toggleArrowRainMode() {
		boolean enabled = state.toggleMode();
		sync();
		return enabled;
	}

	@Override
	public boolean tryStartArrowRainCooldown(int configuredTicks) {
		if (!state.tryStartCooldown(configuredTicks)) {
			return false;
		}
		sync();
		return true;
	}

	@Override
	public void serverTick() {
		ArrowRainState.TickResult result = state.tick();
		if (result == ArrowRainState.TickResult.SECOND_BOUNDARY
				|| result == ArrowRainState.TickResult.COMPLETE) {
			sync();
		}
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity recipient) {
		return recipient == player;
	}

	@Override
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		state.load(tag.getBoolean(MODE_KEY), tag.getInt(COOLDOWN_KEY));
	}

	@Override
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		tag.putBoolean(MODE_KEY, state.isModeEnabled());
		tag.putInt(COOLDOWN_KEY, state.getCooldownTicks());
	}

	private void sync() {
		if (player instanceof ServerPlayerEntity) {
			ArrowRainComponents.ARROW_RAIN.sync(player);
		}
	}
}
