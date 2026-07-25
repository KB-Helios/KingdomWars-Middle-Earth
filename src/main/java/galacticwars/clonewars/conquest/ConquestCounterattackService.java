package galacticwars.clonewars.conquest;

import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.FactionReputationEvent;
import galacticwars.clonewars.faction.ai.FactionReputationService;
import galacticwars.clonewars.progression.ProgressionEvent;
import galacticwars.clonewars.progression.ProgressionEventType;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.recruitment.NpcServiceBranch;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

/** Persistent, visible retaliation against player-held conquest regions. */
final class ConquestCounterattackService {
    private static final long COUNTERATTACK_DURATION_TICKS = 2_400L;
    private static final long REPEAT_DELAY_TICKS = 24_000L;
    private static final int MAX_ATTACKERS = 4;

    private ConquestCounterattackService() {
    }

    static ConquestControlState tick(
            ServerLevel level,
            LaunchContentDefinitions.ConquestRegionDefinition region,
            ConquestControlState state,
            BlockPos beacon
    ) {
        long gameTime = level.getGameTime();
        if (state.controllingKingdom().isBlank() || state.attackingFaction().isBlank()) {
            return state;
        }
        if (!state.counterattackActive()) {
            if (state.counterattackAt() <= 0L || gameTime < state.counterattackAt()) {
                return state;
            }
            ConquestControlState started = state.startCounterattack(
                    gameTime + COUNTERATTACK_DURATION_TICKS);
            notifyMembers(level, started, "message.galacticwars.conquest.counterattack_started",
                    region.id());
            return started;
        }

        spawnAttacker(level, region, state, beacon);
        int clampedRadius = Math.max(32, region.captureRadius());
        int attackers = attackerStrength(level, state, beacon, clampedRadius);
        int defenders = defenderStrength(level, state, beacon, clampedRadius);
        ConquestControlState progressed = defenders > attackers
                ? state.advanceDefense((defenders - attackers) * 20) : state;
        int defenseGoal = Math.max(200, region.captureTicks() / 2);
        if (progressed.counterattackProgress() >= defenseGoal) {
            ConquestControlState defended = progressed.defended(gameTime + REPEAT_DELAY_TICKS);
            rewardDefenders(level, defended, region.id());
            notifyMembers(level, defended, "message.galacticwars.conquest.counterattack_defended",
                    region.id());
            return defended;
        }
        if (gameTime >= progressed.counterattackEndsAt()) {
            notifyMembers(level, progressed, "message.galacticwars.conquest.counterattack_lost",
                    region.id());
            return progressed.counterattackLost();
        }
        return progressed;
    }

    private static void spawnAttacker(
            ServerLevel level,
            LaunchContentDefinitions.ConquestRegionDefinition region,
            ConquestControlState state,
            BlockPos beacon
    ) {
        if (level.getGameTime() % 100L != 0L) {
            return;
        }
        UUID attackId = attackId(state.regionId(), state.revision());
        AABB area = new AABB(beacon).inflate(Math.max(32, region.captureRadius()));
        int present = level.getEntitiesOfClass(GalacticRecruitEntity.class, area,
                recruit -> attackId.equals(recruit.getFactionOutpostId()) && recruit.isAlive()).size();
        if (present >= MAX_ATTACKERS) {
            return;
        }
        EntityType<GalacticRecruitEntity> type = ConquestRuntimeEvents.patrolType(
                state.attackingFaction());
        if (type == null) {
            return;
        }
        GalacticRecruitEntity recruit = type.create(level, EntitySpawnReason.EVENT);
        if (recruit == null) {
            return;
        }
        int offset = 8 + present * 2;
        recruit.setPos(beacon.getX() + offset + 0.5D, beacon.getY(), beacon.getZ() + 0.5D);
        recruit.initializeFromSpawnEgg();
        recruit.initializeNaturalFactionNpc(
                attackId, NpcServiceBranch.MILITARY, beacon, Math.max(32, region.captureRadius()));
        level.addFreshEntity(recruit);
    }

    private static int attackerStrength(
            ServerLevel level, ConquestControlState state, BlockPos beacon, int radius
    ) {
        UUID attackId = attackId(state.regionId(), state.revision());
        return level.getEntitiesOfClass(GalacticRecruitEntity.class,
                new AABB(beacon).inflate(radius),
                recruit -> recruit.isAlive() && attackId.equals(recruit.getFactionOutpostId())).size();
    }

    private static int defenderStrength(
            ServerLevel level, ConquestControlState state, BlockPos beacon, int radius
    ) {
        int recruits = level.getEntitiesOfClass(GalacticRecruitEntity.class,
                new AABB(beacon).inflate(radius), recruit -> recruit.isAlive()
                        && recruit.getServiceBranch() == NpcServiceBranch.MILITARY
                        && recruit.factionIdForGameplay().equals(state.controllingFaction())).size();
        int players = (int) level.players().stream()
                .filter(player -> player.blockPosition().distSqr(beacon) <= (double) radius * radius)
                .filter(player -> ProgressionSavedData.get(level).state(player.getUUID())
                        .factionId().equals(state.controllingFaction()))
                .count();
        return recruits + players;
    }

    private static void rewardDefenders(
            ServerLevel level, ConquestControlState state, String regionId
    ) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (!memberOfControllingKingdom(level, player, state)) {
                continue;
            }
            UUID eventId = UUID.nameUUIDFromBytes(("conquest:defended:" + regionId + ":"
                    + state.revision() + ":" + player.getUUID()).getBytes(StandardCharsets.UTF_8));
            var progressionResult = ProgressionSavedData.get(level).apply(new ProgressionEvent(
                    eventId, player.getUUID(), ProgressionEventType.REGION_DEFENDED, regionId, 1));
            if (progressionResult.accepted() && progressionResult.changed()) {
                FactionReputationService.record(
                        level,
                        player.getUUID(),
                        eventId,
                        state.controllingFaction(),
                        FactionReputationEvent.OUTPOST_DEFENDED);
            }
        }
    }

    private static void notifyMembers(
            ServerLevel level, ConquestControlState state, String messageKey, String regionId
    ) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (memberOfControllingKingdom(level, player, state)) {
                player.sendSystemMessage(Component.translatable(messageKey, regionId));
            }
        }
    }

    private static boolean memberOfControllingKingdom(
            ServerLevel level, ServerPlayer player, ConquestControlState state
    ) {
        return galacticwars.clonewars.kingdom.KingdomSavedData.get(level)
                .kingdomForPlayer(player.getUUID())
                .map(kingdom -> kingdom.id().toString().equals(state.controllingKingdom()))
                .orElse(false);
    }

    private static UUID attackId(String regionId, long revision) {
        return UUID.nameUUIDFromBytes(("conquest-counterattack:" + regionId + ":" + revision)
                .getBytes(StandardCharsets.UTF_8));
    }
}
