package galacticwars.clonewars.kingdom;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.WorkerProfessionCatalog;
import galacticwars.clonewars.workforce.WorkAreaConfiguration;
import galacticwars.clonewars.workforce.WorkAreaBounds;

public record WorksiteRecord(
        UUID id,
        String type,
        String dimensionId,
        int x,
        int y,
        int z,
        int radius,
        int capacity,
        List<WorkerProfession> acceptedProfessions,
        Optional<UUID> sourceProjectId,
        List<UUID> assignmentIds,
        List<StorageEndpoint> storageEndpoints,
        WorkAreaConfiguration configuration
) {
    public WorksiteRecord {
        Objects.requireNonNull(id, "id");
        type = KingdomNormalizers.normalize(type, "type");
        dimensionId = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        if (radius < 1 || radius > 32) {
            throw new IllegalArgumentException("radius must be between 1 and 32");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        acceptedProfessions = List.copyOf(new LinkedHashSet<>(
                Objects.requireNonNull(acceptedProfessions, "acceptedProfessions")));
        if (acceptedProfessions.isEmpty()) {
            acceptedProfessions = type.equals("frontier")
                    ? WorkerProfessionCatalog.enabledProfessions().stream()
                            .map(definition -> definition.profession()).toList()
                    : WorkerProfession.byId(type).map(List::of).orElse(List.of());
        }
        sourceProjectId = sourceProjectId == null ? Optional.empty() : sourceProjectId;
        assignmentIds = List.copyOf(new LinkedHashSet<>(Objects.requireNonNull(assignmentIds, "assignmentIds")));
        if (assignmentIds.size() > capacity) {
            throw new IllegalArgumentException("worksite assignments exceed capacity");
        }
        storageEndpoints = List.copyOf(Objects.requireNonNull(storageEndpoints, "storageEndpoints"));
        configuration = configuration == null ? WorkAreaConfiguration.defaults(radius) : configuration;
    }

    public WorksiteRecord(
            UUID id,
            String type,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius,
            int capacity,
            List<WorkerProfession> acceptedProfessions,
            Optional<UUID> sourceProjectId,
            List<UUID> assignmentIds,
            List<StorageEndpoint> storageEndpoints
    ) {
        this(id, type, dimensionId, x, y, z, radius, capacity, acceptedProfessions, sourceProjectId,
                assignmentIds, storageEndpoints, WorkAreaConfiguration.defaults(radius));
    }

    public WorksiteRecord(UUID id, String type, String dimensionId, int x, int y, int z, int radius, int capacity) {
        this(id, type, dimensionId, x, y, z, radius, capacity,
                WorkerProfession.byId(type).map(List::of).orElse(List.of()), Optional.empty(), List.of(), List.of());
    }

    public boolean accepts(WorkerProfession profession) {
        return acceptedProfessions.contains(profession);
    }

    public boolean hasCapacity() {
        return assignmentIds.size() < capacity;
    }

    public WorksiteRecord assign(UUID recruitId) {
        if (assignmentIds.contains(recruitId)) {
            return this;
        }
        if (!hasCapacity()) {
            throw new IllegalStateException("worksite is full");
        }
        java.util.ArrayList<UUID> assignments = new java.util.ArrayList<>(assignmentIds);
        assignments.add(recruitId);
        return copy(List.copyOf(assignments), storageEndpoints);
    }

    public WorksiteRecord release(UUID recruitId) {
        List<UUID> assignments = assignmentIds.stream().filter(id -> !id.equals(recruitId)).toList();
        return assignments.size() == assignmentIds.size() ? this : copy(assignments, storageEndpoints);
    }

    private WorksiteRecord copy(List<UUID> assignments, List<StorageEndpoint> endpoints) {
        return new WorksiteRecord(id, type, dimensionId, x, y, z, radius, capacity,
                acceptedProfessions, sourceProjectId, assignments, endpoints, configuration);
    }

    public WorksiteRecord withLocation(String dimensionId, int x, int y, int z) {
        return withLocationAndRadius(dimensionId, x, y, z, radius);
    }

    public WorksiteRecord withLocationAndRadius(String dimensionId, int x, int y, int z, int radius) {
        return new WorksiteRecord(id, type, dimensionId, x, y, z, radius, capacity,
                acceptedProfessions, sourceProjectId, assignmentIds, storageEndpoints,
                configuration.withBounds(WorkAreaBounds.radius(radius)));
    }

    static WorksiteRecord fromPersistence(
            UUID id,
            String type,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius,
            int capacity,
            List<WorkerProfession> acceptedProfessions,
            Optional<UUID> sourceProjectId,
            List<UUID> assignmentIds,
            List<StorageEndpoint> storageEndpoints,
            Optional<WorkAreaConfiguration> configuration
    ) {
        return new WorksiteRecord(id, type, dimensionId, x, y, z, radius, capacity,
                acceptedProfessions, sourceProjectId, assignmentIds, storageEndpoints,
                configuration.orElseGet(() -> WorkAreaConfiguration.defaults(radius)));
    }

    public WorksiteRecord configured(WorkAreaConfiguration configuration) {
        return new WorksiteRecord(id, type, dimensionId, x, y, z, radius, capacity,
                acceptedProfessions, sourceProjectId, assignmentIds, storageEndpoints, configuration);
    }

    public WorksiteRecord withPrimaryStorageEndpoint(StorageEndpoint endpoint) {
        Objects.requireNonNull(endpoint, "endpoint");
        if (!dimensionId.equals(endpoint.dimensionId())) {
            return this;
        }
        java.util.ArrayList<StorageEndpoint> endpoints =
                new java.util.ArrayList<>(storageEndpoints.size() + 1);
        endpoints.add(endpoint);
        storageEndpoints.stream()
                .filter(existing -> !samePosition(existing, endpoint))
                .forEach(endpoints::add);
        if (storageEndpoints.equals(endpoints)) {
            return this;
        }
        return new WorksiteRecord(
                id,
                type,
                dimensionId,
                x,
                y,
                z,
                radius,
                capacity,
                acceptedProfessions,
                sourceProjectId,
                assignmentIds,
                List.copyOf(endpoints),
                configuration.nextRevision());
    }

    private static boolean samePosition(StorageEndpoint first, StorageEndpoint second) {
        return first.dimensionId().equals(second.dimensionId())
                && first.x() == second.x()
                && first.y() == second.y()
                && first.z() == second.z();
    }
}
