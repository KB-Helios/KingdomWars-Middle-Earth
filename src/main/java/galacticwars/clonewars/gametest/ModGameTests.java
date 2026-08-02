package galacticwars.clonewars.gametest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import galacticwars.clonewars.Config;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyFieldCommandService;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyGroupLifecycleState;
import galacticwars.clonewars.army.ArmyGroupOrder;
import galacticwars.clonewars.army.ArmyLocation;
import galacticwars.clonewars.army.ArmyMemberSnapshot;
import galacticwars.clonewars.army.ArmyPatrolEnemyPolicy;
import galacticwars.clonewars.army.ArmyPatrolMode;
import galacticwars.clonewars.army.ArmyPatrolPlan;
import galacticwars.clonewars.army.ArmyPatrolState;
import galacticwars.clonewars.army.ArmyPatrolStatus;
import galacticwars.clonewars.army.ArmyPatrolWaypoint;
import galacticwars.clonewars.army.ArmyTravelService;
import galacticwars.clonewars.army.FieldCommandAction;
import galacticwars.clonewars.army.FieldCommandResult;
import galacticwars.clonewars.combat.BlasterCombatEvents;
import galacticwars.clonewars.combat.BlasterBoltEntity;
import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.combat.BlasterHeatPolicy;
import galacticwars.clonewars.combat.BlasterItem;
import galacticwars.clonewars.combat.LightsaberDeflectionService;
import galacticwars.clonewars.combat.RecruitAmmunitionService;
import galacticwars.clonewars.classes.ClassProgressSavedData;
import galacticwars.clonewars.classes.ClassAbilityEffectRegistry;
import galacticwars.clonewars.classes.PlayerClassRuntime;
import galacticwars.clonewars.economy.CreditTransactionService;
import galacticwars.clonewars.economy.PhysicalTradeService;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.data.LaunchContentRuntime;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.entity.ai.ArmyBrainMemoryTypes;
import galacticwars.clonewars.entity.ai.ArmyMarchMemory;
import galacticwars.clonewars.entity.ai.ArmyBrainState;
import galacticwars.clonewars.vehicle.GalacticVehicleEntity;
import galacticwars.clonewars.conquest.ConquestCaptureService;
import galacticwars.clonewars.conquest.ConquestControlState;
import galacticwars.clonewars.conquest.ConquestRuntimeEvents;
import galacticwars.clonewars.conquest.ConquestSavedData;
import galacticwars.clonewars.entity.RecruitSpawnEggItem;
import galacticwars.clonewars.entity.RecruitLifecycleService;
import galacticwars.clonewars.faction.FactionAlignmentSavedData;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRelation;
import galacticwars.clonewars.faction.ai.NpcDisposition;
import galacticwars.clonewars.faction.ai.NpcFactionAiService;
import galacticwars.clonewars.faction.ai.NpcRole;
import galacticwars.clonewars.force.ForceWorldEffectService;
import galacticwars.clonewars.force.ForceShrineService;
import galacticwars.clonewars.kingdom.BuildProject;
import galacticwars.clonewars.kingdom.CommandCenterDashboardState;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomClaim;
import galacticwars.clonewars.kingdom.KingdomActionId;
import galacticwars.clonewars.kingdom.KingdomGameplayAction;
import galacticwars.clonewars.kingdom.KingdomGameplayResult;
import galacticwars.clonewars.kingdom.KingdomGameplayRuntimeService;
import galacticwars.clonewars.kingdom.KingdomFactionRelations;
import galacticwars.clonewars.kingdom.KingdomMemberRole;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomRelation;
import galacticwars.clonewars.kingdom.KingdomSiege;
import galacticwars.clonewars.kingdom.SiegeState;
import galacticwars.clonewars.kingdom.SettlementRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.kingdom.RecruitmentCampaign;
import galacticwars.clonewars.kingdom.RecruitmentCampaignDecision;
import galacticwars.clonewars.kingdom.RecruitmentCampaignState;
import galacticwars.clonewars.kingdom.StorageEndpoint;
import galacticwars.clonewars.kingdom.WorkOrder;
import galacticwars.clonewars.kingdom.WorkOrderState;
import galacticwars.clonewars.kingdom.WorkOrderType;
import galacticwars.clonewars.kingdom.WorksiteRecord;
import galacticwars.clonewars.kingdom.WorksiteUpdateResult;
import galacticwars.clonewars.item.CommandTargetSelection;
import galacticwars.clonewars.menu.RecruitCommandMenu;
import galacticwars.clonewars.menu.RecruitLoadoutMenu;
import galacticwars.clonewars.menu.RecruitLoadoutMenuProvider;
import galacticwars.clonewars.menu.CommandCenterOperationsMenu;
import galacticwars.clonewars.menu.CommandCenterOperationsMenuProvider;
import galacticwars.clonewars.menu.CommandCenterNavigationMenu;
import galacticwars.clonewars.menu.FactionSelectionMenu;
import galacticwars.clonewars.menu.MerchantTradeMenu;
import galacticwars.clonewars.menu.MerchantTradeMenuProvider;
import galacticwars.clonewars.menu.StarterCampSetupMenu;
import galacticwars.clonewars.menu.WorksiteConfigurationAction;
import galacticwars.clonewars.menu.WorksiteConfigurationMenu;
import galacticwars.clonewars.menu.WorksiteConfigurationMenuProvider;
import galacticwars.clonewars.network.ClassActivatePayload;
import galacticwars.clonewars.network.ClassHudPayload;
import galacticwars.clonewars.network.ClassSelectPayload;
import galacticwars.clonewars.network.FieldCommandRequestPayload;
import galacticwars.clonewars.network.WorksiteActionPayload;
import galacticwars.clonewars.progression.LaunchContentCatalog;
import galacticwars.clonewars.progression.GalacticProgressionCoordinator;
import galacticwars.clonewars.progression.MissionAttemptSavedData;
import galacticwars.clonewars.progression.MissionRuntimeEvents;
import galacticwars.clonewars.progression.ProgressionEvent;
import galacticwars.clonewars.progression.ProgressionDecision;
import galacticwars.clonewars.progression.ProgressionEventType;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.progression.ProgressionState;
import galacticwars.clonewars.progression.ForceSavedData;
import galacticwars.clonewars.progression.ForceAbilityRuntimeService;
import galacticwars.clonewars.progression.ForceProgressionService;
import galacticwars.clonewars.recruitment.RecruitmentAction;
import galacticwars.clonewars.recruitment.RecruitDuty;
import galacticwars.clonewars.recruitment.RecruitmentPaymentService;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.registry.ModBlockTags;
import galacticwars.clonewars.registry.ModBlocks;
import galacticwars.clonewars.registry.ModEntityTypes;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.registry.ModDataComponents;
import galacticwars.clonewars.settlement.BaseBlockPlacement;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.settlement.ConstructionPlan;
import galacticwars.clonewars.settlement.ConstructionProjectService;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.settlement.StarterCampDeployment;
import galacticwars.clonewars.settlement.StarterCampDeploymentPhase;
import galacticwars.clonewars.settlement.StarterCampDeploymentService;
import galacticwars.clonewars.technology.KingdomResearchService;
import galacticwars.clonewars.technology.KingdomTechnologyState;
import galacticwars.clonewars.technology.ResearchResult;
import galacticwars.clonewars.workforce.WorkerPhase;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.WorkerStatus;
import galacticwars.clonewars.workforce.WorkerDutyLoadoutPolicy;
import galacticwars.clonewars.workforce.CourierDispatchMode;
import galacticwars.clonewars.workforce.CourierRouteExecutionState;
import galacticwars.clonewars.workforce.CourierRouteMode;
import galacticwars.clonewars.workforce.CourierTransferAction;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.SettlementSupplyLedger;
import galacticwars.clonewars.workforce.SupplyCategory;
import galacticwars.clonewars.workforce.SupplyDemand;
import galacticwars.clonewars.workforce.SupplyReservation;
import galacticwars.clonewars.workforce.WorkAreaBounds;
import galacticwars.clonewars.workforce.WorkerExecutionState;
import galacticwars.clonewars.world.PlanetTravelService;
import galacticwars.clonewars.world.PlanetTravelGameTests;
import galacticwars.clonewars.world.BlueprintSiteAnchorBlockEntity;
import galacticwars.clonewars.world.BlueprintSiteKind;
import galacticwars.clonewars.world.BlueprintSiteLootBlockEntity;
import galacticwars.clonewars.world.FactionCommandPostBlockEntity;
import galacticwars.clonewars.world.PlanetArrivalService;
import galacticwars.clonewars.world.PlanetFactionSpawnPolicy;
import galacticwars.clonewars.world.FactionOutpostRecord;
import galacticwars.clonewars.world.FactionOutpostMarkerService;
import galacticwars.clonewars.world.FactionOutpostSavedData;
import galacticwars.clonewars.world.FactionNaturalSpawnRules;
import galacticwars.clonewars.world.OverworldFactionSpawnProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.SpawnEggItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.tslat.smartbrainlib.util.BrainUtil;

public final class ModGameTests {
    private static final Identifier ENVIRONMENT = id("gameplay");
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final int SMART_BRAIN_TEST_SETUP_INTERVAL = 300;
    private static final int SMART_BRAIN_CHUNK_READY_TIMEOUT = 300;
    private static final int SMART_BRAIN_TEST_AREA_HORIZONTAL_SPACING = 160;
    private static final int SMART_BRAIN_TEST_AREA_VERTICAL_SPACING = 120;
    private static final int SMART_BRAIN_TEST_AREA_VERTICAL_LANES = 2;
    private static final int LOADOUT_WORKER_TOOL_SLOT = 1;
    private static final AtomicInteger SMART_BRAIN_AREA_SEQUENCE = new AtomicInteger();
    private static final Map<Identifier, Consumer<GameTestHelper>> TESTS = createTests();

    private ModGameTests() {
    }

    public static void registerTestFunctions(RegisterEvent event) {
        if (!GameTestHooks.isGametestEnabled()) {
            return;
        }
        event.register(Registries.TEST_FUNCTION, helper -> TESTS.forEach(helper::register));
    }

    public static void registerGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                ENVIRONMENT,
                new TestEnvironmentDefinition.AllOf(List.of()));
        Map<Identifier, Holder<TestEnvironmentDefinition<?>>> isolatedEnvironments =
                new LinkedHashMap<>(Map.of(
                id("ungrouped_recruit_ranged_brain"), event.registerEnvironment(
                        id("ungrouped_recruit_ranged_brain_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("ungrouped_recruit_melee_brain"), event.registerEnvironment(
                        id("ungrouped_recruit_melee_brain_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("smart_brain_follow_and_sit"), event.registerEnvironment(
                        id("smart_brain_follow_and_sit_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("smart_brain_move_command"), event.registerEnvironment(
                        id("smart_brain_move_command_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("smart_brain_move_stall_recovery"), event.registerEnvironment(
                        id("smart_brain_move_stall_recovery_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("natural_civilian_brain_runtime"), event.registerEnvironment(
                        id("natural_civilian_brain_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("grouped_brain_authority"), event.registerEnvironment(
                        id("grouped_brain_authority_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("local_recruit_protect_owner"), event.registerEnvironment(
                        id("local_recruit_protect_owner_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("command_marker_runtime"), event.registerEnvironment(
                        id("command_marker_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())),
                id("vehicle_embodied_runtime"), event.registerEnvironment(
                        id("vehicle_embodied_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of()))));
        isolatedEnvironments.put(id("force_embodied_runtime"), event.registerEnvironment(
                        id("force_embodied_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("field_command_authority"), event.registerEnvironment(
                        id("field_command_authority_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("grouped_patrol_pause_runtime"), event.registerEnvironment(
                        id("grouped_patrol_pause_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("grouped_protect_entity_runtime"), event.registerEnvironment(
                        id("grouped_protect_entity_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("grouped_zero_supply_blaster"), event.registerEnvironment(
                        id("grouped_zero_supply_blaster_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("recruit_blaster_ammunition"), event.registerEnvironment(
                        id("recruit_blaster_ammunition_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("worker_safety_and_upkeep"), event.registerEnvironment(
                        id("worker_safety_and_upkeep_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("recruit_hazard_and_self_care"), event.registerEnvironment(
                        id("recruit_hazard_and_self_care_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_farmer_door_lifecycle"), event.registerEnvironment(
                        id("black_box_farmer_door_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("recruit_door_command_resumption"), event.registerEnvironment(
                        id("recruit_door_command_resumption_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("specialist_worker_loops"), event.registerEnvironment(
                        id("specialist_worker_loops_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_cook_lifecycle"), event.registerEnvironment(
                        id("black_box_cook_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_lumberjack_lifecycle"), event.registerEnvironment(
                        id("black_box_lumberjack_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_miner_lifecycle"), event.registerEnvironment(
                        id("black_box_miner_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_miner_broken_tool_recovery"), event.registerEnvironment(
                        id("black_box_miner_broken_tool_recovery_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_miner_missing_tool_recovery"), event.registerEnvironment(
                        id("black_box_miner_missing_tool_recovery_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_animal_farmer_lifecycle"), event.registerEnvironment(
                        id("black_box_animal_farmer_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_fisher_lifecycle"), event.registerEnvironment(
                        id("black_box_fisher_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_merchant_lifecycle"), event.registerEnvironment(
                        id("black_box_merchant_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_builder_lifecycle"), event.registerEnvironment(
                        id("black_box_builder_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_courier_lifecycle"), event.registerEnvironment(
                        id("black_box_courier_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("black_box_technician_lifecycle"), event.registerEnvironment(
                        id("black_box_technician_lifecycle_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("hybrid_courier_dispatch"), event.registerEnvironment(
                        id("hybrid_courier_dispatch_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("competing_courier_leases"), event.registerEnvironment(
                        id("competing_courier_leases_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("courier_live_lease_reload"), event.registerEnvironment(
                        id("courier_live_lease_reload_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("courier_hall_removal"), event.registerEnvironment(
                        id("courier_hall_removal_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("commander_center_replay_runtime"), event.registerEnvironment(
                        id("commander_center_replay_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("planet_faction_outpost_runtime"), event.registerEnvironment(
                        id("planet_faction_outpost_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("planet_round_trip_home"), event.registerEnvironment(
                        id("planet_round_trip_home_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("recruit_spawn_eggs"), event.registerEnvironment(
                        id("recruit_spawn_eggs_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        isolatedEnvironments.put(id("faction_ai_reaction_runtime"), event.registerEnvironment(
                        id("faction_ai_reaction_runtime_environment"),
                        new TestEnvironmentDefinition.AllOf(List.of())));
        List<Identifier> smartBrainRuntimeTests = List.of(
                id("ungrouped_recruit_ranged_brain"),
                id("ungrouped_recruit_melee_brain"),
                id("smart_brain_follow_and_sit"),
                id("smart_brain_move_command"),
                id("smart_brain_move_stall_recovery"),
                id("natural_civilian_brain_runtime"),
                id("faction_ai_reaction_runtime"),
                id("planet_faction_outpost_runtime"),
                id("grouped_brain_authority"),
                id("grouped_zero_supply_blaster"),
                id("recruit_blaster_ammunition"),
                id("grouped_patrol_pause_runtime"),
                id("grouped_protect_entity_runtime"),
                id("worker_safety_and_upkeep"),
                id("recruit_hazard_and_self_care"),
                id("black_box_farmer_door_lifecycle"),
                id("recruit_door_command_resumption"),
                id("specialist_worker_loops"),
                id("black_box_cook_lifecycle"),
                id("black_box_lumberjack_lifecycle"),
                id("black_box_miner_lifecycle"),
                id("black_box_miner_broken_tool_recovery"),
                id("black_box_miner_missing_tool_recovery"),
                id("black_box_animal_farmer_lifecycle"),
                id("black_box_fisher_lifecycle"),
                id("black_box_merchant_lifecycle"),
                id("black_box_builder_lifecycle"),
                id("black_box_courier_lifecycle"),
                id("black_box_technician_lifecycle"),
                id("competing_courier_leases"),
                id("courier_live_lease_reload"),
                id("courier_hall_removal"),
                id("local_recruit_protect_owner"),
                id("command_marker_runtime"));
        for (Identifier testId : TESTS.keySet()) {
            int timeoutTicks = testId.equals(id("smart_brain_move_stall_recovery"))
                    ? 520
                    : Set.of(
                    id("grouped_brain_authority"),
                    id("grouped_protect_entity_runtime"),
                    id("vehicle_embodied_runtime")).contains(testId)
                    ? 600
                    : Set.of(
                    id("smart_brain_move_command"),
                    id("grouped_zero_supply_blaster"),
                    id("grouped_patrol_pause_runtime")).contains(testId)
                    ? 260
                    : testId.equals(id("smart_brain_follow_and_sit"))
                            ? 900
                            : Set.of(id("command_marker_runtime"), id("field_command_authority")).contains(testId)
                                    ? 180
                                    : testId.equals(id("mission_failure_retry_persistence"))
                                            ? 360
                                    : testId.equals(id("chapter_three_campaign_runtime"))
                                            ? 260
                                    : testId.equals(id("planet_faction_outpost_runtime"))
                                            ? 420
                                    : testId.equals(id("planet_round_trip_home"))
                                            ? 360
                                    : testId.equals(id("natural_civilian_brain_runtime"))
                                            ? 600
                                    : testId.equals(id("faction_ai_reaction_runtime"))
                                            ? 900
                                    : testId.equals(id("black_box_farmer_door_lifecycle"))
                                            ? 1_200
                                    : testId.equals(id("recruit_door_command_resumption"))
                                            ? 900
                                    : testId.equals(id("specialist_worker_loops"))
                                            ? 1_600
                                    : testId.equals(id("black_box_cook_lifecycle"))
                                            ? 900
                                    : testId.equals(id("black_box_lumberjack_lifecycle"))
                                            ? 1_000
                                    : testId.equals(id("black_box_miner_lifecycle"))
                                            ? 900
                                    : testId.equals(id("black_box_miner_broken_tool_recovery"))
                                            ? 1_500
                                    : testId.equals(id("black_box_miner_missing_tool_recovery"))
                                            ? 1_200
                                    : testId.equals(id("black_box_animal_farmer_lifecycle"))
                                            ? 900
                                    : testId.equals(id("black_box_fisher_lifecycle"))
                                            ? 1_000
                                    : testId.equals(id("black_box_merchant_lifecycle"))
                                            ? 900
                                    : testId.equals(id("black_box_builder_lifecycle"))
                                            ? 1_800
                                    : testId.equals(id("black_box_courier_lifecycle"))
                                            ? 1_200
                                    : testId.equals(id("black_box_technician_lifecycle"))
                                            ? 2_100
                                    : testId.equals(id("competing_courier_leases"))
                                            ? 700
                                    : testId.equals(id("courier_live_lease_reload"))
                                            ? 700
                                    : testId.equals(id("courier_hall_removal"))
                                            ? 900
                                    : testId.equals(id("worker_safety_and_upkeep"))
                                            ? 700
                                    : Set.of(
                                            id("local_recruit_protect_owner"),
                                            id("ungrouped_recruit_ranged_brain"),
                                            id("ungrouped_recruit_melee_brain"),
                                            id("recruit_hazard_and_self_care"),
                                            id("faction_selection_transaction")).contains(testId)
                                            ? 360
                                    : testId.equals(id("recruit_blaster_ammunition"))
                                            ? 800
                                    : testId.equals(id("blaster_friendly_fire"))
                                            ? 400
                                            : 100;
            if (testId.equals(id("faction_selection_transaction"))) {
                timeoutTicks = 2_000;
            }
            int runtimeTestIndex = smartBrainRuntimeTests.indexOf(testId);
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    isolatedEnvironments.getOrDefault(testId, environment),
                    EMPTY_STRUCTURE,
                    timeoutTicks,
                    runtimeTestIndex < 0
                            ? 0
                            : runtimeTestIndex * SMART_BRAIN_TEST_SETUP_INTERVAL,
                    true,
                    Rotation.NONE,
                    false,
                    1,
                    1,
                    false,
                    0);
            event.registerTest(testId, new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, testId),
                    data));
        }
    }

    private static Map<Identifier, Consumer<GameTestHelper>> createTests() {
        LinkedHashMap<Identifier, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put(id("command_center_player_interaction"),
                ModGameTests::commandCenterPlayerInteraction);
        tests.put(id("command_center_authority"), ModGameTests::kingdomHallAuthority);
        tests.put(id("command_center_multiplayer_invite"),
                ModGameTests::commandCenterMultiplayerInvite);
        tests.put(id("command_center_squad_orders"), ModGameTests::commandCenterSquadOrders);
        tests.put(id("command_center_workforce_control"), ModGameTests::commandCenterWorkforceControl);
        tests.put(id("technology_recipe_catalog"), TechnologyFabricationGameTests::loadedRecipeCatalog);
        tests.put(id("shared_kingdom_research"), TechnologyFabricationGameTests::sharedKingdomResearch);
        tests.put(id("atomic_fabrication_authority"),
                TechnologyFabricationGameTests::atomicFabricationAuthority);
        tests.put(id("command_marker_runtime"), ModGameTests::commandMarkerRuntime);
        tests.put(id("blueprint_projector_runtime"), ModGameTests::blueprintProjectorRuntime);
        tests.put(id("kingdom_governance_persistence"), ModGameTests::kingdomGovernancePersistence);
        tests.put(id("kingdom_multiplayer_runtime"), ModGameTests::kingdomMultiplayerRuntime);
        tests.put(id("overworld_faction_outpost_runtime"), ModGameTests::blueprintSiteRuntime);
        tests.put(id("commander_center_replay_runtime"), ModGameTests::commanderCenterReplayRuntime);
        tests.put(id("planet_structure_registry_runtime"), ModGameTests::planetStructureRegistryRuntime);
        tests.put(id("blueprint_site_compatible_hash"), ModGameTests::blueprintSiteCompatibleHash);
        tests.put(id("blueprint_site_content_hash_mismatch"), ModGameTests::blueprintSiteContentHashMismatch);
        tests.put(id("blueprint_unknown_block_rejection"), ModGameTests::blueprintUnknownBlockRejection);
        tests.put(id("chunk_generation_rejection_serialization"),
                ModGameTests::chunkGenerationRejectionSerialization);
        tests.put(id("natural_rejection_serialization"),
                ModGameTests::naturalRejectionSerialization);
        tests.put(id("planet_faction_outpost_runtime"), ModGameTests::planetFactionOutpostRuntime);
        tests.put(id("physical_trade_transaction"), ModGameTests::physicalTradeTransaction);
        tests.put(id("faction_trader_disposition_runtime"),
                ModGameTests::factionTraderDispositionRuntime);
        tests.put(id("faction_selection_transaction"), ModGameTests::factionSelectionTransaction);
        tests.put(id("progression_runtime_adapter"), ModGameTests::progressionRuntimeAdapter);
        tests.put(id("class_ability_runtime_data"), ModGameTests::classAbilityRuntimeData);
        tests.put(id("player_class_embodied_runtime"), ModGameTests::playerClassEmbodiedRuntime);
        tests.put(id("vehicle_runtime_contract"), ModGameTests::vehicleRuntimeContract);
        tests.put(id("vehicle_embodied_runtime"), ModGameTests::vehicleEmbodiedRuntime);
        tests.put(id("force_embodied_runtime"), ModGameTests::forceEmbodiedRuntime);
        tests.put(id("chapter_three_campaign_runtime"), ModGameTests::chapterThreeCampaignRuntime);
        tests.put(id("all_faction_campaign_paths"), ModGameTests::allFactionCampaignPaths);
        tests.put(id("mission_failure_retry_persistence"),
                ModGameTests::missionFailureRetryPersistence);
        tests.put(id("conquest_control_authority"), ModGameTests::conquestControlAuthority);
        tests.put(id("recruit_entity_contract"), ModGameTests::recruitEntityContract);
        tests.put(id("curated_npc_runtime_contracts"), ModGameTests::curatedNpcRuntimeContracts);
        tests.put(id("worker_tags_and_loot"), ModGameTests::workerTagsAndLoot);
        tests.put(id("recruit_contract_lifecycle"), ModGameTests::recruitContractLifecycle);
        tests.put(id("recruit_loadout_persistence_and_migration"),
                ModGameTests::recruitLoadoutPersistenceAndMigration);
        tests.put(id("kingdom_role_recruitment"), ModGameTests::kingdomRoleRecruitment);
        tests.put(id("local_recruit_protect_owner"), ModGameTests::localRecruitProtectOwner);
        tests.put(id("worker_resource_conservation"), ModGameTests::workerResourceConservation);
        tests.put(id("worker_safety_and_upkeep"), ModGameTests::workerSafetyAndUpkeep);
        tests.put(id("recruit_hazard_and_self_care"), ModGameTests::recruitHazardAndSelfCare);
        tests.put(id("black_box_farmer_door_lifecycle"),
                ModGameTests::blackBoxFarmerDoorLifecycle);
        tests.put(id("recruit_door_command_resumption"),
                ModGameTests::recruitDoorCommandResumption);
        tests.put(id("physical_logistics_transaction"),
                PhysicalLogisticsGameTests::atomicPhysicalTransfer);
        tests.put(id("enabled_worker_loops"), ModGameTests::enabledWorkerLoops);
        tests.put(id("specialist_worker_loops"), ModGameTests::specialistWorkerLoops);
        tests.put(id("black_box_cook_lifecycle"), ModGameTests::blackBoxCookLifecycle);
        tests.put(id("black_box_lumberjack_lifecycle"),
                ModGameTests::blackBoxLumberjackLifecycle);
        tests.put(id("black_box_miner_lifecycle"), ModGameTests::blackBoxMinerLifecycle);
        tests.put(id("black_box_miner_broken_tool_recovery"),
                ModGameTests::blackBoxMinerBrokenToolRecovery);
        tests.put(id("black_box_miner_missing_tool_recovery"),
                ModGameTests::blackBoxMinerMissingToolRecovery);
        tests.put(id("black_box_animal_farmer_lifecycle"),
                ModGameTests::blackBoxAnimalFarmerLifecycle);
        tests.put(id("black_box_fisher_lifecycle"), ModGameTests::blackBoxFisherLifecycle);
        tests.put(id("black_box_merchant_lifecycle"), ModGameTests::blackBoxMerchantLifecycle);
        tests.put(id("black_box_builder_lifecycle"), ModGameTests::blackBoxBuilderLifecycle);
        tests.put(id("black_box_courier_lifecycle"), ModGameTests::blackBoxCourierLifecycle);
        tests.put(id("black_box_technician_lifecycle"),
                ModGameTests::blackBoxTechnicianLifecycle);
        tests.put(id("bounded_worker_scans"), ModGameTests::boundedWorkerScans);
        tests.put(id("hybrid_courier_dispatch"), ModGameTests::hybridCourierDispatch);
        tests.put(id("competing_courier_leases"), ModGameTests::competingCourierLeases);
        tests.put(id("courier_live_lease_reload"), ModGameTests::courierLiveLeaseReload);
        tests.put(id("courier_hall_removal"), ModGameTests::courierHallRemoval);
        tests.put(id("animal_farmer_species_pairing"), ModGameTests::animalFarmerSpeciesPairing);
        tests.put(id("workforce_saved_data_authority"), ModGameTests::workforceSavedDataAuthority);
        tests.put(id("recruit_spawn_eggs"), ModGameTests::recruitSpawnEggs);
        tests.put(id("blaster_friendly_fire"), ModGameTests::blasterFriendlyFire);
        tests.put(id("lightsaber_bolt_deflection"), ModGameTests::lightsaberBoltDeflection);
        tests.put(id("recruit_blaster_projectile"), ModGameTests::recruitBlasterProjectile);
        tests.put(id("player_blaster_heat"), ModGameTests::playerBlasterHeat);
        tests.put(id("ungrouped_recruit_ranged_brain"), ModGameTests::ungroupedRecruitRangedBrain);
        tests.put(id("ungrouped_recruit_melee_brain"), ModGameTests::ungroupedRecruitMeleeBrain);
        tests.put(id("smart_brain_follow_and_sit"), ModGameTests::smartBrainFollowAndSit);
        tests.put(id("smart_brain_move_command"), ModGameTests::smartBrainMoveCommand);
        tests.put(id("smart_brain_move_stall_recovery"), ModGameTests::smartBrainMoveStallRecovery);
        tests.put(id("natural_civilian_brain_runtime"), ModGameTests::naturalCivilianBrainRuntime);
        tests.put(id("faction_ai_reaction_runtime"), ModGameTests::factionAiReactionRuntime);
        tests.put(id("grouped_brain_authority"), ModGameTests::groupedBrainAuthority);
        tests.put(id("grouped_zero_supply_blaster"), ModGameTests::groupedZeroSupplyBlaster);
        tests.put(id("recruit_blaster_ammunition"), ModGameTests::recruitBlasterAmmunition);
        tests.put(id("grouped_patrol_pause_runtime"), ModGameTests::groupedPatrolPauseRuntime);
        tests.put(id("grouped_protect_entity_runtime"), ModGameTests::groupedProtectEntityRuntime);
        tests.put(id("field_command_authority"), ModGameTests::fieldCommandAuthority);
        tests.put(id("external_library_runtime"), ModGameTests::externalLibraryRuntime);
        tests.put(id("planet_travel_failure_atomicity"), ModGameTests::planetTravelFailureAtomicity);
        tests.put(id("planet_arrival_runtime"), ModGameTests::planetArrivalRuntime);
        tests.put(id("planet_round_trip_home"), PlanetTravelGameTests::roundTripHome);
        tests.put(id("army_planet_transfer_transaction"), ModGameTests::armyPlanetTransferTransaction);
        tests.put(id("recruit_army_snapshot_cargo"), ModGameTests::recruitArmySnapshotCargo);
        return Map.copyOf(tests);
    }

    private static void chunkGenerationRejectionSerialization(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        GalacticRecruitEntity rejected = ModEntityTypes.JEDI_KNIGHT.get().create(
                level, EntitySpawnReason.NATURAL);
        if (rejected == null) {
            helper.fail("Could not create the rejected chunk-generation recruit fixture");
            return;
        }
        rejected.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        rejected.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                EntitySpawnReason.CHUNK_GENERATION,
                null);
        if (rejected.isRemoved() || !rejected.isPendingNaturalSpawnInitialization()) {
            helper.fail("Rejected chunk-generation recruit was initialized before serialization");
            return;
        }

        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess());
        if (!rejected.save(output)) {
            helper.fail("Rejected chunk-generation recruit did not remain serializable");
            return;
        }
        CompoundTag serialized = output.buildResult();
        String typeId = serialized.getString("id").orElse("");
        if (!typeId.equals("galacticwars:jedi_knight")) {
            helper.fail("Rejected chunk-generation recruit serialized without its type id: "
                    + serialized);
            return;
        }

        Entity loaded = EntityType.loadEntityRecursive(
                serialized,
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false),
                entity -> entity);
        if (!(loaded instanceof GalacticRecruitEntity pending)
                || !pending.isPendingNaturalSpawnInitialization()) {
            helper.fail("Deferred natural-spawn initialization did not survive entity persistence");
            return;
        }
        if (!level.addFreshEntity(pending)) {
            helper.fail("Could not add the persisted rejected recruit for first-tick cleanup");
            return;
        }
        pending.tick();
        if (!pending.isRemoved()) {
            helper.fail("Persisted rejected recruit survived its first server tick");
            return;
        }
        helper.succeed();
    }

    private static void naturalRejectionSerialization(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 3));
        GalacticRecruitEntity rejected = ModEntityTypes.JEDI_KNIGHT.get().create(
                level, EntitySpawnReason.NATURAL);
        if (rejected == null) {
            helper.fail("Could not create the rejected live natural-spawn fixture");
            return;
        }
        rejected.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        rejected.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                EntitySpawnReason.NATURAL,
                null);
        if (rejected.isRemoved() || !rejected.isPendingNaturalSpawnInitialization()) {
            helper.fail("Live natural spawn initialized before vanilla completed its add path");
            return;
        }

        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess());
        if (!rejected.save(output)) {
            helper.fail("Rejected live natural spawn did not remain serializable");
            return;
        }
        CompoundTag serialized = output.buildResult();
        if (!serialized.getString("id").orElse("").equals("galacticwars:jedi_knight")) {
            helper.fail("Rejected live natural spawn serialized without its type id: " + serialized);
            return;
        }
        Entity loaded = EntityType.loadEntityRecursive(
                serialized,
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false),
                entity -> entity);
        if (!(loaded instanceof GalacticRecruitEntity pending)
                || !pending.isPendingNaturalSpawnInitialization()) {
            helper.fail("Deferred live natural-spawn initialization did not survive persistence");
            return;
        }
        if (!level.addFreshEntity(pending)) {
            helper.fail("Could not add the persisted rejected live natural spawn");
            return;
        }
        pending.tick();
        if (!pending.isRemoved()) {
            helper.fail("Rejected live natural spawn survived its first server tick");
            return;
        }
        helper.succeed();
    }

    private static void classAbilityRuntimeData(GameTestHelper helper) {
        var snapshot = GameplayDataManager.snapshot();
        OverworldFactionSpawnProfile phaseICloneOutpost = snapshot
                .overworldSpawnProfileForEntity("galacticwars:phase_i_clone_trooper").orElse(null);
        OverworldFactionSpawnProfile phaseIArcOutpost = snapshot
                .overworldSpawnProfileForEntity("galacticwars:phase_i_arc_trooper").orElse(null);
        if (snapshot.unitClasses().size() != 21
                || snapshot.abilities().size() != 33
                || snapshot.factionPolicies().size() != 5
                || snapshot.launchContent().planets().size() != 4
                || snapshot.launchContent().vehicles().size() != 5
                || snapshot.launchContent().forceAbilities().size() != 31
                || snapshot.launchContent().forceTraditions().size() != 3
                || snapshot.launchContent().forceNodes().size() != 39
                || snapshot.launchContent().quests().size() != 24
                || snapshot.launchContent().missions().size() != 15
                || snapshot.launchContent().trades().size() != 13
                || snapshot.launchContent().conquestRegions().size() != 4
                || snapshot.launchContent().forceAbilities().values().stream()
                        .anyMatch(ability -> !ability.enabled())
                || phaseICloneOutpost == null
                || phaseICloneOutpost.branchFor("galacticwars:phase_i_clone_trooper")
                != NpcServiceBranch.MILITARY
                || phaseIArcOutpost == null
                || phaseIArcOutpost.branchFor("galacticwars:phase_i_arc_trooper")
                != NpcServiceBranch.MILITARY) {
            helper.fail("Atomic gameplay snapshot did not load the launch class catalogs");
            return;
        }
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        recruit.initializeFromSpawnEgg();
        if (!recruit.classProgressState().classId().equals("galacticwars:clone_trooper")) {
            helper.fail("Recruit did not derive its shared class state from the unit catalog");
            return;
        }
        var accepted = recruit.activateClassAbility(
                "galacticwars:suppressive_fire", helper.getLevel().getGameTime(),
                true, 8.0D, false);
        var replay = recruit.activateClassAbility(
                "galacticwars:suppressive_fire", helper.getLevel().getGameTime(),
                true, 8.0D, false);
        GalacticRecruitEntity failedEffectRecruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 2));
        failedEffectRecruit.initializeFromSpawnEgg();
        var failedEffect = failedEffectRecruit.activateClassAbility(
                "galacticwars:suppressive_fire",
                helper.getLevel().getGameTime(),
                true,
                8.0D,
                false,
                () -> false);
        if (!accepted.accepted()
                || accepted.state().resource() != 85
                || replay.accepted()
                || !replay.reason().equals("ability_cooldown")
                || failedEffect.accepted()
                || !failedEffect.reason().equals("effect_failed")
                || failedEffectRecruit.classProgressState().resource() != 100
                || !failedEffectRecruit.classProgressState().cooldownEnds().isEmpty()) {
            helper.fail("Recruit class activation was not server-authoritative and cooldown-safe");
            return;
        }
        helper.succeed();
    }

    private static void vehicleRuntimeContract(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        int lane = 1;
        for (var holder : ModEntityTypes.vehicles()) {
            GalacticVehicleEntity vehicle = helper.spawn(holder.get(), new BlockPos(lane++, 1, 2));
            vehicle.deploy(owner, "galacticwars:republic");
            var definition = GameplayDataManager.snapshot().launchContent().vehicles()
                    .get(vehicle.vehicleId());
            if (definition == null || vehicle.ownerId().filter(owner::equals).isEmpty()
                    || vehicle.fuel() != definition.fuelCapacity()
                    || vehicle.health() != definition.maxHealth()
                    || vehicle.syncedFuelCapacity() != definition.fuelCapacity()
                    || vehicle.syncedMaximumHealth() != definition.maxHealth()
                    || definition.seatRoles().size() != definition.seats()
                    || definition.fabricationInputs().isEmpty()) {
                helper.fail("Vehicle did not load its authoritative deployment contract: "
                        + vehicle.vehicleId());
                return;
            }
        }
        helper.succeed();
    }

    private static void conquestControlAuthority(GameTestHelper helper) {
        String region = "gametest_" + UUID.randomUUID();
        BlockPos beacon = helper.absolutePos(new BlockPos(4, 2, 4));
        ConquestControlState state = new ConquestControlState(region,
                helper.getLevel().dimension().identifier().toString(),
                beacon.getX(), beacon.getY(), beacon.getZ(),
                "galacticwars:republic", UUID.randomUUID().toString(), "", 120, 3L);
        ConquestSavedData data = ConquestSavedData.get(helper.getLevel());
        data.put(state);
        helper.getLevel().setBlockAndUpdate(beacon, Blocks.AIR.defaultBlockState());
        var definition = new LaunchContentDefinitions.ConquestRegionDefinition(
                region, "tatooine", 48, 120, 10,
                beacon.getX(), beacon.getZ(), 12, "hutt_cartel");
        var missingBeacon = ConquestCaptureService.tick(
                helper.getLevel(),
                definition,
                beacon);
        boolean restored = ConquestRuntimeEvents.ensureBeaconPresent(helper.getLevel(), beacon);
        BlockPos occupiedLandmark = beacon.offset(1, 0, 0);
        helper.getLevel().setBlockAndUpdate(occupiedLandmark, Blocks.OBSIDIAN.defaultBlockState());
        boolean overwroteOccupiedLandmark = ConquestRuntimeEvents.ensureBeaconPresent(
                helper.getLevel(), occupiedLandmark);
        if (!data.state(region).filter(state::equals).isPresent()
                || missingBeacon.accepted()
                || !missingBeacon.reason().equals("beacon_missing")
                || !restored
                || !helper.getLevel().getBlockState(beacon).is(ModBlocks.CONTROL_BEACON.get())
                || overwroteOccupiedLandmark
                || !helper.getLevel().getBlockState(occupiedLandmark).is(Blocks.OBSIDIAN)) {
            helper.fail("Conquest control was not retained by authoritative saved data");
            return;
        }
        helper.succeed();
    }

    private static void progressionRuntimeAdapter(GameTestHelper helper) {
        UUID playerId = UUID.randomUUID();
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        KingdomGameplayAction pledge = new KingdomGameplayAction(
                KingdomActionId.of("pledge", playerId),
                playerId,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic",
                1);
        KingdomGameplayResult first = KingdomGameplayRuntimeService.applyProgression(progression, pledge);
        KingdomGameplayResult retry = KingdomGameplayRuntimeService.applyProgression(progression, pledge);
        if (!first.accepted() || !first.changed()
                || !retry.accepted() || retry.changed()
                || !retry.reason().equals("duplicate_action")
                || !progression.state(playerId).factionId().equals("galacticwars:republic")) {
            helper.fail("Progression runtime adapter did not commit exactly once");
            return;
        }
        helper.succeed();
    }

    private static void factionSelectionTransaction(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 6);
        List<ChunkPos> forcedCampChunks = new java.util.ArrayList<>();
        int minCampChunkX = hallPos.getX() - 8 >> 4;
        int maxCampChunkX = hallPos.getX() + 8 >> 4;
        int minCampChunkZ = hallPos.getZ() - 8 >> 4;
        int maxCampChunkZ = hallPos.getZ() + 8 >> 4;
        for (int chunkX = minCampChunkX; chunkX <= maxCampChunkX; chunkX++) {
            for (int chunkZ = minCampChunkZ; chunkZ <= maxCampChunkZ; chunkZ++) {
                ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
                helper.getLevel().getChunkSource().updateChunkForced(chunk, true);
                helper.getLevel().getChunk(chunkX, chunkZ);
                forcedCampChunks.add(chunk);
            }
        }
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos floor = hallPos.offset(x, -1, z);
                helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(floor.above(3), Blocks.AIR.defaultBlockState());
            }
        }
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        if (!owner.teleportTo(
                helper.getLevel(),
                hall.getBlockPos().getX() + 0.5D,
                hall.getBlockPos().getY(),
                hall.getBlockPos().getZ() + 0.5D,
                Set.of(),
                0.0F,
                0.0F,
                true)) {
            helper.fail("Faction selection setup could not load the isolated Command Center chunk");
            return;
        }
        // Embedded GameTest players do not acknowledge a same-dimension teleport, so their
        // entity-ticking chunk ticket needs the same explicit update as SmartBrainTestArea.
        helper.getLevel().getChunkSource().move(owner);
        if (!hall.claim(owner)) {
            helper.fail("Faction selection setup could not claim the Command Center");
            return;
        }
        FactionSelectionMenu menu = new FactionSelectionMenu(0, owner.getInventory(), hall.getBlockPos());
        int separatistButton = menu.factionIds().indexOf("galacticwars:separatist");
        if (separatistButton < 0 || menu.clickMenuButton(owner, separatistButton)
                || KingdomSavedData.get(helper.getLevel()).kingdomForOwner(owner.getUUID()).isPresent()
                || !ProgressionSavedData.get(helper.getLevel()).state(owner.getUUID()).factionId().isEmpty()) {
            helper.fail("Faction selection changed state without the matching identity chip");
            return;
        }
        owner.getInventory().add(new ItemStack(ModItems.SEPARATIST_FACTION_TOKEN.get()));
        if (!menu.clickMenuButton(owner, separatistButton)) {
            helper.fail("Server rejected the Separatist faction selection");
            return;
        }
        KingdomRecord kingdom = KingdomSavedData.get(helper.getLevel())
                .kingdomForOwner(owner.getUUID()).orElse(null);
        String pledged = ProgressionSavedData.get(helper.getLevel()).state(owner.getUUID()).factionId();
        int alignment = FactionAlignmentSavedData.get(helper.getLevel()).alignment(owner.getUUID())
                .score(FactionId.of("galacticwars:separatist"));
        if (kingdom == null
                || !kingdom.factionId().equals("galacticwars:separatist")
                || !hall.factionId().equals("galacticwars:separatist")
                || !pledged.equals("galacticwars:separatist")
                || alignment <= 0
                || owner.getInventory().getNonEquipmentItems().stream()
                        .anyMatch(stack -> stack.is(ModItems.SEPARATIST_FACTION_TOKEN.get()))) {
            helper.fail("Faction selection did not atomically bind kingdom, Hall, progression, and alignment");
            return;
        }
        FactionSelectionMenu replay = new FactionSelectionMenu(1, owner.getInventory(), hall.getBlockPos());
        int differentFactionButton = separatistButton == 0 ? 1 : 0;
        if (differentFactionButton >= replay.factionIds().size()
                || replay.clickMenuButton(owner, differentFactionButton)
                || !KingdomSavedData.get(helper.getLevel()).kingdomForOwner(owner.getUUID())
                        .orElseThrow().factionId().equals("galacticwars:separatist")) {
            helper.fail("A second faction selection changed the committed kingdom faction");
            return;
        }
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        if (!progression.state(owner.getUUID()).hasSubjectPath(
                        ProgressionEventType.BUILDING_COMPLETED, "command_center")
                || progression.state(owner.getUUID()).hasSubject(
                        ProgressionEventType.QUEST_ADVANCED, "separatist_chapter_1")) {
            helper.fail("Faction pledge did not preserve the Command Center objective or advanced too early");
            return;
        }
        GalacticRecruitEntity reserveBuilder = ModEntityTypes.B1_BATTLE_DROID.get().create(
                helper.getLevel(), EntitySpawnReason.EVENT);
        if (reserveBuilder == null) {
            helper.fail("Starter camp reassignment fixture could not create its reserve soldier");
            return;
        }
        reserveBuilder.setPos(hallPos.getX() + 3.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        KingdomSavedData kingdomData = KingdomSavedData.get(helper.getLevel());
        if (!reserveBuilder.initializeStarterContract(owner, kingdom)
                || !helper.getLevel().addFreshEntity(reserveBuilder)
                || !kingdomData.registerRecruit(owner.getUUID(), reserveBuilder.getUUID())) {
            helper.fail("Starter camp reassignment fixture could not register its reserve soldier");
            return;
        }
        GalacticRecruitEntity secondReserve = ModEntityTypes.B1_BATTLE_DROID.get().create(
                helper.getLevel(), EntitySpawnReason.EVENT);
        if (secondReserve == null) {
            helper.fail("Starter camp reassignment fixture could not create its second reserve soldier");
            return;
        }
        secondReserve.setPos(hallPos.getX() + 4.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        if (!secondReserve.initializeStarterContract(owner, kingdom)
                || !helper.getLevel().addFreshEntity(secondReserve)
                || !kingdomData.registerRecruit(owner.getUUID(), secondReserve.getUUID())) {
            helper.fail("Starter camp reassignment fixture could not register its second reserve soldier");
            return;
        }
        int recruitsBeforeStarterContract = kingdomData.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlement().recruitIds().size();
        StarterCampSetupMenu campSetup = new StarterCampSetupMenu(
                2, owner.getInventory(), hall.getBlockPos());
        if (!campSetup.clickMenuButton(owner, StarterCampSetupMenu.SET_ROTATION_FIRST + 1)
                || !campSetup.clickMenuButton(owner, StarterCampSetupMenu.CONFIRM_DEPLOYMENT)) {
            helper.fail("Guided camp setup could not preview and confirm the east-facing footprint");
            return;
        }
        StarterCampDeployment deployment = kingdomData.starterCampDeployment(kingdom.id()).orElse(null);
        if (deployment == null
                || deployment.phase() != StarterCampDeploymentPhase.BUILDING
                || !deployment.contractGranted()
                || !deployment.suppliesGranted()
                || deployment.builderId().isEmpty()
                || deployment.projectId().isEmpty()
                || kingdomData.kingdomForOwner(owner.getUUID()).orElseThrow()
                        .settlement().recruitIds().size() != recruitsBeforeStarterContract + 1) {
            helper.fail("Faction pledge did not start one sealed starter-camp deployment: " + deployment);
            return;
        }
        int storedSupplies = countContainerItems(hall);
        UUID initialBuilderId = deployment.builderId().orElseThrow();
        UUID projectId = deployment.projectId().orElseThrow();
        GalacticRecruitEntity[] firstRecruit = {null};
        boolean[] replayChecked = {false};
        boolean[] scenarioRun = {false};
        boolean[] chunksReleased = {false};
        long startedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (scenarioRun[0]) {
                return;
            }
            if (!replayChecked[0]) {
                if (!(helper.getLevel().getEntity(initialBuilderId) instanceof GalacticRecruitEntity indexedRecruit)) {
                    if (helper.getTick() - startedAt >= 120L) {
                        scenarioRun[0] = true;
                        if (!chunksReleased[0]) {
                            forcedCampChunks.forEach(chunk ->
                                    helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                            chunksReleased[0] = true;
                        }
                        helper.fail("Starter builder was persisted but never indexed in the server level");
                    }
                    return;
                }
                if (indexedRecruit.getWorkerCarriedItemCount() == 0
                        && !moveOneContainerItemToWorker(hall, indexedRecruit, Items.OAK_LOG)) {
                    scenarioRun[0] = true;
                    if (!chunksReleased[0]) {
                        forcedCampChunks.forEach(chunk ->
                                helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                        chunksReleased[0] = true;
                    }
                    helper.fail("Starter builder could not stage a carried material for reassignment conservation");
                    return;
                }
                int suppliesAcrossStorageAndBuilder = countContainerItems(hall)
                        + indexedRecruit.getWorkerCarriedItemCount();
                if (!campSetup.clickMenuButton(owner, StarterCampSetupMenu.REASSIGN_BUILDER)) {
                    scenarioRun[0] = true;
                    if (!chunksReleased[0]) {
                        forcedCampChunks.forEach(chunk ->
                                helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                        chunksReleased[0] = true;
                    }
                    helper.fail("Starter camp did not permit an explicit loaded reserve-builder reassignment");
                    return;
                }
                StarterCampDeployment reassigned = kingdomData.starterCampDeployment(kingdom.id()).orElse(null);
                if (reassigned == null
                        || reassigned.builderId().filter(id -> !id.equals(initialBuilderId)).isEmpty()
                        || indexedRecruit.getWorkerProfession().isPresent()
                        || indexedRecruit.getWorkerCarriedItemCount() != 0
                        || countContainerItems(hall) != suppliesAcrossStorageAndBuilder) {
                    scenarioRun[0] = true;
                    if (!chunksReleased[0]) {
                        forcedCampChunks.forEach(chunk ->
                                helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                        chunksReleased[0] = true;
                    }
                    helper.fail("Starter camp reassignment changed the contract, stranded materials, failed to release the prior builder, or did not select a different candidate, reassigned=" + reassigned);
                    return;
                }
                firstRecruit[0] = reserveBuilder;
                StarterCampDeploymentService.Result deploymentReplay = StarterCampDeploymentService.deploy(
                        helper.getLevel(), owner, hall);
                StarterCampDeployment replayed = deploymentReplay.deployment().orElse(null);
                if (reserveBuilder.getType() != ModEntityTypes.B1_BATTLE_DROID.get()
                        || !deploymentReplay.accepted()
                        || replayed == null
                        || replayed.builderId().filter(reserveBuilder.getUUID()::equals).isEmpty()
                        || replayed.projectId().filter(projectId::equals).isEmpty()
                        || kingdomData.kingdomForOwner(owner.getUUID()).orElseThrow()
                                .settlement().recruitIds().size() != recruitsBeforeStarterContract + 1
                        || countContainerItems(hall) != storedSupplies) {
                    scenarioRun[0] = true;
                    if (!chunksReleased[0]) {
                        forcedCampChunks.forEach(chunk ->
                                helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                        chunksReleased[0] = true;
                    }
                    helper.fail("Starter contract or sealed supplies duplicated on deployment replay");
                    return;
                }
                replayChecked[0] = true;
            }
            StarterCampDeployment current = kingdomData.starterCampDeployment(kingdom.id()).orElse(null);
            if (current == null || current.phase() != StarterCampDeploymentPhase.COMPLETE) {
                if (helper.getTick() - startedAt >= 1_800L) {
                    scenarioRun[0] = true;
                    if (!chunksReleased[0]) {
                        forcedCampChunks.forEach(chunk ->
                                helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                        chunksReleased[0] = true;
                    }
                    BuildProject project = current == null || current.projectId().isEmpty()
                            ? null
                            : kingdomData.kingdomForOwner(owner.getUUID()).orElseThrow()
                                    .settlement().buildProjects().stream()
                                    .filter(candidate -> current.projectId().orElseThrow().equals(candidate.id()))
                                    .findFirst().orElse(null);
                    helper.fail("Starter builder did not physically finish camp: deployment=" + current
                            + ", project=" + project
                            + ", worker=" + firstRecruit[0].getWorkerStatus()
                            + ", ticks=" + firstRecruit[0].tickCount);
                }
                return;
            }
            owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
            helper.getLevel().getEntitiesOfClass(
                            GalacticRecruitEntity.class,
                            new AABB(hallPos).inflate(48.0D),
                            recruit -> recruit.getOwnerReference() == null
                                    && recruit.factionIdForGameplay().equals("galacticwars:republic"))
                    .forEach(GalacticRecruitEntity::discard);
            ProgressionState completed = progression.state(owner.getUUID());
            if (!completed.hasSubject(
                    ProgressionEventType.QUEST_ADVANCED, "separatist_chapter_1")) {
                if (helper.getTick() - startedAt >= 1_800L) {
                    scenarioRun[0] = true;
                    helper.fail("Starter camp completed but its active defense mission did not finish: "
                            + MissionAttemptSavedData.get(helper.getLevel())
                            .forPlayer(owner.getUUID()));
                }
                return;
            }
            scenarioRun[0] = true;
            if (!chunksReleased[0]) {
                forcedCampChunks.forEach(chunk ->
                        helper.getLevel().getChunkSource().updateChunkForced(chunk, false));
                chunksReleased[0] = true;
            }
            CommandCenterOperationsMenu operations = (CommandCenterOperationsMenu)
                    new CommandCenterOperationsMenuProvider(hallPos)
                            .createMenu(2, owner.getInventory(), owner);
            boolean chapterVisible = operations.dashboardState().activeQuest()
                    .filter(quest -> quest.questId().equals("separatist_chapter_2"))
                    .isPresent();
            int creditsBeforeClaim = RecruitmentPaymentService.creditCount(owner);
            boolean rewardClaimed = operations.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.CLAIM_REWARDS);
            int creditsAfterClaim = RecruitmentPaymentService.creditCount(owner);
            boolean duplicateRejected = !operations.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.CLAIM_REWARDS);
            KingdomBaseBlueprint starterBlueprint = GameplayDataManager.snapshot()
                    .blueprint(KingdomBaseBlueprint.STARTER_CAMP_ID).orElseThrow();
            boolean worldComplete = java.util.stream.IntStream.range(
                    0, starterBlueprint.placements().size()).allMatch(index -> {
                        var placement = starterBlueprint.rotatedPlacement(index, current.rotationSteps());
                        BlockPos target = hallPos.offset(placement.x(), placement.y(), placement.z());
                        return BuiltInRegistries.BLOCK.getKey(helper.getLevel().getBlockState(target).getBlock())
                                .toString().equals(placement.blockId());
                    });
            StarterCampDeploymentService.Result completedReplay = StarterCampDeploymentService.deploy(
                    helper.getLevel(), owner, hall);
            if (!completed.hasSubject(
                    ProgressionEventType.QUEST_ADVANCED, "separatist_chapter_1")
                    || completed.pendingCreditRewards() != 40
                    || !chapterVisible
                    || !rewardClaimed
                    || creditsAfterClaim != creditsBeforeClaim + 40
                    || progression.pendingCreditRewards(owner.getUUID()) != 0
                    || !duplicateRejected
                    || !worldComplete
                    || firstRecruit[0].getWorkerProfession().isPresent()
                    || !completedReplay.accepted()
                    || !completedReplay.reason().equals("already_complete")
                    || kingdomData.kingdomForOwner(owner.getUUID()).orElseThrow()
                            .settlement().recruitIds().size() != recruitsBeforeStarterContract + 1) {
                helper.fail("Fresh survival chapter one did not build and pay exactly once: completed="
                        + completed.hasSubject(
                        ProgressionEventType.QUEST_ADVANCED, "separatist_chapter_1")
                        + ", pending=" + completed.pendingCreditRewards()
                        + ", chapterVisible=" + chapterVisible
                        + ", rewardClaimed=" + rewardClaimed
                        + ", credits=" + creditsBeforeClaim + "->" + creditsAfterClaim
                        + ", remaining=" + progression.pendingCreditRewards(owner.getUUID())
                        + ", duplicateRejected=" + duplicateRejected
                        + ", worldComplete=" + worldComplete
                        + ", worker=" + firstRecruit[0].getWorkerStatus());
                return;
            }
            helper.succeed();
        });
    }

    private static void planetArrivalRuntime(GameTestHelper helper) {
        BlockPos first = PlanetArrivalService.findOrCreate(helper.getLevel()).orElse(null);
        BlockPos repeated = PlanetArrivalService.findOrCreate(helper.getLevel()).orElse(null);
        if (first == null || !first.equals(repeated)) {
            helper.fail("Planet arrival platform was unavailable or not stably reused");
            return;
        }
        if (!helper.getLevel().getBlockState(first.below()).is(ModBlocks.DURACRETE.get())
                || !helper.getLevel().getBlockState(first).canBeReplaced()
                || !helper.getLevel().getBlockState(first.above()).canBeReplaced()) {
            helper.fail("Planet arrival platform did not provide a solid floor and clear player volume");
            return;
        }
        helper.succeed();
    }

    private static void planetTravelFailureAtomicity(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 7);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData.get(helper.getLevel()).foundKingdom(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos);
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        progression.apply(new ProgressionEvent(
                UUID.randomUUID(), owner.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1));
        progression.apply(new ProgressionEvent(
                UUID.randomUUID(), owner.getUUID(), ProgressionEventType.BUILDING_COMPLETED,
                "forward_base", 1));

        CommandCenterNavigationMenu navigation = new CommandCenterNavigationMenu(
                0, owner.getInventory());
        if (!navigation.destinationIds().equals(PlanetTravelService.navigationDestinations())) {
            helper.fail("Navigation menu did not capture the authoritative destination snapshot");
            return;
        }

        PlanetTravelService.TravelResult result = PlanetTravelService.travel(owner, "tatooine");
        if (result.accepted() || !result.reason().equals("destination_unavailable")) {
            helper.fail("Missing GameTest destination was not rejected atomically: " + result.reason());
            return;
        }
        if (owner.level() != helper.getLevel()) {
            helper.fail("Failed planet travel moved the player");
            return;
        }
        if (progression.state(owner.getUUID()).hasSubject(ProgressionEventType.PLANET_VISITED, "tatooine")) {
            helper.fail("Failed planet travel emitted a visited progression event");
            return;
        }
        helper.succeed();
    }

    private static void recruitBlasterProjectile(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity shooter = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        GalacticRecruitEntity target = helper.spawn(
                ModEntityTypes.B1_BATTLE_DROID.get(), new BlockPos(12, 1, 2));
        shooter.tame(owner);
        ItemStack weapon = new ItemStack(ModItems.DC15_BLASTER.get());
        shooter.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        shooter.createCargoContainer().setItem(
                0, new ItemStack(ModItems.ENERGY_CELL.get()));
        if (!RecruitAmmunitionService.tryConsumeForShot(shooter)
                || countContainerItem(
                shooter.createCargoContainer(), ModItems.ENERGY_CELL.get()) != 0) {
            helper.fail("Tamed recruit projectile fixture could not consume its physical Energy Cell");
            return;
        }
        ModItems.DC15_BLASTER.get().fireAt(helper.getLevel(), shooter, target, weapon);

        GalacticRecruitEntity archer = helper.spawn(
                ModEntityTypes.NIGHTSISTER_ARCHER.get(), new BlockPos(2, 1, 4));
        GalacticRecruitEntity bowTarget = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(12, 1, 4));
        archer.tame(owner);
        ItemStack bow = new ItemStack(ModItems.NIGHTSISTER_BOW.get());
        archer.setItemInHand(InteractionHand.MAIN_HAND, bow);
        FactionRangedWeaponService.fireNightsisterBow(helper.getLevel(), archer, bowTarget, bow);
        AABB blasterShotArea = shooter.getBoundingBox().inflate(20.0D);
        AABB bowShotArea = archer.getBoundingBox().inflate(20.0D);
        helper.succeedWhen(() -> {
            List<BlasterBoltEntity> bolts = helper.getLevel().getEntitiesOfClass(
                    BlasterBoltEntity.class, blasterShotArea, bolt -> bolt.getOwner() == shooter);
            if (bolts.size() != 1
                    || bolts.getFirst().pickup
                            != net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.DISALLOWED
                    || !bolts.getFirst().getWeaponItem().is(ModItems.DC15_BLASTER.get())) {
                helper.fail("Recruit blaster did not spawn one owned, weapon-tagged bolt");
            }
            List<Arrow> arrows = helper.getLevel().getEntitiesOfClass(
                    Arrow.class, bowShotArea, arrow -> arrow.getOwner() == archer);
            if (arrows.size() != 1 || !arrows.getFirst().getWeaponItem().is(ModItems.NIGHTSISTER_BOW.get())) {
                helper.fail("Nightsister Archer did not spawn one owned, bow-tagged ranged projectile");
            }
        });
    }

    private static void armyPlanetTransferTransaction(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 8);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.foundKingdom(owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject completedBase = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), hallPos.offset(0, 16, 0));
        if (!data.completeBuildProject(owner.getUUID(), completedBase, forwardBase)) {
            helper.fail("Army travel setup could not unlock a commander slot");
            return;
        }

        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        GalacticRecruitEntity commander = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        GalacticRecruitEntity member = helper.spawn(
                ModEntityTypes.ARC_TROOPER.get(), new BlockPos(3, 1, 2));
        commander.tick();
        member.tick();
        owner.setPos(commander.getX(), commander.getY(), commander.getZ());
        if (!commander.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !member.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Army travel setup could not hire and promote its squad");
            return;
        }
        var group = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                new ArmyLocation(helper.getLevel().dimension().identifier().toString(),
                        commander.getX(), commander.getY(), commander.getZ()),
                helper.getLevel().getGameTime()).orElseThrow();
        BlockPos arrival = hallPos.offset(0, 32, 0);

        ArmyTravelService.TravelPlan rollbackPlan = ArmyTravelService.prepare(
                data, owner, helper.getLevel(), arrival);
        boolean reserved = rollbackPlan.reserve();
        boolean rolledBack = reserved && rollbackPlan.rollback(helper.getLevel().getGameTime());
        if (!rollbackPlan.accepted() || !rollbackPlan.transfersSquad() || !reserved || !rolledBack) {
            helper.fail("Army travel reservation could not be rolled back atomically; accepted="
                    + rollbackPlan.accepted() + ", reason=" + rollbackPlan.reason()
                    + ", transfers=" + rollbackPlan.transfersSquad()
                    + ", reserved=" + reserved + ", rolledBack=" + rolledBack);
            return;
        }
        var restored = data.armyGroup(group.id()).orElseThrow();
        if (restored.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || commander.isRemoved() || member.isRemoved()) {
            helper.fail("Army travel rollback changed live squad entities");
            return;
        }

        ArmyTravelService.TravelPlan committedPlan = ArmyTravelService.prepare(
                data, owner, helper.getLevel(), arrival);
        if (!committedPlan.reserve()) {
            helper.fail("Army travel transaction could not reserve its second attempt");
            return;
        }
        committedPlan.commit();
        var transferred = data.armyGroup(group.id()).orElseThrow();
        if (transferred.simulation().lifecycleState() != ArmyGroupLifecycleState.VIRTUAL
                || transferred.snapshots().size() != 2
                || transferred.simulation().anchor().blockPosition().x() != arrival.getX()
                || !commander.isRemoved()
                || !member.isRemoved()) {
            helper.fail("Army travel commit did not virtualize, relocate, and remove the source squad");
            return;
        }
        UUID lateRecruit = UUID.randomUUID();
        if (!data.registerRecruit(owner.getUUID(), lateRecruit)
                || data.addRecruitToArmy(owner.getUUID(), group.id(), lateRecruit)
                || data.addRecruitToArmy(owner.getUUID(), lateRecruit)
                || data.armyGroup(group.id()).orElseThrow().snapshots().size() != 2) {
            helper.fail("Virtual squad accepted a member without an authoritative materialization snapshot");
            return;
        }
        helper.succeed();
    }

    private static void recruitArmySnapshotCargo(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        recruit.tick();
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.tame(owner);
        setRecruitField(recruit, "kingdomId", UUID.randomUUID());

        ItemStack mainHand = new ItemStack(Items.IRON_SWORD);
        mainHand.setDamageValue(17);
        ItemStack offHand = new ItemStack(Items.SHIELD);
        offHand.setDamageValue(9);
        recruit.setItemSlot(EquipmentSlot.MAINHAND, mainHand);
        recruit.setItemSlot(EquipmentSlot.OFFHAND, offHand);
        recruit.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        recruit.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        recruit.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        recruit.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));

        Container cargo = recruit.createCargoContainer();
        cargo.setItem(0, new ItemStack(Items.DIAMOND, 3));
        cargo.setItem(8, new ItemStack(Items.GOLD_INGOT, 27));
        if (cargo.getContainerSize() != ArmyMemberSnapshot.CARGO_SLOT_COUNT || !cargo.stillValid(owner)) {
            helper.fail("Recruit cargo did not expose a valid nine-slot server container");
            return;
        }

        ArmyMemberSnapshot snapshot = recruit.createArmySnapshot(7L).orElse(null);
        if (snapshot == null) {
            helper.fail("Recruit could not create its army snapshot");
            return;
        }
        snapshot.equipment().mainHand().setDamageValue(1);
        snapshot.cargo().getFirst().setCount(1);

        recruit.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        recruit.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        recruit.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        recruit.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        recruit.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        recruit.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        cargo.clearContent();
        recruit.restoreArmySnapshot(snapshot, UUID.randomUUID());

        Container restoredCargo = recruit.createCargoContainer();
        if (!recruit.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_SWORD)
                || recruit.getItemBySlot(EquipmentSlot.MAINHAND).getDamageValue() != 17
                || !recruit.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD)
                || recruit.getItemBySlot(EquipmentSlot.OFFHAND).getDamageValue() != 9
                || !recruit.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)
                || !recruit.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE)
                || !recruit.getItemBySlot(EquipmentSlot.LEGS).is(Items.IRON_LEGGINGS)
                || !recruit.getItemBySlot(EquipmentSlot.FEET).is(Items.IRON_BOOTS)
                || !restoredCargo.getItem(0).is(Items.DIAMOND)
                || restoredCargo.getItem(0).getCount() != 3
                || !restoredCargo.getItem(8).is(Items.GOLD_INGOT)
                || restoredCargo.getItem(8).getCount() != 27) {
            helper.fail("Army snapshot did not restore complete recruit equipment and cargo");
            return;
        }
        helper.succeed();
    }

    private static void playerBlasterHeat(GameTestHelper helper) {
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ItemStack weapon = new ItemStack(ModItems.DC15_BLASTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        player.getInventory().add(new ItemStack(ModItems.ENERGY_CELL.get(), 8));

        for (int shot = 0; shot < BlasterHeatPolicy.SHOTS_BEFORE_OVERHEAT; shot++) {
            InteractionResult result = ModItems.DC15_BLASTER.get().use(
                    helper.getLevel(), player, InteractionHand.MAIN_HAND);
            if (result != InteractionResult.SUCCESS) {
                helper.fail("Player blaster rejected legal shot " + shot);
                return;
            }
            for (int tick = 0; tick < BlasterHeatPolicy.SHOT_COOLDOWN_TICKS; tick++) {
                ModItems.DC15_BLASTER.get().inventoryTick(
                        weapon, helper.getLevel(), player, EquipmentSlot.MAINHAND);
            }
        }

        BlasterHeatPolicy.BlasterHeatState overheated = weapon.get(ModDataComponents.BLASTER_HEAT.get());
        if (overheated == null || overheated.overheatTicks() <= 0
                || ModItems.DC15_BLASTER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        != InteractionResult.FAIL) {
            helper.fail("Player blaster did not persist or enforce its overheat state");
            return;
        }
        for (int tick = 0; tick < BlasterHeatPolicy.OVERHEAT_TICKS; tick++) {
            ModItems.DC15_BLASTER.get().inventoryTick(
                    weapon, helper.getLevel(), player, EquipmentSlot.MAINHAND);
        }
        if (!BlasterItem.heat(weapon).isReady()) {
            helper.fail("Player blaster did not return to ready after venting");
            return;
        }
        helper.succeed();
    }

    private static void ungroupedRecruitRangedBrain(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -2, 6, -2, 6);
        GalacticRecruitEntity shooter = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity target = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(4, 1, 1));
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        target.setHealth(target.getMaxHealth());
        shooter.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DC15_BLASTER.get()));
        if (!shooter.createCargoContainer().isEmpty()) {
            helper.fail("Natural ranged recruit unexpectedly began with player-managed cargo ammunition");
            return;
        }
        if (!(shooter.getBrain() instanceof net.tslat.smartbrainlib.api.internal.SmartBrain<?>)) {
            helper.fail("Recruit did not receive a SmartBrainLib brain");
            return;
        }
        AABB shotArea = shooter.getBoundingBox().inflate(20.0D);
        helper.succeedWhen(() -> {
            List<BlasterBoltEntity> bolts = helper.getLevel().getEntitiesOfClass(
                    BlasterBoltEntity.class, shotArea, bolt -> bolt.getOwner() == shooter);
            if (bolts.isEmpty()) {
                helper.fail("Ungrouped ranged recruit brain did not acquire and attack its enemy: nearest="
                        + BrainUtil.getMemory(shooter,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_ATTACKABLE)
                        + ", attack=" + BrainUtil.getMemory(shooter,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        + ", target=" + shooter.getTarget()
                        + ", tickCount=" + shooter.tickCount
                        + ", running=" + shooter.getBrain().getRunningBehaviors());
                return;
            }
            if (!bolts.isEmpty()
                    && !bolts.getFirst().getWeaponItem().is(ModItems.DC15_BLASTER.get())) {
                helper.fail("Ungrouped ranged recruit brain fired a bolt without its weapon identity");
                return;
            }
            if (!shooter.createCargoContainer().isEmpty()) {
                helper.fail("Natural faction recruit consumed or created player-managed cargo ammunition");
                return;
            }
            shooter.discard();
            target.discard();
            bolts.forEach(BlasterBoltEntity::discard);
        });
    }

    private static void recruitBlasterAmmunition(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -2, 48, -3, 5);
        ServerPlayer owner = area.player();
        BlockPos shooterPosition = area.at(2, 1, 1);
        BlockPos targetNearPosition = area.at(12, 1, 1);
        BlockPos targetFarPosition = area.at(44, 1, 1);
        GalacticRecruitEntity shooter = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), shooterPosition);
        GalacticRecruitEntity target = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), targetNearPosition);
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        target.setHealth(target.getMaxHealth());
        shooter.tame(owner);
        shooter.setItemInHand(
                InteractionHand.MAIN_HAND, new ItemStack(ModItems.DC15_BLASTER.get()));
        shooter.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        owner.setPos(shooter.getX(), shooter.getY(), shooter.getZ());
        Container cargo = shooter.createCargoContainer();
        cargo.setItem(0, new ItemStack(ModItems.ENERGY_CELL.get()));
        BlockPos wallBase = area.at(7, 1, 1);
        AABB shotArea = shooter.getBoundingBox().inflate(64.0D);
        Set<UUID> observedBolts = new java.util.HashSet<>();
        int[] phase = {-1};
        int[] phaseStartedRecruitTick = {shooter.tickCount};
        long indexWaitStartedTick = helper.getTick();
        boolean[] complete = {false};

        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            helper.getLevel().getEntitiesOfClass(
                            BlasterBoltEntity.class,
                            shotArea,
                            bolt -> bolt.getOwner() == shooter)
                    .forEach(bolt -> observedBolts.add(bolt.getUUID()));
            int cells = countContainerItem(cargo, ModItems.ENERGY_CELL.get());
            if (shooter.isWithinMeleeAttackRange(target)) {
                complete[0] = true;
                helper.fail("Empty blaster recruit silently closed into melee range");
                return;
            }

            int elapsed = shooter.tickCount - phaseStartedRecruitTick[0];
            switch (phase[0]) {
                case -1 -> {
                    boolean shooterIndexed = helper.getLevel().getEntity(shooter.getUUID()) == shooter;
                    boolean targetIndexed = helper.getLevel().getEntity(target.getUUID()) == target;
                    // The out-of-range phase spans several chunks. Under aggregate load,
                    // the shooter's chunk can tick before the far target's chunk does.
                    boolean areaTicking = areSmartBrainAreaChunksTicking(
                            helper, area, -2, 48, -3, 5);
                    boolean entitiesTicked = shooter.tickCount > 0 && target.tickCount > 0;
                    if (!shooterIndexed || !targetIndexed || !areaTicking || !entitiesTicked) {
                        if (helper.getTick() - indexWaitStartedTick
                                >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                            complete[0] = true;
                            helper.fail("Physical-ammunition fixtures never became entity-ticking: shooter="
                                    + shooterIndexed + ", target=" + targetIndexed
                                    + ", entitiesTicked=" + entitiesTicked
                                    + ", areaTicking=" + areaTicking);
                        }
                        return;
                    }
                    ItemStack marker = new ItemStack(ModItems.COMMAND_MARKER.get());
                    owner.setItemInHand(InteractionHand.MAIN_HAND, marker);
                    owner.setShiftKeyDown(true);
                    marker.interactLivingEntity(owner, target, InteractionHand.MAIN_HAND);
                    owner.setShiftKeyDown(false);
                    if (!shooter.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ATTACK)
                            || shooter.getRecruitCommand() != RecruitmentAction.ATTACK_TARGET
                            || shooter.getTarget() != target) {
                        complete[0] = true;
                        helper.fail("Physical-ammunition recruit could not receive its embodied attack order");
                        return;
                    }
                    target.setPos(
                            targetFarPosition.getX() + 0.5D,
                            targetFarPosition.getY(),
                            targetFarPosition.getZ() + 0.5D);
                    target.setDeltaMovement(Vec3.ZERO);
                    phase[0] = 0;
                    phaseStartedRecruitTick[0] = shooter.tickCount;
                }
                case 0 -> {
                    if (!observedBolts.isEmpty() || cells != 1) {
                        complete[0] = true;
                        helper.fail("Out-of-range blaster attempt consumed or fired ammunition: bolts="
                                + observedBolts.size() + ", cells=" + cells);
                        return;
                    }
                    if (elapsed >= 30) {
                        target.setPos(
                                targetNearPosition.getX() + 0.5D,
                                targetNearPosition.getY(),
                                targetNearPosition.getZ() + 0.5D);
                        target.setDeltaMovement(Vec3.ZERO);
                        for (int x = -1; x <= 1; x++) {
                            for (int y = 0; y <= 3; y++) {
                                for (int z = -4; z <= 4; z++) {
                                    helper.getLevel().setBlockAndUpdate(
                                            wallBase.offset(x, y, z), Blocks.STONE.defaultBlockState());
                                }
                            }
                        }
                        shooter.getSensing().tick();
                        if (shooter.getSensing().hasLineOfSight(target)) {
                            complete[0] = true;
                            helper.fail("Physical-ammunition sight barrier did not block the target");
                            return;
                        }
                        phase[0] = 1;
                        phaseStartedRecruitTick[0] = shooter.tickCount;
                    }
                }
                case 1 -> {
                    if (!observedBolts.isEmpty() || cells != 1) {
                        complete[0] = true;
                        helper.fail("Line-of-sight-blocked blaster attempt consumed or fired ammunition: bolts="
                                + observedBolts.size() + ", cells=" + cells);
                        return;
                    }
                    if (elapsed >= 24) {
                        for (int x = -1; x <= 1; x++) {
                            for (int y = 0; y <= 3; y++) {
                                for (int z = -4; z <= 4; z++) {
                                    helper.getLevel().setBlockAndUpdate(
                                            wallBase.offset(x, y, z), Blocks.AIR.defaultBlockState());
                                }
                            }
                        }
                        shooter.getSensing().tick();
                        phase[0] = 2;
                        phaseStartedRecruitTick[0] = shooter.tickCount;
                    }
                }
                case 2 -> {
                    if (observedBolts.size() > 1) {
                        complete[0] = true;
                        helper.fail("One physical Energy Cell produced multiple blaster bolts");
                        return;
                    }
                    if (observedBolts.size() == 1) {
                        if (cells != 0) {
                            complete[0] = true;
                            helper.fail("Recruit blaster created a bolt without consuming its Energy Cell");
                            return;
                        }
                        phase[0] = 3;
                        phaseStartedRecruitTick[0] = shooter.tickCount;
                        return;
                    }
                    if (elapsed >= 100) {
                        complete[0] = true;
                        helper.fail("Supplied recruit did not resume ranged fire after line of sight cleared");
                    }
                }
                case 3 -> {
                    if (observedBolts.size() != 1 || cells != 0) {
                        complete[0] = true;
                        helper.fail("Empty recruit blaster fired again or recreated ammunition: bolts="
                                + observedBolts.size() + ", cells=" + cells);
                        return;
                    }
                    if (elapsed >= BlasterHeatPolicy.SHOT_COOLDOWN_TICKS + 24) {
                        cargo.setItem(0, new ItemStack(ModItems.ENERGY_CELL.get()));
                        phase[0] = 4;
                        phaseStartedRecruitTick[0] = shooter.tickCount;
                    }
                }
                case 4 -> {
                    if (observedBolts.size() > 2) {
                        complete[0] = true;
                        helper.fail("One-cell resupply produced multiple blaster bolts");
                        return;
                    }
                    if (observedBolts.size() == 2) {
                        if (cells != 0) {
                            complete[0] = true;
                            helper.fail("Resupplied recruit fired without consuming the replacement Energy Cell");
                            return;
                        }
                        complete[0] = true;
                        shooter.discard();
                        target.discard();
                        helper.getLevel().getEntitiesOfClass(
                                        BlasterBoltEntity.class,
                                        shotArea,
                                        bolt -> bolt.getOwner() == shooter)
                                .forEach(BlasterBoltEntity::discard);
                        helper.succeed();
                        return;
                    }
                    if (elapsed >= 100) {
                        complete[0] = true;
                        helper.fail("Physical Energy Cell resupply did not restore ranged fire");
                    }
                }
                default -> throw new IllegalStateException("Unknown ammunition phase " + phase[0]);
            }
        });
    }

    private static void ungroupedRecruitMeleeBrain(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, 0, 6, 0, 4);
        GalacticRecruitEntity attacker = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity target = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(3, 1, 1));
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        target.setHealth(target.getMaxHealth());
        attacker.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        helper.succeedWhen(() -> {
            if (attacker.getLastHurtMob() == null) {
                helper.fail("Ungrouped melee recruit brain did not acquire and strike its enemy");
                return;
            }
            attacker.discard();
            target.discard();
        });
    }

    private static void smartBrainFollowAndSit(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -4, 12, -6, 10);
        ServerPlayer owner = area.player();
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 2));
        recruit.setInvulnerable(true);
        recruit.tame(owner);
        BlockPos ownerPos = area.at(8, 1, 2);
        owner.setNoGravity(true);
        owner.setYRot(-90.0F);
        long indexWaitStartedTick = helper.getTick();
        int[] phase = {0};
        long[] phaseStartedGameTick = {0L};
        Vec3[] startingPosition = {Vec3.ZERO};
        Vec3[] heldPosition = {Vec3.ZERO};
        double[] startingDistance = {0.0D};
        helper.onEachTick(() -> {
            if (phase[0] == 0) {
                owner.setPos(ownerPos.getX() + 0.5D, ownerPos.getY(), ownerPos.getZ() + 0.5D);
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -4, 12, -6, 10);
                boolean recruitIndexed = helper.getLevel().getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - indexWaitStartedTick >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        phase[0] = 4;
                        helper.fail("SmartBrain companion area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                // The shared readiness poll refreshes the embedded player's post-move chunk
                // ticket; retain the local checks before starting the movement window.
                boolean ownerChunkTicking = helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                        ChunkPos.containing(owner.blockPosition()));
                boolean recruitChunkTicking = helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                        ChunkPos.containing(recruit.blockPosition()));
                if (!ownerChunkTicking || !recruitChunkTicking) {
                    if (helper.getTick() - indexWaitStartedTick >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        phase[0] = 4;
                        helper.fail("SmartBrain companion chunks never became entity-ticking: owner="
                                + ownerChunkTicking + ", recruit=" + recruitChunkTicking);
                    }
                    return;
                }
                invokeRecruitCommand(recruit, RecruitmentAction.FOLLOW_OWNER);
                startingPosition[0] = recruit.position();
                startingDistance[0] = recruit.distanceToSqr(owner);
                phaseStartedGameTick[0] = helper.getTick();
                phase[0] = 1;
                return;
            }
            if (phase[0] == 1) {
                long elapsed = helper.getTick() - phaseStartedGameTick[0];
                boolean approachedOwner = recruit.distanceToSqr(owner) < startingDistance[0] - 4.0D;
                boolean followedAnchor = recruit.position().distanceToSqr(startingPosition[0]) > 4.0D
                        && BrainUtil.hasMemory(recruit,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
                if (!approachedOwner && !followedAnchor && elapsed < 520L) {
                    return;
                }
                if (!approachedOwner && !followedAnchor) {
                    phase[0] = 4;
                    net.minecraft.world.level.pathfinder.Path activePath = recruit.getNavigation().getPath();
                    helper.fail("SmartBrain companion behaviour did not follow its owner: position="
                            + recruit.position() + ", owner=" + owner.position()
                            + ", start=" + startingPosition[0]
                            + ", command=" + recruit.getRecruitCommand()
                            + ", shouldFollow=" + recruit.shouldUseCompanionAi()
                            + ", walkMemory=" + BrainUtil.hasMemory(recruit,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                            + ", pathMemory=" + BrainUtil.hasMemory(recruit,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH)
                            + ", noAi=" + recruit.isNoAi()
                            + ", effectiveAi=" + recruit.isEffectiveAi()
                            + ", ownerChunkTicking=" + helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                            ChunkPos.containing(owner.blockPosition()))
                            + ", recruitChunkTicking=" + helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                            ChunkPos.containing(recruit.blockPosition()))
                            + ", navigationDone=" + recruit.getNavigation().isDone()
                            + ", gameTicks=" + elapsed
                            + ", recruitTicks=" + recruit.tickCount
                            + ", path=" + pathState(recruit, owner.blockPosition())
                            + ", activePath=" + (activePath == null ? "none" : "nodes="
                            + activePath.getNodeCount() + ", index=" + activePath.getNextNodeIndex()
                            + ", next=" + activePath.getNextNodePos() + ", target=" + activePath.getTarget())
                            + ", navigationTarget=" + recruit.getNavigation().getTargetPos()
                            + ", moveWanted=" + recruit.getMoveControl().hasWanted()
                            + ", wanted=(" + recruit.getMoveControl().getWantedX()
                            + "," + recruit.getMoveControl().getWantedY()
                            + "," + recruit.getMoveControl().getWantedZ() + ")"
                            + ", moveSpeed=" + recruit.getMoveControl().getSpeedModifier()
                            + ", velocity=" + recruit.getDeltaMovement()
                            + ", running=" + recruit.getBrain().getRunningBehaviors());
                    return;
                }
                recruit.setWorkerProfession(WorkerProfession.FARMER);
                invokeRecruitCommand(recruit, RecruitmentAction.HOLD_POSITION);
                heldPosition[0] = recruit.position();
                phaseStartedGameTick[0] = helper.getTick();
                phase[0] = 2;
                return;
            }
            if (phase[0] == 2 && helper.getTick() - phaseStartedGameTick[0] >= 20L) {
                double heldDeltaX = recruit.getX() - heldPosition[0].x();
                double heldDeltaZ = recruit.getZ() - heldPosition[0].z();
                if (!recruit.isOrderedToSit()
                        || heldDeltaX * heldDeltaX + heldDeltaZ * heldDeltaZ > 0.25D) {
                    phase[0] = 4;
                    helper.fail("SmartBrain sit behaviour did not hold the recruit in place: position="
                            + recruit.position() + ", held=" + heldPosition[0]
                            + ", command=" + recruit.getRecruitCommand()
                            + ", orderedSit=" + recruit.isOrderedToSit()
                            + ", group=" + recruit.getArmyGroupId()
                            + ", velocity=" + recruit.getDeltaMovement()
                            + ", running=" + recruit.getBrain().getRunningBehaviors());
                    return;
                }
                invokeRecruitCommand(recruit, RecruitmentAction.CLEAR_TARGET);
                phaseStartedGameTick[0] = helper.getTick();
                phase[0] = 3;
                return;
            }
            if (phase[0] == 3 && helper.getTick() - phaseStartedGameTick[0] >= 5L) {
                phase[0] = 4;
                if (recruit.isOrderedToSit()) {
                    helper.fail("Clear-target command incorrectly kept the recruit sitting");
                    return;
                }
                recruit.discard();
                helper.succeed();
            }
        });
    }

    private static void smartBrainMoveCommand(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -2, 6, -2, 6);
        ServerPlayer owner = area.player();
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 2));
        recruit.setInvulnerable(true);
        recruit.tame(owner);
        BlockPos target = area.at(3, 1, 4);
        setRecruitField(recruit, "moveTarget", target);
        invokeRecruitCommand(recruit, RecruitmentAction.MOVE_TO_POSITION);
        helper.succeedWhen(() -> {
            if (recruit.distanceToMoveTargetSqr() > 4.0D) {
                helper.fail("SmartBrain move command did not reach its explicit target: position="
                        + recruit.position() + ", target=" + target
                        + ", shouldMove=" + recruit.shouldMoveToCommandTarget()
                        + ", walkMemory=" + BrainUtil.hasMemory(recruit,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                        + ", navigationDone=" + recruit.getNavigation().isDone()
                        + ", pathMemory=" + BrainUtil.hasMemory(recruit,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH)
                        + ", pathRegistered=" + recruit.getBrain().checkMemory(
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH,
                        net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)
                        + ", running=" + recruit.getBrain().getRunningBehaviors()
                        + ", path=" + pathState(recruit, target));
                return;
            }
            recruit.discard();
        });
    }

    private static void smartBrainMoveStallRecovery(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -2, 4, -2, 4);
        ServerPlayer owner = area.player();
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        recruit.setInvulnerable(true);
        recruit.tame(owner);
        BlockPos unreachable = area.at(1, 20, 1);
        setRecruitField(recruit, "moveTarget", unreachable);
        invokeRecruitCommand(recruit, RecruitmentAction.MOVE_TO_POSITION);
        boolean[] finished = {false};
        helper.onEachTick(() -> {
            if (finished[0]) {
                return;
            }
            if (recruit.tickCount >= 200
                    && recruit.isAlive()
                    && recruit.distanceToMoveTargetSqr() > 4.0D
                    && recruit.getNavigation().isDone()
                    && !BrainUtil.hasMemory(recruit,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)) {
                finished[0] = true;
                recruit.discard();
                helper.succeed();
            } else if (recruit.tickCount >= 240) {
                finished[0] = true;
                helper.fail("Unreachable move command did not enter bounded retry backoff: tickCount="
                        + recruit.tickCount + ", walkMemory="
                        + BrainUtil.hasMemory(recruit,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                        + ", navigationDone=" + recruit.getNavigation().isDone()
                        + ", running=" + recruit.getBrain().getRunningBehaviors());
            }
        });
    }

    private static void naturalCivilianBrainRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -4, 8, -4, 8);
        NaturalCivilianTestScenario scenario = new NaturalCivilianTestScenario(helper, area);
        helper.onEachTick(scenario::tick);
    }

    private static void factionAiReactionRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.SURVIVAL, -2, 50, -2, 12);
        FactionAiReactionTestScenario scenario =
                new FactionAiReactionTestScenario(helper, area);
        helper.onEachTick(scenario::tick);
    }

    private static void verifyNaturalCivilianWork(
            GameTestHelper helper,
            GalacticRecruitEntity sheltering,
            GalacticRecruitEntity workingCivilian,
            BlockPos storage
    ) {
        if (!(helper.getLevel().getBlockEntity(storage) instanceof Container container)
                || countContainerItems(container) == 0) {
            helper.fail("Natural civilian work behaviour did not deliver settlement supplies: position="
                    + workingCivilian.position() + ", workstation="
                    + workingCivilian.naturalWorkstationPosition() + ", profession="
                    + workingCivilian.getWorkerProfession() + ", natural="
                    + workingCivilian.isNaturalFactionCivilian() + ", home="
                    + workingCivilian.hasHome() + ", orderedSit="
                    + workingCivilian.isOrderedToSit() + ", bright="
                    + workingCivilian.level().isBrightOutside() + ", tickCount="
                    + workingCivilian.tickCount + ", walkMemory="
                    + BrainUtil.hasMemory(workingCivilian,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                    + ", navigationDone=" + workingCivilian.getNavigation().isDone()
                    + ", productionAt="
                    + getRecruitField(workingCivilian, "nextNaturalProductionGameTime")
                    + ", gameTime=" + workingCivilian.level().getGameTime()
                    + ", running=" + workingCivilian.getBrain().getRunningBehaviors());
            return;
        }
        sheltering.discard();
        workingCivilian.discard();
        helper.succeed();
    }

    /**
     * Exercises the field-command service as the C2S authority boundary. The
     * intruder can construct a request containing the owner's squad UUID, but
     * only the server player passed to the service is considered for kingdom
     * selection. The same scenario also proves that a marker whose selected
     * entity has despawned fails closed and that a replay cannot mutate twice.
     */
    private static void fieldCommandAuthority(GameTestHelper helper) {
        BlockPos commanderPosition = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos staleTargetPosition = helper.absolutePos(new BlockPos(4, 1, 2));
        helper.getLevel().setBlockAndUpdate(commanderPosition.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(staleTargetPosition.below(), Blocks.STONE.defaultBlockState());

        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer intruder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity commander = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), commanderPosition);
        GalacticRecruitEntity staleTarget = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), staleTargetPosition);
        commander.setInvulnerable(true);
        staleTarget.setInvulnerable(true);
        staleTarget.setNoAi(true);
        commander.tame(owner);
        owner.setPos(commander.getX(), commander.getY(), commander.getZ());
        intruder.setPos(commander.getX(), commander.getY(), commander.getZ());
        helper.getLevel().getChunkSource().move(owner);
        helper.getLevel().getChunkSource().move(intruder);

        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos ownerCapital = isolatedCapital(helper, 142);
        BlockPos intruderCapital = isolatedCapital(helper, 143);
        data.foundKingdom(owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), ownerCapital);
        data.foundKingdom(intruder.getUUID(), "galacticwars:separatist",
                helper.getLevel().dimension().identifier().toString(), intruderCapital);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), ownerCapital.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)
                || !data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Field command authority setup could not create an owner commander");
            return;
        }

        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, helper.getLevel().getGameTime()).orElse(null);
        if (squad == null) {
            helper.fail("Field command authority setup could not create an owner squad");
            return;
        }

        long indexWaitStartedTick = helper.getTick();
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            boolean commanderIndexed = helper.getLevel().getEntity(commander.getUUID()) == commander;
            boolean targetIndexed = helper.getLevel().getEntity(staleTarget.getUUID()) == staleTarget;
            if (!commanderIndexed || !targetIndexed) {
                if (helper.getTick() - indexWaitStartedTick >= 120L) {
                    complete[0] = true;
                    helper.fail("Field command authority entities never entered the server index: commander="
                            + commanderIndexed + ", target=" + targetIndexed);
                }
                return;
            }

            complete[0] = true;
            ArmyFieldCommandService.clearReplayHistory(owner.getUUID());
            ArmyFieldCommandService.clearReplayHistory(intruder.getUUID());
            var before = data.armyGroup(squad.id()).orElseThrow();
            UUID replayId = UUID.randomUUID();
            FieldCommandRequestPayload ownerRequest = new FieldCommandRequestPayload(
                    replayId, FieldCommandAction.FOLLOW, List.of(squad.id()));
            var accepted = ArmyFieldCommandService.execute(owner, ownerRequest);
            var afterFirst = data.armyGroup(squad.id()).orElseThrow();
            var replayed = ArmyFieldCommandService.execute(owner, ownerRequest);
            var afterReplay = data.armyGroup(squad.id()).orElseThrow();

            // The only spoofable selector in the payload is the UUID list. Passing the
            // owner's live squad ID as another server player must not select it.
            var spoofed = ArmyFieldCommandService.execute(intruder, new FieldCommandRequestPayload(
                    UUID.randomUUID(), FieldCommandAction.HOLD, List.of(squad.id())));
            var afterSpoofed = data.armyGroup(squad.id()).orElseThrow();

            ItemStack marker = new ItemStack(ModItems.COMMAND_MARKER.get());
            marker.set(ModDataComponents.COMMAND_TARGET,
                    CommandTargetSelection.entity(helper.getLevel(), staleTarget));
            owner.setItemInHand(InteractionHand.MAIN_HAND, marker);
            staleTarget.discard();
            var staleTargetResult = ArmyFieldCommandService.execute(owner, new FieldCommandRequestPayload(
                    UUID.randomUUID(), FieldCommandAction.ATTACK_MARKED_TARGET, List.of(squad.id())));
            var afterStaleTarget = data.armyGroup(squad.id()).orElseThrow();

            boolean ownerAppliedOnce = accepted.result() == FieldCommandResult.ACCEPTED
                    && afterFirst.simulation().revision() == before.simulation().revision() + 1L;
            boolean replayRejectedWithoutMutation = replayed.result() == FieldCommandResult.REPLAY_REJECTED
                    && afterReplay.equals(afterFirst);
            boolean spoofRejectedWithoutMutation = spoofed.result() == FieldCommandResult.SQUAD_UNAVAILABLE
                    && afterSpoofed.equals(afterFirst);
            boolean staleMarkerRejectedWithoutMutation = staleTargetResult.result() == FieldCommandResult.TARGET_REQUIRED
                    && afterStaleTarget.equals(afterFirst);
            if (!ownerAppliedOnce || !replayRejectedWithoutMutation
                    || !spoofRejectedWithoutMutation || !staleMarkerRejectedWithoutMutation) {
                helper.fail("Field command service authority failure: accepted=" + accepted.result()
                        + ", revisions=" + before.simulation().revision() + "/"
                        + afterFirst.simulation().revision()
                        + ", replay=" + replayed.result() + "/" + afterReplay.simulation().revision()
                        + ", spoofed=" + spoofed.result() + "/" + afterSpoofed.simulation().revision()
                        + ", staleMarker=" + staleTargetResult.result() + "/"
                        + afterStaleTarget.simulation().revision());
                return;
            }

            ArmyFieldCommandService.clearReplayHistory(owner.getUUID());
            ArmyFieldCommandService.clearReplayHistory(intruder.getUUID());
            commander.discard();
            helper.succeed();
        });
    }

    private static void groupedBrainAuthority(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -4, 18, -4, 4);
        ServerPlayer owner = area.player();
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos capital = isolatedCapital(helper, 141);
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), capital);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capital.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)) {
            helper.fail("Grouped SmartBrain setup could not unlock a commander slot");
            return;
        }

        GalacticRecruitEntity commander = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity soldier = spawnRecruitAt(
                helper, ModEntityTypes.ARC_TROOPER.get(), area.at(2, 1, 1));
        commander.tame(owner);
        soldier.tame(owner);
        if (!data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.registerRecruit(owner.getUUID(), soldier.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Grouped SmartBrain setup could not register and promote the live squad");
            return;
        }

        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, helper.getLevel().getGameTime()).orElse(null);
        if (squad == null || !squad.kingdomId().equals(kingdom.id())
                || !squad.contains(commander.getUUID()) || !squad.contains(soldier.getUUID())) {
            helper.fail("Grouped SmartBrain setup did not create one authoritative live squad");
            return;
        }

        // The persisted simulation anchor intentionally trails a live group until its next
        // virtual/hibernate transition. A field Hold must therefore resolve the commander's
        // current server position, rather than pulling a live squad back to that stale anchor.
        BlockPos liveHoldPosition = area.at(5, 1, 1);
        BlockPos moveTarget = area.at(14, 1, 1);
        ArmyLocation moveLocation = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                moveTarget.getX(), moveTarget.getY(), moveTarget.getZ());
        ArmyGroupOrder moveOrder = new ArmyGroupOrder(
                ArmyCommandType.MOVE_TO_POSITION,
                Optional.of(moveLocation), Optional.empty(), squad.order().formation(), squad.order().spacing());
        long[] startedAt = {helper.getTick()};
        boolean[] ordersIssued = {false};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            boolean commanderIndexed = helper.getLevel().getEntity(commander.getUUID()) == commander;
            boolean soldierIndexed = helper.getLevel().getEntity(soldier.getUUID()) == soldier;
            if (!ordersIssued[0]) {
                // Under aggregate load, entity indexing can precede the forced area's
                // entity-ticking state; the live command and SmartBrain path require both.
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -4, 18, -4, 4);
                boolean recruitsTicked = commander.tickCount > 0 && soldier.tickCount > 0;
                if (!commanderIndexed || !soldierIndexed || !areaTicking || !recruitsTicked) {
                    if (helper.getTick() - startedAt[0] >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Grouped SmartBrain fixtures never became entity-ticking: commander="
                                + commanderIndexed + ", soldier=" + soldierIndexed
                                + ", recruitsTicked=" + recruitsTicked
                                + ", areaTicking=" + areaTicking);
                    }
                    return;
                }
                commander.setPos(
                        liveHoldPosition.getX() + 0.5D,
                        liveHoldPosition.getY(),
                        liveHoldPosition.getZ() + 0.5D);
                var holdState = ArmyFieldCommandService.execute(
                        owner,
                        new FieldCommandRequestPayload(
                                UUID.randomUUID(), FieldCommandAction.HOLD, List.of(squad.id())));
                ArmyGroupOrder heldOrder = data.armyGroup(squad.id()).orElseThrow().order();
                if (holdState.result() != FieldCommandResult.ACCEPTED
                        || heldOrder.type() != ArmyCommandType.HOLD_POSITION
                        || heldOrder.targetPosition().isEmpty()
                        || heldOrder.targetPosition().orElseThrow().blockPosition().x()
                        != commander.blockPosition().getX()
                        || heldOrder.targetPosition().orElseThrow().blockPosition().z()
                        != commander.blockPosition().getZ()) {
                    complete[0] = true;
                    var currentGroup = data.armyGroup(squad.id()).orElseThrow();
                    helper.fail("Field Hold did not use the live commander's server position: result="
                            + holdState.result() + ", order=" + heldOrder
                            + ", commander=" + commander.blockPosition()
                            + ", owner=" + owner.blockPosition()
                            + ", distanceSqr=" + owner.distanceToSqr(commander)
                            + ", lifecycle=" + currentGroup.simulation().lifecycleState()
                            + ", serviceBranch=" + commander.getServiceBranch()
                            + ", contains=" + currentGroup.contains(commander.getUUID())
                            + ", visibleSquads=" + holdState.squads().size()
                            + ", visibleTarget=" + holdState.squads().stream()
                                    .anyMatch(candidate -> candidate.id().equals(squad.id()))
                            + ", visibleNames=" + holdState.squads().stream()
                                    .map(candidate -> candidate.name()).limit(4).toList()
                            + ", kingdomGroups=" + data.armyGroupsForKingdom(kingdom.id()).size()
                            + ", sameLevel=" + (owner.level() == helper.getLevel())
                            + ", ticks=" + owner.tickCount + "/" + commander.tickCount
                            + ", areaTicking=" + areSmartBrainAreaChunksTicking(
                                    helper, area, -4, 18, -4, 4));
                    return;
                }
                if (!data.issueArmyOrder(owner.getUUID(), squad.id(), moveOrder)) {
                    complete[0] = true;
                    helper.fail("Grouped SmartBrain setup could not persist its move order");
                    return;
                }
                ordersIssued[0] = true;
                startedAt[0] = helper.getTick();
                return;
            }
            ArmyBrainState commanderState = BrainUtil.getMemory(commander, ArmyBrainMemoryTypes.ARMY_STATE);
            ArmyBrainState soldierState = BrainUtil.getMemory(soldier, ArmyBrainMemoryTypes.ARMY_STATE);
            ArmyMarchMemory commanderMarch = BrainUtil.getMemory(commander, ArmyBrainMemoryTypes.MARCH_STATE);
            ArmyMarchMemory soldierMarch = BrainUtil.getMemory(soldier, ArmyBrainMemoryTypes.MARCH_STATE);
            net.minecraft.world.entity.ai.memory.WalkTarget commanderWalkTarget = BrainUtil.getMemory(
                    commander, net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
            net.minecraft.world.entity.ai.memory.WalkTarget soldierWalkTarget = BrainUtil.getMemory(
                    soldier, net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
            if (!commanderIndexed || !soldierIndexed || commanderState == null || soldierState == null
                    || commanderMarch == null || soldierMarch == null
                    || commanderWalkTarget == null || soldierWalkTarget == null) {
                if (helper.getTick() - startedAt[0] >= 180L) {
                    complete[0] = true;
                    helper.fail("Live grouped SmartBrain did not publish state and formation walk targets: indexed="
                            + commanderIndexed + "/" + soldierIndexed
                            + ", groupIds=" + commander.getArmyGroupId() + "/" + soldier.getArmyGroupId()
                            + ", states=" + (commanderState != null) + "/" + (soldierState != null)
                            + ", marches=" + (commanderMarch != null) + "/" + (soldierMarch != null)
                            + ", walkTargets=" + (commanderWalkTarget != null) + "/"
                            + (soldierWalkTarget != null)
                            + ", commanderRunning=" + commander.getBrain().getRunningBehaviors());
                }
                return;
            }

            boolean legacyRuntimeAttached = java.util.Arrays.stream(
                    GalacticRecruitEntity.class.getDeclaredFields())
                    .anyMatch(field -> field.getType().getName().endsWith("ArmyRecruitRuntimeController"));
            BlockPos publishedCommanderTarget = commanderWalkTarget.getTarget().currentBlockPosition();
            var persistedMarch = data.armyGroup(squad.id()).orElseThrow();
            var persistedAnchor = persistedMarch.simulation().anchor().blockPosition();
            if (!squad.id().equals(commander.getArmyGroupId())
                    || !squad.id().equals(soldier.getArmyGroupId())
                    || !squad.id().equals(commanderState.group().id())
                    || !squad.id().equals(soldierState.group().id())
                    || !squad.id().equals(commanderMarch.groupId())
                    || !squad.id().equals(soldierMarch.groupId())
                    || commanderState.memberCommand().type() != ArmyCommandType.MOVE_TO_POSITION
                    || soldierState.memberCommand().type() != ArmyCommandType.MOVE_TO_POSITION
                    || commanderMarch.orderType() != ArmyCommandType.MOVE_TO_POSITION
                    || soldierMarch.orderType() != ArmyCommandType.MOVE_TO_POSITION
                    || commanderMarch.memberSlot() != -1
                    || soldierMarch.memberSlot() < 0
                    || commanderMarch.targetPosition() == null
                    || !commanderMarch.targetPosition().equals(moveLocation.blockPosition())
                    || publishedCommanderTarget.getX() != persistedAnchor.x()
                    || publishedCommanderTarget.getZ() != persistedAnchor.z()
                    || !commanderMarch.movingAnchor().equals(persistedAnchor)
                    || persistedMarch.simulation().marchState().phase()
                            == galacticwars.clonewars.army.ArmyMarchPhase.HALTED
                    || commander.shouldMoveToCommandTarget()
                    || soldier.shouldMoveToCommandTarget()
                    || legacyRuntimeAttached) {
                complete[0] = true;
                helper.fail("Grouped SmartBrain execution bypassed its persisted order path: groupIds="
                        + commander.getArmyGroupId() + "/" + soldier.getArmyGroupId()
                        + ", commands=" + commanderState.memberCommand().type() + "/"
                        + soldierState.memberCommand().type()
                        + ", commanderTarget=" + publishedCommanderTarget
                        + ", movingAnchor=" + persistedAnchor
                        + ", march=" + persistedMarch.simulation().marchState()
                        + ", localMove=" + commander.shouldMoveToCommandTarget() + "/"
                        + soldier.shouldMoveToCommandTarget()
                        + ", legacyRuntimeAttached=" + legacyRuntimeAttached);
                return;
            }

            complete[0] = true;
            commander.discard();
            soldier.discard();
            helper.succeed();
        });
    }

    private static void groupedZeroSupplyBlaster(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -4, 12, -4, 4);
        ServerPlayer owner = area.player();
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos capital = isolatedCapital(helper, 149);
        ChunkPos commandCenterChunk = ChunkPos.containing(capital);
        helper.getLevel().getChunkSource().updateChunkForced(commandCenterChunk, true);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, capital);
        hall.claim(owner);
        hall.setItem(0, new ItemStack(ModItems.CREDIT_CHIP.get(), 8));
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), capital).orElseThrow();
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capital.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)) {
            helper.fail("Zero-supply blaster setup could not unlock a commander slot");
            return;
        }

        GalacticRecruitEntity commander = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity soldier = spawnRecruitAt(
                helper, ModEntityTypes.ARC_TROOPER.get(), area.at(2, 1, 1));
        GalacticRecruitEntity target = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(10, 1, 1));
        commander.tame(owner);
        soldier.tame(owner);
        commander.setMilitaryMainHandItem(new ItemStack(Items.IRON_SWORD));
        soldier.setMilitaryMainHandItem(new ItemStack(ModItems.DC15_BLASTER.get()));
        Container soldierCargo = soldier.createCargoContainer();
        soldierCargo.setItem(0, new ItemStack(
                ModItems.ENERGY_CELL.get(), BlasterHeatPolicy.SHOTS_BEFORE_OVERHEAT + 1));
        target.setNoAi(true);
        target.setInvulnerable(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4096.0D);
        target.setHealth(target.getMaxHealth());
        target.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        if (!soldier.isHostileFactionRecruit(target)) {
            helper.fail("Zero-supply blaster target is not hostile: "
                    + soldier.getRecruitFactionId() + " -> " + target.getRecruitFactionId());
            return;
        }
        if (!data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.registerRecruit(owner.getUUID(), soldier.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Zero-supply blaster setup could not register its squad");
            return;
        }
        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, helper.getLevel().getGameTime()).orElse(null);
        if (squad == null || squad.supplyUnits() != 0) {
            helper.fail("Zero-supply blaster setup did not begin with zero provisioning");
            return;
        }
        ArmyGroupOrder attackOrder = new ArmyGroupOrder(
                ArmyCommandType.ATTACK_TARGET,
                Optional.empty(),
                Optional.of(target.getUUID()),
                squad.order().formation(),
                squad.order().spacing());
        if (!data.issueArmyOrder(owner.getUUID(), squad.id(), attackOrder)) {
            helper.fail("Zero-supply blaster setup could not authorize its attack target");
            return;
        }

        Set<UUID> observedBolts = new java.util.HashSet<>();
        int[] overheatStartedRecruitTick = {-1};
        int testStartedRecruitTick = soldier.tickCount;
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            helper.getLevel().getEntitiesOfClass(
                            BlasterBoltEntity.class,
                            soldier.getBoundingBox().inflate(24.0D),
                            bolt -> bolt.getOwner() == soldier)
                    .forEach(bolt -> observedBolts.add(bolt.getUUID()));
            int supply = data.armyGroup(squad.id()).orElseThrow().supplyUnits();
            if (supply != 0 || soldier.isWithinMeleeAttackRange(target)) {
                complete[0] = true;
                helper.fail("Grouped blaster consumed provisioning or closed to melee range: supply="
                        + supply + ", distance=" + soldier.distanceTo(target));
                return;
            }
            if (observedBolts.size() >= BlasterHeatPolicy.SHOTS_BEFORE_OVERHEAT
                    && overheatStartedRecruitTick[0] < 0) {
                int expectedRemainingCells = 1;
                int remainingCells = countContainerItem(
                        soldierCargo, ModItems.ENERGY_CELL.get());
                if (remainingCells != expectedRemainingCells) {
                    complete[0] = true;
                    helper.fail("Grouped blaster did not consume exactly one physical cell per shot: bolts="
                            + observedBolts.size() + ", cells=" + remainingCells);
                    return;
                }
                overheatStartedRecruitTick[0] = soldier.tickCount;
            }
            if (overheatStartedRecruitTick[0] >= 0) {
                if (observedBolts.size() > BlasterHeatPolicy.SHOTS_BEFORE_OVERHEAT) {
                    complete[0] = true;
                    helper.fail("Grouped blaster fired before its overheat window elapsed");
                    return;
                }
                int remainingCells = countContainerItem(
                        soldierCargo, ModItems.ENERGY_CELL.get());
                if (remainingCells != 1) {
                    complete[0] = true;
                    helper.fail("Overheated grouped blaster consumed its reserved Energy Cell: cells="
                            + remainingCells);
                    return;
                }
                if (soldier.tickCount - overheatStartedRecruitTick[0] >= 30) {
                    complete[0] = true;
                    commander.discard();
                    soldier.discard();
                    target.discard();
                    helper.getLevel().getChunkSource().updateChunkForced(
                            commandCenterChunk, false);
                    helper.succeed();
                }
            }
            if (!complete[0] && soldier.tickCount - testStartedRecruitTick >= 230) {
                complete[0] = true;
                helper.fail("Grouped blaster did not complete its zero-supply heat cycle: bolts="
                        + observedBolts.size()
                        + ", attack=" + BrainUtil.getMemory(
                        soldier,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        + ", state=" + BrainUtil.getMemory(
                        soldier, ArmyBrainMemoryTypes.ARMY_STATE)
                        + ", targetAlive=" + target.isAlive()
                        + ", targetRemoved=" + target.isRemoved()
                        + ", targetIndexed=" + (helper.getLevel().getEntity(target.getUUID()) == target)
                        + ", targetDuty=" + target.getRecruitDuty()
                        + ", hostile=" + soldier.isHostileFactionRecruit(target)
                        + ", target=" + soldier.getTarget()
                        + ", distance=" + soldier.distanceTo(target)
                        + ", sensors="
                        + ((net.tslat.smartbrainlib.api.internal.SmartBrain<?>)soldier.getBrain())
                        .getSensors().sensorTypes()
                        + ", running=" + soldier.getBrain().getRunningBehaviors());
            }
        });
    }

    /**
     * Runs a persisted waited patrol through the real field-command service
     * and the loaded SmartBrain path. This catches a regression where pausing
     * only changed the panel state while the commander kept consuming the
     * waypoint dwell timer or retained a navigation target.
     */
    private static void groupedPatrolPauseRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, -4, 20, -4, 4);
        ServerPlayer owner = area.player();
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos capital = isolatedCapital(helper, 147);
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), capital);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capital.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)) {
            helper.fail("Grouped patrol setup could not unlock a commander slot");
            return;
        }

        GalacticRecruitEntity commander = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity soldier = spawnRecruitAt(
                helper, ModEntityTypes.ARC_TROOPER.get(), area.at(2, 1, 1));
        commander.tame(owner);
        soldier.tame(owner);
        if (!data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.registerRecruit(owner.getUUID(), soldier.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Grouped patrol setup could not register and promote the live squad");
            return;
        }

        String dimensionId = helper.getLevel().dimension().identifier().toString();
        ArmyLocation firstWaypoint = new ArmyLocation(
                dimensionId, commander.getX(), commander.getY(), commander.getZ());
        BlockPos secondWaypointBlock = area.at(14, 1, 1);
        ArmyLocation secondWaypoint = new ArmyLocation(
                dimensionId, secondWaypointBlock.getX(), secondWaypointBlock.getY(), secondWaypointBlock.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                firstWaypoint, helper.getLevel().getGameTime()).orElse(null);
        if (squad == null || !squad.kingdomId().equals(kingdom.id())
                || !squad.contains(commander.getUUID()) || !squad.contains(soldier.getUUID())) {
            helper.fail("Grouped patrol setup did not create an authoritative live squad");
            return;
        }

        ArmyPatrolPlan patrol = new ArmyPatrolPlan(
                List.of(
                        new ArmyPatrolWaypoint(firstWaypoint, 20),
                        ArmyPatrolWaypoint.immediate(secondWaypoint)),
                ArmyPatrolMode.LOOP,
                ArmyPatrolState.start(),
                ArmyPatrolPlan.DEFAULT_ARRIVAL_DISTANCE,
                ArmyPatrolPlan.DEFAULT_MOVEMENT_SPEED,
                ArmyPatrolEnemyPolicy.ENGAGE_HOSTILES);
        ArmyGroupOrder patrolOrder = new ArmyGroupOrder(
                ArmyCommandType.PATROL_ROUTE, Optional.of(firstWaypoint), Optional.empty(),
                squad.order().formation(), squad.order().spacing());
        var configured = squad.withPatrolPlanAndOrder(patrol, patrolOrder);
        if (!data.replaceArmyGroup(configured, squad.simulation().revision())) {
            helper.fail("Grouped patrol setup could not persist its waited route");
            return;
        }

        long startedAt = helper.getTick();
        long[] pausedAtTick = {-1L};
        long[] resumedAtTick = {-1L};
        ArmyPatrolState[] pausedState = {null};
        Vec3[] pausedPosition = {null};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            var current = data.armyGroup(configured.id()).orElse(null);
            ArmyPatrolPlan currentPlan = current == null ? null : current.patrolPlan().orElse(null);
            boolean commanderIndexed = helper.getLevel().getEntity(commander.getUUID()) == commander;
            ArmyBrainState state = BrainUtil.getMemory(commander, ArmyBrainMemoryTypes.ARMY_STATE);
            if (currentPlan == null || !commanderIndexed || state == null) {
                if (helper.getTick() - startedAt >= 120L) {
                    complete[0] = true;
                    helper.fail("Grouped patrol never entered the live SmartBrain: indexed=" + commanderIndexed
                            + ", state=" + (state != null) + ", plan=" + (currentPlan != null));
                }
                return;
            }

            if (pausedAtTick[0] < 0L) {
                ArmyPatrolState patrolState = currentPlan.state();
                if (patrolState.status() != ArmyPatrolStatus.ACTIVE || patrolState.waitTicksRemaining() <= 0) {
                    if (helper.getTick() - startedAt >= 120L) {
                        complete[0] = true;
                        helper.fail("Loaded patrol never began its waited waypoint: state=" + patrolState
                                + ", order=" + current.order());
                    }
                    return;
                }
                var renamed = ArmyFieldCommandService.execute(owner, new FieldCommandRequestPayload(
                        UUID.randomUUID(), FieldCommandAction.RENAME_PATROL_ROUTE, List.of(configured.id()),
                        "Aurek Perimeter", 0, 0));
                var editedWait = ArmyFieldCommandService.execute(owner, new FieldCommandRequestPayload(
                        UUID.randomUUID(), FieldCommandAction.SET_PATROL_WAYPOINT_WAIT, List.of(configured.id()),
                        "", 0, 45));
                current = data.armyGroup(configured.id()).orElse(null);
                currentPlan = current == null ? null : current.patrolPlan().orElse(null);
                if (renamed.result() != FieldCommandResult.ACCEPTED
                        || editedWait.result() != FieldCommandResult.ACCEPTED
                        || currentPlan == null || !currentPlan.name().equals("Aurek Perimeter")
                        || currentPlan.waypoints().getFirst().waitTicks() != 45) {
                    complete[0] = true;
                    helper.fail("Field patrol metadata did not persist atomically: rename=" + renamed.result()
                            + ", wait=" + editedWait.result() + ", plan=" + currentPlan);
                    return;
                }
                var paused = ArmyFieldCommandService.execute(owner, new FieldCommandRequestPayload(
                        UUID.randomUUID(), FieldCommandAction.PAUSE_PATROL, List.of(configured.id())));
                current = data.armyGroup(configured.id()).orElse(null);
                currentPlan = current == null ? null : current.patrolPlan().orElse(null);
                if (paused.result() != FieldCommandResult.ACCEPTED || currentPlan == null
                        || currentPlan.state().status() != ArmyPatrolStatus.PAUSED) {
                    complete[0] = true;
                    helper.fail("Field pause did not persist the live patrol state: result=" + paused.result()
                            + ", plan=" + currentPlan);
                    return;
                }
                pausedState[0] = currentPlan.state();
                pausedPosition[0] = commander.position();
                pausedAtTick[0] = helper.getTick();
                return;
            }

            if (resumedAtTick[0] < 0L) {
                if (helper.getTick() - pausedAtTick[0] < 24L) {
                    return;
                }
                net.minecraft.world.entity.ai.memory.WalkTarget walkTarget = BrainUtil.getMemory(
                        commander, net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
                boolean remainedStill = commander.position().distanceToSqr(pausedPosition[0]) <= 0.25D;
                if (!pausedState[0].equals(currentPlan.state()) || walkTarget != null || !remainedStill) {
                    complete[0] = true;
                    helper.fail("Paused patrol leaked movement or consumed its dwell: expected=" + pausedState[0]
                            + ", actual=" + currentPlan.state() + ", walkTarget=" + walkTarget
                            + ", commander=" + commander.position());
                    return;
                }
                var resumed = ArmyFieldCommandService.execute(owner, new FieldCommandRequestPayload(
                        UUID.randomUUID(), FieldCommandAction.RESUME_PATROL, List.of(configured.id())));
                current = data.armyGroup(configured.id()).orElse(null);
                currentPlan = current == null ? null : current.patrolPlan().orElse(null);
                if (resumed.result() != FieldCommandResult.ACCEPTED || currentPlan == null
                        || currentPlan.state().status() != ArmyPatrolStatus.ACTIVE
                        || currentPlan.state().waitTicksRemaining() != pausedState[0].waitTicksRemaining()) {
                    complete[0] = true;
                    helper.fail("Field resume did not restore the paused dwell state: result=" + resumed.result()
                            + ", plan=" + currentPlan);
                    return;
                }
                resumedAtTick[0] = helper.getTick();
                return;
            }

            net.minecraft.world.entity.ai.memory.WalkTarget walkTarget = BrainUtil.getMemory(
                    commander, net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
            boolean releasedNextWaypoint = currentPlan.state().status() == ArmyPatrolStatus.ACTIVE
                    && currentPlan.state().waitTicksRemaining() == 0
                    && current.order().targetPosition().filter(secondWaypoint::equals).isPresent()
                    && walkTarget != null
                    && walkTarget.getTarget().currentBlockPosition().getX() == secondWaypointBlock.getX()
                    && walkTarget.getTarget().currentBlockPosition().getZ() == secondWaypointBlock.getZ();
            if (releasedNextWaypoint) {
                complete[0] = true;
                commander.discard();
                soldier.discard();
                helper.succeed();
                return;
            }
            if (helper.getTick() - resumedAtTick[0] >= 100L) {
                complete[0] = true;
                helper.fail("Resumed patrol did not release a SmartBrain target for the next waypoint: state="
                        + currentPlan.state() + ", order=" + current.order() + ", walkTarget=" + walkTarget);
            }
        });
    }

    /**
     * Verifies that a server-authenticated Protect Marked Entity command is
     * not merely persisted: when the marked allied recruit is attacked, both
     * members of the loaded squad receive that hostile through their
     * SmartBrain combat path.
     */
    private static void groupedProtectEntityRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.SURVIVAL, -4, 12, -4, 4);
        ServerPlayer owner = area.player();
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos capital = isolatedCapital(helper, 148);
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), capital);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capital.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)) {
            helper.fail("Grouped Protect Entity setup could not unlock a commander slot");
            return;
        }

        GalacticRecruitEntity commander = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        GalacticRecruitEntity soldier = spawnRecruitAt(
                helper, ModEntityTypes.ARC_TROOPER.get(), area.at(2, 1, 1));
        GalacticRecruitEntity protectedRecruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(4, 1, 1));
        GalacticRecruitEntity attacker = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(5, 1, 1));
        commander.tame(owner);
        soldier.tame(owner);
        protectedRecruit.tame(owner);
        protectedRecruit.setNoAi(true);
        attacker.setNoAi(true);
        attacker.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        attacker.setHealth(attacker.getMaxHealth());
        ServerPlayer enemyOwner = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        attacker.setOwner(enemyOwner);
        attacker.setTame(true, true);
        if (!data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.registerRecruit(owner.getUUID(), soldier.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Grouped Protect Entity setup could not register and promote the live squad");
            return;
        }

        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, helper.getLevel().getGameTime()).orElse(null);
        if (squad == null || !squad.kingdomId().equals(kingdom.id())
                || !squad.contains(commander.getUUID()) || !squad.contains(soldier.getUUID())) {
            helper.fail("Grouped Protect Entity setup did not create an authoritative live squad");
            return;
        }

        long[] startedAt = {helper.getTick()};
        int[] threatStartedAtRecruitTick = {-1};
        boolean[] commandIssued = {false};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            boolean entitiesIndexed = helper.getLevel().getEntity(commander.getUUID()) == commander
                    && helper.getLevel().getEntity(soldier.getUUID()) == soldier
                    && helper.getLevel().getEntity(protectedRecruit.getUUID()) == protectedRecruit
                    && helper.getLevel().getEntity(attacker.getUUID()) == attacker;
            if (!commandIssued[0]) {
                if (!entitiesIndexed) {
                    if (helper.getTick() - startedAt[0] >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Grouped Protect Entity fixtures never entered the server index");
                    }
                    return;
                }
                ItemStack marker = new ItemStack(ModItems.COMMAND_MARKER.get());
                marker.set(ModDataComponents.COMMAND_TARGET,
                        CommandTargetSelection.entity(helper.getLevel(), protectedRecruit));
                owner.setItemInHand(InteractionHand.MAIN_HAND, marker);
                ArmyFieldCommandService.clearReplayHistory(owner.getUUID());
                var protectedResult = ArmyFieldCommandService.execute(
                        owner,
                        new FieldCommandRequestPayload(
                                UUID.randomUUID(),
                                FieldCommandAction.PROTECT_MARKED_ENTITY,
                                List.of(squad.id())));
                ArmyGroupOrder protectedOrder = data.armyGroup(squad.id()).orElseThrow().order();
                if (protectedResult.result() != FieldCommandResult.ACCEPTED
                        || protectedOrder.type() != ArmyCommandType.PROTECT_ENTITY
                        || protectedOrder.targetEntityId()
                                .filter(protectedRecruit.getUUID()::equals).isEmpty()) {
                    complete[0] = true;
                    helper.fail("Protect Marked Entity did not persist an authoritative group order: result="
                            + protectedResult.result() + ", order=" + protectedOrder);
                    return;
                }
                commandIssued[0] = true;
                startedAt[0] = helper.getTick();
                return;
            }
            ArmyBrainState commanderState = BrainUtil.getMemory(commander, ArmyBrainMemoryTypes.ARMY_STATE);
            ArmyBrainState soldierState = BrainUtil.getMemory(soldier, ArmyBrainMemoryTypes.ARMY_STATE);
            if (!entitiesIndexed || commanderState == null || soldierState == null) {
                if (helper.getTick() - startedAt[0] >= 120L) {
                    complete[0] = true;
                    helper.fail("Grouped Protect Entity squad never entered the live brain: indexed="
                            + entitiesIndexed + ", states=" + (commanderState != null) + "/"
                            + (soldierState != null));
                }
                return;
            }

            if (threatStartedAtRecruitTick[0] < 0) {
                attacker.setTarget(protectedRecruit);
                protectedRecruit.setLastHurtByMob(attacker);
                threatStartedAtRecruitTick[0] = commander.tickCount;
                return;
            }

            boolean commanderDefending = commander.getTarget() == attacker
                    && attacker.getUUID().equals(commanderState.selectedTargetId());
            boolean soldierDefending = soldier.getTarget() == attacker
                    && attacker.getUUID().equals(soldierState.selectedTargetId());
            if (commanderDefending && soldierDefending) {
                complete[0] = true;
                ArmyFieldCommandService.clearReplayHistory(owner.getUUID());
                commander.discard();
                soldier.discard();
                protectedRecruit.discard();
                attacker.discard();
                helper.succeed();
                return;
            }
            if (commander.tickCount - threatStartedAtRecruitTick[0] >= 140) {
                complete[0] = true;
                helper.fail("Grouped Protect Entity did not defend the marked ally: commander="
                        + commander.getTarget() + "/" + commanderState.selectedTargetId()
                        + ", soldier=" + soldier.getTarget() + "/" + soldierState.selectedTargetId()
                        + ", protectedThreat=" + (protectedRecruit.getLastHurtByMob() == attacker)
                        + ", order=" + data.armyGroup(squad.id()).orElseThrow().order());
            }
        });
    }

    private static void externalLibraryRuntime(GameTestHelper helper) {
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        if (!net.neoforged.fml.ModList.get().isLoaded("smartbrainlib")
                || !net.neoforged.fml.ModList.get().isLoaded("architectury")) {
            helper.fail("Required SmartBrainLib or Architectury runtime was not discovered");
            return;
        }
        if (!(recruit.getBrain() instanceof net.tslat.smartbrainlib.api.internal.SmartBrain<?>)) {
            helper.fail("Recruit did not receive SmartBrainLib's runtime brain");
            return;
        }
        if (!recruit.getBrain().checkMemory(
                net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)
                || !recruit.getBrain().checkMemory(
                net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH,
                net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)) {
            helper.fail("SmartBrain movement memories were not registered");
            return;
        }
        Identifier expectedChannel = Identifier.fromNamespaceAndPath("galacticwars", "main");
        if (!galacticwars.clonewars.network.GalacticNetwork.CHANNEL.id().equals(expectedChannel)) {
            helper.fail("Architectury channel facade did not expose the galacticwars:main identifier");
            return;
        }
        if (!codecRoundTrips(
                        new galacticwars.clonewars.network.ForceActivatePayload(UUID.randomUUID(), 2),
                        galacticwars.clonewars.network.ForceActivatePayload.STREAM_CODEC)
                || !codecRoundTrips(
                        new galacticwars.clonewars.network.VehicleInputPayload(
                                UUID.randomUUID(), 42, 0.75F, -0.5F, true, false, true),
                        galacticwars.clonewars.network.VehicleInputPayload.STREAM_CODEC)
                || !codecRoundTrips(
                        new galacticwars.clonewars.network.MenuActionPayload(
                                UUID.randomUUID(), 7, 255,
                                Optional.of(UUID.randomUUID()), Optional.of(UUID.randomUUID()),
                                GameplayDataManager.generation(), 12),
                        galacticwars.clonewars.network.MenuActionPayload.STREAM_CODEC)
                || !codecRoundTrips(
                        new galacticwars.clonewars.network.ForceHudPayload(81, 4, 17, 0),
                        galacticwars.clonewars.network.ForceHudPayload.STREAM_CODEC)
                || !codecRoundTrips(
                        new galacticwars.clonewars.network.CommandCenterStatePayload(
                                7,
                                populatedDashboard(helper.getLevel().getGameTime())),
                        galacticwars.clonewars.network.CommandCenterStatePayload.STREAM_CODEC)) {
            helper.fail("An Architectury custom-payload codec did not round-trip cleanly");
            return;
        }
        helper.succeed();
    }

    private static <T> boolean codecRoundTrips(
            T expected,
            net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> codec
    ) {
        net.minecraft.network.RegistryFriendlyByteBuf buffer =
                new net.minecraft.network.RegistryFriendlyByteBuf(
                        io.netty.buffer.Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY);
        try {
            codec.encode(buffer, expected);
            return expected.equals(codec.decode(buffer)) && !buffer.isReadable();
        } finally {
            buffer.release();
        }
    }

    private static CommandCenterDashboardState populatedDashboard(long gameTime) {
        UUID actorId = UUID.randomUUID();
        UUID kingdomId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID foreignKingdomId = UUID.randomUUID();
        return new CommandCenterDashboardState(
                Math.max(0L, gameTime), GameplayDataManager.generation(), 7,
                true, actorId, kingdomId, "galacticwars:republic", "owner",
                120, 40, true,
                CommandCenterDashboardState.ActionAvailability.accepted(),
                List.of(new CommandCenterDashboardState.VehicleFabricationSummary(
                        "barc_speeder",
                        CommandCenterDashboardState.ActionAvailability.rejected(
                                "missing_materials"),
                        32,
                        List.of(new CommandCenterDashboardState.StockRequirementSummary(
                                "galacticwars:duracrete", 16, 4)))),
                3, 8, 1, 1, true, true, 2, 3, 1,
                List.of(candidateId),
                List.of(new CommandCenterDashboardState.ClaimSummary(
                        UUID.randomUUID(), "minecraft:overworld", 2, 3, 9, true)),
                List.of(new CommandCenterDashboardState.SquadSummary(
                        UUID.randomUUID(), "Aurek Squad", Optional.of(actorId), List.of(memberId),
                        2, "follow", "line", "active", 16)),
                List.of(new CommandCenterDashboardState.CombatTargetSummary(
                        UUID.randomUUID(), "B1 Battle Droid", "galacticwars:separatist", 14)),
                List.of(new CommandCenterDashboardState.BlueprintSummary(
                        CommandCenterDashboardState.BlueprintSummary.targetId("galacticwars:forward_base"),
                        "galacticwars:forward_base", "Forward Base", List.of(0, 1, 2, 3), 12,
                        List.of(new CommandCenterDashboardState.MaterialSummary(
                                "minecraft:stone_bricks", 12)), 4, 9, 1)),
                List.of(candidateId),
                List.of(new CommandCenterDashboardState.BuildSummary(
                        UUID.randomUUID(), "galacticwars:forward_base", "active", 4, 12, "")),
                List.of(new CommandCenterDashboardState.WorkOrderSummary(
                        UUID.randomUUID(), "build", "assigned", Optional.of(candidateId), 4, 12, "")),
                List.of(new CommandCenterDashboardState.WorkerSummary(
                        candidateId, "CT-6116", "builder", "work_at_site", "interact",
                        "build_place", Optional.of(new CommandCenterDashboardState.PositionSummary(
                        "minecraft:overworld", 2, 64, 3)), 8,
                        Optional.of(new CommandCenterDashboardState.PositionSummary(
                                "minecraft:overworld", 1, 64, 1)),
                        Optional.of(new CommandCenterDashboardState.PositionSummary(
                                "minecraft:overworld", 3, 64, 3)), Optional.empty(), 2, 24, 5)),
                List.of(new CommandCenterDashboardState.QuestSummary(
                        "galacticwars:republic_chapter_1", false, 40, List.of("workforce"),
                        List.of(new CommandCenterDashboardState.ObjectiveSummary(
                                "command_center", 1, 1)))),
                List.of(new CommandCenterDashboardState.NearbyPlayerSummary(
                        UUID.randomUUID(), "Nearby Player", 6)),
                List.of(new CommandCenterDashboardState.MemberSummary(actorId, "owner")),
                List.of(new CommandCenterDashboardState.ForeignKingdomSummary(
                        foreignKingdomId, UUID.randomUUID(), "galacticwars:separatist")),
                List.of(new CommandCenterDashboardState.InviteSummary(
                        UUID.randomUUID(), kingdomId, actorId, memberId, "member", gameTime + 1200L)),
                List.of(new CommandCenterDashboardState.DiplomacyProposalSummary(
                        UUID.randomUUID(), foreignKingdomId, kingdomId, "ally", gameTime + 1200L)),
                List.of(new CommandCenterDashboardState.ConflictSummary(
                        "kamino_platform", "conquest_counterattack", "galacticwars:kamino",
                        128, 96, "scheduled", 0, 200, gameTime + 1200L,
                        "galacticwars:separatist", "galacticwars:republic")));
    }

    private static void blasterFriendlyFire(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        recruit.tame(owner);
        Arrow bolt = new Arrow(
                helper.getLevel(), owner,
                new ItemStack(ModItems.ENERGY_CELL.get()),
                new ItemStack(ModItems.DC15_BLASTER.get()));
        boolean ownerHitBlocked = BlasterCombatEvents.handleProjectileImpact(bolt, recruit);
        if (!ownerHitBlocked || !bolt.isRemoved()) {
            helper.fail("Owned recruit was not protected from its owner's blaster bolt");
            return;
        }

        GalacticRecruitEntity squadmate = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 2));
        squadmate.tame(owner);
        Arrow recruitBolt = new Arrow(
                helper.getLevel(), squadmate,
                new ItemStack(ModItems.ENERGY_CELL.get()),
                new ItemStack(ModItems.DC15_BLASTER.get()));
        boolean recruitHitBlocked = BlasterCombatEvents.handleProjectileImpact(recruitBolt, recruit);
        if (!recruitHitBlocked || !recruitBolt.isRemoved()) {
            helper.fail("Same-owner squadmate was not protected from a recruit blaster bolt");
            return;
        }
        Arrow bowArrow = new Arrow(
                helper.getLevel(), squadmate,
                new ItemStack(Items.ARROW),
                new ItemStack(ModItems.NIGHTSISTER_BOW.get()));
        boolean bowHitBlocked = BlasterCombatEvents.handleProjectileImpact(bowArrow, recruit);
        if (!bowHitBlocked || !bowArrow.isRemoved()) {
            helper.fail("Same-owner squadmate was not protected from a Nightsister bow projectile");
            return;
        }
        Arrow vanillaArrow = new Arrow(EntityTypes.ARROW, helper.getLevel());
        boolean vanillaHitBlocked = BlasterCombatEvents.handleProjectileImpact(vanillaArrow, recruit);
        if (vanillaHitBlocked || vanillaArrow.isRemoved()) {
            helper.fail("Untagged vanilla arrow was incorrectly handled as a faction projectile");
            return;
        }

        ServerPlayer republicTarget = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        KingdomSavedData.get(helper.getLevel()).foundKingdom(
                republicTarget.getUUID(),
                "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(),
                helper.absolutePos(new BlockPos(1, 1, 1)).offset(0, 0, 50_000));
        GalacticRecruitEntity separatistGuard = helper.spawn(
                ModEntityTypes.B1_BATTLE_DROID.get(), new BlockPos(4, 1, 2));
        separatistGuard.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        Arrow hostileBolt = new Arrow(
                helper.getLevel(), separatistGuard,
                new ItemStack(ModItems.ENERGY_CELL.get()),
                new ItemStack(ModItems.DC15_BLASTER.get()));
        boolean hostileImpactBlocked = BlasterCombatEvents.handleProjectileImpact(hostileBolt, republicTarget);
        boolean hostileHitBlocked = !Config.ALLOW_BLASTER_PVP.getAsBoolean();
        if (hostileImpactBlocked != hostileHitBlocked || hostileBolt.isRemoved() != hostileHitBlocked) {
            helper.fail("Hostile faction recruit projectile did not honor the PvP setting");
            return;
        }

        ServerPlayer neutralTarget = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        Arrow neutralBolt = new Arrow(
                helper.getLevel(), separatistGuard,
                new ItemStack(ModItems.ENERGY_CELL.get()),
                new ItemStack(ModItems.DC15_BLASTER.get()));
        boolean neutralHitBlocked = BlasterCombatEvents.handleProjectileImpact(neutralBolt, neutralTarget);
        if (!neutralHitBlocked || !neutralBolt.isRemoved()) {
            helper.fail("Faction recruit projectile damaged a neutral player");
            return;
        }

        SmartBrainTestArea collisionArea = prepareSmartBrainTestArea(
                helper, GameType.CREATIVE, 0, 16, 0, 3);
        ServerPlayer collisionOwner = collisionArea.player();
        GalacticRecruitEntity collisionShooter = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), collisionArea.at(2, 1, 1));
        GalacticRecruitEntity protectedRecruit = spawnRecruitAt(
                helper, ModEntityTypes.ARC_TROOPER.get(), collisionArea.at(7, 1, 1));
        GalacticRecruitEntity collisionTarget = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), collisionArea.at(13, 1, 1));
        collisionShooter.tame(collisionOwner);
        protectedRecruit.tame(collisionOwner);
        collisionShooter.setNoAi(true);
        protectedRecruit.setNoAi(true);
        collisionTarget.setNoAi(true);
        collisionShooter.setNoGravity(true);
        protectedRecruit.setNoGravity(true);
        collisionTarget.setNoGravity(true);
        collisionTarget.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        protectedRecruit.getAttribute(Attributes.MAX_HEALTH).setBaseValue(128.0D);
        protectedRecruit.setHealth(protectedRecruit.getMaxHealth());
        collisionTarget.getAttribute(Attributes.MAX_HEALTH).setBaseValue(128.0D);
        collisionTarget.setHealth(collisionTarget.getMaxHealth());
        ItemStack collisionWeapon = new ItemStack(ModItems.DC15_BLASTER.get());
        collisionShooter.setItemInHand(InteractionHand.MAIN_HAND, collisionWeapon);
        collisionShooter.createCargoContainer().setItem(
                0, new ItemStack(ModItems.ENERGY_CELL.get()));
        long collisionIndexWaitStartedTick = helper.getTick();
        int[] collisionPhase = {0};
        long[] collisionPhaseStartedTick = {helper.getTick()};
        BlasterBoltEntity[] crossingBolt = {null};
        helper.onEachTick(() -> {
            if (collisionPhase[0] == 3) {
                return;
            }
            if (collisionPhase[0] == 0) {
                boolean entitiesIndexed = helper.getLevel().getEntity(collisionShooter.getUUID())
                        == collisionShooter
                        && helper.getLevel().getEntity(protectedRecruit.getUUID()) == protectedRecruit
                        && helper.getLevel().getEntity(collisionTarget.getUUID()) == collisionTarget;
                boolean chunkTicking = helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                        ChunkPos.containing(collisionShooter.blockPosition()));
                if (!entitiesIndexed || !chunkTicking) {
                    if (helper.getTick() - collisionIndexWaitStartedTick
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        collisionPhase[0] = 3;
                        helper.fail("Collision-level friendly-fire fixtures never became ready: indexed="
                                + entitiesIndexed + ", chunkTicking=" + chunkTicking);
                    }
                    return;
                }
                if (!RecruitAmmunitionService.tryConsumeForShot(collisionShooter)) {
                    collisionPhase[0] = 3;
                    helper.fail("Collision-level friendly-fire shooter could not consume its Energy Cell");
                    return;
                }
                ModItems.DC15_BLASTER.get().fireAt(
                        helper.getLevel(), collisionShooter, collisionTarget, collisionWeapon);
                collisionPhase[0] = 1;
                collisionPhaseStartedTick[0] = helper.getTick();
                return;
            }

            if (collisionPhase[0] == 1) {
                if (helper.getTick() == collisionPhaseStartedTick[0]) {
                    return;
                }
                List<BlasterBoltEntity> crossingBolts = helper.getLevel().getEntitiesOfClass(
                        BlasterBoltEntity.class,
                        collisionShooter.getBoundingBox().inflate(20.0D),
                        candidate -> candidate.getOwner() == collisionShooter);
                if (crossingBolts.size() != 1) {
                    collisionPhase[0] = 3;
                    helper.fail("Collision-level friendly-fire fixture did not create exactly one recruit-owned bolt");
                    return;
                }
                crossingBolt[0] = crossingBolts.getFirst();
                Vec3 collisionPathStart = crossingBolt[0].position();
                Vec3 collisionPathEnd = collisionPathStart.add(
                        crossingBolt[0].getDeltaMovement().normalize().scale(24.0D));
                if (protectedRecruit.getBoundingBox().clip(collisionPathStart, collisionPathEnd).isEmpty()
                        || collisionTarget.getBoundingBox().clip(collisionPathStart, collisionPathEnd).isEmpty()) {
                    collisionPhase[0] = 3;
                    helper.fail("Recruit-owned bolt trajectory did not cross both collision fixtures: start="
                            + collisionPathStart + ", end=" + collisionPathEnd
                            + ", protectedBox=" + protectedRecruit.getBoundingBox()
                            + ", enemyBox=" + collisionTarget.getBoundingBox()
                            + ", protectedHittable=" + protectedRecruit.canBeHitByProjectile()
                            + ", enemyHittable=" + collisionTarget.canBeHitByProjectile());
                    return;
                }
                collisionPhase[0] = 2;
                return;
            }
            if (protectedRecruit.hurtTime > 0
                    || protectedRecruit.getLastDamageSource() != null) {
                collisionPhase[0] = 3;
                helper.fail("Real recruit-owned bolt damaged the same-owner recruit in its path: health="
                        + protectedRecruit.getHealth() + ", hurtTime=" + protectedRecruit.hurtTime
                        + ", source=" + protectedRecruit.getLastDamageSource());
                return;
            }
            if (collisionTarget.hurtTime > 0
                    || collisionTarget.getLastDamageSource() != null) {
                collisionPhase[0] = 3;
                helper.fail("Friendly-fire bolt crossed the protected recruit and damaged the enemy behind it: health="
                        + collisionTarget.getHealth() + ", hurtTime=" + collisionTarget.hurtTime
                        + ", source=" + collisionTarget.getLastDamageSource());
                return;
            }
            if (crossingBolt[0].isRemoved()) {
                if (crossingBolt[0].position().distanceToSqr(protectedRecruit.position()) > 6.25D) {
                    collisionPhase[0] = 3;
                    helper.fail("Recruit-owned bolt was removed away from the protected collision target");
                    return;
                }
                collisionPhase[0] = 3;
                collisionShooter.discard();
                protectedRecruit.discard();
                collisionTarget.discard();
                helper.succeed();
                return;
            }
            if (helper.getTick() - collisionPhaseStartedTick[0] >= 20L) {
                collisionPhase[0] = 3;
                helper.fail("Recruit-owned bolt did not collide with and stop at the protected recruit: bolt="
                        + crossingBolt[0].position() + ", velocity=" + crossingBolt[0].getDeltaMovement()
                        + ", protected=" + protectedRecruit.position()
                        + ", enemy=" + collisionTarget.position());
            }
        });
    }

    private static void recruitSpawnEggs(GameTestHelper helper) {
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        List<SpawnEggCase> cases = List.of(
                new SpawnEggCase(ModItems.CLONE_TROOPER_SPAWN_EGG.get(), ModEntityTypes.CLONE_TROOPER.get()),
                new SpawnEggCase(ModItems.ARC_TROOPER_SPAWN_EGG.get(), ModEntityTypes.ARC_TROOPER.get()),
                new SpawnEggCase(ModItems.PHASE_I_CLONE_TROOPER_SPAWN_EGG.get(),
                        ModEntityTypes.PHASE_I_CLONE_TROOPER.get()),
                new SpawnEggCase(ModItems.PHASE_I_ARC_TROOPER_SPAWN_EGG.get(),
                        ModEntityTypes.PHASE_I_ARC_TROOPER.get()),
                new SpawnEggCase(ModItems.SENATE_COMMANDO_SPAWN_EGG.get(),
                        ModEntityTypes.SENATE_COMMANDO.get()),
                new SpawnEggCase(ModItems.REPUBLIC_HONOR_GUARD_SPAWN_EGG.get(),
                        ModEntityTypes.REPUBLIC_HONOR_GUARD.get()),
                new SpawnEggCase(ModItems.JEDI_KNIGHT_SPAWN_EGG.get(), ModEntityTypes.JEDI_KNIGHT.get()),
                new SpawnEggCase(ModItems.MANDALORIAN_WARRIOR_SPAWN_EGG.get(), ModEntityTypes.MANDALORIAN_WARRIOR.get()),
                new SpawnEggCase(ModItems.MANDALORIAN_MARKSMAN_SPAWN_EGG.get(), ModEntityTypes.MANDALORIAN_MARKSMAN.get()),
                new SpawnEggCase(ModItems.MANDALORIAN_HEAVY_SPAWN_EGG.get(), ModEntityTypes.MANDALORIAN_HEAVY.get()),
                new SpawnEggCase(ModItems.B1_BATTLE_DROID_SPAWN_EGG.get(), ModEntityTypes.B1_BATTLE_DROID.get()),
                new SpawnEggCase(ModItems.SITH_ACOLYTE_SPAWN_EGG.get(), ModEntityTypes.SITH_ACOLYTE.get()),
                new SpawnEggCase(ModItems.B1_SECURITY_DROID_SPAWN_EGG.get(),
                        ModEntityTypes.B1_SECURITY_DROID.get()),
                new SpawnEggCase(ModItems.B2_SUPER_BATTLE_DROID_SPAWN_EGG.get(), ModEntityTypes.B2_SUPER_BATTLE_DROID.get()),
                new SpawnEggCase(ModItems.COMMANDO_DROID_SPAWN_EGG.get(), ModEntityTypes.COMMANDO_DROID.get()),
                new SpawnEggCase(ModItems.HUTT_ENFORCER_SPAWN_EGG.get(), ModEntityTypes.HUTT_ENFORCER.get()),
                new SpawnEggCase(ModItems.BOUNTY_HUNTER_SPAWN_EGG.get(), ModEntityTypes.BOUNTY_HUNTER.get()),
                new SpawnEggCase(ModItems.SMUGGLER_SPAWN_EGG.get(), ModEntityTypes.SMUGGLER.get()),
                new SpawnEggCase(ModItems.NIGHTSISTER_ACOLYTE_SPAWN_EGG.get(), ModEntityTypes.NIGHTSISTER_ACOLYTE.get(), false),
                new SpawnEggCase(ModItems.NIGHTSISTER_ARCHER_SPAWN_EGG.get(), ModEntityTypes.NIGHTSISTER_ARCHER.get(), false),
                new SpawnEggCase(ModItems.NIGHTBROTHER_BRUTE_SPAWN_EGG.get(), ModEntityTypes.NIGHTBROTHER_BRUTE.get(), false),
                new SpawnEggCase(ModItems.REPUBLIC_CIVILIAN_SPAWN_EGG.get(), ModEntityTypes.REPUBLIC_CIVILIAN.get(), true),
                new SpawnEggCase(ModItems.TOGRUTA_CIVILIAN_SPAWN_EGG.get(),
                        ModEntityTypes.TOGRUTA_CIVILIAN.get(), true),
                new SpawnEggCase(ModItems.SEPARATIST_TECHNICIAN_SPAWN_EGG.get(), ModEntityTypes.SEPARATIST_TECHNICIAN.get(), true),
                new SpawnEggCase(ModItems.MANDALORIAN_CLANSPERSON_SPAWN_EGG.get(), ModEntityTypes.MANDALORIAN_CLANSPERSON.get(), true),
                new SpawnEggCase(ModItems.HUTT_CIVILIAN_SPAWN_EGG.get(), ModEntityTypes.HUTT_CIVILIAN.get(), true),
                new SpawnEggCase(ModItems.NIGHTSISTER_CIVILIAN_SPAWN_EGG.get(), ModEntityTypes.NIGHTSISTER_CIVILIAN.get(), true));
        List<UUID> spawnedRecruitIds = new java.util.ArrayList<>();

        for (int index = 0; index < cases.size(); index++) {
            SpawnEggCase testCase = cases.get(index);
            BlockPos relativeClicked = new BlockPos(1 + index % 10 * 2, 1, 4 + index / 10 * 3);
            helper.setBlock(relativeClicked, Blocks.STONE);
            BlockPos clicked = helper.absolutePos(relativeClicked);
            BlockPos expectedSpawn = clicked.above();
            ItemStack eggStack = new ItemStack(testCase.item());
            if (!SpawnEggItem.spawnsEntity(eggStack, testCase.type())) {
                helper.fail("Spawn egg item component does not identify " + testCase.type());
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, eggStack);
            InteractionResult result;
            try {
                result = player.gameMode.useItemOn(
                        player,
                        helper.getLevel(),
                        eggStack,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false));
            } catch (Throwable exception) {
                GalacticWars.LOGGER.error(
                        "Spawn egg threw while creating {}", testCase.type(), exception);
                helper.fail("Spawn egg threw while creating " + testCase.type() + ": " + exception);
                return;
            }
            if (!result.consumesAction()) {
                helper.fail("Spawn egg rejected " + testCase.type());
            }
            List<GalacticRecruitEntity> spawned = helper.getLevel().getEntitiesOfClass(
                    GalacticRecruitEntity.class,
                    new AABB(expectedSpawn).inflate(1.0D),
                    recruit -> recruit.getType() == testCase.type());
            if (spawned.size() != 1) {
                helper.fail("Spawn egg did not create " + testCase.type());
            }
            GalacticRecruitEntity recruit = spawned.getFirst();
            spawnedRecruitIds.add(recruit.getUUID());
            // GameTests share a server batch; isolate this lifecycle assertion from nearby combat tests.
            recruit.setInvulnerable(true);
            if (!recruit.isPersistenceRequired()) {
                helper.fail("Spawn egg recruit was not marked persistent: " + testCase.type());
            }
            validateCuratedNpcContract(helper, recruit);
            if (testCase.civilian()) {
                if (recruit.getServiceBranch() != NpcServiceBranch.CIVILIAN
                        || !recruit.getMainHandItem().isEmpty()) {
                    helper.fail("Civilian spawn egg did not apply its archetype: " + testCase.type());
                }
            } else if (recruit.getServiceBranch() != NpcServiceBranch.MILITARY
                    || recruit.getMainHandItem().isEmpty()) {
                helper.fail("Military spawn egg did not apply its unit loadout: " + testCase.type());
            }
            if (recruit.getType() == ModEntityTypes.PHASE_I_CLONE_TROOPER.get()
                    || recruit.getType() == ModEntityTypes.PHASE_I_ARC_TROOPER.get()) {
                if (!recruit.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PHASE_I_CLONE_HELMET.get())
                        || !recruit.getItemBySlot(EquipmentSlot.CHEST)
                        .is(ModItems.PHASE_I_CLONE_CHESTPLATE.get())
                        || !recruit.getItemBySlot(EquipmentSlot.LEGS)
                        .is(ModItems.PHASE_I_CLONE_LEGGINGS.get())
                        || !recruit.getItemBySlot(EquipmentSlot.FEET)
                        .is(ModItems.PHASE_I_CLONE_BOOTS.get())) {
                    helper.fail("Phase I spawn egg did not equip the complete Phase I armor set: "
                            + testCase.type());
                }
            }
        }

        ServerPlayer survivalPlayer = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        BlockPos replaceableFloor = helper.absolutePos(new BlockPos(15, 1, 11));
        BlockPos replaceableTarget = replaceableFloor.above();
        helper.getLevel().setBlockAndUpdate(replaceableFloor, Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(replaceableTarget, Blocks.SHORT_GRASS.defaultBlockState());
        ItemStack namedEgg = new ItemStack(ModItems.CLONE_TROOPER_SPAWN_EGG.get(), 2);
        namedEgg.set(DataComponents.CUSTOM_NAME, Component.literal("QA Clone"));
        survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, namedEgg);
        InteractionResult replaceableResult = survivalPlayer.gameMode.useItemOn(
                survivalPlayer,
                helper.getLevel(),
                namedEgg,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(replaceableTarget),
                        Direction.UP,
                        replaceableTarget,
                        false));
        List<GalacticRecruitEntity> namedRecruits = helper.getLevel().getEntitiesOfClass(
                GalacticRecruitEntity.class,
                new AABB(replaceableTarget).inflate(1.0D),
                recruit -> recruit.getType() == ModEntityTypes.CLONE_TROOPER.get()
                        && Component.literal("QA Clone").equals(recruit.getCustomName()));
        if (!replaceableResult.consumesAction() || namedEgg.getCount() != 1 || namedRecruits.size() != 1) {
            helper.fail("Survival spawn egg did not preserve replaceable-block, naming, or consumption behavior");
        }

        BlockPos relativeSpawner = new BlockPos(1, 1, 11);
        helper.setBlock(relativeSpawner, Blocks.SPAWNER);
        BlockPos spawnerPos = helper.absolutePos(relativeSpawner);
        SpawnerBlockEntity spawner = helper.getBlockEntity(
                relativeSpawner, SpawnerBlockEntity.class);
        ItemStack spawnerEgg = new ItemStack(ModItems.CLONE_TROOPER_SPAWN_EGG.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, spawnerEgg);
        InteractionResult spawnerResult = player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                spawnerEgg,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(spawnerPos), Direction.UP, spawnerPos, false));
        var spawnerDisplay = spawner.getSpawner().getOrCreateDisplayEntity(
                helper.getLevel(), spawnerPos);
        if (!spawnerResult.consumesAction()
                || spawnerDisplay == null
                || spawnerDisplay.getType() != ModEntityTypes.CLONE_TROOPER.get()) {
            helper.fail("Recruit spawn egg did not preserve vanilla spawner configuration behavior");
        }

        BlockPos relativeDispenser = new BlockPos(10, 1, 15);
        BlockPos dispenserPos = helper.absolutePos(relativeDispenser);
        var dispenserState = Blocks.DISPENSER.defaultBlockState()
                .setValue(DispenserBlock.FACING, Direction.EAST);
        helper.getLevel().setBlockAndUpdate(dispenserPos, dispenserState);
        DispenserBlockEntity dispenser = helper.getBlockEntity(
                relativeDispenser, DispenserBlockEntity.class);
        ItemStack dispenserEgg = new ItemStack(ModItems.B1_BATTLE_DROID_SPAWN_EGG.get());
        dispenser.setItem(0, dispenserEgg);
        dispenser.setItem(0, SpawnEggItemBehavior.INSTANCE.dispense(
                new BlockSource(helper.getLevel(), dispenserPos, dispenserState, dispenser),
                dispenserEgg));
        BlockPos dispenserSpawnPos = dispenserPos.relative(Direction.EAST);
        List<GalacticRecruitEntity> dispenserRecruits = helper.getLevel().getEntitiesOfClass(
                GalacticRecruitEntity.class,
                new AABB(dispenserSpawnPos).inflate(1.0D),
                recruit -> recruit.getType() == ModEntityTypes.B1_BATTLE_DROID.get());
        if (dispenserRecruits.size() != 1) {
            helper.fail("Dispenser spawn egg did not create its recruit");
        }
        GalacticRecruitEntity dispenserRecruit = dispenserRecruits.getFirst();
        spawnedRecruitIds.add(dispenserRecruit.getUUID());
        // Keep the dispenser case subject to normal AI ticking without allowing cross-test damage.
        dispenserRecruit.setInvulnerable(true);
        if (!dispenserRecruit.isPersistenceRequired()
                || dispenserRecruit.getServiceBranch() != NpcServiceBranch.MILITARY
                || dispenserRecruit.getMainHandItem().isEmpty()) {
            helper.fail("Dispenser spawn egg did not initialize recruit persistence and loadout");
        }

        BlockPos relativeFurnace = new BlockPos(15, 1, 15);
        helper.setBlock(relativeFurnace, Blocks.FURNACE);
        BlockPos furnacePos = helper.absolutePos(relativeFurnace);
        long spawnGameTime = helper.getLevel().getGameTime();
        helper.runAfterDelay(40, () -> {
            for (UUID recruitId : spawnedRecruitIds) {
                if (!(helper.getLevel().getEntity(recruitId) instanceof GalacticRecruitEntity recruit)
                        || !recruit.isAlive()) {
                    helper.fail("Spawned recruit stopped surviving server ticks: " + recruitId);
                    return;
                }
            }
            if (helper.getLevel().getGameTime() < spawnGameTime + 40L) {
                helper.fail("Server stopped ticking after recruit spawn-egg use");
                return;
            }
            if (!(helper.getLevel().getBlockEntity(furnacePos) instanceof Container furnace)) {
                helper.fail("Furnace block entity became unavailable after recruit spawn-egg use");
                return;
            }
            ItemStack furnaceInput = new ItemStack(Items.RAW_IRON);
            furnace.setItem(0, furnaceInput);
            if (!furnace.getItem(0).is(Items.RAW_IRON)) {
                helper.fail("Server could not process a furnace inventory interaction after recruit spawn-egg use");
                return;
            }
            helper.succeed();
        });
    }

    private static void kingdomHallAuthority(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 9);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer intruder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        if (!hall.claim(owner) || hall.claim(intruder) || !hall.isOwner(owner)) {
            helper.fail("Command Center ownership guard rejected the owner or accepted an intruder");
        }
        long claimGameTime = helper.getLevel().getGameTime();
        if (!hall.chargeDailyUpkeep(Math.addExact(claimGameTime, 23999L), 1)) {
            helper.fail("New Command Center was charged upkeep before its first full day elapsed");
        }
        hall.setFaction("galacticwars:mandalorian");
        if (hall.getUpdatePacket() == null
                || !hall.getUpdateTag(helper.getLevel().registryAccess())
                        .getStringOr("CommandCenterFaction", "")
                        .equals("galacticwars:mandalorian")) {
            helper.fail("Command Center custom state was not exposed through its client update packet");
        }
        hall.setItem(0, new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 32));
        if (!hall.chargeDailyUpkeep(claimGameTime + 72000L, 1)
                || hall.treasuryCredits() != 26) {
            helper.fail("Command Center did not collect three elapsed days of faction upkeep");
        }
        if (!hall.reserveCredits(10) || hall.treasuryCredits() != 16 || hall.refundCredits(5) != 5
                || hall.treasuryCredits() != 21) {
            helper.fail("Command Center treasury did not conserve reserved and refunded credits");
        }
        if (!hall.reserveCredits(21) || hall.getItem(0) != ItemStack.EMPTY) {
            helper.fail("Command Center treasury did not normalize a depleted slot to ItemStack.EMPTY");
        }
        if (hall.chargeDailyUpkeep(claimGameTime + 96000L, 1)) {
            helper.fail("Empty Command Center treasury incorrectly paid accumulated upkeep");
        }
        hall.setItem(0, new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 2));
        if (!hall.chargeDailyUpkeep(claimGameTime + 96000L, 1)
                || hall.treasuryCredits() != 0) {
            helper.fail("Command Center did not settle pending upkeep immediately after resupply");
        }
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos);
        if (!kingdom.ownerId().equals(owner.getUUID())
                || data.kingdomForOwner(owner.getUUID()).isEmpty()) {
            helper.fail("Kingdom state was not stored authoritatively in overworld SavedData");
        }
        BlockPos absoluteHall = hallPos;
        UUID overlappingOwner = UUID.randomUUID();
        if (data.activateHall(
                        overlappingOwner,
                        "galacticwars:separatist",
                        helper.getLevel().dimension().identifier().toString(),
                        absoluteHall.offset(32, 0, 0)).isPresent()
                || data.kingdomForOwner(overlappingOwner).isPresent()) {
            helper.fail("Overlapping Command Center capital claims were accepted");
            return;
        }
        RecruitmentCampaign campaign = new RecruitmentCampaign(
                UUID.randomUUID(),
                "galacticwars:mandalorian_rider",
                "",
                12,
                claimGameTime + 24000L,
                RecruitmentCampaignState.RESERVED,
                "reserved");
        if (!data.beginCampaign(owner.getUUID(), RecruitmentCampaignDecision.accepted(campaign))
                || !data.replaceCampaign(owner.getUUID(), campaign.cancel("commander_lost"))
                || hall.settlePendingCampaignRefunds(helper.getLevel()) != 12
                || hall.treasuryCredits() != 12
                || data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement().recruitmentCampaigns().stream()
                        .filter(stored -> stored.id().equals(campaign.id()))
                        .findFirst()
                        .orElseThrow()
                        .reservedCost() != 0) {
            helper.fail("Cancelled commander campaign refund was not conserved through SavedData and Hall storage");
        }
        BlockPos relocatedHall = absoluteHall.offset(64, 0, 0);
        if (data.activateHall(owner.getUUID(), hall.factionId(),
                        helper.getLevel().dimension().identifier().toString(), relocatedHall).isPresent()) {
            helper.fail("A second active Command Center was accepted");
        }
        BlockPos localDropHallPos = new BlockPos(1, 1, 1);
        helper.setBlock(localDropHallPos, ModBlocks.COMMAND_CENTER.get());
        CommandCenterBlockEntity localDropHall = helper.getBlockEntity(
                localDropHallPos, CommandCenterBlockEntity.class);
        localDropHall.setItem(0, new ItemStack(
                galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 12));
        BlockPos absoluteDropHall = helper.absolutePos(localDropHallPos);
        ModBlocks.COMMAND_CENTER.get().playerWillDestroy(
                helper.getLevel(), absoluteDropHall,
                helper.getLevel().getBlockState(absoluteDropHall), intruder);
        int localRemovalRefund = helper.getLevel().getEntitiesOfClass(
                        ItemEntity.class,
                        new net.minecraft.world.phys.AABB(absoluteDropHall).inflate(3.0))
                .stream()
                .filter(item -> item.getItem().is(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get()))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        if (localRemovalRefund != 12 || !localDropHall.isEmpty()) {
            helper.fail("Command Center inventory removal did not conserve its physical drops");
        }
        ModBlocks.COMMAND_CENTER.get().playerWillDestroy(
                helper.getLevel(), absoluteHall, helper.getLevel().getBlockState(absoluteHall), intruder);
        if (!data.isHallActive(owner.getUUID()) || hall.treasuryCredits() != 12) {
            helper.fail("Intruder Hall removal bypassed owner authority");
        }
        owner.setPos(absoluteHall.getX() + 0.5, absoluteHall.getY(), absoluteHall.getZ() + 0.5);
        helper.runAfterDelay(2, () -> {
            CommandCenterBlockEntity removalHall = (CommandCenterBlockEntity) helper.getLevel()
                    .getBlockEntity(absoluteHall);
            int treasuryBeforeRemoval = removalHall == null ? -1 : removalHall.treasuryCredits();
            ModBlocks.COMMAND_CENTER.get().playerWillDestroy(
                    helper.getLevel(), absoluteHall, helper.getLevel().getBlockState(absoluteHall), owner);
            if (data.isHallActive(owner.getUUID()) || removalHall == null || !removalHall.isEmpty()) {
                helper.fail("Owner Hall removal did not conserve inventory and deactivate authority: active="
                        + data.isHallActive(owner.getUUID()) + ", treasuryBefore=" + treasuryBeforeRemoval
                        + ", hallPresent=" + (removalHall != null)
                        + ", empty=" + (removalHall != null && removalHall.isEmpty()));
            }
            KingdomRecord relocated = data.activateHall(owner.getUUID(), hall.factionId(),
                            helper.getLevel().dimension().identifier().toString(), relocatedHall)
                    .orElseThrow();
            if (!relocated.id().equals(kingdom.id())
                    || relocated.settlement().hallX() != relocatedHall.getX()
                    || relocated.settlement().hallY() != relocatedHall.getY()
                    || relocated.settlement().hallZ() != relocatedHall.getZ()
                    || data.claimAt(
                            helper.getLevel().dimension().identifier().toString(),
                            new net.minecraft.world.level.ChunkPos(
                                    absoluteHall.getX() >> 4, absoluteHall.getZ() >> 4))
                            .isPresent()
                    || data.claimAt(
                            helper.getLevel().dimension().identifier().toString(),
                            new net.minecraft.world.level.ChunkPos(
                                    relocatedHall.getX() >> 4, relocatedHall.getZ() >> 4))
                            .filter(claim -> claim.kingdomId().equals(kingdom.id())).isEmpty()) {
                helper.fail("Command Center relocation did not preserve kingdom identity and update its position");
            }

            int housingBeforeReward = relocated.settlement().housingCapacity();
            KingdomBaseBlueprint farmBlueprint = GameplayDataManager.snapshot()
                    .blueprint("galacticwars:moisture_farm").orElseThrow();
            BuildProject farmProject = fullyProgressProject(
                    data, owner.getUUID(), farmBlueprint,
                    helper.getLevel().dimension().identifier().toString(),
                    relocatedHall.offset(4, 0, 0));
            if (!data.completeBuildProject(owner.getUUID(), farmProject, farmBlueprint)
                    || data.completeBuildProject(owner.getUUID(), farmProject, farmBlueprint)) {
                helper.fail("Build rewards were not applied exactly once");
            }
            KingdomRecord rewarded = data.kingdomForOwner(owner.getUUID()).orElseThrow();
            if (rewarded.settlement().housingCapacity() != housingBeforeReward + farmBlueprint.housingReward()
                    || rewarded.settlement().buildProjects().stream()
                            .filter(project -> project.blueprintId().equals(farmBlueprint.id()))
                            .count() != 1
                    || rewarded.settlement().worksites().stream()
                            .filter(worksite -> worksite.type().equals("farmer"))
                            .count() != 1) {
                helper.fail("Build completion rewards were duplicated or omitted");
            }
            Tag encoded = KingdomSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
            KingdomSavedData restored = KingdomSavedData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
            KingdomRecord restoredReward = restored.kingdomForOwner(owner.getUUID()).orElseThrow();
            if (!restoredReward.settlement().containsCompletedProject(farmProject)
                    || restoredReward.settlement().housingCapacity()
                            != housingBeforeReward + farmBlueprint.housingReward()
                    || restoredReward.settlement().worksites().stream()
                            .filter(worksite -> worksite.type().equals("farmer"))
                            .count() != 1) {
                helper.fail("Build completion rewards did not survive a SavedData codec round trip");
            }
            helper.succeed();
        });
    }

    private static void commandCenterPlayerInteraction(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 28);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        player.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 2.5D);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        InteractionResult result;
        try {
            result = player.gameMode.useItemOn(
                    player,
                    helper.getLevel(),
                    ItemStack.EMPTY,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(hallPos), Direction.SOUTH, hallPos, false));
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage();
            if (!hall.isOwner(player)
                    || !message.contains("advanced_open_screen may not be sent to the client")) {
                helper.fail("Command Center interaction failed before its headless-client menu boundary: "
                        + exception);
                return;
            }
            helper.succeed();
            return;
        }
        if (!result.consumesAction()
                || !hall.isOwner(player)
                || !(player.containerMenu instanceof FactionSelectionMenu)) {
            helper.fail("Empty-hand Command Center use did not claim the Hall and open faction selection: result="
                    + result + ", owner=" + hall.isOwner(player)
                    + ", menu=" + player.containerMenu.getClass().getSimpleName());
            return;
        }
        helper.succeed();
    }

    private static void commandCenterMultiplayerInvite(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 18);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer candidate = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer distant = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        candidate.setPos(hallPos.getX() + 4.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        distant.setPos(hallPos.getX() + 40.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), hallPos);

        CommandCenterOperationsMenu staleMenu = (CommandCenterOperationsMenu)
                new CommandCenterOperationsMenuProvider(hallPos)
                        .createMenu(1, owner.getInventory(), owner);
        List<CommandCenterDashboardState.NearbyPlayerSummary> inviteTargets =
                staleMenu.dashboardState().nearbyPlayers();
        if (inviteTargets.size() != 1
                || !inviteTargets.getFirst().playerId().equals(candidate.getUUID())
                || inviteTargets.stream().anyMatch(target -> target.playerId().equals(distant.getUUID()))) {
            helper.fail("Command Center did not expose only bounded explicit invite targets: "
                    + inviteTargets);
            return;
        }

        int observedRevision = staleMenu.dashboardState().settlementRevision();
        UUID revisionProbe = UUID.randomUUID();
        if (!data.registerRecruit(owner.getUUID(), revisionProbe, NpcServiceBranch.MILITARY)
                || staleMenu.handleReplayAction(
                        owner, UUID.randomUUID(), CommandCenterOperationsMenu.INVITE_NEAREST,
                        Optional.of(candidate.getUUID()), Optional.empty(),
                        staleMenu.dashboardState().contentGeneration(), observedRevision)) {
            helper.fail("Command Center accepted an action from a stale settlement revision");
            return;
        }
        data.unregisterRecruit(owner.getUUID(), revisionProbe);

        candidate.setPos(hallPos.getX() + 32.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        if (staleMenu.handleReplayAction(
                owner, UUID.randomUUID(), CommandCenterOperationsMenu.INVITE_NEAREST,
                Optional.of(candidate.getUUID()), Optional.empty())) {
            helper.fail("Command Center accepted a stale invite target outside embodied range");
            return;
        }
        candidate.setPos(hallPos.getX() + 4.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        CommandCenterOperationsMenu ownerMenu = (CommandCenterOperationsMenu)
                new CommandCenterOperationsMenuProvider(hallPos)
                        .createMenu(2, owner.getInventory(), owner);
        UUID replayId = UUID.randomUUID();
        boolean invited = ownerMenu.handleReplayAction(
                owner, replayId, CommandCenterOperationsMenu.INVITE_NEAREST,
                Optional.of(candidate.getUUID()), Optional.empty());
        boolean replayRejected = !ownerMenu.handleReplayAction(
                owner, replayId, CommandCenterOperationsMenu.INVITE_NEAREST,
                Optional.of(candidate.getUUID()), Optional.empty());
        var invite = data.pendingInvites().stream()
                .filter(value -> value.kingdomId().equals(kingdom.id()))
                .filter(value -> value.targetPlayerId().equals(candidate.getUUID()))
                .findFirst().orElse(null);
        if (!invited || !replayRejected || invite == null) {
            helper.fail("Explicit multiplayer invitation was not replay-safe: invited="
                    + invited + ", replay=" + replayRejected + ", invite=" + invite);
            return;
        }

        CommandCenterOperationsMenu candidateMenu = (CommandCenterOperationsMenu)
                new CommandCenterOperationsMenuProvider(hallPos)
                        .createMenu(3, candidate.getInventory(), candidate);
        if (!candidateMenu.dashboardState().actorRole().equals("visitor")
                || !candidateMenu.handleReplayAction(
                        candidate, UUID.randomUUID(), CommandCenterOperationsMenu.ACCEPT_INVITE,
                        Optional.of(invite.id()), Optional.empty())
                || data.kingdomForPlayer(candidate.getUUID())
                        .filter(joined -> joined.id().equals(kingdom.id()))
                        .flatMap(joined -> joined.member(candidate.getUUID()))
                        .filter(member -> member.role() == KingdomMemberRole.MEMBER)
                        .isEmpty()
                || !hall.canOpen(candidate)) {
            helper.fail("Invited player could not accept into the authoritative kingdom");
            return;
        }
        helper.succeed();
    }

    private static void forceEmbodiedRuntime(GameTestHelper helper) {
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        ForceSavedData force = ForceSavedData.get(helper.getLevel());
        ServerPlayer light = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos lightOrigin = helper.absolutePos(new BlockPos(2, 1, 2))
                .offset(500_000, 0, 0)
                .atY(200);
        helper.getLevel().getChunkAt(lightOrigin);
        light.setPos(lightOrigin.getX() + 0.5D, lightOrigin.getY(), lightOrigin.getZ() + 0.5D);
        light.setYRot(0.0F);
        light.setYHeadRot(0.0F);
        light.setXRot(0.0F);
        completeForceCampaign(progression, light, "galacticwars:republic",
                "clone_trooper", "kamino");
        BlockPos jediShrine = lightOrigin.offset(2, 0, 0);
        helper.getLevel().setBlock(jediShrine, ModBlocks.JEDI_MEDITATION_SHRINE.get().defaultBlockState(), 3);
        if (!ForceShrineService.open(light, jediShrine, "jedi")) {
            helper.fail("Republic chapter-one player could not initiate at the physical Jedi shrine");
            return;
        }
        force.update(light.getUUID(), state -> ForceProgressionService.gainMastery(
                state, 10, LaunchContentCatalog.data()).state());
        force.update(light.getUUID(), state -> ForceProgressionService.learnNode(
                state, "light_pull", LaunchContentCatalog.data()).state());
        force.update(light.getUUID(), state -> ForceProgressionService.equip(
                state, 1, "light_pull", LaunchContentCatalog.data()).state());

        GalacticRecruitEntity ally = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), lightOrigin.offset(0, 0, 3));
        ally.setNoAi(true);
        ally.initializeFromSpawnEgg();
        ally.tame(light);
        var pushed = spawnEntityAt(
                helper, EntityTypes.ZOMBIE, lightOrigin.offset(0, 0, 6));
        pushed.setNoAi(true);
        helper.runAfterDelay(1, () -> {
        ally.setDeltaMovement(Vec3.ZERO);
        pushed.setDeltaMovement(Vec3.ZERO);
        UUID lightPushId = UUID.randomUUID();
        var lightPreflight = ForceAbilityRuntimeService.activate(
                progression.state(light.getUUID()), force.state(light.getUUID()), lightPushId,
                "light_push", helper.getLevel().getGameTime(), false,
                Config.ALLOW_FORCE_PVP.getAsBoolean());
        boolean lightPush = ForceWorldEffectService.activate(light, lightPushId, 0);
        int lightAfterPush = force.state(light.getUUID()).energy();
        boolean lightReplay = ForceWorldEffectService.activate(light, lightPushId, 0);
        boolean lightCooldown = !ForceWorldEffectService.activate(
                light, UUID.randomUUID(), 0);
        int recordedLightPushUses = progression.state(light.getUUID()).subjectTotal(
                ProgressionEventType.FORCE_ABILITY_USED, Set.of("light_push"));
        Vec3 lightLook = light.getLookAngle().normalize();
        double targetDot = pushed.getEyePosition().subtract(light.getEyePosition())
                .normalize().dot(lightLook);
        List<net.minecraft.world.entity.LivingEntity> nearbyLiving = helper.getLevel()
                .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                        light.getBoundingBox().inflate(16.0D));
        if (!lightPush || !lightReplay || !lightCooldown || lightAfterPush != 80
                || force.state(light.getUUID()).energy() != 80
                || recordedLightPushUses != 1
                || ally.getDeltaMovement().lengthSqr() > 0.0001D
                || pushed.getDeltaMovement().z <= 0.1D) {
            helper.fail("Light Push did not protect allied NPCs or enforce replay/cooldown state: "
                    + "accepted=" + lightPush + ", replay=" + lightReplay
                    + ", cooldown=" + lightCooldown + ", energy="
                    + force.state(light.getUUID()).energy() + ", progressionUses="
                    + recordedLightPushUses + ", ally="
                    + ally.getDeltaMovement() + ", target=" + pushed.getDeltaMovement()
                    + ", preflight=" + lightPreflight.reason()
                    + ", lineOfSight=" + light.hasLineOfSight(pushed)
                    + ", player=" + light.position() + ", targetPos=" + pushed.position()
                    + ", alive=" + pushed.isAlive() + ", distance=" + light.distanceTo(pushed)
                    + ", look=" + lightLook + ", dot=" + targetDot
                    + ", nearbyLiving=" + nearbyLiving.stream()
                    .map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString())
                    .toList());
            return;
        }
        ally.discard();
        pushed.discard();

        var pulled = spawnEntityAt(
                helper, EntityTypes.ZOMBIE, lightOrigin.offset(0, 0, 5));
        pulled.setNoAi(true);
        pulled.setDeltaMovement(Vec3.ZERO);
        UUID lightPullId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(light, lightPullId, 1)
                || pulled.getDeltaMovement().z >= -0.1D
                || force.state(light.getUUID()).energy() != 55) {
            helper.fail("Light Pull did not move its physical target toward the player: "
                    + pulled.getDeltaMovement() + "/" + force.state(light.getUUID()));
            return;
        }
        pulled.discard();
        light.setDeltaMovement(Vec3.ZERO);
        UUID lightLeapId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(light, lightLeapId, 2)
                || light.getDeltaMovement().y <= 0.5D
                || force.state(light.getUUID()).energy() != 40) {
            helper.fail("Light Leap did not launch the player and conserve Force energy: "
                    + light.getDeltaMovement() + "/" + force.state(light.getUUID()));
            return;
        }
        light.setPos(lightOrigin.getX() + 100.5D, lightOrigin.getY(), lightOrigin.getZ() + 0.5D);

        ServerPlayer dark = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos darkOrigin = lightOrigin;
        dark.setPos(darkOrigin.getX() + 0.5D, darkOrigin.getY(), darkOrigin.getZ() + 0.5D);
        dark.setYRot(0.0F);
        dark.setYHeadRot(0.0F);
        dark.setXRot(0.0F);
        completeForceCampaign(progression, dark, "galacticwars:nightsister",
                "nightsister_acolyte", "coruscant");
        BlockPos nightsisterShrine = darkOrigin.offset(-2, 0, 0);
        helper.getLevel().setBlock(nightsisterShrine,
                ModBlocks.NIGHTSISTER_SPIRIT_ALTAR.get().defaultBlockState(), 3);
        if (!ForceShrineService.open(dark, nightsisterShrine, "nightsister")) {
            helper.fail("Nightsister chapter-one player could not initiate at the physical spirit altar");
            return;
        }
        force.update(dark.getUUID(), state -> ForceProgressionService.gainMastery(
                state, 25, LaunchContentCatalog.data()).state());
        force.update(dark.getUUID(), state -> ForceProgressionService.learnNode(
                state, "hex", LaunchContentCatalog.data()).state());
        force.update(dark.getUUID(), state -> ForceProgressionService.learnNode(
                state, "spirit_snare", LaunchContentCatalog.data()).state());
        force.update(dark.getUUID(), state -> ForceProgressionService.equip(
                state, 2, "spirit_snare", LaunchContentCatalog.data()).state());

        var darkPushed = spawnEntityAt(
                helper, EntityTypes.ZOMBIE, darkOrigin.offset(0, 0, 6));
        darkPushed.setNoAi(true);
        darkPushed.setDeltaMovement(Vec3.ZERO);
        UUID darkPushId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(dark, darkPushId, 0)
                || darkPushed.getDeltaMovement().z <= 0.1D
                || force.state(dark.getUUID()).energy() != 80) {
            helper.fail("Dark Push did not move its physical target: "
                    + darkPushed.getDeltaMovement() + "/" + force.state(dark.getUUID()));
            return;
        }
        darkPushed.discard();
        dark.setDeltaMovement(Vec3.ZERO);
        UUID darkDashId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(dark, darkDashId, 1)
                || dark.getDeltaMovement().z <= 1.0D
                || force.state(dark.getUUID()).energy() != 65) {
            helper.fail("Dark Dash did not propel the player and conserve Force energy: "
                    + dark.getDeltaMovement() + "/" + force.state(dark.getUUID()));
            return;
        }
        dark.setDeltaMovement(Vec3.ZERO);
        var choked = spawnEntityAt(
                helper, EntityTypes.ZOMBIE, darkOrigin.offset(0, 0, 4));
        choked.setNoAi(true);
        UUID spiritSnareId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(dark, spiritSnareId, 2)
                || !choked.hasEffect(MobEffects.SLOWNESS)
                || !choked.hasEffect(MobEffects.WEAKNESS)
                || force.state(dark.getUUID()).energy() != 55) {
            helper.fail("Spirit Snare did not apply its physical effects and cost: effects="
                    + choked.getActiveEffects() + ", state=" + force.state(dark.getUUID()));
            return;
        }
        choked.discard();

        dark.setPos(darkOrigin.getX() + 100.5D, darkOrigin.getY(), darkOrigin.getZ() + 0.5D);
        ServerPlayer sith = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        sith.setPos(darkOrigin.getX() + 0.5D, darkOrigin.getY(), darkOrigin.getZ() + 0.5D);
        sith.setYRot(0.0F);
        sith.setYHeadRot(0.0F);
        sith.setXRot(0.0F);
        completeForceCampaign(progression, sith, "galacticwars:separatist",
                "b1_battle_droid", "geonosis");
        BlockPos sithShrine = darkOrigin.offset(2, 0, 0);
        helper.getLevel().setBlock(sithShrine,
                ModBlocks.SITH_HOLOCRON_PEDESTAL.get().defaultBlockState(), 3);
        if (!ForceShrineService.open(sith, sithShrine, "sith")) {
            helper.fail("Separatist chapter-one player could not initiate at the Sith pedestal");
            return;
        }
        force.update(sith.getUUID(), state -> ForceProgressionService.gainMastery(
                state, 10, LaunchContentCatalog.data()).state());
        force.update(sith.getUUID(), state -> ForceProgressionService.learnNode(
                state, "force_lightning", LaunchContentCatalog.data()).state());
        var shocked = spawnEntityAt(helper, EntityTypes.ZOMBIE, darkOrigin.offset(0, 0, 5));
        shocked.setNoAi(true);
        UUID lightningId = UUID.randomUUID();
        if (!ForceWorldEffectService.activate(sith, lightningId, 2)
                || shocked.getHealth() >= shocked.getMaxHealth()
                || force.state(sith.getUUID()).energy() != 90) {
            helper.fail("Sith Force Lightning did not use the shared damage and energy runtime: "
                    + shocked.getHealth() + "/" + force.state(sith.getUUID()));
            return;
        }

        Tag encoded = ForceSavedData.CODEC.encodeStart(NbtOps.INSTANCE, force).getOrThrow();
        ForceSavedData restored = ForceSavedData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        if (!restored.state(light.getUUID()).path().equals("light")
                || restored.state(light.getUUID()).energy() != 40
                || !restored.state(light.getUUID()).processedActivationIds().contains(lightLeapId)
                || !restored.state(dark.getUUID()).path().equals("dark")
                || !restored.state(dark.getUUID()).traditionId().equals("nightsister")
                || restored.state(dark.getUUID()).energy() != 55
                || !restored.state(dark.getUUID()).processedActivationIds().contains(spiritSnareId)
                || !restored.state(sith.getUUID()).traditionId().equals("sith")
                || restored.state(sith.getUUID()).energy() != 90
                || !restored.state(sith.getUUID()).processedActivationIds().contains(lightningId)) {
            helper.fail("Force traditions, energy, cooldowns, or replay IDs did not survive persistence");
            return;
        }
        helper.succeed();
        });
    }

    private static void allFactionCampaignPaths(GameTestHelper helper) {
        record CampaignPath(
                String faction,
                String firstRecruit,
                String planet,
                String chapterTwoTrade,
                String chapterThreeRecruit,
                boolean chapterThreeTrade
        ) {
        }
        List<CampaignPath> paths = List.of(
                new CampaignPath("galacticwars:republic", "clone_trooper", "kamino", "", "", true),
                new CampaignPath("galacticwars:separatist", "b1_battle_droid", "geonosis", "", "", true),
                new CampaignPath("galacticwars:mandalorian", "mandalorian_warrior", "tatooine",
                        "mandalorian_armorer", "mandalorian_marksman", false),
                new CampaignPath("galacticwars:hutt_cartel", "hutt_enforcer", "tatooine",
                        "hutt_broker", "smuggler", false),
                new CampaignPath("galacticwars:nightsister", "nightsister_acolyte", "coruscant",
                        "", "", true));
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        for (CampaignPath path : paths) {
            ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.FACTION_PLEDGED, path.faction());
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.BUILDING_COMPLETED, "command_center");
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.BUILDING_COMPLETED, "starter_camp");
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.RECRUIT_HIRED, path.firstRecruit());
            for (int delivery = 1; delivery <= 3; delivery++) {
                applyCampaignSetupEvent(progression, player, ProgressionEventType.DELIVERY_COMPLETED,
                        campaignDeliverySubject());
            }
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.BUILDING_COMPLETED, "forward_base");
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.BUILDING_COMPLETED, "supply_depot");
            if (!path.chapterTwoTrade().isEmpty()) {
                applyCampaignSetupEvent(progression, player, ProgressionEventType.TRADE_COMPLETED,
                        path.chapterTwoTrade());
            }
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.PLANET_VISITED, path.planet());
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.VEHICLE_ACQUIRED,
                    path.faction().equals("galacticwars:separatist") ? "stap" : "barc_speeder");
            applyCampaignSetupEvent(
                    progression, player, ProgressionEventType.REGION_CAPTURED,
                    switch (path.planet()) {
                        case "geonosis" -> "geonosis_foundry";
                        case "kamino" -> "kamino_platform";
                        case "coruscant" -> "coruscant_district";
                        default -> "tatooine_spaceport";
                    });
            if (path.chapterThreeTrade()) {
                applyCampaignSetupEvent(progression, player, ProgressionEventType.TRADE_COMPLETED,
                        switch (path.faction()) {
                            case "galacticwars:separatist" -> "separatist_foundry";
                            case "galacticwars:nightsister" -> "nightsister_matron";
                            default -> "republic_quartermaster";
                        });
            }
            if (!path.chapterThreeRecruit().isEmpty()) {
                applyCampaignSetupEvent(progression, player, ProgressionEventType.RECRUIT_HIRED,
                        path.chapterThreeRecruit());
            }
            String factionPath = path.faction().substring(path.faction().indexOf(':') + 1);
            ProgressionState state = progression.state(player.getUUID());
            if (!state.hasSubject(
                    ProgressionEventType.QUEST_ADVANCED, factionPath + "_chapter_3")
                    || !state.hasSubject(
                    ProgressionEventType.CAMPAIGN_COMPLETED, factionPath + "_campaign")
                    || !state.unlocks().contains("campaign_victory")
                    || !state.unlocks().contains("veteran_operations")) {
                helper.fail("Faction campaign path did not reach persistent victory for "
                        + path.faction() + ": " + state);
                return;
            }
        }
        helper.succeed();
    }

    private static void missionFailureRetryPersistence(GameTestHelper helper) {
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        BlockPos hallPos = isolatedCapital(helper, 27);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(player);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData.get(helper.getLevel()).foundKingdom(
                player.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos);
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        applyCampaignSetupEvent(progression, player,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        String missionId = "republic_secure_starter_camp";
        ProgressionState started = progression.state(player.getUUID());
        if (!started.hasSubject(ProgressionEventType.MISSION_STARTED, missionId)
                || !galacticwars.clonewars.progression.MissionRuntimeService.active(
                started, missionId)) {
            helper.fail("Chapter mission did not enter its persisted active state");
            return;
        }

        MissionRuntimeEvents.failActiveMission(player, "player_defeated");
        ProgressionState failed = progression.state(player.getUUID());
        MissionAttemptSavedData.MissionAttempt cooldown = MissionAttemptSavedData
                .get(helper.getLevel()).forPlayer(player.getUUID()).orElse(null);
        if (cooldown == null || !cooldown.phase().equals("cooldown")
                || failed.subjectTotal(ProgressionEventType.MISSION_FAILED, Set.of(missionId)) != 1
                || galacticwars.clonewars.progression.MissionRuntimeService.active(
                failed, missionId)) {
            helper.fail("Mission failure did not persist a bounded retry cooldown");
            return;
        }
        Tag encoded = MissionAttemptSavedData.CODEC.encodeStart(
                NbtOps.INSTANCE, MissionAttemptSavedData.get(helper.getLevel())).getOrThrow();
        MissionAttemptSavedData restored = MissionAttemptSavedData.CODEC.parse(
                NbtOps.INSTANCE, encoded).getOrThrow();
        if (restored.forPlayer(player.getUUID()).filter(attempt ->
                attempt.missionId().equals(missionId)
                        && attempt.retryAt() == cooldown.retryAt()).isEmpty()) {
            helper.fail("Mission retry state did not survive codec reload");
            return;
        }

        helper.onEachTick(() -> {
            ProgressionState current = progression.state(player.getUUID());
            if (current.subjectTotal(ProgressionEventType.MISSION_STARTED, Set.of(missionId)) >= 2) {
                if (!galacticwars.clonewars.progression.MissionRuntimeService.active(
                        current, missionId)) {
                    helper.fail("Mission retry was recorded without reactivating the mission");
                    return;
                }
                helper.succeed();
            }
        });
    }

    private static void completeForceCampaign(
            ProgressionSavedData progression,
            ServerPlayer player,
            String faction,
            String firstRecruit,
            String chapterTwoPlanet
    ) {
        applyCampaignSetupEvent(progression, player, ProgressionEventType.FACTION_PLEDGED, faction);
        applyCampaignSetupEvent(
                progression, player, ProgressionEventType.BUILDING_COMPLETED, "command_center");
        applyCampaignSetupEvent(
                progression, player, ProgressionEventType.BUILDING_COMPLETED, "starter_camp");
        applyCampaignSetupEvent(
                progression, player, ProgressionEventType.RECRUIT_HIRED, firstRecruit);
        ProgressionState state = progression.state(player.getUUID());
        String factionPath = faction.substring(faction.indexOf(':') + 1);
        if (!state.hasSubject(ProgressionEventType.QUEST_ADVANCED, factionPath + "_chapter_1")
                || state.hasSubject(ProgressionEventType.QUEST_ADVANCED, factionPath + "_chapter_2")) {
            throw new IllegalStateException(
                    "Force test campaign did not stop at chapter-one initiation gate: " + state);
        }
    }

    private static void commandCenterSquadOrders(GameTestHelper helper) {
        BlockPos hallPos = helper.absolutePos(new BlockPos(1, 1, 1));
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), isolatedCapital(helper, 14));
        GalacticRecruitEntity commander = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        GalacticRecruitEntity hostile = helper.spawn(
                ModEntityTypes.B1_BATTLE_DROID.get(), new BlockPos(4, 1, 2));
        commander.tame(owner);
        commander.setNoAi(true);
        hostile.setNoAi(true);
        hostile.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(),
                isolatedCapital(helper, 14).offset(32, 0, 0));
        boolean commanderSlotEarned = data.completeBuildProject(
                owner.getUUID(), forwardBaseProject, forwardBase);
        boolean commanderRegistered = data.registerRecruit(owner.getUUID(), commander.getUUID());
        boolean commanderPromoted = commanderRegistered
                && data.promoteCommander(owner.getUUID(), commander.getUUID());
        if (!commanderSlotEarned || !commanderRegistered || !commanderPromoted) {
            helper.fail("Command Center squad-order setup could not earn/register/promote its commander: slot="
                    + commanderSlotEarned + ", registered=" + commanderRegistered
                    + ", promoted=" + commanderPromoted);
            return;
        }
        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var squad = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, helper.getLevel().getGameTime()).orElseThrow();
        boolean[] scenarioRun = {false};
        helper.onEachTick(() -> {
            if (scenarioRun[0]) {
                return;
            }
            if (helper.getLevel().getEntity(commander.getUUID()) != commander
                    || helper.getLevel().getEntity(hostile.getUUID()) != hostile) {
                if (helper.getTick() >= 30L) {
                    scenarioRun[0] = true;
                    helper.fail("Command Center squad-order entities never entered the server index");
                }
                return;
            }
            scenarioRun[0] = true;
            if (!commander.isHostileFactionRecruit(hostile)) {
                helper.fail("Command Center target fixture is not faction-hostile");
                return;
            }
            CommandCenterOperationsMenu menu = (CommandCenterOperationsMenu)
                    new CommandCenterOperationsMenuProvider(hallPos)
                            .createMenu(7, owner.getInventory(), owner);
            if (menu.dashboardState().combatTargets().stream()
                    .noneMatch(target -> target.entityId().equals(hostile.getUUID()))) {
                helper.fail("Command Center dashboard omitted a nearby explicit hostile target");
                return;
            }
            Optional<UUID> selectedSquad = Optional.of(squad.id());
            boolean moveAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.MOVE_SQUAD, selectedSquad, Optional.empty());
            var moveOrder = data.armyGroup(squad.id()).orElseThrow().order();
            boolean holdAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.HOLD_SQUAD, selectedSquad, Optional.empty());
            var holdOrder = data.armyGroup(squad.id()).orElseThrow().order();
            boolean protectAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.PROTECT_SQUAD, selectedSquad, Optional.empty());
            var protectOrder = data.armyGroup(squad.id()).orElseThrow().order();
            boolean attackAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.ATTACK_SQUAD_TARGET, selectedSquad,
                    Optional.of(hostile.getUUID()));
            var attackOrder = data.armyGroup(squad.id()).orElseThrow().order();
            boolean clearAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.CLEAR_SQUAD_TARGET, selectedSquad, Optional.empty());
            var clearOrder = data.armyGroup(squad.id()).orElseThrow().order();
            boolean rallyAccepted = menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.SET_SQUAD_RALLY, selectedSquad, Optional.empty());
            var rally = data.armyGroup(squad.id()).orElseThrow().rallyPoint();
            boolean friendlyTargetRejected = !menu.handleReplayAction(owner, UUID.randomUUID(),
                    CommandCenterOperationsMenu.ATTACK_SQUAD_TARGET, selectedSquad,
                    Optional.of(commander.getUUID()));
            if (!moveAccepted
                    || moveOrder.type() != galacticwars.clonewars.army.ArmyCommandType.MOVE_TO_POSITION
                    || !holdAccepted
                    || holdOrder.type() != galacticwars.clonewars.army.ArmyCommandType.HOLD_POSITION
                    || !protectAccepted
                    || protectOrder.type() != galacticwars.clonewars.army.ArmyCommandType.PROTECT_OWNER
                    || !attackAccepted
                    || attackOrder.targetEntityId().filter(hostile.getUUID()::equals).isEmpty()
                    || !clearAccepted
                    || clearOrder.type() != galacticwars.clonewars.army.ArmyCommandType.CLEAR_TARGET
                    || !rallyAccepted
                    || rally.filter(location ->
                    new Vec3(location.x(), location.y(), location.z())
                            .distanceToSqr(owner.position()) <= 4.0D).isEmpty()
                    || !friendlyTargetRejected) {
                helper.fail("Command Center selected-squad orders failed: move=" + moveAccepted + "/" + moveOrder
                        + ", hold=" + holdAccepted + "/" + holdOrder
                        + ", protect=" + protectAccepted + "/" + protectOrder
                        + ", attack=" + attackAccepted + "/" + attackOrder
                        + ", clear=" + clearAccepted + "/" + clearOrder
                        + ", rally=" + rallyAccepted + "/" + rally
                        + ", friendlyRejected=" + friendlyTargetRejected);
                return;
            }
            UUID replayId = UUID.randomUUID();
            if (!menu.handleReplayAction(owner, replayId,
                    CommandCenterOperationsMenu.FOLLOW_SQUAD, selectedSquad, Optional.empty())
                    || menu.handleReplayAction(owner, replayId,
                    CommandCenterOperationsMenu.HOLD_SQUAD, selectedSquad, Optional.empty())
                    || data.armyGroup(squad.id()).orElseThrow().order().type()
                    != galacticwars.clonewars.army.ArmyCommandType.FOLLOW_OWNER) {
                helper.fail("Command Center squad command replay protection was not idempotent");
                return;
            }
            helper.succeed();
        });
    }

    private static void commandCenterWorkforceControl(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 15);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Command Center workforce setup could not activate its isolated Hall");
            return;
        }
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        GalacticRecruitEntity worker = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 1));
        worker.setNoAi(true);
        boolean[] scenarioRun = {false};
        helper.onEachTick(() -> {
            if (scenarioRun[0]) {
                return;
            }
            if (helper.getLevel().getEntity(worker.getUUID()) != worker) {
                if (helper.getTick() >= 30L) {
                    scenarioRun[0] = true;
                    helper.fail("Command Center workforce recruit never entered the server index");
                }
                return;
            }
            scenarioRun[0] = true;
            owner.setPos(worker.getX(), worker.getY(), worker.getZ());
            boolean hired = worker.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE);
            boolean assigned = hired && worker.handleMenuButton(
                    owner, RecruitCommandMenu.BUTTON_ASSIGN_FARMER);
            owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
            CommandCenterOperationsMenu menu = (CommandCenterOperationsMenu)
                    new CommandCenterOperationsMenuProvider(hallPos)
                            .createMenu(8, owner.getInventory(), owner);
            boolean listed = menu.dashboardState().workers().stream()
                    .anyMatch(summary -> summary.entityId().equals(worker.getUUID())
                            && summary.profession().equals("farmer")
                            && summary.worksite().isPresent());
            Optional<UUID> selectedWorker = Optional.of(worker.getUUID());
            boolean pauseAccepted = menu.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.PAUSE_WORKER,
                    selectedWorker, Optional.empty());
            boolean paused = worker.getRecruitCommand() == RecruitmentAction.HOLD_POSITION
                    && worker.getWorkerStatus().phase() == WorkerPhase.PAUSED
                    && worker.getWorkerStatus().reasonCode().equals("paused_by_command_center");
            boolean recallAccepted = menu.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.RECALL_WORKER,
                    selectedWorker, Optional.empty());
            boolean recalled = worker.getRecruitCommand() == RecruitmentAction.MOVE_TO_POSITION
                    && hallPos.equals(worker.getMoveTarget())
                    && worker.getWorkerStatus().phase() == WorkerPhase.PAUSED
                    && worker.getWorkerStatus().reasonCode().equals("recalled_to_command_center");
            boolean resumeAccepted = menu.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.RESUME_WORKER,
                    selectedWorker, Optional.empty());
            boolean resumed = worker.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE
                    && worker.getWorkerStatus().phase() == WorkerPhase.ACQUIRE_ORDER
                    && worker.getWorkerStatus().reasonCode().equals("return_to_worksite");
            boolean unknownRejected = !menu.handleReplayAction(
                    owner, UUID.randomUUID(), CommandCenterOperationsMenu.PAUSE_WORKER,
                    Optional.of(UUID.randomUUID()), Optional.empty());
            if (!hired || !assigned || !listed
                    || !pauseAccepted || !paused
                    || !recallAccepted || !recalled
                    || !resumeAccepted || !resumed
                    || !unknownRejected) {
                helper.fail("Command Center workforce control failed: hired=" + hired
                        + ", assigned=" + assigned + ", listed=" + listed
                        + ", pause=" + pauseAccepted + "/" + paused
                        + ", recall=" + recallAccepted + "/" + recalled
                        + ", resume=" + resumeAccepted + "/" + resumed
                        + ", unknownRejected=" + unknownRejected
                        + ", command=" + worker.getRecruitCommand()
                        + ", status=" + worker.getWorkerStatus());
                return;
            }
            helper.succeed();
        });
    }

    private static void blueprintProjectorRuntime(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 13);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(),
                helper.absolutePos(new BlockPos(3, 1, 1)));
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Blueprint projector builder could not be hired");
            return;
        }
        KingdomBaseBlueprint blueprint = GameplayDataManager.snapshot()
                .blueprint("galacticwars:barracks").orElseThrow();
        BlockPos origin = hallPos.offset(4, 8, 4);
        for (int placementIndex = 0; placementIndex < blueprint.placements().size(); placementIndex++) {
            var placement = blueprint.rotatedPlacement(placementIndex, 0);
            helper.getLevel().getChunkAt(origin.offset(
                    placement.x(), placement.y(), placement.z()));
        }
        helper.getLevel().setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 3);
        BlockPos loadedBuilderPosition = helper.absolutePos(new BlockPos(3, 1, 1));
        int[] scenarioPhase = {0};
        long[] phaseStartedTick = {helper.getTick()};
        BuildProject[] startedProject = {null};
        UUID[] startedWorkOrder = {null};
        helper.onEachTick(() -> {
            if (scenarioPhase[0] >= 2) {
                return;
            }
            if (helper.getLevel().getEntity(recruit.getUUID()) != recruit) {
                if (helper.getTick() - phaseStartedTick[0] >= 30L) {
                    int stalledPhase = scenarioPhase[0];
                    scenarioPhase[0] = 2;
                    helper.fail("Blueprint projector builder did not enter the server entity index in phase "
                            + stalledPhase);
                }
                return;
            }
            if (scenarioPhase[0] == 1) {
                scenarioPhase[0] = 2;
                verifyBlueprintProjectCancellation(
                        helper, owner, recruit, data, startedProject[0], startedWorkOrder[0]);
                return;
            }
            owner.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
            ItemStack projector = new ItemStack(ModItems.BLUEPRINT_PROJECTOR.get());
            projector.set(ModDataComponents.CONSTRUCTION_PLAN.get(), new ConstructionPlan(
                    blueprint.id(), 0, recruit.getUUID(), kingdom.id()));
            owner.setItemInHand(InteractionHand.MAIN_HAND, projector);
            ItemStack heldProjector = owner.getItemInHand(InteractionHand.MAIN_HAND);
            InteractionResult interaction = heldProjector.getItem().useOn(new UseOnContext(
                    owner,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(origin.below()), Direction.UP, origin.below(), false)));
            if (interaction != InteractionResult.SUCCESS
                    || heldProjector.get(ModDataComponents.CONSTRUCTION_PLAN.get()) != null) {
                var diagnostic = ConstructionProjectService.start(
                        helper.getLevel(), owner,
                        new ConstructionPlan(blueprint.id(), 0, recruit.getUUID(), kingdom.id()),
                        origin);
                helper.fail("Successful in-world projection did not consume its armed plan: result="
                        + interaction + ", heldPlan="
                        + heldProjector.get(ModDataComponents.CONSTRUCTION_PLAN.get())
                        + ", localPlan=" + projector.get(ModDataComponents.CONSTRUCTION_PLAN.get())
                        + ", duty=" + recruit.getRecruitDuty()
                        + ", profession=" + recruit.getWorkerProfession()
                        + ", base=" + recruit.getBaseTarget()
                        + ", diagnostic=" + diagnostic.reason()
                        + ", indexed=" + (helper.getLevel().getEntity(recruit.getUUID()) == recruit)
                        + ", kingdom=" + recruit.getKingdomId());
                return;
            }
            BuildProject project = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                    .buildProjects().stream()
                    .filter(candidate -> candidate.blueprintId().equals(blueprint.id()))
                    .filter(candidate -> candidate.originX() == origin.getX()
                            && candidate.originY() == origin.getY()
                            && candidate.originZ() == origin.getZ())
                    .findFirst().orElse(null);
            if (project == null
                    || recruit.getWorkerProfession().filter(WorkerProfession.BUILDER::equals).isEmpty()
                    || recruit.getRecruitDuty() != RecruitDuty.WORKER
                    || !origin.equals(recruit.getBaseTarget())
                    || !origin.equals(recruit.getWorkTarget())
                    || data.assignedWorksite(owner.getUUID(), recruit.getUUID())
                            .flatMap(WorksiteRecord::sourceProjectId)
                            .filter(project.id()::equals).isEmpty()) {
                helper.fail("Projection did not create a persisted project-specific builder assignment");
                return;
            }
            putContainerItem(hall, new ItemStack(Items.OAK_PLANKS, 64));
            putContainerItem(hall, new ItemStack(Items.OAK_LOG, 64));
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            UUID workOrderId = (UUID) getRecruitField(recruit, "workOrderId");
            if (workOrderId == null || !recruit.getWorkerStatus().reasonCode()
                    .equals("withdraw_build_material")) {
                helper.fail("Projected build did not enter the persisted material-hauling loop");
                return;
            }
            interactWorkerAt(recruit, hallPos, "withdraw_build_material");
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            var firstPlacement = blueprint.rotatedPlacement(0, 0);
            BlockPos firstPosition = origin.offset(
                    firstPlacement.x(), firstPlacement.y(), firstPlacement.z());
            interactWorkerAt(recruit, firstPosition, "build_place");
            BuildProject progressed = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                    .buildProjects().stream().filter(candidate -> candidate.id().equals(project.id()))
                    .findFirst().orElseThrow();
            if (!helper.getLevel().getBlockState(firstPosition)
                    .is(BuiltInRegistries.BLOCK.getValue(Identifier.parse(firstPlacement.blockId())))
                    || !progressed.completedPlacements().contains(0)
                    || data.workOrder(owner.getUUID(), workOrderId).isEmpty()) {
                helper.fail("Projected builder did not place and persist its first blueprint block");
                return;
            }
            startedProject[0] = project;
            startedWorkOrder[0] = workOrderId;
            scenarioPhase[0] = 1;
            phaseStartedTick[0] = helper.getTick();
            recruit.teleportTo(
                    loadedBuilderPosition.getX() + 0.5D,
                    loadedBuilderPosition.getY(),
                    loadedBuilderPosition.getZ() + 0.5D);
            recruit.getNavigation().stop();
        });
    }

    private static void commandMarkerRuntime(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 16);
        for (int x = -1; x <= 7; x++) {
            for (int z = -1; z <= 5; z++) {
                BlockPos floor = hallPos.offset(x, -1, z);
                ChunkPos chunk = new ChunkPos(floor.getX() >> 4, floor.getZ() >> 4);
                helper.getLevel().getChunkSource().updateChunkForced(chunk, true);
                helper.getLevel().getChunk(chunk.x(), chunk.z());
                helper.getLevel().setBlockAndUpdate(
                        floor, Blocks.STONE.defaultBlockState());
            }
        }
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Command marker scenario could not activate its isolated kingdom");
            return;
        }
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        progression.apply(new ProgressionEvent(
                UUID.randomUUID(), owner.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1));
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);

        BlockPos recruitSpawn = helper.absolutePos(new BlockPos(2, 1, 1));
        BlockPos hostileSpawn = helper.absolutePos(new BlockPos(5, 1, 1));
        helper.getLevel().setBlockAndUpdate(recruitSpawn.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(hostileSpawn.below(), Blocks.STONE.defaultBlockState());
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitSpawn);
        GalacticRecruitEntity hostile = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), hostileSpawn);
        hostile.setNoAi(true);
        hostile.initializeNaturalFactionNpc(UUID.randomUUID(), NpcServiceBranch.MILITARY);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Command marker recruit could not be hired");
            return;
        }
        progression.apply(new ProgressionEvent(
                UUID.randomUUID(), owner.getUUID(), ProgressionEventType.BUILDING_COMPLETED,
                "starter_camp", 1));

        boolean[] scenarioRun = {false};
        long indexWaitStartedTick = helper.getTick();
        helper.onEachTick(() -> {
            if (scenarioRun[0]) {
                return;
            }
            boolean recruitIndexed = helper.getLevel().getEntity(recruit.getUUID()) == recruit;
            boolean hostileIndexed = helper.getLevel().getEntity(hostile.getUUID()) == hostile;
            if (!recruitIndexed || !hostileIndexed) {
                if (helper.getTick() - indexWaitStartedTick >= 120L) {
                    scenarioRun[0] = true;
                    helper.fail("Command marker entities never entered the server entity index: recruit="
                            + recruitIndexed + ", hostile=" + hostileIndexed);
                }
                return;
            }
            scenarioRun[0] = true;
            verifyCommandMarkerActions(
                    helper, hallPos, hall, owner, kingdom, recruit, hostile, progression);
        });
    }

    private static void verifyCommandMarkerActions(
            GameTestHelper helper,
            BlockPos hallPos,
            CommandCenterBlockEntity hall,
            ServerPlayer owner,
            KingdomRecord kingdom,
            GalacticRecruitEntity recruit,
            GalacticRecruitEntity hostile,
            ProgressionSavedData progression
    ) {
        Vec3 managedRecruitPosition = recruit.position();
        ItemStack marker = new ItemStack(ModItems.COMMAND_MARKER.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, marker);
        ItemStack heldMarker = owner.getItemInHand(InteractionHand.MAIN_HAND);
        owner.setShiftKeyDown(true);
        InteractionResult entitySelection = heldMarker.interactLivingEntity(
                owner, hostile, InteractionHand.MAIN_HAND);
        owner.setShiftKeyDown(false);
        CommandTargetSelection selectedEntity = heldMarker.get(ModDataComponents.COMMAND_TARGET.get());
        Optional<net.minecraft.world.entity.LivingEntity> resolvedEntity =
                CommandTargetSelection.entityFromInventory(owner);
        boolean attackAccepted = recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ATTACK);
        boolean attackTargetAssigned = recruit.getTarget() == hostile;

        BlockPos destination = hallPos.offset(4, 0, 3);
        helper.getLevel().setBlock(destination, Blocks.CHEST.defaultBlockState(), 3);
        InteractionResult blockSelection = heldMarker.getItem().useOn(new UseOnContext(
                owner,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(destination), Direction.UP, destination, false)));
        CommandTargetSelection selectedBlock = heldMarker.get(ModDataComponents.COMMAND_TARGET.get());
        boolean courierAssigned = recruit.handleMenuButton(
                owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER);
        boolean worksiteAccepted = courierAssigned && recruit.handleMenuButton(
                owner, RecruitCommandMenu.BUTTON_SET_WORKSITE);

        heldMarker.getItem().useOn(new UseOnContext(
                owner,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(hallPos), Direction.UP, hallPos, false)));
        boolean storageAccepted = recruit.handleMenuButton(
                owner, RecruitCommandMenu.BUTTON_SET_STORAGE);
        invokeWorkerAuthorityReconciliation(recruit, helper.getLevel());
        boolean courierRouteReconciled = hallPos.equals(recruit.getStorageTarget())
                && destination.equals(recruit.getWorkTarget());
        putContainerItem(hall, new ItemStack(ModItems.DURACRETE.get(), 3));
        UUID courierOrder = acquirePersistedOrder(recruit);
        interactWorkerAt(recruit, hallPos, "courier_withdraw");
        setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
        recruit.tickWorkerController();
        depositWorkerAt(recruit, destination);
        boolean courierCompleted = KingdomSavedData.get(helper.getLevel())
                .workOrder(owner.getUUID(), courierOrder)
                .filter(order -> order.state() == WorkOrderState.COMPLETED)
                .isPresent();
        boolean deliveryRecorded = progression.state(owner.getUUID())
                .total(ProgressionEventType.DELIVERY_COMPLETED) == 1;
        int deliveredDuracrete = helper.getLevel().getBlockEntity(destination) instanceof Container destinationStorage
                ? countContainerItem(destinationStorage, ModItems.DURACRETE.get())
                : 0;
        recruit.setPos(managedRecruitPosition);
        recruit.setDeltaMovement(Vec3.ZERO);
        recruit.getNavigation().stop();
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        boolean projectorPrepared = recruit.handleMenuButton(
                owner, RecruitCommandMenu.BUTTON_BUILD_STARTER_KEEP);
        ConstructionPlan projectorPlan = null;
        for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++) {
            ItemStack stack = owner.getInventory().getItem(slot);
            if (stack.is(ModItems.BLUEPRINT_PROJECTOR.get())) {
                projectorPlan = stack.get(ModDataComponents.CONSTRUCTION_PLAN.get());
                break;
            }
        }

        if (entitySelection != InteractionResult.SUCCESS
                || selectedEntity == null
                || selectedEntity.entityId().filter(hostile.getUUID()::equals).isEmpty()
                || !attackAccepted
                || !attackTargetAssigned
                || blockSelection != InteractionResult.SUCCESS
                || selectedBlock == null
                || selectedBlock.entityId().isPresent()
                || !selectedBlock.blockPos().equals(destination)
                || !courierAssigned
                || !worksiteAccepted
                || !destination.equals(recruit.getWorkTarget())
                || !storageAccepted
                || !hallPos.equals(recruit.getStorageTarget())
                || !courierRouteReconciled
                || !courierCompleted
                || !deliveryRecorded
                || deliveredDuracrete != 3
                || !projectorPrepared
                || projectorPlan == null
                || !projectorPlan.builderId().equals(recruit.getUUID())
                || !projectorPlan.kingdomId().equals(kingdom.id())) {
            helper.fail("Command marker did not bridge physical selection into recruit controls: entity="
                    + entitySelection + "/" + selectedEntity
                    + ", attack=" + attackAccepted + "/" + attackTargetAssigned
                    + ", resolved=" + resolvedEntity
                    + ", factions=" + recruit.getRecruitFactionId() + "/" + hostile.getRecruitFactionId()
                    + ", relation=" + recruit.factionRelationTo(hostile)
                    + ", hostile=" + recruit.isHostileFactionRecruit(hostile)
                    + ", duty=" + hostile.getRecruitDuty()
                    + ", block=" + blockSelection + "/" + selectedBlock
                    + ", courier=" + courierAssigned
                    + ", worksite=" + worksiteAccepted + "/" + recruit.getWorkTarget()
                    + ", storage=" + storageAccepted + "/" + recruit.getStorageTarget()
                    + ", reconciled=" + courierRouteReconciled
                    + ", delivery=" + courierCompleted + "/" + deliveryRecorded
                    + "/" + deliveredDuracrete
                    + ", courierStatus=" + recruit.getWorkerStatus()
                    + ", carried=" + workerInventoryCount(recruit)
                    + ", shouldRun=" + recruit.shouldRunWorkerCycle()
                    + ", courierOrder=" + KingdomSavedData.get(helper.getLevel())
                    .workOrder(owner.getUUID(), courierOrder)
                    + ", assigned=" + KingdomSavedData.get(helper.getLevel())
                    .assignedWorksite(owner.getUUID(), recruit.getUUID())
                    + ", projector=" + projectorPrepared + "/" + projectorPlan);
            return;
        }
        if (!completeForwardBaseFromPreparedProjector(
                helper, hallPos, hall, owner, recruit)) {
            return;
        }
        applyCampaignSetupEvent(progression, owner,
                ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                "republic_secure_starter_camp");
        ProgressionState frontierState = progression.state(owner.getUUID());
        boolean chapterOneComplete = frontierState.hasSubject(
                ProgressionEventType.QUEST_ADVANCED, "republic_chapter_1");
        boolean forwardBaseRecorded = frontierState.hasSubjectPath(
                ProgressionEventType.BUILDING_COMPLETED, "forward_base");
        boolean planetTravelUnlocked = frontierState.unlocks().contains("planet_travel");
        boolean chapterTwoWaitingForTravel = !frontierState.hasSubject(
                ProgressionEventType.QUEST_ADVANCED, "republic_chapter_2");
        if (!chapterOneComplete
                || !forwardBaseRecorded
                || !planetTravelUnlocked
                || !chapterTwoWaitingForTravel) {
            helper.fail("Chapter-two frontier journey did not unlock travel at the correct boundary: chapterOne="
                    + chapterOneComplete + ", delivery=" + deliveryRecorded
                    + ", forwardBase=" + forwardBaseRecorded
                    + ", travel=" + planetTravelUnlocked
                    + ", waitingForPlanet=" + chapterTwoWaitingForTravel
                    + ", unlocks=" + frontierState.unlocks());
            return;
        }
        helper.succeed();
    }

    private static void vehicleEmbodiedRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.SURVIVAL, -4, 36, -4, 50);
        ServerPlayer owner = area.player();
        ServerPlayer intruder = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        intruder.setPos(
                area.at(1, 1, 1).getX() + 0.5D,
                area.at(1, 1, 1).getY(),
                area.at(1, 1, 1).getZ() + 0.5D);
        List<GalacticVehicleEntity> vehicles = new java.util.ArrayList<>();
        int lane = 0;
        for (var holder : ModEntityTypes.vehicles()) {
            GalacticVehicleEntity vehicle = spawnVehicleAt(
                    helper, holder.get(), area.at(2 + lane * 7, 1, 4));
            vehicle.deploy(owner.getUUID(), "galacticwars:republic");
            vehicles.add(vehicle);
            lane++;
        }
        VehicleEmbodiedTestScenario scenario = new VehicleEmbodiedTestScenario(
                helper, owner, intruder, vehicles);
        helper.onEachTick(scenario::tick);
    }

    private static void chapterThreeCampaignRuntime(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper, GameType.SURVIVAL, -4, 24, -4, 24,
                isolatedCapital(helper, 17));
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(1, 1, 1);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData kingdoms = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = kingdoms.foundKingdom(
                owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos);
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.BUILDING_COMPLETED, "command_center");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.BUILDING_COMPLETED, "starter_camp");
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.RECRUIT_HIRED, "clone_trooper");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.DELIVERY_COMPLETED, campaignDeliverySubject());
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.BUILDING_COMPLETED, "forward_base");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.PLANET_VISITED, "kamino");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.BUILDING_COMPLETED, "supply_depot");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.FORCE_ABILITY_USED, "light_push");
        applyCampaignSetupEvent(
                progression, owner, ProgressionEventType.FORCE_ABILITY_USED, "light_pull");
        ProgressionState chapterTwo = progression.state(owner.getUUID());
        if (!chapterTwo.hasSubject(ProgressionEventType.QUEST_ADVANCED, "republic_chapter_2")
                || !chapterTwo.unlocks().contains("vehicle_crafting")) {
            helper.fail("Chapter-three setup did not cross the real Chapter 2 and Supply Depot gates");
            return;
        }

        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        putContainerItem(hall, new ItemStack(ModItems.REPUBLIC_PLASTOID_INGOT.get(), 8));
        putContainerItem(hall, new ItemStack(ModItems.ENERGY_CELL.get(), 4));
        putContainerItem(hall, new ItemStack(ModItems.DURACRETE.get(), 16));
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        CommandCenterOperationsMenu fabricationMenu = (CommandCenterOperationsMenu)
                new CommandCenterOperationsMenuProvider(hallPos)
                        .createMenu(20, owner.getInventory(), owner);
        int barcFabricationIndex = -1;
        for (int index = 0;
                index < fabricationMenu.dashboardState().vehicleFabrication().size();
                index++) {
            if (fabricationMenu.dashboardState().vehicleFabrication().get(index)
                    .vehicleId().equals("barc_speeder")) {
                barcFabricationIndex = index;
                break;
            }
        }
        if (barcFabricationIndex < 0) {
            helper.fail("Command Center did not expose the BARC fabrication option");
            return;
        }
        int barcFabricationButton = CommandCenterOperationsMenu.FABRICATE_FIRST
                + barcFabricationIndex;
        UUID fabricationId = UUID.randomUUID();
        boolean fabricated = fabricationMenu.handleReplayAction(
                owner, fabricationId, barcFabricationButton);
        boolean fabricationReplayRejected = !fabricationMenu.handleReplayAction(
                owner, fabricationId, barcFabricationButton);
        int kitSlot = findPlayerItemSlot(owner, ModItems.BARC_SPEEDER_DEPLOYMENT_KIT.get());
        if (!fabricated || !fabricationReplayRejected || kitSlot < 0
                || CreditTransactionService.containerBalance(hall) != 0
                || countContainerItem(hall, ModItems.REPUBLIC_PLASTOID_INGOT.get()) != 0
                || countContainerItem(hall, ModItems.ENERGY_CELL.get()) != 0
                || countContainerItem(hall, ModItems.DURACRETE.get()) != 0) {
            helper.fail("Command Center fabrication did not atomically consume the BARC recipe: accepted="
                    + fabricated + ", replay=" + fabricationReplayRejected + ", kitSlot=" + kitSlot
                    + ", credits=" + CreditTransactionService.containerBalance(hall));
            return;
        }
        owner.getInventory().setSelectedSlot(kitSlot);
        BlockPos deploymentFloor = area.at(5, 0, 4);
        InteractionResult deployed = owner.getMainHandItem().getItem().useOn(new UseOnContext(
                owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(deploymentFloor), Direction.UP, deploymentFloor, false)));
        if (deployed != InteractionResult.SUCCESS || !owner.getMainHandItem().isEmpty()
                || !progression.state(owner.getUUID()).hasSubject(
                ProgressionEventType.VEHICLE_ACQUIRED, "barc_speeder")) {
            helper.fail("Fabricated BARC kit did not deploy into owned in-world gameplay: result="
                    + deployed + ", held=" + owner.getMainHandItem());
            return;
        }

        GalacticRecruitEntity merchant = spawnRecruitAt(
                helper, ModEntityTypes.REPUBLIC_CIVILIAN.get(), area.at(3, 1, 8));
        merchant.setNoAi(true);
        merchant.initializeFromSpawnEgg();
        merchant.tame(owner);
        merchant.setWorkerProfession(WorkerProfession.MERCHANT);
        configurePhysicalMarket(helper, owner, merchant, hall, hallPos, 16);
        BlockPos beacon = area.at(10, 1, 10);
        helper.getLevel().setBlockAndUpdate(beacon, ModBlocks.CONTROL_BEACON.get().defaultBlockState());
        GalacticRecruitEntity escort = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), beacon.offset(1, 0, 0));
        escort.setNoAi(true);
        escort.initializeFromSpawnEgg();
        escort.tame(owner);
        var region = LaunchContentCatalog.data().conquestRegions().get("kamino_platform");
        ConquestSavedData.get(helper.getLevel()).put(new ConquestControlState(
                region.id(), helper.getLevel().dimension().identifier().toString(),
                beacon.getX(), beacon.getY(), beacon.getZ(), "galacticwars:hutt_cartel",
                "", "", 0, 0L));
        applyCampaignSetupEvent(progression, owner,
                ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                "republic_assault_kamino_platform");
        helper.setTime(101L);
        ChapterThreeCampaignTestScenario scenario = new ChapterThreeCampaignTestScenario(
                helper, area, owner, hallPos, hall, kingdom, merchant, escort,
                deploymentFloor, beacon, region);
        helper.onEachTick(scenario::tick);
    }

    private static void applyCampaignSetupEvent(
            ProgressionSavedData progression,
            ServerPlayer player,
            ProgressionEventType type,
            String subject
    ) {
        ProgressionDecision decision = progression.apply(new ProgressionEvent(
                UUID.randomUUID(), player.getUUID(), type, subject, 1));
        if (!decision.accepted()) {
            throw new IllegalStateException(
                    "Campaign setup rejected " + type + "/" + subject + ": " + decision.reason());
        }
        if (type == ProgressionEventType.MISSION_OBJECTIVE_COMPLETED) {
            return;
        }
        ProgressionState state = progression.state(player.getUUID());
        for (LaunchContentDefinitions.MissionDefinition mission
                : LaunchContentCatalog.data().missions().values()) {
            if (!galacticwars.clonewars.progression.MissionRuntimeService.active(
                    state, mission.id())
                    || !galacticwars.clonewars.progression.MissionRuntimeService
                    .requirementsComplete(state, mission)) {
                continue;
            }
            ProgressionDecision objective = progression.apply(new ProgressionEvent(
                    galacticwars.clonewars.progression.MissionRuntimeService
                            .deterministicLifecycleEventId(
                                    player.getUUID(),
                                    ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                                    mission.id(),
                                    state.subjectTotal(
                                            ProgressionEventType.MISSION_STARTED,
                                            Set.of(mission.id()))),
                    player.getUUID(), ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
                    mission.id(), 1));
            if (!objective.accepted()) {
                throw new IllegalStateException(
                        "Campaign mission objective rejected " + mission.id()
                                + ": " + objective.reason());
            }
            state = progression.state(player.getUUID());
        }
    }

    private static String campaignDeliverySubject() {
        return "courier/" + UUID.randomUUID();
    }

    private static int findPlayerItemSlot(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean completeForwardBaseFromPreparedProjector(
            GameTestHelper helper,
            BlockPos hallPos,
            CommandCenterBlockEntity hall,
            ServerPlayer owner,
            GalacticRecruitEntity recruit
    ) {
        KingdomBaseBlueprint blueprint = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BlockPos origin = hallPos.offset(0, 8, 0);
        for (int placementIndex = 0; placementIndex < blueprint.placements().size(); placementIndex++) {
            var placement = blueprint.rotatedPlacement(placementIndex, 0);
            BlockPos placementPos = origin.offset(
                    placement.x(), placement.y(), placement.z());
            helper.getLevel().getChunkAt(placementPos);
            helper.getLevel().setBlock(placementPos, Blocks.AIR.defaultBlockState(), 3);
        }
        helper.getLevel().setBlock(origin.below(), Blocks.STONE.defaultBlockState(), 3);
        ItemStack projector = ItemStack.EMPTY;
        for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = owner.getInventory().getItem(slot);
            ConstructionPlan plan = candidate.get(ModDataComponents.CONSTRUCTION_PLAN.get());
            if (candidate.is(ModItems.BLUEPRINT_PROJECTOR.get())
                    && plan != null
                    && plan.blueprintId().equals(blueprint.id())) {
                projector = candidate;
                break;
            }
        }
        if (projector.isEmpty()) {
            helper.fail("Prepared Forward Base projector was not present in the player's inventory");
            return false;
        }
        owner.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        owner.setItemInHand(InteractionHand.MAIN_HAND, projector);
        ItemStack heldProjector = owner.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult projection = heldProjector.getItem().useOn(new UseOnContext(
                owner,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(origin.below()), Direction.UP, origin.below(), false)));
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BuildProject project = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                .buildProjects().stream()
                .filter(candidate -> candidate.blueprintId().equals(blueprint.id()))
                .filter(candidate -> candidate.originX() == origin.getX()
                        && candidate.originY() == origin.getY()
                        && candidate.originZ() == origin.getZ())
                .findFirst().orElse(null);
        if (projection != InteractionResult.SUCCESS
                || project == null
                || recruit.getWorkerProfession().filter(WorkerProfession.BUILDER::equals).isEmpty()
                || !origin.equals(recruit.getBaseTarget())) {
            var diagnostic = ConstructionProjectService.start(
                    helper.getLevel(), owner,
                    new ConstructionPlan(blueprint.id(), 0, recruit.getUUID(),
                            data.kingdomForOwner(owner.getUUID()).orElseThrow().id()),
                    origin);
            helper.fail("Forward Base projector did not create and assign an embodied build: projection="
                    + projection + ", project=" + project
                    + ", profession=" + recruit.getWorkerProfession()
                    + ", base=" + recruit.getBaseTarget()
                    + ", work=" + recruit.getWorkTarget()
                    + ", diagnostic=" + diagnostic.reason()
                    + ", indexed=" + (helper.getLevel().getEntity(recruit.getUUID()) == recruit)
                    + ", kingdom=" + recruit.getKingdomId());
            return false;
        }

        putContainerItem(hall, new ItemStack(ModItems.DURACRETE.get(), 64));
        putContainerItem(hall, new ItemStack(ModItems.NIGHTSISTER_WEAVE_LOG.get(), 64));
        putContainerItem(hall, new ItemStack(Items.OAK_PLANKS, 64));
        UUID workOrderId = null;
        for (int placementIndex = 0; placementIndex < blueprint.placements().size(); placementIndex++) {
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            if (workOrderId == null) {
                workOrderId = (UUID) getRecruitField(recruit, "workOrderId");
            }
            if (!recruit.getWorkerStatus().reasonCode().equals("withdraw_build_material")) {
                helper.fail("Forward Base builder did not request material for placement "
                        + placementIndex + ": status=" + recruit.getWorkerStatus()
                        + ", carried=" + workerInventoryCount(recruit)
                        + ", storage=" + recruit.getStorageTarget());
                return false;
            }
            interactWorkerAt(recruit, hallPos, "withdraw_build_material");
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            BuildProject current = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                    .buildProjects().stream()
                    .filter(candidate -> candidate.id().equals(project.id()))
                    .findFirst().orElseThrow();
            var placement = blueprint.rotatedPlacement(placementIndex, current.rotationSteps());
            BlockPos placementPos = origin.offset(
                    placement.x(), placement.y(), placement.z());
            interactWorkerAt(recruit, placementPos, "build_place");
        }
        setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
        recruit.tickWorkerController();
        boolean orderComplete = workOrderId != null && data.workOrder(owner.getUUID(), workOrderId)
                .filter(order -> order.state() == WorkOrderState.COMPLETED).isPresent();
        boolean projectComplete = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                .buildProjects().stream()
                .filter(candidate -> candidate.id().equals(project.id()))
                .anyMatch(candidate -> candidate.state()
                        == galacticwars.clonewars.kingdom.BuildProjectState.COMPLETED);
        if (!orderComplete || !projectComplete || recruit.getBaseTarget() != null) {
            helper.fail("Forward Base builder did not complete its physical project: order="
                    + orderComplete + ", project=" + projectComplete
                    + ", status=" + recruit.getWorkerStatus()
                    + ", base=" + recruit.getBaseTarget());
            return false;
        }
        return true;
    }

    private static void verifyBlueprintProjectCancellation(
            GameTestHelper helper,
            ServerPlayer owner,
            GalacticRecruitEntity recruit,
            KingdomSavedData data,
            BuildProject project,
            UUID workOrderId
    ) {
        var cancelled = ConstructionProjectService.cancel(
                helper.getLevel(), owner, project.id());
        BuildProject cancelledProject = data.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlement().buildProjects().stream()
                .filter(candidate -> candidate.id().equals(project.id())).findFirst().orElseThrow();
        if (!cancelled.accepted()
                || cancelledProject.state()
                != galacticwars.clonewars.kingdom.BuildProjectState.CANCELLED
                || data.workOrder(owner.getUUID(), workOrderId)
                        .filter(order -> order.state() == WorkOrderState.CANCELLED).isEmpty()
                || data.assignedWorksite(owner.getUUID(), recruit.getUUID()).isPresent()
                || recruit.getBaseTarget() != null
                || recruit.getWorkTarget() != null) {
            helper.fail("Construction cancellation left stale state: result=" + cancelled
                    + ", project=" + cancelledProject.state()
                    + ", order=" + data.workOrder(owner.getUUID(), workOrderId)
                    + ", worksite=" + data.assignedWorksite(owner.getUUID(), recruit.getUUID())
                    + ", base=" + recruit.getBaseTarget()
                    + ", work=" + recruit.getWorkTarget());
            return;
        }
        helper.succeed();
    }

    private static void kingdomGovernancePersistence(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer builder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer enemyOwner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        // SavedData is shared by the whole GameTest world. Keep governance-only
        // claims away from the physical test structures and their Command Centers.
        BlockPos capitalPos = helper.absolutePos(new BlockPos(1, 1, 1)).offset(0, 0, 10_000);
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), capitalPos);
        if (data.establishTreaty(owner.getUUID(), kingdom.id(),
                helper.getLevel().getGameTime(), 200L, 0L)) {
            helper.fail("Kingdom established a treaty with itself");
        }
        if (!data.addMember(owner.getUUID(), builder.getUUID(), KingdomMemberRole.BUILDER,
                "galacticwars:republic")
                || !data.allows(builder.getUUID(), KingdomPermission.BUILD)
                || data.allows(builder.getUUID(), KingdomPermission.MANAGE_CLAIMS)) {
            helper.fail("Kingdom membership roles did not enforce their permissions");
        }

        KingdomClaim capitalClaim = data.kingdom(kingdom.id()).orElseThrow().claims().getFirst();
        net.minecraft.world.level.ChunkPos expansion = new net.minecraft.world.level.ChunkPos(
                capitalClaim.center().x() + 2, capitalClaim.center().z());
        if (data.expandClaim(builder.getUUID(), capitalClaim.id(), capitalClaim.dimensionId(), expansion)
                || !data.expandClaim(owner.getUUID(), capitalClaim.id(), capitalClaim.dimensionId(), expansion)) {
            helper.fail("Claim expansion ignored role or contiguity authority");
        }

        BlockPos enemyCapital = capitalPos.offset(512, 0, 0);
        KingdomRecord enemy = data.foundKingdom(
                enemyOwner.getUUID(), "galacticwars:separatist",
                helper.getLevel().dimension().identifier().toString(), enemyCapital);
        long diplomacyTime = helper.getLevel().getGameTime();
        FactionId republicFaction = FactionId.of("galacticwars:republic");
        FactionId separatistFaction = FactionId.of("galacticwars:separatist");
        if (KingdomFactionRelations.resolve(
                        GameplayDataManager.snapshot().factions(), data,
                        kingdom.id(), republicFaction, enemy.id(), separatistFaction, diplomacyTime)
                        != FactionRelation.ENEMY
                || !data.setRelation(owner.getUUID(), enemy.id(), KingdomRelation.NEUTRAL, diplomacyTime, 0L)
                || !data.establishTreaty(owner.getUUID(), enemy.id(), diplomacyTime, 100L, 0L)
                || KingdomFactionRelations.resolve(
                        GameplayDataManager.snapshot().factions(), data,
                        kingdom.id(), republicFaction, enemy.id(), separatistFaction, diplomacyTime + 99L)
                        != FactionRelation.ALLY
                || KingdomFactionRelations.resolve(
                        GameplayDataManager.snapshot().factions(), data,
                        kingdom.id(), republicFaction, enemy.id(), separatistFaction, diplomacyTime + 100L)
                        != FactionRelation.NEUTRAL
                || !data.setRelation(
                        owner.getUUID(), enemy.id(), KingdomRelation.ENEMY, diplomacyTime + 100L, 200L)
                || !data.setEmbargo(owner.getUUID(), enemy.id(), true)
                || !data.relation(kingdom.id(), enemy.id()).embargo()) {
            helper.fail("Kingdom diplomacy did not override faction defaults or expire treaties safely");
            return;
        }
        SettlementRecord enemyOutpost = SettlementRecord.create(
                helper.getLevel().dimension().identifier().toString(),
                enemyCapital.getX() + 256, enemyCapital.getY(), enemyCapital.getZ());
        KingdomClaim outpostClaim = data.addOutpost(enemyOwner.getUUID(), enemyOutpost).orElseThrow();
        KingdomSiege siege = data.startSiege(
                owner.getUUID(), outpostClaim.id(), true, true, 10,
                helper.getLevel().getGameTime()).orElseThrow();
        KingdomSiege captured = data.progressSiege(
                siege.id(), 12, 2, helper.getLevel().getGameTime() + 20,
                List.of(owner.getUUID()), List.of(enemyOwner.getUUID())).orElseThrow();
        if (captured.state() != SiegeState.CAPTURED
                || data.kingdom(kingdom.id()).orElseThrow().claims().stream()
                        .noneMatch(claim -> claim.id().equals(outpostClaim.id()))) {
            helper.fail("Outpost siege did not transfer claim authority atomically");
        }

        Tag encoded = KingdomSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        KingdomSavedData restored = KingdomSavedData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        if (restored.schemaVersion() != KingdomSavedData.CURRENT_SCHEMA_VERSION
                || restored.kingdomForPlayer(builder.getUUID()).isEmpty()
                || !restored.allows(builder.getUUID(), KingdomPermission.BUILD)
                || restored.sieges().stream().noneMatch(stored -> stored.id().equals(siege.id())
                        && stored.state() == SiegeState.CAPTURED)) {
            helper.fail("Kingdom governance state did not survive its SavedData codec round trip");
        }
        helper.succeed();
    }

    private static void kingdomMultiplayerRuntime(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer officer = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer builder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer member = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer intruder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        BlockPos capitalPos = helper.absolutePos(new BlockPos(1, 1, 1)).offset(0, 0, 20_000);
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), capitalPos);
        if (!data.addMember(owner.getUUID(), officer.getUUID(), KingdomMemberRole.OFFICER,
                "galacticwars:republic")
                || !data.addMember(owner.getUUID(), builder.getUUID(), KingdomMemberRole.BUILDER, "")
                || !data.addMember(owner.getUUID(), member.getUUID(), KingdomMemberRole.MEMBER, "")) {
            helper.fail("Multiplayer kingdom setup rejected compatible member roles");
            return;
        }

        BlockPos deniedBlock = capitalPos.offset(1, 0, 1);
        BlockPos allowedBlock = capitalPos.offset(2, 0, 1);
        helper.getLevel().setBlock(deniedBlock, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(allowedBlock, Blocks.STONE.defaultBlockState(), 3);
        intruder.setPos(deniedBlock.getX() + 0.5, deniedBlock.getY(), deniedBlock.getZ() + 0.5);
        member.setPos(deniedBlock.getX() + 0.5, deniedBlock.getY(), deniedBlock.getZ() + 0.5);
        builder.setPos(allowedBlock.getX() + 0.5, allowedBlock.getY(), allowedBlock.getZ() + 0.5);
        intruder.gameMode.destroyBlock(deniedBlock);
        member.gameMode.destroyBlock(deniedBlock);
        builder.gameMode.destroyBlock(allowedBlock);
        if (!helper.getLevel().getBlockState(deniedBlock).is(Blocks.STONE)
                || !helper.getLevel().getBlockState(allowedBlock).isAir()) {
            helper.fail("Runtime claim protection did not deny outsiders/members and allow builders");
            return;
        }

        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject firstBase = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capitalPos.offset(32, 0, 0));
        BuildProject secondBase = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                helper.getLevel().dimension().identifier().toString(), capitalPos.offset(64, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), firstBase, forwardBase)
                || !data.completeBuildProject(owner.getUUID(), secondBase, forwardBase)) {
            helper.fail("Multiple squad setup could not earn two commander slots");
            return;
        }
        GalacticRecruitEntity firstCommanderEntity = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        GalacticRecruitEntity secondCommanderEntity = helper.spawn(
                ModEntityTypes.ARC_TROOPER.get(), new BlockPos(3, 1, 2));
        GalacticRecruitEntity soldierEntity = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 2));
        firstCommanderEntity.tame(owner);
        secondCommanderEntity.tame(owner);
        soldierEntity.tame(owner);
        firstCommanderEntity.setNoAi(true);
        secondCommanderEntity.setNoAi(true);
        soldierEntity.setNoAi(true);
        UUID firstCommander = firstCommanderEntity.getUUID();
        UUID secondCommander = secondCommanderEntity.getUUID();
        UUID soldier = soldierEntity.getUUID();
        if (!data.registerRecruit(owner.getUUID(), firstCommander)
                || !data.registerRecruit(owner.getUUID(), secondCommander)
                || !data.registerRecruit(owner.getUUID(), soldier)
                || !data.promoteCommander(officer.getUUID(), firstCommander)) {
            helper.fail("Officer could not register and promote the first military squad");
            return;
        }
        ArmyLocation anchor = new ArmyLocation(
                helper.getLevel().dimension().identifier().toString(),
                capitalPos.getX(), capitalPos.getY(), capitalPos.getZ());
        var firstSquad = data.createOrReclaimArmyGroup(
                officer.getUUID(), firstCommander, ArmyFormation.LINE, anchor,
                helper.getLevel().getGameTime()).orElseThrow();
        var secondSquad = data.splitArmyGroup(
                officer.getUUID(), firstSquad.id(), secondCommander, List.of(soldier),
                "Outer Rim Patrol", anchor, helper.getLevel().getGameTime()).orElse(null);
        if (secondSquad == null || data.armyGroupsForKingdom(kingdom.id()).size() != 2
                || !data.configureArmyGroup(
                        officer.getUUID(), secondSquad.id(), "Outer Rim Patrol", anchor,
                        List.of(anchor, new ArmyLocation(anchor.dimensionId(),
                                anchor.x() + 32, anchor.y(), anchor.z())),
                        Optional.of(kingdom.claims().getFirst().id()))
                || !data.changeArmySupply(officer.getUUID(), secondSquad.id(), 32)) {
            helper.fail("Named multiple squads, patrol, claim defense, or supply authority failed");
            return;
        }
        for (int tick = 0; tick < 20; tick++) {
            secondCommanderEntity.tick();
        }
        officer.setPos(secondCommanderEntity.getX(), secondCommanderEntity.getY(), secondCommanderEntity.getZ());
        builder.setPos(secondCommanderEntity.getX(), secondCommanderEntity.getY(), secondCommanderEntity.getZ());
        if (!secondCommanderEntity.handleMenuButton(officer, RecruitCommandMenu.BUTTON_HOLD)
                || secondCommanderEntity.handleMenuButton(builder, RecruitCommandMenu.BUTTON_HOLD)
                || data.armyGroup(secondSquad.id()).orElseThrow().order().type()
                        != galacticwars.clonewars.army.ArmyCommandType.HOLD_POSITION) {
            helper.fail("Officer army command authority was not honored by the recruit runtime menu");
            return;
        }
        if (!secondCommanderEntity.handleMenuButton(officer, RecruitCommandMenu.BUTTON_PATROL)
                || secondCommanderEntity.handleMenuButton(builder, RecruitCommandMenu.BUTTON_PATROL)
                || data.armyGroup(secondSquad.id()).orElseThrow().order().type()
                        != galacticwars.clonewars.army.ArmyCommandType.PATROL_ROUTE
                || data.armyGroup(secondSquad.id()).orElseThrow().patrolRoute().size() != 2) {
            helper.fail("Playable officer patrol command did not persist an executable route");
            return;
        }
        if (data.setNpcServiceBranch(owner.getUUID(), soldier, NpcServiceBranch.CIVILIAN)
                || !data.releaseArmyMember(owner.getUUID(), soldier, false, anchor)
                || !data.reserveWorksite(owner.getUUID(), soldier, WorkerProfession.FARMER)
                || data.addRecruitToArmy(owner.getUUID(), secondSquad.id(), soldier)
                || !data.kingdom(kingdom.id()).orElseThrow().npc(soldier)
                        .map(entry -> entry.serviceBranch() == NpcServiceBranch.CIVILIAN).orElse(false)) {
            helper.fail("Military and civilian roster separation was not enforced atomically");
            return;
        }
        helper.succeed();
    }

    private static void blueprintSiteRuntime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchorPos = isolatedCapital(helper, 96);
        ChunkPos siteChunk = ChunkPos.containing(anchorPos);
        level.getChunkSource().updateChunkForced(siteChunk, true);
        level.getChunk(siteChunk.x(), siteChunk.z());
        BlockPos primaryLootPos = anchorPos.offset(2, 0, 0);
        BlockPos suppliesLootPos = anchorPos.offset(-2, 0, 0);
        level.setBlock(anchorPos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(anchorPos, ModBlocks.BLUEPRINT_SITE_ANCHOR.get().defaultBlockState(), 3);
        level.setBlock(primaryLootPos, ModBlocks.BLUEPRINT_SITE_LOOT.get().defaultBlockState(), 3);
        level.setBlock(suppliesLootPos, ModBlocks.BLUEPRINT_SITE_LOOT.get().defaultBlockState(), 3);
        level.removeBlockEntity(primaryLootPos);
        level.removeBlockEntity(suppliesLootPos);
        if (!(level.getBlockEntity(anchorPos) instanceof BlueprintSiteAnchorBlockEntity anchor)) {
            level.getChunkSource().updateChunkForced(siteChunk, false);
            helper.fail("Blueprint site anchor block entity was not created");
            return;
        }
        anchor.configure("galacticwars:hutt_salvage_depot", 1);
        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        FactionOutpostSavedData data = FactionOutpostSavedData.get(level);
        FactionOutpostRecord outpost = data.outposts().stream()
                .filter(candidate -> candidate.x() == anchorPos.getX() && candidate.y() == anchorPos.getY()
                        && candidate.z() == anchorPos.getZ())
                .findFirst().orElse(null);
        if (outpost == null || !outpost.factionId().equals("galacticwars:hutt_cartel")
                || !data.siteGenerated(outpost.id())) {
            level.getChunkSource().updateChunkForced(siteChunk, false);
            helper.fail("Blueprint site did not atomically register its faction outpost"
                    + " [record=" + (outpost != null)
                    + ", generated=" + (outpost != null && data.siteGenerated(outpost.id()))
                    + ", initialized=" + anchor.isInitialized()
                    + ", primary=" + hasLazyLoot(level, primaryLootPos)
                    + ", supplies=" + hasLazyLoot(level, suppliesLootPos) + "]");
            return;
        }
        helper.runAfterDelay(2, () -> {
            int expectedResidents = outpost.militaryNpcIds().size() + outpost.civilianNpcIds().size();
            int loadedResidents = (int) java.util.stream.Stream.concat(
                            outpost.militaryNpcIds().stream(), outpost.civilianNpcIds().stream())
                    .filter(residentId -> level.getEntity(residentId) instanceof GalacticRecruitEntity)
                    .count();
            if (expectedResidents < 3 || loadedResidents != expectedResidents
                    || !hasLazyLoot(level, primaryLootPos)
                    || !hasLazyLoot(level, suppliesLootPos)
                    || !anchor.isInitialized()) {
                level.getChunkSource().updateChunkForced(siteChunk, false);
                helper.fail("Blueprint site residents or lazy loot were incomplete"
                        + " [residents=" + loadedResidents + "/" + expectedResidents
                        + ", primary=" + hasLazyLoot(level, primaryLootPos)
                        + ", supplies=" + hasLazyLoot(level, suppliesLootPos)
                        + ", initialized=" + anchor.isInitialized() + "]");
                return;
            }
            BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
            int afterReplay = (int) java.util.stream.Stream.concat(
                            outpost.militaryNpcIds().stream(), outpost.civilianNpcIds().stream())
                    .filter(residentId -> level.getEntity(residentId) instanceof GalacticRecruitEntity)
                    .count();
            Tag encoded = FactionOutpostSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
            FactionOutpostSavedData restored = FactionOutpostSavedData.CODEC.parse(NbtOps.INSTANCE, encoded)
                    .getOrThrow();
            if (afterReplay != loadedResidents || restored.outpost(outpost.id()).isEmpty()
                    || !restored.siteGenerated(outpost.id())) {
                level.getChunkSource().updateChunkForced(siteChunk, false);
                helper.fail("Blueprint site initialization was not reload-idempotent");
                return;
            }
            level.getChunkSource().updateChunkForced(siteChunk, false);
            helper.succeed();
        });
    }

    private static void commanderCenterReplayRuntime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchorPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos primaryLootPos = anchorPos.offset(2, 0, 0);
        BlockPos suppliesLootPos = anchorPos.offset(-2, 0, 0);
        BlockPos commandCachePos = anchorPos.offset(0, 0, 2);
        BlockPos commandPostPos = anchorPos.offset(0, 0, -2);
        level.setBlock(anchorPos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(anchorPos, ModBlocks.BLUEPRINT_SITE_ANCHOR.get().defaultBlockState(), 3);
        placeBlueprintLootMarker(level, primaryLootPos, "primary");
        placeBlueprintLootMarker(level, suppliesLootPos, "supplies");
        placeBlueprintLootMarker(level, commandCachePos, "command_cache");
        level.setBlock(commandPostPos, ModBlocks.FACTION_COMMAND_POST.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(anchorPos) instanceof BlueprintSiteAnchorBlockEntity anchor)) {
            helper.fail("Commander-center anchor block entity was not created");
            return;
        }

        String blueprintId = "galacticwars:hutt_command_center";
        anchor.configure(blueprintId, 0);
        UUID siteId = UUID.nameUUIDFromBytes((level.dimension().identifier() + ":"
                + anchorPos.asLong() + ":" + blueprintId).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        FactionOutpostSavedData data = FactionOutpostSavedData.get(level);
        data.publishGeneratedSiteRecord(
                siteId,
                "galacticwars:hutt_cartel",
                level.dimension().identifier().toString(),
                anchorPos,
                48,
                List.of(),
                List.of(),
                level.getGameTime(),
                BlueprintSiteKind.OUTPOST,
                Optional.empty());
        if (data.siteGenerated(siteId)) {
            helper.fail("Interrupted commander center was marked complete before reconciliation");
            return;
        }

        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        FactionOutpostRecord outpost = data.outpost(siteId).orElse(null);
        if (outpost == null
                || outpost.siteKind() != BlueprintSiteKind.COMMAND_CENTER
                || !outpost.commandPostPosition().equals(Optional.of(commandPostPos))
                || !anchor.isInitialized()
                || !data.siteGenerated(siteId)) {
            helper.fail("Interrupted commander-center record was not reconciled"
                    + " [record=" + (outpost != null)
                    + ", kind=" + (outpost == null ? "missing" : outpost.siteKind().id())
                    + ", post=" + (outpost != null && outpost.commandPostPosition().isPresent())
                    + ", generated=" + data.siteGenerated(siteId)
                    + ", initialized=" + anchor.isInitialized()
                    + ", primary=" + hasLazyLoot(level, primaryLootPos)
                    + ", supplies=" + hasLazyLoot(level, suppliesLootPos)
                    + ", cache=" + hasLazyLoot(level, commandCachePos) + "]");
            return;
        }
        if (!(level.getBlockEntity(commandPostPos) instanceof FactionCommandPostBlockEntity commandPost)
                || !commandPost.configured()
                || !siteId.equals(commandPost.siteId())
                || level.getBlockEntity(commandPostPos) instanceof CommandCenterBlockEntity) {
            helper.fail("Generated NPC command post became claimable or lost site identity");
            return;
        }
        int expectedResidents = outpost.militaryNpcIds().size() + outpost.civilianNpcIds().size();
        List<GalacticRecruitEntity> loaded = level.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                new AABB(anchorPos).inflate(12.0D),
                recruit -> outpost.contains(recruit.getUUID()));
        long commanders = loaded.stream()
                .filter(recruit -> recruit.getNpcRole() == NpcRole.COMMANDER)
                .count();
        if (loaded.size() != expectedResidents || expectedResidents < 6 || commanders != 1
                || !hasLazyLoot(level, primaryLootPos)
                || !hasLazyLoot(level, suppliesLootPos)
                || !hasLazyLoot(level, commandCachePos)) {
            helper.fail("Commander-center residents, commander, or loot were incomplete");
            return;
        }

        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        int replayResidents = level.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                new AABB(anchorPos).inflate(12.0D),
                recruit -> outpost.contains(recruit.getUUID())).size();
        if (replayResidents != loaded.size()) {
            helper.fail("Commander-center replay duplicated residents");
            return;
        }
        helper.succeed();
    }

    private static void planetStructureRegistryRuntime(GameTestHelper helper) {
        Map<String, String> expectedFactions = Map.of(
                "tatooine", "galacticwars:hutt_cartel",
                "geonosis", "galacticwars:separatist",
                "kamino", "galacticwars:republic",
                "coruscant", "galacticwars:republic");
        Map<BlueprintSiteKind, TagKey<Structure>> structureTags = Map.of(
                BlueprintSiteKind.OUTPOST,
                TagKey.create(Registries.STRUCTURE, id("blueprint_sites")),
                BlueprintSiteKind.COMMAND_CENTER,
                TagKey.create(Registries.STRUCTURE, id("commander_centers")));

        var registries = helper.getLevel().registryAccess();
        for (var planetEntry : expectedFactions.entrySet()) {
            boolean hasOutpost = GameplayDataManager.snapshot().blueprints().values().stream()
                    .filter(blueprint -> blueprint.worldgen().isPresent())
                    .map(blueprint -> blueprint.worldgen().orElseThrow())
                    .anyMatch(profile -> profile.siteKind() == BlueprintSiteKind.OUTPOST
                            && profile.factionId().equals(planetEntry.getValue())
                            && profile.biomes().contains("galacticwars:" + planetEntry.getKey()));
            boolean hasCommanderCenter = GameplayDataManager.snapshot().blueprints().values().stream()
                    .filter(blueprint -> blueprint.worldgen().isPresent())
                    .map(blueprint -> blueprint.worldgen().orElseThrow())
                    .anyMatch(profile -> profile.siteKind() == BlueprintSiteKind.COMMAND_CENTER
                            && profile.factionId().equals(planetEntry.getValue())
                            && profile.biomes().contains("galacticwars:" + planetEntry.getKey()));
            if (!hasOutpost || !hasCommanderCenter) {
                helper.fail("Runtime blueprint routing is incomplete for " + planetEntry.getKey());
                return;
            }
        }

        var structures = registries.lookupOrThrow(Registries.STRUCTURE);
        var structureSets = registries.lookupOrThrow(Registries.STRUCTURE_SET);
        for (var structureEntry : structureTags.entrySet()) {
            String structureId = structureEntry.getKey() == BlueprintSiteKind.OUTPOST
                    ? "blueprint_structure" : "commander_center_structure";
            var structureHolder = structures.get(ResourceKey.create(
                    Registries.STRUCTURE, id(structureId))).orElse(null);
            if (structureHolder == null
                    || !(structureHolder.value() instanceof galacticwars.clonewars.world.BlueprintStructure structure)
                    || structure.siteKind() != structureEntry.getKey()) {
                helper.fail("Registered structure kind mismatch for " + structureId);
                return;
            }
            String setId = structureEntry.getKey() == BlueprintSiteKind.OUTPOST
                    ? "blueprint_sites" : "commander_centers";
            if (structureSets.get(ResourceKey.create(
                    Registries.STRUCTURE_SET, id(setId))).isEmpty()) {
                helper.fail("Missing registered structure set " + setId);
                return;
            }
        }
        helper.succeed();
    }

    private static void blueprintSiteCompatibleHash(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchorPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos primaryLootPos = anchorPos.offset(2, 0, 0);
        BlockPos suppliesLootPos = anchorPos.offset(-2, 0, 0);
        level.setBlock(anchorPos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(anchorPos, ModBlocks.BLUEPRINT_SITE_ANCHOR.get().defaultBlockState(), 3);
        placeBlueprintLootMarker(level, primaryLootPos, "primary");
        placeBlueprintLootMarker(level, suppliesLootPos, "supplies");
        if (!(level.getBlockEntity(anchorPos) instanceof BlueprintSiteAnchorBlockEntity anchor)) {
            helper.fail("Compatible-hash site anchor block entity was not created");
            return;
        }
        anchor.configure(
                "galacticwars:hutt_salvage_depot",
                0,
                "c0bcfbc3a785dcb038746273c17e4542dcdeb04ce762af4cae5f4ed9a89048a7");
        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        FactionOutpostRecord outpost = FactionOutpostSavedData.get(level).outposts().stream()
                .filter(candidate -> candidate.x() == anchorPos.getX()
                        && candidate.y() == anchorPos.getY()
                        && candidate.z() == anchorPos.getZ())
                .findFirst()
                .orElse(null);
        if (outpost == null
                || outpost.siteKind() != BlueprintSiteKind.OUTPOST
                || !anchor.isInitialized()
                || !hasLazyLoot(level, primaryLootPos)
                || !hasLazyLoot(level, suppliesLootPos)) {
            String legacyHash =
                    "c0bcfbc3a785dcb038746273c17e4542dcdeb04ce762af4cae5f4ed9a89048a7";
            boolean hashAccepted = GameplayDataManager.snapshot()
                    .blueprint("galacticwars:hutt_salvage_depot")
                    .map(blueprint -> blueprint.matchesContentHash(legacyHash))
                    .orElse(false);
            helper.fail("Compatible pre-v3 blueprint content hash did not initialize"
                    + " [record=" + (outpost != null)
                    + ", invalid=" + anchor.isInitializationInvalid()
                    + ", initialized=" + anchor.isInitialized()
                    + ", hashAccepted=" + hashAccepted
                    + ", primary=" + hasLazyLoot(level, primaryLootPos)
                    + ", supplies=" + hasLazyLoot(level, suppliesLootPos) + "]");
            return;
        }
        helper.succeed();
    }

    private static void blueprintSiteContentHashMismatch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchorPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos lootPos = anchorPos.offset(2, 0, 0);
        level.setBlock(anchorPos, ModBlocks.BLUEPRINT_SITE_ANCHOR.get().defaultBlockState(), 3);
        level.setBlock(lootPos, ModBlocks.BLUEPRINT_SITE_LOOT.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(anchorPos) instanceof BlueprintSiteAnchorBlockEntity anchor)) {
            helper.fail("Blueprint site anchor block entity was not created");
            return;
        }
        anchor.configure("galacticwars:hutt_salvage_depot", 0, "0".repeat(64));
        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        BlueprintSiteAnchorBlockEntity.serverTick(level, anchorPos, level.getBlockState(anchorPos), anchor);
        boolean outpostRegistered = FactionOutpostSavedData.get(level).outposts().stream()
                .anyMatch(candidate -> candidate.x() == anchorPos.getX() && candidate.y() == anchorPos.getY()
                        && candidate.z() == anchorPos.getZ());
        if (!anchor.isInitializationInvalid() || outpostRegistered
                || !level.getBlockState(lootPos).is(ModBlocks.BLUEPRINT_SITE_LOOT.get())) {
            helper.fail("Mismatched blueprint content hash was allowed to initialize a generated site");
            return;
        }
        helper.succeed();
    }

    private static void blueprintUnknownBlockRejection(GameTestHelper helper) {
        try {
            new BaseBlockPlacement(0, 0, 0, "galacticwars:not_registered", "minecraft:stone")
                    .blockState();
            helper.fail("Unknown blueprint block resolved through the defaulted registry");
        } catch (IllegalStateException expected) {
            if (!expected.getMessage().equals("unknown blueprint block galacticwars:not_registered")) {
                helper.fail("Unknown blueprint block error changed: " + expected.getMessage());
                return;
            }
            helper.succeed();
        }
    }

    private static void placeBlueprintLootMarker(ServerLevel level, BlockPos position, String marker) {
        level.setBlock(position, ModBlocks.BLUEPRINT_SITE_LOOT.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(position) instanceof BlueprintSiteLootBlockEntity loot)) {
            throw new IllegalStateException("Blueprint loot marker block entity was not created");
        }
        loot.configure(marker);
    }

    private static boolean hasLazyLoot(ServerLevel level, BlockPos position) {
        return level.getBlockEntity(position)
                instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container
                && container.getLootTable() != null;
    }

    private static void playerClassEmbodiedRuntime(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 97);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        player.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        if (!hall.claim(player)) {
            helper.fail("Player class fixture could not claim its physical Command Center");
            return;
        }
        player.getInventory().add(new ItemStack(ModItems.REPUBLIC_FACTION_TOKEN.get()));
        FactionSelectionMenu factionMenu = new FactionSelectionMenu(
                70, player.getInventory(), hallPos);
        int republic = factionMenu.factionIds().indexOf("galacticwars:republic");
        if (republic < 0 || !factionMenu.clickMenuButton(player, republic)) {
            helper.fail("Player class fixture could not commit its authoritative faction");
            return;
        }
        CommandCenterOperationsMenu operations = (CommandCenterOperationsMenu)
                new CommandCenterOperationsMenuProvider(hallPos)
                        .createMenu(71, player.getInventory(), player);
        player.containerMenu = operations;

        UUID selectionId = UUID.randomUUID();
        boolean selected = PlayerClassRuntime.select(
                player, selectionId, "galacticwars:clone_trooper");
        boolean replayAcceptedWithoutChange = PlayerClassRuntime.select(
                player, selectionId, "galacticwars:clone_trooper");
        boolean wrongFactionRejected = !PlayerClassRuntime.select(
                player, UUID.randomUUID(), "galacticwars:b1_line_droid");
        var assigned = ClassProgressSavedData.get(helper.getLevel()).state(player.getUUID());
        if (!selected || !replayAcceptedWithoutChange || !wrongFactionRejected
                || !assigned.classId().equals("galacticwars:clone_trooper")
                || assigned.rank() != 1 || assigned.resource() != 100) {
            helper.fail("Command Center class selection was not faction-bound and replay-safe: "
                    + assigned);
            return;
        }

        player.setYRot(0.0F);
        player.setYHeadRot(0.0F);
        player.setXRot(0.0F);
        var target = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.EVENT);
        if (target == null) {
            helper.fail("Player class fixture could not create its hostile target");
            return;
        }
        target.setPos(player.getX(), player.getY(), player.getZ() + 6.0D);
        target.setNoAi(true);
        if (!helper.getLevel().addFreshEntity(target)) {
            helper.fail("Player class fixture could not register its hostile target");
            return;
        }
        BlockPos occluder = BlockPos.containing(
                player.getX(), player.getEyeY(), player.getZ() + 3.0D);
        helper.getLevel().setBlock(occluder, Blocks.IRON_BLOCK.defaultBlockState(), 3);
        boolean occludedTargetRejected = !PlayerClassRuntime.activate(
                player, UUID.randomUUID(), 0);
        var occludedState = ClassProgressSavedData.get(helper.getLevel()).state(player.getUUID());
        helper.getLevel().removeBlock(occluder, false);
        if (!occludedTargetRejected || !occludedState.equals(assigned)) {
            helper.fail("Player class targeting crossed solid blocks or charged a rejected activation: "
                    + occludedState);
            return;
        }
        UUID activationId = UUID.randomUUID();
        boolean activated = PlayerClassRuntime.activate(player, activationId, 0);
        var activatedState = ClassProgressSavedData.get(helper.getLevel()).state(player.getUUID());
        boolean duplicateAcceptedWithoutChange = PlayerClassRuntime.activate(player, activationId, 0);
        var duplicateState = ClassProgressSavedData.get(helper.getLevel()).state(player.getUUID());
        boolean cooldownRejected = !PlayerClassRuntime.activate(
                player, UUID.randomUUID(), 0);
        ClassHudPayload hud = PlayerClassRuntime.hudPayload(player);
        boolean fallbackWeaponApplied = helper.getLevel().getEntitiesOfClass(
                        BlasterBoltEntity.class, player.getBoundingBox().inflate(16.0D))
                .stream()
                .anyMatch(bolt -> bolt.getOwner() == player
                        && bolt.getWeaponItem().is(ModItems.DC15_BLASTER.get()));
        if (!activated || !duplicateAcceptedWithoutChange || !cooldownRejected
                || activatedState.resource() != 85
                || activatedState.experience() != 3L
                || !duplicateState.equals(activatedState)
                || !fallbackWeaponApplied
                || hud.cooldown1() <= 0
                || !hud.ability1Id().equals("galacticwars:suppressive_fire")
                || !hud.ability2Id().isEmpty()) {
            helper.fail("Player class ability did not execute with persistent resource, XP, cooldown and replay authority: "
                    + activatedState + ", hud=" + hud);
            return;
        }

        player.setItemInHand(
                InteractionHand.MAIN_HAND, new ItemStack(ModItems.WESTAR_BLASTER.get()));
        var suppressiveFire = GameplayDataManager.snapshot()
                .ability("galacticwars:suppressive_fire").orElseThrow();
        boolean heldWeaponShot = ClassAbilityEffectRegistry.execute(
                helper.getLevel(), player, suppressiveFire, target);
        boolean heldWeaponPreserved = helper.getLevel().getEntitiesOfClass(
                        BlasterBoltEntity.class, player.getBoundingBox().inflate(16.0D))
                .stream()
                .anyMatch(bolt -> bolt.getOwner() == player
                        && bolt.getWeaponItem().is(ModItems.WESTAR_BLASTER.get()));
        if (!heldWeaponShot || !heldWeaponPreserved) {
            helper.fail("Player class projectile did not preserve a valid held faction weapon");
            return;
        }

        Tag encoded = ClassProgressSavedData.CODEC.encodeStart(
                NbtOps.INSTANCE, ClassProgressSavedData.get(helper.getLevel())).getOrThrow();
        ClassProgressSavedData restored = ClassProgressSavedData.CODEC.parse(
                NbtOps.INSTANCE, encoded).getOrThrow();
        if (!restored.state(player.getUUID()).equals(activatedState)
                || !codecRoundTrips(
                        new ClassSelectPayload(UUID.randomUUID(), "galacticwars:clone_trooper"),
                        ClassSelectPayload.STREAM_CODEC)
                || !codecRoundTrips(
                        new ClassActivatePayload(UUID.randomUUID(), 0),
                        ClassActivatePayload.STREAM_CODEC)
                || !codecRoundTrips(hud, ClassHudPayload.STREAM_CODEC)) {
            helper.fail("Player class persistence or bounded packet codecs did not round-trip");
            return;
        }
        helper.succeed();
    }

    private static void planetFactionOutpostRuntime(GameTestHelper helper) {
        ServerLevel planet = helper.getLevel();
        String geonosisDimensionId = "galacticwars:geonosis";
        String guardEntityTypeId = BuiltInRegistries.ENTITY_TYPE
                .getKey(ModEntityTypes.B1_SECURITY_DROID.get()).toString();
        String civilianEntityTypeId = BuiltInRegistries.ENTITY_TYPE
                .getKey(ModEntityTypes.SEPARATIST_TECHNICIAN.get()).toString();
        PlanetFactionSpawnPolicy.Evaluation guardEvaluation = PlanetFactionSpawnPolicy.evaluate(
                GameplayDataManager.snapshot(), geonosisDimensionId, guardEntityTypeId);
        PlanetFactionSpawnPolicy.Evaluation civilianEvaluation = PlanetFactionSpawnPolicy.evaluate(
                GameplayDataManager.snapshot(), geonosisDimensionId, civilianEntityTypeId);
        if (!guardEvaluation.allowed()
                || guardEvaluation.serviceBranch() != NpcServiceBranch.MILITARY
                || !civilianEvaluation.allowed()
                || civilianEvaluation.serviceBranch() != NpcServiceBranch.CIVILIAN
                || !guardEvaluation.factionId().equals(civilianEvaluation.factionId())) {
            helper.fail("Geonosis policy did not authorize its Separatist military and civilian lifecycle");
            return;
        }
        FactionOutpostSavedData outposts = FactionOutpostSavedData.get(planet);
        String dimensionId = planet.dimension().identifier().toString();
        int coordinate = 48_008;
        while (true) {
            int candidate = coordinate;
            boolean isolated = outposts.outposts().stream()
                    .filter(outpost -> outpost.dimensionId().equals(dimensionId))
                    .allMatch(outpost -> outpost.distanceSquared(candidate, candidate) >= 1_048_576L);
            if (isolated) {
                break;
            }
            coordinate += 1_024;
        }
        int chunkX = coordinate >> 4;
        int chunkZ = coordinate >> 4;
        planet.setChunkForced(chunkX, chunkZ, true);
        planet.getChunk(chunkX, chunkZ);
        int surfaceY = planet.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                coordinate,
                coordinate);
        BlockPos center = new BlockPos(coordinate, surfaceY, coordinate);
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                planet.setBlock(center.offset(dx, -1, dz),
                        ModBlocks.GEONOSIS_ROCK.get().defaultBlockState(), 3);
                for (int dy = 0; dy <= 3; dy++) {
                    planet.setBlock(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        GalacticRecruitEntity guard = spawnPlanetPolicyRecruitAt(
                planet, ModEntityTypes.B1_SECURITY_DROID.get(), center, guardEvaluation);
        guard.setInvulnerable(true);
        guard.setPos(center.getX() + 0.5D, center.getY(), center.getZ() - 2.5D);
        GalacticRecruitEntity civilian = spawnPlanetPolicyRecruitAt(
                planet,
                ModEntityTypes.SEPARATIST_TECHNICIAN.get(),
                center.offset(3, 0, 0),
                civilianEvaluation);
        civilian.setInvulnerable(true);
        civilian.setPos(center.getX() + 3.5D, center.getY(), center.getZ() + 0.5D);
        var hostile = EntityTypes.ZOMBIE.create(planet, EntitySpawnReason.EVENT);
        if (hostile == null) {
            guard.discard();
            civilian.discard();
            planet.setChunkForced(chunkX, chunkZ, false);
            helper.fail("Could not create a hostile target for the planet faction lifecycle test");
            return;
        }
        hostile.setPos(center.getX() + 0.5D, center.getY(), center.getZ() - 5.5D);
        hostile.setNoAi(true);
        hostile.setInvulnerable(true);
        if (!planet.addFreshEntity(hostile)) {
            guard.discard();
            civilian.discard();
            planet.setChunkForced(chunkX, chunkZ, false);
            helper.fail("Could not register the planet faction hostile target");
            return;
        }

        FactionOutpostRecord guardOutpost = outposts.outpostForNpc(guard.getUUID()).orElse(null);
        FactionOutpostRecord civilianOutpost = outposts.outpostForNpc(civilian.getUUID()).orElse(null);
        if (guardOutpost == null || civilianOutpost == null
                || !guardOutpost.id().equals(civilianOutpost.id())
                || !dimensionId.equals(guardOutpost.dimensionId())
                || !guard.isNaturalPlanetNpcInitialized()
                || !civilian.isNaturalPlanetNpcInitialized()
                || !guard.hasHome() || !civilian.hasHome()) {
            guard.discard();
            civilian.discard();
            hostile.discard();
            planet.setChunkForced(chunkX, chunkZ, false);
            helper.fail("Natural planet NPCs did not join one persisted faction outpost with homes");
            return;
        }

        UUID outpostId = guardOutpost.id();
        long startedAt = helper.getTick();
        boolean[] complete = {false};
        boolean[] engaged = {false};
        boolean[] workerPrepared = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            engaged[0] = engaged[0]
                    || guard.getTarget() == hostile
                    || BrainUtil.getMemory(guard,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET) == hostile;
            if (engaged[0] && hostile.isAlive()) {
                hostile.discard();
            }
            if (!workerPrepared[0] && outposts.siteGenerated(outpostId)) {
                workerPrepared[0] = true;
                civilian.setWorkerProfession(WorkerProfession.BUILDER);
                setRecruitField(civilian, "nextNaturalProductionGameTime", 0L);
                civilian.setPos(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D);
                civilian.getNavigation().stop();
            }
            // The dedicated natural_civilian_brain_runtime scenario proves daylight scheduling.
            // This combined lifecycle scenario can run beside tests that change the shared world
            // time, so drive the already-authorized physical production transaction directly.
            if (workerPrepared[0]) {
                civilian.tryProduceNaturalSettlementSupplies();
            }
            int storedDuracrete = planet.getBlockEntity(center.offset(1, 0, 0))
                    instanceof Container container
                    ? container.countItem(ModItems.DURACRETE.get()) : 0;
            if (engaged[0] && workerPrepared[0] && storedDuracrete > 0) {
                complete[0] = true;
                boolean physicalSite = planet.getBlockState(center.offset(1, 0, 0)).is(Blocks.BARREL)
                        && planet.getBlockState(center.offset(0, 2, 0)).is(Blocks.DEEPSLATE_TILES);
                guard.discard();
                civilian.discard();
                hostile.discard();
                planet.setChunkForced(chunkX, chunkZ, false);
                if (!physicalSite) {
                    helper.fail("Planet faction outpost was marked generated without its physical shelter");
                    return;
                }
                helper.succeed();
                return;
            }
            if (helper.getTick() - startedAt >= 360L) {
                complete[0] = true;
                String diagnostics = "site=" + outposts.siteGenerated(outpostId)
                        + ", engaged=" + engaged[0]
                        + ", target=" + guard.getTarget()
                        + ", attackMemory=" + BrainUtil.getMemory(guard,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        + ", workerPrepared=" + workerPrepared[0]
                        + ", storedDuracrete=" + storedDuracrete
                        + ", guardTicks=" + guard.tickCount
                        + ", civilianTicks=" + civilian.tickCount;
                guard.discard();
                civilian.discard();
                hostile.discard();
                planet.setChunkForced(chunkX, chunkZ, false);
                helper.fail("Planet faction military/work runtime did not complete: " + diagnostics);
            }
        });
    }

    private static void physicalTradeTransaction(GameTestHelper helper) {
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        ProgressionDecision pledge = progression.apply(new ProgressionEvent(
                UUID.randomUUID(), player.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1));
        if (!pledge.accepted() || !pledge.state().unlocks().contains("faction_intro")) {
            helper.fail("Faction pledge did not unlock introductory physical trade");
            return;
        }
        var runtimeSnapshot = LaunchContentRuntime.current();
        var liveDefinitions = runtimeSnapshot.definitions();
        var liveTrade = liveDefinitions.trades().get("republic_quartermaster");
        LinkedHashMap<String, LaunchContentDefinitions.TradeDefinition>
                overriddenTrades = new LinkedHashMap<>(liveDefinitions.trades());
        overriddenTrades.put(liveTrade.id(),
                new LaunchContentDefinitions.TradeDefinition(
                        liveTrade.id(), liveTrade.factionId(), 17, liveTrade.itemId(), 5,
                        liveTrade.requiredUnlock(), liveTrade.stockTier(), liveTrade.regionalPrerequisite()));
        PhysicalTradeService.TradePreview overridePreview;
        try {
            LaunchContentRuntime.install(
                    new LaunchContentDefinitions(
                            liveDefinitions.planets(), liveDefinitions.vehicles(),
                            liveDefinitions.forceAbilities(), liveDefinitions.forceTraditions(),
                            liveDefinitions.forceNodes(), liveDefinitions.quests(),
                            overriddenTrades, liveDefinitions.conquestRegions()),
                    runtimeSnapshot.factions(), runtimeSnapshot.units());
            overridePreview = PhysicalTradeService.preview(player, "republic_quartermaster");
        } finally {
            LaunchContentRuntime.install(
                    liveDefinitions, runtimeSnapshot.factions(), runtimeSnapshot.units());
        }
        if (overridePreview.itemCount() != 5 || overridePreview.creditPrice() != 17
                || !overridePreview.reason().equals("insufficient_credits")) {
            helper.fail("Server datapack override did not drive the authoritative trade preview: "
                    + overridePreview);
            return;
        }
        PhysicalTradeService.TradePreview insufficient = PhysicalTradeService.preview(
                player, "republic_quartermaster");
        if (insufficient.eligible() || !insufficient.reason().equals("insufficient_credits")
                || !insufficient.itemId().equals("galacticwars:energy_cell")
                || insufficient.itemCount() != 8 || insufficient.creditPrice() != 12) {
            helper.fail("Server trade preview did not expose exact stock and insufficient-credit state: "
                    + insufficient);
            return;
        }
        player.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 12));
        PhysicalTradeService.TradePreview available = PhysicalTradeService.preview(
                player, "republic_quartermaster");
        if (!available.eligible() || !available.reason().equals("available")) {
            helper.fail("Funded introductory trade did not become eligible: " + available);
            return;
        }
        PhysicalTradeService.TradeResult staleQuote = PhysicalTradeService.purchase(
                player,
                UUID.randomUUID(),
                "republic_quartermaster",
                null,
                new PhysicalTradeService.TradeQuote(
                        available.tradeId(), available.itemId(), available.itemCount(),
                        available.creditPrice() + 1));
        if (staleQuote.accepted() || !staleQuote.reason().equals("offer_changed")
                || CreditTransactionService.playerBalance(player) != 12) {
            helper.fail("Stale server quote changed inventory or bypassed revalidation: " + staleQuote);
            return;
        }
        UUID eventId = UUID.randomUUID();
        PhysicalTradeService.TradeResult purchase = PhysicalTradeService.purchase(
                player, eventId, "republic_quartermaster");
        PhysicalTradeService.TradeResult missingTrade = PhysicalTradeService.purchase(
                player, UUID.randomUUID(), null);
        int energyCells = player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(ModItems.ENERGY_CELL.get()))
                .mapToInt(ItemStack::getCount).sum();
        if (!purchase.accepted() || !purchase.changed() || purchase.creditsCharged() != 12
                || missingTrade.accepted() || !missingTrade.reason().equals("unknown_trade")
                || CreditTransactionService.playerBalance(player) != 0 || energyCells != 8) {
            helper.fail("Physical trade did not atomically exchange Credit Chips for goods");
            return;
        }
        PhysicalTradeService.TradeResult replay = PhysicalTradeService.purchase(
                player, eventId, "republic_quartermaster");
        int replayedEnergyCells = player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(ModItems.ENERGY_CELL.get()))
                .mapToInt(ItemStack::getCount).sum();
        if (!replay.accepted() || replay.changed() || replayedEnergyCells != energyCells) {
            helper.fail("Duplicate physical trade charged or granted twice");
            return;
        }

        ServerPlayer huttPlayer = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ProgressionDecision huttPledge = progression.apply(new ProgressionEvent(
                UUID.randomUUID(), huttPlayer.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:hutt_cartel", 1));
        huttPlayer.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 28));
        PhysicalTradeService.TradePreview locked = PhysicalTradeService.preview(
                huttPlayer, "hutt_broker");
        if (!huttPledge.accepted() || locked.eligible() || !locked.reason().equals("trade_locked")) {
            helper.fail("Advanced merchant stock ignored its progression lock: " + locked);
            return;
        }
        for (int delivery = 0; delivery < 3; delivery++) {
            progression.apply(new ProgressionEvent(
                    UUID.randomUUID(), huttPlayer.getUUID(), ProgressionEventType.DELIVERY_COMPLETED,
                    "courier/" + UUID.randomUUID(), 1));
        }
        var regionalSnapshot = LaunchContentRuntime.current();
        var regionalDefinitions = regionalSnapshot.definitions();
        var huttTrade = regionalDefinitions.trades().get("hutt_broker");
        LinkedHashMap<String, LaunchContentDefinitions.TradeDefinition> regionalTrades =
                new LinkedHashMap<>(regionalDefinitions.trades());
        String regionalTestId = "gametest_trade_region";
        regionalTrades.put(huttTrade.id(), new LaunchContentDefinitions.TradeDefinition(
                huttTrade.id(), huttTrade.factionId(), huttTrade.price(), huttTrade.itemId(),
                huttTrade.itemCount(), "advanced_trading", huttTrade.stockTier(),
                regionalTestId));
        var regionTemplate = regionalDefinitions.conquestRegions().get("tatooine_spaceport");
        LinkedHashMap<String, LaunchContentDefinitions.ConquestRegionDefinition> regionalRegions =
                new LinkedHashMap<>(regionalDefinitions.conquestRegions());
        regionalRegions.put(regionalTestId, new LaunchContentDefinitions.ConquestRegionDefinition(
                regionalTestId, regionTemplate.planetId(), regionTemplate.protectedRadius(),
                regionTemplate.captureTicks(), regionTemplate.rewardCredits(),
                regionTemplate.landmarkX(), regionTemplate.landmarkZ(),
                regionTemplate.captureRadius(), regionTemplate.defenderFaction()));
        PhysicalTradeService.TradePreview regional;
        PhysicalTradeService.TradePreview regionEligible;
        try {
            LaunchContentRuntime.install(new LaunchContentDefinitions(
                            regionalDefinitions.planets(), regionalDefinitions.vehicles(),
                            regionalDefinitions.forceAbilities(), regionalDefinitions.forceTraditions(),
                            regionalDefinitions.forceNodes(), regionalDefinitions.quests(),
                            regionalTrades, regionalRegions),
                    regionalSnapshot.factions(), regionalSnapshot.units());
            ConquestSavedData conquest = ConquestSavedData.get(helper.getLevel());
            conquest.put(new ConquestControlState(
                    regionalTestId, helper.getLevel().dimension().identifier().toString(),
                    0, 1, 0, "galacticwars:republic", "", "", 0, 1L));
            regional = PhysicalTradeService.preview(huttPlayer, "hutt_broker");
            conquest.put(new ConquestControlState(
                    regionalTestId, helper.getLevel().dimension().identifier().toString(),
                    0, 1, 0, "galacticwars:hutt_cartel", "", "", 0, 2L));
            regionEligible = PhysicalTradeService.preview(huttPlayer, "hutt_broker");
        } finally {
            LaunchContentRuntime.install(regionalDefinitions,
                    regionalSnapshot.factions(), regionalSnapshot.units());
        }
        if (regional.eligible() || !regional.reason().equals("regional_control_required")) {
            helper.fail("Regional merchant stock ignored conquest control: " + regional);
            return;
        }
        if (!regionEligible.eligible()) {
            helper.fail("Regional merchant stock stayed locked after valid faction control: "
                    + regionEligible);
            return;
        }

        PhysicalTradeService.TradePreview veteranLocked = PhysicalTradeService.preview(
                huttPlayer, "tatooine_hutt_salvage");
        if (veteranLocked.eligible()
                || !veteranLocked.reason().equals("veteran_trade_locked")) {
            helper.fail("Tier-two stock ignored the veteran-trade progression gate: "
                    + veteranLocked);
            return;
        }
        huttPlayer.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 68));
        ProgressionDecision captured = progression.apply(new ProgressionEvent(
                UUID.randomUUID(), huttPlayer.getUUID(), ProgressionEventType.REGION_CAPTURED,
                "tatooine_spaceport", 1));
        // Fund the quote before checking its regional gate so insufficient funds cannot
        // mask the conquest-control reason this assertion is intended to validate.
        huttPlayer.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 68));
        ConquestSavedData.get(helper.getLevel()).put(new ConquestControlState(
                "tatooine_spaceport", helper.getLevel().dimension().identifier().toString(),
                0, 1, 0, "galacticwars:republic", "", "", 0, 2L));
        PhysicalTradeService.TradePreview controlRequired = PhysicalTradeService.preview(
                huttPlayer, "tatooine_hutt_salvage");
        if (!captured.accepted() || !captured.state().unlocks().contains("veteran_trades")
                || controlRequired.eligible()
                || !controlRequired.reason().equals("regional_control_required")) {
            helper.fail("Captured-region progression did not gate veteran stock correctly: "
                    + controlRequired);
            return;
        }
        ConquestSavedData.get(helper.getLevel()).put(new ConquestControlState(
                "tatooine_spaceport", helper.getLevel().dimension().identifier().toString(),
                0, 1, 0, "galacticwars:hutt_cartel", "", "", 0, 3L));
        PhysicalTradeService.TradePreview veteranAvailable = PhysicalTradeService.preview(
                huttPlayer, "tatooine_hutt_salvage");
        int expectedVeteranPrice = FactionBalanceService.tradeCreditPrice(
                "galacticwars:hutt_cartel",
                LaunchContentCatalog.trades().get("tatooine_hutt_salvage").price());
        if (!veteranAvailable.eligible()
                || veteranAvailable.creditPrice() != expectedVeteranPrice
                || veteranAvailable.itemCount() != 4) {
            helper.fail("Valid regional control did not expose veteran physical stock: "
                    + veteranAvailable);
            return;
        }

        ServerPlayer embargoedPlayer = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ServerPlayer merchantOwner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        progression.apply(new ProgressionEvent(
                UUID.randomUUID(), embargoedPlayer.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1));
        embargoedPlayer.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 12));
        KingdomSavedData kingdoms = KingdomSavedData.get(helper.getLevel());
        KingdomRecord customerKingdom = kingdoms.foundKingdom(
                embargoedPlayer.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), isolatedCapital(helper, 81));
        KingdomRecord merchantKingdom = kingdoms.foundKingdom(
                merchantOwner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), isolatedCapital(helper, 82));
        GalacticRecruitEntity merchant = helper.spawn(
                ModEntityTypes.REPUBLIC_CIVILIAN.get(), new BlockPos(2, 1, 2));
        merchant.initializeFromSpawnEgg();
        merchant.setWorkerProfession(WorkerProfession.MERCHANT);
        embargoedPlayer.setPos(merchant.getX(), merchant.getY(), merchant.getZ() + 1.0D);
        boolean merchantRegistered = kingdoms.registerRecruit(
                merchantOwner.getUUID(), merchant.getUUID(), NpcServiceBranch.CIVILIAN);
        boolean embargoApplied = kingdoms.setEmbargo(
                embargoedPlayer.getUUID(), merchantKingdom.id(), true);
        PhysicalTradeService.TradePreview embargoed = PhysicalTradeService.preview(
                embargoedPlayer, "republic_quartermaster", merchant);
        if (!merchantRegistered || !embargoApplied
                || customerKingdom.id().equals(merchantKingdom.id())
                || embargoed.eligible() || !embargoed.reason().equals("trade_embargoed")) {
            helper.fail("Server merchant preview ignored a live kingdom embargo: registered="
                    + merchantRegistered + ", applied=" + embargoApplied + ", preview=" + embargoed);
            return;
        }
        helper.succeed();
    }

    private static void factionTraderDispositionRuntime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ProgressionSavedData progression = ProgressionSavedData.get(level);
        if (!progression.apply(new ProgressionEvent(
                UUID.randomUUID(),
                player.getUUID(),
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:hutt_cartel",
                1)).accepted()) {
            helper.fail("Trader disposition setup could not pledge the neutral customer faction");
            return;
        }
        player.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 100));

        ServerPlayer merchantOwner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos marketHallPos = isolatedCapital(helper, 83);
        CommandCenterBlockEntity marketHall = placeCommandCenter(helper, marketHallPos);
        marketHall.claim(merchantOwner);
        marketHall.setFaction("galacticwars:republic");
        KingdomSavedData kingdoms = KingdomSavedData.get(level);
        if (kingdoms.activateHall(
                merchantOwner.getUUID(),
                marketHall.factionId(),
                level.dimension().identifier().toString(),
                marketHallPos).isEmpty()) {
            helper.fail("Trader disposition setup could not activate its physical market");
            return;
        }
        GalacticRecruitEntity trader = helper.spawn(
                ModEntityTypes.REPUBLIC_CIVILIAN.get(), new BlockPos(2, 1, 2));
        trader.initializeFromSpawnEgg();
        trader.tame(merchantOwner);
        trader.setWorkerProfession(WorkerProfession.MERCHANT);
        configurePhysicalMarket(
                helper, merchantOwner, trader, marketHall, marketHallPos, 32);
        player.setPos(trader.getX(), trader.getY(), trader.getZ() + 1.0D);
        level.getChunkSource().move(player);

        FactionAlignmentSavedData alignment = FactionAlignmentSavedData.get(level);
        FactionId merchantFaction = FactionId.of("republic");
        alignment.setScore(player.getUUID(), merchantFaction, 10);
        PhysicalTradeService.TradePreview friendly =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        int basePrice = FactionBalanceService.tradeCreditPrice(
                "galacticwars:republic",
                LaunchContentCatalog.trades().get("republic_quartermaster").price());
        int friendlyPrice = FactionBalanceService.applyPercentCeil(basePrice, 90);

        alignment.setScore(player.getUUID(), merchantFaction, 0);
        PhysicalTradeService.TradePreview neutral =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        PhysicalTradeService.TradeQuote neutralQuote = new PhysicalTradeService.TradeQuote(
                neutral.tradeId(),
                neutral.itemId(),
                neutral.itemCount(),
                neutral.creditPrice(),
                neutral.disposition());

        alignment.setScore(player.getUUID(), merchantFaction, -1);
        PhysicalTradeService.TradePreview wary =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        alignment.setScore(player.getUUID(), merchantFaction, -21);
        PhysicalTradeService.TradePreview hostile =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        if (!friendly.eligible()
                || !friendly.disposition().equals("friendly")
                || friendly.creditPrice() != friendlyPrice
                || !neutral.eligible()
                || !neutral.disposition().equals("neutral")
                || neutral.creditPrice() != basePrice
                || wary.eligible()
                || !wary.reason().equals("wary_merchant")
                || !wary.disposition().equals("wary")
                || hostile.eligible()
                || !hostile.reason().equals("hostile_merchant")
                || !hostile.disposition().equals("hostile")) {
            helper.fail("Live trader disposition or pricing contract failed: friendly="
                    + friendly + ", neutral=" + neutral + ", wary=" + wary
                    + ", hostile=" + hostile);
            return;
        }

        alignment.setScore(player.getUUID(), merchantFaction, 10);
        int creditsBeforeStale = CreditTransactionService.playerBalance(player);
        PhysicalTradeService.TradeResult stale = PhysicalTradeService.purchase(
                player,
                UUID.randomUUID(),
                "republic_quartermaster",
                trader,
                neutralQuote);
        if (stale.accepted()
                || !stale.reason().equals("offer_changed")
                || CreditTransactionService.playerBalance(player) != creditsBeforeStale) {
            helper.fail("Disposition change did not invalidate the stale trader quote: " + stale);
            return;
        }

        PhysicalTradeService.TradePreview live =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        PhysicalTradeService.TradeQuote liveQuote = new PhysicalTradeService.TradeQuote(
                live.tradeId(),
                live.itemId(),
                live.itemCount(),
                live.creditPrice(),
                live.disposition());
        MerchantTradeMenu marketMenu = (MerchantTradeMenu) new MerchantTradeMenuProvider(trader)
                .createMenu(72, player.getInventory(), player);
        int stockBeforeClose = countContainerItem(marketHall, ModItems.ENERGY_CELL.get());
        int creditsBeforeClose = CreditTransactionService.playerBalance(player);
        setWorkerPhase(trader, WorkerPhase.COOLDOWN, "market_closed", null);
        PhysicalTradeService.TradePreview closedMarket = PhysicalTradeService.preview(
                player, "republic_quartermaster", trader);
        PhysicalTradeService.TradeResult closedPurchase = PhysicalTradeService.purchase(
                player,
                UUID.randomUUID(),
                "republic_quartermaster",
                trader,
                liveQuote);
        if (closedMarket.eligible()
                || !closedMarket.reason().equals("merchant_unavailable")
                || closedPurchase.accepted()
                || !closedPurchase.reason().equals("merchant_unavailable")
                || marketMenu.stillValid(player)
                || countContainerItem(marketHall, ModItems.ENERGY_CELL.get()) != stockBeforeClose
                || CreditTransactionService.playerBalance(player) != creditsBeforeClose) {
            helper.fail("A closed physical market became synthetic stock: preview="
                    + closedMarket + ", purchase=" + closedPurchase
                    + ", menuValid=" + marketMenu.stillValid(player)
                    + ", stock=" + stockBeforeClose + "/"
                    + countContainerItem(marketHall, ModItems.ENERGY_CELL.get())
                    + ", credits=" + creditsBeforeClose + "/"
                    + CreditTransactionService.playerBalance(player));
            return;
        }
        setWorkerPhase(trader, WorkerPhase.COOLDOWN, "market_open", null);

        ServerPlayer overflowPlayer = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        overflowPlayer.setPos(trader.getX(), trader.getY(), trader.getZ() + 1.0D);
        level.getChunkSource().move(overflowPlayer);
        alignment.setScore(overflowPlayer.getUUID(), merchantFaction, 10);
        overflowPlayer.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 100));
        setSavedDataState(
                progression,
                overflowPlayer.getUUID(),
                new ProgressionState(
                        ProgressionState.CURRENT_SCHEMA_VERSION,
                        overflowPlayer.getUUID(),
                        "galacticwars:hutt_cartel",
                        0,
                        Set.of(),
                        Map.of(ProgressionEventType.TRADE_COMPLETED, Integer.MAX_VALUE),
                        Map.of(),
                        Set.of("intro_quest", "faction_intro")));
        PhysicalTradeService.TradePreview overflowPreview = PhysicalTradeService.preview(
                overflowPlayer, "republic_quartermaster", trader);
        PhysicalTradeService.TradeQuote overflowQuote = new PhysicalTradeService.TradeQuote(
                overflowPreview.tradeId(),
                overflowPreview.itemId(),
                overflowPreview.itemCount(),
                overflowPreview.creditPrice(),
                overflowPreview.disposition());
        int stockBeforeOverflow = countContainerItem(marketHall, ModItems.ENERGY_CELL.get());
        int creditsBeforeOverflow = CreditTransactionService.playerBalance(overflowPlayer);
        PhysicalTradeService.TradeResult overflowPurchase = PhysicalTradeService.purchase(
                overflowPlayer,
                UUID.randomUUID(),
                "republic_quartermaster",
                trader,
                overflowQuote);
        if (overflowPurchase.accepted()
                || !overflowPurchase.reason().equals("progression_limit_reached")
                || countContainerItem(marketHall, ModItems.ENERGY_CELL.get()) != stockBeforeOverflow
                || CreditTransactionService.playerBalance(overflowPlayer) != creditsBeforeOverflow) {
            helper.fail("Progression rejection consumed physical market state: preview="
                    + overflowPreview + ", purchase=" + overflowPurchase
                    + ", stock=" + stockBeforeOverflow + "/"
                    + countContainerItem(marketHall, ModItems.ENERGY_CELL.get())
                    + ", credits=" + creditsBeforeOverflow + "/"
                    + CreditTransactionService.playerBalance(overflowPlayer));
            return;
        }

        UUID replayId = UUID.randomUUID();
        PhysicalTradeService.TradeResult purchase = PhysicalTradeService.purchase(
                player,
                replayId,
                "republic_quartermaster",
                trader,
                liveQuote);
        int purchasedItems = countPlayerItem(
                player,
                BuiltInRegistries.ITEM.getValue(Identifier.parse(live.itemId())));
        PhysicalTradeService.TradeResult replay = PhysicalTradeService.purchase(
                player,
                replayId,
                "republic_quartermaster",
                trader,
                liveQuote);
        int replayedItems = countPlayerItem(
                player,
                BuiltInRegistries.ITEM.getValue(Identifier.parse(live.itemId())));
        alignment.setScore(player.getUUID(), merchantFaction, -21);
        PhysicalTradeService.TradePreview hostileRevalidation =
                PhysicalTradeService.preview(player, "republic_quartermaster", trader);
        if (!purchase.accepted()
                || !purchase.changed()
                || purchase.creditsCharged() != friendlyPrice
                || !replay.accepted()
                || replay.changed()
                || replayedItems != purchasedItems
                || alignment.processedEventCount(player.getUUID()) != 1
                || hostileRevalidation.eligible()
                || !hostileRevalidation.reason().equals("hostile_merchant")
                || !hostileRevalidation.disposition().equals("hostile")) {
            helper.fail("Trader purchase replay or live-hostility revalidation failed: purchase="
                    + purchase + ", replay=" + replay + ", items=" + purchasedItems
                    + "/" + replayedItems + ", reputationEvents="
                    + alignment.processedEventCount(player.getUUID())
                    + ", hostility=" + hostileRevalidation);
            return;
        }
        trader.discard();
        helper.succeed();
    }

    private static void recruitEntityContract(GameTestHelper helper) {
        GalacticRecruitEntity recruit = helper.spawn(ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(1, 1, 1));
        recruit.setWorkerProfession(WorkerProfession.MINER);
        if (!recruit.isAlive()
                || recruit.getRecruitDuty() != RecruitDuty.WORKER
                || !recruit.getMainHandItem().is(Items.IRON_PICKAXE)) {
            helper.fail("Recruit entity did not apply its persisted worker duty and held equipment contract");
        }
        helper.succeed();
    }

    private static void curatedNpcRuntimeContracts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        KingdomSavedData data = KingdomSavedData.get(level);
        ServerPlayer republicOwner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer separatistOwner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos republicHallPos = isolatedCapital(helper, 90);
        BlockPos separatistHallPos = isolatedCapital(helper, 91);
        CommandCenterBlockEntity republicHall = placeCommandCenter(helper, republicHallPos);
        CommandCenterBlockEntity separatistHall = placeCommandCenter(helper, separatistHallPos);
        republicHall.claim(republicOwner);
        separatistHall.claim(separatistOwner);
        data.activateHall(
                republicOwner.getUUID(), "galacticwars:republic",
                level.dimension().identifier().toString(), republicHallPos).orElseThrow();
        data.activateHall(
                separatistOwner.getUUID(), "galacticwars:separatist",
                level.dimension().identifier().toString(), separatistHallPos).orElseThrow();
        FactionAlignmentSavedData.get(level).setScore(
                republicOwner.getUUID(), FactionId.of("republic"), 100);
        FactionAlignmentSavedData.get(level).setScore(
                separatistOwner.getUUID(), FactionId.of("separatist"), 100);
        ProgressionSavedData progression = ProgressionSavedData.get(level);
        if (!progression.apply(new ProgressionEvent(
                UUID.randomUUID(), republicOwner.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1)).accepted()
                || !progression.apply(new ProgressionEvent(
                UUID.randomUUID(), separatistOwner.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:separatist", 1)).accepted()) {
            helper.fail("Curated NPC hiring setup could not pledge both test factions");
            return;
        }

        GalacticRecruitEntity togruta = helper.spawn(
                ModEntityTypes.TOGRUTA_CIVILIAN.get(), new BlockPos(2, 1, 2));
        togruta.initializeFromSpawnEgg();
        republicOwner.setPos(togruta.getX(), togruta.getY(), togruta.getZ());
        if (!togruta.handleMenuButton(republicOwner, RecruitCommandMenu.BUTTON_HIRE)
                || !togruta.isOwnedBy(republicOwner)
                || togruta.getServiceBranch() != NpcServiceBranch.CIVILIAN
                || togruta.getRecruitDuty() != RecruitDuty.WORKER) {
            helper.fail("Same-faction Togruta civilian hiring did not create a worker contract");
            return;
        }

        var togrutaArchetype = GameplayDataManager.snapshot()
                .civilianArchetypeForEntity("galacticwars:togruta_civilian").orElseThrow();
        if (!Set.copyOf(togrutaArchetype.professions()).equals(
                Set.of("farmer", "cook", "courier", "builder"))) {
            helper.fail("Togruta civilian profession catalog changed: "
                    + togrutaArchetype.professions());
            return;
        }

        GalacticRecruitEntity crossFaction = helper.spawn(
                ModEntityTypes.SENATE_COMMANDO.get(), new BlockPos(3, 1, 2));
        crossFaction.initializeFromSpawnEgg();
        separatistOwner.setPos(crossFaction.getX(), crossFaction.getY(), crossFaction.getZ());
        if (crossFaction.handleMenuButton(separatistOwner, RecruitCommandMenu.BUTTON_HIRE)
                || crossFaction.isTame()) {
            helper.fail("A Separatist kingdom hired the cross-faction Senate Commando");
            return;
        }

        GalacticRecruitEntity commander = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 2));
        commander.initializeFromSpawnEgg();
        republicOwner.setPos(commander.getX(), commander.getY(), commander.getZ());
        if (!commander.handleMenuButton(republicOwner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Clone commander candidate could not be hired");
            return;
        }
        KingdomBaseBlueprint keepBlueprint = GameplayDataManager.snapshot()
                .blueprint(KingdomBaseBlueprint.STARTER_KEEP_ID).orElseThrow();
        BuildProject keepProject = fullyProgressProject(
                data, republicOwner.getUUID(), keepBlueprint,
                level.dimension().identifier().toString(), republicHallPos.offset(8, 0, 0));
        if (!data.completeBuildProject(republicOwner.getUUID(), keepProject, keepBlueprint)
                || !commander.handleMenuButton(
                republicOwner, RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER)
                || commander.getRecruitDuty() != RecruitDuty.COMMANDER) {
            helper.fail("Clone commander promotion did not update synchronized duty");
            return;
        }

        TagValueOutput commanderOutput = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess());
        if (!commander.save(commanderOutput)) {
            helper.fail("Promoted commander could not be serialized");
            return;
        }
        CompoundTag commanderTag = commanderOutput.buildResult();
        Entity loadedCommander = EntityType.loadEntityRecursive(
                commanderTag,
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false),
                entity -> entity);
        if (!(loadedCommander instanceof GalacticRecruitEntity persistedCommander)
                || persistedCommander.getRecruitDuty() != RecruitDuty.COMMANDER) {
            helper.fail("Commander duty did not survive entity persistence");
            return;
        }

        if (!data.clearCommander(republicOwner.getUUID(), commander.getUUID())) {
            helper.fail("Commander demotion could not clear settlement authority");
            return;
        }
        CompoundTag demotedTag = commanderTag.copy();
        demotedTag.putString("RecruitDuty", RecruitDuty.SOLDIER.id());
        Entity loadedSoldier = EntityType.loadEntityRecursive(
                demotedTag,
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false),
                entity -> entity);
        if (!(loadedSoldier instanceof GalacticRecruitEntity demoted)
                || demoted.getRecruitDuty() != RecruitDuty.SOLDIER
                || demoted.getServiceBranch() != NpcServiceBranch.MILITARY) {
            helper.fail("Demoted commander did not restore the soldier duty and military branch");
            return;
        }

        OverworldFactionSpawnProfile republicSpawns = GameplayDataManager.snapshot()
                .overworldSpawnProfiles().get("galacticwars:republic");
        OverworldFactionSpawnProfile separatistSpawns = GameplayDataManager.snapshot()
                .overworldSpawnProfiles().get("galacticwars:separatist");
        if (republicSpawns == null
                || republicSpawns.branchFor("galacticwars:senate_commando")
                != NpcServiceBranch.MILITARY
                || republicSpawns.branchFor("galacticwars:republic_honor_guard")
                != NpcServiceBranch.MILITARY
                || republicSpawns.branchFor("galacticwars:togruta_civilian")
                != NpcServiceBranch.CIVILIAN
                || separatistSpawns == null
                || separatistSpawns.branchFor("galacticwars:b1_security_droid")
                != NpcServiceBranch.MILITARY
                || separatistSpawns.branchFor("galacticwars:separatist_technician")
                != NpcServiceBranch.CIVILIAN) {
            helper.fail("Curated natural/outpost mappings do not match their runtime roles");
            return;
        }
        helper.succeed();
    }

    private static void validateCuratedNpcContract(
            GameTestHelper helper,
            GalacticRecruitEntity recruit
    ) {
        String expectedFaction;
        NpcServiceBranch expectedBranch;
        net.minecraft.world.item.Item expectedMainHand;
        float expectedHealth;
        if (recruit.getType() == ModEntityTypes.SENATE_COMMANDO.get()) {
            expectedFaction = "galacticwars:republic";
            expectedBranch = NpcServiceBranch.MILITARY;
            expectedMainHand = ModItems.DC15_BLASTER.get();
            expectedHealth = 30.0F;
        } else if (recruit.getType() == ModEntityTypes.REPUBLIC_HONOR_GUARD.get()) {
            expectedFaction = "galacticwars:republic";
            expectedBranch = NpcServiceBranch.MILITARY;
            expectedMainHand = ModItems.VIBROBLADE.get();
            expectedHealth = 34.0F;
        } else if (recruit.getType() == ModEntityTypes.B1_SECURITY_DROID.get()) {
            expectedFaction = "galacticwars:separatist";
            expectedBranch = NpcServiceBranch.MILITARY;
            expectedMainHand = ModItems.E5_BLASTER.get();
            expectedHealth = 24.0F;
        } else if (recruit.getType() == ModEntityTypes.TOGRUTA_CIVILIAN.get()) {
            expectedFaction = "galacticwars:republic";
            expectedBranch = NpcServiceBranch.CIVILIAN;
            expectedMainHand = Items.AIR;
            expectedHealth = 20.0F;
        } else if (recruit.getType() == ModEntityTypes.HUTT_ENFORCER.get()) {
            expectedFaction = "galacticwars:hutt_cartel";
            expectedBranch = NpcServiceBranch.MILITARY;
            expectedMainHand = ModItems.SCATTER_BLASTER.get();
            expectedHealth = 28.0F;
        } else if (recruit.getType() == ModEntityTypes.SMUGGLER.get()) {
            expectedFaction = "galacticwars:hutt_cartel";
            expectedBranch = NpcServiceBranch.MILITARY;
            expectedMainHand = ModItems.SCATTER_BLASTER.get();
            expectedHealth = 24.0F;
        } else if (recruit.getType() == ModEntityTypes.HUTT_CIVILIAN.get()) {
            expectedFaction = "galacticwars:hutt_cartel";
            expectedBranch = NpcServiceBranch.CIVILIAN;
            expectedMainHand = Items.AIR;
            expectedHealth = 18.0F;
        } else if (recruit.getType() == ModEntityTypes.SEPARATIST_TECHNICIAN.get()) {
            expectedFaction = "galacticwars:separatist";
            expectedBranch = NpcServiceBranch.CIVILIAN;
            expectedMainHand = Items.AIR;
            expectedHealth = 18.0F;
        } else {
            return;
        }
        boolean correctMainHand = expectedMainHand == Items.AIR
                ? recruit.getMainHandItem().isEmpty()
                : recruit.getMainHandItem().is(expectedMainHand);
        if (!recruit.getRecruitFactionId().equals(expectedFaction)
                || recruit.getServiceBranch() != expectedBranch
                || !correctMainHand
                || Math.abs(recruit.getMaxHealth() - expectedHealth) > 0.01F
                || recruit.getAttributeValue(Attributes.MOVEMENT_SPEED) <= 0.0D) {
            helper.fail("Curated NPC runtime contract mismatch for " + recruit.getType()
                    + ": faction=" + recruit.getRecruitFactionId()
                    + ", branch=" + recruit.getServiceBranch()
                    + ", mainHand=" + recruit.getMainHandItem()
                    + ", maxHealth=" + recruit.getMaxHealth());
        }
    }

    private static void workerTagsAndLoot(GameTestHelper helper) {
        BlockPos stonePos = new BlockPos(1, 1, 1);
        BlockPos logPos = new BlockPos(2, 1, 1);
        helper.setBlock(stonePos, ModBlocks.DURACRETE.get());
        helper.setBlock(logPos, Blocks.OAK_LOG);
        if (!helper.getBlockState(stonePos).is(ModBlockTags.WORKER_MINEABLE)
                || !helper.getBlockState(logPos).is(ModBlockTags.WORKER_LOGS)) {
            helper.fail("Worker allowlist tags were not loaded from the datapack");
        }
        List<ItemStack> drops = Block.getDrops(
                helper.getBlockState(stonePos),
                helper.getLevel(),
                helper.absolutePos(stonePos),
                null);
        if (drops.stream().mapToInt(ItemStack::getCount).sum() != 1
                || drops.stream().noneMatch(stack -> stack.is(ModItems.DURACRETE.get()))) {
            helper.fail("duracrete loot table did not conserve one mined block");
        }
        helper.succeed();
    }

    private static void workforceSavedDataAuthority(GameTestHelper helper) {
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer builder = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer officer = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer quartermaster = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer member = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos hallPos = isolatedCapital(helper, 3);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(), "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(), hallPos).orElseThrow();
        GalacticRecruitEntity assignedRecruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        assignedRecruit.initializeFromSpawnEgg();
        assignedRecruit.tame(owner);
        assignedRecruit.setWorkerProfession(WorkerProfession.FARMER);
        UUID first = assignedRecruit.getUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        if (!data.addMember(owner.getUUID(), builder.getUUID(), KingdomMemberRole.BUILDER, "")
                || !data.addMember(
                        owner.getUUID(), officer.getUUID(), KingdomMemberRole.OFFICER, "")
                || !data.addMember(
                        owner.getUUID(), quartermaster.getUUID(),
                        KingdomMemberRole.QUARTERMASTER, "")
                || !data.addMember(
                        owner.getUUID(), member.getUUID(), KingdomMemberRole.MEMBER, "")
                || !data.registerRecruit(owner.getUUID(), first)
                || !data.registerRecruit(owner.getUUID(), second)
                || !data.registerRecruit(owner.getUUID(), third)
                || !data.reserveWorksite(owner.getUUID(), first, WorkerProfession.FARMER)
                || !data.reserveWorksite(owner.getUUID(), second, WorkerProfession.FARMER)
                || data.reserveWorksite(owner.getUUID(), third, WorkerProfession.FARMER)) {
            helper.fail("Frontier worksite did not enforce its persisted two-worker capacity");
        }
        WorksiteRecord worksite = data.assignedWorksite(owner.getUUID(), first).orElseThrow();
        UUID worksiteId = worksite.id();
        List<CourierWaypoint> route = List.of(
                new CourierWaypoint(
                        worksite.dimensionId(),
                        worksite.x(),
                        worksite.y(),
                        worksite.z(),
                        List.of(CourierTransferAction.takeAll())),
                new CourierWaypoint(
                        worksite.dimensionId(),
                        worksite.x() + 1,
                        worksite.y(),
                        worksite.z(),
                        List.of(CourierTransferAction.putAll())));
        WorksiteUpdateResult builderRoute = data.configureWorksiteRoute(
                builder.getUUID(),
                worksiteId,
                worksite.configuration().revision(),
                route,
                CourierRouteMode.LOOP);
        if (builderRoute.accepted() || !builderRoute.reasonCode().equals("permission_denied")) {
            helper.fail("Builder changed courier logistics without MANAGE_LOGISTICS");
        }
        worksite = data.assignedWorksite(owner.getUUID(), first).orElseThrow();
        WorksiteUpdateResult oneWaypoint = data.configureWorksiteRoute(
                owner.getUUID(),
                worksiteId,
                worksite.configuration().revision(),
                List.of(route.getFirst()),
                CourierRouteMode.LOOP);
        if (oneWaypoint.accepted() || !oneWaypoint.reasonCode().equals("invalid_route")) {
            helper.fail("A one-waypoint courier route was accepted");
        }
        worksite = data.assignedWorksite(owner.getUUID(), first).orElseThrow();
        WorksiteUpdateResult configuredRoute = data.configureWorksiteRoute(
                owner.getUUID(),
                worksiteId,
                worksite.configuration().revision(),
                route,
                CourierRouteMode.PING_PONG);
        if (!configuredRoute.accepted()) {
            helper.fail("A valid claimed courier route was rejected: "
                    + configuredRoute.reasonCode());
        }
        WorksiteRecord routeConfiguredWorksite = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        StorageEndpoint hallStorage = data.registeredStorageEndpoint(
                        owner.getUUID(),
                        helper.getLevel().dimension().identifier().toString(),
                        hallPos)
                .orElseThrow(() -> new IllegalStateException(
                        "Authority fixture Command Center was not registered as storage"));
        WorksiteUpdateResult configuredStorage = data.configureWorksiteStorage(
                owner.getUUID(),
                routeConfiguredWorksite.id(),
                routeConfiguredWorksite.configuration().revision(),
                hallStorage);
        if (!configuredStorage.accepted()) {
            helper.fail("A registered Command Center storage target was rejected: "
                    + configuredStorage.reasonCode());
        }
        WorksiteRecord configuredWorksite = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        SettlementRecord configuredSettlement = data.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlements().stream()
                .filter(candidate -> candidate.worksites().stream()
                        .anyMatch(candidateWorksite -> candidateWorksite.id().equals(worksiteId)))
                .findFirst()
                .orElseThrow();
        long settlementRevision = configuredSettlement.revision();
        long configurationRevision = configuredWorksite.configuration().revision();
        long routeRevision = configuredWorksite.configuration().courierRouteRevision();
        WorksiteUpdateResult unchangedRoute = data.configureWorksiteRoute(
                owner.getUUID(),
                configuredWorksite.id(),
                configurationRevision,
                List.copyOf(route),
                CourierRouteMode.PING_PONG);
        WorksiteRecord unchangedWorksite = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        SettlementRecord unchangedSettlement = data.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlements().stream()
                .filter(candidate -> candidate.worksites().stream()
                        .anyMatch(candidateWorksite -> candidateWorksite.id().equals(worksiteId)))
                .findFirst()
                .orElseThrow();
        if (unchangedRoute.accepted() || !unchangedRoute.reasonCode().equals("unchanged")
                || unchangedWorksite.configuration().revision() != configurationRevision
                || unchangedWorksite.configuration().courierRouteRevision() != routeRevision
                || unchangedSettlement.revision() != settlementRevision) {
            helper.fail("An identical courier route advanced durable revisions");
        }

        BlockPos alternateStoragePos = hallPos.offset(3, 0, 0);
        helper.getLevel().setBlock(
                alternateStoragePos, Blocks.CHEST.defaultBlockState(), 3);
        if (!(helper.getLevel().getBlockEntity(alternateStoragePos) instanceof Container alternateStorage)) {
            helper.fail("Authority fixture could not place its alternate registered storage");
            return;
        }
        StorageEndpoint alternateEndpoint = new StorageEndpoint(
                helper.getLevel().dimension().identifier().toString(),
                alternateStoragePos.getX(),
                alternateStoragePos.getY(),
                alternateStoragePos.getZ(),
                alternateStorage.getContainerSize());
        KingdomRecord authorityKingdom = data.kingdomForOwner(owner.getUUID()).orElseThrow();
        SettlementRecord authoritySettlement = authorityKingdom.settlement();
        WorksiteRecord authorityWorksite = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        WorksiteRecord registeredAlternate = authorityWorksite
                .withPrimaryStorageEndpoint(alternateEndpoint)
                .withPrimaryStorageEndpoint(hallStorage);
        List<WorksiteRecord> authorityWorksites = authoritySettlement.worksites().stream()
                .map(candidate -> candidate.id().equals(registeredAlternate.id())
                        ? registeredAlternate : candidate)
                .toList();
        SettlementRecord storageEnabledSettlement = new SettlementRecord(
                authoritySettlement.id(),
                authoritySettlement.dimensionId(),
                authoritySettlement.hallX(),
                authoritySettlement.hallY(),
                authoritySettlement.hallZ(),
                authoritySettlement.claimRadius(),
                authoritySettlement.housingCapacity(),
                authoritySettlement.recruitIds(),
                authoritySettlement.commanderId(),
                authoritySettlement.commanderPolicy(),
                authorityWorksites,
                authoritySettlement.buildProjects(),
                authoritySettlement.workOrders(),
                authoritySettlement.recruitmentCampaigns(),
                authoritySettlement.rewards().add(
                        hallStorage.slots() + alternateEndpoint.slots(), 0),
                authoritySettlement.revision() + 1,
                authoritySettlement.additionalCommanderIds());
        storeKingdomFixture(data, authorityKingdom.replaceSettlement(storageEnabledSettlement));
        if (data.registeredStorageEndpoint(
                        owner.getUUID(),
                        helper.getLevel().dimension().identifier().toString(),
                        alternateStoragePos)
                .filter(endpoint -> endpoint.slots() == alternateEndpoint.slots())
                .isEmpty()) {
            helper.fail("Authority fixture did not register its alternate storage");
            return;
        }
        setRecruitField(assignedRecruit, "storageTarget", hallPos.immutable());
        boolean alternateSelected = assignedRecruit.configureWorkerStorageFromMenu(
                owner, alternateStoragePos);
        invokeWorkerAuthorityReconciliation(assignedRecruit, helper.getLevel());
        WorksiteRecord selectedStorageWorksite = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        StorageEndpoint selectedStorage = selectedStorageWorksite.storageEndpoints().getFirst();
        if (!alternateSelected
                || selectedStorage.x() != alternateStoragePos.getX()
                || selectedStorage.y() != alternateStoragePos.getY()
                || selectedStorage.z() != alternateStoragePos.getZ()
                || !alternateStoragePos.equals(assignedRecruit.getStorageTarget())) {
            helper.fail("Storage selection did not survive worksite authority reconciliation: selected="
                    + alternateSelected + ", durable=" + selectedStorage
                    + ", entity=" + assignedRecruit.getStorageTarget());
            return;
        }

        GalacticRecruitEntity unassignedRecruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 2));
        unassignedRecruit.initializeFromSpawnEgg();
        unassignedRecruit.tame(owner);
        unassignedRecruit.setWorkerProfession(WorkerProfession.FARMER);
        if (!data.registerRecruit(owner.getUUID(), unassignedRecruit.getUUID())
                || WorksiteConfigurationMenuProvider.prepare(owner, unassignedRecruit).isPresent()) {
            helper.fail("Menu preparation accepted a worker without a durable worksite");
        }
        WorksiteConfigurationMenuProvider provider = WorksiteConfigurationMenuProvider
                .prepare(owner, assignedRecruit)
                .orElseThrow(() -> new IllegalStateException(
                        "Assigned worker did not produce a menu provider"));
        for (ServerPlayer actor : List.of(owner, officer, quartermaster, member)) {
            actor.setPos(
                    assignedRecruit.getX(),
                    assignedRecruit.getY(),
                    assignedRecruit.getZ());
        }
        WorksiteConfigurationMenu preparedMenu = (WorksiteConfigurationMenu) provider.createMenu(
                41, owner.getInventory(), owner);
        if (!preparedMenu.snapshot().worksiteId().equals(configuredWorksite.id())
                || !preparedMenu.snapshot().recruitId().equals(assignedRecruit.getUUID())) {
            helper.fail("Prepared worksite menu did not retain its authoritative snapshot");
        }

        WorksiteConfigurationMenu officerMenu = (WorksiteConfigurationMenu)
                WorksiteConfigurationMenuProvider.prepare(officer, assignedRecruit)
                        .orElseThrow(() -> new IllegalStateException(
                                "Officer did not receive a worksite snapshot"))
                        .createMenu(42, officer.getInventory(), officer);
        long concurrentRevision = preparedMenu.snapshot().configurationRevision();
        boolean overlayBeforeConcurrentEdit = preparedMenu.snapshot().overlayVisible();
        UUID replayId = UUID.randomUUID();
        WorksiteActionPayload firstEdit = new WorksiteActionPayload(
                replayId,
                preparedMenu.containerId,
                WorksiteConfigurationAction.TOGGLE_OVERLAY.id(),
                concurrentRevision,
                -1);
        boolean firstEditAccepted = preparedMenu.handleReplayAction(owner, firstEdit);
        WorksiteRecord afterFirstEdit = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        long afterFirstEditRevision = afterFirstEdit.configuration().revision();
        boolean replayRejected = !preparedMenu.handleReplayAction(owner, firstEdit);
        WorksiteRecord afterReplay = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        WorksiteActionPayload staleOfficerEdit = new WorksiteActionPayload(
                UUID.randomUUID(),
                officerMenu.containerId,
                WorksiteConfigurationAction.TOGGLE_KINGDOM_ACCESS.id(),
                concurrentRevision,
                -1);
        boolean staleOfficerRejected = !officerMenu.handleReplayAction(
                officer, staleOfficerEdit);
        WorksiteRecord afterStaleEdit = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        if (!firstEditAccepted
                || afterFirstEdit.configuration().overlayVisible()
                        == overlayBeforeConcurrentEdit
                || !replayRejected
                || afterReplay.configuration().revision() != afterFirstEditRevision
                || !"replayed_request".equals(preparedMenu.snapshot().feedbackCode())
                || !staleOfficerRejected
                || afterStaleEdit.configuration().revision() != afterFirstEditRevision
                || !"stale_revision".equals(officerMenu.snapshot().feedbackCode())) {
            helper.fail("Concurrent worksite menus did not enforce revision and replay authority: "
                    + "first=" + firstEditAccepted
                    + ", replay=" + replayRejected
                    + ", stale=" + staleOfficerRejected
                    + ", ownerFeedback=" + preparedMenu.snapshot().feedbackCode()
                    + ", officerFeedback=" + officerMenu.snapshot().feedbackCode()
                    + ", before=" + concurrentRevision
                    + ", after=" + afterStaleEdit.configuration().revision());
            return;
        }

        WorksiteConfigurationMenu memberMenu = (WorksiteConfigurationMenu)
                WorksiteConfigurationMenuProvider.prepare(member, assignedRecruit)
                        .orElseThrow(() -> new IllegalStateException(
                                "Member did not receive a read snapshot"))
                        .createMenu(43, member.getInventory(), member);
        long beforeMemberEdit = afterStaleEdit.configuration().revision();
        boolean memberEditRejected = !memberMenu.handleReplayAction(
                member,
                new WorksiteActionPayload(
                        UUID.randomUUID(),
                        memberMenu.containerId,
                        WorksiteConfigurationAction.HEIGHT_INCREASE.id(),
                        beforeMemberEdit,
                        -1));
        WorksiteRecord afterMemberEdit = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        if (!memberEditRejected
                || afterMemberEdit.configuration().revision() != beforeMemberEdit
                || !"permission_denied".equals(memberMenu.snapshot().feedbackCode())) {
            helper.fail("Ordinary member changed a worksite through the server menu: rejected="
                    + memberEditRejected + ", feedback=" + memberMenu.snapshot().feedbackCode());
            return;
        }

        WorksiteUpdateResult officerRoute = data.configureWorksiteRoute(
                officer.getUUID(),
                afterMemberEdit.id(),
                afterMemberEdit.configuration().revision(),
                route,
                CourierRouteMode.LOOP);
        WorksiteRecord afterOfficerRoute = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        WorksiteUpdateResult quartermasterRoute = data.configureWorksiteRoute(
                quartermaster.getUUID(),
                afterOfficerRoute.id(),
                afterOfficerRoute.configuration().revision(),
                route,
                CourierRouteMode.PING_PONG);
        WorksiteRecord afterQuartermasterRoute = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        WorksiteUpdateResult memberRoute = data.configureWorksiteRoute(
                member.getUUID(),
                afterQuartermasterRoute.id(),
                afterQuartermasterRoute.configuration().revision(),
                route,
                CourierRouteMode.LOOP);
        WorksiteRecord afterDeniedRoute = data.assignedWorksite(
                owner.getUUID(), first).orElseThrow();
        if (!officerRoute.accepted()
                || !quartermasterRoute.accepted()
                || memberRoute.accepted()
                || !"permission_denied".equals(memberRoute.reasonCode())
                || afterDeniedRoute.configuration().revision()
                        != afterQuartermasterRoute.configuration().revision()
                || afterDeniedRoute.configuration().courierRouteMode()
                        != CourierRouteMode.PING_PONG) {
            helper.fail("Worksite role matrix did not preserve logistics authority: officer="
                    + officerRoute.reasonCode() + ", quartermaster="
                    + quartermasterRoute.reasonCode() + ", member="
                    + memberRoute.reasonCode());
            return;
        }
        WorkOrder queued = new WorkOrder(
                UUID.randomUUID(), WorkOrderType.FARM, java.util.Optional.empty(), WorkOrderState.QUEUED,
                java.util.Optional.of(worksite.id()), java.util.Optional.empty(), worksite.dimensionId(),
                worksite.x(), worksite.y(), worksite.z(), "minecraft:wheat", 1, 0, "", 0);
        WorkOrder claimed = data.queueAndClaimWorkOrder(owner.getUUID(), first, queued).orElseThrow();
        WorkOrder blocked = data.blockWorkOrder(
                owner.getUUID(), claimed.id(), claimed.revision(), "target_unloaded").orElseThrow();
        WorkOrder resumed = data.resumeWorkOrder(
                owner.getUUID(), blocked.id(), first, blocked.revision()).orElseThrow();
        WorkOrder completed = data.progressWorkOrder(
                owner.getUUID(), resumed.id(), resumed.revision(), 1).orElseThrow();
        if (completed.state() != WorkOrderState.COMPLETED
                || !data.releaseWorkerAssignments(owner.getUUID(), first)
                || !data.reserveWorksite(owner.getUUID(), third, WorkerProfession.FARMER)) {
            helper.fail("Persisted work order transitions or capacity release failed");
        }
        Tag encoded = KingdomSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        KingdomSavedData restored = KingdomSavedData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        if (restored.assignedWorksite(owner.getUUID(), third).isEmpty()
                || restored.workOrder(owner.getUUID(), completed.id())
                        .filter(order -> order.state() == WorkOrderState.COMPLETED).isEmpty()
                || !restored.kingdomForOwner(owner.getUUID()).orElseThrow().id().equals(kingdom.id())) {
            helper.fail("Worksite and work order authority did not survive a SavedData round trip");
        }
        helper.succeed();
    }

    private static void recruitContractLifecycle(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 10);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ServerPlayer intruder = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.PHASE_I_CLONE_TROOPER.get(), new BlockPos(3, 1, 1));
        recruit.initializeFromSpawnEgg();
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        intruder.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("mandalorian"), 100);
        ProgressionSavedData progression = ProgressionSavedData.get(helper.getLevel());
        ProgressionDecision pledge = progression.apply(new ProgressionEvent(
                UUID.randomUUID(), owner.getUUID(), ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic", 1));
        if (!pledge.accepted()) {
            helper.fail("Phase I hiring setup could not pledge the Republic progression path");
            return;
        }
        owner.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 53));

        boolean hired = recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE);
        boolean owned = recruit.isOwnedBy(owner);
        int remainingCredits = RecruitmentPaymentService.creditCount(owner);
        boolean registered = data.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlement().containsRecruit(recruit.getUUID());
        ProgressionState phaseIProgress = progression.state(owner.getUUID());
        var cloneObjective = LaunchContentCatalog.questObjectives("republic_chapter_1").stream()
                .filter(objective -> objective.id().equals("clone_trooper"))
                .findFirst().orElseThrow();
        boolean objectiveCompatible = GalacticProgressionCoordinator.objectiveComplete(
                phaseIProgress, cloneObjective);
        if (!hired || !owned || remainingCredits != 28 || !registered
                || !phaseIProgress.hasSubjectPath(
                ProgressionEventType.RECRUIT_HIRED, "phase_i_clone_trooper")
                || !objectiveCompatible) {
            helper.fail("Exact-cost direct hiring failed: hired=" + hired
                    + ", owned=" + owned
                    + ", credits=" + remainingCredits
                    + ", registered=" + registered
                    + ", phaseIProgress=" + phaseIProgress
                    + ", objectiveCompatible=" + objectiveCompatible);
        }
        BlockPos intruderHallPos = hallPos.offset(64, 0, 0);
        CommandCenterBlockEntity intruderHall = placeCommandCenter(helper, intruderHallPos);
        intruderHall.claim(intruder);
        data.activateHall(
                intruder.getUUID(),
                intruderHall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                intruderHallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                intruder.getUUID(), FactionId.of("republic"), 100);
        GalacticRecruitEntity poorRecruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 3));
        intruder.setPos(poorRecruit.getX(), poorRecruit.getY(), poorRecruit.getZ());
        intruder.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 24));
        if (poorRecruit.handleMenuButton(intruder, RecruitCommandMenu.BUTTON_HIRE)
                || RecruitmentPaymentService.creditCount(intruder) != 24
                || data.kingdomForOwner(intruder.getUUID()).orElseThrow()
                        .settlement().containsRecruit(poorRecruit.getUUID())) {
            helper.fail("Insufficient direct-hire payment mutated funds or settlement state");
        }
        if (intruderHall.chargeDailyUpkeep(helper.getLevel().getGameTime() + 24000L, 1)) {
            helper.fail("Empty Hall treasury incorrectly paid upkeep");
        }
        intruder.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get()));
        if (poorRecruit.handleMenuButton(intruder, RecruitCommandMenu.BUTTON_HIRE)
                || RecruitmentPaymentService.creditCount(intruder) != 25) {
            helper.fail("Unpaid upkeep did not reject hiring without charging the player");
        }
        intruder.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (recruit.handleMenuButton(intruder, RecruitCommandMenu.BUTTON_FOLLOW)) {
            helper.fail("A non-owner command was accepted");
        }
        if (recruit.handleMenuButton(owner, Integer.MAX_VALUE)) {
            helper.fail("An unrecognized command button was accepted");
        }
        owner.setPos(recruit.getX() + 16.0, recruit.getY(), recruit.getZ());
        if (recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_FOLLOW)) {
            helper.fail("An out-of-range command was accepted");
        }
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        ItemStack preservedMilitaryWeapon = new ItemStack(ModItems.DC15_BLASTER.get());
        preservedMilitaryWeapon.set(
                DataComponents.CUSTOM_NAME, Component.literal("Veteran Service Blaster"));
        recruit.setMilitaryMainHandItem(preservedMilitaryWeapon.copy());
        recruit.setWorkerMainHandItem(new ItemStack(Items.IRON_HOE));
        int creditsBeforeIncompatibleTool = RecruitmentPaymentService.creditCount(owner);
        ProgressionState progressionBeforeIncompatibleTool = progression.state(owner.getUUID());
        if (recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)
                || RecruitmentPaymentService.creditCount(owner) != creditsBeforeIncompatibleTool
                || !progression.state(owner.getUUID()).equals(progressionBeforeIncompatibleTool)
                || !recruit.getWorkerMainHandItem().is(Items.IRON_HOE)) {
            helper.fail("Incompatible profession change mutated the tool, credits, or progression");
        }
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)
                || recruit.getWorkerProfession().filter(value -> value == WorkerProfession.MINER).isEmpty()) {
            helper.fail("Worker profession assignment failed");
        }
        recruit.getWorkerMainHandItem().setDamageValue(12);
        ItemStack preservedWorkerTool = recruit.getWorkerMainHandItem().copy();
        setWorkerInventory(recruit, new ItemStack(Items.COBBLESTONE));
        if (recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_RETURN_TO_SOLDIER)) {
            helper.fail("Worker with carried resources returned to soldier duty");
        }
        setWorkerInventory(recruit, ItemStack.EMPTY);
        BlockPos cancelledBuild = helper.absolutePos(new BlockPos(4, 1, 1));
        setRecruitField(recruit, "baseTarget", cancelledBuild);
        setRecruitField(recruit, "workTarget", cancelledBuild);
        if (recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_RETURN_TO_SOLDIER)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_CANCEL_BUILD)
                || recruit.getBaseTarget() != null
                || recruit.getWorkTarget() != null
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_RETURN_TO_SOLDIER)
                || recruit.getWorkerProfession().isPresent()
                || recruit.getRecruitDuty() != RecruitDuty.SOLDIER
                || !ItemStack.isSameItemSameComponents(
                        preservedMilitaryWeapon, recruit.getMainHandItem())
                || !ItemStack.isSameItemSameComponents(
                        preservedWorkerTool, recruit.getWorkerMainHandItem())) {
            helper.fail("Worker build cancellation or contract exit did not preserve its guards");
        }

        GalacticRecruitEntity commander = helper.spawn(
                ModEntityTypes.MANDALORIAN_WARRIOR.get(), new BlockPos(3, 1, 2));
        owner.setPos(commander.getX(), commander.getY(), commander.getZ());
        owner.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 25));
        if (!commander.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Commander candidate could not be hired");
        }
        KingdomBaseBlueprint keepBlueprint = GameplayDataManager.snapshot()
                .blueprint(KingdomBaseBlueprint.STARTER_KEEP_ID).orElseThrow();
        BuildProject keepProject = fullyProgressProject(
                data, owner.getUUID(), keepBlueprint,
                helper.getLevel().dimension().identifier().toString(),
                hallPos.offset(8, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), keepProject, keepBlueprint)
                || !commander.handleMenuButton(owner, RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER)) {
            helper.fail("Commander promotion prerequisites were not applied");
        }
        RecruitmentCampaign commanderCampaign = new RecruitmentCampaign(
                UUID.randomUUID(),
                "galacticwars:mandalorian_warrior",
                "",
                7,
                helper.getLevel().getGameTime() + 24000L,
                RecruitmentCampaignState.RESERVED,
                "reserved");
        hall.setItem(0, new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 7));
        if (!hall.reserveCredits(7)
                || !data.beginCampaign(owner.getUUID(), RecruitmentCampaignDecision.accepted(commanderCampaign))) {
            helper.fail("Commander campaign setup did not reserve its payment");
        }
        commander.die(helper.getLevel().damageSources().generic());
        KingdomRecord afterCommanderDeath = data.kingdomForOwner(owner.getUUID()).orElseThrow();
        RecruitmentCampaign cancelledCampaign = afterCommanderDeath.settlement().recruitmentCampaigns().stream()
                .filter(campaign -> campaign.id().equals(commanderCampaign.id()))
                .findFirst()
                .orElseThrow();
        if (afterCommanderDeath.settlement().containsRecruit(commander.getUUID())
                || afterCommanderDeath.settlement().commanderId().isPresent()
                || cancelledCampaign.active()
                || cancelledCampaign.reservedCost() != 0
                || hall.treasuryCredits() != 7) {
            helper.fail("Commander death did not release state and conserve its campaign reservation");
        }

        int housingToFill = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement().housingCapacity()
                - data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement().recruitIds().size();
        for (int index = 0; index < housingToFill; index++) {
            if (!data.registerRecruit(owner.getUUID(), UUID.randomUUID())) {
                helper.fail("Could not fill settlement housing for the rejection check");
            }
        }
        GalacticRecruitEntity crowdedRecruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 2));
        owner.setPos(crowdedRecruit.getX(), crowdedRecruit.getY(), crowdedRecruit.getZ());
        owner.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 25));
        int creditsBeforeCrowdedHire = RecruitmentPaymentService.creditCount(owner);
        if (crowdedRecruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || RecruitmentPaymentService.creditCount(owner) != creditsBeforeCrowdedHire
                || crowdedRecruit.isOwnedBy(owner)) {
            helper.fail("Full housing did not reject hiring without charging the player: before="
                    + creditsBeforeCrowdedHire + ", after="
                    + RecruitmentPaymentService.creditCount(owner));
        }
        owner.getInventory().clearContent();
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());

        RecruitLifecycleService.dropCarriedItems(
                helper.getLevel(), recruit, List.of(new ItemStack(Items.COBBLESTONE, 3)));
        int dropped = helper.getLevel().getEntitiesOfClass(
                        ItemEntity.class, recruit.getBoundingBox().inflate(3.0))
                .stream()
                .filter(item -> item.getItem().is(Items.COBBLESTONE))
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        if (dropped != 3) {
            helper.fail("Recruit lifecycle item drops did not conserve carried resources");
        }
        recruit.die(helper.getLevel().damageSources().generic());
        if (data.kingdomForOwner(owner.getUUID()).orElseThrow()
                .settlement().containsRecruit(recruit.getUUID())) {
            helper.fail("Dead recruit continued consuming settlement housing");
        }
        helper.succeed();
    }

    private static void recruitLoadoutPersistenceAndMigration(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        recruit.initializeFromSpawnEgg();

        ItemStack militaryWeapon = new ItemStack(ModItems.DC15_BLASTER.get());
        militaryWeapon.set(DataComponents.CUSTOM_NAME, Component.literal("Persistent DC-15"));
        ItemStack workerTool = new ItemStack(Items.IRON_PICKAXE);
        workerTool.setDamageValue(17);
        workerTool.set(DataComponents.CUSTOM_NAME, Component.literal("Persistent Work Tool"));
        ItemStack offhand = new ItemStack(Items.SHIELD);
        offhand.setDamageValue(9);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helmet.setDamageValue(7);
        ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
        chest.setDamageValue(11);
        ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
        legs.setDamageValue(13);
        ItemStack feet = new ItemStack(Items.IRON_BOOTS);
        feet.setDamageValue(5);

        recruit.setMilitaryMainHandItem(militaryWeapon.copy());
        recruit.setWorkerMainHandItem(workerTool.copy());
        recruit.setItemSlot(EquipmentSlot.OFFHAND, offhand.copy());
        recruit.setItemSlot(EquipmentSlot.HEAD, helmet.copy());
        recruit.setItemSlot(EquipmentSlot.CHEST, chest.copy());
        recruit.setItemSlot(EquipmentSlot.LEGS, legs.copy());
        recruit.setItemSlot(EquipmentSlot.FEET, feet.copy());
        invokeGameplayDataRefresh(recruit);
        assertRecruitStack(helper, militaryWeapon, recruit.getMilitaryMainHandItem(),
                "Gameplay-data refresh replaced the military weapon");
        assertRecruitStack(helper, workerTool, recruit.getWorkerMainHandItem(),
                "Gameplay-data refresh replaced the inactive worker tool");
        assertRecruitStack(helper, chest, recruit.getItemBySlot(EquipmentSlot.CHEST),
                "Gameplay-data refresh replaced custom armor");

        CompoundTag v14Tag = saveRecruit(recruit, level);
        GalacticRecruitEntity v14Loaded = loadRecruit(v14Tag, level);
        assertRecruitStack(helper, militaryWeapon, v14Loaded.getMilitaryMainHandItem(),
                "v14 reload lost military weapon components");
        assertRecruitStack(helper, workerTool, v14Loaded.getWorkerMainHandItem(),
                "v14 reload lost worker-tool durability");
        assertRecruitStack(helper, offhand, v14Loaded.getItemBySlot(EquipmentSlot.OFFHAND),
                "v14 reload lost offhand components");
        assertRecruitStack(helper, helmet, v14Loaded.getItemBySlot(EquipmentSlot.HEAD),
                "v14 reload lost helmet components");
        assertRecruitStack(helper, chest, v14Loaded.getItemBySlot(EquipmentSlot.CHEST),
                "v14 reload lost chest components");
        assertRecruitStack(helper, legs, v14Loaded.getItemBySlot(EquipmentSlot.LEGS),
                "v14 reload lost leg components");
        assertRecruitStack(helper, feet, v14Loaded.getItemBySlot(EquipmentSlot.FEET),
                "v14 reload lost feet components");

        CompoundTag v13CustomTag = v14Tag.copy();
        v13CustomTag.putInt("RecruitDataVersion", 13);
        GalacticRecruitEntity v13Custom = loadRecruit(v13CustomTag, level);
        assertRecruitStack(helper, militaryWeapon, v13Custom.getMilitaryMainHandItem(),
                "v13 migration replaced a non-iron custom military weapon");
        assertRecruitStack(helper, workerTool, v13Custom.getWorkerMainHandItem(),
                "v13 migration replaced a non-iron inactive worker tool");

        ItemStack configuredMilitaryWeapon = new ItemStack(ModItems.DC15_BLASTER.get());
        ItemStack legacyIronSword = new ItemStack(Items.IRON_SWORD);
        legacyIronSword.set(DataComponents.CUSTOM_NAME, Component.literal("Legacy Iron Sword"));
        GalacticRecruitEntity activeIronFixture = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 2));
        activeIronFixture.initializeFromSpawnEgg();
        activeIronFixture.setMilitaryMainHandItem(legacyIronSword.copy());
        CompoundTag activeIronTag = saveRecruit(activeIronFixture, level);
        activeIronTag.putInt("RecruitDataVersion", 13);
        GalacticRecruitEntity migratedActiveIron = loadRecruit(activeIronTag, level);
        assertRecruitStack(helper, configuredMilitaryWeapon, migratedActiveIron.getMilitaryMainHandItem(),
                "v13 migration did not replace an active legacy iron sword");

        GalacticRecruitEntity missingWeaponFixture = helper.spawn(
                ModEntityTypes.NIGHTSISTER_CIVILIAN.get(), new BlockPos(5, 1, 2));
        missingWeaponFixture.initializeFromSpawnEgg();
        missingWeaponFixture.setItemSlot(EquipmentSlot.MAINHAND, legacyIronSword.copy());
        CompoundTag missingWeaponTag = saveRecruit(missingWeaponFixture, level);
        missingWeaponTag.putInt("RecruitDataVersion", 13);
        missingWeaponTag.putString("ServiceBranch", NpcServiceBranch.MILITARY.id());
        GalacticRecruitEntity migratedMissingWeapon = loadRecruit(missingWeaponTag, level);
        helper.assertTrue(
                migratedMissingWeapon.getMilitaryMainHandItem().isEmpty(),
                "v13 migration did not clear a legacy iron sword without a configured replacement");

        GalacticRecruitEntity inactiveIronFixture = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(6, 1, 2));
        inactiveIronFixture.initializeFromSpawnEgg();
        inactiveIronFixture.setWorkerProfession(WorkerProfession.MINER);
        ItemStack activeWorkerTool = inactiveIronFixture.getWorkerMainHandItem().copy();
        inactiveIronFixture.setMilitaryMainHandItem(legacyIronSword.copy());
        CompoundTag inactiveIronTag = saveRecruit(inactiveIronFixture, level);
        inactiveIronTag.putInt("RecruitDataVersion", 13);
        GalacticRecruitEntity migratedInactiveIron = loadRecruit(inactiveIronTag, level);
        assertRecruitStack(helper, activeWorkerTool, migratedInactiveIron.getWorkerMainHandItem(),
                "v13 migration replaced a civilian-duty worker tool");
        assertRecruitStack(helper, configuredMilitaryWeapon, migratedInactiveIron.getMilitaryMainHandItem(),
                "v13 migration did not replace the inactive legacy military iron sword");

        GalacticRecruitEntity modernIronFixture = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(7, 1, 2));
        modernIronFixture.initializeFromSpawnEgg();
        modernIronFixture.setMilitaryMainHandItem(legacyIronSword.copy());
        GalacticRecruitEntity modernIronLoaded = loadRecruit(
                saveRecruit(modernIronFixture, level), level);
        assertRecruitStack(helper, legacyIronSword, modernIronLoaded.getMilitaryMainHandItem(),
                "v14 reload replaced a post-migration iron sword");

        CompoundTag v12SoldierTag = v14Tag.copy();
        v12SoldierTag.putInt("RecruitDataVersion", 12);
        v12SoldierTag.remove("DefaultLoadoutInitialized");
        v12SoldierTag.remove("InactiveDutyMainHand");
        GalacticRecruitEntity v12Soldier = loadRecruit(v12SoldierTag, level);
        assertRecruitStack(helper, militaryWeapon, v12Soldier.getMilitaryMainHandItem(),
                "v12 soldier migration did not retain its active military hand");
        if (!v12Soldier.getWorkerMainHandItem().isEmpty()) {
            helper.fail("v12 soldier migration invented a worker tool");
            return;
        }

        recruit.setWorkerProfession(WorkerProfession.MINER);
        CompoundTag v12WorkerTag = saveRecruit(recruit, level);
        v12WorkerTag.putInt("RecruitDataVersion", 12);
        v12WorkerTag.remove("DefaultLoadoutInitialized");
        v12WorkerTag.remove("InactiveDutyMainHand");
        GalacticRecruitEntity v12Worker = loadRecruit(v12WorkerTag, level);
        assertRecruitStack(helper, workerTool, v12Worker.getWorkerMainHandItem(),
                "v12 worker migration did not retain its active tool");
        GalacticRecruitEntity defaultFixture = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(8, 1, 2));
        defaultFixture.initializeFromSpawnEgg();
        assertRecruitStack(helper, defaultFixture.getMilitaryMainHandItem(),
                v12Worker.getMilitaryMainHandItem(),
                "v12 worker migration did not seed the unit-definition military weapon");
        defaultFixture.discard();
        activeIronFixture.discard();
        missingWeaponFixture.discard();
        inactiveIronFixture.discard();
        modernIronFixture.discard();
        recruit.discard();
        helper.succeed();
    }

    private static void kingdomRoleRecruitment(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hallPos = isolatedCapital(helper, 116);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        ServerPlayer officer = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ServerPlayer quartermaster = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        ServerPlayer member = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElseThrow();
        if (!data.addMember(owner.getUUID(), officer.getUUID(), KingdomMemberRole.OFFICER, "")
                || !data.addMember(owner.getUUID(), quartermaster.getUUID(),
                        KingdomMemberRole.QUARTERMASTER, "")
                || !data.addMember(owner.getUUID(), member.getUUID(), KingdomMemberRole.MEMBER, "")) {
            helper.fail("Recruitment role test could not create kingdom members");
            return;
        }

        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data, owner.getUUID(), forwardBase,
                level.dimension().identifier().toString(), hallPos.offset(32, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)) {
            helper.fail("Recruitment role test could not unlock an army group");
            return;
        }
        GalacticRecruitEntity commander = helper.spawn(
                ModEntityTypes.ARC_TROOPER.get(), new BlockPos(1, 1, 1));
        commander.initializeFromSpawnEgg();
        commander.tame(owner);
        if (!data.registerRecruit(owner.getUUID(), commander.getUUID())
                || !data.promoteCommander(owner.getUUID(), commander.getUUID())) {
            helper.fail("Recruitment role test could not register its commander");
            return;
        }
        ArmyLocation anchor = new ArmyLocation(
                level.dimension().identifier().toString(),
                commander.getX(), commander.getY(), commander.getZ());
        var group = data.createOrReclaimArmyGroup(
                owner.getUUID(), commander.getUUID(), ArmyFormation.LINE,
                anchor, level.getGameTime()).orElse(null);
        if (group == null) {
            helper.fail("Recruitment role test could not create an army group");
            return;
        }

        ProgressionSavedData progression = ProgressionSavedData.get(level);
        for (ServerPlayer actor : List.of(officer, quartermaster, member)) {
            FactionAlignmentSavedData.get(level).setScore(
                    actor.getUUID(), FactionId.of("republic"), 100);
            applyCampaignSetupEvent(
                    progression, actor, ProgressionEventType.FACTION_PLEDGED,
                    "galacticwars:republic");
            actor.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 25));
        }

        GalacticRecruitEntity hostile = helper.spawn(
                ModEntityTypes.B1_BATTLE_DROID.get(), new BlockPos(7, 1, 1));
        hostile.setNoAi(true);
        GalacticRecruitEntity officerHire = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 1));
        officerHire.initializeFromSpawnEgg();
        officer.setPos(officerHire.getX(), officerHire.getY(), officerHire.getZ());
        officerHire.setTarget(hostile);
        BrainUtil.setMemory(
                officerHire,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,
                hostile);
        if (!officerHire.handleMenuButton(officer, RecruitCommandMenu.BUTTON_HIRE)
                || data.armyGroupForRecruit(officerHire.getUUID())
                        .filter(found -> found.id().equals(group.id())).isEmpty()
                || officerHire.getRecruitCommand() != RecruitmentAction.FOLLOW_OWNER
                || officerHire.getTarget() != null
                || BrainUtil.hasMemory(
                        officerHire,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)) {
            helper.fail("Officer recruitment did not join the valid group or clear hostile authority");
            return;
        }

        GalacticRecruitEntity quartermasterHire = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(4, 1, 1));
        quartermasterHire.initializeFromSpawnEgg();
        quartermaster.setPos(
                quartermasterHire.getX(), quartermasterHire.getY(), quartermasterHire.getZ());
        if (!quartermasterHire.handleMenuButton(quartermaster, RecruitCommandMenu.BUTTON_HIRE)
                || data.armyGroupForRecruit(quartermasterHire.getUUID()).isPresent()
                || quartermasterHire.getRecruitCommand() != RecruitmentAction.HOLD_POSITION) {
            helper.fail("Quartermaster recruitment did not begin ungrouped in Hold");
            return;
        }

        GalacticRecruitEntity deniedHire = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(5, 1, 1));
        deniedHire.initializeFromSpawnEgg();
        member.setPos(deniedHire.getX(), deniedHire.getY(), deniedHire.getZ());
        int deniedCredits = RecruitmentPaymentService.creditCount(member);
        ProgressionState deniedProgress = progression.state(member.getUUID());
        int deniedRoster = data.kingdom(kingdom.id()).orElseThrow()
                .settlement().recruitIds().size();
        if (deniedHire.handleMenuButton(member, RecruitCommandMenu.BUTTON_HIRE)
                || RecruitmentPaymentService.creditCount(member) != deniedCredits
                || !progression.state(member.getUUID()).equals(deniedProgress)
                || data.kingdom(kingdom.id()).orElseThrow()
                        .settlement().recruitIds().size() != deniedRoster) {
            helper.fail("Ordinary member recruitment denial mutated credits, progression, or roster");
            return;
        }

        GalacticRecruitEntity unfundedHire = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(6, 1, 1));
        unfundedHire.initializeFromSpawnEgg();
        officer.setPos(unfundedHire.getX(), unfundedHire.getY(), unfundedHire.getZ());
        ProgressionState unfundedProgress = progression.state(officer.getUUID());
        int unfundedRoster = data.kingdom(kingdom.id()).orElseThrow()
                .settlement().recruitIds().size();
        if (unfundedHire.handleMenuButton(officer, RecruitCommandMenu.BUTTON_HIRE)
                || RecruitmentPaymentService.creditCount(officer) != 0
                || !progression.state(officer.getUUID()).equals(unfundedProgress)
                || data.kingdom(kingdom.id()).orElseThrow()
                        .settlement().recruitIds().size() != unfundedRoster) {
            helper.fail("Failed authorized hire mutated credits, progression, or roster");
            return;
        }
        helper.succeed();
    }

    private static void localRecruitProtectOwner(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestArea(
                helper, GameType.SURVIVAL, 0, 4, 0, 3);
        ServerPlayer owner = area.player();
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(1, 1, 1));
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.setOwner(owner);
        recruit.setTame(true, true);
        if (recruit.hasAuthoritativeArmyGroup()
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_PROTECT)) {
            helper.fail("Local recruit protect-order setup failed");
            return;
        }

        GalacticRecruitEntity attacker = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(2, 1, 1));
        ServerPlayer enemyOwner = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        attacker.setOwner(enemyOwner);
        attacker.setTame(true, true);
        attacker.setNoAi(true);
        attacker.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0D);
        attacker.setHealth(attacker.getMaxHealth());
        long chunkWaitStartedTick = helper.getTick();
        int[] scenarioStartedRecruitTick = {-1};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            BlockPos recruitPosition = recruit.blockPosition();
            ChunkPos chunk = new ChunkPos(recruitPosition.getX() >> 4, recruitPosition.getZ() >> 4);
            if (scenarioStartedRecruitTick[0] < 0) {
                boolean areaTicking = helper.getLevel().areEntitiesActuallyLoadedAndTicking(chunk);
                boolean recruitIndexed = helper.getLevel().getEntity(recruit.getUUID()) == recruit;
                boolean attackerIndexed = helper.getLevel().getEntity(attacker.getUUID()) == attacker;
                if (!areaTicking || !recruitIndexed || !attackerIndexed) {
                    if (helper.getTick() - chunkWaitStartedTick >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Local Protect Me area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", attacker=" + attackerIndexed);
                    }
                    return;
                }
                owner.setLastHurtByMob(attacker);
                scenarioStartedRecruitTick[0] = recruit.tickCount;
                return;
            }
            if (recruit.getTarget() == attacker) {
                complete[0] = true;
                recruit.discard();
                attacker.discard();
                helper.succeed();
                return;
            }
            if (recruit.tickCount - scenarioStartedRecruitTick[0] >= 120) {
                complete[0] = true;
                helper.fail("Local Protect Me order did not target the owner's attacker: ownerThreat="
                        + (owner.getLastHurtByMob() == attacker)
                        + ", timestamp=" + owner.getLastHurtByMobTimestamp()
                        + ", attackerAlive=" + attacker.isAlive()
                        + ", wantsToAttack=" + recruit.wantsToAttack(attacker, owner)
                        + ", command=" + recruit.getRecruitCommand()
                        + ", recruitTicks="
                        + (recruit.tickCount - scenarioStartedRecruitTick[0]));
            }
        });
    }

    /**
     * Proves that ordinary embodied army commands retain their persisted
     * authority while the sole SmartBrain navigation path opens and closes a
     * claimed door. The recruit is never moved after the first command.
     */
    private static void recruitDoorCommandResumption(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                18,
                -2,
                10,
                isolatedCapital(helper, 211));
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(0, 1, 4);
        BlockPos doorPos = area.at(6, 1, 4);
        BlockPos followTarget = area.at(14, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        for (int z = -2; z <= 10; z++) {
            for (int y = 1; y <= 3; y++) {
                helper.getLevel().setBlock(
                        area.at(6, y, z),
                        Blocks.STONE.defaultBlockState(),
                        3);
            }
        }
        var lowerDoor = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        var upperDoor = lowerDoor.setValue(
                BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        helper.getLevel().setBlock(doorPos, lowerDoor, 3);
        helper.getLevel().setBlock(doorPos.above(), upperDoor, 3);

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        recruit.setInvulnerable(true);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Door-command recruit could not be hired");
            return;
        }
        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data,
                owner.getUUID(),
                forwardBase,
                helper.getLevel().dimension().identifier().toString(),
                hallPos.offset(24, 0, 0));
        if (!data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)
                || !recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_FOLLOW)) {
            helper.fail("Door-command recruit could not become a following commander");
            return;
        }
        var group = data.armyGroupForRecruit(recruit.getUUID()).orElse(null);
        if (group == null || recruit.getArmyGroupId() == null
                || !recruit.getArmyGroupId().equals(group.id())) {
            helper.fail("Door-command commander lacks an authoritative army group");
            return;
        }

        owner.setNoGravity(true);
        owner.setPos(
                followTarget.getX() + 0.5D,
                followTarget.getY(),
                followTarget.getZ() + 0.5D);
        helper.getLevel().getChunkSource().move(owner);
        UUID groupId = group.id();
        int[] phase = {0};
        int[] phaseStartedRecruitTick = {recruit.tickCount};
        Vec3[] heldPosition = {Vec3.ZERO};
        boolean[] followDoorOpened = {false};
        boolean[] followCrossedDoor = {false};
        boolean[] followDoorClosed = {false};
        boolean[] patrolDoorOpened = {false};
        boolean[] patrolCrossedDoor = {false};
        boolean[] patrolDoorClosed = {false};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            var liveDoor = helper.getLevel().getBlockState(doorPos);
            boolean doorOpen = liveDoor.hasProperty(BlockStateProperties.OPEN)
                    && liveDoor.getValue(BlockStateProperties.OPEN);
            var currentGroup = data.armyGroup(groupId).orElse(null);
            if (currentGroup == null) {
                complete[0] = true;
                helper.fail("Door-command army group disappeared");
                return;
            }

            if (phase[0] == 0) {
                followDoorOpened[0] |= doorOpen;
                followCrossedDoor[0] |= recruit.getX() > doorPos.getX() + 3.0D
                        && recruit.distanceToSqr(
                                doorPos.getX() + 0.5D,
                                doorPos.getY() + 0.5D,
                                doorPos.getZ() + 0.5D) > 12.25D;
                followDoorClosed[0] |= followDoorOpened[0]
                        && followCrossedDoor[0]
                        && !doorOpen;
                if (followDoorOpened[0]
                        && followCrossedDoor[0]
                        && recruit.getRecruitCommand() == RecruitmentAction.FOLLOW_OWNER
                        && currentGroup.order().type() == ArmyCommandType.FOLLOW_OWNER) {
                    owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
                    helper.getLevel().getChunkSource().move(owner);
                    if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HOLD)) {
                        complete[0] = true;
                        helper.fail("Follow-through-door did not accept Hold");
                        return;
                    }
                    heldPosition[0] = recruit.position();
                    phaseStartedRecruitTick[0] = recruit.tickCount;
                    phase[0] = 1;
                    return;
                }
                if (recruit.tickCount - phaseStartedRecruitTick[0] >= 360) {
                    complete[0] = true;
                    helper.fail("Follow command did not complete its claimed-door cycle: position="
                            + recruit.position() + ", command=" + recruit.getRecruitCommand()
                            + ", order=" + currentGroup.order()
                            + ", opened=" + followDoorOpened[0]
                            + ", crossed=" + followCrossedDoor[0]
                            + ", closed=" + followDoorClosed[0]
                            + ", path=" + pathState(recruit, followTarget));
                }
                return;
            }

            if (phase[0] == 1) {
                followDoorClosed[0] |= !doorOpen;
                int holdTicks = recruit.tickCount - phaseStartedRecruitTick[0];
                boolean walkTargetCleared = !BrainUtil.hasMemory(
                        recruit,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
                if (holdTicks >= 20
                        && followDoorClosed[0]
                        && recruit.getNavigation().isDone()
                        && walkTargetCleared
                        && recruit.isOrderedToSit()
                        && recruit.getRecruitCommand() == RecruitmentAction.HOLD_POSITION
                        && currentGroup.order().type() == ArmyCommandType.HOLD_POSITION) {
                    heldPosition[0] = recruit.position();
                    phaseStartedRecruitTick[0] = recruit.tickCount;
                    phase[0] = 2;
                    return;
                }
                if (holdTicks >= 120) {
                    complete[0] = true;
                    helper.fail("Hold did not stop navigation and close the traversed door: position="
                            + recruit.position() + ", held=" + heldPosition[0]
                            + ", command=" + recruit.getRecruitCommand()
                            + ", order=" + currentGroup.order()
                            + ", doorClosed=" + followDoorClosed[0]
                            + ", orderedSit=" + recruit.isOrderedToSit()
                            + ", navigationDone=" + recruit.getNavigation().isDone()
                            + ", walkTargetCleared=" + walkTargetCleared);
                }
                return;
            }

            if (phase[0] == 2) {
                if (recruit.tickCount - phaseStartedRecruitTick[0] < 20) {
                    return;
                }
                double heldDeltaX = recruit.getX() - heldPosition[0].x();
                double heldDeltaZ = recruit.getZ() - heldPosition[0].z();
                if (!recruit.isOrderedToSit()
                        || recruit.getRecruitCommand() != RecruitmentAction.HOLD_POSITION
                        || currentGroup.order().type() != ArmyCommandType.HOLD_POSITION
                        || heldDeltaX * heldDeltaX + heldDeltaZ * heldDeltaZ > 0.25D) {
                    complete[0] = true;
                    helper.fail("Hold did not remain stationary after door traversal: position="
                            + recruit.position() + ", held=" + heldPosition[0]
                            + ", command=" + recruit.getRecruitCommand()
                            + ", order=" + currentGroup.order()
                            + ", orderedSit=" + recruit.isOrderedToSit());
                    return;
                }
                if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_PATROL)) {
                    complete[0] = true;
                    helper.fail("Held commander could not start a persisted patrol");
                    return;
                }
                phaseStartedRecruitTick[0] = recruit.tickCount;
                phase[0] = 3;
                return;
            }

            patrolDoorOpened[0] |= doorOpen;
            patrolCrossedDoor[0] |= recruit.getX() < doorPos.getX() - 2.0D
                    && recruit.distanceToSqr(
                            doorPos.getX() + 0.5D,
                            doorPos.getY() + 0.5D,
                            doorPos.getZ() + 0.5D) > 12.25D;
            patrolDoorClosed[0] |= patrolDoorOpened[0]
                    && patrolCrossedDoor[0]
                    && !doorOpen;
            if (patrolDoorClosed[0]
                    && recruit.getRecruitCommand() == RecruitmentAction.PATROL_ROUTE
                    && currentGroup.order().type() == ArmyCommandType.PATROL_ROUTE
                    && currentGroup.effectivePatrolPlan().isPresent()) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }
            if (recruit.tickCount - phaseStartedRecruitTick[0] >= 420) {
                complete[0] = true;
                var activePath = recruit.getNavigation().getPath();
                helper.fail("Patrol command did not resume through the claimed door: position="
                        + recruit.position() + ", command=" + recruit.getRecruitCommand()
                        + ", order=" + currentGroup.order()
                        + ", plan=" + currentGroup.effectivePatrolPlan().orElse(null)
                        + ", opened=" + patrolDoorOpened[0]
                        + ", crossed=" + patrolCrossedDoor[0]
                        + ", closed=" + patrolDoorClosed[0]
                        + ", door=" + helper.getLevel().getBlockState(doorPos)
                        + ", upperDoor=" + helper.getLevel().getBlockState(doorPos.above())
                        + ", horizontalCollision=" + recruit.horizontalCollision
                        + ", orderedSit=" + recruit.isOrderedToSit()
                        + ", velocity=" + recruit.getDeltaMovement()
                        + ", moveWanted=" + recruit.getMoveControl().hasWanted()
                        + ", wanted=(" + recruit.getMoveControl().getWantedX()
                        + "," + recruit.getMoveControl().getWantedY()
                        + "," + recruit.getMoveControl().getWantedZ() + ")"
                        + ", activePath=" + (activePath == null ? "none"
                                : "nodes=" + activePath.getNodeCount()
                                + ", index=" + activePath.getNextNodeIndex()
                                + ", next=" + activePath.getNextNodePos()
                                + ", target=" + activePath.getTarget())
                        + ", path=" + pathState(
                                recruit, recruitPos));
            }
        });
    }

    /**
     * Exercises the ordinary survival path without setting worker phases, invoking
     * the controller, or moving the recruit after assignment.
     */
    private static void blackBoxFarmerDoorLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                10,
                isolatedCapital(helper, 210));
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos doorPos = area.at(6, 1, 4);
        BlockPos cropPos = area.at(10, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        for (int z = -2; z <= 10; z++) {
            for (int y = 1; y <= 3; y++) {
                helper.getLevel().setBlock(
                        area.at(6, y, z),
                        Blocks.STONE.defaultBlockState(),
                        3);
            }
        }
        var lowerDoor = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        var upperDoor = lowerDoor.setValue(
                BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        helper.getLevel().setBlock(doorPos, lowerDoor, 3);
        helper.getLevel().setBlock(doorPos.above(), upperDoor, 3);
        helper.getLevel().setBlock(
                cropPos.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                cropPos,
                Blocks.WHEAT.defaultBlockState()
                        .setValue(BlockStateProperties.AGE_7, 7),
                3);
        putContainerItem(hall, new ItemStack(Items.WHEAT_SEEDS));

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper,
                ModEntityTypes.CLONE_TROOPER.get(),
                recruitPos);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_FARMER)) {
            helper.fail("Black-box farmer could not be hired and assigned");
            return;
        }
        WorksiteRecord worksite = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElse(null);
        StorageEndpoint hallStorage = data.registeredStorageEndpoint(
                owner.getUUID(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElse(null);
        if (worksite == null || hallStorage == null) {
            helper.fail("Black-box farmer lacks authoritative worksite or storage");
            return;
        }
        var configured = data.configureWorksite(
                owner.getUUID(),
                worksite.id(),
                worksite.configuration().revision(),
                new WorkAreaBounds(25, 7, 17),
                true,
                100,
                true,
                List.of("minecraft:wheat"),
                CourierDispatchMode.AUTOMATIC);
        if (!configured.accepted()) {
            helper.fail("Black-box farmer bounds were rejected: "
                    + configured.reasonCode());
            return;
        }
        WorksiteRecord configuredWorksite = configured.worksite().orElseThrow();
        var storageConfigured = data.configureWorksiteStorage(
                owner.getUUID(),
                configuredWorksite.id(),
                configuredWorksite.configuration().revision(),
                hallStorage);
        if (!storageConfigured.accepted()) {
            helper.fail("Black-box farmer storage was rejected: "
                    + storageConfigured.reasonCode());
            return;
        }

        int toolDamageBefore = recruit.getWorkerMainHandItem().getDamageValue();
        boolean[] doorOpened = {false};
        boolean[] doorClosedAfterPassage = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 10);
                boolean recruitIndexed = helper.getLevel().getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box farmer area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }
            var liveDoor = helper.getLevel().getBlockState(doorPos);
            if (liveDoor.hasProperty(BlockStateProperties.OPEN)
                    && liveDoor.getValue(BlockStateProperties.OPEN)) {
                doorOpened[0] = true;
            } else if (doorOpened[0]
                    && recruit.distanceToSqr(
                            doorPos.getX() + 0.5D,
                            doorPos.getY(),
                            doorPos.getZ() + 0.5D) > 9.0D) {
                doorClosedAfterPassage[0] = true;
            }
            boolean cropReplanted = helper.getLevel().getBlockState(cropPos)
                    .hasProperty(BlockStateProperties.AGE_7)
                    && helper.getLevel().getBlockState(cropPos)
                            .getValue(BlockStateProperties.AGE_7) == 0;
            boolean deposited = countContainerItem(hall, Items.WHEAT) > 0
                    && countContainerItem(hall, Items.WHEAT_SEEDS) > 0;
            boolean completedOrder = data.kingdomForOwner(owner.getUUID())
                    .orElseThrow()
                    .settlement()
                    .workOrders()
                    .stream()
                    .anyMatch(order -> order.type() == WorkOrderType.FARM
                            && order.state() == WorkOrderState.COMPLETED);
            if (cropReplanted
                    && deposited
                    && completedOrder
                    && doorOpened[0]
                    && doorClosedAfterPassage[0]
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE
                    && recruit.getWorkerMainHandItem().getDamageValue()
                            > toolDamageBefore) {
                complete[0] = true;
                helper.succeed();
                return;
            }
            if (recruit.tickCount - startedRecruitTick[0] >= 800) {
                complete[0] = true;
                helper.fail("Black-box farmer lifecycle timed out: status="
                        + recruit.getWorkerStatus()
                        + ", position=" + recruit.blockPosition()
                        + ", cropReplanted=" + cropReplanted
                        + ", deposited=" + deposited
                        + ", completedOrder=" + completedOrder
                        + ", command=" + recruit.getRecruitCommand()
                        + ", doorOpened=" + doorOpened[0]
                        + ", doorClosed=" + doorClosedAfterPassage[0]);
            }
        });
    }

    private static void workerResourceConservation(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 11);
        BlockPos cropPos = hallPos.offset(3, 0, 0);
        BlockPos chestPos = hallPos.offset(4, 0, 0);
        helper.getLevel().setBlock(cropPos.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                cropPos, Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7), 3);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        Container chest = (Container) helper.getLevel().getBlockEntity(chestPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.SURVIVAL);
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 1));
        recruit.setPos(hallPos.getX() + 2.5, hallPos.getY(), hallPos.getZ() + 0.5);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        owner.getInventory().add(new ItemStack(galacticwars.clonewars.registry.ModItems.CREDIT_CHIP.get(), 45));
        boolean hired = recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE);
        boolean assignedFarmer = recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_FARMER);
        if (!hired || !assignedFarmer) {
            helper.fail("Farmer setup failed: hired=" + hired
                    + ", assigned=" + assignedFarmer
                    + ", credits=" + RecruitmentPaymentService.creditCount(owner)
                    + ", owned=" + recruit.isOwnedBy(owner));
        }

        setRecruitField(recruit, "workTarget", cropPos);
        setRecruitField(recruit, "storageTarget", chestPos);
        setWorkerInventory(recruit, new ItemStack(Items.WHEAT_SEEDS));
        invokeRecruitCommand(recruit, RecruitmentAction.WORK_AT_SITE);
        recruit.setPos(cropPos.getX() + 0.5, cropPos.getY(), cropPos.getZ() + 0.5);
        setRecruitField(recruit, "workerPhase", WorkerPhase.INTERACT);
        setRecruitField(recruit, "workerReason", "navigate_work_target");
        setRecruitField(recruit, "activeWorkTarget", cropPos);
        int toolDamageBefore = recruit.getMainHandItem().getDamageValue();
        recruit.tickWorkerController();
        if (helper.getLevel().getBlockState(cropPos).getValue(BlockStateProperties.AGE_7) != 0
                || recruit.getMainHandItem().getDamageValue() != toolDamageBefore + 1
                || workerInventoryCount(recruit) <= 0) {
            helper.fail("Farmer harvesting did not replant, wear its tool, and conserve drops");
        }

        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            chest.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        int carriedBeforeFailedDeposit = workerInventoryCount(recruit);
        recruit.setPos(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5);
        setRecruitField(recruit, "workerPhase", WorkerPhase.DEPOSIT);
        setRecruitField(recruit, "workerReason", "deposit_inventory");
        setRecruitField(recruit, "activeWorkTarget", chestPos);
        recruit.tickWorkerController();
        if (!recruit.getWorkerStatus().reasonCode().equals("storage_full_or_missing")
                || workerInventoryCount(recruit) != carriedBeforeFailedDeposit) {
            helper.fail("Failed deposit consumed worker resources");
        }

        AABB deathArea = recruit.getBoundingBox().inflate(4.0);
        recruit.die(helper.getLevel().damageSources().generic());
        helper.succeedWhen(() -> {
            int droppedWorkerItems = helper.getLevel().getEntitiesOfClass(
                            ItemEntity.class, deathArea)
                    .stream()
                    .filter(item -> item.getItem().is(Items.WHEAT) || item.getItem().is(Items.WHEAT_SEEDS))
                    .mapToInt(item -> item.getItem().getCount())
                    .sum();
            if (droppedWorkerItems != carriedBeforeFailedDeposit
                    || data.kingdomForOwner(owner.getUUID()).orElseThrow()
                            .settlement().containsRecruit(recruit.getUUID())) {
                helper.fail("Worker death did not conserve carried resources and release housing");
            }
        });
    }

    private static void recruitHazardAndSelfCare(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper, GameType.SURVIVAL, -4, 24, -4, 8,
                isolatedCapital(helper, 151));
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(8, 1, 6);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");

        BlockPos hazardPosition = area.at(2, 1, 2);
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.getLevel().setBlockAndUpdate(
                        area.at(x, 0, z),
                        Blocks.CAMPFIRE.defaultBlockState()
                                .setValue(BlockStateProperties.LIT, false));
            }
        }
        GalacticRecruitEntity escaping = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), hazardPosition);
        escaping.initializeFromSpawnEgg();
        escaping.tame(owner);
        escaping.setInvulnerable(true);
        invokeRecruitCommand(escaping, RecruitmentAction.HOLD_POSITION);
        if (escaping.isInRecruitHazard()) {
            helper.fail("Unlit campfire field incorrectly activated recruit hazard avoidance");
            return;
        }
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.getLevel().setBlockAndUpdate(
                        area.at(x, 0, z),
                        Blocks.CAMPFIRE.defaultBlockState()
                                .setValue(BlockStateProperties.LIT, true));
            }
        }

        GalacticRecruitEntity following = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(10, 1, 6));
        GalacticRecruitEntity holding = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(12, 1, 6));
        GalacticRecruitEntity patrolling = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(16, 1, 6));
        GalacticRecruitEntity combat = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(22, 1, 6));
        for (GalacticRecruitEntity recruit : List.of(
                following, holding, patrolling, combat)) {
            recruit.initializeFromSpawnEgg();
            recruit.setInvulnerable(true);
        }
        for (GalacticRecruitEntity recruit : List.of(following, holding, combat)) {
            recruit.tame(owner);
        }
        invokeRecruitCommand(following, RecruitmentAction.FOLLOW_OWNER);
        invokeRecruitCommand(holding, RecruitmentAction.HOLD_POSITION);

        KingdomBaseBlueprint forwardBase = GameplayDataManager.snapshot()
                .blueprint("galacticwars:forward_base").orElseThrow();
        BuildProject forwardBaseProject = fullyProgressProject(
                data,
                owner.getUUID(),
                forwardBase,
                helper.getLevel().dimension().identifier().toString(),
                hallPos.offset(24, 0, 0));
        owner.setPos(patrolling.getX(), patrolling.getY(), patrolling.getZ());
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 25));
        if (!patrolling.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !data.completeBuildProject(owner.getUUID(), forwardBaseProject, forwardBase)
                || !patrolling.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER)) {
            helper.fail("Self-care patrol fixture could not hire and promote its commander");
            return;
        }
        owner.setPos(
                area.at(20, 1, 6).getX() + 0.5D,
                area.at(20, 1, 6).getY(),
                area.at(20, 1, 6).getZ() + 0.5D);
        helper.getLevel().getChunkSource().move(owner);
        if (!patrolling.handleMenuButton(owner, RecruitCommandMenu.BUTTON_PATROL)) {
            helper.fail("Self-care patrol fixture could not persist a two-waypoint route");
            return;
        }
        var patrolGroup = data.armyGroupForRecruit(patrolling.getUUID()).orElse(null);
        if (patrolGroup == null
                || patrolGroup.order().type() != ArmyCommandType.PATROL_ROUTE
                || patrolGroup.effectivePatrolPlan().isEmpty()
                || patrolling.getRecruitCommand() != RecruitmentAction.PATROL_ROUTE) {
            helper.fail("Self-care patrol fixture lacks an authoritative persisted patrol");
            return;
        }

        for (GalacticRecruitEntity recruit : List.of(
                escaping, following, holding, patrolling, combat)) {
            setRecruitField(recruit, "hunger", 0);
            setWorkerInventory(recruit, new ItemStack(Items.DRIED_KELP, 3));
        }
        invokeRecruitCommand(combat, RecruitmentAction.FOLLOW_OWNER);
        BrainUtil.setMemory(
                combat,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,
                owner);
        combat.setTarget(null);
        if (combat.shouldUseRecruitSelfCare()) {
            helper.fail("Attack memory alone did not preempt recruit self-care");
            return;
        }
        combat.performRecruitSelfCare();
        if (workerInventoryCount(combat) != 3
                || combat.getRecruitCommand() != RecruitmentAction.FOLLOW_OWNER) {
            helper.fail("Attack-memory self-care control consumed food or changed Follow");
            return;
        }
        BrainUtil.clearMemory(
                combat,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        combat.discard();

        int[] startTick = {following.tickCount};
        boolean[] escapeTargetObserved = {false};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (escaping.isHazardAvoidanceActive()) {
                if (workerInventoryCount(escaping) != 3) {
                    complete[0] = true;
                    helper.fail("Hazard escape consumed physical food while safety owned the tick");
                    return;
                }
                if (escaping.isInRecruitHazard()) {
                    if (!BrainUtil.hasMemory(
                            escaping,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)) {
                        complete[0] = true;
                        helper.fail("Ordered sit cleared the active hazard escape walk target");
                        return;
                    }
                    escapeTargetObserved[0] = true;
                }
            }

            int followFood = workerInventoryCount(following);
            int holdFood = workerInventoryCount(holding);
            int patrolFood = workerInventoryCount(patrolling);
            if (followFood < 2 || holdFood < 2 || patrolFood < 2) {
                complete[0] = true;
                helper.fail("Ordinary-command self-care consumed more than one item per cadence: follow="
                        + followFood + ", hold=" + holdFood + ", patrol=" + patrolFood);
                return;
            }

            var currentPatrolGroup = data.armyGroup(patrolGroup.id()).orElse(null);
            boolean commandsRetained = following.getRecruitCommand()
                    == RecruitmentAction.FOLLOW_OWNER
                    && holding.getRecruitCommand() == RecruitmentAction.HOLD_POSITION
                    && holding.isOrderedToSit()
                    && patrolling.getRecruitCommand() == RecruitmentAction.PATROL_ROUTE
                    && currentPatrolGroup != null
                    && currentPatrolGroup.order().type() == ArmyCommandType.PATROL_ROUTE
                    && currentPatrolGroup.effectivePatrolPlan().isPresent();
            if (followFood == 2 && holdFood == 2 && patrolFood == 2
                    && escapeTargetObserved[0] && commandsRetained) {
                complete[0] = true;
                for (GalacticRecruitEntity recruit : List.of(
                        escaping, following, holding, patrolling)) {
                    recruit.discard();
                }
                helper.succeed();
                return;
            }
            int elapsedTicks = following.tickCount - startTick[0];
            if (elapsedTicks >= 60) {
                complete[0] = true;
                helper.fail("Command-preserving self-care cadence was not observed: follow="
                        + followFood + ", hold=" + holdFood + ", patrol=" + patrolFood
                        + ", hazard=" + workerInventoryCount(escaping)
                        + ", escape=" + escapeTargetObserved[0]
                        + ", commands=" + following.getRecruitCommand() + "/"
                        + holding.getRecruitCommand() + "/" + patrolling.getRecruitCommand()
                        + ", patrolGroup=" + currentPatrolGroup);
            }
        });
    }

    private static void workerSafetyAndUpkeep(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper, GameType.SURVIVAL, -4, 12, -4, 6,
                isolatedCapital(helper, 150));
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(8, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setItem(0, new ItemStack(ModItems.CREDIT_CHIP.get(), 4));
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 45));

        GalacticRecruitEntity worker = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(2, 1, 2));
        owner.setPos(worker.getX(), worker.getY(), worker.getZ());
        if (!worker.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !worker.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_FARMER)) {
            helper.fail("Worker safety setup could not hire and assign its worker");
            return;
        }
        setWorkerInventory(worker, new ItemStack(Items.DIAMOND, 3));
        invokeRecruitCommand(worker, RecruitmentAction.WORK_AT_SITE);

        GalacticRecruitEntity threat = spawnRecruitAt(
                helper, ModEntityTypes.B1_BATTLE_DROID.get(), area.at(3, 1, 2));
        threat.setNoAi(true);

        GalacticRecruitEntity[] activeWorker = {worker};
        CompoundTag[] persistedWorker = {null};
        int[] phase = {-1};
        long[] phaseStartGameTick = {helper.getTick()};
        int[] phaseStartRecruitTick = {worker.tickCount};
        boolean[] selfCareBlockedDuringSafety = {false};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            GalacticRecruitEntity current = activeWorker[0];
            if (phase[0] == -1) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -4, 12, -4, 6);
                boolean workerIndexed = helper.getLevel().getEntity(worker.getUUID()) == worker;
                boolean threatIndexed = helper.getLevel().getEntity(threat.getUUID()) == threat;
                if (!areaTicking || !workerIndexed || !threatIndexed || worker.tickCount == 0) {
                    if (helper.getTick() - phaseStartGameTick[0]
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Worker safety area never became entity-ticking: ticking="
                                + areaTicking + ", worker=" + workerIndexed
                                + ", threat=" + threatIndexed
                                + ", workerTick=" + worker.tickCount);
                    }
                    return;
                }
                BrainUtil.setMemory(
                        worker,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                        new net.minecraft.world.entity.ai.memory.WalkTarget(
                                area.at(10, 1, 2), 1.0F, 1));
                if (!worker.hurtServer(
                        helper.getLevel(),
                        helper.getLevel().damageSources().mobAttack(threat),
                        1.0F)) {
                    complete[0] = true;
                    helper.fail("Worker safety setup damage was not accepted");
                    return;
                }
                phase[0] = 0;
                phaseStartGameTick[0] = helper.getTick();
                phaseStartRecruitTick[0] = worker.tickCount;
                return;
            }
            if (phase[0] == 0) {
                if (current.isWorkerSafetyRetreating()) {
                    if (workerInventoryCount(current) != 3
                            || BrainUtil.hasMemory(
                            current,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            || current.getWorkerStatus().target()
                            .filter(target -> target.x() == hallPos.getX()
                                    && target.y() == hallPos.getY()
                                    && target.z() == hallPos.getZ())
                            .isEmpty()) {
                        complete[0] = true;
                        helper.fail("Threatened worker did not retreat to its loaded Command Center "
                                + "without mutating cargo: status=" + current.getWorkerStatus()
                                + ", cargo=" + workerInventoryCount(current));
                        return;
                    }
                    persistedWorker[0] = saveRecruit(current, helper.getLevel());
                    current.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                    threat.discard();
                    if (!current.isRemoved()) {
                        complete[0] = true;
                        helper.fail("Threatened worker did not cross a real unload boundary");
                        return;
                    }
                    phase[0] = 1;
                    phaseStartGameTick[0] = helper.getTick();
                    return;
                }
                if (current.tickCount - phaseStartRecruitTick[0] >= 80) {
                    complete[0] = true;
                    helper.fail("Damaged worker never entered its safety retreat: status="
                            + current.getWorkerStatus() + ", hurtTime=" + current.hurtTime
                            + ", rememberedThreat=" + BrainUtil.hasMemory(
                            current,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY_ENTITY));
                }
                return;
            }

            if (phase[0] == 1) {
                GalacticRecruitEntity loaded = loadRecruit(
                        persistedWorker[0], helper.getLevel());
                if (!loaded.getUUID().equals(worker.getUUID())
                        || workerInventoryCount(loaded) != 3) {
                    complete[0] = true;
                    helper.fail("Threatened worker identity or cargo did not decode from its save: id="
                            + loaded.getUUID() + ", cargo=" + workerInventoryCount(loaded));
                    return;
                }
                if (!helper.getLevel().addFreshEntity(loaded)) {
                    complete[0] = true;
                    helper.fail("Threatened worker could not be restored after unload");
                    return;
                }
                activeWorker[0] = loaded;
                phase[0] = 2;
                phaseStartGameTick[0] = helper.getTick();
                phaseStartRecruitTick[0] = loaded.tickCount;
                return;
            }

            if (helper.getLevel().getEntity(current.getUUID()) != current) {
                if (helper.getTick() - phaseStartGameTick[0] >= 40L) {
                    complete[0] = true;
                    helper.fail("Reloaded worker never entered the server entity index");
                }
                return;
            }

            if (!selfCareBlockedDuringSafety[0]
                    && current.isWorkerSafetyRetreating()
                    && current.hurtTime == 0
                    && current.getTarget() == null) {
                setRecruitField(current, "hunger", 0);
                setWorkerInventory(current, new ItemStack(Items.BREAD, 3));
                if (current.shouldUseRecruitSelfCare()) {
                    complete[0] = true;
                    helper.fail("Loaded worker safety retreat did not preempt self-care");
                    return;
                }
                current.performRecruitSelfCare();
                if (workerInventoryCount(current) != 3) {
                    complete[0] = true;
                    helper.fail("Loaded worker safety retreat consumed physical food");
                    return;
                }
                setRecruitField(current, "hunger", 100);
                setWorkerInventory(current, new ItemStack(Items.DIAMOND, 3));
                selfCareBlockedDuringSafety[0] = true;
            }
            if (workerInventoryCount(current) != 3) {
                complete[0] = true;
                helper.fail("Worker cargo changed after safety save/reload: cargo="
                        + workerInventoryCount(current) + ", status=" + current.getWorkerStatus()
                        + ", running=" + current.getBrain().getRunningBehaviors());
                return;
            }
            if (current.getWorkerStatus().phase() == WorkerPhase.ACQUIRE_ORDER
                    && current.getWorkerStatus().reasonCode().equals("threat_cleared")) {
                if (!selfCareBlockedDuringSafety[0]) {
                    complete[0] = true;
                    helper.fail("Reloaded worker left safety before the self-care control ran");
                    return;
                }
                long upkeepTime = helper.getLevel().getGameTime();
                hall.clearContent();
                hall.chargeDailyUpkeep(upkeepTime, 1);
                if (hall.chargeDailyUpkeep(upkeepTime + 24000L, 1)) {
                    complete[0] = true;
                    helper.fail("Empty Command Center unexpectedly paid worker upkeep");
                    return;
                }
                BrainUtil.setMemory(
                        current,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                        new net.minecraft.world.entity.ai.memory.WalkTarget(
                                area.at(10, 1, 2), 1.0F, 1));
                invokeWorkerAuthorityReconciliation(current, helper.getLevel());
                if (current.getWorkerStatus().phase() != WorkerPhase.PAUSED
                        || !current.getWorkerStatus().reasonCode().equals("upkeep_unpaid")
                        || BrainUtil.hasMemory(
                        current,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)) {
                    complete[0] = true;
                    helper.fail("Unpaid upkeep did not immediately pause worker movement: "
                            + current.getWorkerStatus());
                    return;
                }
                hall.setItem(0, new ItemStack(ModItems.CREDIT_CHIP.get(), 64));
                if (!hall.chargeDailyUpkeep(upkeepTime + 24000L, 1)) {
                    complete[0] = true;
                    helper.fail("Restored treasury did not pay pending worker upkeep");
                    return;
                }
                invokeWorkerAuthorityReconciliation(current, helper.getLevel());
                if (current.getWorkerStatus().phase() != WorkerPhase.ACQUIRE_ORDER
                        || !current.getWorkerStatus().reasonCode().equals("upkeep_restored")
                        || workerInventoryCount(current) != 3) {
                    complete[0] = true;
                    helper.fail("Restored upkeep did not resume from authoritative order acquisition");
                    return;
                }
                complete[0] = true;
                current.discard();
                helper.succeed();
                return;
            }
            if (current.tickCount - phaseStartRecruitTick[0] >= 200) {
                complete[0] = true;
                helper.fail("Reloaded worker did not resume after 100 threat-free ticks: "
                        + current.getWorkerStatus() + ", selfCareBlocked="
                        + selfCareBlockedDuringSafety[0]);
            }
        });
    }

    private static void enabledWorkerLoops(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 12);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 1));
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        data.activateHall(owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos).orElseThrow();
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Enabled worker loop recruit could not be hired");
        }

        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_FARMER)) {
            helper.fail("Farmer contract was rejected");
        }
        BlockPos cropPos = hallPos.offset(3, 0, 0);
        helper.getLevel().setBlock(cropPos.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        helper.getLevel().setBlock(cropPos,
                Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7), 3);
        setWorkerInventory(recruit, new ItemStack(Items.WHEAT_SEEDS));
        UUID farmerOrder = acquirePersistedOrder(recruit);
        interactWorkerAt(recruit, cropPos, "navigate_work_target");
        depositWorkerAt(recruit, hallPos);
        assertCompletedOrder(helper, data, owner.getUUID(), farmerOrder, "farmer");
        if (helper.getLevel().getBlockState(cropPos).getValue(BlockStateProperties.AGE_7) != 0) {
            helper.fail("Farmer loop did not replant its crop");
        }

        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_LUMBERJACK)) {
            helper.fail("Lumberjack contract was rejected");
        }
        BlockPos logPos = hallPos.offset(4, 0, 2);
        helper.getLevel().setBlock(logPos.below(), Blocks.DIRT.defaultBlockState(), 3);
        helper.getLevel().setBlock(logPos, Blocks.OAK_LOG.defaultBlockState(), 3);
        setWorkerInventory(recruit, new ItemStack(Items.OAK_SAPLING));
        UUID lumberOrder = acquirePersistedOrder(recruit);
        interactWorkerAt(recruit, logPos, "navigate_work_target");
        depositWorkerAt(recruit, hallPos);
        assertCompletedOrder(helper, data, owner.getUUID(), lumberOrder, "lumberjack");
        if (!helper.getLevel().getBlockState(logPos).is(Blocks.OAK_SAPLING)) {
            helper.fail("Lumberjack loop did not replant its sapling");
        }

        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)) {
            helper.fail("Miner contract was rejected");
        }
        BlockPos orePos = hallPos.offset(5, 0, 2);
        helper.getLevel().setBlock(orePos, Blocks.IRON_ORE.defaultBlockState(), 3);
        setWorkerInventory(recruit, ItemStack.EMPTY);
        UUID minerOrder = acquirePersistedOrder(recruit);
        interactWorkerAt(recruit, orePos, "navigate_work_target");
        depositWorkerAt(recruit, hallPos);
        assertCompletedOrder(helper, data, owner.getUUID(), minerOrder, "miner");
        if (!helper.getLevel().getBlockState(orePos).isAir()) {
            helper.fail("Miner loop did not remove its authorized block");
        }

        KingdomBaseBlueprint barracks = GameplayDataManager.snapshot()
                .blueprint("galacticwars:barracks").orElseThrow();
        // Keep multi-block projects above this test's horizontal cell. GameTests sharing the
        // gameplay environment run in parallel, so building beyond the tiny empty template in X/Z
        // can collide with a neighboring test and produce a nondeterministic blocked placement.
        BlockPos barracksOrigin = hallPos.offset(0, 16, 0);
        BuildProject barracksProject = data.startBuildProject(
                owner.getUUID(), barracks, helper.getLevel().dimension().identifier().toString(),
                barracksOrigin, 0).orElseThrow();
        setRecruitField(recruit, "activeBuildProjectId", barracksProject.id());
        setRecruitField(recruit, "baseTarget", barracksOrigin);
        setRecruitField(recruit, "workTarget", barracksOrigin);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_BUILDER)) {
            helper.fail("Builder contract was rejected");
        }
        putContainerItem(hall, new ItemStack(Items.OAK_PLANKS, 64));
        putContainerItem(hall, new ItemStack(Items.OAK_LOG, 64));
        UUID builderOrder = null;
        for (int placementIndex = 0; placementIndex < barracks.placements().size(); placementIndex++) {
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            if (builderOrder == null) {
                builderOrder = (UUID) getRecruitField(recruit, "workOrderId");
            }
            if (!recruit.getWorkerStatus().reasonCode().equals("withdraw_build_material")) {
                BuildProject current = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                        .buildProjects().stream().filter(project -> project.id().equals(barracksProject.id()))
                        .findFirst().orElseThrow();
                helper.fail("Builder did not request its next material at placement " + placementIndex
                        + "; reason=" + recruit.getWorkerStatus().reasonCode()
                        + ", placed=" + current.completedPlacements().size()
                        + ", inventory=" + workerInventoryCount(recruit));
            }
            interactWorkerAt(recruit, hallPos, "withdraw_build_material");
            setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
            recruit.tickWorkerController();
            BuildProject current = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                    .buildProjects().stream().filter(project -> project.id().equals(barracksProject.id()))
                    .findFirst().orElseThrow();
            var placement = barracks.rotatedPlacement(placementIndex, current.rotationSteps());
            BlockPos placementPos = barracksOrigin.offset(placement.x(), placement.y(), placement.z());
            interactWorkerAt(recruit, placementPos, "build_place");
        }
        setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
        recruit.tickWorkerController();
        assertCompletedOrder(helper, data, owner.getUUID(), builderOrder, "builder");
        if (data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement().buildProjects().stream()
                .filter(project -> project.id().equals(barracksProject.id()))
                .noneMatch(project -> project.state() == galacticwars.clonewars.kingdom.BuildProjectState.COMPLETED)) {
            helper.fail("Builder loop did not complete its persisted project");
        }

        KingdomBaseBlueprint supply_depot = GameplayDataManager.snapshot()
                .blueprint("galacticwars:supply_depot").orElseThrow();
        BuildProject supply_depotProject = fullyProgressProject(
                data, owner.getUUID(), supply_depot,
                helper.getLevel().dimension().identifier().toString(), hallPos.offset(0, 32, 0));
        if (!data.completeBuildProject(owner.getUUID(), supply_depotProject, supply_depot)) {
            helper.fail("Courier supply_depot reward could not be applied");
        }
        List<StorageEndpoint> externalStorage = supply_depot.storageEndpoints(supply_depotProject);
        for (StorageEndpoint endpoint : externalStorage) {
            helper.getLevel().setBlock(
                    new BlockPos(endpoint.x(), endpoint.y(), endpoint.z()), Blocks.CHEST.defaultBlockState(), 3);
        }
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        recruit.setWorkerMainHandItem(ItemStack.EMPTY);
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
            helper.fail("Courier contract was rejected");
        }
        BlockPos sourcePos = new BlockPos(
                externalStorage.getFirst().x(), externalStorage.getFirst().y(), externalStorage.getFirst().z());
        Container source = (Container) helper.getLevel().getBlockEntity(sourcePos);
        source.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        setRecruitField(recruit, "storageTarget", sourcePos);
        setRecruitField(recruit, "workTarget", hallPos);
        invokeRecruitCommand(recruit, RecruitmentAction.WORK_AT_SITE);
        UUID courierOrder = acquirePersistedOrder(recruit);
        interactWorkerAt(recruit, sourcePos, "courier_withdraw");
        setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
        recruit.tickWorkerController();
        depositWorkerAt(recruit, hallPos);
        assertCompletedOrder(helper, data, owner.getUUID(), courierOrder, "courier");
        if (!source.getItem(0).isEmpty() || countContainerItem(hall, Items.IRON_INGOT) < 3) {
            helper.fail("Courier loop did not conserve its transferred stack");
        }
        helper.succeed();
    }

    private static void lightsaberBoltDeflection(GameTestHelper helper) {
        GalacticRecruitEntity shooter = helper.spawn(
                ModEntityTypes.B1_BATTLE_DROID.get(), new BlockPos(2, 1, 2));
        GalacticRecruitEntity guardian = helper.spawn(
                ModEntityTypes.JEDI_KNIGHT.get(), new BlockPos(5, 1, 2));
        shooter.initializeFromSpawnEgg();
        guardian.initializeFromSpawnEgg();
        guardian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BLUE_LIGHTSABER.get()));
        guardian.setYRot(90.0F);
        guardian.setYHeadRot(90.0F);
        guardian.setXRot(0.0F);
        int resourceBeforeRejectedDeflection = guardian.npcForceEnergy();
        BlasterBoltEntity selfOwnedBolt = new BlasterBoltEntity(
                helper.getLevel(), guardian, new ItemStack(ModItems.BLUE_LIGHTSABER.get()), 5.0D);
        if (LightsaberDeflectionService.tryDeflect(
                selfOwnedBolt, guardian, helper.getLevel().getGameTime())
                || guardian.npcForceEnergy() != resourceBeforeRejectedDeflection) {
            helper.fail("Rejected self-owned bolt deflection consumed saber guard resources");
            return;
        }
        BlasterBoltEntity bolt = new BlasterBoltEntity(
                helper.getLevel(), shooter, new ItemStack(ModItems.E5_BLASTER.get()), 5.0D);
        bolt.setPos(guardian.getX() - 0.5D, guardian.getEyeY(), guardian.getZ());
        bolt.setDeltaMovement(1.0D, 0.0D, 0.0D);
        boolean deflected = BlasterCombatEvents.handleProjectileImpact(bolt, guardian);
        if (!deflected
                || bolt.getOwner() != guardian
                || bolt.getDeltaMovement().x >= 0.0D
                || guardian.npcForceEnergy() >= resourceBeforeRejectedDeflection) {
            helper.fail("Jedi saber guard did not authoritatively deflect and spend NPC Force energy");
            return;
        }
        helper.succeed();
    }

    private static void specialistWorkerLoops(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 19);
        ChunkPos specialistCenterChunk = ChunkPos.containing(hallPos);
        Set<ChunkPos> specialistChunks = new LinkedHashSet<>();
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                ChunkPos chunk = new ChunkPos(
                        specialistCenterChunk.x() + chunkX,
                        specialistCenterChunk.z() + chunkZ);
                specialistChunks.add(chunk);
                helper.getLevel().getChunkSource().updateChunkForced(chunk, true);
            }
        }
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                helper.getLevel().setBlock(
                        hallPos.offset(x, -1, z),
                        Blocks.STONE.defaultBlockState(),
                        3);
            }
        }
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        GalacticRecruitEntity recruit = ModEntityTypes.CLONE_TROOPER.get().create(
                helper.getLevel(), EntitySpawnReason.TRIGGERED);
        if (recruit == null) {
            helper.fail("Could not create specialist worker recruit");
            return;
        }
        recruit.setPos(hallPos.getX() + 2.5, hallPos.getY(), hallPos.getZ() + 0.5);
        recruit.setInvulnerable(true);
        helper.getLevel().addFreshEntity(recruit);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        // Embedded GameTest clients never acknowledge this direct long-distance move. Update the
        // authoritative player ticket so the isolated specialist chunk remains entity-ticking.
        helper.getLevel().getChunkSource().move(owner);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        if (data.activateHall(owner.getUUID(), hall.factionId(),
                helper.getLevel().dimension().identifier().toString(), hallPos).isEmpty()) {
            helper.fail("Specialist worker loop could not reserve its isolated capital claim");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(helper.getLevel()).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(helper.getLevel()), owner,
                ProgressionEventType.FACTION_PLEDGED, "galacticwars:republic");
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Specialist worker loop recruit could not be hired");
            return;
        }

        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_FISHERMAN)) {
            helper.fail("Fisher contract was rejected");
            return;
        }
        BlockPos waterPos = hallPos.offset(3, 0, 1);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    helper.getLevel().setBlock(
                            waterPos.offset(x, 0, z),
                            Blocks.STONE.defaultBlockState(),
                            3);
                }
            }
        }
        helper.getLevel().setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
        if (!configureWorkerLifecycleWorksite(
                helper, data, owner, recruit, hallPos, List.of())) {
            return;
        }
        SpecialistWorkerLifecycleScenario scenario = new SpecialistWorkerLifecycleScenario(
                helper,
                hallPos,
                hall,
                owner,
                recruit,
                data,
                Set.copyOf(specialistChunks),
                countContainerItems(hall));
        helper.onEachTick(scenario::tick);
    }

    /**
     * Exercises one complete cook order through ordinary hire, profession-assignment, and
     * worksite paths. The fixture never mutates a worker phase, invokes the controller, or
     * moves the recruit after assignment.
     */
    private static void blackBoxCookLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 218));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos furnacePos = area.at(7, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box cook fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), 3);
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_COOK)) {
            helper.fail("Black-box cook could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("#minecraft:meat"))) {
            return;
        }
        putContainerItem(hall, new ItemStack(Items.BEEF));
        putContainerItem(hall, new ItemStack(Items.COAL));

        Container recruitCargo = recruit.createCargoContainer();
        boolean[] approachedFurnace = {false};
        boolean[] furnaceLoaded = {false};
        boolean[] furnaceLit = {false};
        boolean[] consumedFuelWhileLit = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box cook area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }
            if (!(level.getBlockEntity(furnacePos) instanceof Container furnace)) {
                complete[0] = true;
                helper.fail("Black-box cook furnace became unavailable");
                return;
            }

            approachedFurnace[0] |= recruit.distanceToSqr(Vec3.atCenterOf(furnacePos)) <= 4.0D;
            furnaceLoaded[0] |= furnace.getItem(0).is(Items.BEEF);
            var furnaceState = level.getBlockState(furnacePos);
            boolean litNow = furnaceState.hasProperty(BlockStateProperties.LIT)
                    && furnaceState.getValue(BlockStateProperties.LIT);
            furnaceLit[0] |= litNow;
            consumedFuelWhileLit[0] |= litNow
                    && furnace.getItem(0).is(Items.BEEF)
                    && furnace.getItem(1).isEmpty();

            int raw = countContainerItem(hall, Items.BEEF)
                    + countContainerItem(recruitCargo, Items.BEEF)
                    + countContainerItem(furnace, Items.BEEF);
            int cooked = countContainerItem(hall, Items.COOKED_BEEF)
                    + countContainerItem(recruitCargo, Items.COOKED_BEEF)
                    + countContainerItem(furnace, Items.COOKED_BEEF);
            int coal = countContainerItem(hall, Items.COAL)
                    + countContainerItem(recruitCargo, Items.COAL)
                    + countContainerItem(furnace, Items.COAL);
            if (raw + cooked != 1 || coal > 1) {
                complete[0] = true;
                helper.fail("Black-box cook violated physical conservation: raw="
                        + raw + ", cooked=" + cooked + ", coal=" + coal
                        + ", furnace=" + List.of(
                                furnace.getItem(0), furnace.getItem(1), furnace.getItem(2))
                        + ", cargo=" + workerInventoryCount(recruit));
                return;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            if (litNow
                    && furnace.getItem(0).is(Items.BEEF)
                    && status.phase() == WorkerPhase.BLOCKED
                    && status.requiredResource().equals("minecraft:coal")) {
                complete[0] = true;
                helper.fail("Lit furnace incorrectly requested a second fuel item: " + status);
                return;
            }

            boolean completedOrder = hasCompletedWorkerOrder(
                    data, owner.getUUID(), WorkOrderType.COOK);
            if (completedOrder
                    && countContainerItem(hall, Items.COOKED_BEEF) == 1
                    && raw == 0
                    && workerInventoryCount(recruit) == 0
                    && furnace.getItem(0).isEmpty()
                    && furnace.getItem(2).isEmpty()) {
                if (!approachedFurnace[0]
                        || !furnaceLoaded[0]
                        || !furnaceLit[0]
                        || !consumedFuelWhileLit[0]) {
                    complete[0] = true;
                    helper.fail("Cook completed without all embodied furnace observations: approached="
                            + approachedFurnace[0] + ", loaded=" + furnaceLoaded[0]
                            + ", lit=" + furnaceLit[0]
                            + ", consumedFuelWhileLit=" + consumedFuelWhileLit[0]);
                    return;
                }
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 750) {
                complete[0] = true;
                helper.fail("Black-box cook lifecycle timed out: status=" + status
                        + ", raw=" + raw + ", cooked=" + cooked + ", coal=" + coal
                        + ", approached=" + approachedFurnace[0]
                        + ", loaded=" + furnaceLoaded[0]
                        + ", lit=" + furnaceLit[0]
                        + ", consumedFuelWhileLit=" + consumedFuelWhileLit[0]
                        + ", furnace=" + List.of(
                                furnace.getItem(0), furnace.getItem(1), furnace.getItem(2)));
            }
        });
    }

    /**
     * Exercises one complete connected-tree order through ordinary hire, profession-assignment,
     * worksite, registered-storage, navigation, harvest, replant, and deposit paths. The fixture
     * never mutates a worker phase, invokes the controller, or moves the recruit after assignment.
     */
    private static void blackBoxLumberjackLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 219));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos treeRoot = area.at(7, 1, 4);
        List<BlockPos> logPositions = List.of(
                treeRoot,
                treeRoot.above(),
                treeRoot.above(2));
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box lumberjack fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        level.setBlock(treeRoot.below(), Blocks.DIRT.defaultBlockState(), 3);
        logPositions.forEach(pos -> level.setBlock(pos, Blocks.OAK_LOG.defaultBlockState(), 3));
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_LUMBERJACK)) {
            helper.fail("Black-box lumberjack could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("minecraft:oak_log"))) {
            return;
        }
        putContainerItem(hall, new ItemStack(Items.OAK_SAPLING));

        Container recruitCargo = recruit.createCargoContainer();
        int toolDamageBefore = recruit.getWorkerMainHandItem().getDamageValue();
        boolean[] withdrewSapling = {false};
        boolean[] approachedTree = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box lumberjack area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            withdrewSapling[0] |= countContainerItem(hall, Items.OAK_SAPLING) == 0
                    && countContainerItem(recruitCargo, Items.OAK_SAPLING) == 1;
            approachedTree[0] |= recruit.distanceToSqr(Vec3.atCenterOf(treeRoot)) <= 4.0D;

            int standingLogs = 0;
            for (BlockPos logPos : logPositions) {
                if (level.getBlockState(logPos).is(Blocks.OAK_LOG)) {
                    standingLogs++;
                }
            }
            int physicalLogs = standingLogs
                    + countContainerItem(hall, Items.OAK_LOG)
                    + countContainerItem(recruitCargo, Items.OAK_LOG);
            int physicalSaplings = countContainerItem(hall, Items.OAK_SAPLING)
                    + countContainerItem(recruitCargo, Items.OAK_SAPLING)
                    + (level.getBlockState(treeRoot).is(Blocks.OAK_SAPLING) ? 1 : 0);
            if (physicalLogs != 3 || physicalSaplings != 1) {
                complete[0] = true;
                helper.fail("Black-box lumberjack violated physical conservation: logs="
                        + physicalLogs + ", saplings=" + physicalSaplings
                        + ", standingLogs=" + standingLogs
                        + ", storedLogs=" + countContainerItem(hall, Items.OAK_LOG)
                        + ", storedSaplings=" + countContainerItem(hall, Items.OAK_SAPLING)
                        + ", cargo=" + recruitCargo);
                return;
            }

            boolean replanted = level.getBlockState(treeRoot).is(Blocks.OAK_SAPLING)
                    && level.getBlockState(treeRoot.above()).isAir()
                    && level.getBlockState(treeRoot.above(2)).isAir();
            boolean completedOrder = hasCompletedWorkerOrder(
                    data, owner.getUUID(), WorkOrderType.LUMBER);
            int toolDamage = recruit.getWorkerMainHandItem().getDamageValue();
            if (completedOrder
                    && replanted
                    && countContainerItem(hall, Items.OAK_LOG) == 3
                    && countContainerItem(hall, Items.OAK_SAPLING) == 0
                    && workerInventoryCount(recruit) == 0
                    && withdrewSapling[0]
                    && approachedTree[0]
                    && toolDamage == toolDamageBefore + 3
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 600) {
                complete[0] = true;
                helper.fail("Black-box lumberjack lifecycle timed out: status="
                        + recruit.getWorkerStatus()
                        + ", position=" + recruit.position()
                        + ", standingLogs=" + standingLogs
                        + ", storedLogs=" + countContainerItem(hall, Items.OAK_LOG)
                        + ", storedSaplings=" + countContainerItem(hall, Items.OAK_SAPLING)
                        + ", cargo=" + recruitCargo
                        + ", replanted=" + replanted
                        + ", withdrewSapling=" + withdrewSapling[0]
                        + ", approachedTree=" + approachedTree[0]
                        + ", completedOrder=" + completedOrder
                        + ", toolDamage=" + toolDamageBefore + "->" + toolDamage);
            }
        });
    }

    /**
     * Exercises one complete ore order through ordinary hire, profession-assignment, worksite,
     * registered-storage, navigation, mining, and deposit paths. The fixture never mutates a
     * worker phase, invokes the controller, or moves the recruit after assignment.
     */
    private static void blackBoxMinerLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 220));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos orePos = area.at(7, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box miner fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        level.setBlock(orePos, Blocks.IRON_ORE.defaultBlockState(), 3);
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)) {
            helper.fail("Black-box miner could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("minecraft:iron_ore"))) {
            return;
        }

        Container recruitCargo = recruit.createCargoContainer();
        int toolDamageBefore = recruit.getWorkerMainHandItem().getDamageValue();
        boolean[] approachedOre = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box miner area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            approachedOre[0] |= recruit.distanceToSqr(Vec3.atCenterOf(orePos)) <= 4.0D;
            int physicalIron = (level.getBlockState(orePos).is(Blocks.IRON_ORE) ? 1 : 0)
                    + countContainerItem(hall, Items.RAW_IRON)
                    + countContainerItem(recruitCargo, Items.RAW_IRON);
            if (physicalIron != 1) {
                complete[0] = true;
                helper.fail("Black-box miner violated physical conservation: iron="
                        + physicalIron + ", block=" + level.getBlockState(orePos)
                        + ", stored=" + countContainerItem(hall, Items.RAW_IRON)
                        + ", cargo=" + recruitCargo);
                return;
            }

            boolean completedOrder = hasCompletedWorkerOrder(
                    data, owner.getUUID(), WorkOrderType.MINE);
            int toolDamage = recruit.getWorkerMainHandItem().getDamageValue();
            if (completedOrder
                    && level.getBlockState(orePos).isAir()
                    && countContainerItem(hall, Items.RAW_IRON) == 1
                    && workerInventoryCount(recruit) == 0
                    && approachedOre[0]
                    && toolDamage == toolDamageBefore + 1
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 500) {
                complete[0] = true;
                helper.fail("Black-box miner lifecycle timed out: status="
                        + recruit.getWorkerStatus()
                        + ", position=" + recruit.position()
                        + ", block=" + level.getBlockState(orePos)
                        + ", stored=" + countContainerItem(hall, Items.RAW_IRON)
                        + ", cargo=" + recruitCargo
                        + ", approached=" + approachedOre[0]
                        + ", completedOrder=" + completedOrder
                        + ", toolDamage=" + toolDamageBefore + "->" + toolDamage);
            }
        });
    }

    /**
     * Proves that an owner can physically reinstall a miner's missing pickaxe through the
     * provider-owned loadout and the already-blocked order naturally retries without mutation.
     */
    private static void blackBoxMinerMissingToolRecovery(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                14,
                -2,
                10,
                isolatedCapital(helper, 232));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos orePos = area.at(7, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Missing-tool recovery fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");
        level.setBlock(orePos, Blocks.IRON_ORE.defaultBlockState(), 3);

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 53));
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)) {
            helper.fail("Missing-tool recovery miner could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("minecraft:iron_ore"))) {
            return;
        }

        ItemStack removedTool = quickMoveWorkerToolToPlayer(
                helper, openServerRecruitLoadout(helper, owner, recruit, 76), owner);
        if (!WorkerDutyLoadoutPolicy.isUsableTool(WorkerProfession.MINER, removedTool)
                || !recruit.getWorkerMainHandItem().isEmpty()
                || !inventoryContainsExact(owner.getInventory(), removedTool)
                || countContainerItem(hall, Items.IRON_PICKAXE) != 0) {
            helper.fail("Missing-tool fixture did not retain the removed physical pickaxe");
            return;
        }

        Container recruitCargo = recruit.createCargoContainer();
        int toolDamageBefore = removedTool.getDamageValue();
        UUID[] blockedOrderId = {null};
        boolean[] naturalRetryObserved = {false};
        boolean[] approachedOreAfterReinsertion = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 10);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Missing-tool recovery area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            if (blockedOrderId[0] == null
                    && status.phase() == WorkerPhase.BLOCKED
                    && status.reasonCode().equals("missing_tool")) {
                WorkOrder order = data.assignedWorkOrder(
                        owner.getUUID(), recruit.getUUID()).orElse(null);
                if (order == null || order.type() != WorkOrderType.MINE
                        || !level.getBlockState(orePos).is(Blocks.IRON_ORE)
                        || countContainerItem(hall, Items.RAW_IRON) != 0
                        || workerInventoryCount(recruit) != 0) {
                    helper.fail("Missing-tool block mutated ore, output, cargo, or order");
                    return;
                }
                blockedOrderId[0] = order.id();
                quickMovePlayerToolToWorker(
                        helper,
                        openServerRecruitLoadout(helper, owner, recruit, 77),
                        owner,
                        recruit,
                        removedTool);
            }

            if (blockedOrderId[0] != null) {
                WorkOrder recordedOrder = data.workOrder(
                        owner.getUUID(), blockedOrderId[0]).orElse(null);
                if (recordedOrder == null || recordedOrder.type() != WorkOrderType.MINE) {
                    complete[0] = true;
                    helper.fail("Missing-tool recovery lost its recorded mine order");
                    return;
                }
                if (!recordedOrder.state().terminal()) {
                    WorkOrder activeOrder = data.assignedWorkOrder(
                            owner.getUUID(), recruit.getUUID()).orElse(null);
                    if (activeOrder == null || !activeOrder.id().equals(blockedOrderId[0])) {
                        complete[0] = true;
                        helper.fail("Missing-tool recovery replaced the blocked order: expected="
                                + blockedOrderId[0] + ", actual="
                                + (activeOrder == null ? null : activeOrder.id()));
                        return;
                    }
                    naturalRetryObserved[0] |= activeOrder.state() != WorkOrderState.BLOCKED;
                    approachedOreAfterReinsertion[0] |= recruit.distanceToSqr(
                            Vec3.atCenterOf(orePos)) <= 4.0D;
                } else if (recordedOrder.state() != WorkOrderState.COMPLETED) {
                    complete[0] = true;
                    helper.fail("Missing-tool recovery ended the recorded order as "
                            + recordedOrder.state());
                    return;
                }

                ItemStack expectedUsedTool = removedTool.copy();
                expectedUsedTool.setDamageValue(toolDamageBefore + 1);
                if (recordedOrder.state() == WorkOrderState.COMPLETED
                        && level.getBlockState(orePos).isAir()
                        && countContainerItem(hall, Items.RAW_IRON) == 1
                        && workerInventoryCount(recruit) == 0
                        && naturalRetryObserved[0]
                        && approachedOreAfterReinsertion[0]
                        && ItemStack.isSameItemSameComponents(
                                recruit.getWorkerMainHandItem(), expectedUsedTool)
                        && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                    complete[0] = true;
                    recruit.discard();
                    helper.succeed();
                    return;
                }
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 800) {
                complete[0] = true;
                helper.fail("Missing-tool recovery timed out: status=" + status
                        + ", blockedOrder=" + blockedOrderId[0]
                        + ", block=" + level.getBlockState(orePos)
                        + ", stored=" + countContainerItem(hall, Items.RAW_IRON)
                        + ", cargo=" + workerInventoryCount(recruit)
                        + ", naturalRetry=" + naturalRetryObserved[0]
                        + ", approached=" + approachedOreAfterReinsertion[0]
                        + ", toolDamage=" + toolDamageBefore + "->"
                        + recruit.getWorkerMainHandItem().getDamageValue());
            }
        });
    }

    /**
     * Verifies that a miner whose pickaxe breaks can physically withdraw a component-bearing
     * replacement from registered storage and resume the already-blocked mine order.
     */
    private static void blackBoxMinerBrokenToolRecovery(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                14,
                -2,
                10,
                isolatedCapital(helper, 231));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos firstOre = area.at(7, 1, 4);
        BlockPos secondOre = area.at(8, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Broken-tool recovery fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");
        level.setBlock(firstOre, Blocks.IRON_ORE.defaultBlockState(), 3);
        level.setBlock(secondOre, Blocks.IRON_ORE.defaultBlockState(), 3);

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 53));
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MINER)) {
            helper.fail("Broken-tool recovery miner could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("minecraft:iron_ore"))) {
            return;
        }

        ItemStack original = quickMoveWorkerToolToPlayer(
                helper, openServerRecruitLoadout(helper, owner, recruit, 74), owner);
        ItemStack nearlyBroken = original.copy();
        nearlyBroken.setDamageValue(nearlyBroken.getMaxDamage() - 1);
        replaceExactPlayerStack(helper, owner, original, nearlyBroken);
        quickMovePlayerToolToWorker(
                helper,
                openServerRecruitLoadout(helper, owner, recruit, 75),
                owner,
                recruit,
                nearlyBroken);

        ItemStack replacement = new ItemStack(Items.IRON_PICKAXE);
        replacement.set(DataComponents.CUSTOM_NAME, Component.literal("Recovery Pickaxe"));
        replacement.setDamageValue(7);

        Container recruitCargo = recruit.createCargoContainer();
        int[] phase = {0};
        UUID[] blockedOrderId = {null};
        boolean[] storageNavigationObserved = {false};
        boolean[] complete = {false};
        long[] readinessStartedAt = {helper.getTick()};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            boolean areaTicking = areSmartBrainAreaChunksTicking(helper, area, -2, 14, -2, 10);
            boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
            if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                if (helper.getTick() - readinessStartedAt[0] >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                    complete[0] = true;
                    helper.fail("Broken-tool recovery area never became entity-ticking: ticking="
                            + areaTicking + ", recruit=" + recruitIndexed
                            + ", recruitTick=" + recruit.tickCount);
                }
                return;
            }

            int oreAndOutput = (level.getBlockState(firstOre).is(Blocks.IRON_ORE) ? 1 : 0)
                    + (level.getBlockState(secondOre).is(Blocks.IRON_ORE) ? 1 : 0)
                    + countContainerItem(hall, Items.RAW_IRON)
                    + countContainerItem(recruitCargo, Items.RAW_IRON);
            if (oreAndOutput != 2) {
                complete[0] = true;
                helper.fail("Broken-tool recovery violated ore conservation: total=" + oreAndOutput
                        + ", stored=" + countContainerItem(hall, Items.RAW_IRON)
                        + ", cargo=" + countContainerItem(recruitCargo, Items.RAW_IRON));
                return;
            }

            long completedMineOrders = data.kingdomForOwner(owner.getUUID()).orElseThrow()
                    .settlement().workOrders().stream()
                    .filter(order -> order.type() == WorkOrderType.MINE)
                    .filter(order -> order.state() == WorkOrderState.COMPLETED)
                    .count();
            WorkerStatus status = recruit.getWorkerStatus();
            WorkOrder assigned = data.assignedWorkOrder(
                    owner.getUUID(), recruit.getUUID()).orElse(null);

            if (phase[0] == 0 && completedMineOrders == 1
                    && recruit.getWorkerMainHandItem().isEmpty()) {
                phase[0] = 1;
            }
            if (phase[0] == 1
                    && status.phase() == WorkerPhase.BLOCKED
                    && status.reasonCode().equals("missing_tool")
                    && assigned != null
                    && assigned.type() == WorkOrderType.MINE) {
                blockedOrderId[0] = assigned.id();
                putContainerItem(hall, replacement.copy());
                phase[0] = 2;
            }
            if (phase[0] != 2) {
                return;
            }

            storageNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("withdraw_worker_tool");
            if (assigned != null && !assigned.id().equals(blockedOrderId[0])) {
                complete[0] = true;
                helper.fail("Broken-tool recovery replaced the blocked order: expected="
                        + blockedOrderId[0] + ", actual=" + assigned.id());
                return;
            }
            int physicalReplacementCount = countContainerItem(hall, Items.IRON_PICKAXE)
                    + countContainerItem(recruitCargo, Items.IRON_PICKAXE)
                    + (recruit.getWorkerMainHandItem().is(Items.IRON_PICKAXE) ? 1 : 0);
            if (physicalReplacementCount != 1) {
                complete[0] = true;
                helper.fail("Broken-tool recovery duplicated or lost the replacement: count="
                        + physicalReplacementCount);
                return;
            }
            ItemStack expectedUsedReplacement = replacement.copy();
            expectedUsedReplacement.setDamageValue(replacement.getDamageValue() + 1);
            if (completedMineOrders == 2
                    && countContainerItem(hall, Items.RAW_IRON) == 2
                    && workerInventoryCount(recruit) == 0
                    && storageNavigationObserved[0]
                    && ItemStack.isSameItemSameComponents(
                            recruit.getWorkerMainHandItem(), expectedUsedReplacement)) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
            }
        });
    }

    private static RecruitLoadoutMenu openServerRecruitLoadout(
            GameTestHelper helper,
            ServerPlayer owner,
            GalacticRecruitEntity recruit,
            int containerId
    ) {
        if (!recruit.canPlayerManageLogistics(owner)
                || owner.distanceToSqr(recruit) > 64.0D) {
            helper.fail("Recruit loadout authority or distance gate was not satisfied");
            return null;
        }
        return (RecruitLoadoutMenu) new RecruitLoadoutMenuProvider(recruit)
                .createMenu(containerId, owner.getInventory(), owner);
    }

    private static ItemStack quickMoveWorkerToolToPlayer(
            GameTestHelper helper,
            RecruitLoadoutMenu menu,
            ServerPlayer owner
    ) {
        ItemStack moved = menu.quickMoveStack(owner, LOADOUT_WORKER_TOOL_SLOT);
        if (moved.isEmpty() || !inventoryContainsExact(owner.getInventory(), moved)) {
            helper.fail("Provider-owned loadout did not move the physical worker tool to the player");
        }
        menu.removed(owner);
        return moved;
    }

    private static void quickMovePlayerToolToWorker(
            GameTestHelper helper,
            RecruitLoadoutMenu menu,
            ServerPlayer owner,
            GalacticRecruitEntity recruit,
            ItemStack expected
    ) {
        int sourceSlot = java.util.stream.IntStream.range(
                        RecruitLoadoutMenu.PLAYER_INVENTORY_START,
                        RecruitLoadoutMenu.PLAYER_INVENTORY_END)
                .filter(slot -> ItemStack.isSameItemSameComponents(
                        menu.getSlot(slot).getItem(), expected))
                .findFirst()
                .orElse(-1);
        if (sourceSlot < 0 || menu.quickMoveStack(owner, sourceSlot).isEmpty()
                || !ItemStack.isSameItemSameComponents(
                        recruit.getWorkerMainHandItem(), expected)) {
            helper.fail("Provider-owned loadout did not install the expected worker tool");
        }
        menu.removed(owner);
    }

    private static boolean inventoryContainsExact(Container inventory, ItemStack expected) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), expected)) {
                return true;
            }
        }
        return false;
    }

    private static void replaceExactPlayerStack(
            GameTestHelper helper,
            ServerPlayer owner,
            ItemStack previous,
            ItemStack replacement
    ) {
        Container inventory = owner.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), previous)) {
                inventory.setItem(slot, replacement.copy());
                inventory.setChanged();
                return;
            }
        }
        helper.fail("Physical worker tool was not present in player inventory for fixture damage");
    }

    /**
     * Exercises one complete breeding order through ordinary hire, profession-assignment,
     * worksite, registered-storage, navigation, feed withdrawal, and feeding paths. The fixture
     * never mutates a worker phase, invokes the controller, or moves the recruit after assignment.
     */
    private static void blackBoxAnimalFarmerLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 222));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos firstCowPos = area.at(6, 1, 4);
        BlockPos secondCowPos = area.at(7, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box animal farmer fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_ANIMAL_FARMER)) {
            helper.fail("Black-box animal farmer could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper,
                data,
                owner,
                recruit,
                hallPos,
                List.of("minecraft:cow"))) {
            return;
        }

        Animal first = EntityTypes.COW.create(level, EntitySpawnReason.TRIGGERED);
        Animal second = EntityTypes.COW.create(level, EntitySpawnReason.TRIGGERED);
        if (first == null || second == null) {
            helper.fail("Black-box animal farmer could not create its livestock pair");
            return;
        }
        first.setPos(Vec3.atCenterOf(firstCowPos));
        second.setPos(Vec3.atCenterOf(secondCowPos));
        first.setNoAi(true);
        second.setNoAi(true);
        first.setPersistenceRequired();
        second.setPersistenceRequired();
        if (!level.addFreshEntity(first) || !level.addFreshEntity(second)) {
            helper.fail("Black-box animal farmer could not spawn its livestock pair");
            return;
        }
        putContainerItem(hall, new ItemStack(Items.WHEAT, 2));

        Container recruitCargo = recruit.createCargoContainer();
        boolean[] withdrewFeed = {false};
        boolean[] approachedPair = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                boolean livestockIndexed = level.getEntity(first.getUUID()) == first
                        && level.getEntity(second.getUUID()) == second;
                if (!areaTicking || !recruitIndexed || !livestockIndexed
                        || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box animal farmer area never became entity-ticking: "
                                + "ticking=" + areaTicking + ", recruit=" + recruitIndexed
                                + ", livestock=" + livestockIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            int storedWheat = countContainerItem(hall, Items.WHEAT);
            int carriedWheat = countContainerItem(recruitCargo, Items.WHEAT);
            int fedAnimals = (first.isInLove() ? 1 : 0) + (second.isInLove() ? 1 : 0);
            withdrewFeed[0] |= storedWheat == 0 && carriedWheat == 2;
            approachedPair[0] |= recruit.distanceToSqr(first) <= 4.0D
                    || recruit.distanceToSqr(second) <= 4.0D;
            if (!first.isAlive() || !second.isAlive()
                    || storedWheat + carriedWheat + fedAnimals != 2) {
                complete[0] = true;
                helper.fail("Black-box animal farmer violated feed conservation: stored="
                        + storedWheat + ", carried=" + carriedWheat
                        + ", fed=" + fedAnimals + ", alive="
                        + first.isAlive() + "/" + second.isAlive());
                return;
            }

            boolean completedOrder = hasCompletedWorkerOrder(
                    data, owner.getUUID(), WorkOrderType.ANIMAL_FARM);
            if (completedOrder
                    && first.isInLove()
                    && second.isInLove()
                    && storedWheat == 0
                    && carriedWheat == 0
                    && withdrewFeed[0]
                    && approachedPair[0]
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                complete[0] = true;
                recruit.discard();
                first.discard();
                second.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 500) {
                complete[0] = true;
                helper.fail("Black-box animal farmer lifecycle timed out: status="
                        + recruit.getWorkerStatus()
                        + ", position=" + recruit.position()
                        + ", cows=" + first.position() + "/" + second.position()
                        + ", stored=" + storedWheat
                        + ", carried=" + carriedWheat
                        + ", love=" + first.isInLove() + "/" + second.isInLove()
                        + ", withdrew=" + withdrewFeed[0]
                        + ", approached=" + approachedPair[0]
                        + ", completedOrder=" + completedOrder);
            }
        });
    }

    /**
     * Exercises one complete fishing order through ordinary hire, profession-assignment,
     * worksite, registered-storage, navigation, timed loot-table fishing, and deposit paths. The
     * fixture never mutates a worker phase, invokes the controller, or moves the recruit after
     * assignment.
     */
    private static void blackBoxFisherLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 224));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(2, 1, 4);
        BlockPos waterPos = area.at(7, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box fisher fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_FISHERMAN)) {
            helper.fail("Black-box fisher could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper, data, owner, recruit, hallPos, List.of())) {
            return;
        }
        if (!recruit.getWorkerMainHandItem().is(Items.FISHING_ROD)) {
            helper.fail("Black-box fisher assignment did not equip a fishing rod");
            return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    level.setBlock(
                            waterPos.offset(x, 0, z),
                            Blocks.STONE.defaultBlockState(),
                            3);
                }
            }
        }
        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);

        Container recruitCargo = recruit.createCargoContainer();
        List<ItemStack> storageBaseline = copyContainerStacks(hall);
        int storedCountBefore = countContainerItems(hall);
        int rodDamageBefore = recruit.getWorkerMainHandItem().getDamageValue();
        List<ItemStack> caughtStacks = new ArrayList<>();
        boolean[] approachedWater = {false};
        boolean[] castObserved = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box fisher area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            approachedWater[0] |= recruit.distanceToSqr(Vec3.atCenterOf(waterPos)) <= 4.0D;
            castObserved[0] |= status.phase() == WorkerPhase.INTERACT
                    && status.reasonCode().equals("fishing_wait");
            if (caughtStacks.isEmpty() && !recruitCargo.isEmpty()) {
                caughtStacks.addAll(copyContainerStacks(recruitCargo));
            }

            int caughtCount = caughtStacks.stream().mapToInt(ItemStack::getCount).sum();
            int physicalCatchDelta = countContainerItems(hall)
                    + countContainerItems(recruitCargo)
                    - storedCountBefore;
            if (caughtStacks.isEmpty()) {
                if (physicalCatchDelta != 0) {
                    complete[0] = true;
                    helper.fail("Black-box fisher deposited output without observable cargo: delta="
                            + physicalCatchDelta + ", stored=" + copyContainerStacks(hall));
                    return;
                }
            } else {
                if (physicalCatchDelta != caughtCount) {
                    complete[0] = true;
                    helper.fail("Black-box fisher violated catch-count conservation: caught="
                            + caughtCount + ", physicalDelta=" + physicalCatchDelta
                            + ", caughtStacks=" + caughtStacks
                            + ", stored=" + copyContainerStacks(hall)
                            + ", cargo=" + copyContainerStacks(recruitCargo));
                    return;
                }
                for (int index = 0; index < caughtStacks.size(); index++) {
                    ItemStack caught = caughtStacks.get(index);
                    boolean alreadyChecked = false;
                    for (int prior = 0; prior < index; prior++) {
                        if (ItemStack.isSameItemSameComponents(caughtStacks.get(prior), caught)) {
                            alreadyChecked = true;
                            break;
                        }
                    }
                    if (alreadyChecked) {
                        continue;
                    }
                    int expectedCount = countMatchingStack(caughtStacks, caught);
                    int currentCount = countMatchingStack(hall, caught)
                            + countMatchingStack(recruitCargo, caught)
                            - countMatchingStack(storageBaseline, caught);
                    if (currentCount != expectedCount) {
                        complete[0] = true;
                        helper.fail("Black-box fisher changed catch components or quantity: expected="
                                + expectedCount + " of " + caught + ", current=" + currentCount
                                + ", stored=" + copyContainerStacks(hall)
                                + ", cargo=" + copyContainerStacks(recruitCargo));
                        return;
                    }
                }
            }

            boolean completedOrder = hasCompletedWorkerOrder(
                    data, owner.getUUID(), WorkOrderType.FISH);
            int rodDamage = recruit.getWorkerMainHandItem().getDamageValue();
            if (completedOrder
                    && !caughtStacks.isEmpty()
                    && recruitCargo.isEmpty()
                    && physicalCatchDelta == caughtCount
                    && approachedWater[0]
                    && castObserved[0]
                    && rodDamage == rodDamageBefore + 1
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 650) {
                complete[0] = true;
                helper.fail("Black-box fisher lifecycle timed out: status=" + status
                        + ", position=" + recruit.position()
                        + ", water=" + level.getBlockState(waterPos)
                        + ", caught=" + caughtStacks
                        + ", stored=" + copyContainerStacks(hall)
                        + ", cargo=" + copyContainerStacks(recruitCargo)
                        + ", approached=" + approachedWater[0]
                        + ", cast=" + castObserved[0]
                        + ", completedOrder=" + completedOrder
                        + ", rodDamage=" + rodDamageBefore + "->" + rodDamage);
            }
        });
    }

    /**
     * Exercises one physical market transaction through ordinary Survival hire, profession
     * assignment, worksite, registered storage, navigation, server-authored menu, and menu-action
     * paths. The fixture never mutates a worker phase, invokes the controller, or moves the
     * recruit after assignment. Headless GameTest cannot send NeoForge's extended-screen packet,
     * so it constructs the same provider-owned server menu after proving the live interaction
     * distance and market-validity gates.
     */
    private static void blackBoxMerchantLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 226));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(12, 1, 4);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box merchant fixture could not activate its Command Center");
            return;
        }
        putContainerItem(hall, new ItemStack(ModItems.CREDIT_CHIP.get(), 32));
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 128));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_MERCHANT)) {
            helper.fail("Black-box merchant could not be hired and assigned");
            return;
        }
        if (!configureWorkerLifecycleWorksite(
                helper, data, owner, recruit, hallPos, List.of())) {
            return;
        }
        WorksiteRecord worksite = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElseThrow();
        Vec3 marketCenter = new Vec3(
                worksite.x() + 0.5D, worksite.y() + 0.5D, worksite.z() + 0.5D);
        double initialDistance = recruit.distanceToSqr(marketCenter);
        if (initialDistance <= 16.0D) {
            helper.fail("Black-box merchant did not begin far enough away to prove navigation: "
                    + initialDistance);
            return;
        }
        int expectedTradePrice = FactionBalanceService.applyPercentCeil(
                FactionBalanceService.tradeCreditPrice(
                        "galacticwars:republic",
                        LaunchContentCatalog.trades()
                                .get("republic_quartermaster").price()),
                90);

        putContainerItem(hall, new ItemStack(ModItems.ENERGY_CELL.get(), 8));
        boolean[] navigationObserved = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 14, -2, 8);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box merchant area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            navigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("open_market");
            if (recruit.isMarketAvailable()) {
                WorkOrder order = data.assignedWorkOrder(
                        owner.getUUID(), recruit.getUUID()).orElse(null);
                owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ() + 1.0D);
                level.getChunkSource().move(owner);
                MerchantTradeMenu tradeMenu = (MerchantTradeMenu)
                        new MerchantTradeMenuProvider(recruit)
                                .createMenu(73, owner.getInventory(), owner);

                int offerIndex = tradeMenu.tradeIds().indexOf("republic_quartermaster");
                var offer = offerIndex < 0 ? null : tradeMenu.offers().get(offerIndex);
                int creditsBefore = CreditTransactionService.playerBalance(owner);
                int stockBefore = countContainerItem(hall, ModItems.ENERGY_CELL.get());
                int playerStockBefore = countPlayerItem(owner, ModItems.ENERGY_CELL.get());
                int tradesBefore = ProgressionSavedData.get(level).state(owner.getUUID())
                        .total(ProgressionEventType.TRADE_COMPLETED);
                UUID requestId = UUID.randomUUID();
                boolean traded = offerIndex >= 0
                        && tradeMenu.handleReplayAction(owner, requestId, offerIndex);
                boolean replayRejected = offerIndex >= 0
                        && !tradeMenu.handleReplayAction(owner, requestId, offerIndex);
                int creditsAfter = CreditTransactionService.playerBalance(owner);
                int stockAfter = countContainerItem(hall, ModItems.ENERGY_CELL.get());
                int playerStockAfter = countPlayerItem(owner, ModItems.ENERGY_CELL.get());
                int tradesAfter = ProgressionSavedData.get(level).state(owner.getUUID())
                        .total(ProgressionEventType.TRADE_COMPLETED);
                WorkOrder persistedOrder = data.assignedWorkOrder(
                        owner.getUUID(), recruit.getUUID()).orElse(null);
                boolean truthfulOffer = offer != null
                        && offer.tradeId().equals("republic_quartermaster")
                        && offer.itemId().equals("galacticwars:energy_cell")
                        && offer.itemCount() == 8
                        && offer.creditPrice() == expectedTradePrice
                        && offer.disposition().equals("friendly")
                        && offer.eligible();
                boolean validOrder = order != null
                        && persistedOrder != null
                        && persistedOrder.id().equals(order.id())
                        && persistedOrder.type() == WorkOrderType.MERCHANT
                        && !persistedOrder.state().terminal()
                        && persistedOrder.assignedRecruitId()
                                .filter(recruit.getUUID()::equals).isPresent();
                boolean conserved = offer != null
                        && stockBefore == 8
                        && stockAfter == 0
                        && playerStockBefore == 0
                        && playerStockAfter == 8
                        && creditsAfter == creditsBefore - expectedTradePrice;
                if (!navigationObserved[0]
                        || !truthfulOffer
                        || !traded
                        || !replayRejected
                        || !conserved
                        || tradesAfter != tradesBefore + 1
                        || !validOrder
                        || !tradeMenu.stillValid(owner)
                        || recruit.getRecruitCommand() != RecruitmentAction.WORK_AT_SITE) {
                    complete[0] = true;
                    helper.fail("Black-box merchant did not complete one conserved live-menu trade: "
                            + "status=" + status
                            + ", initialDistance=" + initialDistance
                            + ", navigation=" + navigationObserved[0]
                            + ", offer=" + offer
                            + ", expectedPrice=" + expectedTradePrice
                            + ", traded=" + traded
                            + ", replay=" + replayRejected
                            + ", credits=" + creditsBefore + "->" + creditsAfter
                            + ", stock=" + stockBefore + "->" + stockAfter
                            + ", playerStock=" + playerStockBefore + "->" + playerStockAfter
                            + ", trades=" + tradesBefore + "->" + tradesAfter
                            + ", order=" + persistedOrder
                            + ", menuValid=" + tradeMenu.stillValid(owner));
                    return;
                }

                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 500) {
                complete[0] = true;
                helper.fail("Black-box merchant lifecycle timed out: status=" + status
                        + ", position=" + recruit.position()
                        + ", worksite=" + worksite
                        + ", navigation=" + navigationObserved[0]
                        + ", market=" + recruit.isMarketAvailable()
                        + ", stock=" + countContainerItem(hall, ModItems.ENERGY_CELL.get()));
            }
        });
    }

    /**
     * Exercises one shipped blueprint through ordinary Survival hire, recruit-menu projector
     * preparation, in-world projection, project-specific assignment, registered storage,
     * navigation, physical placement, persistence, and completion. The fixture never mutates a
     * worker phase, invokes the controller, or moves the recruit after project assignment.
     */
    private static void blackBoxBuilderLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                18,
                -2,
                10,
                isolatedCapital(helper, 228));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos origin = area.at(5, 1, 1);
        BlockPos recruitPos = area.at(14, 1, 6);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Black-box builder fixture could not activate its Command Center");
            return;
        }
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 128));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)) {
            helper.fail("Black-box builder could not be hired");
            return;
        }
        owner.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.BLUEPRINT_PROJECTOR.get()));
        ConstructionPlan selectedPlan = null;
        int blueprintCount = GameplayDataManager.snapshot().blueprints().size();
        for (int attempt = 0; attempt < blueprintCount; attempt++) {
            if (!recruit.handleMenuButton(
                    owner, RecruitCommandMenu.BUTTON_BUILD_STARTER_KEEP)) {
                helper.fail("Black-box builder could not prepare its physical projector");
                return;
            }
            selectedPlan = owner.getItemInHand(InteractionHand.MAIN_HAND)
                    .get(ModDataComponents.CONSTRUCTION_PLAN.get());
            if (selectedPlan != null && selectedPlan.blueprintId().equals("galacticwars:mine")) {
                break;
            }
            if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_NEXT_BLUEPRINT)) {
                helper.fail("Black-box builder could not cycle to the shipped mine blueprint");
                return;
            }
        }
        if (selectedPlan == null || !selectedPlan.blueprintId().equals("galacticwars:mine")) {
            helper.fail("Black-box builder did not select the shipped mine blueprint: "
                    + selectedPlan);
            return;
        }
        KingdomBaseBlueprint blueprint = GameplayDataManager.snapshot()
                .blueprint(selectedPlan.blueprintId()).orElseThrow();
        if (blueprint.placements().size() != 9
                || blueprint.placements().stream()
                        .anyMatch(placement -> !placement.itemId().equals("galacticwars:duracrete"))) {
            helper.fail("Black-box builder fixture requires the shipped nine-Duracrete mine");
            return;
        }
        for (int placementIndex = 0;
                placementIndex < blueprint.placements().size();
                placementIndex++) {
            BaseBlockPlacement placement = blueprint.rotatedPlacement(placementIndex, 0);
            BlockPos placementPos = origin.offset(
                    placement.x(), placement.y(), placement.z());
            level.getChunkAt(placementPos);
            level.setBlock(placementPos, Blocks.AIR.defaultBlockState(), 3);
        }

        int initialMaterials = 9;
        putContainerItem(hall, new ItemStack(ModItems.DURACRETE.get(), initialMaterials));
        int buildingEventsBefore = ProgressionSavedData.get(level).state(owner.getUUID())
                .total(ProgressionEventType.BUILDING_COMPLETED);
        Vec3 initialRecruitPosition = recruit.position();
        double initialStorageDistance = recruit.distanceToSqr(
                hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        double initialBuildDistance = recruit.distanceToSqr(
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
        if (initialStorageDistance <= 16.0D || initialBuildDistance <= 16.0D) {
            helper.fail("Black-box builder did not begin far enough away to prove navigation: "
                    + initialStorageDistance + "/" + initialBuildDistance);
            return;
        }
        ItemStack projector = owner.getItemInHand(InteractionHand.MAIN_HAND);
        BuildProject[] assignedProject = {null};
        boolean[] withdrawNavigationObserved = {false};
        boolean[] buildNavigationObserved = {false};
        boolean[] movedPhysically = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        int[] lastPersistedPlacements = {0};
        UUID[] workOrderId = {null};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (startedRecruitTick[0] < 0) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 18, -2, 10);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box builder area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }
                startedRecruitTick[0] = recruit.tickCount;
            }
            if (assignedProject[0] == null) {
                owner.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
                level.getChunkSource().move(owner);
                InteractionResult projection = projector.getItem().useOn(new UseOnContext(
                        owner,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(
                                Vec3.atCenterOf(origin.below()),
                                Direction.UP,
                                origin.below(),
                                false)));
                BuildProject project = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                        .buildProjects().stream()
                        .filter(candidate -> candidate.blueprintId().equals(blueprint.id()))
                        .filter(candidate -> candidate.originX() == origin.getX()
                                && candidate.originY() == origin.getY()
                                && candidate.originZ() == origin.getZ())
                        .findFirst().orElse(null);
                boolean projectWorksiteAssigned = project != null
                        && data.assignedWorksite(owner.getUUID(), recruit.getUUID())
                                .flatMap(WorksiteRecord::sourceProjectId)
                                .filter(project.id()::equals)
                                .isPresent();
                if (projection != InteractionResult.SUCCESS
                        || projector.get(ModDataComponents.CONSTRUCTION_PLAN.get()) != null
                        || project == null
                        || !projectWorksiteAssigned
                        || recruit.getWorkerProfession().filter(WorkerProfession.BUILDER::equals).isEmpty()
                        || recruit.getRecruitCommand() != RecruitmentAction.WORK_AT_SITE) {
                    complete[0] = true;
                    helper.fail("Black-box builder projection did not create its authoritative assignment: "
                            + "projection=" + projection
                            + ", plan=" + projector.get(ModDataComponents.CONSTRUCTION_PLAN.get())
                            + ", project=" + project
                            + ", worksite=" + projectWorksiteAssigned
                            + ", profession=" + recruit.getWorkerProfession()
                            + ", command=" + recruit.getRecruitCommand()
                            + ", indexed=" + (level.getEntity(recruit.getUUID()) == recruit));
                    return;
                }
                assignedProject[0] = project;
                return;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            withdrawNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("withdraw_build_material");
            buildNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("build_place");
            movedPhysically[0] |= recruit.position().distanceToSqr(initialRecruitPosition) > 4.0D;
            status.workOrderId().ifPresent(currentOrderId -> {
                if (workOrderId[0] == null) {
                    workOrderId[0] = currentOrderId;
                } else if (!workOrderId[0].equals(currentOrderId)) {
                    complete[0] = true;
                    helper.fail("Black-box builder replaced its live persisted work order: "
                            + workOrderId[0] + "->" + currentOrderId);
                }
            });
            if (complete[0]) {
                return;
            }

            BuildProject current = data.kingdomForOwner(owner.getUUID()).orElseThrow().settlement()
                    .buildProjects().stream()
                    .filter(candidate -> candidate.id().equals(assignedProject[0].id()))
                    .findFirst().orElseThrow();
            int placedBlocks = 0;
            for (int placementIndex = 0;
                    placementIndex < blueprint.placements().size();
                    placementIndex++) {
                BaseBlockPlacement placement = blueprint.rotatedPlacement(placementIndex, 0);
                BlockPos placementPos = origin.offset(
                        placement.x(), placement.y(), placement.z());
                if (level.getBlockState(placementPos).is(ModBlocks.DURACRETE.get())) {
                    placedBlocks++;
                }
            }
            int persistedPlacements = current.completedPlacements().size();
            int storedMaterials = countContainerItem(hall, ModItems.DURACRETE.get());
            int carriedMaterials = workerInventoryCount(recruit);
            int conservedMaterials = storedMaterials + carriedMaterials + placedBlocks;
            if (persistedPlacements < lastPersistedPlacements[0]
                    || persistedPlacements != placedBlocks
                    || conservedMaterials != initialMaterials) {
                complete[0] = true;
                helper.fail("Black-box builder violated monotonic physical conservation: stored="
                        + storedMaterials + ", carried=" + carriedMaterials
                        + ", placed=" + placedBlocks + ", persisted=" + persistedPlacements
                        + ", previous=" + lastPersistedPlacements[0]
                        + ", expected=" + initialMaterials + ", status=" + status);
                return;
            }
            lastPersistedPlacements[0] = persistedPlacements;

            if (status.phase() == WorkerPhase.BLOCKED
                    && status.reasonCode().equals("build_material_missing")) {
                complete[0] = true;
                helper.fail("Black-box builder exhausted physical material before completion: stored="
                        + storedMaterials + ", carried=" + carriedMaterials
                        + ", placed=" + placedBlocks + ", persisted=" + persistedPlacements
                        + ", order=" + workOrderId[0]);
                return;
            }

            WorkOrder completedOrder = workOrderId[0] == null
                    ? null
                    : data.workOrder(owner.getUUID(), workOrderId[0]).orElse(null);
            int buildingEventsAfter = ProgressionSavedData.get(level).state(owner.getUUID())
                    .total(ProgressionEventType.BUILDING_COMPLETED);
            if (current.state()
                            == galacticwars.clonewars.kingdom.BuildProjectState.COMPLETED
                    && placedBlocks == 9
                    && storedMaterials == 0
                    && carriedMaterials == 0
                    && completedOrder != null
                    && completedOrder.type() == WorkOrderType.BUILD
                    && completedOrder.state() == WorkOrderState.COMPLETED
                    && buildingEventsAfter == buildingEventsBefore + 1
                    && withdrawNavigationObserved[0]
                    && buildNavigationObserved[0]
                    && movedPhysically[0]
                    && recruit.getRecruitCommand() == RecruitmentAction.FOLLOW_OWNER
                    && recruit.getBaseTarget() == null
                    && recruit.getWorkTarget() == null) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 1_300) {
                complete[0] = true;
                helper.fail("Black-box builder lifecycle timed out: status=" + status
                        + ", project=" + current
                        + ", stored=" + storedMaterials
                        + ", carried=" + carriedMaterials
                        + ", placed=" + placedBlocks
                        + ", order=" + completedOrder
                        + ", events=" + buildingEventsBefore + "->" + buildingEventsAfter
                        + ", navigation=" + withdrawNavigationObserved[0] + "/"
                        + buildNavigationObserved[0]
                        + ", moved=" + movedPhysically[0]);
            }
        });
    }

    /**
     * Exercises one manual delivery through ordinary Survival hire, marker-selected worksite
     * and storage actions, the replay-safe worksite menu, SmartBrain navigation, physical cargo,
     * persisted order completion, and progression. The fixture never mutates a worker phase,
     * invokes the controller, or moves the recruit after assignment.
     */
    private static void blackBoxCourierLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                18,
                -2,
                10,
                isolatedCapital(helper, 229));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos destination = area.at(8, 1, 2);
        BlockPos recruitPos = area.at(14, 1, 6);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Black-box courier fixture could not activate its Command Center");
            return;
        }
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 128));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        level.setBlock(destination, Blocks.CHEST.defaultBlockState(), 3);
        if (!(level.getBlockEntity(destination) instanceof Container destinationStorage)) {
            helper.fail("Black-box courier fixture could not place its destination chest");
            return;
        }
        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
        if (!recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !recruit.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
            helper.fail("Black-box courier could not be hired and assigned");
            return;
        }
        ItemStack marker = new ItemStack(ModItems.COMMAND_MARKER.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, marker);

        int initialMaterials = 3;
        if (initialMaterials > 0) {
            putContainerItem(hall, new ItemStack(ModItems.DURACRETE.get(), initialMaterials));
        }
        int deliveryEventsBefore = ProgressionSavedData.get(level).state(owner.getUUID())
                .total(ProgressionEventType.DELIVERY_COMPLETED);
        Container recruitCargo = recruit.createCargoContainer();
        Vec3 initialRecruitPosition = recruit.position();
        boolean[] configured = {false};
        boolean[] sourceNavigationObserved = {false};
        boolean[] destinationNavigationObserved = {false};
        boolean[] sourceApproached = {false};
        boolean[] destinationApproached = {false};
        boolean[] cargoObserved = {false};
        boolean[] movedPhysically = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        UUID[] workOrderId = {null};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (!configured[0]) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 18, -2, 10);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box courier area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }

                InteractionResult worksiteSelection = marker.getItem().useOn(new UseOnContext(
                        owner,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(
                                Vec3.atCenterOf(destination),
                                Direction.UP,
                                destination,
                                false)));
                boolean worksiteAccepted = recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_SET_WORKSITE);
                InteractionResult storageSelection = marker.getItem().useOn(new UseOnContext(
                        owner,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(
                                Vec3.atCenterOf(hallPos),
                                Direction.UP,
                                hallPos,
                                false)));
                boolean storageAccepted = recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_SET_STORAGE);
                owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
                level.getChunkSource().move(owner);
                WorksiteConfigurationMenu menu = (WorksiteConfigurationMenu)
                        WorksiteConfigurationMenuProvider.prepare(owner, recruit)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Black-box courier did not expose its worksite menu"))
                                .createMenu(51, owner.getInventory(), owner);
                CourierDispatchMode initialDispatch = menu.snapshot().dispatchMode();
                boolean hybridAccepted = menu.handleReplayAction(
                        owner,
                        new WorksiteActionPayload(
                                UUID.randomUUID(),
                                menu.containerId,
                                WorksiteConfigurationAction.CYCLE_DISPATCH_MODE.id(),
                                menu.snapshot().configurationRevision(),
                                -1));
                CourierDispatchMode intermediateDispatch = menu.snapshot().dispatchMode();
                boolean manualAccepted = menu.handleReplayAction(
                        owner,
                        new WorksiteActionPayload(
                                UUID.randomUUID(),
                                menu.containerId,
                                WorksiteConfigurationAction.CYCLE_DISPATCH_MODE.id(),
                                menu.snapshot().configurationRevision(),
                                -1));
                WorksiteRecord worksite = data.assignedWorksite(
                        owner.getUUID(), recruit.getUUID()).orElse(null);
                boolean durableStorage = worksite != null
                        && worksite.storageEndpoints().stream().anyMatch(endpoint ->
                                endpoint.dimensionId().equals(
                                        level.dimension().identifier().toString())
                                        && endpoint.x() == hallPos.getX()
                                        && endpoint.y() == hallPos.getY()
                                        && endpoint.z() == hallPos.getZ());
                double initialSourceDistance = recruit.distanceToSqr(Vec3.atCenterOf(hallPos));
                double initialDestinationDistance = recruit.distanceToSqr(
                        Vec3.atCenterOf(destination));
                if (worksiteSelection != InteractionResult.SUCCESS
                        || !worksiteAccepted
                        || storageSelection != InteractionResult.SUCCESS
                        || !storageAccepted
                        || initialDispatch != CourierDispatchMode.AUTOMATIC
                        || !hybridAccepted
                        || intermediateDispatch != CourierDispatchMode.HYBRID
                        || !manualAccepted
                        || menu.snapshot().dispatchMode() != CourierDispatchMode.MANUAL
                        || worksite == null
                        || worksite.x() != destination.getX()
                        || worksite.y() != destination.getY()
                        || worksite.z() != destination.getZ()
                        || worksite.configuration().courierDispatchMode()
                                != CourierDispatchMode.MANUAL
                        || !durableStorage
                        || !hallPos.equals(recruit.getStorageTarget())
                        || !destination.equals(recruit.getWorkTarget())
                        || recruit.getRecruitCommand() != RecruitmentAction.WORK_AT_SITE
                        || initialSourceDistance <= 16.0D
                        || initialDestinationDistance <= 16.0D) {
                    complete[0] = true;
                    helper.fail("Black-box courier setup did not cross its player-facing authority: "
                            + "selection=" + worksiteSelection + "/" + storageSelection
                            + ", actions=" + worksiteAccepted + "/" + storageAccepted
                            + ", dispatch=" + initialDispatch + "->" + intermediateDispatch
                            + "->" + menu.snapshot().dispatchMode()
                            + ", worksite=" + worksite
                            + ", storage=" + recruit.getStorageTarget()
                            + ", target=" + recruit.getWorkTarget()
                            + ", command=" + recruit.getRecruitCommand()
                            + ", distances=" + initialSourceDistance + "/"
                            + initialDestinationDistance);
                    return;
                }
                configured[0] = true;
                startedRecruitTick[0] = recruit.tickCount;
                return;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            sourceNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("courier_withdraw");
            destinationNavigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_STORAGE
                    && status.reasonCode().equals("courier_deliver");
            sourceApproached[0] |= recruit.distanceToSqr(Vec3.atCenterOf(hallPos)) <= 4.0D;
            destinationApproached[0] |= recruit.distanceToSqr(
                    Vec3.atCenterOf(destination)) <= 4.0D;
            movedPhysically[0] |= recruit.position().distanceToSqr(initialRecruitPosition) > 4.0D;
            status.workOrderId().ifPresent(currentOrderId -> {
                if (workOrderId[0] == null) {
                    workOrderId[0] = currentOrderId;
                } else if (!workOrderId[0].equals(currentOrderId)) {
                    complete[0] = true;
                    helper.fail("Black-box courier replaced its live persisted work order: "
                            + workOrderId[0] + "->" + currentOrderId);
                }
            });
            if (complete[0]) {
                return;
            }

            int storedMaterials = countContainerItem(hall, ModItems.DURACRETE.get());
            int carriedMaterials = countContainerItem(
                    recruitCargo, ModItems.DURACRETE.get());
            int deliveredMaterials = countContainerItem(
                    destinationStorage, ModItems.DURACRETE.get());
            cargoObserved[0] |= carriedMaterials == initialMaterials
                    && initialMaterials > 0
                    && storedMaterials == 0
                    && deliveredMaterials == 0;
            if (storedMaterials + carriedMaterials + deliveredMaterials != initialMaterials
                    || carriedMaterials != workerInventoryCount(recruit)) {
                complete[0] = true;
                helper.fail("Black-box courier violated exact physical conservation: source="
                        + storedMaterials + ", cargo=" + carriedMaterials
                        + ", destination=" + deliveredMaterials
                        + ", totalCargo=" + workerInventoryCount(recruit)
                        + ", expected=" + initialMaterials + ", status=" + status);
                return;
            }
            if (status.phase() == WorkerPhase.BLOCKED
                    && status.reasonCode().equals("courier_source_empty")) {
                complete[0] = true;
                WorkOrder blockedOrder = workOrderId[0] == null
                        ? null
                        : data.workOrder(owner.getUUID(), workOrderId[0]).orElse(null);
                helper.fail("Black-box courier reached the registered source without physical cargo: "
                        + "source=" + storedMaterials + ", cargo=" + carriedMaterials
                        + ", destination=" + deliveredMaterials
                        + ", navigation=" + sourceNavigationObserved[0]
                        + ", approached=" + sourceApproached[0]
                        + ", moved=" + movedPhysically[0]
                        + ", order=" + blockedOrder);
                return;
            }

            WorkOrder completedOrder = workOrderId[0] == null
                    ? null
                    : data.workOrder(owner.getUUID(), workOrderId[0]).orElse(null);
            int deliveryEventsAfter = ProgressionSavedData.get(level).state(owner.getUUID())
                    .total(ProgressionEventType.DELIVERY_COMPLETED);
            if (storedMaterials == 0
                    && carriedMaterials == 0
                    && deliveredMaterials == initialMaterials
                    && completedOrder != null
                    && completedOrder.type() == WorkOrderType.COURIER
                    && completedOrder.state() == WorkOrderState.COMPLETED
                    && deliveryEventsAfter == deliveryEventsBefore + 1
                    && sourceNavigationObserved[0]
                    && destinationNavigationObserved[0]
                    && sourceApproached[0]
                    && destinationApproached[0]
                    && cargoObserved[0]
                    && movedPhysically[0]
                    && recruit.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 800) {
                complete[0] = true;
                helper.fail("Black-box courier lifecycle timed out: status=" + status
                        + ", source=" + storedMaterials
                        + ", cargo=" + carriedMaterials
                        + ", destination=" + deliveredMaterials
                        + ", order=" + completedOrder
                        + ", events=" + deliveryEventsBefore + "->" + deliveryEventsAfter
                        + ", navigation=" + sourceNavigationObserved[0] + "/"
                        + destinationNavigationObserved[0]
                        + ", approached=" + sourceApproached[0] + "/"
                        + destinationApproached[0]
                        + ", cargoObserved=" + cargoObserved[0]
                        + ", moved=" + movedPhysically[0]);
            }
        });
    }

    /**
     * Exercises the ordinary Survival technician contract and Command Center research actions.
     * The fixture never calls the research tick service, mutates a worker phase, invokes the
     * worker controller, or moves the recruit after research assignment.
     */
    private static void blackBoxTechnicianLifecycle(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.SURVIVAL,
                -2,
                24,
                -2,
                6,
                isolatedCapital(helper, 230));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(0, 1, 0);
        BlockPos recruitPos = area.at(20, 1, 0);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Black-box technician fixture could not activate its Command Center");
            return;
        }
        owner.getInventory().add(new ItemStack(ModItems.CREDIT_CHIP.get(), 128));
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity recruit = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), recruitPos);
        Vec3 initialRecruitPosition = recruit.position();
        boolean[] configured = {false};
        boolean[] navigationObserved = {false};
        boolean[] arrivalObserved = {false};
        boolean[] researchingObserved = {false};
        boolean[] movedPhysically = {false};
        boolean[] progressObserved = {false};
        boolean[] complete = {false};
        int[] startedRecruitTick = {-1};
        int[] lastProgress = {0};
        long readinessStartedAt = helper.getTick();
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            if (!configured[0]) {
                boolean areaTicking = areSmartBrainAreaChunksTicking(
                        helper, area, -2, 24, -2, 6);
                boolean recruitIndexed = level.getEntity(recruit.getUUID()) == recruit;
                if (!areaTicking || !recruitIndexed || recruit.tickCount == 0) {
                    if (helper.getTick() - readinessStartedAt
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        complete[0] = true;
                        helper.fail("Black-box technician area never became entity-ticking: ticking="
                                + areaTicking + ", recruit=" + recruitIndexed
                                + ", recruitTick=" + recruit.tickCount);
                    }
                    return;
                }

                owner.setPos(recruit.getX(), recruit.getY(), recruit.getZ());
                level.getChunkSource().move(owner);
                boolean hired = recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_HIRE);
                boolean professionAssigned = hired && recruit.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_TECHNICIAN);
                owner.setPos(
                        hallPos.getX() + 0.5D,
                        hallPos.getY(),
                        hallPos.getZ() + 0.5D);
                level.getChunkSource().move(owner);
                CommandCenterOperationsMenu operations = (CommandCenterOperationsMenu)
                        new CommandCenterOperationsMenuProvider(hallPos)
                                .createMenu(52, owner.getInventory(), owner);
                owner.containerMenu = operations;
                boolean technicianListed = operations.dashboardState().workers().stream()
                        .anyMatch(summary -> summary.entityId().equals(recruit.getUUID())
                                && summary.profession().equals("technician"));
                if (!hired || !professionAssigned || !operations.stillValid(owner)
                        || !technicianListed
                        || operations.dashboardState().contentGeneration()
                                != GameplayDataManager.generation()) {
                    complete[0] = true;
                    helper.fail("Black-box technician did not cross its player-facing setup: hired="
                            + hired + ", profession=" + professionAssigned
                            + ", menu=" + operations.stillValid(owner)
                            + ", listed=" + technicianListed
                            + ", generation="
                            + operations.dashboardState().contentGeneration() + "/"
                            + GameplayDataManager.generation());
                    return;
                }

                putContainerItem(hall, new ItemStack(Items.IRON_INGOT, 16));
                putContainerItem(hall, new ItemStack(Items.REDSTONE, 8));
                int initialRevision = data.technologyStateOrDefault(kingdom.id()).revision();
                ResearchResult started = KingdomResearchService.start(
                        owner,
                        hall,
                        "galacticwars:field_fabrication",
                        UUID.randomUUID(),
                        initialRevision);
                ResearchResult contributed = started.accepted()
                        ? KingdomResearchService.contribute(
                                owner,
                                hall,
                                UUID.randomUUID(),
                                started.revision())
                        : ResearchResult.rejected("start_failed", initialRevision);
                double assignmentDistance = recruit.distanceToSqr(Vec3.atCenterOf(hallPos));
                ResearchResult assigned = contributed.accepted()
                        ? KingdomResearchService.assignTechnician(
                                owner,
                                hall,
                                recruit.getUUID(),
                                UUID.randomUUID(),
                                contributed.revision())
                        : ResearchResult.rejected("contribution_failed", contributed.revision());
                KingdomTechnologyState assignedState = data.technologyStateOrDefault(kingdom.id());
                var assignedProject = assignedState.activeProject().orElse(null);
                if (!started.accepted() || !started.changed()
                        || !contributed.accepted() || !contributed.changed()
                        || countContainerItem(hall, Items.IRON_INGOT) != 0
                        || countContainerItem(hall, Items.REDSTONE) != 0
                        || assignmentDistance <= 64.0D
                        || !assigned.accepted() || !assigned.changed()
                        || assignedProject == null
                        || assignedProject.technicianId()
                                .filter(recruit.getUUID()::equals).isEmpty()
                        || assignedProject.deliveredInputs()
                                .getOrDefault("minecraft:iron_ingot", 0) != 16
                        || assignedProject.deliveredInputs()
                                .getOrDefault("minecraft:redstone", 0) != 8
                        || !hallPos.equals(recruit.getWorkTarget())
                        || recruit.getRecruitCommand() != RecruitmentAction.WORK_AT_SITE) {
                    complete[0] = true;
                    helper.fail("Black-box distant technician assignment failed before its travel loop: "
                            + "start=" + started + ", contribution=" + contributed
                            + ", assignment=" + assigned
                            + ", distance=" + assignmentDistance
                            + ", project=" + assignedProject
                            + ", iron=" + countContainerItem(hall, Items.IRON_INGOT)
                            + ", redstone=" + countContainerItem(hall, Items.REDSTONE)
                            + ", status=" + recruit.getWorkerStatus());
                    return;
                }
                configured[0] = true;
                startedRecruitTick[0] = recruit.tickCount;
                return;
            }

            WorkerStatus status = recruit.getWorkerStatus();
            navigationObserved[0] |= status.phase() == WorkerPhase.NAVIGATE_SOURCE
                    && status.reasonCode().equals("travel_to_command_center");
            arrivalObserved[0] |= recruit.distanceToSqr(Vec3.atCenterOf(hallPos)) <= 64.0D;
            researchingObserved[0] |= status.phase() == WorkerPhase.COOLDOWN
                    && status.reasonCode().equals("researching");
            movedPhysically[0] |= recruit.position().distanceToSqr(initialRecruitPosition) > 4.0D;

            KingdomTechnologyState current = data.technologyStateOrDefault(kingdom.id());
            var project = current.activeProject().orElse(null);
            if (project != null) {
                int progress = project.workProgress();
                int progressDelta = progress - lastProgress[0];
                if (progressDelta < 0 || progressDelta > 20
                        || Math.floorMod(progressDelta, 20) != 0
                        || countContainerItem(hall, Items.IRON_INGOT) != 0
                        || countContainerItem(hall, Items.REDSTONE) != 0
                        || project.deliveredInputs()
                                .getOrDefault("minecraft:iron_ingot", 0) != 16
                        || project.deliveredInputs()
                                .getOrDefault("minecraft:redstone", 0) != 8) {
                    complete[0] = true;
                    helper.fail("Black-box technician violated monotonic research conservation: "
                            + "progress=" + lastProgress[0] + "->" + progress
                            + ", project=" + project
                            + ", iron=" + countContainerItem(hall, Items.IRON_INGOT)
                            + ", redstone=" + countContainerItem(hall, Items.REDSTONE)
                            + ", status=" + status);
                    return;
                }
                progressObserved[0] |= progress > 0;
                lastProgress[0] = progress;
            }

            if (current.completed("galacticwars:field_fabrication")
                    && current.activeProject().isEmpty()) {
                Tag encoded = KingdomSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
                KingdomSavedData restored = KingdomSavedData.CODEC.parse(
                        NbtOps.INSTANCE, encoded).getOrThrow();
                boolean durableCompletion = restored.technologyStateOrDefault(kingdom.id())
                        .completed("galacticwars:field_fabrication");
                if (!navigationObserved[0]
                        || !arrivalObserved[0]
                        || !researchingObserved[0]
                        || !movedPhysically[0]
                        || !progressObserved[0]
                        || lastProgress[0] < 1_180
                        || countContainerItem(hall, Items.IRON_INGOT) != 0
                        || countContainerItem(hall, Items.REDSTONE) != 0
                        || recruit.getRecruitCommand() != RecruitmentAction.FOLLOW_OWNER
                        || status.phase() != WorkerPhase.BLOCKED
                        || !status.reasonCode().equals("awaiting_research")
                        || !durableCompletion) {
                    complete[0] = true;
                    helper.fail("Black-box technician completed without its embodied lifecycle: "
                            + "navigation=" + navigationObserved[0]
                            + ", arrival=" + arrivalObserved[0]
                            + ", researching=" + researchingObserved[0]
                            + ", moved=" + movedPhysically[0]
                            + ", progress=" + lastProgress[0]
                            + ", durable=" + durableCompletion
                            + ", status=" + status);
                    return;
                }
                complete[0] = true;
                recruit.discard();
                helper.succeed();
                return;
            }

            if (recruit.tickCount - startedRecruitTick[0] >= 1_700) {
                complete[0] = true;
                helper.fail("Black-box technician lifecycle timed out: status=" + status
                        + ", project=" + project
                        + ", navigation=" + navigationObserved[0]
                        + ", arrival=" + arrivalObserved[0]
                        + ", researching=" + researchingObserved[0]
                        + ", moved=" + movedPhysically[0]
                        + ", progress=" + lastProgress[0]);
            }
        });
    }

    /**
     * Exercises automatic courier contention without setting private phases,
     * invoking the worker controller, or moving either courier after assignment.
     */
    private static void competingCourierLeases(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 215));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(1, 1, 3);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Competing courier fixture could not activate its kingdom");
            return;
        }
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity firstCourier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(3, 1, 2));
        GalacticRecruitEntity secondCourier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(3, 1, 4));
        GalacticRecruitEntity requester = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(11, 1, 3));
        requester.initializeFromSpawnEgg();
        requester.tame(owner);
        requester.setNoAi(true);
        if (!data.registerRecruit(
                owner.getUUID(), requester.getUUID(), NpcServiceBranch.CIVILIAN)) {
            helper.fail("Competing courier requester could not join the settlement");
            return;
        }
        for (GalacticRecruitEntity courier : List.of(firstCourier, secondCourier)) {
            courier.initializeFromSpawnEgg();
            owner.setPos(courier.getX(), courier.getY(), courier.getZ());
            if (!courier.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                    || !courier.handleMenuButton(
                            owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
                helper.fail("Competing courier could not be hired and assigned: "
                        + courier.getUUID());
                return;
            }
        }

        putContainerItem(hall, new ItemStack(ModItems.ENERGY_CELL.get(), 4));
        SupplyDemand demand = new SupplyDemand(
                UUID.randomUUID(),
                SupplyCategory.AMMUNITION,
                "galacticwars:energy_cell",
                6,
                0,
                100,
                "recruit/" + requester.getUUID() + "/competing_couriers");
        if (!data.requestSupply(owner.getUUID(), kingdom.settlement().id(), demand)) {
            helper.fail("Competing courier demand was rejected");
            return;
        }

        long startedAt = helper.getTick();
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            SettlementSupplyLedger ledger = data.supplyLedger(
                    kingdom.settlement().id()).orElseThrow();
            long activeReservations = ledger.reservations().stream()
                    .filter(reservation -> reservation.active(level.getGameTime()))
                    .count();
            if (activeReservations > 1) {
                complete[0] = true;
                helper.fail("Competing couriers double-reserved partial stock: "
                        + ledger.reservations());
                return;
            }

            int sourceStock = countContainerItem(hall, ModItems.ENERGY_CELL.get());
            int firstCargo = countContainerItem(
                    firstCourier.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int secondCargo = countContainerItem(
                    secondCourier.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int requesterCargo = countContainerItem(
                    requester.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int physicalTotal = sourceStock + firstCargo + secondCargo + requesterCargo;
            if (physicalTotal != 4) {
                complete[0] = true;
                helper.fail("Competing courier transfer violated physical conservation: source="
                        + sourceStock + ", first=" + firstCargo + ", second=" + secondCargo
                        + ", requester=" + requesterCargo + ", ledger=" + ledger);
                return;
            }

            SupplyDemand currentDemand = ledger.demands().stream()
                    .filter(candidate -> candidate.id().equals(demand.id()))
                    .findFirst().orElseThrow();
            long completedReservations = ledger.reservations().stream()
                    .filter(reservation -> reservation.state()
                            == SupplyReservation.State.COMPLETED)
                    .count();
            if (requesterCargo == 4
                    && sourceStock == 0
                    && firstCargo == 0
                    && secondCargo == 0
                    && currentDemand.outstandingQuantity() == 2
                    && completedReservations == 1) {
                complete[0] = true;
                firstCourier.discard();
                secondCourier.discard();
                requester.discard();
                helper.succeed();
                return;
            }
            if (helper.getTick() - startedAt >= 650) {
                complete[0] = true;
                helper.fail("Competing courier delivery timed out: source=" + sourceStock
                        + ", first=" + firstCourier.getWorkerStatus()
                        + ", second=" + secondCourier.getWorkerStatus()
                        + ", requester=" + requesterCargo
                        + ", demand=" + currentDemand
                        + ", reservations=" + ledger.reservations());
            }
        });
    }

    /**
     * Proves that an exact automatic delivery retries without partial mutation and that its
     * physical cargo plus durable lease survive a real entity save/unload/load boundary.
     */
    private static void courierLiveLeaseReload(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 216));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(1, 1, 3);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Live-lease reload fixture could not activate its kingdom");
            return;
        }
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity courier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(3, 1, 3));
        GalacticRecruitEntity requester = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(11, 1, 3));
        requester.initializeFromSpawnEgg();
        requester.tame(owner);
        requester.setNoAi(true);
        if (!data.registerRecruit(
                owner.getUUID(), requester.getUUID(), NpcServiceBranch.CIVILIAN)) {
            helper.fail("Live-lease reload requester could not join the settlement");
            return;
        }
        courier.initializeFromSpawnEgg();
        owner.setPos(courier.getX(), courier.getY(), courier.getZ());
        if (!courier.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !courier.handleMenuButton(owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
            helper.fail("Live-lease reload courier could not be hired and assigned");
            return;
        }

        Container requesterCargo = requester.createCargoContainer();
        for (int slot = 0; slot < requesterCargo.getContainerSize(); slot++) {
            requesterCargo.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        putContainerItem(hall, new ItemStack(ModItems.ENERGY_CELL.get(), 4));
        SupplyDemand demand = new SupplyDemand(
                UUID.randomUUID(),
                SupplyCategory.AMMUNITION,
                "galacticwars:energy_cell",
                4,
                0,
                100,
                "recruit/" + requester.getUUID() + "/live_lease_reload");
        UUID settlementId = kingdom.settlement().id();
        if (!data.requestSupply(owner.getUUID(), settlementId, demand)) {
            helper.fail("Live-lease reload demand was rejected");
            return;
        }

        UUID courierId = courier.getUUID();
        GalacticRecruitEntity[] currentCourier = {courier};
        CompoundTag[] persistedCourier = {null};
        UUID[] reservationId = {null};
        int[] phase = {0};
        long startedAt = helper.getTick();
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            SettlementSupplyLedger ledger = data.supplyLedger(settlementId).orElseThrow();
            SupplyDemand currentDemand = ledger.demands().stream()
                    .filter(candidate -> candidate.id().equals(demand.id()))
                    .findFirst().orElseThrow();
            List<SupplyReservation> activeReservations = ledger.reservations().stream()
                    .filter(reservation -> reservation.workerId().equals(courierId))
                    .filter(reservation -> reservation.active(level.getGameTime()))
                    .toList();
            if (activeReservations.size() > 1) {
                complete[0] = true;
                helper.fail("Reloading courier created duplicate active leases: "
                        + activeReservations);
                return;
            }

            GalacticRecruitEntity activeCourier = currentCourier[0];
            int sourceCells = countContainerItem(hall, ModItems.ENERGY_CELL.get());
            int courierCells = countContainerItem(
                    activeCourier.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int requesterCells = countContainerItem(
                    requesterCargo, ModItems.ENERGY_CELL.get());
            if (sourceCells + courierCells + requesterCells != 4) {
                complete[0] = true;
                helper.fail("Live-lease reload violated physical conservation: source="
                        + sourceCells + ", courier=" + courierCells
                        + ", requester=" + requesterCells + ", phase=" + phase[0]);
                return;
            }

            switch (phase[0]) {
                case 0 -> {
                    if (requesterCells != 0 || currentDemand.outstandingQuantity() != 4) {
                        complete[0] = true;
                        helper.fail("Full recipient was partially mutated before exact retry: requester="
                                + requesterCells + ", demand=" + currentDemand);
                        return;
                    }
                    if (courierCells == 4
                            && activeCourier.getWorkerStatus().reasonCode()
                                    .equals("recipient_inventory_full")) {
                        if (sourceCells != 0 || activeReservations.size() != 1) {
                            complete[0] = true;
                            helper.fail("Exact retry did not retain one loaded lease: source="
                                    + sourceCells + ", leases=" + activeReservations);
                            return;
                        }
                        reservationId[0] = activeReservations.getFirst().id();
                        persistedCourier[0] = saveRecruit(activeCourier, level);
                        activeCourier.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                        if (!activeCourier.isRemoved()
                                || data.supplyLedger(settlementId).orElseThrow()
                                        .reservation(reservationId[0])
                                        .filter(candidate -> candidate.active(level.getGameTime()))
                                        .isEmpty()) {
                            complete[0] = true;
                            helper.fail("Chunk-unload removal released the live courier lease");
                            return;
                        }
                        phase[0] = 1;
                    }
                }
                case 1 -> {
                    GalacticRecruitEntity reloaded = loadRecruit(persistedCourier[0], level);
                    if (!reloaded.getUUID().equals(courierId)
                            || !level.addFreshEntity(reloaded)) {
                        complete[0] = true;
                        helper.fail("Serialized live-lease courier could not rejoin the server");
                        return;
                    }
                    currentCourier[0] = reloaded;
                    int reloadedCells = countContainerItem(
                            reloaded.createCargoContainer(), ModItems.ENERGY_CELL.get());
                    SettlementSupplyLedger reloadedLedger = data.supplyLedger(
                            settlementId).orElseThrow();
                    long matchingActiveLeases = reloadedLedger.reservations().stream()
                            .filter(candidate -> candidate.id().equals(reservationId[0]))
                            .filter(candidate -> candidate.workerId().equals(courierId))
                            .filter(candidate -> candidate.active(level.getGameTime()))
                            .count();
                    if (reloadedCells != 4 || matchingActiveLeases != 1L) {
                        complete[0] = true;
                        helper.fail("Reload lost physical cargo or original lease: cargo="
                                + reloadedCells + ", leases=" + reloadedLedger.reservations());
                        return;
                    }
                    requesterCargo.setItem(8, ItemStack.EMPTY);
                    phase[0] = 2;
                }
                case 2 -> {
                    SupplyReservation persistedReservation = ledger.reservation(
                            reservationId[0]).orElse(null);
                    if (requesterCells == 4) {
                        if (sourceCells != 0
                                || courierCells != 0
                                || currentDemand.outstandingQuantity() != 0
                                || persistedReservation == null
                                || persistedReservation.state()
                                        != SupplyReservation.State.COMPLETED
                                || activeReservations.size() != 0) {
                            complete[0] = true;
                            helper.fail("Reloaded courier did not complete the original exact lease: source="
                                    + sourceCells + ", courier=" + courierCells
                                    + ", requester=" + requesterCells
                                    + ", demand=" + currentDemand
                                    + ", reservation=" + persistedReservation);
                            return;
                        }
                        complete[0] = true;
                        currentCourier[0].discard();
                        requester.discard();
                        helper.succeed();
                    }
                }
                default -> throw new IllegalStateException(
                        "Unknown live-lease reload phase " + phase[0]);
            }

            if (!complete[0] && helper.getTick() - startedAt >= 650) {
                complete[0] = true;
                helper.fail("Live-lease reload delivery timed out: phase=" + phase[0]
                        + ", courier=" + currentCourier[0].getWorkerStatus()
                        + ", source=" + sourceCells + ", courierCargo=" + courierCells
                        + ", requester=" + requesterCells
                        + ", demand=" + currentDemand
                        + ", reservations=" + ledger.reservations());
            }
        });
    }

    /**
     * Proves that owner removal of the authoritative Command Center releases an in-flight
     * automatic lease, pauses a configured route, and preserves every physical item.
     */
    private static void courierHallRemoval(GameTestHelper helper) {
        SmartBrainTestArea area = prepareSmartBrainTestAreaAt(
                helper,
                GameType.CREATIVE,
                -2,
                14,
                -2,
                8,
                isolatedCapital(helper, 217));
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = area.player();
        BlockPos hallPos = area.at(1, 1, 3);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Courier hall-removal fixture could not activate its kingdom");
            return;
        }
        FactionAlignmentSavedData.get(level).setScore(
                owner.getUUID(), FactionId.of("republic"), 100);
        applyCampaignSetupEvent(
                ProgressionSavedData.get(level),
                owner,
                ProgressionEventType.FACTION_PLEDGED,
                "galacticwars:republic");

        GalacticRecruitEntity firstCourier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(3, 1, 3));
        GalacticRecruitEntity secondCourier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(3, 1, 6));
        GalacticRecruitEntity requester = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), area.at(11, 1, 3));
        requester.initializeFromSpawnEgg();
        requester.tame(owner);
        requester.setNoAi(true);
        if (!data.registerRecruit(
                owner.getUUID(), requester.getUUID(), NpcServiceBranch.CIVILIAN)) {
            helper.fail("Courier hall-removal requester could not join the settlement");
            return;
        }
        firstCourier.initializeFromSpawnEgg();
        secondCourier.initializeFromSpawnEgg();
        owner.setPos(firstCourier.getX(), firstCourier.getY(), firstCourier.getZ());
        if (!firstCourier.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !firstCourier.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
            helper.fail("First hall-removal courier could not be hired and assigned");
            return;
        }
        owner.setPos(secondCourier.getX(), secondCourier.getY(), secondCourier.getZ());
        if (!secondCourier.handleMenuButton(owner, RecruitCommandMenu.BUTTON_HIRE)
                || !secondCourier.handleMenuButton(
                        owner, RecruitCommandMenu.BUTTON_ASSIGN_COURIER)) {
            helper.fail("Second hall-removal courier could not be hired and assigned");
            return;
        }

        WorksiteRecord sharedWorksite = data.assignedWorksite(
                owner.getUUID(), firstCourier.getUUID()).orElse(null);
        if (sharedWorksite == null
                || data.assignedWorksite(owner.getUUID(), secondCourier.getUUID())
                        .filter(candidate -> candidate.id().equals(sharedWorksite.id()))
                        .isEmpty()) {
            helper.fail("Hall-removal couriers did not share the frontier worksite");
            return;
        }
        var configuration = sharedWorksite.configuration();
        WorksiteUpdateResult dispatchConfigured = data.configureWorksite(
                owner.getUUID(),
                sharedWorksite.id(),
                configuration.revision(),
                configuration.bounds(),
                configuration.kingdomAccess(),
                configuration.priority(),
                configuration.overlayVisible(),
                configuration.itemFilters(),
                CourierDispatchMode.HYBRID);
        if (!dispatchConfigured.accepted()) {
            helper.fail("Hall-removal hybrid dispatch configuration failed: "
                    + dispatchConfigured.reasonCode());
            return;
        }
        String dimensionId = level.dimension().identifier().toString();
        List<CourierWaypoint> route = List.of(
                new CourierWaypoint(
                        dimensionId,
                        area.at(4, 1, 6).getX(),
                        area.at(4, 1, 6).getY(),
                        area.at(4, 1, 6).getZ(),
                        List.of(CourierTransferAction.waitTicks(400))),
                new CourierWaypoint(
                        dimensionId,
                        area.at(10, 1, 6).getX(),
                        area.at(10, 1, 6).getY(),
                        area.at(10, 1, 6).getZ(),
                        List.of(CourierTransferAction.waitTicks(1))));
        WorksiteRecord hybridWorksite = dispatchConfigured.worksite().orElseThrow();
        WorksiteUpdateResult routeConfigured = data.configureWorksiteRoute(
                owner.getUUID(),
                hybridWorksite.id(),
                hybridWorksite.configuration().revision(),
                route,
                CourierRouteMode.LOOP);
        if (!routeConfigured.accepted()) {
            helper.fail("Hall-removal route configuration failed: "
                    + routeConfigured.reasonCode());
            return;
        }

        Container requesterCargo = requester.createCargoContainer();
        for (int slot = 0; slot < requesterCargo.getContainerSize(); slot++) {
            requesterCargo.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        putContainerItem(hall, new ItemStack(ModItems.ENERGY_CELL.get(), 4));
        SupplyDemand demand = new SupplyDemand(
                UUID.randomUUID(),
                SupplyCategory.AMMUNITION,
                "galacticwars:energy_cell",
                4,
                0,
                100,
                "recruit/" + requester.getUUID() + "/hall_removal");
        UUID settlementId = kingdom.settlement().id();
        if (!data.requestSupply(owner.getUUID(), settlementId, demand)) {
            helper.fail("Courier hall-removal demand was rejected");
            return;
        }

        GalacticRecruitEntity[] leaseCourier = {null};
        GalacticRecruitEntity[] routeCourier = {null};
        UUID[] reservationId = {null};
        CourierRouteExecutionState[] frozenRoute = {null};
        int[] phase = {0};
        long[] phaseStartedAt = {helper.getTick()};
        boolean[] complete = {false};
        helper.onEachTick(() -> {
            if (complete[0]) {
                return;
            }
            SettlementSupplyLedger ledger = data.supplyLedger(settlementId).orElseThrow();
            SupplyDemand currentDemand = ledger.demands().stream()
                    .filter(candidate -> candidate.id().equals(demand.id()))
                    .findFirst().orElseThrow();
            List<SupplyReservation> activeReservations = ledger.reservations().stream()
                    .filter(candidate -> candidate.active(level.getGameTime()))
                    .toList();
            int firstCells = countContainerItem(
                    firstCourier.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int secondCells = countContainerItem(
                    secondCourier.createCargoContainer(), ModItems.ENERGY_CELL.get());
            int requesterCells = countContainerItem(
                    requesterCargo, ModItems.ENERGY_CELL.get());
            int hallCells = level.getBlockEntity(hallPos) instanceof Container currentHall
                    ? countContainerItem(currentHall, ModItems.ENERGY_CELL.get())
                    : 0;
            if (firstCells + secondCells + requesterCells + hallCells != 4) {
                complete[0] = true;
                helper.fail("Hall removal violated physical courier conservation: first="
                        + firstCells + ", second=" + secondCells
                        + ", requester=" + requesterCells + ", hall=" + hallCells
                        + ", phase=" + phase[0]);
                return;
            }

            switch (phase[0]) {
                case 0 -> {
                    GalacticRecruitEntity loaded = firstCells == 4
                            ? firstCourier
                            : secondCells == 4 ? secondCourier : null;
                    GalacticRecruitEntity routing = loaded == firstCourier
                            ? secondCourier
                            : loaded == secondCourier ? firstCourier : null;
                    if (loaded != null
                            && routing.courierRouteExecutionState().dwellTicksRemaining() > 0
                            && loaded.getWorkerStatus().reasonCode()
                                    .equals("recipient_inventory_full")
                            && currentDemand.outstandingQuantity() == 4
                            && activeReservations.size() == 1
                            && activeReservations.getFirst().workerId()
                                    .equals(loaded.getUUID())) {
                        leaseCourier[0] = loaded;
                        routeCourier[0] = routing;
                        reservationId[0] = activeReservations.getFirst().id();
                        frozenRoute[0] = routing.courierRouteExecutionState();
                        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
                        if (!owner.gameMode.destroyBlock(hallPos)
                                || !level.getBlockState(hallPos).isAir()
                                || data.isHallActive(owner.getUUID())) {
                            complete[0] = true;
                            helper.fail("Owner block removal did not deactivate the Command Center");
                            return;
                        }
                        SupplyReservation released = data.supplyLedger(settlementId)
                                .flatMap(current -> current.reservation(reservationId[0]))
                                .orElse(null);
                        if (released == null
                                || released.state() != SupplyReservation.State.RELEASED) {
                            complete[0] = true;
                            helper.fail("Command Center removal did not release the active lease: "
                                    + released);
                            return;
                        }
                        phase[0] = 1;
                        phaseStartedAt[0] = helper.getTick();
                    }
                }
                case 1 -> {
                    SupplyReservation released = ledger.reservation(reservationId[0])
                            .orElse(null);
                    if (released == null
                            || released.state() != SupplyReservation.State.RELEASED
                            || !activeReservations.isEmpty()
                            || currentDemand.outstandingQuantity() != 4
                            || countContainerItem(
                                    leaseCourier[0].createCargoContainer(),
                                    ModItems.ENERGY_CELL.get()) != 4
                            || requesterCells != 0
                            || !routeCourier[0].courierRouteExecutionState()
                                    .equals(frozenRoute[0])) {
                        complete[0] = true;
                        helper.fail("Inactive settlement mutated courier state: lease="
                                + released + ", active=" + activeReservations
                                + ", demand=" + currentDemand
                                + ", leaseCargo=" + countContainerItem(
                                        leaseCourier[0].createCargoContainer(),
                                        ModItems.ENERGY_CELL.get())
                                + ", requester=" + requesterCells
                                + ", route=" + routeCourier[0].courierRouteExecutionState());
                        return;
                    }
                    if (helper.getTick() - phaseStartedAt[0] >= 45
                            && leaseCourier[0].getWorkerStatus().phase() == WorkerPhase.PAUSED
                            && routeCourier[0].getWorkerStatus().phase() == WorkerPhase.PAUSED
                            && leaseCourier[0].getWorkerStatus().reasonCode()
                                    .equals("upkeep_unpaid")
                            && routeCourier[0].getWorkerStatus().reasonCode()
                                    .equals("upkeep_unpaid")) {
                        requesterCargo.setItem(8, ItemStack.EMPTY);
                        CommandCenterBlockEntity replacement = placeCommandCenter(helper, hallPos);
                        replacement.claim(owner);
                        replacement.setFaction("galacticwars:republic");
                        if (data.activateHall(
                                owner.getUUID(),
                                replacement.factionId(),
                                dimensionId,
                                hallPos).isEmpty()) {
                            complete[0] = true;
                            helper.fail("Removed Command Center could not be reactivated");
                            return;
                        }
                        phase[0] = 2;
                        phaseStartedAt[0] = helper.getTick();
                    }
                }
                case 2 -> {
                    SupplyReservation released = ledger.reservation(reservationId[0])
                            .orElse(null);
                    int retainedCargo = countContainerItem(
                            leaseCourier[0].createCargoContainer(),
                            ModItems.ENERGY_CELL.get());
                    if (released == null
                            || released.state() != SupplyReservation.State.RELEASED
                            || !activeReservations.isEmpty()
                            || currentDemand.outstandingQuantity() != 4
                            || retainedCargo != 4
                            || requesterCells != 0) {
                        complete[0] = true;
                        helper.fail("Reactivation completed or duplicated a released lease: lease="
                                + released + ", active=" + activeReservations
                                + ", demand=" + currentDemand
                                + ", cargo=" + retainedCargo
                                + ", requester=" + requesterCells);
                        return;
                    }
                    if (!routeCourier[0].courierRouteExecutionState().equals(frozenRoute[0])
                            && routeCourier[0].getWorkerStatus().phase() != WorkerPhase.PAUSED) {
                        complete[0] = true;
                        firstCourier.discard();
                        secondCourier.discard();
                        requester.discard();
                        helper.succeed();
                    }
                }
                default -> throw new IllegalStateException(
                        "Unknown courier hall-removal phase " + phase[0]);
            }

            if (!complete[0] && helper.getTick() - phaseStartedAt[0] >= 700) {
                complete[0] = true;
                helper.fail("Courier hall-removal lifecycle timed out: phase=" + phase[0]
                        + ", first=" + firstCourier.getWorkerStatus()
                        + ", second=" + secondCourier.getWorkerStatus()
                        + ", demand=" + currentDemand
                        + ", reservations=" + ledger.reservations());
            }
        });
    }

    private static void hybridCourierDispatch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        BlockPos hallPos = isolatedCapital(helper, 214);
        ChunkPos fixtureChunk = new ChunkPos(hallPos.getX() >> 4, hallPos.getZ() >> 4);
        level.getChunkSource().updateChunkForced(fixtureChunk, true);
        level.getChunk(fixtureChunk.x(), fixtureChunk.z());
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        hall.claim(owner);
        hall.setFaction("galacticwars:republic");

        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord kingdom = data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                level.dimension().identifier().toString(),
                hallPos).orElse(null);
        if (kingdom == null) {
            helper.fail("Hybrid courier fixture could not activate its isolated kingdom");
            return;
        }

        GalacticRecruitEntity courier = spawnRecruitAt(
                helper, ModEntityTypes.CLONE_TROOPER.get(), hallPos.offset(2, 0, 0));
        GalacticRecruitEntity requester = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(3, 1, 3));
        courier.initializeFromSpawnEgg();
        requester.initializeFromSpawnEgg();
        courier.setNoAi(true);
        requester.setNoAi(true);
        courier.tame(owner);
        requester.tame(owner);
        if (!data.registerRecruit(
                        owner.getUUID(), courier.getUUID(), NpcServiceBranch.CIVILIAN)
                || !data.registerRecruit(
                        owner.getUUID(), requester.getUUID(), NpcServiceBranch.CIVILIAN)
                || !data.reserveWorksite(
                        owner.getUUID(), courier.getUUID(), WorkerProfession.COURIER)) {
            helper.fail("Hybrid courier fixture could not register its recruits and worksite");
            return;
        }
        courier.setWorkerProfession(WorkerProfession.COURIER);

        WorksiteRecord worksite = data.assignedWorksite(
                owner.getUUID(), courier.getUUID()).orElseThrow();
        var configuration = worksite.configuration();
        WorksiteUpdateResult dispatchConfigured = data.configureWorksite(
                owner.getUUID(),
                worksite.id(),
                configuration.revision(),
                configuration.bounds(),
                configuration.kingdomAccess(),
                configuration.priority(),
                configuration.overlayVisible(),
                configuration.itemFilters(),
                CourierDispatchMode.HYBRID);
        if (!dispatchConfigured.accepted()) {
            helper.fail("Hybrid courier dispatch mode was rejected: "
                    + dispatchConfigured.reasonCode());
            return;
        }

        BlockPos firstRouteWaypoint = hallPos.offset(1, 0, 0);
        BlockPos secondRouteWaypoint = hallPos.offset(2, 0, 0);
        String dimensionId = level.dimension().identifier().toString();
        List<CourierWaypoint> route = List.of(
                new CourierWaypoint(
                        dimensionId,
                        firstRouteWaypoint.getX(),
                        firstRouteWaypoint.getY(),
                        firstRouteWaypoint.getZ(),
                        List.of(CourierTransferAction.waitTicks(1))),
                new CourierWaypoint(
                        dimensionId,
                        secondRouteWaypoint.getX(),
                        secondRouteWaypoint.getY(),
                        secondRouteWaypoint.getZ(),
                        List.of(CourierTransferAction.waitTicks(1))));
        WorksiteRecord hybridWorksite = dispatchConfigured.worksite().orElseThrow();
        WorksiteUpdateResult routeConfigured = data.configureWorksiteRoute(
                owner.getUUID(),
                hybridWorksite.id(),
                hybridWorksite.configuration().revision(),
                route,
                CourierRouteMode.LOOP);
        if (!routeConfigured.accepted()) {
            helper.fail("Hybrid courier route was rejected: " + routeConfigured.reasonCode());
            return;
        }

        putContainerItem(hall, new ItemStack(Items.BREAD, 8));
        SupplyDemand sustainedDemand = new SupplyDemand(
                UUID.randomUUID(),
                SupplyCategory.FOOD,
                "minecraft:bread",
                4,
                0,
                100,
                "recruit/" + requester.getUUID() + "/hybrid_dispatch");
        if (!data.requestSupply(
                owner.getUUID(), kingdom.settlement().id(), sustainedDemand)) {
            helper.fail("Hybrid courier sustained demand was rejected");
            return;
        }
        StorageEndpoint hallStorage = data.registeredStorageEndpoint(
                owner.getUUID(), dimensionId, hallPos).orElseThrow();
        var reserved = data.reserveSupply(
                owner.getUUID(),
                kingdom.settlement().id(),
                sustainedDemand.id(),
                courier.getUUID(),
                hallStorage,
                sustainedDemand.outstandingQuantity(),
                8,
                level.getGameTime(),
                1_200L);
        if (!reserved.accepted()) {
            helper.fail("Hybrid courier automatic reservation was rejected: "
                    + reserved.reason());
            return;
        }
        setActiveSupplyReservation(
                courier, reserved.reservation().orElseThrow().id());

        helper.runAfterDelay(2, () -> verifyHybridCourierDispatch(
                helper, level, courier, firstRouteWaypoint, fixtureChunk));
    }

    private static void verifyHybridCourierDispatch(
            GameTestHelper helper,
            ServerLevel level,
            GalacticRecruitEntity courier,
            BlockPos firstRouteWaypoint,
            ChunkPos fixtureChunk
    ) {
        invokeAcquireCourierOrder(courier);
        if (!courier.getWorkerStatus().reasonCode().equals("automatic_supply_withdraw")) {
            helper.fail("Automatic-first hybrid courier did not acquire sustained demand: "
                    + courier.getWorkerStatus());
            return;
        }
        if (!invokeReleaseActiveSupplyReservation(courier)) {
            helper.fail("Hybrid courier could not release its first source reservation");
            return;
        }

        CompoundTag persisted = saveRecruit(courier, level);
        String persistedTurn = persisted.getString("CourierHybridTurn").orElse("");
        if (!persistedTurn.equals("route")) {
            helper.fail("Automatic hybrid acquisition did not persist the route turn: "
                    + persistedTurn);
            return;
        }

        GalacticRecruitEntity reloaded = loadRecruit(persisted, level);
        invokeAcquireCourierOrder(reloaded);
        if (!reloaded.getWorkerStatus().reasonCode().equals("courier_route_action")
                || reloaded.getWorkerStatus().target().filter(target ->
                        target.x() == firstRouteWaypoint.getX()
                                && target.y() == firstRouteWaypoint.getY()
                                && target.z() == firstRouteWaypoint.getZ()).isEmpty()) {
            helper.fail("Reloaded hybrid courier starved its configured route: "
                    + reloaded.getWorkerStatus());
            return;
        }
        CompoundTag afterRoute = saveRecruit(reloaded, level);
        if (!afterRoute.getString("CourierHybridTurn").orElse("").equals("automatic")) {
            helper.fail("Successful route acquisition did not persist the next automatic turn");
            return;
        }

        CompoundTag legacy = persisted.copy();
        legacy.remove("CourierHybridTurn");
        GalacticRecruitEntity legacyReload = loadRecruit(legacy, level);
        CompoundTag migratedLegacy = saveRecruit(legacyReload, level);
        if (!migratedLegacy.getString("CourierHybridTurn").orElse("").equals("automatic")) {
            helper.fail("A legacy courier without a persisted turn was not migrated automatic-first");
            return;
        }
        level.getChunkSource().updateChunkForced(fixtureChunk, false);
        helper.succeed();
    }

    private static void boundedWorkerScans(GameTestHelper helper) {
        BlockPos hallPos = isolatedCapital(helper, 20);
        CommandCenterBlockEntity hall = placeCommandCenter(helper, hallPos);
        ServerPlayer owner = makeConnectedMockPlayer(helper, GameType.CREATIVE);
        hall.claim(owner);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        if (data.activateHall(
                owner.getUUID(),
                hall.factionId(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos).isEmpty()) {
            helper.fail("Bounded scan fixture could not activate its Command Center");
            return;
        }
        GalacticRecruitEntity recruit = ModEntityTypes.CLONE_TROOPER.get().create(
                helper.getLevel(), EntitySpawnReason.TRIGGERED);
        if (recruit == null) {
            helper.fail("Bounded scan fixture could not create its recruit");
            return;
        }
        recruit.setPos(hallPos.getX() + 1.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(recruit);
        recruit.tame(owner);
        recruit.setWorkerProfession(WorkerProfession.ANIMAL_FARMER);
        if (!data.registerRecruit(owner.getUUID(), recruit.getUUID())
                || !data.reserveWorksite(
                        owner.getUUID(), recruit.getUUID(), WorkerProfession.ANIMAL_FARMER)) {
            helper.fail("Bounded scan fixture could not assign its initial worksite");
            return;
        }
        WorksiteRecord initialWorksite = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElseThrow();
        WorkOrder queuedAnimalOrder = new WorkOrder(
                UUID.randomUUID(),
                WorkOrderType.ANIMAL_FARM,
                Optional.empty(),
                WorkOrderState.QUEUED,
                Optional.of(initialWorksite.id()),
                Optional.empty(),
                helper.getLevel().dimension().identifier().toString(),
                hallPos.getX(),
                hallPos.getY(),
                hallPos.getZ(),
                "minecraft:wheat",
                1,
                0,
                "",
                0);
        WorkOrder claimedAnimalOrder = data.queueAndClaimWorkOrder(
                owner.getUUID(), recruit.getUUID(), queuedAnimalOrder).orElseThrow();
        setRecruitField(recruit, "workOrderId", claimedAnimalOrder.id());
        recruit.setWorkerProfession(WorkerProfession.COOK);
        WorkOrder releasedAnimalOrder = data.workOrder(
                owner.getUUID(), claimedAnimalOrder.id()).orElseThrow();
        if (releasedAnimalOrder.assignedRecruitId().isPresent()
                || recruit.getWorkerStatus().workOrderId().isPresent()
                || recruit.getWorkerStatus().phase() != WorkerPhase.ACQUIRE_ORDER) {
            helper.fail("Profession change retained its previous order or execution cursor: "
                    + releasedAnimalOrder + ", status=" + recruit.getWorkerStatus());
            return;
        }
        WorksiteRecord assigned = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElseThrow();
        WorksiteUpdateResult tagConfigured = data.configureWorksite(
                owner.getUUID(),
                assigned.id(),
                assigned.configuration().revision(),
                assigned.configuration().bounds(),
                true,
                assigned.configuration().priority(),
                assigned.configuration().overlayVisible(),
                List.of("#minecraft:meat"),
                assigned.configuration().courierDispatchMode());
        net.minecraft.world.item.Item tagDemand = invokeConfiguredCookingDemand(
                recruit, helper.getLevel());
        TagKey<net.minecraft.world.item.Item> meatTag = TagKey.create(
                Registries.ITEM, Identifier.withDefaultNamespace("meat"));
        if (!tagConfigured.accepted()
                || tagDemand == null
                || !new ItemStack(tagDemand).is(meatTag)) {
            helper.fail("Cook did not resolve a recipe-bearing member from #minecraft:meat; item="
                    + (tagDemand == null
                            ? "none"
                            : BuiltInRegistries.ITEM.getKey(tagDemand)));
            return;
        }

        BlockPos chestPos = hallPos.offset(3, 0, 0);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (!(helper.getLevel().getBlockEntity(chestPos) instanceof Container chest)) {
            helper.fail("Bounded scan fixture could not place its backing chest");
            return;
        }
        StorageEndpoint oneSlotEndpoint = new StorageEndpoint(
                helper.getLevel().dimension().identifier().toString(),
                chestPos.getX(),
                chestPos.getY(),
                chestPos.getZ(),
                1);
        KingdomRecord currentKingdom = data.kingdomForOwner(owner.getUUID()).orElseThrow();
        SettlementRecord currentSettlement = currentKingdom.settlement();
        WorksiteRecord currentWorksite = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElseThrow();
        WorksiteRecord endpointWorksite = currentWorksite.withPrimaryStorageEndpoint(
                oneSlotEndpoint);
        List<WorksiteRecord> updatedWorksites = currentSettlement.worksites().stream()
                .map(worksite -> worksite.id().equals(endpointWorksite.id())
                        ? endpointWorksite : worksite)
                .toList();
        SettlementRecord updatedSettlement = new SettlementRecord(
                currentSettlement.id(),
                currentSettlement.dimensionId(),
                currentSettlement.hallX(),
                currentSettlement.hallY(),
                currentSettlement.hallZ(),
                currentSettlement.claimRadius(),
                currentSettlement.housingCapacity(),
                currentSettlement.recruitIds(),
                currentSettlement.commanderId(),
                currentSettlement.commanderPolicy(),
                updatedWorksites,
                currentSettlement.buildProjects(),
                currentSettlement.workOrders(),
                currentSettlement.recruitmentCampaigns(),
                currentSettlement.rewards().add(oneSlotEndpoint.slots(), 0),
                currentSettlement.revision() + 1,
                currentSettlement.additionalCommanderIds());
        storeKingdomFixture(data, currentKingdom.replaceSettlement(updatedSettlement));
        if (data.registeredStorageEndpoint(
                        owner.getUUID(),
                        helper.getLevel().dimension().identifier().toString(),
                        chestPos)
                .filter(endpoint -> endpoint.slots() == 1)
                .isEmpty()) {
            helper.fail("Bounded scan fixture did not install its one-slot endpoint");
            return;
        }

        chest.setItem(1, new ItemStack(Items.BREAD));
        boolean unreachableDemand = invokeRequestRecruitFoodSupply(
                recruit, helper.getLevel());
        if (unreachableDemand
                || !data.supplyLedger(updatedSettlement.id()).orElseThrow().demands().isEmpty()) {
            helper.fail("Food beyond the registered slot limit published an unreachable demand");
            return;
        }
        chest.setItem(1, ItemStack.EMPTY);
        chest.setItem(0, new ItemStack(Items.BREAD));
        boolean reachableDemand = invokeRequestRecruitFoodSupply(recruit, helper.getLevel());
        var demands = data.supplyLedger(updatedSettlement.id()).orElseThrow().demands();
        if (!reachableDemand
                || demands.size() != 1
                || !demands.getFirst().itemId().equals("minecraft:bread")) {
            helper.fail("Food in the registered slot did not publish a reachable demand");
            return;
        }
        helper.succeed();
    }

    private static void animalFarmerSpeciesPairing(GameTestHelper helper) {
        GalacticRecruitEntity recruit = helper.spawn(
                ModEntityTypes.CLONE_TROOPER.get(), new BlockPos(2, 1, 2));
        Animal cow = EntityTypes.COW.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        Animal sheep = EntityTypes.SHEEP.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        Animal secondCow = EntityTypes.COW.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        if (cow == null || sheep == null || secondCow == null) {
            helper.fail("Could not create cross-species livestock pair");
            return;
        }
        cow.setPos(recruit.getX() + 1.0D, recruit.getY(), recruit.getZ());
        sheep.setPos(recruit.getX() - 1.0D, recruit.getY(), recruit.getZ());
        secondCow.setPos(recruit.getX() + 2.0D, recruit.getY(), recruit.getZ());
        secondCow.setAge(-24_000);
        helper.getLevel().addFreshEntity(cow);
        helper.getLevel().addFreshEntity(sheep);
        helper.getLevel().addFreshEntity(secondCow);
        helper.runAfterDelay(2, () -> {
            setWorkerInventory(recruit, new ItemStack(Items.WHEAT, 2));
            invokeFeedAnimalPair(recruit, helper.getLevel());
            if (!recruit.getWorkerStatus().reasonCode().equals("breeding_pair_required")
                    || cow.isInLove()
                    || sheep.isInLove()
                    || workerInventoryCount(recruit) != 2) {
                helper.fail("Animal farmer accepted an incompatible cow/sheep breeding pair");
                return;
            }

            secondCow.setAge(0);
            helper.runAfterDelay(2, () -> {
                invokeFeedAnimalPair(recruit, helper.getLevel());
                if (!cow.isInLove()
                        || !secondCow.isInLove()
                        || sheep.isInLove()
                        || workerInventoryCount(recruit) != 0) {
                    helper.fail("Animal farmer did not select and feed the compatible cow pair; reason="
                            + recruit.getWorkerStatus().reasonCode()
                            + ", carried=" + workerInventoryCount(recruit)
                            + ", firstCow=" + cow.isAlive() + "/" + cow.canFallInLove() + "/" + cow.isInLove()
                            + ", secondCow=" + secondCow.isAlive() + "/" + secondCow.canFallInLove() + "/" + secondCow.isInLove()
                            + ", sheep=" + sheep.isAlive() + "/" + sheep.canFallInLove() + "/" + sheep.isInLove());
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static boolean configureWorkerLifecycleWorksite(
            GameTestHelper helper,
            KingdomSavedData data,
            ServerPlayer owner,
            GalacticRecruitEntity recruit,
            BlockPos storagePos,
            List<String> filters
    ) {
        WorksiteRecord worksite = data.assignedWorksite(
                owner.getUUID(), recruit.getUUID()).orElse(null);
        StorageEndpoint storage = data.registeredStorageEndpoint(
                owner.getUUID(),
                helper.getLevel().dimension().identifier().toString(),
                storagePos).orElse(null);
        if (worksite == null || storage == null) {
            helper.fail("Worker lifecycle lacks authoritative worksite or storage");
            return false;
        }
        var configured = data.configureWorksite(
                owner.getUUID(),
                worksite.id(),
                worksite.configuration().revision(),
                new WorkAreaBounds(17, 7, 17),
                true,
                100,
                true,
                filters,
                CourierDispatchMode.AUTOMATIC);
        WorksiteRecord configuredWorksite;
        if (configured.accepted()) {
            configuredWorksite = configured.worksite().orElseThrow();
        } else if (configured.reasonCode().equals("unchanged")) {
            configuredWorksite = data.assignedWorksite(
                    owner.getUUID(), recruit.getUUID()).orElseThrow();
        } else {
            helper.fail("Worker lifecycle configuration was rejected: "
                    + configured.reasonCode());
            return false;
        }
        var storageConfigured = data.configureWorksiteStorage(
                owner.getUUID(),
                worksite.id(),
                configuredWorksite.configuration().revision(),
                storage);
        boolean storageAlreadyConfigured = storageConfigured.reasonCode().equals("unchanged")
                && configuredWorksite.storageEndpoints().contains(storage);
        if (!storageConfigured.accepted() && !storageAlreadyConfigured) {
            helper.fail("Worker lifecycle storage was rejected: "
                    + storageConfigured.reasonCode());
            return false;
        }
        return true;
    }

    private static boolean hasCompletedWorkerOrder(
            KingdomSavedData data,
            UUID ownerId,
            WorkOrderType type
    ) {
        return data.kingdomForOwner(ownerId)
                .orElseThrow()
                .settlement()
                .workOrders()
                .stream()
                .anyMatch(order -> order.type() == type
                        && order.state() == WorkOrderState.COMPLETED);
    }

    /**
     * Runs through the real scheduler. The fixture never mutates worker phases,
     * invokes the controller, or moves the recruit after its first assignment.
     */
    private static final class SpecialistWorkerLifecycleScenario {
        private final GameTestHelper helper;
        private final BlockPos hallPos;
        private final CommandCenterBlockEntity hall;
        private final ServerPlayer owner;
        private final GalacticRecruitEntity recruit;
        private final KingdomSavedData data;
        private final Set<ChunkPos> forcedChunks;
        private final int storedBeforeFishing;
        private int phase;
        private long phaseStartedAt;
        private boolean complete;
        private Animal first;
        private Animal second;
        private BlockPos furnacePos;
        private long rawWithdrawnAt = -1L;
        private long furnaceLoadedAt = -1L;
        private long furnaceLitAt = -1L;
        private String lastCookStatus = "";
        private long lastCookStatusAt = -1L;

        private SpecialistWorkerLifecycleScenario(
                GameTestHelper helper,
                BlockPos hallPos,
                CommandCenterBlockEntity hall,
                ServerPlayer owner,
                GalacticRecruitEntity recruit,
                KingdomSavedData data,
                Set<ChunkPos> forcedChunks,
                int storedBeforeFishing
        ) {
            this.helper = helper;
            this.hallPos = hallPos;
            this.hall = hall;
            this.owner = owner;
            this.recruit = recruit;
            this.data = data;
            this.forcedChunks = forcedChunks;
            this.storedBeforeFishing = storedBeforeFishing;
            this.phaseStartedAt = helper.getTick();
        }

        private void tick() {
            if (this.complete) {
                return;
            }
            switch (this.phase) {
                case 0 -> this.waitForFishing();
                case 1 -> this.waitForAnimalFarming();
                case 2 -> this.waitForCooking();
                case 3 -> this.waitForMerchant();
                default -> this.fail("Specialist lifecycle entered an unknown phase");
            }
        }

        private void waitForFishing() {
            boolean completedOrder = hasCompletedWorkerOrder(
                    this.data, this.owner.getUUID(), WorkOrderType.FISH);
            boolean physicalCatchStored =
                    countContainerItems(this.hall) > this.storedBeforeFishing;
            if (completedOrder
                    && physicalCatchStored
                    && workerInventoryCount(this.recruit) == 0) {
                this.beginAnimalFarming();
                return;
            }
            this.timeoutAfter(520, "Fisher did not complete timed loot-table fishing"
                    + "; status=" + this.recruit.getWorkerStatus()
                    + ", stored=" + countContainerItems(this.hall)
                    + ", order=" + completedOrder);
        }

        private void beginAnimalFarming() {
            this.owner.setPos(
                    this.recruit.getX(), this.recruit.getY(), this.recruit.getZ());
            this.recruit.setWorkerMainHandItem(ItemStack.EMPTY);
            if (!this.recruit.handleMenuButton(
                    this.owner, RecruitCommandMenu.BUTTON_ASSIGN_ANIMAL_FARMER)) {
                this.fail("Animal farmer contract was rejected");
                return;
            }
            if (!configureWorkerLifecycleWorksite(
                    this.helper,
                    this.data,
                    this.owner,
                    this.recruit,
                    this.hallPos,
                    List.of("minecraft:cow"))) {
                this.complete = true;
                return;
            }
            this.first = EntityTypes.COW.create(
                    this.helper.getLevel(), EntitySpawnReason.TRIGGERED);
            this.second = EntityTypes.COW.create(
                    this.helper.getLevel(), EntitySpawnReason.TRIGGERED);
            if (this.first == null || this.second == null) {
                this.fail("Could not create livestock for animal farmer lifecycle");
                return;
            }
            this.first.setPos(
                    this.hallPos.getX() + 3.5D,
                    this.hallPos.getY(),
                    this.hallPos.getZ() + 2.5D);
            this.second.setPos(
                    this.hallPos.getX() + 4.5D,
                    this.hallPos.getY(),
                    this.hallPos.getZ() + 2.5D);
            this.first.setNoAi(true);
            this.second.setNoAi(true);
            this.first.setPersistenceRequired();
            this.second.setPersistenceRequired();
            this.helper.getLevel().addFreshEntity(this.first);
            this.helper.getLevel().addFreshEntity(this.second);
            putContainerItem(this.hall, new ItemStack(Items.WHEAT, 2));
            this.advanceTo(1);
        }

        private void waitForAnimalFarming() {
            boolean completedOrder = hasCompletedWorkerOrder(
                    this.data, this.owner.getUUID(), WorkOrderType.ANIMAL_FARM);
            if (completedOrder
                    && this.first.isInLove()
                    && this.second.isInLove()
                    && countContainerItem(this.hall, Items.WHEAT) == 0
                    && workerInventoryCount(this.recruit) == 0) {
                this.beginCooking();
                return;
            }
            this.timeoutAfter(520, "Animal farmer did not withdraw feed and breed"
                    + "; status=" + this.recruit.getWorkerStatus()
                    + ", order=" + completedOrder
                    + ", love=" + this.first.isInLove() + "/"
                    + this.second.isInLove()
                    + ", storedFeed=" + countContainerItem(this.hall, Items.WHEAT));
        }

        private void beginCooking() {
            WorkOrder previousOrder = this.data.assignedWorkOrder(
                    this.owner.getUUID(), this.recruit.getUUID()).orElse(null);
            this.owner.setPos(
                    this.recruit.getX(), this.recruit.getY(), this.recruit.getZ());
            this.recruit.setWorkerMainHandItem(ItemStack.EMPTY);
            if (!this.recruit.handleMenuButton(
                    this.owner, RecruitCommandMenu.BUTTON_ASSIGN_COOK)) {
                this.fail("Cook contract was rejected");
                return;
            }
            WorkOrder releasedOrder = previousOrder == null
                    ? null
                    : this.data.workOrder(
                            this.owner.getUUID(), previousOrder.id()).orElse(null);
            if ((releasedOrder != null && releasedOrder.assignedRecruitId().isPresent())
                    || this.recruit.getWorkerStatus().workOrderId().isPresent()) {
                this.fail("Profession change retained the previous worker order; previous="
                        + releasedOrder + ", status=" + this.recruit.getWorkerStatus());
                return;
            }
            if (!configureWorkerLifecycleWorksite(
                    this.helper,
                    this.data,
                    this.owner,
                    this.recruit,
                    this.hallPos,
                    List.of("#minecraft:meat"))) {
                this.complete = true;
                return;
            }
            this.furnacePos = this.hallPos.offset(3, 0, -2);
            this.helper.getLevel().setBlock(
                    this.furnacePos, Blocks.FURNACE.defaultBlockState(), 3);
            putContainerItem(this.hall, new ItemStack(Items.BEEF));
            putContainerItem(this.hall, new ItemStack(Items.COAL));
            this.advanceTo(2);
        }

        private void waitForCooking() {
            long currentTick = this.helper.getTick();
            int raw = countContainerItem(this.hall, Items.BEEF);
            if (raw == 0 && this.rawWithdrawnAt < 0L) {
                this.rawWithdrawnAt = currentTick;
            }
            Container furnace = this.helper.getLevel().getBlockEntity(this.furnacePos)
                    instanceof Container container ? container : null;
            if (furnace != null && !furnace.getItem(0).isEmpty()
                    && this.furnaceLoadedAt < 0L) {
                this.furnaceLoadedAt = currentTick;
            }
            if (this.helper.getLevel().getBlockState(this.furnacePos)
                    .getValue(BlockStateProperties.LIT) && this.furnaceLitAt < 0L) {
                this.furnaceLitAt = currentTick;
            }
            String cookStatus = this.recruit.getWorkerStatus().phase() + "/"
                    + this.recruit.getWorkerStatus().reasonCode() + "/"
                    + this.recruit.getWorkerStatus().target();
            if (!cookStatus.equals(this.lastCookStatus)) {
                this.lastCookStatus = cookStatus;
                this.lastCookStatusAt = currentTick;
            }
            boolean completedOrder = hasCompletedWorkerOrder(
                    this.data, this.owner.getUUID(), WorkOrderType.COOK);
            if (completedOrder
                    && countContainerItem(this.hall, Items.COOKED_BEEF) == 1
                    && raw == 0
                    && workerInventoryCount(this.recruit) == 0) {
                this.beginMerchant();
                return;
            }
            this.timeoutAfter(600, "Cook did not use a real furnace recipe"
                    + "; status=" + this.recruit.getWorkerStatus()
                    + ", order=" + completedOrder
                    + ", raw=" + raw
                    + ", cooked=" + countContainerItem(this.hall, Items.COOKED_BEEF)
                    + ", rawWithdrawnAt=" + this.rawWithdrawnAt
                    + ", furnaceLoadedAt=" + this.furnaceLoadedAt
                    + ", furnaceLitAt=" + this.furnaceLitAt
                    + ", lastStatusAt=" + this.lastCookStatusAt
                    + ", position=" + this.recruit.position()
                    + ", navigationDone=" + this.recruit.getNavigation().isDone()
                    + ", walkMemory=" + BrainUtil.hasMemory(this.recruit,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                    + ", navigationResult=" + BrainUtil.getMemory(
                    this.recruit, ArmyBrainMemoryTypes.NAVIGATION_RESULT)
                    + ", furnace=" + (furnace == null ? "missing" : List.of(
                    furnace.getItem(0), furnace.getItem(1), furnace.getItem(2))));
        }

        private void beginMerchant() {
            this.owner.setPos(
                    this.recruit.getX(), this.recruit.getY(), this.recruit.getZ());
            this.recruit.setWorkerMainHandItem(ItemStack.EMPTY);
            if (!this.recruit.handleMenuButton(
                    this.owner, RecruitCommandMenu.BUTTON_ASSIGN_MERCHANT)) {
                this.fail("Merchant contract was rejected");
                return;
            }
            if (!configureWorkerLifecycleWorksite(
                    this.helper,
                    this.data,
                    this.owner,
                    this.recruit,
                    this.hallPos,
                    List.of())) {
                this.complete = true;
                return;
            }
            putContainerItem(this.hall, new ItemStack(ModItems.ENERGY_CELL.get(), 2));
            this.advanceTo(3);
        }

        private void waitForMerchant() {
            WorkOrder order = this.data.assignedWorkOrder(
                    this.owner.getUUID(), this.recruit.getUUID()).orElse(null);
            if (this.recruit.isMarketAvailable()
                    && order != null
                    && order.type() == WorkOrderType.MERCHANT
                    && !order.state().terminal()
                    && this.recruit.hasMerchantStock(
                            ModItems.ENERGY_CELL.get(), 2)) {
                this.complete = true;
                this.releaseForcedChunks();
                this.helper.succeed();
                return;
            }
            this.timeoutAfter(260, "Merchant did not open a physical-stock market"
                    + "; status=" + this.recruit.getWorkerStatus()
                    + ", order=" + order
                    + ", market=" + this.recruit.isMarketAvailable());
        }

        private void advanceTo(int nextPhase) {
            this.phase = nextPhase;
            this.phaseStartedAt = this.helper.getTick();
        }

        private void timeoutAfter(long ticks, String message) {
            if (this.helper.getTick() - this.phaseStartedAt >= ticks) {
                this.fail(message);
            }
        }

        private void fail(String message) {
            this.complete = true;
            this.releaseForcedChunks();
            this.helper.fail(message);
        }

        private void releaseForcedChunks() {
            this.forcedChunks.forEach(chunk -> this.helper.getLevel().getChunkSource()
                    .updateChunkForced(chunk, false));
        }
    }

    private static void setWorkerInventory(GalacticRecruitEntity recruit, ItemStack stack) {
        NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            inventory.set(0, stack.copy());
        }
        setRecruitField(recruit, "workerInventory", inventory);
    }

    private static UUID acquirePersistedOrder(GalacticRecruitEntity recruit) {
        invokeRecruitCommand(recruit, RecruitmentAction.WORK_AT_SITE);
        setWorkerPhase(recruit, WorkerPhase.ACQUIRE_ORDER, "ready", null);
        recruit.tickWorkerController();
        Object orderId = getRecruitField(recruit, "workOrderId");
        if (!(orderId instanceof UUID uuid)) {
            throw new IllegalStateException("Worker did not claim a persisted work order; profession="
                    + recruit.getWorkerProfession().orElse(null)
                    + ", phase=" + recruit.getWorkerStatus().phase()
                    + ", reason=" + recruit.getWorkerStatus().reasonCode()
                    + ", target=" + recruit.getTarget()
                    + ", tame=" + recruit.isTame()
                    + ", removed=" + recruit.isRemoved()
                    + ", removalReason=" + recruit.getRemovalReason()
                    + ", duty=" + recruit.getRecruitDuty()
                    + ", command=" + recruit.getRecruitCommand()
                    + ", workTarget=" + recruit.getWorkTarget()
                    + ", shouldRun=" + recruit.shouldRunWorkerCycle());
        }
        return uuid;
    }

    private static void interactWorkerAt(GalacticRecruitEntity recruit, BlockPos target, String reason) {
        recruit.setPos(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        setWorkerPhase(recruit, WorkerPhase.INTERACT, reason, target);
        recruit.tickWorkerController();
    }

    private static void depositWorkerAt(GalacticRecruitEntity recruit, BlockPos storage) {
        recruit.setPos(storage.getX() + 0.5, storage.getY(), storage.getZ() + 0.5);
        setWorkerPhase(recruit, WorkerPhase.DEPOSIT, "deposit_inventory", storage);
        recruit.tickWorkerController();
    }

    private static void setWorkerPhase(
            GalacticRecruitEntity recruit,
            WorkerPhase phase,
            String reason,
            BlockPos target
    ) {
        setRecruitField(recruit, "workerPhase", phase);
        setRecruitField(recruit, "workerReason", reason);
        setRecruitField(recruit, "activeWorkTarget", target);
    }

    private static void assertCompletedOrder(
            GameTestHelper helper,
            KingdomSavedData data,
            UUID ownerId,
            UUID orderId,
            String profession
    ) {
        if (orderId == null || data.workOrder(ownerId, orderId)
                .filter(order -> order.state() == WorkOrderState.COMPLETED).isEmpty()) {
            helper.fail(profession + " loop did not complete its persisted work order");
        }
    }

    private static void putContainerItem(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, stack.copy());
                container.setChanged();
                return;
            }
        }
        throw new IllegalStateException("Test container has no empty slot");
    }

    private static boolean moveOneContainerItemToWorker(
            Container container,
            GalacticRecruitEntity recruit,
            net.minecraft.world.item.Item item
    ) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.is(item) || stored.isEmpty()) {
                continue;
            }
            stored.shrink(1);
            if (stored.isEmpty()) {
                container.setItem(slot, ItemStack.EMPTY);
            } else {
                container.setChanged();
            }
            setWorkerInventory(recruit, new ItemStack(item));
            return true;
        }
        return false;
    }

    private static int countContainerItem(Container container, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<ItemStack> copyContainerStacks(Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    private static int countMatchingStack(Container container, ItemStack template) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countMatchingStack(List<ItemStack> stacks, ItemStack template) {
        return stacks.stream()
                .filter(stack -> ItemStack.isSameItemSameComponents(stack, template))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static int countContainerItems(Container container) {
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            count += container.getItem(slot).getCount();
        }
        return count;
    }

    private static WorksiteRecord configurePhysicalMarket(
            GameTestHelper helper,
            ServerPlayer owner,
            GalacticRecruitEntity merchant,
            CommandCenterBlockEntity storage,
            BlockPos storagePos,
            int energyCellStock
    ) {
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        if (data.kingdomForRecruit(merchant.getUUID()).isEmpty()
                && !data.registerRecruit(
                        owner.getUUID(), merchant.getUUID(), NpcServiceBranch.CIVILIAN)) {
            throw new IllegalStateException("Could not register the physical market merchant");
        }
        if (!data.reserveWorksite(
                owner.getUUID(), merchant.getUUID(), WorkerProfession.MERCHANT)) {
            throw new IllegalStateException("Could not reserve the physical market worksite");
        }
        String dimensionId = helper.getLevel().dimension().identifier().toString();
        WorksiteRecord worksite = data.configureAssignedFrontierWorksite(
                        owner.getUUID(),
                        merchant.getUUID(),
                        dimensionId,
                        storagePos.offset(2, 0, 0),
                        8)
                .orElseGet(() -> data.assignedWorksite(
                        owner.getUUID(), merchant.getUUID()).orElseThrow());
        StorageEndpoint endpoint = data.registeredStorageEndpoint(
                        owner.getUUID(), dimensionId, storagePos)
                .orElseThrow(() -> new IllegalStateException(
                        "Command Center was not registered as physical market storage"));
        WorksiteUpdateResult configured = data.configureWorksiteStorage(
                owner.getUUID(), worksite.id(), worksite.configuration().revision(), endpoint);
        if (!configured.accepted() && !configured.reasonCode().equals("unchanged")) {
            throw new IllegalStateException(
                    "Could not configure physical market storage: " + configured.reasonCode());
        }
        WorksiteRecord authoritative = data.assignedWorksite(
                owner.getUUID(), merchant.getUUID()).orElseThrow();
        setRecruitField(merchant, "storageTarget", storagePos.immutable());
        merchant.setPos(
                authoritative.x() + 0.5D,
                authoritative.y(),
                authoritative.z() + 0.5D);
        setWorkerPhase(merchant, WorkerPhase.COOLDOWN, "market_open", null);
        if (energyCellStock > 0) {
            putContainerItem(storage, new ItemStack(ModItems.ENERGY_CELL.get(), energyCellStock));
        }
        if (!merchant.isMarketAvailable()) {
            throw new IllegalStateException("Configured physical market did not open");
        }
        return authoritative;
    }

    private static SmartBrainTestArea prepareSmartBrainTestArea(
            GameTestHelper helper,
            GameType playerGameType,
            int minX,
            int maxX,
            int minZ,
            int maxZ
    ) {
        int lane = SMART_BRAIN_AREA_SEQUENCE.getAndIncrement();
        int horizontalLane = lane / SMART_BRAIN_TEST_AREA_VERTICAL_LANES;
        int verticalLane = lane % SMART_BRAIN_TEST_AREA_VERTICAL_LANES;
        BlockPos testOrigin = helper.absolutePos(BlockPos.ZERO);
        int baseY = Math.min(
                helper.getLevel().getMaxY() - 8,
                helper.getLevel().getMinY() + 256
                        + verticalLane * SMART_BRAIN_TEST_AREA_VERTICAL_SPACING);
        // Two well-separated vertical floors halve chunk generation, while the horizontal
        // lane prevents the old max-height clamp from collapsing later fixtures together.
        BlockPos isolatedBase = new BlockPos(
                testOrigin.getX()
                        + horizontalLane * SMART_BRAIN_TEST_AREA_HORIZONTAL_SPACING,
                baseY,
                testOrigin.getZ());
        return prepareSmartBrainTestAreaAt(
                helper, playerGameType, minX, maxX, minZ, maxZ,
                isolatedBase);
    }

    private static SmartBrainTestArea prepareSmartBrainTestAreaAt(
            GameTestHelper helper,
            GameType playerGameType,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            BlockPos base
    ) {
        int minChunkX = Math.min(base.getX() + minX, base.getX() + maxX) >> 4;
        int maxChunkX = Math.max(base.getX() + minX, base.getX() + maxX) >> 4;
        int minChunkZ = Math.min(base.getZ() + minZ, base.getZ() + maxZ) >> 4;
        int maxChunkZ = Math.max(base.getZ() + minZ, base.getZ() + maxZ) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
                helper.getLevel().getChunkSource().updateChunkForced(chunk, true);
                helper.getLevel().getChunk(chunkX, chunkZ);
            }
        }
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                helper.getLevel().setBlockAndUpdate(base.offset(x, 0, z), Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 4; y++) {
                    helper.getLevel().setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        ServerPlayer player = makeConnectedMockPlayer(helper, playerGameType);
        if (!player.teleportTo(
                helper.getLevel(),
                base.getX() + 0.5D,
                base.getY() + 1.0D,
                base.getZ() + 0.5D,
                Set.of(),
                0.0F,
                0.0F,
                true)) {
            throw new IllegalStateException("Could not load SmartBrain GameTest area");
        }
        // Embedded GameTest clients do not send the movement acknowledgement that normally
        // advances a player's entity-ticking chunk ticket after a same-dimension teleport.
        helper.getLevel().getChunkSource().move(player);
        return new SmartBrainTestArea(base, player);
    }

    private static boolean areSmartBrainAreaChunksTicking(
            GameTestHelper helper,
            SmartBrainTestArea area,
            int minX,
            int maxX,
            int minZ,
            int maxZ
    ) {
        // Aggregate GameTests can register the embedded player after the one-shot
        // post-teleport update. Refresh its ticket before polling the prepared lane.
        helper.getLevel().getChunkSource().move(area.player());
        int minChunkX = Math.min(area.base().getX() + minX, area.base().getX() + maxX) >> 4;
        int maxChunkX = Math.max(area.base().getX() + minX, area.base().getX() + maxX) >> 4;
        int minChunkZ = Math.min(area.base().getZ() + minZ, area.base().getZ() + maxZ) >> 4;
        int maxChunkZ = Math.max(area.base().getZ() + minZ, area.base().getZ() + maxZ) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                        new ChunkPos(chunkX, chunkZ))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static GalacticRecruitEntity spawnRecruitAt(
            GameTestHelper helper,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position
    ) {
        GalacticRecruitEntity recruit = type.create(
                helper.getLevel(), EntitySpawnReason.TRIGGERED);
        if (recruit == null) {
            throw new IllegalStateException("Could not create SmartBrain GameTest recruit");
        }
        recruit.setPos(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D);
        if (!helper.getLevel().addFreshEntity(recruit)) {
            throw new IllegalStateException("Could not register SmartBrain GameTest recruit");
        }
        recruit.setDeltaMovement(Vec3.ZERO);
        recruit.getNavigation().stop();
        return recruit;
    }

    private static <T extends Entity> T spawnEntityAt(
            GameTestHelper helper,
            EntityType<T> type,
            BlockPos position
    ) {
        helper.getLevel().getChunkAt(position);
        T entity = type.create(helper.getLevel(), EntitySpawnReason.EVENT);
        if (entity == null) {
            throw new IllegalStateException("Could not create GameTest entity "
                    + BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
        entity.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        if (!helper.getLevel().addFreshEntity(entity)) {
            throw new IllegalStateException("Could not register GameTest entity "
                    + BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
        return entity;
    }

    private static GalacticRecruitEntity spawnNaturalRecruitAt(
            GameTestHelper helper,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position
    ) {
        return spawnNaturalRecruitAt(helper.getLevel(), type, position, Optional.empty());
    }

    private static GalacticRecruitEntity spawnNaturalRecruitAt(
            GameTestHelper helper,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position,
            UUID entityId
    ) {
        return spawnNaturalRecruitAt(helper.getLevel(), type, position, Optional.of(entityId));
    }

    private static GalacticRecruitEntity spawnNaturalRecruitAt(
            ServerLevel level,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position
    ) {
        return spawnNaturalRecruitAt(level, type, position, Optional.empty());
    }

    private static GalacticRecruitEntity spawnNaturalRecruitAt(
            ServerLevel level,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position,
            Optional<UUID> entityId
    ) {
        level.getChunkAt(position);
        GalacticRecruitEntity recruit = type.create(level, EntitySpawnReason.NATURAL);
        if (recruit == null) {
            throw new IllegalStateException("Could not create natural faction GameTest recruit");
        }
        entityId.ifPresent(recruit::setUUID);
        recruit.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        recruit.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(position),
                EntitySpawnReason.NATURAL,
                null);
        if (recruit.isRemoved() || !level.addFreshEntity(recruit)) {
            throw new IllegalStateException("Could not register natural faction GameTest recruit");
        }
        recruit.tick();
        if (recruit.isRemoved() || recruit.isPendingNaturalSpawnInitialization()) {
            throw new IllegalStateException("Natural faction GameTest recruit failed live initialization");
        }
        return recruit;
    }

    private static GalacticRecruitEntity spawnPlanetPolicyRecruitAt(
            ServerLevel level,
            EntityType<GalacticRecruitEntity> type,
            BlockPos position,
            PlanetFactionSpawnPolicy.Evaluation evaluation
    ) {
        level.getChunkAt(position);
        GalacticRecruitEntity recruit = type.create(level, EntitySpawnReason.EVENT);
        if (recruit == null) {
            throw new IllegalStateException("Could not create planet faction GameTest recruit");
        }
        recruit.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        try {
            Method initializer = GalacticRecruitEntity.class.getDeclaredMethod(
                    "initializeNaturalPlanetNpc",
                    ServerLevel.class,
                    PlanetFactionSpawnPolicy.Evaluation.class);
            initializer.setAccessible(true);
            if (!(boolean) initializer.invoke(recruit, level, evaluation)) {
                throw new IllegalStateException("Planet faction lifecycle rejected an authorized GameTest recruit");
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not execute planet faction lifecycle", exception);
        }
        if (!level.addFreshEntity(recruit)) {
            throw new IllegalStateException("Could not register planet faction GameTest recruit");
        }
        return recruit;
    }

    private static String pathState(GalacticRecruitEntity recruit, BlockPos target) {
        net.minecraft.world.level.pathfinder.Path path = recruit.getNavigation().createPath(target, 0);
        if (path == null) {
            return "none";
        }
        return "nodes=" + path.getNodeCount()
                + ", canReach=" + path.canReach()
                + ", target=" + path.getTarget();
    }

    @SuppressWarnings("unchecked")
    private static int workerInventoryCount(GalacticRecruitEntity recruit) {
        return ((NonNullList<ItemStack>) getRecruitField(recruit, "workerInventory")).stream()
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static Object getRecruitField(GalacticRecruitEntity recruit, String fieldName) {
        try {
            Field field = GalacticRecruitEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(recruit);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not read recruit field " + fieldName, exception);
        }
    }

    private static void setRecruitField(GalacticRecruitEntity recruit, String fieldName, Object value) {
        try {
            Field field = GalacticRecruitEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(recruit, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set recruit field " + fieldName, exception);
        }
    }

    private static net.minecraft.world.item.Item invokeConfiguredCookingDemand(
            GalacticRecruitEntity recruit,
            ServerLevel level
    ) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod(
                    "configuredCookingDemand", ServerLevel.class);
            method.setAccessible(true);
            return (net.minecraft.world.item.Item) method.invoke(recruit, level);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not resolve configured cooking demand", exception);
        }
    }

    private static boolean invokeRequestRecruitFoodSupply(
            GalacticRecruitEntity recruit,
            ServerLevel level
    ) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod(
                    "requestRecruitFoodSupply", ServerLevel.class);
            method.setAccessible(true);
            return (boolean) method.invoke(recruit, level);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not request recruit food supply", exception);
        }
    }

    private static void storeKingdomFixture(KingdomSavedData data, KingdomRecord kingdom) {
        try {
            Method method = KingdomSavedData.class.getDeclaredMethod(
                    "storeKingdom", KingdomRecord.class);
            method.setAccessible(true);
            method.invoke(data, kingdom);
            data.setDirty();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not install GameTest kingdom fixture", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setSavedDataState(
            ProgressionSavedData data,
            UUID playerId,
            ProgressionState state
    ) {
        try {
            Field field = ProgressionSavedData.class.getDeclaredField("states");
            field.setAccessible(true);
            ((Map<UUID, ProgressionState>) field.get(data)).put(playerId, state);
            data.setDirty();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not install progression overflow fixture", exception);
        }
    }

    private static GalacticVehicleEntity spawnVehicleAt(
            GameTestHelper helper,
            EntityType<GalacticVehicleEntity> type,
            BlockPos position
    ) {
        GalacticVehicleEntity vehicle = type.create(
                helper.getLevel(), EntitySpawnReason.TRIGGERED);
        if (vehicle == null) {
            throw new IllegalStateException("Could not create vehicle GameTest entity");
        }
        vehicle.setPos(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D);
        if (!helper.getLevel().addFreshEntity(vehicle)) {
            throw new IllegalStateException("Could not register vehicle GameTest entity");
        }
        vehicle.setDeltaMovement(Vec3.ZERO);
        return vehicle;
    }

    private static void invokeRecruitCommand(GalacticRecruitEntity recruit, RecruitmentAction command) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod(
                    "setRecruitCommand", RecruitmentAction.class);
            method.setAccessible(true);
            method.invoke(recruit, command);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set recruit command", exception);
        }
    }

    private static void invokeFeedAnimalPair(GalacticRecruitEntity recruit, ServerLevel level) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod("feedAnimalPair", ServerLevel.class);
            method.setAccessible(true);
            method.invoke(recruit, level);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not run animal feeding interaction", exception);
        }
    }

    private static BuildProject fullyProgressProject(
            KingdomSavedData data,
            UUID ownerId,
            KingdomBaseBlueprint blueprint,
            String dimensionId,
            BlockPos origin
    ) {
        BuildProject project = data.startBuildProject(ownerId, blueprint, dimensionId, origin, 0)
                .orElseThrow();
        for (int placement = 0; placement < blueprint.placements().size(); placement++) {
            BuildProject progressed = project.markCompleted(placement);
            if (!data.replaceBuildProject(ownerId, progressed)) {
                throw new IllegalStateException("Could not persist placement " + placement
                        + " for " + blueprint.id());
            }
            project = progressed;
        }
        return project;
    }

    private static final class VehicleEmbodiedTestScenario {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final ServerPlayer intruder;
        private final List<GalacticVehicleEntity> vehicles;
        private final long indexWaitStartedTick;
        private int phase;
        private int driveStartedVehicleTick;
        private long destructionStartedTick;
        private int startingFuel;
        private Vec3 startingPosition = Vec3.ZERO;
        private boolean shotObserved;
        private boolean shotRequested;

        private VehicleEmbodiedTestScenario(
                GameTestHelper helper,
                ServerPlayer owner,
                ServerPlayer intruder,
                List<GalacticVehicleEntity> vehicles
        ) {
            this.helper = helper;
            this.owner = owner;
            this.intruder = intruder;
            this.vehicles = vehicles;
            this.indexWaitStartedTick = helper.getTick();
        }

        private void tick() {
            if (this.phase == 0) {
                this.waitForIndexAndPrepare();
            } else if (this.phase == 1) {
                this.driveAndFire();
            } else if (this.phase == 2) {
                this.verifyDestruction();
            }
        }

        private void waitForIndexAndPrepare() {
            this.helper.getLevel().getChunkSource().move(this.owner);
            long indexedVehicles = this.vehicles.stream()
                    .filter(vehicle -> this.helper.getLevel().getEntity(vehicle.getUUID()) == vehicle)
                    .count();
            long tickingVehicles = this.vehicles.stream()
                    .filter(vehicle -> this.helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                            ChunkPos.containing(vehicle.blockPosition())))
                    .count();
            if (indexedVehicles != this.vehicles.size() || tickingVehicles != this.vehicles.size()) {
                if (this.helper.getTick() - this.indexWaitStartedTick
                        >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                    this.phase = 3;
                    this.helper.fail("Vehicle chassis never became ready: indexed="
                            + indexedVehicles + "/" + this.vehicles.size()
                            + ", ticking=" + tickingVehicles + "/" + this.vehicles.size());
                }
                return;
            }
            for (GalacticVehicleEntity vehicle : this.vehicles) {
                int maximum = vehicle.syncedMaximumHealth();
                vehicle.damageVehicle(20);
                int damaged = vehicle.health();
                this.intruder.setItemInHand(
                        InteractionHand.MAIN_HAND, new ItemStack(ModItems.DURACRETE.get()));
                InteractionResult denied = vehicle.interact(
                        this.intruder, InteractionHand.MAIN_HAND, this.intruder.position());
                if (denied != InteractionResult.FAIL
                        || vehicle.health() != damaged
                        || this.intruder.getMainHandItem().getCount() != 1) {
                    this.phase = 3;
                    this.helper.fail("Unauthorized player repaired chassis " + vehicle.vehicleId());
                    return;
                }
                this.owner.setItemInHand(
                        InteractionHand.MAIN_HAND, new ItemStack(ModItems.DURACRETE.get()));
                InteractionResult repaired = vehicle.interact(
                        this.owner, InteractionHand.MAIN_HAND, this.owner.position());
                int expectedHealth = Math.min(maximum, damaged + 12);
                if (repaired != InteractionResult.SUCCESS
                        || vehicle.health() != expectedHealth
                        || !this.owner.getMainHandItem().isEmpty()) {
                    this.phase = 3;
                    this.helper.fail("Authorized Duracrete repair was not atomic for "
                            + vehicle.vehicleId() + ": health=" + damaged + "->" + vehicle.health()
                            + "/" + maximum + ", item=" + this.owner.getMainHandItem());
                    return;
                }
            }

            GalacticVehicleEntity driverVehicle = this.vehicles.getFirst();
            this.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            this.owner.setYRot(0.0F);
            this.owner.setXRot(0.0F);
            InteractionResult boarded = driverVehicle.interact(
                    this.owner, InteractionHand.MAIN_HAND, this.owner.position());
            if (boarded != InteractionResult.SUCCESS || this.owner.getVehicle() != driverVehicle) {
                this.phase = 3;
                this.helper.fail("Authorized owner could not physically board the BARC speeder");
                return;
            }
            this.startingPosition = driverVehicle.position();
            this.startingFuel = driverVehicle.fuel();
            this.driveStartedVehicleTick = driverVehicle.tickCount;
            this.phase = 1;
        }

        private void driveAndFire() {
            GalacticVehicleEntity driverVehicle = this.vehicles.getFirst();
            boolean fire = !this.shotRequested;
            if (!driverVehicle.applyInput(
                    this.owner, UUID.randomUUID(), 1.0F, 0.0F, false, false, fire)) {
                this.phase = 3;
                this.helper.fail("Server rejected fresh authorized BARC driving input");
                return;
            }
            this.shotRequested = true;
            this.shotObserved |= !this.helper.getLevel().getEntitiesOfClass(
                    BlasterBoltEntity.class,
                    driverVehicle.getBoundingBox().inflate(120.0D),
                    bolt -> bolt.getOwner() == this.owner).isEmpty();
            int drivenTicks = driverVehicle.tickCount - this.driveStartedVehicleTick;
            if (drivenTicks < 25 || !this.shotObserved) {
                if (drivenTicks >= 80) {
                    this.phase = 3;
                    this.helper.fail("BARC embodied runtime did not fire within 80 vehicle ticks: fuel="
                            + driverVehicle.fuel() + ", position=" + driverVehicle.position()
                            + ", passenger=" + this.owner.getVehicle());
                }
                return;
            }
            if (driverVehicle.position().distanceToSqr(this.startingPosition) <= 1.0D
                    || driverVehicle.fuel() >= this.startingFuel) {
                this.phase = 3;
                this.helper.fail("BARC input did not produce movement and fuel use: position="
                        + this.startingPosition + "->" + driverVehicle.position()
                        + ", fuel=" + this.startingFuel + "->" + driverVehicle.fuel());
                return;
            }

            int fuelBeforeIntrusion = driverVehicle.fuel();
            this.intruder.setItemInHand(
                    InteractionHand.MAIN_HAND, new ItemStack(ModItems.ENERGY_CELL.get()));
            InteractionResult denied = driverVehicle.interact(
                    this.intruder, InteractionHand.MAIN_HAND, this.intruder.position());
            if (denied != InteractionResult.FAIL
                    || driverVehicle.fuel() != fuelBeforeIntrusion
                    || this.intruder.getMainHandItem().getCount() != 1) {
                this.phase = 3;
                this.helper.fail("Unauthorized player refuelled the occupied BARC speeder");
                return;
            }

            this.owner.setItemInHand(
                    InteractionHand.MAIN_HAND, new ItemStack(ModItems.ENERGY_CELL.get()));
            InteractionResult refuelled = driverVehicle.interact(
                    this.owner, InteractionHand.MAIN_HAND, this.owner.position());
            if (refuelled != InteractionResult.SUCCESS
                    || driverVehicle.fuel() <= fuelBeforeIntrusion
                    || !this.owner.getMainHandItem().isEmpty()) {
                this.phase = 3;
                this.helper.fail("Authorized Energy Cell refuel was not atomic: fuel="
                        + fuelBeforeIntrusion + "->" + driverVehicle.fuel()
                        + ", item=" + this.owner.getMainHandItem());
                return;
            }

            for (GalacticVehicleEntity vehicle : this.vehicles) {
                vehicle.damageVehicle(vehicle.health());
            }
            this.destructionStartedTick = this.helper.getTick();
            this.phase = 2;
        }

        private void verifyDestruction() {
            boolean allRemoved = this.vehicles.stream().allMatch(GalacticVehicleEntity::isRemoved);
            if (allRemoved && this.owner.getVehicle() == null) {
                this.phase = 3;
                this.helper.succeed();
                return;
            }
            if (this.helper.getTick() - this.destructionStartedTick >= 20L) {
                this.phase = 3;
                this.helper.fail("Destroyed vehicle did not discard and release its passenger: removed="
                        + this.vehicles.stream().map(GalacticVehicleEntity::isRemoved).toList()
                        + ", passengerVehicle=" + this.owner.getVehicle());
            }
        }
    }

    private static final class ChapterThreeCampaignTestScenario {
        private static final int EXPECTED_CAMPAIGN_REWARDS = 330;
        private final GameTestHelper helper;
        private final SmartBrainTestArea area;
        private final ServerPlayer owner;
        private final BlockPos hallPos;
        private final CommandCenterBlockEntity hall;
        private final KingdomRecord kingdom;
        private final GalacticRecruitEntity merchant;
        private final GalacticRecruitEntity escort;
        private final BlockPos deploymentFloor;
        private final BlockPos beacon;
        private final galacticwars.clonewars.data.LaunchContentDefinitions.ConquestRegionDefinition region;
        private final long indexWaitStartedTick;
        private int phase;
        private int captureTicks;
        private GalacticVehicleEntity vehicle;

        private ChapterThreeCampaignTestScenario(
                GameTestHelper helper,
                SmartBrainTestArea area,
                ServerPlayer owner,
                BlockPos hallPos,
                CommandCenterBlockEntity hall,
                KingdomRecord kingdom,
                GalacticRecruitEntity merchant,
                GalacticRecruitEntity escort,
                BlockPos deploymentFloor,
                BlockPos beacon,
                galacticwars.clonewars.data.LaunchContentDefinitions.ConquestRegionDefinition region
        ) {
            this.helper = helper;
            this.area = area;
            this.owner = owner;
            this.hallPos = hallPos;
            this.hall = hall;
            this.kingdom = kingdom;
            this.merchant = merchant;
            this.escort = escort;
            this.deploymentFloor = deploymentFloor;
            this.beacon = beacon;
            this.region = region;
            this.indexWaitStartedTick = helper.getTick();
        }

        private void tick() {
            if (this.phase == 0) {
                this.waitForEntitiesAndTrade();
            } else if (this.phase == 1) {
                this.captureRegion();
            }
        }

        private void waitForEntitiesAndTrade() {
            if (this.vehicle == null) {
                this.vehicle = this.helper.getLevel().getEntitiesOfClass(
                        GalacticVehicleEntity.class, new AABB(this.deploymentFloor).inflate(4.0D),
                        candidate -> candidate.ownerId().filter(this.owner.getUUID()::equals).isPresent())
                        .stream().findFirst().orElse(null);
            }
            boolean indexed = this.helper.getLevel().getEntity(this.merchant.getUUID()) == this.merchant
                    && this.helper.getLevel().getEntity(this.escort.getUUID()) == this.escort
                    && this.vehicle != null
                    && this.helper.getLevel().getEntity(this.vehicle.getUUID()) == this.vehicle;
            if (!indexed) {
                if (this.helper.getTick() - this.indexWaitStartedTick >= 120L) {
                    this.phase = 2;
                    this.helper.fail("Chapter-three merchant, escort, or vehicle never entered the entity index");
                }
                return;
            }
            this.owner.setPos(
                    this.merchant.getX(), this.merchant.getY(), this.merchant.getZ() + 1.0D);
            int expectedFriendlyPrice = FactionBalanceService.applyPercentCeil(
                    FactionBalanceService.tradeCreditPrice(
                            "galacticwars:republic",
                            LaunchContentCatalog.trades()
                                    .get("republic_quartermaster").price()),
                    90);
            this.owner.getInventory().add(
                    new ItemStack(ModItems.CREDIT_CHIP.get(), expectedFriendlyPrice));
            MerchantTradeMenu tradeMenu = (MerchantTradeMenu) new MerchantTradeMenuProvider(this.merchant)
                    .createMenu(21, this.owner.getInventory(), this.owner);
            int offer = tradeMenu.tradeIds().indexOf("republic_quartermaster");
            var serverOffer = offer < 0 ? null : tradeMenu.offers().get(offer);
            UUID tradeId = UUID.randomUUID();
            boolean truthfulOffer = serverOffer != null
                    && serverOffer.itemId().equals("galacticwars:energy_cell")
                    && serverOffer.itemCount() == 8
                    && serverOffer.creditPrice() == expectedFriendlyPrice
                    && serverOffer.eligible();
            boolean traded = truthfulOffer && tradeMenu.handleReplayAction(this.owner, tradeId, offer);
            boolean replayRejected = offer >= 0
                    && !tradeMenu.handleReplayAction(this.owner, tradeId, offer);
            ProgressionState tradedState = ProgressionSavedData.get(this.helper.getLevel())
                    .state(this.owner.getUUID());
            if (!traded || !replayRejected || countPlayerItem(this.owner, ModItems.ENERGY_CELL.get()) != 8
                    || CreditTransactionService.playerBalance(this.owner) != 0
                    || tradedState.total(ProgressionEventType.TRADE_COMPLETED) != 1) {
                this.phase = 2;
                this.helper.fail("Physical merchant trade did not commit exactly once in Chapter 3: offer="
                        + offer + ", traded=" + traded + ", replay=" + replayRejected
                        + ", energy=" + countPlayerItem(this.owner, ModItems.ENERGY_CELL.get())
                        + ", credits=" + CreditTransactionService.playerBalance(this.owner)
                        + ", total=" + tradedState.total(ProgressionEventType.TRADE_COMPLETED)
                        + ", merchant=" + this.merchant.isMerchant() + "/"
                        + this.merchant.factionIdForGameplay() + "/"
                        + this.merchant.factionRelationTo(this.owner)
                        + ", valid=" + tradeMenu.stillValid(this.owner)
                        + ", serverOffer=" + serverOffer
                        + ", unlocks=" + tradedState.unlocks());
                return;
            }
            this.owner.setPos(
                    this.beacon.getX() + 0.5D, this.beacon.getY(), this.beacon.getZ() + 0.5D);
            this.escort.setPos(
                    this.beacon.getX() + 1.5D, this.beacon.getY(), this.beacon.getZ() + 0.5D);
            this.phase = 1;
        }

        private void captureRegion() {
            this.captureTicks++;
            ConquestCaptureService.CaptureResult result = ConquestCaptureService.tick(
                    this.helper.getLevel(), this.region, this.beacon);
            if (!result.accepted()) {
                this.phase = 2;
                this.helper.fail("Embodied conquest transaction was rejected: " + result);
                return;
            }
            ConquestControlState control = ConquestSavedData.get(this.helper.getLevel())
                    .state(this.region.id()).orElseThrow();
            if (!control.controllingFaction().equals("galacticwars:republic")) {
                if (this.captureTicks >= 80) {
                    this.phase = 2;
                    this.helper.fail("Owned military escort did not capture the physical beacon: result="
                            + result + ", control=" + control + ", escortOwner="
                            + this.escort.getOwnerReference() + ", escortDuty="
                            + this.escort.getServiceBranch());
                }
                return;
            }
            this.verifyVictory(control);
        }

        private void verifyVictory(ConquestControlState control) {
            ProgressionSavedData progression = ProgressionSavedData.get(this.helper.getLevel());
            ProgressionState victory = progression.state(this.owner.getUUID());
            boolean chapterComplete = victory.hasSubject(
                    ProgressionEventType.QUEST_ADVANCED, "republic_chapter_3");
            boolean campaignComplete = victory.hasSubject(
                    ProgressionEventType.CAMPAIGN_COMPLETED, "republic_campaign");
            boolean victoryUnlocks = victory.unlocks().contains("campaign_victory")
                    && victory.unlocks().contains("veteran_operations");
            ConquestCaptureService.CaptureResult replay = ConquestCaptureService.tick(
                    this.helper.getLevel(), this.region, this.beacon);
            ConquestControlState replayedControl = ConquestSavedData.get(this.helper.getLevel())
                    .state(this.region.id()).orElseThrow();
            if (!chapterComplete || !campaignComplete || !victoryUnlocks
                    || replayedControl.revision() != control.revision()
                    || !replayedControl.controllingFaction().equals(control.controllingFaction())
                    || !replayedControl.controllingKingdom().equals(control.controllingKingdom())
                    || !control.controllingKingdom().equals(this.kingdom.id().toString())
                    || victory.total(ProgressionEventType.VEHICLE_ACQUIRED) != 1
                    || victory.total(ProgressionEventType.TRADE_COMPLETED) != 1
                    || victory.total(ProgressionEventType.REGION_CAPTURED) != 1
                    || victory.pendingCreditRewards() != EXPECTED_CAMPAIGN_REWARDS
                    || !replay.accepted() || replay.captured()
                    || progression.state(this.owner.getUUID())
                    .total(ProgressionEventType.REGION_CAPTURED) != 1) {
                this.phase = 2;
                this.helper.fail("Chapter 3 did not atomically become a persistent campaign victory: chapter="
                        + chapterComplete + ", campaign=" + campaignComplete
                        + ", unlocks=" + victory.unlocks() + ", control=" + control
                        + ", totals=" + victory.eventTotals() + ", pending="
                        + victory.pendingCreditRewards() + ", replay=" + replay);
                return;
            }

            this.owner.setPos(
                    this.merchant.getX(), this.merchant.getY(), this.merchant.getZ() + 1.0D);
            int expectedFriendlyPrice = FactionBalanceService.applyPercentCeil(
                    FactionBalanceService.tradeCreditPrice(
                            "galacticwars:republic",
                            LaunchContentCatalog.trades()
                                    .get("republic_quartermaster").price()),
                    90);
            this.owner.getInventory().add(
                    new ItemStack(ModItems.CREDIT_CHIP.get(), expectedFriendlyPrice));
            MerchantTradeMenu veteranTrade = (MerchantTradeMenu)
                    new MerchantTradeMenuProvider(this.merchant)
                            .createMenu(22, this.owner.getInventory(), this.owner);
            int offer = veteranTrade.tradeIds().indexOf("republic_quartermaster");
            boolean repeatedTrade = offer >= 0 && veteranTrade.handleReplayAction(
                    this.owner, UUID.randomUUID(), offer);
            ProgressionState repeated = progression.state(this.owner.getUUID());
            if (!repeatedTrade || repeated.total(ProgressionEventType.TRADE_COMPLETED) != 2
                    || repeated.pendingCreditRewards() != EXPECTED_CAMPAIGN_REWARDS
                    || countPlayerItem(this.owner, ModItems.ENERGY_CELL.get()) != 16) {
                this.phase = 2;
                this.helper.fail("Veteran trade operation was not repeatable without duplicating victory rewards");
                return;
            }

            this.owner.setPos(
                    this.hallPos.getX() + 0.5D, this.hallPos.getY(), this.hallPos.getZ() + 0.5D);
            CommandCenterOperationsMenu operations = (CommandCenterOperationsMenu)
                    new CommandCenterOperationsMenuProvider(this.hallPos)
                            .createMenu(23, this.owner.getInventory(), this.owner);
            CommandCenterDashboardState dashboard = operations.dashboardState();
            boolean claimed = operations.handleReplayAction(
                    this.owner, UUID.randomUUID(), CommandCenterOperationsMenu.CLAIM_REWARDS);
            int paidBalance = CreditTransactionService.playerBalance(this.owner);
            boolean duplicateClaimRejected = !operations.handleReplayAction(
                    this.owner, UUID.randomUUID(), CommandCenterOperationsMenu.CLAIM_REWARDS);
            Tag encoded = ProgressionSavedData.CODEC.encodeStart(
                    NbtOps.INSTANCE, progression).getOrThrow();
            ProgressionSavedData restored = ProgressionSavedData.CODEC.parse(
                    NbtOps.INSTANCE, encoded).getOrThrow();
            ProgressionState restoredVictory = restored.state(this.owner.getUUID());
            if (!dashboard.campaignVictory()
                    || dashboard.activeQuest().isPresent()
                    || dashboard.veteranVehicleDeployments() != 1
                    || dashboard.veteranTrades() != 2
                    || dashboard.veteranRegionCaptures() != 1
                    || !claimed || !duplicateClaimRejected
                    || paidBalance != EXPECTED_CAMPAIGN_REWARDS
                    || progression.pendingCreditRewards(this.owner.getUUID()) != 0
                    || !restoredVictory.hasSubject(
                    ProgressionEventType.CAMPAIGN_COMPLETED, "republic_campaign")
                    || !restoredVictory.unlocks().contains("veteran_operations")
                    || restoredVictory.total(ProgressionEventType.TRADE_COMPLETED) != 2) {
                this.phase = 2;
                this.helper.fail("Victory dashboard, reward claim, or persistence contract failed: dashboard="
                        + dashboard.campaignVictory() + "/" + dashboard.activeQuest()
                        + "/" + dashboard.veteranVehicleDeployments() + "/"
                        + dashboard.veteranTrades() + "/" + dashboard.veteranRegionCaptures()
                        + ", claim=" + claimed + "/" + duplicateClaimRejected
                        + ", paid=" + paidBalance + ", pending="
                        + progression.pendingCreditRewards(this.owner.getUUID())
                        + ", restored=" + restoredVictory);
                return;
            }
            this.phase = 2;
            this.helper.succeed();
        }
    }

    private static int countPlayerItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        return player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static final class FactionAiReactionTestScenario {
        private final GameTestHelper helper;
        private final SmartBrainTestArea area;
        private final List<GalacticRecruitEntity> responders = new ArrayList<>();
        private final long chunkWaitStartedTick;
        private boolean started;
        private boolean fixturesReady;
        private boolean complete;
        private boolean alertPrecedenceVerified;
        private boolean warningCooldownVerified;
        private long startTick;
        private UUID mainOutpostId;
        private UUID shortAlertPlayerId;
        private GalacticRecruitEntity commander;
        private GalacticRecruitEntity trader;
        private GalacticRecruitEntity civilian;
        private GalacticRecruitEntity fallbackTrooper;
        private GalacticRecruitEntity foreignOutpostTrooper;
        private double traderInitialHomeDistance;
        private double civilianInitialHomeDistance;

        private FactionAiReactionTestScenario(
                GameTestHelper helper,
                SmartBrainTestArea area
        ) {
            this.helper = helper;
            this.area = area;
            this.chunkWaitStartedTick = helper.getTick();
        }

        private void tick() {
            if (this.complete) {
                return;
            }
            if (!this.started) {
                if (!areSmartBrainAreaChunksTicking(
                        this.helper, this.area, -2, 50, -2, 12)) {
                    if (this.helper.getTick() - this.chunkWaitStartedTick
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        this.fail("Faction AI reaction area never became entity-ticking");
                    }
                    return;
                }
                this.start();
                if (this.complete) {
                    return;
                }
            }

            if (!this.fixturesReady) {
                List<GalacticRecruitEntity> fixtures = new ArrayList<>(this.responders);
                fixtures.add(this.commander);
                fixtures.add(this.trader);
                fixtures.add(this.civilian);
                fixtures.add(this.fallbackTrooper);
                fixtures.add(this.foreignOutpostTrooper);
                long indexedFixtures = fixtures.stream()
                        .filter(recruit -> this.helper.getLevel().getEntity(recruit.getUUID()) == recruit)
                        .count();
                long tickingFixtures = fixtures.stream()
                        .filter(recruit -> this.helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                                ChunkPos.containing(recruit.blockPosition())))
                        .count();
                if (indexedFixtures != fixtures.size() || tickingFixtures != fixtures.size()) {
                    if (this.helper.getTick() - this.startTick
                            >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        this.fail("Faction AI reaction fixtures never became ready: indexed="
                                + indexedFixtures + "/" + fixtures.size()
                                + ", ticking=" + tickingFixtures + "/" + fixtures.size());
                    }
                    return;
                }
                this.fixturesReady = true;
                this.startTick = this.helper.getTick();
            }

            ServerLevel level = this.helper.getLevel();
            ServerPlayer player = this.area.player();
            long elapsed = this.helper.getTick() - this.startTick;
            FactionOutpostSavedData outposts = FactionOutpostSavedData.get(level);
            boolean alertActive = outposts.activeAlert(
                    this.mainOutpostId, player.getUUID(), level.getGameTime()).isPresent();
            if (alertActive && !this.alertPrecedenceVerified) {
                FactionAlignmentSavedData.get(level).setScore(
                        player.getUUID(), FactionId.of("republic"), 10);
                if (this.commander.npcReactionTo(player).disposition()
                        != NpcDisposition.HOSTILE) {
                    this.fail("Active local alarm did not override friendly reputation");
                    return;
                }
                FactionAlignmentSavedData.get(level).setScore(
                        player.getUUID(), FactionId.of("republic"), -21);
                this.alertPrecedenceVerified = true;
            }

            if (elapsed >= 8L && outposts.activeAlert(
                    this.mainOutpostId, this.shortAlertPlayerId,
                    level.getGameTime()).isPresent()) {
                this.fail("Short-lived outpost alarm did not expire on server time");
                return;
            }
            if (elapsed < 105L) {
                return;
            }

            int selectedResponders = (int) this.responders.stream()
                    .filter(recruit -> BrainUtil.getMemory(
                            recruit,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            == player)
                    .count();
            int responderLimit = NpcFactionAiService.responderLimit(this.commander);
            boolean pvpEnabled = Config.ALLOW_BLASTER_PVP.getAsBoolean();
            boolean militaryReaction = pvpEnabled
                    ? BrainUtil.getMemory(
                            this.commander,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            == player
                            && BrainUtil.getMemory(
                            this.fallbackTrooper,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            == player
                            && selectedResponders == responderLimit
                    : BrainUtil.getMemory(
                            this.commander,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            == null
                            && BrainUtil.getMemory(
                            this.fallbackTrooper,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                            == null
                            && selectedResponders == 0;
            boolean civiliansSafe = BrainUtil.getMemory(
                    this.trader,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET) == null
                    && BrainUtil.getMemory(
                    this.civilian,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET) == null;
            boolean sameOutpostBounded = BrainUtil.getMemory(
                    this.foreignOutpostTrooper,
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET) == null;
            boolean sheltering = this.trader.distanceToSqr(
                    Vec3.atCenterOf(this.trader.getHomePosition()))
                    < this.traderInitialHomeDistance - 2.0D
                    && this.civilian.distanceToSqr(
                    Vec3.atCenterOf(this.civilian.getHomePosition()))
                    < this.civilianInitialHomeDistance - 2.0D;
            if (!this.warningCooldownVerified) {
                this.warningCooldownVerified = this.commander.tryWarnPlayer(player);
            }
            if (alertActive && this.alertPrecedenceVerified
                    && militaryReaction && civiliansSafe && sameOutpostBounded
                    && sheltering && this.warningCooldownVerified) {
                this.complete = true;
                this.cleanup();
                this.helper.succeed();
                return;
            }
            if (elapsed >= 220L) {
                this.fail("Faction AI reaction did not converge: alert=" + alertActive
                        + ", precedence=" + this.alertPrecedenceVerified
                        + ", pvp=" + pvpEnabled
                        + ", commanderTarget=" + this.commander.getTarget()
                        + ", commanderRole=" + this.commander.getNpcRole()
                        + ", commanderAuthorized="
                        + this.commander.canAttackFactionPlayer(player)
                        + ", commanderVisible=" + BrainUtil.getMemory(
                        this.commander,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER)
                        + ", commanderAttackMemory=" + BrainUtil.getMemory(
                        this.commander,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        + ", commanderRunning="
                        + this.commander.getBrain().getRunningBehaviors()
                        + ", fallbackTarget=" + this.fallbackTrooper.getTarget()
                        + ", fallbackRole=" + this.fallbackTrooper.getNpcRole()
                        + ", fallbackAuthorized="
                        + this.fallbackTrooper.canAttackFactionPlayer(player)
                        + ", fallbackVisible=" + BrainUtil.getMemory(
                        this.fallbackTrooper,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER)
                        + ", fallbackAttackMemory=" + BrainUtil.getMemory(
                        this.fallbackTrooper,
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        + ", fallbackRunning="
                        + this.fallbackTrooper.getBrain().getRunningBehaviors()
                        + ", responders=" + selectedResponders + "/" + responderLimit
                        + ", foreignTarget=" + this.foreignOutpostTrooper.getTarget()
                        + ", traderTarget=" + this.trader.getTarget()
                        + ", civilianTarget=" + this.civilian.getTarget()
                        + ", traderShelter=" + this.trader.distanceToSqr(
                        Vec3.atCenterOf(this.trader.getHomePosition()))
                        + "/" + this.traderInitialHomeDistance
                        + ", civilianShelter=" + this.civilian.distanceToSqr(
                        Vec3.atCenterOf(this.civilian.getHomePosition()))
                        + "/" + this.civilianInitialHomeDistance);
            }
        }

        private void start() {
            this.started = true;
            this.startTick = this.helper.getTick();
            ServerLevel level = this.helper.getLevel();
            ServerPlayer player = this.area.player();
            player.setGameMode(GameType.SURVIVAL);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10_000.0D);
            player.setHealth(10_000.0F);
            FactionAlignmentSavedData alignment = FactionAlignmentSavedData.get(level);

            this.mainOutpostId = UUID.randomUUID();
            BlockPos commanderPosition = this.area.at(20, 1, 1);
            this.commander = spawnRecruitAt(
                    this.helper, ModEntityTypes.CLONE_TROOPER.get(), commanderPosition);
            this.commander.setInvulnerable(true);
            this.commander.initializeBlueprintSiteResident(
                    this.mainOutpostId,
                    NpcServiceBranch.MILITARY,
                    NpcRole.COMMANDER,
                    commanderPosition,
                    64);

            List<UUID> militaryNpcIds = new ArrayList<>();
            militaryNpcIds.add(this.commander.getUUID());
            for (int index = 0; index < 13; index++) {
                BlockPos position = this.area.at(
                        42 + index % 7,
                        1,
                        1 + (index / 7) * 2);
                GalacticRecruitEntity responder = spawnRecruitAt(
                        this.helper, ModEntityTypes.CLONE_TROOPER.get(), position);
                responder.setInvulnerable(true);
                responder.initializeBlueprintSiteResident(
                        this.mainOutpostId,
                        NpcServiceBranch.MILITARY,
                        NpcRole.TROOPER,
                        position,
                        64);
                this.responders.add(responder);
                militaryNpcIds.add(responder.getUUID());
            }

            BlockPos traderHome = this.area.at(30, 1, 6);
            this.trader = spawnRecruitAt(
                    this.helper, ModEntityTypes.REPUBLIC_CIVILIAN.get(),
                    this.area.at(18, 1, 6));
            this.trader.setInvulnerable(true);
            this.trader.initializeBlueprintSiteResident(
                    this.mainOutpostId,
                    NpcServiceBranch.CIVILIAN,
                    NpcRole.TRADER,
                    traderHome,
                    64);
            this.traderInitialHomeDistance = this.trader.distanceToSqr(
                    Vec3.atCenterOf(traderHome));

            BlockPos civilianHome = this.area.at(30, 1, 8);
            this.civilian = spawnRecruitAt(
                    this.helper, ModEntityTypes.REPUBLIC_CIVILIAN.get(),
                    this.area.at(18, 1, 8));
            this.civilian.setInvulnerable(true);
            this.civilian.initializeBlueprintSiteResident(
                    this.mainOutpostId,
                    NpcServiceBranch.CIVILIAN,
                    NpcRole.CIVILIAN,
                    civilianHome,
                    64);
            this.civilianInitialHomeDistance = this.civilian.distanceToSqr(
                    Vec3.atCenterOf(civilianHome));

            FactionOutpostSavedData outposts = FactionOutpostSavedData.get(level);
            outposts.registerGeneratedSite(
                    this.mainOutpostId,
                    "galacticwars:republic",
                    level.dimension().identifier().toString(),
                    commanderPosition,
                    64,
                    militaryNpcIds,
                    List.of(this.trader.getUUID(), this.civilian.getUUID()),
                    level.getGameTime());

            UUID fallbackOutpostId = UUID.randomUUID();
            BlockPos fallbackPosition = this.area.at(5, 1, 10);
            this.fallbackTrooper = spawnRecruitAt(
                    this.helper, ModEntityTypes.CLONE_TROOPER.get(), fallbackPosition);
            this.fallbackTrooper.setInvulnerable(true);
            this.fallbackTrooper.initializeBlueprintSiteResident(
                    fallbackOutpostId,
                    NpcServiceBranch.MILITARY,
                    NpcRole.TROOPER,
                    fallbackPosition,
                    32);
            outposts.registerGeneratedSite(
                    fallbackOutpostId,
                    "galacticwars:republic",
                    level.dimension().identifier().toString(),
                    fallbackPosition,
                    32,
                    List.of(this.fallbackTrooper.getUUID()),
                    List.of(),
                    level.getGameTime());

            UUID foreignOutpostId = UUID.randomUUID();
            BlockPos foreignPosition = this.area.at(44, 1, 10);
            this.foreignOutpostTrooper = spawnRecruitAt(
                    this.helper, ModEntityTypes.CLONE_TROOPER.get(), foreignPosition);
            this.foreignOutpostTrooper.setInvulnerable(true);
            this.foreignOutpostTrooper.initializeBlueprintSiteResident(
                    foreignOutpostId,
                    NpcServiceBranch.MILITARY,
                    NpcRole.TROOPER,
                    foreignPosition,
                    32);
            outposts.registerGeneratedSite(
                    foreignOutpostId,
                    "galacticwars:republic",
                    level.dimension().identifier().toString(),
                    foreignPosition,
                    32,
                    List.of(this.foreignOutpostTrooper.getUUID()),
                    List.of(),
                    level.getGameTime());

            alignment.setScore(player.getUUID(), FactionId.of("republic"), 10);
            NpcDisposition friendly = this.commander.npcReactionTo(player).disposition();
            alignment.setScore(player.getUUID(), FactionId.of("republic"), 0);
            NpcDisposition neutral = this.commander.npcReactionTo(player).disposition();
            alignment.setScore(player.getUUID(), FactionId.of("republic"), -1);
            NpcDisposition wary = this.commander.npcReactionTo(player).disposition();
            boolean warned = this.commander.tryWarnPlayer(player);
            boolean warningSuppressed = !this.commander.tryWarnPlayer(player);
            alignment.setScore(player.getUUID(), FactionId.of("republic"), -21);
            NpcDisposition hostile = this.commander.npcReactionTo(player).disposition();
            if (friendly != NpcDisposition.FRIENDLY
                    || neutral != NpcDisposition.NEUTRAL
                    || wary != NpcDisposition.WARY
                    || hostile != NpcDisposition.HOSTILE
                    || !warned
                    || !warningSuppressed) {
                this.fail("Live disposition thresholds or warning cooldown failed: "
                        + friendly + "/" + neutral + "/" + wary + "/" + hostile
                        + ", warning=" + warned + "/" + warningSuppressed);
                return;
            }
            if (Config.ALLOW_BLASTER_PVP.getAsBoolean()
                    && !this.commander.canAttackFactionPlayer(player)) {
                this.fail("Hostile survival player failed the final attack authorization gate: "
                        + "spectator=" + player.isSpectator()
                        + ", infinite=" + player.hasInfiniteMaterials()
                        + ", gameMode=" + player.gameMode.getGameModeForPlayer()
                        + ", disposition=" + this.commander.npcReactionTo(player).disposition()
                        + ", role=" + this.commander.getNpcRole()
                        + ", group=" + this.commander.getArmyGroupId());
                return;
            }

            this.shortAlertPlayerId = UUID.randomUUID();
            outposts.raiseAlert(
                    this.mainOutpostId,
                    this.shortAlertPlayerId,
                    level.getGameTime(),
                    5,
                    "expiry_probe");
        }

        private void fail(String message) {
            this.complete = true;
            this.cleanup();
            this.helper.fail(message);
        }

        private void cleanup() {
            if (this.commander != null) {
                this.commander.discard();
            }
            this.responders.forEach(Entity::discard);
            if (this.trader != null) {
                this.trader.discard();
            }
            if (this.civilian != null) {
                this.civilian.discard();
            }
            if (this.fallbackTrooper != null) {
                this.fallbackTrooper.discard();
            }
            if (this.foreignOutpostTrooper != null) {
                this.foreignOutpostTrooper.discard();
            }
        }
    }

    private static final class NaturalCivilianTestScenario {
        private final GameTestHelper helper;
        private final SmartBrainTestArea area;
        private boolean started;
        private boolean shelterVerified;
        private boolean workerPrepared;
        private boolean complete;
        private long startTick;
        private long workerSpawnTick;
        private long workStartTick;
        private BlockPos shelterHome;
        private BlockPos storage;
        private GalacticRecruitEntity sheltering;
        private GalacticRecruitEntity threat;
        private GalacticRecruitEntity worker;
        private double initialShelterDistance;

        private NaturalCivilianTestScenario(GameTestHelper helper, SmartBrainTestArea area) {
            this.helper = helper;
            this.area = area;
        }

        private void tick() {
            if (this.complete) {
                return;
            }
            if (!this.started) {
                BlockPos spawnPosition = this.area.at(1, 1, 1);
                if (!this.helper.getLevel().areEntitiesActuallyLoadedAndTicking(
                        new ChunkPos(spawnPosition.getX() >> 4, spawnPosition.getZ() >> 4))) {
                    if (this.helper.getTick() >= SMART_BRAIN_CHUNK_READY_TIMEOUT) {
                        this.complete = true;
                        this.helper.fail("Natural civilian GameTest chunk never reached entity-ticking status");
                    }
                    return;
                }
                this.start();
            }
            if (!this.shelterVerified) {
                if (this.sheltering.distanceToSqr(Vec3.atCenterOf(this.shelterHome))
                        < this.initialShelterDistance - 4.0D) {
                    this.startWorkerScenario();
                } else if (this.helper.getTick() - this.startTick >= 120L) {
                    this.complete = true;
                    this.helper.fail("Natural civilian shelter behaviour did not retreat from a hostile soldier: position="
                            + this.sheltering.position() + ", home=" + this.shelterHome
                            + ", orderedSit=" + this.sheltering.isOrderedToSit()
                            + ", tickCount=" + this.sheltering.tickCount
                            + ", branch=" + this.sheltering.getServiceBranch()
                            + ", outpost=" + this.sheltering.getFactionOutpostId()
                            + ", group=" + this.sheltering.getArmyGroupId()
                            + ", threatAlive=" + this.threat.isAlive()
                            + ", threatDistance=" + this.sheltering.distanceToSqr(this.threat)
                            + ", hostile=" + this.sheltering.isHostileFactionRecruit(this.threat)
                            + ", walkMemory=" + BrainUtil.hasMemory(this.sheltering,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)
                            + ", navigationDone=" + this.sheltering.getNavigation().isDone()
                            + ", pathMemory=" + BrainUtil.hasMemory(this.sheltering,
                            net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH)
                            + ", running=" + this.sheltering.getBrain().getRunningBehaviors()
                            + ", path=" + pathState(this.sheltering, this.shelterHome));
                }
                return;
            }
            if (!this.workerPrepared && this.helper.getTick() - this.workerSpawnTick >= 2L) {
                this.prepareWorker();
            }
            if (this.workerPrepared && this.helper.getTick() - this.workStartTick >= 30L) {
                this.complete = true;
                verifyNaturalCivilianWork(
                        this.helper, this.sheltering, this.worker, this.storage);
            }
        }

        private void start() {
            this.started = true;
            this.startTick = this.helper.getTick();
            this.shelterHome = this.area.at(4, 1, 4);
            this.sheltering = spawnRecruitAt(
                    this.helper, ModEntityTypes.REPUBLIC_CIVILIAN.get(), this.area.at(1, 1, 1));
            this.sheltering.setInvulnerable(true);
            this.sheltering.initializeNaturalFactionNpc(
                    UUID.randomUUID(), NpcServiceBranch.CIVILIAN, this.shelterHome, 32);
            this.initialShelterDistance = this.sheltering.distanceToSqr(
                    Vec3.atCenterOf(this.shelterHome));
            this.threat = spawnRecruitAt(
                    this.helper, ModEntityTypes.B1_BATTLE_DROID.get(), this.area.at(1, 1, 2));
            this.threat.setInvulnerable(true);
            this.threat.setNoAi(true);
            this.threat.initializeNaturalFactionNpc(
                    UUID.randomUUID(), NpcServiceBranch.MILITARY);
            if (!this.sheltering.isHostileFactionRecruit(this.threat)) {
                this.complete = true;
                this.helper.fail("Natural civilian test threat is not hostile: relation="
                        + this.sheltering.factionRelationTo(this.threat)
                        + ", civilianFaction=" + this.sheltering.getRecruitFactionId()
                        + ", threatFaction=" + this.threat.getRecruitFactionId()
                        + ", threatDuty=" + this.threat.getRecruitDuty());
                return;
            }
            BlockPos workHome = this.area.at(3, 1, 0);
            this.storage = workHome.offset(1, 0, 0);
            this.helper.getLevel().setBlockAndUpdate(this.storage, Blocks.CHEST.defaultBlockState());
        }

        private void startWorkerScenario() {
            this.shelterVerified = true;
            this.threat.discard();
            this.helper.setTime(6000L);
            BlockPos workHome = this.area.at(3, 1, 0);
            this.worker = spawnRecruitAt(
                    this.helper, ModEntityTypes.HUTT_CIVILIAN.get(), this.area.at(1, 1, 1));
            this.worker.setInvulnerable(true);
            this.worker.initializeNaturalFactionNpc(
                    UUID.randomUUID(), NpcServiceBranch.CIVILIAN, workHome, 32);
            this.workerSpawnTick = this.helper.getTick();
        }

        private void prepareWorker() {
            this.workerPrepared = true;
            this.worker.setWorkerProfession(WorkerProfession.FARMER);
            setRecruitField(this.worker, "nextNaturalProductionGameTime", 0L);
            BlockPos workstation = this.worker.naturalWorkstationPosition();
            this.worker.setPos(
                    workstation.getX() + 0.5D,
                    workstation.getY(),
                    workstation.getZ() + 0.5D);
            this.worker.getNavigation().stop();
            this.workStartTick = this.helper.getTick();
        }
    }

    private static void setActiveSupplyReservation(
            GalacticRecruitEntity recruit,
            UUID reservationId
    ) {
        WorkerExecutionState execution = (WorkerExecutionState) getRecruitField(
                recruit, "workerExecutionState");
        setRecruitField(
                recruit,
                "workerExecutionState",
                execution.withSupplyReservation(Optional.of(reservationId)));
    }

    private static void invokeAcquireCourierOrder(GalacticRecruitEntity recruit) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod("acquireCourierOrder");
            method.setAccessible(true);
            method.invoke(recruit);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not acquire courier work", exception);
        }
    }

    private static boolean invokeReleaseActiveSupplyReservation(
            GalacticRecruitEntity recruit
    ) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod(
                    "releaseActiveSupplyReservation");
            method.setAccessible(true);
            return (boolean) method.invoke(recruit);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not release courier supply reservation", exception);
        }
    }

    private static void invokeWorkerAuthorityReconciliation(
            GalacticRecruitEntity recruit,
            net.minecraft.server.level.ServerLevel level
    ) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod(
                    "reconcileWorkerAuthority", net.minecraft.server.level.ServerLevel.class);
            method.setAccessible(true);
            method.invoke(recruit, level);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not reconcile recruit worker authority", exception);
        }
    }

    private static void invokeGameplayDataRefresh(GalacticRecruitEntity recruit) {
        try {
            Method method = GalacticRecruitEntity.class.getDeclaredMethod("applyUnitDefinition");
            method.setAccessible(true);
            method.invoke(recruit);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not refresh recruit gameplay data", exception);
        }
    }

    private static CompoundTag saveRecruit(GalacticRecruitEntity recruit, ServerLevel level) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess());
        if (!recruit.save(output)) {
            throw new IllegalStateException("Could not serialize recruit " + recruit.getUUID());
        }
        return output.buildResult();
    }

    private static GalacticRecruitEntity loadRecruit(CompoundTag tag, ServerLevel level) {
        Entity loaded = EntityType.loadEntityRecursive(
                tag,
                level,
                new EntitySpawnRequest(EntitySpawnReason.LOAD, false),
                entity -> entity);
        if (!(loaded instanceof GalacticRecruitEntity recruit)) {
            throw new IllegalStateException("Serialized recruit did not load as a Galactic recruit");
        }
        return recruit;
    }

    private static void assertRecruitStack(
            GameTestHelper helper,
            ItemStack expected,
            ItemStack actual,
            String message
    ) {
        if (expected.getCount() != actual.getCount()
                || !ItemStack.isSameItemSameComponents(expected, actual)) {
            helper.fail(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private record SpawnEggCase(
            RecruitSpawnEggItem item,
            EntityType<GalacticRecruitEntity> type,
            boolean civilian
    ) {
        private SpawnEggCase(
                RecruitSpawnEggItem item,
                EntityType<GalacticRecruitEntity> type
        ) {
            this(item, type, false);
        }
    }

    private record SmartBrainTestArea(BlockPos base, ServerPlayer player) {
        private BlockPos at(int x, int y, int z) {
            return base.offset(x, y, z);
        }
    }

    private static BlockPos isolatedCapital(GameTestHelper helper, int lane) {
        return helper.absolutePos(new BlockPos(1, 1, 1)).offset(0, 0, lane * 10_000);
    }

    private static CommandCenterBlockEntity placeCommandCenter(GameTestHelper helper, BlockPos position) {
        helper.getLevel().getChunkAt(position);
        if (!helper.getLevel().setBlock(position, ModBlocks.COMMAND_CENTER.get().defaultBlockState(), 3)
                || !(helper.getLevel().getBlockEntity(position) instanceof CommandCenterBlockEntity hall)) {
            throw new IllegalStateException("Could not place isolated GameTest Command Center at " + position);
        }
        return hall;
    }

    @SuppressWarnings("removal")
    private static ServerPlayer makeConnectedMockPlayer(GameTestHelper helper, GameType gameType) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        gameType.updatePlayerAbilities(player.getAbilities());
        return player;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GalacticWars.MODID, path);
    }
}
