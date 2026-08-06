# Miner Tool Recovery Design

**Date:** 2026-08-02

**Status:** Approved for implementation

**Target:** Minecraft 26.2, Architectury common code with NeoForge and Fabric builds

**Branch:** `codex/pr40-courier-recruit-continuation`

## Purpose

Complete the next bounded worker-acceptance slice by proving that an ordinary miner can fail closed
when its pickaxe is missing or breaks, receive a physical replacement through the existing embodied
loadout and settlement-storage paths, and resume the same persisted mining order without duplicating
or losing tools, ore, or output.

This is a Galactic-native adaptation of the authorized Workers and Recruits sources. It does not
restore Forge-era goals, mutable global managers, or donor entity hierarchies. The runtime remains
server-authoritative through SmartBrain scheduling, Kingdom `SavedData`, registered storage,
revisioned work orders, and physical Minecraft 26.2 item stacks.

## Authorized donor mapping

- Workers is pinned at `c1eb9bdb016af93eb2df2ce8e3b17fc3463d7ee1`.
  `MinerWorkGoal` declares a required pickaxe when none is available,
  `GetNeededItemsFromStorage` retrieves required physical items, and
  `AbstractWorkerEntity.switchMainHandItem` selects a matching carried item.
- Recruits is pinned at `cff03e085d65653406a8b6ddcdd0ebff615c3e48`.
  Its inventory and equipment interaction remains behavioral context for the existing Galactic
  loadout menu; no Forge menu or entity inventory implementation will be copied.
- The port rewrites those behaviors around `GalacticRecruitEntity`, `WorkerDutyLoadoutPolicy`,
  `PhysicalLogisticsTransaction`, registered Kingdom storage endpoints, and the existing
  provider-owned recruit loadout menu.

The implementation must update `docs/authorized-source-intake.md` with the exact behavioral mapping,
transformation, excluded donor machinery, and executed evidence.

## Selected approach

Use an inventory-first hybrid recovery path:

1. Keep the existing player-managed worker-tool slot as the active equipment authority.
2. Recover a compatible tool already present in the recruit's physical cargo.
3. Otherwise let the miner walk to registered settlement storage and withdraw one compatible tool.
4. If neither source contains a compatible tool, remain visibly blocked until the player supplies one.

The owner can resolve the block by placing a compatible pickaxe directly in the worker-tool slot
through the live loadout menu. A manually routed delivery may also place a pickaxe in cargo, from
which the worker can equip it on the next retry.

This slice will not publish an automatic `SupplyDemand` for tools. The current ledger has no safe
request-cancellation and return-to-storage contract when a manual replacement races an active courier
lease. Publishing a demand now could leave a withdrawn tool stranded in courier cargo or deliver a
duplicate after the owner already repaired the worker. Automatic courier-backed tool demand remains
a separate follow-up that must first define cancellation, in-flight return, and exact reconciliation.

## Runtime architecture

### Usable-tool policy

Add a policy operation that distinguishes assignment compatibility from runtime usability:

- `WorkerDutyLoadoutPolicy.isCompatible` remains unchanged because an empty slot is intentionally
  accepted while assigning a profession.
- Runtime recovery uses `isUsableTool(profession, stack)`, defined as a non-empty stack that satisfies
  the profession compatibility policy.
- The miner accepts any item in the Minecraft 26.2 pickaxe tag, not only the default iron pickaxe.
  The selected physical stack retains its durability, enchantments, custom components, and name.
- A non-empty incompatible worker-tool stack is never overwritten automatically. The miner remains
  blocked until an authorized player removes or replaces that stack through the loadout menu.

The new predicate is reusable by later farmer, lumberjack, and fisherman failure-matrix work, but only
the miner runtime is connected in this slice. No unproved behavior change is introduced for other
professions.

### Physical worker-tool endpoint

`GalacticRecruitEntity` will expose its active worker-tool slot to the logistics engine through a
private one-slot `LogisticsInventory` adapter and a bounded endpoint identity of
`recruit:<uuid>:worker_tool`.

The adapter:

- reads and replaces the live worker main-hand stack;
- accepts at most one item;
- accepts only a usable tool for the currently assigned profession;
- calls the existing loadout-change synchronization hook after mutation; and
- uses the recruit as its stable transaction identity.

Cargo-to-tool and storage-to-tool movement uses `PhysicalLogisticsTransaction` with
`REQUIRE_EXACT`. The transaction already snapshots both endpoints, compares exact item components,
rechecks policy and state immediately before commit, and rolls back a partial mutation. Production
code must not shrink a source stack and then separately set the worker hand.

Storage authority remains bounded by the registered endpoint's live slot count, loaded dimension,
settlement ownership, and recruit life/owner checks. Cargo authority remains bounded to the live
same-owner recruit.

### Recovery state flow

The ordinary mining interaction remains the first authority that identifies an unusable pickaxe:

1. The miner physically reaches an accepted ore target.
2. Before changing the block, it records the default required-tool identifier and calls
   `blockWorker("missing_tool")`.
3. The ore, cargo, storage, tool slot, completed quantity, and work-order identity remain unchanged.
4. The existing blocked cooldown naturally returns the worker to `ACQUIRE_ORDER`; the persisted
   blocked order is resumed through the current revision-aware path.
5. Before scanning for another target, miner recovery checks the active tool, then compatible cargo,
   then registered storage.

If cargo contains a compatible pickaxe, one exact stack moves atomically into the empty worker-tool
slot and the target scan continues. If registered storage contains a compatible pickaxe, the worker
transitions to `NAVIGATE_SOURCE/withdraw_worker_tool`, walks to that storage, and performs one exact
storage-to-tool transaction at interaction range. A successful transfer clears the required-item
status and returns to `ACQUIRE_ORDER` without releasing or replacing the current order.

If no compatible source exists, the worker returns to `BLOCKED/missing_tool`. If the source changes,
unloads, loses authority, becomes unreachable, or changes between simulation and commit, existing
navigation reasons or `worker_tool_transfer_failed` report the condition while the transaction leaves
both inventories unchanged. The same order remains available for a later retry.

Direct player replacement needs no special resume packet. The existing blocked retry observes the
new live tool slot, resumes the same order, and continues naturally.

## Persistence and compatibility

No schema version changes are required. The design reuses persisted fields that already survive
entity and world saves:

- worker profession and duty tool;
- worker phase, reason, target, and retry cursor;
- assigned worksite and registered storage endpoints; and
- durable work-order identifier, state, revision, and completed quantity.

The new reason strings are ordinary bounded status values. An older save blocked on `missing_tool`
will recover through the new logic after loading if cargo or registered storage contains a compatible
pickaxe. Existing item components remain the physical source of truth.

## Player-facing behavior

- A miner never mines correct-tool ore with an empty or incompatible pickaxe.
- `missing_tool` remains visible while no valid replacement is available.
- The owner can walk to the recruit and use the existing logistics-authorized loadout menu to install
  a replacement.
- A stocked base lets the miner physically return to registered storage, take one compatible pickaxe,
  and return to work.
- No free default tool appears, no detached strategy screen resolves the failure, and no physical
  item disappears merely because a status or ledger value changed.

## Runtime acceptance tests

Add two isolated NeoForge GameTests beside the existing ordinary miner lifecycle proof. Both measure
their deadlines from real entity-ticking readiness and avoid private phase mutation, direct controller
invocation, or post-assignment recruit teleportation.

### Missing-tool loadout recovery

1. Use a Survival owner, active Command Center, real hire and miner-assignment actions, configured
   worksite, registered storage, and one in-bounds iron ore.
2. Prove the loadout opening authority, then use the same provider-owned server menu accepted by the
   existing headless-menu convention to remove the assigned pickaxe.
3. Observe physical approach and `BLOCKED/missing_tool`; capture the persisted mining-order ID and
   assert that ore, raw iron, cargo, storage, and tool counts remain exact.
4. Put the same physical pickaxe back through the menu.
5. Require natural retry, the same order ID, ore removal, one raw-iron output, exactly one point of
   pickaxe wear, registered-storage deposit, and terminal order completion.

### Broken-tool storage recovery

1. Use two in-bounds iron ores and install a compatible pickaxe with exactly one durability point
   remaining through the provider-owned loadout menu.
2. Let ordinary mining consume that final durability point on the first ore and conserve its exact
   raw-iron output. Track the original pickaxe identity before the break and assert its removal
   from the worker-tool slot after the break.
3. With no replacement initially available, require the next mining order to reach its target and
   block as `missing_tool`; capture that second order ID.
4. Add one component-bearing compatible replacement to registered physical storage as the fixture's
   explicit resupply event. This event mutates only the registered container; it does not move or
   mutate the recruit, controller phase, work target, or persisted order.
5. Observe natural retry, physical travel to storage, `withdraw_worker_tool`, an exact atomic transfer,
   return to the second ore, and completion of the same captured order.
6. Throughout the test, assert exactly two ore/raw-iron units, exactly one replacement moving only
   among storage and the worker-tool slot, preserved replacement components, and exactly one
   durability point consumed from the replacement by the second ore.

The controlled RED must be captured before production recovery is implemented. It should show the
broken miner repeatedly blocking with the replacement still in registered storage and the second ore
unchanged.

## Verification and publication gates

Implementation is complete only after all of the following evidence is fresh:

1. The controlled RED fails for the intended missing recovery path before the production repair.
2. Each focused tool-recovery GameTest passes twice.
3. `runHarnesses` passes.
4. Two consecutive complete NeoForge GameTest runs pass from fresh generated GameTest worlds.
5. `buildAll` passes for Fabric and NeoForge on Java 25.
6. Working-tree and staged `git diff --check` pass.
7. A focused code review reports no unresolved Critical or Important findings.
8. README acceptance status and donor provenance match the runtime evidence exactly.

After the verified implementation commits are ready, query PR #40's live `headRefName` and head SHA.
Push `codex/pr40-courier-recruit-continuation`, then create or update one stacked pull request whose
base is PR #40's current head branch. Verify local HEAD, remote branch SHA, stacked-PR head SHA, and
base branch before reporting publication. Do not create a duplicate PR if one already exists.

## Non-goals

- Automatic courier-created or courier-cancelled tool demands.
- Tool recovery for professions other than miner.
- Full-storage, changed-target, unreachable-target, cancellation, death, threat, or chunk-reload
  matrix coverage beyond conditions directly exercised by the atomic tool transfer.
- New screens, detached automation menus, new persistence schemas, or donor Forge abstractions.
