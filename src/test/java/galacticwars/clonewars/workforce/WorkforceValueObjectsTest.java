package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class WorkforceValueObjectsTest {
    private WorkforceValueObjectsTest() {
    }

    public static void main(String[] args) {
        machineIdentifiersUseLocaleIndependentLowercase();
        centeredBoundsUseEveryConfiguredDimension();

        System.out.println("WorkforceValueObjectsTest passed");
    }

    private static void machineIdentifiersUseLocaleIndependentLowercase() {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            WorkerAction action = new WorkerAction(
                    WorkerAction.Type.REQUEST_SUPPLY,
                    Optional.empty(),
                    "minecraft:IRON_INGOT",
                    1,
                    "test");
            WorkerStatus status = new WorkerStatus(
                    WorkerPhase.PAUSED,
                    "test",
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    0,
                    0,
                    "minecraft:IRON_INGOT");
            WorkerResourceDecision decision = new WorkerResourceDecision(
                    WorkerResourceAction.IDLE,
                    "minecraft:IRON_INGOT",
                    0,
                    "test");
            ResourceInventory inventory = ResourceInventory.of("minecraft:IRON_INGOT", 3);
            WorkerAssignment assignment = new WorkerAssignment(
                    UUID.randomUUID(),
                    WorkerProfession.MINER,
                    "MINECRAFT:IRON_DIMENSION",
                    0,
                    64,
                    0,
                    8,
                    0L,
                    Optional.empty());

            assertEquals("minecraft:iron_ingot", action.itemId(), "worker action item ID");
            assertEquals("minecraft:iron_ingot", status.requiredResource(), "worker status resource ID");
            assertEquals("minecraft:iron_ingot", decision.itemId(), "resource decision item ID");
            assertEquals(3, inventory.count("minecraft:iron_ingot"), "resource inventory count");
            assertEquals("minecraft:iron_dimension", assignment.dimensionId(), "assignment dimension ID");
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    private static void centeredBoundsUseEveryConfiguredDimension() {
        WorkAreaBounds bounds = new WorkAreaBounds(4, 3, 2);

        assertTrue(bounds.containsCenteredAt(10, 20, 30, 9, 19, 30), "minimum corner");
        assertTrue(bounds.containsCenteredAt(10, 20, 30, 12, 21, 31), "maximum corner");
        assertFalse(bounds.containsCenteredAt(10, 20, 30, 8, 20, 30), "outside width");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label + " expected true");
        }
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) {
            throw new AssertionError(label + " expected false");
        }
    }
}
