package galacticwars.clonewars.faction.ai;

import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRelation;

public final class FactionDispositionResolverTest {
    private FactionDispositionResolverTest() {
    }

    public static void main(String[] args) {
        NpcAiProfile profile = NpcAiProfile.defaults(FactionId.of("galacticwars:republic"));

        assertDisposition(NpcDisposition.FRIENDLY,
                FactionDispositionResolver.resolve(FactionRelation.SAME, -100, false, profile));
        assertDisposition(NpcDisposition.FRIENDLY,
                FactionDispositionResolver.resolve(FactionRelation.ALLY, -100, false, profile));
        assertDisposition(NpcDisposition.HOSTILE,
                FactionDispositionResolver.resolve(FactionRelation.ENEMY, 100, false, profile));
        assertDisposition(NpcDisposition.HOSTILE,
                FactionDispositionResolver.resolve(FactionRelation.SAME, 100, true, profile));

        assertDisposition(NpcDisposition.FRIENDLY,
                FactionDispositionResolver.resolve(FactionRelation.NEUTRAL, 10, false, profile));
        assertDisposition(NpcDisposition.NEUTRAL,
                FactionDispositionResolver.resolve(FactionRelation.NEUTRAL, 0, false, profile));
        assertDisposition(NpcDisposition.WARY,
                FactionDispositionResolver.resolve(FactionRelation.NEUTRAL, -20, false, profile));
        NpcReactionDecision hostile = FactionDispositionResolver.resolve(
                FactionRelation.NEUTRAL, -21, false, profile);
        assertDisposition(NpcDisposition.HOSTILE, hostile);

        NpcReactionDecision friendly = FactionDispositionResolver.resolve(
                FactionRelation.NEUTRAL, 10, false, profile);
        assertTrue(friendly.tradeAllowed(), "friendly trade availability");
        assertEquals(90, friendly.tradePricePercent(), "friendly price percent");
        assertFalse(hostile.tradeAllowed(), "hostile trade availability");
        assertTrue(hostile.shouldRaiseAlert(), "hostile alarm decision");

        System.out.println("FactionDispositionResolverTest passed");
    }

    private static void assertDisposition(
            NpcDisposition expected,
            NpcReactionDecision actual
    ) {
        if (actual.disposition() != expected) {
            throw new AssertionError(
                    "expected disposition " + expected + " but was " + actual.disposition());
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean value, String label) {
        if (value) {
            throw new AssertionError(label);
        }
    }
}
