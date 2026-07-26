package galacticwars.clonewars.network;

import java.util.Collections;
import java.util.List;

public final class GameplayCatalogPayloadValidationTest {
    private GameplayCatalogPayloadValidationTest() {
    }

    public static void main(String[] args) {
        var requirement = new GameplayCatalogPayload.RequirementEntry("quest", "chapter1", 1);
        var unitClass = new GameplayCatalogPayload.ClassEntry(
                "galacticwars:clone_trooper",
                "Clone Trooper",
                "galacticwars:republic",
                "",
                List.of(requirement),
                List.of("Suppressive Fire", "Rally"));
        var vehicle = new GameplayCatalogPayload.VehicleEntry("laat_gunship", 180, 2400);
        var blueprint = new GameplayCatalogPayload.BlueprintEntry(
                "galacticwars:forward_base", "Forward Base", 37);
        var technology = new GameplayCatalogPayload.TechnologyEntry(
                "galacticwars:clone_field_arms",
                "galacticwars:republic",
                "Clone Field Arms",
                List.of("galacticwars:plastoid_processing"),
                List.of(new GameplayCatalogPayload.CostEntry("minecraft:iron_ingot", 8)),
                1200,
                List.of("galacticwars:dc15_blaster"));
        var fabrication = new GameplayCatalogPayload.FabricationEntry(
                "galacticwars:dc15_blaster",
                "galacticwars:republic",
                "galacticwars:clone_field_arms",
                true);
        var relation = new GameplayCatalogPayload.RelationPolicyEntry(
                "galacticwars:republic", 10, 0, -10, 90, 10);
        String contentHash = "a".repeat(64);
        var payload = new GameplayCatalogPayload(
                4L,
                contentHash,
                List.of(unitClass),
                List.of(vehicle),
                List.of(blueprint),
                List.of(technology),
                List.of(fabrication),
                List.of(relation));

        if (payload.generation() != 4L
                || !payload.classes().getFirst().classId().equals("galacticwars:clone_trooper")
                || payload.vehicles().getFirst().fuelCapacity() != 2400
                || payload.blueprints().getFirst().placementCount() != 37
                || !payload.technology().getFirst().nodeId().equals("galacticwars:clone_field_arms")
                || !payload.fabrication().getFirst().exportable()
                || payload.relationPolicies().getFirst().friendlyThreshold() != 10) {
            throw new AssertionError("valid gameplay catalog payload did not retain its projection");
        }
        expectFailure(() -> new GameplayCatalogPayload(-1L, "", List.of(), List.of(), List.of()),
                "negative generation");
        expectFailure(() -> new GameplayCatalogPayload(1L, "not-a-sha256", List.of(), List.of(), List.of()),
                "invalid content hash");
        expectFailure(() -> new GameplayCatalogPayload(
                        1L, contentHash, List.of(unitClass, unitClass), List.of(), List.of()),
                "duplicate class id");
        expectFailure(() -> new GameplayCatalogPayload(
                        1L, contentHash,
                        Collections.nCopies(GameplayCatalogPayload.MAX_CLASSES + 1, unitClass),
                        List.of(), List.of()),
                "class count bound");
        expectFailure(() -> new GameplayCatalogPayload.ClassEntry(
                        "galacticwars:invalid",
                        "x".repeat(GameplayCatalogPayload.MAX_TEXT_BYTES + 1),
                        "galacticwars:republic",
                        "",
                        List.of(),
                        List.of()),
                "text byte bound");
        expectFailure(() -> new GameplayCatalogPayload.RequirementEntry(
                        "quest", "chapter1", GameplayCatalogPayload.MAX_REQUIREMENT_AMOUNT + 1),
                "requirement amount bound");
        expectFailure(() -> new GameplayCatalogPayload.VehicleEntry(
                        "laat_gunship", GameplayCatalogPayload.MAX_VEHICLE_STAT + 1, 1),
                "vehicle maximum bound");
        expectFailure(() -> new GameplayCatalogPayload.BlueprintEntry(
                        "galacticwars:forward_base", "Forward Base",
                        GameplayCatalogPayload.MAX_BLUEPRINT_PLACEMENTS + 1),
                "blueprint placement bound");
        expectFailure(() -> new GameplayCatalogPayload.TechnologyEntry(
                        "galacticwars:bad",
                        "galacticwars:republic",
                        "Bad",
                        List.of(),
                        List.of(new GameplayCatalogPayload.CostEntry("minecraft:iron_ingot", 1)),
                        19,
                        List.of()),
                "technology work bound");
        expectFailure(() -> new GameplayCatalogPayload.CostEntry(
                        "minecraft:iron_ingot", 4_097),
                "technology cost bound");
        expectFailure(() -> new GameplayCatalogPayload.RelationPolicyEntry(
                        "galacticwars:republic", 0, 0, -10, 90, 10),
                "relation threshold ordering");

        try {
            payload.classes().add(unitClass);
            throw new AssertionError("payload class list is mutable");
        } catch (UnsupportedOperationException expected) {
            // Expected immutable projection.
        }

        System.out.println("GameplayCatalogPayloadValidationTest passed");
    }

    private static void expectFailure(Runnable action, String label) {
        try {
            action.run();
            throw new AssertionError(label + " was accepted");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }
    }
}
