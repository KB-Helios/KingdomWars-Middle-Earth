package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.kingdom.CommandCenterDashboardState;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.ActionAvailability;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.BuildSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.BlueprintSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.ClaimSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.CombatTargetSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.DiplomacyProposalSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.ForeignKingdomSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.InviteSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.MemberSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.NearbyPlayerSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.PositionSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.QuestSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.SquadSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.VehicleFabricationSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.WorkerSummary;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState.WorkOrderSummary;
import galacticwars.clonewars.kingdom.KingdomMemberRole;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomPermissionPolicy;
import galacticwars.clonewars.menu.CommandCenterOperationsMenu;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.network.MenuActionPayload;
import galacticwars.clonewars.network.ResearchActionPayload;
import galacticwars.clonewars.client.ClientGameplayCatalog;
import galacticwars.clonewars.network.GameplayCatalogPayload;
import galacticwars.clonewars.progression.PlayerCampaignAttachmentRuntime;
import galacticwars.clonewars.progression.PlayerCampaignAttachmentState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Responsive, server-backed operations dashboard for the physical Command Center. */
public final class CommandCenterOperationsScreen extends Screen
        implements MenuAccess<CommandCenterOperationsMenu> {
    private static final String[] TABS = {
            "overview", "campaign", "construction", "squads",
            "workforce", "kingdom", "diplomacy", "storage", "technology", "relations"
    };
    private static final int TAB_HEIGHT = 20;
    private static final int ROW_GAP = 2;
    private static final int TEXT = 0xFFE7EEF5;
    private static final int MUTED = 0xFFAAB7C4;
    private static final int ACCENT = 0xFFFFE082;
    private static final int GOOD = 0xFF8FD6A3;
    private static final int WARNING = 0xFFFFB86B;
    private static final int PANEL = 0xD018202B;
    private static final int PANEL_BORDER = 0xFF46566A;

    private final CommandCenterOperationsMenu menu;
    private int selectedTab;
    private int actionPage;
    private int squadIndex;
    private int secondarySquadIndex;
    private int commandCandidateIndex;
    private int splitMemberIndex;
    private int combatTargetIndex;
    private int claimIndex;
    private int buildIndex;
    private int workOrderIndex;
    private int workerIndex;
    private int blueprintIndex;
    private int constructionBuilderIndex;
    private int rotationIndex;
    private int memberIndex;
    private int nearbyPlayerIndex;
    private int inviteIndex;
    private int foreignKingdomIndex;
    private int proposalIndex;
    private int technologyIndex;
    private int lastDashboardRevision = -1;
    private int lastTechnologyRevision = -1;
    private int panelLeft;
    private int panelWidth;
    private int bodyTop;

    public CommandCenterOperationsScreen(
            CommandCenterOperationsMenu menu, Inventory inventory, Component title
    ) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        normalizeSelections();
        lastDashboardRevision = menu.dashboardRevision();
        lastTechnologyRevision = gameplayProjection()
                .map(PlayerCampaignAttachmentState.GameplayProjection::getKingdomRevision)
                .orElse(-1);
        int tabColumns = Math.max(2, Math.min(TABS.length, Math.max(1, (width - 16) / 76)));
        int tabRows = (TABS.length + tabColumns - 1) / tabColumns;
        int tabWidth = (width - 16) / tabColumns;
        for (int index = 0; index < TABS.length; index++) {
            int tab = index;
                    Button button = Button.builder(Component.translatable(
                            "screen.galacticwars.operations.tab." + TABS[index]), pressed -> {
                        selectedTab = tab;
                        actionPage = 0;
                        rebuildWidgets();
                    })
                    .bounds(8 + (index % tabColumns) * tabWidth,
                            28 + (index / tabColumns) * (TAB_HEIGHT + ROW_GAP),
                            tabWidth - 2, TAB_HEIGHT)
                    .build();
            button.active = index != selectedTab;
            addRenderableWidget(button);
        }
        bodyTop = 28 + tabRows * (TAB_HEIGHT + ROW_GAP) + 7;
        panelWidth = Math.max(120, Math.min(720, width - 24));
        panelLeft = (width - panelWidth) / 2;

        switch (selectedTab) {
            case 0 -> addOverviewActions();
            case 1 -> addCampaignActions();
            case 2 -> addConstructionActions();
            case 3 -> addSquadActions();
            case 4 -> addWorkforceActions();
            case 5 -> addKingdomActions();
            case 6 -> addDiplomacyActions();
            case 7 -> addStorageActions();
            case 8 -> addTechnologyActions();
            case 9 -> { }
            default -> { }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (lastDashboardRevision != menu.dashboardRevision()) {
            normalizeSelections();
            rebuildWidgets();
            return;
        }
        int technologyRevision = gameplayProjection()
                .map(PlayerCampaignAttachmentState.GameplayProjection::getKingdomRevision)
                .orElse(-1);
        if (technologyRevision != lastTechnologyRevision) {
            lastTechnologyRevision = technologyRevision;
            rebuildWidgets();
        }
    }

    private void addTechnologyActions() {
        List<GameplayCatalogPayload.TechnologyEntry> nodes = technologyEntries();
        List<WorkerSummary> technicians = technicians();
        addSelector(selectorY(0), nodes.size(),
                () -> technologyIndex = cycle(technologyIndex, -1, nodes.size()),
                () -> technologyIndex = cycle(technologyIndex, 1, nodes.size()));
        addSelector(selectorY(1), technicians.size(),
                () -> workerIndex = cycle(workerIndex, -1, technicians.size()),
                () -> workerIndex = cycle(workerIndex, 1, technicians.size()));
        var projection = gameplayProjection().orElse(null);
        var selectedNode = selected(nodes, technologyIndex).orElse(null);
        boolean manager = canManageTechnology();
        boolean noProject = projection != null && projection.getActiveResearchNode().isEmpty();
        boolean prerequisites = selectedNode != null && projection != null
                && projection.getCompletedTechnology().containsAll(selectedNode.prerequisites());
        boolean start = manager && selectedNode != null && noProject && prerequisites
                && !projection.getCompletedTechnology().contains(selectedNode.nodeId());
        boolean project = projection != null && !projection.getActiveResearchNode().isEmpty();
        addResearchButton(0, "screen.galacticwars.operations.technology.start", start,
                () -> sendResearch(ResearchActionPayload.START,
                        selectedNode == null ? "" : selectedNode.nodeId(), Optional.empty()));
        addResearchButton(1, "screen.galacticwars.operations.technology.contribute", project,
                () -> sendResearch(ResearchActionPayload.CONTRIBUTE, "", Optional.empty()));
        Optional<UUID> technician = selected(technicians, workerIndex).map(WorkerSummary::entityId);
        addResearchButton(2, "screen.galacticwars.operations.technology.assign",
                manager && project && technician.isPresent(),
                () -> sendResearch(
                        ResearchActionPayload.ASSIGN_TECHNICIAN, "", technician));
        addResearchButton(3, "screen.galacticwars.operations.technology.cancel",
                manager && project,
                () -> sendResearch(ResearchActionPayload.CANCEL, "", Optional.empty()));
    }

    private void addResearchButton(int index, String translationKey, boolean enabled, Runnable action) {
        int columns = panelWidth >= 360 ? 2 : 1;
        int gap = 6;
        int buttonWidth = (panelWidth - 16 - gap * (columns - 1)) / columns;
        Button button = Button.builder(Component.translatable(translationKey), pressed -> action.run())
                .bounds(panelLeft + 8 + (index % columns) * (buttonWidth + gap),
                        afterSelectors(2) + (index / columns) * controlStride(),
                        buttonWidth, controlHeight())
                .build();
        button.active = enabled;
        addRenderableWidget(button);
    }

    private void sendResearch(int action, String nodeId, Optional<UUID> technicianId) {
        var projection = gameplayProjection().orElse(null);
        var catalog = ClientGameplayCatalog.snapshot();
        if (projection == null || catalog.serverGeneration() < 0L) {
            return;
        }
        GalacticNetwork.CHANNEL.sendToServer(new ResearchActionPayload(
                UUID.randomUUID(),
                menu.containerId,
                action,
                nodeId,
                technicianId,
                catalog.serverGeneration(),
                projection.getKingdomRevision()));
    }

    private void addOverviewActions() {
        CommandCenterDashboardState state = menu.dashboardState();
        ArrayList<ActionSpec> actions = new ArrayList<>();
        actions.add(action("screen.galacticwars.operations.claim_rewards",
                CommandCenterOperationsMenu.CLAIM_REWARDS,
                menu.dashboardState().pendingRewardCredits() > 0));
        actions.add(action("screen.galacticwars.operations.navigation",
                CommandCenterOperationsMenu.NAVIGATION, state.navigationAvailability()));
        actions.add(action("screen.galacticwars.operations.player_class",
                CommandCenterOperationsMenu.PLAYER_CLASS, true));
        for (int index = 0; index < state.vehicleFabrication().size(); index++) {
            VehicleFabricationSummary fabrication = state.vehicleFabrication().get(index);
            actions.add(action("screen.galacticwars.operations.fabricate." + fabrication.vehicleId(),
                    CommandCenterOperationsMenu.FABRICATE_FIRST + index,
                    fabrication.availability(),
                    fabricationTooltip(fabrication, state.treasuryCredits())));
        }
        addActionGrid(bodyTop + 86, actions);
    }

    private void addCampaignActions() {
        addActionGrid(Math.max(bodyTop + 100, height - 36), List.of(action(
                "screen.galacticwars.operations.claim_rewards",
                CommandCenterOperationsMenu.CLAIM_REWARDS,
                menu.dashboardState().pendingRewardCredits() > 0)));
    }

    private void addConstructionActions() {
        CommandCenterDashboardState state = menu.dashboardState();
        addSelector(selectorY(0), state.blueprints().size(),
                () -> {
                    blueprintIndex = cycle(blueprintIndex, -1, state.blueprints().size());
                    rotationIndex = 0;
                },
                () -> {
                    blueprintIndex = cycle(blueprintIndex, 1, state.blueprints().size());
                    rotationIndex = 0;
                });
        addSelector(selectorY(1), state.constructionBuilderIds().size(),
                () -> constructionBuilderIndex = cycle(
                        constructionBuilderIndex, -1, state.constructionBuilderIds().size()),
                () -> constructionBuilderIndex = cycle(
                        constructionBuilderIndex, 1, state.constructionBuilderIds().size()));
        int rotationCount = selected(state.blueprints(), blueprintIndex)
                .map(value -> value.allowedRotations().size()).orElse(0);
        addSelector(selectorY(2), rotationCount,
                () -> rotationIndex = cycle(rotationIndex, -1, rotationCount),
                () -> rotationIndex = cycle(rotationIndex, 1, rotationCount));
        addSelector(selectorY(3), state.builds().size(),
                () -> buildIndex = cycle(buildIndex, -1, state.builds().size()),
                () -> buildIndex = cycle(buildIndex, 1, state.builds().size()));
        addSelector(selectorY(4), state.workOrders().size(),
                () -> workOrderIndex = cycle(workOrderIndex, -1, state.workOrders().size()),
                () -> workOrderIndex = cycle(workOrderIndex, 1, state.workOrders().size()));
        Optional<BlueprintSummary> blueprint = selected(state.blueprints(), blueprintIndex);
        Optional<UUID> builder = selected(state.constructionBuilderIds(), constructionBuilderIndex);
        int rotationSteps = blueprint.flatMap(value -> selected(value.allowedRotations(), rotationIndex))
                .map(degrees -> degrees / 90).orElse(0);
        Optional<UUID> outpost = selectedCompletedOutpost();
        Optional<UUID> cancellableBuild = selected(state.builds(), buildIndex)
                .filter(build -> build.state().equals("active") || build.state().equals("blocked"))
                .map(BuildSummary::id);
        addActionGrid(afterSelectors(5), List.of(
                action("screen.galacticwars.operations.prepare_projector",
                        CommandCenterOperationsMenu.PREPARE_PROJECTOR_FIRST + rotationSteps,
                        blueprint.map(BlueprintSummary::targetId), builder,
                        blueprint.isPresent() && builder.isPresent()),
                action("screen.galacticwars.operations.cancel_build",
                        CommandCenterOperationsMenu.CANCEL_BUILD_PROJECT,
                        cancellableBuild, Optional.empty(), cancellableBuild.isPresent()),
                action("screen.galacticwars.operations.outpost",
                        CommandCenterOperationsMenu.REGISTER_OUTPOST, outpost, Optional.empty(), outpost.isPresent()),
                action("screen.galacticwars.operations.open_storage",
                        CommandCenterOperationsMenu.STORAGE, true)));
    }

    private void addSquadActions() {
        CommandCenterDashboardState state = menu.dashboardState();
        addSelector(selectorY(0), state.squads().size(),
                () -> squadIndex = cycle(squadIndex, -1, state.squads().size()),
                () -> squadIndex = cycle(squadIndex, 1, state.squads().size()));
        List<SquadSummary> secondary = secondarySquads();
        addSelector(selectorY(1), secondary.size(),
                () -> secondarySquadIndex = cycle(secondarySquadIndex, -1, secondary.size()),
                () -> secondarySquadIndex = cycle(secondarySquadIndex, 1, secondary.size()));
        addSelector(selectorY(2), state.commandCandidateIds().size(),
                () -> commandCandidateIndex = cycle(
                        commandCandidateIndex, -1, state.commandCandidateIds().size()),
                () -> commandCandidateIndex = cycle(
                        commandCandidateIndex, 1, state.commandCandidateIds().size()));
        int splitMemberCount = selectedSquad().map(squad -> squad.memberIds().size()).orElse(0);
        addSelector(selectorY(3), splitMemberCount,
                () -> splitMemberIndex = cycle(splitMemberIndex, -1, splitMemberCount),
                () -> splitMemberIndex = cycle(splitMemberIndex, 1, splitMemberCount));
        addSelector(selectorY(4), state.combatTargets().size(),
                () -> combatTargetIndex = cycle(
                        combatTargetIndex, -1, state.combatTargets().size()),
                () -> combatTargetIndex = cycle(
                        combatTargetIndex, 1, state.combatTargets().size()));

        Optional<UUID> squad = selectedSquad().map(SquadSummary::id);
        Optional<UUID> secondSquad = selected(secondary, secondarySquadIndex).map(SquadSummary::id);
        Optional<UUID> commander = selected(state.commandCandidateIds(), commandCandidateIndex);
        Optional<UUID> splitMember = selectedSquad().flatMap(value ->
                selected(value.memberIds(), splitMemberIndex));
        Optional<UUID> combatTarget = selected(state.combatTargets(), combatTargetIndex)
                .map(CombatTargetSummary::entityId);
        Optional<UUID> claim = selected(state.claims(), claimIndex).map(ClaimSummary::id);
        boolean configurable = squad.isPresent() && (state.claims().size() <= 1 || claim.isPresent());
        addActionGrid(afterSelectors(5), List.of(
                action("screen.galacticwars.operations.create_squad",
                        CommandCenterOperationsMenu.CREATE_SQUAD, commander, Optional.empty(), commander.isPresent()),
                action("screen.galacticwars.operations.split",
                        CommandCenterOperationsMenu.SPLIT_SQUAD, squad, splitMember,
                        squad.isPresent() && splitMember.isPresent()),
                action("screen.galacticwars.operations.merge",
                        CommandCenterOperationsMenu.MERGE_SQUADS, squad, secondSquad,
                        squad.isPresent() && secondSquad.isPresent()),
                action("screen.galacticwars.operations.configure",
                        CommandCenterOperationsMenu.CONFIGURE_SQUAD, squad, claim, configurable),
                action("screen.galacticwars.operations.formation",
                        CommandCenterOperationsMenu.CYCLE_FORMATION, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.patrol",
                        CommandCenterOperationsMenu.PATROL_SQUAD, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.follow",
                        CommandCenterOperationsMenu.FOLLOW_SQUAD, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.hold",
                        CommandCenterOperationsMenu.HOLD_SQUAD, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.move_here",
                        CommandCenterOperationsMenu.MOVE_SQUAD, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.protect",
                        CommandCenterOperationsMenu.PROTECT_SQUAD, squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.clear_target",
                        CommandCenterOperationsMenu.CLEAR_SQUAD_TARGET,
                        squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.attack_target",
                        CommandCenterOperationsMenu.ATTACK_SQUAD_TARGET,
                        squad, combatTarget, squad.isPresent() && combatTarget.isPresent()),
                action("screen.galacticwars.operations.set_rally",
                        CommandCenterOperationsMenu.SET_SQUAD_RALLY,
                        squad, Optional.empty(), squad.isPresent()),
                action("screen.galacticwars.operations.supply",
                        CommandCenterOperationsMenu.SUPPLY_SQUAD, squad, Optional.empty(), squad.isPresent())));
    }

    private void addWorkforceActions() {
        CommandCenterDashboardState state = menu.dashboardState();
        addSelector(selectorY(0), state.workers().size(),
                () -> workerIndex = cycle(workerIndex, -1, state.workers().size()),
                () -> workerIndex = cycle(workerIndex, 1, state.workers().size()));
        addSelector(selectorY(1), state.workOrders().size(),
                () -> workOrderIndex = cycle(workOrderIndex, -1, state.workOrders().size()),
                () -> workOrderIndex = cycle(workOrderIndex, 1, state.workOrders().size()));
        Optional<UUID> worker = selected(state.workers(), workerIndex).map(WorkerSummary::entityId);
        addActionGrid(afterSelectors(2), List.of(
                action("screen.galacticwars.operations.resume_worker",
                        CommandCenterOperationsMenu.RESUME_WORKER,
                        worker, Optional.empty(), worker.isPresent()),
                action("screen.galacticwars.operations.recall_worker",
                        CommandCenterOperationsMenu.RECALL_WORKER,
                        worker, Optional.empty(), worker.isPresent()),
                action("screen.galacticwars.operations.pause_worker",
                        CommandCenterOperationsMenu.PAUSE_WORKER,
                        worker, Optional.empty(), worker.isPresent()),
                action("screen.galacticwars.operations.open_storage",
                        CommandCenterOperationsMenu.STORAGE, true)));
    }

    private void addKingdomActions() {
        List<MemberSummary> members = manageableMembers();
        List<NearbyPlayerSummary> nearbyPlayers = menu.dashboardState().nearbyPlayers();
        List<InviteSummary> invites = incomingInvites();
        addSelector(selectorY(0), members.size(),
                () -> memberIndex = cycle(memberIndex, -1, members.size()),
                () -> memberIndex = cycle(memberIndex, 1, members.size()));
        addSelector(selectorY(1), nearbyPlayers.size(),
                () -> nearbyPlayerIndex = cycle(nearbyPlayerIndex, -1, nearbyPlayers.size()),
                () -> nearbyPlayerIndex = cycle(nearbyPlayerIndex, 1, nearbyPlayers.size()));
        addSelector(selectorY(2), invites.size(),
                () -> inviteIndex = cycle(inviteIndex, -1, invites.size()),
                () -> inviteIndex = cycle(inviteIndex, 1, invites.size()));
        int claimCount = menu.dashboardState().claims().size();
        addSelector(selectorY(3), claimCount,
                () -> claimIndex = cycle(claimIndex, -1, claimCount),
                () -> claimIndex = cycle(claimIndex, 1, claimCount));
        Optional<UUID> member = selected(members, memberIndex).map(MemberSummary::playerId);
        Optional<UUID> inviteTarget = selected(nearbyPlayers, nearbyPlayerIndex)
                .map(NearbyPlayerSummary::playerId);
        Optional<UUID> invite = selected(invites, inviteIndex).map(InviteSummary::inviteId);
        addActionGrid(afterSelectors(4), List.of(
                action("screen.galacticwars.operations.outpost",
                        CommandCenterOperationsMenu.REGISTER_OUTPOST,
                        selectedCompletedOutpost(), Optional.empty(), selectedCompletedOutpost().isPresent()),
                action("screen.galacticwars.operations.invite",
                        CommandCenterOperationsMenu.INVITE_NEAREST,
                        inviteTarget, Optional.empty(), inviteTarget.isPresent()),
                action("screen.galacticwars.operations.accept_invite",
                        CommandCenterOperationsMenu.ACCEPT_INVITE, invite, Optional.empty(), invite.isPresent()),
                action("screen.galacticwars.operations.reject_invite",
                        CommandCenterOperationsMenu.REJECT_INVITE, invite, Optional.empty(), invite.isPresent()),
                action("screen.galacticwars.operations.role",
                        CommandCenterOperationsMenu.CYCLE_MEMBER_ROLE, member, Optional.empty(), member.isPresent()),
                action("screen.galacticwars.operations.remove",
                        CommandCenterOperationsMenu.REMOVE_MEMBER, member, Optional.empty(), member.isPresent())));
    }

    private void addDiplomacyActions() {
        CommandCenterDashboardState state = menu.dashboardState();
        List<DiplomacyProposalSummary> proposals = incomingProposals();
        addSelector(selectorY(0), state.foreignKingdoms().size(),
                () -> foreignKingdomIndex = cycle(
                        foreignKingdomIndex, -1, state.foreignKingdoms().size()),
                () -> foreignKingdomIndex = cycle(
                        foreignKingdomIndex, 1, state.foreignKingdoms().size()));
        addSelector(selectorY(1), proposals.size(),
                () -> proposalIndex = cycle(proposalIndex, -1, proposals.size()),
                () -> proposalIndex = cycle(proposalIndex, 1, proposals.size()));
        Optional<UUID> kingdom = selected(state.foreignKingdoms(), foreignKingdomIndex)
                .map(ForeignKingdomSummary::kingdomId);
        Optional<UUID> proposal = selected(proposals, proposalIndex)
                .map(DiplomacyProposalSummary::proposalId);
        addActionGrid(afterSelectors(2), List.of(
                action("screen.galacticwars.operations.alliance",
                        CommandCenterOperationsMenu.PROPOSE_ALLIANCE, kingdom, Optional.empty(), kingdom.isPresent()),
                action("screen.galacticwars.operations.accept_diplomacy",
                        CommandCenterOperationsMenu.ACCEPT_DIPLOMACY, proposal, Optional.empty(), proposal.isPresent()),
                action("screen.galacticwars.operations.reject_diplomacy",
                        CommandCenterOperationsMenu.REJECT_DIPLOMACY, proposal, Optional.empty(), proposal.isPresent()),
                action("screen.galacticwars.operations.hostility",
                        CommandCenterOperationsMenu.DECLARE_HOSTILITY, kingdom, Optional.empty(), kingdom.isPresent()),
                action("screen.galacticwars.operations.embargo",
                        CommandCenterOperationsMenu.TOGGLE_EMBARGO, kingdom, Optional.empty(), kingdom.isPresent())));
    }

    private void addStorageActions() {
        addActionGrid(Math.max(bodyTop + 60, height - 36), List.of(action(
                "screen.galacticwars.operations.open_storage",
                CommandCenterOperationsMenu.STORAGE, true)));
    }

    private void addSelector(int y, int size, Runnable previous, Runnable next) {
        int buttonWidth = Math.min(48, Math.max(34, panelWidth / 7));
        Button previousButton = Button.builder(
                        Component.translatable("screen.galacticwars.operations.previous"), pressed -> {
                            previous.run();
                            rebuildWidgets();
                        })
                .bounds(panelLeft + 8, y, buttonWidth, controlHeight())
                .build();
        Button nextButton = Button.builder(
                        Component.translatable("screen.galacticwars.operations.next"), pressed -> {
                            next.run();
                            rebuildWidgets();
                        })
                .bounds(panelLeft + panelWidth - buttonWidth - 8, y, buttonWidth, controlHeight())
                .build();
        previousButton.active = size > 1;
        nextButton.active = size > 1;
        addRenderableWidget(previousButton);
        addRenderableWidget(nextButton);
    }

    private int controlHeight() {
        return height < 300 ? 16 : 20;
    }

    private int controlStride() {
        return controlHeight() + 2;
    }

    private int selectorY(int row) {
        return bodyTop + 18 + row * controlStride();
    }

    private int afterSelectors(int count) {
        return bodyTop + 22 + count * controlStride();
    }

    private void addActionGrid(int startY, List<ActionSpec> actions) {
        int columns = actions.size() > 10 && panelWidth >= 420 ? 3
                : actions.size() > 1 && panelWidth >= 280 ? 2 : 1;
        int gap = 6;
        int buttonWidth = (panelWidth - 16 - gap * (columns - 1)) / columns;
        int availableRows = Math.max(1, (height - 10 - startY) / controlStride());
        int unpagedCapacity = Math.max(1, columns * availableRows);
        boolean paged = actions.size() > unpagedCapacity;
        int actionRows = paged ? Math.max(1, availableRows - 1) : availableRows;
        int pageSize = Math.max(1, columns * actionRows);
        int pageCount = Math.max(1, (actions.size() + pageSize - 1) / pageSize);
        actionPage = normalize(actionPage, pageCount);
        int firstAction = actionPage * pageSize;
        int lastAction = Math.min(actions.size(), firstAction + pageSize);
        for (int index = firstAction; index < lastAction; index++) {
            ActionSpec action = actions.get(index);
            int visibleIndex = index - firstAction;
            int column = visibleIndex % columns;
            int row = visibleIndex / columns;
            Button button = Button.builder(Component.translatable(action.translationKey()), pressed ->
                            sendAction(action.actionId(), action.primaryTargetId(), action.secondaryTargetId()))
                    .bounds(panelLeft + 8 + column * (buttonWidth + gap),
                            startY + row * controlStride(), buttonWidth, controlHeight())
                    .build();
            boolean permitted = canUseAction(action.actionId());
            button.active = action.enabled() && permitted;
            if (!button.active) {
                button.setTooltip(Tooltip.create(permitted
                        ? action.disabledTooltip()
                        : reasonTooltip("permission_denied")));
            }
            addRenderableWidget(button);
        }
        if (paged) {
            int pagerY = startY + actionRows * controlStride();
            int pagerWidth = Math.min(150, (panelWidth - 22) / 2);
            Button previous = Button.builder(Component.translatable(
                            "screen.galacticwars.operations.actions.previous",
                            actionPage + 1, pageCount), pressed -> {
                        actionPage = cycle(actionPage, -1, pageCount);
                        rebuildWidgets();
                    })
                    .bounds(panelLeft + 8, pagerY, pagerWidth, controlHeight())
                    .build();
            Button next = Button.builder(Component.translatable(
                            "screen.galacticwars.operations.actions.next",
                            actionPage + 1, pageCount), pressed -> {
                        actionPage = cycle(actionPage, 1, pageCount);
                        rebuildWidgets();
                    })
                    .bounds(panelLeft + panelWidth - pagerWidth - 8,
                            pagerY, pagerWidth, controlHeight())
                    .build();
            addRenderableWidget(previous);
            addRenderableWidget(next);
        }
    }

    private boolean canUseAction(int actionId) {
        if (actionId == CommandCenterOperationsMenu.ACCEPT_INVITE
                || actionId == CommandCenterOperationsMenu.REJECT_INVITE) {
            return true;
        }
        String roleId = menu.dashboardState().actorRole();
        if (roleId.equals("visitor")) {
            return false;
        }
        KingdomMemberRole role = KingdomMemberRole.byId(roleId);
        KingdomPermission permission;
        if ((actionId >= CommandCenterOperationsMenu.CREATE_SQUAD
                && actionId <= CommandCenterOperationsMenu.FOLLOW_SQUAD)
                || (actionId >= CommandCenterOperationsMenu.HOLD_SQUAD
                && actionId <= CommandCenterOperationsMenu.SET_SQUAD_RALLY)
                || actionId == CommandCenterOperationsMenu.MERGE_SQUADS
                || actionId == CommandCenterOperationsMenu.PATROL_SQUAD) {
            permission = KingdomPermission.COMMAND_ARMY;
        } else if (actionId >= CommandCenterOperationsMenu.PREPARE_PROJECTOR_FIRST
                && actionId < CommandCenterOperationsMenu.PREPARE_PROJECTOR_FIRST + 4) {
            permission = KingdomPermission.BUILD;
        } else if (actionId == CommandCenterOperationsMenu.CANCEL_BUILD_PROJECT) {
            permission = KingdomPermission.BUILD;
        } else if (actionId >= CommandCenterOperationsMenu.RESUME_WORKER
                && actionId <= CommandCenterOperationsMenu.PAUSE_WORKER) {
            permission = KingdomPermission.MANAGE_WORKSITES;
        } else if (actionId == CommandCenterOperationsMenu.REGISTER_OUTPOST) {
            permission = KingdomPermission.MANAGE_CLAIMS;
        } else if (actionId == CommandCenterOperationsMenu.INVITE_NEAREST
                || actionId == CommandCenterOperationsMenu.CYCLE_MEMBER_ROLE
                || actionId == CommandCenterOperationsMenu.REMOVE_MEMBER) {
            permission = KingdomPermission.MANAGE_MEMBERS;
        } else if (actionId >= CommandCenterOperationsMenu.PROPOSE_ALLIANCE
                && actionId <= CommandCenterOperationsMenu.REJECT_DIPLOMACY) {
            permission = KingdomPermission.MANAGE_DIPLOMACY;
        } else if (actionId == CommandCenterOperationsMenu.NAVIGATION) {
            permission = KingdomPermission.TRAVEL;
        } else {
            permission = KingdomPermission.USE_STORAGE;
        }
        return KingdomPermissionPolicy.allows(role, permission);
    }

    private void sendAction(int actionId, Optional<UUID> primary, Optional<UUID> secondary) {
        if (actionId == CommandCenterOperationsMenu.PLAYER_CLASS) {
            Minecraft.getInstance().setScreenAndShow(new PlayerClassScreen(this));
            return;
        }
        GalacticNetwork.CHANNEL.sendToServer(new MenuActionPayload(
                UUID.randomUUID(), menu.containerId, actionId, primary, secondary,
                menu.dashboardState().contentGeneration(),
                menu.dashboardState().settlementRevision()));
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick
    ) {
        graphics.fill(0, 0, width, height, 0xC0080C12);
        graphics.fill(panelLeft, bodyTop - 4, panelLeft + panelWidth,
                Math.max(bodyTop, height - 8), PANEL);
        graphics.fill(panelLeft, bodyTop - 4, panelLeft + panelWidth, bodyTop - 3, PANEL_BORDER);
        graphics.fill(panelLeft, height - 9, panelLeft + panelWidth, height - 8, PANEL_BORDER);
        graphics.fill(panelLeft, bodyTop - 4, panelLeft + 1, height - 8, PANEL_BORDER);
        graphics.fill(panelLeft + panelWidth - 1, bodyTop - 4,
                panelLeft + panelWidth, height - 8, PANEL_BORDER);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        drawCentered(graphics, title, 9, ACCENT);
        CommandCenterDashboardState state = menu.dashboardState();
        if (!state.kingdomAvailable()) {
            drawCentered(graphics,
                    Component.translatable("screen.galacticwars.operations.no_kingdom"),
                    bodyTop + 9, WARNING);
            return;
        }
        switch (selectedTab) {
            case 0 -> renderOverview(graphics, state);
            case 1 -> renderCampaign(graphics, state);
            case 2 -> renderConstruction(graphics, state);
            case 3 -> renderSquads(graphics, state);
            case 4 -> renderWorkforce(graphics, state);
            case 5 -> renderKingdom(graphics, state);
            case 6 -> renderDiplomacy(graphics, state);
            case 7 -> renderStorage(graphics, state);
            case 8 -> renderTechnology(graphics);
            case 9 -> renderRelations(graphics);
            default -> { }
        }
    }

    private void renderOverview(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.overview.title"));
        drawLine(graphics, 1, Component.translatable("screen.galacticwars.operations.overview.identity",
                factionName(state.factionId()), humanize(state.actorRole())), TEXT);
        drawLine(graphics, 2, Component.translatable("screen.galacticwars.operations.overview.treasury",
                state.treasuryCredits(), state.pendingRewardCredits()), TEXT);
        drawLine(graphics, 3, Component.translatable("screen.galacticwars.operations.overview.population",
                state.recruitCount(), state.housingCapacity()), TEXT);
        drawLine(graphics, 4, Component.translatable("screen.galacticwars.operations.overview.domain",
                state.settlementCount(), state.claimCount(), state.squads().size()), MUTED);
        drawLine(graphics, 5, Component.translatable("screen.galacticwars.operations.overview.upkeep",
                state.upkeepPaid()
                        ? Component.translatable("screen.galacticwars.operations.status.ready")
                        : Component.translatable("screen.galacticwars.operations.status.unpaid")),
                state.upkeepPaid() ? GOOD : WARNING);
        state.nextObjective().ifPresent(objective -> drawLine(
                graphics, 6, Component.translatable(
                        "screen.galacticwars.operations.overview.next_objective",
                        objectiveInstruction(objective.objectiveId())), ACCENT));
    }

    private void renderCampaign(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.campaign.title"));
        Optional<QuestSummary> active = state.activeQuest();
        if (active.isEmpty()) {
            drawLine(graphics, 1,
                    Component.translatable(state.campaignVictory()
                            ? "screen.galacticwars.operations.campaign.victory"
                            : "screen.galacticwars.operations.campaign.complete"), GOOD);
            if (state.campaignVictory()) {
                drawLine(graphics, 2, Component.translatable(
                        "screen.galacticwars.operations.campaign.veteran_counts",
                        state.veteranVehicleDeployments(), state.veteranTrades(),
                        state.veteranRegionCaptures()), TEXT);
                drawCentered(graphics, clipped(Component.translatable(
                        "screen.galacticwars.operations.campaign.veteran_guidance"),
                        panelWidth - 20), bodyTop + 49, ACCENT);
            }
            state.activeForceTrainingQuest().ifPresent(training -> drawCentered(
                    graphics, clipped(Component.translatable(
                            "screen.galacticwars.operations.campaign.force_training",
                            questTitle(training.questId()), completedObjectives(training),
                            training.objectives().size()), panelWidth - 20),
                    bodyTop + 60, MUTED));
            return;
        }
        QuestSummary quest = active.orElseThrow();
        drawLine(graphics, 1, Component.translatable("screen.galacticwars.operations.campaign.active",
                questTitle(quest.questId()), quest.rewardCredits()), TEXT);
        drawCentered(graphics, clipped(Component.translatable(
                "quest.galacticwars." + path(quest.questId()) + ".briefing"), panelWidth - 20),
                bodyTop + 29, MUTED);
        int line = 3;
        for (var objective : quest.objectives()) {
            if (line > 5) break;
            drawLine(graphics, line++, Component.literal(
                    (objective.complete() ? "[x] " : "[ ] ") + humanize(objective.objectiveId())
                            + " " + objective.currentCount() + "/" + objective.requiredCount()),
                    objective.complete() ? GOOD : MUTED);
        }
        state.nextObjective().ifPresent(objective -> drawCentered(
                graphics, clipped(objectiveInstruction(objective.objectiveId()), panelWidth - 20),
                bodyTop + 73, ACCENT));
        if (!quest.unlocks().isEmpty()) {
            drawCentered(graphics, clipped(Component.translatable(
                    "screen.galacticwars.operations.campaign.unlocks",
                    quest.unlocks().stream().map(CommandCenterOperationsScreen::humanize)
                            .reduce((left, right) -> left + ", " + right).orElse("")), panelWidth - 20),
                    bodyTop + 84, MUTED);
        }
        state.activeForceTrainingQuest().ifPresent(training -> drawCentered(
                graphics, clipped(Component.translatable(
                        "screen.galacticwars.operations.campaign.force_training",
                        questTitle(training.questId()), completedObjectives(training),
                        training.objectives().size()), panelWidth - 20),
                bodyTop + 95, MUTED));
    }

    private void renderConstruction(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.construction.title"));
        drawSelectorLabel(graphics, selectorY(0),
                Component.translatable("screen.galacticwars.operations.selector.blueprint",
                        selected(state.blueprints(), blueprintIndex).map(this::blueprintLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(1),
                Component.translatable("screen.galacticwars.operations.selector.builder",
                        selected(state.constructionBuilderIds(), constructionBuilderIndex)
                                .map(id -> Component.literal(shortId(id)))
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(2),
                Component.translatable("screen.galacticwars.operations.selector.rotation",
                        selected(state.blueprints(), blueprintIndex)
                                .flatMap(value -> selected(value.allowedRotations(), rotationIndex))
                                .orElse(0)));
        drawSelectorLabel(graphics, selectorY(3),
                Component.translatable("screen.galacticwars.operations.selector.build",
                        selected(state.builds(), buildIndex).map(this::buildLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(4),
                Component.translatable("screen.galacticwars.operations.selector.work_order",
                        selected(state.workOrders(), workOrderIndex).map(this::workOrderLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
    }

    private void renderSquads(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.squads.title",
                state.squads().size(), state.commandCandidateIds().size()));
        drawSelectorLabel(graphics, selectorY(0),
                Component.translatable("screen.galacticwars.operations.selector.squad",
                        selectedSquad().map(this::squadLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(1),
                Component.translatable("screen.galacticwars.operations.selector.merge_target",
                        selected(secondarySquads(), secondarySquadIndex).map(this::squadLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(2),
                Component.translatable("screen.galacticwars.operations.selector.commander",
                        selected(state.commandCandidateIds(), commandCandidateIndex)
                                .map(id -> Component.literal(shortId(id)))
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(3),
                Component.translatable("screen.galacticwars.operations.selector.split_member",
                        selectedSquad().flatMap(squad -> selected(squad.memberIds(), splitMemberIndex))
                                .map(id -> Component.literal(shortId(id)))
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(4),
                Component.translatable("screen.galacticwars.operations.selector.combat_target",
                        selected(state.combatTargets(), combatTargetIndex).map(this::combatTargetLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
    }

    private void renderWorkforce(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable(
                "screen.galacticwars.operations.workforce.title", state.workers().size()));
        drawSelectorLabel(graphics, selectorY(0),
                Component.translatable("screen.galacticwars.operations.selector.worker",
                        selected(state.workers(), workerIndex).map(this::workerLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(1),
                Component.translatable("screen.galacticwars.operations.selector.work_order",
                        selected(state.workOrders(), workOrderIndex).map(this::workOrderLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        selected(state.workers(), workerIndex).ifPresent(worker -> drawCentered(
                graphics, clipped(workerDetails(worker), panelWidth - 20),
                afterSelectors(2) - 9,
                worker.phase().equals("blocked") ? WARNING : MUTED));
    }

    private void renderKingdom(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.kingdom.title",
                state.members().size(), state.settlementCount()));
        List<MemberSummary> members = manageableMembers();
        List<NearbyPlayerSummary> nearbyPlayers = state.nearbyPlayers();
        List<InviteSummary> invites = incomingInvites();
        drawSelectorLabel(graphics, selectorY(0),
                Component.translatable("screen.galacticwars.operations.selector.member",
                        selected(members, memberIndex).map(this::memberLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(1),
                Component.translatable("screen.galacticwars.operations.selector.invite_target",
                        selected(nearbyPlayers, nearbyPlayerIndex).map(this::nearbyPlayerLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(2),
                Component.translatable("screen.galacticwars.operations.selector.invite",
                        selected(invites, inviteIndex).map(this::inviteLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(3),
                Component.translatable("screen.galacticwars.operations.selector.claim",
                        selected(state.claims(), claimIndex).map(this::claimLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        state.conflicts().stream().filter(conflict -> conflict.state().equals("active"))
                .findFirst().or(() -> state.conflicts().stream().findFirst())
                .ifPresent(conflict -> drawCentered(graphics,
                        clipped(conflictLabel(conflict), panelWidth - 20),
                        afterSelectors(4) - 9,
                        conflict.state().equals("active") ? WARNING : MUTED));
    }

    private void renderDiplomacy(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.diplomacy.title"));
        List<DiplomacyProposalSummary> proposals = incomingProposals();
        drawSelectorLabel(graphics, selectorY(0),
                Component.translatable("screen.galacticwars.operations.selector.foreign_kingdom",
                        selected(state.foreignKingdoms(), foreignKingdomIndex)
                                .map(this::foreignKingdomLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
        drawSelectorLabel(graphics, selectorY(1),
                Component.translatable("screen.galacticwars.operations.selector.proposal",
                        selected(proposals, proposalIndex).map(this::proposalLabel)
                                .orElse(Component.translatable("screen.galacticwars.operations.none"))));
    }

    private void renderStorage(GuiGraphicsExtractor graphics, CommandCenterDashboardState state) {
        drawHeading(graphics, Component.translatable("screen.galacticwars.operations.storage.title"));
        drawLine(graphics, 1, Component.translatable("screen.galacticwars.operations.storage.summary",
                state.treasuryCredits(), state.upkeepPaid()
                        ? Component.translatable("screen.galacticwars.operations.status.ready")
                        : Component.translatable("screen.galacticwars.operations.status.unpaid")), TEXT);
        drawLine(graphics, 2,
                Component.translatable("screen.galacticwars.operations.storage.hint"), MUTED);
    }

    private void renderTechnology(GuiGraphicsExtractor graphics) {
        drawHeading(graphics, Component.translatable(
                "screen.galacticwars.operations.technology.title"));
        List<GameplayCatalogPayload.TechnologyEntry> nodes = technologyEntries();
        var node = selected(nodes, technologyIndex).orElse(null);
        var projection = gameplayProjection().orElse(null);
        drawSelectorLabel(graphics, selectorY(0), node == null
                ? Component.translatable("screen.galacticwars.operations.none")
                : Component.literal(node.displayName()));
        List<WorkerSummary> technicians = technicians();
        drawSelectorLabel(graphics, selectorY(1), selected(technicians, workerIndex)
                .map(this::workerLabel)
                .orElse(Component.translatable("screen.galacticwars.operations.none")));
        if (node == null || projection == null) {
            return;
        }
        String lock = technologyLockReason(node, projection);
        drawCentered(graphics, clipped(Component.translatable(
                        "screen.galacticwars.operations.technology.status",
                        humanize(lock)), panelWidth - 20),
                afterSelectors(2) - 9, lock.equals("completed") ? GOOD
                        : lock.equals("available") ? ACCENT : WARNING);
        String costs = node.costs().stream()
                .map(cost -> {
                    int delivered = projection.getActiveResearchNode().equals(node.nodeId())
                            ? projection.getDeliveredResearchInputs()
                                    .getOrDefault(cost.itemId(), 0) : 0;
                    return humanize(path(cost.itemId())) + " " + delivered + "/" + cost.count();
                })
                .reduce((left, right) -> left + ", " + right).orElse("");
        drawCentered(graphics, clipped(Component.translatable(
                        "screen.galacticwars.operations.technology.costs", costs),
                        panelWidth - 20),
                afterSelectors(2) + 2, MUTED);
        String recipes = node.recipeIds().stream().map(id -> humanize(path(id)))
                .reduce((left, right) -> left + ", " + right).orElse("-");
        drawCentered(graphics, clipped(Component.translatable(
                        "screen.galacticwars.operations.technology.recipes", recipes),
                        panelWidth - 20),
                afterSelectors(2) + 13, MUTED);
        if (projection.getActiveResearchNode().equals(node.nodeId())) {
            drawCentered(graphics, clipped(Component.translatable(
                            "screen.galacticwars.operations.technology.progress",
                            projection.getResearchProgress(),
                            projection.getResearchRequired(),
                            projection.getTechnicianId().isEmpty()
                                    ? Component.translatable("screen.galacticwars.operations.none")
                                    : Component.literal(projection.getTechnicianId())),
                            panelWidth - 20),
                    afterSelectors(2) + 24, TEXT);
        }
    }

    private void renderRelations(GuiGraphicsExtractor graphics) {
        drawHeading(graphics, Component.translatable(
                "screen.galacticwars.operations.relations.title"));
        var projection = gameplayProjection().orElse(null);
        if (projection == null) {
            return;
        }
        int row = 1;
        for (var policy : ClientGameplayCatalog.snapshot().relationPolicies().values()) {
            int score = projection.getReputation().getOrDefault(policy.factionId(), 0);
            String tier = relationTier(score, policy);
            int next = nextRelationThreshold(score, policy);
            boolean recruitment = score >= policy.recruitmentThreshold();
            long licenses = projection.getEffectiveCapabilities().stream()
                    .filter(capability -> capability.startsWith("recipe:"))
                    .map(capability -> capability.substring("recipe:".length()))
                    .filter(recipe -> ClientGameplayCatalog.snapshot().fabrication(recipe)
                            .filter(entry -> entry.factionId().equals(policy.factionId())).isPresent())
                    .count();
            drawLine(graphics, row++, Component.translatable(
                    "screen.galacticwars.operations.relations.row",
                    factionName(policy.factionId()), score, humanize(tier), next,
                    tier.equals("friendly") ? policy.friendlyTradePricePercent() : 100,
                    recruitment ? Component.translatable("screen.galacticwars.operations.status.ready")
                            : Component.translatable("screen.galacticwars.operations.status.locked"),
                    licenses), tier.equals("hostile") ? WARNING
                            : tier.equals("friendly") ? GOOD : TEXT);
        }
    }

    private String technologyLockReason(
            GameplayCatalogPayload.TechnologyEntry node,
            PlayerCampaignAttachmentState.GameplayProjection projection
    ) {
        if (projection.getCompletedTechnology().contains(node.nodeId())) {
            return "completed";
        }
        if (!projection.getActiveResearchNode().isEmpty()) {
            return projection.getActiveResearchNode().equals(node.nodeId())
                    ? "researching" : "another_project_active";
        }
        if (!projection.getCompletedTechnology().containsAll(node.prerequisites())) {
            return "prerequisites_missing";
        }
        return canManageTechnology() ? "available" : "officer_required";
    }

    private static String relationTier(
            int score,
            GameplayCatalogPayload.RelationPolicyEntry policy
    ) {
        if (score >= policy.friendlyThreshold()) return "friendly";
        if (score >= policy.neutralThreshold()) return "neutral";
        if (score >= policy.waryThreshold()) return "wary";
        return "hostile";
    }

    private static int nextRelationThreshold(
            int score,
            GameplayCatalogPayload.RelationPolicyEntry policy
    ) {
        if (score < policy.waryThreshold()) return policy.waryThreshold();
        if (score < policy.neutralThreshold()) return policy.neutralThreshold();
        if (score < policy.friendlyThreshold()) return policy.friendlyThreshold();
        return 100;
    }

    private List<GameplayCatalogPayload.TechnologyEntry> technologyEntries() {
        String faction = menu.dashboardState().factionId();
        return ClientGameplayCatalog.snapshot().technology().values().stream()
                .filter(node -> node.factionId().equals("galacticwars:universal")
                        || node.factionId().equals(faction))
                .toList();
    }

    private List<WorkerSummary> technicians() {
        return menu.dashboardState().workers().stream()
                .filter(worker -> worker.profession().equals("technician"))
                .toList();
    }

    private Optional<PlayerCampaignAttachmentState.GameplayProjection> gameplayProjection() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PlayerCampaignAttachmentRuntime.get(player))
                .map(PlayerCampaignAttachmentState::getGameplay);
    }

    private boolean canManageTechnology() {
        try {
            return KingdomPermissionPolicy.allows(
                    KingdomMemberRole.valueOf(
                            menu.dashboardState().actorRole().toUpperCase(Locale.ROOT)),
                    KingdomPermission.MANAGE_TECHNOLOGY);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void drawHeading(GuiGraphicsExtractor graphics, Component heading) {
        drawCentered(graphics, heading, bodyTop + 7, ACCENT);
    }

    private void drawLine(GuiGraphicsExtractor graphics, int row, Component line, int color) {
        graphics.text(font, line, panelLeft + 10, bodyTop + 7 + row * 11, color);
    }

    private void drawSelectorLabel(GuiGraphicsExtractor graphics, int y, Component label) {
        int availableWidth = panelWidth - Math.min(96, Math.max(68, panelWidth * 2 / 7)) - 24;
        Component display = font.width(label) <= availableWidth
                ? label : Component.literal(font.plainSubstrByWidth(label.getString(), availableWidth - 8) + "...");
        drawCentered(graphics, display, y + 6, TEXT);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, (width - font.width(text)) / 2, y, color);
    }

    private Component squadLabel(SquadSummary squad) {
        return Component.literal(squad.name() + " | " + squad.unitCount() + " | "
                + humanize(squad.order()) + " | " + squad.supplyUnits());
    }

    private Component buildLabel(BuildSummary build) {
        String progress = build.completedPlacements() + "/" + build.totalPlacements();
        String blocked = build.blockedReason().isBlank()
                ? "" : " | " + reasonText(build.blockedReason()).getString();
        return Component.literal(humanize(build.blueprintId()) + " | "
                + humanize(build.state()) + " | " + progress + blocked);
    }

    private Component blueprintLabel(BlueprintSummary blueprint) {
        String rewards = "H+" + blueprint.housingReward()
                + " S+" + blueprint.storageSlotReward()
                + " C+" + blueprint.commanderSlotReward();
        return Component.literal(blueprint.displayName() + " | "
                + blueprint.placementCount() + " blocks | " + rewards);
    }

    private Component workOrderLabel(WorkOrderSummary order) {
        return Component.literal(humanize(order.type()) + " | " + humanize(order.state())
                + " | " + order.completedQuantity() + "/" + order.quantity());
    }

    private Component workerLabel(WorkerSummary worker) {
        return Component.literal(worker.displayName() + " | " + humanize(worker.profession())
                + " | " + humanize(worker.phase()) + ": " + reasonText(worker.reasonCode()).getString()
                + " | " + worker.distanceBlocks() + "m");
    }

    private Component workerDetails(WorkerSummary worker) {
        String worksite = worker.worksite().map(this::positionLabel)
                .map(value -> value + " r" + worker.workRadius()).orElse("-");
        String storage = worker.storage().map(this::positionLabel).orElse("-");
        String target = worker.activeTarget().map(this::positionLabel).orElse("-");
        return Component.translatable("screen.galacticwars.operations.workforce.details",
                worksite, storage, target, worker.carriedItemCount(), worker.storageItemCount());
    }

    private Component objectiveInstruction(String objectiveId) {
        return Component.translatable(
                "screen.galacticwars.operations.objective." + path(objectiveId));
    }

    private Component questTitle(String questId) {
        return Component.translatable("quest.galacticwars." + path(questId) + ".title");
    }

    private static long completedObjectives(QuestSummary quest) {
        return quest.objectives().stream().filter(objective -> objective.complete()).count();
    }

    private String positionLabel(PositionSummary position) {
        return humanize(position.dimensionId()) + " "
                + position.x() + "," + position.y() + "," + position.z();
    }

    private Component combatTargetLabel(CombatTargetSummary target) {
        return Component.literal(target.displayName() + " | " + humanize(target.factionId())
                + " | " + target.distanceBlocks() + "m");
    }

    private Component memberLabel(MemberSummary member) {
        return Component.literal(shortId(member.playerId()) + " | " + humanize(member.role()));
    }

    private Component nearbyPlayerLabel(NearbyPlayerSummary player) {
        return Component.literal(player.displayName() + " | " + player.distanceBlocks() + "m");
    }

    private Component claimLabel(ClaimSummary claim) {
        String kind = claim.capital()
                ? Component.translatable("screen.galacticwars.operations.claim.capital").getString()
                : Component.translatable("screen.galacticwars.operations.claim.outpost").getString();
        return Component.literal(kind + " | " + humanize(claim.dimensionId()) + " | "
                + claim.centerChunkX() + "," + claim.centerChunkZ() + " | " + claim.chunkCount());
    }

    private Component conflictLabel(
            galacticwars.clonewars.kingdom.CommandCenterDashboardState.ConflictSummary conflict
    ) {
        long remainingTicks = Math.max(0L,
                conflict.endsAt() - menu.dashboardState().generatedGameTime());
        return Component.translatable("screen.galacticwars.operations.kingdom.conflict",
                Component.translatable("screen.galacticwars.operations.kingdom.conflict."
                        + conflict.type()),
                Component.translatable("screen.galacticwars.operations.kingdom.conflict.state."
                        + conflict.state()),
                humanize(conflict.dimensionId()), conflict.x(), conflict.z(),
                conflict.progress(), conflict.goal(), remainingTicks / 20L);
    }

    private Component inviteLabel(InviteSummary invite) {
        return Component.literal(shortId(invite.inviterId()) + " | " + humanize(invite.offeredRole()));
    }

    private Component foreignKingdomLabel(ForeignKingdomSummary kingdom) {
        return Component.literal(factionName(kingdom.factionId()).getString()
                + " | " + shortId(kingdom.kingdomId()));
    }

    private Component proposalLabel(DiplomacyProposalSummary proposal) {
        return Component.literal(humanize(proposal.relation()) + " | "
                + shortId(proposal.proposerKingdomId()));
    }

    private Optional<SquadSummary> selectedSquad() {
        return selected(menu.dashboardState().squads(), squadIndex);
    }

    private List<SquadSummary> secondarySquads() {
        Optional<UUID> selectedId = selectedSquad().map(SquadSummary::id);
        return menu.dashboardState().squads().stream()
                .filter(squad -> selectedId.filter(squad.id()::equals).isEmpty())
                .toList();
    }

    private List<MemberSummary> manageableMembers() {
        CommandCenterDashboardState state = menu.dashboardState();
        return state.members().stream()
                .filter(member -> !member.playerId().equals(state.actorId()))
                .filter(member -> !member.role().equals("owner"))
                .toList();
    }

    private List<InviteSummary> incomingInvites() {
        UUID actorId = menu.dashboardState().actorId();
        return menu.dashboardState().invites().stream()
                .filter(invite -> invite.targetPlayerId().equals(actorId))
                .toList();
    }

    private List<DiplomacyProposalSummary> incomingProposals() {
        UUID kingdomId = menu.dashboardState().kingdomId();
        return menu.dashboardState().diplomacyProposals().stream()
                .filter(proposal -> proposal.targetKingdomId().equals(kingdomId))
                .toList();
    }

    private Optional<UUID> selectedCompletedOutpost() {
        List<BuildSummary> candidates = menu.dashboardState().builds().stream()
                .filter(build -> build.state().equals("completed"))
                .filter(build -> path(build.blueprintId()).equals("forward_base"))
                .toList();
        return selected(candidates, buildIndex).map(BuildSummary::id);
    }

    private void normalizeSelections() {
        CommandCenterDashboardState state = menu.dashboardState();
        squadIndex = normalize(squadIndex, state.squads().size());
        secondarySquadIndex = normalize(secondarySquadIndex, secondarySquads().size());
        commandCandidateIndex = normalize(commandCandidateIndex, state.commandCandidateIds().size());
        splitMemberIndex = normalize(splitMemberIndex,
                selectedSquad().map(squad -> squad.memberIds().size()).orElse(0));
        combatTargetIndex = normalize(combatTargetIndex, state.combatTargets().size());
        claimIndex = normalize(claimIndex, state.claims().size());
        buildIndex = normalize(buildIndex, state.builds().size());
        workOrderIndex = normalize(workOrderIndex, state.workOrders().size());
        workerIndex = normalize(workerIndex, state.workers().size());
        blueprintIndex = normalize(blueprintIndex, state.blueprints().size());
        constructionBuilderIndex = normalize(
                constructionBuilderIndex, state.constructionBuilderIds().size());
        rotationIndex = normalize(rotationIndex, selected(state.blueprints(), blueprintIndex)
                .map(value -> value.allowedRotations().size()).orElse(0));
        memberIndex = normalize(memberIndex, manageableMembers().size());
        nearbyPlayerIndex = normalize(nearbyPlayerIndex, state.nearbyPlayers().size());
        inviteIndex = normalize(inviteIndex, incomingInvites().size());
        foreignKingdomIndex = normalize(foreignKingdomIndex, state.foreignKingdoms().size());
        proposalIndex = normalize(proposalIndex, incomingProposals().size());
    }

    private static int cycle(int current, int amount, int size) {
        if (size < 1) return 0;
        return Math.floorMod(current + amount, size);
    }

    private static int normalize(int current, int size) {
        return size < 1 ? 0 : Math.min(Math.max(current, 0), size - 1);
    }

    private static <T> Optional<T> selected(List<T> values, int index) {
        return values.isEmpty() ? Optional.empty()
                : Optional.of(values.get(normalize(index, values.size())));
    }

    private Component clipped(Component value, int availableWidth) {
        if (font.width(value) <= availableWidth) {
            return value;
        }
        return Component.literal(font.plainSubstrByWidth(
                value.getString(), Math.max(8, availableWidth - 8)) + "...");
    }

    private static ActionSpec action(String key, int id, boolean enabled) {
        return action(key, id, Optional.empty(), Optional.empty(), enabled);
    }

    private static ActionSpec action(String key, int id, ActionAvailability availability) {
        return action(key, id, availability, reasonTooltip(availability.reason()));
    }

    private static ActionSpec action(
            String key,
            int id,
            ActionAvailability availability,
            Component disabledTooltip
    ) {
        return new ActionSpec(
                key, id, Optional.empty(), Optional.empty(), availability.available(),
                disabledTooltip);
    }

    private static ActionSpec action(
            String key, int id, Optional<UUID> primary, Optional<UUID> secondary, boolean enabled
    ) {
        return new ActionSpec(
                key, id, primary, secondary, enabled,
                enabled ? Component.empty() : reasonTooltip("selection_required"));
    }

    private static Component fabricationTooltip(
            VehicleFabricationSummary fabrication, int treasuryCredits
    ) {
        String materialStock = fabrication.materials().stream()
                .map(material -> humanize(material.itemId()) + " "
                        + material.available() + "/" + material.required())
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
        return Component.translatable(
                "screen.galacticwars.operations.fabrication.disabled",
                reasonTooltip(fabrication.availability().reason()),
                treasuryCredits,
                fabrication.requiredCredits(),
                materialStock);
    }

    private static Component reasonTooltip(String reason) {
        return Component.translatable("reason.galacticwars.operations." + reason);
    }

    /**
     * Blocked/status reason codes are curated into {@code reason.galacticwars.operations.*}
     * as they are surfaced to players. Codes without a curated key still fall back to a
     * readable humanized string instead of leaking the raw key or snake_case identifier.
     */
    private static Component reasonText(String reason) {
        String key = "reason.galacticwars.operations." + reason;
        return net.minecraft.locale.Language.getInstance().has(key)
                ? Component.translatable(key)
                : Component.literal(humanize(reason));
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    private static String humanize(String value) {
        String normalized = path(value == null ? "" : value.trim().toLowerCase(Locale.ROOT))
                .replace('_', ' ').replace('/', ' ');
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            result.append(capitalize ? Character.toUpperCase(character) : character);
            capitalize = character == ' ';
        }
        return result.toString();
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private static Component factionName(String factionId) {
        return Component.translatable("faction.galacticwars." + path(factionId));
    }

    @Override
    public CommandCenterOperationsMenu getMenu() {
        return menu;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        super.onClose();
    }

    private record ActionSpec(
            String translationKey,
            int actionId,
            Optional<UUID> primaryTargetId,
            Optional<UUID> secondaryTargetId,
            boolean enabled,
            Component disabledTooltip
    ) {
        private ActionSpec {
            disabledTooltip = disabledTooltip == null ? Component.empty() : disabledTooltip;
        }
    }
}
