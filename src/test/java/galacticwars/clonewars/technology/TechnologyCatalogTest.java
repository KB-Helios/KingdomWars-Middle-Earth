package galacticwars.clonewars.technology;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TechnologyCatalogTest {
    private static final Set<String> FACTIONS = Set.of(
            "galacticwars:republic",
            "galacticwars:separatist");

    private TechnologyCatalogTest() {
    }

    public static void main(String[] args) {
        acceptsFactionIsolatedReachableGraph();
        rejectsMissingReferencesAndCycles();
        rejectsCrossFactionPrerequisitesAndUnknownFactions();
        rejectsDuplicateRecipeAssignmentsAndMismatchedIds();
        rejectsInvalidCostsAndWorkBounds();
        definitionHashIsCanonical();
        System.out.println("TechnologyCatalogTest passed");
    }

    private static void acceptsFactionIsolatedReachableGraph() {
        TechnologyNodeDefinition universal = node(
                "galacticwars:field_fabrication",
                TechnologyCatalog.UNIVERSAL,
                Set.of(),
                Set.of());
        TechnologyNodeDefinition republic = node(
                "galacticwars:clone_field_arms",
                "galacticwars:republic",
                Set.of(universal.id()),
                Set.of("galacticwars:dc15_blaster"));
        TechnologyNodeDefinition separatist = node(
                "galacticwars:droid_field_arms",
                "galacticwars:separatist",
                Set.of(universal.id()),
                Set.of("galacticwars:e5_blaster"));

        TechnologyCatalog catalog = new TechnologyCatalog(
                ordered(universal, republic, separatist),
                FACTIONS);

        assertEquals(3, catalog.nodes().size(), "node count");
        assertEquals(republic.id(), catalog.nodeForRecipe("galacticwars:dc15_blaster")
                .orElseThrow().id(), "recipe owner");
        assertEquals(Set.of(universal.id(), republic.id()),
                catalog.visibleToFaction("galacticwars:republic").stream()
                        .map(TechnologyNodeDefinition::id)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()),
                "Republic tree isolation");
    }

    private static void rejectsMissingReferencesAndCycles() {
        TechnologyNodeDefinition missing = node(
                "galacticwars:missing_child",
                "galacticwars:republic",
                Set.of("galacticwars:not_present"),
                Set.of());
        assertThrows(() -> new TechnologyCatalog(ordered(missing), FACTIONS),
                "unknown prerequisite");

        TechnologyNodeDefinition first = node(
                "galacticwars:first",
                "galacticwars:republic",
                Set.of("galacticwars:second"),
                Set.of());
        TechnologyNodeDefinition second = node(
                "galacticwars:second",
                "galacticwars:republic",
                Set.of(first.id()),
                Set.of());
        assertThrows(() -> new TechnologyCatalog(ordered(first, second), FACTIONS),
                "cycle");
    }

    private static void rejectsCrossFactionPrerequisitesAndUnknownFactions() {
        TechnologyNodeDefinition republic = node(
                "galacticwars:republic_root",
                "galacticwars:republic",
                Set.of(),
                Set.of());
        TechnologyNodeDefinition separatist = node(
                "galacticwars:separatist_child",
                "galacticwars:separatist",
                Set.of(republic.id()),
                Set.of());
        assertThrows(() -> new TechnologyCatalog(ordered(republic, separatist), FACTIONS),
                "crosses faction tree");

        TechnologyNodeDefinition unknown = node(
                "galacticwars:unknown_root",
                "galacticwars:unknown",
                Set.of(),
                Set.of());
        assertThrows(() -> new TechnologyCatalog(ordered(unknown), FACTIONS),
                "Unknown technology faction");
    }

    private static void rejectsDuplicateRecipeAssignmentsAndMismatchedIds() {
        TechnologyNodeDefinition first = node(
                "galacticwars:first",
                "galacticwars:republic",
                Set.of(),
                Set.of("galacticwars:dc15_blaster"));
        TechnologyNodeDefinition second = node(
                "galacticwars:second",
                "galacticwars:republic",
                Set.of(),
                Set.of("galacticwars:dc15_blaster"));
        assertThrows(() -> new TechnologyCatalog(ordered(first, second), FACTIONS),
                "unlocked by both");

        assertThrows(() -> new TechnologyCatalog(Map.of("galacticwars:wrong", first), FACTIONS),
                "mismatched technology node");
    }

    private static void rejectsInvalidCostsAndWorkBounds() {
        assertThrows(() -> new TechnologyNodeDefinition(
                        "galacticwars:bad_cost",
                        "galacticwars:republic",
                        "Bad Cost",
                        Set.of(),
                        Map.of("minecraft:iron_ingot", TechnologyNodeDefinition.MAX_INPUT_COUNT + 1),
                        20,
                        Set.of()),
                "excessive item cost");
        assertThrows(() -> new TechnologyNodeDefinition(
                        "galacticwars:bad_work",
                        "galacticwars:republic",
                        "Bad Work",
                        Set.of(),
                        Map.of("minecraft:iron_ingot", 1),
                        TechnologyNodeDefinition.MAX_REQUIRED_WORK + 1,
                        Set.of()),
                "excessive work cost");
    }

    private static void definitionHashIsCanonical() {
        LinkedHashMap<String, Integer> firstInputs = new LinkedHashMap<>();
        firstInputs.put("minecraft:redstone", 2);
        firstInputs.put("minecraft:iron_ingot", 1);
        LinkedHashMap<String, Integer> secondInputs = new LinkedHashMap<>();
        secondInputs.put("minecraft:iron_ingot", 1);
        secondInputs.put("minecraft:redstone", 2);
        TechnologyNodeDefinition first = new TechnologyNodeDefinition(
                "galacticwars:canonical",
                TechnologyCatalog.UNIVERSAL,
                "Canonical",
                new java.util.LinkedHashSet<>(java.util.List.of(
                        "galacticwars:z", "galacticwars:a")),
                firstInputs,
                20,
                new java.util.LinkedHashSet<>(java.util.List.of(
                        "galacticwars:z_recipe", "galacticwars:a_recipe")));
        TechnologyNodeDefinition second = new TechnologyNodeDefinition(
                "galacticwars:canonical",
                TechnologyCatalog.UNIVERSAL,
                "Canonical",
                new java.util.LinkedHashSet<>(java.util.List.of(
                        "galacticwars:a", "galacticwars:z")),
                secondInputs,
                20,
                new java.util.LinkedHashSet<>(java.util.List.of(
                        "galacticwars:a_recipe", "galacticwars:z_recipe")));
        assertEquals(first.definitionHash(), second.definitionHash(),
                "definition hash ignores collection iteration order");
    }

    private static TechnologyNodeDefinition node(
            String id,
            String faction,
            Set<String> prerequisites,
            Set<String> recipes
    ) {
        return new TechnologyNodeDefinition(
                id,
                faction,
                id,
                prerequisites,
                Map.of("minecraft:iron_ingot", 1),
                20,
                recipes);
    }

    private static Map<String, TechnologyNodeDefinition> ordered(TechnologyNodeDefinition... nodes) {
        LinkedHashMap<String, TechnologyNodeDefinition> result = new LinkedHashMap<>();
        for (TechnologyNodeDefinition node : nodes) {
            result.put(node.id(), node);
        }
        return result;
    }

    private static void assertThrows(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " was accepted");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
