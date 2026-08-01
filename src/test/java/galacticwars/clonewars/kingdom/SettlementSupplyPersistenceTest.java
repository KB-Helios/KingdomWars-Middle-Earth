package galacticwars.clonewars.kingdom;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import galacticwars.clonewars.workforce.SettlementSupplyLedger;
import galacticwars.clonewars.workforce.SupplyCategory;
import galacticwars.clonewars.workforce.SupplyDemand;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public final class SettlementSupplyPersistenceTest {
    private SettlementSupplyPersistenceTest() {
    }

    public static void main(String[] args) {
        authorizedReservationsPersistAndComplete();
        reservationRequiresTargetSettlementMembership();
        completionRevalidatesWorkerMembership();
        completionRevalidatesPersistedStorageEndpoint();
        deactivationReleasesActiveReservations();
        System.out.println("SettlementSupplyPersistenceTest passed");
    }

    private static void authorizedReservationsPersistAndComplete() {
        UUID ownerId = UUID.randomUUID();
        UUID workerId = UUID.randomUUID();
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                ownerId, workerId, "galacticwars:republic", "minecraft:overworld", new BlockPos(0, 64, 0));
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElseThrow();
        UUID settlementId = kingdom.settlement().id();
        StorageEndpoint endpoint = data.registeredStorageEndpoints(ownerId).getFirst();
        SupplyDemand demand = new SupplyDemand(
                UUID.randomUUID(), SupplyCategory.AMMUNITION, "galacticwars:energy_cell",
                12, 0, 80, "recruit:" + workerId);

        assertTrue(data.requestSupply(ownerId, settlementId, demand), "request accepted");
        SettlementSupplyLedger.ReservationDecision reserved = data.reserveSupply(
                ownerId, settlementId, demand.id(), workerId, endpoint, 8, 12, 100L, 40L);
        assertTrue(reserved.accepted(), "reservation accepted");

        JsonElement encoded = KingdomSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        KingdomSavedData decoded = KingdomSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        SettlementSupplyLedger restored = decoded.supplyLedger(settlementId).orElseThrow();

        assertEquals(1, restored.demands().size(), "demand count");
        assertEquals(1, restored.reservations().size(), "reservation count");
        assertEquals("galacticwars:energy_cell", restored.demands().getFirst().itemId(), "item identity");
        assertTrue(decoded.completeSupply(ownerId, settlementId,
                restored.reservations().getFirst().id(), workerId, 8, 120L), "delivery completion");
        assertEquals(4, decoded.supplyLedger(settlementId).orElseThrow()
                .demands().getFirst().outstandingQuantity(), "remaining demand");
    }

    private static void reservationRequiresTargetSettlementMembership() {
        UUID ownerId = UUID.randomUUID();
        UUID capitalWorkerId = UUID.randomUUID();
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                ownerId, capitalWorkerId, "galacticwars:republic", "minecraft:overworld",
                new BlockPos(0, 64, 0));

        SettlementRecord outpost = SettlementRecord.create("minecraft:overworld", 256, 64, 0);
        assertTrue(data.addOutpost(ownerId, outpost).isPresent(), "outpost registered");
        SupplyDemand demand = demand(capitalWorkerId);
        assertTrue(data.requestSupply(ownerId, outpost.id(), demand), "outpost demand accepted");
        StorageEndpoint outpostStorage = KingdomStoragePolicy.registeredEndpoints(outpost).getFirst();

        SettlementSupplyLedger.ReservationDecision rejected = data.reserveSupply(
                ownerId, outpost.id(), demand.id(), capitalWorkerId,
                outpostStorage, 4, 8, 100L, 40L);
        assertTrue(!rejected.accepted(), "cross-settlement worker rejected");
        assertEquals("worker_unavailable", rejected.reason(), "membership rejection reason");
        assertEquals(0, rejected.ledger().reservations().size(), "rejected reservation count");
    }

    private static void completionRevalidatesWorkerMembership() {
        UUID ownerId = UUID.randomUUID();
        UUID workerId = UUID.randomUUID();
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                ownerId, workerId, "galacticwars:republic", "minecraft:overworld", new BlockPos(0, 64, 0));
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElseThrow();
        SupplyDemand demand = demand(workerId);
        assertTrue(data.requestSupply(ownerId, kingdom.settlement().id(), demand), "demand accepted");
        StorageEndpoint endpoint = data.registeredStorageEndpoints(ownerId).getFirst();
        SettlementSupplyLedger.ReservationDecision reserved = data.reserveSupply(
                ownerId, kingdom.settlement().id(), demand.id(), workerId,
                endpoint, 4, 8, 100L, 40L);
        assertTrue(reserved.accepted(), "reservation accepted before worker removal");
        assertTrue(data.unregisterRecruit(ownerId, workerId), "worker removed");

        assertTrue(!data.completeSupply(
                        ownerId,
                        kingdom.settlement().id(),
                        reserved.reservation().orElseThrow().id(),
                        workerId,
                        4,
                        120L),
                "removed worker completion rejected");
    }

    private static void completionRevalidatesPersistedStorageEndpoint() {
        UUID ownerId = UUID.randomUUID();
        UUID workerId = UUID.randomUUID();
        BlockPos originalHall = new BlockPos(0, 64, 0);
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                ownerId, workerId, "galacticwars:republic", "minecraft:overworld", originalHall);
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElseThrow();
        SupplyDemand demand = demand(workerId);
        assertTrue(data.requestSupply(ownerId, kingdom.settlement().id(), demand), "demand accepted");
        StorageEndpoint originalEndpoint = data.registeredStorageEndpoints(ownerId).getFirst();
        SettlementSupplyLedger.ReservationDecision reserved = data.reserveSupply(
                ownerId, kingdom.settlement().id(), demand.id(), workerId,
                originalEndpoint, 4, 8, 100L, 40L);
        assertTrue(reserved.accepted(), "reservation accepted before relocation");

        assertTrue(data.deactivateHall(ownerId, "minecraft:overworld", originalHall),
                "original hall deactivated");
        assertTrue(data.activateHall(
                        ownerId,
                        "galacticwars:republic",
                        "minecraft:overworld",
                        new BlockPos(160, 64, 0)).isPresent(),
                "hall relocated");
        assertTrue(!data.registeredStorageEndpoints(ownerId).contains(originalEndpoint),
                "reserved endpoint deregistered");

        assertTrue(!data.completeSupply(
                        ownerId,
                        kingdom.settlement().id(),
                        reserved.reservation().orElseThrow().id(),
                        workerId,
                        4,
                        120L),
                "deregistered endpoint completion rejected");
    }

    private static void deactivationReleasesActiveReservations() {
        UUID ownerId = UUID.randomUUID();
        UUID workerId = UUID.randomUUID();
        BlockPos hall = new BlockPos(0, 64, 0);
        KingdomSavedData data = KingdomTestFixtures.withCivilianRecruit(
                ownerId, workerId, "galacticwars:republic", "minecraft:overworld", hall);
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElseThrow();
        UUID settlementId = kingdom.settlement().id();
        SupplyDemand demand = demand(workerId);
        assertTrue(data.requestSupply(ownerId, settlementId, demand), "demand accepted");
        StorageEndpoint endpoint = data.registeredStorageEndpoints(ownerId).getFirst();
        SettlementSupplyLedger.ReservationDecision reserved = data.reserveSupply(
                ownerId, settlementId, demand.id(), workerId,
                endpoint, 4, 8, 100L, 1_200L);
        assertTrue(reserved.accepted(), "reservation accepted before deactivation");

        assertTrue(data.deactivateHall(ownerId, "minecraft:overworld", hall),
                "active hall deactivated");
        SettlementSupplyLedger deactivated = data.supplyLedger(settlementId).orElseThrow();
        assertTrue(deactivated.reservation(reserved.reservation().orElseThrow().id())
                        .orElseThrow().state()
                        == galacticwars.clonewars.workforce.SupplyReservation.State.RELEASED,
                "deactivation releases the active reservation");
        assertEquals(8, deactivated.demands().getFirst().outstandingQuantity(),
                "deactivation preserves outstanding demand");

        SettlementSupplyLedger.ReservationDecision inactiveRetry = data.reserveSupply(
                ownerId, settlementId, demand.id(), workerId,
                endpoint, 4, 8, 101L, 1_200L);
        assertTrue(!inactiveRetry.accepted(), "inactive settlement rejects a new reservation");
        assertEquals("settlement_inactive", inactiveRetry.reason(),
                "inactive reservation rejection reason");
    }

    private static SupplyDemand demand(UUID workerId) {
        return new SupplyDemand(
                UUID.randomUUID(),
                SupplyCategory.AMMUNITION,
                "galacticwars:energy_cell",
                8,
                0,
                80,
                "recruit:" + workerId);
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
