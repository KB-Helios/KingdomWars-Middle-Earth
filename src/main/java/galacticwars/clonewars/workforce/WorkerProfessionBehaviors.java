package galacticwars.clonewars.workforce;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Stable registry of server-side profession handlers. */
public final class WorkerProfessionBehaviors {
    private static final Map<WorkerProfession, WorkerProfessionBehavior> BEHAVIORS =
            createBehaviors();

    private WorkerProfessionBehaviors() {
    }

    public static Optional<WorkerProfessionBehavior> behavior(
            WorkerProfession profession
    ) {
        return Optional.ofNullable(BEHAVIORS.get(profession));
    }

    private static Map<WorkerProfession, WorkerProfessionBehavior> createBehaviors() {
        EnumMap<WorkerProfession, WorkerProfessionBehavior> behaviors =
                new EnumMap<>(WorkerProfession.class);
        for (WorkerProfession profession : WorkerProfession.values()) {
            behaviors.put(profession, new BoundedWorkerProfessionBehavior(profession));
        }
        return Map.copyOf(behaviors);
    }
}
