# Lightning Enchantment and Scanning Arrow Trail Design

## Scope

Add a client-only red laser-like trail to activated scanning spectral arrows, and add a bow-only data-driven `enhanced-bows:lightning` enchantment with two server-owned rechargeable charges and a synchronized client HUD.

## Architecture

- `SpectralArrowEntityMixin` publishes the existing scanning-arrow flag through tracked entity data. A client-only spectral-arrow mixin reads that flag and creates red dust particles locally, so normal spectral arrows have no trail and the server sends no trail particles.
- `RangedWeaponItemMixin` intercepts the existing per-projectile spawn boundary. It arms only persistent projectiles fired by a `BowItem` whose stack has the Lightning enchantment.
- `PersistentProjectileEntityMixin` persists the armed/consumed flags and triggers at most once after a living target survives the arrow hit.
- `LightningChargeComponent` is a CCA player component. The logical server owns charge consumption, recharge ticks, persistence, respawn policy, and sync cadence. The client reads only the synchronized mirror.
- `LightningHudRenderer` is client-only and renders at configurable top-left coordinates only while the configured holding rule passes.

## Lightning Safety

Three strike approaches were considered: a normal lightning entity with a shooter-immunity mixin, direct damage with no lightning entity, and a cosmetic lightning entity plus targeted vanilla lightning damage. The third approach is selected because it preserves the visible bolt and vanilla thunder while guaranteeing that the shooter and nearby bystanders are not damaged by the bolt entity.

## Data and Configuration

- The enchantment is defined under `data/enhanced-bows/enchantment/lightning.json`, supports only `#minecraft:enchantable/bow`, and is added to enchanting-table, non-treasure, tradeable, and random-loot tags.
- Server config owns Lightning enablement, maximum charges, recharge duration, damage mode, death retention, and creative infinite behavior.
- Client config owns trail enablement/shape and Lightning HUD visibility/position/scale.

## Verification

Pure tests cover charge consumption, two-stage recharge, configuration bounds, arrow one-shot policy, trail sampling, HUD text state, and resource JSON. Runtime verification includes `test`, `clean build`, client startup, and dedicated-server startup before commit and push.
