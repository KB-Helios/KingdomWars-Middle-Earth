package galacticwars.clonewars.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import galacticwars.clonewars.army.ArmyUnitCatalog;
import galacticwars.clonewars.army.ArmyUnitDefinition;
import galacticwars.clonewars.army.ArmyUnitId;
import galacticwars.clonewars.ability.AbilityDefinition;
import galacticwars.clonewars.ability.AbilityId;
import galacticwars.clonewars.classes.UnitClassDefinition;
import galacticwars.clonewars.classes.UnitClassId;
import galacticwars.clonewars.faction.FactionCatalog;
import galacticwars.clonewars.faction.FactionDefinition;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRuntimePolicy;
import galacticwars.clonewars.faction.ai.NpcAiProfile;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.world.OverworldFactionSpawnProfile;
import galacticwars.clonewars.world.CivilianArchetypeDefinition;

public record GameplayDataSnapshot(
        FactionCatalog factions,
        ArmyUnitCatalog units,
        Map<String, ArmyUnitId> unitIdsByEntityType,
        Map<String, ArmyUnitId> unitAliases,
        Map<String, KingdomBaseBlueprint> blueprints,
        Map<String, OverworldFactionSpawnProfile> overworldSpawnProfiles,
        Map<String, CivilianArchetypeDefinition> civilianArchetypesByEntityType,
        Map<AbilityId, AbilityDefinition> abilities,
        Map<UnitClassId, UnitClassDefinition> unitClasses,
        Map<FactionId, FactionRuntimePolicy> factionPolicies,
        Map<FactionId, NpcAiProfile> npcAiProfiles,
        LaunchContentDefinitions launchContent
) {
    public GameplayDataSnapshot {
        Objects.requireNonNull(factions, "factions");
        Objects.requireNonNull(units, "units");
        unitIdsByEntityType = immutableMap(unitIdsByEntityType, "unitIdsByEntityType");
        unitAliases = immutableMap(unitAliases, "unitAliases");
        Objects.requireNonNull(blueprints, "blueprints");
        LinkedHashMap<String, KingdomBaseBlueprint> normalizedBlueprints = new LinkedHashMap<>();
        for (Map.Entry<String, KingdomBaseBlueprint> entry : blueprints.entrySet()) {
            KingdomBaseBlueprint blueprint = Objects.requireNonNull(entry.getValue(), "blueprint");
            String key = normalizeBlueprintId(entry.getKey());
            if (!key.equals(blueprint.id())) {
                throw new IllegalArgumentException("Blueprint map key " + entry.getKey()
                        + " does not match definition id " + blueprint.id());
            }
            if (normalizedBlueprints.putIfAbsent(key, blueprint) != null) {
                throw new IllegalArgumentException("Duplicate blueprint id " + key);
            }
        }
        blueprints = Collections.unmodifiableMap(normalizedBlueprints);
        overworldSpawnProfiles = immutableMap(overworldSpawnProfiles, "overworldSpawnProfiles");
        civilianArchetypesByEntityType = immutableMap(
                civilianArchetypesByEntityType, "civilianArchetypesByEntityType");
        abilities = immutableMap(abilities, "abilities");
        unitClasses = immutableMap(unitClasses, "unitClasses");
        factionPolicies = immutableMap(factionPolicies, "factionPolicies");
        npcAiProfiles = immutableMap(npcAiProfiles, "npcAiProfiles");
        Objects.requireNonNull(launchContent, "launchContent");
    }

    public GameplayDataSnapshot(
            FactionCatalog factions,
            ArmyUnitCatalog units,
            Map<String, ArmyUnitId> unitIdsByEntityType,
            Map<String, ArmyUnitId> unitAliases,
            Map<String, KingdomBaseBlueprint> blueprints
    ) {
        this(factions, units, unitIdsByEntityType, unitAliases, blueprints, Map.of(), Map.of(),
                Map.of(), Map.of(), Map.of(), Map.of(), LaunchContentDefinitions.empty());
    }

    public GameplayDataSnapshot(
            FactionCatalog factions,
            ArmyUnitCatalog units,
            Map<String, ArmyUnitId> unitIdsByEntityType,
            Map<String, ArmyUnitId> unitAliases,
            Map<String, KingdomBaseBlueprint> blueprints,
            Map<String, OverworldFactionSpawnProfile> overworldSpawnProfiles
    ) {
        this(factions, units, unitIdsByEntityType, unitAliases, blueprints, overworldSpawnProfiles, Map.of(),
                Map.of(), Map.of(), Map.of(), Map.of(), LaunchContentDefinitions.empty());
    }

    public GameplayDataSnapshot(
            FactionCatalog factions,
            ArmyUnitCatalog units,
            Map<String, ArmyUnitId> unitIdsByEntityType,
            Map<String, ArmyUnitId> unitAliases,
            Map<String, KingdomBaseBlueprint> blueprints,
            Map<String, OverworldFactionSpawnProfile> overworldSpawnProfiles,
            Map<String, CivilianArchetypeDefinition> civilianArchetypesByEntityType
    ) {
        this(factions, units, unitIdsByEntityType, unitAliases, blueprints, overworldSpawnProfiles,
                civilianArchetypesByEntityType, Map.of(), Map.of(), Map.of(), Map.of(),
                LaunchContentDefinitions.empty());
    }

    public Optional<FactionDefinition> faction(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return factions.definition(FactionId.of(id));
    }

    public Optional<ArmyUnitDefinition> unit(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        ArmyUnitId requested = ArmyUnitId.of(id);
        ArmyUnitId resolved = unitAliases.getOrDefault(requested.toString(), requested);
        return units.definition(resolved);
    }

    public Optional<ArmyUnitDefinition> unitForEntityType(String entityTypeId) {
        ArmyUnitId id = unitIdsByEntityType.get(entityTypeId);
        return id == null ? Optional.empty() : units.definition(id);
    }

    public Optional<KingdomBaseBlueprint> blueprint(String id) {
        return Optional.ofNullable(blueprints.get(normalizeBlueprintId(id)));
    }

    public List<FactionDefinition> selectableFactions() {
        ArrayList<FactionDefinition> ordered = new ArrayList<>(factions.definitions().values());
        ordered.sort(Comparator.comparingInt(FactionDefinition::selectionOrder)
                .thenComparing(definition -> definition.id().toString()));
        return List.copyOf(ordered);
    }

    public Optional<OverworldFactionSpawnProfile> overworldSpawnProfileForEntity(String entityTypeId) {
        if (entityTypeId == null || entityTypeId.isBlank()) {
            return Optional.empty();
        }
        return overworldSpawnProfiles.values().stream().filter(profile -> profile.supports(entityTypeId)).findFirst();
    }

    public Optional<CivilianArchetypeDefinition> civilianArchetypeForEntity(String entityTypeId) {
        return Optional.ofNullable(civilianArchetypesByEntityType.get(entityTypeId));
    }

    public Optional<AbilityDefinition> ability(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(abilities.get(AbilityId.of(id)));
    }

    public Optional<UnitClassDefinition> unitClass(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(unitClasses.get(UnitClassId.of(id)));
    }

    public Optional<UnitClassDefinition> unitClassForUnit(ArmyUnitId unitId) {
        if (unitId == null) {
            return Optional.empty();
        }
        return unitClasses.values().stream().filter(definition -> definition.unitId().equals(unitId)).findFirst();
    }

    public List<AbilityDefinition> abilitiesForClass(UnitClassId classId) {
        UnitClassDefinition definition = unitClasses.get(classId);
        if (definition == null) {
            return List.of();
        }
        return definition.abilityIds().stream().map(abilities::get).filter(Objects::nonNull).toList();
    }

    public Optional<FactionRuntimePolicy> factionPolicy(FactionId factionId) {
        return Optional.ofNullable(factionPolicies.get(factionId));
    }

    public Optional<NpcAiProfile> npcAiProfile(FactionId factionId) {
        return Optional.ofNullable(npcAiProfiles.get(factionId));
    }

    public static String normalizeBlueprintId(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }
        return KingdomBaseBlueprint.canonicalId(id);
    }

    private static <K, V> Map<K, V> immutableMap(Map<K, V> source, String label) {
        Objects.requireNonNull(source, label);
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
