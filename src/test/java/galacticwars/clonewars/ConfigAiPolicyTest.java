package galacticwars.clonewars;

public final class ConfigAiPolicyTest {
    private ConfigAiPolicyTest() {
    }

    public static void main(String[] args) {
        int originalRadius = Config.NPC_AI_MAX_SCAN_RADIUS.get();
        int originalResponders = Config.NPC_AI_MAX_RESPONDERS.get();
        try {
            Config.NPC_AI_MAX_SCAN_RADIUS.set(64);
            Config.NPC_AI_MAX_RESPONDERS.set(32);
            assertEquals(64, Config.NPC_AI_MAX_SCAN_RADIUS.get(), "maximum scan radius");
            assertEquals(32, Config.NPC_AI_MAX_RESPONDERS.get(), "maximum responders");
            assertThrows(() -> Config.NPC_AI_MAX_SCAN_RADIUS.set(65), "scan radius upper bound");
            assertThrows(() -> Config.NPC_AI_MAX_SCAN_RADIUS.set(7), "scan radius lower bound");
            assertThrows(() -> Config.NPC_AI_MAX_RESPONDERS.set(0), "responder lower bound");
        } finally {
            Config.NPC_AI_MAX_SCAN_RADIUS.set(originalRadius);
            Config.NPC_AI_MAX_RESPONDERS.set(originalResponders);
        }
        System.out.println("ConfigAiPolicyTest passed");
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertThrows(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " did not reject invalid value");
    }
}
