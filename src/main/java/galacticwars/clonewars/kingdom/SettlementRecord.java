package galacticwars.clonewars.kingdom;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.WorkAreaConfiguration;
import galacticwars.clonewars.workforce.WorkerProfessionCatalog;

public record SettlementRecord(
        UUID id,
        String dimensionId,
        int hallX,
        int hallY,
        int hallZ,
        int claimRadius,
        int housingCapacity,
        List<UUID> recruitIds,
        Optional<UUID> commanderId,
        CommanderPolicy commanderPolicy,
        List<WorksiteRecord> worksites,
        List<BuildProject> buildProjects,
        List<WorkOrder> workOrders,
        List<RecruitmentCampaign> recruitmentCampaigns,
        SettlementRewards rewards,
        int revision,
        List<UUID> additionalCommanderIds
) {
    public static final int MAX_ACTIVE_BUILD_PROJECTS = 32;
    public static final int MAX_RECENT_TERMINAL_BUILD_PROJECTS = 32;
    public static final int MAX_ACTIVE_WORK_ORDERS = 64;
    public static final int MAX_RECENT_TERMINAL_WORK_ORDERS = 32;

    public SettlementRecord {
        Objects.requireNonNull(id, "id");
        dimensionId = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        if (claimRadius < 8 || claimRadius > 256) {
            throw new IllegalArgumentException("claimRadius must be between 8 and 256");
        }
        if (housingCapacity < 0) {
            throw new IllegalArgumentException("housingCapacity cannot be negative");
        }
        Objects.requireNonNull(recruitIds, "recruitIds");
        recruitIds = List.copyOf(new LinkedHashSet<>(recruitIds));
        commanderId = commanderId == null ? Optional.empty() : commanderId;
        Optional<UUID> primaryCommander = commanderId;
        additionalCommanderIds = List.copyOf(new LinkedHashSet<>(
                Objects.requireNonNull(additionalCommanderIds, "additionalCommanderIds"))).stream()
                .filter(commander -> primaryCommander.filter(commander::equals).isEmpty())
                .filter(recruitIds::contains)
                .toList();
        Objects.requireNonNull(commanderPolicy, "commanderPolicy");
        worksites = List.copyOf(Objects.requireNonNull(worksites, "worksites"));
        if (worksites.stream().noneMatch(worksite -> worksite.type().equals("frontier"))) {
            java.util.ArrayList<WorksiteRecord> migratedWorksites = new java.util.ArrayList<>(worksites);
            migratedWorksites.add(frontierWorksite(id, dimensionId, hallX, hallY, hallZ));
            worksites = List.copyOf(migratedWorksites);
        }
        List<BuildProject> suppliedProjects = List.copyOf(
                Objects.requireNonNull(buildProjects, "buildProjects"));
        List<WorkOrder> suppliedOrders = List.copyOf(
                Objects.requireNonNull(workOrders, "workOrders"));
        recruitmentCampaigns = List.copyOf(Objects.requireNonNull(recruitmentCampaigns, "recruitmentCampaigns"));
        Objects.requireNonNull(rewards, "rewards");
        SettlementTerminalLedger ledger = rewards.terminalLedger();
        for (BuildProject project : suppliedProjects) {
            ledger = ledger.recordProject(project);
        }
        for (WorkOrder workOrder : suppliedOrders) {
            ledger = ledger.recordWorkOrder(workOrder);
        }
        rewards = rewards.withTerminalLedger(ledger);
        buildProjects = compactBuildProjects(suppliedProjects);
        workOrders = compactWorkOrders(suppliedOrders);
        if (revision < 0) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
    }

    public SettlementRecord(
            UUID id,
            String dimensionId,
            int hallX,
            int hallY,
            int hallZ,
            int claimRadius,
            int housingCapacity,
            List<UUID> recruitIds,
            Optional<UUID> commanderId,
            CommanderPolicy commanderPolicy,
            List<WorksiteRecord> worksites,
            List<BuildProject> buildProjects,
            List<WorkOrder> workOrders,
            List<RecruitmentCampaign> recruitmentCampaigns,
            SettlementRewards rewards,
            int revision
    ) {
        this(id, dimensionId, hallX, hallY, hallZ, claimRadius, housingCapacity, recruitIds, commanderId,
                commanderPolicy, worksites, buildProjects, workOrders, recruitmentCampaigns, rewards, revision,
                List.of());
    }

    public static SettlementRecord create(String dimensionId, int x, int y, int z) {
        UUID settlementId = UUID.randomUUID();
        WorksiteRecord frontier = frontierWorksite(settlementId, dimensionId, x, y, z);
        return new SettlementRecord(settlementId, dimensionId, x, y, z, 48, 4,
                List.of(), Optional.empty(), CommanderPolicy.defaults(), List.of(frontier), List.of(), List.of(),
                List.of(), SettlementRewards.none(), 0);
    }

    private static WorksiteRecord frontierWorksite(
            UUID settlementId,
            String dimensionId,
            int x,
            int y,
            int z
    ) {
        UUID worksiteId = UUID.nameUUIDFromBytes(
                (settlementId + ":frontier").getBytes(StandardCharsets.UTF_8));
        return new WorksiteRecord(
                worksiteId, "frontier", dimensionId, x, y, z, 32, 2,
                WorkerProfessionCatalog.enabledProfessions().stream()
                        .map(definition -> definition.profession()).toList(),
                Optional.empty(), List.of(), List.of());
    }

    public boolean hasHousingSpace() {
        return recruitIds.size() < housingCapacity;
    }

    public boolean containsRecruit(UUID recruitId) {
        return recruitIds.contains(recruitId);
    }

    public boolean hasActiveCampaign() {
        return recruitmentCampaigns.stream().anyMatch(RecruitmentCampaign::active);
    }

    public boolean hasCommanderSlot() {
        return commanderIds().size() < rewards.commanderSlots();
    }

    public List<UUID> commanderIds() {
        java.util.ArrayList<UUID> commanders = new java.util.ArrayList<>();
        commanderId.ifPresent(commanders::add);
        commanders.addAll(additionalCommanderIds);
        return List.copyOf(commanders);
    }

    public boolean containsCompletedProject(BuildProject project) {
        Objects.requireNonNull(project, "project");
        return rewards.terminalLedger().completedBuild(project)
                || buildProjects.stream().anyMatch(existing -> existing.state() == BuildProjectState.COMPLETED
                && existing.blueprintId().equals(project.blueprintId())
                && existing.dimensionId().equals(project.dimensionId())
                && existing.originX() == project.originX()
                && existing.originY() == project.originY()
                && existing.originZ() == project.originZ());
    }

    public SettlementRecord withRecruit(UUID recruitId) {
        if (containsRecruit(recruitId)) {
            return this;
        }
        LinkedHashSet<UUID> updated = new LinkedHashSet<>(recruitIds);
        updated.add(Objects.requireNonNull(recruitId, "recruitId"));
        return copy(List.copyOf(updated), commanderId, commanderPolicy, recruitmentCampaigns, revision + 1);
    }

    public SettlementRecord withoutRecruit(UUID recruitId) {
        Objects.requireNonNull(recruitId, "recruitId");
        if (!containsRecruit(recruitId)) {
            return this;
        }
        LinkedHashSet<UUID> updated = new LinkedHashSet<>(recruitIds);
        updated.remove(recruitId);
        Optional<UUID> updatedCommander = commanderId.filter(id -> !id.equals(recruitId));
        List<UUID> updatedAdditionalCommanders = additionalCommanderIds.stream()
                .filter(id -> !id.equals(recruitId)).toList();
        SettlementRecord removed = copy(
                List.copyOf(updated), updatedCommander, commanderPolicy, recruitmentCampaigns, revision + 1,
                updatedAdditionalCommanders)
                .releaseWorksite(recruitId);
        for (WorkOrder workOrder : List.copyOf(removed.workOrders())) {
            if (workOrder.assignedRecruitId().filter(recruitId::equals).isPresent() && !workOrder.state().terminal()) {
                removed = removed.withWorkOrder(workOrder.release(), false);
            }
        }
        return removed;
    }

    public SettlementRecord withHallLocation(String dimensionId, int x, int y, int z) {
        List<WorksiteRecord> relocatedWorksites = worksites.stream()
                .map(worksite -> worksite.type().equals("frontier")
                        ? worksite.withLocation(dimensionId, x, y, z)
                        : worksite)
                .toList();
        return new SettlementRecord(id, dimensionId, x, y, z, claimRadius, housingCapacity,
                recruitIds, commanderId, commanderPolicy, relocatedWorksites, buildProjects, workOrders,
                recruitmentCampaigns, rewards, revision + 1, additionalCommanderIds);
    }

    public SettlementRecord withCommander(UUID recruitId) {
        if (!containsRecruit(recruitId)) {
            throw new IllegalArgumentException("commander must be a settlement recruit");
        }
        if (commanderIds().contains(recruitId)) {
            return this;
        }
        if (commanderId.isEmpty()) {
            return copy(recruitIds, Optional.of(recruitId), commanderPolicy, recruitmentCampaigns, revision + 1,
                    additionalCommanderIds);
        }
        java.util.ArrayList<UUID> commanders = new java.util.ArrayList<>(additionalCommanderIds);
        commanders.add(recruitId);
        return copy(recruitIds, commanderId, commanderPolicy, recruitmentCampaigns, revision + 1,
                List.copyOf(commanders));
    }

    public SettlementRecord withoutCommander(UUID recruitId) {
        if (!commanderIds().contains(recruitId)) {
            return this;
        }
        if (commanderId.filter(recruitId::equals).isPresent()) {
            Optional<UUID> promotedPrimary = additionalCommanderIds.stream().findFirst();
            List<UUID> remaining = additionalCommanderIds.stream().skip(1).toList();
            return copy(recruitIds, promotedPrimary, commanderPolicy, recruitmentCampaigns, revision + 1, remaining);
        }
        return copy(recruitIds, commanderId, commanderPolicy, recruitmentCampaigns, revision + 1,
                additionalCommanderIds.stream().filter(id -> !id.equals(recruitId)).toList());
    }

    public SettlementRecord withCommanderPolicy(CommanderPolicy policy) {
        return copy(recruitIds, commanderId, policy, recruitmentCampaigns, revision + 1);
    }

    public SettlementRecord withCampaign(RecruitmentCampaign campaign) {
        if (hasActiveCampaign()) {
            throw new IllegalStateException("settlement already has an active recruitment campaign");
        }
        java.util.ArrayList<RecruitmentCampaign> updated = new java.util.ArrayList<>(recruitmentCampaigns);
        updated.add(campaign);
        return copy(recruitIds, commanderId, commanderPolicy, List.copyOf(updated), revision + 1);
    }

    public SettlementRecord replaceCampaign(RecruitmentCampaign campaign) {
        java.util.ArrayList<RecruitmentCampaign> updated = new java.util.ArrayList<>(recruitmentCampaigns);
        for (int i = 0; i < updated.size(); i++) {
            if (updated.get(i).id().equals(campaign.id())) {
                updated.set(i, campaign);
                return copy(recruitIds, commanderId, commanderPolicy, List.copyOf(updated), revision + 1);
            }
        }
        return this;
    }

    public Optional<WorksiteRecord> assignedWorksite(UUID recruitId) {
        return worksites.stream().filter(worksite -> worksite.assignmentIds().contains(recruitId)).findFirst();
    }

    public Optional<WorkOrder> workOrder(UUID workOrderId) {
        return workOrders.stream().filter(workOrder -> workOrder.id().equals(workOrderId)).findFirst();
    }

    public SettlementRecord reserveWorksite(UUID recruitId, WorkerProfession profession) {
        return reserveWorksite(recruitId, profession, Optional.empty());
    }

    public SettlementRecord reserveWorksite(
            UUID recruitId,
            WorkerProfession profession,
            Optional<UUID> preferredProjectId
    ) {
        Objects.requireNonNull(preferredProjectId, "preferredProjectId");
        Optional<WorksiteRecord> existing = assignedWorksite(recruitId);
        if (existing.filter(worksite -> worksite.accepts(profession))
                .filter(worksite -> preferredProjectId.isEmpty()
                        || worksite.sourceProjectId().equals(preferredProjectId))
                .isPresent()) {
            return this;
        }
        WorksiteRecord available = worksites.stream()
                .filter(worksite -> worksite.accepts(profession) && worksite.hasCapacity())
                .filter(worksite -> preferredProjectId.isEmpty()
                        || worksite.sourceProjectId().equals(preferredProjectId))
                .sorted(java.util.Comparator
                        .comparingInt((WorksiteRecord worksite) ->
                                worksite.configuration().priority())
                        .reversed()
                        .thenComparing(worksite -> worksite.id().toString()))
                .findFirst().orElse(null);
        if (available == null) {
            return this;
        }
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            WorksiteRecord next = worksite.release(recruitId);
            if (worksite.id().equals(available.id())) {
                next = next.assign(recruitId);
            }
            updated.add(next);
        }
        List<WorkOrder> releasedOrders = workOrders.stream()
                .map(order -> order.assignedRecruitId().filter(recruitId::equals).isPresent()
                        && !order.state().terminal() ? order.release() : order)
                .toList();
        return withOperationalState(List.copyOf(updated), buildProjects, releasedOrders, revision + 1);
    }

    public SettlementRecord configureAssignedFrontierWorksite(
            UUID recruitId,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius
    ) {
        WorksiteRecord assigned = assignedWorksite(recruitId)
                .filter(worksite -> worksite.type().equals("frontier"))
                .orElse(null);
        if (assigned == null) {
            return this;
        }
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            updated.add(worksite.id().equals(assigned.id())
                    ? worksite.withLocationAndRadius(dimensionId, x, y, z, radius)
                    : worksite);
        }
        return withOperationalState(List.copyOf(updated), buildProjects, workOrders, revision + 1);
    }

    public SettlementRecord configureAssignedCourierRoute(
            UUID recruitId,
            List<CourierWaypoint> route,
            CourierRouteMode mode
    ) {
        WorksiteRecord assigned = assignedWorksite(recruitId)
                .filter(worksite -> worksite.accepts(WorkerProfession.COURIER))
                .orElse(null);
        if (assigned == null) {
            return this;
        }
        WorksiteRecord configured = assigned.configured(
                assigned.configuration().withCourierRoute(route, mode));
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            updated.add(worksite.id().equals(assigned.id()) ? configured : worksite);
        }
        return withOperationalState(List.copyOf(updated), buildProjects, workOrders, revision + 1);
    }

    public SettlementRecord configureWorksite(
            UUID worksiteId,
            long expectedConfigurationRevision,
            WorkAreaBounds bounds,
            boolean kingdomAccess,
            int priority,
            boolean overlayVisible,
            List<String> itemFilters,
            CourierDispatchMode dispatchMode
    ) {
        WorksiteRecord selected = worksites.stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElse(null);
        if (selected == null
                || selected.configuration().revision() != expectedConfigurationRevision) {
            return this;
        }
        WorkAreaConfiguration configured = selected.configuration().withSettings(
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                itemFilters,
                dispatchMode);
        if (configured == selected.configuration()) {
            return this;
        }
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            updated.add(worksite.id().equals(worksiteId)
                    ? worksite.configured(configured)
                    : worksite);
        }
        return withOperationalState(List.copyOf(updated), buildProjects, workOrders, revision + 1);
    }

    public SettlementRecord configureWorksiteRoute(
            UUID worksiteId,
            long expectedConfigurationRevision,
            List<galacticwars.clonewars.workforce.CourierWaypoint> route,
            galacticwars.clonewars.workforce.CourierRouteMode mode
    ) {
        WorksiteRecord selected = worksites.stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElse(null);
        if (selected == null
                || selected.configuration().revision() != expectedConfigurationRevision) {
            return this;
        }
        WorkAreaConfiguration configured = selected.configuration().withCourierRoute(route, mode);
        if (configured == selected.configuration()) {
            return this;
        }
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            updated.add(worksite.id().equals(worksiteId)
                    ? worksite.configured(configured)
                    : worksite);
        }
        return withOperationalState(List.copyOf(updated), buildProjects, workOrders, revision + 1);
    }

    public SettlementRecord configureWorksiteStorage(
            UUID worksiteId,
            long expectedConfigurationRevision,
            StorageEndpoint endpoint
    ) {
        WorksiteRecord selected = worksites.stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElse(null);
        if (selected == null
                || selected.configuration().revision() != expectedConfigurationRevision) {
            return this;
        }
        WorksiteRecord configured = selected.withPrimaryStorageEndpoint(endpoint);
        if (configured == selected) {
            return this;
        }
        java.util.ArrayList<WorksiteRecord> updated =
                new java.util.ArrayList<>(worksites.size());
        for (WorksiteRecord worksite : worksites) {
            updated.add(worksite.id().equals(worksiteId) ? configured : worksite);
        }
        return withOperationalState(
                List.copyOf(updated),
                buildProjects,
                workOrders,
                revision + 1);
    }

    public SettlementRecord releaseWorksite(UUID recruitId) {
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(worksites.size());
        boolean changed = false;
        for (WorksiteRecord worksite : worksites) {
            WorksiteRecord next = worksite.release(recruitId);
            changed |= next != worksite;
            updated.add(next);
        }
        return changed ? withOperationalState(List.copyOf(updated), buildProjects, workOrders, revision + 1) : this;
    }

    public SettlementRecord releaseWorkerAssignments(UUID recruitId) {
        java.util.ArrayList<WorksiteRecord> updatedWorksites = new java.util.ArrayList<>(worksites.size());
        boolean changed = false;
        for (WorksiteRecord worksite : worksites) {
            WorksiteRecord next = worksite.release(recruitId);
            changed |= next != worksite;
            updatedWorksites.add(next);
        }
        java.util.ArrayList<WorkOrder> updatedOrders = new java.util.ArrayList<>(workOrders.size());
        for (WorkOrder workOrder : workOrders) {
            WorkOrder next = workOrder.assignedRecruitId().filter(recruitId::equals).isPresent()
                    && !workOrder.state().terminal()
                    ? workOrder.release()
                    : workOrder;
            changed |= next != workOrder;
            updatedOrders.add(next);
        }
        return changed
                ? withOperationalState(List.copyOf(updatedWorksites), buildProjects,
                        List.copyOf(updatedOrders), revision + 1)
                : this;
    }

    SettlementRecord withNewBuildProject(BuildProject project) {
        Objects.requireNonNull(project, "project");
        if (project.revision() != 0 || project.state() != BuildProjectState.ACTIVE
                || !project.completedPlacements().isEmpty()
                || rewards.terminalLedger().terminalProject(project.id())
                || rewards.terminalLedger().completedBuild(project)
                || buildProjects.stream().filter(existing -> !existing.state().terminal()).count()
                        >= MAX_ACTIVE_BUILD_PROJECTS
                || buildProjects.stream().anyMatch(existing -> existing.id().equals(project.id()))) {
            return this;
        }
        java.util.ArrayList<BuildProject> updated = new java.util.ArrayList<>(buildProjects);
        updated.add(project);
        List<WorksiteRecord> updatedWorksites = reconcileBuilderWorksite(worksites, project);
        return withOperationalState(List.copyOf(updatedWorksites), List.copyOf(updated), workOrders, revision + 1);
    }

    SettlementRecord replaceBuildProject(BuildProject project) {
        Objects.requireNonNull(project, "project");
        java.util.ArrayList<BuildProject> updated = new java.util.ArrayList<>(buildProjects);
        for (int index = 0; index < updated.size(); index++) {
            BuildProject current = updated.get(index);
            if (!current.id().equals(project.id())) {
                continue;
            }
            if (!current.sameAuthority(project)
                    || current.revision() + 1 != project.revision()
                    || current.state() == BuildProjectState.COMPLETED
                    || current.state() == BuildProjectState.CANCELLED
                    || project.state() == BuildProjectState.COMPLETED) {
                return this;
            }
            updated.set(index, project);
            List<WorksiteRecord> updatedWorksites = reconcileBuilderWorksite(worksites, project);
            return withOperationalState(updatedWorksites, List.copyOf(updated), workOrders, revision + 1);
        }
        return this;
    }

    public SettlementRecord withWorkOrder(WorkOrder workOrder, boolean allowInsert) {
        java.util.ArrayList<WorkOrder> updated = new java.util.ArrayList<>(workOrders);
        for (int index = 0; index < updated.size(); index++) {
            WorkOrder current = updated.get(index);
            if (current.id().equals(workOrder.id())) {
                if (current.revision() != workOrder.revision() - 1) {
                    return this;
                }
                updated.set(index, workOrder);
                return withOperationalState(worksites, buildProjects, List.copyOf(updated), revision + 1);
            }
        }
        if (!allowInsert || workOrder.revision() != 0) {
            return this;
        }
        if (rewards.terminalLedger().terminalWorkOrder(workOrder.id())
                || workOrders.stream().filter(order -> !order.state().terminal()).count()
                        >= MAX_ACTIVE_WORK_ORDERS) {
            return this;
        }
        updated.add(workOrder);
        return withOperationalState(worksites, buildProjects, List.copyOf(updated), revision + 1);
    }

    public SettlementRecord withCompletedProject(BuildProject project, KingdomBaseBlueprint blueprint) {
        Objects.requireNonNull(project, "project");
        Objects.requireNonNull(blueprint, "blueprint");
        BuildProject stored = buildProjects.stream()
                .filter(existing -> existing.id().equals(project.id()))
                .findFirst()
                .orElse(null);
        if (stored == null
                || !stored.equals(project)
                || stored.state() == BuildProjectState.COMPLETED
                || stored.state() == BuildProjectState.CANCELLED
                || !stored.blueprintId().equals(blueprint.id())
                || !blueprint.matchesDefinitionHash(stored.definitionHash())
                || !blueprint.supportsRotationSteps(stored.rotationSteps())
                || !stored.hasAllPlacements(blueprint.placements().size())
                || containsCompletedProject(project)) {
            return this;
        }
        java.util.ArrayList<BuildProject> projects = new java.util.ArrayList<>(buildProjects);
        for (int index = 0; index < projects.size(); index++) {
            if (projects.get(index).id().equals(stored.id())) {
                projects.set(index, stored.complete());
                break;
            }
        }
        java.util.ArrayList<WorksiteRecord> updatedWorksites = new java.util.ArrayList<>(worksites.stream()
                .filter(worksite -> worksite.sourceProjectId().filter(project.id()::equals).isEmpty())
                .toList());
        List<StorageEndpoint> storageEndpoints = blueprint.storageEndpoints(project);
        if (!blueprint.worksiteType().isBlank() && blueprint.worksiteCapacity() > 0) {
            List<WorkerProfession> accepted = WorkerProfession.byId(blueprint.worksiteType())
                    .map(List::of).orElse(List.of());
            updatedWorksites.add(new WorksiteRecord(
                    projectWorksiteId(project, "completed:" + blueprint.worksiteType()),
                    blueprint.worksiteType(),
                    project.dimensionId(),
                    project.originX(),
                    project.originY(),
                    project.originZ(),
                    8,
                    blueprint.worksiteCapacity(),
                    accepted,
                    Optional.of(project.id()),
                    List.of(),
                    storageEndpoints));
        } else if (!storageEndpoints.isEmpty()) {
            updatedWorksites.add(new WorksiteRecord(
                    projectWorksiteId(project, "completed:storage"),
                    "storage",
                    project.dimensionId(),
                    project.originX(),
                    project.originY(),
                    project.originZ(),
                    8,
                    1,
                    List.of(),
                    Optional.of(project.id()),
                    List.of(),
                    storageEndpoints));
        }
        return new SettlementRecord(id, dimensionId, hallX, hallY, hallZ, claimRadius,
                Math.addExact(housingCapacity, blueprint.housingReward()), recruitIds, commanderId,
                commanderPolicy, List.copyOf(updatedWorksites), List.copyOf(projects), workOrders,
                recruitmentCampaigns,
                rewards.add(blueprint.storageSlotReward(), blueprint.commanderSlotReward()),
                revision + 1, additionalCommanderIds);
    }

    private List<WorksiteRecord> reconcileBuilderWorksite(
            List<WorksiteRecord> currentWorksites,
            BuildProject project
    ) {
        java.util.ArrayList<WorksiteRecord> updated = new java.util.ArrayList<>(currentWorksites.stream()
                .filter(worksite -> project.state() != BuildProjectState.CANCELLED
                        || worksite.sourceProjectId().filter(project.id()::equals).isEmpty())
                .toList());
        if ((project.state() == BuildProjectState.ACTIVE || project.state() == BuildProjectState.BLOCKED)
                && updated.stream().noneMatch(worksite -> worksite.sourceProjectId()
                        .filter(project.id()::equals).isPresent())) {
            updated.add(new WorksiteRecord(
                    projectWorksiteId(project, "active:builder"), "builder", project.dimensionId(),
                    project.originX(), project.originY(), project.originZ(), 16, 1,
                    List.of(WorkerProfession.BUILDER), Optional.of(project.id()), List.of(), List.of()));
        }
        return List.copyOf(updated);
    }

    private UUID projectWorksiteId(BuildProject project, String role) {
        return UUID.nameUUIDFromBytes(
                (id + ":" + project.id() + ":" + role).getBytes(StandardCharsets.UTF_8));
    }

    private SettlementRecord copy(
            List<UUID> recruits,
            Optional<UUID> commander,
            CommanderPolicy policy,
            List<RecruitmentCampaign> campaigns,
            int nextRevision
    ) {
        return copy(recruits, commander, policy, campaigns, nextRevision, additionalCommanderIds);
    }

    private SettlementRecord copy(
            List<UUID> recruits,
            Optional<UUID> commander,
            CommanderPolicy policy,
            List<RecruitmentCampaign> campaigns,
            int nextRevision,
            List<UUID> additionalCommanders
    ) {
        return new SettlementRecord(id, dimensionId, hallX, hallY, hallZ, claimRadius, housingCapacity,
                recruits, commander, policy, worksites, buildProjects, workOrders, campaigns, rewards, nextRevision,
                additionalCommanders);
    }

    private SettlementRecord withOperationalState(
            List<WorksiteRecord> updatedWorksites,
            List<BuildProject> updatedProjects,
            List<WorkOrder> updatedOrders,
            int nextRevision
    ) {
        return new SettlementRecord(id, dimensionId, hallX, hallY, hallZ, claimRadius, housingCapacity,
                recruitIds, commanderId, commanderPolicy, updatedWorksites, updatedProjects, updatedOrders,
                recruitmentCampaigns, rewards, nextRevision, additionalCommanderIds);
    }

    private static List<BuildProject> compactBuildProjects(List<BuildProject> projects) {
        int terminalCount = (int) projects.stream().filter(project -> project.state().terminal()).count();
        int terminalsToSkip = Math.max(0, terminalCount - MAX_RECENT_TERMINAL_BUILD_PROJECTS);
        java.util.ArrayList<BuildProject> compacted = new java.util.ArrayList<>(
                projects.size() - terminalsToSkip);
        for (BuildProject project : projects) {
            if (project.state().terminal() && terminalsToSkip > 0) {
                terminalsToSkip--;
                continue;
            }
            compacted.add(project);
        }
        return List.copyOf(compacted);
    }

    private static List<WorkOrder> compactWorkOrders(List<WorkOrder> workOrders) {
        int terminalCount = (int) workOrders.stream().filter(order -> order.state().terminal()).count();
        int terminalsToSkip = Math.max(0, terminalCount - MAX_RECENT_TERMINAL_WORK_ORDERS);
        java.util.ArrayList<WorkOrder> compacted = new java.util.ArrayList<>(
                workOrders.size() - terminalsToSkip);
        for (WorkOrder workOrder : workOrders) {
            if (workOrder.state().terminal() && terminalsToSkip > 0) {
                terminalsToSkip--;
                continue;
            }
            compacted.add(workOrder);
        }
        return List.copyOf(compacted);
    }
}
