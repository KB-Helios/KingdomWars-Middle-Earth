package galacticwars.clonewars.technology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record KingdomResearchProject(
        String nodeId,
        String definitionHash,
        Map<String, Integer> requiredInputs,
        Map<String, Integer> deliveredInputs,
        Optional<UUID> technicianId,
        int workProgress,
        int revision,
        List<UUID> replayIds
) {
    public static final int MAX_REPLAY_IDS = 128;

    public KingdomResearchProject {
        nodeId = requireText(nodeId, "nodeId");
        definitionHash = requireText(definitionHash, "definitionHash");
        requiredInputs = boundedCounts(requiredInputs, "requiredInputs");
        deliveredInputs = boundedCounts(deliveredInputs, "deliveredInputs");
        Map<String, Integer> required = requiredInputs;
        technicianId = Objects.requireNonNull(technicianId, "technicianId");
        replayIds = List.copyOf(new LinkedHashSet<>(Objects.requireNonNull(replayIds, "replayIds")));
        if (requiredInputs.isEmpty() || deliveredInputs.keySet().stream()
                .anyMatch(key -> !required.containsKey(key))
                || deliveredInputs.entrySet().stream()
                .anyMatch(entry -> entry.getValue() > required.get(entry.getKey()))
                || workProgress < 0 || workProgress > TechnologyNodeDefinition.MAX_REQUIRED_WORK
                || revision < 0 || replayIds.size() > MAX_REPLAY_IDS) {
            throw new IllegalArgumentException("Invalid research project " + nodeId);
        }
    }

    public static KingdomResearchProject start(TechnologyNodeDefinition node, UUID replayId) {
        return new KingdomResearchProject(
                node.id(), node.definitionHash(), node.requiredInputs(), Map.of(), Optional.empty(),
                0, 0, List.of(replayId));
    }

    public boolean hasReplay(UUID replayId) {
        return replayIds.contains(replayId);
    }

    public boolean materialsComplete() {
        return requiredInputs.entrySet().stream()
                .allMatch(entry -> deliveredInputs.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    public KingdomResearchProject withContribution(Map<String, Integer> contribution, UUID replayId) {
        LinkedHashMap<String, Integer> delivered = new LinkedHashMap<>(deliveredInputs);
        contribution.forEach((item, count) -> delivered.merge(item, count, Integer::sum));
        return update(delivered, technicianId, workProgress, replayId);
    }

    public KingdomResearchProject withTechnician(UUID technicianId, UUID replayId) {
        return update(deliveredInputs, Optional.of(technicianId), workProgress, replayId);
    }

    public KingdomResearchProject withWork(int work, UUID replayId) {
        return update(deliveredInputs, technicianId,
                Math.min(TechnologyNodeDefinition.MAX_REQUIRED_WORK, workProgress + Math.max(0, work)),
                replayId);
    }

    public KingdomResearchProject withWork(int work) {
        return new KingdomResearchProject(
                nodeId, definitionHash, requiredInputs, deliveredInputs, technicianId,
                Math.min(TechnologyNodeDefinition.MAX_REQUIRED_WORK,
                        workProgress + Math.max(0, work)),
                revision + 1, replayIds);
    }

    private KingdomResearchProject update(
            Map<String, Integer> delivered,
            Optional<UUID> technician,
            int work,
            UUID replayId
    ) {
        ArrayList<UUID> replays = new ArrayList<>(replayIds);
        replays.add(replayId);
        while (replays.size() > MAX_REPLAY_IDS) {
            replays.removeFirst();
        }
        return new KingdomResearchProject(
                nodeId, definitionHash, requiredInputs, delivered, technician, work,
                revision + 1, replays);
    }

    private static Map<String, Integer> boundedCounts(Map<String, Integer> source, String label) {
        Objects.requireNonNull(source, label);
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key == null || key.isBlank() || value == null || value < 1
                    || value > TechnologyNodeDefinition.MAX_INPUT_COUNT
                    || result.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Invalid " + label);
            }
        });
        if (result.size() > TechnologyNodeDefinition.MAX_INPUT_TYPES) {
            throw new IllegalArgumentException(label + " exceeds input bound");
        }
        return Map.copyOf(result);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        return value;
    }
}
