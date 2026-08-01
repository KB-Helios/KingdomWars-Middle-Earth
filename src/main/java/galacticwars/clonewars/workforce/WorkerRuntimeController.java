package galacticwars.clonewars.workforce;

import java.util.Objects;

/** Server-side owner of the common worker phase machine. */
public final class WorkerRuntimeController {
    private WorkerRuntimeController() {
    }

    public static void tick(WorkerRuntimeHost host) {
        Objects.requireNonNull(host, "host");
        if (!host.workerRuntimeAvailable()) {
            return;
        }
        if (!host.reconcileWorkerRuntimeAuthority()) {
            return;
        }
        if (host.workerRuntimeInterrupted()) {
            host.interruptWorkerRuntime();
            return;
        }
        if (host.tickSpecializedWorkerRuntime()) {
            return;
        }
        if (host.workerRuntimePhase() == WorkerPhase.FIND_TARGET
                && host.throttleWorkerTargetScan()) {
            return;
        }
        host.ageWorkerTargetBlacklist();

        switch (host.workerRuntimePhase()) {
            case ACQUIRE_ORDER -> host.acquireWorkerOrder();
            case FIND_TARGET -> host.findWorkerTarget();
            case NAVIGATE_SOURCE -> host.navigateWorkerToInteraction();
            case INTERACT -> host.interactWithWorkerTarget();
            case COLLECT -> host.finishWorkerCollection();
            case NAVIGATE_STORAGE -> host.navigateWorkerToDeposit();
            case DEPOSIT -> host.depositWorkerInventory();
            case COOLDOWN, BLOCKED -> host.tickWorkerDelay();
            case PAUSED -> host.maintainPausedWorker();
        }
    }
}
