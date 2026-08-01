package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.StorageEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SettlementSupplyLedgerTest {
    public static void main(String[] args) {
        UUID settlement = UUID.randomUUID();
        UUID demandId = UUID.randomUUID();
        UUID firstWorker = UUID.randomUUID();
        UUID secondWorker = UUID.randomUUID();
        StorageEndpoint storage = new StorageEndpoint("minecraft:overworld", 1, 64, 1, 9);
        SupplyDemand demand = new SupplyDemand(demandId, SupplyCategory.CONSTRUCTION,
                "minecraft:stone", 8, 0, 80, "build/project_1");
        SettlementSupplyLedger ledger = SettlementSupplyLedger.create(settlement).request(demand);

        var first = ledger.reserve(demandId, firstWorker, storage, 6, 8, 100, 20);
        assertTrue(first.accepted(), "first lease accepted");
        var second = first.ledger().reserve(demandId, secondWorker, storage, 6, 8, 100, 20);
        assertEquals(2, second.reservation().orElseThrow().quantity(), "physical stock not double reserved");

        SettlementSupplyLedger delivered = second.ledger().complete(
                first.reservation().orElseThrow().id(), firstWorker, 6, 110);
        assertEquals(2, delivered.nextDemand().orElseThrow().outstandingQuantity(), "delivery fulfills demand");
        SettlementSupplyLedger expired = delivered.releaseExpired(121);
        assertTrue(expired.reservation(second.reservation().orElseThrow().id()).orElseThrow().state()
                == SupplyReservation.State.RELEASED, "expired lease released");
        repeatedRequestsAndEndpointAccounting(demand, storage, firstWorker);
        competingWorkersRespectExpiredLeases(storage);
        releasesEveryActiveLease(storage);
        compactsCompletedDemandHistory(storage);
        handlesReservationCapacity(storage);
        System.out.println("SettlementSupplyLedgerTest passed");
    }

    private static void competingWorkersRespectExpiredLeases(StorageEndpoint storage) {
        UUID settlementId = UUID.randomUUID();
        UUID demandId = UUID.randomUUID();
        UUID firstWorker = UUID.randomUUID();
        UUID secondWorker = UUID.randomUUID();
        SupplyDemand demand = new SupplyDemand(
                demandId,
                SupplyCategory.AMMUNITION,
                "galacticwars:energy_cell",
                6,
                0,
                100,
                "recruit/" + UUID.randomUUID() + "/ammunition");
        SettlementSupplyLedger requested = SettlementSupplyLedger.create(settlementId).request(demand);

        SettlementSupplyLedger.ReservationDecision first = requested.reserve(
                demandId, firstWorker, storage, 4, 4, 100L, 10L);
        assertTrue(first.accepted(), "first competing courier reserves the partial stock");
        int firstRevision = first.ledger().revision();

        SettlementSupplyLedger.ReservationDecision blocked = first.ledger().reserve(
                demandId, secondWorker, storage, 4, 4, 101L, 10L);
        assertTrue(!blocked.accepted(), "second courier cannot double-reserve the same stock");
        assertEquals("physical_stock_unavailable", blocked.reason(),
                "stock contention has a stable rejection reason");
        assertTrue(blocked.ledger() == first.ledger(),
                "contention rejection does not advance the ledger revision");

        SettlementSupplyLedger.ReservationDecision reacquired = blocked.ledger().reserve(
                demandId, secondWorker, storage, 4, 4, 110L, 20L);
        assertTrue(reacquired.accepted(), "second courier reacquires stock at lease expiry");
        assertTrue(reacquired.ledger().revision() > firstRevision,
                "expiry and reacquisition advance the ledger revision");
        assertTrue(reacquired.ledger().reservation(first.reservation().orElseThrow().id())
                        .orElseThrow().state() == SupplyReservation.State.RELEASED,
                "expired competing lease is terminal before reacquisition");
        assertThrows(() -> reacquired.ledger().complete(
                        first.reservation().orElseThrow().id(), firstWorker, 4, 111L),
                "expired first courier cannot complete after stock is reacquired");

        SettlementSupplyLedger completed = reacquired.ledger().complete(
                reacquired.reservation().orElseThrow().id(), secondWorker, 4, 111L);
        assertEquals(2, completed.nextDemand().orElseThrow().outstandingQuantity(),
                "only the successful competing delivery fulfills demand");
        assertTrue(completed.complete(
                        reacquired.reservation().orElseThrow().id(), secondWorker, 4, 111L)
                        == completed,
                "successful competing delivery retry remains idempotent");
    }

    private static void releasesEveryActiveLease(StorageEndpoint storage) {
        SupplyDemand demand = demand(2_500, 8, 0);
        SupplyReservation firstActive = reservation(
                2_501, demand.id(), 2_501, storage, SupplyReservation.State.ACTIVE);
        SupplyReservation secondActive = reservation(
                2_502, demand.id(), 2_502, storage, SupplyReservation.State.ACTIVE);
        SupplyReservation completed = reservation(
                2_503, demand.id(), 2_503, storage, SupplyReservation.State.COMPLETED);
        SupplyReservation released = reservation(
                2_504, demand.id(), 2_504, storage, SupplyReservation.State.RELEASED);
        SettlementSupplyLedger ledger = new SettlementSupplyLedger(
                UUID.randomUUID(),
                List.of(demand),
                List.of(firstActive, secondActive, completed, released),
                12);

        SettlementSupplyLedger deactivated = ledger.releaseAllActive();
        assertTrue(deactivated.reservations().stream()
                        .noneMatch(candidate -> candidate.state() == SupplyReservation.State.ACTIVE),
                "bulk release terminates every active lease");
        assertTrue(deactivated.reservation(completed.id()).orElseThrow().state()
                        == SupplyReservation.State.COMPLETED,
                "bulk release preserves completed history");
        assertTrue(deactivated.reservation(released.id()).orElseThrow().state()
                        == SupplyReservation.State.RELEASED,
                "bulk release preserves released history");
        assertEquals(demand.outstandingQuantity(),
                deactivated.demands().getFirst().outstandingQuantity(),
                "bulk release preserves demand quantity");
        assertEquals(ledger.revision() + 1, deactivated.revision(),
                "bulk release advances the ledger once");
        assertTrue(deactivated.releaseAllActive() == deactivated,
                "repeated bulk release is idempotent");
    }

    private static void compactsCompletedDemandHistory(StorageEndpoint storage) {
        UUID settlementId = UUID.randomUUID();
        ArrayList<SupplyDemand> demands = new ArrayList<>();
        ArrayList<SupplyReservation> reservations = new ArrayList<>();
        SupplyDemand oldestCompleted = demand(0, 1, 1);
        demands.add(oldestCompleted);
        reservations.add(reservation(0, oldestCompleted.id(), 1,
                storage, SupplyReservation.State.COMPLETED));
        for (int index = 1; index < WorkforceCodecs.MAX_SUPPLY_DEMANDS - 1; index++) {
            demands.add(demand(index, 1, 1));
        }
        SupplyDemand active = demand(WorkforceCodecs.MAX_SUPPLY_DEMANDS, 2, 0);
        demands.add(active);

        SettlementSupplyLedger full = new SettlementSupplyLedger(
                settlementId, demands, reservations, 10);
        SupplyDemand replacement = demand(WorkforceCodecs.MAX_SUPPLY_DEMANDS + 1, 3, 0);
        SettlementSupplyLedger compacted = full.request(replacement);
        assertEquals(WorkforceCodecs.MAX_SUPPLY_DEMANDS, compacted.demands().size(),
                "demand history stays bounded");
        assertTrue(compacted.demands().stream().noneMatch(candidate -> candidate.id().equals(oldestCompleted.id())),
                "oldest completed demand compacted");
        assertTrue(compacted.demands().contains(active) && compacted.demands().contains(replacement),
                "active and newly requested demands preserved");
        assertTrue(compacted.reservations().isEmpty(),
                "terminal reservations for a compacted demand are removed");

        ArrayList<SupplyDemand> allActive = new ArrayList<>();
        for (int index = 0; index < WorkforceCodecs.MAX_SUPPLY_DEMANDS; index++) {
            allActive.add(demand(index + 1_000, 1, 0));
        }
        SettlementSupplyLedger saturated = new SettlementSupplyLedger(
                UUID.randomUUID(), allActive, List.of(), 0);
        assertIllegalArgument(
                () -> saturated.request(demand(2_000, 1, 0)),
                "supply_demand_capacity",
                "new demand rejected when every demand remains active");
    }

    private static void handlesReservationCapacity(StorageEndpoint storage) {
        SupplyDemand demand = demand(3_000, 10_000, 0);
        UUID retainedWorker = new UUID(40L, 1L);
        SupplyReservation retainedActive = new SupplyReservation(
                new UUID(41L, 1L), demand.id(), retainedWorker, storage,
                1, 1_000L, SupplyReservation.State.ACTIVE);
        ArrayList<SupplyReservation> reservations = new ArrayList<>();
        reservations.add(retainedActive);
        SupplyReservation oldestTerminal = null;
        SupplyReservation newestCompletedTerminal = null;
        for (int index = 1; index < WorkforceCodecs.MAX_SUPPLY_RESERVATIONS; index++) {
            SupplyReservation terminal = reservation(
                    index + 10, demand.id(), index + 10, storage,
                    index % 2 == 0
                            ? SupplyReservation.State.COMPLETED
                            : SupplyReservation.State.RELEASED);
            reservations.add(terminal);
            if (oldestTerminal == null) oldestTerminal = terminal;
            if (terminal.state() == SupplyReservation.State.COMPLETED) {
                newestCompletedTerminal = terminal;
            }
        }
        SettlementSupplyLedger historyFull = new SettlementSupplyLedger(
                UUID.randomUUID(), List.of(demand), reservations, Integer.MAX_VALUE);
        UUID newWorker = new UUID(42L, 1L);
        SettlementSupplyLedger.ReservationDecision accepted = historyFull.reserve(
                demand.id(), newWorker, storage, 1, 10_000, 100L, 20L);
        assertTrue(accepted.accepted(), "terminal history makes room for a live reservation");
        assertEquals(WorkforceCodecs.MAX_SUPPLY_RESERVATIONS,
                accepted.ledger().reservations().size(), "reservation history stays bounded");
        assertEquals(Integer.MAX_VALUE, accepted.ledger().revision(),
                "revision remains valid after counter saturation");
        assertTrue(accepted.ledger().reservation(retainedActive.id()).isPresent(),
                "active reservation survives compaction");
        assertTrue(accepted.ledger().reservation(oldestTerminal.id()).isEmpty(),
                "oldest terminal reservation compacted first");
        assertTrue(accepted.ledger().reservation(newestCompletedTerminal.id()).isPresent(),
                "recent terminal reservation retained for completion idempotence");
        assertTrue(accepted.ledger().complete(
                newestCompletedTerminal.id(), newestCompletedTerminal.workerId(),
                newestCompletedTerminal.quantity(), 100L)
                == accepted.ledger(), "retained completion retry remains a no-op");

        ArrayList<SupplyReservation> activeReservations = new ArrayList<>();
        for (int index = 0; index < WorkforceCodecs.MAX_SUPPLY_RESERVATIONS; index++) {
            activeReservations.add(reservation(
                    index + 5_000, demand.id(), index + 5_000, storage,
                    SupplyReservation.State.ACTIVE));
        }
        SettlementSupplyLedger liveFull = new SettlementSupplyLedger(
                UUID.randomUUID(), List.of(demand), activeReservations, 30);
        SettlementSupplyLedger.ReservationDecision rejected = liveFull.reserve(
                demand.id(), new UUID(43L, 1L), storage, 1,
                20_000, 100L, 20L);
        assertTrue(!rejected.accepted(), "fully active reservation ledger rejects without throwing");
        assertTrue("reservation_capacity".equals(rejected.reason()),
                "capacity rejection has a stable reason");
        assertTrue(rejected.ledger() == liveFull,
                "capacity rejection leaves the full active ledger untouched");
    }

    private static SupplyDemand demand(int seed, int quantity, int fulfilled) {
        return new SupplyDemand(
                new UUID(50L, seed), SupplyCategory.CONSTRUCTION,
                "minecraft:stone", quantity, fulfilled, 50, "test:" + seed);
    }

    private static SupplyReservation reservation(
            int seed,
            UUID demandId,
            int workerSeed,
            StorageEndpoint storage,
            SupplyReservation.State state
    ) {
        return new SupplyReservation(
                new UUID(60L, seed), demandId, new UUID(70L, workerSeed), storage,
                1, 1_000L, state);
    }

    private static void repeatedRequestsAndEndpointAccounting(
            SupplyDemand demand,
            StorageEndpoint firstStorage,
            UUID worker
    ) {
        SettlementSupplyLedger original = SettlementSupplyLedger.create(UUID.randomUUID()).request(demand);
        SettlementSupplyLedger repeated = original.request(demand);
        assertTrue(repeated == original, "identical request is a no-op");
        var first = repeated.reserve(demand.id(), worker, firstStorage, 3, 3, 200, 20);
        var retry = first.ledger().reserve(demand.id(), worker, firstStorage, 3, 3, 200, 20);
        assertTrue(retry.ledger() == first.ledger(), "reservation retry is a no-op");
        assertTrue(retry.reservation().orElseThrow().id().equals(first.reservation().orElseThrow().id()),
                "reservation retry keeps its identity");

        StorageEndpoint secondStorage = new StorageEndpoint("minecraft:overworld", 2, 64, 2, 9);
        var second = retry.ledger().reserve(demand.id(), UUID.randomUUID(), secondStorage, 10, 10, 200, 20);
        assertEquals(5, second.reservation().orElseThrow().quantity(),
                "separate endpoint stock remains capped by outstanding demand");

        SettlementSupplyLedger completed = second.ledger().complete(
                first.reservation().orElseThrow().id(), worker, 3, 201);
        assertTrue(completed.complete(first.reservation().orElseThrow().id(), worker, 3, 201) == completed,
                "completion retry is a no-op");

        var expiring = SettlementSupplyLedger.create(UUID.randomUUID()).request(demand)
                .reserve(demand.id(), UUID.randomUUID(), secondStorage, 1, 3, 300, 1);
        assertThrows(() -> expiring.ledger().complete(
                expiring.reservation().orElseThrow().id(),
                expiring.reservation().orElseThrow().workerId(), 1, 301), "expired completion rejected");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertThrows(Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError(message);
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }

    private static void assertIllegalArgument(
            Runnable action,
            String expectedMessage,
            String message
    ) {
        try {
            action.run();
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
            if (!expectedMessage.equals(expected.getMessage())) {
                throw new AssertionError(message + ": expected reason " + expectedMessage
                        + ", got " + expected.getMessage());
            }
        }
    }
}
