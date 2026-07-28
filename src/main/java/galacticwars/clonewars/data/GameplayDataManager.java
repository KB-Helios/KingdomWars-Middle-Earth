package galacticwars.clonewars.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.registry.ReloadListenerRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.ability.AbilityActivation;
import galacticwars.clonewars.ability.AbilityDefinition;
import galacticwars.clonewars.ability.AbilityId;
import galacticwars.clonewars.ability.AbilityKind;
import galacticwars.clonewars.army.ArmyEquipmentLoadout;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyUnitCatalog;
import galacticwars.clonewars.army.ArmyUnitDefinition;
import galacticwars.clonewars.army.ArmyUnitId;
import galacticwars.clonewars.army.ArmyUnitRole;
import galacticwars.clonewars.classes.ProgressionRequirement;
import galacticwars.clonewars.classes.UnitClassDefinition;
import galacticwars.clonewars.classes.UnitClassId;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.FactionCatalog;
import galacticwars.clonewars.faction.FactionDefinition;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRuntimePolicy;
import galacticwars.clonewars.faction.FactionStrategyDefinition;
import galacticwars.clonewars.faction.FactionAlignmentRule;
import galacticwars.clonewars.faction.ai.FactionReputationEvent;
import galacticwars.clonewars.faction.ai.NpcAiProfile;
import galacticwars.clonewars.faction.ai.NpcRole;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.settlement.BaseBlockPlacement;
import galacticwars.clonewars.settlement.BlueprintAnchor;
import galacticwars.clonewars.settlement.BlueprintMode;
import galacticwars.clonewars.settlement.BlueprintRosterEntry;
import galacticwars.clonewars.settlement.BlueprintTerrainConstraints;
import galacticwars.clonewars.settlement.BlueprintWorldgenProfile;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.world.BlueprintSiteKind;
import galacticwars.clonewars.world.OverworldFactionSpawnProfile;
import galacticwars.clonewars.world.CivilianArchetypeDefinition;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class GameplayDataManager extends SimplePreparableReloadListener<GameplayDataManager.LoadResult> {
    private static final Gson GSON = new Gson();
    private static final GameplayDataManager INSTANCE = new GameplayDataManager();
    private static final Identifier LISTENER_ID =
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "gameplay_data");
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final GameplayDataSnapshot DEFAULT_SNAPSHOT = defaults();

    private GameplayDataManager() {
    }

    public static GameplayDataSnapshot snapshot() {
        return currentContent().snapshot();
    }

    public static long generation() {
        return currentContent().state().generation();
    }

    public static GameplayContentState contentState() {
        return currentContent().state();
    }

    public static boolean isReady() {
        return currentContent().state().hasUsableSnapshot();
    }

    public static CurrentContent currentContent() {
        LaunchContentRuntime.RuntimeSnapshot current = LaunchContentRuntime.current();
        GameplayDataSnapshot accepted = current.gameplaySnapshot();
        return new CurrentContent(
                accepted == null ? DEFAULT_SNAPSHOT : accepted,
                current.contentState());
    }

    public static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            ReloadListenerRegistry.register(PackType.SERVER_DATA, INSTANCE, LISTENER_ID);
        }
    }

    @Override
    protected LoadResult prepare(ResourceManager manager, ProfilerFiller profiler) {
        try {
            PreparedGameplayData prepared = GameplayDataReloadPipeline.load(manager);
            Map<FactionId, FactionDefinition> factions = prepared.getFactions();
            List<ArmyUnitDefinition> units = prepared.getUnits();
            Map<AbilityId, AbilityDefinition> abilities = prepared.getAbilities();
            Map<UnitClassId, UnitClassDefinition> unitClasses = prepared.getUnitClasses();
            Map<FactionId, FactionRuntimePolicy> factionPolicies = prepared.getFactionPolicies();
            Map<FactionId, NpcAiProfile> npcAiProfiles = prepared.getNpcAiProfiles();
            Map<String, KingdomBaseBlueprint> blueprints = prepared.getBlueprints();
            Map<String, CivilianArchetypeDefinition> civilianArchetypes = prepared.getCivilianArchetypes();
            Map<String, OverworldFactionSpawnProfile> overworldSpawnProfiles =
                    prepared.getOverworldSpawnProfiles();
            LaunchContentDefinitions launchContent = prepared.getLaunchContent();
            galacticwars.clonewars.technology.TechnologyCatalog technology = prepared.getTechnology();
            validateRelations(factions);
            LinkedHashMap<String, ArmyUnitId> byEntityType = new LinkedHashMap<>();
            for (ArmyUnitDefinition unit : units) {
                if (unit.entityTypeId().isBlank()) {
                    throw new IllegalArgumentException("Unit " + unit.id() + " is missing entity_type");
                }
                ArmyUnitId previous = byEntityType.putIfAbsent(unit.entityTypeId(), unit.id());
                if (previous != null) {
                    throw new IllegalArgumentException("Entity type " + unit.entityTypeId()
                            + " is mapped by both " + previous + " and " + unit.id());
                }
                requireRegistered(BuiltInRegistries.ENTITY_TYPE, unit.entityTypeId(),
                        "entity type for " + unit.id());
                validateEquipment(unit);
                String expectedTradition = switch (unit.factionId().toString()) {
                    case "galacticwars:republic" -> "jedi";
                    case "galacticwars:separatist" -> "sith";
                    case "galacticwars:nightsister" -> "nightsister";
                    default -> "";
                };
                for (String abilityId : unit.forceLoadout()) {
                    LaunchContentDefinitions.ForceAbilityDefinition forceAbility =
                            launchContent.forceAbilities().get(abilityId);
                    if (forceAbility == null) {
                        throw new IllegalArgumentException("Unit " + unit.id()
                                + " references unknown Force ability " + abilityId);
                    }
                    if (!forceAbility.path().equals(expectedTradition)) {
                        throw new IllegalArgumentException("Unit " + unit.id()
                                + " has cross-tradition Force ability " + abilityId);
                    }
                }
            }
            for (FactionDefinition faction : factions.values()) {
                if (faction.pledgeDirectDelta() < faction.minimumHiringAlignment()) {
                    throw new IllegalArgumentException("Faction " + faction.id()
                            + " pledge grants " + faction.pledgeDirectDelta()
                            + " alignment but direct hiring requires "
                            + faction.minimumHiringAlignment());
                }
                requireRegistered(BuiltInRegistries.ITEM, faction.pledgeTokenItemId(),
                        "pledge token for " + faction.id());
                ArmyUnitDefinition starterUnit = units.stream()
                        .filter(unit -> unit.id().toString().equals(faction.starterUnitId()))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException(
                                "Faction " + faction.id() + " has unknown starter_unit "
                                        + faction.starterUnitId()));
                if (!starterUnit.factionId().equals(faction.id())) {
                    throw new IllegalArgumentException("Faction " + faction.id()
                            + " starter_unit belongs to " + starterUnit.factionId());
                }
            }
            for (LaunchContentDefinitions.TradeDefinition trade : launchContent.trades().values()) {
                requireRegistered(BuiltInRegistries.ITEM, trade.itemId(),
                        "stock item for trade " + trade.id());
            }
            for (LaunchContentDefinitions.VehicleDefinition vehicle : launchContent.vehicles().values()) {
                for (String itemId : vehicle.fabricationInputs().keySet()) {
                    requireRegistered(BuiltInRegistries.ITEM, itemId,
                            "fabrication input for vehicle " + vehicle.id());
                }
            }
            for (LaunchContentDefinitions.ForceAbilityDefinition ability
                    : launchContent.forceAbilities().values()) {
                if (ability.enabled() && !galacticwars.clonewars.force.ForceEffectExecutorCatalog
                        .supports(ability.effect())) {
                    throw new IllegalArgumentException(
                            "Enabled Force ability has no executor: " + ability.id());
                }
            }
            for (KingdomBaseBlueprint blueprint : blueprints.values()) {
                for (BaseBlockPlacement placement : blueprint.placements()) {
                    requireRegistered(BuiltInRegistries.BLOCK, placement.blockId(),
                            "blueprint block for " + blueprint.id());
                    requireRegistered(BuiltInRegistries.ITEM, placement.itemId(),
                            "blueprint item for " + blueprint.id());
                    placement.blockState();
                }
                blueprint.worldgen().ifPresent(profile -> {
                    if (!factions.containsKey(FactionId.of(profile.factionId()))) {
                        throw new IllegalArgumentException("Blueprint " + blueprint.id()
                                + " references unknown faction " + profile.factionId());
                    }
                    for (BlueprintRosterEntry entry : profile.roster()) {
                        requireRegistered(BuiltInRegistries.ENTITY_TYPE, entry.entityTypeId(),
                                "worldgen roster entity for " + blueprint.id());
                        ArmyUnitDefinition unit = units.stream()
                                .filter(candidate -> candidate.entityTypeId().equals(entry.entityTypeId()))
                                .findFirst().orElse(null);
                        CivilianArchetypeDefinition civilian = civilianArchetypes.get(entry.entityTypeId());
                        String actualFaction = unit != null ? unit.factionId().toString()
                                : civilian != null ? civilian.factionId() : "";
                        if (!actualFaction.equals(profile.factionId())) {
                            throw new IllegalArgumentException("Roster entity " + entry.entityTypeId()
                                    + " does not belong to " + profile.factionId());
                        }
                    }
                });
            }
            GameplayDataSnapshot loaded = new GameplayDataSnapshot(
                    new FactionCatalog(factions),
                    new ArmyUnitCatalog(units),
                    byEntityType,
                    Map.of("galacticwars:mandalorian_rider", ArmyUnitId.of("galacticwars:mandalorian_warrior")),
                    blueprints,
                    overworldSpawnProfiles,
                    civilianArchetypes,
                    abilities,
                    unitClasses,
                    factionPolicies,
                    npcAiProfiles,
                    launchContent,
                    technology);
            CoreContentBindings.validate(loaded);
            for (CoreContentBindings.VehicleBinding binding : CoreContentBindings.vehicles().values()) {
                requireRegistered(BuiltInRegistries.ENTITY_TYPE, binding.entityTypeId(),
                        "vehicle entity type");
                requireRegistered(BuiltInRegistries.ITEM, binding.deploymentKitItemId(),
                        "vehicle deployment kit");
            }
            return LoadResult.success(loaded);
        } catch (RuntimeException | IOException exception) {
            return LoadResult.failure(exception);
        }
    }

    @Override
    protected void apply(LoadResult result, ResourceManager manager, ProfilerFiller profiler) {
        if (result.snapshot().isPresent()) {
            GameplayDataSnapshot loaded = result.snapshot().orElseThrow();
            LinkedHashMap<String, List<String>> launchUnits = new LinkedHashMap<>();
            loaded.factions().definitions().keySet().forEach(faction -> launchUnits.put(
                    faction.path(), java.util.stream.Stream.concat(
                            loaded.units().definitions().values().stream()
                                    .filter(unit -> unit.factionId().equals(faction))
                                    .map(unit -> unit.id().path()),
                            loaded.civilianArchetypesByEntityType().values().stream()
                                    .filter(civilian -> civilian.factionId().equals(faction.toString()))
                                    .map(civilian -> subjectPath(civilian.id())))
                            .distinct().sorted().toList()));
            long nextGeneration = currentContent().state().generation() + 1;
            String contentHash = GameplayContentFingerprint.compute(loaded);
            GameplayContentState ready = GameplayContentState.ready(nextGeneration, contentHash);
            LaunchContentRuntime.installAccepted(
                    loaded,
                    ready,
                    loaded.selectableFactions().stream().map(faction -> faction.id().toString()).toList(),
                    launchUnits);
            GalacticWars.LOGGER.info(
                    "Loaded gameplay content generation {} ({}) with {} factions, {} units, {} classes, {} abilities, {} faction policies, {} NPC AI profiles, {} civilian archetypes, {} base blueprints, {} Overworld spawn profiles, {} quests, {} trades, {} vehicles, and {} planets",
                    nextGeneration,
                    contentHash,
                    loaded.factions().definitions().size(),
                    loaded.units().definitions().size(),
                    loaded.unitClasses().size(),
                    loaded.abilities().size(),
                    loaded.factionPolicies().size(),
                    loaded.npcAiProfiles().size(),
                    loaded.civilianArchetypesByEntityType().size(),
                    loaded.blueprints().size(),
                    loaded.overworldSpawnProfiles().size(),
                    loaded.launchContent().quests().size(),
                    loaded.launchContent().trades().size(),
                    loaded.launchContent().vehicles().size(),
                    loaded.launchContent().planets().size());
        } else {
            Throwable failure = result.failure().orElseGet(
                    () -> new IllegalStateException("gameplay data reload failed without a diagnostic"));
            String diagnostic = failure.getMessage() == null
                    ? failure.getClass().getSimpleName()
                    : failure.getClass().getSimpleName() + ": " + failure.getMessage();
            GameplayContentState currentState = currentContent().state();
            if (!currentState.hasUsableSnapshot()) {
                LaunchContentRuntime.updateContentState(GameplayContentState.initialFailure(diagnostic));
                GalacticWars.LOGGER.error("Initial gameplay data load failed; refusing to start", failure);
                throw new IllegalStateException("Initial Galactic Wars gameplay data is invalid: "
                        + diagnostic, failure);
            }
            LaunchContentRuntime.updateContentState(GameplayContentState.rejectedReload(
                    currentState.generation(), currentState.contentHash(), diagnostic));
            GalacticWars.LOGGER.error(
                    "Rejected gameplay data reload; retaining the previous valid snapshot",
                    failure);
        }
    }

    static Map<FactionId, FactionDefinition> loadFactions(ResourceManager manager) throws IOException {
        LinkedHashMap<FactionId, FactionDefinition> definitions = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/factions")) {
            JsonObject json = resource.json();
            FactionId id = FactionId.of(requiredString(json, "id", resource.id()));
            Set<FactionId> allies = factionIds(json, "allies");
            Set<FactionId> enemies = factionIds(json, "enemies");
            FactionDefinition.validateRelationSets(resource.id().toString(), id, allies, enemies);
            JsonObject pledge = object(json, "pledge", new JsonObject());
            JsonObject strategy = object(json, "strategy", new JsonObject());
            FactionDefinition definition = new FactionDefinition(
                    id,
                    requiredString(json, "display_name", resource.id()),
                    requiredInteger(json, "hire_cost", resource.id()),
                    integer(json, "minimum_hiring_alignment", 0),
                    integer(json, "max_owned_recruits", 0),
                    allies,
                    enemies,
                    integer(json, "selection_order", definitions.size()),
                    string(pledge, "token_item", "galacticwars:" + id.path() + "_identity_chip"),
                    integer(pledge, "direct_delta", 10),
                    integer(pledge, "ally_delta", 2),
                    integer(pledge, "enemy_delta", -5),
                    new FactionStrategyDefinition(
                            string(strategy, "archetype", "shared"),
                            integer(strategy, "recruitment_capacity_bonus", 0),
                            integer(strategy, "upkeep_percent", 100),
                            integer(strategy, "production_percent", 100),
                            integer(strategy, "morale_bonus", 0),
                            string(strategy, "strength", "combined_arms"),
                            string(strategy, "weakness", "none")),
                    requiredString(json, "starter_unit", resource.id()));
            if (definitions.putIfAbsent(id, definition) != null) {
                throw new IllegalArgumentException("Duplicate faction id " + id + " in " + resource.id());
            }
        }
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("No faction definitions were loaded");
        }
        return definitions;
    }

    static List<ArmyUnitDefinition> loadUnits(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions
    ) throws IOException {
        ArrayList<ArmyUnitDefinition> definitions = new ArrayList<>();
        LinkedHashSet<ArmyUnitId> seen = new LinkedHashSet<>();
        for (ResourceJson resource : resources(manager, "galacticwars/units")) {
            JsonObject json = resource.json();
            ArmyUnitId id = ArmyUnitId.of(requiredString(json, "id", resource.id()));
            FactionId factionId = FactionId.of(requiredString(json, "faction", resource.id()));
            FactionDefinition faction = factions.get(factionId);
            if (faction == null) {
                throw new IllegalArgumentException("Unit " + id + " references unknown faction " + factionId);
            }
            JsonObject attributes = object(json, "attributes", json);
            JsonObject equipment = object(json, "equipment", new JsonObject());
            ArmyUnitDefinition definition = new ArmyUnitDefinition(
                    id,
                    requiredString(json, "display_name", resource.id()),
                    factionId,
                    ArmyUnitRole.valueOf(requiredString(json, "role", resource.id()).toUpperCase(Locale.ROOT)),
                    integer(json, "hire_cost", faction.hireCost()),
                    integer(attributes, "max_health", integer(json, "max_health", 20)),
                    integer(attributes, "attack_damage", integer(json, "attack_damage", 5)),
                    ArmyFormation.valueOf(requiredString(json, "default_formation", resource.id())
                            .toUpperCase(Locale.ROOT)),
                    requiredString(json, "entity_type", resource.id()),
                    decimal(attributes, "movement_speed", 0.28D),
                    decimal(attributes, "follow_range", 24.0D),
                    decimal(attributes, "armor", 0.0D),
                    parseUnitEquipment(equipment),
                    strings(json, "force_loadout"));
            if (!seen.add(id)) {
                throw new IllegalArgumentException("Duplicate unit id " + id + " in " + resource.id());
            }
            definitions.add(definition);
        }
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("No army unit definitions were loaded");
        }
        return List.copyOf(definitions);
    }

    static ArmyEquipmentLoadout parseUnitEquipment(JsonObject equipment) {
        JsonObject resolved = equipment == null ? new JsonObject() : equipment;
        return new ArmyEquipmentLoadout(
                string(resolved, "main_hand", ""),
                string(resolved, "head", ""),
                string(resolved, "chest", ""),
                string(resolved, "legs", ""),
                string(resolved, "feet", ""));
    }

    static Map<AbilityId, AbilityDefinition> loadAbilities(ResourceManager manager) throws IOException {
        LinkedHashMap<AbilityId, AbilityDefinition> definitions = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/abilities")) {
            requireSchema(resource, 1);
            for (JsonObject json : definitionObjects(resource, "abilities")) {
                AbilityDefinition definition = new AbilityDefinition(
                        AbilityId.of(requiredString(json, "id", resource.id())),
                        requiredString(json, "display_name", resource.id()),
                        AbilityKind.valueOf(requiredString(json, "kind", resource.id())
                                .toUpperCase(Locale.ROOT)),
                        AbilityActivation.valueOf(requiredString(json, "activation", resource.id())
                                .toUpperCase(Locale.ROOT)),
                        integer(json, "cooldown_ticks", 0),
                        integer(json, "resource_cost", 0),
                        decimal(json, "range", 0.0D),
                        integer(json, "ai_interval_ticks", 20),
                        bool(json, "enabled", true));
                if (definitions.putIfAbsent(definition.id(), definition) != null) {
                    throw new IllegalArgumentException("Duplicate ability id " + definition.id()
                            + " in " + resource.id());
                }
                if (definition.enabled()
                        && !galacticwars.clonewars.classes.ClassAbilityEffectRegistry.registered(
                        definition.id().toString())) {
                    throw new IllegalArgumentException("Enabled ability lacks runtime executor "
                            + definition.id() + " in " + resource.id());
                }
            }
        }
        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("No ability definitions were loaded");
        }
        return Map.copyOf(definitions);
    }

    static Map<UnitClassId, UnitClassDefinition> loadUnitClasses(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions,
            List<ArmyUnitDefinition> units,
            Map<AbilityId, AbilityDefinition> abilities
    ) throws IOException {
        LinkedHashMap<ArmyUnitId, ArmyUnitDefinition> unitsById = new LinkedHashMap<>();
        for (ArmyUnitDefinition unit : units) {
            unitsById.put(unit.id(), unit);
        }
        LinkedHashMap<UnitClassId, UnitClassDefinition> definitions = new LinkedHashMap<>();
        LinkedHashSet<ArmyUnitId> representedUnits = new LinkedHashSet<>();
        for (ResourceJson resource : resources(manager, "galacticwars/classes")) {
            requireSchema(resource, 1);
            for (JsonObject json : definitionObjects(resource, "classes")) {
                FactionId factionId = FactionId.of(requiredString(json, "faction", resource.id()));
                ArmyUnitId unitId = ArmyUnitId.of(requiredString(json, "unit", resource.id()));
                ArmyUnitDefinition unit = unitsById.get(unitId);
                if (!factions.containsKey(factionId)) {
                    throw new IllegalArgumentException("Class references unknown faction " + factionId);
                }
                if (unit == null || !unit.factionId().equals(factionId)) {
                    throw new IllegalArgumentException("Class unit " + unitId
                            + " is missing or does not belong to " + factionId);
                }
                List<AbilityId> abilityIds = strings(json, "abilities").stream().map(AbilityId::of).toList();
                for (AbilityId abilityId : abilityIds) {
                    if (!abilities.containsKey(abilityId)) {
                        throw new IllegalArgumentException("Class references unknown ability " + abilityId);
                    }
                }
                ArrayList<ProgressionRequirement> requirements = new ArrayList<>();
                JsonArray requirementArray = json.has("requirements")
                        ? json.getAsJsonArray("requirements") : new JsonArray();
                for (JsonElement element : requirementArray) {
                    JsonObject requirement = element.getAsJsonObject();
                    requirements.add(new ProgressionRequirement(
                            requiredString(requirement, "type", resource.id()),
                            requiredString(requirement, "subject", resource.id()),
                            integer(requirement, "amount", 1)));
                }
                UnitClassDefinition definition = new UnitClassDefinition(
                        UnitClassId.of(requiredString(json, "id", resource.id())),
                        requiredString(json, "display_name", resource.id()),
                        factionId,
                        unitId,
                        bool(json, "player_assignable", false),
                        abilityIds,
                        requirements,
                        json.has("force_tradition_slot")
                                ? string(json, "force_tradition_slot", "")
                                : string(json, "force_path_slot", ""));
                if (definitions.putIfAbsent(definition.id(), definition) != null) {
                    throw new IllegalArgumentException("Duplicate class id " + definition.id()
                            + " in " + resource.id());
                }
                if (!representedUnits.add(unitId)) {
                    throw new IllegalArgumentException("Multiple classes target unit " + unitId);
                }
            }
        }
        if (!representedUnits.equals(unitsById.keySet())) {
            LinkedHashSet<ArmyUnitId> missing = new LinkedHashSet<>(unitsById.keySet());
            missing.removeAll(representedUnits);
            throw new IllegalArgumentException("Every launch unit requires one class; missing " + missing);
        }
        return Map.copyOf(definitions);
    }

    static Map<FactionId, FactionRuntimePolicy> loadFactionPolicies(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions
    ) throws IOException {
        LinkedHashMap<FactionId, FactionRuntimePolicy> policies = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/faction_policies")) {
            requireSchema(resource, 1);
            for (JsonObject json : definitionObjects(resource, "policies")) {
                FactionId factionId = FactionId.of(requiredString(json, "faction", resource.id()));
                if (!factions.containsKey(factionId)) {
                    throw new IllegalArgumentException("Policy references unknown faction " + factionId);
                }
                FactionRuntimePolicy policy = new FactionRuntimePolicy(
                        factionId,
                        Set.copyOf(strings(json, "traits")),
                        integerMap(json, "modifiers", resource.id()));
                FactionBalanceService.resolve(factions.get(factionId), policy);
                if (policies.putIfAbsent(factionId, policy) != null) {
                    throw new IllegalArgumentException("Duplicate faction policy for " + factionId);
                }
            }
        }
        if (!policies.keySet().equals(factions.keySet())) {
            LinkedHashSet<FactionId> missing = new LinkedHashSet<>(factions.keySet());
            missing.removeAll(policies.keySet());
            throw new IllegalArgumentException("Every faction requires one runtime policy; missing " + missing);
        }
        return Map.copyOf(policies);
    }

    static Map<FactionId, NpcAiProfile> loadNpcAiProfiles(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions
    ) throws IOException {
        LinkedHashMap<FactionId, NpcAiProfile> profiles = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/npc_ai_profiles")) {
            requireSchema(resource, NpcAiProfile.SCHEMA_VERSION);
            JsonObject json = resource.json();
            FactionId factionId = FactionId.of(requiredString(json, "faction", resource.id()));
            if (!factions.containsKey(factionId)) {
                throw new IllegalArgumentException(
                        "NPC AI profile references unknown faction " + factionId);
            }

            JsonObject rolesJson = requiredObject(json, "roles", resource.id());
            EnumMap<NpcRole, NpcAiProfile.RoleScanSettings> roles =
                    new EnumMap<>(NpcRole.class);
            for (NpcRole role : NpcRole.values()) {
                JsonObject settings = requiredObject(rolesJson, role.id(), resource.id());
                roles.put(role, new NpcAiProfile.RoleScanSettings(
                        requiredInteger(settings, "scan_interval", resource.id()),
                        requiredInteger(settings, "scan_radius", resource.id())));
            }

            JsonObject thresholds = requiredObject(
                    json, "alignment_thresholds", resource.id());
            JsonObject eventsJson = requiredObject(
                    json, "reputation_events", resource.id());
            EnumMap<FactionReputationEvent, FactionAlignmentRule> reputationRules =
                    new EnumMap<>(FactionReputationEvent.class);
            for (FactionReputationEvent event : FactionReputationEvent.values()) {
                JsonObject rule = requiredObject(eventsJson, event.id(), resource.id());
                reputationRules.put(event, new FactionAlignmentRule(
                        requiredInteger(rule, "direct", resource.id()),
                        requiredInteger(rule, "allies", resource.id()),
                        requiredInteger(rule, "enemies", resource.id()),
                        event.id()));
            }

            NpcAiProfile profile = new NpcAiProfile(
                    factionId,
                    roles,
                    requiredInteger(json, "coordination_radius", resource.id()),
                    requiredInteger(json, "max_responders", resource.id()),
                    requiredInteger(json, "warning_cooldown", resource.id()),
                    requiredInteger(json, "alert_duration", resource.id()),
                    requiredInteger(thresholds, "friendly", resource.id()),
                    requiredInteger(thresholds, "neutral", resource.id()),
                    requiredInteger(thresholds, "wary", resource.id()),
                    requiredInteger(json, "friendly_trade_price_percent", resource.id()),
                    reputationRules);
            if (profiles.putIfAbsent(factionId, profile) != null) {
                throw new IllegalArgumentException(
                        "Duplicate NPC AI profile for " + factionId);
            }
        }
        if (!profiles.keySet().equals(factions.keySet())) {
            LinkedHashSet<FactionId> missing = new LinkedHashSet<>(factions.keySet());
            missing.removeAll(profiles.keySet());
            throw new IllegalArgumentException("Missing NPC AI profiles for " + missing);
        }
        return Map.copyOf(profiles);
    }

    static Map<String, KingdomBaseBlueprint> loadBlueprints(ResourceManager manager) throws IOException {
        LinkedHashMap<String, KingdomBaseBlueprint> blueprints = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/blueprints")) {
            KingdomBaseBlueprint blueprint = parseBlueprint(manager, resource.id(), resource.json());
            blueprint.worldgen().ifPresent(profile -> {
                for (String tableId : profile.lootTables().values()) {
                    Identifier id = Identifier.parse(tableId);
                    Identifier lootResource = Identifier.fromNamespaceAndPath(
                            id.getNamespace(), "loot_table/" + id.getPath() + ".json");
                    if (manager.getResource(lootResource).isEmpty()) {
                        throw new IllegalArgumentException("Blueprint " + blueprint.id()
                                + " references missing faction site loot table " + lootResource);
                    }
                }
            });
            if (blueprints.putIfAbsent(blueprint.id(), blueprint) != null) {
                throw new IllegalArgumentException("Duplicate blueprint id " + blueprint.id() + " in " + resource.id());
            }
        }
        if (!blueprints.containsKey(KingdomBaseBlueprint.STARTER_KEEP_ID)) {
            throw new IllegalArgumentException("Missing required forward_base blueprint");
        }
        if (!blueprints.containsKey(KingdomBaseBlueprint.STARTER_CAMP_ID)) {
            throw new IllegalArgumentException("Missing required starter_camp blueprint");
        }
        return blueprints;
    }

    static Map<String, OverworldFactionSpawnProfile> loadOverworldSpawnProfiles(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions,
            List<ArmyUnitDefinition> units,
            Map<String, CivilianArchetypeDefinition> civilianArchetypes
    ) throws IOException {
        Map<String, ArmyUnitDefinition> unitsByEntity = units.stream().collect(java.util.stream.Collectors.toMap(
                ArmyUnitDefinition::entityTypeId, unit -> unit));
        LinkedHashMap<String, OverworldFactionSpawnProfile> profiles = new LinkedHashMap<>();
        for (ResourceJson resource : resources(manager, "galacticwars/overworld_faction_spawns")) {
            JsonArray definitions = requiredArray(resource.json(), "profiles", resource.id());
            for (JsonElement element : definitions) {
                JsonObject json = element.getAsJsonObject();
                String factionId = requiredString(json, "faction", resource.id());
                FactionId faction = FactionId.of(factionId);
                if (!factions.containsKey(faction)) {
                    throw new IllegalArgumentException("Overworld spawn profile references unknown faction " + faction);
                }
                LinkedHashMap<String, NpcServiceBranch> branches = new LinkedHashMap<>();
                JsonArray entities = requiredArray(json, "entities", resource.id());
                for (JsonElement entityElement : entities) {
                    JsonObject entity = entityElement.getAsJsonObject();
                    String entityTypeId = requiredString(entity, "entity_type", resource.id());
                    ArmyUnitDefinition unit = unitsByEntity.get(entityTypeId);
                    CivilianArchetypeDefinition civilian = civilianArchetypes.get(entityTypeId);
                    boolean matchingUnit = unit != null && unit.factionId().equals(faction);
                    boolean matchingCivilian = civilian != null && civilian.factionId().equals(faction.toString());
                    if (!matchingUnit && !matchingCivilian) {
                        throw new IllegalArgumentException("Overworld spawn entity " + entityTypeId
                                + " does not belong to " + faction);
                    }
                    NpcServiceBranch branch = NpcServiceBranch.byId(
                            requiredString(entity, "service_branch", resource.id()));
                    if (branches.putIfAbsent(entityTypeId, branch) != null) {
                        throw new IllegalArgumentException("Duplicate Overworld spawn entity " + entityTypeId);
                    }
                }
                OverworldFactionSpawnProfile profile = new OverworldFactionSpawnProfile(
                        faction.toString(), branches,
                        integer(json, "outpost_radius", 96),
                        integer(json, "minimum_outpost_spacing", 256),
                        integer(json, "military_capacity", 12),
                        integer(json, "civilian_capacity", 4));
                if (profiles.putIfAbsent(profile.factionId(), profile) != null) {
                    throw new IllegalArgumentException("Duplicate Overworld spawn profile for " + faction);
                }
            }
        }
        if (profiles.size() != factions.size()) {
            throw new IllegalArgumentException("Every faction requires one Overworld spawn profile");
        }
        return Map.copyOf(profiles);
    }

    static Map<String, CivilianArchetypeDefinition> loadCivilianArchetypes(
            ResourceManager manager,
            Map<FactionId, FactionDefinition> factions
    ) throws IOException {
        LinkedHashMap<String, CivilianArchetypeDefinition> definitions = new LinkedHashMap<>();
        LinkedHashSet<FactionId> representedFactions = new LinkedHashSet<>();
        for (ResourceJson resource : resources(manager, "galacticwars/civilian_archetypes")) {
            JsonObject json = resource.json();
            FactionId faction = FactionId.of(requiredString(json, "faction", resource.id()));
            if (!factions.containsKey(faction)) {
                throw new IllegalArgumentException("Civilian archetype references unknown faction " + faction);
            }
            JsonArray professionsJson = requiredArray(json, "professions", resource.id());
            ArrayList<String> professions = new ArrayList<>();
            professionsJson.forEach(element -> professions.add(element.getAsString()));
            CivilianArchetypeDefinition definition = new CivilianArchetypeDefinition(
                    requiredString(json, "id", resource.id()),
                    requiredString(json, "display_name", resource.id()),
                    faction.toString(),
                    requiredString(json, "entity_type", resource.id()),
                    professions,
                    integer(json, "max_health", 20),
                    decimal(json, "movement_speed", 0.26D),
                    integer(json, "base_morale", 60),
                    string(json, "home_type", "housing"));
            requireRegistered(BuiltInRegistries.ENTITY_TYPE, definition.entityTypeId(),
                    "civilian entity type for " + definition.id());
            if (definitions.putIfAbsent(definition.entityTypeId(), definition) != null) {
                throw new IllegalArgumentException("Duplicate civilian archetype entity type "
                        + definition.entityTypeId());
            }
            representedFactions.add(faction);
        }
        if (representedFactions.size() != factions.size()) {
            throw new IllegalArgumentException("Every faction requires at least one civilian archetype");
        }
        return Map.copyOf(definitions);
    }

    static KingdomBaseBlueprint parseBlueprint(Identifier resourceId, JsonObject json) {
        return parseBlueprint(null, resourceId, json);
    }

    static KingdomBaseBlueprint parseBlueprint(ResourceManager manager, Identifier resourceId, JsonObject json) {
        int schema = integer(json, "schema_version", -1);
        if (schema != 1 && schema != 2 && schema != 3) {
            throw new IllegalArgumentException("Unsupported blueprint schema in " + resourceId);
        }
        String id = GameplayDataSnapshot.normalizeBlueprintId(requiredString(json, "id", resourceId));
        String canonicalResourceId = GameplayDataSnapshot.normalizeBlueprintId(resourceId.toString());
        if (!id.equals(canonicalResourceId)) {
            throw new IllegalArgumentException("Blueprint " + resourceId + " declares mismatched id " + id);
        }
        JsonObject anchorJson = requiredObject(json, "anchor", resourceId);
        BlueprintAnchor anchor = new BlueprintAnchor(
                integer(anchorJson, "x", 0),
                integer(anchorJson, "y", 0),
                integer(anchorJson, "z", 0));
        JsonArray rotationJson = requiredArray(json, "allowed_rotations", resourceId);
        ArrayList<Integer> allowedRotations = new ArrayList<>(rotationJson.size());
        for (JsonElement element : rotationJson) {
            allowedRotations.add(element.getAsInt());
        }
        String templateId = schema >= 2 ? requiredString(json, "template", resourceId) : "";
        TemplateContents template = schema >= 2
                ? loadTemplate(Objects.requireNonNull(manager, "resource manager for schema v2"), templateId, resourceId)
                : null;
        ArrayList<BaseBlockPlacement> placements = schema >= 2
                ? new ArrayList<>(template.placements())
                : parseInlinePlacements(requiredArray(json, "placements", resourceId), resourceId);
        JsonObject rewards = object(json, "rewards", new JsonObject());
        Set<BlueprintMode> modes = schema >= 2 ? parseModes(json, resourceId) : Set.of(BlueprintMode.CONSTRUCTION);
        JsonObject terrain = object(json, "terrain", new JsonObject());
        BlueprintTerrainConstraints constraints = new BlueprintTerrainConstraints(
                integer(terrain, "max_slope", 4), integer(terrain, "min_y", -64), integer(terrain, "max_y", 320));
        JsonObject construction = object(json, "construction", new JsonObject());
        Map<String, Integer> costs = schema >= 2 && construction.has("costs")
                ? integerMap(construction, "costs", resourceId) : Map.of();
        Optional<BlueprintWorldgenProfile> worldgen = modes.contains(BlueprintMode.WORLDGEN)
                ? Optional.of(parseWorldgen(
                        requiredObject(json, "worldgen", resourceId), resourceId, schema))
                : Optional.empty();
        if (schema >= 2 && modes.contains(BlueprintMode.WORLDGEN) && template.siteAnchorCount() != 1) {
            throw new IllegalArgumentException("Worldgen blueprint " + id
                    + " template must contain exactly one site_anchor marker");
        }
        if (schema >= 2 && worldgen.isPresent()
                && !template.lootMarkers().equals(Set.copyOf(worldgen.orElseThrow().lootMarkers()))) {
            throw new IllegalArgumentException("Loot markers do not match template contents for " + id);
        }
        if (schema >= 3 && worldgen.isPresent()) {
            BlueprintSiteKind siteKind = worldgen.orElseThrow().siteKind();
            int expectedCommandPosts = siteKind == BlueprintSiteKind.COMMAND_CENTER ? 1 : 0;
            if (template.commandPostCount() != expectedCommandPosts) {
                throw new IllegalArgumentException("Worldgen blueprint " + id + " requires "
                        + expectedCommandPosts + " command_post markers");
            }
        }
        if (!costs.isEmpty()) {
            Map<String, Integer> derived = new java.util.TreeMap<>();
            placements.forEach(placement -> derived.merge(placement.itemId(), 1, Integer::sum));
            if (!derived.equals(new java.util.TreeMap<>(costs))) {
                throw new IllegalArgumentException("Construction costs do not match template contents for " + id);
            }
        }
        return new KingdomBaseBlueprint(
                id,
                requiredString(json, "display_name", resourceId),
                anchor,
                allowedRotations,
                placements,
                integer(rewards, "housing", 0),
                integer(rewards, "storage_slots", 0),
                string(rewards, "worksite", ""),
                integer(rewards, "worksite_capacity", 0),
                integer(rewards, "commander_slots", 0),
                templateId,
                modes,
                constraints,
                costs,
                worldgen,
                stringSet(json, "compatible_definition_hashes"),
                stringSet(json, "compatible_content_hashes"));
    }

    private static ArrayList<BaseBlockPlacement> parseInlinePlacements(JsonArray placementJson, Identifier resourceId) {
        ArrayList<BaseBlockPlacement> placements = new ArrayList<>(placementJson.size());
        for (JsonElement element : placementJson) {
            JsonObject placement = element.getAsJsonObject();
            placements.add(new BaseBlockPlacement(
                    integer(placement, "x", 0), integer(placement, "y", 0), integer(placement, "z", 0),
                    requiredString(placement, "block", resourceId), requiredString(placement, "item", resourceId)));
        }
        return placements;
    }

    private static Set<BlueprintMode> parseModes(JsonObject json, Identifier resourceId) {
        LinkedHashSet<BlueprintMode> modes = new LinkedHashSet<>();
        for (JsonElement element : requiredArray(json, "modes", resourceId)) {
            try {
                modes.add(BlueprintMode.valueOf(element.getAsString().trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Invalid blueprint mode in " + resourceId, exception);
            }
        }
        return Set.copyOf(modes);
    }

    private static BlueprintWorldgenProfile parseWorldgen(
            JsonObject json, Identifier resourceId, int schema
    ) {
        ArrayList<BlueprintRosterEntry> roster = new ArrayList<>();
        for (JsonElement element : requiredArray(json, "roster", resourceId)) {
            JsonObject entry = element.getAsJsonObject();
            roster.add(new BlueprintRosterEntry(requiredString(entry, "entity_type", resourceId),
                    integer(entry, "minimum", 1), integer(entry, "maximum", 1), integer(entry, "weight", 1),
                    string(entry, "service_branch", "military"),
                    string(entry, "role", "")));
        }
        if (schema < 3) {
            return new BlueprintWorldgenProfile(
                    strings(json, "biomes"),
                    requiredString(json, "faction", resourceId),
                    integer(json, "site_radius", 32),
                    roster,
                    strings(json, "loot_markers"),
                    integer(json, "placement_weight", 1));
        }
        return new BlueprintWorldgenProfile(
                strings(json, "biomes"),
                requiredString(json, "faction", resourceId),
                BlueprintSiteKind.byId(requiredString(json, "site_kind", resourceId)),
                integer(json, "site_radius", 32),
                roster,
                stringMap(json, "loot_tables", resourceId),
                integer(json, "placement_weight", 1));
    }

    private static TemplateContents loadTemplate(ResourceManager manager, String templateId, Identifier descriptorId) {
        Identifier id = Identifier.parse(templateId);
        Identifier resourceId = Identifier.fromNamespaceAndPath(id.getNamespace(), "structure/" + id.getPath() + ".nbt");
        Resource resource = manager.getResource(resourceId).orElseThrow(() ->
                new IllegalArgumentException("Blueprint " + descriptorId + " references missing template " + templateId));
        try (InputStream input = resource.open()) {
            CompoundTag root = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            ListTag size = root.getListOrEmpty("size");
            if (size.size() != 3 || size.getIntOr(0, 0) <= 0 || size.getIntOr(1, 0) <= 0
                    || size.getIntOr(2, 0) <= 0 || size.getIntOr(0, 0) > 48 || size.getIntOr(1, 0) > 32
                    || size.getIntOr(2, 0) > 48) {
                throw new IllegalArgumentException("Unsafe template bounds for " + templateId);
            }
            ListTag palette = root.getListOrEmpty("palette");
            ListTag blocks = root.getListOrEmpty("blocks");
            ArrayList<BaseBlockPlacement> placements = new ArrayList<>();
            HashSet<String> occupiedPositions = new HashSet<>();
            int siteAnchors = 0;
            int commandPosts = 0;
            LinkedHashSet<String> lootMarkers = new LinkedHashSet<>();
            for (int index = 0; index < blocks.size(); index++) {
                CompoundTag block = blocks.getCompoundOrEmpty(index);
                int paletteIndex = block.getIntOr("state", -1);
                if (paletteIndex < 0 || paletteIndex >= palette.size()) {
                    throw new IllegalArgumentException("Invalid palette index in " + templateId);
                }
                CompoundTag state = palette.getCompoundOrEmpty(paletteIndex);
                String blockId = state.getStringOr("Name", "");
                ListTag position = block.getListOrEmpty("pos");
                if (position.size() != 3) {
                    throw new IllegalArgumentException("Invalid block position in " + templateId);
                }
                int x = position.getIntOr(0, 0);
                int y = position.getIntOr(1, 0);
                int z = position.getIntOr(2, 0);
                if (x < 0 || x >= size.getIntOr(0, 0)
                        || y < 0 || y >= size.getIntOr(1, 0)
                        || z < 0 || z >= size.getIntOr(2, 0)) {
                    throw new IllegalArgumentException("Block position outside template bounds in " + templateId);
                }
                if (!occupiedPositions.add(x + ":" + y + ":" + z)) {
                    throw new IllegalArgumentException("Duplicate block position in " + templateId);
                }
                if (blockId.equals("minecraft:structure_block")) {
                    String marker = block.getCompoundOrEmpty("nbt").getStringOr("metadata", "");
                    siteAnchors += marker.equals("site_anchor") ? 1 : 0;
                    commandPosts += marker.equals("command_post") ? 1 : 0;
                    if (marker.startsWith("loot:")) {
                        String lootMarker = marker.substring("loot:".length());
                        if (lootMarker.isBlank() || !lootMarkers.add(lootMarker)) {
                            throw new IllegalArgumentException("Invalid or duplicate loot marker in " + templateId);
                        }
                    }
                    continue;
                }
                LinkedHashMap<String, String> properties = new LinkedHashMap<>();
                state.getCompoundOrEmpty("Properties").forEach((key, value) ->
                        properties.put(key, state.getCompoundOrEmpty("Properties").getStringOr(key, "")));
                placements.add(new BaseBlockPlacement(x, y, z, blockId, blockId, properties));
            }
            for (int index = 0; index < root.getListOrEmpty("entities").size(); index++) {
                CompoundTag entity = root.getListOrEmpty("entities").getCompoundOrEmpty(index);
                String entityId = entity.getCompoundOrEmpty("nbt").getStringOr("id", "");
                requireRegistered(BuiltInRegistries.ENTITY_TYPE, entityId,
                        "template entity for " + templateId);
            }
            return new TemplateContents(
                    List.copyOf(placements), siteAnchors, commandPosts, Set.copyOf(lootMarkers));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read template " + templateId, exception);
        }
    }

    private record TemplateContents(
            List<BaseBlockPlacement> placements,
            int siteAnchorCount,
            int commandPostCount,
            Set<String> lootMarkers
    ) {
    }

    private static void validateRelations(Map<FactionId, FactionDefinition> factions) {
        for (FactionDefinition definition : factions.values()) {
            for (FactionId ally : definition.allies()) {
                FactionDefinition related = factions.get(ally);
                if (related == null || !related.allies().contains(definition.id())) {
                    throw new IllegalArgumentException("Alliance must be reciprocal: " + definition.id() + " <-> " + ally);
                }
            }
            for (FactionId enemy : definition.enemies()) {
                FactionDefinition related = factions.get(enemy);
                if (related == null || !related.enemies().contains(definition.id())) {
                    throw new IllegalArgumentException("Enemy relation must be reciprocal: " + definition.id() + " <-> " + enemy);
                }
            }
        }
    }

    private static List<ResourceJson> resources(ResourceManager manager, String path) throws IOException {
        FileToIdConverter converter = FileToIdConverter.json(path);
        ArrayList<ResourceJson> result = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> entry : converter.listMatchingResources(manager).entrySet()) {
            if (!entry.getKey().getNamespace().equals(GalacticWars.MODID)) {
                continue;
            }
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                result.add(new ResourceJson(converter.fileToId(entry.getKey()), JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Could not parse gameplay data resource " + entry.getKey(), exception);
            }
        }
        result.sort(java.util.Comparator.comparing(resource -> resource.id().toString()));
        return List.copyOf(result);
    }

    private static Set<FactionId> factionIds(JsonObject json, String key) {
        LinkedHashSet<FactionId> result = new LinkedHashSet<>();
        JsonArray values = json.has(key) ? json.getAsJsonArray(key) : new JsonArray();
        for (JsonElement value : values) {
            result.add(FactionId.of(value.getAsString()));
        }
        return Set.copyOf(result);
    }

    private static List<JsonObject> definitionObjects(ResourceJson resource, String arrayKey) {
        if (!resource.json().has(arrayKey)) {
            return List.of(resource.json());
        }
        JsonArray array = requiredArray(resource.json(), arrayKey, resource.id());
        ArrayList<JsonObject> definitions = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Resource " + resource.id()
                        + " contains a non-object in " + arrayKey);
            }
            definitions.add(element.getAsJsonObject());
        }
        return List.copyOf(definitions);
    }

    private static void requireSchema(ResourceJson resource, int supported) {
        int schema = integer(resource.json(), "schema_version", -1);
        if (schema != supported) {
            throw new IllegalArgumentException("Unsupported schema " + schema + " in " + resource.id());
        }
    }

    private static List<String> strings(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        ArrayList<String> values = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray(key)) {
            String value = element.getAsString().trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty()) {
                throw new IllegalArgumentException(key + " cannot contain a blank value");
            }
            values.add(value);
        }
        return List.copyOf(values);
    }

    private static Set<String> stringSet(JsonObject json, String key) {
        return Set.copyOf(strings(json, key));
    }

    private static Map<String, Integer> integerMap(
            JsonObject json,
            String key,
            Identifier resourceId
    ) {
        JsonObject object = requiredObject(json, key, resourceId);
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getAsInt());
        }
        return Map.copyOf(values);
    }

    private static Map<String, String> stringMap(
            JsonObject json,
            String key,
            Identifier resourceId
    ) {
        JsonObject object = requiredObject(json, key, resourceId);
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString() || entry.getValue().getAsString().isBlank()) {
                throw new IllegalArgumentException(
                        "Resource " + resourceId + " has invalid string map " + key);
            }
            values.put(entry.getKey(), entry.getValue().getAsString());
        }
        return Map.copyOf(values);
    }

    private static String requiredString(JsonObject json, String key, Identifier resourceId) {
        if (!json.has(key) || json.get(key).getAsString().isBlank()) {
            throw new IllegalArgumentException("Resource " + resourceId + " is missing " + key);
        }
        return json.get(key).getAsString();
    }

    private static JsonArray requiredArray(JsonObject json, String key, Identifier resourceId) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            throw new IllegalArgumentException("Resource " + resourceId + " is missing array " + key);
        }
        return json.getAsJsonArray(key);
    }

    private static JsonObject requiredObject(JsonObject json, String key, Identifier resourceId) {
        if (!json.has(key) || !json.get(key).isJsonObject()) {
            throw new IllegalArgumentException("Resource " + resourceId + " is missing object " + key);
        }
        return json.getAsJsonObject(key);
    }

    private static JsonObject object(JsonObject json, String key, JsonObject fallback) {
        return json.has(key) && json.get(key).isJsonObject() ? json.getAsJsonObject(key) : fallback;
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static int requiredInteger(JsonObject json, String key, Identifier resourceId) {
        if (!json.has(key) || !json.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("Resource " + resourceId + " is missing integer " + key);
        }
        return json.get(key).getAsInt();
    }

    private static double decimal(JsonObject json, String key, double fallback) {
        return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static void validateEquipment(ArmyUnitDefinition unit) {
        ArmyEquipmentLoadout equipment = unit.equipment();
        for (String itemId : List.of(
                equipment.mainHandItemId(), equipment.headItemId(), equipment.chestItemId(),
                equipment.legsItemId(), equipment.feetItemId())) {
            if (!itemId.isBlank()) {
                requireRegistered(BuiltInRegistries.ITEM, itemId, "equipment for " + unit.id());
            }
        }
    }

    private static <T> void requireRegistered(Registry<T> registry, String value, String label) {
        Identifier id;
        try {
            id = Identifier.parse(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid " + label + " resource id " + value, exception);
        }
        T registered = registry.getValue(id);
        if (registered == null || !id.equals(registry.getKey(registered))) {
            throw new IllegalArgumentException("Unknown " + label + " resource id " + id);
        }
    }

    private static GameplayDataSnapshot defaults() {
        return new GameplayDataSnapshot(
                new FactionCatalog(Map.of()),
                new ArmyUnitCatalog(List.of()),
                Map.of(),
                Map.of("galacticwars:mandalorian_rider", ArmyUnitId.of("galacticwars:mandalorian_warrior")),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                LaunchContentDefinitions.empty(),
                galacticwars.clonewars.technology.TechnologyCatalog.empty());
    }

    record LoadResult(Optional<GameplayDataSnapshot> snapshot, Optional<Throwable> failure) {
        static LoadResult success(GameplayDataSnapshot snapshot) {
            return new LoadResult(Optional.of(snapshot), Optional.empty());
        }

        static LoadResult failure(Throwable failure) {
            return new LoadResult(Optional.empty(), Optional.of(failure));
        }
    }

    private static String subjectPath(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    public record CurrentContent(GameplayDataSnapshot snapshot, GameplayContentState state) {
        public CurrentContent {
            java.util.Objects.requireNonNull(snapshot, "snapshot");
            java.util.Objects.requireNonNull(state, "state");
        }
    }

    private record ResourceJson(Identifier id, JsonObject json) {
    }
}
