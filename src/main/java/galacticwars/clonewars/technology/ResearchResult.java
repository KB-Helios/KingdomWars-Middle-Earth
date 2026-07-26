package galacticwars.clonewars.technology;

public record ResearchResult(boolean accepted, boolean changed, String reason, int revision) {
    public static ResearchResult success(int revision) {
        return new ResearchResult(true, true, "", revision);
    }

    public static ResearchResult rejected(String reason, int revision) {
        return new ResearchResult(false, false, reason, revision);
    }

    public static ResearchResult replay(int revision) {
        return new ResearchResult(true, false, "replay", revision);
    }
}
