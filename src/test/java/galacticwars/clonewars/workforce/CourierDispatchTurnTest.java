package galacticwars.clonewars.workforce;

import java.util.List;

public final class CourierDispatchTurnTest {
    private CourierDispatchTurnTest() {
    }

    public static void main(String[] args) {
        selectsOnlyAutomaticWorkForAutomaticMode();
        selectsOnlyConfiguredRoutesForManualMode();
        resumesActiveReservationsBeforeAnyNewSource();
        alternatesAvailableHybridSources();
        fallsBackToAutomaticWhenHybridRouteIsUnavailable();
        flipsOnlyAfterTheSelectedSourceSucceeds();
        parsesPersistedTurnsWithAnAutomaticFirstDefault();

        System.out.println("CourierDispatchTurnTest passed");
    }

    private static void selectsOnlyAutomaticWorkForAutomaticMode() {
        assertEquals(
                List.of(CourierDispatchTurn.Source.AUTOMATIC),
                CourierDispatchTurn.ROUTE.preferredSources(
                        CourierDispatchMode.AUTOMATIC, false, true),
                "automatic mode source order");
    }

    private static void selectsOnlyConfiguredRoutesForManualMode() {
        assertEquals(
                List.of(CourierDispatchTurn.Source.ROUTE),
                CourierDispatchTurn.AUTOMATIC.preferredSources(
                        CourierDispatchMode.MANUAL, false, true),
                "manual mode configured route");
        assertEquals(
                List.of(),
                CourierDispatchTurn.ROUTE.preferredSources(
                        CourierDispatchMode.MANUAL, false, false),
                "manual mode without configured route");
    }

    private static void resumesActiveReservationsBeforeAnyNewSource() {
        for (CourierDispatchMode mode : CourierDispatchMode.values()) {
            assertEquals(
                    List.of(CourierDispatchTurn.Source.AUTOMATIC),
                    CourierDispatchTurn.ROUTE.preferredSources(mode, true, true),
                    mode + " active reservation source order");
        }
    }

    private static void alternatesAvailableHybridSources() {
        assertEquals(
                List.of(
                        CourierDispatchTurn.Source.AUTOMATIC,
                        CourierDispatchTurn.Source.ROUTE),
                CourierDispatchTurn.AUTOMATIC.preferredSources(
                        CourierDispatchMode.HYBRID, false, true),
                "automatic-first hybrid order");
        assertEquals(
                List.of(
                        CourierDispatchTurn.Source.ROUTE,
                        CourierDispatchTurn.Source.AUTOMATIC),
                CourierDispatchTurn.ROUTE.preferredSources(
                        CourierDispatchMode.HYBRID, false, true),
                "route-first hybrid order");
    }

    private static void fallsBackToAutomaticWhenHybridRouteIsUnavailable() {
        assertEquals(
                List.of(CourierDispatchTurn.Source.AUTOMATIC),
                CourierDispatchTurn.ROUTE.preferredSources(
                        CourierDispatchMode.HYBRID, false, false),
                "hybrid without configured route");
    }

    private static void flipsOnlyAfterTheSelectedSourceSucceeds() {
        assertEquals(
                CourierDispatchTurn.ROUTE,
                CourierDispatchTurn.AUTOMATIC.afterAutomatic(),
                "next turn after automatic work");
        assertEquals(
                CourierDispatchTurn.AUTOMATIC,
                CourierDispatchTurn.ROUTE.afterRoute(),
                "next turn after route work");
    }

    private static void parsesPersistedTurnsWithAnAutomaticFirstDefault() {
        assertEquals(CourierDispatchTurn.ROUTE, CourierDispatchTurn.byId("route"),
                "persisted route turn");
        assertEquals(CourierDispatchTurn.AUTOMATIC, CourierDispatchTurn.byId(""),
                "missing persisted turn");
        assertEquals(CourierDispatchTurn.AUTOMATIC, CourierDispatchTurn.byId("future_value"),
                "unknown persisted turn");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
