package galacticwars.clonewars.kingdom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KingdomSavedDataTest {
    private KingdomSavedDataTest() {
    }

    public static void main(String[] args) throws IOException {
        savedDataUsesVersionedOverworldStorage();
        savedDataExposesGuardedMutations();
        recordsKeepRuntimeCodecsOutOfThePureDomainLayer();
        schema9StarterCampDeploymentsRoundTrip();
        schema8MigrationPreservesData();
        System.out.println("KingdomSavedDataTest passed");
    }

    private static void savedDataUsesVersionedOverworldStorage() throws IOException {
        String source = read("src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java");
        assertContains(source, "CURRENT_SCHEMA_VERSION", "schema version");
        assertContains(source, "CURRENT_SCHEMA_VERSION = 11", "kingdom workforce schema version");
        assertContains(source, "\"technology\"", "kingdom technology persistence");
        assertContains(source, "starter_camp_deployments", "starter camp exact-once persistence");
        assertContains(source, "pending_invites", "pending invitation persistence");
        assertContains(source, "pending_diplomacy", "pending diplomacy persistence");
        assertContains(source, "inactive_hall_owners", "inactive Hall persistence");
        assertContains(source, "SavedDataType<KingdomSavedData>", "saved data type");
        assertContains(source, "level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE)", "overworld storage");
        assertContains(source, "this.setDirty()", "dirty tracking");
    }

    private static void savedDataExposesGuardedMutations() throws IOException {
        String source = read("src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java");
        assertContains(source, "foundKingdom", "kingdom founding");
        assertContains(source, "registerRecruit", "recruit registration");
        assertContains(source, "FactionBalanceService.effectiveRecruitLimit(kingdom.factionId())",
                "authoritative kingdom faction recruit cap");
        assertContains(source, "unregisterRecruit", "recruit removal");
        assertContains(source, "activateHall", "Hall activation and relocation");
        assertContains(source, "deactivateHall", "Hall deactivation");
        assertContains(source, "cancelActiveCampaigns", "transactional campaign cancellation");
        assertContains(source, "promoteCommander", "commander promotion");
        assertContains(source, "completeBuildProject", "building completion rewards");
        assertContains(source, "reserveWorksite", "capacity reservation");
        assertContains(source, "claimWorkOrder", "atomic work order claim");
        assertContains(source, "progressWorkOrder", "guarded work order progress");
        assertContains(source, "hasCommanderSlot", "commander unlock guard");
        assertContains(source, "expectedRevision", "stale revision guard");
        assertContains(source, "beginCampaign", "campaign reservation");
        assertContains(source, "applyPendingCampaignRefunds", "persisted campaign refund settlement");
    }

    private static void recordsKeepRuntimeCodecsOutOfThePureDomainLayer() throws IOException {
        String record = read("src/main/java/galacticwars/clonewars/kingdom/SettlementRecord.java");
        String codecs = read("src/main/java/galacticwars/clonewars/kingdom/KingdomCodecs.java");
        assertNotContains(record, "com.mojang.serialization", "pure settlement record");
        assertNotContains(record, "KingdomCodecs", "runtime codec holder in pure settlement record");
        assertContains(codecs, "Codec<SettlementRecord>", "settlement persistence codec");
        assertContains(codecs, "Codec<KingdomRecord>", "kingdom persistence codec");
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static void assertContains(String value, String expected, String label) {
        if (!value.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void assertNotContains(String value, String unexpected, String label) {
        if (value.contains(unexpected)) {
            throw new AssertionError(label + " unexpectedly contains <" + unexpected + ">");
        }
    }

    private static void schema9StarterCampDeploymentsRoundTrip() {
        KingdomSavedData data = new KingdomSavedData();
        java.util.UUID owner1 = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        java.util.UUID owner2 = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002");
        java.util.UUID builder = java.util.UUID.fromString("00000000-0000-0000-0000-0000000000bb");
        java.util.UUID project = java.util.UUID.fromString("00000000-0000-0000-0000-0000000000cc");
        KingdomRecord kingdom1 = data.foundKingdom(owner1, "galacticwars:republic",
                "minecraft:overworld", new net.minecraft.core.BlockPos(100, 64, 200));
        KingdomRecord kingdom2 = data.foundKingdom(owner2, "galacticwars:separatist",
                "minecraft:the_nether", new net.minecraft.core.BlockPos(-50, 80, -100));

        galacticwars.clonewars.settlement.StarterCampDeployment deployment1 =
            galacticwars.clonewars.settlement.StarterCampDeployment.awaiting(
                kingdom1.id(), "minecraft:overworld", 100, 64, 200, 1)
            .withSuppliesGranted()
            .withBuilder(builder)
            .building(project);
        galacticwars.clonewars.settlement.StarterCampDeployment deployment2 =
            galacticwars.clonewars.settlement.StarterCampDeployment.awaiting(
                kingdom2.id(), "minecraft:the_nether", -50, 80, -100, 2);

        if (!data.storeStarterCampDeployment(deployment1, -1)
                || !data.storeStarterCampDeployment(deployment2, -1)) {
            throw new AssertionError("schema 9 deployment insertion");
        }

        com.mojang.serialization.DataResult<com.google.gson.JsonElement> encoded =
            KingdomSavedData.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, data);
        if (encoded.error().isPresent()) {
            throw new AssertionError("schema 9 codec encode failed: " + encoded.error().get());
        }

        com.mojang.serialization.DataResult<KingdomSavedData> decoded =
            KingdomSavedData.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, encoded.result().get());
        if (decoded.error().isPresent()) {
            throw new AssertionError("schema 9 codec decode failed: " + decoded.error().get());
        }

        KingdomSavedData roundTripped = decoded.result().get();
        galacticwars.clonewars.settlement.StarterCampDeployment retrieved1 =
                roundTripped.starterCampDeployment(kingdom1.id()).orElseThrow();
        galacticwars.clonewars.settlement.StarterCampDeployment retrieved2 =
                roundTripped.starterCampDeployment(kingdom2.id()).orElseThrow();
        if (retrieved1.originX() != 100 || retrieved1.rotationSteps() != 1
                || !retrieved1.builderId().equals(java.util.Optional.of(builder))
                || !retrieved1.projectId().equals(java.util.Optional.of(project))) {
            throw new AssertionError("schema 9 deployment1 round-trip failed: " + retrieved1);
        }
        if (retrieved2.originZ() != -100 || retrieved2.rotationSteps() != 2) {
            throw new AssertionError("schema 9 deployment2 round-trip failed: " + retrieved2);
        }
        if (!roundTripped.storeStarterCampDeployment(
                retrieved1.blocked("test_obstruction"), retrieved1.revision())
                || !roundTripped.isDirty()) {
            throw new AssertionError("starter camp update did not mark decoded saved data dirty");
        }
    }

    private static void schema8MigrationPreservesData() {
        java.util.UUID owner = java.util.UUID.fromString("00000000-0000-0000-0000-000000000008");
        KingdomSavedData legacy = new KingdomSavedData();
        legacy.foundKingdom(owner, "galacticwars:republic", "minecraft:overworld",
                new net.minecraft.core.BlockPos(8, 64, 8));
        com.google.gson.JsonObject parsed = KingdomSavedData.CODEC.encodeStart(
                        com.mojang.serialization.JsonOps.INSTANCE, legacy)
                .result().orElseThrow().getAsJsonObject();
        parsed.addProperty("schema_version", 8);
        parsed.remove("starter_camp_deployments");
        com.mojang.serialization.DataResult<KingdomSavedData> result =
            KingdomSavedData.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, parsed);

        if (result.error().isPresent()) {
            throw new AssertionError("schema 8 migration failed: " + result.error().get());
        }

        KingdomSavedData migrated = result.result().get();
        if (migrated.schemaVersion() != KingdomSavedData.CURRENT_SCHEMA_VERSION) {
            throw new AssertionError("schema 8 should migrate to current schema, got: " + migrated.schemaVersion());
        }
        if (migrated.kingdomForOwner(owner).isEmpty()) {
            throw new AssertionError("schema 8 migration lost existing kingdom data");
        }
        if (!migrated.starterCampDeployments().isEmpty()) {
            throw new AssertionError("schema 8 migration should have empty deployments");
        }
    }
}
