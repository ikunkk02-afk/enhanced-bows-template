package io.github.ikunkk02.enhancedbows.scan;

/** Internal bridge exposing scanning-arrow bounce state to the block-hit Mixin. */
public interface ScanningArrowAccess {
	boolean enhancedBows$isScanningArrow();

	int enhancedBows$getBounceCount();

	int enhancedBows$getMaxBounces();

	void enhancedBows$recordBounce();
}
