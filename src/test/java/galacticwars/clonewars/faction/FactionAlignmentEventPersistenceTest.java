package galacticwars.clonewars.faction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public final class FactionAlignmentEventPersistenceTest {
    private static final UUID PLAYER =
            UUID.fromString("00000000-0000-0000-0000-00000000a101");

    private FactionAlignmentEventPersistenceTest() {
    }

    public static void main(String[] args) {
        replayIsRejectedAndPersists();
        eventHistoryIsBounded();
        schemaOneScoresMigrateWithoutAnEventLedger();
        System.out.println("FactionAlignmentEventPersistenceTest passed");
    }

    private static void replayIsRejectedAndPersists() {
        FactionAlignmentSavedData data = new FactionAlignmentSavedData();
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-00000000a201");
        FactionAlignmentRule rule = new FactionAlignmentRule(-5, -2, 1, "npc_damaged");
        FactionAlignmentEventResult first = data.applyEvent(
                PLAYER, eventId, catalog(), FactionId.of("republic"), rule);
        FactionAlignmentEventResult replay = data.applyEvent(
                PLAYER, eventId, catalog(), FactionId.of("republic"), rule);
        assertFalse(first.duplicate(), "first event");
        assertTrue(replay.duplicate(), "replayed event");
        assertEquals(-5, data.alignment(PLAYER).score(FactionId.of("republic")),
                "direct score applied once");
        assertEquals(-2, data.alignment(PLAYER).score(FactionId.of("mandalorian")),
                "ally score applied once");
        assertEquals(1, data.alignment(PLAYER).score(FactionId.of("separatist")),
                "enemy score applied once");

        var encoded = FactionAlignmentSavedData.CODEC
                .encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        FactionAlignmentSavedData restored = FactionAlignmentSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(restored.processed(PLAYER, eventId), "persisted replay ledger");
        assertTrue(restored.applyEvent(
                PLAYER, eventId, catalog(), FactionId.of("republic"), rule).duplicate(),
                "replay after reload");
    }

    private static void eventHistoryIsBounded() {
        FactionAlignmentSavedData data = new FactionAlignmentSavedData();
        UUID first = null;
        for (int index = 0;
                index < FactionAlignmentSavedData.MAX_PROCESSED_EVENTS_PER_PLAYER + 20;
                index++) {
            UUID event = UUID.nameUUIDFromBytes(("bounded-event-" + index).getBytes());
            if (index == 0) {
                first = event;
            }
            data.applyEvent(
                    PLAYER,
                    event,
                    catalog(),
                    FactionId.of("republic"),
                    new FactionAlignmentRule(1, 0, 0, "bounded"));
        }
        assertEquals(
                FactionAlignmentSavedData.MAX_PROCESSED_EVENTS_PER_PLAYER,
                data.processedEventCount(PLAYER),
                "bounded replay ledger");
        assertFalse(data.processed(PLAYER, first), "oldest replay entry evicted");
    }

    private static void schemaOneScoresMigrateWithoutAnEventLedger() {
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", 1);
        JsonObject player = new JsonObject();
        player.add("player_id", UUIDUtil.CODEC
                .encodeStart(JsonOps.INSTANCE, PLAYER).getOrThrow());
        JsonObject scores = new JsonObject();
        scores.addProperty("republic", 17);
        player.add("scores", scores);
        JsonArray players = new JsonArray();
        players.add(player);
        root.add("players", players);

        FactionAlignmentSavedData migrated = FactionAlignmentSavedData.CODEC
                .parse(JsonOps.INSTANCE, root).getOrThrow();
        assertEquals(17, migrated.alignment(PLAYER).score(FactionId.of("republic")),
                "legacy score");
        assertEquals(0, migrated.processedEventCount(PLAYER), "legacy event ledger");
    }

    private static FactionCatalog catalog() {
        FactionDefinition republic = new FactionDefinition(
                FactionId.of("republic"), "Republic", 25, 10, 12,
                Set.of(FactionId.of("mandalorian")),
                Set.of(FactionId.of("separatist")));
        FactionDefinition mandalorian = new FactionDefinition(
                FactionId.of("mandalorian"), "Mandalorian", 20, 8, 10,
                Set.of(FactionId.of("republic")), Set.of());
        FactionDefinition separatist = new FactionDefinition(
                FactionId.of("separatist"), "Separatist", 30, 15, 16,
                Set.of(), Set.of(FactionId.of("republic")));
        LinkedHashMap<FactionId, FactionDefinition> definitions = new LinkedHashMap<>();
        definitions.put(republic.id(), republic);
        definitions.put(mandalorian.id(), mandalorian);
        definitions.put(separatist.id(), separatist);
        return new FactionCatalog(definitions);
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
