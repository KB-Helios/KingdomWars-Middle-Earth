package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PlanetFactionEcologyResourcesTest {
    private static final Path DATA_ROOT = Path.of("src/main/resources/data/galacticwars");
    private static final Path GAMEPLAY_DATA_ROOT = DATA_ROOT.resolve("galacticwars");
    private static final Pattern PLANET = Pattern.compile(
            "\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\"[^{}]*"
                    + "\"dimension\"\\s*:\\s*\"([^\"]+)\"[^{}]*"
                    + "\"faction\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CUSTOM_SPAWNER = Pattern.compile(
            "\\{\\s*\"type\"\\s*:\\s*\"(galacticwars:[^\"]+)\"\\s*,"
                    + "\\s*\"maxCount\"\\s*:\\s*(\\d+)\\s*,"
                    + "\\s*\"minCount\"\\s*:\\s*(\\d+)\\s*,"
                    + "\\s*\"weight\"\\s*:\\s*(\\d+)\\s*}");
    private static final Map<String, String> PLANET_FEATURE_MARKERS = Map.of(
            "tatooine", "minecraft:patch_cactus_desert",
            "geonosis", "minecraft:ore_gold_extra",
            "kamino", "minecraft:kelp_cold",
            "coruscant", "minecraft:glow_lichen");

    private PlanetFactionEcologyResourcesTest() {
    }

    public static void main(String[] args) throws IOException {
        Map<String, PlanetData> planets = readPlanets();
        if (planets.size() != 4) {
            throw new AssertionError("Expected exactly four launch planets, found " + planets.keySet());
        }
        Map<String, Set<String>> entitiesByFaction = readFactionEntities();

        for (PlanetData planet : planets.values()) {
            String namespacedFaction = namespaced(planet.factionId());
            Set<String> expectedEntities = entitiesByFaction.getOrDefault(namespacedFaction, Set.of());
            if (expectedEntities.isEmpty()) {
                throw new AssertionError("Launch planet " + planet.id()
                        + " has no authoritative faction entity definitions for " + namespacedFaction);
            }

            Path dimensionPath = DATA_ROOT.resolve("dimension/" + planet.id() + ".json");
            Path biomePath = DATA_ROOT.resolve("worldgen/biome/" + planet.id() + ".json");
            String dimension = read(dimensionPath);
            String biome = read(biomePath);
            assertContains(dimension, "\"biome\": \"galacticwars:" + planet.id() + "\"",
                    planet.id() + " custom biome reference");
            if (dimension.contains("\"type\": \"minecraft:flat\"")) {
                assertContains(dimension, "\"features\": true", planet.id() + " feature generation");
            } else {
                assertContains(dimension, "\"type\": \"minecraft:noise\"",
                        planet.id() + " noise terrain");
            }
            assertContains(biome, "\"features\"", planet.id() + " biome features");
            assertContains(biome, "\"spawners\"", planet.id() + " biome spawners");
            assertContains(biome, PLANET_FEATURE_MARKERS.get(planet.id()),
                    planet.id() + " thematic generation feature");
            assertNotContains(biome, "neoforge:", planet.id() + " loader-neutral biome");
            assertNotContains(biome, "fabric:", planet.id() + " loader-neutral biome");

            Set<String> actualEntities = customSpawnerEntityIds(biome, planet.id());
            if (!actualEntities.equals(expectedEntities)) {
                throw new AssertionError(planet.id() + " faction spawners expected <"
                        + expectedEntities + "> but were <" + actualEntities + ">");
            }
        }
        assertNaturalInitializationContract();

        System.out.println("PlanetFactionEcologyResourcesTest passed");
    }

    private static void assertNaturalInitializationContract() throws IOException {
        String entity = read(Path.of(
                "src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java"));
        String finalizeSpawn = section(
                entity,
                "public SpawnGroupData finalizeSpawn(",
                "private boolean initializeNaturalWorldSpawn(");
        assertContains(finalizeSpawn, "reason == EntitySpawnReason.CHUNK_GENERATION",
                "deferred chunk-generation initialization");
        assertContains(finalizeSpawn, "|| reason == EntitySpawnReason.NATURAL",
                "loader-cancellation-safe natural initialization");
        assertContains(finalizeSpawn, "this.pendingNaturalSpawnInitialization = true",
                "serializable deferred natural-spawn initialization");
        assertNotContains(finalizeSpawn, "this.initializeNaturalWorldSpawn(",
                "finalizeSpawn must not mutate faction SavedData");
        assertNotContains(finalizeSpawn, "this.discard()",
                "finalizeSpawn must not pass removed entities to vanilla add paths");

        String naturalWorldSpawn = section(
                entity,
                "private boolean initializeNaturalWorldSpawn(",
                "private void tryGenerateFactionOutpostSite(");
        assertContains(naturalWorldSpawn, "level.dimension().equals(Level.OVERWORLD)",
                "common Overworld spawn branch");
        assertContains(naturalWorldSpawn,
                "this.isTame() || this.kingdomId != null || this.settlementId != null",
                "failure-atomic deferred initialization guard");
        assertContains(naturalWorldSpawn, "Overworld faction residents are created only",
                "generated-site-only Overworld contract");
        assertNotContains(naturalWorldSpawn, ".overworldSpawnProfileForEntity(entityTypeId)",
                "free Overworld profile spawning must be retired");
        assertNotContains(naturalWorldSpawn, "data.assignNaturalNpc(",
                "free Overworld outpost assignment must be retired");
        assertContains(naturalWorldSpawn, "PlanetFactionSpawnPolicy.evaluate(",
                "common planet faction gate");
        assertContains(naturalWorldSpawn, "this.initializeNaturalPlanetNpc(level, evaluation)",
                "common planet NPC outpost initialization");

        String siteAnchor = read(Path.of(
                "src/main/java/galacticwars/clonewars/world/BlueprintSiteAnchorBlockEntity.java"));
        assertContains(siteAnchor, "data.publishGeneratedSiteRecord(",
                "generated site must publish identity before residents");
        assertContains(siteAnchor, "data.markSiteGenerated(siteId);",
                "generated site must mark completion after reconciliation");
        if (siteAnchor.indexOf("data.publishGeneratedSiteRecord(")
                >= siteAnchor.indexOf("data.markSiteGenerated(siteId);")) {
            throw new AssertionError(
                    "generated site completion must follow deterministic identity publication");
        }
        assertContains(siteAnchor, "initialized = true", "persistent one-shot site initialization");
        assertContains(siteAnchor, "recruit.setPersistenceRequired()", "persistent site residents");

        String marker = read(Path.of(
                "src/main/java/galacticwars/clonewars/world/FactionOutpostMarkerService.java"));
        String relocatedGeneration = section(
                marker,
                "public static Optional<BlockPos> generateFirstViableLoadedSite(",
                "private static void build(");
        assertContains(relocatedGeneration, "candidateIndex < window.endExclusive()",
                "bounded candidate window");
        assertContains(relocatedGeneration, "FactionOutpostSitePlan.candidate(candidateIndex)",
                "deterministic relocation candidate");
        assertContains(relocatedGeneration, "siteAreaLoaded(level, horizontalCandidate)",
                "loaded-only candidate guard");
        assertNotContains(relocatedGeneration, "getChunk(",
                "relocated generation synchronous chunk request");
        assertNotContains(relocatedGeneration, "getHeight(",
                "relocated generation heightmap chunk request");

        String savedData = read(Path.of(
                "src/main/java/galacticwars/clonewars/world/FactionOutpostSavedData.java"));
        assertContains(savedData, "optionalFieldOf(\"site_progress\"",
                "persisted site attempt authority");
        assertContains(savedData, "siteProgressByOutpost.put(outpostId, claim.followingProgress())",
                "exclusive site attempt cursor advancement");

        String tick = section(entity, "public void tick()", "public SpawnGroupData finalizeSpawn(");
        assertContains(tick, "this.pendingNaturalSpawnRemoval",
                "legacy deferred-removal persistence cleanup");
        assertContains(tick, "this.pendingNaturalSpawnInitialization",
                "deferred chunk-generation initialization");
        assertContains(tick, "this.initializeNaturalWorldSpawn(serverLevel)",
                "first live server-tick faction initialization");
        assertContains(tick, "this.discard()",
                "first-tick rejected natural spawn removal");
        assertContains(tick, "this.tryGenerateFactionOutpostSite(serverLevel)",
                "existing planet post-spawn shelter lifecycle");

        String initializer = section(
                entity,
                "public void initializeNaturalPlanetNpc()",
                "public @Nullable UUID getArmyGroupId()");
        assertContains(initializer, ".overworldSpawnProfiles().get(evaluation.factionId())",
                "matching faction outpost profile");
        assertContains(initializer, "FactionOutpostSavedData.get(level).assignNaturalNpc(",
                "persisted planet outpost assignment");
        assertContains(initializer, "this.initializeNaturalFactionNpc(",
                "shared civilian and military outpost lifecycle");
        assertContains(initializer, "FactionOutpostMarkerService.shelterCenter(outpost)",
                "planet outpost home assignment");
        assertNotContains(initializer, "this.setPersistenceRequired()",
                "planet NPCs must remain eligible for natural despawn");
        assertContains(initializer, "this.applyUnitDefinition()",
                "planet NPC loadout application");
        assertContains(initializer, "this.naturalPlanetNpcInitialized = true",
                "planet initialization completion guard");
        assertContains(entity, "output.putBoolean(\"NaturalPlanetNpc\"",
                "planet lifecycle persistence");
        assertContains(entity, "output.putBoolean(\"PendingNaturalSpawnInitialization\"",
                "deferred natural spawn persistence");
        assertContains(entity, "output.putBoolean(\"PendingNaturalSpawnRemoval\"",
                "deferred rejected spawn persistence");
        assertContains(entity, "input.getBooleanOr(\n                \"PendingNaturalSpawnInitialization\"",
                "deferred natural spawn reload");
        assertContains(entity, "input.getBooleanOr(\n                \"PendingNaturalSpawnRemoval\"",
                "deferred rejected spawn reload");

        String removal = section(entity, "public void onRemoval(", "public InteractionResult mobInteract(");
        assertContains(removal, "reason.shouldDestroy()", "destructive removal guard");
        assertContains(removal, "FactionOutpostSavedData.get(serverLevel).removeNpc(",
                "common outpost roster cleanup");
    }

    private static Map<String, PlanetData> readPlanets() throws IOException {
        String launch = read(GAMEPLAY_DATA_ROOT.resolve("planets/launch.json"));
        LinkedHashMap<String, PlanetData> planets = new LinkedHashMap<>();
        Matcher matcher = PLANET.matcher(launch);
        while (matcher.find()) {
            String id = matcher.group(1);
            PlanetData previous = planets.putIfAbsent(
                    id, new PlanetData(id, matcher.group(2), matcher.group(3)));
            if (previous != null) {
                throw new AssertionError("Duplicate launch planet " + id);
            }
        }
        for (PlanetData planet : planets.values()) {
            String expectedDimension = "galacticwars:" + planet.id();
            if (!planet.dimensionId().equals(expectedDimension)) {
                throw new AssertionError("Launch planet " + planet.id() + " dimension expected <"
                        + expectedDimension + "> but was <" + planet.dimensionId() + ">");
            }
        }
        return Map.copyOf(planets);
    }

    private static Map<String, Set<String>> readFactionEntities() throws IOException {
        LinkedHashMap<String, Set<String>> entitiesByFaction = new LinkedHashMap<>();
        collectFactionEntities(GAMEPLAY_DATA_ROOT.resolve("units"), entitiesByFaction);
        collectFactionEntities(GAMEPLAY_DATA_ROOT.resolve("civilian_archetypes"), entitiesByFaction);
        LinkedHashMap<String, Set<String>> immutable = new LinkedHashMap<>();
        entitiesByFaction.forEach((faction, entities) -> immutable.put(faction, Set.copyOf(entities)));
        return Map.copyOf(immutable);
    }

    private static void collectFactionEntities(
            Path directory,
            Map<String, Set<String>> entitiesByFaction
    ) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new AssertionError("Missing gameplay data directory <" + directory + ">");
        }
        try (Stream<Path> files = Files.list(directory)) {
            for (Path path : files.filter(file -> file.getFileName().toString().endsWith(".json"))
                    .sorted().toList()) {
                String json = read(path);
                String faction = namespaced(requiredStringField(json, "faction", path));
                String entityType = namespaced(requiredStringField(json, "entity_type", path));
                if (!entitiesByFaction.computeIfAbsent(faction, ignored -> new LinkedHashSet<>())
                        .add(entityType)) {
                    throw new AssertionError("Duplicate faction entity mapping " + entityType);
                }
            }
        }
    }

    private static Set<String> customSpawnerEntityIds(String biome, String planetId) {
        LinkedHashSet<String> entities = new LinkedHashSet<>();
        Matcher matcher = CUSTOM_SPAWNER.matcher(biome);
        while (matcher.find()) {
            int maxCount = Integer.parseInt(matcher.group(2));
            int minCount = Integer.parseInt(matcher.group(3));
            int weight = Integer.parseInt(matcher.group(4));
            if (minCount < 1 || maxCount < minCount || weight < 1) {
                throw new AssertionError("Invalid " + planetId + " spawner values for " + matcher.group(1));
            }
            if (!entities.add(matcher.group(1))) {
                throw new AssertionError("Duplicate " + planetId + " spawner for " + matcher.group(1));
            }
        }
        return Set.copyOf(entities);
    }

    private static String requiredStringField(String json, String field, Path source) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Missing field " + field + " in <" + source + ">");
        }
        return matcher.group(1);
    }

    private static String namespaced(String id) {
        String normalized = id.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.contains(":") ? normalized : "galacticwars:" + normalized;
    }

    private static String read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new AssertionError("Required planet ecology resource missing <" + path + ">");
        }
        // Source-contract expectations deliberately use LF. Normalise Windows checkouts so
        // the contract validates source content rather than line-ending configuration.
        return Files.readString(path).replace("\r\n", "\n");
    }

    private static String section(String content, String start, String end) {
        int startIndex = content.indexOf(start);
        int endIndex = content.indexOf(end, startIndex + start.length());
        if (startIndex < 0 || endIndex < 0) {
            throw new AssertionError("Missing source section " + start + " -> " + end);
        }
        return content.substring(startIndex, endIndex);
    }

    private static void assertContains(String content, String expected, String label) {
        if (!content.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void assertNotContains(String content, String forbidden, String label) {
        if (content.contains(forbidden)) {
            throw new AssertionError(label + " contains forbidden <" + forbidden + ">");
        }
    }

    private record PlanetData(String id, String dimensionId, String factionId) {
    }
}
