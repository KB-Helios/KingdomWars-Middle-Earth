package galacticwars.clonewars.workforce;

import java.util.List;

public final class WorkAreaConfigurationTest {
    private WorkAreaConfigurationTest() {
    }

    public static void main(String[] args) {
        identicalCourierRoutePreservesIdentityAndRevisions();

        System.out.println("WorkAreaConfigurationTest passed");
    }

    private static void identicalCourierRoutePreservesIdentityAndRevisions() {
        List<CourierWaypoint> route = List.of(
                waypoint(1, CourierTransferAction.takeAll()),
                waypoint(2, CourierTransferAction.putAll()));
        WorkAreaConfiguration configuration = new WorkAreaConfiguration(
                new WorkAreaBounds(9, 5, 9),
                true,
                60,
                true,
                List.of("galacticwars:energy_cell"),
                route,
                CourierRouteMode.PING_PONG,
                4L,
                CourierDispatchMode.MANUAL,
                9L);

        WorkAreaConfiguration unchanged = configuration.withCourierRoute(
                List.copyOf(route), CourierRouteMode.PING_PONG);

        assertSame(configuration, unchanged, "identical route configuration");
        assertEquals(4L, unchanged.courierRouteRevision(), "route revision");
        assertEquals(9L, unchanged.revision(), "configuration revision");
    }

    private static CourierWaypoint waypoint(int x, CourierTransferAction action) {
        return new CourierWaypoint(
                "minecraft:overworld",
                x,
                64,
                0,
                List.of(action));
    }

    private static void assertSame(Object expected, Object actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected the same instance");
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
