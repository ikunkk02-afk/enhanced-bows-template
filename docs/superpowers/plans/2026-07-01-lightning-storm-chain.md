# Lightning Storm and Chain Strike Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn an impact-triggered Lightning arrow into a bounded five-second server-authoritative storm with immediate nearest-first chain strikes and explicit configurable damage.

**Architecture:** Keep projectile arming and one-shot NBT in the existing persistent-projectile mixin, but delegate impact activation to a focused controller. A global per-world manager ticks lightweight storm records, while pure policy/state classes make cadence, repeat suppression, filtering, and ordering testable without a running Minecraft server.

**Tech Stack:** Java 21, Fabric API 0.116.12 for Minecraft 1.21.1, Yarn 1.21.1+build.3, Sponge Mixin, Cloth Config, JUnit 5.

---

## File Structure

- Modify `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningArrowRules.java`: distinguish entity, block, and miss impact gates.
- Create `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStormState.java`: pure storm timing and repeat-strike bookkeeping.
- Create `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningTargetRules.java`: pure eligibility and nearest-first limit helpers.
- Create `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStorm.java`: server-world storm query and strike execution.
- Create `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStormManager.java`: per-world storm storage and Fabric tick registration.
- Create `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningImpactController.java`: charge gate, immediate chain, and storm creation.
- Modify `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStrikeController.java`: cosmetic bolt plus explicit targeted damage/fire.
- Modify `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`: call the shared controller from entity and final block impacts.
- Modify `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`: add storm settings while retaining hidden `lightningDamageMode` compatibility.
- Modify `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`: expose new settings and hide damage mode.
- Modify both language JSON files: add Chinese and English labels.
- Modify `src/main/java/io/github/ikunkk02/enhancedbows/EnhancedBows.java`: register the storm manager.
- Add/update JUnit tests under `src/test/java/io/github/ikunkk02/enhancedbows/`.

### Task 1: Impact gates and configuration contract

**Files:**
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/lightning/LightningArrowRulesTest.java`
- Modify: `src/test/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfigTest.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningArrowRules.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/config/ServerScanConfig.java`

- [ ] **Step 1: Write failing impact and configuration tests**

Add assertions equivalent to:

```java
assertTrue(LightningArrowRules.shouldTriggerImpact(true, false, true, true, true, false));
assertTrue(LightningArrowRules.shouldTriggerImpact(true, false, true, true, false, true));
assertFalse(LightningArrowRules.shouldTriggerImpact(true, false, true, true, false, false));
assertEquals(8.0, Values.defaults().lightningStormRadius());
assertEquals(100, Values.defaults().lightningStormDurationTicks());
assertEquals(10.0, Values.defaults().lightningBonusDamage());
assertEquals(8.0, Values.defaults().lightningChainBonusDamage());
assertEquals(6, Values.defaults().lightningMaxChainTargets());
```

Construct invalid values and assert finite positive radii/damage, at least one duration/interval tick, a bounded chain cap, and non-negative fire seconds. Parse legacy JSON and assert every missing field receives the new default while `lightningDamageMode` remains readable.

- [ ] **Step 2: Run the focused tests and verify RED**

Run:

```powershell
.\gradlew.bat test --tests "*LightningArrowRulesTest" --tests "*ServerScanConfigTest"
```

Expected: compilation failures for the missing `shouldTriggerImpact` method and missing config accessors.

- [ ] **Step 3: Implement the minimal rule and immutable config expansion**

Implement the gate:

```java
public static boolean shouldTriggerImpact(boolean armed, boolean triggered, boolean playerOwner,
        boolean hasChargeOrInfinite, boolean entityImpact, boolean blockImpact) {
    return armed && !triggered && playerOwner && hasChargeOrInfinite && (entityImpact || blockImpact);
}
```

Append the eleven new fields to `Values`, populate them in `defaults()` and `fromJson()`, and sanitize with explicit limits: radii `0.1..64`, damage `0..2048`, chain targets `0..64`, storm duration `1..12000`, interval `1..1200`, and fire seconds `0..300`. Increment the config version without changing legacy scan migrations.

- [ ] **Step 4: Re-run focused tests and verify GREEN**

Run the same focused command. Expected: all selected tests pass.

### Task 2: Pure storm state and target policy

**Files:**
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/lightning/LightningStormStateTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/lightning/LightningTargetRulesTest.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStormState.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningTargetRules.java`

- [ ] **Step 1: Write failing lifecycle and policy tests**

Test a 100-tick state with interval 10: scans occur on ticks 10 through 100, expiry occurs at tick 100, and a marked UUID is rejected forever when repeats are disabled. Test repeat mode with cooldown 40: reject at ages 0..39 and allow at age 40. Test pure target facts for owner exclusion, player toggle, creative/spectator exclusion, dead/removed exclusion, already-struck exclusion, exact-radius inclusion, nearest-first sorting, and a six-target cap.

- [ ] **Step 2: Run focused tests and verify RED**

```powershell
.\gradlew.bat test --tests "*LightningStormStateTest" --tests "*LightningTargetRulesTest"
```

Expected: compile failure because both production classes are absent.

- [ ] **Step 3: Implement minimal pure classes**

Use this lifecycle API:

```java
final class LightningStormState {
    LightningStormState(int durationTicks, int scanIntervalTicks,
        boolean allowRepeatStrike, int repeatCooldownTicks) { ... }
    boolean tickAndShouldScan() { ... }
    boolean isExpired() { ... }
    int ageTicks() { ... }
    boolean canStrike(UUID uuid) { ... }
    void recordStrike(UUID uuid) { ... }
}
```

Use a pure candidate record containing UUID, squared distance, alive/removed/player/creative/spectator flags, then filter and sort without Minecraft dependencies:

```java
record Candidate(UUID uuid, double squaredDistance, boolean alive, boolean removed,
    boolean player, boolean creative, boolean spectator) {}
```

- [ ] **Step 4: Re-run focused tests and verify GREEN**

Run the focused command. Expected: all selected tests pass.

### Task 3: Runtime storm, strike, and impact integration

**Files:**
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStorm.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStormManager.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningImpactController.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/lightning/LightningStrikeController.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/EnhancedBows.java`

- [ ] **Step 1: Add a source-level integration regression test**

Create `LightningStormIntegrationStructureTest` that reads source files and asserts the mixin contains both `onEntityHit` and `onBlockHit` trigger injections, manager registration exists, the strike controller uses `lightningBolt()` and `setOnFireFor`, and scanning bounce cancellation still precedes the final block trigger. This test protects the required integration boundaries that a plain JUnit unit test cannot instantiate safely.

- [ ] **Step 2: Run the structure test and verify RED**

```powershell
.\gradlew.bat test --tests "*LightningStormIntegrationStructureTest"
```

Expected: failure because the manager and impact controller source files do not exist.

- [ ] **Step 3: Implement targeted strike behavior**

Refactor the strike controller into:

```java
public static void visual(ServerWorld world, Vec3d position) { ... }
public static void strike(ServerWorld world, LivingEntity target, float bonusDamage, int fireSeconds) {
    visual(world, target.getPos());
    if (target.isAlive()) target.damage(world.getDamageSources().lightningBolt(), bonusDamage);
    if (target.isAlive() && fireSeconds > 0) target.setOnFireFor(fireSeconds);
}
```

The spawned lightning entity is cosmetic so vanilla does not splash-damage nearby entities.

- [ ] **Step 4: Implement storm runtime and registration**

`LightningStorm` queries `LivingEntity` in `new Box(center, center).expand(radius)`, converts them to policy candidates, checks exact squared distance, sorts nearest-first, caps by `lightningMaxChainTargets`, records UUID before striking, and applies chain damage. `LightningStormManager` stores `IdentityHashMap<ServerWorld, List<LightningStorm>>`, ticks via `ServerTickEvents.END_WORLD_TICK`, drops empty world lists, and clears on server stop.

- [ ] **Step 5: Implement entity and final-block impact orchestration**

`LightningImpactController` validates the one-shot/charge gate, consumes when required, marks the arrow triggered, builds the storm, immediately strikes the primary and nearest chain targets for entity hits, seeds their UUIDs, and only creates a startup visual for block hits. Replace the old entity-only mixin body with controller calls and add a `TAIL` `onBlockHit` injection. The existing cancellable bounce injection remains first; a successful cancellation prevents the final block injection from running.

- [ ] **Step 6: Compile and resolve Yarn 1.21.1 API signatures**

```powershell
.\gradlew.bat compileJava compileClientJava
```

Expected: successful compilation. If Yarn differs, inspect local mapped sources and adjust only the affected call signatures.

- [ ] **Step 7: Run the integration and existing lightning tests**

```powershell
.\gradlew.bat test --tests "*Lightning*"
```

Expected: all Lightning tests pass.

### Task 4: Cloth Config and localization

**Files:**
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/LightningConfigScreenResourcesTest.java`

- [ ] **Step 1: Write a failing resource/UI test**

Assert both locale files contain keys for storm radius, duration, interval, primary damage, chain damage, chain limit, strike players, strike owner, and repeat strikes. Assert the screen source references every new accessor and no longer constructs an entry for `config.enhanced-bows.lightning_damage_mode`.

- [ ] **Step 2: Run the resource test and verify RED**

```powershell
.\gradlew.bat test --tests "*LightningConfigScreenResourcesTest"
```

Expected: failures for missing translation keys and UI references.

- [ ] **Step 3: Add entries and drafts**

Add numeric entries with the same limits as server sanitization and boolean toggles for player, owner, and repeat policy. Keep `lightningDamageMode` in `ServerDraft` and `toValues()` only for compatibility; remove its visible string field entry. Write clear Chinese labels matching the user's requested terms and equivalent English labels.

- [ ] **Step 4: Re-run the resource test and compile client code**

```powershell
.\gradlew.bat test --tests "*LightningConfigScreenResourcesTest"
.\gradlew.bat compileClientJava
```

Expected: test and client compilation pass.

### Task 5: Full regression and runtime verification

**Files:**
- Review all modified files.

- [ ] **Step 1: Run all unit/resource tests**

```powershell
.\gradlew.bat test
```

Expected: zero failed tests.

- [ ] **Step 2: Run full Gradle build**

```powershell
.\gradlew.bat build
```

Expected: `BUILD SUCCESSFUL` with remapped production and source jars.

- [ ] **Step 3: Run a bounded dedicated-server smoke test**

Ensure `run/eula.txt` contains `eula=true`, launch `gradlew.bat runServer --no-daemon`, wait for the normal server-ready log line, then stop it cleanly. Expected: mod initialization succeeds with no client-class loading or mixin crash.

- [ ] **Step 4: Inspect final scope and diff**

```powershell
git status -sb
git diff --check
git diff --stat
```

Confirm no unrelated files changed, old charge/HUD/scan/trail code remains intact, and report interactive in-game checks as unverified unless actually performed.
