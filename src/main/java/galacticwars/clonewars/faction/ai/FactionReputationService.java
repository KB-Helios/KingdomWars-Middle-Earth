package galacticwars.clonewars.faction.ai;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.FactionAlignmentEventResult;
import galacticwars.clonewars.faction.FactionAlignmentSavedData;
import galacticwars.clonewars.faction.FactionId;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Applies profile-driven alignment only after authoritative gameplay events commit. */
public final class FactionReputationService {
    private static final long DAMAGE_EVENT_WINDOW_TICKS = 200L;

    private FactionReputationService() {
    }

    public static Optional<FactionAlignmentEventResult> record(
            ServerLevel level,
            UUID playerId,
            UUID eventId,
            String factionId,
            FactionReputationEvent event
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(event, "event");
        if (factionId == null) {
            return Optional.empty();
        }
        FactionId sourceFaction;
        var snapshot = GameplayDataManager.snapshot();
        if (!snapshot.factions().contains(sourceFaction)) {
            return Optional.empty();
        }
        NpcAiProfile profile = snapshot.npcAiProfile(sourceFaction)
                .orElseGet(() -> NpcAiProfile.defaults(sourceFaction));
        return Optional.of(FactionAlignmentSavedData.get(level).applyEvent(
                playerId,
                eventId,
                snapshot.factions(),
                sourceFaction,
                profile.rule(event)));
    }

    public static void recordNaturalNpcDamage(
            ServerLevel level,
            GalacticRecruitEntity recruit,
            ServerPlayer attacker
    ) {
        if (!recruit.isNaturalFactionNpc()) {
            return;
        }
        long window = level.getGameTime() / DAMAGE_EVENT_WINDOW_TICKS;
        UUID eventId = deterministicId(
                "npc-damaged", attacker.getUUID(), recruit.getUUID(), window);
        record(level, attacker.getUUID(), eventId, recruit.factionIdForGameplay(),
                FactionReputationEvent.NPC_DAMAGED);
        NpcFactionAiService.raiseAlert(recruit, attacker, "npc_damaged");
    }

    public static void recordNaturalNpcKill(
            ServerLevel level,
            GalacticRecruitEntity recruit,
            ServerPlayer attacker
    ) {
        if (!recruit.isNaturalFactionNpc()) {
            return;
        }
        UUID eventId = deterministicId(
                "npc-killed", attacker.getUUID(), recruit.getUUID(), 0L);
        record(level, attacker.getUUID(), eventId, recruit.factionIdForGameplay(),
                FactionReputationEvent.NPC_KILLED);
        NpcFactionAiService.raiseAlert(recruit, attacker, "npc_killed");
    }

    public static UUID deterministicId(
            String event,
            UUID playerId,
            UUID subjectId,
            long revision
    ) {
        String value = event + ":" + playerId + ":" + subjectId + ":" + revision;
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }
}
