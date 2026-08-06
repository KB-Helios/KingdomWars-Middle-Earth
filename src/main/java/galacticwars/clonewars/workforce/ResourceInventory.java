package galacticwars.clonewars.workforce;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ResourceInventory(Map<String, Integer> resources) {
    public ResourceInventory {
        Objects.requireNonNull(resources, "resources");
        LinkedHashMap<String, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            String itemId = normalizeItemId(entry.getKey());
            int count = Objects.requireNonNull(entry.getValue(), "count");
            if (count > 0) {
                normalized.merge(itemId, count, Integer::sum);
            }
        }
        resources = Collections.unmodifiableMap(normalized);
    }

    public static ResourceInventory empty() {
        return new ResourceInventory(Map.of());
    }

    public static ResourceInventory of(String itemId, int count) {
        return new ResourceInventory(Map.of(normalizeItemId(itemId), count));
    }

    public int count(String itemId) {
        return resources.getOrDefault(normalizeItemId(itemId), 0);
    }

    public boolean hasAtLeast(String itemId, int count) {
        if (count <= 0) {
            return true;
        }
        return count(itemId) >= count;
    }

    public int totalCount() {
        int total = 0;
        for (int count : resources.values()) {
            total += count;
        }
        return total;
    }

    public Optional<Map.Entry<String, Integer>> firstResource() {
        return resources.entrySet().stream().findFirst();
    }

    public ResourceInventory withAdded(String itemId, int count) {
        LinkedHashMap<String, Integer> updated = new LinkedHashMap<>(resources);
        if (count > 0) {
            updated.merge(normalizeItemId(itemId), count, Integer::sum);
        }
        return new ResourceInventory(updated);
    }

    public ResourceInventory withRemoved(String itemId, int count) {
        if (count <= 0) {
            return this;
        }
        String normalized = normalizeItemId(itemId);
        LinkedHashMap<String, Integer> updated = new LinkedHashMap<>(resources);
        int remaining = Math.max(0, updated.getOrDefault(normalized, 0) - count);
        if (remaining == 0) {
            updated.remove(normalized);
        } else {
            updated.put(normalized, remaining);
        }
        return new ResourceInventory(updated);
    }

    private static String normalizeItemId(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        String normalized = itemId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("itemId cannot be blank");
        }
        return normalized;
    }
}
