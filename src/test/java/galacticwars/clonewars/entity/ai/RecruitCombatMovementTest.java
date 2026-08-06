package galacticwars.clonewars.entity.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;

public final class RecruitCombatMovementTest {
    private RecruitCombatMovementTest() {
    }

    public static void main(String[] args) {
        candidateRankingCachesWorldChecksAndDeduplicatesPositions();
        recruitAiCadencesAreBoundedAndDeterministic();

        System.out.println("RecruitCombatMovementTest passed");
    }

    private static void candidateRankingCachesWorldChecksAndDeduplicatesPositions() {
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos near = new BlockPos(1, 64, 0);
        BlockPos far = new BlockPos(3, 64, 0);
        BlockPos unsafe = new BlockPos(2, 64, 0);
        Map<BlockPos, Integer> safetyCalls = new HashMap<>();
        Map<BlockPos, Integer> coverCalls = new HashMap<>();

        List<BlockPos> ranked = RecruitCombatMovement.rankCandidates(
                List.of(near, far, near, unsafe),
                origin,
                candidate -> {
                    safetyCalls.merge(candidate, 1, Integer::sum);
                    return !candidate.equals(unsafe);
                },
                candidate -> {
                    coverCalls.merge(candidate, 1, Integer::sum);
                    return candidate.equals(far);
                });

        assertEquals(List.of(far, near), ranked, "cover-first stable ranking");
        assertEquals(Map.of(near, 1, far, 1, unsafe, 1), safetyCalls,
                "one safety evaluation per unique position");
        assertEquals(Map.of(near, 1, far, 1), coverCalls,
                "one cover evaluation per safe position");
    }

    private static void recruitAiCadencesAreBoundedAndDeterministic() {
        assertEquals(List.of(0, 8), IntStream.range(0, 16)
                        .filter(RecruitAiCadence::shouldRecomputeArmyCover)
                        .boxed()
                        .toList(),
                "army cover cadence");
        assertEquals(List.of(0, 20), IntStream.range(0, 40)
                        .filter(RecruitAiCadence::shouldCheckSelfCare)
                        .boxed()
                        .toList(),
                "self-care cadence");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
