package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public final class AssetReferenceIntegrityTest {
    private static final Set<String> LAUNCH_UNITS = Set.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard", "jedi_knight", "sith_acolyte",
            "b1_battle_droid", "b1_security_droid", "b2_super_battle_droid", "commando_droid",
            "mandalorian_warrior", "mandalorian_marksman", "mandalorian_heavy",
            "hutt_enforcer", "bounty_hunter", "smuggler",
            "nightsister_acolyte", "nightsister_archer", "nightbrother_brute",
            "republic_civilian", "togruta_civilian", "separatist_technician",
            "mandalorian_clansperson", "hutt_civilian", "nightsister_civilian");
    private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
    private static final Path MOD_ASSET_ROOT = RESOURCE_ROOT.resolve("assets/galacticwars");
    private static final Path MOD_DATA_ROOT = RESOURCE_ROOT.resolve("data/galacticwars");
    private static final Path GRADLE_PROPERTIES = Path.of("gradle.properties");
    private static final Pattern MOD_REFERENCE = Pattern.compile("\"(galacticwars:(?:block|item)/[^\"]+)\"");
    private static final Pattern TEXTURES_OBJECT = Pattern.compile(
            "\"textures\"\\s*:\\s*\\{([^}]*)}", Pattern.DOTALL);
    private static final Pattern MOD_TEXTURE_REFERENCE = Pattern.compile(
            "\"[^\"]+\"\\s*:\\s*\"(galacticwars:(?:block|item)/[^\"]+)\"");

    private AssetReferenceIntegrityTest() {
    }

    public static void main(String[] args) throws IOException {
        blockstatesReferenceExistingBlockModels();
        itemDefinitionsReferenceExistingItemModels();
        itemModelsHaveModernDefinitions();
        exactRegisteredItemDefinitionsExist();
        modelsReferenceExistingModTextures();
        recruitRenderTexturesExist();
        geckoRecruitAssetsExist();
        overworldBiomeModifiersContainOnlyResourceFeatures();
        builtJarContainsModAssetsAndData();

        System.out.println("AssetReferenceIntegrityTest passed");
    }

    private static void blockstatesReferenceExistingBlockModels() throws IOException {
        try (Stream<Path> files = Files.walk(MOD_ASSET_ROOT.resolve("blockstates"))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                for (String reference : referencesIn(file)) {
                    if (reference.startsWith("galacticwars:block/")) {
                        assertRegularFile(modelPath(reference), "blockstate model reference " + reference);
                    }
                }
            }
        }
    }

    private static void itemDefinitionsReferenceExistingItemModels() throws IOException {
        try (Stream<Path> files = Files.walk(MOD_ASSET_ROOT.resolve("items"))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                for (String reference : referencesIn(file)) {
                    if (reference.startsWith("galacticwars:item/")) {
                        assertRegularFile(modelPath(reference), "item definition model reference " + reference);
                    }
                }
            }
        }
    }

    private static void itemModelsHaveModernDefinitions() throws IOException {
        Path models = MOD_ASSET_ROOT.resolve("models/item");
        Path definitions = MOD_ASSET_ROOT.resolve("items");
        try (Stream<Path> files = Files.walk(models)) {
            for (Path model : files.filter(Files::isRegularFile).toList()) {
                String name = model.getFileName().toString();
                if (name.equals("lightsaber_base.json") || name.equals("spawn_capsule_base.json")) {
                    continue;
                }
                assertRegularFile(definitions.resolve(name), "modern item definition for " + name);
            }
        }
    }

    private static void modelsReferenceExistingModTextures() throws IOException {
        try (Stream<Path> files = Files.walk(MOD_ASSET_ROOT.resolve("models"))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                for (String reference : textureReferencesIn(file)) {
                    assertRegularFile(texturePath(reference), "model texture reference " + reference);
                }
            }
        }
    }

    private static void recruitRenderTexturesExist() throws IOException {
        String client = Files.readString(Path.of("src/main/java/galacticwars/clonewars/GalacticWarsClient.java"));
        String renderer = Files.readString(Path.of("src/main/java/galacticwars/clonewars/client/render/GalacticRecruitRenderer.java"));

        assertContains(renderer, "GeoEntityRenderer", "GeckoLib recruit renderer");
        assertContains(client, "ModEntityTypes.recruits()", "data-driven launch-unit renderer registration");
        for (String recruit : LAUNCH_UNITS) {
            assertRegularFile(MOD_ASSET_ROOT.resolve("textures/entity/" + recruit + ".png"), recruit + " texture");
        }
    }

    private static void geckoRecruitAssetsExist() {
        for (String recruit : LAUNCH_UNITS) {
            assertRegularFile(MOD_ASSET_ROOT.resolve("geckolib/models/entity/" + recruit + ".geo.json"),
                    "GeckoLib model " + recruit);
            assertRegularFile(MOD_ASSET_ROOT.resolve("geckolib/animations/entity/" + recruit + ".animation.json"),
                    "GeckoLib animation " + recruit);
        }
    }

    private static void overworldBiomeModifiersContainOnlyResourceFeatures() throws IOException {
        String entities = Files.readString(Path.of("src/main/java/galacticwars/clonewars/registry/ModEntityTypes.java"));
        for (String recruit : LAUNCH_UNITS) {
            assertContains(entities, "\"" + recruit + "\"", recruit + " entity registration");
        }

        try (Stream<Path> files = Files.walk(MOD_DATA_ROOT.resolve("neoforge/biome_modifier"))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(file);
                assertContains(content, "galacticwars:", "feature namespace");
                assertContains(content, "\"type\"", "biome modifier type field");
                assertContains(content, "\"neoforge:add_features\"", "resource feature modifier type");
                if (content.contains("neoforge:add_spawns")) {
                    throw new AssertionError("Overworld NPC spawn injection must remain disabled: " + file);
                }
            }
        }
    }

    private static void builtJarContainsModAssetsAndData() throws IOException {
        Path builtJar = builtJar();
        assertRegularFile(builtJar, "built mod jar");
        try (ZipFile jar = new ZipFile(builtJar.toFile())) {
            for (String entry : sourceResourceEntries()) {
                if (jar.getEntry(entry) == null) {
                    throw new AssertionError("built jar missing resource <" + entry + ">");
                }
            }
        }
    }

    private static Path builtJar() throws IOException {
        String version = Files.readAllLines(GRADLE_PROPERTIES).stream()
                .filter(line -> line.startsWith("mod_version="))
                .map(line -> line.substring("mod_version=".length()).trim())
                .findFirst()
                .orElseThrow(() -> new AssertionError("mod_version missing from " + GRADLE_PROPERTIES));
        return Path.of("neoforge/build/libs/galacticwars-neoforge-" + version + ".jar");
    }

    private static Set<String> sourceResourceEntries() throws IOException {
        HashSet<String> entries = new HashSet<>();
        try (Stream<Path> files = Files.walk(MOD_ASSET_ROOT)) {
            files.filter(Files::isRegularFile)
                    .map(RESOURCE_ROOT::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .forEach(entries::add);
        }
        try (Stream<Path> files = Files.walk(MOD_DATA_ROOT)) {
            files.filter(Files::isRegularFile)
                    .map(RESOURCE_ROOT::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .forEach(entries::add);
        }
        return entries;
    }

    private static Set<String> referencesIn(Path file) throws IOException {
        Matcher matcher = MOD_REFERENCE.matcher(Files.readString(file));
        HashSet<String> references = new HashSet<>();
        while (matcher.find()) {
            references.add(matcher.group(1));
        }
        return references;
    }

    private static void exactRegisteredItemDefinitionsExist() throws IOException {
        assertRegularFile(MOD_ASSET_ROOT.resolve("items/nightsister_weave.json"),
                "Nightsister Weave exact item definition");
        if (Files.exists(MOD_ASSET_ROOT.resolve("items/nightsister_weave_weave.json"))) {
            throw new AssertionError("stale Nightsister Weave duplicate item definition");
        }
        String navigatorModel = Files.readString(MOD_ASSET_ROOT.resolve("models/item/hyperspace_navigator.json"));
        assertContains(navigatorModel, "\"parent\": \"minecraft:item/compass_16\"",
                "Hyperspace Navigator vanilla model parent");
    }

    private static Set<String> textureReferencesIn(Path file) throws IOException {
        HashSet<String> references = new HashSet<>();
        Matcher textures = TEXTURES_OBJECT.matcher(Files.readString(file));
        while (textures.find()) {
            Matcher reference = MOD_TEXTURE_REFERENCE.matcher(textures.group(1));
            while (reference.find()) {
                references.add(reference.group(1));
            }
        }
        return references;
    }

    private static Path modelPath(String reference) {
        String path = reference.substring("galacticwars:".length());
        return MOD_ASSET_ROOT.resolve("models").resolve(path + ".json");
    }

    private static Path texturePath(String reference) {
        String path = reference.substring("galacticwars:".length());
        return MOD_ASSET_ROOT.resolve("textures").resolve(path + ".png");
    }

    private static void assertContains(String haystack, String needle, String label) {
        if (!haystack.contains(needle)) {
            throw new AssertionError(label + " missing <" + needle + ">");
        }
    }

    private static void assertRegularFile(Path path, String label) {
        if (!Files.isRegularFile(path)) {
            throw new AssertionError(label + " missing <" + path + ">");
        }
    }
}
