# Scan Text HUD and Custom Sounds Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace scan frame animations and bundled audio with a two-second text HUD, vanilla sound fallbacks, and a client-only OGG/MP3/WAV import workflow backed by a generated resource pack.

**Architecture:** Server gameplay remains authoritative and emits small S2C cue packets. Client-only state renders text, resolves custom-or-vanilla sounds, and runs file import/FFmpeg conversion on a dedicated executor. Pure import/config rules remain isolated and unit-testable while all Minecraft client APIs stay in `src/client`.

**Tech Stack:** Java 21, Fabric Loader/API 1.21.1, Yarn 1.21.1+build.3, Cloth Config, Mod Menu, JUnit 5, Java `ProcessBuilder` and NIO.

---

### Task 1: Client config migration and text HUD state

**Files:**
- Modify: `build.gradle`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ClientScanConfig.java`
- Replace: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ScanHudState.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ScanHudRenderer.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/config/ClientScanConfigTest.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/hud/ScanHudStateTest.java`

- [ ] **Step 1: Make JUnit tests see client output and write failing config/state tests**

Add `sourceSets.test` client output to the test classpath, then assert this wished-for API:

```java
ClientScanConfig.Values defaults = ClientScanConfig.Values.defaults();
assertTrue(defaults.enableScanTextHud());
assertEquals(40, defaults.scanTextHudY());
assertTrue(defaults.enableScanSounds());
assertFalse(defaults.useCustomScanStartSound());
assertFalse(defaults.useCustomDetectedSound());
assertFalse(defaults.useCustomBounceSound());
```

Parse legacy animation JSON and assert it produces only the six new fields. For HUD state, assert `startScanning()` and `startDetected()` last exactly 40 ticks and detected takes priority.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew test --tests "*ClientScanConfigTest" --tests "*ScanHudStateTest"`

Expected: compilation fails because the new record fields and text-state API do not exist.

- [ ] **Step 3: Implement minimal config and text state**

Use this record shape:

```java
public record Values(
    boolean enableScanSounds,
    boolean useCustomScanStartSound,
    boolean useCustomDetectedSound,
    boolean useCustomBounceSound,
    boolean enableScanTextHud,
    int scanTextHudY
) {
    public static Values defaults() {
        return new Values(true, false, false, false, true, 40);
    }
}
```

`fromJson` reads only these keys and clamps Y to `0..10000`. Replace frame state with a `Message` enum (`SCANNING`, `DETECTED`) and two 40-tick counters. Renderer uses `drawCenteredTextWithShadow` at `scanTextHudY` and translatable text keys.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run: `./gradlew test --tests "*ClientScanConfigTest" --tests "*ScanHudStateTest"`

Expected: both test classes pass.

### Task 2: Config-controlled vanilla/custom sound cues

**Files:**
- Delete: `src/main/java/io/github/ikunkk02/enhancedbows/sound/ModSounds.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/EnhancedBows.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/network/ScanSoundCue.java`
- Create: `src/main/java/io/github/ikunkk02/enhancedbows/network/ScanSoundCueS2CPayload.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/network/ModNetworking.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/mixin/PersistentProjectileEntityMixin.java`
- Modify: `src/main/java/io/github/ikunkk02/enhancedbows/scan/SpectralScanController.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/network/ClientScanNetworking.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/sound/ClientScanSoundPlayer.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/network/ScanSoundCueTest.java`

- [ ] **Step 1: Write failing cue encoding/fallback selection tests**

Assert stable wire IDs:

```java
assertEquals(0, ScanSoundCue.SCAN_START.wireId());
assertEquals(1, ScanSoundCue.DETECTED.wireId());
assertEquals(2, ScanSoundCue.BOUNCE.wireId());
assertEquals(3, ScanSoundCue.MARK_SUCCESS.wireId());
assertNull(ScanSoundCue.fromWireId(999));
```

- [ ] **Step 2: Run cue test and verify RED**

Run: `./gradlew test --tests "*ScanSoundCueTest"`

Expected: compilation fails because `ScanSoundCue` does not exist.

- [ ] **Step 3: Implement payload and server emission**

Encode the enum as `VAR_INT`. Replace direct bounce `world.playSound` with packet sends to `PlayerLookup.tracking(projectile)`. `SpectralScanController.scan` tracks all first-seen target UUIDs and emits one `MARK_SUCCESS` cue to the owner per newly found target while retaining the one-time detected-player packet.

- [ ] **Step 4: Implement client fallback resolution**

`ClientScanSoundPlayer` maps cues to:

```java
SCAN_START -> BLOCK_BEACON_ACTIVATE
DETECTED -> ENTITY_EXPERIENCE_ORB_PICKUP
BOUNCE -> BLOCK_AMETHYST_BLOCK_HIT
MARK_SUCCESS -> BLOCK_NOTE_BLOCK_PLING
```

For the first three, check the matching `useCustom...` flag and whether the sound manager resolves `enhanced_bows:custom.<slot>`; otherwise play the vanilla event. Catch runtime resource/playback errors and play vanilla. Existing start/detected packets trigger both HUD and their sound cue.

- [ ] **Step 5: Run focused and existing tests**

Run: `./gradlew test --tests "*ScanSoundCueTest" --tests "*ScanRulesTest" --tests "*BouncePhysicsTest"`

Expected: all selected tests pass.

### Task 3: Resource-pack writer and asynchronous FFmpeg import service

**Files:**
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/sound/CustomSoundSlot.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/sound/AudioImportResult.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/sound/CustomSoundImportService.java`
- Create: `src/test/java/io/github/ikunkk02/enhancedbows/client/sound/CustomSoundImportServiceTest.java`

- [ ] **Step 1: Write failing import tests using temporary directories and a fake process runner**

Cover:

```java
assertTrue(service.supports(Path.of("voice.ogg")));
assertTrue(service.supports(Path.of("voice.MP3")));
assertTrue(service.supports(Path.of("voice.wav")));
assertFalse(service.supports(Path.of("voice.flac")));
```

Test direct OGG copy to both fixed destinations and generated `pack.mcmeta`/`sounds.json`. For MP3 and WAV, inject a fake runner that writes valid non-empty bytes to the last command argument; assert the command contains `-ac 1 -ar 44100 -c:a libvorbis -q:a 4`. Test local `config/enhanced_bows/tools/ffmpeg.exe` priority, PATH fallback, missing FFmpeg, timeout, nonzero exit and preservation of an existing target.

- [ ] **Step 2: Run import tests and verify RED**

Run: `./gradlew test --tests "*CustomSoundImportServiceTest"`

Expected: compilation fails because the import service API does not exist.

- [ ] **Step 3: Implement synchronous core plus async boundary**

Use fixed slots:

```java
SCAN_START("scan_start.ogg", "custom.scan_start")
DETECTED("detected.ogg", "custom.detected")
BOUNCE("bounce.ogg", "custom.bounce")
```

The production runner uses `new ProcessBuilder(command).redirectErrorStream(true)`, drains output, waits with a finite timeout, forcibly destroys on timeout, and never constructs a shell command string. `importAsync` runs on a daemon single-thread executor. Convert/copy into temporary files, require nonzero size, then replace final files; failures leave old targets intact.

- [ ] **Step 4: Generate exact resource-pack metadata**

Write format 34 `pack.mcmeta` and this mapping:

```json
{
  "custom.scan_start": {"sounds": ["enhanced_bows:custom/scan_start"]},
  "custom.detected": {"sounds": ["enhanced_bows:custom/detected"]},
  "custom.bounce": {"sounds": ["enhanced_bows:custom/bounce"]}
}
```

- [ ] **Step 5: Run import tests and verify GREEN**

Run: `./gradlew test --tests "*CustomSoundImportServiceTest"`

Expected: all OGG, MP3, WAV, lookup and failure-path tests pass.

### Task 4: Import Screen, Cloth Config wiring, and animation/resource removal

**Files:**
- Delete: `src/client/java/io/github/ikunkk02/enhancedbows/client/hud/ScanAnimationResources.java`
- Delete: `src/client/java/io/github/ikunkk02/enhancedbows/client/screen/ScanHudEditorScreen.java`
- Create: `src/client/java/io/github/ikunkk02/enhancedbows/client/screen/CustomSoundImportScreen.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/EnhancedBowsClient.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/config/ScanConfigScreenFactory.java`
- Modify: `src/client/java/io/github/ikunkk02/enhancedbows/client/mixin/ScreenAccessor.java`
- Modify: `src/client/resources/assets/enhanced-bows/lang/en_us.json`
- Modify: `src/client/resources/assets/enhanced-bows/lang/zh_cn.json`
- Modify: `src/main/resources/fabric.mod.json`
- Delete: `src/client/resources/assets/enhanced-bows/sounds.json`
- Delete: `src/client/resources/assets/enhanced-bows/sounds/`
- Delete: `src/client/resources/assets/enhanced-bows/textures/gui/scanning/`
- Delete: `src/client/resources/assets/enhanced-bows/textures/gui/detected/`

- [ ] **Step 1: Replace config controls and open button**

Remove all animation toggles/coordinates/scales. Add text HUD, Y, sound and three custom toggles. Reuse the after-init accessor only to add “打开自定义音效导入界面”. Remove animation editor keybinding and frame reload listener from the client initializer.

- [ ] **Step 2: Implement three drag zones and status messages**

`CustomSoundImportScreen.filesDragged(List<Path>)` selects a slot by current mouse Y / zone bounds, rejects unsupported suffixes with the exact localized message, disables repeated work while busy, invokes `importAsync`, and returns the completion through `client.execute`. On success it updates only the matching `useCustom...` field and saves config.

- [ ] **Step 3: Add resource reload and guidance**

Add “重新加载资源” calling `client.reloadResources()` and display “自定义音效已导入，请点击重新加载资源” plus “资源包列表中启用 EnhancedBows Custom Sounds”. Surface missing FFmpeg and conversion failure messages without throwing.

- [ ] **Step 4: Delete obsolete assets/classes and update metadata/lang**

Remove every frame and bundled OGG. Update `fabric.mod.json` description to text HUD/custom sounds. Remove obsolete editor and animation translation keys; add Chinese and English text/import/status keys.

- [ ] **Step 5: Compile client code**

Run: `./gradlew compileClientJava processClientResources`

Expected: build succeeds with no references to deleted animation classes or assets.

### Task 5: Full regression and runtime verification

**Files:**
- Modify as required only when a verification failure identifies a scoped defect.

- [ ] **Step 1: Static removal audit**

Run searches for deleted config names, `frame_000`, `ScanAnimationResources`, `ScanHudEditorScreen`, `.mov`, old sound IDs and bundled OGG/PNG resources. Expected: no production/resource matches.

- [ ] **Step 2: Run all tests**

Run: `./gradlew test`

Expected: all JUnit tests pass with zero failures.

- [ ] **Step 3: Run clean build and inspect jar**

Run: `./gradlew clean build`

Inspect the final jar. Expected: no scan/detected PNG sequences and no bundled custom OGG files; client text/import classes and common payloads are present.

- [ ] **Step 4: Exercise real FFmpeg conversion when available**

Detect local/config/PATH FFmpeg. Generate short WAV and MP3 fixtures outside source control, import/convert them through the tested service or a focused integration harness, and verify mono Vorbis 44.1 kHz outputs. If FFmpeg is unavailable, verify the exact missing-tool result path and report the unavailable live conversion separately.

- [ ] **Step 5: Dedicated-server smoke test**

Run: `./gradlew runServer --no-daemon`, wait until the server reports completion/startup, then issue `stop` through stdin.

Expected: no client HUD, Screen, sound-manager or FFmpeg classloading errors.

- [ ] **Step 6: Client smoke test**

Run: `./gradlew runClient --no-daemon` long enough to reach the title screen and inspect `latest.log` for resource, mixin and classloading failures. Manual in-world drag-and-drop/gameplay interactions that cannot be automated are explicitly listed as remaining checks.
