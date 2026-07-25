package galacticwars.clonewars.settlement;

import galacticwars.clonewars.faction.ai.NpcRole;

public final class BlueprintNpcRoleTest {
    private BlueprintNpcRoleTest() {
    }

    public static void main(String[] args) {
        BlueprintRosterEntry legacy = new BlueprintRosterEntry(
                "galacticwars:clone_trooper", 1, 2, 1, "military");
        BlueprintRosterEntry commander = new BlueprintRosterEntry(
                "galacticwars:clone_trooper", 1, 1, 1, "military", "commander");
        BlueprintRosterEntry trader = new BlueprintRosterEntry(
                "galacticwars:republic_civilian", 1, 1, 1, "civilian", "trader");
        assertTrue(legacy.explicitRole().isEmpty(), "legacy role remains derived");
        assertEquals(NpcRole.COMMANDER, commander.explicitRole().orElseThrow(),
                "commander role");
        assertEquals(NpcRole.TRADER, trader.explicitRole().orElseThrow(), "trader role");
        assertThrows(() -> new BlueprintRosterEntry(
                "galacticwars:clone_trooper", 1, 1, 1, "civilian", "commander"));
        assertThrows(() -> new BlueprintRosterEntry(
                "galacticwars:clone_trooper", 1, 1, 1, "military", "trader"));
        System.out.println("BlueprintNpcRoleTest passed");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertThrows(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
