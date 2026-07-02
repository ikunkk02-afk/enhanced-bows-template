# Arrow Rain Impact Revision Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Arrow Rain trigger at block or entity impacts without requiring a creature hit, use an eight-block default radius, and preserve miss, cooldown, spectral-arrow, compatibility, and recursion rules.

**Architecture:** Keep the existing projectile arming and shared server impact controller. Add explicit server-config impact policy fields, pass the impact kind through a pure rule, and schedule rain only after the owner's cooldown starts successfully; rain children remain ordinary non-pickup short-lived arrows.

**Tech Stack:** Java 21, Fabric 1.21.1, Yarn mappings, Cardinal Components API, Cloth Config, JUnit 5.

---

### Task 1: Server configuration contract

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfigTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/ArrowRainConfigResourcesTest.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`

- [ ] **Step 1: Write failing defaults and persistence assertions** for `arrowRainRadius == 8.0`, `arrowRainTriggerOnBlockHit == true`, `arrowRainTriggerOnEntityHit == true`, `arrowRainTriggerOnMiss == false`, and `arrowRainAllowSpectralArrow == false`.
- [ ] **Step 2: Run `./gradlew.bat test --tests "*ServerScanConfigTest" --tests "*ArrowRainConfigResourcesTest"`** and require failures caused by the absent fields and old radius.
- [ ] **Step 3: Extend `ServerScanConfig.Values`** by placing the three trigger booleans immediately before `arrowRainAllowSpectralArrow`, loading them with `getBoolean`, preserving them through `sanitized()`, and setting defaults to `true, true, false, false` after the existing damage multiplier.
- [ ] **Step 4: Extend the Cloth Config draft and category** with localized boolean toggles using default values `true`, `true`, and `false`, and change the radius field default to `8.0`.
- [ ] **Step 5: Re-run the focused tests** and require zero failures.

### Task 2: Impact policy and successful-trigger cooldown

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainArrowRulesTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainRuntimeStructureTest.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainArrowRules.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainImpactController.java`

- [ ] **Step 1: Write failing pure-rule assertions** proving enabled block hits and enabled entity hits qualify independently, disabled impact kinds do not qualify, misses never qualify, triggered arrows never qualify, and child arrows never qualify.
- [ ] **Step 2: Run `./gradlew.bat test --tests "*ArrowRainArrowRulesTest" --tests "*ArrowRainRuntimeStructureTest"`** and require failures from the missing impact-policy parameters.
- [ ] **Step 3: Change `shouldHandleImpact`** to accept `triggerOnEntityHit` and `triggerOnBlockHit` and return `armed && !triggered && !childArrow && ((entityImpact && triggerOnEntityHit) || (blockImpact && triggerOnBlockHit))`.
- [ ] **Step 4: Load server config before the impact decision**, pass the policy flags, retain `target.getPos()` and the mixin-provided `BlockHitResult.getPos()`, mark an enabled impact spent, then call `tryStartArrowRainCooldown`; schedule rain only when that call succeeds.
- [ ] **Step 5: Re-run the focused tests plus `compileJava`** and require zero failures.

### Task 3: Defaults, waves, recursion, and full verification

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainSpawnRulesTest.java`
- Review: `src/main/java/io/github/ikunkk02/enhancedbows/arrowrain/ArrowRainManager.java`
- Review: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Review: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/RangedWeaponItemMixin.java`

- [ ] **Step 1: Change spawn-boundary tests from radius 6 to radius 8** and verify the random offset stays inside the configured circular radius.
- [ ] **Step 2: Run all `*ArrowRain*` tests** and require exact 24-arrow, four-wave distribution plus child-marker, non-pickup, 100-tick cleanup, spectral exclusion, and Burst/Lightning compatibility assertions.
- [ ] **Step 3: Run `./gradlew.bat test` and `./gradlew.bat clean build`** and require successful exit codes.
- [ ] **Step 4: Run a dedicated server to ready state and stop it cleanly**, then audit `git diff --check`, `git status -sb`, and the built JAR.
- [ ] **Step 5: Report automated evidence and explicitly retain the eight requested in-game scenarios as manual checks** because unit tests cannot simulate real player aim and projectile visuals.
