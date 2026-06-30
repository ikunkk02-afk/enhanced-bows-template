# Spectral Scan Reliability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every player-fired spectral arrow scan continuously with strict block visibility, predictable three-hit bouncing, rate-limited player feedback, and sharp configuration UI text.

**Architecture:** Keep activation, candidate queries, effects, and collision authority on the logical server. Isolate launch, scheduling, visibility aggregation, bounce eligibility, and client cooldown as pure testable rules; custom screens use a non-blurred background and Cloth Config uses its solid-background mode.

**Tech Stack:** Java 21, Fabric Loader/API 1.21.1, Yarn 1.21.1+build.3, Mixin, Cloth Config 15, JUnit 5, Gradle/Fabric Loom.

---

### Task 1: Encode the new scan contract in failing tests

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfigTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/scan/ScanRulesTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/scan/BouncePhysicsTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/client/hud/ScanHudStateTest.java`

- [ ] Assert defaults `scanIntervalTicks == 2`, radius `10.0`, strict LOS enabled, and max bounces `3`.
- [ ] Assert `shouldTrigger(true, true)` accepts horizontal/downward launches by removing velocity parameters from the wished-for API.
- [ ] Assert scan ticks are `0, 2, 4...` without a duration cutoff and strict visibility requires both eye and center rays clear.
- [ ] Assert bounce eligibility rejects a reflected speed below the minimum and rejects collision four when three bounces are already recorded.
- [ ] Assert `startDetected()` returns true once, false during the following 40 ticks, and true again after cooldown.
- [ ] Run `./gradlew test --tests "*ServerScanConfigTest" --tests "*ScanRulesTest" --tests "*BouncePhysicsTest" --tests "*ScanHudStateTest"`; expect compilation/assertion failure against the old APIs and defaults.

### Task 2: Implement activation, scheduling, config migration, and strict visibility

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/scan/ScanRules.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/scan/SpectralScanController.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/SpectralArrowEntityMixin.java`

- [ ] Change activation to `enabled && playerOwned` and scheduling to `elapsedTicks >= 0 && intervalTicks > 0 && elapsedTicks % intervalTicks == 0`.
- [ ] Add `strictScanLineOfSight` to `ServerScanConfig.Values`, default it to true, merge it from JSON, and default the interval to 2; keep legacy duration/threshold values only for config compatibility.
- [ ] Raycast from `arrow.getPos().add(0.0, 0.1, 0.0)` to eye and bounding-box center using `COLLIDER/NONE`; aggregate with `eyeClear && centerClear` in strict mode or `eyeClear || centerClear` otherwise.
- [ ] Keep scan state active until the arrow enters ground or loses its server-player owner; do not stop at `scanDurationTicks`.
- [ ] Refresh Glowing for every successful scan, notify detected players each successful scan for client-side throttling, and retain UUID tracking only for one-time owner mark-success cues.
- [ ] Re-run the four focused test classes; expect all scan/config tests to pass.

### Task 3: Implement deterministic bounce stopping and detected cooldown

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/scan/BouncePhysics.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ScanHudState.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/network/ClientScanNetworking.java`

- [ ] Make `canBounce` require enabled scanning state, `bounceCount < maxBounces`, and `speed * damping >= minimumResultingSpeed`; do not boost a slow reflected vector.
- [ ] Reflect and cancel vanilla embedding only for eligible collisions; after three recorded bounces, allow the fourth collision through vanilla unchanged.
- [ ] Make `ScanHudState.startDetected()` return false while its 40-tick cooldown/display is active and true when a new warning is accepted.
- [ ] Play the detected sound only when `startDetected()` accepts the notification.
- [ ] Re-run bounce and HUD tests; expect all to pass.

### Task 4: Remove blur from the configuration surfaces

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/EnhancedBowsConfigHubScreen.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/screen/CustomSoundImportScreen.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`

- [ ] Replace custom-screen `renderBackground` calls with an opaque/dark fill drawn before all custom text and `super.render` widgets.
- [ ] Call Cloth Config `solidBackground()` so its entries and tooltips are not composed into the in-game blur pass.
- [ ] Remove legacy upward-threshold and duration controls; add strict-LOS toggle and preserve legacy values when saving.
- [ ] Add English/Chinese strict-LOS labels and leave legacy translation keys unused for config compatibility.
- [ ] Run `./gradlew compileClientJava processClientResources`; expect successful client compilation and valid JSON resources.

### Task 5: Full verification

**Files:**
- Modify only a scoped file if a verification failure identifies a defect.

- [ ] Run `./gradlew test`; expect zero failures.
- [ ] Run `./gradlew clean build`; expect `BUILD SUCCESSFUL`.
- [ ] Start `./gradlew runServer --no-daemon --args nogui`, wait for normal startup, send `stop`, and confirm no client-class loading or Mixin failure.
- [ ] Start `./gradlew runClient --no-daemon`, reach a usable screen, inspect `run/logs/latest.log`, and visually verify sharp hub/Cloth/sound-import text.
- [ ] Report remaining manual in-world checks: horizontal/downward/indoor activation, ground bounce count, outside-to-inside wall blocking, inside clear detection, and stable unobstructed Glowing.
