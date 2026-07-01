# Burst Enchantment Design

## Scope

Add a new level-one, bow-only `enhanced-bows:burst` enchantment. Burst arrows explode once when they hit a living entity or reach a final block impact. Preserve the existing Lightning storm, Lightning charges/HUD, scanning spectral-arrow detection, red trail, bounce, and line-of-sight behavior.

Spectral arrows are permanently excluded from Burst because they are reserved for scanning. The serialized `burstExcludeSpectralArrows` setting remains present and visible as a compatibility/policy statement, but sanitization forces it to `true` and the Cloth Config screen does not allow disabling it.

## Enchantment Registration and Availability

Create `data/enhanced-bows/enchantment/burst.json` with:

- description key `enchantment.enhanced_bows.burst`;
- `supported_items` and `primary_items` set to `#minecraft:enchantable/bow`;
- maximum level 1;
- normal enchanting costs, weight, and anvil cost appropriate to a combat enchantment;
- an exclusive set reference shared with Lightning.

Add Burst to the existing `minecraft:in_enchanting_table`, `minecraft:non_treasure`, `minecraft:on_random_loot`, and `minecraft:tradeable` enchantment tags. This makes it available from enchanting tables, ordinary enchanted books, random loot, and librarian trading while keeping it non-treasure. Add a Burst enchanted book beside the existing Lightning book in the combat creative tab.

Crossbows remain unsupported because `#minecraft:enchantable/bow` does not include them and the runtime arming hook checks `BowItem`.

## Lightning Mutual Exclusion

Create `data/enhanced-bows/tags/enchantment/exclusive_set/lightning_burst.json` containing both `enhanced-bows:lightning` and `enhanced-bows:burst`. Add `"exclusive_set": "#enhanced-bows:exclusive_set/lightning_burst"` to both enchantment JSON definitions.

Minecraft 1.21.1 reads each enchantment's `exclusive_set` and enforces `Enchantment.canBeCombined` for enchanting and anvil/book combination paths. The custom set contains no vanilla enchantments, so Burst remains compatible with Power, Flame, Infinity, Punch, Unbreaking, and Mending, subject only to vanilla mutual-exclusion rules among those enchantments themselves.

Runtime code provides a second boundary for illegal command/NBT combinations. At the `RangedWeaponItem.shootAll` projectile-spawn boundary:

1. Read Lightning and Burst levels from the bow stack.
2. If both levels are positive, log `Bow has both Lightning and Burst enchantments. This is not allowed. Lightning takes priority.` and only apply Lightning arming.
3. Otherwise arm exactly the enabled enchantment.

Lightning priority is unconditional for an illegal dual-enchantment stack. If Lightning is disabled in server configuration, that illegal stack produces neither effect rather than silently switching priority to Burst.

## Projectile State and Impact Semantics

Add `BurstArrowAccess` to the existing `PersistentProjectileEntityMixin`. Persist these fields:

- `enhancedbows:burst_arrow`: whether the projectile was armed by a Burst bow;
- `enhancedbows:burst_triggered`: whether its single explosion has already fired;
- `enhancedbows:burst_owner_uuid`: the firing player's UUID for owner immunity after entity reloads.

Only a player-fired persistent projectile created by a `BowItem` can be armed. `SpectralArrowEntity` is rejected before arming regardless of configuration values. This preserves its scan activation, red trail, bounce count, and scan obstruction behavior.

Entity impacts and final block impacts use the same one-shot controller:

- entity impact center is the hit entity's current position;
- block impact center is `BlockHitResult.getPos()`;
- the triggered flag is set before effects are created;
- a miss has no callback and therefore no explosion;
- successful scanning spectral-arrow bounce cancellation prevents the final block-impact callback, although spectral arrows are also unarmed by rule;
- later collision, piercing, reflection, save/reload, or duplicated callbacks return immediately when triggered is true.

Burst has no charge or cooldown system.

## Explosion Architecture

Use a server-only `BurstExplosionController` with two coordinated paths.

### Visual and block explosion

Call the current Yarn `ServerWorld.createExplosion` overload with a custom `ExplosionBehavior` that returns `false` from `shouldDamage` and `0` from `getKnockbackModifier`. This retains vanilla explosion particles, sound, optional fire, and optional block destruction without applying a second uncontrolled entity damage/knockback pass.

- `burstBreakBlocks=false` selects `World.ExplosionSourceType.NONE`.
- `burstBreakBlocks=true` selects `World.ExplosionSourceType.BLOCK`.
- `burstCreateFire` is passed to the vanilla explosion call.

The explosion source is the projectile owner when available for game-event attribution. Entity damage remains entirely in the manual path below.

### Manual entity damage and knockback

The effective entity radius is `burstExplosionPower * 2.0`, matching vanilla explosion entity reach. Query living entities in an expanded box and then apply an exact spherical distance check.

Eligibility rules:

- exclude dead and removed entities;
- exclude the stored owner UUID unless `burstDamageOwner=true`;
- exclude every player when `burstAffectPlayers=false`;
- always exclude creative and spectator players.

For normalized distance `t = clamp(distance / radius, 0, 1)`, damage is:

`damage = burstBaseDamage + (burstMinDamage - burstBaseDamage) * t`

With defaults, the center receives 8 damage and the outer edge receives 2 damage. Use `world.getDamageSources().explosion(owner, owner)` when the owner entity is available and an unattributed explosion source otherwise.

Knockback direction is the normalized vector from the explosion center to the target body center. Magnitude is `(1 - t) * burstKnockbackMultiplier`; zero-length vectors do not receive an invalid velocity. Damage and knockback occur only on the logical server.

## Configuration

Append these server-owned settings to `ServerScanConfig.Values`, JSON loading, defaults, sanitization, and the existing Cloth Config Lightning/settings page:

- `enableBurstEnchantment = true`
- `burstExplosionPower = 2.5`
- `burstBreakBlocks = false`
- `burstCreateFire = false`
- `burstDamageOwner = false`
- `burstAffectPlayers = true`
- `burstExcludeSpectralArrows = true` (forced true)
- `burstKnockbackMultiplier = 1.0`
- `burstBaseDamage = 8.0`
- `burstMinDamage = 2.0`

Use finite non-negative bounds and practical upper limits. Ensure `burstMinDamage` cannot exceed the sanitized base damage. Increment the overall config version without changing the existing fixed scan-migration cutoff.

Cloth Config adds a Burst category or a clearly separated Burst section with Chinese and English labels. The spectral-arrow entry is a disabled true value plus the Chinese explanation `光灵箭默认不触发爆裂，因为它用于探测功能。`

## Components and Boundaries

- `ModEnchantments`: Burst registry key, lookup helper, and creative enchanted book.
- `BurstArrowRules`: pure arming, spectral exclusion, illegal-combination priority, and one-shot impact gates.
- `BurstArrowAccess`: projectile state interface.
- `BurstDamageRules`: pure radius, linear damage, eligibility facts, and knockback magnitude helpers.
- `BurstExplosionController`: world query, vanilla visual/block explosion, manual targeted damage, and knockback.
- `RangedWeaponItemMixin`: chooses exactly one enchantment and arms eligible projectiles before spawn.
- `PersistentProjectileEntityMixin`: persists Burst state and forwards entity/final-block impacts.
- `ServerScanConfig` and `ScanConfigScreenFactory`: server policy and UI.

No client gameplay state, packet, HUD, cooldown, or new custom entity is required.

## Failure and Performance Behavior

Invalid or unavailable player ownership prevents arming. A missing owner entity at impact does not remove stored owner immunity because the UUID remains on the projectile. A non-positive explosion power still produces no manual entity radius and is sanitized to a safe configurable minimum. Entity queries are bounded by the configured power and use a single pass per impact.

The vanilla explosion behavior prevents double entity effects. Default block destruction and fire are off, protecting existing worlds. Logging occurs only for an illegal dual-enchantment bow when a projectile is spawned.

## Verification

Automated tests cover:

- Burst resource schema, bow-only support, level, source tags, translations, and creative book registration;
- the exclusive-set tag containing exactly Lightning and Burst, with both enchantments referencing it;
- Lightning runtime priority for forced illegal combinations;
- normal/tipped arrow eligibility and permanent spectral-arrow exclusion;
- block/entity/miss and one-shot impact gates plus NBT key presence;
- center, midpoint, edge, outside-radius, owner, player, creative, spectator, damage, and knockback rules;
- defaults, sanitization, version migration, forced spectral exclusion, and all Cloth Config labels;
- regression coverage for Lightning storms, charges/HUD, spectral scanning, trail, bounce, and line of sight.

Fresh verification runs `gradlew.bat test`, `gradlew.bat build`, a client startup/resource-load smoke test, and a dedicated-server startup-to-ready smoke test. Actual enchanting-table rolls, anvil interaction, arrow impacts, block destruction, and entity knockback require an in-game manual pass and will not be claimed from compilation alone.
