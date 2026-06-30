package io.github.ikunkk02.enhancedbows.lightning;

/** Pure rechargeable two-stage charge state used by the player component. */
public final class LightningChargeState {
	private int charges;
	private int maxCharges;
	private int rechargeTicks;
	private int rechargeTimeTicks;

	public LightningChargeState(int maxCharges, int rechargeTimeTicks) {
		this.maxCharges = Math.max(1, maxCharges);
		this.rechargeTimeTicks = Math.max(1, rechargeTimeTicks);
		this.charges = this.maxCharges;
	}

	public int getCharges() {
		return charges;
	}

	public int getMaxCharges() {
		return maxCharges;
	}

	public int getRechargeTicks() {
		return rechargeTicks;
	}

	public int getRechargeTimeTicks() {
		return rechargeTimeTicks;
	}

	public int getRemainingRechargeTicks() {
		return charges >= maxCharges ? 0 : Math.max(0, rechargeTimeTicks - rechargeTicks);
	}

	public boolean consume() {
		if (charges <= 0) {
			return false;
		}
		charges--;
		if (charges < maxCharges && rechargeTicks >= rechargeTimeTicks) {
			rechargeTicks = 0;
		}
		return true;
	}

	/** Returns true only on a tick that restores one charge. */
	public boolean tick() {
		if (charges >= maxCharges) {
			rechargeTicks = 0;
			return false;
		}
		rechargeTicks++;
		if (rechargeTicks < rechargeTimeTicks) {
			return false;
		}
		charges++;
		rechargeTicks = 0;
		return true;
	}

	public void configure(int configuredMaxCharges, int configuredRechargeTimeTicks) {
		maxCharges = Math.max(1, configuredMaxCharges);
		rechargeTimeTicks = Math.max(1, configuredRechargeTimeTicks);
		charges = Math.min(charges, maxCharges);
		rechargeTicks = charges >= maxCharges ? 0 : Math.min(rechargeTicks, rechargeTimeTicks - 1);
	}

	public void load(int storedCharges, int storedMaxCharges, int storedRechargeTicks,
			int storedRechargeTimeTicks, int configuredMaxCharges, int configuredRechargeTimeTicks) {
		maxCharges = Math.max(1, configuredMaxCharges);
		rechargeTimeTicks = Math.max(1, configuredRechargeTimeTicks);
		charges = Math.max(0, Math.min(storedCharges, maxCharges));
		rechargeTicks = charges >= maxCharges ? 0 : Math.max(0, Math.min(storedRechargeTicks, rechargeTimeTicks - 1));
	}

	public void reset(int configuredMaxCharges, int configuredRechargeTimeTicks) {
		maxCharges = Math.max(1, configuredMaxCharges);
		rechargeTimeTicks = Math.max(1, configuredRechargeTimeTicks);
		charges = maxCharges;
		rechargeTicks = 0;
	}
}
