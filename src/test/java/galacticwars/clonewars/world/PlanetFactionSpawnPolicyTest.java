package galacticwars.clonewars.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import galacticwars.clonewars.army.ArmyEquipmentLoadout;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyUnitCatalog;
import galacticwars.clonewars.army.ArmyUnitDefinition;
import galacticwars.clonewars.army.ArmyUnitId;
import galacticwars.clonewars.army.ArmyUnitRole;
import galacticwars.clonewars.data.GameplayDataSnapshot;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.faction.FactionCatalog;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.recruitment.NpcServiceBranch;

public final class PlanetFactionSpawnPolicyTest {
    private PlanetFactionSpawnPolicyTest() {
    }

    public static void main(String[] args) {
        GameplayDataSnapshot snapshot = snapshot();

        assertEquals(Set.of(
                "galacticwars:hutt_enforcer",
                "galacticwars:bounty_hunter",
                "galacticwars:smuggler",
                "galacticwars:hutt_civilian"),
                PlanetFactionSpawnPolicy.allowedEntityTypeIds(snapshot, "galacticwars:tatooine"),
                "Tatooine faction ecology");
        assertEquals(Set.of(
                "galacticwars:b1_battle_droid",
                "galacticwars:b2_super_battle_droid",
                "galacticwars:commando_droid",
                "galacticwars:separatist_technician"),
                PlanetFactionSpawnPolicy.allowedEntityTypeIds(snapshot, "galacticwars:geonosis"),
                "Geonosis faction ecology");
        Set<String> republicEcology = Set.of(
                "galacticwars:clone_trooper",
                "galacticwars:arc_trooper",
                "galacticwars:jedi_knight",
                "galacticwars:republic_civilian");
        assertEquals(republicEcology,
                PlanetFactionSpawnPolicy.allowedEntityTypeIds(snapshot, "galacticwars:kamino"),
                "Kamino faction ecology");
        assertEquals(republicEcology,
                PlanetFactionSpawnPolicy.allowedEntityTypeIds(snapshot, "galacticwars:coruscant"),
                "Coruscant faction ecology");

        assertAllowed(snapshot, " GALACTICWARS:TATOOINE ", "GALACTICWARS:HUTT_ENFORCER",
                "galacticwars:hutt_cartel", "galacticwars:hutt_enforcer", NpcServiceBranch.MILITARY);
        assertAllowed(snapshot, "galacticwars:tatooine", "galacticwars:hutt_civilian",
                "galacticwars:hutt_cartel", "galacticwars:hutt_civilian", NpcServiceBranch.CIVILIAN);
        assertAllowed(snapshot, "galacticwars:geonosis", "galacticwars:separatist_technician",
                "galacticwars:separatist", "galacticwars:separatist_technician", NpcServiceBranch.CIVILIAN);

        PlanetFactionSpawnPolicy.Evaluation crossFaction = PlanetFactionSpawnPolicy.evaluate(
                snapshot, "galacticwars:tatooine", "galacticwars:clone_trooper");
        assertTrue(crossFaction.knownPlanetDimension(), "Tatooine must be recognized");
        assertFalse(crossFaction.allowed(), "Republic units must not spawn on Hutt Tatooine");
        assertEquals(NpcServiceBranch.MILITARY, crossFaction.serviceBranch(),
                "Known cross-faction military archetype branch");

        PlanetFactionSpawnPolicy.Evaluation unknownEntity = PlanetFactionSpawnPolicy.evaluate(
                snapshot, "galacticwars:kamino", "galacticwars:unmapped_recruit");
        assertTrue(unknownEntity.knownPlanetDimension(), "Kamino must be recognized");
        assertFalse(unknownEntity.allowed(), "Unmapped recruits must be rejected");
        assertEquals(null, unknownEntity.serviceBranch(), "Unmapped recruit branch");

        PlanetFactionSpawnPolicy.Evaluation unknownDimension = PlanetFactionSpawnPolicy.evaluate(
                snapshot, "minecraft:the_nether", "galacticwars:clone_trooper");
        assertFalse(unknownDimension.knownPlanetDimension(), "Vanilla dimensions are not launch planets");
        assertFalse(unknownDimension.allowed(), "Vanilla dimensions must not use planet ecology");
        assertEquals(Set.of(), PlanetFactionSpawnPolicy.allowedEntityTypeIds(snapshot, "minecraft:the_nether"),
                "Unknown planet ecology");

        System.out.println("PlanetFactionSpawnPolicyTest passed");
    }

    private static void assertAllowed(
            GameplayDataSnapshot snapshot,
            String dimensionId,
            String entityTypeId,
            String factionId,
            String definitionId,
            NpcServiceBranch branch
    ) {
        PlanetFactionSpawnPolicy.Evaluation evaluation = PlanetFactionSpawnPolicy.evaluate(
                snapshot, dimensionId, entityTypeId);
        assertTrue(evaluation.knownPlanetDimension(), dimensionId + " must be recognized");
        assertTrue(evaluation.allowed(), entityTypeId + " must be allowed in " + dimensionId);
        assertEquals(factionId, evaluation.factionId(), entityTypeId + " faction");
        assertEquals(definitionId, evaluation.definitionId(), entityTypeId + " definition");
        assertEquals(branch, evaluation.serviceBranch(), entityTypeId + " service branch");
    }

    private static GameplayDataSnapshot snapshot() {
        List<ArmyUnitDefinition> units = new ArrayList<>();
        addUnits(units, "hutt_cartel", "hutt_enforcer", "bounty_hunter", "smuggler");
        addUnits(units, "separatist", "b1_battle_droid", "b2_super_battle_droid", "commando_droid");
        addUnits(units, "republic", "clone_trooper", "arc_trooper", "jedi_knight");

        LinkedHashMap<String, ArmyUnitId> unitIdsByEntityType = new LinkedHashMap<>();
        for (ArmyUnitDefinition unit : units) {
            unitIdsByEntityType.put(unit.entityTypeId(), unit.id());
        }

        LinkedHashMap<String, CivilianArchetypeDefinition> civilians = new LinkedHashMap<>();
        addCivilian(civilians, "hutt_cartel", "hutt_civilian");
        addCivilian(civilians, "separatist", "separatist_technician");
        addCivilian(civilians, "republic", "republic_civilian");

        LinkedHashMap<String, LaunchContentDefinitions.PlanetDefinition> planets = new LinkedHashMap<>();
        addPlanet(planets, "tatooine", "hutt_cartel");
        addPlanet(planets, "geonosis", "separatist");
        addPlanet(planets, "kamino", "republic");
        addPlanet(planets, "coruscant", "republic");
        LaunchContentDefinitions launchContent = new LaunchContentDefinitions(
                planets, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

        return new GameplayDataSnapshot(
                new FactionCatalog(Map.of()),
                new ArmyUnitCatalog(units),
                unitIdsByEntityType,
                Map.of(),
                Map.of(),
                Map.of(),
                civilians,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                launchContent);
    }

    private static void addUnits(List<ArmyUnitDefinition> units, String factionId, String... unitIds) {
        for (String unitId : unitIds) {
            String entityTypeId = "galacticwars:" + unitId;
            units.add(new ArmyUnitDefinition(
                    ArmyUnitId.of(entityTypeId),
                    unitId,
                    FactionId.of(factionId),
                    ArmyUnitRole.INFANTRY,
                    1,
                    20,
                    2,
                    ArmyFormation.LINE,
                    entityTypeId,
                    0.28D,
                    24.0D,
                    0.0D,
                    ArmyEquipmentLoadout.empty()));
        }
    }

    private static void addCivilian(
            Map<String, CivilianArchetypeDefinition> civilians,
            String factionId,
            String archetypeId
    ) {
        String entityTypeId = "galacticwars:" + archetypeId;
        civilians.put(entityTypeId, new CivilianArchetypeDefinition(
                entityTypeId,
                archetypeId,
                "galacticwars:" + factionId,
                entityTypeId,
                List.of("builder"),
                20,
                0.27D,
                75,
                "planet_hab"));
    }

    private static void addPlanet(
            Map<String, LaunchContentDefinitions.PlanetDefinition> planets,
            String planetId,
            String factionId
    ) {
        planets.put(planetId, new LaunchContentDefinitions.PlanetDefinition(
                planetId,
                "galacticwars:" + planetId,
                "arrival",
                "theme",
                factionId));
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        assertTrue(!condition, label);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
