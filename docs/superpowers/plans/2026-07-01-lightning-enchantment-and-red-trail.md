# Lightning Enchantment and Red Trail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a client-only red scanning-arrow trail and a server-authoritative rechargeable Lightning bow enchantment with synchronized HUD.

**Architecture:** Publish scanning state through entity tracking, arm projectiles at `RangedWeaponItem.shootAll`, consume CCA-owned charges on a valid living-entity hit, and render synchronized HUD state only from client code. Data-driven enchantment resources make the book available from normal vanilla sources.

**Tech Stack:** Java 21, Fabric Loader/API 1.21.1, Yarn 1.21.1+build.3, Cardinal Components API 6.1.3, Cloth Config, JUnit 5.

---

### Task 1: Configuration and pure state

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ClientScanConfig.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningChargeState.java`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfigTest.java`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/ClientScanConfigTest.java`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/lightning/LightningChargeStateTest.java`

- [ ] Add failing assertions for all requested defaults and clamping rules.
- [ ] Add failing tests proving two charges, one charge restored at 400 ticks, and full restoration at 800 ticks.
- [ ] Run `./gradlew test --tests '*ConfigTest' --tests '*LightningChargeStateTest'` and confirm failure from missing behavior.
- [ ] Implement the minimum immutable config fields and pure charge state needed by the tests.
- [ ] Re-run the targeted tests and confirm they pass.

### Task 2: CCA persistence and synchronization

**Files:**
- Modify: `build.gradle`
- Modify: `gradle.properties`
- Modify: `src/main/resources/fabric.mod.json`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningChargeComponent.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/PlayerLightningChargeComponent.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningComponents.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningEvents.java`

- [ ] Add CCA base/entity 6.1.3 dependencies and the `cardinal-components` entrypoint.
- [ ] Persist charge count, max count, recharge progress, and recharge duration in NBT.
- [ ] Sync immediately on consumption/recovery and every 20 recharge ticks; rely on CCA initial tracking sync for login.
- [ ] Register `ALWAYS_COPY` respawn behavior and reset after respawn only when configuration disables retention.
- [ ] Run `./gradlew compileJava` and resolve only feature-related API errors.

### Task 3: Enchantment resources and projectile trigger

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningArrowAccess.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStrikeController.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/RangedWeaponItemMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Modify: `src/main/resources/enhanced-bows.mixins.json`
- Create: `src/main/resources/data/enhanced-bows/enchantment/lightning.json`
- Create: `src/main/resources/data/minecraft/tags/enchantment/in_enchanting_table.json`
- Create: `src/main/resources/data/minecraft/tags/enchantment/non_treasure.json`
- Create: `src/main/resources/data/minecraft/tags/enchantment/tradeable.json`
- Create: `src/main/resources/data/minecraft/tags/enchantment/on_random_loot.json`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/lightning/LightningArrowRulesTest.java`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/enchantment/LightningEnchantmentResourcesTest.java`

- [ ] Add failing tests for bow-only arming, valid-target consumption, creative policy, one-shot behavior, and resource fields/tags.
- [ ] Run the targeted tests and confirm failure from missing classes/resources.
- [ ] Arm projectiles before spawn only from enchanted bows, persist the flags, and trigger once after a living target survives the arrow hit.
- [ ] Spawn a cosmetic lightning bolt, apply vanilla lightning damage only to the target, and consume the component charge.
- [ ] Re-run the targeted tests and compile the main source set.

### Task 4: Scanning-arrow tracked state and red trail

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/SpectralArrowEntityMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/scan/ScanningArrowAccess.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/mixin/SpectralArrowTrailMixin.java`
- Create: `src/client/resources/enhanced-bows.client.mixins.json`
- Modify: `src/main/resources/fabric.mod.json`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/trail/RedTrailSampler.java`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/client/trail/RedTrailSamplerTest.java`

- [ ] Add a failing sampler test for 14 evenly spaced points across a 12-block reverse-velocity trail.
- [ ] Publish scanning state with a tracked boolean and preserve it in NBT.
- [ ] Spawn `DustParticleEffect` particles only on the client for tracked scanning arrows.
- [ ] Verify ordinary arrows and unarmed spectral arrows return before particle creation.

### Task 5: HUD, config screen, and translations

**Files:**
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/LightningHudRenderer.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/EnhancedBowsClient.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/screen/CustomSoundImportScreen.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`
- Test: `src/test/java/io/github/ikunkk02/enhancedbows/client/hud/LightningHudTextTest.java`

- [ ] Add failing tests for full, recharging, and empty HUD text states.
- [ ] Render synchronized CCA values at x=8, y=8, scale=0.8 and hide according to the enchanted-bow holding rule.
- [ ] Add all server, trail, and HUD options to Cloth Config without reintroducing background blur.
- [ ] Add English and Chinese enchantment/config/HUD translations.

### Task 6: Verification and publication

**Files:**
- Verify all modified files in the working tree.

- [ ] Run `./gradlew test` and require zero failures.
- [ ] Run `./gradlew clean build` and require exit code 0.
- [ ] Run `./gradlew runClient` and inspect trail/HUD/resource loading.
- [ ] Run `./gradlew runServer --no-daemon` and confirm common-side initialization without client class loading failures.
- [ ] Review `git diff --check`, `git status -sb`, and the staged diff.
- [ ] Commit the complete requested scope, push `main` to `origin`, and confirm the final remote/local state.
