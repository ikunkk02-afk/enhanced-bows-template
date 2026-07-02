# Arrow Rain Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a bow-only Arrow Rain enchantment, a server-authoritative rebindable mode toggle, independently synchronized cooldown/HUD state, and bounded non-recursive four-wave arrow rain impacts.

**Architecture:** Data-pack enchantment compatibility prevents Burst plus Arrow Rain while preserving Lightning plus Arrow Rain. A dedicated CCA component owns mode/cooldown, C2S/S2C payloads carry toggle intent/results, projectile mixin state captures eligible shots, and a server-world tick manager emits ordinary short-lived rain arrows in configured waves.

**Tech Stack:** Java 21, Fabric Loader 0.19.3, Fabric API 0.116.12 for Minecraft 1.21.1, Yarn 1.21.1+build.3, Cardinal Components API 6.1.3, Sponge Mixin, Cloth Config, JUnit 5.

---

## Working Tree Constraint

The current `main` worktree intentionally contains uncommitted Lightning storm and Burst implementation. Arrow Rain must preserve those changes and extend the shared `ServerScanConfig`, `ModEnchantments`, `RangedWeaponItemMixin`, `PersistentProjectileEntityMixin`, language files, and source tags without resetting or partially staging unrelated work. Do not create implementation commits unless the user separately requests them.

## File Structure

- `arrowrain/ArrowRainComponent*`: persistent, synchronized player mode/cooldown authority.
- `arrowrain/ArrowRainArrowAccess`, `ArrowRainArrowRules`: projectile state boundary and pure arming/impact decisions.
- `arrowrain/ArrowRainSchedule`, `ArrowRainManager`: pure wave timing plus server-world event/projectile creation.
- `network/ToggleArrowRainModeC2SPayload`, `ArrowRainToggleResultS2CPayload`: explicit toggle protocol.
- `client/input/ArrowRainKeyBinding`, `client/network/ClientArrowRainNetworking`: client input and feedback only.
- `client/hud/ArrowRainHudText`, `ArrowRainHudRenderer`: passive rendering of synchronized state.

### Task 1: Data-driven enchantment and compatibility graph

**Files:**
- Create: `src/main/resources/data/enhanced-bows/enchantment/arrow_rain.json`
- Create/modify: `src/main/resources/data/enhanced-bows/tags/enchantment/exclusive_set/*.json`
- Modify: `src/main/resources/data/enhanced-bows/enchantment/burst.json`
- Modify: `src/main/resources/data/enhanced-bows/enchantment/lightning.json`
- Modify: `src/main/resources/data/minecraft/tags/enchantment/{in_enchanting_table,non_treasure,on_random_loot,tradeable}.json`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/enchantment/ArrowRainEnchantmentResourcesTest.java`

- [ ] **Step 1: Write the failing resource test** asserting `arrow_rain` is bow-only, level 1, present in all four source tags and creative-book code, Burst rejects Arrow Rain, Arrow Rain rejects Burst, and neither Lightning nor Arrow Rain rejects the other.
- [ ] **Step 2: Verify RED** with `./gradlew.bat test --tests "*ArrowRainEnchantmentResourcesTest"`; expect missing Arrow Rain JSON/key/tag assertions.
- [ ] **Step 3: Add the minimal resources and registry helper** using `public static final RegistryKey<Enchantment> ARROW_RAIN`, `getArrowRainLevel(World, ItemStack)`, and an Arrow Rain enchanted book alongside existing books. Use separate exclusive tags so no tag referenced by Lightning contains Arrow Rain and no tag referenced by Arrow Rain contains Lightning.
- [ ] **Step 4: Verify GREEN** with the focused resource test and `./gradlew.bat processResources`; expect success.

### Task 2: Player mode/cooldown state and CCA persistence

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainState.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainComponent.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/PlayerArrowRainComponent.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainComponents.java`
- Modify: `src/main/resources/fabric.mod.json`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainStateTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainComponentStructureTest.java`

- [ ] **Step 1: Write failing pure-state tests** for default-off/default-ready, toggle, cooldown start, one-tick decrement, whole-second boundary reporting, zero completion, and load clamping.
- [ ] **Step 2: Verify RED** with `./gradlew.bat test --tests "*ArrowRainStateTest"`; expect `ArrowRainState` missing.
- [ ] **Step 3: Implement the minimal pure state API:**

```java
final class ArrowRainState {
    boolean isModeEnabled();
    int getCooldownTicks();
    boolean toggleMode();
    boolean tryStartCooldown(int configuredTicks);
    TickResult tick();
    void load(boolean enabled, int cooldownTicks);
    enum TickResult { NONE, SECOND_BOUNDARY, COMPLETE }
}
```

- [ ] **Step 4: Verify GREEN** for `ArrowRainStateTest`.
- [ ] **Step 5: Write the failing structure test** asserting `AutoSyncedComponent`, `ServerTickingComponent`, `RespawnCopyStrategy.ALWAYS_COPY`, owner-only sync, NBT keys `arrowRainModeEnabled`/`arrowRainCooldownTicks`, sync on toggle/start/second/completion, and Fabric entrypoint/custom component declarations.
- [ ] **Step 6: Verify RED**, then implement `ArrowRainComponent`, `PlayerArrowRainComponent`, and `ArrowRainComponents` using `ComponentKey.sync(player)` only on the server.
- [ ] **Step 7: Verify GREEN** with both Arrow Rain component tests and `compileJava`.

### Task 3: Server-validated toggle networking and rebindable V key

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainToggleRules.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/network/ToggleArrowRainModeC2SPayload.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/network/ArrowRainToggleResultS2CPayload.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/network/ModNetworking.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/input/ArrowRainKeyBinding.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/network/ClientArrowRainNetworking.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/EnhancedBowsClient.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainToggleRulesTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainNetworkingStructureTest.java`

- [ ] **Step 1: Write failing toggle-rule tests** for no bow, valid enable/disable, Burst rejecting enable, and Burst still allowing disable. Desired API:

```java
static ToggleDecision decide(boolean currentlyEnabled, boolean holdingArrowRainBow,
    boolean heldBowHasBurst)
```

- [ ] **Step 2: Verify RED**, implement `ToggleDecision` values `ENABLE`, `DISABLE`, `REQUIRES_BOW`, `BURST_CONFLICT`, then verify GREEN.
- [ ] **Step 3: Write a failing structure test** asserting both payload codecs/registrations, server hand inspection with `BowItem` plus enchantment helpers, component mutation only after validation, `KeyBindingHelper.registerKeyBinding`, default `GLFW_KEY_V`, a dedicated Enhanced Bows category, `while (wasPressed())`, C2S send, localized S2C feedback, and `SoundEvents.UI_BUTTON_CLICK` only for success.
- [ ] **Step 4: Verify RED**, implement empty/unit C2S payload and result enum/string S2C payload, register common codecs/receiver without client classes, then register the key and client receiver under `src/client`.
- [ ] **Step 5: Verify GREEN** with focused tests plus `compileJava compileClientJava`.

### Task 4: Projectile arming, persistence, and impact gates

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainArrowAccess.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainArrowRules.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/RangedWeaponItemMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainArrowRulesTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainProjectileStructureTest.java`

- [ ] **Step 1: Write failing pure tests** covering enabled bow/player/mode/ready arming, cooldown rejection, Burst rejection regardless of config, default spectral rejection/config allowance, child rejection, first entity/final-block impact, miss rejection, and already-triggered rejection.
- [ ] **Step 2: Verify RED**, then implement pure `shouldArm(...)` and `shouldHandleImpact(...)` methods and verify GREEN.
- [ ] **Step 3: Write a failing source test** for the six exact Arrow Rain NBT fields, independent Lightning/Arrow Rain arming (not an `else if`), illegal Burst warning, both impact forwards, mark-triggered-before-cooldown behavior, and child lifetime cleanup.
- [ ] **Step 4: Verify RED**, extend the projectile interface/mixin and spawn redirect. Capture `projectile.getDamage()` as base damage and owner UUID. Keep existing Lightning/Burst selection intact, then independently arm Arrow Rain when rules pass.
- [ ] **Step 5: Add child marker/lifetime support** with a server tick that increments only marked children and discards them at the stored maximum lifetime. Rain children are never armed.
- [ ] **Step 6: Verify GREEN** with Arrow Rain, Burst, Lightning, scan, and bounce tests plus `compileJava`.

### Task 5: Wave scheduler and ordinary rain arrows

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainSchedule.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainSpawnRules.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainManager.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainImpactController.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/EnhancedBows.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainScheduleTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainSpawnRulesTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainRuntimeStructureTest.java`

- [ ] **Step 1: Write failing scheduler tests** asserting 24/4 distributes `6,6,6,6`, 25/4 distributes `7,6,6,6`, wave ticks are evenly spread within 40 ticks, each wave emits once, and completion removes the schedule.
- [ ] **Step 2: Write failing spawn-rule tests** for circular sampling (`sqrt(random) * radius`), height offset, base damage times multiplier, and non-negative downward speed inputs.
- [ ] **Step 3: Verify RED**, implement the pure schedule/spawn calculations, then verify GREEN.
- [ ] **Step 4: Write the failing runtime structure test** asserting `END_WORLD_TICK`, per-world bounded schedules, owner resolution, ordinary `ArrowEntity`, ordinary arrow stack, downward velocity, `PickupPermission.DISALLOWED`, base-damage scaling, child marker, 100-tick cleanup, and no copied tipped/fire/critical state.
- [ ] **Step 5: Verify RED**, implement `ArrowRainManager` using current Yarn constructors verified from local mapped classes. Implement `ArrowRainImpactController` to mark the source projectile triggered before atomically starting the owner component cooldown and scheduling rain.
- [ ] **Step 6: Wire entity and final-block impacts after existing Lightning/Burst calls**, preserving independent flags, then verify GREEN with `compileJava` and all `*ArrowRain*`, `*Lightning*`, and `*Burst*` tests.

### Task 6: Configuration, Cloth Config, localization, and HUD

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ClientScanConfig.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ArrowRainHudText.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ArrowRainHudRenderer.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/EnhancedBowsClient.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`
- Modify: existing config tests
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/hud/ArrowRainHudTextTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/ArrowRainConfigResourcesTest.java`

- [ ] **Step 1: Extend failing server/client config tests** with the nine gameplay defaults and `enableArrowRainHud=true`, legacy migration, finite bounds, maximum caps, `waves <= count` for positive counts, and constructor/save propagation.
- [ ] **Step 2: Verify RED**, increment server config version, append/load/sanitize gameplay fields, append/load/save the client HUD field, then verify GREEN.
- [ ] **Step 3: Write failing HUD text tests** for hidden/off/on/cooldown states and rounded-up seconds. Desired pure API returns translation key and remaining seconds from `holdingBow`, `modeEnabled`, and cooldown ticks.
- [ ] **Step 4: Verify RED**, implement `ArrowRainHudText`, then verify GREEN.
- [ ] **Step 5: Write the failing resource test** for enchantment/key/category/toggle/result/HUD/config translations, every Cloth Config draft accessor and save consumer, client-only HUD source location, and Lightning-offset logic.
- [ ] **Step 6: Verify RED**, implement the Arrow Rain category and localized strings. Register `ArrowRainHudRenderer` only from `EnhancedBowsClient`; anchor it to Lightning HUD coordinates, use fixed scale `0.85`, and add a vertical offset only when the held bow also has Lightning and Lightning HUD rendering is enabled.
- [ ] **Step 7: Verify GREEN** with config/HUD/resource tests and `compileClientJava`.

### Task 7: Full verification and artifact audit

**Files:** Review every modified/new file; do not stage implementation.

- [ ] Run `./gradlew.bat test`; require zero failures/errors.
- [ ] Run `./gradlew.bat clean build`; require `BUILD SUCCESSFUL` and remapped JARs.
- [ ] Run `./gradlew.bat runServer --no-daemon` in a TTY, wait for `Done`, send `stop`, and require clean save/exit.
- [ ] Run `./gradlew.bat runClient --no-daemon`, wait for Enhanced Bows initialization, resource reload, sound engine, and main-menu readiness, then terminate the smoke test.
- [ ] Run `git diff --check`, `git status -sb`, and audit `build/libs/enhanced-bows-1.0.0.jar` for Arrow Rain classes, enchantment JSON, exclusive tags, payload classes, translations, and no client classes referenced by common entrypoints.
- [ ] Report actual automated/runtime evidence separately from manual checks. Keep enchanting/anvil interactions, V rebind UI, real impacts, wave visuals, spectral scan regression, Burst rejection messages, and simultaneous Lightning plus Arrow Rain as explicit in-game manual tests unless exercised.
