package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.data.GameplayDataSnapshot;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Bounded client projection of server-owned datapack content needed by gameplay UI.
 * The full authoritative definitions remain server-side.
 */
public record GameplayCatalogPayload(
        long generation,
        String contentHash,
        List<ClassEntry> classes,
        List<VehicleEntry> vehicles,
        List<BlueprintEntry> blueprints,
        List<TechnologyEntry> technology,
        List<FabricationEntry> fabrication,
        List<RelationPolicyEntry> relationPolicies
) implements CustomPacketPayload {
    public static final int MAX_CLASSES = 128;
    public static final int MAX_VEHICLES = 64;
    public static final int MAX_BLUEPRINTS = 128;
    public static final int MAX_TECHNOLOGY = 256;
    public static final int MAX_FABRICATION = 256;
    public static final int MAX_RELATION_POLICIES = 16;
    public static final int MAX_ABILITIES_PER_CLASS = 8;
    public static final int MAX_REQUIREMENTS_PER_CLASS = 16;
    public static final int MAX_TEXT_BYTES = 192;
    public static final int MAX_REQUIREMENT_AMOUNT = 1_000_000;
    public static final int MAX_VEHICLE_STAT = 1_000_000;
    public static final int MAX_BLUEPRINT_PLACEMENTS = 16_384;
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.:/-]+");

    public static final Type<GameplayCatalogPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "gameplay_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GameplayCatalogPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeVarLong(payload.generation());
                        buffer.writeUtf(payload.contentHash(), 64);
                        writeList(buffer, payload.classes(), MAX_CLASSES, ClassEntry::write);
                        writeList(buffer, payload.vehicles(), MAX_VEHICLES, VehicleEntry::write);
                        writeList(buffer, payload.blueprints(), MAX_BLUEPRINTS, BlueprintEntry::write);
                        writeList(buffer, payload.technology(), MAX_TECHNOLOGY, TechnologyEntry::write);
                        writeList(buffer, payload.fabrication(), MAX_FABRICATION, FabricationEntry::write);
                        writeList(buffer, payload.relationPolicies(), MAX_RELATION_POLICIES,
                                RelationPolicyEntry::write);
                    },
                    buffer -> new GameplayCatalogPayload(
                            buffer.readVarLong(),
                            buffer.readUtf(64),
                            readList(buffer, MAX_CLASSES, ClassEntry::read),
                            readList(buffer, MAX_VEHICLES, VehicleEntry::read),
                            readList(buffer, MAX_BLUEPRINTS, BlueprintEntry::read),
                            readList(buffer, MAX_TECHNOLOGY, TechnologyEntry::read),
                            readList(buffer, MAX_FABRICATION, FabricationEntry::read),
                            readList(buffer, MAX_RELATION_POLICIES, RelationPolicyEntry::read)));

    public GameplayCatalogPayload {
        if (generation < 0L) {
            throw new IllegalArgumentException("gameplay catalog generation cannot be negative");
        }
        contentHash = Objects.requireNonNull(contentHash, "contentHash").trim().toLowerCase(Locale.ROOT);
        if ((generation == 0L && !contentHash.isEmpty())
                || (generation > 0L && !contentHash.matches("[0-9a-f]{64}"))) {
            throw new IllegalArgumentException("gameplay catalog content hash is invalid");
        }
        classes = boundedCopy(classes, MAX_CLASSES, "classes");
        vehicles = boundedCopy(vehicles, MAX_VEHICLES, "vehicles");
        blueprints = boundedCopy(blueprints, MAX_BLUEPRINTS, "blueprints");
        technology = boundedCopy(technology, MAX_TECHNOLOGY, "technology");
        fabrication = boundedCopy(fabrication, MAX_FABRICATION, "fabrication");
        relationPolicies = boundedCopy(
                relationPolicies, MAX_RELATION_POLICIES, "relationPolicies");
        requireUnique(classes.stream().map(ClassEntry::classId).toList(), "class id");
        requireUnique(vehicles.stream().map(VehicleEntry::vehicleId).toList(), "vehicle id");
        requireUnique(blueprints.stream().map(BlueprintEntry::blueprintId).toList(), "blueprint id");
        requireUnique(technology.stream().map(TechnologyEntry::nodeId).toList(), "technology id");
        requireUnique(fabrication.stream().map(FabricationEntry::recipeId).toList(), "fabrication id");
        requireUnique(relationPolicies.stream().map(RelationPolicyEntry::factionId).toList(),
                "relation policy faction");
    }

    public GameplayCatalogPayload(
            long generation,
            String contentHash,
            List<ClassEntry> classes,
            List<VehicleEntry> vehicles,
            List<BlueprintEntry> blueprints
    ) {
        this(generation, contentHash, classes, vehicles, blueprints, List.of(), List.of(), List.of());
    }

    public static GameplayCatalogPayload fromSnapshot(
            GameplayDataSnapshot snapshot,
            long generation,
            String contentHash
    ) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<ClassEntry> classes = snapshot.unitClasses().values().stream()
                .filter(definition -> definition.playerAssignable())
                .sorted(java.util.Comparator.comparing(definition -> definition.id().toString()))
                .map(definition -> new ClassEntry(
                        definition.id().toString(),
                        definition.displayName(),
                        definition.factionId().toString(),
                        definition.forcePathSlot(),
                        definition.requirements().stream()
                                .map(requirement -> new RequirementEntry(
                                        requirement.type(),
                                        requirement.subjectId(),
                                        requirement.amount()))
                                .toList(),
                        definition.abilityIds().stream()
                                .map(snapshot.abilities()::get)
                                .filter(Objects::nonNull)
                                .filter(ability -> ability.enabled())
                                .map(ability -> ability.displayName())
                                .toList()))
                .toList();
        List<VehicleEntry> vehicles = snapshot.launchContent().vehicles().values().stream()
                .sorted(java.util.Comparator.comparing(
                        LaunchContentDefinitions.VehicleDefinition::id))
                .map(definition -> new VehicleEntry(
                        definition.id(), definition.maxHealth(), definition.fuelCapacity()))
                .toList();
        List<BlueprintEntry> blueprints = snapshot.blueprints().values().stream()
                .sorted(java.util.Comparator.comparing(
                        galacticwars.clonewars.settlement.KingdomBaseBlueprint::id))
                .map(definition -> new BlueprintEntry(
                        definition.id(), definition.displayName(), definition.placements().size()))
                .toList();
        List<TechnologyEntry> technology = snapshot.technology().nodes().values().stream()
                .sorted(java.util.Comparator.comparing(
                        galacticwars.clonewars.technology.TechnologyNodeDefinition::id))
                .map(node -> new TechnologyEntry(
                        node.id(),
                        node.factionId(),
                        node.displayName(),
                        node.prerequisites().stream().sorted().toList(),
                        node.requiredInputs().entrySet().stream()
                                .sorted(java.util.Map.Entry.comparingByKey())
                                .map(entry -> new CostEntry(entry.getKey(), entry.getValue()))
                                .toList(),
                        node.requiredWork(),
                        node.recipeIds().stream().sorted().toList()))
                .toList();
        List<FabricationEntry> fabrication = snapshot.technology().nodes().values().stream()
                .flatMap(node -> node.recipeIds().stream().map(recipe -> new FabricationEntry(
                        recipe,
                        node.factionId(),
                        node.id(),
                        galacticwars.clonewars.progression.GameplayAccessResolver.EXPORTABLE_RECIPES
                                .contains(recipe))))
                .sorted(java.util.Comparator.comparing(FabricationEntry::recipeId))
                .toList();
        List<RelationPolicyEntry> relationPolicies = snapshot.factions().definitions().values().stream()
                .sorted(java.util.Comparator.comparing(faction -> faction.id().toString()))
                .map(faction -> {
                    var profile = snapshot.npcAiProfile(faction.id())
                            .orElseGet(() -> galacticwars.clonewars.faction.ai.NpcAiProfile.defaults(
                                    faction.id()));
                    return new RelationPolicyEntry(
                            faction.id().toString(),
                            profile.friendlyThreshold(),
                            profile.neutralThreshold(),
                            profile.waryThreshold(),
                            profile.friendlyTradePricePercent(),
                            faction.minimumHiringAlignment());
                })
                .toList();
        return new GameplayCatalogPayload(
                generation, contentHash, classes, vehicles, blueprints, technology, fabrication,
                relationPolicies);
    }

    @Override
    public Type<GameplayCatalogPayload> type() {
        return TYPE;
    }

    public record ClassEntry(
            String classId,
            String displayName,
            String factionId,
            String forcePathSlot,
            List<RequirementEntry> requirements,
            List<String> abilityDisplayNames
    ) {
        public ClassEntry {
            classId = identifier(classId, "classId");
            displayName = display(displayName, "displayName");
            factionId = identifier(factionId, "factionId");
            forcePathSlot = Objects.requireNonNull(forcePathSlot, "forcePathSlot")
                    .trim().toLowerCase(Locale.ROOT);
            if (forcePathSlot.equals("light")) {
                forcePathSlot = "jedi";
            } else if (forcePathSlot.equals("dark")) {
                forcePathSlot = "nightsister";
            }
            if (!forcePathSlot.isEmpty()
                    && !Set.of("jedi", "sith", "nightsister").contains(forcePathSlot)) {
                throw new IllegalArgumentException("invalid Force tradition slot " + forcePathSlot);
            }
            requirements = boundedCopy(
                    requirements, MAX_REQUIREMENTS_PER_CLASS, "class requirements");
            abilityDisplayNames = boundedCopy(
                    abilityDisplayNames, MAX_ABILITIES_PER_CLASS, "class abilities").stream()
                    .map(value -> display(value, "ability display name"))
                    .toList();
        }

        private static void write(RegistryFriendlyByteBuf buffer, ClassEntry value) {
            writeText(buffer, value.classId());
            writeText(buffer, value.displayName());
            writeText(buffer, value.factionId());
            writeText(buffer, value.forcePathSlot());
            writeList(buffer, value.requirements(), MAX_REQUIREMENTS_PER_CLASS,
                    RequirementEntry::write);
            writeList(buffer, value.abilityDisplayNames(), MAX_ABILITIES_PER_CLASS,
                    GameplayCatalogPayload::writeText);
        }

        private static ClassEntry read(RegistryFriendlyByteBuf buffer) {
            return new ClassEntry(
                    readText(buffer),
                    readText(buffer),
                    readText(buffer),
                    readText(buffer),
                    readList(buffer, MAX_REQUIREMENTS_PER_CLASS, RequirementEntry::read),
                    readList(buffer, MAX_ABILITIES_PER_CLASS, GameplayCatalogPayload::readText));
        }
    }

    public record RequirementEntry(String type, String subjectId, int amount) {
        public RequirementEntry {
            type = identifier(type, "requirement type");
            subjectId = identifier(subjectId, "requirement subject");
            if (amount < 1 || amount > MAX_REQUIREMENT_AMOUNT) {
                throw new IllegalArgumentException("requirement amount is outside the supported range");
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, RequirementEntry value) {
            writeText(buffer, value.type());
            writeText(buffer, value.subjectId());
            buffer.writeVarInt(value.amount());
        }

        private static RequirementEntry read(RegistryFriendlyByteBuf buffer) {
            return new RequirementEntry(readText(buffer), readText(buffer), buffer.readVarInt());
        }
    }

    public record VehicleEntry(String vehicleId, int maxHealth, int fuelCapacity) {
        public VehicleEntry {
            vehicleId = identifier(vehicleId, "vehicleId");
            if (maxHealth < 1 || maxHealth > MAX_VEHICLE_STAT
                    || fuelCapacity < 1 || fuelCapacity > MAX_VEHICLE_STAT) {
                throw new IllegalArgumentException("vehicle HUD maxima are outside the supported range");
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, VehicleEntry value) {
            writeText(buffer, value.vehicleId());
            buffer.writeVarInt(value.maxHealth());
            buffer.writeVarInt(value.fuelCapacity());
        }

        private static VehicleEntry read(RegistryFriendlyByteBuf buffer) {
            return new VehicleEntry(readText(buffer), buffer.readVarInt(), buffer.readVarInt());
        }
    }

    public record BlueprintEntry(String blueprintId, String displayName, int placementCount) {
        public BlueprintEntry {
            blueprintId = identifier(blueprintId, "blueprintId");
            displayName = display(displayName, "blueprint display name");
            if (placementCount < 1 || placementCount > MAX_BLUEPRINT_PLACEMENTS) {
                throw new IllegalArgumentException("blueprint placement count is outside the supported range");
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, BlueprintEntry value) {
            writeText(buffer, value.blueprintId());
            writeText(buffer, value.displayName());
            buffer.writeVarInt(value.placementCount());
        }

        private static BlueprintEntry read(RegistryFriendlyByteBuf buffer) {
            return new BlueprintEntry(readText(buffer), readText(buffer), buffer.readVarInt());
        }
    }

    public record CostEntry(String itemId, int count) {
        public CostEntry {
            itemId = identifier(itemId, "technology cost item");
            if (count < 1 || count > 4_096) {
                throw new IllegalArgumentException("technology cost is outside its bound");
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, CostEntry value) {
            writeText(buffer, value.itemId());
            buffer.writeVarInt(value.count());
        }

        private static CostEntry read(RegistryFriendlyByteBuf buffer) {
            return new CostEntry(readText(buffer), buffer.readVarInt());
        }
    }

    public record TechnologyEntry(
            String nodeId,
            String factionId,
            String displayName,
            List<String> prerequisites,
            List<CostEntry> costs,
            int requiredWork,
            List<String> recipeIds
    ) {
        public TechnologyEntry {
            nodeId = identifier(nodeId, "technology node");
            factionId = identifier(factionId, "technology faction");
            displayName = display(displayName, "technology display name");
            prerequisites = boundedCopy(prerequisites, 16, "technology prerequisites").stream()
                    .map(value -> identifier(value, "technology prerequisite")).toList();
            costs = boundedCopy(costs, 16, "technology costs");
            recipeIds = boundedCopy(recipeIds, 64, "technology recipes").stream()
                    .map(value -> identifier(value, "technology recipe")).toList();
            if (requiredWork < 20 || requiredWork > 720_000) {
                throw new IllegalArgumentException("technology work is outside its bound");
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, TechnologyEntry value) {
            writeText(buffer, value.nodeId());
            writeText(buffer, value.factionId());
            writeText(buffer, value.displayName());
            writeList(buffer, value.prerequisites(), 16, GameplayCatalogPayload::writeText);
            writeList(buffer, value.costs(), 16, CostEntry::write);
            buffer.writeVarInt(value.requiredWork());
            writeList(buffer, value.recipeIds(), 64, GameplayCatalogPayload::writeText);
        }

        private static TechnologyEntry read(RegistryFriendlyByteBuf buffer) {
            return new TechnologyEntry(
                    readText(buffer),
                    readText(buffer),
                    readText(buffer),
                    readList(buffer, 16, GameplayCatalogPayload::readText),
                    readList(buffer, 16, CostEntry::read),
                    buffer.readVarInt(),
                    readList(buffer, 64, GameplayCatalogPayload::readText));
        }
    }

    public record FabricationEntry(
            String recipeId,
            String factionId,
            String technologyId,
            boolean exportable
    ) {
        public FabricationEntry {
            recipeId = identifier(recipeId, "fabrication recipe");
            factionId = identifier(factionId, "fabrication faction");
            technologyId = identifier(technologyId, "fabrication technology");
        }

        private static void write(RegistryFriendlyByteBuf buffer, FabricationEntry value) {
            writeText(buffer, value.recipeId());
            writeText(buffer, value.factionId());
            writeText(buffer, value.technologyId());
            buffer.writeBoolean(value.exportable());
        }

        private static FabricationEntry read(RegistryFriendlyByteBuf buffer) {
            return new FabricationEntry(
                    readText(buffer), readText(buffer), readText(buffer), buffer.readBoolean());
        }
    }

    public record RelationPolicyEntry(
            String factionId,
            int friendlyThreshold,
            int neutralThreshold,
            int waryThreshold,
            int friendlyTradePricePercent,
            int recruitmentThreshold
    ) {
        public RelationPolicyEntry {
            factionId = identifier(factionId, "relation faction");
            if (friendlyThreshold > 100 || waryThreshold < -100
                    || friendlyThreshold <= neutralThreshold || neutralThreshold <= waryThreshold
                    || friendlyTradePricePercent < 1 || friendlyTradePricePercent > 100
                    || recruitmentThreshold < -100 || recruitmentThreshold > 100) {
                throw new IllegalArgumentException("Invalid relation policy for " + factionId);
            }
        }

        private static void write(RegistryFriendlyByteBuf buffer, RelationPolicyEntry value) {
            writeText(buffer, value.factionId());
            buffer.writeVarInt(value.friendlyThreshold());
            buffer.writeVarInt(value.neutralThreshold());
            buffer.writeVarInt(value.waryThreshold());
            buffer.writeVarInt(value.friendlyTradePricePercent());
            buffer.writeVarInt(value.recruitmentThreshold());
        }

        private static RelationPolicyEntry read(RegistryFriendlyByteBuf buffer) {
            return new RelationPolicyEntry(
                    readText(buffer),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt());
        }
    }

    private static String identifier(String value, String label) {
        String normalized = Objects.requireNonNull(value, label).trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || !IDENTIFIER.matcher(normalized).matches()) {
            throw new IllegalArgumentException(label + " is not a valid identifier");
        }
        requireUtf8Bound(normalized, label);
        return normalized;
    }

    private static String display(String value, String label) {
        String normalized = Objects.requireNonNull(value, label).trim();
        if (normalized.isEmpty() || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(label + " is not safe display text");
        }
        requireUtf8Bound(normalized, label);
        return normalized;
    }

    private static void requireUtf8Bound(String value, String label) {
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_TEXT_BYTES) {
            throw new IllegalArgumentException(label + " exceeds the payload text bound");
        }
    }

    private static void writeText(RegistryFriendlyByteBuf buffer, String value) {
        buffer.writeUtf(value, MAX_TEXT_BYTES);
    }

    private static String readText(RegistryFriendlyByteBuf buffer) {
        return buffer.readUtf(MAX_TEXT_BYTES);
    }

    private static <T> void writeList(
            RegistryFriendlyByteBuf buffer,
            List<T> values,
            int maximum,
            BiConsumer<RegistryFriendlyByteBuf, T> writer
    ) {
        if (values.size() > maximum) {
            throw new IllegalArgumentException("payload list exceeds its entry bound");
        }
        buffer.writeVarInt(values.size());
        values.forEach(value -> writer.accept(buffer, value));
    }

    private static <T> List<T> readList(
            RegistryFriendlyByteBuf buffer,
            int maximum,
            Function<RegistryFriendlyByteBuf, T> reader
    ) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("payload list size is outside its entry bound");
        }
        ArrayList<T> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(reader.apply(buffer));
        }
        return List.copyOf(values);
    }

    private static <T> List<T> boundedCopy(List<T> values, int maximum, String label) {
        List<T> copy = List.copyOf(Objects.requireNonNull(values, label));
        if (copy.size() > maximum) {
            throw new IllegalArgumentException(label + " exceeds " + maximum + " entries");
        }
        return copy;
    }

    private static void requireUnique(List<String> values, String label) {
        if (new HashSet<>(values).size() != values.size()) {
            throw new IllegalArgumentException("duplicate " + label + " in gameplay catalog payload");
        }
    }
}
