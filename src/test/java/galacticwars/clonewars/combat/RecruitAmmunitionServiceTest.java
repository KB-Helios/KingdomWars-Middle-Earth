package galacticwars.clonewars.combat;

public final class RecruitAmmunitionServiceTest {
    private RecruitAmmunitionServiceTest() {
    }

    public static void main(String[] args) {
        assertTrue(RecruitAmmunitionService.tryConsumeForShot(
                        false, 0, slot -> false, slot -> false),
                "natural faction NPCs should not require player-managed cargo");

        boolean[] ammunitionSlots = {false, true, false};
        int[] cargoCounts = {2, 0, 0};
        assertFalse(tryConsume(ammunitionSlots, cargoCounts),
                "empty player-managed cargo should reject a ranged shot");

        cargoCounts[1] = 2;
        assertTrue(tryConsume(ammunitionSlots, cargoCounts),
                "physical ammunition should authorize a shot");
        assertCount(cargoCounts[0], 2, "non-ammunition cargo must be preserved");
        assertCount(cargoCounts[1], 1, "one shot must consume exactly one item");

        assertTrue(tryConsume(ammunitionSlots, cargoCounts),
                "the second physical ammunition item should authorize one more shot");
        assertCount(cargoCounts[1], 0,
                "the final physical ammunition item should empty its slot");
        assertFalse(tryConsume(ammunitionSlots, cargoCounts),
                "depleted player-managed cargo should reject further shots");
        System.out.println("RecruitAmmunitionServiceTest passed");
    }

    private static boolean tryConsume(boolean[] ammunitionSlots, int[] cargoCounts) {
        return RecruitAmmunitionService.tryConsumeForShot(
                true,
                cargoCounts.length,
                slot -> ammunitionSlots[slot] && cargoCounts[slot] > 0,
                slot -> {
                    cargoCounts[slot]--;
                    return true;
                });
    }

    private static void assertCount(int actual, int expected, String label) {
        assertTrue(actual == expected,
                label + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        assertTrue(!condition, label);
    }
}
