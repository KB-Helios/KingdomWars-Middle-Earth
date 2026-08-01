package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.kingdom.CommandCenterDashboardState.WorkerSummary;
import java.util.Optional;

public final class CommandCenterActionAvailability {
    private CommandCenterActionAvailability() {
    }

    public static boolean canConfigureWorksite(Optional<WorkerSummary> worker) {
        return worker.flatMap(WorkerSummary::worksite).isPresent();
    }
}
