package galacticwars.clonewars.workforce;

import java.util.Objects;

public final class WorkerTaskPlanner {
    private WorkerTaskPlanner() {
    }

    public static WorkerTaskDecision plan(WorkerProfession profession, WorkerWorksite worksite) {
        Objects.requireNonNull(profession, "profession");
        WorkerProfessionDefinition definition = WorkerProfessionCatalog.definition(profession).orElseThrow();
        WorkAreaType requiredArea = definition.workAreaType();
        if (worksite == null) {
            return new WorkerTaskDecision(WorkerTaskType.IDLE, requiredArea, "missing_worksite");
        }
        if (worksite.areaType() != requiredArea) {
            return new WorkerTaskDecision(WorkerTaskType.REASSIGN_WORKSITE, requiredArea, "worksite_type_mismatch");
        }
        return new WorkerTaskDecision(taskFor(profession), requiredArea, "ready");
    }

    private static WorkerTaskType taskFor(WorkerProfession profession) {
        return switch (profession) {
            case FARMER -> WorkerTaskType.HARVEST_AND_REPLANT;
            case LUMBERJACK -> WorkerTaskType.CHOP_AND_REPLANT;
            case FISHERMAN -> WorkerTaskType.FISH;
            case ANIMAL_FARMER -> WorkerTaskType.FEED_AND_BREED_ANIMALS;
            case MINER -> WorkerTaskType.MINE_AND_STORE;
            case BUILDER -> WorkerTaskType.BUILD_FROM_SUPPLIES;
            case COOK -> WorkerTaskType.COOK_FOOD;
            case MERCHANT -> WorkerTaskType.TRADE_GOODS;
            case COURIER -> WorkerTaskType.DELIVER_SUPPLIES;
            case TECHNICIAN -> WorkerTaskType.RESEARCH_TECHNOLOGY;
        };
    }
}
