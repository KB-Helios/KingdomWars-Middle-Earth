package galacticwars.clonewars.technology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class TechnologyCatalog {
    public static final String UNIVERSAL = "galacticwars:universal";

    private final Map<String, TechnologyNodeDefinition> nodes;
    private final Map<String, String> nodeByRecipe;

    public TechnologyCatalog(Map<String, TechnologyNodeDefinition> definitions, Set<String> knownFactions) {
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(knownFactions, "knownFactions");
        LinkedHashMap<String, TechnologyNodeDefinition> normalized = new LinkedHashMap<>();
        for (var entry : definitions.entrySet()) {
            TechnologyNodeDefinition node = Objects.requireNonNull(entry.getValue(), "node");
            if (!entry.getKey().equals(node.id()) || normalized.putIfAbsent(node.id(), node) != null) {
                throw new IllegalArgumentException("Duplicate or mismatched technology node " + entry.getKey());
            }
            if (!node.factionId().equals(UNIVERSAL) && !knownFactions.contains(node.factionId())) {
                throw new IllegalArgumentException("Unknown technology faction " + node.factionId());
            }
        }
        validateGraph(normalized);
        LinkedHashMap<String, String> recipes = new LinkedHashMap<>();
        for (TechnologyNodeDefinition node : normalized.values()) {
            for (String recipeId : node.recipeIds()) {
                String previous = recipes.putIfAbsent(recipeId, node.id());
                if (previous != null) {
                    throw new IllegalArgumentException(
                            "Recipe " + recipeId + " is unlocked by both " + previous + " and " + node.id());
                }
            }
        }
        this.nodes = Collections.unmodifiableMap(normalized);
        this.nodeByRecipe = Collections.unmodifiableMap(recipes);
    }

    public static TechnologyCatalog empty() {
        return new TechnologyCatalog(Map.of(), Set.of());
    }

    public Map<String, TechnologyNodeDefinition> nodes() {
        return nodes;
    }

    public Optional<TechnologyNodeDefinition> node(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Optional<TechnologyNodeDefinition> nodeForRecipe(String recipeId) {
        String nodeId = nodeByRecipe.get(recipeId);
        return nodeId == null ? Optional.empty() : node(nodeId);
    }

    public List<TechnologyNodeDefinition> visibleToFaction(String factionId) {
        return nodes.values().stream()
                .filter(node -> node.factionId().equals(UNIVERSAL) || node.factionId().equals(factionId))
                .toList();
    }

    public Set<String> recipes() {
        return nodeByRecipe.keySet();
    }

    private static void validateGraph(Map<String, TechnologyNodeDefinition> nodes) {
        for (TechnologyNodeDefinition node : nodes.values()) {
            for (String prerequisiteId : node.prerequisites()) {
                TechnologyNodeDefinition prerequisite = nodes.get(prerequisiteId);
                if (prerequisite == null) {
                    throw new IllegalArgumentException(
                            "Technology " + node.id() + " has unknown prerequisite " + prerequisiteId);
                }
                if (!prerequisite.factionId().equals(UNIVERSAL)
                        && !prerequisite.factionId().equals(node.factionId())) {
                    throw new IllegalArgumentException(
                            "Technology " + node.id() + " crosses faction tree into " + prerequisiteId);
                }
            }
        }

        LinkedHashSet<String> visiting = new LinkedHashSet<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        for (String id : nodes.keySet()) {
            visit(id, nodes, visiting, visited);
        }

        Set<String> roots = nodes.values().stream()
                .filter(node -> node.prerequisites().isEmpty())
                .map(TechnologyNodeDefinition::id)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (!nodes.isEmpty() && roots.isEmpty()) {
            throw new IllegalArgumentException("Technology graph has no campaign-reachable root");
        }
        LinkedHashSet<String> reachable = new LinkedHashSet<>(roots);
        ArrayDeque<String> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            String completed = queue.removeFirst();
            for (TechnologyNodeDefinition candidate : nodes.values()) {
                if (!reachable.contains(candidate.id())
                        && candidate.prerequisites().stream().allMatch(reachable::contains)) {
                    reachable.add(candidate.id());
                    queue.addLast(candidate.id());
                }
            }
        }
        if (reachable.size() != nodes.size()) {
            ArrayList<String> unreachable = new ArrayList<>(nodes.keySet());
            unreachable.removeAll(reachable);
            throw new IllegalArgumentException("Campaign-unreachable technology: " + unreachable);
        }
    }

    private static void visit(
            String id,
            Map<String, TechnologyNodeDefinition> nodes,
            Set<String> visiting,
            Set<String> visited
    ) {
        if (visited.contains(id)) {
            return;
        }
        if (!visiting.add(id)) {
            throw new IllegalArgumentException("Technology cycle includes " + id);
        }
        for (String prerequisite : nodes.get(id).prerequisites()) {
            visit(prerequisite, nodes, visiting, visited);
        }
        visiting.remove(id);
        visited.add(id);
    }
}
