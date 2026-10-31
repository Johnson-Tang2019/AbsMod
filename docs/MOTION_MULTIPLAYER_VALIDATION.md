# MOTION-03: transition and real peer test

Date: 2026-10-31. Base: `fb84ffc`. Existing local reference packs were retained.

## Change

`BladeMotionPlayer` snapshots the preceding per-player pose when a new authorized strike starts. The first 0.75 tick blends from that snapshot to the current source sample. The source timeline continues advancing during the blend; no attack delay, damage callback, queued recovery or server-stat change is added. This is an initial continuity improvement, not a replacement animation set.

The opt-in `MotionPeerTest` uses two independent Minecraft Java processes and separate `run-host`/`run-observer` directories. The listener binds **127.0.0.1:25583 only** and accepts development identities; this setting is confined to the test source set and never changes a user's existing server. The observer connects over TCP. Both clients send `AttackIntentPayload` through the production server scheduler and receive normal tracking confirmations. No local RemotePlayer substitute is used in this test.

## Executed evidence

- Final dual-loader build and shared verification passed; all 12 Fabric GameTests passed (`build/motion-peers-validation.log`). Both normal JARs were inspected and exclude MotionPeerTest/MotionVisualTest. The host and observer exited successfully. These checks do not replace the visual evidence below.

- NeoForge single-client visual fixture: `build/motion-october-client.log`, successful world startup and exit. Reviewed first/third-person screenshots with effects disabled. This run preceded the transition change.
- Two-client final run: `build/motion-peer-host.log`, `build/motion-peer-observer.log`. Host records `MotionGuest[/127.0.0.1:50704] logged in`, followed by `MOTION_PEERS connected=2`; observer exited successfully after its capture window.
- [54 unmodified observer screenshots](../art/animation/slashblade_baseline/multiplayer/preview.html). Images `peer-44.png` and `peer-52.png` show different remote arm/weapon poses while the local weapon also moves. Effects are disabled. The transition change is present in this run.
- The fixture alternates 100 ticks of held input with 60 ticks of released input. It exercises server-authorized empty/immune-target swings and remote animation transport; it does **not** establish PvP damage correctness or simulate physical mouse events.
- Initial attempts exposed missing fresh-directory setup and IPv6/IPv4 loopback mismatch. Final run explicitly uses IPv4. Failed connection attempts are not counted as successful multiplayer checks.

## Reproduction

Use JDK 25. Create `26.2NeoForge/run-host` and `run-observer` first. Complete first-run accessibility onboarding in each, or copy the project's development `run/options.txt` into these test-only directories before launching. Never overwrite a normal player's options.

Start the host with `./gradlew.bat -PmotionVisualTest -PmotionPeer=host :neoforge:runClient`; wait until its test world is ready, then run the same task with `-PmotionPeer=observer` in another terminal. The observer captures about 11 seconds; the host stops after its test timeout. Keep port 25583 free. These test classes are excluded from a normal build.

## Remaining limits

This is actual two-client local networking, **not public-network latency/loss testing**. It does not complete Fabric client visuals, every movement pose, stage-specific grip polishing, hand-driven GUI/mining tests, or the full damage matrix. Generated item edges still look ribbed in some third-person angles, and the sprite can be nearly edge-on. No new nine-stage texture was drawn. Do not mark the full visual task DONE solely from this test.

Growth/recipe design is delivered separately as [PROPOSED v2](MEOW_BLADE_PROGRESSION_PROPOSAL.md). It does not claim recipes, affection income, dark heavy attacks or the boss are implemented.
