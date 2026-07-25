package galacticwars.clonewars.faction.ai;

import java.util.Objects;

/** Pure result consumed by AI, interaction, and trade authority. */
public record NpcReactionDecision(
        NpcDisposition disposition,
        boolean tradeAllowed,
        int tradePricePercent,
        boolean shouldWarn,
        boolean shouldRaiseAlert
) {
    public NpcReactionDecision {
        Objects.requireNonNull(disposition, "disposition");
        if (tradePricePercent < 1 || tradePricePercent > 500) {
            throw new IllegalArgumentException("tradePricePercent must be between 1 and 500");
        }
    }

    public static NpcReactionDecision forDisposition(
            NpcDisposition disposition,
            NpcAiProfile profile
    ) {
        Objects.requireNonNull(profile, "profile");
        return switch (Objects.requireNonNull(disposition, "disposition")) {
            case FRIENDLY -> new NpcReactionDecision(
                    disposition, true, profile.friendlyTradePricePercent(), false, false);
            case NEUTRAL -> new NpcReactionDecision(disposition, true, 100, false, false);
            case WARY -> new NpcReactionDecision(disposition, false, 100, true, false);
            case HOSTILE -> new NpcReactionDecision(disposition, false, 100, false, true);
        };
    }
}
