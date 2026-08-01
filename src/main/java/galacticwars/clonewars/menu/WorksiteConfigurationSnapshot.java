package galacticwars.clonewars.menu;

import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.WorkAreaConfiguration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/** Bounded, revisioned client projection of one authoritative worksite. */
public record WorksiteConfigurationSnapshot(
        int recruitEntityId,
        UUID recruitId,
        String recruitName,
        UUID worksiteId,
        String professionId,
        String dimensionId,
        int centerX,
        int centerY,
        int centerZ,
        WorkAreaBounds bounds,
        boolean kingdomAccess,
        int priority,
        boolean overlayVisible,
        List<String> itemFilters,
        CourierDispatchMode dispatchMode,
        CourierRouteMode routeMode,
        long configurationRevision,
        long routeRevision,
        List<RouteWaypointView> route,
        Optional<BlockPos> storageTarget,
        List<AssignedWorkerView> assignedWorkers,
        String phase,
        String reasonCode,
        String requiredResource,
        int completedQuantity,
        int totalQuantity,
        String feedbackCode
) {
    private static final int MAX_STRING = 128;
    private static final int MAX_ASSIGNED_WORKERS = 64;

    public WorksiteConfigurationSnapshot {
        if (recruitEntityId < 0 || priority < 0 || priority > 100
                || configurationRevision < 0L || routeRevision < 0L
                || completedQuantity < 0 || totalQuantity < completedQuantity) {
            throw new IllegalArgumentException("invalid worksite snapshot");
        }
        Objects.requireNonNull(recruitId, "recruitId");
        Objects.requireNonNull(worksiteId, "worksiteId");
        recruitName = bounded(recruitName);
        professionId = bounded(professionId);
        dimensionId = bounded(dimensionId);
        Objects.requireNonNull(bounds, "bounds");
        itemFilters = List.copyOf(new LinkedHashSet<>(
                Objects.requireNonNull(itemFilters, "itemFilters"))).stream()
                .limit(WorkAreaConfiguration.MAX_ITEM_FILTERS)
                .map(WorksiteConfigurationSnapshot::bounded)
                .toList();
        Objects.requireNonNull(dispatchMode, "dispatchMode");
        Objects.requireNonNull(routeMode, "routeMode");
        route = List.copyOf(Objects.requireNonNull(route, "route")).stream()
                .limit(galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS)
                .toList();
        storageTarget = storageTarget == null ? Optional.empty() : storageTarget;
        assignedWorkers = List.copyOf(Objects.requireNonNull(
                        assignedWorkers, "assignedWorkers")).stream()
                .limit(MAX_ASSIGNED_WORKERS)
                .toList();
        phase = bounded(phase);
        reasonCode = bounded(reasonCode);
        requiredResource = bounded(requiredResource);
        feedbackCode = bounded(feedbackCode);
    }

    public static void write(FriendlyByteBuf buffer, WorksiteConfigurationSnapshot value) {
        buffer.writeVarInt(value.recruitEntityId());
        buffer.writeUUID(value.recruitId());
        writeString(buffer, value.recruitName());
        buffer.writeUUID(value.worksiteId());
        writeString(buffer, value.professionId());
        writeString(buffer, value.dimensionId());
        buffer.writeInt(value.centerX());
        buffer.writeInt(value.centerY());
        buffer.writeInt(value.centerZ());
        buffer.writeVarInt(value.bounds().width());
        buffer.writeVarInt(value.bounds().height());
        buffer.writeVarInt(value.bounds().depth());
        buffer.writeBoolean(value.kingdomAccess());
        buffer.writeVarInt(value.priority());
        buffer.writeBoolean(value.overlayVisible());
        writeList(buffer, value.itemFilters(), WorkAreaConfiguration.MAX_ITEM_FILTERS,
                WorksiteConfigurationSnapshot::writeString);
        writeString(buffer, value.dispatchMode().id());
        writeString(buffer, value.routeMode().id());
        buffer.writeVarLong(value.configurationRevision());
        buffer.writeVarLong(value.routeRevision());
        writeList(buffer, value.route(),
                galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS,
                WorksiteConfigurationSnapshot::writeRouteWaypoint);
        buffer.writeBoolean(value.storageTarget().isPresent());
        value.storageTarget().ifPresent(buffer::writeBlockPos);
        writeList(buffer, value.assignedWorkers(), MAX_ASSIGNED_WORKERS,
                WorksiteConfigurationSnapshot::writeAssignedWorker);
        writeString(buffer, value.phase());
        writeString(buffer, value.reasonCode());
        writeString(buffer, value.requiredResource());
        buffer.writeVarInt(value.completedQuantity());
        buffer.writeVarInt(value.totalQuantity());
        writeString(buffer, value.feedbackCode());
    }

    public static WorksiteConfigurationSnapshot read(FriendlyByteBuf buffer) {
        int recruitEntityId = nonNegative(buffer.readVarInt(), "recruitEntityId");
        UUID recruitId = buffer.readUUID();
        String recruitName = readString(buffer);
        UUID worksiteId = buffer.readUUID();
        String professionId = readString(buffer);
        String dimensionId = readString(buffer);
        int centerX = buffer.readInt();
        int centerY = buffer.readInt();
        int centerZ = buffer.readInt();
        WorkAreaBounds bounds = new WorkAreaBounds(
                boundedDimension(buffer.readVarInt()),
                boundedDimension(buffer.readVarInt()),
                boundedDimension(buffer.readVarInt()));
        boolean kingdomAccess = buffer.readBoolean();
        int priority = buffer.readVarInt();
        if (priority < 0 || priority > 100) {
            throw new IllegalArgumentException("priority outside wire bounds");
        }
        boolean overlayVisible = buffer.readBoolean();
        List<String> filters = readList(
                buffer, WorkAreaConfiguration.MAX_ITEM_FILTERS,
                WorksiteConfigurationSnapshot::readString);
        CourierDispatchMode dispatchMode = CourierDispatchMode.byId(readString(buffer));
        CourierRouteMode routeMode = CourierRouteMode.byId(readString(buffer));
        long revision = nonNegative(buffer.readVarLong(), "configurationRevision");
        long routeRevision = nonNegative(buffer.readVarLong(), "routeRevision");
        List<RouteWaypointView> route = readList(
                buffer,
                galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS,
                WorksiteConfigurationSnapshot::readRouteWaypoint);
        Optional<BlockPos> storage = buffer.readBoolean()
                ? Optional.of(buffer.readBlockPos().immutable())
                : Optional.empty();
        List<AssignedWorkerView> assigned = readList(
                buffer, MAX_ASSIGNED_WORKERS,
                WorksiteConfigurationSnapshot::readAssignedWorker);
        String phase = readString(buffer);
        String reason = readString(buffer);
        String required = readString(buffer);
        int completed = nonNegative(buffer.readVarInt(), "completedQuantity");
        int total = nonNegative(buffer.readVarInt(), "totalQuantity");
        String feedback = readString(buffer);
        return new WorksiteConfigurationSnapshot(
                recruitEntityId, recruitId, recruitName, worksiteId, professionId, dimensionId,
                centerX, centerY, centerZ, bounds, kingdomAccess, priority, overlayVisible,
                filters, dispatchMode, routeMode, revision, routeRevision, route, storage,
                assigned, phase, reason, required, completed, total, feedback);
    }

    private static void writeRouteWaypoint(FriendlyByteBuf buffer, RouteWaypointView value) {
        buffer.writeInt(value.x());
        buffer.writeInt(value.y());
        buffer.writeInt(value.z());
        writeString(buffer, value.action());
        buffer.writeVarInt(value.actionCount());
    }

    private static RouteWaypointView readRouteWaypoint(FriendlyByteBuf buffer) {
        return new RouteWaypointView(
                buffer.readInt(), buffer.readInt(), buffer.readInt(),
                readString(buffer), nonNegative(buffer.readVarInt(), "actionCount"));
    }

    private static void writeAssignedWorker(FriendlyByteBuf buffer, AssignedWorkerView value) {
        buffer.writeUUID(value.recruitId());
        writeString(buffer, value.displayName());
        writeString(buffer, value.professionId());
        writeString(buffer, value.phase());
        writeString(buffer, value.reasonCode());
    }

    private static AssignedWorkerView readAssignedWorker(FriendlyByteBuf buffer) {
        return new AssignedWorkerView(
                buffer.readUUID(), readString(buffer), readString(buffer),
                readString(buffer), readString(buffer));
    }

    private static void writeString(FriendlyByteBuf buffer, String value) {
        buffer.writeUtf(bounded(value), MAX_STRING);
    }

    private static String readString(FriendlyByteBuf buffer) {
        return buffer.readUtf(MAX_STRING);
    }

    private static <T> void writeList(
            FriendlyByteBuf buffer,
            List<T> values,
            int maximum,
            BiConsumer<FriendlyByteBuf, T> writer
    ) {
        int count = Math.min(values.size(), maximum);
        buffer.writeVarInt(count);
        for (int index = 0; index < count; index++) {
            writer.accept(buffer, values.get(index));
        }
    }

    private static <T> List<T> readList(
            FriendlyByteBuf buffer,
            int maximum,
            Function<FriendlyByteBuf, T> reader
    ) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("worksite list exceeds wire bounds");
        }
        java.util.ArrayList<T> values = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            values.add(reader.apply(buffer));
        }
        return List.copyOf(values);
    }

    private static int boundedDimension(int value) {
        if (value < 1 || value > 64) {
            throw new IllegalArgumentException("worksite dimension outside wire bounds");
        }
        return value;
    }

    private static int nonNegative(int value, String label) {
        if (value < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
        return value;
    }

    private static long nonNegative(long value, String label) {
        if (value < 0L) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
        return value;
    }

    private static String bounded(String value) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        return normalized.length() <= MAX_STRING
                ? normalized
                : normalized.substring(0, MAX_STRING);
    }

    public record RouteWaypointView(int x, int y, int z, String action, int actionCount) {
        public RouteWaypointView {
            action = bounded(action);
            if (actionCount < 0) {
                throw new IllegalArgumentException("actionCount cannot be negative");
            }
        }
    }

    public record AssignedWorkerView(
            UUID recruitId,
            String displayName,
            String professionId,
            String phase,
            String reasonCode
    ) {
        public AssignedWorkerView {
            Objects.requireNonNull(recruitId, "recruitId");
            displayName = bounded(displayName);
            professionId = bounded(professionId);
            phase = bounded(phase);
            reasonCode = bounded(reasonCode);
        }
    }
}
