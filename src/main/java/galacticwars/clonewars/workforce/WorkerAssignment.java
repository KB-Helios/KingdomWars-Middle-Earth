package galacticwars.clonewars.workforce;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record WorkerAssignment(
        UUID worksiteId,
        WorkerProfession profession,
        String dimensionId,
        int workX,
        int workY,
        int workZ,
        int radius,
        long configurationRevision,
        Optional<UUID> workOrderId
) {
    public WorkerAssignment {
        Objects.requireNonNull(worksiteId, "worksiteId");
        Objects.requireNonNull(profession, "profession");
        Objects.requireNonNull(dimensionId, "dimensionId");
        dimensionId = dimensionId.trim().toLowerCase();
        if (dimensionId.isEmpty()) {
            throw new IllegalArgumentException("dimensionId cannot be blank");
        }
        if (radius < 1 || radius > 32) {
            throw new IllegalArgumentException("radius must be between 1 and 32");
        }
        if (configurationRevision < 0L) {
            throw new IllegalArgumentException("configurationRevision cannot be negative");
        }
        workOrderId = workOrderId == null ? Optional.empty() : workOrderId;
    }
}
