package galacticwars.clonewars.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class SurvivalAcquisitionIntegrityTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final Path RECIPES = ROOT.resolve("data/galacticwars/recipe");
    private static final Path ADVANCEMENTS = ROOT.resolve("data/galacticwars/advancement");
    private static final Path LANG = ROOT.resolve("assets/galacticwars/lang/en_us.json");
    private static final Path CREATIVE_TAB = Path.of(
            "src/main/java/galacticwars/clonewars/registry/ModCreativeTabs.java");

    private static final Map<String, String> ADVERTISED_PLAYER_ITEMS = advertisedPlayerItems();
    private static final Set<String> MANDATORY_PROGRESSION_BLOCKS = Set.of("command_center");
    private static final Set<String> ONBOARDING_RECIPE_REWARDS = Set.of(
            "command_center", "fabricator", "energy_cell", "claim_transponder");

    private SurvivalAcquisitionIntegrityTest() {
    }

    public static void main(String[] args) throws Exception {
        everyAdvertisedPlayerItemHasATransitiveSurvivalPath();
        mandatoryCommandCenterRecipeUsesReachableNeutralMaterials();
        onboardingAdvancementsAreLiveLocalizedAndUnlockTheRecipeBook();
        System.out.println("SurvivalAcquisitionIntegrityTest passed");
    }

    private static void everyAdvertisedPlayerItemHasATransitiveSurvivalPath() throws Exception {
        String creativeTab = Files.readString(CREATIVE_TAB);
        Map<String, List<Set<String>>> alternatives = recipeAlternatives();
        addTradeAlternatives(alternatives);

        Set<String> reachable = new HashSet<>();
        addRuntimeAcquisitions(reachable);
        addLootAlternatives(alternatives);

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<Set<String>>> entry : alternatives.entrySet()) {
                if (reachable.contains(entry.getKey())) {
                    continue;
                }
                if (entry.getValue().stream().anyMatch(reachable::containsAll)) {
                    changed |= reachable.add(entry.getKey());
                }
            }
        } while (changed);

        for (Map.Entry<String, String> entry : ADVERTISED_PLAYER_ITEMS.entrySet()) {
            String id = "galacticwars:" + entry.getKey();
            assertContains(creativeTab,
                    "output.accept(ModItems." + entry.getValue() + ".get())",
                    entry.getKey() + " is advertised in the player content tab");
            assertTrue(reachable.contains(id), id + " has no transitive survival acquisition path");
        }
        for (String block : MANDATORY_PROGRESSION_BLOCKS) {
            assertTrue(reachable.contains("galacticwars:" + block),
                    block + " mandatory progression block has no survival acquisition path");
        }
    }

    private static void mandatoryCommandCenterRecipeUsesReachableNeutralMaterials() throws Exception {
        JsonObject commandCenter = readJson(RECIPES.resolve("command_center.json"));
        Set<String> ingredients = ingredientIds(commandCenter);
        assertEquals(Set.of(
                "minecraft:copper_block",
                "minecraft:iron_ingot",
                "minecraft:redstone",
                "minecraft:stone_bricks",
                "galacticwars:credit_chip"), ingredients, "Command Center ingredients");
        assertTrue(ingredients.stream().noneMatch(id -> id.contains("nightsister") || id.contains("weave")),
                "The faction-neutral Command Center cannot require Nightsister materials");

        JsonObject creditChip = readJson(RECIPES.resolve("credit_chip.json"));
        assertTrue(ingredientIds(creditChip).stream().allMatch(id -> id.startsWith("minecraft:")),
                "Credit Chip must be craftable entirely from vanilla materials");
        assertEquals(8, creditChip.getAsJsonObject("result").get("count").getAsInt(),
                "Credit Chip starter batch size");
    }

    private static void onboardingAdvancementsAreLiveLocalizedAndUnlockTheRecipeBook() throws Exception {
        List<String> chain = List.of("credit_chip", "command_center", "faction_pledged", "first_recruit");
        List<String> parents = List.of(
                "", "galacticwars:onboarding/credit_chip", "galacticwars:onboarding/command_center",
                "galacticwars:onboarding/faction_pledged");
        JsonObject language = readJson(LANG);
        Set<String> rewardedRecipes = new LinkedHashSet<>();

        for (int index = 0; index < chain.size(); index++) {
            String id = chain.get(index);
            JsonObject advancement = readJson(ADVANCEMENTS.resolve("onboarding/" + id + ".json"));
            String parent = advancement.has("parent") ? advancement.get("parent").getAsString() : "";
            assertEquals(parents.get(index), parent, id + " onboarding parent");

            JsonObject display = advancement.getAsJsonObject("display");
            assertTrue(display.get("show_toast").getAsBoolean(), id + " onboarding toast");
            assertTrue(!display.get("announce_to_chat").getAsBoolean(), id + " avoids chat spam");
            for (String field : List.of("title", "description")) {
                String key = display.getAsJsonObject(field).get("translate").getAsString();
                assertTrue(language.has(key) && !language.get(key).getAsString().isBlank(),
                        id + " localized " + field);
            }
            if (advancement.has("rewards")) {
                JsonArray recipes = advancement.getAsJsonObject("rewards").getAsJsonArray("recipes");
                recipes.forEach(recipe -> rewardedRecipes.add(path(recipe.getAsString())));
            }
        }
        assertTrue(rewardedRecipes.containsAll(ONBOARDING_RECIPE_REWARDS),
                "Onboarding milestones must unlock only the baseline progression recipes");

        JsonObject creditRecipeUnlock = readJson(ADVANCEMENTS.resolve("recipes/credit_chip.json"));
        assertTrue(creditRecipeUnlock.toString().contains("minecraft:copper_ingot")
                        && creditRecipeUnlock.toString().contains("minecraft:redstone"),
                "Credit Chip recipe discovery starts from normal mining materials");
        assertTrue(rewardRecipes(creditRecipeUnlock).contains("credit_chip"),
                "Credit Chip recipe advancement grants the recipe");

        String advancementService = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/progression/OnboardingAdvancementService.java"));
        assertContains(advancementService, "onboarding/faction_pledged",
                "authoritative faction advancement id");
        assertContains(advancementService, "ProgressionEventType.RECRUIT_HIRED",
                "authoritative recruit milestone");
        String attachmentRuntime = Files.readString(Path.of(
                "src/main/kotlin/galacticwars/clonewars/progression/PlayerCampaignAttachmentRuntime.kt"));
        assertContains(attachmentRuntime, "OnboardingAdvancementService.synchronize",
                "server lifecycle invokes onboarding synchronization");
    }

    private static Map<String, List<Set<String>>> recipeAlternatives() throws IOException {
        Map<String, List<Set<String>>> alternatives = new HashMap<>();
        try (Stream<Path> files = Files.list(RECIPES)) {
            for (Path recipe : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject json = readJson(recipe);
                JsonObject result = json.getAsJsonObject("result");
                assertTrue(result != null && result.has("id"), recipe + " recipe result");
                String resultId = result.get("id").getAsString();
                alternatives.computeIfAbsent(resultId, ignored -> new ArrayList<>())
                        .add(modDependencies(ingredientIds(json)));
            }
        }
        return alternatives;
    }

    private static void addTradeAlternatives(Map<String, List<Set<String>>> alternatives) throws Exception {
        JsonArray trades = readJson(ROOT.resolve("data/galacticwars/galacticwars/trades/launch.json"))
                .getAsJsonArray("trades");
        for (JsonElement element : trades) {
            String item = element.getAsJsonObject().get("item").getAsString();
            alternatives.computeIfAbsent(item, ignored -> new ArrayList<>())
                    .add(Set.of("galacticwars:credit_chip"));
        }
    }

    private static void addRuntimeAcquisitions(Set<String> reachable) throws Exception {
        String operations = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java"));
        assertContains(operations, "new ItemStack(ModItems.BLUEPRINT_PROJECTOR.get())",
                "Command Center prepares a physical Blueprint Projector");
        reachable.add("galacticwars:blueprint_projector");

        String recovery = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/survival/MountFiberRecoveryEvents.java"));
        String runtime = Files.readString(Path.of(
                "src/main/kotlin/galacticwars/clonewars/runtime/GalacticRuntimeEvents.kt"));
        assertContains(recovery, "new ItemStack(ModItems.MANDALORIAN_FIBER.get())",
                "horse brushing yields Mandalorian Fiber");
        assertContains(runtime, "MountFiberRecoveryEvents.onHorseBrushed",
                "horse brushing acquisition is registered");
        reachable.add("galacticwars:mandalorian_fiber");
    }

    private static void addLootAlternatives(Map<String, List<Set<String>>> alternatives) throws Exception {
        JsonObject beskarLoot = readJson(ROOT.resolve("data/galacticwars/loot_table/blocks/beskar_ore.json"));
        assertTrue(beskarLoot.toString().contains("galacticwars:raw_beskar"),
                "Beskar ore loot yields Raw Beskar");
        alternatives.computeIfAbsent("galacticwars:raw_beskar", ignored -> new ArrayList<>())
                .add(Set.of("galacticwars:beskar_ore"));
    }

    private static Set<String> ingredientIds(JsonObject recipe) {
        Set<String> ingredients = new LinkedHashSet<>();
        if (recipe.has("key")) collectIds(recipe.get("key"), ingredients);
        if (recipe.has("ingredient")) collectIds(recipe.get("ingredient"), ingredients);
        if (recipe.has("ingredients")) collectIds(recipe.get("ingredients"), ingredients);
        return Set.copyOf(ingredients);
    }

    private static void collectIds(JsonElement element, Set<String> output) {
        if (element == null || element.isJsonNull()) return;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value.contains(":")) output.add(value.startsWith("#") ? value.substring(1) : value);
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> collectIds(value, output));
            return;
        }
        if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> collectIds(entry.getValue(), output));
        }
    }

    private static Set<String> modDependencies(Set<String> ingredients) {
        return ingredients.stream().filter(id -> id.startsWith("galacticwars:")).collect(
                java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static Set<String> rewardRecipes(JsonObject advancement) {
        Set<String> recipes = new HashSet<>();
        advancement.getAsJsonObject("rewards").getAsJsonArray("recipes")
                .forEach(recipe -> recipes.add(path(recipe.getAsString())));
        return Set.copyOf(recipes);
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Map<String, String> advertisedPlayerItems() {
        LinkedHashMap<String, String> items = new LinkedHashMap<>();
        items.put("credit_chip", "CREDIT_CHIP");
        items.put("energy_cell", "ENERGY_CELL");
        items.put("command_center", "COMMAND_CENTER");
        items.put("hyperspace_navigator", "HYPERSPACE_NAVIGATOR");
        items.put("claim_transponder", "CLAIM_TRANSPONDER");
        items.put("blueprint_projector", "BLUEPRINT_PROJECTOR");
        items.put("command_marker", "COMMAND_MARKER");
        items.put("dc15_blaster", "DC15_BLASTER");
        items.put("e5_blaster", "E5_BLASTER");
        items.put("westar_blaster", "WESTAR_BLASTER");
        items.put("scatter_blaster", "SCATTER_BLASTER");
        items.put("nightsister_bow", "NIGHTSISTER_BOW");
        items.put("blue_lightsaber", "BLUE_LIGHTSABER");
        items.put("green_lightsaber", "GREEN_LIGHTSABER");
        items.put("red_lightsaber", "RED_LIGHTSABER");
        items.put("purple_lightsaber", "PURPLE_LIGHTSABER");
        items.put("yellow_lightsaber", "YELLOW_LIGHTSABER");
        items.put("white_lightsaber", "WHITE_LIGHTSABER");
        items.put("vibroblade", "VIBROBLADE");
        items.put("power_drill", "POWER_DRILL");
        items.put("plasma_cutter", "PLASMA_CUTTER");
        items.put("sonic_excavator", "SONIC_EXCAVATOR");
        items.put("hydrospanner", "HYDROSPANNER");
        return Map.copyOf(items);
    }

    private static void assertContains(String value, String expected, String label) {
        assertTrue(value.contains(expected), label);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
