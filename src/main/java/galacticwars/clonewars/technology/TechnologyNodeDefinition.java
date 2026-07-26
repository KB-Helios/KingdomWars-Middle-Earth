package galacticwars.clonewars.technology;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record TechnologyNodeDefinition(
        String id,
        String factionId,
        String displayName,
        Set<String> prerequisites,
        Map<String, Integer> requiredInputs,
        int requiredWork,
        Set<String> recipeIds
) {
    public static final int MAX_INPUT_TYPES = 16;
    public static final int MAX_INPUT_COUNT = 4_096;
    public static final int MAX_REQUIRED_WORK = 720_000;

    public TechnologyNodeDefinition {
        id = requireId(id, "id");
        factionId = requireId(factionId, "factionId");
        displayName = Objects.requireNonNull(displayName, "displayName").trim();
        prerequisites = Set.copyOf(Objects.requireNonNull(prerequisites, "prerequisites"));
        requiredInputs = Map.copyOf(new LinkedHashMap<>(
                Objects.requireNonNull(requiredInputs, "requiredInputs")));
        recipeIds = Set.copyOf(Objects.requireNonNull(recipeIds, "recipeIds"));
        if (displayName.isEmpty() || prerequisites.contains(id)
                || prerequisites.stream().anyMatch(String::isBlank)
                || recipeIds.stream().anyMatch(String::isBlank)
                || requiredInputs.isEmpty() || requiredInputs.size() > MAX_INPUT_TYPES
                || requiredInputs.entrySet().stream().anyMatch(entry -> entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue() < 1
                        || entry.getValue() > MAX_INPUT_COUNT)
                || requiredWork < 20 || requiredWork > MAX_REQUIRED_WORK) {
            throw new IllegalArgumentException("Invalid technology node " + id);
        }
    }

    public String definitionHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder canonical = new StringBuilder()
                    .append(id).append('\n')
                    .append(factionId).append('\n')
                    .append(displayName).append('\n')
                    .append(requiredWork).append('\n');
            prerequisites.stream().sorted()
                    .forEach(value -> canonical.append("prerequisite=").append(value).append('\n'));
            requiredInputs.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> canonical.append("input=")
                            .append(entry.getKey()).append('=').append(entry.getValue()).append('\n'));
            recipeIds.stream().sorted()
                    .forEach(value -> canonical.append("recipe=").append(value).append('\n'));
            digest.update(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String requireId(String value, String label) {
        if (value == null || value.isBlank() || !value.contains(":")) {
            throw new IllegalArgumentException(label + " must be a namespaced identifier");
        }
        return value.trim();
    }
}
