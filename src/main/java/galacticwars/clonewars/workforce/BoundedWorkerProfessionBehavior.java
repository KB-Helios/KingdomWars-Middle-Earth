package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.WorkOrder;
import java.util.Optional;

/**
 * Common Galactic-native profession handler. Target discovery and movement are
 * controller concerns; this handler authorizes and executes one bounded atomic
 * world or inventory interaction.
 */
final class BoundedWorkerProfessionBehavior implements WorkerProfessionBehavior {
    private final WorkerProfession profession;

    BoundedWorkerProfessionBehavior(WorkerProfession profession) {
        this.profession = profession;
    }

    @Override
    public WorkerProfession profession() {
        return profession;
    }

    @Override
    public WorkerAction plan(WorkerRuntimeContext context) {
        if (context.profession() != profession
                || context.executionState().phase() != WorkerPhase.INTERACT) {
            return WorkerAction.idle("interaction_not_ready");
        }
        WorkerExecutionState state = context.executionState();
        String reason = state.reasonCode();
        WorkerAction.Type type;
        if (reason.contains("withdraw")) {
            type = WorkerAction.Type.WITHDRAW;
        } else if (reason.contains("deliver")) {
            type = WorkerAction.Type.DEPOSIT;
        } else {
            type = WorkerAction.Type.INTERACT;
        }
        WorkOrder order = context.workOrder().orElse(null);
        String itemId = order == null ? "" : order.resourceId();
        int quantity = order == null
                ? 0
                : Math.max(0, order.quantity() - order.completedQuantity());
        return new WorkerAction(
                type,
                state.target(),
                itemId,
                quantity,
                reason);
    }

    @Override
    public WorkerActionResult execute(
            WorkerRuntimeContext context,
            WorkerAction action
    ) {
        if (action.type() == WorkerAction.Type.IDLE) {
            return WorkerActionResult.unchanged(context.executionState());
        }
        WorkerTarget target = action.target().orElse(null);
        if (target == null) {
            return WorkerActionResult.unchanged(context.executionState().transition(
                    WorkerPhase.BLOCKED,
                    "target_unloaded",
                    Optional.empty()));
        }
        if (requiresWorksiteBounds(action.reasonCode())
                && !insideWorksite(target, context)) {
            return WorkerActionResult.unchanged(context.executionState().transition(
                    WorkerPhase.BLOCKED,
                    "target_outside_worksite",
                    Optional.empty()));
        }
        return context.worldActions().executeAtomicWorkerAction(profession, action);
    }

    private static boolean requiresWorksiteBounds(String reason) {
        return reason.equals("navigate_work_target")
                || reason.equals("feed_animals")
                || reason.equals("harvest_animal")
                || reason.equals("cook_station_wait")
                || reason.equals("fishing_wait")
                || reason.equals("build_place")
                || reason.equals("open_market");
    }

    private static boolean insideWorksite(
            WorkerTarget target,
            WorkerRuntimeContext context
    ) {
        var worksite = context.worksite();
        if (!worksite.dimensionId().equals(target.dimensionId())) {
            return false;
        }
        return worksite.configuration().bounds().containsCenteredAt(
                worksite.x(),
                worksite.y(),
                worksite.z(),
                target.x(),
                target.y(),
                target.z());
    }
}
