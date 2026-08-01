package galacticwars.clonewars.menu;

import java.util.Arrays;
import java.util.Optional;

/** Fixed server-owned actions accepted by the worksite configuration menu. */
public enum WorksiteConfigurationAction {
    WIDTH_DECREASE(0),
    WIDTH_INCREASE(1),
    HEIGHT_DECREASE(2),
    HEIGHT_INCREASE(3),
    DEPTH_DECREASE(4),
    DEPTH_INCREASE(5),
    PRIORITY_DECREASE(6),
    PRIORITY_INCREASE(7),
    TOGGLE_KINGDOM_ACCESS(8),
    TOGGLE_OVERLAY(9),
    CYCLE_DISPATCH_MODE(10),
    CYCLE_ROUTE_MODE(11),
    ADD_HELD_ITEM_FILTER(12),
    ADD_LOOKED_BLOCK_FILTER(13),
    REMOVE_FILTER(14),
    CLEAR_FILTERS(15),
    SET_STORAGE_FROM_LOOK(16),
    ADD_ROUTE_TAKE_FROM_LOOK(17),
    ADD_ROUTE_PUT_FROM_LOOK(18),
    REMOVE_ROUTE_WAYPOINT(19),
    CLEAR_ROUTE(20),
    ADD_LOOKED_ENTITY_FILTER(21);

    private final int id;

    WorksiteConfigurationAction(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static Optional<WorksiteConfigurationAction> byId(int id) {
        return Arrays.stream(values()).filter(value -> value.id == id).findFirst();
    }
}
