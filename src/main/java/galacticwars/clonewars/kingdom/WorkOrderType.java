package galacticwars.clonewars.kingdom;

import java.util.Locale;
import java.util.Optional;
import galacticwars.clonewars.workforce.WorkerProfession;

public enum WorkOrderType {
    FARM,
    LUMBER,
    FISH,
    ANIMAL_FARM,
    MINE,
    BUILD,
    COOK,
    MERCHANT,
    COURIER,
    RESEARCH;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static WorkOrderType byId(String id) {
        return valueOf(id.trim().toUpperCase(Locale.ROOT));
    }

    public WorkerProfession profession() {
        return switch (this) {
            case FARM -> WorkerProfession.FARMER;
            case LUMBER -> WorkerProfession.LUMBERJACK;
            case FISH -> WorkerProfession.FISHERMAN;
            case ANIMAL_FARM -> WorkerProfession.ANIMAL_FARMER;
            case MINE -> WorkerProfession.MINER;
            case BUILD -> WorkerProfession.BUILDER;
            case COOK -> WorkerProfession.COOK;
            case MERCHANT -> WorkerProfession.MERCHANT;
            case COURIER -> WorkerProfession.COURIER;
            case RESEARCH -> WorkerProfession.TECHNICIAN;
        };
    }

    public static Optional<WorkOrderType> forProfession(WorkerProfession profession) {
        return switch (profession) {
            case FARMER -> Optional.of(FARM);
            case LUMBERJACK -> Optional.of(LUMBER);
            case FISHERMAN -> Optional.of(FISH);
            case ANIMAL_FARMER -> Optional.of(ANIMAL_FARM);
            case MINER -> Optional.of(MINE);
            case BUILDER -> Optional.of(BUILD);
            case COOK -> Optional.of(COOK);
            case MERCHANT -> Optional.of(MERCHANT);
            case COURIER -> Optional.of(COURIER);
            case TECHNICIAN -> Optional.of(RESEARCH);
        };
    }
}
