package galacticwars.clonewars.technology;

import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class KingdomTechnologyStateTest {
    private KingdomTechnologyStateTest() {
    }

    public static void main(String[] args) {
        projectTracksMaterialsTechnicianWorkAndReplays();
        replayLedgerIsBounded();
        completionAndMigrationAreIdempotent();
        codecRoundTripsActiveResearch();
        System.out.println("KingdomTechnologyStateTest passed");
    }

    private static void projectTracksMaterialsTechnicianWorkAndReplays() {
        TechnologyNodeDefinition node = node();
        UUID startReplay = uuid(1);
        UUID contributionReplay = uuid(2);
        UUID technician = uuid(3);
        KingdomResearchProject project = KingdomResearchProject.start(node, startReplay);

        assertTrue(project.hasReplay(startReplay), "start replay is recorded");
        assertTrue(!project.materialsComplete(), "fresh project lacks materials");

        project = project.withContribution(
                Map.of("minecraft:iron_ingot", 2, "minecraft:redstone", 1),
                contributionReplay);
        assertTrue(project.materialsComplete(), "delivered materials complete");
        assertTrue(project.hasReplay(contributionReplay), "contribution replay is recorded");

        project = project.withTechnician(technician, uuid(4));
        assertEquals(Optional.of(technician), project.technicianId(), "technician assignment");
        project = project.withWork(20);
        assertEquals(20, project.workProgress(), "technician work progress");

        KingdomResearchProject completedProject = project;
        assertThrows(() -> completedProject.withContribution(
                Map.of("minecraft:iron_ingot", 1), uuid(5)), "over-delivery");
    }

    private static void replayLedgerIsBounded() {
        KingdomResearchProject project = KingdomResearchProject.start(node(), uuid(0));
        for (int index = 1; index <= 140; index++) {
            project = project.withTechnician(uuid(index), uuid(1_000 + index));
        }
        assertEquals(KingdomResearchProject.MAX_REPLAY_IDS, project.replayIds().size(),
                "bounded replay ledger size");
        assertTrue(!project.hasReplay(uuid(0)), "oldest replay is evicted");
        assertTrue(project.hasReplay(uuid(1_140)), "newest replay is retained");
    }

    private static void completionAndMigrationAreIdempotent() {
        UUID kingdomId = uuid(50);
        KingdomTechnologyState state = KingdomTechnologyState.empty(
                kingdomId, "galacticwars:republic")
                .withProject(KingdomResearchProject.start(node(), uuid(51)))
                .completeProject();
        assertTrue(state.completed("galacticwars:field_fabrication"),
                "completed project becomes shared technology");
        assertTrue(state.activeProject().isEmpty(), "completion clears active project");

        KingdomTechnologyState legacy = KingdomTechnologyState.legacyPending(
                kingdomId, "galacticwars:republic");
        KingdomTechnologyState migrated = legacy.grantMigrationNodes(Set.of(
                "galacticwars:field_fabrication",
                "galacticwars:plastoid_processing"));
        assertTrue(!migrated.legacyMigrationPending(), "migration marker is cleared");
        assertEquals(migrated, migrated.grantMigrationNodes(Set.of(
                "galacticwars:field_fabrication",
                "galacticwars:plastoid_processing")), "migration is idempotent");
        assertTrue(!migrated.completed("galacticwars:droid_field_arms"),
                "migration does not grant foreign technology");
    }

    private static void codecRoundTripsActiveResearch() {
        UUID kingdomId = uuid(60);
        KingdomTechnologyState expected = KingdomTechnologyState.empty(
                        kingdomId, "galacticwars:republic")
                .withProject(KingdomResearchProject.start(node(), uuid(61))
                        .withContribution(Map.of(
                                "minecraft:iron_ingot", 2,
                                "minecraft:redstone", 1), uuid(62))
                        .withTechnician(uuid(63), uuid(64))
                        .withWork(7));
        var encoded = TechnologyCodecs.KINGDOM_STATE.encodeStart(JsonOps.INSTANCE, expected)
                .result().orElseThrow();
        KingdomTechnologyState decoded = TechnologyCodecs.KINGDOM_STATE.parse(
                        JsonOps.INSTANCE, encoded)
                .result().orElseThrow();
        assertEquals(expected, decoded, "technology persistence round-trip");
    }

    private static TechnologyNodeDefinition node() {
        return new TechnologyNodeDefinition(
                "galacticwars:field_fabrication",
                TechnologyCatalog.UNIVERSAL,
                "Field Fabrication",
                Set.of(),
                Map.of("minecraft:iron_ingot", 2, "minecraft:redstone", 1),
                20,
                Set.of());
    }

    private static UUID uuid(long suffix) {
        return new UUID(0L, suffix);
    }

    private static void assertThrows(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " was accepted");
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
