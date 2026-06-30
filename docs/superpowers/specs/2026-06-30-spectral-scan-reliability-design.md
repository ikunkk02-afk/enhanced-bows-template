# Spectral Scan Reliability Design

## Goal

Make every player-fired spectral arrow scan reliably at any launch angle, enforce strict block line of sight, preserve scanning through up to three bounces, rate-limit detected-player feedback, and keep all custom configuration-screen text sharp.

## Server-authoritative scan behavior

- A spectral arrow enters scan mode when scanning is enabled and its owner is a server player. Launch velocity is not part of activation.
- The legacy `upwardVelocityThreshold` JSON value remains readable for compatibility but is ignored and no longer appears in the configuration UI.
- The legacy `scanDurationTicks` JSON value also remains readable for compatibility but no longer limits an in-flight scan and no longer appears in the configuration UI.
- A scanning arrow scans from its current position every two ticks while it remains in active flight.
- The default scan radius is 10 blocks.
- Each candidate must pass the existing target policy and spherical distance check.
- With line of sight enabled, the server raycasts from `arrow.getPos().add(0, 0.1, 0)` to both `target.getEyePos()` and `target.getBoundingBox().getCenter()` using `COLLIDER` and `NONE`.
- In strict mode, both rays must miss blocks. In non-strict mode, either clear ray is sufficient.
- Valid targets have Glowing refreshed on every successful scan.

## Detection feedback

- Target UUIDs are not used to suppress future effect refreshes.
- A detected player may receive repeated network notifications, but the client accepts the warning HUD and sound at most once every 40 client ticks.
- Non-player entities only receive refreshed Glowing.

## Bounce behavior

- Every activated scanning arrow is eligible to bounce regardless of launch direction.
- A block collision reflects velocity across the collision face while applying configured damping.
- A bounce only occurs when the reflected result remains above the configured minimum usable speed.
- The first three eligible block collisions bounce. The fourth collision follows vanilla behavior and stops/embeds the arrow.
- Scanning remains active after each successful bounce.

## Configuration and migration

- Defaults are `scanRadius = 10.0`, `scanIntervalTicks = 2`, `scanRequiresLineOfSight = true`, `strictScanLineOfSight = true`, and `scanningArrowMaxBounces = 3`.
- Existing configuration files gain `strictScanLineOfSight` through default merging.
- Existing `upwardVelocityThreshold` values are retained only to avoid destructive config rewrites; they do not affect gameplay.

## Screen rendering

- The custom hub and sound-import screens do not invoke the in-game blur path.
- They draw a stable dark translucent background first, custom text and panels second, and vanilla widgets last.
- Cloth Config remains responsible for its own vanilla-style widgets; no custom Gaussian blur or scaled text matrices are introduced.

## Verification

- Pure tests cover launch-angle independence, two-tick scheduling, strict/tolerant visibility aggregation, defaults/migration, three-bounce limits, low-speed rejection, and 40-tick client notification cooldown.
- `gradlew test` and `gradlew build` must pass.
- `gradlew runServer --args nogui` must reach normal dedicated-server startup without client-class loading failures.
- Client validation covers sharp text plus horizontal, downward, indoor, wall-occluded, unobstructed, and three-bounce behavior.
