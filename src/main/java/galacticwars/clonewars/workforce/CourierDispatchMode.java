package galacticwars.clonewars.workforce;

import java.util.Locale;

/**
 * Selects whether a courier follows a configured route, services authoritative
 * settlement demands, or alternates between both sources of work.
 */
public enum CourierDispatchMode {
    MANUAL,
    AUTOMATIC,
    HYBRID;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CourierDispatchMode byId(String id) {
        if (id == null || id.isBlank()) {
            return AUTOMATIC;
        }
        try {
            return valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return AUTOMATIC;
        }
    }
}
