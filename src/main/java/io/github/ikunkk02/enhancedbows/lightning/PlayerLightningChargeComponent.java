package io.github.ikunkk02.enhancedbows.lightning;

import io.github.ikunkk02.enhancedbows.config.ServerScanConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

final class PlayerLightningChargeComponent implements LightningChargeComponent {
	private static final String CHARGES_KEY = "lightningCharges";
	private static final String MAX_CHARGES_KEY = "lightningMaxCharges";
	private static final String RECHARGE_TICKS_KEY = "lightningRechargeTicks";
	private static final String RECHARGE_TIME_KEY = "lightningRechargeTimeTicks";

	private final PlayerEntity player;
	private final LightningChargeState state;

	PlayerLightningChargeComponent(PlayerEntity player) {
		this.player = player;
		ServerScanConfig.Values config = ServerScanConfig.get();
		this.state = new LightningChargeState(config.lightningMaxCharges(), config.lightningRechargeTicks());
	}

	@Override
	public int getLightningCharges() {
		return state.getCharges();
	}

	@Override
	public int getLightningMaxCharges() {
		return state.getMaxCharges();
	}

	@Override
	public int getLightningRechargeTicks() {
		return state.getRechargeTicks();
	}

	@Override
	public int getLightningRechargeTimeTicks() {
		return state.getRechargeTimeTicks();
	}

	@Override
	public int getRemainingRechargeTicks() {
		return state.getRemainingRechargeTicks();
	}

	@Override
	public boolean consumeLightningCharge() {
		if (!state.consume()) {
			return false;
		}
		sync();
		return true;
	}

	@Override
	public void resetLightningCharges() {
		ServerScanConfig.Values config = ServerScanConfig.get();
		state.reset(config.lightningMaxCharges(), config.lightningRechargeTicks());
		sync();
	}

	@Override
	public void serverTick() {
		ServerScanConfig.Values config = ServerScanConfig.get();
		int oldCharges = state.getCharges();
		int oldMax = state.getMaxCharges();
		int oldRechargeTime = state.getRechargeTimeTicks();
		state.configure(config.lightningMaxCharges(), config.lightningRechargeTicks());
		boolean recovered = state.tick();
		boolean configChanged = oldCharges != state.getCharges()
			|| oldMax != state.getMaxCharges()
			|| oldRechargeTime != state.getRechargeTimeTicks();
		boolean secondBoundary = state.getCharges() < state.getMaxCharges()
			&& state.getRechargeTicks() % 20 == 0;
		if (recovered || configChanged || secondBoundary) {
			sync();
		}
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity recipient) {
		return recipient == player;
	}

	@Override
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		ServerScanConfig.Values config = ServerScanConfig.get();
		int configuredMaxCharges = player.getWorld().isClient()
			? Math.max(1, tag.getInt(MAX_CHARGES_KEY))
			: config.lightningMaxCharges();
		int configuredRechargeTime = player.getWorld().isClient()
			? Math.max(1, tag.getInt(RECHARGE_TIME_KEY))
			: config.lightningRechargeTicks();
		state.load(tag.getInt(CHARGES_KEY), tag.getInt(MAX_CHARGES_KEY), tag.getInt(RECHARGE_TICKS_KEY),
			tag.getInt(RECHARGE_TIME_KEY), configuredMaxCharges, configuredRechargeTime);
	}

	@Override
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		tag.putInt(CHARGES_KEY, state.getCharges());
		tag.putInt(MAX_CHARGES_KEY, state.getMaxCharges());
		tag.putInt(RECHARGE_TICKS_KEY, state.getRechargeTicks());
		tag.putInt(RECHARGE_TIME_KEY, state.getRechargeTimeTicks());
	}

	private void sync() {
		if (player instanceof ServerPlayerEntity) {
			LightningComponents.LIGHTNING_CHARGE.sync(player);
		}
	}
}
