package galacticwars.clonewars.workforce;

/** Stable public contract for profession-specific runtime handlers. */
public interface WorkerProfessionBehavior {
    WorkerProfession profession();

    WorkerAction plan(WorkerRuntimeContext context);

    WorkerActionResult execute(WorkerRuntimeContext context, WorkerAction action);

    default WorkerExecutionState cancel(WorkerRuntimeContext context, String reasonCode) {
        return context.executionState().transition(
                WorkerPhase.BLOCKED,
                reasonCode,
                java.util.Optional.empty());
    }

    default boolean isEnabled() {
        return WorkerProfessionCatalog.isEnabled(this.profession());
    }
}
