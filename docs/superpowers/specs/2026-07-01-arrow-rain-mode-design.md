# Arrow Rain Mode Design

## Scope

Add a level-one, bow-only `enhanced-bows:arrow_rain` enchantment and a server-authoritative per-player mode toggled through a configurable client key binding. A qualifying entity or block impact schedules a bounded four-wave rain of ordinary arrows; hitting a creature is not required. Preserve the existing spectral-arrow scan, Lightning storm/charges/HUD, Burst explosion, bounce, trail, and configuration navigation.

Rain arrows are plain, non-pickup projectiles. They inherit only the triggering projectile's base damage multiplied by `arrowRainDamageMultiplier`; they do not copy potion effects, fire, critical state, or other ammunition effects.

## Enchantment Registration and Compatibility

Create `data/enhanced-bows/enchantment/arrow_rain.json` with bow-only supported and primary items, maximum level 1, normal combat-enchantment costs, and an exclusive-set reference that rejects Burst. Add Arrow Rain to the existing enchanting-table, non-treasure, random-loot, and tradeable enchantment tags and add an enchanted book to the combat creative tab.

Crossbows remain unsupported through both the bow item tag and the runtime `BowItem` check.

The compatibility tags must express these exact relationships:

- Lightning and Burst are incompatible.
- Arrow Rain and Burst are incompatible.
- Lightning and Arrow Rain are compatible.
- Arrow Rain remains compatible with ordinary vanilla bow enchantments, subject to vanilla's own rules.

Use separate per-enchantment exclusive-set tags rather than placing all three enchantments in one shared group. Burst's set contains Lightning and Arrow Rain; Lightning's set rejects Burst; Arrow Rain's set rejects Burst. Resource tests must call the actual 1.21.1 combination semantics in both ordering directions or assert the equivalent resolved tag relationships.

If a command-created bow contains Burst and Arrow Rain, the projectile spawn boundary logs a warning and disables Arrow Rain. Existing Lightning priority remains intact. A bow containing Lightning and Arrow Rain can arm both independent effects.

## Player Mode and Cooldown Component

Create a dedicated CCA player component rather than adding unrelated state to the Lightning charge component. It stores:

- `arrowRainModeEnabled`, default `false`;
- `arrowRainCooldownTicks`, default `0`.

Register it with `RespawnCopyStrategy.ALWAYS_COPY`. The component implements automatic synchronization and server ticking. The server decrements cooldown once per tick and syncs on mode changes, cooldown start, whole-second boundaries, and completion so the HUD remains accurate without sending a packet every tick.

The component is the only authority for mode and cooldown. Its client copy is display-only. Mode state is retained across death and reconnect through CCA persistence.

## Key Binding and Toggle Protocol

Register `key.enhanced-bows.toggle_arrow_rain` with `KeyBindingHelper`, default keyboard key V, under the mod's own Enhanced Bows category. The key remains rebindable through Minecraft controls.

On key press, the client sends an empty C2S toggle request. The server checks the player's main hand and off hand for a `BowItem` with Arrow Rain and applies these decisions:

1. No qualifying bow: do not change mode and return `requires_arrow_rain_bow`.
2. A qualifying bow also has Burst and the request would enable the mode: do not change mode and return `burst_conflict`.
3. Otherwise toggle the component, synchronize it, and return `enabled` or `disabled`.

Use an S2C result payload so client feedback follows the server decision. Successful toggles display `箭雨模式：已开启` or `箭雨模式：已关闭` and play the vanilla UI button-click sound. Rejections display the requested localized explanation without changing local state or playing a success sound.

If mode is already enabled and the player later holds an illegal Burst plus Arrow Rain bow, the server refuses to arm Arrow Rain. Switching the mode off remains allowed so players cannot become stuck in the enabled state.

## Projectile Arming and One-Shot State

Add an `ArrowRainArrowAccess` interface to `PersistentProjectileEntityMixin` with persisted state:

- `enhancedbows:arrow_rain_armed`;
- `enhancedbows:arrow_rain_triggered`;
- `enhancedbows:arrow_rain_owner_uuid`;
- `enhancedbows:arrow_rain_base_damage`;
- `enhancedbows:arrow_rain_child`;
- a bounded child lifetime counter.

At the existing `RangedWeaponItem.shootAll` spawn boundary, arm Arrow Rain only when all conditions hold:

- server configuration enables Arrow Rain;
- the weapon is a bow with the Arrow Rain enchantment;
- the shooter is a server player whose mode is enabled;
- the player's Arrow Rain cooldown is zero;
- the bow does not contain Burst;
- the projectile is not an Arrow Rain child;
- the projectile is not spectral unless `arrowRainAllowSpectralArrow=true`.

Arming captures the mode and damage decision at shot time. Turning the mode off after firing does not retroactively disarm an arrow. An entity hit uses the hit entity's current `entity.getPos()` as the rain center. A final block hit uses `BlockHitResult.getPos()` as the rain center, making nearby ground or walls the primary suppression-zone trigger. The configured `arrowRainTriggerOnEntityHit` and `arrowRainTriggerOnBlockHit` flags independently gate those impact types.

Entity and final block impacts call one shared server controller. On the first enabled qualifying impact while the owner is ready, atomically start the owner's cooldown, set `arrow_rain_triggered=true`, and schedule the rain. The projectile remains untriggered when an impact type is disabled, so a later enabled impact can still qualify. Once a rain is scheduled, piercing, reflection, duplicated callbacks, and long-lived arrows cannot trigger that projectile again. Other armed arrows that land during cooldown are marked spent, create no rain, and do not restart cooldown.

Misses do not trigger Arrow Rain, do not start cooldown, and consume no Arrow Rain resource. `arrowRainTriggerOnMiss` remains an explicit default-false compatibility setting, but this design does not add a miss-detection callback; setting it to true is reserved for a future implementation rather than silently treating projectile expiry or chunk unload as a miss.

Spectral arrows remain excluded by default. Their scan activation, red trail, bounce, and obstruction behavior are unchanged. When the configuration explicitly allows them, a successful scanning bounce still cancels the final block-impact callback and therefore does not create rain at the bounce point.

## Arrow Rain Scheduling and Projectiles

Use a server-world manager registered on `ServerTickEvents.END_WORLD_TICK`. Each active rain records its center, owner UUID, base damage, configured radius, height, duration, wave count, total arrow count, and next-wave schedule.

Distribute the configured total count exactly across configured waves. The first `count % waves` waves receive one additional arrow. For the defaults, four waves each spawn six arrows at evenly distributed ticks across the 40-tick duration.

Each rain arrow:

- spawns at a random horizontal position inside a circle of radius 8 centered 14 blocks above the impact;
- points downward with small horizontal variance;
- is a normal `ArrowEntity` using ordinary arrow ammunition;
- has owner attribution when the player can be resolved;
- uses triggering projectile base damage multiplied by 0.7;
- cannot be picked up;
- is explicitly marked as an Arrow Rain child and is never armed for Arrow Rain;
- is discarded after a short bounded lifetime, defaulting to 100 ticks after spawn.

Do not copy tipped-arrow effects, spectral behavior, Flame fire, critical state, or Burst/Lightning/Arrow Rain arming data to rain children. The manager removes completed rain schedules immediately and removes empty per-world lists.

## Lightning and Burst Interaction

Arrow Rain state is independent of existing Lightning state. A bow with both Lightning and Arrow Rain can arm both flags on the same projectile. Entity or final block impact invokes both controllers; Lightning consumes its own charge and Arrow Rain consumes its own cooldown. Their triggered flags remain separate.

Burst and Arrow Rain cannot coexist through normal enchanting or anvils. At runtime, the mere presence of Burst on an illegal bow disables Arrow Rain arming and logs a warning. This applies even if Burst is disabled in server configuration, because the conflict protects terrain and performance rather than choosing between two enabled effects.

For an illegal three-enchantment bow, existing Lightning priority applies, Burst does not trigger, and Arrow Rain is disabled because Burst is present. Only Lightning may run.

## Configuration

Append these server-authoritative gameplay settings to `ServerScanConfig.Values`, persistence, defaults, sanitization, migration, and Cloth Config:

- `enableArrowRainEnchantment = true`
- `arrowRainRadius = 8.0`
- `arrowRainArrowCount = 24`
- `arrowRainHeight = 14.0`
- `arrowRainDurationTicks = 40`
- `arrowRainWaves = 4`
- `arrowRainCooldownTicks = 200`
- `arrowRainDamageMultiplier = 0.7`
- `arrowRainTriggerOnBlockHit = true`
- `arrowRainTriggerOnEntityHit = true`
- `arrowRainTriggerOnMiss = false`
- `arrowRainAllowSpectralArrow = false`

Sanitize finite numeric values to practical non-negative bounds, ensure waves do not exceed arrow count when the count is positive, and cap count, waves, radius, height, duration, and cooldown to prevent accidental server overload.

Add `enableArrowRainHud = true` to the client-owned `ClientScanConfig`, because it controls presentation only. The Cloth Config page exposes localized Arrow Rain gameplay settings and the client HUD toggle while retaining existing Lightning and Burst options.

## HUD

Implement the renderer strictly under `src/client`. It reads the synchronized Arrow Rain component and checks both hands for an Arrow Rain bow. If no such bow is held or the client HUD setting is disabled, render nothing.

Display one localized line:

- mode off: `箭雨：关闭`;
- mode on and ready: `箭雨：开启`;
- mode on during cooldown: `箭雨：冷却 %ss`, rounding remaining ticks up.

Use a fixed small scale and the existing Lightning HUD anchor. If the held bow also has Lightning and the Lightning HUD is enabled, offset Arrow Rain downward enough to avoid overlap; otherwise use the base Lightning HUD position. No client class is referenced from common/server code.

## Failure and Performance Behavior

All mode changes, arming decisions, cooldown consumption, rain scheduling, and spawned projectile creation occur on the logical server. Malformed or repeated toggle packets re-run the same held-item validation. Missing owners prevent owner attribution but do not crash or make rain recursive.

The configured arrow count and active duration are bounded. No custom synchronized entity or per-arrow client packet is required; vanilla projectile spawning handles visuals. Arrow Rain children have a short explicit lifetime and cannot be picked up.

## Verification

Automated tests cover:

- Arrow Rain resource schema, bow-only support, level, source tags, translations, and creative book;
- Burst/Arrow Rain incompatibility in both directions and Lightning/Arrow Rain compatibility;
- toggle validation, Burst rejection, death persistence, cooldown ticking, sync boundaries, and packet registration;
- default-enabled block/entity impact policies, default-disabled miss policy, block-hit and entity-hit center selection, successful-trigger-only cooldown, default spectral exclusion, config-enabled spectral support, child rejection, one-shot impact rules, and illegal-combination warning;
- exact wave/count distribution, duration schedule, circular spawn sampling bounds, damage scaling, no recursive arming, non-pickup state, and cleanup marker;
- independent Lightning and Arrow Rain arming/triggered flags;
- HUD hidden/closed/open/cooldown text and Lightning offset;
- configuration defaults, sanitization, migration, and localized Cloth Config entries;
- regression coverage for scan, bounce, trail, Lightning, and Burst.

Fresh verification runs `gradlew.bat test`, `gradlew.bat clean build`, a dedicated-server startup-to-ready and clean-stop smoke test, and a client resource/HUD/key-binding startup smoke test. Manual in-game checks explicitly cover ground-hit suppression without a creature target, an entity-centered rain, a deliberate miss with no cooldown, an eight-block coverage area, spectral-arrow scan regression, and combined Lightning plus Arrow Rain behavior.
