package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.WorksiteRecord;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WorksiteOverlaySelector {
    private WorksiteOverlaySelector() {
    }

    public static List<WorksiteRecord> nearestVisible(
            Collection<WorksiteRecord> candidates,
            double playerX,
            double playerY,
            double playerZ,
            int limit
    ) {
        Objects.requireNonNull(candidates, "candidates");
        if (limit <= 0 || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .sorted(Comparator
                        .comparingDouble((WorksiteRecord worksite) -> distanceSquared(
                                worksite, playerX, playerY, playerZ))
                        .thenComparing(WorksiteRecord::id))
                .limit(limit)
                .toList();
    }

    private static double distanceSquared(
            WorksiteRecord worksite,
            double playerX,
            double playerY,
            double playerZ
    ) {
        double x = worksite.x() + 0.5D - playerX;
        double y = worksite.y() + 0.5D - playerY;
        double z = worksite.z() + 0.5D - playerZ;
        return x * x + y * y + z * z;
    }
}
