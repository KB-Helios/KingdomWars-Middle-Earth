package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CommandCenterIntegrationTest {
    private CommandCenterIntegrationTest() {
    }

    public static void main(String[] args) throws IOException {
        hallIsRegisteredAsPhysicalOwnedStorage();
        hallFoundsAuthoritativeKingdomState();
        hallRequiresExplicitFactionSelection();
        hallAssetsAreComplete();
        hallOpensPlanetNavigationAfterPledge();
        hallSynchronizesTruthfulTargetedOperations();
        hallSupplyActionsUseFactionEfficiencyAtomically();
        System.out.println("CommandCenterIntegrationTest passed");
    }

    private static void hallRequiresExplicitFactionSelection() throws IOException {
        String block = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterBlock.java");
        String menu = read("src/main/java/galacticwars/clonewars/menu/FactionSelectionMenu.java");
        String pledge = read("src/main/java/galacticwars/clonewars/faction/FactionPledgeService.java");
        String menus = read("src/main/java/galacticwars/clonewars/registry/ModMenuTypes.java");
        String client = read("src/main/java/galacticwars/clonewars/GalacticWarsClient.java");
        assertContains(block, "FactionSelectionMenuProvider", "placement-time faction picker");
        assertContains(menu, "FactionPledgeService.pledge", "shared server-authoritative pledge");
        assertContains(pledge, "ProgressionEventType.FACTION_PLEDGED", "pledge progression event");
        assertContains(pledge, "activateHall", "selected faction kingdom activation");
        assertContains(pledge, "FactionAlignmentSavedData", "selected faction alignment");
        assertContains(pledge, "rollbackFreshKingdom", "kingdom compensation");
        assertContains(pledge, "restoreAfterFailedTransaction", "state compensation");
        String provider = read("src/main/java/galacticwars/clonewars/menu/FactionSelectionMenuProvider.java");
        assertContains(provider, "buffer.writeUtf(id, 128)", "bounded faction id encoding");
        String claims = read("src/main/java/galacticwars/clonewars/kingdom/KingdomSavedData.java");
        assertContains(claims, "Map<ClaimKey, KingdomClaim> claimsByChunk", "constant-time claim index");
        assertContains(menus, "FACTION_SELECTION", "faction menu registration");
        assertContains(client, "FactionSelectionScreen", "faction screen registration");
    }

    private static void hallOpensPlanetNavigationAfterPledge() throws IOException {
        String block = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterBlock.java");
        String menus = read("src/main/java/galacticwars/clonewars/registry/ModMenuTypes.java");
        assertContains(block, "CommandCenterNavigationMenuProvider", "navigation console provider");
        assertContains(block, "KingdomPermission.TRAVEL", "navigation travel permission");
        assertContains(menus, "COMMAND_CENTER_NAVIGATION", "navigation menu registration");
    }

    private static void hallSynchronizesTruthfulTargetedOperations() throws IOException {
        String provider = read("src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenuProvider.java");
        String menu = read("src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java");
        String network = read("src/main/java/galacticwars/clonewars/network/GalacticNetwork.java");
        String payload = read("src/main/java/galacticwars/clonewars/network/MenuActionPayload.java");
        String screen = read("src/main/java/galacticwars/clonewars/client/gui/CommandCenterOperationsScreen.java");
        String fabrication = read("src/main/java/galacticwars/clonewars/vehicle/VehicleFabricationService.java");
        String navigation = read("src/main/java/galacticwars/clonewars/world/PlanetTravelService.java");
        String plan = read("docs/gameplay-completion-plan.md");
        assertContains(provider, "CommandCenterDashboardCodec.write", "initial dashboard snapshot");
        assertContains(menu, "CommandCenterDashboardState.capture", "server dashboard authority");
        assertContains(menu, "CommandTargetResolver.resolve", "explicit operation target resolution");
        assertContains(menu, "ATTACK_SQUAD_TARGET", "explicit squad attack action");
        assertContains(menu, "nearbyCombatTargets", "server-revalidated nearby hostile targets");
        assertContains(network, "CommandCenterStatePayload.TYPE", "Architectury dashboard refresh payload");
        assertContains(payload, "primaryTargetId", "primary operation target payload");
        assertContains(payload, "secondaryTargetId", "secondary operation target payload");
        assertContains(screen, "construction", "construction dashboard surface");
        assertContains(screen, "workforce", "workforce dashboard surface");
        assertContains(screen, "commandCandidateIndex", "explicit commander selector");
        assertContains(screen, "combatTargetIndex", "explicit combat-target selector");
        assertContains(screen, "HOLD_SQUAD", "complete squad order controls");
        assertContains(screen, "workerIndex", "explicit live-worker selector");
        assertContains(screen, "RESUME_WORKER", "workforce resume control");
        assertContains(screen, "RECALL_WORKER", "workforce recall control");
        assertContains(screen, "PAUSE_WORKER", "workforce pause control");
        assertContains(screen, "overview.next_objective", "overview next-objective guidance");
        assertContains(screen, "objectiveInstruction", "actionable campaign objective guidance");
        assertContains(screen, "new MenuActionPayload(", "targeted operation dispatch");
        assertContains(screen, "KingdomPermissionPolicy.allows", "role-aware client controls");
        assertContains(screen, "fabrication.availability()",
                "server-authored fabrication button availability");
        assertContains(screen, "Tooltip.create", "localized disabled action feedback");
        assertContains(screen, "font.split", "bounded wrapped tab text");
        assertContains(screen, "enableScissor", "tab content scissor");
        assertContains(screen, "mouseScrolled", "per-tab content scrolling");
        assertContains(screen, "font.plainSubstrByWidth", "single-line selector truncation");
        String navigationScreen = read(
                "src/main/java/galacticwars/clonewars/client/gui/CommandCenterNavigationScreen.java");
        assertContains(navigationScreen, "font.split", "bounded navigation hints");
        assertContains(navigationScreen, "Math.min(BUTTON_WIDTH", "responsive navigation controls");
        assertContains(fabrication, "hall.canUse(player, KingdomPermission.USE_STORAGE)",
                "server-enforced fabrication permission");
        assertContains(fabrication, "hall.upkeepPaid()",
                "server-enforced fabrication upkeep");
        assertContains(fabrication, "MaterialRequirement",
                "exact fabrication stock preflight");
        assertContains(navigation, "List<NavigationDestination> navigationOptions",
                "per-destination server navigation availability");
        assertContains(plan, "fresh survival player", "player-facing completion criterion");
        assertContains(plan, "Green catalogs", "runtime proof boundary");
    }

    private static void hallSupplyActionsUseFactionEfficiencyAtomically() throws IOException {
        String menu = read("src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java");
        String policy = read("src/main/java/galacticwars/clonewars/army/ArmySupplyPolicy.java");
        assertContains(menu, "FactionBalanceService.resolve(kingdom.factionId()).supplyEfficiencyPercent()",
                "authoritative kingdom supply efficiency");
        assertContains(menu, "ArmySupplyPolicy.unitsPerEnergyCell", "data-driven Energy Cell yield");
        assertBefore(menu, "data.changeArmySupply(", "consumeSupplyCell(hall, supplyCellSlot)",
                "SavedData acceptance before Energy Cell consumption");
        assertContains(policy, "BASE_UNITS_PER_ENERGY_CELL = 16", "base Energy Cell yield");
        assertContains(policy, "applyPercentFloor", "floor-rounded faction supply yield");
    }

    private static void hallIsRegisteredAsPhysicalOwnedStorage() throws IOException {
        String blocks = read("src/main/java/galacticwars/clonewars/registry/ModBlocks.java");
        String items = read("src/main/java/galacticwars/clonewars/registry/ModItems.java");
        String blockEntities = read("src/main/java/galacticwars/clonewars/registry/ModBlockEntityTypes.java");
        String hall = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterBlockEntity.java");
        String credits = read("src/main/java/galacticwars/clonewars/economy/CreditTransactionService.java");
        assertContains(blocks, "COMMAND_CENTER", "hall block registration");
        assertContains(items, "COMMAND_CENTER", "hall item registration");
        assertContains(blockEntities, "COMMAND_CENTER", "hall block entity registration");
        assertContains(hall, "CONTAINER_SIZE = 54", "physical shared inventory");
        assertContains(hall, "ownerId", "persisted owner");
        assertContains(hall, "reserveCredits", "atomic treasury reservation");
        assertContains(hall, "refundCredits", "campaign refund");
        assertContains(hall, "chargeDailyUpkeep", "daily upkeep");
        assertContains(hall, "UpkeepClockInitialized", "new Hall upkeep grace clock");
        assertContains(hall, "settlePendingCampaignRefunds", "persisted commander campaign refunds");
        assertContains(hall, "CreditTransactionService.withdrawContainer", "single treasury transaction authority");
        assertContains(hall, "CreditTransactionService.depositContainer", "single treasury refund authority");
        assertContains(credits, "container.setItem(slot, ItemStack.EMPTY)", "depleted-slot normalization");
        assertContains(credits, "containerBalance(container) < amount", "atomic withdrawal preflight");
        assertContains(hall, "getUpdatePacket", "block entity client update packet");
        assertContains(hall, "getUpdateTag", "block entity client update tag");
        assertContains(hall, "sendBlockUpdated", "custom state synchronization");
    }

    private static void hallFoundsAuthoritativeKingdomState() throws IOException {
        String block = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterBlock.java");
        String events = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterEvents.java");
        String runtime = read("src/main/kotlin/galacticwars/clonewars/runtime/GalacticRuntimeEvents.kt");
        String lifecycle = read("src/main/java/galacticwars/clonewars/settlement/CommandCenterLifecycleService.java");
        assertContains(block, "KingdomSavedData.get(serverLevel)", "overworld saved data access");
        assertContains(block, "activateHall", "kingdom founding and relocation");
        assertContains(block, "FactionSelectionMenuProvider", "explicit faction selection");
        assertContains(block, "CommandCenterOperationsMenuProvider", "operations hub menu");
        assertContains(block, "BlockStateProperties.HORIZONTAL_FACING", "directional console state");
        assertNotContains(block, "claimCreditRewards", "silent reward claim on open");
        String operations = read("src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java");
        assertContains(operations, "serverPlayer.openMenu(hall)", "storage tab treasury menu");
        assertContains(block, "onExplosionHit", "explosion-resistant owner removal path");
        assertContains(block, "playerWillDestroy", "pre-removal lifecycle transaction");
        assertContains(block, "dropHallContents", "exactly-once inventory drops");
        assertContains(events, "allowBlockBreak", "loader-neutral owner-authorized block breaking");
        assertContains(events, "return false", "non-owner break rejection");
        assertContains(runtime, "BlockEvent.BREAK.register", "Architectury block-break bridge");
        assertContains(runtime, "EventResult.interruptFalse()", "cross-loader break cancellation");
        assertContains(lifecycle, "cancelActiveCampaigns", "campaign cancellation before removal");
        assertContains(lifecycle, "deactivateHall", "authoritative Hall deactivation");
    }

    private static void hallAssetsAreComplete() {
        assertFile("src/main/resources/assets/galacticwars/blockstates/command_center.json");
        assertFile("src/main/resources/assets/galacticwars/items/command_center.json");
        assertFile("src/main/resources/assets/galacticwars/models/block/command_center.json");
        assertFile("src/main/resources/assets/galacticwars/models/item/command_center.json");
        assertFile("src/main/resources/data/galacticwars/loot_table/blocks/command_center.json");
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static void assertFile(String path) {
        if (!Files.isRegularFile(Path.of(path))) {
            throw new AssertionError("missing file " + path);
        }
    }

    private static void assertContains(String value, String expected, String label) {
        if (!value.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void assertNotContains(String value, String unexpected, String label) {
        if (value.contains(unexpected)) {
            throw new AssertionError(label + " unexpectedly contained <" + unexpected + ">");
        }
    }

    private static void assertBefore(String value, String first, String second, String label) {
        int firstIndex = value.indexOf(first);
        int secondIndex = value.indexOf(second);
        if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
            throw new AssertionError(label + " expected <" + first + "> before <" + second + ">");
        }
    }
}
