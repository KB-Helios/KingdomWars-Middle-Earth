package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.WorkOrder;
import galacticwars.clonewars.kingdom.WorksiteRecord;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Immutable authority snapshot supplied to one profession planning step. */
public record WorkerRuntimeContext(
        UUID workerId,
        WorkerProfession profession,
        WorksiteRecord worksite,
        Optional<WorkOrder> workOrder,
        WorkerExecutionState executionState,
        WorkerWorldActions worldActions,
        long gameTime
) {
    public WorkerRuntimeContext {
        Objects.requireNonNull(workerId, "workerId");
        Objects.requireNonNull(profession, "profession");
        Objects.requireNonNull(worksite, "worksite");
        workOrder = workOrder == null ? Optional.empty() : workOrder;
        Objects.requireNonNull(executionState, "executionState");
        Objects.requireNonNull(worldActions, "worldActions");
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
    }
}
