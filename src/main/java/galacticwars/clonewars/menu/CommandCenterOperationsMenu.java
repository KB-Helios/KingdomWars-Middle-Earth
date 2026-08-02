package galacticwars.clonewars.menu;

import dev.architectury.registry.menu.MenuRegistry;
import galacticwars.clonewars.army.ArmyLocation;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyGroupOrder;
import galacticwars.clonewars.army.ArmySupplyPolicy;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.kingdom.KingdomMemberRole;
import galacticwars.clonewars.kingdom.KingdomRelation;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.kingdom.BuildProject;
import galacticwars.clonewars.kingdom.BuildProjectState;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.ActionAvailability;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.CombatTargetSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.NearbyPlayerSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.PositionSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.StockRequirementSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.VehicleFabricationSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.WorkerSummary;
import galacticwars.clonewars.kingdom.CommandTargetResolver;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.item.CommandTargetSelection;
import galacticwars.clonewars.kingdom.SettlementRecord;
import galacticwars.clonewars.network.CommandCenterStatePayload;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.progression.LaunchContentCatalog;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.registry.ModDataComponents;
import galacticwars.clonewars.registry.ModMenuTypes;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.settlement.ConstructionPlan;
import galacticwars.clonewars.settlement.ConstructionProjectService;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.vehicle.VehicleFabricationService;
import galacticwars.clonewars.world.PlanetTravelService;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public final class CommandCenterOperationsMenu extends AbstractContainerMenu {
    public static final int STORAGE = 0;
    public static final int CLAIM_REWARDS = 1;
    public static final int NAVIGATION = 2;
    public static final int FABRICATE_FIRST = 10;
    public static final int MERGE_SQUADS = 20;
    public static final int PATROL_SQUAD = 21;
    public static final int CREATE_SQUAD = 22;
    public static final int SPLIT_SQUAD = 23;
    public static final int CONFIGURE_SQUAD = 24;
    public static final int CYCLE_FORMATION = 25;
    public static final int SUPPLY_SQUAD = 26;
    public static final int FOLLOW_SQUAD = 27;
    public static final int HOLD_SQUAD = 68;
    public static final int MOVE_SQUAD = 69;
    public static final int PROTECT_SQUAD = 70;
    public static final int CLEAR_SQUAD_TARGET = 71;
    public static final int ATTACK_SQUAD_TARGET = 72;
    public static final int SET_SQUAD_RALLY = 73;
    public static final int RESUME_WORKER = 74;
    public static final int RECALL_WORKER = 75;
    public static final int PAUSE_WORKER = 76;
    public static final int CONFIGURE_WORKSITE = 77;
    public static final int PLAYER_CLASS = 90;
    public static final int REGISTER_OUTPOST = 30;
    public static final int INVITE_NEAREST = 31;
    public static final int ACCEPT_INVITE = 32;
    public static final int REJECT_INVITE = 35;
    public static final int CYCLE_MEMBER_ROLE = 33;
    public static final int REMOVE_MEMBER = 34;
    public static final int PROPOSE_ALLIANCE = 40;
    public static final int ACCEPT_DIPLOMACY = 41;
    public static final int REJECT_DIPLOMACY = 44;
    public static final int DECLARE_HOSTILITY = 42;
    public static final int TOGGLE_EMBARGO = 43;
    public static final int PREPARE_PROJECTOR_FIRST = 50;
    public static final int CANCEL_BUILD_PROJECT = 54;
    private static final double INVITE_RANGE_SQUARED = 16.0D * 16.0D;
    private final BlockPos hallPos;
    private final LinkedHashSet<UUID> processedActionIds = new LinkedHashSet<>();
    private CommandCenterDashboardState dashboardState;
    private int dashboardRevision;

    public BlockPos hallPos() {
        return hallPos;
    }

    public CommandCenterOperationsMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), CommandCenterDashboardCodec.read(buffer));
    }

    CommandCenterOperationsMenu(int id, Inventory inventory, BlockPos hallPos) {
        this(id, inventory, hallPos, captureDashboard(inventory, hallPos));
    }

    private CommandCenterOperationsMenu(
            int id,
            Inventory inventory,
            BlockPos hallPos,
            CommandCenterDashboardState dashboardState
    ) {
        super(ModMenuTypes.COMMAND_CENTER_OPERATIONS.get(), id);
        this.hallPos = hallPos.immutable();
        this.dashboardState = dashboardState;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return false;
    }

    public boolean handleReplayAction(ServerPlayer player, UUID replayId, int buttonId) {
        return handleReplayAction(player, replayId, buttonId, Optional.empty(), Optional.empty());
    }

    public boolean handleReplayAction(
            ServerPlayer player,
            UUID replayId,
            int buttonId,
            Optional<UUID> primaryTargetId,
            Optional<UUID> secondaryTargetId
    ) {
        return handleReplayAction(
                player, replayId, buttonId, primaryTargetId, secondaryTargetId, -1L, -1);
    }

    public boolean handleReplayAction(
            ServerPlayer player,
            UUID replayId,
            int buttonId,
            Optional<UUID> primaryTargetId,
            Optional<UUID> secondaryTargetId,
            long expectedContentGeneration,
            int expectedSettlementRevision
    ) {
        if (!processedActionIds.add(replayId)) return false;
        while (processedActionIds.size() > 64) {
            processedActionIds.remove(processedActionIds.iterator().next());
        }
        String staleReason = staleReason(
                player, expectedContentGeneration, expectedSettlementRevision);
        if (!staleReason.isEmpty()) {
            boolean result = report(player, false, staleReason);
            if (player.containerMenu == this) {
                refreshDashboard(player);
            }
            return result;
        }
        boolean result = executeAction(player, buttonId, primaryTargetId, secondaryTargetId);
        if (player.containerMenu == this) {
            refreshDashboard(player);
        }
        return result;
    }

    private String staleReason(
            ServerPlayer player,
            long expectedContentGeneration,
            int expectedSettlementRevision
    ) {
        if (expectedContentGeneration >= 0L
                && expectedContentGeneration != GameplayDataManager.generation()) {
            return "content_changed";
        }
        if (expectedSettlementRevision < 0) {
            return "";
        }
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall)
                || hall.ownerId() == null) {
            return "stale_state";
        }
        int currentRevision = KingdomSavedData.get(level).kingdomForOwner(hall.ownerId())
                .map(kingdom -> kingdom.settlement().revision()).orElse(-1);
        return currentRevision == expectedSettlementRevision ? "" : "stale_state";
    }

    private boolean executeAction(
            Player player,
            int buttonId,
            Optional<UUID> primaryTargetId,
            Optional<UUID> secondaryTargetId
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level() instanceof ServerLevel level)
                || !(level.getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall)
                || !stillValid(player)) return false;
        KingdomSavedData data = KingdomSavedData.get(level);
        boolean inviteResponse = buttonId == ACCEPT_INVITE || buttonId == REJECT_INVITE;
        if (!hall.canOpen(player) && !inviteResponse) {
            return report(serverPlayer, false, "permission_denied");
        }
        boolean success;
        String reason = "action_rejected";
        if (buttonId == STORAGE) {
            serverPlayer.openMenu(hall);
            return true;
        } else if (buttonId == NAVIGATION) {
            if (!hall.canUse(player, galacticwars.clonewars.kingdom.KingdomPermission.TRAVEL)) {
                return report(serverPlayer, false, "permission_denied");
            }
            MenuRegistry.openExtendedMenu(
                    serverPlayer, new CommandCenterNavigationMenuProvider(serverPlayer));
            return true;
        } else if (buttonId == CLAIM_REWARDS) {
            success = ProgressionSavedData.get(level).claimCreditRewards(serverPlayer) > 0;
            if (!success) reason = "no_pending_rewards";
        } else if (buttonId >= FABRICATE_FIRST
                && buttonId < FABRICATE_FIRST + vehicleIds().size()) {
            reason = VehicleFabricationService.fabricate(serverPlayer, hall,
                    vehicleIds().get(buttonId - FABRICATE_FIRST));
            success = reason.equals("accepted");
        } else if (buttonId == MERGE_SQUADS) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            var groups = kingdom == null ? List.<galacticwars.clonewars.army.ArmyGroupRecord>of()
                    : data.armyGroupsForKingdom(kingdom.id());
            var targetGroup = selectTarget(groups, primaryTargetId,
                    galacticwars.clonewars.army.ArmyGroupRecord::id);
            var sourceCandidates = targetGroup == null ? List.<galacticwars.clonewars.army.ArmyGroupRecord>of()
                    : groups.stream().filter(group -> !group.id().equals(targetGroup.id())).toList();
            var sourceGroup = selectTarget(sourceCandidates, secondaryTargetId,
                    galacticwars.clonewars.army.ArmyGroupRecord::id);
            success = targetGroup != null && sourceGroup != null && data.mergeArmyGroups(
                    player.getUUID(), targetGroup.id(), sourceGroup.id());
            if (!success && (targetGroup == null || sourceGroup == null)) {
                reason = selectionReason(groups.size() >= 2,
                        primaryTargetId.isEmpty() || secondaryTargetId.isEmpty());
            }
        } else if (buttonId == PATROL_SQUAD) {
            var groups = data.kingdomForPlayer(player.getUUID())
                    .map(kingdom -> data.armyGroupsForKingdom(kingdom.id())).orElse(List.of());
            var group = selectTarget(groups, primaryTargetId,
                    galacticwars.clonewars.army.ArmyGroupRecord::id);
            ArmyLocation center = selectedCommandLocation(level, serverPlayer)
                    .orElseGet(() -> new ArmyLocation(level.dimension().identifier().toString(),
                            player.getX(), player.getY(), player.getZ()));
            success = group != null && data.startArmyPatrol(player.getUUID(), group.id(), center, List.of(
                    center, new ArmyLocation(center.dimensionId(), center.x() + 16, center.y(), center.z()),
                    new ArmyLocation(center.dimensionId(), center.x() + 16, center.y(), center.z() + 16),
                    new ArmyLocation(center.dimensionId(), center.x(), center.y(), center.z() + 16)));
            if (!success && group == null) {
                reason = selectionReason(!groups.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if ((buttonId >= CREATE_SQUAD && buttonId <= FOLLOW_SQUAD)
                || isDirectSquadAction(buttonId)) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            var groups = kingdom == null ? List.<galacticwars.clonewars.army.ArmyGroupRecord>of()
                    : data.armyGroupsForKingdom(kingdom.id());
            var group = selectTarget(groups, primaryTargetId,
                    galacticwars.clonewars.army.ArmyGroupRecord::id);
            ArmyLocation here = new ArmyLocation(level.dimension().identifier().toString(),
                    player.getX(), player.getY(), player.getZ());
            ArmyLocation markedTarget = selectedCommandLocation(level, serverPlayer).orElse(here);
            if (buttonId == CREATE_SQUAD) {
                List<UUID> candidates = kingdom == null ? List.of() : kingdom.npcRoster().stream()
                        .filter(npc -> npc.serviceBranch()
                                == galacticwars.clonewars.recruitment.NpcServiceBranch.MILITARY)
                        .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                        .filter(id -> level.getEntity(id) != null)
                        .filter(id -> data.armyGroupForRecruit(id).isEmpty()).toList();
                UUID commander = selectTarget(candidates, primaryTargetId, Function.identity());
                success = commander != null && data.promoteCommander(player.getUUID(), commander)
                        && data.createOrReclaimArmyGroup(player.getUUID(), commander,
                        ArmyFormation.LINE, here, level.getGameTime()).isPresent();
                if (!success && commander == null) {
                    reason = selectionReason(!candidates.isEmpty(), primaryTargetId.isEmpty());
                }
            } else if (buttonId == SPLIT_SQUAD) {
                List<UUID> candidates = group == null ? List.of() : group.memberIds();
                UUID commander = selectTarget(candidates, secondaryTargetId, Function.identity());
                List<UUID> transferred = group == null ? List.of()
                        : group.memberIds().stream().skip(1)
                        .limit(Math.max(0, group.memberIds().size() / 2)).toList();
                success = commander != null && data.splitArmyGroup(player.getUUID(), group.id(), commander,
                        transferred, "Split Squad", here, level.getGameTime()).isPresent();
                if (!success && (group == null || commander == null)) {
                    reason = selectionReason(!groups.isEmpty() && !candidates.isEmpty(),
                            primaryTargetId.isEmpty() || secondaryTargetId.isEmpty());
                }
            } else if (buttonId == CONFIGURE_SQUAD) {
                List<galacticwars.clonewars.kingdom.KingdomClaim> claims = kingdom == null
                        ? List.of() : kingdom.claims();
                var claim = selectTarget(claims, secondaryTargetId,
                        galacticwars.clonewars.kingdom.KingdomClaim::id);
                boolean ambiguousClaim = claims.size() > 1 && secondaryTargetId.isEmpty();
                success = !ambiguousClaim && group != null && data.configureArmyGroup(player.getUUID(), group.id(),
                        "Squad at " + player.getBlockX() + "," + player.getBlockZ(), here,
                        group.patrolRoute(), Optional.ofNullable(claim).map(
                                galacticwars.clonewars.kingdom.KingdomClaim::id));
                if (ambiguousClaim) {
                    reason = "selection_required";
                }
            } else if (buttonId == CYCLE_FORMATION) {
                ArmyFormation formation = group == null ? ArmyFormation.LINE
                        : nextFormation(group.order().formation());
                success = group != null && data.issueArmyOrder(player.getUUID(), group.id(),
                        group.order().withFormation(formation));
            } else if (buttonId == SUPPLY_SQUAD) {
                int supplyCellSlot = findSupplyCellSlot(hall);
                int suppliedUnits = kingdom == null ? 0 : ArmySupplyPolicy.unitsPerEnergyCell(
                        FactionBalanceService.resolve(kingdom.factionId()).supplyEfficiencyPercent());
                if (group == null) {
                    success = false;
                } else if (supplyCellSlot < 0) {
                    success = false;
                    reason = "supply_cell_required";
                } else if (data.changeArmySupply(
                        player.getUUID(), group.id(), suppliedUnits)) {
                    success = true;
                    reason = "squad_supplied";
                    consumeSupplyCell(hall, supplyCellSlot);
                } else {
                    success = false;
                    reason = "supply_update_rejected";
                }
            } else if (buttonId == HOLD_SQUAD || buttonId == MOVE_SQUAD) {
                ArmyCommandType type = buttonId == HOLD_SQUAD
                        ? ArmyCommandType.HOLD_POSITION : ArmyCommandType.MOVE_TO_POSITION;
                ArmyLocation orderTarget = buttonId == HOLD_SQUAD && group != null
                        ? group.simulation().anchor() : markedTarget;
                success = group != null && data.issueArmyOrder(player.getUUID(), group.id(),
                        new ArmyGroupOrder(type, Optional.of(orderTarget), Optional.empty(),
                                group.order().formation(), group.order().spacing()));
            } else if (buttonId == PROTECT_SQUAD || buttonId == CLEAR_SQUAD_TARGET) {
                ArmyCommandType type = buttonId == PROTECT_SQUAD
                        ? ArmyCommandType.PROTECT_OWNER : ArmyCommandType.CLEAR_TARGET;
                success = group != null && data.issueArmyOrder(player.getUUID(), group.id(),
                        new ArmyGroupOrder(type, Optional.empty(), Optional.empty(),
                                group.order().formation(), group.order().spacing()));
            } else if (buttonId == ATTACK_SQUAD_TARGET) {
                List<CombatTargetSummary> targets = kingdom == null
                        ? List.of() : nearbyCombatTargets(level, player, kingdom);
                CombatTargetSummary target = selectTarget(
                        targets, secondaryTargetId, CombatTargetSummary::entityId);
                LivingEntity targetEntity = target == null ? null
                        : level.getEntity(target.entityId()) instanceof LivingEntity living ? living : null;
                success = group != null && targetEntity != null && targetEntity.isAlive()
                        && !group.contains(targetEntity.getUUID())
                        && data.issueArmyOrder(player.getUUID(), group.id(),
                        new ArmyGroupOrder(ArmyCommandType.ATTACK_TARGET,
                                Optional.of(new ArmyLocation(
                                        level.dimension().identifier().toString(),
                                        targetEntity.getX(), targetEntity.getY(), targetEntity.getZ())),
                                Optional.of(targetEntity.getUUID()), group.order().formation(),
                                group.order().spacing()));
                if (!success && target == null) {
                    reason = selectionReason(!targets.isEmpty(), secondaryTargetId.isEmpty());
                }
            } else if (buttonId == SET_SQUAD_RALLY) {
                success = group != null && data.configureArmyGroup(
                        player.getUUID(), group.id(), group.name(), markedTarget,
                        group.patrolRoute(), group.defendedClaimId());
            } else {
                success = group != null && data.issueArmyOrder(player.getUUID(), group.id(),
                        ArmyGroupOrder.follow(group.order().formation()));
            }
            if (!success && reason.equals("action_rejected") && group == null
                    && buttonId != CREATE_SQUAD) {
                reason = selectionReason(!groups.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if (buttonId == CONFIGURE_WORKSITE) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            if (kingdom == null || !hall.canUse(player,
                    galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES)) {
                return report(serverPlayer, false, "permission_denied");
            }
            List<WorkerSummary> workers = loadedWorkers(level, player, kingdom);
            WorkerSummary selected = selectTarget(
                    workers, primaryTargetId, WorkerSummary::entityId);
            galacticwars.clonewars.entity.GalacticRecruitEntity worker =
                    selected == null ? null
                            : level.getEntity(selected.entityId())
                                    instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit
                                            ? recruit
                                            : null;
            if (worker == null) {
                return report(serverPlayer, false, selectionReason(
                        !workers.isEmpty(), primaryTargetId.isEmpty()));
            }
            if (WorksiteConfigurationMenu.captureSnapshot(serverPlayer, worker).isEmpty()) {
                return report(serverPlayer, false, "worksite_missing");
            }
            MenuRegistry.openExtendedMenu(
                    serverPlayer,
                    new WorksiteConfigurationMenuProvider(worker, hallPos));
            return true;
        } else if (buttonId == RESUME_WORKER
                || buttonId == RECALL_WORKER
                || buttonId == PAUSE_WORKER) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            if (kingdom == null || !hall.canUse(player,
                    galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES)) {
                return report(serverPlayer, false, "permission_denied");
            }
            List<WorkerSummary> workers = loadedWorkers(level, player, kingdom);
            WorkerSummary selected = selectTarget(workers, primaryTargetId, WorkerSummary::entityId);
            galacticwars.clonewars.entity.GalacticRecruitEntity worker = selected == null ? null
                    : level.getEntity(selected.entityId())
                    instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit ? recruit : null;
            success = worker != null && switch (buttonId) {
                case RESUME_WORKER -> worker.resumeWorkFromCommandCenter(serverPlayer);
                case RECALL_WORKER -> worker.recallWorkerToCommandCenter(serverPlayer, hallPos);
                case PAUSE_WORKER -> worker.pauseWorkerFromCommandCenter(serverPlayer);
                default -> false;
            };
            if (!success && worker == null) {
                reason = selectionReason(!workers.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if (buttonId >= PREPARE_PROJECTOR_FIRST
                && buttonId < PREPARE_PROJECTOR_FIRST + 4) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            if (kingdom == null
                    || !hall.canUse(player, galacticwars.clonewars.kingdom.KingdomPermission.BUILD)
                    || !hall.canUse(player,
                    galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES)) {
                return report(serverPlayer, false, "permission_denied");
            }
            var blueprints = List.copyOf(GameplayDataManager.snapshot().blueprints().values());
            var blueprint = selectTarget(blueprints, primaryTargetId,
                    value -> CommandCenterDashboardState.BlueprintSummary.targetId(value.id()));
            List<UUID> builders = kingdom.npcRoster().stream()
                    .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                    .filter(id -> level.getEntity(id)
                            instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit
                            && recruit.isAlive()
                            && recruit.getRecruitDuty()
                            != galacticwars.clonewars.recruitment.RecruitDuty.COMMANDER)
                    .toList();
            UUID builderId = selectTarget(builders, secondaryTargetId, Function.identity());
            int rotationSteps = buttonId - PREPARE_PROJECTOR_FIRST;
            if (blueprint == null || builderId == null) {
                return report(serverPlayer, false, selectionReason(
                        !blueprints.isEmpty() && !builders.isEmpty(),
                        primaryTargetId.isEmpty() || secondaryTargetId.isEmpty()));
            }
            if (!blueprint.supportsRotationSteps(rotationSteps)) {
                return report(serverPlayer, false, "rotation_unavailable");
            }
            ItemStack projector = ItemStack.EMPTY;
            for (int slot = 0; slot < serverPlayer.getInventory().getContainerSize(); slot++) {
                ItemStack candidate = serverPlayer.getInventory().getItem(slot);
                if (candidate.is(ModItems.BLUEPRINT_PROJECTOR.get())) {
                    projector = candidate;
                    break;
                }
            }
            if (projector.isEmpty()) {
                projector = new ItemStack(ModItems.BLUEPRINT_PROJECTOR.get());
                projector.set(ModDataComponents.CONSTRUCTION_PLAN.get(), new ConstructionPlan(
                        blueprint.id(), rotationSteps, builderId, kingdom.id()));
                if (!serverPlayer.getInventory().add(projector)) {
                    serverPlayer.drop(projector, false);
                }
            } else {
                projector.set(ModDataComponents.CONSTRUCTION_PLAN.get(), new ConstructionPlan(
                        blueprint.id(), rotationSteps, builderId, kingdom.id()));
            }
            report(serverPlayer, true, "projector_prepared");
            serverPlayer.closeContainer();
            return true;
        } else if (buttonId == CANCEL_BUILD_PROJECT) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            List<BuildProject> projects = kingdom == null ? List.of()
                    : kingdom.settlement().buildProjects().stream()
                    .filter(project -> project.state() == BuildProjectState.ACTIVE
                            || project.state() == BuildProjectState.BLOCKED)
                    .toList();
            BuildProject project = selectTarget(projects, primaryTargetId, BuildProject::id);
            var cancelled = project == null
                    ? null : ConstructionProjectService.cancel(level, serverPlayer, project.id());
            success = cancelled != null && cancelled.accepted();
            if (cancelled != null) {
                reason = cancelled.reason();
            } else {
                reason = selectionReason(!projects.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if (buttonId == REGISTER_OUTPOST) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            List<BuildProject> candidates = kingdom == null ? List.of()
                    : kingdom.settlement().buildProjects().stream()
                    .filter(candidate -> candidate.state() == BuildProjectState.COMPLETED)
                    .filter(candidate -> KingdomBaseBlueprint.path(candidate.blueprintId())
                            .equals("forward_base"))
                    .filter(candidate -> candidate.dimensionId()
                            .equals(level.dimension().identifier().toString()))
                    .filter(candidate -> kingdom.settlements().stream().noneMatch(settlement ->
                            settlement.dimensionId().equals(candidate.dimensionId())
                                    && settlement.hallX() == candidate.originX()
                                    && settlement.hallY() == candidate.originY()
                                    && settlement.hallZ() == candidate.originZ()))
                    .filter(candidate -> completedProjectStillExists(level, candidate))
                    .toList();
            BuildProject project = selectTarget(candidates, primaryTargetId, BuildProject::id);
            success = project != null && data.addOutpost(player.getUUID(), SettlementRecord.create(
                    project.dimensionId(), project.originX(), project.originY(), project.originZ())).isPresent();
            if (!success) {
                reason = candidates.size() > 1 && primaryTargetId.isEmpty()
                        ? "selection_required" : "completed_forward_base_required";
            }
        } else if (buttonId == INVITE_NEAREST) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            List<ServerPlayer> candidates = nearbyInvitePlayers(
                    level, serverPlayer, kingdom, data);
            ServerPlayer target = selectTarget(candidates, primaryTargetId, ServerPlayer::getUUID);
            success = target != null && data.inviteMember(player.getUUID(), target.getUUID(),
                    KingdomMemberRole.MEMBER, level.getGameTime() + 12000L).isPresent();
            if (!success && target == null) {
                reason = selectionReason(!candidates.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if (buttonId == ACCEPT_INVITE || buttonId == REJECT_INVITE) {
            var invites = data.pendingInvites().stream()
                    .filter(candidate -> candidate.targetPlayerId().equals(player.getUUID()))
                    .filter(candidate -> candidate.expiresGameTime() >= level.getGameTime()).toList();
            var invite = selectTarget(invites, primaryTargetId,
                    galacticwars.clonewars.kingdom.KingdomInvite::id);
            String faction = ProgressionSavedData.get(level).state(player.getUUID()).factionId();
            success = invite != null && data.respondToInvite(player.getUUID(), invite.id(),
                    buttonId == ACCEPT_INVITE,
                    faction, level.getGameTime());
            if (!success && invite == null) {
                reason = selectionReason(!invites.isEmpty(), primaryTargetId.isEmpty());
            }
        } else if (buttonId == CYCLE_MEMBER_ROLE || buttonId == REMOVE_MEMBER) {
            var kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            var members = kingdom == null ? List.<galacticwars.clonewars.kingdom.KingdomMember>of()
                    : kingdom.members().stream()
                    .filter(candidate -> !candidate.playerId().equals(kingdom.ownerId())
                            && !candidate.playerId().equals(player.getUUID())).toList();
            var member = selectTarget(members, primaryTargetId,
                    galacticwars.clonewars.kingdom.KingdomMember::playerId);
            success = member != null && (buttonId == REMOVE_MEMBER
                    ? data.removeMember(player.getUUID(), member.playerId())
                    : data.updateMemberRole(player.getUUID(), member.playerId(), nextRole(member.role())));
            if (!success && member == null) {
                reason = selectionReason(!members.isEmpty(), primaryTargetId.isEmpty());
            }
        } else {
            var own = data.kingdomForPlayer(player.getUUID()).orElse(null);
            var others = data.kingdoms().stream()
                    .filter(kingdom -> own != null && !kingdom.id().equals(own.id())).toList();
            var other = selectTarget(others, primaryTargetId,
                    galacticwars.clonewars.kingdom.KingdomRecord::id);
            if (buttonId == ACCEPT_DIPLOMACY || buttonId == REJECT_DIPLOMACY) {
                var proposals = data.pendingDiplomacy().stream()
                        .filter(candidate -> own != null && candidate.targetKingdomId().equals(own.id()))
                        .filter(candidate -> candidate.expiresGameTime() >= level.getGameTime()).toList();
                var proposal = selectTarget(proposals, primaryTargetId,
                        galacticwars.clonewars.kingdom.DiplomacyProposal::id);
                success = proposal != null && data.respondToDiplomacy(player.getUUID(), proposal.id(),
                        buttonId == ACCEPT_DIPLOMACY,
                        level.getGameTime(), 2400L);
                if (!success && proposal == null) {
                    reason = selectionReason(!proposals.isEmpty(), primaryTargetId.isEmpty());
                }
            } else if (buttonId == PROPOSE_ALLIANCE) {
                success = other != null && data.proposeAlliance(player.getUUID(), other.id(),
                        level.getGameTime(), 24000L, 12000L).isPresent();
            } else if (buttonId == DECLARE_HOSTILITY) {
                success = other != null && data.setRelation(player.getUUID(), other.id(),
                        KingdomRelation.ENEMY, level.getGameTime(), 2400L);
            } else if (buttonId == TOGGLE_EMBARGO) {
                success = other != null && data.setEmbargo(player.getUUID(), other.id(), true,
                        level.getGameTime(), 2400L);
            } else return false;
            if (!success && reason.equals("action_rejected") && other == null
                    && buttonId != ACCEPT_DIPLOMACY && buttonId != REJECT_DIPLOMACY) {
                reason = selectionReason(!others.isEmpty(), primaryTargetId.isEmpty());
            }
        }
        if (success && reason.equals("action_rejected")) reason = "accepted";
        return report(serverPlayer, success, reason);
    }

    public CommandCenterDashboardState dashboardState() {
        return dashboardState;
    }

    public void applyClientDashboard(CommandCenterDashboardState state) {
        if (state.generatedGameTime() >= dashboardState.generatedGameTime()) {
            this.dashboardState = state;
            this.dashboardRevision++;
        }
    }

    public int dashboardRevision() {
        return dashboardRevision;
    }

    private void refreshDashboard(ServerPlayer player) {
        this.dashboardState = captureDashboard(player.getInventory(), hallPos);
        GalacticNetwork.CHANNEL.sendToPlayer(
                () -> player,
                new CommandCenterStatePayload(containerId, dashboardState));
    }

    private static CommandCenterDashboardState captureDashboard(
            Inventory inventory,
            BlockPos hallPos
    ) {
        Player player = inventory.player;
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(serverPlayer.level() instanceof ServerLevel level)
                || !(level.getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall)
                || hall.ownerId() == null) {
            return CommandCenterDashboardState.empty(player.getUUID(),
                    Math.max(0L, player.level().getGameTime()));
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        var kingdom = data.kingdomForOwner(hall.ownerId()).orElse(null);
        if (kingdom == null) {
            return CommandCenterDashboardState.empty(player.getUUID(), level.getGameTime());
        }
        List<UUID> commandCandidates = kingdom.npcRoster().stream()
                .filter(npc -> npc.serviceBranch()
                        == galacticwars.clonewars.recruitment.NpcServiceBranch.MILITARY)
                .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                .filter(id -> level.getEntity(id) != null)
                .filter(id -> data.armyGroupForRecruit(id).isEmpty())
                .toList();
        List<UUID> constructionBuilders = kingdom.npcRoster().stream()
                .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                .filter(id -> level.getEntity(id) instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit
                        && recruit.isAlive()
                        && recruit.getRecruitDuty()
                                != galacticwars.clonewars.recruitment.RecruitDuty.COMMANDER)
                .toList();
        var navigationOptions = PlanetTravelService.navigationOptions(serverPlayer);
        ActionAvailability navigationAvailability = navigationOptions.stream()
                .filter(option -> option.available())
                .findFirst()
                .map(option -> ActionAvailability.accepted())
                .orElseGet(() -> navigationOptions.stream()
                        .filter(option -> !option.destinationId().equals(
                                PlanetTravelService.HOME_DESTINATION_ID))
                        .findFirst()
                        .or(() -> navigationOptions.stream().findFirst())
                        .map(option -> ActionAvailability.rejected(option.reason()))
                        .orElseGet(() -> ActionAvailability.rejected("destination_unavailable")));
        List<VehicleFabricationSummary> fabrication = vehicleIds().stream().map(vehicleId -> {
            var assessment = VehicleFabricationService.assess(serverPlayer, hall, vehicleId);
            return new VehicleFabricationSummary(
                    assessment.vehicleId(),
                    new ActionAvailability(assessment.available(), assessment.reason()),
                    assessment.requiredCredits(),
                    assessment.materials().stream().map(material ->
                            new StockRequirementSummary(
                                    material.itemId(), material.required(), material.available()))
                            .toList());
        }).toList();
        return CommandCenterDashboardState.capture(
                player.getUUID(), kingdom, data.armyGroupsForKingdom(kingdom.id()), commandCandidates,
                nearbyCombatTargets(level, player, kingdom),
                loadedWorkers(level, player, kingdom),
                constructionBuilders,
                List.copyOf(GameplayDataManager.snapshot().blueprints().values()),
                nearbyInvitePlayers(level, serverPlayer, kingdom, data).stream()
                        .map(target -> new NearbyPlayerSummary(
                                target.getUUID(), target.getDisplayName().getString(),
                                Math.max(0, (int) Math.round(Math.sqrt(
                                        player.distanceToSqr(target))))))
                        .toList(),
                ProgressionSavedData.get(level).state(player.getUUID()), hall.treasuryCredits(),
                hall.upkeepPaid(), navigationAvailability, fabrication,
                data.kingdoms(), data.pendingInvites(), data.pendingDiplomacy(),
                conflictSummaries(serverPlayer, kingdom, data),
                level.getGameTime());
    }

    private static List<galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary>
            conflictSummaries(
                    ServerPlayer player,
                    galacticwars.clonewars.kingdom.KingdomRecord kingdom,
                    KingdomSavedData kingdoms
            ) {
         java.util.ArrayList<galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary>
                 result = new java.util.ArrayList<>();
         galacticwars.clonewars.progression.MissionAttemptSavedData.get(player.level().getServer().overworld())
                 .forPlayer(player.getUUID()).ifPresent(attempt -> {
                     if (!attempt.phase().equals("complete")) {
                         long endsAt = attempt.phase().equals("cooldown")
                                 ? attempt.retryAt()
                                 : attempt.phase().equals("hold")
                                 ? player.level().getServer().overworld().getGameTime()
                                 + Math.max(0, 200 - attempt.holdTicks()) : 0L;
                         result.add(new galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary(
                                 attempt.missionId(), "campaign_mission", attempt.targetDimension(),
                                 attempt.target().getX(), attempt.target().getZ(), attempt.phase(),
                                 attempt.holdTicks(), 200, endsAt,
                                 kingdom.factionId(), attempt.failureReason()));
                     }
                 });
         for (galacticwars.clonewars.kingdom.KingdomSiege siege : kingdoms.sieges()) {
            if (!siege.attackerKingdomId().equals(kingdom.id())
                    && !siege.defenderKingdomId().equals(kingdom.id())) {
                continue;
            }
            galacticwars.clonewars.kingdom.KingdomClaim claim = kingdoms
                    .kingdom(siege.defenderKingdomId()).stream()
                    .flatMap(record -> record.claims().stream())
                    .filter(candidate -> candidate.id().equals(siege.claimId()))
                    .findFirst().orElse(null);
            if (claim == null) {
                continue;
            }
            result.add(new galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary(
                    siege.id().toString(), "kingdom_siege", claim.dimensionId(),
                    (claim.center().x() << 4) + 8, (claim.center().z() << 4) + 8,
                    siege.state().name().toLowerCase(java.util.Locale.ROOT),
                    siege.captureProgress(), siege.captureGoal(), 0L,
                    siege.attackerKingdomId().toString(), siege.defenderKingdomId().toString()));
        }
        for (var region : LaunchContentCatalog.data().conquestRegions().values()) {
            var planet = LaunchContentCatalog.data().planets().get(region.planetId());
            if (planet == null) {
                continue;
            }
            net.minecraft.server.level.ServerLevel level;
            try {
                level = player.level().getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        net.minecraft.resources.Identifier.parse(planet.dimensionId())));
            } catch (RuntimeException invalidDimension) {
                continue;
            }
            if (level == null) {
                continue;
            }
            var state = galacticwars.clonewars.conquest.ConquestSavedData.get(level)
                    .state(region.id()).orElse(null);
            if (state == null || !state.controllingKingdom().equals(kingdom.id().toString())
                    || state.counterattackAt() <= 0L) {
                continue;
            }
            result.add(new galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary(
                    state.regionId(), "conquest_counterattack", state.dimensionId(),
                    state.beaconX(), state.beaconZ(),
                    state.counterattackActive() ? "active" : "scheduled",
                    state.counterattackProgress(), Math.max(200, region.captureTicks() / 2),
                    state.counterattackActive() ? state.counterattackEndsAt() : state.counterattackAt(),
                    state.attackingFaction(), state.controllingFaction()));
        }
        return result.stream()
                .sorted(java.util.Comparator.comparing(
                        galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary::endsAt)
                        .thenComparing(
                                galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary::state))
                .limit(16).toList();
    }

    private static List<String> vehicleIds() {
        return GameplayDataManager.snapshot().launchContent().vehicleIds().stream()
                .sorted().toList();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level() instanceof ServerLevel level)
                || !(level.getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall)) return false;
        boolean access = hall.canOpen(player) || hasPendingInvite(
                KingdomSavedData.get(level), hall, player.getUUID(), level.getGameTime());
        return access && player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(hallPos)) <= 64.0D;
    }

    /** Server-side authority check for workflows that temporarily replace the menu screen. */
    public boolean authorizesClassSelection(Player player) {
        return player.level() instanceof ServerLevel level
                && level.getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall
                && stillValid(player)
                && hall.canOpen(player);
    }

    public static boolean hasPendingInvite(
            KingdomSavedData data, CommandCenterBlockEntity hall, UUID playerId, long gameTime
    ) {
        if (hall.ownerId() == null) return false;
        var kingdom = data.kingdomForOwner(hall.ownerId()).orElse(null);
        return kingdom != null && data.pendingInvites().stream().anyMatch(invite ->
                invite.kingdomId().equals(kingdom.id())
                        && invite.targetPlayerId().equals(playerId)
                        && invite.expiresGameTime() >= gameTime);
    }

    private static boolean completedProjectStillExists(ServerLevel level, BuildProject project) {
        var blueprint = GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
        if (blueprint == null) return false;
        for (int index = 0; index < blueprint.placements().size(); index++) {
            var placement = blueprint.rotatedPlacement(index, project.rotationSteps());
            Identifier blockId;
            try {
                blockId = Identifier.parse(placement.blockId());
            } catch (RuntimeException invalid) {
                return false;
            }
            var block = BuiltInRegistries.BLOCK.getValue(blockId);
            BlockPos position = new BlockPos(project.originX() + placement.x(),
                    project.originY() + placement.y(), project.originZ() + placement.z());
            if (block == null || !level.getBlockState(position).is(block)) return false;
        }
        return true;
    }

    private static boolean report(ServerPlayer player, boolean success, String reason) {
        player.sendSystemMessage(Component.translatable(
                success ? "message.galacticwars.operations.accepted"
                        : "message.galacticwars.operations.rejected",
                Component.translatable("reason.galacticwars.operations." + reason)));
        return success;
    }

    private static List<ServerPlayer> nearbyInvitePlayers(
            ServerLevel level,
            ServerPlayer player,
            KingdomRecord kingdom,
            KingdomSavedData data
    ) {
        if (kingdom == null) {
            return List.of();
        }
        return level.players().stream()
                .filter(other -> other != player && other.isAlive() && !other.isSpectator())
                .filter(other -> player.distanceToSqr(other) <= INVITE_RANGE_SQUARED)
                .filter(other -> kingdom.member(other.getUUID()).isEmpty())
                .filter(other -> data.kingdomForPlayer(other.getUUID()).isEmpty())
                .sorted(Comparator.comparingDouble((ServerPlayer other) -> player.distanceToSqr(other))
                        .thenComparing(ServerPlayer::getUUID))
                .limit(16)
                .toList();
    }

    private static <T> T selectTarget(
            List<T> candidates,
            Optional<UUID> requestedId,
            Function<T, UUID> id
    ) {
        return CommandTargetResolver.resolve(candidates, requestedId, id).target().orElse(null);
    }

    private static String selectionReason(boolean hasCandidates, boolean selectionMissing) {
        return hasCandidates && selectionMissing ? "selection_required" : "target_unavailable";
    }

    private static boolean isDirectSquadAction(int buttonId) {
        return buttonId >= HOLD_SQUAD && buttonId <= SET_SQUAD_RALLY;
    }

    private static List<CombatTargetSummary> nearbyCombatTargets(
            ServerLevel level,
            Player player,
            KingdomRecord kingdom
    ) {
        java.util.Set<UUID> ownRecruitIds = kingdom.npcRoster().stream()
                .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                .collect(java.util.stream.Collectors.toSet());
        List<galacticwars.clonewars.entity.GalacticRecruitEntity> ownedMilitary = ownRecruitIds.stream()
                .map(level::getEntity)
                .filter(galacticwars.clonewars.entity.GalacticRecruitEntity.class::isInstance)
                .map(galacticwars.clonewars.entity.GalacticRecruitEntity.class::cast)
                .filter(recruit -> recruit.isAlive()
                        && recruit.getServiceBranch()
                        == galacticwars.clonewars.recruitment.NpcServiceBranch.MILITARY)
                .toList();
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(32.0D),
                        target -> target != player && target.isAlive() && !target.isInvulnerable())
                .stream()
                .filter(target -> !ownRecruitIds.contains(target.getUUID()))
                .filter(target -> target instanceof Monster
                        || target instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit
                        && ownedMilitary.stream().anyMatch(source ->
                        source.isHostileFactionRecruit(recruit)))
                .sorted(Comparator.comparingDouble(
                                (LivingEntity target) -> player.distanceToSqr(target))
                        .thenComparing(LivingEntity::getUUID))
                .limit(32)
                .map(target -> new CombatTargetSummary(
                        target.getUUID(), target.getDisplayName().getString(),
                        target instanceof galacticwars.clonewars.entity.GalacticRecruitEntity recruit
                                ? recruit.getRecruitFactionId() : "minecraft:hostile",
                        Math.max(0, (int) Math.round(Math.sqrt(player.distanceToSqr(target))))))
                .toList();
    }

    private static List<WorkerSummary> loadedWorkers(
            ServerLevel level,
            Player player,
            KingdomRecord kingdom
    ) {
        String dimensionId = level.dimension().identifier().toString();
        return kingdom.npcRoster().stream()
                .map(galacticwars.clonewars.kingdom.KingdomNpcRecord::recruitId)
                .map(level::getEntity)
                .filter(galacticwars.clonewars.entity.GalacticRecruitEntity.class::isInstance)
                .map(galacticwars.clonewars.entity.GalacticRecruitEntity.class::cast)
                .filter(recruit -> recruit.isAlive() && recruit.getWorkerProfession().isPresent())
                .map(recruit -> {
                    var assignment = recruit.getWorkerAssignment();
                    var status = recruit.getWorkerStatus();
                    Optional<PositionSummary> worksite = assignment.map(value -> new PositionSummary(
                            value.dimensionId(), value.workX(), value.workY(), value.workZ()));
                    Optional<PositionSummary> storage = Optional.ofNullable(recruit.getStorageTarget())
                            .map(pos -> new PositionSummary(
                                    dimensionId, pos.getX(), pos.getY(), pos.getZ()));
                    Optional<PositionSummary> activeTarget = status.target()
                            .map(target -> new PositionSummary(
                                    target.dimensionId(), target.x(), target.y(), target.z()));
                    return new WorkerSummary(
                            recruit.getUUID(), recruit.getDisplayName().getString(),
                            recruit.getWorkerProfession().orElseThrow().id(),
                            recruit.getRecruitCommand().name(), status.phase().id(), status.reasonCode(),
                            worksite, assignment.map(value -> value.radius()).orElse(0),
                            storage, activeTarget, assignment.flatMap(value -> value.workOrderId()),
                            recruit.getWorkerCarriedItemCount(), recruit.getWorkerStorageItemCount(),
                            Math.max(0, (int) Math.round(Math.sqrt(player.distanceToSqr(recruit)))));
                })
                .toList();
    }

    private static Optional<ArmyLocation> selectedCommandLocation(
            ServerLevel level,
            ServerPlayer player
    ) {
        return CommandTargetSelection.blockFromInventory(player).map(pos -> new ArmyLocation(
                level.dimension().identifier().toString(),
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
    }

    private static KingdomMemberRole nextRole(KingdomMemberRole role) {
        return switch (role) {
            case MEMBER -> KingdomMemberRole.BUILDER;
            case BUILDER -> KingdomMemberRole.QUARTERMASTER;
            case QUARTERMASTER -> KingdomMemberRole.OFFICER;
            case OFFICER, OWNER -> KingdomMemberRole.MEMBER;
        };
    }

    private static ArmyFormation nextFormation(ArmyFormation formation) {
        ArmyFormation[] values = ArmyFormation.values();
        return values[(formation.ordinal() + 1) % values.length];
    }

    private static int findSupplyCellSlot(CommandCenterBlockEntity hall) {
        for (int slot = 0; slot < hall.getContainerSize(); slot++) {
            if (hall.getItem(slot).is(ModItems.ENERGY_CELL.get())) {
                return slot;
            }
        }
        return -1;
    }

    private static void consumeSupplyCell(CommandCenterBlockEntity hall, int slot) {
        hall.removeItem(slot, 1);
        hall.setChanged();
    }
}
