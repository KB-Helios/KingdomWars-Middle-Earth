package galacticwars.clonewars.workforce;

/**
 * Narrow server-runtime boundary implemented by the loaded recruit entity.
 * Profession and phase orchestration must use this surface instead of owning
 * navigation or SavedData directly.
 */
public interface WorkerRuntimeHost {
    boolean workerRuntimeAvailable();

    /**
     * Reconciles the resumable entity cursor with durable SavedData authority.
     *
     * @return {@code true} when the current tick may continue, or {@code false}
     *         when reconciliation deliberately restarted/blocked execution
     */
    boolean reconcileWorkerRuntimeAuthority();

    boolean workerRuntimeInterrupted();

    void interruptWorkerRuntime();

    boolean tickSpecializedWorkerRuntime();

    boolean throttleWorkerTargetScan();

    void ageWorkerTargetBlacklist();

    WorkerPhase workerRuntimePhase();

    void acquireWorkerOrder();

    void findWorkerTarget();

    void navigateWorkerToInteraction();

    void interactWithWorkerTarget();

    void finishWorkerCollection();

    void navigateWorkerToDeposit();

    void depositWorkerInventory();

    void tickWorkerDelay();

    void maintainPausedWorker();
}
