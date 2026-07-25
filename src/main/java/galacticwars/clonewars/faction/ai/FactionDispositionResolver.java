package galacticwars.clonewars.faction.ai;

import galacticwars.clonewars.faction.FactionRelation;
import java.util.Objects;

/** Shared pure precedence for diplomacy, local incidents, and reputation. */
public final class FactionDispositionResolver {
    private FactionDispositionResolver() {
    }

    public static NpcReactionDecision resolve(
            FactionRelation relation,
            int alignmentScore,
            boolean activeAlert,
            NpcAiProfile profile
    ) {
        Objects.requireNonNull(relation, "relation");
        Objects.requireNonNull(profile, "profile");
        NpcDisposition disposition;
        if (activeAlert) {
            disposition = NpcDisposition.HOSTILE;
        } else if (relation == FactionRelation.SAME || relation == FactionRelation.ALLY) {
            disposition = NpcDisposition.FRIENDLY;
        } else if (relation == FactionRelation.ENEMY) {
            disposition = NpcDisposition.HOSTILE;
        } else if (alignmentScore >= profile.friendlyThreshold()) {
            disposition = NpcDisposition.FRIENDLY;
        } else if (alignmentScore >= profile.neutralThreshold()) {
            disposition = NpcDisposition.NEUTRAL;
        } else if (alignmentScore >= profile.waryThreshold()) {
            disposition = NpcDisposition.WARY;
        } else {
            disposition = NpcDisposition.HOSTILE;
        }
        return NpcReactionDecision.forDisposition(disposition, profile);
    }
}
