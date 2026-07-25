package galacticwars.clonewars.faction.ai;

import galacticwars.clonewars.Config;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.FactionAlignmentSavedData;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.world.FactionOutpostSavedData;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** Runtime adapter from validated faction policy to one loaded recruit. */
public final class NpcFactionAiService {
    private NpcFactionAiService() {
    }

    public static NpcAiProfile profile(GalacticRecruitEntity recruit) {
        Objects.requireNonNull(recruit, "recruit");
        FactionId factionId = FactionId.of(recruit.factionIdForGameplay());
        return GameplayDataManager.snapshot().npcAiProfile(factionId)
                .orElseGet(() -> NpcAiProfile.defaults(factionId));
    }

    public static int scanInterval(GalacticRecruitEntity recruit) {
        return profile(recruit).settings(recruit.getNpcRole()).scanIntervalTicks();
    }

    public static int scanRadius(GalacticRecruitEntity recruit) {
        return Math.min(
                Config.NPC_AI_MAX_SCAN_RADIUS.get(),
                profile(recruit).settings(recruit.getNpcRole()).scanRadius());
    }

    public static int coordinationRadius(GalacticRecruitEntity recruit) {
        return Math.min(
                Config.NPC_AI_MAX_SCAN_RADIUS.get(),
                profile(recruit).coordinationRadius());
    }

    public static int responderLimit(GalacticRecruitEntity recruit) {
        int configured = Math.min(
                Config.NPC_AI_MAX_RESPONDERS.get(),
                profile(recruit).maxResponders());
        int coordinated = FactionBalanceService.applyPercentFloor(
                configured,
                FactionBalanceService.resolve(
                        recruit.factionIdForGameplay()).coordinationPercent());
        return Math.max(1, Math.min(configured, coordinated));
    }

    public static NpcReactionDecision decision(
            GalacticRecruitEntity recruit,
            Player player
    ) {
        Objects.requireNonNull(recruit, "recruit");
        Objects.requireNonNull(player, "player");
        NpcAiProfile profile = profile(recruit);
        if (!(recruit.level() instanceof ServerLevel level)
                || player.level() != recruit.level()) {
            return NpcReactionDecision.forDisposition(NpcDisposition.NEUTRAL, profile);
        }
        FactionId factionId = FactionId.of(recruit.factionIdForGameplay());
        boolean dynamic = Config.ENABLE_DYNAMIC_FACTION_AI.getAsBoolean();
        int score = dynamic
                ? FactionAlignmentSavedData.get(level)
                        .alignment(player.getUUID())
                        .score(factionId)
                : 0;
        boolean activeAlert = dynamic
                && recruit.getFactionOutpostId() != null
                && FactionOutpostSavedData.get(level).activeAlert(
                        recruit.getFactionOutpostId(),
                        player.getUUID(),
                        level.getGameTime()).isPresent();
        return FactionDispositionResolver.resolve(
                recruit.factionRelationTo(player), score, activeAlert, profile);
    }

    public static boolean raiseAlert(
            GalacticRecruitEntity recruit,
            Player player,
            String reason
    ) {
        if (!Config.ENABLE_DYNAMIC_FACTION_AI.getAsBoolean()
                || recruit.getFactionOutpostId() == null
                || !(recruit.level() instanceof ServerLevel level)
                || player.level() != recruit.level()) {
            return false;
        }
        return FactionOutpostSavedData.get(level).raiseAlert(
                recruit.getFactionOutpostId(),
                player.getUUID(),
                level.getGameTime(),
                profile(recruit).alertDurationTicks(),
                reason).isPresent();
    }
}
