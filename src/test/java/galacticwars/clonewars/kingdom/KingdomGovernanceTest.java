package galacticwars.clonewars.kingdom;

import java.util.List;
import java.util.UUID;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierTransferAction;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.WorkerProfession;
import net.minecraft.core.BlockPos;

public final class KingdomGovernanceTest {
    private KingdomGovernanceTest() {
    }

    public static void main(String[] args) {
        membershipRolesAreAuthoritative();
        claimsRequireContiguousExpansionAndTransferExplicitly();
        diplomacyIsSymmetricAndCooldownAware();
        siegesRequireMilitaryAdvantageToProgress();
        siegesRejectNullParticipantLists();
        npcRosterMigratesWorkersAndSoldiers();
        malformedMemberRolesUseSafeFallback();
        permissionSetsArePrecomputedAndImmutable();
        recruitmentAuthorityMatchesOperationalRoles();
        logisticsAuthorityBelongsToOperationalLeaders();
        technologyAuthorityBelongsToOwnerAndOfficers();
        courierRouteUpdatesAreRevisionedOnAssignedWorksites();
        courierRouteAdmissionRequiresRuntimeDimensionAndClaims();
        System.out.println("KingdomGovernanceTest passed");
    }

    private static void npcRosterMigratesWorkersAndSoldiers() {
        UUID worker = UUID.randomUUID();
        UUID soldier = UUID.randomUUID();
        SettlementRecord settlement = SettlementRecord.create("minecraft:overworld", 0, 64, 0)
                .withRecruit(worker)
                .withRecruit(soldier)
                .reserveWorksite(worker, WorkerProfession.FARMER);
        KingdomRecord kingdom = new KingdomRecord(
                UUID.randomUUID(), UUID.randomUUID(), "galacticwars:republic", settlement);
        assertEquals(NpcServiceBranch.CIVILIAN,
                kingdom.npc(worker).orElseThrow().serviceBranch(), "worker branch migration");
        assertEquals(NpcServiceBranch.MILITARY,
                kingdom.npc(soldier).orElseThrow().serviceBranch(), "soldier branch migration");
    }

    private static void membershipRolesAreAuthoritative() {
        UUID kingdomId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID builder = UUID.randomUUID();
        SettlementRecord capital = SettlementRecord.create("minecraft:overworld", 0, 64, 0);
        KingdomRecord kingdom = new KingdomRecord(kingdomId, owner, "galacticwars:republic", capital)
                .withMember(builder, KingdomMemberRole.BUILDER);
        assertEquals(KingdomMemberRole.OWNER, kingdom.member(owner).orElseThrow().role(), "legacy owner migration");
        assertTrue(kingdom.allows(builder, KingdomPermission.BUILD), "builder build permission");
        assertTrue(!kingdom.allows(builder, KingdomPermission.MANAGE_MEMBERS), "builder membership denial");
        assertEquals(9, kingdom.claims().getFirst().chunks().size(), "migrated capital claim size");
        assertTrue(kingdom.withoutMember(builder).member(builder).isEmpty(), "member removal");
    }

    private static void claimsRequireContiguousExpansionAndTransferExplicitly() {
        UUID firstKingdom = UUID.randomUUID();
        UUID secondKingdom = UUID.randomUUID();
        SettlementRecord outpost = SettlementRecord.create("minecraft:overworld", 320, 64, 0);
        KingdomClaim claim = KingdomClaim.outpost(firstKingdom, outpost);
        ClaimedChunk adjacent = new ClaimedChunk(claim.center().x() + 1, claim.center().z());
        ClaimedChunk disconnected = new ClaimedChunk(claim.center().x() + 3, claim.center().z());
        assertTrue(claim.canExpandTo(adjacent), "adjacent expansion");
        assertTrue(!claim.canExpandTo(disconnected), "disconnected expansion denial");
        KingdomClaim expanded = claim.expandedTo(adjacent);
        assertEquals(2, expanded.chunks().size(), "expanded chunk count");
        assertEquals(secondKingdom, expanded.transferredTo(secondKingdom).kingdomId(), "explicit transfer");
    }

    private static void diplomacyIsSymmetricAndCooldownAware() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        KingdomDiplomacy diplomacy = KingdomDiplomacy.neutral(first, second)
                .withTreaty(1200L, 200L)
                .withEmbargo(true);
        assertTrue(diplomacy.matches(second, first), "symmetric pair lookup");
        assertEquals(KingdomRelation.ALLY, diplomacy.relation(), "treaty relation");
        assertTrue(diplomacy.treatyActive(1199L), "active treaty");
        assertTrue(diplomacy.embargo(), "embargo state");
        KingdomDiplomacy renewedAlliance = diplomacy.withRelation(KingdomRelation.ALLY, 1400L);
        assertEquals(0L, renewedAlliance.treatyExpiresGameTime(), "explicit alliance clears treaty expiry");
        assertEquals(KingdomRelation.ALLY, renewedAlliance.effectiveRelation(1400L),
                "explicit alliance remains effective after expired treaty");
    }

    private static void siegesRequireMilitaryAdvantageToProgress() {
        KingdomSiege siege = KingdomSiege.start(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10, 100L);
        KingdomSiege contested = siege.progress(4, 4, 120L, List.of(), List.of());
        assertEquals(0, contested.captureProgress(), "contested progress");
        KingdomSiege captured = contested.progress(12, 2, 140L, List.of(), List.of());
        assertEquals(SiegeState.CAPTURED, captured.state(), "capture state");
        assertEquals(10, captured.captureProgress(), "bounded capture progress");
    }

    private static void malformedMemberRolesUseSafeFallback() {
        assertEquals(KingdomMemberRole.MEMBER, KingdomMemberRole.byId(null), "null role fallback");
        assertEquals(KingdomMemberRole.MEMBER, KingdomMemberRole.byId("future_role"),
                "unknown role fallback");
    }

    private static void permissionSetsArePrecomputedAndImmutable() {
        var first = KingdomPermissionPolicy.permissions(KingdomMemberRole.OFFICER);
        var second = KingdomPermissionPolicy.permissions(KingdomMemberRole.OFFICER);
        assertTrue(first == second, "precomputed permission set");
        assertThrows(() -> first.add(KingdomPermission.RECRUIT), "immutable permission set");
    }

    private static void logisticsAuthorityBelongsToOperationalLeaders() {
        assertTrue(KingdomPermissionPolicy.allows(KingdomMemberRole.OWNER,
                KingdomPermission.MANAGE_LOGISTICS), "owner logistics authority");
        assertTrue(KingdomPermissionPolicy.allows(KingdomMemberRole.OFFICER,
                KingdomPermission.MANAGE_LOGISTICS), "officer logistics authority");
        assertTrue(KingdomPermissionPolicy.allows(KingdomMemberRole.QUARTERMASTER,
                KingdomPermission.MANAGE_LOGISTICS), "quartermaster logistics authority");
        assertTrue(!KingdomPermissionPolicy.allows(KingdomMemberRole.BUILDER,
                KingdomPermission.MANAGE_LOGISTICS), "builder logistics denial");
        assertTrue(!KingdomPermissionPolicy.allows(KingdomMemberRole.MEMBER,
                KingdomPermission.MANAGE_LOGISTICS), "member logistics denial");
    }

    private static void recruitmentAuthorityMatchesOperationalRoles() {
        assertTrue(KingdomPermissionPolicy.allows(
                KingdomMemberRole.OWNER, KingdomPermission.RECRUIT), "owner recruitment");
        assertTrue(KingdomPermissionPolicy.allows(
                KingdomMemberRole.OFFICER, KingdomPermission.RECRUIT), "officer recruitment");
        assertTrue(KingdomPermissionPolicy.allows(
                KingdomMemberRole.QUARTERMASTER, KingdomPermission.RECRUIT),
                "quartermaster recruitment");
        assertTrue(!KingdomPermissionPolicy.allows(
                KingdomMemberRole.BUILDER, KingdomPermission.RECRUIT), "builder recruitment denial");
        assertTrue(!KingdomPermissionPolicy.allows(
                KingdomMemberRole.MEMBER, KingdomPermission.RECRUIT), "member recruitment denial");
        assertTrue(KingdomPermissionPolicy.allows(
                KingdomMemberRole.OFFICER, KingdomPermission.COMMAND_ARMY), "officer army command");
        assertTrue(!KingdomPermissionPolicy.allows(
                KingdomMemberRole.QUARTERMASTER, KingdomPermission.COMMAND_ARMY),
                "quartermaster army command denial");
    }

    private static void technologyAuthorityBelongsToOwnerAndOfficers() {
        assertTrue(KingdomPermissionPolicy.allows(KingdomMemberRole.OWNER,
                KingdomPermission.MANAGE_TECHNOLOGY), "owner technology authority");
        assertTrue(KingdomPermissionPolicy.allows(KingdomMemberRole.OFFICER,
                KingdomPermission.MANAGE_TECHNOLOGY), "officer technology authority");
        assertTrue(!KingdomPermissionPolicy.allows(KingdomMemberRole.QUARTERMASTER,
                KingdomPermission.MANAGE_TECHNOLOGY), "quartermaster technology denial");
        assertTrue(!KingdomPermissionPolicy.allows(KingdomMemberRole.BUILDER,
                KingdomPermission.MANAGE_TECHNOLOGY), "builder technology denial");
        assertTrue(!KingdomPermissionPolicy.allows(KingdomMemberRole.MEMBER,
                KingdomPermission.MANAGE_TECHNOLOGY), "member technology denial");
    }

    private static void courierRouteUpdatesAreRevisionedOnAssignedWorksites() {
        UUID recruit = UUID.randomUUID();
        SettlementRecord settlement = SettlementRecord.create("minecraft:overworld", 0, 64, 0)
                .withRecruit(recruit)
                .reserveWorksite(recruit, WorkerProfession.COURIER);
        List<CourierWaypoint> route = List.of(
                new CourierWaypoint("minecraft:overworld", 1, 64, 1,
                        List.of(CourierTransferAction.takeAll())),
                new CourierWaypoint("minecraft:overworld", 17, 64, 1,
                        List.of(CourierTransferAction.putAll())));
        SettlementRecord configured = settlement.configureAssignedCourierRoute(
                recruit, route, CourierRouteMode.PING_PONG);
        WorksiteRecord worksite = configured.assignedWorksite(recruit).orElseThrow();
        assertEquals(CourierRouteMode.PING_PONG,
                worksite.configuration().courierRouteMode(), "courier route mode");
        assertEquals(1L, worksite.configuration().courierRouteRevision(),
                "courier route revision");
        assertEquals(settlement.revision() + 1, configured.revision(),
                "settlement route revision");
    }

    private static void courierRouteAdmissionRequiresRuntimeDimensionAndClaims() {
        UUID owner = UUID.randomUUID();
        UUID recruit = UUID.randomUUID();
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                owner, recruit, "galacticwars:republic", "minecraft:overworld", new BlockPos(0, 64, 0));
        assertTrue(data.reserveWorksite(owner, recruit, WorkerProfession.COURIER),
                "courier worksite reserved");
        assertTrue(data.addOutpost(
                owner, SettlementRecord.create("minecraft:the_nether", 0, 64, 0)).isPresent(),
                "cross-dimension claim registered");

        List<CourierWaypoint> crossDimension = List.of(
                new CourierWaypoint("minecraft:overworld", 1, 64, 1,
                        List.of(CourierTransferAction.takeAll())),
                new CourierWaypoint("minecraft:the_nether", 1, 64, 1,
                        List.of(CourierTransferAction.putAll())));
        assertTrue(data.configureAssignedCourierRoute(
                        owner, recruit, crossDimension, CourierRouteMode.LOOP).isEmpty(),
                "claimed cross-dimension route rejected");

        List<CourierWaypoint> outsideClaim = List.of(
                new CourierWaypoint("minecraft:overworld", 1, 64, 1,
                        List.of(CourierTransferAction.takeAll())),
                new CourierWaypoint("minecraft:overworld", 512, 64, 1,
                        List.of(CourierTransferAction.putAll())));
        assertTrue(data.configureAssignedCourierRoute(
                        owner, recruit, outsideClaim, CourierRouteMode.LOOP).isEmpty(),
                "same-dimension unclaimed route rejected");

        List<CourierWaypoint> executable = List.of(
                new CourierWaypoint("minecraft:overworld", 1, 64, 1,
                        List.of(CourierTransferAction.takeAll())),
                new CourierWaypoint("minecraft:overworld", 17, 64, 1,
                        List.of(CourierTransferAction.putAll())));
        assertTrue(data.configureAssignedCourierRoute(
                        owner, recruit, executable, CourierRouteMode.LOOP).isPresent(),
                "claimed same-dimension route accepted");
    }

    private static void siegesRejectNullParticipantLists() {
        assertThrows(() -> new KingdomSiege(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SiegeState.ACTIVE, 0, 10, 0L, null, List.of()), "null attackers");
        assertThrows(() -> new KingdomSiege(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SiegeState.ACTIVE, 0, 10, 0L, List.of(), null), "null defenders");
    }

    private static void assertThrows(Runnable action, String label) {
        try {
            action.run();
            throw new AssertionError(label + " did not throw");
        } catch (RuntimeException expected) {
            // Expected validation failure.
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
