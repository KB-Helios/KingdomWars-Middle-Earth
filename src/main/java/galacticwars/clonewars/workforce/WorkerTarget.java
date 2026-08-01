package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Objects;

/** Loader-independent block target used by worker status and persisted execution. */
public record WorkerTarget(String dimensionId, int x, int y, int z) {
    public WorkerTarget {
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId")
                .trim()
                .toLowerCase(Locale.ROOT);
        if (dimensionId.isEmpty()) {
            throw new IllegalArgumentException("dimensionId cannot be blank");
        }
    }
}
