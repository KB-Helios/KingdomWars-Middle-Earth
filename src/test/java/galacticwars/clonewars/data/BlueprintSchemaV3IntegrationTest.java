package galacticwars.clonewars.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import galacticwars.clonewars.settlement.BlueprintRosterEntry;
import galacticwars.clonewars.settlement.BlueprintWorldgenProfile;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.world.BlueprintSiteKind;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;

public final class BlueprintSchemaV3IntegrationTest {
    private static final Path BLUEPRINTS = Path.of(
            "src/main/resources/data/galacticwars/galacticwars/blueprints");
    private static final Path STRUCTURES = Path.of(
            "src/main/resources/data/galacticwars/structure");
    private static final Set<String> CONSTRUCTION = Set.of(
            "barracks", "forward_base", "mine", "moisture_farm", "salvage_yard", "starter_camp", "supply_depot");
    private static final Set<String> OUTPOSTS = Set.of(
            "republic_field_base", "separatist_relay_outpost", "mandalorian_mountain_camp",
            "hutt_salvage_depot", "nightsister_enclave");
    private static final Set<String> COMMAND_CENTERS = Set.of(
            "republic_command_center", "separatist_command_center", "mandalorian_command_center",
            "hutt_command_center", "nightsister_command_center");
    private static final Set<String> SITES = java.util.stream.Stream.concat(
            OUTPOSTS.stream(), COMMAND_CENTERS.stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final Map<String, Map<String, Integer>> COSTS = Map.of(
            "barracks", Map.of("minecraft:oak_planks", 11, "minecraft:oak_log", 8),
            "forward_base", Map.of("galacticwars:duracrete", 25, "galacticwars:nightsister_weave_log", 4,
                    "minecraft:oak_planks", 4),
            "mine", Map.of("galacticwars:duracrete", 9),
            "moisture_farm", Map.of("minecraft:dirt", 25, "minecraft:oak_log", 10),
            "salvage_yard", Map.of("minecraft:oak_planks", 9, "minecraft:oak_log", 3),
            "starter_camp", Map.of("minecraft:oak_log", 8, "minecraft:oak_planks", 5,
                    "minecraft:campfire", 1, "minecraft:crafting_table", 1, "minecraft:chest", 1),
            "supply_depot", Map.of("galacticwars:duracrete", 12, "minecraft:oak_log", 8,
                    "minecraft:oak_planks", 2, "minecraft:chest", 2));
    private static final Map<String, String> PRE_V3_CONTENT_HASHES = Map.of(
            "republic_field_base", "cde6a6bcbc39e1893b4ed042b67ed8586d33d377cce01d3f958414c95b504a88",
            "separatist_relay_outpost", "54758be4efb5d0fd871c161884bc7a45c99a05d2be4fc261b43cd66da3a1105c",
            "mandalorian_mountain_camp", "a40ee5367d294bd94430d694bfd94bc6a3ad0b63ec126b86a5d318168c6f2df8",
            "hutt_salvage_depot", "c0bcfbc3a785dcb038746273c17e4542dcdeb04ce762af4cae5f4ed9a89048a7",
            "nightsister_enclave", "07110718487ccf524296dfa14e48b59d3f38df24dab91254228ab4acc74efb6f");

    private BlueprintSchemaV3IntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        schemaV1RemainsAccepted();
        schemaV2WorldgenDefaultsToOutpost();
        constructionTemplatesPreserveExactCosts();
        factionTemplatesContainRequiredMarkers();
        crossLoaderResourcesAreComplete();
        System.out.println("BlueprintSchemaV3IntegrationTest passed");
    }

    private static void schemaV1RemainsAccepted() {
        JsonObject json = JsonParser.parseString("""
                {"schema_version":1,"id":"galacticwars:fixture","display_name":"Fixture",
                 "anchor":{"x":0,"y":0,"z":0},"allowed_rotations":[0],
                 "placements":[{"x":0,"y":0,"z":0,"block":"minecraft:stone","item":"minecraft:stone"}],
                 "rewards":{}}
                """).getAsJsonObject();
        KingdomBaseBlueprint parsed = GameplayDataManager.parseBlueprint(
                Identifier.parse("galacticwars:fixture"), json);
        require(parsed.id().equals("galacticwars:fixture") && parsed.placements().size() == 1,
                "schema v1 compatibility failed");
    }

    private static void schemaV2WorldgenDefaultsToOutpost() {
        BlueprintWorldgenProfile legacy = new BlueprintWorldgenProfile(
                java.util.List.of("minecraft:plains"),
                "galacticwars:republic",
                32,
                java.util.List.of(new BlueprintRosterEntry(
                        "galacticwars:clone_trooper", 1, 1, 1, "military", "trooper")),
                java.util.List.of("primary", "supplies"),
                1);
        require(legacy.siteKind() == BlueprintSiteKind.OUTPOST,
                "schema v2 worldgen did not default to an outpost");
        require(legacy.lootTables().values().stream().allMatch(
                        "galacticwars:chests/faction_site/republic"::equals),
                "schema v2 worldgen did not retain its legacy faction loot table");
    }

    private static void constructionTemplatesPreserveExactCosts() throws Exception {
        for (String id : CONSTRUCTION) {
            JsonObject descriptor = json(BLUEPRINTS.resolve(id + ".json"));
            require(descriptor.get("schema_version").getAsInt() == 2, id + " was not migrated to v2");
            require(descriptor.getAsJsonArray("modes").toString().contains("construction"),
                    id + " is not construction-enabled");
            JsonObject costs = descriptor.getAsJsonObject("construction").getAsJsonObject("costs");
            Map<String, Integer> expected = COSTS.get(id);
            require(costs.size() == expected.size(), id + " cost key count changed");
            expected.forEach((item, count) -> require(costs.get(item).getAsInt() == count,
                    id + " cost changed for " + item));
            CompoundTag template = nbt(STRUCTURES.resolve("construction").resolve(id + ".nbt"));
            require(!template.getListOrEmpty("palette").isEmpty(), id + " has no NBT palette");
            require(!template.getListOrEmpty("blocks").isEmpty(), id + " has no NBT blocks");
            require(template.getListOrEmpty("entities").isEmpty(), id + " construction template contains entities");
        }
    }

    private static void factionTemplatesContainRequiredMarkers() throws Exception {
        for (String id : SITES) {
            JsonObject descriptor = json(BLUEPRINTS.resolve(id + ".json"));
            require(descriptor.get("schema_version").getAsInt() == 3,
                    id + " is not schema v3");
            JsonObject worldgen = descriptor.getAsJsonObject("worldgen");
            boolean commandCenter = COMMAND_CENTERS.contains(id);
            require(worldgen.get("site_kind").getAsString().equals(
                    commandCenter ? "command_center" : "outpost"), id + " site kind changed");
            require(worldgen.getAsJsonArray("biomes").size() >= 3, id + " biome routing changed");
            if (id.startsWith("republic_")) {
                require(worldgen.getAsJsonArray("biomes").toString().contains("galacticwars:kamino")
                                && worldgen.getAsJsonArray("biomes").toString().contains("galacticwars:coruscant"),
                        id + " Republic planet routing missing");
            } else if (id.startsWith("separatist_")) {
                require(worldgen.getAsJsonArray("biomes").toString().contains("galacticwars:geonosis"),
                        id + " Geonosis routing missing");
            } else if (id.startsWith("hutt_")) {
                require(worldgen.getAsJsonArray("biomes").toString().contains("galacticwars:tatooine"),
                        id + " Tatooine routing missing");
            } else {
                require(!worldgen.getAsJsonArray("biomes").toString().contains("galacticwars:"),
                        id + " must remain Overworld-only");
            }
            int cap = 0;
            int guaranteedCommanders = 0;
            for (var entry : worldgen.getAsJsonArray("roster")) {
                cap += entry.getAsJsonObject().get("maximum").getAsInt();
                if (entry.getAsJsonObject().get("role").getAsString().equals("commander")) {
                    guaranteedCommanders += entry.getAsJsonObject().get("minimum").getAsInt();
                }
            }
            require(cap > 0 && cap <= 32, id + " roster cap is unsafe");
            require(!commandCenter || guaranteedCommanders == 1,
                    id + " must guarantee exactly one commander");
            CompoundTag template = nbt(STRUCTURES.resolve("sites").resolve(id + ".nbt"));
            ListTag palette = template.getListOrEmpty("palette");
            ListTag blocks = template.getListOrEmpty("blocks");
            int anchors = 0;
            int commandPosts = 0;
            int loot = 0;
            int anchorMarkerY = Integer.MIN_VALUE;
            int commandCacheY = Integer.MIN_VALUE;
            int anchorX = descriptor.getAsJsonObject("anchor").get("x").getAsInt();
            int anchorZ = descriptor.getAsJsonObject("anchor").get("z").getAsInt();
            Set<String> templateLootMarkers = new HashSet<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag block = blocks.getCompoundOrEmpty(index);
                int stateIndex = block.getIntOr("state", -1);
                if (stateIndex >= 0 && palette.getCompoundOrEmpty(stateIndex)
                        .getStringOr("Name", "").equals("minecraft:structure_block")) {
                    String marker = block.getCompoundOrEmpty("nbt").getStringOr("metadata", "");
                    anchors += marker.equals("site_anchor") ? 1 : 0;
                    if (marker.equals("site_anchor")) {
                        anchorMarkerY = block.getListOrEmpty("pos").getIntOr(1, Integer.MIN_VALUE);
                    }
                    commandPosts += marker.equals("command_post") ? 1 : 0;
                    loot += marker.startsWith("loot:") ? 1 : 0;
                    if (marker.startsWith("loot:")) {
                        templateLootMarkers.add(marker.substring("loot:".length()));
                    }
                    if (marker.equals("command_post") || marker.startsWith("loot:")) {
                        ListTag position = block.getListOrEmpty("pos");
                        int markerX = position.getIntOr(0, Integer.MIN_VALUE);
                        int markerZ = position.getIntOr(2, Integer.MIN_VALUE);
                        require(Math.abs(markerX - anchorX) <= 4
                                        && Math.abs(markerZ - anchorZ) <= 4,
                                id + " operational marker exceeds the bounded anchor scan");
                        if (marker.equals("loot:command_cache")) {
                            commandCacheY = position.getIntOr(1, Integer.MIN_VALUE);
                            require(Math.abs(markerX - anchorX) + Math.abs(markerZ - anchorZ) > 1,
                                    id + " command cache overlaps a rotated civilian deposit point");
                        }
                    }
                }
            }
            Set<String> configuredLootMarkers = new HashSet<>();
            worldgen.getAsJsonObject("loot_tables").keySet()
                    .forEach(configuredLootMarkers::add);
            require(anchors == 1, id + " must contain exactly one site anchor");
            require(commandPosts == (commandCenter ? 1 : 0),
                    id + " command post marker count changed");
            require(!commandCenter || commandCacheY == anchorMarkerY,
                    id + " command cache must remain accessible at anchor floor height");
            require(loot == worldgen.getAsJsonObject("loot_tables").size(), id + " loot marker count changed");
            require(templateLootMarkers.equals(configuredLootMarkers),
                    id + " loot marker names do not match descriptor");
            for (var table : worldgen.getAsJsonObject("loot_tables").entrySet()) {
                String tableId = table.getValue().getAsString();
                String path = tableId.substring(tableId.indexOf(':') + 1);
                require(Files.isRegularFile(Path.of(
                                "src/main/resources/data/galacticwars/loot_table/" + path + ".json")),
                        id + " missing loot table for " + table.getKey());
            }
            if (!commandCenter) {
                var compatible = descriptor.getAsJsonArray("compatible_content_hashes");
                require(compatible.size() == 1
                                && compatible.get(0).getAsString().equals(PRE_V3_CONTENT_HASHES.get(id)),
                        id + " missing exact pre-v3 content hash");
            }
        }
    }

    private static void crossLoaderResourcesAreComplete() throws Exception {
        require(Files.readString(Path.of("fabric/src/main/kotlin/galacticwars/clonewars/fabric/FabricWorldgenFeatures.kt"))
                .contains("beskar_ore"), "Fabric Beskar feature missing");
        String fabricBootstrap = Files.readString(Path.of(
                "fabric/src/main/kotlin/galacticwars/clonewars/fabric/GalacticWarsFabric.kt"));
        require(!fabricBootstrap.contains("FabricBiomeSpawns"), "Fabric free NPC spawns remain enabled");
        try (var files = Files.walk(Path.of("src/main/resources/data/galacticwars/neoforge/biome_modifier"))) {
            require(files.filter(Files::isRegularFile).noneMatch(path -> {
                try {
                    return Files.readString(path).contains("add_spawns");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }), "NeoForge free NPC spawn modifier remains enabled");
        }
        require(Files.isRegularFile(Path.of(
                "src/main/resources/data/galacticwars/worldgen/placed_feature/beskar_ore.json")),
                "Beskar placed feature missing");
        require(Files.isRegularFile(Path.of(
                "src/main/resources/data/galacticwars/worldgen/placed_feature/nightsister_weave_grove.json")),
                "Nightsister grove placed feature missing");
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static CompoundTag nbt(Path path) throws Exception {
        try (InputStream input = Files.newInputStream(path)) {
            return NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
