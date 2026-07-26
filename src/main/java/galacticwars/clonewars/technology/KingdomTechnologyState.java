package galacticwars.clonewars.technology;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record KingdomTechnologyState(
        UUID kingdomId,
        String factionId,
        List<String> completedNodes,
        Optional<KingdomResearchProject> activeProject,
        int revision,
        boolean legacyMigrationPending
) {
    public static final int MAX_COMPLETED_NODES = 256;

    public KingdomTechnologyState {
        Objects.requireNonNull(kingdomId, "kingdomId");
        if (factionId == null || factionId.isBlank()) {
            throw new IllegalArgumentException("factionId cannot be blank");
        }
        completedNodes = List.copyOf(new LinkedHashSet<>(
                Objects.requireNonNull(completedNodes, "completedNodes")));
        activeProject = Objects.requireNonNull(activeProject, "activeProject");
        if (completedNodes.size() > MAX_COMPLETED_NODES || completedNodes.stream().anyMatch(String::isBlank)
                || revision < 0) {
            throw new IllegalArgumentException("Invalid technology state for " + kingdomId);
        }
    }

    public static KingdomTechnologyState empty(UUID kingdomId, String factionId) {
        return new KingdomTechnologyState(
                kingdomId, factionId, List.of(), Optional.empty(), 0, false);
    }

    public boolean completed(String nodeId) {
        return completedNodes.contains(nodeId);
    }

    public KingdomTechnologyState withProject(KingdomResearchProject project) {
        return new KingdomTechnologyState(
                kingdomId, factionId, completedNodes, Optional.of(project), revision + 1,
                legacyMigrationPending);
    }

    public KingdomTechnologyState cancelProject() {
        return new KingdomTechnologyState(
                kingdomId, factionId, completedNodes, Optional.empty(), revision + 1,
                legacyMigrationPending);
    }

    public KingdomTechnologyState completeProject() {
        KingdomResearchProject project = activeProject.orElseThrow();
        ArrayList<String> completed = new ArrayList<>(completedNodes);
        if (!completed.contains(project.nodeId())) {
            completed.add(project.nodeId());
        }
        return new KingdomTechnologyState(
                kingdomId, factionId, completed, Optional.empty(), revision + 1,
                legacyMigrationPending);
    }

    public KingdomTechnologyState grantMigrationNodes(Iterable<String> nodeIds) {
        ArrayList<String> completed = new ArrayList<>(completedNodes);
        nodeIds.forEach(id -> {
            if (!completed.contains(id)) {
                completed.add(id);
            }
        });
        return completed.equals(completedNodes) && !legacyMigrationPending ? this : new KingdomTechnologyState(
                kingdomId, factionId, completed, activeProject, revision + 1, false);
    }

    public static KingdomTechnologyState legacyPending(UUID kingdomId, String factionId) {
        return new KingdomTechnologyState(
                kingdomId, factionId, List.of(), Optional.empty(), 0, true);
    }
}
