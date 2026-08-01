# PR #40 Courier and Recruit Continuation Plan

**Goal:** Complete the approved post-PR #40 slice for courier contention, multiplayer worksite authority, command-preserving door traversal, ordinary-command self-care, friendly fire, and physical recruit ammunition on Minecraft 26.2.

**Architecture:** Keep SmartBrainLib as the only recruit scheduler, Kingdom `SavedData` as durable settlement and army authority, revisioned Architectury payloads as the client intent boundary, and component-preserving physical inventories as logistics truth. The authorized Forge 1.20.1 donors provide behavior references only; their goals, mutable route state, networking, and inventory implementations are not ported.

**Pinned donors:** `recruits` at `cff03e085d65653406a8b6ddcdd0ebff615c3e48` and `workers` at `c1eb9bdb016af93eb2df2ce8e3b17fc3463d7ee1`.

## Acceptance constraints

- Target Java 25 and Minecraft 26.2 on the existing Fabric/NeoForge Architectury layout.
- Fixtures may place initial players, recruits, doors, claimed storage, stock, and demands.
- Black-box worker acceptance may not mutate a private worker phase, invoke `tickWorkerController` or another private controller method, or teleport a recruit after assignment.
- Every inventory assertion compares physical stock, courier cargo, requester cargo, and delivered demand so duplication and loss cannot hide behind status changes.
- A ranged recruit with no physical ammunition may hold or reposition, but must not fire and must not silently enter melee behavior while still holding a ranged weapon.
- Self-care may run during non-combat commands only when no attack memory, live target, recent hurt, hazard escape, or worker-safety retreat owns the tick. The durable command/order must remain unchanged.

## Task 1: Competing courier leases and conservation

**Files:**

- Modify: `src/test/java/galacticwars/clonewars/workforce/SettlementSupplyLedgerTest.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the tests fail: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify only if the tests fail: `src/main/java/galacticwars/clonewars/workforce/SettlementSupplyLedger.java`

- [x] Extend the dependency-light ledger harness with two workers competing for partial stock, lease expiry and reacquisition, stale-worker completion rejection, idempotent retries, and revision monotonicity.
- [x] Add an ordinary-path courier GameTest with two assigned couriers, one registered source, one requester, and less stock than aggregate demand. Assert bounded reservations and exact physical conservation without private phase mutation.
- [x] If the runtime test exposes a gap, make the smallest lease/cleanup repair and first preserve the failing test as RED evidence. The new evidence passed without a production repair.
- [x] Run the focused harness and the GameTest twice, then commit `Prove competing courier lease integrity`. The focused harness passed and both NeoForge runs passed all 77 required tests.

## Task 2: Concurrent worksite authority and replay rejection

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if required: `src/main/java/galacticwars/clonewars/menu/WorksiteConfigurationMenu.java`
- Modify only if required: `src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java`

- [ ] Extend the authority GameTest with owner, officer, quartermaster, and member actors.
- [ ] Open two immutable snapshots at the same configuration revision. Accept the first authorized edit, reject the second as stale, and leave its requested state unapplied.
- [ ] Reject the same replay ID twice without advancing configuration or settlement revisions.
- [ ] Prove officer and quartermaster logistics authority and member denial through the server menu/data boundary.
- [ ] Run the GameTest twice, then commit `Prove concurrent worksite authority`.

## Task 3: Follow, hold, patrol, and work resumption through doors

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if required: `src/main/java/galacticwars/clonewars/entity/ai/RecruitDoorInteractionBehaviour.java`
- Modify only if required: command/order SmartBrain behaviors under `src/main/java/galacticwars/clonewars/entity/ai`

- [ ] Add a claimed wooden-door corridor fixture that records natural open and close transitions.
- [ ] Issue follow through the recruit menu, observe traversal, then issue hold and prove the recruit remains held with the command intact.
- [ ] Issue an authoritative two-waypoint patrol across the same boundary and prove the loaded commander advances/resumes the persisted patrol without teleportation.
- [ ] Retain the existing black-box farmer lifecycle as the worker resumption proof and add explicit final command/status assertions if missing.
- [ ] Run the door tests twice, then commit `Prove recruit door command resumption`.

## Task 4: Ordinary-command self-care arbitration

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify only if useful for a pure seam: `src/main/java/galacticwars/clonewars/entity/ai/RecruitSelfCarePolicy.java`

- [ ] Add a failing GameTest for an exhausted recruit with physical food under follow, hold, and persisted patrol orders. Each case consumes exactly one item at the bounded cadence and retains its command/order.
- [ ] Add combat and hazard controls proving food is unchanged while attack memory or escape authority is active.
- [ ] Replace blanket command/group exclusions with explicit higher-priority-state arbitration, including SmartBrain attack memory.
- [ ] Run the focused test twice, then commit `Allow command-preserving recruit self care`.

## Task 5: Physical recruit ammunition and collision-level friendly fire

**Files:**

- Add: `src/main/java/galacticwars/clonewars/combat/RecruitAmmunitionService.java`
- Add: `src/test/java/galacticwars/clonewars/combat/RecruitAmmunitionServiceTest.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitRangedCombatBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/ArmyCombatBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify as needed: existing blaster integration harnesses

- [ ] Add RED coverage showing a tamed recruit cannot create a bolt with empty cargo, does not close into melee, consumes exactly one `energy_cell` for one shot, and resumes ranged fire when one cell is physically supplied.
- [ ] Keep natural faction NPC ammunition outside player-managed cargo logistics; only tamed/player-managed recruits consume shared cargo cells.
- [ ] Consume a cell immediately before `BlasterItem.fireAt`; overheated, blocked, or out-of-range attempts consume nothing. Preserve the ranged weapon classification when empty so melee never becomes an implicit fallback.
- [ ] Extend friendly-fire GameTest coverage from direct policy invocation to a real recruit-owned projectile crossing an owned/allied recruit before an enemy. Assert the protected entity is unharmed and the projectile is consumed.
- [ ] Seed existing tamed-recruit blaster fixtures with explicit Energy Cells where they intend to prove firing. Preserve the separate zero-army-supply test.
- [ ] Run harnesses and GameTests twice, then commit `Require physical recruit blaster ammunition`.

## Task 6: Documentation, full verification, and publication

**Files:**

- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`
- Modify: this plan

- [ ] Record the donor behavior mapping for door traversal, command-aware self-care, courier lease behavior, and ranged ammunition without claiming copied Forge code.
- [ ] Update the README port checklist only for acceptance rows actually proven by the new tests; keep fresh Survival and real two-client gates open.
- [ ] Run `rtk .\gradlew.bat runHarnesses --no-daemon --console=plain`.
- [ ] Run `rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain` twice on fresh generated worlds.
- [ ] Run `rtk .\gradlew.bat buildAll --no-daemon --console=plain` and `git diff --check`.
- [ ] Commit the verified documentation and evidence.
- [ ] When GitHub authentication is restored, first push repaired PR #40 and synchronize all three SHAs/comments/body. Then publish this branch as a stacked draft against `codex/workers-recruits-runtime-port` while PR #40 remains open.

## Completion gate

This slice is complete when courier contention and expiry are conservation-proven, concurrent role/revision/replay cases are rejected server-side, follow/hold/patrol/work commands survive real door traversal, self-care retains ordinary commands while yielding to danger and combat, tamed recruit bolts consume physical Energy Cells with no empty-gun melee fallback, collision-level friendly-fire proof passes, provenance is current, and all four verification gates are green.
