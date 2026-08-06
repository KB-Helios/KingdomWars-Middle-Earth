# PR #40 Workers and Recruits Hardening Design

**Date:** 2026-08-01

**Status:** Approved for implementation

**Target:** Minecraft 26.2, Java 25, Architectury with NeoForge and Fabric loaders

**PR:** `KB01111/GalacticWars#40` at initial head
`07870b8ad311a0d494517c957cebc65adecf6e8e`

## Goal

Repair every current, reproducible issue in PR #40, respond to review findings that are not valid
against the current implementation, and continue the authorized Workers/Recruits port without
reintroducing Forge 1.20.1 architecture. The result must strengthen the embodied
recruit-command-build-fight loop: workers and recruits remain physical entities with physical
inventories, server-authoritative assignments, bounded navigation, visible failure states, and
save-compatible recovery.

This design deliberately separates two review boundaries:

1. Stabilize and publish PR #40 with its valid review repairs and current runtime failure fixed.
2. Continue the port in a stacked, focused follow-up branch based on the repaired PR head, rather
   than expanding the already-large 62-file PR with the rest of the acceptance matrix.

The second boundary does not narrow the port objective. It preserves the remaining checklist in
`README.md` and makes the next donor-derived slice independently reviewable.

## Current evidence

The initial PR head and the remote PR head are identical. The project currently targets Minecraft
26.2, NeoForge `26.2.0.25-beta`, and Java 25. The worktree was clean before this design document.

Two fresh full NeoForge GameTest runs completed 73 required tests each. Both passed 72 tests and
failed only `galacticwars:worker_safety_and_upkeep`, where accepted damage was not converted into a
worker retreat before the 80-tick deadline. `specialist_worker_loops`, including the cook loop,
passed both fresh runs and must not receive a speculative correctness patch. A fresh
`runHarnesses` baseline passed all 169 Gradle tasks.

The root cause of the remaining runtime failure is a timing gap: `WorkerSafetyBehaviour` reads
`HURT_BY_ENTITY`, while the damage boundary currently updates morale and reputation but does not
immediately retain the accepted living attacker. `HurtBySensor` can therefore miss the short
damage window before it publishes its memory.

## Authorized donors and compatibility boundary

The project owner has authorized derivative reuse and redistribution from both local donors. New
derivations must be recorded in `docs/authorized-source-intake.md` before merge.

- Recruits donor: `C:\Users\kevin\Desktop\Programmering\Projekt\recruits`, pinned commit
  `cff03e085d65653406a8b6ddcdd0ebff615c3e48`.
- Workers donor: `C:\Users\kevin\Desktop\Programmering\Projekt\workers`, pinned commit
  `c1eb9bdb016af93eb2df2ce8e3b17fc3463d7ee1`.

The donors are Forge 1.20.1 code sources. Their behavioral contracts may be adapted, but their
registries, packets, global managers, entity hierarchy, thread assumptions, and navigation classes
must not be copied into the 26.2 runtime. The relevant donor behaviors are:

- Workers `WorkerFleeGoal`, `CourierWorkGoal`, and `WorkerOpenDoorGoal`: explicit safety state,
  bounded navigation reissue, physical route actions, and open/close traversal semantics.
- Recruits `RecruitEatGoal`, `RecruitPickupWantedItemGoal`, `RecruitHurtByTargetGoal`, and
  `RecruitsOpenDoorGoal`: rate-limited self-care, command-aware pickup, damage reaction, and
  short-lived door interaction.

Galactic Wars keeps SmartBrainLib scheduling, Kingdom `SavedData`, Architectury networking,
revisioned worksite configuration, and failure-atomic physical inventory transactions as the
authoritative 26.2 architecture.

## Review disposition

All 24 inline findings were checked against the exact PR head. Twenty-one comments map to nineteen
distinct code repairs because two late findings duplicate earlier reports; three should receive
evidence-backed replies without speculative production changes.

| Comment | Disposition | Designed response |
| --- | --- | --- |
| `3695614048` | Valid | Recompute ranged army cover/dodge at a bounded eight-tick cadence while retaining the last valid walk target between calculations. |
| `3695614051` | Partly valid | Existing move-to-command and companion predicates already gate hazards. Add the missing hazard arbitration to sit, natural work, civilian shelter, faction reaction, idle wander, and the walk-target consumer's sit guard. |
| `3695614052` | Valid performance issue | Evaluate safety and line-of-sight cover once per unique candidate before sorting. The review's radius-zero duplicate explanation is inaccurate, but comparator-triggered repeated raycasts are real. |
| `3695614054` | Already protected | `nearbyRecruitPickupTarget` and `canCollectRecruitItem` both flow through `canStartRecruitItemPickup`, which rejects active hazard avoidance. Reply with the exact call chain. |
| `3695614056` | Valid | Rate-limit self-care checks to a 20-tick cadence, adapting the donor's bounded hunger check without importing its inventory swapping or Forge goal. |
| `3695614058` | Valid | Treat only lit normal and soul campfires as dangerous; unlit campfires remain traversable. |
| `3695614061` | Not reproducible under the current transaction contract | Delivery and rollback both use `REQUIRE_EXACT`; `PhysicalLogisticsTransaction` either commits the full quantity or changes neither inventory. Add/retain regression proof and reply with this invariant rather than adding a partial-transfer branch. |
| `3695614064` | Valid | Resolve configured item tags through the registry's tag holders instead of scanning every registered item on the worker tick path. Preserve recipe validation and malformed-filter fail-closed behavior. |
| `3695614069` | Valid | Route mutations require both `MANAGE_WORKSITES` and `MANAGE_LOGISTICS`; ordinary worksite settings remain available to worksite managers. |
| `3695614073` | Valid | Make identical courier route/mode updates return the same `WorkAreaConfiguration`, preventing worksite and settlement revision churn. |
| `3695614075` | Valid | Preflight snapshot creation before opening the menu. Missing kingdom, settlement, profession, or durable worksite returns `worksite_missing` and never throws during menu construction. |
| `3695614077` | Valid | Move centered inclusive-bound calculations into `WorkAreaBounds` and use the shared predicate from worker execution and other worksite containment paths. |
| `3695614078` | Valid | Use `Locale.ROOT` for worker item/resource identifiers and the persisted `WorkerRequiredItem` normalization path. Audit adjacent machine-facing worker identifiers in the same pass. |
| `3695614079` | No reachable runtime issue | `WorkerProfessionBehavior.cancel` has no callers and its context has no Kingdom ledger authority. Do not invent a partial reservation cleanup. Reply with the call-site audit; removal may be considered only as a separate dead-API cleanup. |
| `3695614081` | Valid | Select a fixed maximum of the nearest visible worksites per player before emitting the 16 boundary particles for each site. Permission, dimension, visibility, and distance filtering remain server-side. |
| `3695619028` | Valid | A supplied merchant whose physical market is closed is unavailable. Reject preview/purchase and make the open menu invalid instead of falling back to synthetic stock. |
| `3695619032` | Valid | Preserve each `StorageEndpoint` while scanning food and inspect only `min(endpoint.slots, container size)` authorized slots. |
| `3695619034` | Valid | Add a persisted hybrid-dispatch turn so sustained authoritative demand cannot starve a configured manual route. Resume active reservations first; otherwise alternate eligible automatic and route work. |
| `3695619038` | Valid | Evaluate the pure progression decision before reserving physical merchant stock. Keep compensation for credit withdrawal or commit failures after reservation. |
| `3695619040` | Valid | Resolve the assigned durable worksite and call revisioned `configureWorksiteStorage` before updating the loaded entity. Reject missing/stale authority and let reconciliation retain the persisted selection. |
| `3695642790` | Valid | Enable the Command Center's Configure Worksite action only when the selected `WorkerSummary` carries an assigned worksite; retain the server preflight as authoritative. |
| `3695642795` | Duplicate of `3695614056` | The designed 20-tick self-care cadence resolves both reports; reply to both with the same regression evidence. |
| `3695642796` | Duplicate of `3695614073` | The designed equality short-circuit resolves both reports; reply to both with the same revision-stability evidence. |
| `3695642797` | Valid | Reject a one-waypoint worksite route as `invalid_route`, matching the assigned-courier route validator and the two-waypoint `CourierRoutePlan` contract. |

## Runtime design

### Safety and movement arbitration

The accepted living attacker will be written to the recruit brain at the server damage boundary.
This closes the sensor latency gap without creating a second persistent threat model. The existing
worker phase and retreat target remain the save-compatible source of recovery state across reload.
The safety behavior continues to require a tame, assigned, enabled worker in `WORK_AT_SITE`, keeps
physical cargo unchanged, and resumes only after its safe-tick window.

Hazard escape has priority over all non-combat idle movement producers. Predicates that can publish
or clear `WALK_TARGET` must decline while hazard avoidance is active. The walk-target consumer must
not apply the ordinary ordered-sit clearing rule to a hazard escape target. Existing attack and army
authority remain unchanged; this slice prevents idle scheduling from erasing an already-authorized
escape.

Ranged movement retains deterministic candidate ordering. Candidate generation is deduplicated,
stand safety and cover are evaluated once, and the cached facts are sorted without world raycasts in
the comparator. Army ranged behavior asks for a new cover position every eight ticks, matching the
existing recruit ranged cadence.

### Bounded hot paths

Self-care uses a 20-tick behavior cooldown. Food consumption still mutates exactly one physical
cargo stack, and missing food still publishes the existing epoch-deduplicated settlement demand.
The food endpoint scan retains the endpoint's slot authority throughout the pipeline.

Cooking tag filters resolve only the members of the requested item tag and then perform recipe
checks on those members. Direct item filters and the unfiltered beef fallback retain their current
semantics. No global item-registry scan remains on the cook acquisition path.

Worksite overlay rendering first builds the authorized, same-dimension, visible candidates, orders
them by squared player distance with a stable worksite-ID tie break, and limits the list to a fixed
per-player budget. This bounds packets independently of settlement size.

### Durable authority and configuration

Courier routes are both worksite configuration and logistics policy, so route mutation requires
both permissions. Equality checks occur before any route or settlement revision increment.
Worksite menu providers carry a successfully captured immutable snapshot; opening paths report a
domain reason and do not construct a provider when no durable worksite exists.

Recruit-screen storage selection resolves the registered endpoint and assigned worksite, submits
the current configuration revision to `KingdomSavedData.configureWorksiteStorage`, and only mirrors
the accepted result to the loaded recruit. Periodic reconciliation therefore observes the same
durable endpoint instead of undoing the player's choice.

Hybrid courier arbitration adds one optional entity save key with a backward-compatible default.
An active supply reservation always resumes before selecting new work. Otherwise a hybrid courier
alternates between automatic demand and a configured route when both are eligible, falls back to
the available source when only one exists, and advances its persisted turn only after it selects a
source. Kingdom schema 11 does not need a bump because the new state is optional entity data rather
than a changed Kingdom codec.

### Trade integrity

Physical merchant presence is never allowed to degrade into synthetic stock. A supplied merchant
must be alive, near, a merchant, and have an open physical market. Closed-market previews and open
menus fail with `merchant_unavailable`.

The progression event is evaluated from the captured pre-trade state before stock reservation. If
it is rejected or unchanged, no inventory has been touched. After stock reservation, the existing
credit-withdraw and progression-commit compensation paths restore both credits and stock on
failure. Successful delivery remains physical and component-preserving.

Automatic supply delivery retains its exact transaction contract. A regression test will exercise
insufficient destination space and verify that neither inventory changes; no partial rollback API
will be introduced.

## TDD and verification strategy

Every valid production change starts with a focused failing test. Prefer dependency-light domain
harnesses for normalization, bounds, route equality, dispatch alternation, permissions, overlay
selection, and transaction invariants. Use GameTests when proof requires Minecraft brain memory,
block state, containers, menus, merchant lifecycle, entity save/reload, or ordinary player actions.

The current red `worker_safety_and_upkeep` GameTest is the first failing test for the safety repair.
New coverage must include:

- immediate damage-to-retreat and save/reload recovery;
- lit versus unlit campfire hazard classification and idle movement non-interference;
- bounded self-care and configured cooking-tag resolution;
- route permission denial, no-op revision stability, single-waypoint rejection, menu preflight,
  durable storage selection, and disabled configuration UI for unassigned workers;
- endpoint slot-limited food sourcing;
- hybrid automatic/route alternation across entity save/reload;
- closed physical markets and progression-rejected trades with unchanged stock;
- bounded nearest-worksite overlay selection;
- Turkish-default-locale worker identifier normalization;
- exact courier transfer failure with unchanged source and destination inventories.

After focused red/green cycles, run these gates from the repository root:

1. `rtk .\gradlew.bat runHarnesses --no-daemon --console=plain`
2. `rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain` twice
3. `rtk .\gradlew.bat buildAll --no-daemon --console=plain`
4. `git diff --check`

Two full GameTest passes are required because the PR began with timing-sensitive reports. A green
headless suite is bounded evidence; fresh Survival and two-client dedicated-server acceptance stay
explicitly unverified until actually executed.

## Publication sequence

1. Commit this approved design document by itself.
2. Write a task-by-task implementation plan with exact files, failing tests, commands, and review
   comment IDs.
3. Implement PR #40 repairs in focused TDD groups and update donor intake entries for substantive
   derivations.
4. Refresh the README port status from fresh evidence; do not preserve the stale 71/73 statement.
5. Run all verification gates, review the diff, and commit the repairs.
6. Push the verified repair commit to `origin/codex/workers-recruits-runtime-port`, reply to all 24
   inline findings, and compare local, remote, and PR-head SHAs.
7. Reconcile PR metadata/body with the verified state. Keep it ready for review only if all automated
   gates are green; otherwise return it to draft with the exact blocker.
8. Branch the next port slice from the repaired head. If PR #40 is not yet merged, open it as a
   stacked draft against `codex/workers-recruits-runtime-port`; retarget to `main` after #40 lands.

## Next port slice

The first follow-up focuses on courier integrity and embodied recruit resilience because those areas
join both donor codebases and the highest-risk remaining README requirements:

- competing couriers, partial stock, expired leases, exact rollback, and manual/automatic/hybrid
  route behavior;
- owner/officer/quartermaster/member authority, concurrent edits, stale revisions, and replayed
  payload rejection;
- follow, hold, patrol, and worker resumption through doors;
- self-care during ordinary commands without overriding hazard or combat authority;
- friendly-fire policy and zero-ammunition blaster behavior without silent melee fallback.

Fixtures may place the initial recruit and world objects. Acceptance tests may not mutate private
worker phases, invoke the worker controller directly, or teleport a recruit after assignment. This
keeps the follow-up evidence at the player-visible runtime boundary.

## Completion criteria for this design

PR #40 stabilization is complete only when all valid findings have regression coverage, the three
evidence-only findings have precise replies, both repeated GameTest runs pass all required tests,
`runHarnesses` and `buildAll` pass, provenance is current, the repair commit is pushed, and the three
SHAs agree.

The continuation slice is complete only when its ordinary-player GameTests pass without private
phase mutation or post-assignment teleportation, its authority and conservation harnesses pass, its
provenance entries are recorded, and it is published as the focused follow-up boundary described
above.
