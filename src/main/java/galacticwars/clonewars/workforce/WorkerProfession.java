package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Optional;

public enum WorkerProfession {
    FARMER("farmer"),
    LUMBERJACK("lumberjack"),
    FISHERMAN("fisherman"),
    ANIMAL_FARMER("animal_farmer"),
    MINER("miner"),
    BUILDER("builder"),
    COOK("cook"),
    MERCHANT("merchant"),
    COURIER("courier"),
    TECHNICIAN("technician");

    private final String id;

    WorkerProfession(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "screen.galacticwars.recruit.profession." + id;
    }

    public static Optional<WorkerProfession> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (WorkerProfession profession : values()) {
            if (profession.id.equals(normalized)) {
                return Optional.of(profession);
            }
        }
        return Optional.empty();
    }
}
