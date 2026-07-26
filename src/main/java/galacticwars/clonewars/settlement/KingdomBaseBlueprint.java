package galacticwars.clonewars.settlement;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import galacticwars.clonewars.kingdom.BuildProject;
import galacticwars.clonewars.kingdom.StorageEndpoint;

import galacticwars.clonewars.workforce.ResourceInventory;

public record KingdomBaseBlueprint(
        String id,
        String displayName,
        BlueprintAnchor anchor,
        List<Integer> allowedRotations,
        List<BaseBlockPlacement> placements,
        int housingReward,
        int storageSlotReward,
        String worksiteType,
        int worksiteCapacity,
        int commanderSlotReward,
        String templateId,
        Set<BlueprintMode> modes,
        BlueprintTerrainConstraints terrainConstraints,
        Map<String, Integer> constructionCostOverrides,
        Optional<BlueprintWorldgenProfile> worldgen,
        Set<String> compatibleDefinitionHashes,
        Set<String> compatibleContentHashes
) {
    public static final String DEFAULT_NAMESPACE = "galacticwars";
    public static final String STARTER_CAMP_ID = DEFAULT_NAMESPACE + ":starter_camp";
    public static final String STARTER_KEEP_ID = DEFAULT_NAMESPACE + ":forward_base";
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH = Pattern.compile("[a-z0-9/._-]+");
    private static final List<Integer> ALL_ROTATIONS = List.of(0, 90, 180, 270);

    public KingdomBaseBlueprint {
        id = canonicalId(id);
        displayName = requireNonBlank(displayName, "displayName");
        anchor = Objects.requireNonNull(anchor, "anchor");
        allowedRotations = normalizeAllowedRotations(allowedRotations);
        placements = normalizePlacements(placements);
        validateRewards(housingReward, storageSlotReward, worksiteCapacity, commanderSlotReward);
        worksiteType = normalizeWorksiteType(worksiteType, worksiteCapacity);
        validateStorageReward(storageSlotReward, placements);
        templateId = normalizeTemplateId(templateId);
        modes = normalizeModes(modes);
        terrainConstraints = Objects.requireNonNull(terrainConstraints, "terrainConstraints");
        constructionCostOverrides = normalizeCostOverrides(constructionCostOverrides);
        worldgen = Objects.requireNonNull(worldgen, "worldgen");
        compatibleDefinitionHashes = normalizeCompatibleHashes(compatibleDefinitionHashes);
        compatibleContentHashes = normalizeCompatibleHashes(compatibleContentHashes);
        validateWorldgenConfiguration(templateId, modes, worldgen);
    }

    public KingdomBaseBlueprint(
            String id,
            String displayName,
            BlueprintAnchor anchor,
            List<Integer> allowedRotations,
            List<BaseBlockPlacement> placements,
            int housingReward,
            int storageSlotReward,
            String worksiteType,
            int worksiteCapacity,
            int commanderSlotReward
    ) {
        this(id, displayName, anchor, allowedRotations, placements, housingReward, storageSlotReward,
                worksiteType, worksiteCapacity, commanderSlotReward, "", Set.of(BlueprintMode.CONSTRUCTION),
                BlueprintTerrainConstraints.DEFAULT, Map.of(), Optional.empty(), Set.of(), Set.of());
    }

    public KingdomBaseBlueprint(
            String id,
            String displayName,
            List<BaseBlockPlacement> placements,
            int housingReward,
            int storageSlotReward,
            String worksiteType,
            int worksiteCapacity,
            int commanderSlotReward
    ) {
        this(id, displayName, BlueprintAnchor.ORIGIN, ALL_ROTATIONS, placements,
                housingReward, storageSlotReward, worksiteType, worksiteCapacity, commanderSlotReward);
    }

    private static final List<KingdomBaseBlueprint> ALL_BLUEPRINTS = List.of(
            starterCamp(),
            starterKeep(),
            barracks(),
            supply_depot(),
            farmPlot(),
            lumberCamp(),
            mineSite());

    public static KingdomBaseBlueprint starterKeep() {
        ArrayList<BaseBlockPlacement> placements = new ArrayList<>();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                placements.add(new BaseBlockPlacement(
                        x,
                        0,
                        z,
                        "galacticwars:duracrete",
                        "galacticwars:duracrete"));
            }
        }
        placements.add(new BaseBlockPlacement(0, 1, 0, "galacticwars:nightsister_weave_log", "galacticwars:nightsister_weave_log"));
        placements.add(new BaseBlockPlacement(4, 1, 0, "galacticwars:nightsister_weave_log", "galacticwars:nightsister_weave_log"));
        placements.add(new BaseBlockPlacement(0, 1, 4, "galacticwars:nightsister_weave_log", "galacticwars:nightsister_weave_log"));
        placements.add(new BaseBlockPlacement(4, 1, 4, "galacticwars:nightsister_weave_log", "galacticwars:nightsister_weave_log"));
        placements.add(new BaseBlockPlacement(2, 1, 0, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(2, 1, 4, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(0, 1, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(4, 1, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        return new KingdomBaseBlueprint(STARTER_KEEP_ID, "Forward Base", placements, 2, 0, "", 0, 1);
    }

    public static KingdomBaseBlueprint starterCamp() {
        List<BaseBlockPlacement> placements = List.of(
                new BaseBlockPlacement(-2, 0, -2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(2, 0, -2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(-2, 0, 2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(2, 0, 2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(-2, 1, -2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(2, 1, -2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(-2, 1, 2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(2, 1, 2, "minecraft:oak_log", "minecraft:oak_log"),
                new BaseBlockPlacement(-1, 0, -2, "minecraft:oak_planks", "minecraft:oak_planks"),
                new BaseBlockPlacement(0, 0, -2, "minecraft:oak_planks", "minecraft:oak_planks"),
                new BaseBlockPlacement(1, 0, -2, "minecraft:oak_planks", "minecraft:oak_planks"),
                new BaseBlockPlacement(-1, 0, 2, "minecraft:oak_planks", "minecraft:oak_planks"),
                new BaseBlockPlacement(0, 0, 2, "minecraft:campfire", "minecraft:campfire"),
                new BaseBlockPlacement(1, 0, 2, "minecraft:oak_planks", "minecraft:oak_planks"),
                new BaseBlockPlacement(-2, 0, 0, "minecraft:crafting_table", "minecraft:crafting_table"),
                new BaseBlockPlacement(2, 0, 0, "minecraft:chest", "minecraft:chest"));
        return new KingdomBaseBlueprint(
                STARTER_CAMP_ID, "Starter Camp", BlueprintAnchor.ORIGIN, ALL_ROTATIONS,
                placements, 2, 27, "", 0, 1);
    }

    public static KingdomBaseBlueprint barracks() {
        ArrayList<BaseBlockPlacement> placements = floor(3, 3, "minecraft:oak_planks", "minecraft:oak_planks");
        addCornerPosts(placements, 2, 2, 2);
        placements.add(new BaseBlockPlacement(1, 1, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(1, 2, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        return new KingdomBaseBlueprint("barracks", "Starter Barracks", placements, 4, 0, "", 0, 0);
    }

    public static KingdomBaseBlueprint supply_depot() {
        ArrayList<BaseBlockPlacement> placements = floor(4, 3, "galacticwars:duracrete",
                "galacticwars:duracrete");
        addCornerPosts(placements, 3, 2, 2);
        placements.add(new BaseBlockPlacement(1, 1, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(2, 1, 2, "minecraft:oak_planks", "minecraft:oak_planks"));
        placements.add(new BaseBlockPlacement(1, 1, 1, "minecraft:chest", "minecraft:chest"));
        placements.add(new BaseBlockPlacement(2, 1, 1, "minecraft:chest", "minecraft:chest"));
        return new KingdomBaseBlueprint("supply_depot", "Supply Depot", placements, 0, 54, "courier", 2, 0);
    }

    public static KingdomBaseBlueprint farmPlot() {
        ArrayList<BaseBlockPlacement> placements = floor(5, 5, "minecraft:dirt", "minecraft:dirt");
        for (int x = 0; x < 5; x++) {
            placements.add(new BaseBlockPlacement(x, 1, 0, "minecraft:oak_log", "minecraft:oak_log"));
            placements.add(new BaseBlockPlacement(x, 1, 4, "minecraft:oak_log", "minecraft:oak_log"));
        }
        return new KingdomBaseBlueprint("moisture_farm", "Moisture Farm", placements, 0, 0, "farmer", 2, 0);
    }

    public static KingdomBaseBlueprint lumberCamp() {
        ArrayList<BaseBlockPlacement> placements = floor(3, 3, "minecraft:oak_planks", "minecraft:oak_planks");
        placements.add(new BaseBlockPlacement(0, 1, 0, "minecraft:oak_log", "minecraft:oak_log"));
        placements.add(new BaseBlockPlacement(2, 1, 0, "minecraft:oak_log", "minecraft:oak_log"));
        placements.add(new BaseBlockPlacement(1, 1, 2, "minecraft:oak_log", "minecraft:oak_log"));
        return new KingdomBaseBlueprint("salvage_yard", "Salvage Yard", placements, 0, 0, "lumberjack", 2, 0);
    }

    public static KingdomBaseBlueprint mineSite() {
        ArrayList<BaseBlockPlacement> placements = floor(3, 2, "galacticwars:duracrete",
                "galacticwars:duracrete");
        placements.add(new BaseBlockPlacement(0, 1, 1, "galacticwars:duracrete",
                "galacticwars:duracrete"));
        placements.add(new BaseBlockPlacement(2, 1, 1, "galacticwars:duracrete",
                "galacticwars:duracrete"));
        placements.add(new BaseBlockPlacement(1, 2, 1, "galacticwars:duracrete",
                "galacticwars:duracrete"));
        return new KingdomBaseBlueprint("mine", "Mine", placements, 0, 0, "miner", 2, 0);
    }

    public static List<KingdomBaseBlueprint> all() {
        return ALL_BLUEPRINTS;
    }

    public static List<KingdomBaseBlueprint> builtIns() {
        return ALL_BLUEPRINTS;
    }

    public static Optional<KingdomBaseBlueprint> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String normalizedId;
        try {
            normalizedId = canonicalId(id);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        return all().stream().filter(blueprint -> blueprint.id().equals(normalizedId)).findFirst();
    }

    public String definitionHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "kingdom-blueprint-v2");
            update(digest, id);
            update(digest, anchor.x());
            update(digest, anchor.y());
            update(digest, anchor.z());
            update(digest, allowedRotations.size());
            for (int rotation : allowedRotations) {
                update(digest, rotation);
            }
            update(digest, placements.size());
            for (BaseBlockPlacement placement : placements) {
                updatePlacementCore(digest, placement);
                updatePlacementPropertiesAsString(digest, placement);
            }
            update(digest, housingReward);
            update(digest, storageSlotReward);
            update(digest, worksiteType);
            update(digest, worksiteCapacity);
            update(digest, commanderSlotReward);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public boolean matchesDefinitionHash(String hash) {
        return definitionHash().equals(hash) || compatibleDefinitionHashes.contains(hash);
    }

    public boolean matchesContentHash(String hash) {
        return contentHash().equals(hash) || compatibleContentHashes.contains(hash);
    }

    /** Hashes the complete descriptor and parsed template contents for persisted worldgen pieces. */
    public String contentHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "galacticwars-blueprint-content-v2");
            update(digest, id);
            update(digest, displayName);
            update(digest, templateId);
            modes.stream().map(Enum::name).sorted().forEach(value -> update(digest, value));
            update(digest, anchor.x());
            update(digest, anchor.y());
            update(digest, anchor.z());
            allowedRotations.forEach(value -> update(digest, value));
            for (BaseBlockPlacement placement : placements) {
                updatePlacementCore(digest, placement);
                updatePlacementPropertiesAsPairs(digest, placement);
            }
            update(digest, housingReward);
            update(digest, storageSlotReward);
            update(digest, worksiteType);
            update(digest, worksiteCapacity);
            update(digest, commanderSlotReward);
            update(digest, terrainConstraints.maxSlope());
            update(digest, terrainConstraints.minY());
            update(digest, terrainConstraints.maxY());
            new TreeMap<>(constructionCostOverrides).forEach((key, value) -> {
                update(digest, key);
                update(digest, value);
            });
            worldgen.ifPresent(profile -> {
                profile.biomes().stream().sorted().forEach(value -> update(digest, value));
                update(digest, profile.factionId());
                update(digest, profile.siteKind().id());
                update(digest, profile.siteRadius());
                for (BlueprintRosterEntry entry : profile.roster()) {
                    update(digest, entry.entityTypeId());
                    update(digest, entry.minimum());
                    update(digest, entry.maximum());
                    update(digest, entry.weight());
                    update(digest, entry.serviceBranch());
                    update(digest, entry.role());
                }
                new TreeMap<>(profile.lootTables()).forEach((marker, table) -> {
                    update(digest, marker);
                    update(digest, table);
                });
                update(digest, profile.placementWeight());
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public BaseBlockPlacement rotatedPlacement(int index, int rotationSteps) {
        requireRotationSteps(rotationSteps);
        BaseBlockPlacement placement = placements.get(index);
        int relativeX = placement.x() - anchor.x();
        int relativeY = placement.y() - anchor.y();
        int relativeZ = placement.z() - anchor.z();
        return switch (rotationSteps) {
            case 1 -> new BaseBlockPlacement(-relativeZ, relativeY, relativeX,
                    placement.blockId(), placement.itemId(), placement.rotatedProperties(rotationSteps));
            case 2 -> new BaseBlockPlacement(-relativeX, relativeY, -relativeZ,
                    placement.blockId(), placement.itemId(), placement.rotatedProperties(rotationSteps));
            case 3 -> new BaseBlockPlacement(relativeZ, relativeY, -relativeX,
                    placement.blockId(), placement.itemId(), placement.rotatedProperties(rotationSteps));
            default -> new BaseBlockPlacement(relativeX, relativeY, relativeZ,
                    placement.blockId(), placement.itemId(), placement.rotatedProperties(rotationSteps));
        };
    }

    public boolean supportsRotationSteps(int rotationSteps) {
        return rotationSteps >= 0 && rotationSteps < 4 && allowedRotations.contains(rotationSteps * 90);
    }

    public void requireRotationSteps(int rotationSteps) {
        if (!supportsRotationSteps(rotationSteps)) {
            throw new IllegalArgumentException("rotation " + rotationSteps * 90 + " is not allowed for " + id);
        }
    }

    public List<StorageEndpoint> storageEndpoints(BuildProject project) {
        if (storageSlotReward <= 0) {
            return List.of();
        }
        List<BaseBlockPlacement> containers = java.util.stream.IntStream.range(0, placements.size())
                .mapToObj(index -> rotatedPlacement(index, project.rotationSteps()))
                .filter(placement -> placement.blockId().equals("minecraft:chest"))
                .toList();
        if (containers.isEmpty()) {
            return List.of();
        }
        int slotsPerEndpoint = storageSlotReward / containers.size();
        int remainder = storageSlotReward % containers.size();
        ArrayList<StorageEndpoint> endpoints = new ArrayList<>(containers.size());
        for (int index = 0; index < containers.size(); index++) {
            BaseBlockPlacement placement = containers.get(index);
            endpoints.add(new StorageEndpoint(
                    project.dimensionId(),
                    project.originX() + placement.x(),
                    project.originY() + placement.y(),
                    project.originZ() + placement.z(),
                    slotsPerEndpoint + (index < remainder ? 1 : 0)));
        }
        return List.copyOf(endpoints);
    }

    private static ArrayList<BaseBlockPlacement> floor(int width, int depth, String blockId, String itemId) {
        ArrayList<BaseBlockPlacement> placements = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                placements.add(new BaseBlockPlacement(x, 0, z, blockId, itemId));
            }
        }
        return placements;
    }

    private static void addCornerPosts(ArrayList<BaseBlockPlacement> placements, int maxX, int maxZ, int height) {
        for (int y = 1; y <= height; y++) {
            placements.add(new BaseBlockPlacement(0, y, 0, "minecraft:oak_log", "minecraft:oak_log"));
            placements.add(new BaseBlockPlacement(maxX, y, 0, "minecraft:oak_log", "minecraft:oak_log"));
            placements.add(new BaseBlockPlacement(0, y, maxZ, "minecraft:oak_log", "minecraft:oak_log"));
            placements.add(new BaseBlockPlacement(maxX, y, maxZ, "minecraft:oak_log", "minecraft:oak_log"));
        }
    }

    public ResourceInventory requiredResources() {
        ResourceInventory inventory = ResourceInventory.empty();
        if (!constructionCostOverrides.isEmpty()) {
            for (Map.Entry<String, Integer> entry : constructionCostOverrides.entrySet()) {
                inventory = inventory.withAdded(entry.getKey(), entry.getValue());
            }
            return inventory;
        }
        for (BaseBlockPlacement placement : placements) {
            inventory = inventory.withAdded(placement.itemId(), 1);
        }
        return inventory;
    }

    private static String requireNonBlank(String value, String label) {
        Objects.requireNonNull(value, label);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        return trimmed;
    }

    private static List<BaseBlockPlacement> normalizePlacements(List<BaseBlockPlacement> placements) {
        List<BaseBlockPlacement> copy = List.copyOf(Objects.requireNonNull(placements, "placements"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("placements cannot be empty");
        }
        HashSet<String> occupiedPositions = new HashSet<>();
        for (BaseBlockPlacement placement : copy) {
            Objects.requireNonNull(placement, "placement");
            String position = placement.x() + ":" + placement.y() + ":" + placement.z();
            if (!occupiedPositions.add(position)) {
                throw new IllegalArgumentException("duplicate blueprint placement at " + position);
            }
        }
        return copy;
    }

    private static void validateRewards(
            int housingReward, int storageSlotReward, int worksiteCapacity, int commanderSlotReward
    ) {
        if (housingReward < 0 || storageSlotReward < 0 || worksiteCapacity < 0 || commanderSlotReward < 0) {
            throw new IllegalArgumentException("blueprint rewards cannot be negative");
        }
    }

    private static String normalizeWorksiteType(String worksiteType, int worksiteCapacity) {
        String normalized = worksiteType == null ? "" : worksiteType.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() != (worksiteCapacity == 0)) {
            throw new IllegalArgumentException("worksite type and capacity must be defined together");
        }
        return normalized;
    }

    private static void validateStorageReward(int storageSlotReward, List<BaseBlockPlacement> placements) {
        if (storageSlotReward <= 0) {
            return;
        }
        long containerCount = placements.stream()
                .filter(placement -> placement.blockId().equals("minecraft:chest"))
                .count();
        if (containerCount == 0 || storageSlotReward < containerCount) {
            throw new IllegalArgumentException("storage reward requires enough chest placements");
        }
    }

    private static String normalizeTemplateId(String templateId) {
        return templateId == null || templateId.isBlank() ? "" : canonicalId(templateId);
    }

    private static Set<BlueprintMode> normalizeModes(Set<BlueprintMode> modes) {
        Set<BlueprintMode> copy = Set.copyOf(Objects.requireNonNull(modes, "modes"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("blueprint modes cannot be empty");
        }
        return copy;
    }

    private static Map<String, Integer> normalizeCostOverrides(Map<String, Integer> overrides) {
        return Map.copyOf(Objects.requireNonNull(overrides, "constructionCostOverrides"));
    }

    private static Set<String> normalizeCompatibleHashes(Set<String> hashes) {
        Set<String> copy = Set.copyOf(Objects.requireNonNull(hashes, "compatibleDefinitionHashes"));
        if (copy.stream().anyMatch(hash -> !hash.matches("[0-9a-f]{64}"))) {
            throw new IllegalArgumentException("compatible definition hashes must be lowercase SHA-256 values");
        }
        return copy;
    }

    private static void validateWorldgenConfiguration(
            String templateId, Set<BlueprintMode> modes, Optional<BlueprintWorldgenProfile> worldgen
    ) {
        boolean worldgenMode = modes.contains(BlueprintMode.WORLDGEN);
        if (worldgenMode != worldgen.isPresent()) {
            throw new IllegalArgumentException("worldgen mode and profile must be defined together");
        }
        if (worldgenMode && templateId.isBlank()) {
            throw new IllegalArgumentException("worldgen blueprints require a template");
        }
    }

    public static String canonicalId(String value) {
        String normalized = requireNonBlank(value, "id").toLowerCase(Locale.ROOT);
        if (normalized.indexOf(':') < 0) {
            normalized = DEFAULT_NAMESPACE + ":" + normalized;
        }
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator != normalized.lastIndexOf(':')
                || !NAMESPACE.matcher(normalized.substring(0, separator)).matches()
                || !PATH.matcher(normalized.substring(separator + 1)).matches()) {
            throw new IllegalArgumentException("invalid namespaced blueprint id " + value);
        }
        return normalized;
    }

    public static String path(String id) {
        String canonical = canonicalId(id);
        return canonical.substring(canonical.indexOf(':') + 1);
    }

    private static List<Integer> normalizeAllowedRotations(List<Integer> rotations) {
        Objects.requireNonNull(rotations, "allowedRotations");
        TreeSet<Integer> normalized = new TreeSet<>();
        for (Integer rotation : rotations) {
            if (rotation == null || rotation < 0 || rotation > 270 || rotation % 90 != 0) {
                throw new IllegalArgumentException("allowed rotations must be 0, 90, 180, or 270 degrees");
            }
            normalized.add(rotation);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("at least one rotation must be allowed");
        }
        return List.copyOf(normalized);
    }

    private static void updatePlacementCore(MessageDigest digest, BaseBlockPlacement placement) {
        update(digest, placement.x());
        update(digest, placement.y());
        update(digest, placement.z());
        update(digest, placement.blockId());
        update(digest, placement.itemId());
    }

    private static void updatePlacementPropertiesAsString(MessageDigest digest, BaseBlockPlacement placement) {
        if (!placement.properties().isEmpty()) {
            update(digest, new TreeMap<>(placement.properties()).toString());
        }
    }

    private static void updatePlacementPropertiesAsPairs(MessageDigest digest, BaseBlockPlacement placement) {
        new TreeMap<>(placement.properties()).forEach((key, value) -> {
            update(digest, key);
            update(digest, value);
        });
    }

    private static void update(MessageDigest digest, int value) {
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
    }

    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        update(digest, bytes.length);
        digest.update(bytes);
    }
}
