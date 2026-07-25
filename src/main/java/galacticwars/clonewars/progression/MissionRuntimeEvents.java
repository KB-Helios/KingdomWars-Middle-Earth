package galacticwars.clonewars.progression;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import galacticwars.clonewars.conquest.ConquestRuntimeEvents;
import galacticwars.clonewars.conquest.ConquestSavedData;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.FactionReputationEvent;
import galacticwars.clonewars.faction.ai.FactionReputationService;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.vehicle.GalacticVehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** Active, persistent secure/escort/assault mission encounters. */
public final class MissionRuntimeEvents {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final int TICK_INTERVAL = 20;
    private static final int HOLD_GOAL = 200;
    private static final int LEAVE_GRACE_TICKS = 100;
    private static final int MISSION_RADIUS = 36;
    private static final long FEEDBACK_INTERVAL = 100L;

    private MissionRuntimeEvents() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TickEvent.SERVER_POST.register(MissionRuntimeEvents::onServerTick);
        PlayerEvent.PLAYER_RESPAWN.register((player, previousPlayer, removalReason) -> {
            if (removalReason == net.minecraft.world.entity.Entity.RemovalReason.KILLED) {
                failActiveMission(player, "player_defeated");
            }
        });
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        ProgressionSavedData progression = ProgressionSavedData.get(overworld);
        ProgressionState state = progression.state(player.getUUID());
        MissionAttemptSavedData attempts = MissionAttemptSavedData.get(overworld);
        MissionAttemptSavedData.MissionAttempt persisted = attempts
                .forPlayer(player.getUUID()).orElse(null);
        LaunchContentDefinitions.MissionDefinition mission = activeMission(state);
        long gameTime = overworld.getGameTime();

        if (mission == null) {
            if (persisted != null && persisted.phase().equals("cooldown")) {
                if (gameTime >= persisted.retryAt()) {
                    int nextAttempt = persisted.attempt() + 1;
                    ProgressionDecision retry = progression.apply(new ProgressionEvent(
                            MissionRuntimeService.deterministicLifecycleEventId(
                                    player.getUUID(), ProgressionEventType.MISSION_STARTED,
                                    persisted.missionId(), nextAttempt),
                            player.getUUID(), ProgressionEventType.MISSION_STARTED,
                            persisted.missionId(), 1));
                    if (retry.accepted() && MissionRuntimeService.active(
                            retry.state(), persisted.missionId())) {
                        attempts.put(persisted.restarted(nextAttempt, gameTime));
                        player.sendSystemMessage(Component.translatable(
                                "message.galacticwars.mission.retry_started",
                                missionTitle(persisted.missionId())));
                    }
                }
            } else if (persisted != null && !persisted.phase().equals("complete")) {
                attempts.remove(player.getUUID());
            }
            return;
        }

        int attemptNumber = state.subjectTotal(
                ProgressionEventType.MISSION_STARTED, java.util.Set.of(mission.id()));
        if (persisted == null || !persisted.missionId().equals(mission.id())
                || persisted.attempt() != attemptNumber) {
            Target target = resolveTarget(player, mission);
            persisted = new MissionAttemptSavedData.MissionAttempt(
                    player.getUUID(), mission.id(), Math.max(1, attemptNumber), "objectives",
                    target.dimensionId(), target.pos(), gameTime, 0L,
                    0, 0, false, "", 0L);
            attempts.put(persisted);
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.mission.started", missionTitle(mission.id())));
        }

        if (!MissionRuntimeService.requirementsComplete(state, mission)) {
            if (!persisted.phase().equals("objectives")) {
                attempts.put(persisted.phase("objectives"));
            }
            return;
        }

        ServerLevel targetLevel = level(player.level().getServer(), persisted.targetDimension());
        String readiness = readinessReason(player, targetLevel, persisted.target(), mission);
        if (!readiness.equals("accepted")) {
            MissionAttemptSavedData.MissionAttempt waiting = persisted.phase().equals("hold")
                    ? persisted.absent(TICK_INTERVAL) : persisted.phase("ready");
            if (waiting.phase().equals("hold") && waiting.absentTicks() >= LEAVE_GRACE_TICKS) {
                failActiveMission(player, "left_mission_area");
                return;
            }
            if (gameTime - waiting.lastFeedbackAt() >= FEEDBACK_INTERVAL) {
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.mission." + readiness,
                        missionTitle(mission.id())));
                waiting = waiting.feedback(gameTime);
            }
            attempts.put(waiting);
            return;
        }

        MissionAttemptSavedData.MissionAttempt holding = persisted.present();
        if (!holding.phase().equals("hold")) {
            spawnWave(targetLevel, holding.target(), mission, holding.attempt());
            holding = holding.holding(true);
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.mission.hold_started",
                    missionTitle(mission.id())));
        }
        if (activeAttackers(targetLevel, holding.target(), mission.id(), holding.attempt()) > 0) {
            holding = holding.feedback(gameTime);
            attempts.put(holding);
            return;
        }
        holding = holding.advanceHold(TICK_INTERVAL);
        if (holding.holdTicks() < HOLD_GOAL) {
            attempts.put(holding);
            return;
        }

        ProgressionDecision objective = progression.apply(new ProgressionEvent(
                MissionRuntimeService.deterministicLifecycleEventId(
                        player.getUUID(), ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                        mission.id(), holding.attempt()),
                player.getUUID(), ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                mission.id(), 1));
        if (objective.accepted() && objective.state().hasSubject(
                ProgressionEventType.MISSION_COMPLETED, mission.id())) {
            attempts.put(holding.complete());
            UUID reputationEventId = UUID.nameUUIDFromBytes(
                    ("faction-mission:" + player.getUUID() + ":" + mission.id() + ":"
                            + holding.attempt()).getBytes(StandardCharsets.UTF_8));
            FactionReputationService.record(
                    overworld,
                    player.getUUID(),
                    reputationEventId,
                    objective.state().factionId(),
                    FactionReputationEvent.MISSION_COMPLETED);
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.mission.completed", missionTitle(mission.id())));
        }
    }

    public static void failActiveMission(ServerPlayer player, String reason) {
        ServerLevel overworld = player.level().getServer().overworld();
        ProgressionSavedData progression = ProgressionSavedData.get(overworld);
        ProgressionState state = progression.state(player.getUUID());
        LaunchContentDefinitions.MissionDefinition mission = activeMission(state);
        if (mission == null) {
            return;
        }
        int failure = state.subjectTotal(
                ProgressionEventType.MISSION_FAILED, java.util.Set.of(mission.id())) + 1;
        ProgressionDecision failed = progression.apply(new ProgressionEvent(
                MissionRuntimeService.deterministicLifecycleEventId(
                        player.getUUID(), ProgressionEventType.MISSION_FAILED,
                        mission.id(), failure),
                player.getUUID(), ProgressionEventType.MISSION_FAILED, mission.id(), 1));
        if (!failed.accepted()) {
            return;
        }
        MissionAttemptSavedData attempts = MissionAttemptSavedData.get(overworld);
        MissionAttemptSavedData.MissionAttempt current = attempts.forPlayer(player.getUUID())
                .orElseGet(() -> {
                    Target target = resolveTarget(player, mission);
                    return new MissionAttemptSavedData.MissionAttempt(
                            player.getUUID(), mission.id(), Math.max(1, failure), "objectives",
                            target.dimensionId(), target.pos(), overworld.getGameTime(), 0L,
                            0, 0, false, "", 0L);
                });
        long retryAt = overworld.getGameTime() + mission.retryCooldownTicks();
        attempts.put(current.failed(retryAt, reason));
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.mission.failed", missionTitle(mission.id()),
                Component.translatable("message.galacticwars.mission.failure." + reason),
                Math.max(0L, mission.retryCooldownTicks() / 20L)));
    }

    private static LaunchContentDefinitions.MissionDefinition activeMission(ProgressionState state) {
        return LaunchContentCatalog.data().missions().values().stream()
                .filter(mission -> MissionRuntimeService.active(state, mission.id()))
                .sorted(Comparator.comparing(LaunchContentDefinitions.MissionDefinition::questId))
                .findFirst().orElse(null);
    }

    private static String readinessReason(
            ServerPlayer player,
            ServerLevel targetLevel,
            BlockPos target,
            LaunchContentDefinitions.MissionDefinition mission
    ) {
        if (targetLevel == null
                || !player.level().dimension().equals(targetLevel.dimension())) {
            return "wrong_dimension";
        }
        if (player.blockPosition().distSqr(target) > (double) MISSION_RADIUS * MISSION_RADIUS) {
            return "too_far";
        }
        AABB area = new AABB(target).inflate(MISSION_RADIUS);
        int workers = targetLevel.getEntitiesOfClass(GalacticRecruitEntity.class, area,
                recruit -> alliedTo(recruit, player)
                        && recruit.getServiceBranch() == NpcServiceBranch.CIVILIAN).size();
        int soldiers = targetLevel.getEntitiesOfClass(GalacticRecruitEntity.class, area,
                recruit -> alliedTo(recruit, player)
                        && recruit.getServiceBranch() == NpcServiceBranch.MILITARY).size();
        return switch (mission.archetype()) {
            case "secure" -> workers + soldiers > 0 ? "accepted" : "support_required";
            case "escort" -> workers > 0 && soldiers > 0
                    ? "accepted" : "escort_required";
            case "assault" -> soldiers > 0 && hasVehicle(targetLevel, area, player)
                    ? "accepted" : "assault_force_required";
            default -> "support_required";
        };
    }

    private static boolean alliedTo(GalacticRecruitEntity recruit, ServerPlayer player) {
        if (!recruit.isAlive()) {
            return false;
        }
        if (recruit.getOwnerReference() != null
                && recruit.getOwnerReference().getUUID().equals(player.getUUID())) {
            return true;
        }
        String faction = ProgressionSavedData.get(player.level().getServer().overworld())
                .state(player.getUUID()).factionId();
        return !faction.isBlank() && faction.equals(recruit.factionIdForGameplay());
    }

    private static boolean hasVehicle(ServerLevel level, AABB area, ServerPlayer player) {
        String faction = ProgressionSavedData.get(player.level().getServer().overworld())
                .state(player.getUUID()).factionId();
        return !level.getEntitiesOfClass(GalacticVehicleEntity.class, area,
                vehicle -> vehicle.isAlive()
                        && (vehicle.ownerId().filter(player.getUUID()::equals).isPresent()
                        || vehicle.factionId().equals(faction))).isEmpty();
    }

    private static void spawnWave(
            ServerLevel level,
            BlockPos target,
            LaunchContentDefinitions.MissionDefinition mission,
            int attempt
    ) {
        String faction = opposingFaction(mission.questId());
        var type = ConquestRuntimeEvents.patrolType(faction);
        if (type == null) {
            return;
        }
        UUID waveId = waveId(mission.id(), attempt);
        int count = mission.archetype().equals("assault") ? 4 : 2;
        int present = activeAttackers(level, target, mission.id(), attempt);
        for (int index = present; index < count; index++) {
            GalacticRecruitEntity recruit = type.create(level, EntitySpawnReason.EVENT);
            if (recruit == null) {
                continue;
            }
            int offset = 9 + index * 2;
            recruit.setPos(target.getX() + offset + 0.5D, target.getY(), target.getZ() + 0.5D);
            recruit.initializeFromSpawnEgg();
            recruit.initializeNaturalFactionNpc(
                    waveId, NpcServiceBranch.MILITARY, target, MISSION_RADIUS);
            level.addFreshEntity(recruit);
        }
    }

    private static int activeAttackers(
            ServerLevel level, BlockPos target, String missionId, int attempt
    ) {
        UUID waveId = waveId(missionId, attempt);
        return level.getEntitiesOfClass(GalacticRecruitEntity.class,
                new AABB(target).inflate(MISSION_RADIUS),
                recruit -> recruit.isAlive() && waveId.equals(recruit.getFactionOutpostId())).size();
    }

    private static Target resolveTarget(
            ServerPlayer player, LaunchContentDefinitions.MissionDefinition mission
    ) {
        if (mission.archetype().equals("secure")) {
            KingdomRecord kingdom = KingdomSavedData.get(player.level().getServer().overworld())
                    .kingdomForPlayer(player.getUUID()).orElse(null);
            if (kingdom != null) {
                var settlement = kingdom.settlement();
                return new Target(settlement.dimensionId(), new BlockPos(
                        settlement.hallX(), settlement.hallY(), settlement.hallZ()));
            }
        }
        if (mission.archetype().equals("assault") && !mission.targetRegionId().isBlank()) {
            var control = ConquestSavedData.get(player.level().getServer().overworld())
                    .state(mission.targetRegionId()).orElse(null);
            if (control != null) {
                return new Target(control.dimensionId(), new BlockPos(
                        control.beaconX(), control.beaconY(), control.beaconZ()));
            }
        }
        var planet = LaunchContentCatalog.data().planets().get(mission.planetId());
        if (planet != null) {
            String role = mission.archetype().equals("escort") ? "economy" : "contested";
            var poi = planet.pointsOfInterest().stream()
                    .filter(candidate -> candidate.role().equals(role)).findFirst().orElse(null);
            if (poi != null) {
                ServerLevel level = level(player.level().getServer(), planet.dimensionId());
                int y = level == null ? 64 : level.getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        poi.x(), poi.z());
                return new Target(planet.dimensionId(), new BlockPos(poi.x(), y, poi.z()));
            }
        }
        return new Target(player.level().dimension().identifier().toString(), player.blockPosition());
    }

    private static ServerLevel level(MinecraftServer server, String dimensionId) {
        try {
            return server.getLevel(ResourceKey.create(
                    Registries.DIMENSION, Identifier.parse(dimensionId)));
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    private static String opposingFaction(String questId) {
        int chapterIndex = questId.indexOf("_chapter_");
        if (chapterIndex < 0) {
            return "galacticwars:separatist";
        }
        String path = questId.substring(0, chapterIndex);
        return switch (path) {
            case "republic", "nightsister" -> "galacticwars:separatist";
            case "separatist" -> "galacticwars:republic";
            case "mandalorian" -> "galacticwars:hutt_cartel";
            case "hutt_cartel" -> "galacticwars:mandalorian";
            default -> "galacticwars:separatist";
        };
    }

    private static UUID waveId(String missionId, int attempt) {
        return UUID.nameUUIDFromBytes(("mission-wave:" + missionId + ":" + attempt)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static Component missionTitle(String missionId) {
        return Component.translatable(
                "mission.galacticwars." + missionId.toLowerCase(Locale.ROOT) + ".title");
    }

    private record Target(String dimensionId, BlockPos pos) {
    }
}
