package galacticwars.clonewars.menu;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.kingdom.SettlementRecord;
import galacticwars.clonewars.kingdom.WorksiteRecord;
import galacticwars.clonewars.kingdom.WorksiteUpdateResult;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.network.WorksiteActionPayload;
import galacticwars.clonewars.network.WorksiteStatePayload;
import galacticwars.clonewars.registry.ModMenuTypes;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierTransferAction;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.WorkAreaConfiguration;
import galacticwars.clonewars.workforce.WorkerStatus;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative worksite editor. Every world selection is resolved from the
 * server player's current ray and every durable mutation uses the configuration revision.
 */
public final class WorksiteConfigurationMenu extends AbstractContainerMenu {
    private static final int MAX_REPLAY_IDS = 64;
    private static final int DIMENSION_STEP = 2;
    private static final int PRIORITY_STEP = 10;

    private final Optional<BlockPos> commandCenterAnchor;
    private final LinkedHashSet<UUID> processedReplayIds = new LinkedHashSet<>();
    private WorksiteConfigurationSnapshot snapshot;

    public WorksiteConfigurationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                id,
                inventory,
                WorksiteConfigurationSnapshot.read(buffer),
                buffer.readBoolean()
                        ? Optional.of(buffer.readBlockPos().immutable())
                        : Optional.empty());
    }

    static Optional<WorksiteConfigurationSnapshot> captureSnapshot(
            ServerPlayer player,
            GalacticRecruitEntity recruit
    ) {
        try {
            return Optional.of(capture(player, recruit, "ready"));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    WorksiteConfigurationMenu(
            int id,
            Inventory inventory,
            GalacticRecruitEntity recruit,
            Optional<BlockPos> commandCenterAnchor
    ) {
        this(
                id,
                inventory,
                capture((ServerPlayer) inventory.player, recruit, "ready"),
                commandCenterAnchor);
    }

    private WorksiteConfigurationMenu(
            int id,
            Inventory inventory,
            WorksiteConfigurationSnapshot snapshot,
            Optional<BlockPos> commandCenterAnchor
    ) {
        super(ModMenuTypes.WORKSITE_CONFIGURATION.get(), id);
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        this.commandCenterAnchor = commandCenterAnchor == null
                ? Optional.empty()
                : commandCenterAnchor.map(BlockPos::immutable);
    }

    public WorksiteConfigurationSnapshot snapshot() {
        return snapshot;
    }

    public Optional<BlockPos> commandCenterAnchor() {
        return commandCenterAnchor;
    }

    public void applyClientSnapshot(WorksiteConfigurationSnapshot snapshot) {
        if (snapshot.worksiteId().equals(this.snapshot.worksiteId())
                && snapshot.recruitId().equals(this.snapshot.recruitId())) {
            this.snapshot = snapshot;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return false;
    }

    public boolean handleReplayAction(ServerPlayer player, WorksiteActionPayload payload) {
        if (!processedReplayIds.add(payload.replayId())) {
            refresh(player, "replayed_request");
            return false;
        }
        while (processedReplayIds.size() > MAX_REPLAY_IDS) {
            processedReplayIds.remove(processedReplayIds.iterator().next());
        }
        if (!stillValid(player)) {
            refresh(player, "permission_denied");
            return false;
        }
        WorksiteConfigurationAction action = WorksiteConfigurationAction
                .byId(payload.actionId()).orElse(null);
        if (action == null) {
            refresh(player, "invalid_action");
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get((ServerLevel) player.level());
        WorksiteRecord worksite = currentWorksite(data, player).orElse(null);
        if (worksite == null) {
            refresh(player, "worksite_missing");
            return false;
        }
        if (payload.expectedConfigurationRevision()
                != worksite.configuration().revision()) {
            refresh(player, "stale_revision");
            return false;
        }

        String result = switch (action) {
            case SET_STORAGE_FROM_LOOK -> configureStorage(player, data, worksite);
            case ADD_ROUTE_TAKE_FROM_LOOK, ADD_ROUTE_PUT_FROM_LOOK,
                    REMOVE_ROUTE_WAYPOINT, CLEAR_ROUTE, CYCLE_ROUTE_MODE ->
                    configureRoute(player, data, worksite, action, payload.selectedIndex());
            default -> configureSettings(
                    player, data, worksite, action, payload.selectedIndex());
        };
        refresh(player, result);
        return result.equals("updated") || result.equals("storage_assigned");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level() instanceof ServerLevel level)) {
            return true;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        WorksiteRecord worksite = currentWorksite(data, player).orElse(null);
        if (kingdom == null || worksite == null
                || !data.isHallActive(kingdom.ownerId())
                || !kingdom.allows(player.getUUID(), KingdomPermission.MANAGE_WORKSITES)
                || !worksite.configuration().kingdomAccess()
                        && !kingdom.ownerId().equals(player.getUUID())) {
            return false;
        }
        if (commandCenterAnchor.isPresent()) {
            BlockPos anchor = commandCenterAnchor.orElseThrow();
            return player.distanceToSqr(
                    anchor.getX() + 0.5D,
                    anchor.getY() + 0.5D,
                    anchor.getZ() + 0.5D) <= 64.0D
                    && level.getBlockEntity(anchor) instanceof CommandCenterBlockEntity hall
                    && hall.canUse(player, KingdomPermission.MANAGE_WORKSITES);
        }
        Entity recruit = level.getEntity(snapshot.recruitId());
        return recruit instanceof GalacticRecruitEntity
                && recruit.isAlive()
                && player.distanceToSqr(recruit) <= 64.0D;
    }

    private String configureSettings(
            ServerPlayer player,
            KingdomSavedData data,
            WorksiteRecord worksite,
            WorksiteConfigurationAction action,
            int selectedIndex
    ) {
        WorkAreaConfiguration configuration = worksite.configuration();
        WorkAreaBounds bounds = configuration.bounds();
        boolean kingdomAccess = configuration.kingdomAccess();
        int priority = configuration.priority();
        boolean overlayVisible = configuration.overlayVisible();
        List<String> filters = new ArrayList<>(configuration.itemFilters());
        CourierDispatchMode dispatch = configuration.courierDispatchMode();
        switch (action) {
            case WIDTH_DECREASE -> bounds = new WorkAreaBounds(
                    boundedDimension(bounds.width() - DIMENSION_STEP),
                    bounds.height(), bounds.depth());
            case WIDTH_INCREASE -> bounds = new WorkAreaBounds(
                    boundedDimension(bounds.width() + DIMENSION_STEP),
                    bounds.height(), bounds.depth());
            case HEIGHT_DECREASE -> bounds = new WorkAreaBounds(
                    bounds.width(), boundedDimension(bounds.height() - DIMENSION_STEP),
                    bounds.depth());
            case HEIGHT_INCREASE -> bounds = new WorkAreaBounds(
                    bounds.width(), boundedDimension(bounds.height() + DIMENSION_STEP),
                    bounds.depth());
            case DEPTH_DECREASE -> bounds = new WorkAreaBounds(
                    bounds.width(), bounds.height(),
                    boundedDimension(bounds.depth() - DIMENSION_STEP));
            case DEPTH_INCREASE -> bounds = new WorkAreaBounds(
                    bounds.width(), bounds.height(),
                    boundedDimension(bounds.depth() + DIMENSION_STEP));
            case PRIORITY_DECREASE -> priority = Math.max(0, priority - PRIORITY_STEP);
            case PRIORITY_INCREASE -> priority = Math.min(100, priority + PRIORITY_STEP);
            case TOGGLE_KINGDOM_ACCESS -> kingdomAccess = !kingdomAccess;
            case TOGGLE_OVERLAY -> overlayVisible = !overlayVisible;
            case CYCLE_DISPATCH_MODE -> dispatch = nextDispatchMode(dispatch);
            case ADD_HELD_ITEM_FILTER -> {
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty()) {
                    return "held_item_required";
                }
                addFilter(filters, BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
            }
            case ADD_LOOKED_BLOCK_FILTER -> {
                BlockPos target = lookedAtBlock(player).orElse(null);
                if (target == null || player.level().getBlockState(target).isAir()) {
                    return "block_target_required";
                }
                addFilter(filters, BuiltInRegistries.BLOCK.getKey(
                        player.level().getBlockState(target).getBlock()).toString());
            }
            case ADD_LOOKED_ENTITY_FILTER -> {
                Entity target = lookedAtEntity(player).orElse(null);
                if (!(target instanceof Animal)) {
                    return "animal_target_required";
                }
                addFilter(filters, BuiltInRegistries.ENTITY_TYPE.getKey(
                        target.getType()).toString());
            }
            case REMOVE_FILTER -> {
                if (selectedIndex < 0 || selectedIndex >= filters.size()) {
                    return "filter_selection_required";
                }
                filters.remove(selectedIndex);
            }
            case CLEAR_FILTERS -> filters.clear();
            default -> {
                return "invalid_action";
            }
        }
        WorksiteUpdateResult result = data.configureWorksite(
                player.getUUID(),
                worksite.id(),
                configuration.revision(),
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                filters,
                dispatch);
        return result.reasonCode();
    }

    private String configureRoute(
            ServerPlayer player,
            KingdomSavedData data,
            WorksiteRecord worksite,
            WorksiteConfigurationAction action,
            int selectedIndex
    ) {
        WorkAreaConfiguration configuration = worksite.configuration();
        List<CourierWaypoint> route = new ArrayList<>(configuration.courierRoute());
        CourierRouteMode mode = configuration.courierRouteMode();
        if (action == WorksiteConfigurationAction.CYCLE_ROUTE_MODE) {
            mode = mode == CourierRouteMode.LOOP
                    ? CourierRouteMode.PING_PONG
                    : CourierRouteMode.LOOP;
        } else if (action == WorksiteConfigurationAction.CLEAR_ROUTE) {
            route.clear();
        } else if (action == WorksiteConfigurationAction.REMOVE_ROUTE_WAYPOINT) {
            if (selectedIndex < 0 || selectedIndex >= route.size()) {
                return "route_selection_required";
            }
            route.remove(selectedIndex);
        } else {
            BlockPos target = lookedAtBlock(player).orElse(null);
            KingdomRecord kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            String dimensionId = player.level().dimension().identifier().toString();
            if (target == null || kingdom == null
                    || data.registeredStorageEndpoint(
                            kingdom.ownerId(), dimensionId, target).isEmpty()) {
                return "registered_storage_required";
            }
            if (route.size()
                    >= galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS) {
                return "route_full";
            }
            CourierTransferAction transfer =
                    action == WorksiteConfigurationAction.ADD_ROUTE_TAKE_FROM_LOOK
                            ? CourierTransferAction.takeAll()
                            : CourierTransferAction.putAll();
            route.add(new CourierWaypoint(
                    dimensionId,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    List.of(transfer)));
        }
        return data.configureWorksiteRoute(
                player.getUUID(),
                worksite.id(),
                configuration.revision(),
                route,
                mode).reasonCode();
    }

    private String configureStorage(
            ServerPlayer player,
            KingdomSavedData data,
            WorksiteRecord worksite
    ) {
        BlockPos target = lookedAtBlock(player).orElse(null);
        if (target == null) {
            return "registered_storage_required";
        }
        KingdomRecord kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        String dimensionId = player.level().dimension().identifier().toString();
        galacticwars.clonewars.kingdom.StorageEndpoint endpoint = kingdom == null
                ? null
                : data.registeredStorageEndpoint(
                        kingdom.ownerId(), dimensionId, target).orElse(null);
        if (endpoint == null
                || !(player.level().getBlockEntity(target) instanceof Container)) {
            return "registered_storage_required";
        }
        WorksiteUpdateResult result = data.configureWorksiteStorage(
                player.getUUID(),
                worksite.id(),
                worksite.configuration().revision(),
                endpoint);
        return result.accepted() ? "storage_assigned" : result.reasonCode();
    }

    private Optional<WorksiteRecord> currentWorksite(KingdomSavedData data, Player player) {
        KingdomRecord kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        if (kingdom == null) {
            return Optional.empty();
        }
        return kingdom.settlements().stream()
                .flatMap(settlement -> settlement.worksites().stream())
                .filter(worksite -> worksite.id().equals(snapshot.worksiteId()))
                .findFirst();
    }

    private void refresh(ServerPlayer player, String feedback) {
        Entity entity = ((ServerLevel) player.level()).getEntity(snapshot.recruitId());
        if (entity instanceof GalacticRecruitEntity recruit) {
            try {
                snapshot = capture(player, recruit, feedback);
                GalacticNetwork.CHANNEL.sendToPlayer(
                        () -> player,
                        new WorksiteStatePayload(containerId, snapshot));
            } catch (IllegalStateException missing) {
                // The assignment disappeared between validation and refresh; close safely.
                player.closeContainer();
            }
        } else {
            player.closeContainer();
        }
    }

    private static WorksiteConfigurationSnapshot capture(
            ServerPlayer player,
            GalacticRecruitEntity recruit,
            String feedback
    ) {
        ServerLevel level = (ServerLevel) player.level();
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.kingdomForRecruit(recruit.getUUID()).orElseThrow(
                () -> new IllegalStateException("recruit has no kingdom"));
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.containsRecruit(recruit.getUUID()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("recruit has no settlement"));
        WorksiteRecord worksite = settlement.assignedWorksite(recruit.getUUID()).orElseThrow(
                () -> new IllegalStateException("recruit has no worksite"));
        WorkAreaConfiguration configuration = worksite.configuration();
        WorkerStatus status = recruit.getWorkerStatus();
        List<WorksiteConfigurationSnapshot.RouteWaypointView> route =
                configuration.courierRoute().stream()
                        .map(waypoint -> new WorksiteConfigurationSnapshot.RouteWaypointView(
                                waypoint.x(),
                                waypoint.y(),
                                waypoint.z(),
                                waypoint.actions().isEmpty()
                                        ? "none"
                                        : waypoint.actions().getFirst().effectiveType().id(),
                                waypoint.actions().size()))
                        .toList();
        List<WorksiteConfigurationSnapshot.AssignedWorkerView> workers =
                worksite.assignmentIds().stream().map(recruitId -> {
                    Entity loaded = level.getEntity(recruitId);
                    if (loaded instanceof GalacticRecruitEntity assigned) {
                        WorkerStatus assignedStatus = assigned.getWorkerStatus();
                        return new WorksiteConfigurationSnapshot.AssignedWorkerView(
                                recruitId,
                                assigned.getDisplayName().getString(),
                                assigned.getWorkerProfession().map(value -> value.id())
                                        .orElse("unassigned"),
                                assignedStatus.phase().id(),
                                assignedStatus.reasonCode());
                    }
                    return new WorksiteConfigurationSnapshot.AssignedWorkerView(
                            recruitId,
                            recruitId.toString().substring(0, 8),
                            worksite.type(),
                            "unloaded",
                            "chunk_unloaded");
                }).toList();
        return new WorksiteConfigurationSnapshot(
                recruit.getId(),
                recruit.getUUID(),
                recruit.getDisplayName().getString(),
                worksite.id(),
                recruit.getWorkerProfession().orElseThrow().id(),
                worksite.dimensionId(),
                worksite.x(),
                worksite.y(),
                worksite.z(),
                configuration.bounds(),
                configuration.kingdomAccess(),
                configuration.priority(),
                configuration.overlayVisible(),
                configuration.itemFilters(),
                configuration.courierDispatchMode(),
                configuration.courierRouteMode(),
                configuration.revision(),
                configuration.courierRouteRevision(),
                route,
                worksite.storageEndpoints().stream()
                        .filter(endpoint -> endpoint.dimensionId().equals(worksite.dimensionId()))
                        .findFirst()
                        .map(endpoint -> new BlockPos(
                                endpoint.x(), endpoint.y(), endpoint.z()))
                        .or(() -> Optional.ofNullable(recruit.getStorageTarget())),
                workers,
                status.phase().id(),
                status.reasonCode(),
                status.requiredResource(),
                status.completedQuantity(),
                status.totalQuantity(),
                feedback);
    }

    private static Optional<BlockPos> lookedAtBlock(ServerPlayer player) {
        HitResult hit = player.pick(8.0D, 1.0F, false);
        return hit instanceof BlockHitResult blockHit
                && hit.getType() == HitResult.Type.BLOCK
                ? Optional.of(blockHit.getBlockPos().immutable())
                : Optional.empty();
    }

    private static Optional<Entity> lookedAtEntity(ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(8.0D));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                player.getBoundingBox()
                        .expandTowards(end.subtract(start))
                        .inflate(1.0D),
                candidate -> candidate != player
                        && candidate.isAlive()
                        && candidate.isPickable(),
                64.0D);
        return hit == null ? Optional.empty() : Optional.of(hit.getEntity());
    }

    private static void addFilter(List<String> filters, String filter) {
        if (!filters.contains(filter)
                && filters.size() < WorkAreaConfiguration.MAX_ITEM_FILTERS) {
            filters.add(filter);
        }
    }

    private static int boundedDimension(int value) {
        return Math.max(1, Math.min(64, value));
    }

    private static CourierDispatchMode nextDispatchMode(CourierDispatchMode current) {
        CourierDispatchMode[] values = CourierDispatchMode.values();
        return values[(current.ordinal() + 1) % values.length];
    }
}
