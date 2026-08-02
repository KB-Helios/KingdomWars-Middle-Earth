# Miner Tool Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add ordinary-path missing and broken pickaxe recovery so a Minecraft 26.2 miner can receive one exact physical replacement from its loadout, cargo, or registered storage and resume the same persisted order.

**Architecture:** Keep the live worker-tool slot as equipment authority and expose it as a private one-slot `LogisticsInventory` endpoint inside `GalacticRecruitEntity`. The miner first fails closed at its ore, then natural blocked-order retry checks the active slot, physical cargo, and registered storage; every cargo/storage transfer uses `PhysicalLogisticsTransaction` with exact component matching and rollback.

**Tech Stack:** Java 25, Minecraft 26.2, Architectury common code, NeoForge GameTests, Fabric and NeoForge Gradle builds, SmartBrainLib, Kingdom `SavedData`, physical Minecraft inventories.

## Global Constraints

- Target exactly Minecraft 26.2; do not substitute older Minecraft, Forge, Quilt, or loader-specific common-code APIs.
- Keep Java 25 and the existing Architectury, NeoForge, Fabric, and SmartBrainLib boundaries.
- Workers donor revision is exactly `c1eb9bdb016af93eb2df2ce8e3b17fc3463d7ee1`.
- Recruits donor revision is exactly `cff03e085d65653406a8b6ddcdd0ebff615c3e48`.
- Adapt donor behavior to Galactic runtime and document provenance; do not import Forge goals, donor entity hierarchies, global managers, packets, or menus.
- Do not create an automatic tool `SupplyDemand`; manual-resupply/courier races lack cancellation and in-flight return semantics.
- Do not generate a free replacement tool or overwrite a non-empty incompatible tool.
- Preserve exact item components, durability, enchantments, custom names, item counts, work-order identity, and work-order revision authority.
- GameTests must not mutate private worker phases, call the worker controller, or move the recruit after assignment.
- A headless menu test may construct the provider-owned server menu only after proving the same live distance and logistics-authority gates.
- Update runtime claims only after the corresponding focused and aggregate evidence passes.

---

## File Structure

- Modify `src/main/java/galacticwars/clonewars/workforce/WorkerDutyLoadoutPolicy.java`: define runtime tool usability without changing assignment compatibility.
- Modify `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java`: coordinate miner recovery and host the private one-slot logistics adapter and exact cargo/storage transfer methods.
- Modify `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java`: register and execute the two isolated ordinary-path recovery scenarios plus bounded menu/container helpers.
- Modify `README.md`: distinguish completed miner tool recovery from the still-open profession failure matrix.
- Modify `docs/authorized-source-intake.md`: record the pinned donor methods, Galactic transformation, exclusions, and executed evidence.
- Modify `docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md`: append Task 18 and its exact verification record.
- Reference `docs/superpowers/specs/2026-08-02-miner-tool-recovery-design.md`: the approved source of truth; do not change scope while executing this plan.

---

### Task 1: Broken-tool storage recovery and physical runtime

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java:327-570,9037-9162,11579-11645`
- Modify: `src/main/java/galacticwars/clonewars/workforce/WorkerDutyLoadoutPolicy.java:9-55`
- Modify: `src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java:144-149,2377-2419,3712-4065,5986-6175`

**Interfaces:**

- Produces: `WorkerDutyLoadoutPolicy.isUsableTool(WorkerProfession, ItemStack): boolean`.
- Produces: `GalacticRecruitEntity.prepareMinerToolForTargetScan(WorkerProfession): boolean`; `true` means the current `FIND_TARGET` tick was consumed by recovery or blocking.
- Produces: `GalacticRecruitEntity.firstCompatibleWorkerTool(Container, int, WorkerProfession): ItemStack` and `firstCompatibleStoredWorkerTool(BlockPos, WorkerProfession): ItemStack`; both return an exact defensive copy or `ItemStack.EMPTY`.
- Produces: private exact-transfer helpers `equipCompatibleWorkerToolFromCargo(WorkerProfession): boolean` and `equipCompatibleWorkerToolFromStorage(BlockPos, WorkerProfession): boolean`.
- Produces: GameTest `galacticwars:black_box_miner_broken_tool_recovery`.
- Consumes: `PhysicalLogisticsTransaction`, `LogisticsEndpoint`, `LogisticsInventory`, registered storage-slot authority, `RecruitLoadoutMenuProvider`, and the existing durable blocked-order retry.

- [ ] **Step 1: Register the isolated broken-tool GameTest**

Add a dedicated environment, SmartBrain stagger entry, 1,500-tick timeout, and test mapping beside the existing miner lifecycle entries:

```java
isolatedEnvironments.put(id("black_box_miner_broken_tool_recovery"),
        event.registerEnvironment(
                id("black_box_miner_broken_tool_recovery_environment"),
                new TestEnvironmentDefinition.AllOf(List.of())));

// Add to smartBrainRuntimeTests after black_box_miner_lifecycle.
id("black_box_miner_broken_tool_recovery"),

// Add to the timeout expression.
: testId.equals(id("black_box_miner_broken_tool_recovery"))
        ? 1_500

// Add to createTests().
tests.put(id("black_box_miner_broken_tool_recovery"),
        ModGameTests::blackBoxMinerBrokenToolRecovery);
```

Use `isolatedCapital(helper, 231)` so this scenario does not share Kingdom state with any current test.

- [ ] **Step 2: Add provider-owned loadout helpers used by both recovery tests**

Import `RecruitLoadoutMenu` and `RecruitLoadoutMenuProvider`, then add bounded helpers near the existing worker-lifecycle helpers:

```java
private static final int LOADOUT_WORKER_TOOL_SLOT = 1;

private static RecruitLoadoutMenu openServerRecruitLoadout(
        GameTestHelper helper,
        ServerPlayer owner,
        GalacticRecruitEntity recruit,
        int containerId
) {
    if (!recruit.canPlayerManageLogistics(owner)
            || owner.distanceToSqr(recruit) > 64.0D) {
        helper.fail("Recruit loadout authority or distance gate was not satisfied");
        return null;
    }
    return (RecruitLoadoutMenu) new RecruitLoadoutMenuProvider(recruit)
            .createMenu(containerId, owner.getInventory(), owner);
}

private static ItemStack quickMoveWorkerToolToPlayer(
        GameTestHelper helper,
        RecruitLoadoutMenu menu,
        ServerPlayer owner
) {
    ItemStack moved = menu.quickMoveStack(owner, LOADOUT_WORKER_TOOL_SLOT);
    if (moved.isEmpty() || !inventoryContainsExact(owner.getInventory(), moved)) {
        helper.fail("Provider-owned loadout did not move the physical worker tool to the player");
    }
    menu.removed(owner);
    return moved;
}

private static void quickMovePlayerToolToWorker(
        GameTestHelper helper,
        RecruitLoadoutMenu menu,
        ServerPlayer owner,
        GalacticRecruitEntity recruit,
        ItemStack expected
) {
    int sourceSlot = java.util.stream.IntStream.range(
                    RecruitLoadoutMenu.PLAYER_INVENTORY_START,
                    RecruitLoadoutMenu.PLAYER_INVENTORY_END)
            .filter(slot -> ItemStack.isSameItemSameComponents(
                    menu.getSlot(slot).getItem(), expected))
            .findFirst()
            .orElse(-1);
    if (sourceSlot < 0 || menu.quickMoveStack(owner, sourceSlot).isEmpty()
            || !ItemStack.isSameItemSameComponents(
                    recruit.getWorkerMainHandItem(), expected)) {
        helper.fail("Provider-owned loadout did not install the expected worker tool");
    }
    menu.removed(owner);
}

private static boolean inventoryContainsExact(Container inventory, ItemStack expected) {
    for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
        if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), expected)) {
            return true;
        }
    }
    return false;
}

private static void replaceExactPlayerStack(
        GameTestHelper helper,
        ServerPlayer owner,
        ItemStack previous,
        ItemStack replacement
) {
    Container inventory = owner.getInventory();
    for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
        if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), previous)) {
            inventory.setItem(slot, replacement.copy());
            inventory.setChanged();
            return;
        }
    }
    helper.fail("Physical worker tool was not present in player inventory for fixture damage");
}
```

These helpers must never call `setWorkerMainHandItem`; all equipment movement goes through `RecruitLoadoutMenu.quickMoveStack`.

- [ ] **Step 3: Write the broken-tool storage recovery scenario before production code**

Add `blackBoxMinerBrokenToolRecovery` beside `blackBoxMinerLifecycle`. Reuse the same public hire, assignment, worksite configuration, registered Command Center storage, chunk-readiness, and conservation helpers, but use a Survival owner, two in-bounds iron ores, and no initial replacement in storage.

The fixture sequence must be explicit:

```java
ItemStack original = quickMoveWorkerToolToPlayer(
        helper, openServerRecruitLoadout(helper, owner, recruit, 74), owner);
ItemStack nearlyBroken = original.copy();
nearlyBroken.setDamageValue(nearlyBroken.getMaxDamage() - 1);
replaceExactPlayerStack(helper, owner, original, nearlyBroken);
quickMovePlayerToolToWorker(
        helper,
        openServerRecruitLoadout(helper, owner, recruit, 75),
        owner,
        recruit,
        nearlyBroken);

ItemStack replacement = new ItemStack(Items.IRON_PICKAXE);
replacement.set(DataComponents.CUSTOM_NAME, Component.literal("Recovery Pickaxe"));
replacement.setDamageValue(7);
```

Drive the rest from `helper.onEachTick` and require these phase transitions:

```java
int[] phase = {0};
UUID[] blockedOrderId = {null};
boolean[] storageNavigationObserved = {false};
boolean[] complete = {false};

helper.onEachTick(() -> {
    if (complete[0]) {
        return;
    }
    int oreAndOutput = (level.getBlockState(firstOre).is(Blocks.IRON_ORE) ? 1 : 0)
            + (level.getBlockState(secondOre).is(Blocks.IRON_ORE) ? 1 : 0)
            + countContainerItem(hall, Items.RAW_IRON)
            + countContainerItem(recruitCargo, Items.RAW_IRON);
    if (oreAndOutput != 2) {
        complete[0] = true;
        helper.fail("Broken-tool recovery violated ore conservation: total=" + oreAndOutput
                + ", stored=" + countContainerItem(hall, Items.RAW_IRON)
                + ", cargo=" + countContainerItem(recruitCargo, Items.RAW_IRON));
        return;
    }

    long completedMineOrders = data.kingdomForOwner(owner.getUUID()).orElseThrow()
            .settlement().workOrders().stream()
            .filter(order -> order.type() == WorkOrderType.MINE)
            .filter(order -> order.state() == WorkOrderState.COMPLETED)
            .count();
    WorkerStatus status = recruit.getWorkerStatus();
    WorkOrder assigned = data.assignedWorkOrder(
            owner.getUUID(), recruit.getUUID()).orElse(null);

    if (phase[0] == 0 && completedMineOrders == 1
            && recruit.getWorkerMainHandItem().isEmpty()) {
        phase[0] = 1;
    }
    if (phase[0] == 1
            && status.phase() == WorkerPhase.BLOCKED
            && status.reasonCode().equals("missing_tool")
            && assigned != null
            && assigned.type() == WorkOrderType.MINE) {
        blockedOrderId[0] = assigned.id();
        putContainerItem(hall, replacement.copy());
        phase[0] = 2;
    }
    if (phase[0] != 2) {
        return;
    }

    storageNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
            && status.reasonCode().equals("withdraw_worker_tool");
    if (assigned != null && !assigned.id().equals(blockedOrderId[0])) {
        complete[0] = true;
        helper.fail("Broken-tool recovery replaced the blocked order: expected="
                + blockedOrderId[0] + ", actual=" + assigned.id());
        return;
    }
    int physicalReplacementCount = countContainerItem(hall, Items.IRON_PICKAXE)
            + countContainerItem(recruitCargo, Items.IRON_PICKAXE)
            + (recruit.getWorkerMainHandItem().is(Items.IRON_PICKAXE) ? 1 : 0);
    if (physicalReplacementCount != 1) {
        complete[0] = true;
        helper.fail("Broken-tool recovery duplicated or lost the replacement: count="
                + physicalReplacementCount);
        return;
    }
    ItemStack expectedUsedReplacement = replacement.copy();
    expectedUsedReplacement.setDamageValue(replacement.getDamageValue() + 1);
    if (completedMineOrders == 2
            && countContainerItem(hall, Items.RAW_IRON) == 2
            && workerInventoryCount(recruit) == 0
            && storageNavigationObserved[0]
            && ItemStack.isSameItemSameComponents(
                    recruit.getWorkerMainHandItem(), expectedUsedReplacement)) {
        complete[0] = true;
        helper.succeed();
    }
});
```

Count ore blocks plus raw-iron cargo/storage on every tick and require exactly two. Count the named replacement across registered storage, recruit cargo, and the worker-tool slot and require exactly one until the second mining action changes only its damage component. The scenario may insert the replacement into the registered Command Center after observing `missing_tool`; it must not mutate recruit position, phase, target, order, or controller state.

- [ ] **Step 4: Run the controlled RED**

Run:

```powershell
rtk .\gradlew.bat :neoforge:runGameTestServer --args="--tests galacticwars:black_box_miner_broken_tool_recovery" --no-daemon --console=plain
```

Expected: the test fails after observing `BLOCKED/missing_tool`; the named replacement remains in registered storage, the second ore remains unchanged, and no duplicate raw iron or pickaxe appears. Preserve the exact failing status and physical counts in the Task 18 evidence note.

- [ ] **Step 5: Add the runtime usability predicate**

Add this method without changing `isCompatible`:

```java
public static boolean isUsableTool(WorkerProfession profession, ItemStack stack) {
    return !stack.isEmpty() && isCompatible(profession, stack);
}
```

Keep empty-slot compatibility for profession assignment. Do not auto-replace a non-empty incompatible stack.

- [ ] **Step 6: Add the private worker-tool logistics endpoint**

Import `galacticwars.clonewars.workforce.logistics.LogisticsInventory`. In `GalacticRecruitEntity`, create a private endpoint method whose inventory adapter is live, one-slot, component-preserving, and bound to the recruit:

```java
private LogisticsEndpoint workerToolEndpoint(
        WorkerProfession profession,
        LogisticsAccessPolicy policy
) {
    LogisticsInventory inventory = new LogisticsInventory() {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemStack getStack(int slot) {
            return slot == 0 ? GalacticRecruitEntity.this.getWorkerMainHandItem() : ItemStack.EMPTY;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack) {
            return slot == 0
                    && GalacticRecruitEntity.this.getWorkerMainHandItem().isEmpty()
                    && WorkerDutyLoadoutPolicy.isUsableTool(profession, stack);
        }

        @Override
        public int maxStackSize(int slot, ItemStack stack) {
            return slot == 0 ? 1 : 0;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot != 0 || stack.getCount() > 1) {
                throw new IndexOutOfBoundsException("worker tool slot " + slot);
            }
            GalacticRecruitEntity.this.setWorkerMainHandItem(stack.copy());
        }

        @Override
        public void setChanged() {
            GalacticRecruitEntity.this.markLoadoutChanged();
        }

        @Override
        public Object transactionIdentity() {
            return GalacticRecruitEntity.this;
        }
    };
    return new LogisticsEndpoint(
            new LogisticsEndpointIdentity("recruit:" + this.getUUID() + ":worker_tool"),
            inventory,
            1,
            policy);
}
```

The implementation must reject insertion when the active slot is non-empty; overwriting is never an automatic recovery operation.

- [ ] **Step 7: Add exact cargo and registered-storage transfer helpers**

For cargo, scan slots from zero upward for the first `isUsableTool` stack, then transfer its exact copy from the existing cargo endpoint to `workerToolEndpoint` with an owner/alive/same-level policy and `REQUIRE_EXACT`.

For storage, scan only `min(registeredStorageSlots, containerSize)` live slots, retain the exact selected stack including components, and build the same registered-storage identity/policy used by `transferPhysicalQuantity`:

```java
PhysicalLogisticsTransaction.Result result = PhysicalLogisticsTransaction.transfer(
        sourceEndpoint,
        toolEndpoint,
        new LogisticsTransferAuthority(
                ownerId, sourceEndpoint.identity(), toolEndpoint.identity()),
        new LogisticsTransferRequest(
                selected.copyWithCount(1),
                1,
                LogisticsTransferRequest.Fulfillment.REQUIRE_EXACT));
return result.committed() && result.transferredQuantity() == 1;
```

Do not fall back to `ItemStack.shrink`, `setItem`, or a default stack if the transaction rejects stale state, policy, components, or capacity.

- [ ] **Step 8: Wire miner blocking, retry, and storage interaction**

When correct-tool ore rejects the active stack in `performGatheringInteraction`, record the default required item before blocking:

```java
if (profession == WorkerProfession.MINER
        && state.requiresCorrectToolForDrops()
        && !tool.isCorrectToolForDrops(state)) {
    this.workerRequiredItemId = BuiltInRegistries.ITEM
            .getKey(WorkerDutyLoadoutPolicy.defaultTool(profession).getItem())
            .toString();
    this.blockWorker("missing_tool");
    return;
}
```

Immediately after resolving `profession` in `tickFindTarget`, consume the tick when recovery blocks or starts navigation:

```java
if (profession == WorkerProfession.MINER
        && this.prepareMinerToolForTargetScan(profession)) {
    return;
}
```

Implement `prepareMinerToolForTargetScan` with this exact precedence:

```java
if (WorkerDutyLoadoutPolicy.isUsableTool(profession, this.getWorkerMainHandItem())) {
    return false;
}
this.workerRequiredItemId = BuiltInRegistries.ITEM
        .getKey(WorkerDutyLoadoutPolicy.defaultTool(profession).getItem())
        .toString();
if (!this.getWorkerMainHandItem().isEmpty()) {
    this.blockWorker("missing_tool");
    return true;
}
ItemStack cargoTool = this.firstCompatibleWorkerTool(
        this.createCargoContainer(), ArmyMemberSnapshot.CARGO_SLOT_COUNT, profession);
if (!cargoTool.isEmpty()) {
    if (this.equipCompatibleWorkerToolFromCargo(profession)) {
        this.workerRequiredItemId = "";
        return false;
    }
    this.blockWorker("worker_tool_transfer_failed");
    return true;
}
if (this.storageTarget != null
        && !this.firstCompatibleStoredWorkerTool(this.storageTarget, profession).isEmpty()) {
    this.transitionWorker(
            WorkerPhase.NAVIGATE_SOURCE,
            "withdraw_worker_tool",
            this.storageTarget);
    return true;
}
this.blockWorker("missing_tool");
return true;
```

Add an interaction branch before ordinary gathering:

```java
case "withdraw_worker_tool" -> {
    WorkerProfession profession = this.getWorkerProfession().orElse(null);
    if (profession == WorkerProfession.MINER
            && this.equipCompatibleWorkerToolFromStorage(
                    this.activeWorkTarget, profession)) {
        this.workerRequiredItemId = "";
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "worker_tool_ready", null);
    } else {
        this.blockWorker("worker_tool_transfer_failed");
    }
}
```

Do not release `workOrderId`; the current `tickAcquireOrder` revision-aware resume remains authoritative.

- [ ] **Step 9: Run GREEN twice and inspect exact evidence**

Run the focused command twice. Each run must report 1/1 passing and demonstrate the named replacement leaving storage, appearing in the live tool slot with preserved components, gaining exactly one damage point on the second ore, and completing the captured second order.

- [ ] **Step 10: Run focused compilation/harness safety checks**

Run:

```powershell
rtk .\gradlew.bat runHarnesses --no-daemon --console=plain
git diff --check
```

Expected: Gradle succeeds and the working diff check is empty.

- [ ] **Step 11: Commit the runtime and broken-tool proof**

```powershell
git add src/main/java/galacticwars/clonewars/workforce/WorkerDutyLoadoutPolicy.java src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java
git diff --cached --check
git commit -m "Add physical miner tool recovery"
```

---

### Task 2: Missing-tool loadout recovery proof

**Files:**

- Modify: `src/main/java/galacticwars/clonewars/gametest/ModGameTests.java:327-570,9037-9300`

**Interfaces:**

- Consumes: Task 1's provider-owned menu helpers and `WorkerDutyLoadoutPolicy.isUsableTool` runtime.
- Produces: GameTest `galacticwars:black_box_miner_missing_tool_recovery`.

- [ ] **Step 1: Register the isolated missing-tool GameTest**

Add the environment, SmartBrain stagger entry, 1,200-tick timeout, and `createTests` entry exactly as in Task 1 but with ID `black_box_miner_missing_tool_recovery`, method `blackBoxMinerMissingToolRecovery`, and `isolatedCapital(helper, 232)`.

- [ ] **Step 2: Write the ordinary manual recovery scenario**

Use a Survival owner, one iron ore, real hire and miner assignment, configured worksite, and registered storage with no pickaxe. After assignment, remove the default pickaxe through the provider-owned loadout and retain the exact physical stack in player inventory.

The scenario must record the active order at the first block and require unchanged physical state:

```java
if (status.phase() == WorkerPhase.BLOCKED
        && status.reasonCode().equals("missing_tool")) {
    WorkOrder order = data.assignedWorkOrder(
            owner.getUUID(), recruit.getUUID()).orElse(null);
    if (order == null || order.type() != WorkOrderType.MINE
            || !level.getBlockState(orePos).is(Blocks.IRON_ORE)
            || countContainerItem(hall, Items.RAW_IRON) != 0
            || workerInventoryCount(recruit) != 0) {
        helper.fail("Missing-tool block mutated ore, output, cargo, or order");
        return;
    }
    blockedOrderId = order.id();
    quickMovePlayerToolToWorker(
            helper,
            openServerRecruitLoadout(helper, owner, recruit, 77),
            owner,
            recruit,
            removedTool);
}
```

After reinsertion, require natural retry, physical re-approach, the same non-terminal order ID until completion, one raw iron deposited, empty cargo, exactly one tool damage point, and `WORK_AT_SITE` still active. Do not call `setWorkerMainHandItem`, `transitionWorker`, `WorkerRuntimeController.tick`, or `setPos` after profession/worksite assignment.

- [ ] **Step 3: Run the focused test twice**

Run twice:

```powershell
rtk .\gradlew.bat :neoforge:runGameTestServer --args="--tests galacticwars:black_box_miner_missing_tool_recovery" --no-daemon --console=plain
```

Expected: both runs pass 1/1. If the ordinary loadout retry exposes a runtime defect, preserve the failing evidence, apply only the smallest menu/blocked-order repair, and rerun the same command twice.

- [ ] **Step 4: Run both recovery tests together**

Run:

```powershell
rtk .\gradlew.bat :neoforge:runGameTestServer --args="--tests galacticwars:black_box_miner_missing_tool_recovery,galacticwars:black_box_miner_broken_tool_recovery" --no-daemon --console=plain
```

Expected: 2/2 pass with isolated Kingdom and physical inventories.

- [ ] **Step 5: Commit the manual recovery proof**

```powershell
git add src/main/java/galacticwars/clonewars/gametest/ModGameTests.java
git diff --cached --check
git commit -m "Prove manual miner tool recovery"
```

---

### Task 3: Provenance and player-facing acceptance status

**Files:**

- Modify: `README.md:85-95`
- Modify: `docs/authorized-source-intake.md:45`
- Modify: `docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md:277-end`

**Interfaces:**

- Consumes: the exact RED output and focused GREEN results from Tasks 1 and 2.
- Produces: truthful completion claims and Task 18 execution record.

- [ ] **Step 1: Update README without closing unrelated matrix items**

Extend the implemented-worker paragraph with the exact player-facing behavior:

```markdown
Miner tool recovery now fails closed before changing correct-tool ore, preserves the same persisted
order, accepts an authorized physical loadout replacement, or walks to registered storage for one
component-preserving pickaxe transfer before resuming naturally.
```

Change the remaining matrix line so it keeps every unproved boundary open and narrows only this item to `missing/broken tools for the remaining tool-using professions`.

- [ ] **Step 2: Expand the authorized source intake row**

Update the Workers miner row to name `MinerWorkGoal.java`, `GetNeededItemsFromStorage.java`, and `AbstractWorkerEntity.switchMainHandItem`. State that the Galactic rewrite uses a one-slot exact logistics endpoint, durable work orders, registered storage, SmartBrain navigation, and provider-owned loadout; explicitly exclude donor Forge goals, inventory code, and global work-area state.

List the evidence IDs `black_box_miner_lifecycle`, `black_box_miner_missing_tool_recovery`, and `black_box_miner_broken_tool_recovery`. Do not record aggregate counts until Task 4 has produced them.

- [ ] **Step 3: Append Task 18 to the continuation plan**

Add a checked Task 18 section containing:

- the controlled RED reason and exact physical counts;
- both focused 1/1 runs for each test;
- the production files changed and why;
- the no-`SupplyDemand` race boundary;
- the donor revisions; and
- pending aggregate/build/review gates as unchecked until Task 4 completes.

- [ ] **Step 4: Validate documentation and commit**

Run:

```powershell
git diff --check
rtk .\gradlew.bat runHarnesses --no-daemon --console=plain
```

Then commit:

```powershell
git add README.md docs/authorized-source-intake.md docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md
git diff --cached --check
git commit -m "Document miner tool recovery"
```

---

### Task 4: Full verification and focused review

**Files:**

- Modify only if evidence changes: `README.md`, `docs/authorized-source-intake.md`, `docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md`
- Modify only for a reproduced defect: Task 1 and Task 2 production/test files

**Interfaces:**

- Consumes: all Task 1-3 commits.
- Produces: fresh Java 25 Fabric/NeoForge build evidence, two 92/92 NeoForge GameTest runs, clean diffs, and a review-ready local branch.

- [ ] **Step 1: Run the dependency-light harness gate**

```powershell
rtk .\gradlew.bat runHarnesses --no-daemon --console=plain
```

Record the actual actionable/executed/up-to-date counts. Do not reuse the prior 176-task result without a fresh run.

- [ ] **Step 2: Verify and clear only the generated NeoForge GameTest world**

Resolve `neoforge\run\gametestserver` and verify that it is inside this worktree's `neoforge\run` directory before each aggregate run:

```powershell
Resolve-Path neoforge\run\gametestserver
Resolve-Path neoforge\run
Remove-Item -LiteralPath "C:\Users\kevin\.codex\worktrees\1f5d\GalacticWars\neoforge\run\gametestserver" -Recurse -Force
```

Do not remove `neoforge\run`, any workspace root, or any donor directory.

- [ ] **Step 3: Run the complete NeoForge GameTest suite twice on fresh worlds**

Run this command, repeat Step 2, then run it again:

```powershell
rtk .\gradlew.bat :neoforge:runGameTestServer --no-daemon --console=plain
```

Expected: both runs pass all 92 registered tests. Diagnose any failure by its current code and focused rerun; do not lengthen unrelated timeouts or weaken assertions to manufacture an aggregate pass.

- [ ] **Step 4: Run the cross-loader build gate**

```powershell
rtk .\gradlew.bat buildAll --no-daemon --console=plain
```

Expected: Fabric and NeoForge compile/package successfully on Java 25. Record actual Gradle task counts.

- [ ] **Step 5: Run working and staged diff gates**

```powershell
git diff --check
git status --short
git add README.md docs/authorized-source-intake.md docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java src/main/java/galacticwars/clonewars/workforce/WorkerDutyLoadoutPolicy.java
git diff --cached --check
```

Stage only files changed by this feature; preserve unrelated user changes if any appear.

- [ ] **Step 6: Update exact verification evidence**

Replace the pending Task 18 evidence with actual focused, harness, aggregate, and build outputs. README may claim the two new proofs only after both aggregate runs and `buildAll` succeed.

- [ ] **Step 7: Invoke focused code review**

Use `superpowers:requesting-code-review` with the diff from the PR #40 head through local HEAD. Require review of:

- item conservation and exact-component matching;
- stale-policy rollback;
- non-empty incompatible tool preservation;
- work-order identity/revision continuity;
- menu authority and physical item movement;
- no automatic demand or free tool creation; and
- test shortcuts, post-assignment teleports, or private phase/controller calls.

Use `superpowers:receiving-code-review` for every finding. Reproduce valid findings with a focused failing test before changing production behavior. Rerun the affected focused test twice and all four gates after any repair.

- [ ] **Step 8: Commit final evidence or review repairs**

If Task 4 changed tracked files:

```powershell
git add README.md docs/authorized-source-intake.md docs/superpowers/plans/2026-08-01-pr40-courier-recruit-continuation.md src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java src/main/java/galacticwars/clonewars/gametest/ModGameTests.java src/main/java/galacticwars/clonewars/workforce/WorkerDutyLoadoutPolicy.java
git diff --cached --check
git commit -m "Verify miner tool recovery"
```

Require `git status --short` to be empty after the commit.

---

### Task 5: Publish the stacked pull request into PR #40

**Files:**

- No repository files unless the live PR review exposes a valid issue.

**Interfaces:**

- Consumes: a clean, fully verified `codex/pr40-courier-recruit-continuation` branch.
- Produces: one ready stacked pull request with PR #40's current head branch as its base.

- [ ] **Step 1: Verify authentication, repository, branch, and PR #40 live state**

Run:

```powershell
gh auth status --hostname github.com
git remote -v
git branch --show-current
$taskPr40 = gh api repos/KB01111/GalacticWars/pulls/40 | ConvertFrom-Json
$taskPr40 | Select-Object state,@{Name='headRefName';Expression={$_.head.ref}},@{Name='headSha';Expression={$_.head.sha}},@{Name='baseRefName';Expression={$_.base.ref}},html_url
$taskPr40HeadSha = $taskPr40.head.sha
$taskPr40HeadRef = $taskPr40.head.ref
```

Require repository `KB01111/GalacticWars`, branch `codex/pr40-courier-recruit-continuation`, and PR #40 state `open`. Use the returned `headRefName` as the stacked PR base; do not rely on the previously observed branch name if live state differs.

- [ ] **Step 2: Confirm the continuation diff is based on PR #40**

```powershell
git merge-base --is-ancestor $taskPr40HeadSha HEAD
git log --oneline "$taskPr40HeadSha..HEAD"
git diff --stat "$taskPr40HeadSha..HEAD"
```

Expected: the ancestry check succeeds and the diff contains only the verified continuation commits. If PR #40 moved, rebase or merge only after inspecting conflicts and rerunning Task 4 in full.

- [ ] **Step 3: Check for an existing stacked PR before creating one**

```powershell
$taskExistingPr = gh pr list --repo KB01111/GalacticWars --head codex/pr40-courier-recruit-continuation --state all --json number,state,baseRefName,headRefName,url | ConvertFrom-Json
$taskExistingPr | Format-Table number,state,baseRefName,headRefName,url
```

If an open PR exists, update its base/body rather than creating a duplicate. If only a closed unmerged PR exists, create a new one only when GitHub will not reopen it.

- [ ] **Step 4: Push the verified continuation branch**

```powershell
git push -u origin codex/pr40-courier-recruit-continuation
```

Do not force-push. If the remote branch already exists with divergent commits, inspect the divergence and preserve remote work before proceeding.

- [ ] **Step 5: Create or update the ready stacked PR**

Create a ready PR with title `Continue PR #40 worker and recruit runtime port`, head `codex/pr40-courier-recruit-continuation`, and base equal to PR #40's live `headRefName`:

```powershell
$taskPrTitle = "Continue PR #40 worker and recruit runtime port"
$taskPrBody = "Stacks the verified courier, recruit, profession-lifecycle, and miner-tool-recovery continuation onto PR #40. Includes exact physical inventory conservation, durable worker orders, Minecraft 26.2 SmartBrain runtime proofs, donor provenance, two consecutive complete NeoForge GameTest passes, runHarnesses, and Fabric/NeoForge buildAll evidence. Merging this PR updates PR #40's head branch with the continuation work."
$taskOpenPr = $taskExistingPr | Where-Object { $_.state -eq 'OPEN' } | Select-Object -First 1
if ($null -eq $taskOpenPr) {
    gh pr create --repo KB01111/GalacticWars --base $taskPr40HeadRef --head codex/pr40-courier-recruit-continuation --title $taskPrTitle --body $taskPrBody
} else {
    gh pr edit $taskOpenPr.number --repo KB01111/GalacticWars --base $taskPr40HeadRef --title $taskPrTitle --body $taskPrBody
}
```

- [ ] **Step 6: Verify all publication SHAs and the stacked base**

```powershell
git rev-parse HEAD
git ls-remote origin refs/heads/codex/pr40-courier-recruit-continuation
$taskStackedPr = gh pr list --repo KB01111/GalacticWars --head codex/pr40-courier-recruit-continuation --state open --json number | ConvertFrom-Json | Select-Object -First 1
gh pr view $taskStackedPr.number --repo KB01111/GalacticWars --json headRefOid,headRefName,baseRefName,state,isDraft,url
gh api repos/KB01111/GalacticWars/pulls/40 --jq '{headRefName:.head.ref,headSha:.head.sha,state:.state}'
```

Require local HEAD, remote branch SHA, and stacked-PR `headRefOid` to match; require `isDraft=false`, `state=OPEN`, and `baseRefName` to equal PR #40's current `headRefName`.

- [ ] **Step 7: Report the final integration state**

Provide the stacked PR URL, local/remote/PR head SHA, PR #40 base branch and head SHA, all fresh verification commands/results, and any manual acceptance gates that remain outside automation. Do not claim the full port complete merely because this bounded tool-recovery slice and stacked PR are complete.
