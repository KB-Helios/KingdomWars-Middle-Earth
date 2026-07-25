package galacticwars.clonewars.faction;

import java.util.Objects;

/** Outcome of a replay-safe faction-alignment event. */
public record FactionAlignmentEventResult(
        boolean duplicate,
        FactionAlignmentUpdateResult update
) {
    public FactionAlignmentEventResult {
        Objects.requireNonNull(update, "update");
    }

    public static FactionAlignmentEventResult duplicate(FactionAlignment alignment) {
        return new FactionAlignmentEventResult(
                true, new FactionAlignmentUpdateResult(alignment, java.util.List.of()));
    }
}
