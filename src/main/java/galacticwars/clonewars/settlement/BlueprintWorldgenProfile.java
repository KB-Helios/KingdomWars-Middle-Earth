package galacticwars.clonewars.settlement;

import galacticwars.clonewars.world.BlueprintSiteKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record BlueprintWorldgenProfile(
        List<String> biomes,
        String factionId,
        BlueprintSiteKind siteKind,
        int siteRadius,
        List<BlueprintRosterEntry> roster,
        Map<String, String> lootTables,
        int placementWeight
) {
    public BlueprintWorldgenProfile {
        biomes = normalizeList(biomes, "biome");
        factionId = normalize(factionId, "factionId");
        siteKind = Objects.requireNonNull(siteKind, "siteKind");
        roster = List.copyOf(Objects.requireNonNull(roster, "roster"));
        lootTables = normalizeLootTables(lootTables);
        if (biomes.isEmpty() || roster.isEmpty() || lootTables.isEmpty()
                || siteRadius < 8 || siteRadius > 128 || placementWeight <= 0) {
            throw new IllegalArgumentException("invalid worldgen profile for " + factionId);
        }
        int maximumResidents = roster.stream().mapToInt(BlueprintRosterEntry::maximum).sum();
        if (maximumResidents > 32) {
            throw new IllegalArgumentException("worldgen roster exceeds 32 residents for " + factionId);
        }
        long commanders = roster.stream()
                .filter(entry -> entry.explicitRole().orElse(null)
                        == galacticwars.clonewars.faction.ai.NpcRole.COMMANDER)
                .mapToInt(BlueprintRosterEntry::minimum)
                .sum();
        if (siteKind == BlueprintSiteKind.COMMAND_CENTER && commanders != 1L) {
            throw new IllegalArgumentException(
                    "commander center requires exactly one guaranteed commander for " + factionId);
        }
    }

    public BlueprintWorldgenProfile(
            List<String> biomes,
            String factionId,
            int siteRadius,
            List<BlueprintRosterEntry> roster,
            List<String> lootMarkers,
            int placementWeight
    ) {
        this(biomes, factionId, BlueprintSiteKind.OUTPOST, siteRadius, roster,
                legacyLootTables(factionId, lootMarkers), placementWeight);
    }

    public List<String> lootMarkers() {
        return List.copyOf(lootTables.keySet());
    }

    private static List<String> normalizeList(List<String> values, String label) {
        return Objects.requireNonNull(values, label).stream().map(value -> normalize(value, label)).distinct().toList();
    }

    private static Map<String, String> normalizeLootTables(Map<String, String> values) {
        Objects.requireNonNull(values, "lootTables");
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        values.forEach((marker, table) -> {
            String normalizedMarker = normalize(marker, "loot marker");
            String normalizedTable = normalize(table, "loot table");
            if (normalized.putIfAbsent(normalizedMarker, normalizedTable) != null) {
                throw new IllegalArgumentException("duplicate loot marker " + normalizedMarker);
            }
        });
        return java.util.Collections.unmodifiableMap(normalized);
    }

    private static Map<String, String> legacyLootTables(String factionId, List<String> markers) {
        String normalizedFaction = normalize(factionId, "factionId");
        String factionPath = normalizedFaction.substring(normalizedFaction.indexOf(':') + 1);
        LinkedHashMap<String, String> tables = new LinkedHashMap<>();
        normalizeList(markers, "loot marker").forEach(marker ->
                tables.put(marker, "galacticwars:chests/faction_site/" + factionPath));
        return tables;
    }

    private static String normalize(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        return normalized;
    }
}
