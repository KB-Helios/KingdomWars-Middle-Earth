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

- [x] Extend the authority GameTest with owner, officer, quartermaster, and member actors.
- [x] Open two immutable snapshots at the same configuration revision. Accept the first authorized edit, reject the second as stale, and leave its requested state unapplied.
- [x] Reject the same replay ID twice without advancing configuration or settlement revisions.
- [x] Prove officer and quartermaster logistics authority and member denial through the server menu/data boundary.
- [x] Run the GameTest twice, then commit `Prove concurrent worksite authority`. Both corrected NeoForge runs passed all 77 required tests; the first two RED runs identified fixture proximity and a maximum-dimension no-op before the guaranteed overlay mutation exercised concurrency.

## Task 3: Follow, hold, patrol, and work resumption through doors

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if required: `src/main/java/galacticwars/clonewars/entity/ai/RecruitDoorInteractionBehaviour.java`
- Modify only if required: command/order SmartBrain behaviors under `src/main/java/galacticwars/clonewars/entity/ai`

- [x] Add a claimed wooden-door corridor fixture that records natural open and close transitions.
- [x] Issue follow through the recruit menu, observe traversal, then issue hold and prove the recruit remains held with the command intact.
- [x] Issue an authoritative two-waypoint patrol across the same boundary and prove the loaded commander advances/resumes the persisted patrol without teleportation.
- [x] Retain the existing black-box farmer lifecycle as the worker resumption proof and add explicit final command/status assertions if missing.
- [x] Run the door tests twice, then commit `Prove recruit door command resumption`. Both corrected fresh-world NeoForge runs passed all 78 required tests; RED fixture calibration exposed that the first rally point sat inside patrol arrival tolerance but outside the door-close radius, so the proof was corrected without a production AI change.

## Task 4: Ordinary-command self-care arbitration

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify only if useful for a pure seam: `src/main/java/galacticwars/clonewars/entity/ai/RecruitSelfCarePolicy.java`

- [x] Add a failing GameTest for an exhausted recruit with physical food under follow, hold, and persisted patrol orders. Each case consumes exactly one item at the bounded cadence and retains its command/order.
- [x] Add combat and hazard controls proving food is unchanged while attack memory or escape authority is active.
- [x] Replace blanket command/group exclusions with explicit higher-priority-state arbitration, including SmartBrain attack memory.
- [x] Run the focused test twice, then commit `Allow command-preserving recruit self care`. Both corrected fresh-world NeoForge runs passed all 78 required tests; RED proved that attack memory and a save/reloaded worker safety retreat were both ignored by the previous command/group blacklist.

## Task 5: Physical recruit ammunition and collision-level friendly fire

**Files:**

- Add: `src/main/java/galacticwars/clonewars/combat/RecruitAmmunitionService.java`
- Add: `src/test/java/galacticwars/clonewars/combat/RecruitAmmunitionServiceTest.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitRangedCombatBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/ArmyCombatBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify as needed: existing blaster integration harnesses

- [x] Add RED coverage showing a tamed recruit cannot create a bolt with empty cargo, does not close into melee, consumes exactly one `energy_cell` for one shot, and resumes ranged fire when one cell is physically supplied.
- [x] Keep natural faction NPC ammunition outside player-managed cargo logistics; only tamed/player-managed recruits consume shared cargo cells.
- [x] Consume a cell immediately before `BlasterItem.fireAt`; overheated, blocked, or out-of-range attempts consume nothing. Preserve the ranged weapon classification when empty so melee never becomes an implicit fallback.
- [x] Extend friendly-fire GameTest coverage from direct policy invocation to a real recruit-owned projectile crossing an owned/allied recruit before an enemy. Assert the protected entity is unharmed and the projectile is consumed.
- [x] Seed existing tamed-recruit blaster fixtures with explicit Energy Cells where they intend to prove firing. Preserve the separate zero-army-supply test.
- [x] Run harnesses and GameTests twice, then commit `Require physical recruit blaster ammunition`. The focused ammunition harness and all 163 full harness tasks passed; both fresh NeoForge runs passed all 79 required tests. RED first proved that tamed blasters produced bolts without consuming seeded cells, while natural faction recruits correctly retained cargo-free firing.

## Task 6: Documentation, full verification, and publication

**Files:**

- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`
- Modify: this plan

- [x] Record the donor behavior mapping for door traversal, command-aware self-care, courier lease behavior, and ranged ammunition without claiming copied Forge code.
- [x] Update the README port checklist only for acceptance rows actually proven by the new tests; keep fresh Survival and real two-client gates open.
- [x] Run `rtk .\gradlew.bat runHarnesses --no-daemon --console=plain`. All 176 actionable tasks passed.
- [x] Run `rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain` twice on fresh generated worlds. Both runs passed all 79 required tests.
- [x] Run `rtk .\gradlew.bat buildAll --no-daemon --console=plain` and `git diff --check`. Both loader builds passed with 186 actionable tasks, and the diff check was clean.
- [x] Commit the verified documentation and evidence in the final local documentation commit.
- [ ] When GitHub authentication is restored, first push repaired PR #40 and synchronize all three SHAs/comments/body. Then publish this branch as a stacked draft against `codex/workers-recruits-runtime-port` while PR #40 remains open. Blocked on 2026-08-01 because the active `KB01111` GitHub CLI token is invalid.

## Task 7: Exact courier retry across live-lease reload

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: automatic courier runtime and persistence code in `GalacticRecruitEntity`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Add an ordinary-path automatic courier GameTest with a full requester cargo and exact physical demand; do not set a private phase, invoke the worker controller, or move the courier after assignment.
- [x] Observe the courier withdraw the complete reservation, then prove a full recipient causes an atomic retry: source stock is empty, courier cargo retains the complete batch, requester receives none, demand remains unchanged, and the lease remains active.
- [x] Serialize and remove the courier with the chunk-unload removal reason, load the same entity back into the server, and prove its cargo and the single original reservation survive without duplication or release.
- [x] Free exactly one requester slot and prove the reloaded courier completes the same lease with exact physical conservation and no stale cargo. No production repair was required; the existing Minecraft 26.2 runtime already preserved the cargo and lease invariants.
- [x] Run the focused GameTest twice, then run the full harness, two full GameTest passes, `buildAll`, and `git diff --check` before committing. Both focused runs passed 1/1, `runHarnesses` passed 176 actionable tasks, both fresh full runs passed all 80 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge.

## Task 8: Command Center removal during active courier work

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/workforce/SettlementSupplyLedger.java`
- Modify: `src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java`
- Modify: `src/test/java/galacticwars/clonewars/kingdom/SettlementSupplyPersistenceTest.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Add a RED persistence harness proving authoritative Command Center deactivation releases all active settlement leases and rejects new reservations while inactive without completing or deleting their demands. RED failed because the original reservation remained `ACTIVE`; the repaired harness passes with an explicit `settlement_inactive` retry rejection.
- [x] Add an ordinary owner-removal GameTest with two HYBRID couriers sharing a worksite: one holds an exact automatic batch while the other executes the configured-route fallback. Do not mutate private worker phases, invoke the controller, or move either courier after assignment.
- [x] Prove removal through the owner's real `destroyBlock` path pauses both couriers, releases the live lease, preserves the full carried batch and outstanding demand, performs no ghost delivery, and freezes the configured route until authority returns.
- [x] Re-place and reactivate the same Command Center, then prove the configured route resumes from durable state while the released automatic lease remains terminal and its cargo remains conserved for player recovery.
- [x] Run focused harnesses and the focused GameTest twice, then run the full harness, two full GameTest passes, `buildAll`, and `git diff --check` before committing. `SettlementSupplyLedgerTest` and `SettlementSupplyPersistenceTest` passed, both focused `courier_hall_removal` runs passed 1/1, the final `runHarnesses` gate passed 176 actionable tasks, two fresh aggregate NeoForge runs passed all 81 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. Aggregate verification also exposed and fixed the cook's false second-fuel request for an already-lit furnace and replaced premature distant-fixture clocks with entity-index and entity-ticking readiness gates.

## Task 9: Ordinary-player cook lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Add a dedicated ordinary-player-path cook GameTest using the real hire, profession assignment, worksite, registered storage, navigation, furnace, collection, and deposit paths. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Seed exactly one raw beef and one coal, then prove exact input/output conservation, physical approach, furnace loading and ignition, work-order completion, and storage deposit.
- [x] Observe the consumed fuel slot while the furnace remains lit and fail immediately if the cook requests redundant coal before the input finishes. The corrected in-bounds fixture passed twice; the initial RED timeout proved the furnace was correctly rejected when it sat one block outside the claimed worksite.
- [x] Run the focused GameTest twice, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing. Both corrected cook runs passed 1/1; the initial RED fixture placed the furnace one block beyond the claimed worksite and correctly timed out during station discovery. Aggregate verification exposed that `worker_safety_and_upkeep` started its deadline before its distant fixture became entity-ticking, so the proof now waits for chunk/entity readiness and measures recruit ticks. Its focused control passed 1/1, both final aggregate runs passed 82/82, `runHarnesses` passed 176 actionable tasks, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge.

## Task 10: Ordinary-player lumberjack lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Add a dedicated ordinary-player-path lumberjack GameTest using real hire, profession assignment, configured worksite, registered storage, navigation, connected-log harvesting, replanting, and deposit paths. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Seed exactly one matching sapling in storage and a three-log oak tree in-bounds. Prove the worker physically withdraws the sapling, approaches the tree, consumes the sapling only by replanting it, deposits exactly three logs, completes its persisted work order, and applies exact axe wear.
- [x] Assert log and sapling conservation throughout the lifecycle so no direct inventory/world mutation can duplicate or lose either resource. Both focused runs passed 1/1 without a production repair.
- [x] Record the pinned Workers lumberjack behavioral mapping and Minecraft 26.2 transformation in the authorized-source ledger.
- [x] Run the focused GameTest twice, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing. Both focused runs passed 1/1, `runHarnesses` passed 176 actionable tasks, both aggregate NeoForge runs passed all 83 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. No production repair was required; the existing Minecraft 26.2 runtime already satisfied the ordinary lifecycle and exact conservation proof.

## Task 11: Ordinary-player miner lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Add a dedicated ordinary-player-path miner GameTest using real hire, profession assignment, configured worksite, registered storage, navigation, correct-tool mining, and deposit paths. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Seed exactly one in-bounds iron ore and prove physical approach, ore removal, exactly one raw-iron output, one point of pickaxe wear, persisted work-order completion, and registered-storage deposit. A controlled RED fixture placed the ore one block outside the worksite and correctly remained in `FIND_TARGET/scan_worksite` until its bounded timeout; the restored in-bounds fixture passed twice.
- [x] Assert ore/raw-iron conservation throughout the lifecycle so no direct inventory/world mutation can duplicate or lose the mined resource.
- [x] Record the pinned Workers miner behavioral mapping and Minecraft 26.2 transformation in the authorized-source ledger.
- [x] Run the focused GameTest twice, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing. The controlled out-of-bounds RED failed in `FIND_TARGET/scan_worksite` at recruit tick 503, both restored focused runs passed 1/1, `runHarnesses` passed 176 actionable tasks, both aggregate NeoForge runs passed all 84 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. No production repair was required; the existing Minecraft 26.2 runtime already satisfied the ordinary lifecycle and exact conservation proof.

## Task 12: Ordinary-player animal-farmer lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Register `black_box_animal_farmer_lifecycle` as an isolated, staggered GameTest with a 900-tick harness timeout and a 500-recruit-tick lifecycle deadline.
- [x] Use the real hire, animal-farmer assignment, configured `minecraft:cow` worksite, registered Command Center storage, navigation, feed withdrawal, feeding, and persisted-order paths. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Seed exactly two wheat and two adult cows. Observe the recruit withdraw both wheat and physically approach the pair, then require both cows to enter love state, both wheat to leave physical storage/cargo, the `ANIMAL_FARM` order to complete, and `WORK_AT_SITE` to remain active.
- [x] Assert `stored wheat + carried wheat + fed animals == 2` throughout the lifecycle. The controlled out-of-bounds pair correctly remained `BLOCKED/breeding_pair_required` through recruit tick 505 with both wheat untouched; both restored in-bounds runs passed 1/1.
- [x] Record the pinned Workers `AnimalFarmerWorkGoal.java` mapping and Minecraft 26.2 transformation, update the remaining ordinary-path list, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing `Prove ordinary animal farmer lifecycle`. The focused in-bounds proof passed twice, `runHarnesses` passed 176 actionable tasks, both aggregate NeoForge runs passed all 85 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. No production repair was required.

## Task 13: Ordinary-player fisher lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Register `black_box_fisher_lifecycle` as an isolated, staggered GameTest with a 1,000-tick harness timeout and a 650-recruit-tick lifecycle deadline.
- [x] Use the real hire, fisherman assignment, configured worksite, registered Command Center storage, navigation, source-water selection, timed fishing interaction, loot-table catch, collection, deposit, and persisted-order paths. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Require physical approach, an observed `INTERACT/fishing_wait` cast, exactly one point of fishing-rod wear, a non-empty component-bearing catch, empty final cargo, completed `FISH` order, registered-storage deposit, and preserved `WORK_AT_SITE` command.
- [x] Snapshot the storage baseline and the actual caught cargo stack multiset, then prove exact count and component conservation across baseline, live cargo, and final storage. The controlled x=9 source remained `FIND_TARGET/scan_worksite` through recruit tick 664 with no cast, catch, deposit, or rod wear; both restored x=7 runs passed 1/1 across their actual loot rolls.
- [x] Record the pinned Workers `FishermanWorkGoal.java` mapping and Minecraft 26.2 transformation, update the remaining ordinary-path list, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing `Prove ordinary fisher lifecycle`. The focused fisher proof passed twice across its actual loot rolls and required no production repair. Aggregate diagnosis also hardened the existing grouped-command, physical-ammunition, and cook fixtures with condition-based entity-ticking gates, while the shared Minecraft 26.2 test-area poll now refreshes embedded-player chunk tickets. `runHarnesses` passed 176 actionable tasks, two consecutive aggregate NeoForge runs passed all 86 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge.

## Task 14: Ordinary-player merchant lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Register `black_box_merchant_lifecycle` as an isolated, staggered GameTest with a 900-tick harness timeout and a 500-recruit-tick lifecycle deadline.
- [x] Use a Survival player's real hire and merchant-assignment actions, configured worksite, registered Command Center storage, recruit navigation, live distance/availability gates, server-authored merchant menu, and replay-addressed purchase. Do not mutate private phases, invoke the worker controller, or move the recruit after assignment. The headless GameTest constructs the provider-owned server menu directly because its embedded client cannot accept NeoForge's extended-screen packet.
- [x] Seed one exact eight-cell `republic_quartermaster` batch, observe `NAVIGATE_SOURCE/open_market`, require physical arrival and an active persisted `MERCHANT` order, then prove one successful menu purchase and one rejected replay.
- [x] Assert exact Energy Cell and Credit Chip conservation across registered storage and player inventory, plus one and only one `TRADE_COMPLETED` progression event. The controlled seven-cell RED failed with `merchant_out_of_stock` while stock, credits, and progression remained unchanged; the restored exact batch passed twice.
- [x] Record the pinned Workers `MerchantWorkGoal.java` mapping and Minecraft 26.2 transformation, update the remaining ordinary-path list, then run `runHarnesses`, two complete GameTest passes, `buildAll`, and `git diff --check` before committing `Prove ordinary merchant lifecycle`. No production repair was required. Both restored focused merchant runs passed 1/1, `runHarnesses` passed 176 actionable tasks, two consecutive aggregate NeoForge runs passed all 87 required GameTests, `buildAll` passed 186 actionable tasks for Fabric and NeoForge, and `git diff --check` was clean. Aggregate diagnosis also gave the existing worker-safety test its full bounded outer deadline, made the farmer-door proof wait for entity-ticking readiness and measure recruit ticks, and made follow/sit prove area readiness before its entity-index deadline; no gameplay success criterion was weakened.

## Task 15: Ordinary-player builder lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify only if the black-box test exposes an authority/persistence gap: `src/main/java/galacticwars/clonewars/settlement/ConstructionProjectService.java` and Kingdom project/worksite/order records
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Register `black_box_builder_lifecycle` as an isolated, staggered GameTest with a 1,800-tick harness timeout and a 1,300-recruit-tick lifecycle deadline.
- [x] Use a Survival player's real `BUTTON_HIRE`, `BUTTON_NEXT_BLUEPRINT`, and `BUTTON_BUILD_STARTER_KEEP` actions to prepare the shipped `galacticwars:mine` plan, then invoke the physical projector item's `useOn` path. Require the resulting project-specific `BUILDER` worksite/order and never mutate a private phase, invoke the worker controller, or move the recruit after project assignment.
- [x] Establish RED with only eight Duracrete for the nine-placement blueprint. The ordinary loop placed and persisted exactly eight blocks, preserved `stored + carried + placed == 8`, then failed at tick 674 with `BLOCKED/build_material_missing` and a stable work-order ID before the exact nine-item fixture was restored.
- [x] On GREEN, observe `NAVIGATE_SOURCE/withdraw_build_material` and `NAVIGATE_SOURCE/build_place`, physical movement from the distant spawn, nine persisted placements, exact `stored + carried + placed == 9` conservation throughout, a terminal `BUILD` work order, a completed build project, one `BUILDING_COMPLETED` progression event, and return to `FOLLOW_OWNER` with no active base target. Both restored focused runs passed 1/1.
- [x] If RED reveals a runtime defect rather than the intentional short-stock boundary, make only the smallest Minecraft 26.2 authority, navigation, transaction, or persistence repair needed for the ordinary path; preserve the failing test first. The first diagnostic run exposed only a tick-zero fixture race with the server entity index, so the test now waits for ordinary entity-ticking readiness before projector use; no production repair was required.
- [x] Record the pinned Workers `BuilderWorkGoal.java` mapping and the SmartBrain/Kingdom/projector transformation, remove builder from the README's remaining ordinary-path list, then run the focused GameTest twice, `runHarnesses`, two complete GameTest passes, `buildAll`, and both working-tree and staged `git diff --check` before committing `Prove ordinary builder lifecycle`. The focused builder test passed twice, `runHarnesses` passed 176 actionable tasks, two consecutive aggregate NeoForge runs passed all 88 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. The first aggregate attempt exposed an existing embedded-player ticket race in `vehicle_embodied_runtime`; refreshing its owner ticket during readiness made the unchanged vehicle assertions pass focused and in both aggregate runs without extending a timeout.

## Task 16: Ordinary-player courier lifecycle

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`
- Modify only if the black-box test exposes a gap: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify only if the black-box test exposes an authority gap: `src/main/java/galacticwars/clonewars/menu/WorksiteConfigurationMenu.java` and Kingdom worksite/order records
- Modify: `README.md`
- Modify: `docs/authorized-source-intake.md`

- [x] Register `black_box_courier_lifecycle` as an isolated, staggered GameTest with a 1,200-tick outer timeout and an 800-recruit-tick lifecycle deadline.
- [x] Use a Survival player's real hire, courier assignment, command-marker worksite/storage selection, and two replay-safe worksite-menu `CYCLE_DISPATCH_MODE` actions to establish durable `AUTOMATIC -> HYBRID -> MANUAL` dispatch. The fixture does not mutate private phases, invoke the worker controller, or move the recruit after assignment.
- [x] Establish RED with an empty registered source. At recruit tick 92 the courier had created a persisted `COURIER` order, physically moved to and approached the source, and failed closed at `BLOCKED/courier_source_empty`; source, cargo, destination, and delivery progression remained zero.
- [x] On GREEN, seed one exact three-Duracrete stack and observe source navigation, withdrawal into real recruit cargo, destination navigation, atomic deposit, terminal `COURIER` work-order persistence, one `DELIVERY_COMPLETED` event, and exact `source + cargo + destination == 3` conservation throughout. Both restored focused runs passed 1/1 without a production repair.
- [x] Record the pinned Workers `CourierWorkGoal.java`, `CourierAction.java`, and `CourierRoute.java` behavioral mapping and Minecraft 26.2 SmartBrain/Kingdom/menu transformation, and remove courier from the README's remaining ordinary-path list. Both focused GREEN runs passed 1/1, `runHarnesses` passed 176 actionable tasks, two consecutive fresh-world NeoForge runs passed all 89 required GameTests, and `buildAll` passed 186 actionable tasks for Fabric and NeoForge. Working-tree and staged `git diff --check` complete the commit gate for `Prove ordinary courier lifecycle`.

## Completion gate

This slice is complete when courier contention and expiry are conservation-proven, an exact automatic transfer survives a live-lease reload, owner removal of settlement authority releases leases and pauses/resumes configured routes without physical loss or ghost delivery, concurrent role/revision/replay cases are rejected server-side, follow/hold/patrol/work commands survive real door traversal, self-care retains ordinary commands while yielding to danger and combat, tamed recruit bolts consume physical Energy Cells with no empty-gun melee fallback, collision-level friendly-fire proof passes, an ordinary cook completes one exact physical furnace cycle without redundant fuel, an ordinary lumberjack conserves a matching sapling and connected tree through replant and deposit, an ordinary miner conserves one ore through bounded physical mining and deposit, an ordinary animal farmer conserves two feed items through bounded physical feeding, an ordinary fisher conserves its actual component-bearing catch through timed casting and deposit, an ordinary merchant completes one exact physical server-authored trade with replay rejection and a single progression event, an ordinary builder completes one projector-authored Mine with exact physical conservation and persisted cleanup, an ordinary courier completes player-configured physical source-to-destination delivery with exact conservation and persisted progression, provenance is current, and all four verification gates are green.
