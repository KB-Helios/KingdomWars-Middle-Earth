package galacticwars.clonewars.workforce;

/** Stable public contract for profession-specific runtime handlers. */
public interface WorkerProfessionBehavior {
    WorkerProfession profession();

    WorkerAction plan(WorkerRuntimeContext context);

    WorkerActionResult execute(WorkerRuntimeContext context, WorkerAction action);

    default WorkerExecutionState cancel(WorkerRuntimeContext context, String reasonCode) {
        context.executionState().supplyReservation().ifPresent(reservation ->
                context.data().releaseSupply(
                        context.kingdom().ownerId(),
                        context.settlement().id(),
                        reservation.id(),
                        context.recruit().getUUID()));
        return context.executionState()
                .withSupplyReservation(java.util.Optional.empty())
                .transition(
                        WorkerPhase.BLOCKED,
                        reasonCode,
                        java.util.Optional.empty());
    }

    default boolean isEnabled() {
        return WorkerProfessionCatalog.isEnabled(this.profession());
    }
}
