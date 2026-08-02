package galacticwars.clonewars.workforce;

import java.util.Objects;
import java.util.Optional;

/** Result of one bounded worker action without duplicating physical inventory. */
public record WorkerActionResult(
        WorkerExecutionState executionState,
        Optional<WorkerSupplyRequest> supplyRequest,
        int progress
) {
    public WorkerActionResult {
        Objects.requireNonNull(executionState, "executionState");
        supplyRequest = supplyRequest == null ? Optional.empty() : supplyRequest;
        if (progress < 0) {
            throw new IllegalArgumentException("progress cannot be negative");
        }
    }

    public static WorkerActionResult unchanged(WorkerExecutionState state) {
        return new WorkerActionResult(state, Optional.empty(), 0);
    }
}
