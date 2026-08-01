package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.StorageEndpoint;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Immutable demand and lease index. Physical containers remain the resource authority. */
public record SettlementSupplyLedger(
        UUID settlementId,
        List<SupplyDemand> demands,
        List<SupplyReservation> reservations,
        int revision
) {
    public SettlementSupplyLedger {
        Objects.requireNonNull(settlementId, "settlementId");
        demands = uniqueDemands(demands);
        reservations = uniqueReservations(reservations);
        if (demands.size() > WorkforceCodecs.MAX_SUPPLY_DEMANDS) {
            throw new IllegalArgumentException("too many settlement supply demands");
        }
        if (reservations.size() > WorkforceCodecs.MAX_SUPPLY_RESERVATIONS) {
            throw new IllegalArgumentException("too many settlement supply reservations");
        }
        if (revision < 0) {
            throw new IllegalArgumentException("revision cannot be negative");
        }
    }

    public static SettlementSupplyLedger create(UUID settlementId) {
        return new SettlementSupplyLedger(settlementId, List.of(), List.of(), 0);
    }

    public Optional<SupplyDemand> nextDemand() {
        return demands.stream().filter(demand -> !demand.complete())
                .sorted(Comparator.comparingInt(SupplyDemand::priority).reversed()
                        .thenComparing(demand -> demand.id().toString()))
                .findFirst();
    }

    public SettlementSupplyLedger request(SupplyDemand demand) {
        Objects.requireNonNull(demand, "demand");
        SupplyDemand existing = demands.stream().filter(candidate -> candidate.id().equals(demand.id()))
                .findFirst().orElse(null);
        if (existing != null) {
            if (!existing.equals(demand)) {
                throw new IllegalArgumentException("supply demand id reused with different authority");
            }
            return this;
        }
        List<SupplyDemand> retainedDemands = demands;
        List<SupplyReservation> retainedReservations = reservations;
        if (demands.size() >= WorkforceCodecs.MAX_SUPPLY_DEMANDS) {
            Set<UUID> activeDemandIds = new HashSet<>();
            reservations.stream()
                    .filter(reservation -> reservation.state() == SupplyReservation.State.ACTIVE)
                    .map(SupplyReservation::demandId)
                    .forEach(activeDemandIds::add);
            int removalsNeeded = demands.size() - WorkforceCodecs.MAX_SUPPLY_DEMANDS + 1;
            ArrayList<SupplyDemand> compactedDemands = new ArrayList<>(demands.size());
            Set<UUID> removedDemandIds = new HashSet<>();
            for (SupplyDemand candidate : demands) {
                if (removalsNeeded > 0 && candidate.complete()
                        && !activeDemandIds.contains(candidate.id())) {
                    removedDemandIds.add(candidate.id());
                    removalsNeeded--;
                } else {
                    compactedDemands.add(candidate);
                }
            }
            if (removalsNeeded > 0) {
                throw new IllegalArgumentException("supply_demand_capacity");
            }
            retainedDemands = compactedDemands;
            retainedReservations = reservations.stream()
                    .filter(reservation -> !removedDemandIds.contains(reservation.demandId())
                            || reservation.state() == SupplyReservation.State.ACTIVE)
                    .toList();
        }
        ArrayList<SupplyDemand> updated = new ArrayList<>(retainedDemands);
        updated.add(demand);
        return copy(updated, retainedReservations);
    }

    public ReservationDecision reserve(
            UUID demandId,
            UUID workerId,
            StorageEndpoint endpoint,
            int requestedQuantity,
            int physicalStock,
            long gameTime,
            long leaseTicks
    ) {
        Objects.requireNonNull(demandId, "demandId");
        Objects.requireNonNull(workerId, "workerId");
        Objects.requireNonNull(endpoint, "endpoint");
        SettlementSupplyLedger cleaned = releaseExpired(gameTime);
        SupplyDemand demand = cleaned.demands.stream()
                .filter(candidate -> candidate.id().equals(demandId)).findFirst().orElse(null);
        if (demand == null || demand.complete()) {
            return ReservationDecision.rejected("demand_unavailable", cleaned);
        }
        if (requestedQuantity <= 0 || physicalStock < 0 || gameTime < 0 || leaseTicks <= 0
                || leaseTicks > Long.MAX_VALUE - gameTime) {
            return ReservationDecision.rejected("invalid_reservation", cleaned);
        }
        Optional<SupplyReservation> existingReservation = cleaned.reservations.stream()
                .filter(reservation -> reservation.demandId().equals(demandId))
                .filter(reservation -> reservation.workerId().equals(workerId))
                .filter(reservation -> reservation.active(gameTime))
                .findFirst();
        if (existingReservation.isPresent()) {
            return ReservationDecision.accepted(existingReservation.orElseThrow(), cleaned);
        }
        LinkedHashMap<UUID, SupplyDemand> demandsById = new LinkedHashMap<>();
        cleaned.demands.forEach(candidate -> demandsById.put(candidate.id(), candidate));
        int alreadyReserved = cleaned.reservations.stream()
                .filter(reservation -> reservation.active(gameTime))
                .filter(reservation -> reservation.endpoint().equals(endpoint))
                .filter(reservation -> Optional.ofNullable(demandsById.get(reservation.demandId()))
                        .map(SupplyDemand::itemId).filter(demand.itemId()::equals).isPresent())
                .mapToInt(SupplyReservation::quantity).sum();
        int demandReserved = cleaned.reservations.stream()
                .filter(reservation -> reservation.active(gameTime))
                .filter(reservation -> reservation.demandId().equals(demandId))
                .mapToInt(SupplyReservation::quantity).sum();
        int available = Math.max(0, physicalStock - alreadyReserved);
        int demandAvailable = Math.max(0, demand.outstandingQuantity() - demandReserved);
        int quantity = Math.min(Math.min(requestedQuantity, demandAvailable), available);
        if (quantity == 0) {
            return ReservationDecision.rejected("physical_stock_unavailable", cleaned);
        }
        Optional<SettlementSupplyLedger> ledgerWithRoom = cleaned.withReservationRoom();
        if (ledgerWithRoom.isEmpty()) {
            return ReservationDecision.rejected("reservation_capacity", cleaned);
        }
        SettlementSupplyLedger prepared = ledgerWithRoom.orElseThrow();
        long expiresAtGameTime = gameTime + leaseTicks;
        UUID reservationId = prepared.nextReservationId(
                demandId, workerId, gameTime, expiresAtGameTime);
        SupplyReservation reservation = new SupplyReservation(
                reservationId, demandId, workerId, endpoint, quantity,
                expiresAtGameTime, SupplyReservation.State.ACTIVE);
        ArrayList<SupplyReservation> updated = new ArrayList<>(prepared.reservations);
        updated.add(reservation);
        return ReservationDecision.accepted(reservation, prepared.copy(prepared.demands, updated));
    }

    public SettlementSupplyLedger complete(
            UUID reservationId,
            UUID workerId,
            int deliveredQuantity,
            long gameTime
    ) {
        SupplyReservation reservation = reservation(reservationId)
                .filter(candidate -> candidate.workerId().equals(workerId))
                .orElseThrow(() -> new IllegalStateException("reservation_unavailable"));
        if (deliveredQuantity != reservation.quantity()) {
            throw new IllegalArgumentException("delivery must match the reserved quantity");
        }
        if (reservation.state() == SupplyReservation.State.COMPLETED) {
            return this;
        }
        if (!reservation.active(gameTime)) {
            throw new IllegalStateException("reservation_expired");
        }
        ArrayList<SupplyDemand> updatedDemands = new ArrayList<>(demands.size());
        for (SupplyDemand demand : demands) {
            updatedDemands.add(demand.id().equals(reservation.demandId())
                    ? demand.fulfill(deliveredQuantity) : demand);
        }
        ArrayList<SupplyReservation> updatedReservations = new ArrayList<>(reservations.size());
        for (SupplyReservation candidate : reservations) {
            updatedReservations.add(candidate.id().equals(reservationId) ? candidate.complete() : candidate);
        }
        return copy(updatedDemands, updatedReservations);
    }

    public SettlementSupplyLedger releaseExpired(long gameTime) {
        boolean changed = false;
        ArrayList<SupplyReservation> updated = new ArrayList<>(reservations.size());
        for (SupplyReservation reservation : reservations) {
            SupplyReservation next = reservation.state() == SupplyReservation.State.ACTIVE
                    && !reservation.active(gameTime) ? reservation.release() : reservation;
            changed |= next != reservation;
            updated.add(next);
        }
        return changed ? copy(demands, updated) : this;
    }

    public SettlementSupplyLedger release(UUID reservationId, UUID workerId) {
        Objects.requireNonNull(reservationId, "reservationId");
        Objects.requireNonNull(workerId, "workerId");
        boolean changed = false;
        ArrayList<SupplyReservation> updated = new ArrayList<>(reservations.size());
        for (SupplyReservation reservation : reservations) {
            SupplyReservation next = reservation.id().equals(reservationId)
                    && reservation.workerId().equals(workerId)
                    ? reservation.release()
                    : reservation;
            changed |= next != reservation;
            updated.add(next);
        }
        return changed ? copy(demands, updated) : this;
    }

    public Optional<SupplyReservation> reservation(UUID reservationId) {
        return reservations.stream().filter(candidate -> candidate.id().equals(reservationId)).findFirst();
    }

    private Optional<SettlementSupplyLedger> withReservationRoom() {
        if (reservations.size() < WorkforceCodecs.MAX_SUPPLY_RESERVATIONS) {
            return Optional.of(this);
        }
        ArrayList<SupplyReservation> compacted = new ArrayList<>(reservations);
        for (int index = 0; index < compacted.size(); index++) {
            if (compacted.get(index).state() != SupplyReservation.State.ACTIVE) {
                compacted.remove(index);
                return Optional.of(copy(demands, compacted));
            }
        }
        return Optional.empty();
    }

    private UUID nextReservationId(
            UUID demandId,
            UUID workerId,
            long gameTime,
            long expiresAtGameTime
    ) {
        int salt = 0;
        while (true) {
            UUID candidate = UUID.nameUUIDFromBytes((demandId + ":" + workerId + ":"
                    + gameTime + ":" + expiresAtGameTime + ":" + revision + ":" + salt)
                    .getBytes(StandardCharsets.UTF_8));
            if (reservation(candidate).isEmpty()) {
                return candidate;
            }
            salt++;
        }
    }

    private SettlementSupplyLedger copy(List<SupplyDemand> demands, List<SupplyReservation> reservations) {
        int nextRevision = revision == Integer.MAX_VALUE ? Integer.MAX_VALUE : revision + 1;
        return new SettlementSupplyLedger(settlementId, demands, reservations, nextRevision);
    }

    private static List<SupplyDemand> uniqueDemands(List<SupplyDemand> demands) {
        LinkedHashMap<UUID, SupplyDemand> unique = new LinkedHashMap<>();
        Objects.requireNonNull(demands, "demands").forEach(demand -> unique.putIfAbsent(demand.id(), demand));
        return List.copyOf(unique.values());
    }

    private static List<SupplyReservation> uniqueReservations(List<SupplyReservation> reservations) {
        LinkedHashMap<UUID, SupplyReservation> unique = new LinkedHashMap<>();
        Objects.requireNonNull(reservations, "reservations")
                .forEach(reservation -> unique.putIfAbsent(reservation.id(), reservation));
        return List.copyOf(unique.values());
    }

    public record ReservationDecision(
            boolean accepted,
            String reason,
            Optional<SupplyReservation> reservation,
            SettlementSupplyLedger ledger
    ) {
        public static ReservationDecision accepted(
                SupplyReservation reservation,
                SettlementSupplyLedger ledger
        ) {
            return new ReservationDecision(true, "accepted", Optional.of(reservation), ledger);
        }

        public static ReservationDecision rejected(String reason, SettlementSupplyLedger ledger) {
            return new ReservationDecision(false, reason, Optional.empty(), ledger);
        }
    }
}
