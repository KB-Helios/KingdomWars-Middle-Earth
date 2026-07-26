package galacticwars.clonewars.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public final class FactionOutpostSiteSchemaMigrationTest {
    private FactionOutpostSiteSchemaMigrationTest() {
    }

    public static void main(String[] args) {
        UUID oldId = UUID.fromString("00000000-0000-0000-0000-00000000c101");
        FactionOutpostSavedData oldData = new FactionOutpostSavedData();
        oldData.registerGeneratedSite(
                oldId,
                "galacticwars:republic",
                "minecraft:overworld",
                new BlockPos(10, 70, -20),
                40,
                List.of(),
                List.of(),
                12L);
        JsonObject legacy = FactionOutpostSavedData.CODEC
                .encodeStart(JsonOps.INSTANCE, oldData).getOrThrow().getAsJsonObject();
        legacy.addProperty("schema_version", 3);
        JsonArray oldOutposts = legacy.getAsJsonArray("outposts");
        oldOutposts.get(0).getAsJsonObject().remove("site_kind");
        oldOutposts.get(0).getAsJsonObject().remove("command_post");

        FactionOutpostSavedData migrated = FactionOutpostSavedData.CODEC
                .parse(JsonOps.INSTANCE, legacy).getOrThrow();
        FactionOutpostRecord oldRecord = migrated.outpost(oldId).orElseThrow();
        assertEquals(BlueprintSiteKind.OUTPOST, oldRecord.siteKind(), "legacy site kind");
        assertTrue(oldRecord.commandPostPosition().isEmpty(), "legacy command post");

        UUID commandId = UUID.fromString("00000000-0000-0000-0000-00000000c102");
        BlockPos center = new BlockPos(120, 68, 240);
        BlockPos commandPost = center.offset(0, 1, -3);
        FactionOutpostSavedData current = new FactionOutpostSavedData();
        current.publishGeneratedSiteRecord(
                commandId,
                "galacticwars:separatist",
                "galacticwars:geonosis",
                center,
                48,
                List.of(UUID.fromString("00000000-0000-0000-0000-00000000c201")),
                List.of(),
                20L,
                BlueprintSiteKind.COMMAND_CENTER,
                Optional.of(commandPost));
        assertTrue(!current.siteGenerated(commandId), "published record is incomplete");
        current.markSiteGenerated(commandId);

        var encoded = FactionOutpostSavedData.CODEC
                .encodeStart(JsonOps.INSTANCE, current).getOrThrow();
        FactionOutpostSavedData restored = FactionOutpostSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded).getOrThrow();
        FactionOutpostRecord restoredRecord = restored.outpost(commandId).orElseThrow();
        assertEquals(BlueprintSiteKind.COMMAND_CENTER, restoredRecord.siteKind(), "current site kind");
        assertEquals(Optional.of(commandPost), restoredRecord.commandPostPosition(), "current command post");
        assertTrue(restored.siteGenerated(commandId), "current completion state");

        System.out.println("FactionOutpostSiteSchemaMigrationTest passed");
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
}
