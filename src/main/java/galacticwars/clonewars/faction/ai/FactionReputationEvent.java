package galacticwars.clonewars.faction.ai;

import java.util.Locale;
import java.util.Optional;

/** Replay-safe authoritative events that can change faction alignment. */
public enum FactionReputationEvent {
    NPC_DAMAGED,
    NPC_KILLED,
    TRADE_COMPLETED,
    DELIVERY_COMPLETED,
    MISSION_COMPLETED,
    OUTPOST_DEFENDED;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<FactionReputationEvent> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(id.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
