package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.kingdom.CommandCenterDashboardState.PositionSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.WorkerSummary;
import java.util.Optional;
import java.util.UUID;

public final class CommandCenterActionAvailabilityTest {
    private CommandCenterActionAvailabilityTest() {
    }

    public static void main(String[] args) {
        configureWorksiteRequiresDurableWorkerAssignment();

        System.out.println("CommandCenterActionAvailabilityTest passed");
    }

    private static void configureWorksiteRequiresDurableWorkerAssignment() {
        assertFalse(CommandCenterActionAvailability.canConfigureWorksite(Optional.empty()),
                "missing worker");
        assertFalse(CommandCenterActionAvailability.canConfigureWorksite(
                        Optional.of(worker(Optional.empty()))),
                "worker without worksite");
        assertTrue(CommandCenterActionAvailability.canConfigureWorksite(
                        Optional.of(worker(Optional.of(new PositionSummary(
                                "minecraft:overworld", 10, 64, 10))))),
                "assigned worker");
    }

    private static WorkerSummary worker(Optional<PositionSummary> worksite) {
        return new WorkerSummary(
                UUID.randomUUID(),
                "CT-6116",
                "farmer",
                "work_at_site",
                "paused",
                "ready",
                worksite,
                8,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                0,
                4);
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
