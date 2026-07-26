package galacticwars.clonewars.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class PlanetStructureWorldgenResourcesTest {
    private static final Path DATA = Path.of("src/main/resources/data/galacticwars");
    private static final Path BLUEPRINTS = DATA.resolve("galacticwars/blueprints");

    private PlanetStructureWorldgenResourcesTest() {
    }

    public static void main(String[] args) throws Exception {
        assertStructureSet("blueprint_sites", 48, 16, "galacticwars:blueprint_structure");
        assertStructureSet("commander_centers", 96, 32, "galacticwars:commander_center_structure");
        require(json(DATA.resolve("worldgen/structure/blueprint_structure.json"))
                        .get("site_kind").getAsString().equals("outpost"),
                "outpost structure kind changed");
        require(json(DATA.resolve("worldgen/structure/commander_center_structure.json"))
                        .get("site_kind").getAsString().equals("command_center"),
                "commander-center structure kind changed");
        assertStructureTag("blueprint_sites", "galacticwars:blueprint_structure");
        assertStructureTag("commander_centers", "galacticwars:commander_center_structure");

        String biomeTag = Files.readString(
                DATA.resolve("tags/worldgen/biome/has_structure/blueprint_structure.json"));
        for (String planet : Map.of(
                "tatooine", "hutt",
                "geonosis", "separatist",
                "kamino", "republic",
                "coruscant", "republic").keySet()) {
            require(biomeTag.contains("\"galacticwars:" + planet + "\""),
                    planet + " structure biome tag missing");
        }

        for (String flatPlanet : new String[]{"kamino", "coruscant"}) {
            String dimension = Files.readString(DATA.resolve("dimension/" + flatPlanet + ".json"));
            require(dimension.contains("\"galacticwars:blueprint_sites\""),
                    flatPlanet + " outpost override missing");
            require(dimension.contains("\"galacticwars:commander_centers\""),
                    flatPlanet + " commander-center override missing");
        }

        assertPlanetRoute("hutt_salvage_depot", "hutt_command_center", "galacticwars:tatooine");
        assertPlanetRoute(
                "separatist_relay_outpost", "separatist_command_center", "galacticwars:geonosis");
        assertPlanetRoute("republic_field_base", "republic_command_center", "galacticwars:kamino");
        assertPlanetRoute("republic_field_base", "republic_command_center", "galacticwars:coruscant");
        assertOverworldOnly("mandalorian_mountain_camp", "mandalorian_command_center");
        assertOverworldOnly("nightsister_enclave", "nightsister_command_center");

        String selector = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/world/BlueprintStructure.java"));
        require(selector.contains("PLANET_POI_EXCLUSION_RADIUS = 160"),
                "planet POI exclusion radius changed");
        require(selector.contains("nearProtectedPlanetPoi(biomeId, x, z)"),
                "planet POI exclusion is not wired to generation");

        System.out.println("PlanetStructureWorldgenResourcesTest passed");
    }

    private static void assertStructureSet(
            String id, int spacing, int separation, String structureId
    ) throws Exception {
        JsonObject set = json(DATA.resolve("worldgen/structure_set/" + id + ".json"));
        JsonObject placement = set.getAsJsonObject("placement");
        require(placement.get("spacing").getAsInt() == spacing, id + " spacing changed");
        require(placement.get("separation").getAsInt() == separation, id + " separation changed");
        require(set.getAsJsonArray("structures").get(0).getAsJsonObject()
                        .get("structure").getAsString().equals(structureId),
                id + " structure target changed");
    }

    private static void assertStructureTag(String id, String structureId) throws Exception {
        String tag = Files.readString(DATA.resolve("tags/worldgen/structure/" + id + ".json"));
        require(tag.contains("\"" + structureId + "\""),
                id + " runtime locate tag is missing " + structureId);
    }

    private static void assertPlanetRoute(
            String outpost, String commandCenter, String biome
    ) throws Exception {
        for (String id : new String[]{outpost, commandCenter}) {
            String biomes = json(BLUEPRINTS.resolve(id + ".json"))
                    .getAsJsonObject("worldgen").getAsJsonArray("biomes").toString();
            require(biomes.contains(biome), id + " missing " + biome);
        }
    }

    private static void assertOverworldOnly(String outpost, String commandCenter) throws Exception {
        for (String id : new String[]{outpost, commandCenter}) {
            String biomes = json(BLUEPRINTS.resolve(id + ".json"))
                    .getAsJsonObject("worldgen").getAsJsonArray("biomes").toString();
            require(!biomes.contains("galacticwars:"),
                    id + " must remain Overworld-only");
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
