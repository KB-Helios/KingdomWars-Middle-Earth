package galacticwars.clonewars.world;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Persisted, player-specific incident at one natural faction outpost. */
public record OutpostAlert(
        UUID outpostId,
        UUID playerId,
        long raisedAt,
        long expiresAt,
        String reason
) {
    public OutpostAlert {
        Objects.requireNonNull(outpostId, "outpostId");
        Objects.requireNonNull(playerId, "playerId");
        if (raisedAt < 0L || expiresAt <= raisedAt) {
            throw new IllegalArgumentException("Invalid outpost alert time bounds");
        }
        reason = Objects.requireNonNull(reason, "reason")
                .trim().toLowerCase(Locale.ROOT);
        if (reason.isEmpty() || reason.length() > 64) {
            throw new IllegalArgumentException("Invalid outpost alert reason");
        }
    }

    public boolean activeAt(long gameTime) {
        return gameTime >= raisedAt && gameTime < expiresAt;
    }

    public OutpostAlert extend(long gameTime, int durationTicks, String nextReason) {
        long nextExpiry = Math.max(expiresAt, Math.addExact(gameTime, durationTicks));
        return new OutpostAlert(
                outpostId,
                playerId,
                Math.min(raisedAt, gameTime),
                nextExpiry,
                nextReason);
    }
}
