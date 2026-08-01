package galacticwars.clonewars.kingdom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyFormationSlotAssignment;
import galacticwars.clonewars.army.ArmyGroupTactics;
import galacticwars.clonewars.army.ArmyGroupLifecycleState;
import galacticwars.clonewars.army.ArmyGroupOrder;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyGroupSimulation;
import galacticwars.clonewars.army.ArmyLocation;
import galacticwars.clonewars.army.ArmyMarchPhase;
import galacticwars.clonewars.army.ArmyMarchState;
import galacticwars.clonewars.army.ArmyMemberSnapshot;
import galacticwars.clonewars.army.ArmyPatrolEnemyPolicy;
import galacticwars.clonewars.army.ArmyPatrolMode;
import galacticwars.clonewars.army.ArmyPatrolPlan;
import galacticwars.clonewars.army.ArmyPatrolState;
import galacticwars.clonewars.army.ArmyPatrolStatus;
import galacticwars.clonewars.army.ArmyPatrolWaypoint;
import galacticwars.clonewars.army.ArmyRangedFirePolicy;
import galacticwars.clonewars.army.ArmySnapshotEquipment;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyEngagementStance;
import galacticwars.clonewars.army.ArmyTargetPriority;
import galacticwars.clonewars.recruitment.RecruitDuty;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.settlement.StarterCampDeployment;
import galacticwars.clonewars.settlement.StarterCampDeploymentPhase;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.CourierTransferAction;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.CourierRoutePlan;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.WorkAreaConfiguration;
import galacticwars.clonewars.workforce.WorkforceCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class KingdomCodecs {
    static final Codec<StarterCampDeployment> STARTER_CAMP_DEPLOYMENT = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(StarterCampDeployment::kingdomId),
                    Codec.STRING.fieldOf("dimension").forGetter(StarterCampDeployment::dimensionId),
                    Codec.INT.fieldOf("origin_x").forGetter(StarterCampDeployment::originX),
                    Codec.INT.fieldOf("origin_y").forGetter(StarterCampDeployment::originY),
                    Codec.INT.fieldOf("origin_z").forGetter(StarterCampDeployment::originZ),
                    Codec.intRange(0, 3).optionalFieldOf("rotation_steps", 0)
                            .forGetter(StarterCampDeployment::rotationSteps),
                    Codec.STRING.xmap(StarterCampDeploymentPhase::byId, StarterCampDeploymentPhase::id)
                            .optionalFieldOf("phase", StarterCampDeploymentPhase.AWAITING_CONFIRMATION)
                            .forGetter(StarterCampDeployment::phase),
                    Codec.BOOL.optionalFieldOf("contract_granted", false)
                            .forGetter(StarterCampDeployment::contractGranted),
                    Codec.BOOL.optionalFieldOf("supplies_granted", false)
                            .forGetter(StarterCampDeployment::suppliesGranted),
                    UUIDUtil.CODEC.optionalFieldOf("builder_id").forGetter(StarterCampDeployment::builderId),
                    UUIDUtil.CODEC.optionalFieldOf("project_id").forGetter(StarterCampDeployment::projectId),
                    Codec.STRING.optionalFieldOf("blocker", "").forGetter(StarterCampDeployment::blocker),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0)
                            .forGetter(StarterCampDeployment::revision)
            ).apply(instance, StarterCampDeployment::new));

    static final Codec<KingdomNpcRecord> KINGDOM_NPC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("recruit_id").forGetter(KingdomNpcRecord::recruitId),
            UUIDUtil.CODEC.fieldOf("settlement_id").forGetter(KingdomNpcRecord::settlementId),
            Codec.STRING.xmap(NpcServiceBranch::byId, NpcServiceBranch::id)
                    .fieldOf("service_branch").forGetter(KingdomNpcRecord::serviceBranch)
    ).apply(instance, KingdomNpcRecord::new));

    static final Codec<KingdomMember> KINGDOM_MEMBER = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(KingdomMember::playerId),
            Codec.STRING.xmap(KingdomMemberRole::byId, KingdomMemberRole::id)
                    .fieldOf("role").forGetter(KingdomMember::role)
    ).apply(instance, KingdomMember::new));

    static final Codec<KingdomInvite> KINGDOM_INVITE = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(KingdomInvite::id),
            UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(KingdomInvite::kingdomId),
            UUIDUtil.CODEC.fieldOf("inviter_id").forGetter(KingdomInvite::inviterId),
            UUIDUtil.CODEC.fieldOf("target_player_id").forGetter(KingdomInvite::targetPlayerId),
            Codec.STRING.xmap(KingdomMemberRole::byId, KingdomMemberRole::id)
                    .fieldOf("offered_role").forGetter(KingdomInvite::offeredRole),
            Codec.LONG.fieldOf("expires_game_time").forGetter(KingdomInvite::expiresGameTime)
    ).apply(instance, KingdomInvite::new));

    static final Codec<DiplomacyProposal> DIPLOMACY_PROPOSAL = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(DiplomacyProposal::id),
            UUIDUtil.CODEC.fieldOf("proposer_kingdom_id").forGetter(DiplomacyProposal::proposerKingdomId),
            UUIDUtil.CODEC.fieldOf("target_kingdom_id").forGetter(DiplomacyProposal::targetKingdomId),
            Codec.STRING.xmap(KingdomRelation::byId, KingdomRelation::id)
                    .fieldOf("relation").forGetter(DiplomacyProposal::relation),
            Codec.LONG.fieldOf("treaty_duration_ticks").forGetter(DiplomacyProposal::treatyDurationTicks),
            Codec.LONG.fieldOf("created_game_time").forGetter(DiplomacyProposal::createdGameTime),
            Codec.LONG.fieldOf("expires_game_time").forGetter(DiplomacyProposal::expiresGameTime)
    ).apply(instance, DiplomacyProposal::new));

    static final Codec<ClaimedChunk> CLAIMED_CHUNK = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(ClaimedChunk::x),
            Codec.INT.fieldOf("z").forGetter(ClaimedChunk::z)
    ).apply(instance, ClaimedChunk::new));

    static final Codec<KingdomClaim> KINGDOM_CLAIM = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(KingdomClaim::id),
            UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(KingdomClaim::kingdomId),
            Codec.STRING.fieldOf("dimension").forGetter(KingdomClaim::dimensionId),
            CLAIMED_CHUNK.fieldOf("center").forGetter(KingdomClaim::center),
            CLAIMED_CHUNK.listOf().fieldOf("chunks").forGetter(KingdomClaim::chunks),
            Codec.BOOL.optionalFieldOf("capital", false).forGetter(KingdomClaim::capital)
    ).apply(instance, KingdomClaim::new));

    static final Codec<KingdomDiplomacy> KINGDOM_DIPLOMACY = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("first_kingdom_id").forGetter(KingdomDiplomacy::firstKingdomId),
            UUIDUtil.CODEC.fieldOf("second_kingdom_id").forGetter(KingdomDiplomacy::secondKingdomId),
            Codec.STRING.xmap(KingdomRelation::byId, KingdomRelation::id)
                    .optionalFieldOf("relation", KingdomRelation.NEUTRAL).forGetter(KingdomDiplomacy::relation),
            Codec.LONG.optionalFieldOf("treaty_expires_game_time", 0L)
                    .forGetter(KingdomDiplomacy::treatyExpiresGameTime),
            Codec.LONG.optionalFieldOf("cooldown_until_game_time", 0L)
                    .forGetter(KingdomDiplomacy::cooldownUntilGameTime),
            Codec.BOOL.optionalFieldOf("embargo", false).forGetter(KingdomDiplomacy::embargo)
    ).apply(instance, KingdomDiplomacy::new));

    static final Codec<KingdomSiege> KINGDOM_SIEGE = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(KingdomSiege::id),
            UUIDUtil.CODEC.fieldOf("claim_id").forGetter(KingdomSiege::claimId),
            UUIDUtil.CODEC.fieldOf("attacker_kingdom_id").forGetter(KingdomSiege::attackerKingdomId),
            UUIDUtil.CODEC.fieldOf("defender_kingdom_id").forGetter(KingdomSiege::defenderKingdomId),
            Codec.STRING.xmap(SiegeState::byId, SiegeState::id)
                    .optionalFieldOf("state", SiegeState.ACTIVE).forGetter(KingdomSiege::state),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("capture_progress", 0)
                    .forGetter(KingdomSiege::captureProgress),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("capture_goal").forGetter(KingdomSiege::captureGoal),
            Codec.LONG.optionalFieldOf("last_progress_game_time", 0L)
                    .forGetter(KingdomSiege::lastProgressGameTime),
            UUIDUtil.CODEC.listOf().optionalFieldOf("attackers", List.of())
                    .forGetter(KingdomSiege::attackingParticipants),
            UUIDUtil.CODEC.listOf().optionalFieldOf("defenders", List.of())
                    .forGetter(KingdomSiege::defendingParticipants)
    ).apply(instance, KingdomSiege::new));

    static final Codec<ArmyLocation> ARMY_LOCATION = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(ArmyLocation::dimensionId),
            Codec.DOUBLE.fieldOf("x").forGetter(ArmyLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(ArmyLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(ArmyLocation::z)
    ).apply(instance, ArmyLocation::new));
    private static final Codec<List<ArmyLocation>> LEGACY_ARMY_PATROL_ROUTE = ARMY_LOCATION.listOf()
            .xmap(KingdomCodecs::normalizeLegacyPatrolRoute, List::copyOf);

    static final Codec<ArmyGroupOrder> ARMY_GROUP_ORDER = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(value -> ArmyCommandType.valueOf(value.toUpperCase()), value -> value.name().toLowerCase())
                    .fieldOf("type").forGetter(ArmyGroupOrder::type),
            ARMY_LOCATION.optionalFieldOf("target_position").forGetter(ArmyGroupOrder::targetPosition),
            UUIDUtil.CODEC.optionalFieldOf("target_entity_id").forGetter(ArmyGroupOrder::targetEntityId),
            Codec.STRING.xmap(value -> ArmyFormation.valueOf(value.toUpperCase()), value -> value.name().toLowerCase())
                    .optionalFieldOf("formation", ArmyFormation.LINE).forGetter(ArmyGroupOrder::formation),
            Codec.intRange(1, 8).optionalFieldOf("spacing", 2).forGetter(ArmyGroupOrder::spacing)
    ).apply(instance, ArmyGroupOrder::new));

    private static final Codec<ArmySnapshotEquipment> COMPLETE_ARMY_SNAPSHOT_EQUIPMENT =
            RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("main_hand", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::mainHand),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("off_hand", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::offHand),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("head", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::head),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("chest", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::chest),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("legs", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::legs),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("feet", ItemStack.EMPTY)
                            .forGetter(ArmySnapshotEquipment::feet)
            ).apply(instance, ArmySnapshotEquipment::new));

    private static final Codec<LegacyArmySnapshotEquipment> LEGACY_ARMY_SNAPSHOT_EQUIPMENT =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("main_hand", "").forGetter(LegacyArmySnapshotEquipment::mainHand),
                    Codec.STRING.optionalFieldOf("head", "").forGetter(LegacyArmySnapshotEquipment::head),
                    Codec.STRING.optionalFieldOf("chest", "").forGetter(LegacyArmySnapshotEquipment::chest),
                    Codec.STRING.optionalFieldOf("legs", "").forGetter(LegacyArmySnapshotEquipment::legs),
                    Codec.STRING.optionalFieldOf("feet", "").forGetter(LegacyArmySnapshotEquipment::feet)
            ).apply(instance, LegacyArmySnapshotEquipment::new));

    static final Codec<ArmySnapshotEquipment> ARMY_SNAPSHOT_EQUIPMENT =
            COMPLETE_ARMY_SNAPSHOT_EQUIPMENT.withAlternative(
                    LEGACY_ARMY_SNAPSHOT_EQUIPMENT,
                    KingdomCodecs::migrateLegacyEquipment);

    private static final Codec<List<ItemStack>> ARMY_SNAPSHOT_CARGO =
            ItemStack.OPTIONAL_CODEC.listOf(0, ArmyMemberSnapshot.CARGO_SLOT_COUNT);

    static final Codec<ArmyMemberSnapshot> ARMY_MEMBER_SNAPSHOT = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("recruit_id").forGetter(ArmyMemberSnapshot::recruitId),
            Codec.STRING.fieldOf("entity_type_id").forGetter(ArmyMemberSnapshot::entityTypeId),
            Codec.STRING.fieldOf("unit_id").forGetter(ArmyMemberSnapshot::unitId),
            UUIDUtil.CODEC.fieldOf("owner_id").forGetter(ArmyMemberSnapshot::ownerId),
            UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(ArmyMemberSnapshot::kingdomId),
            Codec.STRING.xmap(RecruitDuty::byId, RecruitDuty::id).fieldOf("duty").forGetter(ArmyMemberSnapshot::duty),
            Codec.FLOAT.fieldOf("health").forGetter(ArmyMemberSnapshot::health),
            Codec.intRange(0, 100).optionalFieldOf("morale", 100).forGetter(ArmyMemberSnapshot::morale),
            Codec.intRange(0, 100).optionalFieldOf("hunger", 100).forGetter(ArmyMemberSnapshot::hunger),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("unpaid_ticks", 0).forGetter(ArmyMemberSnapshot::unpaidTicks),
            Codec.LONG.optionalFieldOf("generation", 0L).forGetter(ArmyMemberSnapshot::generation),
            ARMY_SNAPSHOT_EQUIPMENT.fieldOf("equipment").forGetter(ArmyMemberSnapshot::equipment),
            ARMY_SNAPSHOT_CARGO.optionalFieldOf("cargo", List.of()).forGetter(ArmyMemberSnapshot::cargo),
            Codec.STRING.optionalFieldOf("custom_name", "").forGetter(ArmyMemberSnapshot::customName)
    ).apply(instance, ArmyMemberSnapshot::new));

    private static ArmySnapshotEquipment migrateLegacyEquipment(LegacyArmySnapshotEquipment equipment) {
        return new ArmySnapshotEquipment(
                legacyItemStack(equipment.mainHand()),
                ItemStack.EMPTY,
                legacyItemStack(equipment.head()),
                legacyItemStack(equipment.chest()),
                legacyItemStack(equipment.legs()),
                legacyItemStack(equipment.feet()));
    }

    private static ItemStack legacyItemStack(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        Identifier identifier;
        try {
            identifier = Identifier.parse(itemId);
        } catch (RuntimeException invalid) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getValue(identifier);
        return item != null && identifier.equals(BuiltInRegistries.ITEM.getKey(item))
                ? new ItemStack(item)
                : ItemStack.EMPTY;
    }

    private record LegacyArmySnapshotEquipment(
            String mainHand,
            String head,
            String chest,
            String legs,
            String feet
    ) {
    }

    static final Codec<ArmyMarchState> ARMY_MARCH_STATE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
                            value -> ArmyMarchPhase.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("phase", ArmyMarchPhase.HALTED).forGetter(ArmyMarchState::phase),
            Codec.STRING.xmap(
                            value -> ArmyFormation.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("active_formation", ArmyFormation.LINE)
                    .forGetter(ArmyMarchState::activeFormation),
            Codec.intRange(0, 100).optionalFieldOf("cohesion_percent", 100)
                    .forGetter(ArmyMarchState::cohesionPercent),
            Codec.FLOAT.optionalFieldOf("yaw_degrees", 0.0F).forGetter(ArmyMarchState::yawDegrees),
            Codec.LONG.optionalFieldOf("phase_since_game_time", 0L)
                    .forGetter(ArmyMarchState::phaseSinceGameTime)
    ).apply(instance, ArmyMarchState::new));

    static final Codec<ArmyGroupSimulation> ARMY_GROUP_SIMULATION = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(value -> ArmyGroupLifecycleState.valueOf(value.toUpperCase()), value -> value.name().toLowerCase())
                    .optionalFieldOf("state", ArmyGroupLifecycleState.LIVE).forGetter(ArmyGroupSimulation::lifecycleState),
            ARMY_LOCATION.fieldOf("anchor").forGetter(ArmyGroupSimulation::anchor),
            Codec.LONG.optionalFieldOf("last_simulation_game_time", 0L).forGetter(ArmyGroupSimulation::lastSimulationGameTime),
            Codec.LONG.optionalFieldOf("revision", 0L).forGetter(ArmyGroupSimulation::revision),
            Codec.LONG.optionalFieldOf("snapshot_generation", 0L).forGetter(ArmyGroupSimulation::snapshotGeneration),
            Codec.STRING.optionalFieldOf("blocked_reason", "").forGetter(ArmyGroupSimulation::blockedReason),
            ARMY_MARCH_STATE.optionalFieldOf("march", ArmyMarchState.halted(ArmyFormation.LINE))
                    .forGetter(ArmyGroupSimulation::marchState)
    ).apply(instance, ArmyGroupSimulation::new));

    static final Codec<ArmyFormationSlotAssignment> ARMY_FORMATION_SLOT = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("member_id").forGetter(ArmyFormationSlotAssignment::memberId),
            Codec.intRange(0, 8192).fieldOf("slot").forGetter(ArmyFormationSlotAssignment::slotIndex)
    ).apply(instance, ArmyFormationSlotAssignment::new));

    static final Codec<ArmyPatrolWaypoint> ARMY_PATROL_WAYPOINT = RecordCodecBuilder.create(instance -> instance.group(
            ARMY_LOCATION.fieldOf("location").forGetter(ArmyPatrolWaypoint::location),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("wait_ticks", 0)
                    .forGetter(ArmyPatrolWaypoint::waitTicks)
    ).apply(instance, ArmyPatrolWaypoint::new));

    static final Codec<ArmyPatrolStatus> ARMY_PATROL_STATUS = Codec.STRING.xmap(
            value -> ArmyPatrolStatus.valueOf(value.toUpperCase()),
            value -> value.name().toLowerCase());

    static final Codec<ArmyPatrolState> ARMY_PATROL_STATE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 31).optionalFieldOf("waypoint_index", 0).forGetter(ArmyPatrolState::waypointIndex),
            Codec.INT.optionalFieldOf("direction", 1).forGetter(ArmyPatrolState::direction),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("wait_ticks_remaining", 0)
                    .forGetter(ArmyPatrolState::waitTicksRemaining),
            ARMY_PATROL_STATUS.optionalFieldOf("status", ArmyPatrolStatus.ACTIVE).forGetter(ArmyPatrolState::status)
    ).apply(instance, ArmyPatrolState::new));

    static final Codec<ArmyPatrolPlan> ARMY_PATROL_PLAN = RecordCodecBuilder.create(instance -> instance.group(
            ARMY_PATROL_WAYPOINT.listOf().fieldOf("waypoints").forGetter(ArmyPatrolPlan::waypoints),
            Codec.STRING.xmap(value -> ArmyPatrolMode.valueOf(value.toUpperCase()), value -> value.name().toLowerCase())
                    .optionalFieldOf("mode", ArmyPatrolMode.LOOP).forGetter(ArmyPatrolPlan::mode),
            ARMY_PATROL_STATE.optionalFieldOf("state", ArmyPatrolState.start()).forGetter(ArmyPatrolPlan::state),
            Codec.intRange(0, 64).optionalFieldOf("arrival_distance", ArmyPatrolPlan.DEFAULT_ARRIVAL_DISTANCE)
                    .forGetter(ArmyPatrolPlan::arrivalDistance),
            Codec.DOUBLE.optionalFieldOf("movement_speed", ArmyPatrolPlan.DEFAULT_MOVEMENT_SPEED)
                    .forGetter(ArmyPatrolPlan::movementSpeed),
            Codec.STRING.xmap(
                            value -> ArmyPatrolEnemyPolicy.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("enemy_policy", ArmyPatrolEnemyPolicy.ENGAGE_HOSTILES)
                    .forGetter(ArmyPatrolPlan::enemyPolicy),
            Codec.STRING.optionalFieldOf("name", ArmyPatrolPlan.DEFAULT_NAME)
                    .forGetter(ArmyPatrolPlan::name)
    ).apply(instance, ArmyPatrolPlan::new));

    static final Codec<ArmyGroupTactics> ARMY_GROUP_TACTICS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("formation_yaw_degrees").forGetter(ArmyGroupTactics::formationYawDegrees),
            Codec.BOOL.optionalFieldOf("hold_formation", false).forGetter(ArmyGroupTactics::holdFormation),
            Codec.BOOL.optionalFieldOf("tight_formation", false).forGetter(ArmyGroupTactics::tightFormation),
            Codec.STRING.xmap(
                            value -> ArmyEngagementStance.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("engagement_stance", ArmyEngagementStance.DEFENSIVE)
                    .forGetter(ArmyGroupTactics::engagementStance),
            Codec.STRING.xmap(
                            value -> ArmyTargetPriority.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("target_priority", ArmyTargetPriority.COMMAND_TARGET)
                    .forGetter(ArmyGroupTactics::targetPriority),
            Codec.STRING.xmap(
                            value -> ArmyRangedFirePolicy.valueOf(value.toUpperCase()),
                            value -> value.name().toLowerCase())
                    .optionalFieldOf("ranged_fire_policy", ArmyRangedFirePolicy.FREE_FIRE)
                    .forGetter(ArmyGroupTactics::rangedFirePolicy)
    ).apply(instance, ArmyGroupTactics::new));

    static final Codec<ArmyGroupRecord> ARMY_GROUP = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(ArmyGroupRecord::id),
            UUIDUtil.CODEC.fieldOf("owner_id").forGetter(ArmyGroupRecord::ownerId),
            UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(ArmyGroupRecord::kingdomId),
            UUIDUtil.CODEC.optionalFieldOf("commander_id").forGetter(ArmyGroupRecord::commanderId),
            UUIDUtil.CODEC.listOf().optionalFieldOf("member_ids", List.of()).forGetter(ArmyGroupRecord::memberIds),
            ARMY_GROUP_ORDER.fieldOf("order").forGetter(ArmyGroupRecord::order),
            ARMY_GROUP_SIMULATION.fieldOf("simulation").forGetter(ArmyGroupRecord::simulation),
            ARMY_MEMBER_SNAPSHOT.listOf().optionalFieldOf("snapshots", List.of()).forGetter(ArmyGroupRecord::snapshots),
            Codec.STRING.optionalFieldOf("name", "Squad").forGetter(ArmyGroupRecord::name),
            ARMY_LOCATION.optionalFieldOf("rally_point").forGetter(ArmyGroupRecord::rallyPoint),
            LEGACY_ARMY_PATROL_ROUTE.optionalFieldOf("patrol_route", List.of())
                    .forGetter(ArmyGroupRecord::patrolRoute),
            UUIDUtil.CODEC.optionalFieldOf("defended_claim_id").forGetter(ArmyGroupRecord::defendedClaimId),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("supply_units", 0)
                    .forGetter(ArmyGroupRecord::supplyUnits),
            ARMY_FORMATION_SLOT.listOf().optionalFieldOf("formation_slots")
                    .forGetter(ArmyGroupRecord::formationSlotAssignments),
            ARMY_PATROL_PLAN.optionalFieldOf("patrol_plan").forGetter(ArmyGroupRecord::patrolPlan),
            ARMY_GROUP_TACTICS.optionalFieldOf("tactics").forGetter(ArmyGroupRecord::tactics)
    ).apply(instance, ArmyGroupRecord::new));

    static final Codec<CommanderPolicy> COMMANDER_POLICY = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("automatic_recruitment", false).forGetter(CommanderPolicy::automaticRecruitment),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_recruit_count", 4).forGetter(CommanderPolicy::targetRecruitCount),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("maximum_campaign_spend", 64).forGetter(CommanderPolicy::maximumCampaignSpend),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("minimum_treasury_reserve", 16).forGetter(CommanderPolicy::minimumTreasuryReserve),
            Codec.intRange(20, Integer.MAX_VALUE).optionalFieldOf("campaign_delay_ticks", 24000).forGetter(CommanderPolicy::campaignDelayTicks)
    ).apply(instance, CommanderPolicy::new));

    static final Codec<RecruitmentCampaign> RECRUITMENT_CAMPAIGN = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(RecruitmentCampaign::id),
            Codec.STRING.fieldOf("unit_id").forGetter(RecruitmentCampaign::unitId),
            Codec.STRING.optionalFieldOf("profession_id", "").forGetter(RecruitmentCampaign::professionId),
            Codec.INT.fieldOf("reserved_cost").forGetter(RecruitmentCampaign::reservedCost),
            Codec.LONG.fieldOf("ready_game_time").forGetter(RecruitmentCampaign::readyGameTime),
            Codec.STRING.xmap(RecruitmentCampaignState::byId, RecruitmentCampaignState::id)
                    .fieldOf("state").forGetter(RecruitmentCampaign::state),
            Codec.STRING.optionalFieldOf("reason_code", "reserved").forGetter(RecruitmentCampaign::reasonCode),
            Codec.BOOL.optionalFieldOf("refund_pending", false).forGetter(RecruitmentCampaign::refundPending)
    ).apply(instance, RecruitmentCampaign::new));

    static final Codec<StorageEndpoint> STORAGE_ENDPOINT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(StorageEndpoint::dimensionId),
            Codec.INT.fieldOf("x").forGetter(StorageEndpoint::x),
            Codec.INT.fieldOf("y").forGetter(StorageEndpoint::y),
            Codec.INT.fieldOf("z").forGetter(StorageEndpoint::z),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("slots").forGetter(StorageEndpoint::slots)
    ).apply(instance, StorageEndpoint::new));

    static final Codec<WorkAreaBounds> WORK_AREA_BOUNDS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 64).fieldOf("width").forGetter(WorkAreaBounds::width),
            Codec.intRange(1, 64).fieldOf("height").forGetter(WorkAreaBounds::height),
            Codec.intRange(1, 64).fieldOf("depth").forGetter(WorkAreaBounds::depth)
    ).apply(instance, WorkAreaBounds::new));

    static final Codec<CourierTransferAction> COURIER_TRANSFER_ACTION =
            WorkforceCodecs.COURIER_TRANSFER_ACTION;

    private static final Codec<CourierWaypoint> LEGACY_UNBOUNDED_COURIER_WAYPOINT =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(CourierWaypoint::dimensionId),
                    Codec.INT.fieldOf("x").forGetter(CourierWaypoint::x),
                    Codec.INT.fieldOf("y").forGetter(CourierWaypoint::y),
                    Codec.INT.fieldOf("z").forGetter(CourierWaypoint::z),
                    COURIER_TRANSFER_ACTION.listOf().optionalFieldOf("actions", List.of())
                            .forGetter(CourierWaypoint::actions)
            ).apply(instance, CourierWaypoint::new));

    static final Codec<CourierWaypoint> COURIER_WAYPOINT =
            WorkforceCodecs.COURIER_WAYPOINT.withAlternative(LEGACY_UNBOUNDED_COURIER_WAYPOINT);

    private static final Codec<List<CourierWaypoint>> COURIER_ROUTE =
            COURIER_WAYPOINT.listOf(0, CourierRoutePlan.MAX_WAYPOINTS)
                    .withAlternative(COURIER_WAYPOINT.listOf());

    static final Codec<WorkAreaConfiguration> WORK_AREA_CONFIGURATION = RecordCodecBuilder.create(instance -> instance.group(
            WORK_AREA_BOUNDS.fieldOf("bounds").forGetter(WorkAreaConfiguration::bounds),
            Codec.BOOL.optionalFieldOf("kingdom_access", true).forGetter(WorkAreaConfiguration::kingdomAccess),
            Codec.intRange(0, 100).optionalFieldOf("priority", 50).forGetter(WorkAreaConfiguration::priority),
            Codec.BOOL.optionalFieldOf("overlay_visible", false).forGetter(WorkAreaConfiguration::overlayVisible),
            Codec.STRING.listOf().optionalFieldOf("item_filters", List.of()).forGetter(WorkAreaConfiguration::itemFilters),
            COURIER_ROUTE.optionalFieldOf("courier_route", List.of())
                    .forGetter(WorkAreaConfiguration::courierRoute),
            Codec.STRING.xmap(CourierRouteMode::byId, CourierRouteMode::id)
                    .optionalFieldOf("courier_route_mode", CourierRouteMode.LOOP)
                    .forGetter(WorkAreaConfiguration::courierRouteMode),
            Codec.LONG.optionalFieldOf("courier_route_revision", 0L)
                    .forGetter(WorkAreaConfiguration::courierRouteRevision),
            Codec.STRING.xmap(CourierDispatchMode::byId, CourierDispatchMode::id)
                    .optionalFieldOf("courier_dispatch_mode")
                    .forGetter(configuration -> Optional.of(configuration.courierDispatchMode())),
            Codec.LONG.optionalFieldOf("revision", 0L)
                    .forGetter(WorkAreaConfiguration::revision)
    ).apply(instance, WorkAreaConfiguration::fromPersistence));

    static final Codec<WorksiteRecord> WORKSITE = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(WorksiteRecord::id),
            Codec.STRING.fieldOf("type").forGetter(WorksiteRecord::type),
            Codec.STRING.fieldOf("dimension").forGetter(WorksiteRecord::dimensionId),
            Codec.INT.fieldOf("x").forGetter(WorksiteRecord::x),
            Codec.INT.fieldOf("y").forGetter(WorksiteRecord::y),
            Codec.INT.fieldOf("z").forGetter(WorksiteRecord::z),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(WorksiteRecord::radius),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("capacity", 1).forGetter(WorksiteRecord::capacity),
            Codec.STRING.xmap(value -> WorkerProfession.byId(value).orElseThrow(), WorkerProfession::id)
                    .listOf().optionalFieldOf("accepted_professions", List.of()).forGetter(WorksiteRecord::acceptedProfessions),
            UUIDUtil.CODEC.optionalFieldOf("source_project_id").forGetter(WorksiteRecord::sourceProjectId),
            UUIDUtil.CODEC.listOf().optionalFieldOf("assignment_ids", List.of()).forGetter(WorksiteRecord::assignmentIds),
            STORAGE_ENDPOINT.listOf().optionalFieldOf("storage_endpoints", List.of()).forGetter(WorksiteRecord::storageEndpoints),
            WORK_AREA_CONFIGURATION.optionalFieldOf("configuration")
                    .forGetter(worksite -> Optional.of(worksite.configuration()))
    ).apply(instance, WorksiteRecord::fromPersistence));

    static final Codec<BuildProject> BUILD_PROJECT = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(BuildProject::id),
            Codec.STRING.fieldOf("blueprint_id").forGetter(BuildProject::blueprintId),
            Codec.STRING.fieldOf("dimension").forGetter(BuildProject::dimensionId),
            Codec.INT.fieldOf("origin_x").forGetter(BuildProject::originX),
            Codec.INT.fieldOf("origin_y").forGetter(BuildProject::originY),
            Codec.INT.fieldOf("origin_z").forGetter(BuildProject::originZ),
            Codec.INT.optionalFieldOf("rotation_steps", 0).forGetter(BuildProject::rotationSteps),
            Codec.STRING.optionalFieldOf("definition_hash")
                    .forGetter(project -> Optional.of(project.definitionHash())),
            Codec.INT.listOf().optionalFieldOf("completed_placements", List.of()).forGetter(BuildProject::completedPlacements),
            Codec.STRING.xmap(BuildProjectState::byId, BuildProjectState::id)
                    .optionalFieldOf("state").forGetter(project -> Optional.of(project.state())),
            Codec.STRING.optionalFieldOf("blocked_reason", "").forGetter(BuildProject::blockedReason),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0).forGetter(BuildProject::revision)
    ).apply(instance, BuildProject::fromPersistence));

    static final Codec<WorkOrder> WORK_ORDER = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(WorkOrder::id),
            Codec.STRING.xmap(WorkOrderType::byId, WorkOrderType::id).fieldOf("type").forGetter(WorkOrder::type),
            UUIDUtil.CODEC.optionalFieldOf("assigned_recruit_id").forGetter(WorkOrder::assignedRecruitId),
            Codec.STRING.xmap(WorkOrderState::byId, WorkOrderState::id)
                    .optionalFieldOf("state", WorkOrderState.QUEUED).forGetter(WorkOrder::state),
            UUIDUtil.CODEC.optionalFieldOf("worksite_id").forGetter(WorkOrder::worksiteId),
            UUIDUtil.CODEC.optionalFieldOf("project_id").forGetter(WorkOrder::projectId),
            Codec.STRING.fieldOf("dimension").forGetter(WorkOrder::dimensionId),
            Codec.INT.fieldOf("target_x").forGetter(WorkOrder::targetX),
            Codec.INT.fieldOf("target_y").forGetter(WorkOrder::targetY),
            Codec.INT.fieldOf("target_z").forGetter(WorkOrder::targetZ),
            Codec.STRING.optionalFieldOf("resource_id", "").forGetter(WorkOrder::resourceId),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("quantity", 1).forGetter(WorkOrder::quantity),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("completed_quantity", 0).forGetter(WorkOrder::completedQuantity),
            Codec.STRING.optionalFieldOf("blocked_reason", "").forGetter(WorkOrder::blockedReason),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0).forGetter(WorkOrder::revision)
    ).apply(instance, WorkOrder::new));

    static final Codec<SettlementTerminalLedger> SETTLEMENT_TERMINAL_LEDGER = RecordCodecBuilder.create(
            instance -> instance.group(
                    UUIDUtil.CODEC.listOf().optionalFieldOf("terminal_project_ids", List.of())
                            .forGetter(ledger -> List.copyOf(ledger.terminalProjectIds())),
                    Codec.STRING.listOf().optionalFieldOf("completed_build_keys", List.of())
                            .forGetter(ledger -> List.copyOf(ledger.completedBuildKeys())),
                    UUIDUtil.CODEC.listOf().optionalFieldOf("terminal_work_order_ids", List.of())
                            .forGetter(ledger -> List.copyOf(ledger.terminalWorkOrderIds()))
            ).apply(instance, (projects, builds, workOrders) -> new SettlementTerminalLedger(
                    new LinkedHashSet<>(projects), new LinkedHashSet<>(builds),
                    new LinkedHashSet<>(workOrders))));

    static final Codec<SettlementRewards> SETTLEMENT_REWARDS = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("external_storage_slots", 0)
                    .forGetter(SettlementRewards::externalStorageSlots),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("commander_slots", 0)
                    .forGetter(SettlementRewards::commanderSlots),
            SETTLEMENT_TERMINAL_LEDGER.optionalFieldOf(
                    "terminal_ledger", SettlementTerminalLedger.empty())
                    .forGetter(SettlementRewards::terminalLedger)
    ).apply(instance, SettlementRewards::new));

    static final Codec<SettlementRecord> SETTLEMENT = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(SettlementRecord::id),
            Codec.STRING.fieldOf("dimension").forGetter(SettlementRecord::dimensionId),
            Codec.INT.fieldOf("hall_x").forGetter(SettlementRecord::hallX),
            Codec.INT.fieldOf("hall_y").forGetter(SettlementRecord::hallY),
            Codec.INT.fieldOf("hall_z").forGetter(SettlementRecord::hallZ),
            Codec.intRange(8, 256).optionalFieldOf("claim_radius", 48).forGetter(SettlementRecord::claimRadius),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("housing_capacity", 4).forGetter(SettlementRecord::housingCapacity),
            UUIDUtil.CODEC.listOf().optionalFieldOf("recruit_ids", List.of()).forGetter(SettlementRecord::recruitIds),
            Codec.either(UUIDUtil.CODEC, UUIDUtil.CODEC.listOf())
                    .xmap(value -> value.map(List::of, List::copyOf), Either::right)
                    .optionalFieldOf("commander_id", List.of()).forGetter(SettlementRecord::commanderIds),
            COMMANDER_POLICY.optionalFieldOf("commander_policy", CommanderPolicy.defaults()).forGetter(SettlementRecord::commanderPolicy),
            WORKSITE.listOf().optionalFieldOf("worksites", List.of()).forGetter(SettlementRecord::worksites),
            BUILD_PROJECT.listOf().optionalFieldOf("build_projects", List.of()).forGetter(SettlementRecord::buildProjects),
            WORK_ORDER.listOf().optionalFieldOf("work_orders", List.of()).forGetter(SettlementRecord::workOrders),
            RECRUITMENT_CAMPAIGN.listOf().optionalFieldOf("recruitment_campaigns", List.of()).forGetter(SettlementRecord::recruitmentCampaigns),
            SETTLEMENT_REWARDS.optionalFieldOf("rewards", SettlementRewards.none()).forGetter(SettlementRecord::rewards),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0).forGetter(SettlementRecord::revision)
    ).apply(instance, (id, dimension, hallX, hallY, hallZ, claimRadius, housingCapacity, recruitIds,
            commanderIds, commanderPolicy, worksites, buildProjects, workOrders, recruitmentCampaigns,
            rewards, revision) -> new SettlementRecord(
                    id, dimension, hallX, hallY, hallZ, claimRadius, housingCapacity, recruitIds,
                    commanderIds.stream().findFirst(), commanderPolicy, worksites, buildProjects, workOrders,
                    recruitmentCampaigns, rewards, revision,
                    commanderIds.size() < 2 ? List.of() : commanderIds.subList(1, commanderIds.size()))));

    static final Codec<KingdomRecord> KINGDOM_RECORD = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(KingdomRecord::id),
            UUIDUtil.CODEC.fieldOf("owner_id").forGetter(KingdomRecord::ownerId),
            Codec.STRING.fieldOf("faction_id").forGetter(KingdomRecord::factionId),
            SETTLEMENT.fieldOf("settlement").forGetter(KingdomRecord::settlement),
            KINGDOM_MEMBER.listOf().optionalFieldOf("members", List.of()).forGetter(KingdomRecord::members),
            SETTLEMENT.listOf().optionalFieldOf("outposts", List.of()).forGetter(KingdomRecord::outposts),
            KINGDOM_CLAIM.listOf().optionalFieldOf("claims", List.of()).forGetter(KingdomRecord::claims),
            KINGDOM_NPC.listOf().optionalFieldOf("npc_roster", List.of()).forGetter(KingdomRecord::npcRoster)
    ).apply(instance, KingdomRecord::new));

    private static List<ArmyLocation> normalizeLegacyPatrolRoute(List<ArmyLocation> patrolRoute) {
        return patrolRoute.size() == 1 || patrolRoute.size() > 32 ? List.of() : patrolRoute;
    }

    private KingdomCodecs() {
    }
}
