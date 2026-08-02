package galacticwars.clonewars.workforce;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Resumable execution cursor for one loaded worker. Durable job authority still
 * belongs to the settlement work order and supply ledgers.
 */
public record WorkerExecutionState(
        Optional<UUID> worksiteId,
        Optional<UUID> workOrderId,
        WorkerPhase phase,
        Optional<WorkerTarget> target,
        long configurationRevision,
        int retryCount,
        long retryAtGameTime,
        Optional<UUID> supplyReservationId,
        String reasonCode
) {
    public WorkerExecutionState {
        worksiteId = worksiteId == null ? Optional.empty() : worksiteId;
        workOrderId = workOrderId == null ? Optional.empty() : workOrderId;
        Objects.requireNonNull(phase, "phase");
        target = target == null ? Optional.empty() : target;
        supplyReservationId = supplyReservationId == null
                ? Optional.empty()
                : supplyReservationId;
        if (configurationRevision < 0L || retryCount < 0 || retryAtGameTime < 0L) {
            throw new IllegalArgumentException("worker execution counters cannot be negative");
        }
        reasonCode = Objects.requireNonNull(reasonCode, "reasonCode").trim();
        if (reasonCode.isEmpty()) {
            throw new IllegalArgumentException("reasonCode cannot be blank");
        }
    }

    public static WorkerExecutionState initial() {
        return new WorkerExecutionState(
                Optional.empty(),
                Optional.empty(),
                WorkerPhase.ACQUIRE_ORDER,
                Optional.empty(),
                0L,
                0,
                0L,
                Optional.empty(),
                "ready");
    }

    public WorkerExecutionState withAuthority(
            UUID nextWorksiteId,
            UUID nextWorkOrderId,
            long nextConfigurationRevision
    ) {
        return new WorkerExecutionState(
                Optional.of(nextWorksiteId),
                Optional.of(nextWorkOrderId),
                phase,
                target,
                nextConfigurationRevision,
                retryCount,
                retryAtGameTime,
                supplyReservationId,
                reasonCode);
    }

    public WorkerExecutionState transition(
            WorkerPhase nextPhase,
            String nextReason,
            Optional<WorkerTarget> nextTarget
    ) {
        return new WorkerExecutionState(
                worksiteId,
                workOrderId,
                nextPhase,
                nextTarget,
                configurationRevision,
                nextPhase == WorkerPhase.BLOCKED ? retryCount : 0,
                nextPhase == WorkerPhase.BLOCKED ? retryAtGameTime : 0L,
                supplyReservationId,
                nextReason);
    }

    public WorkerExecutionState retryAt(long gameTime, String reason) {
        return new WorkerExecutionState(
                worksiteId,
                workOrderId,
                WorkerPhase.BLOCKED,
                target,
                configurationRevision,
                Math.addExact(retryCount, 1),
                gameTime,
                supplyReservationId,
                reason);
    }

    public WorkerExecutionState withSupplyReservation(Optional<UUID> reservationId) {
        return new WorkerExecutionState(
                worksiteId,
                workOrderId,
                phase,
                target,
                configurationRevision,
                retryCount,
                retryAtGameTime,
                reservationId,
                reasonCode);
    }
}
