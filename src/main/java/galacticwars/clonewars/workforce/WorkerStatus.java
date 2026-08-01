package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record WorkerStatus(
        WorkerPhase phase,
        String reasonCode,
        Optional<WorkerTarget> target,
        Optional<UUID> worksiteId,
        Optional<UUID> workOrderId,
        int completedQuantity,
        int totalQuantity,
        String requiredResource
) {
    public WorkerStatus {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(reasonCode, "reasonCode");
        reasonCode = reasonCode.trim();
        if (reasonCode.isEmpty()) {
            throw new IllegalArgumentException("reasonCode cannot be blank");
        }
        target = target == null ? Optional.empty() : target;
        worksiteId = worksiteId == null ? Optional.empty() : worksiteId;
        workOrderId = workOrderId == null ? Optional.empty() : workOrderId;
        if (completedQuantity < 0 || totalQuantity < 0 || completedQuantity > totalQuantity) {
            throw new IllegalArgumentException("invalid worker progress");
        }
        requiredResource = requiredResource == null ? "" : requiredResource.trim().toLowerCase(Locale.ROOT);
    }

    public WorkerStatus(WorkerPhase phase, String reasonCode, Optional<WorkerTarget> target) {
        this(phase, reasonCode, target, Optional.empty(), Optional.empty(), 0, 0, "");
    }
}
