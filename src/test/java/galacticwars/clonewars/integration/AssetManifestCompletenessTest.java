package galacticwars.clonewars.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public final class AssetManifestCompletenessTest {
    private static final Path MANIFEST = Path.of("docs/galacticwars-asset-manifest.json");
    private static final Path ASSET_ROOT = Path.of("src/main/resources/assets/galacticwars");
    private static final Set<String> UNIQUE_BATCHES = Set.of(
            "equipped_armor", "combat_and_tools", "planets", "vehicles", "effects_and_gui",
            "field_command_and_deployment");
    private static final Set<String> SOURCE_128_UNITS = Set.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard");
    private static final Set<String> SOURCE_128_BY_64_UNITS = Set.of(
            "b1_battle_droid", "b1_security_droid", "separatist_technician");
    private static final List<String> FACTIONS = List.of(
            "republic", "separatist", "mandalorian", "hutt_cartel", "nightsister");
    private static final List<String> UNITS = List.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard", "jedi_knight", "sith_acolyte",
            "b1_battle_droid", "b1_security_droid", "b2_super_battle_droid", "commando_droid",
            "mandalorian_warrior", "mandalorian_marksman", "mandalorian_heavy",
            "hutt_enforcer", "bounty_hunter", "smuggler",
            "nightsister_acolyte", "nightsister_archer", "nightbrother_brute",
            "republic_civilian", "togruta_civilian", "separatist_technician", "mandalorian_clansperson",
            "hutt_civilian", "nightsister_civilian");
    private static final Map<String, String> COMMANDER_TEXTURES = Map.of(
            "clone_trooper_commander", "128x128",
            "arc_trooper_commander", "128x128",
            "b1_battle_droid_commander", "128x64");
    private static final List<String> COMBAT = List.of(
            "vibroblade", "plasma_cutter", "power_drill", "sonic_excavator", "hydrospanner",
            "dc15_blaster", "e5_blaster", "westar_blaster", "scatter_blaster", "nightsister_bow",
            "blue_lightsaber", "green_lightsaber", "red_lightsaber", "purple_lightsaber",
            "yellow_lightsaber", "white_lightsaber", "beskar_vibroblade",
            "blaster_bolt", "force_light", "force_dark");
    private static final List<String> VEHICLES = List.of(
            "barc_speeder", "at_rt", "stap", "aat", "laat_gunship");
    private static final List<String> ARMOR_FAMILIES = List.of(
            "republic_plastoid", "separatist_alloy", "mandalorian_alloy",
            "nightsister_weave", "beskar", "phase_i_clone");

    private AssetManifestCompletenessTest() {
    }

    public static void main(String[] args) throws Exception {
        String manifest = Files.readString(MANIFEST);
        validateManifestJson(manifest);
        assertContains(manifest, "\"schema_version\": 1", "asset schema");
        assertContains(manifest, "\"namespace\": \"galacticwars\"", "asset namespace");
        for (String batch : List.of("core", "factions", "units", "equipped_armor", "combat_and_tools",
                "planets", "vehicles", "effects_and_gui", "field_command_and_deployment")) {
            assertContains(manifest, "\"id\": \"" + batch + "\"", "asset batch " + batch);
        }
        for (String id : UNITS) assertContains(manifest, "\"" + id + "\"", "unit manifest id " + id);
        for (String id : COMMANDER_TEXTURES.keySet()) {
            assertContains(manifest, "textures/entity/" + id + ".png",
                    "commander texture manifest path " + id);
        }
        for (String id : COMBAT) assertContains(manifest, "\"" + id + "\"", "combat manifest id " + id);
        for (String id : VEHICLES) assertContains(manifest, "\"" + id + "\"", "vehicle manifest id " + id);
        for (String id : ARMOR_FAMILIES) {
            assertContains(manifest, "\"" + id + "\"", "equipped armor manifest id " + id);
        }

        List<ExpectedAsset> assets = expectations();
        Map<String, Set<String>> hashesByBatch = new HashMap<>();
        for (ExpectedAsset asset : assets) {
            Path file = ASSET_ROOT.resolve(asset.path());
            if (!Files.isRegularFile(file)) {
                throw new AssertionError("Manifest asset missing: " + file);
            }
            validateImage(asset, file);
            if (asset.itemModel()) {
                validateItemModel(asset.id());
            }
            if (asset.batch().equals("vehicles")) {
                validateVehicleModel(asset.id());
            }
            if (UNIQUE_BATCHES.contains(asset.batch())) {
                String hash = sha256(file);
                if (!hashesByBatch.computeIfAbsent(asset.batch(), ignored -> new HashSet<>()).add(hash)) {
                    throw new AssertionError("Duplicate artwork hash in unique batch " + asset.batch()
                            + " at " + asset.path());
                }
            }
        }
        validateMinimumDistinctUnitTextures(UNITS.size() - 2);
        for (String faction : FACTIONS) validateFactionEquipment(faction);
        System.out.println("AssetManifestCompletenessTest passed (" + assets.size() + " authored assets)");
    }

    private static void validateManifestJson(String manifest) {
        JsonArray batches = JsonParser.parseString(manifest).getAsJsonObject().getAsJsonArray("batches");
        JsonObject lightsabers = null;
        for (var element : batches) {
            JsonObject batch = element.getAsJsonObject();
            if ("lightsaber_models".equals(batch.get("id").getAsString())) {
                lightsabers = batch;
                break;
            }
        }
        if (lightsabers == null) {
            throw new AssertionError("asset manifest lacks lightsaber_models batch");
        }
        assertEquals(
                "geckolib/animations/item/lightsaber/{id}.animation.json",
                lightsabers.get("animation_path_template").getAsString(),
                "lightsaber GeckoLib animation path");
        assertEquals(
                "textures/item/lightsaber/{id}.png.mcmeta",
                lightsabers.get("mcmeta_path_template").getAsString(),
                "lightsaber sprite animation metadata path");
        if (occurrences(manifest, "\"animation_path_template\"") != 2) {
            throw new AssertionError("asset manifest must contain one animation path per GeckoLib item batch");
        }
    }

    private static List<ExpectedAsset> expectations() {
        ArrayList<ExpectedAsset> assets = new ArrayList<>();
        opaque(assets, "core", "command_center_side", "textures/block/command_center_side.png", "16x16");
        opaque(assets, "core", "command_center_top", "textures/block/command_center_top.png", "16x16");
        opaque(assets, "core", "command_center_bottom", "textures/block/command_center_bottom.png", "16x16");
        opaque(assets, "core", "duracrete", "textures/block/duracrete.png", "16x16");
        opaque(assets, "core", "beskar_ore", "textures/block/beskar_ore.png", "16x16");
        item(assets, "core", "raw_beskar");
        item(assets, "core", "beskar_ingot");
        item(assets, "core", "credit_chip");
        item(assets, "core", "energy_cell");
        transparent(assets, "core", "galactic_wars_tab", "textures/gui/galactic_wars_tab.png", "64x64");

        opaque(assets, "field_command_and_deployment", "control_beacon",
                "textures/block/control_beacon.png", "16x16");
        for (String id : List.of(
                "blueprint_projector", "claim_transponder", "command_marker", "hyperspace_navigator",
                "barc_speeder_deployment_kit", "at_rt_deployment_kit", "stap_deployment_kit",
                "aat_deployment_kit", "laat_gunship_deployment_kit")) {
            item(assets, "field_command_and_deployment", id);
        }

        for (String faction : FACTIONS) {
            item(assets, "factions", faction + "_identity_chip");
            for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
                item(assets, "factions", faction + "_" + piece);
            }
            transparent(assets, "factions", faction + "_armor_adult",
                    "textures/entity/equipment/humanoid/" + faction + ".png", "64x32");
            transparent(assets, "factions", faction + "_armor_baby",
                    "textures/entity/equipment/humanoid_baby/" + faction + ".png", "64x64");
            transparent(assets, "factions", faction + "_armor_leggings",
                    "textures/entity/equipment/humanoid_leggings/" + faction + ".png", "64x32");
        }
        for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
            item(assets, "equipped_armor", "phase_i_clone_" + piece);
        }
        for (String unit : UNITS) {
            String size = SOURCE_128_BY_64_UNITS.contains(unit) ? "128x64"
                    : SOURCE_128_UNITS.contains(unit) ? "128x128" : "256x256";
            transparent(assets, "units", unit, "textures/entity/" + unit + ".png", size);
        }
        for (Map.Entry<String, String> commander : COMMANDER_TEXTURES.entrySet()) {
            transparent(assets, "units", commander.getKey(),
                    "textures/entity/" + commander.getKey() + ".png", commander.getValue());
        }
        for (String family : ARMOR_FAMILIES) {
            String size = Set.of("phase_i_clone", "republic_plastoid").contains(family)
                    ? "128x128" : "1024x1024";
            transparent(assets, "equipped_armor", family,
                    "textures/armor/" + family + ".png", size);
        }
        for (String id : COMBAT) {
            if (id.endsWith("_lightsaber")) {
                transparent(assets, "combat_and_tools", id,
                        "textures/item/" + id + ".png", "16x16");
            } else {
                item(assets, "combat_and_tools", id);
            }
        }

        transparent(assets, "planets", "tatooine", "textures/gui/planet/tatooine.png", "128x128");
        transparent(assets, "planets", "geonosis", "textures/gui/planet/geonosis.png", "128x128");
        transparent(assets, "planets", "kamino", "textures/gui/planet/kamino.png", "128x128");
        transparent(assets, "planets", "coruscant", "textures/gui/planet/coruscant.png", "128x128");
        opaque(assets, "planets", "tatooine_sand", "textures/block/tatooine_sand.png", "16x16");
        opaque(assets, "planets", "geonosis_rock", "textures/block/geonosis_rock.png", "16x16");
        opaque(assets, "planets", "kamino_panel", "textures/block/kamino_panel.png", "16x16");
        opaque(assets, "planets", "coruscant_panel", "textures/block/coruscant_panel.png", "16x16");
        transparent(assets, "planets", "navigation_console", "textures/gui/navigation_console.png", "256x256");
        for (String vehicle : VEHICLES) {
            transparent(assets, "vehicles", vehicle,
                    "textures/entity/vehicle/" + vehicle + ".png", "256x256");
        }
        transparent(assets, "effects_and_gui", "blaster_bolt_particle",
                "textures/particle/blaster_bolt.png", "16x16");
        transparent(assets, "effects_and_gui", "force_wave",
                "textures/particle/force_wave.png", "16x16");
        transparent(assets, "effects_and_gui", "quest_panel", "textures/gui/quest_panel.png", "256x256");
        transparent(assets, "effects_and_gui", "conquest_panel", "textures/gui/conquest_panel.png", "256x256");
        transparent(assets, "effects_and_gui", "vehicle_hud", "textures/gui/vehicle_hud.png", "256x128");
        transparent(assets, "effects_and_gui", "force_meter", "textures/gui/force_meter.png", "128x16");
        return List.copyOf(assets);
    }

    private static void item(List<ExpectedAsset> assets, String batch, String id) {
        assets.add(new ExpectedAsset(batch, id, "textures/item/" + id + ".png",
                "16x16", true, false, true));
    }

    private static void transparent(
            List<ExpectedAsset> assets, String batch, String id, String path, String size
    ) {
        assets.add(new ExpectedAsset(batch, id, path, size, true, false, false));
    }

    private static void opaque(List<ExpectedAsset> assets, String batch, String id, String path, String size) {
        assets.add(new ExpectedAsset(batch, id, path, size, false, true, false));
    }

    private static void validateImage(ExpectedAsset asset, Path file) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null) {
            throw new AssertionError("Unreadable PNG: " + file);
        }
        String actualSize = image.getWidth() + "x" + image.getHeight();
        if (!actualSize.equals(asset.size())) {
            throw new AssertionError(file + " expected " + asset.size() + " but was " + actualSize);
        }
        boolean hasTransparent = false;
        boolean hasVisible = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                hasTransparent |= alpha == 0;
                hasVisible |= alpha != 0;
                if (asset.opaque() && alpha != 255) {
                    throw new AssertionError("Opaque asset contains alpha at " + file + " (" + x + "," + y + ")");
                }
            }
        }
        if (!hasVisible || (asset.transparent() && !hasTransparent)) {
            throw new AssertionError("Alpha/occupancy rule failed for " + file);
        }
    }

    private static void validateItemModel(String id) throws IOException {
        Path definition = ASSET_ROOT.resolve("items/" + id + ".json");
        Path model = ASSET_ROOT.resolve("models/item/" + id + ".json");
        if (!Files.isRegularFile(definition) || !Files.isRegularFile(model)) {
            throw new AssertionError("Item asset lacks definition/model: " + id);
        }
        String definitionJson = Files.readString(definition);
        String modelJson = Files.readString(model);
        if (Set.of("dc15_blaster", "e5_blaster", "westar_blaster", "scatter_blaster").contains(id)) {
            if (!definitionJson.contains("galacticwars:item/" + id)
                    || !definitionJson.contains("geckolib:geckolib")
                    || !modelJson.contains("\"parent\": \"builtin/entity\"")
                    || !Files.isRegularFile(ASSET_ROOT.resolve(
                            "geckolib/models/item/blaster/" + id + ".geo.json"))
                    || !Files.isRegularFile(ASSET_ROOT.resolve(
                            "textures/item/blaster/" + id + ".png"))) {
                throw new AssertionError("Volumetric blaster contract is incomplete: " + id);
            }
        } else if (!definitionJson.contains("galacticwars:item/" + id)
                || !modelJson.contains("galacticwars:item/" + id)) {
            throw new AssertionError("Item model reference does not resolve to its manifest texture: " + id);
        }
    }

    private static void validateVehicleModel(String id) throws IOException {
        Path geometry = ASSET_ROOT.resolve("geckolib/models/entity/vehicle/" + id + ".geo.json");
        Path animation = ASSET_ROOT.resolve("geckolib/animations/entity/vehicle/" + id + ".animation.json");
        if (!Files.isRegularFile(geometry) || !Files.isRegularFile(animation)) {
            throw new AssertionError("Vehicle atlas lacks GeckoLib references: " + id);
        }
        String geometryText = Files.readString(geometry);
        String animationText = Files.readString(animation);
        assertContains(geometryText, "geometry.galacticwars.vehicle." + id,
                "vehicle geometry identifier " + id);
        assertContains(geometryText, "\"texture_width\": 256", "vehicle texture width " + id);
        assertContains(geometryText, "\"texture_height\": 256", "vehicle texture height " + id);
        assertContains(animationText, "\"vehicle.idle\"", "vehicle idle animation " + id);
        assertContains(animationText, "\"vehicle.travel\"", "vehicle travel animation " + id);
    }

    private static void validateFactionEquipment(String faction) throws IOException {
        Path equipment = ASSET_ROOT.resolve("equipment/" + faction + ".json");
        if (!Files.isRegularFile(equipment)) {
            throw new AssertionError("Faction equipment descriptor missing: " + faction);
        }
        String text = Files.readString(equipment);
        assertContains(text, "galacticwars:" + faction, "faction equipment layer " + faction);
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
    }

    private static void validateMinimumDistinctUnitTextures(int minimum) throws Exception {
        Set<String> hashes = new HashSet<>();
        for (String unit : UNITS) {
            hashes.add(sha256(ASSET_ROOT.resolve("textures/entity/" + unit + ".png")));
        }
        if (hashes.size() < minimum) {
            throw new AssertionError("Unit roster has only " + hashes.size()
                    + " distinct texture atlases; expected at least " + minimum);
        }
    }

    private static void assertContains(String value, String expected, String label) {
        if (!value.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static int occurrences(String value, String expected) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(expected, offset)) >= 0) {
            count++;
            offset += expected.length();
        }
        return count;
    }

    private record ExpectedAsset(
            String batch,
            String id,
            String path,
            String size,
            boolean transparent,
            boolean opaque,
            boolean itemModel
    ) {
    }
}
