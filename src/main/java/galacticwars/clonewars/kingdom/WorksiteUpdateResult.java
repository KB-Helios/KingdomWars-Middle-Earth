package galacticwars.clonewars.kingdom;

import java.util.Objects;
import java.util.Optional;

/** Result returned by revision-checked, server-authoritative worksite mutations. */
public record WorksiteUpdateResult(
        boolean accepted,
        String reasonCode,
        Optional<WorksiteRecord> worksite
) {
    public WorksiteUpdateResult {
        reasonCode = Objects.requireNonNull(reasonCode, "reasonCode").trim();
        if (reasonCode.isEmpty()) {
            throw new IllegalArgumentException("reasonCode cannot be blank");
        }
        worksite = worksite == null ? Optional.empty() : worksite;
        if (accepted != worksite.isPresent()) {
            throw new IllegalArgumentException("accepted worksite updates require a result");
        }
    }

    public static WorksiteUpdateResult accepted(WorksiteRecord worksite) {
        return new WorksiteUpdateResult(true, "updated", Optional.of(worksite));
    }

    public static WorksiteUpdateResult rejected(String reasonCode) {
        return new WorksiteUpdateResult(false, reasonCode, Optional.empty());
    }
}
