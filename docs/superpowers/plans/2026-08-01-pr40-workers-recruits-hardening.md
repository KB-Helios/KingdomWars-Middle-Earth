# PR #40 Workers and Recruits Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Repair every current PR #40 finding, make the Minecraft 26.2 worker/recruit runtime deterministic and failure-atomic, and publish a fully verified repair head before starting the focused continuation branch.

**Architecture:** Keep SmartBrainLib as the only recruit scheduler, Kingdom `SavedData` as durable authority, Architectury as the cross-loader boundary, and physical component-preserving inventories as the logistics truth. Add small pure policy seams for cadence, bounds, route equality, overlay selection, UI availability, and hybrid dispatch; prove world, menu, entity-memory, and inventory behavior with NeoForge GameTests.

**Tech Stack:** Java 25, Kotlin 2.4, Minecraft 26.2, NeoForge `26.2.0.25-beta`, Fabric `0.19.3`, Architectury `21.0.4`, SmartBrainLib `2.0.0`, Gradle executable harnesses, NeoForge GameTests.

## Global Constraints

- Target Minecraft `26.2` only; do not import Forge 1.20.1 APIs, registries, goals, packets, global managers, or navigation classes.
- Authorized donor revisions are recruits `cff03e085d65653406a8b6ddcdd0ebff615c3e48` and workers `c1eb9bdb016af93eb2df2ce8e3b17fc3463d7ee1`.
- Record substantive donor-derived behavior in `docs/authorized-source-intake.md` before merge.
- Every production behavior change starts with a test that is observed failing for the intended reason.
- Preserve physical cargo, component identity, registered slot authority, current worksite revisions, and save compatibility.
- Treat review comments `3695614054`, `3695614061`, and `3695614079` as evidence-only unless fresh evidence contradicts the audited call chains.
- Do not claim Survival, visual client, or two-client dedicated-server acceptance unless those gates are actually run.

---

### Task 1: Locale-stable worker values and shared worksite bounds

**Review comments:** `3695614077`, `3695614078`

**Files:**
- Create: `src/test/java/galacticwars/clonewars/workforce/WorkforceValueObjectsTest.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkAreaBounds.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/BoundedWorkerProfessionBehavior.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkerAction.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkerStatus.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkerResourceDecision.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/ResourceInventory.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkerAssignment.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`

**Interfaces:**
- Produces: `WorkAreaBounds.containsCenteredAt(int centerX, int centerY, int centerZ, int x, int y, int z): boolean`.
- Produces: locale-independent normalization for every worker item/resource/dimension identifier touched by this port.

- [x] **Step 1: Write the failing value-object harness**

Add a `main` harness that temporarily sets `Locale.setDefault(Locale.forLanguageTag("tr-TR"))`, constructs `WorkerAction`, `WorkerStatus`, `WorkerResourceDecision`, `ResourceInventory`, and `WorkerAssignment` with uppercase `I`, and asserts literal ASCII-lowercase values. Add centered-bound assertions for odd and even dimensions:

```java
WorkAreaBounds bounds = new WorkAreaBounds(4, 3, 2);
assertTrue(bounds.containsCenteredAt(10, 20, 30, 9, 19, 30), "inclusive minimum");
assertTrue(bounds.containsCenteredAt(10, 20, 30, 12, 21, 31), "inclusive maximum");
assertFalse(bounds.containsCenteredAt(10, 20, 30, 8, 20, 30), "outside x");
assertEquals("minecraft:iron_ingot",
        new WorkerAction(WorkerAction.Type.WITHDRAW, Optional.empty(),
                "minecraft:IRON_INGOT", 1, "test").itemId(),
        "action item normalization");
```

Restore the original default locale in `finally` and print `WorkforceValueObjectsTest passed`.

- [x] **Step 2: Run the harness and verify RED**

Run: `rtk .\gradlew.bat :neoforge:runGalacticwarsClonewarsWorkforceWorkforceValueObjectsTest --no-daemon --console=plain`

Expected: FAIL because `containsCenteredAt` does not exist; after adding only its test compile seam, the Turkish-locale assertions must fail on at least one existing `toLowerCase()` site.

- [x] **Step 3: Implement the shared predicate and locale fixes**

Add to `WorkAreaBounds`:

```java
public boolean containsCenteredAt(
        int centerX, int centerY, int centerZ,
        int x, int y, int z
) {
    int minX = centerX - (width - 1) / 2;
    int maxX = centerX + width / 2;
    int minY = centerY - (height - 1) / 2;
    int maxY = centerY + height / 2;
    int minZ = centerZ - (depth - 1) / 2;
    int maxZ = centerZ + depth / 2;
    return x >= minX && x <= maxX
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
}
```

Replace the duplicated arithmetic in `BoundedWorkerProfessionBehavior.insideWorksite` and `GalacticRecruitEntity.isInsideWorksiteBounds` with this method. Import `java.util.Locale` and use `toLowerCase(Locale.ROOT)` in the five workforce value objects; use `java.util.Locale.ROOT` when loading `WorkerRequiredItem`.

- [x] **Step 4: Run the harness and verify GREEN**

Run the same focused Gradle task. Expected: PASS with `WorkforceValueObjectsTest passed`.

- [x] **Step 5: Commit**

```powershell
git add src/main/java/galacticwars/clonewars/workforce src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/test/java/galacticwars/clonewars/workforce/WorkforceValueObjectsTest.java
git commit -m "Harden worker value normalization and bounds"
```

### Task 2: Bounded ranged movement and cached cover evaluation

**Review comments:** `3695614048`, `3695614052`

**Files:**
- Create: `src/main/java/galacticwars/clonewars/entity/ai/RecruitAiCadence.java`
- Create: `src/test/java/galacticwars/clonewars/entity/ai/RecruitCombatMovementTest.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/ArmyCombatBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitCombatMovement.java`

**Interfaces:**
- Produces: `RecruitAiCadence.shouldRecomputeArmyCover(int tickCount): boolean` with an eight-tick interval.
- Produces: `RecruitAiCadence.shouldCheckSelfCare(int tickCount): boolean` with a twenty-tick interval.
- Produces: `RecruitCombatMovement.rankCandidates(...)` that deduplicates candidates and invokes each safety and cover predicate no more than once per unique position.

- [x] **Step 1: Write the failing movement harness**

In the AI package, feed duplicate `BlockPos` values into the wished-for ranking seam, count predicate calls in maps, and assert literal order: covered candidates first, then distance, then `asLong`. Enumerate ticks `0..15` and assert only `0` and `8` request army cover recomputation.

```java
List<BlockPos> ranked = RecruitCombatMovement.rankCandidates(
        List.of(near, far, near), origin,
        candidate -> { safetyCalls.merge(candidate, 1, Integer::sum); return true; },
        candidate -> { coverCalls.merge(candidate, 1, Integer::sum); return candidate.equals(far); });
assertEquals(List.of(far, near), ranked, "cover-first stable ranking");
assertEquals(Map.of(near, 1, far, 1), safetyCalls, "one safety evaluation per unique position");
assertEquals(Map.of(near, 1, far, 1), coverCalls, "one raycast decision per safe position");
assertEquals(List.of(0, 20), IntStream.range(0, 40)
        .filter(RecruitAiCadence::shouldCheckSelfCare).boxed().toList(),
        "self-care cadence");
```

- [x] **Step 2: Run the harness and verify RED**

Run: `rtk .\gradlew.bat :neoforge:runGalacticwarsClonewarsEntityAiRecruitCombatMovementTest --no-daemon --console=plain`

Expected: compilation FAIL because the cadence and ranking seams do not exist.

- [x] **Step 3: Implement cadence and cached ranking**

Implement `RecruitAiCadence.shouldRecomputeArmyCover` with `Math.floorMod(tickCount, 8) == 0`. In `ArmyCombatBehaviour.holdOrRepositionRanged`, return without replacing `WALK_TARGET` when the recruit is in range/visible but the cadence is closed. In `RecruitCombatMovement`, build a `LinkedHashSet<BlockPos>` of immutable candidates, evaluate `safeStand` once, cache `hasCover` in a record or map, and sort only cached booleans and coordinates.

- [x] **Step 4: Run the focused harness and compile both loaders**

Run:

```powershell
rtk .\gradlew.bat :neoforge:runGalacticwarsClonewarsEntityAiRecruitCombatMovementTest :fabric:compileJava --no-daemon --console=plain
```

Expected: PASS and no Java compile failures.

- [x] **Step 5: Commit**

```powershell
git add src/main/java/galacticwars/clonewars/entity/ai src/test/java/galacticwars/clonewars/entity/ai/RecruitCombatMovementTest.java
git commit -m "Bound recruit ranged repositioning"
```

### Task 3: Damage-driven worker retreat, hazard arbitration, and self-care cadence

**Review comments:** `3695614051`, `3695614054` evidence, `3695614056`, `3695614058`, `3695642795`

**Files:**
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitAiCadence.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitSelfCareBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitSitBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitWalkTargetBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/NaturalCivilianWorkBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/CivilianShelterBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/FactionPlayerReactionBehaviour.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/ai/RecruitBrain.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`

**Interfaces:**
- Consumes: `RecruitAiCadence` from Task 2.
- Produces: immediate `HURT_BY_ENTITY` publication for accepted living attackers.
- Produces: non-combat walk producers that yield to an active hazard escape.

- [x] **Step 1: Extend GameTests before production changes**

Keep `worker_safety_and_upkeep` as the existing RED regression. Add an isolated `recruit_hazard_and_self_care` GameTest that:

1. Places an unlit campfire and proves a recruited worker does not enter hazard avoidance.
2. Lights the same campfire and proves an escape `WALK_TARGET` survives ordered-sit, civilian, faction-reaction, and random-idle scheduling until the recruit leaves danger.
3. Exhausts a tame off-duty recruit carrying a stack of three food items and proves exactly one item is consumed during the first 20-tick cadence window.

Add the test to `createTests`, an isolated environment, the SmartBrain runtime list, and a 360-tick timeout.

- [x] **Step 2: Run GameTests and verify RED**

Run: `rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: `worker_safety_and_upkeep` fails with “Damaged worker never entered its safety retreat”; the new test fails because unlit campfires are classified as dangerous and the exhausted recruit consumes multiple food items before the next 20-tick boundary.

- [x] **Step 3: Implement the damage and hazard fixes**

After `super.actuallyHurt`, when health decreased and `damageSource.getEntity()` is a living entity, call:

```java
BrainUtil.setMemory(this, MemoryModuleType.HURT_BY_ENTITY, attacker);
```

Classify campfires with their lit property:

```java
|| (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
        && state.getValue(BlockStateProperties.LIT)
```

Add `!recruit.isHazardAvoidanceActive()` to sit, natural work, shelter, faction reaction, and idle-wander predicates. In `RecruitWalkTargetBehaviour`, apply the ordered-sit clearing branch only when hazard avoidance is inactive. Preserve the already-audited pickup chain through `canStartRecruitItemPickup` without adding a redundant production branch.

- [x] **Step 4: Implement the 20-tick self-care cadence**

Add `RecruitAiCadence.shouldCheckSelfCare(int tickCount)` and require it in `RecruitSelfCareBehaviour.checkExtraStartConditions` before `shouldUseRecruitSelfCare()`.

- [x] **Step 5: Run GameTests and verify GREEN twice for the changed tests**

Run the full GameTest command twice. Expected on both fresh worlds: every required test passes, including `worker_safety_and_upkeep` and `recruit_hazard_and_self_care`.

- [x] **Step 6: Commit**

```powershell
git add src/main/java/galacticwars/clonewars/entity src/main/java/galacticwars/clonewars/entity/ai src/main/java/galacticwars/clonewars/gametest/ModGameTests.java
git commit -m "Make recruit safety arbitration deterministic"
```

### Task 4: Revisioned worksite configuration and truthful UI availability

**Review comments:** `3695614069`, `3695614073`, `3695614075`, `3695642790`, `3695642796`, `3695642797`

**Files:**
- Create: `src/test/java/galacticwars/clonewars/workforce/WorkAreaConfigurationTest.java`
- Create: `src/main/java/galacticwars/clonewars/client/gui/CommandCenterActionAvailability.java`
- Create: `src/test/java/galacticwars/clonewars/client/gui/CommandCenterActionAvailabilityTest.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkAreaConfiguration.java`
- Modify: `src/main/java/galacticwars/clonewars/kingdom/SettlementRecord.java`
- Modify: `src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java`
- Modify: `src/main/java/galacticwars/clonewars/menu/WorksiteConfigurationMenu.java`
- Modify: `src/main/java/galacticwars/clonewars/menu/WorksiteConfigurationMenuProvider.java`
- Modify: `src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java`
- Modify: `src/main/java/galacticwars/clonewars/client/gui/CommandCenterOperationsScreen.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`

**Interfaces:**
- Produces: `WorkAreaConfiguration.withCourierRoute` identity return for equal route/mode.
- Produces: provider factories that return `Optional<WorksiteConfigurationMenuProvider>` only after snapshot capture succeeds.
- Produces: `CommandCenterActionAvailability.canConfigureWorksite(Optional<WorkerSummary>): boolean`.

- [x] **Step 1: Write failing pure harnesses**

`WorkAreaConfigurationTest` must assert an identical route and mode return the same object and retain both revisions. `CommandCenterActionAvailabilityTest` must assert absent worker and worker-with-empty-worksite are disabled while an assigned summary is enabled.

- [x] **Step 2: Add failing runtime authority assertions**

Extend `workforce_saved_data_authority` or add `worksite_configuration_authority` to prove:

- a `BUILDER` member with `MANAGE_WORKSITES` but no `MANAGE_LOGISTICS` receives `permission_denied` for route mutation;
- the owner receives `invalid_route` for exactly one waypoint;
- an identical route/mode returns `unchanged` without changing settlement/configuration revisions;
- provider preparation for a recruit without durable worksite returns empty and does not construct a menu;
- a valid assigned recruit produces a snapshot and opens normally.

- [x] **Step 3: Run harnesses and GameTests for RED**

Run the two focused harness tasks, then `runGameTestServer`. Expected failures must name the missing identity short-circuit, UI availability, permission, single-waypoint, or preflight behavior.

- [x] **Step 4: Implement route validation and no-op identity**

In `WorkAreaConfiguration.withCourierRoute`, normalize the incoming list, then return `this` when both route and mode equal current values. In `SettlementRecord.configureWorksiteRoute`, return `this` when the configuration identity is unchanged. In `KingdomSavedData.configureWorksiteRoute`, require both permissions and reject `route.size() == 1` as `invalid_route`.

- [x] **Step 5: Implement snapshot preflight**

Change snapshot capture to an `Optional` result with missing kingdom, settlement, profession, or worksite mapped to empty. Make provider creation accept the captured immutable snapshot rather than a recruit whose menu constructor can throw. Both direct recruit and Command Center opening paths must call the factory, report `worksite_missing`, and skip `MenuRegistry.openExtendedMenu` when empty. Refresh must close the menu if a subsequent capture becomes empty.

- [x] **Step 6: Implement truthful client availability**

Have `CommandCenterActionAvailability.canConfigureWorksite` return:

```java
return worker.flatMap(WorkerSummary::worksite).isPresent();
```

Use the selected `WorkerSummary` rather than only its UUID when constructing the Configure Worksite action.

- [x] **Step 7: Verify GREEN and commit**

Run both focused harnesses and GameTests. Then commit:

```powershell
git add src/main/java/galacticwars/clonewars/client src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/kingdom src/main/java/galacticwars/clonewars/menu src/main/java/galacticwars/clonewars/workforce/WorkAreaConfiguration.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java src/test/java/galacticwars/clonewars/client src/test/java/galacticwars/clonewars/workforce/WorkAreaConfigurationTest.java
git commit -m "Enforce revisioned worksite configuration"
```

### Task 5: Bounded cooking tags, food slots, and worksite overlays

**Review comments:** `3695614064`, `3695614081`, `3695619032`

**Files:**
- Create: `src/main/java/galacticwars/clonewars/workforce/WorksiteOverlaySelector.java`
- Create: `src/test/java/galacticwars/clonewars/workforce/WorksiteOverlaySelectorTest.java`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorksiteOverlayService.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`

**Interfaces:**
- Produces: `WorksiteOverlaySelector.nearestVisible(Collection<WorksiteRecord>, double playerX, double playerY, double playerZ, int limit)` with distance then UUID ordering.
- Consumes: Minecraft registry tag holders instead of iterating the entire item registry.

- [x] **Step 1: Write failing overlay and runtime tests**

The pure selector harness creates more candidates than the budget and asserts exactly the nearest IDs are returned with stable UUID ties. Extend `specialist_worker_loops` or add `bounded_worker_scans` to configure a cook tag and prove a recipe-bearing tag member is selected. Add a storage endpoint exposing one slot backed by a larger container; put food only beyond slot one and prove the recruit publishes no unreachable demand, then move food into slot zero and prove it can publish the demand.

- [x] **Step 2: Run tests and verify RED**

Run the selector harness and full GameTests. Expected: selector API is absent and the out-of-authority food slot currently affects demand discovery.

- [x] **Step 3: Implement bounded scans**

For tag filters, use the Minecraft 26.2 `BuiltInRegistries.ITEM.getTagOrEmpty(tag)` API and iterate only holder values before recipe checks. For food, keep each `StorageEndpoint` paired with its container and loop only to `Math.min(endpoint.slots(), container.getContainerSize())`.

- [x] **Step 4: Implement overlay budgeting**

Set `MAX_WORKSITES_PER_PLAYER` to a fixed small budget, collect only already-authorized visible candidates, call the pure selector, then render the existing 16 markers for selected worksites only.

- [x] **Step 5: Verify GREEN and commit**

Run the selector harness and GameTests, then commit:

```powershell
git add src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/workforce src/main/java/galacticwars/clonewars/gametest/ModGameTests.java src/test/java/galacticwars/clonewars/workforce/WorksiteOverlaySelectorTest.java
git commit -m "Bound worker storage and overlay scans"
```

### Task 6: Persisted hybrid courier arbitration

**Review comment:** `3695619034`

**Files:**
- Create: `src/main/java/galacticwars/clonewars/workforce/CourierDispatchTurn.java`
- Create: `src/test/java/galacticwars/clonewars/workforce/CourierDispatchTurnTest.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`

**Interfaces:**
- Produces: persisted `CourierHybridTurn` entity key with backward-compatible automatic-first default.
- Produces: `CourierDispatchTurn.preferredSources(CourierDispatchMode mode, boolean activeReservation, boolean routeAvailable): List<Source>` and next-turn methods after a source is actually selected.
- Produces: automatic reservation resumption before new hybrid source selection.

- [x] **Step 1: Write the failing dispatch harness and GameTest**

The pure harness enumerates manual, automatic, and hybrid source orders for `(hasActiveReservation, routeAvailable, turn)`. It asserts active reservations yield only `AUTOMATIC`, automatic-first hybrid yields `[AUTOMATIC, ROUTE]`, route-first hybrid yields `[ROUTE, AUTOMATIC]`, and successful selection flips to the other turn. Add an isolated `hybrid_courier_dispatch` GameTest with a sustained demand and a valid two-waypoint route; observe one automatic acquisition and one route acquisition, save/reload the recruit between selections, and assert the persisted turn prevents automatic starvation.

- [x] **Step 2: Run tests and verify RED**

Run the focused dispatch harness and full GameTests. Expected: missing policy API and hybrid remains automatic-only while demand persists.

- [x] **Step 3: Implement policy and entity persistence**

Define `CourierDispatchTurn` with `AUTOMATIC` and `ROUTE`, a nested `Source`, `preferredSources`, `afterAutomatic`, and `afterRoute`. In `acquireCourierOrder`, resume a valid active supply reservation first. Otherwise, iterate the preferred sources for HYBRID, let the existing automatic acquisition report whether it selected work, select a configured route only when present, and store the next turn only after one source succeeds. Write/read `CourierHybridTurn` with `AUTOMATIC` as the missing-key default; do not bump Kingdom schema 11.

- [x] **Step 4: Verify GREEN across save/reload and commit**

Run the harness and GameTests twice, then commit:

```powershell
git add src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/workforce/CourierDispatchTurn.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java src/test/java/galacticwars/clonewars/workforce/CourierDispatchTurnTest.java
git commit -m "Alternate persisted hybrid courier work"
```

### Task 7: Physical market integrity and durable recruit storage selection

**Review comments:** `3695619028`, `3695619038`, `3695619040`

**Files:**
- Modify: `src/main/java/galacticwars/clonewars/economy/PhysicalTradeService.java`
- Modify: `src/main/java/galacticwars/clonewars/menu/MerchantTradeMenu.java`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`
- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`

**Interfaces:**
- Produces: a supplied merchant is eligible only while `isMarketAvailable()` remains true.
- Produces: progression preflight before physical stock reservation.
- Produces: `configureWorkerStorageFromMenu` commits through `KingdomSavedData.configureWorksiteStorage` before mirroring the loaded entity target.

- [x] **Step 1: Add failing trade and storage assertions**

Extend `faction_trader_disposition_runtime` to close the merchant market after capturing a quote and assert preview and purchase return `merchant_unavailable`, stock count is unchanged, and `MerchantTradeMenu.stillValid` is false. Inject a valid progression state whose `TRADE_COMPLETED` total is `Integer.MAX_VALUE`, attempt a purchase, and assert `progression_limit_reached` with unchanged physical stock and credits. Extend worksite authority coverage to choose registered storage B for a worksite using A, run authority reconciliation, and assert both durable worksite and loaded recruit remain on B.

Build the overflow fixture independently of the code under test and install it only in the test's `ProgressionSavedData.states` map through the existing reflection-fixture pattern:

```java
ProgressionState overflow = new ProgressionState(
        ProgressionState.CURRENT_SCHEMA_VERSION,
        player.getUUID(),
        "galacticwars:republic",
        0,
        Set.of(),
        Map.of(ProgressionEventType.TRADE_COMPLETED, Integer.MAX_VALUE),
        Map.of(),
        Set.of("intro_quest", "faction_intro"));
setSavedDataState(progression, player.getUUID(), overflow);
```

Add this GameTest-only helper beside the existing reflection fixtures:

```java
@SuppressWarnings("unchecked")
private static void setSavedDataState(
        ProgressionSavedData data, UUID playerId, ProgressionState state
) {
    try {
        var field = ProgressionSavedData.class.getDeclaredField("states");
        field.setAccessible(true);
        ((Map<UUID, ProgressionState>) field.get(data)).put(playerId, state);
    } catch (ReflectiveOperationException failure) {
        throw new IllegalStateException("Could not install progression overflow fixture", failure);
    }
}
```

- [x] **Step 2: Run GameTests and verify RED**

Run the full GameTest command. Expected: closed merchant purchase falls through to synthetic stock and/or progression rejection removes stock; storage reconciliation restores A over the entity-only B selection.

- [x] **Step 3: Implement physical-market and progression ordering**

Reject any supplied merchant that fails `isMarketAvailable()` in preview. Make menu validity require the same predicate. Build and evaluate the `ProgressionEvent` after eligibility and item resolution but before `takeMerchantStock`; return rejection/duplicate before inventory mutation. Keep existing credit and progression-commit compensation after reservation.

- [x] **Step 4: Implement revisioned storage selection**

Resolve the exact registered `StorageEndpoint` and assigned `WorksiteRecord`, call `configureWorksiteStorage(actorId, worksite.id(), worksite.configuration().revision(), endpoint)`, and only after an accepted result release the active work order, update the entity target, and transition to `storage_assigned`. Route the legacy `SET_STORAGE` case through this method.

- [x] **Step 5: Verify GREEN and commit**

Run GameTests twice, then commit:

```powershell
git add src/main/java/galacticwars/clonewars/economy/PhysicalTradeService.java src/main/java/galacticwars/clonewars/menu/MerchantTradeMenu.java src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java
git commit -m "Preserve physical trade and storage authority"
```

### Task 8: Provenance, evidence-only findings, README truth, and full verification

**Review comments:** `3695614054`, `3695614061`, `3695614079`, plus verification evidence for all code-changing comments.

**Files:**
- Modify: `docs/authorized-source-intake.md`
- Modify: `README.md`
- Modify: `docs/superpowers/plans/2026-08-01-pr40-workers-recruits-hardening.md`

**Interfaces:**
- Produces: current donor provenance and truthful automated/runtime evidence.
- Produces: exact review replies for all 24 comments without speculative changes.

- [x] **Step 1: Record donor derivations**

Add a ledger row naming both pinned commits and the exact safety/self-care/courier paths. State that cadence, attacker retention, bounded escape arbitration, and persisted route turn were selectively adapted into SmartBrain/Kingdom architecture; list the focused harnesses and GameTests.

- [x] **Step 2: Confirm the three evidence-only invariants in current code**

Run:

```powershell
rg -n "nearbyRecruitPickupTarget|canCollectRecruitItem|canStartRecruitItemPickup|hazardAvoidanceActive" src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java
rg -n "REQUIRE_EXACT|exactTransferDoesNotPartiallyMutate|completeSupply|transferCargoBetween" src/main/java/galacticwars/clonewars
rg -n "\.cancel\(|cancel\(" src/main/java/galacticwars/clonewars -g '*.java'
```

Expected: pickup reaches the hazard gate; exact transfer failures leave both inventories unchanged; `WorkerProfessionBehavior.cancel` still has no runtime caller or ledger authority.

- [x] **Step 3: Run focused and full verification**

Run, in order:

```powershell
rtk .\gradlew.bat runHarnesses --no-daemon --console=plain
rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain
rtk .\gradlew.bat runGameTestServer --no-daemon --console=plain
rtk .\gradlew.bat buildAll --no-daemon --console=plain
rtk git diff --check
```

Read every exit code and the GameTest required/pass/fail totals. Update `README.md` only with these fresh counts; retain explicit unverified labels for Survival, visual client, and two-client dedicated-server gates.

- [x] **Step 4: Audit plan and review coverage**

Create a local checklist mapping all 24 IDs to a commit/test or evidence reply. Confirm no code-changing finding lacks regression proof, no evidence-only finding gained speculative code, and the worktree diff contains only intended PR repairs, tests, provenance, and truthful docs.

Final review coverage checklist:

| Comment | Repair or evidence | Focused proof |
| --- | --- | --- |
| `3695614048` | `072495a` bounded ranged recomputation | `RecruitCombatMovementTest`, `ungrouped_recruit_ranged_brain` |
| `3695614051` | `e08d0ce` hazard arbitration | `recruit_hazard_and_self_care`, `worker_safety_and_upkeep` |
| `3695614052` | `072495a` cached unique-candidate ranking | `RecruitCombatMovementTest` |
| `3695614054` | Evidence only: both pickup entry points call `canStartRecruitItemPickup`, which rejects active hazard escape | Source call-chain audit, `recruit_hazard_and_self_care` |
| `3695614056` | `e08d0ce` 20-tick self-care cadence | `RecruitCombatMovementTest`, `recruit_hazard_and_self_care` |
| `3695614058` | `e08d0ce` lit-campfire classification | `recruit_hazard_and_self_care` |
| `3695614061` | Evidence only: delivery and rollback use failure-atomic `REQUIRE_EXACT` | `PhysicalLogisticsGameTests.exactTransferDoesNotPartiallyMutate`, `physical_logistics_transaction` |
| `3695614064` | `03bd8c5` tag-holder cooking resolution | `specialist_worker_loops`, `bounded_worker_scans` |
| `3695614069` | `b0c3fb1` split worksite/logistics permission authority | `WorkAreaConfigurationTest`, `workforce_saved_data_authority` |
| `3695614073` | `b0c3fb1` identical-route equality short-circuit | `WorkAreaConfigurationTest`, `workforce_saved_data_authority` |
| `3695614075` | `b0c3fb1` worksite-menu snapshot preflight | `CommandCenterActionAvailabilityTest`, `command_center_workforce_control` |
| `3695614077` | `28c5f93` shared inclusive worksite bounds | `WorkAreaConfigurationTest`, `WorkforceValueObjectsTest` |
| `3695614078` | `28c5f93` `Locale.ROOT` persisted identifier normalization | `WorkforceValueObjectsTest` |
| `3695614079` | Evidence only: `WorkerProfessionBehavior.cancel` has no runtime caller or Kingdom ledger authority | Production call-site audit |
| `3695614081` | `03bd8c5` nearest-first bounded overlay selection | `WorksiteOverlaySelectorTest`, `bounded_worker_scans` |
| `3695619028` | `eb42b6e` closed physical-market rejection and menu invalidation | `faction_trader_disposition_runtime` |
| `3695619032` | `03bd8c5` endpoint-authorized food-slot scan | `specialist_worker_loops`, `bounded_worker_scans` |
| `3695619034` | `15709e0` entity-persisted hybrid dispatch turn | `CourierDispatchTurnTest`, `hybrid_courier_dispatch` |
| `3695619038` | `eb42b6e` progression preflight before stock reservation | `faction_trader_disposition_runtime` |
| `3695619040` | `eb42b6e` revisioned durable storage selection | `workforce_saved_data_authority` |
| `3695642790` | `b0c3fb1` selected-worker worksite action availability | `CommandCenterActionAvailabilityTest` |
| `3695642795` | Duplicate resolved by `e08d0ce` | Same cadence harness and GameTest as `3695614056` |
| `3695642796` | Duplicate resolved by `b0c3fb1` | Same revision-stability proof as `3695614073` |
| `3695642797` | `b0c3fb1` one-waypoint route rejection | `WorkAreaConfigurationTest`, `workforce_saved_data_authority` |

Final automated evidence: `runHarnesses` completed 175 actionable tasks; two fresh NeoForge servers each passed all 76 required GameTests; `buildAll` completed 185 actionable tasks and built Fabric plus NeoForge for Minecraft 26.2. Manual Survival, visual-client, and two-client dedicated-server acceptance remain explicitly unverified.

- [x] **Step 5: Commit verified repairs**

```powershell
git add README.md docs/authorized-source-intake.md docs/superpowers/plans/2026-08-01-pr40-workers-recruits-hardening.md src
git diff --cached --check
git commit -m "Stabilize workers and recruits runtime port"
```

- [ ] **Step 6: Push the verified repair head and synchronize PR state**

Push the current HEAD explicitly to the existing PR branch:

```powershell
git push origin HEAD:codex/workers-recruits-runtime-port
git rev-parse HEAD
git ls-remote origin refs/heads/codex/workers-recruits-runtime-port
gh api repos/KB01111/GalacticWars/pulls/40 --jq '.head.sha'
```

Require all three SHAs to match. Reply to every inline comment with its exact commit/test evidence, refresh the PR body and verification counts, and keep the PR ready only when all automated gates are green.

- [ ] **Step 7: Start the continuation boundary**

After PR #40 synchronization, create `codex/pr40-courier-recruit-continuation` from the repaired head. Write a separate implementation plan for the already-approved continuation slice: competing couriers/lease expiry, concurrent authority/replay, follow-hold-patrol door resumption, ordinary-command self-care, friendly fire, and zero-ammunition blaster behavior. If #40 is still open, publish that branch as a stacked draft against `codex/workers-recruits-runtime-port`.
