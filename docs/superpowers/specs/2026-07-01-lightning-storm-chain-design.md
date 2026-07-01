# Lightning Storm and Chain Strike Design

## Scope

Replace only the post-impact effect of the existing Lightning enchantment. Preserve the enchantment definition, two-charge component, 20-second recharge, synchronized HUD, scanning spectral-arrow behavior, bounce behavior, red trail, and scan line-of-sight rules.

An armed arrow consumes one available charge only when it hits a living entity or reaches a final block impact. A miss consumes nothing. Each projectile can create at most one storm, including scanning spectral arrows that bounce.

## Selected Architecture

Use a logical-server `LightningStormManager` registered on the server-world tick event. The manager owns lightweight in-memory storm records rather than spawning a custom entity or tying storm lifetime to an arrow. This keeps authority server-side, avoids entity synchronization and persistence overhead, and lets the storm finish even after the projectile is removed.

`PersistentProjectileEntityMixin` remains the single projectile impact boundary. Entity hits and final block hits call a shared trigger service. The existing `lightningArmed` and `lightningTriggered` state remains persisted in projectile NBT; owner identity comes from the projectile owner and is copied into each storm record.

## Impact Semantics

### Entity impact

1. Require an armed, not-yet-triggered, server-owned player arrow and a live `LivingEntity` hit target.
2. Require a charge, except when the existing creative-infinite option applies.
3. Mark the arrow triggered before applying effects, then consume exactly one charge when consumption is required.
4. Create a storm centered on the target's current position.
5. Immediately strike the hit target as the primary target.
6. Find other eligible living entities within `lightningChainRadius`, sort by squared distance to the center, and strike at most `lightningMaxChainTargets` of them as chain targets.
7. Seed the storm's struck-target state with every immediate target so the periodic scan does not strike them again when repeat strikes are disabled.

### Block impact

1. Require an armed, not-yet-triggered, server-owned player arrow and an available charge under the same rules.
2. If an activated scanning spectral arrow is performing a valid bounce, bounce handling takes precedence and no charge is consumed. When it can no longer bounce and reaches the normal block-impact path, trigger Lightning once.
3. Mark the arrow triggered, consume exactly one charge, and create a storm centered at `BlockHitResult.getPos()`.
4. Spawn one cosmetic vanilla lightning bolt at the impact point as the storm-start cue. This startup bolt deals no custom damage.
5. Targets found by later storm scans use chain-target damage.

If charge consumption fails because state changed between the gate and mutation, no visual, storm, or damage is created. A projectile that is already marked triggered always returns without consuming again.

## Storm Model and Lifecycle

Each storm record contains:

- its `ServerWorld` ownership through the manager's per-world collection;
- immutable center position and owner UUID;
- remaining duration ticks;
- radius and scan interval copied from the configuration at creation time;
- elapsed tick state for deterministic scans;
- a set of struck entity UUIDs;
- repeat-strike policy and a per-entity last-strike tick map when repeats are enabled;
- repeat cooldown of 40 ticks.

The manager ticks at `ServerTickEvents.END_WORLD_TICK`. It decrements lifetime every world tick, removes expired storms, and scans on the configured interval. A default duration of 100 ticks gives five seconds of logical-server lifetime, and a default interval of 10 ticks scans twice per second.

The scan uses an axis-aligned box expanded by the storm radius, then applies an exact squared-distance check. It accepts living, alive, non-removed entities and applies these policy filters:

- owner is excluded unless `lightningStrikeOwner` is true;
- all players are excluded when `lightningStrikePlayers` is false;
- creative and spectator players are always excluded;
- a UUID already struck by this storm is excluded when repeat strikes are disabled;
- when repeat strikes are enabled, the same UUID is eligible only after 40 ticks.

To bound work, each periodic scan sorts eligible entities by distance and strikes no more than `lightningMaxChainTargets`. Targets that remain unselected can be selected by a later scan. Storm records are runtime-only and are discarded on server/world shutdown; five-second regions do not need save persistence.

## Strike and Damage Semantics

`LightningStrikeController` separates visual creation from targeted damage:

- every strike spawns a cosmetic vanilla `LightningEntity`, preserving the bolt, thunder, and visual effect without uncontrolled vanilla area damage;
- the primary entity target receives `lightningBonusDamage`, default `10.0`, using `world.getDamageSources().lightningBolt()`;
- immediate chain targets and block-storm scan targets receive `lightningChainBonusDamage`, default `8.0`, using the same damage source;
- a surviving target is set on fire for `lightningSetFireSeconds`, default 3 seconds.

Damage and fire are server-authoritative. The cosmetic bolt must not damage the owner or unrelated bystanders. Owner/player eligibility is checked before spawning a target bolt.

## Configuration and Migration

Add these server-owned settings with sanitization and Cloth Config Chinese and English labels:

- `lightningChainRadius = 8.0`
- `lightningMaxChainTargets = 6`
- `lightningBonusDamage = 10.0`
- `lightningChainBonusDamage = 8.0`
- `lightningStormDurationTicks = 100`
- `lightningStormScanIntervalTicks = 10`
- `lightningStormRadius = 8.0`
- `lightningStrikeOwner = false`
- `lightningStrikePlayers = true`
- `lightningSetFireSeconds = 3`
- `lightningAllowRepeatStrikeInSameStorm = false`

The existing `lightningDamageMode` field remains accepted, serialized, and present in the immutable config record for backward compatibility, but is removed from the Cloth Config screen and no longer controls damage. Existing charge, recharge, death-retention, creative-infinite, HUD, and scanning options remain unchanged.

Sanitization enforces finite positive radii and damage, a duration and interval of at least one tick, non-negative target/fire limits, and practical upper bounds to prevent accidental pathological scans.

## Components and Boundaries

- `LightningImpactController`: owns charge gating, one-shot impact activation, immediate primary/chain orchestration, and storm creation.
- `LightningStormManager`: owns per-world storm collections and tick lifecycle.
- `LightningStorm`: owns pure lifetime, repeat-policy bookkeeping, and scan eligibility state.
- `LightningTargetRules`: pure player/owner/alive filtering and distance ordering helpers suitable for unit tests.
- `LightningStrikeController`: owns cosmetic bolt spawning plus explicit targeted damage/fire.
- `PersistentProjectileEntityMixin`: adapts vanilla entity/block callbacks to `LightningImpactController`; retains NBT one-shot state.
- `ServerScanConfig` and `ScanConfigScreenFactory`: own defaults, migration, validation, persistence, and UI entries.

The scanning controller, spectral-arrow trail mixin, HUD renderer, and charge recharge logic are not modified except where compilation requires new config constructor arguments.

## Failure Handling and Performance

Null bolt creation skips only that visual; explicit target damage still runs. Invalid or unavailable owners prevent activation. An empty scan has no side effects. Each storm uses one bounded entity query per scan, an exact radius filter, and a configurable target cap. Default behavior allows at most six targeted strikes per scan and only one strike per entity per storm.

No particles beyond vanilla cosmetic lightning are required for this change. This satisfies the requested fallback and avoids additional packet volume.

## Verification

Automated tests will cover:

- entity and block impacts being eligible while misses are not;
- the one-shot gate and charge-consumption rules;
- owner, player, creative, spectator, dead, and repeat-target filtering;
- nearest-first ordering and the six-target cap;
- 100-tick expiry, 10-tick scan cadence, and 40-tick repeat cooldown;
- all new configuration defaults, JSON migration, and sanitization;
- retained enchantment, HUD, scanning, bounce, red-trail, and line-of-sight regression tests.

Fresh verification will run `gradlew.bat test`, `gradlew.bat build`, and a bounded `gradlew.bat runServer` startup smoke test. Runtime-only behaviors that cannot be proven without interactive gameplay will be reported explicitly rather than claimed from compilation alone.
