package galacticwars.clonewars.force;

import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.economy.CreditTransactionService;
import galacticwars.clonewars.network.ForceProgressionActionPayload;
import galacticwars.clonewars.network.ForceProgressionPayload;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.progression.ForceProgressionService;
import galacticwars.clonewars.progression.ForceRuntimeState;
import galacticwars.clonewars.progression.ForceSavedData;
import galacticwars.clonewars.progression.LaunchContentCatalog;
import galacticwars.clonewars.progression.ProgressionSavedData;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Failure-atomic shrine transactions with bounded replay protection. */
public final class ForceShrineService {
    private static final int MAX_REPLAYS_PER_PLAYER = 128;
    private static final Map<UUID, LinkedHashSet<UUID>> REPLAYS = new LinkedHashMap<>();

    private ForceShrineService() {
    }

    public static synchronized boolean open(
            ServerPlayer player, BlockPos position, String traditionId
    ) {
        if (!(player.level() instanceof ServerLevel level)
                || !validShrine(player, level, position, traditionId)) return false;
        ForceSavedData forceData = ForceSavedData.get(level);
        ForceRuntimeState state = forceData.state(player.getUUID());
        if (!state.initiated()) {
            ForceProgressionService.ProgressionDecision initiation =
                    ForceProgressionService.initiate(
                            ProgressionSavedData.get(level).state(player.getUUID()), state,
                            traditionId, LaunchContentCatalog.data());
            if (!initiation.accepted()) return fail(player, initiation.reason());
            if (!forceData.compareAndSet(player.getUUID(), state, initiation.state())) {
                return fail(player, "force_state_changed");
            }
            state = initiation.state();
        }
        if (!state.traditionId().equals(traditionId)) {
            return fail(player, "wrong_force_shrine");
        }
        send(player, position, state);
        return true;
    }

    public static synchronized boolean handle(
            ServerPlayer player, ForceProgressionActionPayload payload
    ) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        ForceShrineBlock shrine = level.getBlockState(payload.shrinePos()).getBlock()
                instanceof ForceShrineBlock block ? block : null;
        if (shrine == null || !validShrine(player, level, payload.shrinePos(), shrine.traditionId())) {
            return fail(player, "force_shrine_required");
        }
        if (!remember(player.getUUID(), payload.replayId())) {
            send(player, payload.shrinePos(), ForceSavedData.get(level).state(player.getUUID()));
            return true;
        }
        ForceSavedData forceData = ForceSavedData.get(level);
        ForceRuntimeState before = forceData.state(player.getUUID());
        if (payload.action() == ForceProgressionActionPayload.RITUAL) {
            boolean accepted = ForceRitualService.perform(
                    player, payload.shrinePos(), payload.subjectId()).accepted();
            send(player, payload.shrinePos(), before);
            return accepted;
        }
        if (!before.traditionId().equals(shrine.traditionId())) {
            return fail(player, "wrong_force_shrine");
        }
        LaunchContentDefinitions content = LaunchContentCatalog.data();
        ForceProgressionService.ProgressionDecision decision;
        boolean paid = false;
        if (payload.action() == ForceProgressionActionPayload.LEARN) {
            decision = ForceProgressionService.learnNode(before, payload.subjectId(), content);
        } else if (payload.action() == ForceProgressionActionPayload.EQUIP) {
            decision = ForceProgressionService.equip(
                    before, payload.slot(), payload.subjectId(), content);
        } else {
            decision = ForceProgressionService.respec(before, content);
            if (decision.accepted()) {
                paid = CreditTransactionService.withdrawPlayer(
                        player, ForceProgressionService.RESPEC_CREDIT_COST);
                if (!paid) return fail(player, "force_respec_payment_required");
            }
        }
        if (!decision.accepted()) return fail(player, decision.reason());
        if (!forceData.compareAndSet(player.getUUID(), before, decision.state())) {
            if (paid) CreditTransactionService.refundPlayer(
                    player, ForceProgressionService.RESPEC_CREDIT_COST);
            return fail(player, "force_state_changed");
        }
        send(player, payload.shrinePos(), decision.state());
        return true;
    }

    public static synchronized void clearReplayHistory(UUID playerId) {
        REPLAYS.remove(playerId);
    }

    private static boolean validShrine(
            ServerPlayer player, ServerLevel level, BlockPos position, String traditionId
    ) {
        if (player.distanceToSqr(position.getX() + 0.5D, position.getY() + 0.5D,
                position.getZ() + 0.5D) > 64.0D) return false;
        if (!(level.getBlockState(position).getBlock() instanceof ForceShrineBlock block)
                || !block.traditionId().equals(traditionId)) return false;
        String expected = ForceProgressionService.defaultTraditionForFaction(
                ProgressionSavedData.get(level).state(player.getUUID()).factionId());
        return expected.equals(traditionId);
    }

    private static void send(ServerPlayer player, BlockPos position, ForceRuntimeState state) {
        if (!GalacticNetwork.canPlayerReceive(player, ForceProgressionPayload.TYPE)) return;
        LaunchContentDefinitions content = LaunchContentCatalog.data();
        var nodes = content.forceNodes().values().stream()
                .filter(node -> node.tradition().equals(state.traditionId()))
                .sorted(Comparator.comparing(LaunchContentDefinitions.ForceNodeDefinition::branch)
                        .thenComparingInt(LaunchContentDefinitions.ForceNodeDefinition::tier))
                .map(node -> new ForceProgressionPayload.NodeEntry(
                        node.id(), node.branch(), node.tier(), node.pointCost(), node.abilityId(),
                        node.passive(), node.prerequisites().stream().toList()))
                .toList();
        var progression = ProgressionSavedData.get(player.level()).state(player.getUUID());
        String factionPath = switch (state.traditionId()) {
            case "jedi" -> "republic";
            case "sith" -> "separatist";
            case "nightsister" -> "nightsister";
            default -> "";
        };
        var training = content.quests().values().stream()
                .filter(quest -> quest.id().startsWith(factionPath + "_force_training_"))
                .sorted(Comparator.comparing(LaunchContentDefinitions.QuestDefinition::id))
                .limit(3)
                .map(quest -> {
                    int required = quest.objectives().stream()
                            .mapToInt(LaunchContentDefinitions.QuestObjectiveDefinition::requiredCount).sum();
                    int current = quest.objectives().stream()
                            .mapToInt(objective -> Math.min(objective.requiredCount(),
                                    galacticwars.clonewars.progression.GalacticProgressionCoordinator
                                            .objectiveProgress(progression, objective)))
                            .sum();
                    return new ForceProgressionPayload.TrainingQuestEntry(
                            quest.id(), current, required, quest.rewardMasteryExperience(),
                            progression.hasSubject(
                                    galacticwars.clonewars.progression.ProgressionEventType.QUEST_ADVANCED,
                                    quest.id()));
                }).toList();
        GalacticNetwork.CHANNEL.sendToPlayer(() -> player, new ForceProgressionPayload(
                position, state.traditionId(), state.rank(), state.masteryExperience(),
                state.unspentPoints(), state.learnedNodeIds().stream().toList(),
                state.equippedAbilityIds(), nodes, training,
                ForceProgressionService.RESPEC_CREDIT_COST));
    }

    private static boolean remember(UUID playerId, UUID replayId) {
        LinkedHashSet<UUID> ids = REPLAYS.computeIfAbsent(playerId, ignored -> new LinkedHashSet<>());
        if (!ids.add(replayId)) return false;
        while (ids.size() > MAX_REPLAYS_PER_PLAYER) ids.remove(ids.iterator().next());
        return true;
    }

    private static boolean fail(ServerPlayer player, String reason) {
        player.sendOverlayMessage(Component.translatable(
                "message.galacticwars.force.failed", reason));
        return false;
    }
}
