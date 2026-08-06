package galacticwars.clonewars.entity.ai;

final class RecruitAiCadence {
    private static final int ARMY_COVER_INTERVAL_TICKS = 8;
    private static final int SELF_CARE_INTERVAL_TICKS = 20;

    private RecruitAiCadence() {
    }

    static boolean shouldRecomputeArmyCover(int tickCount) {
        return Math.floorMod(tickCount, ARMY_COVER_INTERVAL_TICKS) == 0;
    }

    static boolean shouldCheckSelfCare(int tickCount) {
        return Math.floorMod(tickCount, SELF_CARE_INTERVAL_TICKS) == 0;
    }
}
