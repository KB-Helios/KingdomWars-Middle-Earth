package galacticwars.clonewars.faction.ai;

import java.util.Locale;
import java.util.Optional;

/** Stable gameplay role used to select faction AI policy without changing entity registrations. */
public enum NpcRole {
    COMMANDER,
    TROOPER,
    TRADER,
    CIVILIAN;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<NpcRole> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(id.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
