package galacticwars.clonewars.workforce;

import java.util.EnumSet;
import java.util.List;

public final class WorkerProfessionCatalogTest {
    private WorkerProfessionCatalogTest() {
    }

    public static void main(String[] args) {
        catalogIncludesWorkersFeatureSet();
        eachProfessionHasMatchingWorkAreaAndCost();
        eachProfessionHasDistinctCommandButton();

        System.out.println("WorkerProfessionCatalogTest passed");
    }

    private static void catalogIncludesWorkersFeatureSet() {
        EnumSet<WorkerProfession> expected = EnumSet.of(
                WorkerProfession.FARMER,
                WorkerProfession.LUMBERJACK,
                WorkerProfession.FISHERMAN,
                WorkerProfession.ANIMAL_FARMER,
                WorkerProfession.MINER,
                WorkerProfession.BUILDER,
                WorkerProfession.COOK,
                WorkerProfession.MERCHANT,
                WorkerProfession.COURIER,
                WorkerProfession.TECHNICIAN);

        EnumSet<WorkerProfession> actual = WorkerProfessionCatalog.professions().stream()
                .map(WorkerProfessionDefinition::profession)
                .collect(() -> EnumSet.noneOf(WorkerProfession.class), EnumSet::add, EnumSet::addAll);

        assertEquals(expected, actual, "profession set");
    }

    private static void eachProfessionHasMatchingWorkAreaAndCost() {
        assertProfession(WorkerProfession.FARMER, WorkAreaType.CROP_FARM, 20, "minecraft:iron_hoe");
        assertProfession(WorkerProfession.LUMBERJACK, WorkAreaType.LUMBER_AREA, 20, "minecraft:iron_axe");
        assertProfession(WorkerProfession.FISHERMAN, WorkAreaType.FISHING_AREA, 18, "minecraft:fishing_rod");
        assertProfession(WorkerProfession.ANIMAL_FARMER, WorkAreaType.ANIMAL_FARM, 22, "minecraft:wheat");
        assertProfession(WorkerProfession.MINER, WorkAreaType.MINING_AREA, 28, "minecraft:iron_pickaxe");
        assertProfession(WorkerProfession.BUILDER, WorkAreaType.BUILDING_AREA, 30, "minecraft:bricks");
        assertProfession(WorkerProfession.COOK, WorkAreaType.KITCHEN, 18, "minecraft:bread");
        assertProfession(WorkerProfession.MERCHANT, WorkAreaType.MARKET, 26, "galacticwars:credit_chip");
        assertProfession(WorkerProfession.COURIER, WorkAreaType.COURIER_ROUTE, 16, "minecraft:chest");
        assertProfession(WorkerProfession.TECHNICIAN, WorkAreaType.LABORATORY, 16, "minecraft:redstone");
    }

    private static void eachProfessionHasDistinctCommandButton() {
        List<Integer> buttonIds = WorkerProfessionCatalog.professions().stream()
                .map(WorkerProfessionDefinition::commandButtonId)
                .distinct()
                .toList();

        assertEquals(WorkerProfessionCatalog.professions().size(), buttonIds.size(), "distinct button count");
        assertEquals(WorkerProfession.FARMER, WorkerProfessionCatalog.professionForButton(20).orElseThrow(), "farmer button");
        assertEquals(WorkerProfession.COURIER, WorkerProfessionCatalog.professionForButton(28).orElseThrow(), "courier button");
        assertEquals(WorkerProfession.TECHNICIAN,
                WorkerProfessionCatalog.professionForButton(33).orElseThrow(), "technician button");
        assertTrue(WorkerProfessionCatalog.professionForButton(99).isEmpty(), "unknown button ignored");
    }

    private static void assertProfession(
            WorkerProfession profession,
            WorkAreaType workArea,
            int cost,
            String toolItemId
    ) {
        WorkerProfessionDefinition definition = WorkerProfessionCatalog.definition(profession).orElseThrow();

        assertEquals(workArea, definition.workAreaType(), profession.name() + " work area");
        assertEquals(cost, definition.hireCostCredits(), profession.name() + " cost");
        assertEquals(toolItemId, definition.defaultHeldItemId(), profession.name() + " held item");
        assertTrue(!definition.translationKey().isBlank(), profession.name() + " translation key");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label + " expected to be true");
        }
    }
}
