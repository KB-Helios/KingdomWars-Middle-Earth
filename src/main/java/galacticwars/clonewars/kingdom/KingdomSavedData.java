package galacticwars.clonewars.kingdom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.data.SavedDataSchemaPolicy;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyFieldCommandBatch;
import galacticwars.clonewars.army.ArmyGroupLifecycleState;
import galacticwars.clonewars.army.ArmyGroupOrder;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyLocation;
import galacticwars.clonewars.army.ArmyMemberSnapshot;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.settlement.StarterCampDeployment;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.SettlementSupplyLedger;
import galacticwars.clonewars.workforce.SupplyDemand;
import galacticwars.clonewars.workforce.WorkforceCodecs;
import galacticwars.clonewars.technology.KingdomTechnologyState;
import galacticwars.clonewars.technology.TechnologyCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class KingdomSavedData extends SavedData {
    public static final int CURRENT_SCHEMA_VERSION = 11;
    public static final Codec<KingdomSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA_VERSION).forGetter(KingdomSavedData::schemaVersion),
            KingdomCodecs.KINGDOM_RECORD.listOf().optionalFieldOf("kingdoms", List.of()).forGetter(KingdomSavedData::kingdoms),
            net.minecraft.core.UUIDUtil.CODEC.listOf().optionalFieldOf("inactive_hall_owners", List.of())
                    .forGetter(data -> List.copyOf(data.inactiveHallOwners)),
            KingdomCodecs.ARMY_GROUP.listOf().optionalFieldOf("army_groups", List.of())
                    .forGetter(KingdomSavedData::armyGroups),
            KingdomCodecs.KINGDOM_DIPLOMACY.listOf().optionalFieldOf("diplomacy", List.of())
                    .forGetter(KingdomSavedData::diplomacy),
            KingdomCodecs.KINGDOM_SIEGE.listOf().optionalFieldOf("sieges", List.of())
                    .forGetter(KingdomSavedData::sieges),
            KingdomCodecs.KINGDOM_INVITE.listOf().optionalFieldOf("pending_invites", List.of())
                    .forGetter(KingdomSavedData::pendingInvites),
            KingdomCodecs.DIPLOMACY_PROPOSAL.listOf().optionalFieldOf("pending_diplomacy", List.of())
                    .forGetter(KingdomSavedData::pendingDiplomacy),
            WorkforceCodecs.SETTLEMENT_SUPPLY_LEDGER.listOf()
                    .optionalFieldOf("supply_ledgers", List.of())
                    .forGetter(KingdomSavedData::supplyLedgers),
            KingdomCodecs.STARTER_CAMP_DEPLOYMENT.listOf()
                    .optionalFieldOf("starter_camp_deployments", List.of())
                    .forGetter(KingdomSavedData::starterCampDeployments),
            TechnologyCodecs.KINGDOM_STATE.listOf()
                    .optionalFieldOf("technology", List.of())
                    .forGetter(KingdomSavedData::technologyStates)
    ).apply(instance, KingdomSavedData::new));
    public static final SavedDataType<KingdomSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "kingdoms"),
            KingdomSavedData::new,
            CODEC,
            null);

    private final int schemaVersion;
    private final Map<UUID, KingdomRecord> kingdomsByOwner = new LinkedHashMap<>();
    private final Map<UUID, KingdomRecord> kingdomsById = new LinkedHashMap<>();
    private final Map<UUID, UUID> kingdomIdsByMember = new LinkedHashMap<>();
    private final Map<UUID, UUID> kingdomIdsBySettlement = new LinkedHashMap<>();
    private final Map<UUID, UUID> kingdomIdsByRecruit = new LinkedHashMap<>();
    private final Map<UUID, UUID> kingdomIdsByArmyGroup = new LinkedHashMap<>();
    private final Map<ClaimKey, KingdomClaim> claimsByChunk = new LinkedHashMap<>();
    private final LinkedHashSet<UUID> inactiveHallOwners = new LinkedHashSet<>();
    private final Map<UUID, ArmyGroupRecord> armyGroupsById = new LinkedHashMap<>();
    private final Map<DiplomacyKey, KingdomDiplomacy> diplomacyByPair = new LinkedHashMap<>();
    private final Map<UUID, KingdomSiege> siegesById = new LinkedHashMap<>();
    private final Map<UUID, KingdomInvite> invitesById = new LinkedHashMap<>();
    private final Map<UUID, DiplomacyProposal> proposalsById = new LinkedHashMap<>();
    private final Map<UUID, SettlementSupplyLedger> supplyLedgersBySettlement = new LinkedHashMap<>();
    private final Map<UUID, StarterCampDeployment> starterCampDeploymentsByKingdom = new LinkedHashMap<>();
    private final Map<UUID, KingdomTechnologyState> technologyByKingdom = new LinkedHashMap<>();

    public KingdomSavedData() {
        this(CURRENT_SCHEMA_VERSION, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());
    }

    private KingdomSavedData(
            int schemaVersion,
            List<KingdomRecord> kingdoms,
            List<UUID> inactiveHallOwners,
            List<ArmyGroupRecord> armyGroups,
            List<KingdomDiplomacy> diplomacy,
            List<KingdomSiege> sieges,
            List<KingdomInvite> invites,
            List<DiplomacyProposal> proposals,
            List<SettlementSupplyLedger> supplyLedgers,
            List<StarterCampDeployment> starterCampDeployments,
            List<KingdomTechnologyState> technologyStates
    ) {
        boolean legacyTechnology = schemaVersion < 10;
        this.schemaVersion = SavedDataSchemaPolicy.migrate(
                schemaVersion, CURRENT_SCHEMA_VERSION, "kingdom");
        for (KingdomRecord kingdom : kingdoms) {
            if (!this.kingdomsByOwner.containsKey(kingdom.ownerId())
                    && !this.kingdomsById.containsKey(kingdom.id())
                    && kingdom.members().stream().noneMatch(member -> kingdomIdsByMember.containsKey(member.playerId()))) {
                indexKingdom(kingdom);
            }
        }
        this.inactiveHallOwners.addAll(inactiveHallOwners);
        this.inactiveHallOwners.retainAll(this.kingdomsByOwner.keySet());
        for (ArmyGroupRecord armyGroup : armyGroups) {
            boolean knownKingdom = this.kingdomsByOwner.values().stream()
                    .anyMatch(kingdom -> kingdom.id().equals(armyGroup.kingdomId())
                            && kingdom.ownerId().equals(armyGroup.ownerId()));
            if (knownKingdom) {
                this.armyGroupsById.putIfAbsent(armyGroup.id(), armyGroup);
                this.kingdomIdsByArmyGroup.putIfAbsent(armyGroup.id(), armyGroup.kingdomId());
            }
        }
        for (KingdomDiplomacy relation : diplomacy) {
            if (kingdomsById.containsKey(relation.firstKingdomId())
                    && kingdomsById.containsKey(relation.secondKingdomId())) {
                diplomacyByPair.putIfAbsent(DiplomacyKey.of(
                        relation.firstKingdomId(), relation.secondKingdomId()), relation);
            }
        }
        for (KingdomSiege siege : sieges) {
            if (kingdomsById.containsKey(siege.attackerKingdomId())
                    && kingdomsById.containsKey(siege.defenderKingdomId())) {
                siegesById.putIfAbsent(siege.id(), siege);
            }
        }
        for (KingdomInvite invite : invites) {
            if (kingdomsById.containsKey(invite.kingdomId())
                    && !kingdomIdsByMember.containsKey(invite.targetPlayerId())) {
                invitesById.putIfAbsent(invite.id(), invite);
            }
        }
        for (DiplomacyProposal proposal : proposals) {
            if (kingdomsById.containsKey(proposal.proposerKingdomId())
                    && kingdomsById.containsKey(proposal.targetKingdomId())) {
                proposalsById.putIfAbsent(proposal.id(), proposal);
            }
        }
        for (SettlementSupplyLedger ledger : supplyLedgers) {
            if (kingdomIdsBySettlement.containsKey(ledger.settlementId())) {
                supplyLedgersBySettlement.put(ledger.settlementId(), ledger);
            }
        }
        for (StarterCampDeployment deployment : starterCampDeployments) {
            if (kingdomsById.containsKey(deployment.kingdomId())) {
                starterCampDeploymentsByKingdom.putIfAbsent(deployment.kingdomId(), deployment);
            }
        }
        for (KingdomTechnologyState state : technologyStates) {
            KingdomRecord kingdom = kingdomsById.get(state.kingdomId());
            if (kingdom != null && kingdom.factionId().equals(state.factionId())) {
                technologyByKingdom.putIfAbsent(state.kingdomId(), state);
            }
        }
        if (legacyTechnology) {
            kingdomsById.values().forEach(kingdom -> technologyByKingdom.putIfAbsent(
                    kingdom.id(),
                    KingdomTechnologyState.legacyPending(kingdom.id(), kingdom.factionId())));
            if (!kingdomsById.isEmpty()) {
                this.setDirty();
            }
        }
    }

    public static KingdomSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public List<KingdomRecord> kingdoms() {
        return List.copyOf(kingdomsByOwner.values());
    }

    public List<ArmyGroupRecord> armyGroups() {
        return List.copyOf(armyGroupsById.values());
    }

    public List<StarterCampDeployment> starterCampDeployments() {
        return List.copyOf(starterCampDeploymentsByKingdom.values());
    }

    public List<KingdomTechnologyState> technologyStates() {
        return List.copyOf(technologyByKingdom.values());
    }

    public Optional<KingdomTechnologyState> technologyState(UUID kingdomId) {
        return Optional.ofNullable(technologyByKingdom.get(kingdomId));
    }

    public KingdomTechnologyState technologyStateOrDefault(UUID kingdomId) {
        KingdomRecord kingdom = kingdomsById.get(kingdomId);
        if (kingdom == null) {
            throw new IllegalArgumentException("Unknown kingdom " + kingdomId);
        }
        return technologyByKingdom.getOrDefault(
                kingdomId, KingdomTechnologyState.empty(kingdomId, kingdom.factionId()));
    }

    public boolean storeTechnologyState(KingdomTechnologyState state, int expectedRevision) {
        Objects.requireNonNull(state, "state");
        KingdomRecord kingdom = kingdomsById.get(state.kingdomId());
        KingdomTechnologyState current = technologyByKingdom.get(state.kingdomId());
        int currentRevision = current == null ? 0 : current.revision();
        if (kingdom == null || !kingdom.factionId().equals(state.factionId())
                || currentRevision != expectedRevision || state.revision() <= currentRevision) {
            return false;
        }
        technologyByKingdom.put(state.kingdomId(), state);
        this.setDirty();
        return true;
    }

    public Optional<StarterCampDeployment> starterCampDeployment(UUID kingdomId) {
        return Optional.ofNullable(starterCampDeploymentsByKingdom.get(kingdomId));
    }

    public boolean storeStarterCampDeployment(StarterCampDeployment deployment, int expectedRevision) {
        Objects.requireNonNull(deployment, "deployment");
        if (!kingdomsById.containsKey(deployment.kingdomId())) {
            return false;
        }
        StarterCampDeployment current = starterCampDeploymentsByKingdom.get(deployment.kingdomId());
        if ((current == null && expectedRevision != -1)
                || (current != null && current.revision() != expectedRevision)
                || (current != null && deployment.revision() <= current.revision())) {
            return false;
        }
        starterCampDeploymentsByKingdom.put(deployment.kingdomId(), deployment);
        this.setDirty();
        return true;
    }

    public List<KingdomDiplomacy> diplomacy() {
        return List.copyOf(diplomacyByPair.values());
    }

    public List<KingdomSiege> sieges() {
        return List.copyOf(siegesById.values());
    }

    public List<KingdomInvite> pendingInvites() {
        return List.copyOf(invitesById.values());
    }

    public List<DiplomacyProposal> pendingDiplomacy() {
        return List.copyOf(proposalsById.values());
    }

    public List<SettlementSupplyLedger> supplyLedgers() {
        return List.copyOf(supplyLedgersBySettlement.values());
    }

    public Optional<SettlementSupplyLedger> supplyLedger(UUID settlementId) {
        return Optional.ofNullable(supplyLedgersBySettlement.get(settlementId));
    }

    public boolean requestSupply(
            UUID ownerId,
            UUID settlementId,
            SupplyDemand demand
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || kingdom.settlements().stream().noneMatch(settlement -> settlement.id().equals(settlementId))) {
            return false;
        }
        SettlementSupplyLedger current = supplyLedgersBySettlement.get(settlementId);
        if (current == null) {
            return false;
        }
        SettlementSupplyLedger updated;
        try {
            updated = current.request(demand);
        } catch (IllegalArgumentException rejected) {
            return false;
        }
        if (updated != current) {
            supplyLedgersBySettlement.put(settlementId, updated);
            this.setDirty();
        }
        return true;
    }

    public SettlementSupplyLedger.ReservationDecision reserveSupply(
            UUID ownerId,
            UUID settlementId,
            UUID demandId,
            UUID workerId,
            StorageEndpoint endpoint,
            int requestedQuantity,
            int physicalStock,
            long gameTime,
            long leaseTicks
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        SettlementRecord settlement = kingdom == null ? null : kingdom.settlements().stream()
                .filter(candidate -> candidate.id().equals(settlementId)).findFirst().orElse(null);
        SettlementSupplyLedger current = supplyLedgersBySettlement.get(settlementId);
        if (settlement == null || current == null) {
            return SettlementSupplyLedger.ReservationDecision.rejected("endpoint_unavailable", current == null
                    ? SettlementSupplyLedger.create(settlementId) : current);
        }
        if (!settlement.containsRecruit(workerId)) {
            return SettlementSupplyLedger.ReservationDecision.rejected("worker_unavailable", current);
        }
        if (KingdomStoragePolicy.registeredEndpoints(settlement).stream().noneMatch(endpoint::equals)) {
            return SettlementSupplyLedger.ReservationDecision.rejected("endpoint_unavailable", current);
        }
        SettlementSupplyLedger.ReservationDecision decision = current.reserve(
                demandId, workerId, endpoint, requestedQuantity, physicalStock, gameTime, leaseTicks);
        if (decision.ledger() != current) {
            supplyLedgersBySettlement.put(settlementId, decision.ledger());
            this.setDirty();
        }
        return decision;
    }

    public boolean completeSupply(
            UUID ownerId,
            UUID settlementId,
            UUID reservationId,
            UUID workerId,
            int deliveredQuantity,
            long gameTime
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        SettlementRecord settlement = kingdom == null ? null : kingdom.settlements().stream()
                .filter(candidate -> candidate.id().equals(settlementId)).findFirst().orElse(null);
        SettlementSupplyLedger current = supplyLedgersBySettlement.get(settlementId);
        if (settlement == null || current == null || !settlement.containsRecruit(workerId)) {
            return false;
        }
        StorageEndpoint reservedEndpoint = current.reservations().stream()
                .filter(reservation -> reservation.id().equals(reservationId))
                .map(reservation -> reservation.endpoint())
                .findFirst()
                .orElse(null);
        if (reservedEndpoint == null || KingdomStoragePolicy.registeredEndpoints(settlement).stream()
                .noneMatch(reservedEndpoint::equals)) {
            return false;
        }
        try {
            SettlementSupplyLedger updated = current.complete(
                    reservationId, workerId, deliveredQuantity, gameTime);
            if (updated != current) {
                supplyLedgersBySettlement.put(settlementId, updated);
                this.setDirty();
            }
            return true;
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return false;
        }
    }

    public boolean releaseSupply(
            UUID ownerId,
            UUID settlementId,
            UUID reservationId,
            UUID workerId
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        SettlementRecord settlement = kingdom == null ? null : kingdom.settlements().stream()
                .filter(candidate -> candidate.id().equals(settlementId))
                .findFirst()
                .orElse(null);
        SettlementSupplyLedger current = supplyLedgersBySettlement.get(settlementId);
        if (settlement == null || current == null || !settlement.containsRecruit(workerId)) {
            return false;
        }
        SettlementSupplyLedger updated = current.release(reservationId, workerId);
        if (updated == current) {
            return false;
        }
        supplyLedgersBySettlement.put(settlementId, updated);
        this.setDirty();
        return true;
    }

    public Optional<KingdomInvite> inviteMember(
            UUID actorId, UUID targetPlayerId, KingdomMemberRole role, long expiresGameTime
    ) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_MEMBERS)
                || kingdomIdsByMember.containsKey(targetPlayerId) || role == KingdomMemberRole.OWNER
                || expiresGameTime <= 0L
                || invitesById.values().stream().anyMatch(invite ->
                invite.targetPlayerId().equals(targetPlayerId) && invite.kingdomId().equals(kingdom.id()))) {
            return Optional.empty();
        }
        UUID id = UUID.randomUUID();
        KingdomInvite invite = new KingdomInvite(id, kingdom.id(), actorId, targetPlayerId, role, expiresGameTime);
        invitesById.put(id, invite);
        setDirty();
        return Optional.of(invite);
    }

    public boolean respondToInvite(
            UUID targetPlayerId, UUID inviteId, boolean accept,
            String pledgedFactionId, long gameTime
    ) {
        KingdomInvite invite = invitesById.get(inviteId);
        if (invite == null || !invite.targetPlayerId().equals(targetPlayerId)) return false;
        invitesById.remove(inviteId);
        if (!accept || invite.expiresGameTime() < gameTime) {
            setDirty();
            return !accept;
        }
        KingdomRecord kingdom = kingdomsById.get(invite.kingdomId());
        if (kingdom == null || kingdomIdsByMember.containsKey(targetPlayerId)
                || (pledgedFactionId != null && !pledgedFactionId.isBlank()
                && !kingdom.factionId().equals(pledgedFactionId))) {
            setDirty();
            return false;
        }
        storeKingdom(kingdom.withMember(targetPlayerId, invite.offeredRole()));
        setDirty();
        return true;
    }

    public Optional<DiplomacyProposal> proposeAlliance(
            UUID actorId, UUID targetKingdomId, long gameTime,
            long treatyDurationTicks, long proposalLifetimeTicks
    ) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_DIPLOMACY)
                || !kingdomsById.containsKey(targetKingdomId) || kingdom.id().equals(targetKingdomId)
                || treatyDurationTicks <= 0L || proposalLifetimeTicks <= 0L
                || relation(kingdom.id(), targetKingdomId).cooldownUntilGameTime() > gameTime) {
            return Optional.empty();
        }
        DiplomacyProposal proposal = new DiplomacyProposal(UUID.randomUUID(), kingdom.id(), targetKingdomId,
                KingdomRelation.ALLY, treatyDurationTicks, gameTime,
                Math.addExact(gameTime, proposalLifetimeTicks));
        proposalsById.put(proposal.id(), proposal);
        setDirty();
        return Optional.of(proposal);
    }

    public boolean respondToDiplomacy(
            UUID actorId, UUID proposalId, boolean accept, long gameTime, long cooldownTicks
    ) {
        DiplomacyProposal proposal = proposalsById.get(proposalId);
        KingdomRecord responder = kingdomForPlayer(actorId).orElse(null);
        if (proposal == null || responder == null
                || !responder.id().equals(proposal.targetKingdomId())
                || !responder.allows(actorId, KingdomPermission.MANAGE_DIPLOMACY)) return false;
        proposalsById.remove(proposalId);
        if (!accept || proposal.expiresGameTime() < gameTime) {
            setDirty();
            return !accept;
        }
        KingdomDiplomacy updated = relation(proposal.proposerKingdomId(), proposal.targetKingdomId())
                .withTreaty(Math.addExact(gameTime, proposal.treatyDurationTicks()),
                        Math.addExact(gameTime, Math.max(0L, cooldownTicks)));
        diplomacyByPair.put(DiplomacyKey.of(proposal.proposerKingdomId(), proposal.targetKingdomId()), updated);
        setDirty();
        return true;
    }

    public Optional<ArmyGroupRecord> armyGroup(UUID groupId) {
        return Optional.ofNullable(armyGroupsById.get(groupId));
    }

    public Optional<ArmyGroupRecord> armyGroupForOwner(UUID ownerId) {
        return armyGroupsById.values().stream().filter(group -> group.ownerId().equals(ownerId)).findFirst();
    }

    public Optional<ArmyGroupRecord> armyGroupForRecruit(UUID recruitId) {
        return armyGroupsById.values().stream().filter(group -> group.contains(recruitId)).findFirst();
    }

    public Optional<ArmyGroupRecord> armyGroupForCommander(UUID commanderId) {
        return armyGroupsById.values().stream()
                .filter(group -> group.commanderId().filter(commanderId::equals).isPresent()).findFirst();
    }

    public Optional<KingdomRecord> kingdomForOwner(UUID ownerId) {
        return Optional.ofNullable(kingdomsByOwner.get(ownerId));
    }

    public List<ArmyGroupRecord> armyGroupsForKingdom(UUID kingdomId) {
        return armyGroupsById.values().stream().filter(group -> group.kingdomId().equals(kingdomId)).toList();
    }

    public Optional<KingdomRecord> kingdom(UUID kingdomId) {
        return Optional.ofNullable(kingdomsById.get(kingdomId));
    }

    public Optional<KingdomRecord> kingdomForPlayer(UUID playerId) {
        UUID kingdomId = kingdomIdsByMember.get(playerId);
        return kingdomId == null ? Optional.empty() : kingdom(kingdomId);
    }

    public Optional<KingdomRecord> kingdomForSettlement(UUID settlementId) {
        UUID kingdomId = kingdomIdsBySettlement.get(settlementId);
        return kingdomId == null ? Optional.empty() : kingdom(kingdomId);
    }

    public Optional<KingdomRecord> kingdomForRecruit(UUID recruitId) {
        UUID kingdomId = kingdomIdsByRecruit.get(recruitId);
        return kingdomId == null ? Optional.empty() : kingdom(kingdomId);
    }

    public boolean allows(UUID playerId, KingdomPermission permission) {
        return kingdomForPlayer(playerId).map(kingdom -> kingdom.allows(playerId, permission)).orElse(false);
    }

    public boolean addMember(UUID actorId, UUID playerId, KingdomMemberRole role, String pledgedFactionId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_MEMBERS)
                || kingdomIdsByMember.containsKey(playerId)
                || (pledgedFactionId != null && !pledgedFactionId.isBlank()
                        && !kingdom.factionId().equals(pledgedFactionId))) {
            return false;
        }
        if (role == KingdomMemberRole.OWNER) {
            return false;
        }
        storeKingdom(kingdom.withMember(playerId, role));
        this.setDirty();
        return true;
    }

    public boolean updateMemberRole(UUID actorId, UUID playerId, KingdomMemberRole role) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_MEMBERS)
                || kingdom.member(playerId).isEmpty() || role == KingdomMemberRole.OWNER) {
            return false;
        }
        storeKingdom(kingdom.withMember(playerId, role));
        this.setDirty();
        return true;
    }

    public boolean removeMember(UUID actorId, UUID playerId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        boolean selfRemoval = actorId.equals(playerId);
        if (kingdom == null || kingdom.ownerId().equals(playerId)
                || (!selfRemoval && !kingdom.allows(actorId, KingdomPermission.MANAGE_MEMBERS))) {
            return false;
        }
        KingdomRecord updated = kingdom.withoutMember(playerId);
        if (updated == kingdom) {
            return false;
        }
        storeKingdom(updated);
        this.setDirty();
        return true;
    }

    public boolean isHallActive(UUID ownerId) {
        return kingdomsByOwner.containsKey(ownerId) && !inactiveHallOwners.contains(ownerId);
    }

    public KingdomRecord foundKingdom(UUID ownerId, String factionId, String dimensionId, BlockPos hallPos) {
        KingdomRecord existing = kingdomsByOwner.get(ownerId);
        if (existing != null) {
            return existing;
        }
        if (!canActivateHall(ownerId, dimensionId, hallPos)) {
            throw new IllegalStateException("command center claim overlaps an existing kingdom claim");
        }
        return createKingdom(ownerId, factionId, dimensionId, hallPos);
    }

    private KingdomRecord createKingdom(UUID ownerId, String factionId, String dimensionId, BlockPos hallPos) {
        KingdomRecord kingdom = new KingdomRecord(
                UUID.randomUUID(),
                ownerId,
                factionId,
                SettlementRecord.create(dimensionId, hallPos.getX(), hallPos.getY(), hallPos.getZ()));
        storeKingdom(kingdom);
        this.setDirty();
        return kingdom;
    }

    public boolean canActivateHall(UUID ownerId, String dimensionId, BlockPos hallPos) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(hallPos, "hallPos");
        String normalizedDimension = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        KingdomRecord existing = kingdomsByOwner.get(ownerId);
        if (existing == null) {
            return capitalClaimAvailable(normalizedDimension, hallPos, null);
        }
        SettlementRecord settlement = existing.settlement();
        boolean sameHall = settlement.dimensionId().equals(normalizedDimension)
                && settlement.hallX() == hallPos.getX()
                && settlement.hallY() == hallPos.getY()
                && settlement.hallZ() == hallPos.getZ();
        if (!inactiveHallOwners.contains(ownerId)) {
            return sameHall;
        }
        UUID replacedCapitalClaimId = existing.claims().stream()
                .filter(KingdomClaim::capital)
                .map(KingdomClaim::id)
                .findFirst()
                .orElse(null);
        return capitalClaimAvailable(normalizedDimension, hallPos, replacedCapitalClaimId);
    }

    public Optional<KingdomRecord> activateHall(
            UUID ownerId,
            String factionId,
            String dimensionId,
            BlockPos hallPos
    ) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(hallPos, "hallPos");
        dimensionId = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        if (!canActivateHall(ownerId, dimensionId, hallPos)) {
            return Optional.empty();
        }
        KingdomRecord existing = kingdomsByOwner.get(ownerId);
        if (existing == null) {
            return Optional.of(createKingdom(ownerId, factionId, dimensionId, hallPos));
        }
        SettlementRecord settlement = existing.settlement();
        boolean sameHall = settlement.dimensionId().equals(dimensionId)
                && settlement.hallX() == hallPos.getX()
                && settlement.hallY() == hallPos.getY()
                && settlement.hallZ() == hallPos.getZ();
        if (!inactiveHallOwners.contains(ownerId)) {
            return sameHall ? Optional.of(existing) : Optional.empty();
        }
        SettlementRecord relocatedSettlement = settlement.withHallLocation(
                dimensionId, hallPos.getX(), hallPos.getY(), hallPos.getZ());
        KingdomRecord relocated = existing.withSettlement(relocatedSettlement)
                .replaceClaim(KingdomClaim.capital(existing.id(), relocatedSettlement));
        storeKingdom(relocated);
        inactiveHallOwners.remove(ownerId);
        this.setDirty();
        return Optional.of(relocated);
    }

    /**
     * Removes only a just-created, otherwise untouched kingdom while compensating a failed
     * faction-pledge transaction. Established kingdoms can never be removed through this path.
     */
    public boolean rollbackFreshKingdom(UUID ownerId, UUID expectedKingdomId) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || !kingdom.id().equals(expectedKingdomId)
                || kingdom.members().size() != 1
                || !kingdom.members().getFirst().playerId().equals(ownerId)
                || kingdom.settlements().size() != 1
                || !kingdom.settlement().recruitIds().isEmpty()
                || armyGroupsById.values().stream().anyMatch(group -> group.kingdomId().equals(kingdom.id()))
                || diplomacyByPair.values().stream().anyMatch(relation ->
                        relation.firstKingdomId().equals(kingdom.id())
                                || relation.secondKingdomId().equals(kingdom.id()))
                || siegesById.values().stream().anyMatch(siege ->
                        siege.attackerKingdomId().equals(kingdom.id())
                                || siege.defenderKingdomId().equals(kingdom.id()))) {
            return false;
        }
        kingdomsByOwner.remove(ownerId, kingdom);
        kingdomsById.remove(kingdom.id(), kingdom);
        inactiveHallOwners.remove(ownerId);
        kingdom.members().forEach(member -> kingdomIdsByMember.remove(member.playerId(), kingdom.id()));
        kingdom.settlements().forEach(settlement -> {
            kingdomIdsBySettlement.remove(settlement.id(), kingdom.id());
            supplyLedgersBySettlement.remove(settlement.id());
            settlement.recruitIds().forEach(recruitId ->
                    kingdomIdsByRecruit.remove(recruitId, kingdom.id()));
        });
        kingdom.claims().forEach(claim -> claim.chunks().forEach(chunk ->
                claimsByChunk.remove(new ClaimKey(claim.dimensionId(), chunk.x(), chunk.z()), claim)));
        invitesById.values().removeIf(invite -> invite.kingdomId().equals(kingdom.id()));
        proposalsById.values().removeIf(proposal ->
                proposal.proposerKingdomId().equals(kingdom.id())
                        || proposal.targetKingdomId().equals(kingdom.id()));
        this.setDirty();
        return true;
    }

    public boolean deactivateHall(UUID ownerId, String dimensionId, BlockPos hallPos) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || inactiveHallOwners.contains(ownerId)) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlement();
        if (!settlement.dimensionId().equals(dimensionId)
                || settlement.hallX() != hallPos.getX()
                || settlement.hallY() != hallPos.getY()
                || settlement.hallZ() != hallPos.getZ()) {
            return false;
        }
        inactiveHallOwners.add(ownerId);
        this.setDirty();
        return true;
    }

    public boolean registerRecruit(UUID actorId, UUID recruitId) {
        return registerRecruit(actorId, recruitId, NpcServiceBranch.MILITARY);
    }

    public boolean registerRecruit(UUID actorId, UUID recruitId, NpcServiceBranch serviceBranch) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.RECRUIT)
                || kingdomIdsByRecruit.containsKey(recruitId)
                || kingdom.settlement().recruitIds().size()
                        >= FactionBalanceService.effectiveRecruitLimit(kingdom.factionId())
                || !kingdom.settlement().hasHousingSpace()) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().withRecruit(recruitId);
        storeKingdom(kingdom.withSettlement(updated).withNpcBranch(recruitId, serviceBranch));
        this.setDirty();
        return true;
    }

    public boolean setNpcServiceBranch(UUID actorId, UUID recruitId, NpcServiceBranch serviceBranch) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        KingdomNpcRecord npc = kingdom == null ? null : kingdom.npc(recruitId).orElse(null);
        if (kingdom == null || npc == null
                || !(kingdom.allows(actorId, KingdomPermission.RECRUIT)
                        || kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES))) {
            return false;
        }
        if (serviceBranch == NpcServiceBranch.CIVILIAN && armyGroupForRecruit(recruitId).isPresent()) {
            return false;
        }
        boolean assignedWorksite = kingdom.settlements().stream()
                .anyMatch(settlement -> settlement.assignedWorksite(recruitId).isPresent());
        if (serviceBranch == NpcServiceBranch.MILITARY && assignedWorksite) {
            return false;
        }
        storeKingdom(kingdom.withNpcBranch(recruitId, serviceBranch));
        this.setDirty();
        return true;
    }

    public boolean unregisterRecruit(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.RECRUIT)) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().withoutRecruit(recruitId);
        if (updated == kingdom.settlement()) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return true;
    }

    public int cancelActiveCampaigns(UUID ownerId, String reasonCode) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return 0;
        }
        SettlementRecord updated = kingdom.settlement();
        int cancelled = 0;
        for (RecruitmentCampaign campaign : kingdom.settlement().recruitmentCampaigns()) {
            if (campaign.active()) {
                updated = updated.replaceCampaign(campaign.cancel(reasonCode));
                cancelled++;
            }
        }
        if (cancelled > 0) {
            storeKingdom(kingdom.withSettlement(updated));
            this.setDirty();
        }
        return cancelled;
    }

    public boolean changeFaction(UUID ownerId, String factionId) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return true;
        }
        if (!kingdom.settlement().recruitIds().isEmpty() || kingdom.settlement().hasActiveCampaign()) {
            return false;
        }
        storeKingdom(kingdom.withFaction(factionId));
        this.setDirty();
        return true;
    }

    public boolean promoteCommander(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || kingdom.npc(recruitId).map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isEmpty()
                || armyGroupForRecruit(recruitId).isPresent()) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.containsRecruit(recruitId) && candidate.hasCommanderSlot())
                .findFirst().orElse(null);
        if (settlement == null) {
            return false;
        }
        storeKingdom(kingdom.replaceSettlement(settlement.withCommander(recruitId)));
        this.setDirty();
        return true;
    }

    public Optional<ArmyGroupRecord> createOrReclaimArmyGroup(
            UUID actorId,
            UUID commanderId,
            ArmyFormation formation,
            ArmyLocation anchor,
            long gameTime
    ) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || kingdom.settlements().stream().noneMatch(
                        settlement -> settlement.commanderIds().contains(commanderId))) {
            return Optional.empty();
        }
        Optional<ArmyGroupRecord> existing = armyGroupForCommander(commanderId);
        if (existing.isPresent()) {
            ArmyGroupRecord reclaimed = existing.orElseThrow().withCommander(commanderId);
            armyGroupsById.put(reclaimed.id(), reclaimed);
            this.setDirty();
            return Optional.of(reclaimed);
        }
        Set<UUID> workerIds = kingdom.settlement().worksites().stream()
                .flatMap(worksite -> worksite.assignmentIds().stream())
                .collect(java.util.stream.Collectors.toSet());
        List<UUID> members = kingdom.settlement().recruitIds().stream()
                .filter(recruitId -> !recruitId.equals(commanderId))
                .filter(recruitId -> !workerIds.contains(recruitId))
                .filter(recruitId -> kingdom.npc(recruitId)
                        .map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isPresent())
                .filter(recruitId -> armyGroupForRecruit(recruitId).isEmpty())
                .toList();
        ArmyGroupRecord group = ArmyGroupRecord.create(
                kingdom.ownerId(), kingdom.id(), commanderId, members, formation, anchor, gameTime);
        armyGroupsById.put(group.id(), group);
        kingdomIdsByArmyGroup.put(group.id(), kingdom.id());
        this.setDirty();
        return Optional.of(group);
    }

    public boolean issueArmyOrder(UUID actorId, UUID groupId, ArmyGroupOrder order) {
        ArmyGroupRecord group = armyGroupsById.get(groupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (group == null || kingdom == null || !group.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || group.commanderId().isEmpty()
                || group.simulation().lifecycleState() == galacticwars.clonewars.army.ArmyGroupLifecycleState.ORPHANED) {
            return false;
        }
        armyGroupsById.put(groupId, group.withOrder(order));
        this.setDirty();
        return true;
    }

    public boolean replaceArmyGroup(ArmyGroupRecord group, long expectedRevision) {
        ArmyGroupRecord current = armyGroupsById.get(group.id());
        if (current == null || current.simulation().revision() != expectedRevision
                || !current.ownerId().equals(group.ownerId())) {
            return false;
        }
        armyGroupsById.put(group.id(), group);
        this.setDirty();
        return true;
    }

    /**
     * Applies a bounded set of already-validated squad replacements as one
     * SavedData transaction. The caller supplies the revision observed for
     * every group; no update is written unless every selected group still
     * belongs to the issuing player's kingdom and matches that revision.
     */
    public boolean replaceArmyGroupsAtomically(
            UUID actorId,
            List<ArmyGroupRecord> replacements,
            Map<UUID, Long> expectedRevisions
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(replacements, "replacements");
        Objects.requireNonNull(expectedRevisions, "expectedRevisions");
        if (replacements.isEmpty() || replacements.size() > ArmyFieldCommandBatch.MAX_GROUPS
                || expectedRevisions.size() != replacements.size()) {
            return false;
        }

        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)) {
            return false;
        }

        LinkedHashMap<UUID, ArmyGroupRecord> updates = new LinkedHashMap<>();
        for (ArmyGroupRecord replacement : replacements) {
            if (replacement == null || updates.putIfAbsent(replacement.id(), replacement) != null) {
                return false;
            }
            Long expectedRevision = expectedRevisions.get(replacement.id());
            ArmyGroupRecord current = armyGroupsById.get(replacement.id());
            if (expectedRevision == null || current == null
                    || current.simulation().revision() != expectedRevision
                    || replacement.simulation().revision() != expectedRevision + 1L
                    || !current.ownerId().equals(replacement.ownerId())
                    || !current.kingdomId().equals(kingdom.id())
                    || !current.kingdomId().equals(replacement.kingdomId())
                    || !current.commanderId().equals(replacement.commanderId())
                    || !current.memberIds().equals(replacement.memberIds())) {
                return false;
            }
        }

        updates.forEach(armyGroupsById::put);
        this.setDirty();
        return true;
    }

    public boolean upsertArmySnapshot(UUID groupId, ArmyMemberSnapshot snapshot) {
        ArmyGroupRecord current = armyGroupsById.get(groupId);
        if (current == null || !current.contains(snapshot.recruitId())) {
            return false;
        }
        ArmyGroupRecord updated = current.withSnapshot(snapshot);
        if (updated == current) {
            return true;
        }
        armyGroupsById.put(groupId, updated);
        this.setDirty();
        return true;
    }

    public boolean addRecruitToArmy(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || !kingdom.id().equals(kingdomIdsByRecruit.get(recruitId))
                || kingdom.npc(recruitId).map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isEmpty()) {
            return false;
        }
        Optional<ArmyGroupRecord> existing = armyGroupForRecruit(recruitId);
        if (existing.isPresent()) {
            return existing.orElseThrow().kingdomId().equals(kingdom.id());
        }
        ArmyGroupRecord group = armyGroupsById.values().stream()
                .filter(candidate -> candidate.kingdomId().equals(kingdom.id()))
                .filter(candidate -> candidate.simulation().lifecycleState() == ArmyGroupLifecycleState.LIVE)
                .min(Comparator.comparingInt((ArmyGroupRecord candidate) -> candidate.memberIds().size())
                        .thenComparing(ArmyGroupRecord::id))
                .orElse(null);
        if (group == null) {
            return false;
        }
        ArrayList<UUID> members = new ArrayList<>(group.memberIds());
        members.add(recruitId);
        armyGroupsById.put(group.id(), group.withMembers(members));
        this.setDirty();
        return true;
    }

    public boolean releaseArmyMember(UUID actorId, UUID recruitId, boolean commander, ArmyLocation lastLocation) {
        Optional<ArmyGroupRecord> groupOptional = armyGroupForRecruit(recruitId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (groupOptional.isEmpty() || kingdom == null
                || !groupOptional.orElseThrow().kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        ArmyGroupRecord group = groupOptional.orElseThrow();
        ArmyGroupRecord updated;
        if (commander || group.commanderId().filter(recruitId::equals).isPresent()) {
            updated = group.orphan(lastLocation);
        } else {
            updated = group.withMembers(group.memberIds().stream().filter(id -> !id.equals(recruitId)).toList());
        }
        armyGroupsById.put(group.id(), updated);
        this.setDirty();
        return true;
    }

    /**
     * Compatibility signature for old integrations. Caller-provided reward values are deliberately ignored.
     */
    @Deprecated
    public boolean completeBuildProject(
            UUID ownerId,
            BuildProject project,
            int housingReward,
            String worksiteType,
            int worksiteCapacity
    ) {
        return GameplayDataManager.snapshot().blueprint(project.blueprintId())
                .map(blueprint -> completeBuildProject(ownerId, project, blueprint))
                .orElse(false);
    }

    public boolean completeBuildProject(UUID ownerId, BuildProject project, KingdomBaseBlueprint blueprint) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        KingdomBaseBlueprint authoritative = GameplayDataManager.snapshot().blueprint(project.blueprintId())
                .orElse(null);
        if (kingdom == null
                || authoritative == null
                || !blueprint.id().equals(authoritative.id())
                || !blueprint.definitionHash().equals(authoritative.definitionHash())) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().withCompletedProject(project, authoritative);
        if (updated == kingdom.settlement()) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(updated));
        if (KingdomBaseBlueprint.STARTER_CAMP_ID.equals(authoritative.id())) {
            StarterCampDeployment deployment = starterCampDeploymentsByKingdom.get(kingdom.id());
            if (deployment != null
                    && deployment.projectId().filter(project.id()::equals).isPresent()
                    && deployment.phase() != galacticwars.clonewars.settlement.StarterCampDeploymentPhase.COMPLETE) {
                starterCampDeploymentsByKingdom.put(kingdom.id(), deployment.complete());
            }
        }
        this.setDirty();
        return true;
    }

    public Optional<BuildProject> startBuildProject(
            UUID ownerId,
            KingdomBaseBlueprint blueprint,
            String dimensionId,
            BlockPos origin,
            int rotationSteps
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        KingdomBaseBlueprint authoritative = GameplayDataManager.snapshot().blueprint(blueprint.id())
                .orElse(null);
        if (kingdom == null
                || authoritative == null
                || !authoritative.definitionHash().equals(blueprint.definitionHash())
                || !authoritative.supportsRotationSteps(rotationSteps)) {
            return Optional.empty();
        }
        String normalizedDimension = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        Optional<BuildProject> existing = kingdom.settlement().buildProjects().stream()
                .filter(project -> project.state() == BuildProjectState.ACTIVE || project.state() == BuildProjectState.BLOCKED)
                .filter(project -> project.dimensionId().equals(normalizedDimension)
                        && project.originX() == origin.getX()
                        && project.originY() == origin.getY()
                        && project.originZ() == origin.getZ())
                .findFirst();
        if (existing.isPresent()) {
            BuildProject project = existing.orElseThrow();
            if (!project.blueprintId().equals(authoritative.id()) || project.rotationSteps() != rotationSteps) {
                return Optional.empty();
            }
            if (!authoritative.matchesDefinitionHash(project.definitionHash())) {
                BuildProject blocked = project.block("blueprint_definition_changed");
                replaceBuildProject(ownerId, blocked);
                return Optional.of(blocked);
            }
            return existing;
        }
        BuildProject project = new BuildProject(
                UUID.randomUUID(), authoritative.id(), normalizedDimension,
                origin.getX(), origin.getY(), origin.getZ(), rotationSteps,
                authoritative.definitionHash(), List.of(), BuildProjectState.ACTIVE, "", 0);
        SettlementRecord updated = kingdom.settlement().withNewBuildProject(project);
        if (updated == kingdom.settlement()) {
            return Optional.empty();
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return Optional.of(project);
    }

    public boolean replaceBuildProject(UUID ownerId, BuildProject project) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().replaceBuildProject(project);
        if (updated == kingdom.settlement()) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return true;
    }

    public boolean reserveWorksite(UUID actorId, UUID recruitId, WorkerProfession profession) {
        return reserveWorksite(actorId, recruitId, profession, Optional.empty());
    }

    public boolean reserveWorksite(
            UUID actorId,
            UUID recruitId,
            WorkerProfession profession,
            Optional<UUID> preferredProjectId
    ) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)
                || inactiveHallOwners.contains(kingdom.ownerId()) || armyGroupForRecruit(recruitId).isPresent()) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.containsRecruit(recruitId)).findFirst().orElse(null);
        if (settlement == null) {
            return false;
        }
        SettlementRecord updated = settlement
                .reserveWorksite(recruitId, profession, preferredProjectId);
        if (updated == settlement) {
            return settlement.assignedWorksite(recruitId)
                    .filter(worksite -> worksite.accepts(profession))
                    .filter(worksite -> preferredProjectId.isEmpty()
                            || worksite.sourceProjectId().equals(preferredProjectId))
                    .isPresent();
        }
        storeKingdom(kingdom.replaceSettlement(updated).withNpcBranch(recruitId, NpcServiceBranch.CIVILIAN));
        this.setDirty();
        return true;
    }

    public Optional<WorksiteRecord> assignedWorksite(UUID ownerId, UUID recruitId) {
        return kingdomForOwner(ownerId)
                .filter(kingdom -> kingdom.settlement().containsRecruit(recruitId))
                .flatMap(kingdom -> kingdom.settlement().assignedWorksite(recruitId));
    }

    public Optional<WorksiteRecord> configureAssignedFrontierWorksite(
            UUID ownerId,
            UUID recruitId,
            String dimensionId,
            BlockPos target,
            int radius
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || inactiveHallOwners.contains(ownerId)
                || !kingdom.settlement().containsRecruit(recruitId)
                || radius < 1 || radius > 32
                || !insideSettlementClaim(kingdom.settlement(), dimensionId, target)) {
            return Optional.empty();
        }
        SettlementRecord updated = kingdom.settlement().configureAssignedFrontierWorksite(
                recruitId, dimensionId, target.getX(), target.getY(), target.getZ(), radius);
        if (updated == kingdom.settlement()) {
            return Optional.empty();
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return updated.assignedWorksite(recruitId);
    }

    public Optional<WorksiteRecord> configureAssignedCourierRoute(
            UUID actorId,
            UUID recruitId,
            List<CourierWaypoint> route,
            CourierRouteMode mode
    ) {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(mode, "mode");
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        SettlementRecord current = kingdom == null ? null : kingdom.settlement();
        WorksiteRecord courierWorksite = current == null ? null : current.assignedWorksite(recruitId)
                .filter(worksite -> worksite.accepts(WorkerProfession.COURIER))
                .orElse(null);
        if (kingdom == null
                || inactiveHallOwners.contains(kingdom.ownerId())
                || !kingdom.allows(actorId, KingdomPermission.MANAGE_LOGISTICS)
                || !current.containsRecruit(recruitId)
                || courierWorksite == null
                || route.size() == 1
                || route.size() > galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS
                || route.stream().anyMatch(waypoint ->
                        !courierWorksite.dimensionId().equals(waypoint.dimensionId()))
                || route.stream().anyMatch(waypoint -> kingdom.claims().stream().noneMatch(claim ->
                        claim.contains(waypoint.dimensionId(), waypoint.x() >> 4, waypoint.z() >> 4)))) {
            return Optional.empty();
        }
        SettlementRecord updated = current.configureAssignedCourierRoute(recruitId, route, mode);
        if (updated == current) {
            return Optional.empty();
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return updated.assignedWorksite(recruitId);
    }

    public WorksiteUpdateResult configureWorksite(
            UUID actorId,
            UUID worksiteId,
            long expectedConfigurationRevision,
            WorkAreaBounds bounds,
            boolean kingdomAccess,
            int priority,
            boolean overlayVisible,
            List<String> itemFilters,
            CourierDispatchMode dispatchMode
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(worksiteId, "worksiteId");
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(itemFilters, "itemFilters");
        Objects.requireNonNull(dispatchMode, "dispatchMode");
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || inactiveHallOwners.contains(kingdom.ownerId())
                || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)) {
            return WorksiteUpdateResult.rejected("permission_denied");
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.worksites().stream()
                        .anyMatch(worksite -> worksite.id().equals(worksiteId)))
                .findFirst()
                .orElse(null);
        if (settlement == null) {
            return WorksiteUpdateResult.rejected("worksite_missing");
        }
        WorksiteRecord current = settlement.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        if (!current.configuration().kingdomAccess()
                && !kingdom.ownerId().equals(actorId)) {
            return WorksiteUpdateResult.rejected("owner_only");
        }
        if (current.configuration().revision() != expectedConfigurationRevision) {
            return WorksiteUpdateResult.rejected("stale_revision");
        }
        SettlementRecord updated = settlement.configureWorksite(
                worksiteId,
                expectedConfigurationRevision,
                bounds,
                kingdomAccess,
                priority,
                overlayVisible,
                itemFilters,
                dispatchMode);
        if (updated == settlement) {
            return WorksiteUpdateResult.rejected("unchanged");
        }
        WorksiteRecord configured = updated.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        storeKingdom(kingdom.replaceSettlement(updated));
        this.setDirty();
        return WorksiteUpdateResult.accepted(configured);
    }

    public WorksiteUpdateResult configureWorksiteRoute(
            UUID actorId,
            UUID worksiteId,
            long expectedConfigurationRevision,
            List<CourierWaypoint> route,
            CourierRouteMode mode
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(worksiteId, "worksiteId");
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(mode, "mode");
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || inactiveHallOwners.contains(kingdom.ownerId())
                || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)
                || !kingdom.allows(actorId, KingdomPermission.MANAGE_LOGISTICS)) {
            return WorksiteUpdateResult.rejected("permission_denied");
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.worksites().stream()
                        .anyMatch(worksite -> worksite.id().equals(worksiteId)))
                .findFirst()
                .orElse(null);
        if (settlement == null) {
            return WorksiteUpdateResult.rejected("worksite_missing");
        }
        WorksiteRecord current = settlement.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        if (!current.configuration().kingdomAccess()
                && !kingdom.ownerId().equals(actorId)) {
            return WorksiteUpdateResult.rejected("owner_only");
        }
        if (current.configuration().revision() != expectedConfigurationRevision) {
            return WorksiteUpdateResult.rejected("stale_revision");
        }
        if (route.size() == 1
                || route.size() > galacticwars.clonewars.workforce.CourierRoutePlan.MAX_WAYPOINTS
                || route.stream().anyMatch(waypoint ->
                        !current.dimensionId().equals(waypoint.dimensionId()))
                || route.stream().anyMatch(waypoint -> kingdom.claims().stream().noneMatch(claim ->
                        claim.contains(waypoint.dimensionId(),
                                waypoint.x() >> 4, waypoint.z() >> 4)))) {
            return WorksiteUpdateResult.rejected("invalid_route");
        }
        SettlementRecord updated = settlement.configureWorksiteRoute(
                worksiteId,
                expectedConfigurationRevision,
                route,
                mode);
        if (updated == settlement) {
            return WorksiteUpdateResult.rejected("unchanged");
        }
        WorksiteRecord configured = updated.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        storeKingdom(kingdom.replaceSettlement(updated));
        this.setDirty();
        return WorksiteUpdateResult.accepted(configured);
    }

    public WorksiteUpdateResult configureWorksiteStorage(
            UUID actorId,
            UUID worksiteId,
            long expectedConfigurationRevision,
            StorageEndpoint endpoint
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(worksiteId, "worksiteId");
        Objects.requireNonNull(endpoint, "endpoint");
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null
                || inactiveHallOwners.contains(kingdom.ownerId())
                || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)) {
            return WorksiteUpdateResult.rejected("permission_denied");
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.worksites().stream()
                        .anyMatch(worksite -> worksite.id().equals(worksiteId)))
                .findFirst()
                .orElse(null);
        if (settlement == null) {
            return WorksiteUpdateResult.rejected("worksite_missing");
        }
        WorksiteRecord current = settlement.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        if (!current.configuration().kingdomAccess()
                && !kingdom.ownerId().equals(actorId)) {
            return WorksiteUpdateResult.rejected("owner_only");
        }
        if (current.configuration().revision() != expectedConfigurationRevision) {
            return WorksiteUpdateResult.rejected("stale_revision");
        }
        boolean registered = registeredStorageEndpoints(kingdom.ownerId()).stream()
                .anyMatch(candidate -> candidate.dimensionId().equals(endpoint.dimensionId())
                        && candidate.x() == endpoint.x()
                        && candidate.y() == endpoint.y()
                        && candidate.z() == endpoint.z()
                        && candidate.slots() == endpoint.slots());
        if (!registered || !current.dimensionId().equals(endpoint.dimensionId())) {
            return WorksiteUpdateResult.rejected("registered_storage_required");
        }
        SettlementRecord updated = settlement.configureWorksiteStorage(
                worksiteId,
                expectedConfigurationRevision,
                endpoint);
        if (updated == settlement) {
            return WorksiteUpdateResult.rejected("unchanged");
        }
        WorksiteRecord configured = updated.worksites().stream()
                .filter(worksite -> worksite.id().equals(worksiteId))
                .findFirst()
                .orElseThrow();
        storeKingdom(kingdom.replaceSettlement(updated));
        this.setDirty();
        return WorksiteUpdateResult.accepted(configured);
    }

    public boolean releaseWorksite(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.containsRecruit(recruitId)).findFirst().orElse(null);
        if (settlement == null) {
            return false;
        }
        SettlementRecord updated = settlement.releaseWorksite(recruitId);
        if (updated == settlement) {
            return false;
        }
        storeKingdom(kingdom.replaceSettlement(updated));
        this.setDirty();
        return true;
    }

    public boolean releaseWorkerAssignments(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_WORKSITES)) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.containsRecruit(recruitId)).findFirst().orElse(null);
        if (settlement == null) {
            return false;
        }
        SettlementRecord updated = settlement.releaseWorkerAssignments(recruitId);
        if (updated == settlement) {
            return false;
        }
        storeKingdom(kingdom.replaceSettlement(updated));
        this.setDirty();
        return true;
    }

    public List<StorageEndpoint> registeredStorageEndpoints(UUID ownerId) {
        return kingdomForOwner(ownerId)
                .map(KingdomRecord::settlement)
                .map(KingdomStoragePolicy::registeredEndpoints)
                .orElse(List.of());
    }

    public boolean isRegisteredStorage(UUID ownerId, String dimensionId, BlockPos target) {
        return registeredStorageEndpoint(ownerId, dimensionId, target).isPresent();
    }

    public Optional<StorageEndpoint> registeredStorageEndpoint(
            UUID ownerId,
            String dimensionId,
            BlockPos target
    ) {
        return registeredStorageEndpoints(ownerId).stream()
                .filter(endpoint -> endpoint.dimensionId().equals(dimensionId)
                        && endpoint.x() == target.getX()
                        && endpoint.y() == target.getY()
                        && endpoint.z() == target.getZ())
                .findFirst();
    }

    public Optional<WorkOrder> workOrder(UUID ownerId, UUID orderId) {
        return kingdomForOwner(ownerId).flatMap(kingdom -> kingdom.settlement().workOrder(orderId));
    }

    public Optional<WorkOrder> assignedWorkOrder(UUID ownerId, UUID recruitId) {
        return kingdomForOwner(ownerId).stream()
                .flatMap(kingdom -> kingdom.settlement().workOrders().stream())
                .filter(order -> !order.state().terminal())
                .filter(order -> order.assignedRecruitId().filter(recruitId::equals).isPresent())
                .findFirst();
    }

    public boolean queueWorkOrder(UUID ownerId, WorkOrder order) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        return kingdom != null
                && !inactiveHallOwners.contains(ownerId)
                && order.state() == WorkOrderState.QUEUED
                && order.assignedRecruitId().isEmpty()
                && order.revision() == 0
                && validWorkOrderReferences(kingdom.settlement(), order, Optional.empty(), true)
                && updateWorkOrder(ownerId, order, true);
    }

    public Optional<WorkOrder> queueAndClaimWorkOrder(UUID ownerId, UUID recruitId, WorkOrder order) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || inactiveHallOwners.contains(ownerId)
                || order.state() != WorkOrderState.QUEUED
                || order.assignedRecruitId().isPresent()
                || order.revision() != 0
                || kingdom.settlement().workOrders().stream().anyMatch(existing -> existing.id().equals(order.id()))
                || kingdom.settlement().workOrders().stream().anyMatch(existing -> !existing.state().terminal()
                        && existing.assignedRecruitId().filter(recruitId::equals).isPresent())
                || !validWorkOrderReferences(kingdom.settlement(), order, Optional.of(recruitId), true)) {
            return Optional.empty();
        }
        WorkOrder claimed = order.claim(recruitId);
        SettlementRecord queued = kingdom.settlement().withWorkOrder(order, true);
        SettlementRecord updated = queued.withWorkOrder(claimed, false);
        if (queued == kingdom.settlement() || updated == queued) {
            return Optional.empty();
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return Optional.of(claimed);
    }

    public Optional<WorkOrder> claimWorkOrder(UUID ownerId, UUID orderId, UUID recruitId, int expectedRevision) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || inactiveHallOwners.contains(ownerId)
                || kingdom.settlement().workOrders().stream().anyMatch(existing -> !existing.id().equals(orderId)
                        && !existing.state().terminal()
                        && existing.assignedRecruitId().filter(recruitId::equals).isPresent())) {
            return Optional.empty();
        }
        WorkOrder current = kingdom.settlement().workOrder(orderId)
                .filter(order -> order.revision() == expectedRevision)
                .orElse(null);
        if (current == null
                || !validWorkOrderReferences(kingdom.settlement(), current, Optional.of(recruitId), true)) {
            return Optional.empty();
        }
        return mutateWorkOrder(ownerId, orderId, expectedRevision, order -> order.claim(recruitId));
    }

    public Optional<WorkOrder> progressWorkOrder(UUID ownerId, UUID orderId, int expectedRevision, int amount) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        WorkOrder current = kingdom == null || inactiveHallOwners.contains(ownerId)
                ? null : kingdom.settlement().workOrder(orderId)
                .filter(order -> order.revision() == expectedRevision).orElse(null);
        if (current == null || current.assignedRecruitId().isEmpty()
                || !validWorkOrderReferences(
                        kingdom.settlement(), current, current.assignedRecruitId(), false)) {
            return Optional.empty();
        }
        if (current.type() == WorkOrderType.BUILD) {
            BuildProject project = current.projectId().flatMap(id -> kingdom.settlement().buildProjects().stream()
                    .filter(candidate -> candidate.id().equals(id)).findFirst()).orElse(null);
            int availableProgress = project == null
                    ? 0 : project.completedPlacements().size() - current.completedQuantity();
            if (amount < 1 || amount > availableProgress) {
                return Optional.empty();
            }
        }
        return mutateWorkOrder(ownerId, orderId, expectedRevision, order -> order.progress(amount));
    }

    public Optional<WorkOrder> blockWorkOrder(UUID ownerId, UUID orderId, int expectedRevision, String reason) {
        return mutateWorkOrder(ownerId, orderId, expectedRevision, order -> order.block(reason));
    }

    public Optional<WorkOrder> resumeWorkOrder(
            UUID ownerId,
            UUID orderId,
            UUID recruitId,
            int expectedRevision
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        WorkOrder current = kingdom == null || inactiveHallOwners.contains(ownerId)
                ? null : kingdom.settlement().workOrder(orderId)
                .filter(order -> order.revision() == expectedRevision).orElse(null);
        if (current == null
                || !validWorkOrderReferences(kingdom.settlement(), current, Optional.of(recruitId), false)) {
            return Optional.empty();
        }
        return mutateWorkOrder(ownerId, orderId, expectedRevision, order -> order.resume(recruitId));
    }

    public Optional<WorkOrder> cancelWorkOrder(UUID ownerId, UUID orderId, int expectedRevision) {
        return mutateWorkOrder(ownerId, orderId, expectedRevision, WorkOrder::cancel);
    }

    public Optional<WorkOrder> releaseWorkOrder(UUID ownerId, UUID orderId, int expectedRevision) {
        return mutateWorkOrder(ownerId, orderId, expectedRevision, WorkOrder::release);
    }

    private Optional<WorkOrder> mutateWorkOrder(
            UUID ownerId,
            UUID orderId,
            int expectedRevision,
            java.util.function.UnaryOperator<WorkOrder> operation
    ) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return Optional.empty();
        }
        WorkOrder current = kingdom.settlement().workOrders().stream()
                .filter(order -> order.id().equals(orderId) && order.revision() == expectedRevision)
                .findFirst().orElse(null);
        if (current == null) {
            return Optional.empty();
        }
        WorkOrder updated = operation.apply(current);
        return updateWorkOrder(ownerId, updated, false) ? Optional.of(updated) : Optional.empty();
    }

    private boolean updateWorkOrder(UUID ownerId, WorkOrder workOrder, boolean allowInsert) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().withWorkOrder(workOrder, allowInsert);
        if (updated == kingdom.settlement()) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return true;
    }

    private boolean validWorkOrderReferences(
            SettlementRecord settlement,
            WorkOrder order,
            Optional<UUID> recruitId,
            boolean requireActiveProject
    ) {
        WorksiteRecord worksite = order.worksiteId().stream()
                .flatMap(id -> settlement.worksites().stream().filter(candidate -> candidate.id().equals(id)))
                .findFirst().orElse(null);
        if (worksite == null
                || !worksite.accepts(order.type().profession())
                || !worksite.dimensionId().equals(order.dimensionId())
                || Math.abs(order.targetX() - worksite.x()) > worksite.radius()
                || Math.abs(order.targetZ() - worksite.z()) > worksite.radius()
                || Math.abs(order.targetY() - worksite.y()) > 4) {
            return false;
        }
        if (recruitId.isPresent()) {
            UUID recruit = recruitId.orElseThrow();
            if (!settlement.containsRecruit(recruit)
                    || !worksite.assignmentIds().contains(recruit)) {
                return false;
            }
        }
        if (order.type() == WorkOrderType.BUILD) {
            BuildProject project = order.projectId().stream()
                    .flatMap(id -> settlement.buildProjects().stream()
                            .filter(candidate -> candidate.id().equals(id)))
                    .findFirst().orElse(null);
            KingdomBaseBlueprint blueprint = project == null
                    ? null
                    : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
            if (project == null || blueprint == null
                    || worksite.sourceProjectId().filter(project.id()::equals).isEmpty()
                    || !blueprint.matchesDefinitionHash(project.definitionHash())
                    || !project.dimensionId().equals(order.dimensionId())
                    || order.targetX() != project.originX()
                    || order.targetY() != project.originY()
                    || order.targetZ() != project.originZ()
                    || order.quantity() != blueprint.placements().size()
                    || order.completedQuantity() > project.completedPlacements().size()
                    || (order.state() == WorkOrderState.QUEUED
                            && order.completedQuantity() != project.completedPlacements().size())
                    || (requireActiveProject && project.state() != BuildProjectState.ACTIVE)
                    || (!requireActiveProject && project.state() != BuildProjectState.ACTIVE
                            && project.state() != BuildProjectState.BLOCKED)) {
                return false;
            }
        } else if (order.projectId().isPresent()
                || order.quantity() != 1
                || order.completedQuantity() != 0) {
            return false;
        }
        return true;
    }

    private static boolean insideSettlementClaim(
            SettlementRecord settlement,
            String dimensionId,
            BlockPos target
    ) {
        return settlement.dimensionId().equals(KingdomNormalizers.normalize(dimensionId, "dimensionId"))
                && Math.abs(target.getX() - settlement.hallX()) <= settlement.claimRadius()
                && Math.abs(target.getZ() - settlement.hallZ()) <= settlement.claimRadius();
    }

    public boolean clearCommander(UUID actorId, UUID recruitId) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        SettlementRecord settlement = kingdom.settlements().stream()
                .filter(candidate -> candidate.commanderIds().contains(recruitId)).findFirst().orElse(null);
        if (settlement == null) {
            return false;
        }
        storeKingdom(kingdom.replaceSettlement(settlement.withoutCommander(recruitId)));
        this.setDirty();
        return true;
    }

    public boolean updateCommanderPolicy(UUID actorId, int expectedRevision, CommanderPolicy policy) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || kingdom.settlement().revision() != expectedRevision) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(kingdom.settlement().withCommanderPolicy(policy)));
        this.setDirty();
        return true;
    }

    public boolean beginCampaign(UUID ownerId, RecruitmentCampaignDecision decision) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null || !decision.accepted()) {
            return false;
        }
        RecruitmentCampaign campaign = decision.campaign().orElseThrow();
        storeKingdom(kingdom.withSettlement(kingdom.settlement().withCampaign(campaign)));
        this.setDirty();
        return true;
    }

    public boolean replaceCampaign(UUID ownerId, RecruitmentCampaign campaign) {
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return false;
        }
        SettlementRecord updated = kingdom.settlement().replaceCampaign(campaign);
        if (updated == kingdom.settlement()) {
            return false;
        }
        storeKingdom(kingdom.withSettlement(updated));
        this.setDirty();
        return true;
    }

    public int applyPendingCampaignRefunds(UUID ownerId, IntUnaryOperator refundSink) {
        Objects.requireNonNull(refundSink, "refundSink");
        KingdomRecord kingdom = kingdomsByOwner.get(ownerId);
        if (kingdom == null) {
            return 0;
        }
        SettlementRecord updated = kingdom.settlement();
        int totalRefunded = 0;
        for (RecruitmentCampaign campaign : kingdom.settlement().recruitmentCampaigns()) {
            if (!campaign.refundPending()) {
                continue;
            }
            int refunded = Math.max(0, Math.min(campaign.reservedCost(), refundSink.applyAsInt(campaign.reservedCost())));
            if (refunded > 0) {
                updated = updated.replaceCampaign(campaign.applyRefund(refunded));
                totalRefunded = Math.addExact(totalRefunded, refunded);
            }
        }
        if (totalRefunded > 0) {
            storeKingdom(kingdom.withSettlement(updated));
            this.setDirty();
        }
        return totalRefunded;
    }

    public boolean addRecruitToArmy(UUID actorId, UUID groupId, UUID recruitId) {
        ArmyGroupRecord group = armyGroupsById.get(groupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (group == null || kingdom == null || !group.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || group.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || !kingdom.id().equals(kingdomIdsByRecruit.get(recruitId))
                || kingdom.npc(recruitId).map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isEmpty()
                || armyGroupForRecruit(recruitId).isPresent()) {
            return false;
        }
        ArrayList<UUID> members = new ArrayList<>(group.memberIds());
        members.add(recruitId);
        armyGroupsById.put(group.id(), group.withMembers(members));
        this.setDirty();
        return true;
    }

    public boolean configureArmyGroup(
            UUID actorId,
            UUID groupId,
            String name,
            ArmyLocation rallyPoint,
            List<ArmyLocation> patrolRoute,
            Optional<UUID> defendedClaimId
    ) {
        ArmyGroupRecord group = armyGroupsById.get(groupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (group == null || kingdom == null || !group.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || defendedClaimId.flatMap(claimId -> kingdom.claims().stream()
                        .filter(claim -> claim.id().equals(claimId)).findFirst()).isEmpty()
                        && defendedClaimId.isPresent()) {
            return false;
        }
        ArmyGroupRecord updated = group.withName(name).withRallyPoint(rallyPoint).withPatrolRoute(patrolRoute);
        if (defendedClaimId.isPresent()) {
            updated = updated.defendingClaim(defendedClaimId.orElseThrow());
        }
        armyGroupsById.put(groupId, updated);
        this.setDirty();
        return true;
    }

    public boolean startArmyPatrol(
            UUID actorId,
            UUID groupId,
            ArmyLocation rallyPoint,
            List<ArmyLocation> patrolRoute
    ) {
        Objects.requireNonNull(rallyPoint, "rallyPoint");
        patrolRoute = List.copyOf(Objects.requireNonNull(patrolRoute, "patrolRoute"));
        ArmyGroupRecord group = armyGroupsById.get(groupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (group == null || kingdom == null
                || !group.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || group.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || patrolRoute.size() < 2 || patrolRoute.size() > 32
                || patrolRoute.stream().anyMatch(
                        waypoint -> !waypoint.dimensionId().equals(rallyPoint.dimensionId()))) {
            return false;
        }
        ArmyGroupOrder patrolOrder = new ArmyGroupOrder(
                galacticwars.clonewars.army.ArmyCommandType.PATROL_ROUTE,
                Optional.of(patrolRoute.getFirst()),
                Optional.empty(),
                group.order().formation(),
                group.order().spacing());
        ArmyGroupRecord updated = group.withRallyPoint(rallyPoint)
                .withPatrolRoute(patrolRoute)
                .withOrder(patrolOrder);
        armyGroupsById.put(groupId, updated);
        this.setDirty();
        return true;
    }

    public Optional<ArmyGroupRecord> splitArmyGroup(
            UUID actorId,
            UUID sourceGroupId,
            UUID newCommanderId,
            List<UUID> transferredMemberIds,
            String name,
            ArmyLocation anchor,
            long gameTime
    ) {
        ArmyGroupRecord source = armyGroupsById.get(sourceGroupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (source == null || kingdom == null || !source.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || source.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || !source.memberIds().contains(newCommanderId)
                || kingdom.npc(newCommanderId).map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isEmpty()
                || transferredMemberIds.stream().anyMatch(id -> kingdom.npc(id)
                        .map(KingdomNpcRecord::serviceBranch)
                        .filter(NpcServiceBranch.MILITARY::equals).isEmpty())
                || transferredMemberIds.stream().anyMatch(id -> !source.memberIds().contains(id))) {
            return Optional.empty();
        }
        SettlementRecord commanderSettlement = kingdom.settlements().stream()
                .filter(settlement -> settlement.containsRecruit(newCommanderId) && settlement.hasCommanderSlot())
                .findFirst().orElse(null);
        if (commanderSettlement == null) {
            return Optional.empty();
        }
        LinkedHashSet<UUID> transferred = new LinkedHashSet<>(transferredMemberIds);
        transferred.remove(newCommanderId);
        List<UUID> remaining = source.memberIds().stream()
                .filter(id -> !id.equals(newCommanderId) && !transferred.contains(id)).toList();
        ArmyGroupRecord created = ArmyGroupRecord.create(
                kingdom.ownerId(), kingdom.id(), newCommanderId, List.copyOf(transferred),
                source.order().formation(), anchor, gameTime).withName(name);
        storeKingdom(kingdom.replaceSettlement(commanderSettlement.withCommander(newCommanderId)));
        armyGroupsById.put(source.id(), source.withMembers(remaining));
        armyGroupsById.put(created.id(), created);
        kingdomIdsByArmyGroup.put(created.id(), kingdom.id());
        this.setDirty();
        return Optional.of(created);
    }

    public boolean mergeArmyGroups(UUID actorId, UUID targetGroupId, UUID sourceGroupId) {
        if (targetGroupId.equals(sourceGroupId)) {
            return false;
        }
        ArmyGroupRecord target = armyGroupsById.get(targetGroupId);
        ArmyGroupRecord source = armyGroupsById.get(sourceGroupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (target == null || source == null || kingdom == null
                || !target.kingdomId().equals(kingdom.id()) || !source.kingdomId().equals(kingdom.id())
                || target.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || source.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        LinkedHashSet<UUID> mergedMembers = new LinkedHashSet<>(target.memberIds());
        source.commanderId().ifPresent(mergedMembers::add);
        mergedMembers.addAll(source.memberIds());
        KingdomRecord updatedKingdom = kingdom;
        if (source.commanderId().isPresent()) {
            UUID commanderId = source.commanderId().orElseThrow();
            SettlementRecord settlement = kingdom.settlements().stream()
                    .filter(candidate -> candidate.commanderIds().contains(commanderId)).findFirst().orElse(null);
            if (settlement != null) {
                updatedKingdom = kingdom.replaceSettlement(settlement.withoutCommander(commanderId));
            }
        }
        storeKingdom(updatedKingdom);
        armyGroupsById.put(target.id(), target.withMembers(List.copyOf(mergedMembers)));
        armyGroupsById.remove(source.id());
        kingdomIdsByArmyGroup.remove(source.id());
        this.setDirty();
        return true;
    }

    public boolean changeArmySupply(UUID actorId, UUID groupId, int delta) {
        ArmyGroupRecord group = armyGroupsById.get(groupId);
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (group == null || kingdom == null || !group.kingdomId().equals(kingdom.id())
                || !kingdom.allows(actorId, KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        int updated;
        try {
            updated = Math.addExact(group.supplyUnits(), delta);
        } catch (ArithmeticException exception) {
            return false;
        }
        if (updated < 0) {
            return false;
        }
        armyGroupsById.put(groupId, group.withSupplyUnits(updated));
        this.setDirty();
        return true;
    }

    public Optional<KingdomClaim> claimAt(String dimensionId, net.minecraft.world.level.ChunkPos chunk) {
        return Optional.ofNullable(claimsByChunk.get(new ClaimKey(dimensionId, chunk.x(), chunk.z())));
    }

    public boolean expandClaim(UUID actorId, UUID claimId, String dimensionId, net.minecraft.world.level.ChunkPos chunk) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_CLAIMS)
                || claimAt(dimensionId, chunk).isPresent()) {
            return false;
        }
        KingdomClaim claim = kingdom.claims().stream()
                .filter(candidate -> candidate.id().equals(claimId) && candidate.dimensionId().equals(dimensionId))
                .findFirst().orElse(null);
        ClaimedChunk claimedChunk = new ClaimedChunk(chunk.x(), chunk.z());
        if (claim == null || !claim.canExpandTo(claimedChunk)) {
            return false;
        }
        storeKingdom(kingdom.replaceClaim(claim.expandedTo(claimedChunk)));
        this.setDirty();
        return true;
    }

    public Optional<KingdomClaim> addOutpost(UUID actorId, SettlementRecord outpost) {
        KingdomRecord kingdom = kingdomForPlayer(actorId).orElse(null);
        if (kingdom == null || !kingdom.allows(actorId, KingdomPermission.MANAGE_CLAIMS)
                || kingdom.settlements().stream().anyMatch(existing -> existing.id().equals(outpost.id()))) {
            return Optional.empty();
        }
        KingdomClaim claim = KingdomClaim.outpost(kingdom.id(), outpost);
        boolean overlap = claim.chunks().stream().anyMatch(chunk ->
                claimsByChunk.containsKey(new ClaimKey(claim.dimensionId(), chunk.x(), chunk.z())));
        if (overlap) {
            return Optional.empty();
        }
        storeKingdom(kingdom.withOutpost(outpost).replaceClaim(claim));
        this.setDirty();
        return Optional.of(claim);
    }

    public KingdomDiplomacy relation(UUID firstKingdomId, UUID secondKingdomId) {
        if (firstKingdomId.equals(secondKingdomId)) {
            throw new IllegalArgumentException("kingdom cannot have diplomacy with itself");
        }
        return diplomacyBetween(firstKingdomId, secondKingdomId)
                .orElseGet(() -> KingdomDiplomacy.neutral(firstKingdomId, secondKingdomId));
    }

    public Optional<KingdomDiplomacy> diplomacyBetween(UUID firstKingdomId, UUID secondKingdomId) {
        Objects.requireNonNull(firstKingdomId, "firstKingdomId");
        Objects.requireNonNull(secondKingdomId, "secondKingdomId");
        if (firstKingdomId.equals(secondKingdomId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(diplomacyByPair.get(DiplomacyKey.of(firstKingdomId, secondKingdomId)));
    }

    public KingdomRelation effectiveRelation(UUID firstKingdomId, UUID secondKingdomId, long gameTime) {
        return relation(firstKingdomId, secondKingdomId).effectiveRelation(gameTime);
    }

    public boolean setRelation(
            UUID actorId,
            UUID otherKingdomId,
            KingdomRelation relation,
            long gameTime,
            long cooldownTicks
    ) {
        KingdomRecord ownKingdom = kingdomForPlayer(actorId).orElse(null);
        if (ownKingdom == null || !ownKingdom.allows(actorId, KingdomPermission.MANAGE_DIPLOMACY)
                || !kingdomsById.containsKey(otherKingdomId) || ownKingdom.id().equals(otherKingdomId)) {
            return false;
        }
        KingdomDiplomacy current = relation(ownKingdom.id(), otherKingdomId);
        if (current.cooldownUntilGameTime() > gameTime) {
            return false;
        }
        KingdomDiplomacy updated = current.withRelation(relation, Math.addExact(gameTime, Math.max(0L, cooldownTicks)));
        diplomacyByPair.put(DiplomacyKey.of(ownKingdom.id(), otherKingdomId), updated);
        this.setDirty();
        return true;
    }

    public boolean establishTreaty(
            UUID actorId,
            UUID otherKingdomId,
            long gameTime,
            long durationTicks,
            long cooldownTicks
    ) {
        KingdomRecord ownKingdom = kingdomForPlayer(actorId).orElse(null);
        if (ownKingdom == null || !ownKingdom.allows(actorId, KingdomPermission.MANAGE_DIPLOMACY)
                || !kingdomsById.containsKey(otherKingdomId)
                || ownKingdom.id().equals(otherKingdomId)
                || durationTicks <= 0L) {
            return false;
        }
        KingdomDiplomacy current = relation(ownKingdom.id(), otherKingdomId);
        if (current.cooldownUntilGameTime() > gameTime || current.relation() == KingdomRelation.ENEMY) {
            return false;
        }
        KingdomDiplomacy updated = current.withTreaty(
                Math.addExact(gameTime, durationTicks),
                Math.addExact(gameTime, Math.max(0L, cooldownTicks)));
        diplomacyByPair.put(DiplomacyKey.of(ownKingdom.id(), otherKingdomId), updated);
        this.setDirty();
        return true;
    }

    public boolean setEmbargo(UUID actorId, UUID otherKingdomId, boolean embargo) {
        return setEmbargo(actorId, otherKingdomId, embargo, 0L, 0L, false);
    }

    public boolean setEmbargo(
            UUID actorId, UUID otherKingdomId, boolean embargo,
            long gameTime, long cooldownTicks
    ) {
        return setEmbargo(actorId, otherKingdomId, embargo, gameTime, cooldownTicks, true);
    }

    private boolean setEmbargo(
            UUID actorId, UUID otherKingdomId, boolean embargo,
            long gameTime, long cooldownTicks, boolean enforceCooldown
    ) {
        KingdomRecord ownKingdom = kingdomForPlayer(actorId).orElse(null);
        if (ownKingdom == null || !ownKingdom.allows(actorId, KingdomPermission.MANAGE_DIPLOMACY)
                || ownKingdom.id().equals(otherKingdomId)
                || !kingdomsById.containsKey(otherKingdomId)) {
            return false;
        }
        KingdomDiplomacy current = relation(ownKingdom.id(), otherKingdomId);
        if (enforceCooldown && current.cooldownUntilGameTime() > gameTime) return false;
        KingdomDiplomacy updated = current.withEmbargo(embargo,
                enforceCooldown
                        ? Math.addExact(gameTime, Math.max(0L, cooldownTicks))
                        : current.cooldownUntilGameTime());
        diplomacyByPair.put(DiplomacyKey.of(ownKingdom.id(), otherKingdomId), updated);
        this.setDirty();
        return true;
    }

    public Optional<KingdomSiege> startSiege(
            UUID actorId,
            UUID claimId,
            boolean defenderOnline,
            boolean withinSiegeWindow,
            int captureGoal,
            long gameTime
    ) {
        KingdomRecord attacker = kingdomForPlayer(actorId).orElse(null);
        KingdomClaim claim = kingdomsById.values().stream().flatMap(kingdom -> kingdom.claims().stream())
                .filter(candidate -> candidate.id().equals(claimId)).findFirst().orElse(null);
        KingdomRecord defender = claim == null ? null : kingdomsById.get(claim.kingdomId());
        if (attacker == null || defender == null || attacker.id().equals(defender.id())
                || !attacker.allows(actorId, KingdomPermission.COMMAND_ARMY)
                || effectiveRelation(attacker.id(), defender.id(), gameTime) != KingdomRelation.ENEMY
                || !defenderOnline || !withinSiegeWindow || captureGoal <= 0
                || siegesById.values().stream().anyMatch(siege -> siege.claimId().equals(claimId)
                        && siege.state() == SiegeState.ACTIVE)
                || (claim.capital() && defender.claims().stream().anyMatch(candidate -> !candidate.capital()))) {
            return Optional.empty();
        }
        KingdomSiege siege = KingdomSiege.start(claimId, attacker.id(), defender.id(), captureGoal, gameTime);
        siegesById.put(siege.id(), siege);
        this.setDirty();
        return Optional.of(siege);
    }

    public Optional<KingdomSiege> progressSiege(
            UUID siegeId,
            int attackerStrength,
            int defenderStrength,
            long gameTime,
            List<UUID> attackers,
            List<UUID> defenders
    ) {
        KingdomSiege current = siegesById.get(siegeId);
        if (current == null || current.state() != SiegeState.ACTIVE) {
            return Optional.empty();
        }
        KingdomSiege updated = current.progress(
                attackerStrength, defenderStrength, gameTime, attackers, defenders);
        siegesById.put(siegeId, updated);
        if (updated.state() == SiegeState.CAPTURED) {
            transferCapturedClaim(updated);
        }
        this.setDirty();
        return Optional.of(updated);
    }

    private void transferCapturedClaim(KingdomSiege siege) {
        KingdomRecord defender = kingdomsById.get(siege.defenderKingdomId());
        KingdomRecord attacker = kingdomsById.get(siege.attackerKingdomId());
        if (defender == null || attacker == null) {
            return;
        }
        KingdomClaim captured = defender.claims().stream()
                .filter(claim -> claim.id().equals(siege.claimId())).findFirst().orElse(null);
        if (captured == null || captured.capital()) {
            return;
        }
        storeKingdom(defender.withoutClaim(captured.id()));
        storeKingdom(attacker.replaceClaim(captured.transferredTo(attacker.id())));
    }

    private void indexKingdom(KingdomRecord kingdom) {
        kingdomsByOwner.put(kingdom.ownerId(), kingdom);
        kingdomsById.put(kingdom.id(), kingdom);
        kingdom.members().forEach(member -> kingdomIdsByMember.put(member.playerId(), kingdom.id()));
        kingdom.settlements().forEach(settlement -> {
            kingdomIdsBySettlement.put(settlement.id(), kingdom.id());
            supplyLedgersBySettlement.putIfAbsent(
                    settlement.id(), SettlementSupplyLedger.create(settlement.id()));
            settlement.recruitIds().forEach(recruitId -> kingdomIdsByRecruit.put(recruitId, kingdom.id()));
        });
        kingdom.claims().forEach(claim -> claim.chunks().forEach(chunk ->
                claimsByChunk.put(new ClaimKey(claim.dimensionId(), chunk.x(), chunk.z()), claim)));
    }

    private void storeKingdom(KingdomRecord kingdom) {
        KingdomRecord previous = kingdomsById.get(kingdom.id());
        if (previous != null) {
            previous.members().forEach(member -> kingdomIdsByMember.remove(member.playerId(), previous.id()));
            previous.settlements().forEach(settlement -> {
                kingdomIdsBySettlement.remove(settlement.id(), previous.id());
                settlement.recruitIds().forEach(recruitId -> kingdomIdsByRecruit.remove(recruitId, previous.id()));
            });
            previous.claims().forEach(claim -> claim.chunks().forEach(chunk ->
                    claimsByChunk.remove(new ClaimKey(claim.dimensionId(), chunk.x(), chunk.z()), claim)));
        }
        indexKingdom(kingdom);
        if (previous != null) {
            Set<UUID> retainedSettlementIds = kingdom.settlements().stream()
                    .map(SettlementRecord::id).collect(java.util.stream.Collectors.toSet());
            previous.settlements().stream().map(SettlementRecord::id)
                    .filter(settlementId -> !retainedSettlementIds.contains(settlementId))
                    .forEach(supplyLedgersBySettlement::remove);
        }
    }

    private boolean capitalClaimAvailable(String dimensionId, BlockPos hallPos, UUID replacedClaimId) {
        int centerX = hallPos.getX() >> 4;
        int centerZ = hallPos.getZ() >> 4;
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                KingdomClaim occupied = claimsByChunk.get(new ClaimKey(dimensionId, x, z));
                if (occupied != null && (replacedClaimId == null || !occupied.id().equals(replacedClaimId))) {
                    return false;
                }
            }
        }
        return true;
    }

    private record ClaimKey(String dimensionId, int x, int z) {
        private ClaimKey {
            dimensionId = KingdomNormalizers.normalize(dimensionId, "dimensionId");
        }
    }

    private record DiplomacyKey(UUID first, UUID second) {
        static DiplomacyKey of(UUID first, UUID second) {
            KingdomDiplomacy normalized = KingdomDiplomacy.neutral(first, second);
            return new DiplomacyKey(normalized.firstKingdomId(), normalized.secondKingdomId());
        }
    }
}
