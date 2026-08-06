package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.WorksiteRecord;
import java.util.List;
import java.util.UUID;

public final class WorksiteOverlaySelectorTest {
    private WorksiteOverlaySelectorTest() {
    }

    public static void main(String[] args) {
        nearestVisibleRespectsBudgetAndStableUuidTies();

        System.out.println("WorksiteOverlaySelectorTest passed");
    }

    private static void nearestVisibleRespectsBudgetAndStableUuidTies() {
        UUID tieFirst = new UUID(0L, 1L);
        UUID tieSecond = new UUID(0L, 2L);
        UUID third = new UUID(0L, 3L);
        UUID fourth = new UUID(0L, 4L);
        UUID fifth = new UUID(0L, 5L);
        List<WorksiteRecord> candidates = List.of(
                worksite(fifth, 4),
                worksite(tieSecond, -1),
                worksite(fourth, 3),
                worksite(tieFirst, 1),
                worksite(third, 2));

        List<WorksiteRecord> selected = WorksiteOverlaySelector.nearestVisible(
                candidates, 0.5D, 64.5D, 0.5D, 3);

        assertEquals(List.of(tieFirst, tieSecond, third),
                selected.stream().map(WorksiteRecord::id).toList(),
                "nearest stable selection");
        assertEquals(List.of(), WorksiteOverlaySelector.nearestVisible(
                candidates, 0.5D, 64.5D, 0.5D, 0), "zero budget");
    }

    private static WorksiteRecord worksite(UUID id, int x) {
        return new WorksiteRecord(
                id,
                "frontier",
                "minecraft:overworld",
                x,
                64,
                0,
                8,
                2);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <"
                    + actual + ">");
        }
    }
}
