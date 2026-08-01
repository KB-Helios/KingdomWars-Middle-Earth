package galacticwars.clonewars.workforce;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.kingdom.StorageEndpoint;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.UUIDUtil;

/** Durable workforce codecs kept independent from loader-specific registration. */
public final class WorkforceCodecs {
    public static final int MAX_ACTIONS_PER_WAYPOINT = 32;
    public static final int MAX_SUPPLY_DEMANDS = 256;
    public static final int MAX_SUPPLY_RESERVATIONS = 512;

    public static final Codec<StorageEndpoint> STORAGE_ENDPOINT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(StorageEndpoint::dimensionId),
            Codec.INT.fieldOf("x").forGetter(StorageEndpoint::x),
            Codec.INT.fieldOf("y").forGetter(StorageEndpoint::y),
            Codec.INT.fieldOf("z").forGetter(StorageEndpoint::z),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("slots").forGetter(StorageEndpoint::slots)
    ).apply(instance, StorageEndpoint::new));

    public static final Codec<SupplyDemand> SUPPLY_DEMAND = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(SupplyDemand::id),
            Codec.STRING.xmap(WorkforceCodecs::supplyCategory, WorkforceCodecs::enumId)
                    .fieldOf("category").forGetter(SupplyDemand::category),
            Codec.STRING.fieldOf("item").forGetter(SupplyDemand::itemId),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("quantity").forGetter(SupplyDemand::quantity),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("fulfilled", 0)
                    .forGetter(SupplyDemand::fulfilledQuantity),
            Codec.intRange(0, 100).optionalFieldOf("priority", 50).forGetter(SupplyDemand::priority),
            Codec.STRING.fieldOf("source").forGetter(SupplyDemand::sourceId)
    ).apply(instance, SupplyDemand::new));

    public static final Codec<SupplyReservation> SUPPLY_RESERVATION = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(SupplyReservation::id),
            UUIDUtil.CODEC.fieldOf("demand_id").forGetter(SupplyReservation::demandId),
            UUIDUtil.CODEC.fieldOf("worker_id").forGetter(SupplyReservation::workerId),
            STORAGE_ENDPOINT.fieldOf("endpoint").forGetter(SupplyReservation::endpoint),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("quantity").forGetter(SupplyReservation::quantity),
            Codec.LONG.fieldOf("expires_at").forGetter(SupplyReservation::expiresAtGameTime),
            Codec.STRING.xmap(WorkforceCodecs::reservationState, WorkforceCodecs::enumId)
                    .fieldOf("state").forGetter(SupplyReservation::state)
    ).apply(instance, SupplyReservation::new));

    public static final Codec<SettlementSupplyLedger> SETTLEMENT_SUPPLY_LEDGER =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("settlement_id").forGetter(SettlementSupplyLedger::settlementId),
                    SUPPLY_DEMAND.listOf(0, MAX_SUPPLY_DEMANDS).optionalFieldOf("demands", List.of())
                            .forGetter(SettlementSupplyLedger::demands),
                    SUPPLY_RESERVATION.listOf(0, MAX_SUPPLY_RESERVATIONS)
                            .optionalFieldOf("reservations", List.of())
                            .forGetter(SettlementSupplyLedger::reservations),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0)
                            .forGetter(SettlementSupplyLedger::revision)
            ).apply(instance, SettlementSupplyLedger::new));

    public static final Codec<CourierTransferAction> COURIER_TRANSFER_ACTION =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.xmap(CourierTransferType::byId, CourierTransferType::id)
                            .fieldOf("type").forGetter(CourierTransferAction::type),
                    Codec.STRING.optionalFieldOf("item", "").forGetter(CourierTransferAction::itemId),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("quantity", 0)
                            .forGetter(CourierTransferAction::quantity)
            ).apply(instance, CourierTransferAction::new));

    public static final Codec<CourierWaypoint> COURIER_WAYPOINT =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(CourierWaypoint::dimensionId),
                    Codec.INT.fieldOf("x").forGetter(CourierWaypoint::x),
                    Codec.INT.fieldOf("y").forGetter(CourierWaypoint::y),
                    Codec.INT.fieldOf("z").forGetter(CourierWaypoint::z),
                    COURIER_TRANSFER_ACTION.listOf(0, MAX_ACTIONS_PER_WAYPOINT)
                            .optionalFieldOf("actions", List.of()).forGetter(CourierWaypoint::actions)
            ).apply(instance, CourierWaypoint::new));

    public static final Codec<CourierRoutePlan> COURIER_ROUTE_PLAN =
            RecordCodecBuilder.create(instance -> instance.group(
                    COURIER_WAYPOINT.listOf(2, CourierRoutePlan.MAX_WAYPOINTS)
                            .fieldOf("waypoints").forGetter(CourierRoutePlan::waypoints),
                    Codec.STRING.xmap(CourierRouteMode::byId, CourierRouteMode::id)
                            .optionalFieldOf("mode", CourierRouteMode.LOOP).forGetter(CourierRoutePlan::mode),
                    Codec.LONG.optionalFieldOf("revision", 0L).forGetter(CourierRoutePlan::revision)
            ).apply(instance, CourierRoutePlan::new));

    public static final Codec<CourierRouteExecutionState> COURIER_ROUTE_EXECUTION_STATE =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(0, CourierRoutePlan.MAX_WAYPOINTS - 1)
                            .optionalFieldOf("waypoint_cursor", 0)
                            .forGetter(CourierRouteExecutionState::waypointCursor),
                    Codec.intRange(0, MAX_ACTIONS_PER_WAYPOINT - 1)
                            .optionalFieldOf("action_cursor", 0)
                            .forGetter(CourierRouteExecutionState::actionCursor),
                    Codec.intRange(0, 1)
                            .xmap(value -> value == 0 ? -1 : 1, direction -> direction == -1 ? 0 : 1)
                            .optionalFieldOf("direction", 1)
                            .forGetter(CourierRouteExecutionState::direction),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("dwell_ticks", 0)
                            .forGetter(CourierRouteExecutionState::dwellTicksRemaining),
                    Codec.LONG.optionalFieldOf("route_revision", 0L)
                            .forGetter(CourierRouteExecutionState::routeRevision)
            ).apply(instance, CourierRouteExecutionState::new));

    public static final Codec<WorkerTarget> WORKER_TARGET =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(WorkerTarget::dimensionId),
                    Codec.INT.fieldOf("x").forGetter(WorkerTarget::x),
                    Codec.INT.fieldOf("y").forGetter(WorkerTarget::y),
                    Codec.INT.fieldOf("z").forGetter(WorkerTarget::z)
            ).apply(instance, WorkerTarget::new));

    public static final Codec<WorkerExecutionState> WORKER_EXECUTION_STATE =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("worksite_id")
                            .forGetter(WorkerExecutionState::worksiteId),
                    UUIDUtil.CODEC.optionalFieldOf("work_order_id")
                            .forGetter(WorkerExecutionState::workOrderId),
                    Codec.STRING.xmap(WorkerPhase::byId, WorkerPhase::id)
                            .optionalFieldOf("phase", WorkerPhase.ACQUIRE_ORDER)
                            .forGetter(WorkerExecutionState::phase),
                    WORKER_TARGET.optionalFieldOf("target")
                            .forGetter(WorkerExecutionState::target),
                    Codec.LONG.optionalFieldOf("configuration_revision", 0L)
                            .forGetter(WorkerExecutionState::configurationRevision),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("retry_count", 0)
                            .forGetter(WorkerExecutionState::retryCount),
                    Codec.LONG.optionalFieldOf("retry_at", 0L)
                            .forGetter(WorkerExecutionState::retryAtGameTime),
                    UUIDUtil.CODEC.optionalFieldOf("supply_reservation_id")
                            .forGetter(WorkerExecutionState::supplyReservationId),
                    Codec.STRING.optionalFieldOf("reason", "ready")
                            .forGetter(WorkerExecutionState::reasonCode)
            ).apply(instance, WorkerExecutionState::new));

    private WorkforceCodecs() {
    }

    private static SupplyCategory supplyCategory(String id) {
        return SupplyCategory.valueOf(id.trim().toUpperCase(Locale.ROOT));
    }

    private static SupplyReservation.State reservationState(String id) {
        return SupplyReservation.State.valueOf(id.trim().toUpperCase(Locale.ROOT));
    }

    private static String enumId(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
