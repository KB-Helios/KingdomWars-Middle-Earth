package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecruitWorkerAiIntegrationTest {
    private RecruitWorkerAiIntegrationTest() {
    }

    public static void main(String[] args) throws IOException {
        recruitRegistersWorkerBehaviour();
        recruitPersistsWorkerStateMachine();
        recruitUsesRealLootAndInventoryTransactions();
        recruitRequiresReachableClaimedTargets();
        recruitImplementsEnabledProfessionHandlers();
        builderNormalizesProgressionSubject();
        workerBehaviourDelegatesToRecruitCycle();
        naturalCivilianShelterUsesOnePredicate();

        System.out.println("RecruitWorkerAiIntegrationTest passed");
    }

    private static void recruitRegistersWorkerBehaviour() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");
        String brain = read("src/main/java/galacticwars/clonewars/entity/ai/RecruitBrain.java");

        assertContains(entity, "SmartBrainOwner<GalacticRecruitEntity>", "SmartBrain owner contract");
        assertContains(brain, "new RecruitWorkerBehaviour()", "worker behaviour registration");
    }

    private static void recruitPersistsWorkerStateMachine() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");

        assertContains(entity, "workerInventory", "real worker inventory field");
        assertContains(entity, "WorkerPhase workerPhase", "worker phase field");
        assertContains(entity, "activeWorkTarget", "active target field");
        assertContains(entity, "starterBaseCompletedBlocks", "base completed block count field");
        assertContains(entity, "\"WorkerInventory\"", "worker inventory save key");
        assertContains(entity, "\"WorkerPhase\"", "worker phase save key");
        assertContains(entity, "\"StarterBaseCompletedBlocks\"", "base progress save key");
    }

    private static void builderNormalizesProgressionSubject() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");
        assertContains(entity, "KingdomBaseBlueprint.path(blueprint.id())",
                "canonical builder progression subject");
    }

    private static void recruitUsesRealLootAndInventoryTransactions() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");

        assertContains(entity, "Block.getDrops", "loot table evaluation");
        assertContains(entity, "destroyBlock(target, false", "single non-dropping world removal");
        assertContains(entity, "mergeAll", "transactional inventory simulation");
        assertContains(entity, "insertWorkerInventory", "real container deposit");
        assertContains(entity, "withdrawSpecificItem", "real container withdrawal");
        assertContains(entity, "tool.hurtAndBreak", "tool durability");
        assertNotContains(entity, "this.carriedResources = this.carriedResources.withAdded(buildDecision.itemId(), 1)",
                "conjured builder supplies");
    }

    private static void recruitRequiresReachableClaimedTargets() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");

        assertContains(entity, "tickWorkerNavigation", "worker-owned navigation");
        assertContains(entity, "target_unreachable", "path failure reason");
        assertContains(entity, "blacklistedWorkTarget", "unreachable target blacklist");
        assertContains(entity, "interaction_out_of_reach", "interaction reach guard");
        assertContains(entity, "canModifyWorkerTarget", "claim validation");
        assertContains(entity, "scanBudget = Math.min(128", "bounded scan budget");
        assertContains(entity, "Math.floorMod(this.getUUID().hashCode(), 4)", "staggered worker scans");
        assertContains(entity, "this.level().isLoaded", "loaded chunk guard");
    }

    private static void recruitImplementsEnabledProfessionHandlers() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");

        assertContains(entity, "CropBlock", "farmer crop handling");
        assertContains(entity, "Items.WHEAT_SEEDS", "farmer seed consumption");
        assertContains(entity, "matchingSaplingForLog", "species-preserving lumberjack replanting");
        assertContains(entity, "\"mangrove_propagule\"", "non-sapling tree propagation");
        assertContains(entity, "ModBlockTags.WORKER_MINEABLE", "miner allowlist");
        assertContains(entity, "acquireCourierOrder", "courier route handler");
        assertContains(entity, "placeCurrentBuildBlock", "builder placement handler");
    }

    private static void workerBehaviourDelegatesToRecruitCycle() throws IOException {
        String behaviour = read(
                "src/main/java/galacticwars/clonewars/entity/ai/RecruitWorkerBehaviour.java");

        assertContains(behaviour, "extends ExtendedBehaviour<GalacticRecruitEntity>",
                "worker SmartBrainLib behaviour class");
        assertContains(behaviour, "shouldRunWorkerCycle", "worker cycle guard");
        assertContains(behaviour, "tickWorkerController", "worker controller tick");
        assertContains(behaviour, "pauseWorkerNavigation", "worker navigation cleanup");
    }

    private static void naturalCivilianShelterUsesOnePredicate() throws IOException {
        String behaviour = read(
                "src/main/java/galacticwars/clonewars/entity/ai/CivilianShelterBehaviour.java");

        assertContains(behaviour, "return shouldShelter(civilian);",
                "shared shelter start and continuation predicate");
        assertContains(behaviour, "civilian.isNaturalFactionCivilian()",
                "canonical natural-civilian predicate");
        assertContains(behaviour,
                "!civilian.blockPosition().closerThan(civilian.getHomePosition(), 3.0D)",
                "at-home shelter start guard");
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static void assertContains(String haystack, String needle, String label) {
        if (!haystack.contains(needle)) {
            throw new AssertionError(label + " missing <" + needle + ">");
        }
    }

    private static void assertNotContains(String haystack, String needle, String label) {
        if (haystack.contains(needle)) {
            throw new AssertionError(label + " unexpectedly contains <" + needle + ">");
        }
    }
}
