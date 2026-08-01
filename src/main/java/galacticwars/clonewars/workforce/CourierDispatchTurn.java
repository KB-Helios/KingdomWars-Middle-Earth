package galacticwars.clonewars.workforce;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Persists which source a hybrid courier should try first without interrupting
 * an already reserved physical transfer.
 */
public enum CourierDispatchTurn {
    AUTOMATIC,
    ROUTE;

    public enum Source {
        AUTOMATIC,
        ROUTE
    }

    public List<Source> preferredSources(
            CourierDispatchMode mode,
            boolean activeReservation,
            boolean routeAvailable
    ) {
        Objects.requireNonNull(mode, "mode");
        if (activeReservation) {
            return List.of(Source.AUTOMATIC);
        }
        return switch (mode) {
            case AUTOMATIC -> List.of(Source.AUTOMATIC);
            case MANUAL -> routeAvailable ? List.of(Source.ROUTE) : List.of();
            case HYBRID -> {
                if (!routeAvailable) {
                    yield List.of(Source.AUTOMATIC);
                }
                yield this == AUTOMATIC
                        ? List.of(Source.AUTOMATIC, Source.ROUTE)
                        : List.of(Source.ROUTE, Source.AUTOMATIC);
            }
        };
    }

    public CourierDispatchTurn afterAutomatic() {
        return ROUTE;
    }

    public CourierDispatchTurn afterRoute() {
        return AUTOMATIC;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CourierDispatchTurn byId(String id) {
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
