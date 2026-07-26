package galacticwars.clonewars.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class FactionStructureLootTablesTest {
    private static final Path ROOT = Path.of(
            "src/main/resources/data/galacticwars/loot_table/chests/faction_site");
    private static final Map<String, String> WEAPONS = Map.of(
            "republic", "galacticwars:dc15_blaster",
            "separatist", "galacticwars:e5_blaster",
            "mandalorian", "galacticwars:westar_blaster",
            "hutt_cartel", "galacticwars:scatter_blaster",
            "nightsister", "galacticwars:nightsister_bow");
    private static final List<String> RESTRICTED = List.of(
            "identity_chip", "command_center", "hyperspace_navigator", "deployment_kit",
            "lightsaber", "meditation_shrine", "holocron_pedestal", "spirit_altar", "beskar_");

    private FactionStructureLootTablesTest() {
    }

    public static void main(String[] args) throws Exception {
        for (Map.Entry<String, String> faction : WEAPONS.entrySet()) {
            Path directory = ROOT.resolve(faction.getKey());
            JsonObject supplies = json(directory.resolve("supplies.json"));
            JsonObject primary = json(directory.resolve("primary.json"));
            JsonObject commandCache = json(directory.resolve("command_cache.json"));

            require(pool(supplies, "credits").get("rolls").getAsInt() == 1,
                    faction.getKey() + " supplies must guarantee Credits");
            JsonObject creditCount = firstItem(pool(supplies, "credits"))
                    .getAsJsonArray("functions").get(0).getAsJsonObject()
                    .getAsJsonObject("count");
            require(creditCount.get("min").getAsInt() == 2
                            && creditCount.get("max").getAsInt() == 8,
                    faction.getKey() + " Credit count changed");
            require(pool(supplies, "components").get("rolls").getAsInt() == 2,
                    faction.getKey() + " component rolls changed");

            assertChance(primary, "gear_20_percent", 20);
            assertChance(primary, "weapon_5_percent", 5);
            require(itemNames(pool(primary, "weapon_5_percent"))
                            .contains(faction.getValue()),
                    faction.getKey() + " signature weapon changed");

            require(pool(commandCache, "cache_rolls").get("rolls").getAsInt() == 3,
                    faction.getKey() + " command-cache rolls changed");
            assertChance(commandCache, "gear_35_percent", 35);
            assertChance(commandCache, "weapon_12_percent", 12);
            require(itemNames(pool(commandCache, "weapon_12_percent"))
                            .contains(faction.getValue()),
                    faction.getKey() + " command-cache weapon changed");

            for (Path table : List.of(
                    directory.resolve("supplies.json"),
                    directory.resolve("primary.json"),
                    directory.resolve("command_cache.json"))) {
                String content = Files.readString(table);
                for (String restricted : RESTRICTED) {
                    require(!content.contains(restricted),
                            table + " contains progression-sensitive loot " + restricted);
                }
                for (String item : itemNames(JsonParser.parseString(content).getAsJsonObject())) {
                    if (item.startsWith("galacticwars:")) {
                        String path = item.substring(item.indexOf(':') + 1);
                        require(Files.isRegularFile(Path.of(
                                        "src/main/resources/assets/galacticwars/items/" + path + ".json")),
                                table + " references item without registered client definition " + item);
                    }
                }
            }
        }
        System.out.println("FactionStructureLootTablesTest passed");
    }

    private static void assertChance(JsonObject table, String poolName, int expectedItemWeight) {
        JsonObject pool = pool(table, poolName);
        int itemWeight = 0;
        int totalWeight = 0;
        for (var element : pool.getAsJsonArray("entries")) {
            JsonObject entry = element.getAsJsonObject();
            int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
            totalWeight += weight;
            if (entry.get("type").getAsString().equals("minecraft:item")) {
                itemWeight += weight;
            }
        }
        require(totalWeight == 100 && itemWeight == expectedItemWeight,
                poolName + " expected " + expectedItemWeight + "/100 but was "
                        + itemWeight + "/" + totalWeight);
    }

    private static JsonObject pool(JsonObject table, String name) {
        for (var element : table.getAsJsonArray("pools")) {
            JsonObject pool = element.getAsJsonObject();
            if (pool.get("name").getAsString().equals(name)) {
                return pool;
            }
        }
        throw new AssertionError("Missing loot pool " + name);
    }

    private static JsonObject firstItem(JsonObject pool) {
        return pool.getAsJsonArray("entries").get(0).getAsJsonObject();
    }

    private static java.util.Set<String> itemNames(JsonObject object) {
        java.util.LinkedHashSet<String> items = new java.util.LinkedHashSet<>();
        collectItems(object, items);
        return java.util.Set.copyOf(items);
    }

    private static void collectItems(com.google.gson.JsonElement element, java.util.Set<String> items) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("minecraft:item")
                    && object.has("name")) {
                items.add(object.get("name").getAsString());
            }
            object.entrySet().forEach(entry -> collectItems(entry.getValue(), items));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> collectItems(child, items));
        }
    }

    private static JsonObject json(Path path) throws Exception {
        require(Files.isRegularFile(path), "Missing loot table " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
