package galacticwars.clonewars.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import galacticwars.clonewars.progression.GameplayAccessResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class RecipeClassificationManifestTest {
    private static final Path RECIPES =
            Path.of("src/main/resources/data/galacticwars/recipe");
    private static final Path TECHNOLOGY =
            Path.of("src/main/resources/data/galacticwars/galacticwars/technology");
    private static final Set<String> VANILLA = Set.of(
            "command_center",
            "credit_chip",
            "energy_cell",
            "claim_transponder",
            "command_marker",
            "republic_identity_chip",
            "separatist_identity_chip",
            "mandalorian_identity_chip",
            "hutt_cartel_identity_chip",
            "nightsister_identity_chip",
            "beskar_ore",
            "duracrete_stonecutting",
            "nightsister_weave_planks",
            "nightsister_weave_sapling",
            "jedi_meditation_shrine",
            "sith_holocron_pedestal",
            "nightsister_spirit_altar",
            "fabricator");
    private static final Set<String> FORCE_RITUALS = Set.of(
            "blue_lightsaber",
            "green_lightsaber",
            "yellow_lightsaber",
            "purple_lightsaber",
            "white_lightsaber",
            "red_lightsaber");
    private static final Map<String, Integer> FORCE_RANKS = Map.of(
            "blue_lightsaber", 1,
            "green_lightsaber", 1,
            "red_lightsaber", 1,
            "yellow_lightsaber", 3,
            "purple_lightsaber", 5,
            "white_lightsaber", 7);
    private static final Set<String> NO_RECIPE_ITEMS = Set.of(
            "blueprint_projector",
            "republic_vehicle_kit",
            "separatist_vehicle_kit",
            "mandalorian_vehicle_kit",
            "hutt_cartel_vehicle_kit",
            "nightsister_vehicle_kit",
            "mandalorian_fiber",
            "raw_beskar",
            "blaster_bolt");

    private RecipeClassificationManifestTest() {
    }

    public static void main(String[] args) throws Exception {
        classifiesEveryRecipeDefinition();
        fabricationRecipesHaveOneTechnologyOwner();
        forceRitualsHaveExactShrinesAndRanks();
        baselineRecipesDoNotDependOnResearchOutputs();
        excludedItemsRemainOnTheirRuntimeAcquisitionPaths();
        System.out.println("RecipeClassificationManifestTest passed");
    }

    private static void classifiesEveryRecipeDefinition() throws Exception {
        Map<String, JsonObject> recipes = readRecipes();
        assertEquals(91, recipes.size(), "recipe definition count");

        Set<String> vanilla = new LinkedHashSet<>();
        Set<String> fabrication = new LinkedHashSet<>();
        Set<String> rituals = new LinkedHashSet<>();
        for (var entry : recipes.entrySet()) {
            switch (entry.getValue().get("type").getAsString()) {
                case "galacticwars:fabrication" -> fabrication.add(entry.getKey());
                case "galacticwars:force_ritual" -> rituals.add(entry.getKey());
                default -> vanilla.add(entry.getKey());
            }
        }
        assertEquals(VANILLA, Set.copyOf(vanilla), "vanilla recipe manifest");
        assertEquals(FORCE_RITUALS, Set.copyOf(rituals), "Force ritual manifest");
        assertEquals(67, fabrication.size(), "fabrication recipe count");
        assertEquals(91, vanilla.size() + fabrication.size() + rituals.size(),
                "every recipe classified once");
    }

    private static void fabricationRecipesHaveOneTechnologyOwner() throws Exception {
        Map<String, JsonObject> recipes = readRecipes();
        Map<String, String> technologyOwner = new LinkedHashMap<>();
        int nodeCount = 0;
        try (Stream<Path> files = Files.list(TECHNOLOGY)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".json")).toList()) {
                JsonObject tree = readJson(path);
                String treeFaction = tree.get("faction").getAsString();
                for (JsonElement nodeElement : tree.getAsJsonArray("nodes")) {
                    nodeCount++;
                    JsonObject node = nodeElement.getAsJsonObject();
                    String nodeId = node.get("id").getAsString();
                    for (JsonElement recipeElement : node.getAsJsonArray("recipes")) {
                        String recipeId = path(recipeElement.getAsString());
                        String previous = technologyOwner.putIfAbsent(recipeId, nodeId);
                        assertTrue(previous == null,
                                recipeId + " is assigned to only one technology node");
                    }
                    assertTrue(node.getAsJsonObject("inputs").size() >= 1,
                            nodeId + " has physical research inputs");
                    assertTrue(node.get("work").getAsInt() >= 20,
                            nodeId + " has technician work");
                    assertTrue(treeFaction.equals("galacticwars:universal")
                                    || nodeId.startsWith("galacticwars:"),
                            nodeId + " uses a namespaced faction tree");
                }
            }
        }
        assertEquals(24, nodeCount, "seeded technology node count");

        Set<String> fabrication = new LinkedHashSet<>();
        Set<String> exportable = new LinkedHashSet<>();
        for (var entry : recipes.entrySet()) {
            JsonObject recipe = entry.getValue();
            if (!recipe.get("type").getAsString().equals("galacticwars:fabrication")) {
                continue;
            }
            String id = entry.getKey();
            fabrication.add(id);
            assertEquals(technologyOwner.get(id), recipe.get("technology").getAsString(),
                    id + " technology metadata");
            assertTrue(recipe.getAsJsonArray("ingredients").size() >= 1,
                    id + " has ingredients");
            assertTrue(recipe.has("faction"), id + " has faction metadata");
            if (recipe.has("exportable") && recipe.get("exportable").getAsBoolean()) {
                exportable.add("galacticwars:" + id);
            }
        }
        assertEquals(fabrication, technologyOwner.keySet(),
                "all fabrication recipes are assigned to technology");
        assertEquals(GameplayAccessResolver.EXPORTABLE_RECIPES, Set.copyOf(exportable),
                "exact foreign recipe license allowlist");
    }

    private static void forceRitualsHaveExactShrinesAndRanks() throws Exception {
        Map<String, JsonObject> recipes = readRecipes();
        for (String id : FORCE_RITUALS) {
            JsonObject recipe = recipes.get(id);
            assertEquals(FORCE_RANKS.get(id), recipe.get("required_rank").getAsInt(),
                    id + " Force rank");
            String expectedPath = id.equals("red_lightsaber") ? "sith" : "jedi";
            assertEquals(expectedPath, recipe.get("path").getAsString(), id + " Force path");
            assertEquals(expectedPath, recipe.get("shrine").getAsString(), id + " shrine");
            assertTrue(recipe.getAsJsonArray("ingredients").size() >= 1,
                    id + " has ritual ingredients");
        }
    }

    private static void baselineRecipesDoNotDependOnResearchOutputs() throws Exception {
        Map<String, JsonObject> recipes = readRecipes();
        Set<String> fabricationOutputs = new HashSet<>();
        for (JsonObject recipe : recipes.values()) {
            if (recipe.get("type").getAsString().equals("galacticwars:fabrication")) {
                fabricationOutputs.add(recipe.getAsJsonObject("result").get("id").getAsString());
            }
        }
        for (String id : VANILLA) {
            JsonObject recipe = recipes.get(id);
            Set<String> ingredients = new HashSet<>();
            collectIngredientIds(recipe.get("key"), ingredients);
            collectIngredientIds(recipe.get("ingredient"), ingredients);
            collectIngredientIds(recipe.get("ingredients"), ingredients);
            Set<String> blocked = new HashSet<>(ingredients);
            blocked.retainAll(fabricationOutputs);
            assertTrue(blocked.isEmpty(), id + " baseline recipe depends on research outputs " + blocked);
        }
    }

    private static void excludedItemsRemainOnTheirRuntimeAcquisitionPaths() throws Exception {
        Map<String, JsonObject> recipes = readRecipes();
        for (String id : NO_RECIPE_ITEMS) {
            assertTrue(!recipes.containsKey(id), id + " must not have a normal recipe");
        }
        String commandCenterMenu = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/menu/CommandCenterOperationsMenu.java"));
        assertTrue(commandCenterMenu.contains("ModItems.BLUEPRINT_PROJECTOR"),
                "Blueprint Projector is Command Center-issued");
        String vehicleService = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/vehicle/VehicleFabricationService.java"));
        assertTrue(vehicleService.contains("fabricat"),
                "vehicle kits remain VehicleFabricationService outputs");
        String brushing = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/survival/MountFiberRecoveryEvents.java"));
        assertTrue(brushing.contains("MANDALORIAN_FIBER"),
                "Mandalorian Fiber remains horse-brushing loot");
        JsonObject oreLoot = readJson(Path.of(
                "src/main/resources/data/galacticwars/loot_table/blocks/beskar_ore.json"));
        assertTrue(oreLoot.toString().contains("galacticwars:raw_beskar"),
                "Raw Beskar remains ore loot");
    }

    private static Map<String, JsonObject> readRecipes() throws IOException {
        Map<String, JsonObject> recipes = new HashMap<>();
        try (Stream<Path> files = Files.list(RECIPES)) {
            for (Path recipe : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                String name = recipe.getFileName().toString();
                recipes.put(name.substring(0, name.length() - ".json".length()), readJson(recipe));
            }
        }
        return recipes;
    }

    private static void collectIngredientIds(JsonElement element, Set<String> output) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value.contains(":")) {
                output.add(value.startsWith("#") ? value.substring(1) : value);
            }
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> collectIngredientIds(value, output));
            return;
        }
        if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet()
                    .forEach(entry -> collectIngredientIds(entry.getValue(), output));
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
