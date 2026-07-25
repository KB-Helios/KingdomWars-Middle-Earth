package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.Config;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.NpcFactionAiService;
import galacticwars.clonewars.faction.ai.NpcRole;
import galacticwars.clonewars.world.FactionOutpostSavedData;
import galacticwars.clonewars.world.OutpostAlert;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

/** Coordinates only loaded same-outpost troops and never writes player army orders. */
public final class NaturalCommanderCoordinationBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(
            ServerLevel level,
            GalacticRecruitEntity commander
    ) {
        return Config.ENABLE_DYNAMIC_FACTION_AI.getAsBoolean()
                && commander.isNaturalFactionNpc()
                && commander.getNpcRole() == NpcRole.COMMANDER
                && alertedPlayer(level, commander) != null;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity commander) {
        return false;
    }

    @Override
    protected void start(GalacticRecruitEntity commander) {
        if (!(commander.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer target = alertedPlayer(level, commander);
        UUID outpostId = commander.getFactionOutpostId();
        if (target == null || outpostId == null) {
            return;
        }

        commander.acceptNaturalDefenseTarget(target);
        int radius = NpcFactionAiService.coordinationRadius(commander);
        int limit = NpcFactionAiService.responderLimit(commander);
        level.getEntitiesOfClass(
                        GalacticRecruitEntity.class,
                        commander.getBoundingBox().inflate(radius),
                        recruit -> recruit != commander
                                && recruit.isAlive()
                                && recruit.isNaturalFactionNpc()
                                && outpostId.equals(recruit.getFactionOutpostId())
                                && recruit.getNpcRole() == NpcRole.TROOPER)
                .stream()
                .sorted(Comparator
                        .comparingDouble((GalacticRecruitEntity recruit) ->
                                recruit.distanceToSqr(commander))
                        .thenComparing(GalacticRecruitEntity::getUUID))
                .limit(limit)
                .forEach(recruit -> recruit.acceptNaturalDefenseTarget(target));
    }

    private static ServerPlayer alertedPlayer(
            ServerLevel level,
            GalacticRecruitEntity commander
    ) {
        UUID outpostId = commander.getFactionOutpostId();
        if (outpostId == null) {
            return null;
        }
        int radius = NpcFactionAiService.coordinationRadius(commander);
        for (OutpostAlert alert : FactionOutpostSavedData.get(level)
                .activeAlerts(outpostId, level.getGameTime())) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(alert.playerId());
            if (player != null
                    && player.level() == level
                    && player.isAlive()
                    && commander.distanceToSqr(player) <= (double) radius * radius) {
                return player;
            }
        }
        return null;
    }
}
