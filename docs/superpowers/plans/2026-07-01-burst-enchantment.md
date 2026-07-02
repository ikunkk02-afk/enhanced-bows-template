# Burst Enchantment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a bow-only Burst enchantment whose non-spectral arrows create one configurable, owner-safe explosion on entity or block impact and which is mutually exclusive with Lightning.

**Architecture:** Data-pack enchantment definitions and a shared exclusive-set tag enforce normal enchanting/anvil compatibility. The existing projectile spawn and impact mixins gain a separate Burst state interface; a server-only explosion controller combines a no-entity-effect vanilla explosion with tested manual damage and knockback rules.

**Tech Stack:** Java 21, Fabric API 0.116.12, Minecraft 1.21.1, Yarn 1.21.1+build.3, Sponge Mixin, Cloth Config, JUnit 5.

---

## Working Tree Constraint

Lightning storm implementation files are intentionally present as uncommitted work and overlap `ServerScanConfig`, `ScanConfigScreenFactory`, `RangedWeaponItemMixin`, and `PersistentProjectileEntityMixin`. Preserve those edits and do not make partial commits that accidentally omit or split the established Lightning implementation.

### Task 1: Data-driven Burst enchantment and exclusivity

**Files:**
- Create: `src/main/resources/data/enhanced-bows/enchantment/burst.json`
- Create: `src/main/resources/data/enhanced-bows/tags/enchantment/exclusive_set/lightning_burst.json`
- Modify: `src/main/resources/data/enhanced-bows/enchantment/lightning.json`
- Modify: `src/main/resources/data/minecraft/tags/enchantment/in_enchanting_table.json`
- Modify: `src/main/resources/data/minecraft/tags/enchantment/non_treasure.json`
- Modify: `src/main/resources/data/minecraft/tags/enchantment/on_random_loot.json`
- Modify: `src/main/resources/data/minecraft/tags/enchantment/tradeable.json`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/enchantment/ModEnchantments.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/enchantment/BurstEnchantmentResourcesTest.java`

- [ ] Write a failing resource test asserting bow-only support, level 1, all four source tags, both enchantments referencing `#enhanced-bows:exclusive_set/lightning_burst`, and the exclusive tag containing exactly Lightning and Burst.
- [ ] Run `.\gradlew.bat test --tests "*BurstEnchantmentResourcesTest"`; expect missing Burst resources.
- [ ] Add the Burst registry key/helper/book and data resources. Use this JSON compatibility field on both enchantments:

```json
"exclusive_set": "#enhanced-bows:exclusive_set/lightning_burst"
```

- [ ] Re-run the focused resource test; expect success.

### Task 2: Pure arming, damage, filtering, and knockback rules

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/burst/BurstArrowRules.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/burst/BurstDamageRules.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/burst/BurstArrowRulesTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/burst/BurstDamageRulesTest.java`

- [ ] Write failing tests for non-spectral bow arming, permanent spectral exclusion, one-shot entity/block but not miss triggering, and Lightning priority for illegal combinations.
- [ ] Write failing tests for effective radius `power * 2`, damage 8/5/2 at center/midpoint/edge, outside exclusion, owner/player/creative/spectator filtering, and knockback falloff multiplied by the configured multiplier.
- [ ] Run `.\gradlew.bat test --tests "*BurstArrowRulesTest" --tests "*BurstDamageRulesTest"`; expect missing classes.
- [ ] Implement pure APIs:

```java
public static ArmDecision decideArming(boolean enabled, boolean bow, boolean spectral,
    int burstLevel, int lightningLevel)
public static boolean shouldTrigger(boolean armed, boolean triggered,
    boolean entityImpact, boolean blockImpact)
public static double damageAtDistance(double distance, double radius,
    double baseDamage, double minDamage)
public static boolean shouldAffect(TargetFacts facts, UUID ownerUuid,
    boolean damageOwner, boolean affectPlayers, double radius)
public static double knockbackMagnitude(double distance, double radius, double multiplier)
```

- [ ] Re-run focused tests; expect success.

### Task 3: Burst server configuration and migration

**Files:**
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfigTest.java`

- [ ] Extend failing configuration tests with all ten defaults, invalid finite/bounds cases, `burstMinDamage <= burstBaseDamage`, forced `burstExcludeSpectralArrows=true`, and a version-4 migration case that preserves Lightning storm and scan values.
- [ ] Run `.\gradlew.bat test --tests "*ServerScanConfigTest"`; expect missing Burst accessors/constructor arguments.
- [ ] Increment config version to 5, append all fields to `Values`, load missing JSON fields from defaults, clamp power/radius work to practical bounds, clamp damage and knockback non-negative, and force spectral exclusion true.
- [ ] Re-run the focused test; expect success without changing `SCAN_DEFAULTS_MIGRATION_VERSION = 3`.

### Task 4: Projectile persistence, arming priority, and explosion runtime

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/burst/BurstArrowAccess.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/burst/BurstExplosionController.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/RangedWeaponItemMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/burst/BurstIntegrationStructureTest.java`

- [ ] Write a failing source integration test asserting exact Burst NBT keys, both impact callbacks, spectral rejection, one shared trigger controller, custom `ExplosionBehavior`, manual explosion damage, and the illegal-combination warning text.
- [ ] Run `.\gradlew.bat test --tests "*BurstIntegrationStructureTest"`; expect missing files/calls.
- [ ] Add `BurstArrowAccess` methods for arming with owner UUID, armed/triggered reads, and marking triggered.
- [ ] Extend the persistent-projectile mixin fields and NBT read/write using:

```text
enhancedbows:burst_arrow
enhancedbows:burst_triggered
enhancedbows:burst_owner_uuid
```

- [ ] Extend the ranged-weapon spawn redirect: read both levels once, warn and arm only Lightning on illegal dual enchantments, never arm `SpectralArrowEntity`, otherwise arm Burst when enabled.
- [ ] Implement `BurstExplosionController`: mark triggered first, create a custom `ExplosionBehavior` with `shouldDamage=false` and `getKnockbackModifier=0`, call `createExplosion` using `NONE` or `BLOCK`, query living targets, apply `DamageSources.explosion`, and add normalized velocity.
- [ ] Forward entity and final block impacts after the existing Lightning calls; Burst state guarantees only one explosion and dual-enchantment arming prevents double effects.
- [ ] Run `.\gradlew.bat compileJava compileClientJava`; resolve only verified Yarn 1.21.1 signatures.
- [ ] Run `.\gradlew.bat test --tests "*Burst*" --tests "*Lightning*"`; expect success.

### Task 5: Cloth Config and localization

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/BurstConfigScreenResourcesTest.java`

- [ ] Write a failing test asserting the Burst enchantment translation, nine editable labels, permanent spectral-exclusion explanation, every server draft accessor, and no editable save consumer for spectral exclusion.
- [ ] Run `.\gradlew.bat test --tests "*BurstConfigScreenResourcesTest"`; expect missing keys and source references.
- [ ] Add a Burst category with power, block breaking, fire, owner/player policy, knockback, base/min damage, and a disabled true spectral setting/description. Preserve all Lightning entries unchanged.
- [ ] Update both locale JSON files with `enchantment.enhanced_bows.burst` and clear UI labels.
- [ ] Re-run the focused test and `.\gradlew.bat compileClientJava`; expect success.

### Task 6: Full verification

**Files:**
- Review all modified and new files.

- [ ] Run `.\gradlew.bat test`; expect zero failed tests.
- [ ] Run `.\gradlew.bat build`; expect `BUILD SUCCESSFUL` and remapped jars.
- [ ] Run `.\gradlew.bat runServer --no-daemon`, wait for `Done`, issue `stop`, and confirm clean exit.
- [ ] Run `.\gradlew.bat runClient --no-daemon`, wait through mod initialization and resource loading, then terminate the smoke test.
- [ ] Run `git diff --check`, inspect `git status -sb`, and verify no scan/trail/bounce/Lightning functionality was removed.
- [ ] Report enchanting/anvil/impact/block-destruction behavior as requiring manual in-game validation unless it was actually exercised.
