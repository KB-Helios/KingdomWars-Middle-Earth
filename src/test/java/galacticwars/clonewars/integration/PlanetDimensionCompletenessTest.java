package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlanetDimensionCompletenessTest {
    private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
    private static final Path MOD_ASSETS = RESOURCE_ROOT.resolve("assets/galacticwars");
    private static final Path MOD_DATA = RESOURCE_ROOT.resolve("data/galacticwars");
    private static final Map<String, PlanetContract> PLANETS = planets();

    private PlanetDimensionCompletenessTest() {
    }

    public static void main(String[] args) throws IOException {
        String language = Files.readString(MOD_ASSETS.resolve("lang/en_us.json"));
        String pickaxeTag = Files.readString(
                RESOURCE_ROOT.resolve("data/minecraft/tags/block/mineable/pickaxe.json"));
        String shovelTag = Files.readString(
                RESOURCE_ROOT.resolve("data/minecraft/tags/block/mineable/shovel.json"));
        String planetDefinitions = read(MOD_DATA.resolve("galacticwars/planets/launch.json"));

        for (Map.Entry<String, PlanetContract> entry : PLANETS.entrySet()) {
            String id = entry.getKey();
            PlanetContract contract = entry.getValue();
            String dimension = read(MOD_DATA.resolve("dimension/" + id + ".json"));
            String dimensionType = read(MOD_DATA.resolve("dimension_type/" + id + ".json"));

            assertContains(dimension, "\"type\": \"galacticwars:" + id + "\"",
                    id + " dimension type");
            assertContains(dimension, "\"type\": \"minecraft:"
                            + (contract.noiseTerrain() ? "noise" : "flat") + "\"",
                    id + " intentional planet generator");
            assertContains(dimension, "\"biome\": \"" + contract.biome() + "\"",
                    id + " biome");
            if (contract.noiseTerrain()) {
                assertContains(dimension, "\"settings\": \"minecraft:overworld\"",
                        id + " noise settings");
            } else {
                assertContains(dimension, "\"block\": \"galacticwars:"
                                + contract.surfaceBlock() + "\"",
                        id + " authored surface material");
                assertContains(dimension, "\"galacticwars:blueprint_sites\"",
                        id + " outpost structure override");
                assertContains(dimension, "\"galacticwars:commander_centers\"",
                        id + " commander-center structure override");
            }
            assertContains(planetDefinitions, "\"id\":\"" + id + "\"", id + " definition");
            assertContains(dimensionType, "\"minecraft:visual/fog_color\"",
                    id + " fog treatment");
            assertContains(dimensionType, "\"minecraft:visual/sky_color\"",
                    id + " sky treatment");
            assertContains(dimensionType, "\"has_skylight\": true",
                    id + " skylight contract");

            String block = contract.surfaceBlock();
            requireFile(MOD_ASSETS.resolve("blockstates/" + block + ".json"));
            requireFile(MOD_ASSETS.resolve("models/block/" + block + ".json"));
            requireFile(MOD_ASSETS.resolve("models/item/" + block + ".json"));
            requireFile(MOD_ASSETS.resolve("items/" + block + ".json"));
            requireFile(MOD_ASSETS.resolve("textures/block/" + block + ".png"));
            requireFile(MOD_DATA.resolve("loot_table/blocks/" + block + ".json"));
            assertContains(language, "\"block.galacticwars." + block + "\"",
                    block + " localization");
            String toolTag = contract.shovelMineable() ? shovelTag : pickaxeTag;
            assertContains(toolTag, "\"galacticwars:" + block + "\"",
                    block + " mining tool tag");
        }

        long distinctFogColors = PLANETS.keySet().stream().map(id -> {
            try {
                return attributeValue(read(MOD_DATA.resolve("dimension_type/" + id + ".json")),
                        "minecraft:visual/fog_color");
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }).distinct().count();
        if (distinctFogColors != PLANETS.size()) {
            throw new AssertionError("Every launch planet must have a distinct fog treatment");
        }

        System.out.println("PlanetDimensionCompletenessTest passed");
    }

    private static Map<String, PlanetContract> planets() {
        LinkedHashMap<String, PlanetContract> planets = new LinkedHashMap<>();
        planets.put("tatooine", new PlanetContract("galacticwars:tatooine", "tatooine_sand", true, true));
        planets.put("geonosis", new PlanetContract("galacticwars:geonosis", "geonosis_rock", false, true));
        planets.put("kamino", new PlanetContract("galacticwars:kamino", "kamino_panel", false, false));
        planets.put("coruscant", new PlanetContract("galacticwars:coruscant", "coruscant_panel", false, false));
        return Map.copyOf(planets);
    }

    private static String read(Path path) throws IOException {
        requireFile(path);
        return Files.readString(path);
    }

    private static String attributeValue(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyOffset = json.indexOf(marker);
        if (keyOffset < 0) {
            throw new AssertionError("Missing dimension attribute " + key);
        }
        int valueStart = json.indexOf('"', keyOffset + marker.length());
        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueStart < 0 || valueEnd < 0) {
            throw new AssertionError("Malformed dimension attribute " + key);
        }
        return json.substring(valueStart + 1, valueEnd);
    }

    private static void assertContains(String content, String expected, String label) {
        if (!content.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void requireFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new AssertionError("Required planet resource missing <" + path + ">");
        }
    }

    private record PlanetContract(
            String biome, String surfaceBlock, boolean shovelMineable, boolean noiseTerrain
    ) {
    }
}
