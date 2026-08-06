package galacticwars.clonewars.technology;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.workforce.WorkerProfession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class KingdomResearchService {
    private KingdomResearchService() {
    }

    public static ResearchResult start(
            ServerPlayer player,
            CommandCenterBlockEntity commandCenter,
            String nodeId,
            UUID replayId,
            int expectedRevision
    ) {
        Context context = context(player, commandCenter);
        if (context == null || !commandCenter.canUse(player, KingdomPermission.MANAGE_TECHNOLOGY)) {
            return ResearchResult.rejected("permission", -1);
        }
        KingdomTechnologyState state = context.data.technologyStateOrDefault(context.kingdom.id());
        if (state.revision() != expectedRevision) {
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        if (state.activeProject().filter(project -> project.hasReplay(replayId)).isPresent()) {
            return ResearchResult.replay(state.revision());
        }
        TechnologyNodeDefinition node = GameplayDataManager.snapshot().technology().node(nodeId).orElse(null);
        if (node == null || state.completed(nodeId) || state.activeProject().isPresent()
                || (!node.factionId().equals(TechnologyCatalog.UNIVERSAL)
                        && !node.factionId().equals(context.kingdom.factionId()))
                || !state.completedNodes().containsAll(node.prerequisites())) {
            return ResearchResult.rejected("locked", state.revision());
        }
        KingdomTechnologyState updated = state.withProject(KingdomResearchProject.start(node, replayId));
        if (!context.data.storeTechnologyState(updated, state.revision())) {
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        synchronizeMembers(context.level, context.kingdom);
        return ResearchResult.success(updated.revision());
    }

    public static ResearchResult contribute(
            ServerPlayer player,
            CommandCenterBlockEntity commandCenter,
            UUID replayId,
            int expectedRevision
    ) {
        Context context = context(player, commandCenter);
        if (context == null) {
            return ResearchResult.rejected("not_member", -1);
        }
        KingdomTechnologyState state = context.data.technologyStateOrDefault(context.kingdom.id());
        KingdomResearchProject project = state.activeProject().orElse(null);
        if (state.revision() != expectedRevision) {
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        if (project == null) {
            return ResearchResult.rejected("no_project", state.revision());
        }
        if (project.hasReplay(replayId)) {
            return ResearchResult.replay(state.revision());
        }
        LinkedHashMap<String, Integer> remaining = new LinkedHashMap<>();
        project.requiredInputs().forEach((item, count) -> {
            int needed = count - project.deliveredInputs().getOrDefault(item, 0);
            if (needed > 0) {
                remaining.put(item, needed);
            }
        });
        Map<String, Integer> contribution = commandCenter.availableResearchContribution(remaining);
        if (contribution.isEmpty()) {
            return ResearchResult.rejected("no_materials", state.revision());
        }
        KingdomTechnologyState updated = state.withProject(
                project.withContribution(contribution, replayId));
        if (!context.data.storeTechnologyState(updated, state.revision())) {
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        if (!commandCenter.consumeResearchContribution(contribution)) {
            throw new IllegalStateException("Committed research contribution was no longer present");
        }
        synchronizeMembers(context.level, context.kingdom);
        return ResearchResult.success(updated.revision());
    }

    public static ResearchResult assignTechnician(
            ServerPlayer player,
            CommandCenterBlockEntity commandCenter,
            UUID technicianId,
            UUID replayId,
            int expectedRevision
    ) {
        Context context = context(player, commandCenter);
        if (context == null || !commandCenter.canUse(player, KingdomPermission.MANAGE_TECHNOLOGY)) {
            return ResearchResult.rejected("permission", -1);
        }
        KingdomTechnologyState state = context.data.technologyStateOrDefault(context.kingdom.id());
        KingdomResearchProject project = state.activeProject().orElse(null);
        if (state.revision() != expectedRevision || project == null) {
            return ResearchResult.rejected(
                    project == null ? "no_project" : "stale_revision", state.revision());
        }
        if (project.hasReplay(replayId)) {
            return ResearchResult.replay(state.revision());
        }
        GalacticRecruitEntity recruit = technicianCandidate(
                context.level, context.data, context.kingdom, technicianId);
        if (recruit == null) {
            return ResearchResult.rejected("invalid_technician", state.revision());
        }
        UUID previousTechnicianId = project.technicianId().orElse(null);
        KingdomTechnologyState updated = state.withProject(project.withTechnician(technicianId, replayId));
        if (!context.data.storeTechnologyState(updated, state.revision())) {
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        if (previousTechnicianId != null && !previousTechnicianId.equals(technicianId)
                && context.level.getEntity(previousTechnicianId) instanceof GalacticRecruitEntity previous) {
            previous.stopTechnologyResearch(commandCenter.getBlockPos());
        }
        if (!recruit.beginTechnologyResearch(commandCenter.getBlockPos())) {
            throw new IllegalStateException("Committed research technician could not enter the research work loop");
        }
        synchronizeMembers(context.level, context.kingdom);
        return ResearchResult.success(updated.revision());
    }

    public static ResearchResult cancel(
            ServerPlayer player,
            CommandCenterBlockEntity commandCenter,
            UUID replayId,
            int expectedRevision
    ) {
        Context context = context(player, commandCenter);
        if (context == null || !commandCenter.canUse(player, KingdomPermission.MANAGE_TECHNOLOGY)) {
            return ResearchResult.rejected("permission", -1);
        }
        KingdomTechnologyState state = context.data.technologyStateOrDefault(context.kingdom.id());
        KingdomResearchProject project = state.activeProject().orElse(null);
        if (state.revision() != expectedRevision || project == null) {
            return ResearchResult.rejected(
                    project == null ? "no_project" : "stale_revision", state.revision());
        }
        if (project.hasReplay(replayId)) {
            return ResearchResult.replay(state.revision());
        }
        if (!commandCenter.refundResearchMaterials(project.deliveredInputs())) {
            return ResearchResult.rejected("storage_full", state.revision());
        }
        KingdomTechnologyState updated = state.cancelProject();
        if (!context.data.storeTechnologyState(updated, state.revision())) {
            if (!commandCenter.consumeResearchContribution(project.deliveredInputs())) {
                throw new IllegalStateException("Could not roll back research cancellation refund");
            }
            return ResearchResult.rejected("stale_revision", state.revision());
        }
        project.technicianId().map(context.level::getEntity)
                .filter(GalacticRecruitEntity.class::isInstance)
                .map(GalacticRecruitEntity.class::cast)
                .ifPresent(recruit -> recruit.stopTechnologyResearch(commandCenter.getBlockPos()));
        synchronizeMembers(context.level, context.kingdom);
        return ResearchResult.success(updated.revision());
    }

    public static boolean tick(ServerLevel level, CommandCenterBlockEntity commandCenter) {
        UUID ownerId = commandCenter.ownerId();
        if (ownerId == null) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElse(null);
        if (kingdom == null || !isAuthoritativeCommandCenter(level, data, kingdom, commandCenter)) {
            return false;
        }
        KingdomTechnologyState state = data.technologyStateOrDefault(kingdom.id());
        KingdomResearchProject project = state.activeProject().orElse(null);
        if (project == null || !project.materialsComplete()
                || !project.definitionHash().equals(GameplayDataManager.snapshot().technology()
                        .node(project.nodeId()).map(TechnologyNodeDefinition::definitionHash).orElse(""))) {
            return false;
        }
        UUID technicianId = project.technicianId().orElse(null);
        GalacticRecruitEntity technician = technicianCandidate(
                level, data, kingdom, technicianId);
        if (technician == null || !technician.isActivelyResearchingAt(commandCenter.getBlockPos())) {
            return false;
        }
        TechnologyNodeDefinition node = GameplayDataManager.snapshot().technology()
                .node(project.nodeId()).orElseThrow();
        KingdomResearchProject progressed = project.withWork(20);
        boolean completed = progressed.workProgress() >= node.requiredWork();
        KingdomTechnologyState updated = completed
                ? state.withProject(progressed).completeProject()
                : state.withProject(progressed);
        boolean stored = data.storeTechnologyState(updated, state.revision());
        if (stored) {
            if (completed) {
                technician.stopTechnologyResearch(commandCenter.getBlockPos());
            }
            synchronizeMembers(level, kingdom);
        }
        return stored;
    }

    private static GalacticRecruitEntity technicianCandidate(
            ServerLevel level,
            KingdomSavedData data,
            KingdomRecord kingdom,
            UUID technicianId
    ) {
        if (technicianId == null || data.kingdomForRecruit(technicianId)
                .filter(candidate -> candidate.id().equals(kingdom.id())).isEmpty()
                || !(level.getEntity(technicianId) instanceof GalacticRecruitEntity recruit)
                || !recruit.isAlive()
                || !recruit.isTame()
                || recruit.getWorkerProfession().filter(WorkerProfession.TECHNICIAN::equals).isEmpty()) {
            return null;
        }
        return recruit;
    }

    private static Context context(ServerPlayer player, CommandCenterBlockEntity commandCenter) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(commandCenter, "commandCenter");
        if (!(player.level() instanceof ServerLevel level)
                || commandCenter.getLevel() != level
                || player.distanceToSqr(
                        commandCenter.getBlockPos().getX() + 0.5D,
                        commandCenter.getBlockPos().getY() + 0.5D,
                        commandCenter.getBlockPos().getZ() + 0.5D) > 64.0D
                || commandCenter.ownerId() == null) {
            return null;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.kingdomForOwner(commandCenter.ownerId()).orElse(null);
        if (kingdom == null || data.kingdomForPlayer(player.getUUID())
                .filter(candidate -> candidate.id().equals(kingdom.id())).isEmpty()
                || !isAuthoritativeCommandCenter(level, data, kingdom, commandCenter)) {
            return null;
        }
        return new Context(level, data, kingdom);
    }

    private static boolean isAuthoritativeCommandCenter(
            ServerLevel level,
            KingdomSavedData data,
            KingdomRecord kingdom,
            CommandCenterBlockEntity commandCenter
    ) {
        BlockPos hallPos = commandCenter.getBlockPos();
        return commandCenter.getLevel() == level
                && data.isHallActive(kingdom.ownerId())
                && kingdom.ownerId().equals(commandCenter.ownerId())
                && kingdom.settlement().dimensionId().equals(level.dimension().identifier().toString())
                && kingdom.settlement().hallX() == hallPos.getX()
                && kingdom.settlement().hallY() == hallPos.getY()
                && kingdom.settlement().hallZ() == hallPos.getZ()
                && level.getBlockEntity(hallPos) == commandCenter;
    }

    private static void synchronizeMembers(ServerLevel level, KingdomRecord kingdom) {
        kingdom.members().forEach(member -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                galacticwars.clonewars.progression.PlayerCampaignAttachmentRuntime.synchronize(player);
            }
        });
    }

    private record Context(ServerLevel level, KingdomSavedData data, KingdomRecord kingdom) {
    }
}
