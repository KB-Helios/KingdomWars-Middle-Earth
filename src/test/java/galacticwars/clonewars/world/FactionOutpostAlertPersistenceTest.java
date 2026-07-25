package galacticwars.clonewars.world;

import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public final class FactionOutpostAlertPersistenceTest {
    private FactionOutpostAlertPersistenceTest() {
    }

    public static void main(String[] args) {
        UUID outpostId = UUID.fromString("00000000-0000-0000-0000-00000000b101");
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-00000000b201");
        FactionOutpostSavedData data = new FactionOutpostSavedData();
        data.registerGeneratedSite(
                outpostId,
                "galacticwars:republic",
                "minecraft:overworld",
                new BlockPos(0, 64, 0),
                40,
                List.of(),
                List.of(),
                10L);

        OutpostAlert first = data.raiseAlert(
                outpostId, playerId, 20L, 100, "npc_damaged").orElseThrow();
        OutpostAlert extended = data.raiseAlert(
                outpostId, playerId, 40L, 200, "npc_killed").orElseThrow();
        assertEquals(120L, first.expiresAt(), "initial expiry");
        assertEquals(240L, extended.expiresAt(), "extended expiry");
        assertTrue(data.activeAlert(outpostId, playerId, 239L).isPresent(), "active alert");

        var encoded = FactionOutpostSavedData.CODEC
                .encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        FactionOutpostSavedData restored = FactionOutpostSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(restored.activeAlert(outpostId, playerId, 239L).isPresent(),
                "alert survives reload");
        assertEquals(1, restored.pruneExpired(240L), "expired alert pruned");
        assertTrue(restored.activeAlerts(outpostId, 240L).isEmpty(), "no expired alerts");

        System.out.println("FactionOutpostAlertPersistenceTest passed");
    }

    private static void assertEquals(long expected, long actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
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
}
