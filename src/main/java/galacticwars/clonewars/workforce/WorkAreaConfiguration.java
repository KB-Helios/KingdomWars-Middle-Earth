package galacticwars.clonewars.workforce;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public record WorkAreaConfiguration(
        WorkAreaBounds bounds,
        boolean kingdomAccess,
        int priority,
        boolean overlayVisible,
        List<String> itemFilters,
        List<CourierWaypoint> courierRoute,
        CourierRouteMode courierRouteMode,
        long courierRouteRevision,
        CourierDispatchMode courierDispatchMode,
        long revision
) {
    public static final int MAX_ITEM_FILTERS = 64;
    public static final int MAX_FILTER_LENGTH = 128;

    public WorkAreaConfiguration {
        Objects.requireNonNull(bounds, "bounds");
        if (priority < 0 || priority > 100) {
            throw new IllegalArgumentException("work area priority must be between 0 and 100");
        }
        LinkedHashSet<String> filters = new LinkedHashSet<>();
        for (String filter : Objects.requireNonNull(itemFilters, "itemFilters")) {
            String normalized = filter.trim().toLowerCase(Locale.ROOT);
            if (isValidFilter(normalized)) {
                filters.add(normalized);
                if (filters.size() == MAX_ITEM_FILTERS) {
                    break;
                }
            }
        }
        itemFilters = List.copyOf(filters);
        List<CourierWaypoint> normalizedRoute = List.copyOf(
                Objects.requireNonNull(courierRoute, "courierRoute"));
        courierRoute = normalizedRoute.size() <= CourierRoutePlan.MAX_WAYPOINTS
                ? normalizedRoute
                : List.copyOf(normalizedRoute.subList(0, CourierRoutePlan.MAX_WAYPOINTS));
        Objects.requireNonNull(courierRouteMode, "courierRouteMode");
        Objects.requireNonNull(courierDispatchMode, "courierDispatchMode");
        if (courierRouteRevision < 0L || revision < 0L) {
            throw new IllegalArgumentException("work area revisions cannot be negative");
        }
    }

    public WorkAreaConfiguration(
            WorkAreaBounds bounds,
            boolean kingdomAccess,
            int priority,
            boolean overlayVisible,
            List<String> itemFilters,
            List<CourierWaypoint> courierRoute
    ) {
        this(bounds, kingdomAccess, priority, overlayVisible, itemFilters, courierRoute,
                CourierRouteMode.LOOP, 0L, legacyDispatchMode(courierRoute), 0L);
    }

    /**
     * Compatibility constructor for schema-10 call sites and persisted data.
     */
    public WorkAreaConfiguration(
            WorkAreaBounds bounds,
            boolean kingdomAccess,
            int priority,
            boolean overlayVisible,
            List<String> itemFilters,
            List<CourierWaypoint> courierRoute,
            CourierRouteMode courierRouteMode,
            long courierRouteRevision
    ) {
        this(bounds, kingdomAccess, priority, overlayVisible, itemFilters, courierRoute,
                courierRouteMode, courierRouteRevision, legacyDispatchMode(courierRoute), 0L);
    }

    public static WorkAreaConfiguration defaults(int radius) {
        return new WorkAreaConfiguration(
                WorkAreaBounds.radius(radius), true, 50, false, List.of(), List.of(),
                CourierRouteMode.LOOP, 0L, CourierDispatchMode.AUTOMATIC, 0L);
    }

    public static WorkAreaConfiguration fromPersistence(
            WorkAreaBounds bounds,
            boolean kingdomAccess,
            int priority,
            boolean overlayVisible,
            List<String> itemFilters,
            List<CourierWaypoint> courierRoute,
            CourierRouteMode courierRouteMode,
            long courierRouteRevision,
            Optional<CourierDispatchMode> courierDispatchMode,
            long revision
    ) {
        return new WorkAreaConfiguration(
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                itemFilters,
                courierRoute,
                courierRouteMode,
                courierRouteRevision,
                courierDispatchMode.orElseGet(() -> legacyDispatchMode(courierRoute)),
                revision);
    }

    public Optional<CourierRoutePlan> courierRoutePlan() {
        return courierRoute.size() < 2
                ? Optional.empty()
                : Optional.of(new CourierRoutePlan(courierRoute, courierRouteMode, courierRouteRevision));
    }

    public WorkAreaConfiguration withCourierRoute(List<CourierWaypoint> route, CourierRouteMode mode) {
        WorkAreaConfiguration candidate = new WorkAreaConfiguration(
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                itemFilters,
                route,
                mode,
                courierRouteRevision,
                courierDispatchMode,
                revision);
        if (this.equals(candidate)) {
            return this;
        }
        return new WorkAreaConfiguration(
                candidate.bounds(),
                candidate.kingdomAccess(),
                candidate.priority(),
                candidate.overlayVisible(),
                candidate.itemFilters(),
                candidate.courierRoute(),
                candidate.courierRouteMode(),
                Math.addExact(courierRouteRevision, 1L),
                candidate.courierDispatchMode(),
                Math.addExact(revision, 1L));
    }

    public WorkAreaConfiguration withBounds(WorkAreaBounds nextBounds) {
        if (bounds.equals(nextBounds)) {
            return this;
        }
        return new WorkAreaConfiguration(nextBounds, kingdomAccess, priority, overlayVisible, itemFilters,
                courierRoute, courierRouteMode, courierRouteRevision, courierDispatchMode,
                Math.addExact(revision, 1L));
    }

    public WorkAreaConfiguration nextRevision() {
        return new WorkAreaConfiguration(
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                itemFilters,
                courierRoute,
                courierRouteMode,
                courierRouteRevision,
                courierDispatchMode,
                Math.addExact(revision, 1L));
    }

    public WorkAreaConfiguration withSettings(
            WorkAreaBounds nextBounds,
            boolean nextKingdomAccess,
            int nextPriority,
            boolean nextOverlayVisible,
            List<String> nextItemFilters,
            CourierDispatchMode nextDispatchMode
    ) {
        WorkAreaConfiguration candidate = new WorkAreaConfiguration(
                nextBounds,
                nextKingdomAccess,
                nextPriority,
                nextOverlayVisible,
                nextItemFilters,
                courierRoute,
                courierRouteMode,
                courierRouteRevision,
                nextDispatchMode,
                revision);
        if (this.equals(candidate)) {
            return this;
        }
        return new WorkAreaConfiguration(
                nextBounds,
                nextKingdomAccess,
                nextPriority,
                nextOverlayVisible,
                nextItemFilters,
                courierRoute,
                courierRouteMode,
                courierRouteRevision,
                nextDispatchMode,
                Math.addExact(revision, 1L));
    }

    private static CourierDispatchMode legacyDispatchMode(List<CourierWaypoint> route) {
        return route == null || route.isEmpty()
                ? CourierDispatchMode.AUTOMATIC
                : CourierDispatchMode.MANUAL;
    }

    private static boolean isValidFilter(String filter) {
        if (filter.isBlank() || filter.length() > MAX_FILTER_LENGTH) {
            return false;
        }
        String identifier = filter.charAt(0) == '#' ? filter.substring(1) : filter;
        int separator = identifier.indexOf(':');
        if (separator < 1 || separator == identifier.length() - 1) {
            return false;
        }
        for (int index = 0; index < identifier.length(); index++) {
            char value = identifier.charAt(index);
            boolean allowed = value >= 'a' && value <= 'z'
                    || value >= '0' && value <= '9'
                    || value == '_' || value == '-' || value == '.'
                    || value == '/' || value == ':';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }
}
