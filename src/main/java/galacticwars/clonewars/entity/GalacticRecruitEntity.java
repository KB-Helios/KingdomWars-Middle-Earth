package galacticwars.clonewars.entity;

import dev.architectury.registry.menu.MenuRegistry;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import galacticwars.clonewars.Config;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.army.ArmyEquipmentLoadout;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyCommandPolicy;
import galacticwars.clonewars.army.ArmyFormation;
import galacticwars.clonewars.army.ArmyGroupOrder;
import galacticwars.clonewars.army.ArmyGroupOrderAssignment;
import galacticwars.clonewars.army.ArmyGroupOrderPlanner;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyLocation;
import galacticwars.clonewars.army.ArmyPosition;
import galacticwars.clonewars.army.ArmyMemberSnapshot;
import galacticwars.clonewars.army.ArmySnapshotEquipment;
import galacticwars.clonewars.army.ArmyUnitDefinition;
import galacticwars.clonewars.army.ArmyFormationRole;
import galacticwars.clonewars.army.RecruitVitals;
import galacticwars.clonewars.classes.ClassAbilityRuntimeService;
import galacticwars.clonewars.classes.ClassAbilityEffectRegistry;
import galacticwars.clonewars.classes.ClassProgressCodecs;
import galacticwars.clonewars.classes.ClassProgressState;
import galacticwars.clonewars.classes.UnitClassDefinition;
import galacticwars.clonewars.client.ClientGameplayCatalog;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.data.GameplayDataSnapshot;
import galacticwars.clonewars.entity.ai.RecruitBrain;
import galacticwars.clonewars.entity.ai.ArmyBrainMemoryTypes;
import galacticwars.clonewars.entity.ai.RecruitNavigationResult;
import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.faction.FactionAlignment;
import galacticwars.clonewars.faction.FactionAlignmentSavedData;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.FactionDefinition;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRelation;
import galacticwars.clonewars.faction.ai.FactionReputationEvent;
import galacticwars.clonewars.faction.ai.FactionReputationService;
import galacticwars.clonewars.faction.ai.NpcDisposition;
import galacticwars.clonewars.faction.ai.NpcFactionAiService;
import galacticwars.clonewars.faction.ai.NpcReactionDecision;
import galacticwars.clonewars.faction.ai.NpcRole;
import galacticwars.clonewars.force.NpcForceRuntimeService;
import galacticwars.clonewars.kingdom.KingdomFactionRelations;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.kingdom.KingdomActionId;
import galacticwars.clonewars.kingdom.KingdomGameplayAction;
import galacticwars.clonewars.kingdom.KingdomGameplayResult;
import galacticwars.clonewars.kingdom.KingdomGameplayRuntimeService;
import galacticwars.clonewars.kingdom.KingdomGameplayTransactionService;
import galacticwars.clonewars.kingdom.BuildProject;
import galacticwars.clonewars.kingdom.CommanderPolicy;
import galacticwars.clonewars.kingdom.RecruitmentCampaign;
import galacticwars.clonewars.kingdom.RecruitmentCampaignDecision;
import galacticwars.clonewars.kingdom.RecruitmentEligibility;
import galacticwars.clonewars.kingdom.RecruitmentService;
import galacticwars.clonewars.kingdom.StorageEndpoint;
import galacticwars.clonewars.kingdom.WorkOrder;
import galacticwars.clonewars.kingdom.WorkOrderState;
import galacticwars.clonewars.kingdom.WorkOrderType;
import galacticwars.clonewars.kingdom.WorksiteRecord;
import galacticwars.clonewars.item.CommandTargetSelection;
import galacticwars.clonewars.menu.RecruitCommandMenu;
import galacticwars.clonewars.menu.RecruitCommandAction;
import galacticwars.clonewars.menu.RecruitCommandMenuProvider;
import galacticwars.clonewars.menu.RecruitLoadoutMenuProvider;
import galacticwars.clonewars.menu.MerchantTradeMenuProvider;
import galacticwars.clonewars.recruitment.RecruitmentAction;
import galacticwars.clonewars.progression.ProgressionEventType;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.recruitment.RecruitDuty;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.recruitment.RecruitmentPaymentService;
import galacticwars.clonewars.registry.ModBlocks;
import galacticwars.clonewars.registry.ModBlockTags;
import galacticwars.clonewars.registry.ModEntityTypes;
import galacticwars.clonewars.registry.ModDataComponents;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.settlement.BaseBlockPlacement;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import galacticwars.clonewars.settlement.KingdomBaseBuildAction;
import galacticwars.clonewars.settlement.KingdomBaseBuildDecision;
import galacticwars.clonewars.settlement.KingdomBaseBuildPlanner;
import galacticwars.clonewars.settlement.KingdomSettlementPlanner;
import galacticwars.clonewars.settlement.KingdomSettlementState;
import galacticwars.clonewars.settlement.KingdomWorkOrder;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.settlement.ConstructionPlan;
import galacticwars.clonewars.workforce.ResourceInventory;
import galacticwars.clonewars.workforce.CourierTransferAction;
import galacticwars.clonewars.workforce.CourierTransferType;
import galacticwars.clonewars.workforce.CourierRouteExecutionState;
import galacticwars.clonewars.workforce.CourierRoutePlan;
import galacticwars.clonewars.workforce.CourierRoutePlanner;
import galacticwars.clonewars.workforce.CourierWaypoint;
import galacticwars.clonewars.workforce.WorkAreaType;
import galacticwars.clonewars.workforce.WorkerLogisticsDecision;
import galacticwars.clonewars.workforce.WorkerLogisticsPlanner;
import galacticwars.clonewars.workforce.WorkerLogisticsRoute;
import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.WorkerProfessionCatalog;
import galacticwars.clonewars.workforce.WorkerProfessionDefinition;
import galacticwars.clonewars.workforce.WorkerAssignment;
import galacticwars.clonewars.workforce.WorkerContractService;
import galacticwars.clonewars.workforce.WorkerDutyLoadoutPolicy;
import galacticwars.clonewars.workforce.WorkerPhase;
import galacticwars.clonewars.workforce.WorkerResourceAction;
import galacticwars.clonewars.workforce.WorkerResourceDecision;
import galacticwars.clonewars.workforce.WorkerResourcePlanner;
import galacticwars.clonewars.workforce.WorkerTaskDecision;
import galacticwars.clonewars.workforce.WorkerTaskPlanner;
import galacticwars.clonewars.workforce.WorkerStatus;
import galacticwars.clonewars.workforce.WorkerWorksite;
import galacticwars.clonewars.workforce.WorkforceCodecs;
import galacticwars.clonewars.workforce.logistics.LogisticsAccessPolicy;
import galacticwars.clonewars.workforce.logistics.LogisticsEndpoint;
import galacticwars.clonewars.workforce.logistics.LogisticsEndpointIdentity;
import galacticwars.clonewars.workforce.logistics.LogisticsTransferAuthority;
import galacticwars.clonewars.workforce.logistics.LogisticsTransferRequest;
import galacticwars.clonewars.workforce.logistics.PhysicalLogisticsTransaction;
import galacticwars.clonewars.world.CivilianArchetypeDefinition;
import galacticwars.clonewars.world.FactionOutpostMarkerService;
import galacticwars.clonewars.world.FactionOutpostRecord;
import galacticwars.clonewars.world.FactionOutpostSavedData;
import galacticwars.clonewars.world.OverworldFactionSpawnProfile;
import galacticwars.clonewars.world.PlanetFactionSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.tslat.smartbrainlib.api.SmartBrainBuilder;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class GalacticRecruitEntity extends TamableAnimal
        implements GeoEntity, SmartBrainOwner<GalacticRecruitEntity> {
    private static final int DEFAULT_WORK_RADIUS = 8;
    private static final int MIN_WORK_RADIUS = 2;
    private static final int MAX_WORK_RADIUS = 32;
    private static final int WORK_RADIUS_STEP = 2;
    private static final EntityDataAccessor<Integer> DATA_COMMAND =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WORKER_PROFESSION =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RECRUIT_DUTY =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_WORK_TARGET =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_STORAGE_TARGET =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BASE_TARGET =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> DATA_STORAGE_RESOURCE_COUNT =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CARRIED_RESOURCE_COUNT =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BASE_PROGRESS =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WORK_RADIUS =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_RESOURCE_ACTION =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_RESOURCE_ITEM =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_WORKER_PHASE =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_WORKER_REASON =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_ACTIVE_WORK_TARGET =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<String> DATA_SELECTED_BLUEPRINT =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_VISUAL_ACTION =
            SynchedEntityData.defineId(GalacticRecruitEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation VISUAL_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation VISUAL_WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation VISUAL_BUILD = RawAnimation.begin().thenLoop("attack.swing");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable BlockPos moveTarget;
    private @Nullable BlockPos workTarget;
    private @Nullable BlockPos storageTarget;
    private @Nullable BlockPos baseTarget;
    private int starterBaseCompletedBlocks;
    private int buildRotationSteps;
    private @Nullable UUID activeBuildProjectId;
    private String selectedBlueprintId = KingdomBaseBlueprint.STARTER_KEEP_ID;
    private int workRadius = DEFAULT_WORK_RADIUS;
    private int acknowledgeOrderTicks;
    private @Nullable UUID kingdomId;
    private @Nullable UUID settlementId;
    private @Nullable UUID factionOutpostId;
    private @Nullable UUID armyGroupId;
    private @Nullable UUID workOrderId;
    private NonNullList<ItemStack> workerInventory = NonNullList.withSize(9, ItemStack.EMPTY);
    private ItemStack inactiveDutyMainHand = ItemStack.EMPTY;
    private boolean defaultLoadoutInitialized;
    private CourierRouteExecutionState courierRouteState = CourierRouteExecutionState.start(0L);
    private WorkerPhase workerPhase = WorkerPhase.ACQUIRE_ORDER;
    private String workerReason = "ready";
    private @Nullable BlockPos activeWorkTarget;
    private int workerCooldownTicks;
    private int workerNavigationFailures;
    private int workerScanCursor;
    private @Nullable BlockPos blacklistedWorkTarget;
    private int blacklistedWorkTargetTicks;
    private long lastCommanderCampaignGameTime;
    private boolean deathResourcesReleased;
    private boolean spawnEggInitialized;
    private boolean naturalPlanetNpcInitialized;
    private boolean pendingNaturalSpawnRemoval;
    private boolean pendingNaturalSpawnInitialization;
    private boolean factionOutpostSiteGenerationResolved;
    private String unitId = "";
    private NpcServiceBranch serviceBranch = NpcServiceBranch.MILITARY;
    private long appliedGameplayDataGeneration = -1L;
    private int morale = 100;
    private int hunger = 100;
    private int unpaidTicks;
    private ClassProgressState classProgressState = ClassProgressState.unassigned();
    private int npcForceEnergy = 100;
    private final long[] npcForceCooldownEnds = new long[3];
    private int npcForceLoadoutCursor;
    private long armySnapshotGeneration;
    private long nextNaturalProductionGameTime;
    private final Map<UUID, Long> factionWarningCooldowns = new LinkedHashMap<>();

    public GalacticRecruitEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void registerGoals() {
        // SmartBrainLib owns all recruit scheduling; vanilla selectors must stay empty.
    }

    @Override
    public SmartBrainBuilder<GalacticRecruitEntity> getBrainBuilder() {
        return RecruitBrain.INSTANCE;
    }

    @Override
    public List<? extends net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor<?>> getSensors(
            GalacticRecruitEntity owner
    ) {
        return RecruitBrain.INSTANCE.getSensors(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_COMMAND, RecruitmentAction.FOLLOW_OWNER.ordinal());
        entityData.define(DATA_WORKER_PROFESSION, -1);
        entityData.define(DATA_RECRUIT_DUTY, RecruitDuty.SOLDIER.ordinal());
        entityData.define(DATA_WORK_TARGET, Optional.empty());
        entityData.define(DATA_STORAGE_TARGET, Optional.empty());
        entityData.define(DATA_BASE_TARGET, Optional.empty());
        entityData.define(DATA_STORAGE_RESOURCE_COUNT, 0);
        entityData.define(DATA_CARRIED_RESOURCE_COUNT, 0);
        entityData.define(DATA_BASE_PROGRESS, 0);
        entityData.define(DATA_WORK_RADIUS, DEFAULT_WORK_RADIUS);
        entityData.define(DATA_RESOURCE_ACTION, "");
        entityData.define(DATA_RESOURCE_ITEM, "");
        entityData.define(DATA_WORKER_PHASE, WorkerPhase.ACQUIRE_ORDER.id());
        entityData.define(DATA_WORKER_REASON, "ready");
        entityData.define(DATA_ACTIVE_WORK_TARGET, Optional.empty());
        entityData.define(DATA_SELECTED_BLUEPRINT, KingdomBaseBlueprint.STARTER_KEEP_ID);
        entityData.define(DATA_VISUAL_ACTION, RecruitVisualAction.IDLE.id());
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("recruit_gameplay", 4, state -> {
            RecruitVisualAction action = RecruitVisualAction.byId(this.entityData.get(DATA_VISUAL_ACTION));
            if (action == RecruitVisualAction.BUILD && !state.isMoving()) {
                return state.setAndContinue(VISUAL_BUILD);
            }
            boolean locomotion = state.isMoving() && switch (action) {
                case FORM_UP, MARCH, RETREAT, BUILD, RANGED_COMBAT, MELEE_COMBAT,
                     ACKNOWLEDGE_ORDER, INJURED, LOW_MORALE -> true;
                default -> false;
            };
            return state.setAndContinue(locomotion ? VISUAL_WALK : VISUAL_IDLE);
        }));
        controllers.add(DefaultAnimations.genericAttackAnimation(DefaultAnimations.ATTACK_SWING));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("RecruitCommand", this.getRecruitCommand().name());
        output.putString("RecruitDuty", this.getRecruitDuty().id());
        output.putString("ServiceBranch", this.serviceBranch.id());
        output.putString("UnitId", this.unitId);
        output.putInt("Morale", this.morale);
        output.putInt("Hunger", this.hunger);
        output.putInt("UnpaidTicks", this.unpaidTicks);
        output.store("ClassProgress", ClassProgressCodecs.CODEC, this.classProgressState);
        output.putInt("NpcForceEnergy", this.npcForceEnergy);
        output.putInt("NpcForceLoadoutCursor", this.npcForceLoadoutCursor);
        for (int slot = 0; slot < this.npcForceCooldownEnds.length; slot++) {
            output.putLong("NpcForceCooldown" + slot, this.npcForceCooldownEnds[slot]);
        }
        output.putLong("ArmySnapshotGeneration", this.armySnapshotGeneration);
        output.putLong("NextNaturalProductionGameTime", this.nextNaturalProductionGameTime);
        output.putBoolean("NaturalPlanetNpc", this.naturalPlanetNpcInitialized);
        output.putBoolean("PendingNaturalSpawnRemoval", this.pendingNaturalSpawnRemoval);
        output.putBoolean("PendingNaturalSpawnInitialization", this.pendingNaturalSpawnInitialization);
        this.getWorkerProfession().ifPresent(profession -> output.putString("WorkerProfession", profession.id()));
        output.putInt("RecruitDataVersion", 14);
        output.putBoolean("DefaultLoadoutInitialized", this.defaultLoadoutInitialized);
        output.store("InactiveDutyMainHand", ItemStack.OPTIONAL_CODEC, this.inactiveDutyMainHand);
        output.storeNullable("KingdomId", UUIDUtil.CODEC, this.kingdomId);
        output.storeNullable("SettlementId", UUIDUtil.CODEC, this.settlementId);
        output.storeNullable("FactionOutpostId", UUIDUtil.CODEC, this.factionOutpostId);
        output.storeNullable("ArmyGroupId", UUIDUtil.CODEC, this.armyGroupId);
        output.storeNullable("WorkOrderId", UUIDUtil.CODEC, this.workOrderId);
        output.storeNullable("ActiveBuildProjectId", UUIDUtil.CODEC, this.activeBuildProjectId);
        ValueOutput workerInventoryOutput = output.child("WorkerInventory");
        ContainerHelper.saveAllItems(workerInventoryOutput, this.workerInventory);
        output.store("CourierRouteState", WorkforceCodecs.COURIER_ROUTE_EXECUTION_STATE,
                this.courierRouteState);
        output.putString("WorkerPhase", this.workerPhase.id());
        output.putString("WorkerReason", this.workerReason);
        output.putInt("WorkerCooldown", this.workerCooldownTicks);
        output.putLong("LastCommanderCampaignGameTime", this.lastCommanderCampaignGameTime);
        if (this.activeWorkTarget != null) {
            output.putInt("ActiveWorkTargetX", this.activeWorkTarget.getX());
            output.putInt("ActiveWorkTargetY", this.activeWorkTarget.getY());
            output.putInt("ActiveWorkTargetZ", this.activeWorkTarget.getZ());
        }
        output.putInt("BlacklistedWorkTargetTicks", this.blacklistedWorkTargetTicks);
        if (this.blacklistedWorkTarget != null) {
            output.putInt("BlacklistedWorkTargetX", this.blacklistedWorkTarget.getX());
            output.putInt("BlacklistedWorkTargetY", this.blacklistedWorkTarget.getY());
            output.putInt("BlacklistedWorkTargetZ", this.blacklistedWorkTarget.getZ());
        }
        output.putInt("StarterBaseCompletedBlocks", this.starterBaseCompletedBlocks);
        output.putInt("BuildRotationSteps", this.buildRotationSteps);
        output.putString("SelectedBlueprint", this.selectedBlueprintId);
        output.putInt("WorkRadius", this.workRadius);
        if (this.workTarget != null) {
            output.putInt("WorkTargetX", this.workTarget.getX());
            output.putInt("WorkTargetY", this.workTarget.getY());
            output.putInt("WorkTargetZ", this.workTarget.getZ());
        }
        if (this.storageTarget != null) {
            output.putInt("StorageTargetX", this.storageTarget.getX());
            output.putInt("StorageTargetY", this.storageTarget.getY());
            output.putInt("StorageTargetZ", this.storageTarget.getZ());
        }
        if (this.baseTarget != null) {
            output.putInt("BaseTargetX", this.baseTarget.getX());
            output.putInt("BaseTargetY", this.baseTarget.getY());
            output.putInt("BaseTargetZ", this.baseTarget.getZ());
        }
        if (this.moveTarget != null) {
            output.putInt("MoveTargetX", this.moveTarget.getX());
            output.putInt("MoveTargetY", this.moveTarget.getY());
            output.putInt("MoveTargetZ", this.moveTarget.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setRecruitCommand(parseCommand(input.getStringOr("RecruitCommand", RecruitmentAction.FOLLOW_OWNER.name())));
        this.setRecruitDuty(RecruitDuty.byId(input.getStringOr("RecruitDuty", RecruitDuty.SOLDIER.id())));
        this.serviceBranch = NpcServiceBranch.byId(input.getStringOr(
                "ServiceBranch", NpcServiceBranch.migrate(this.getRecruitDuty()).id()));
        String savedUnitId = input.getStringOr("UnitId", "");
        this.unitId = GameplayDataManager.snapshot().unit(savedUnitId)
                .map(definition -> definition.id().toString())
                .orElse(savedUnitId);
        this.morale = clampVital(input.getIntOr("Morale", 100));
        this.hunger = clampVital(input.getIntOr("Hunger", 100));
        this.unpaidTicks = Math.max(0, input.getIntOr("UnpaidTicks", 0));
        this.classProgressState = input.read("ClassProgress", ClassProgressCodecs.CODEC)
                .orElseGet(ClassProgressState::unassigned);
        this.npcForceEnergy = Math.max(0, Math.min(100, input.getIntOr("NpcForceEnergy", 100)));
        this.npcForceLoadoutCursor = Math.floorMod(input.getIntOr("NpcForceLoadoutCursor", 0), 3);
        for (int slot = 0; slot < this.npcForceCooldownEnds.length; slot++) {
            this.npcForceCooldownEnds[slot] = Math.max(
                    0L, input.getLongOr("NpcForceCooldown" + slot, 0L));
        }
        this.armySnapshotGeneration = Math.max(0L, input.getLongOr("ArmySnapshotGeneration", 0L));
        this.nextNaturalProductionGameTime = Math.max(
                0L, input.getLongOr("NextNaturalProductionGameTime", 0L));
        this.naturalPlanetNpcInitialized = input.getBooleanOr("NaturalPlanetNpc", false);
        this.pendingNaturalSpawnRemoval = input.getBooleanOr(
                "PendingNaturalSpawnRemoval", false);
        this.pendingNaturalSpawnInitialization = input.getBooleanOr(
                "PendingNaturalSpawnInitialization", false);
        WorkerProfession.byId(input.getStringOr("WorkerProfession", ""))
                .ifPresentOrElse(
                        profession -> this.entityData.set(DATA_WORKER_PROFESSION, profession.ordinal()),
                        () -> this.entityData.set(DATA_WORKER_PROFESSION, -1));
        int dataVersion = input.getIntOr("RecruitDataVersion", 0);
        if (dataVersion >= 13) {
            this.defaultLoadoutInitialized = input.getBooleanOr(
                    "DefaultLoadoutInitialized", true);
            this.inactiveDutyMainHand = input.read(
                            "InactiveDutyMainHand", ItemStack.OPTIONAL_CODEC)
                    .orElse(ItemStack.EMPTY);
        } else {
            // The active main hand was already restored by the vanilla entity loader.
            // Older worker saves did not preserve their military weapon, so seed only
            // the newly introduced inactive slot from the unit definition.
            this.defaultLoadoutInitialized = true;
            this.inactiveDutyMainHand = this.serviceBranch == NpcServiceBranch.CIVILIAN
                    ? this.defaultMilitaryMainHand()
                    : ItemStack.EMPTY;
        }
        if (dataVersion < 14) {
            this.migrateLegacyIronSwordLoadout();
        }
        if (dataVersion < 3 && (!input.getStringOr("WorkerCarriedResources", "").isBlank()
                || !input.getStringOr("WorkerStorageResources", "").isBlank())) {
            GalacticWars.LOGGER.warn(
                    "Reset legacy synthetic worker resource counters for recruit {} during schema migration",
                    this.getUUID());
        }
        this.starterBaseCompletedBlocks = dataVersion >= 2
                ? Math.max(0, input.getIntOr("StarterBaseCompletedBlocks", 0))
                : 0;
        String savedBlueprintId = GameplayDataSnapshot.normalizeBlueprintId(input.getStringOr(
                "SelectedBlueprint", KingdomBaseBlueprint.STARTER_KEEP_ID));
        this.selectedBlueprintId = savedBlueprintId.isBlank()
                ? KingdomBaseBlueprint.STARTER_KEEP_ID
                : savedBlueprintId;
        this.entityData.set(DATA_SELECTED_BLUEPRINT, this.selectedBlueprintId);
        this.kingdomId = input.read("KingdomId", UUIDUtil.CODEC).orElse(null);
        this.settlementId = input.read("SettlementId", UUIDUtil.CODEC).orElse(null);
        this.factionOutpostId = input.read("FactionOutpostId", UUIDUtil.CODEC).orElse(null);
        this.armyGroupId = input.read("ArmyGroupId", UUIDUtil.CODEC).orElse(null);
        this.workOrderId = input.read("WorkOrderId", UUIDUtil.CODEC).orElse(null);
        this.activeBuildProjectId = input.read("ActiveBuildProjectId", UUIDUtil.CODEC).orElse(null);
        this.buildRotationSteps = Math.floorMod(input.getIntOr("BuildRotationSteps", 0), 4);
        this.workerInventory = NonNullList.withSize(9, ItemStack.EMPTY);
        input.child("WorkerInventory").ifPresent(child -> ContainerHelper.loadAllItems(child, this.workerInventory));
        this.courierRouteState = input.read(
                        "CourierRouteState", WorkforceCodecs.COURIER_ROUTE_EXECUTION_STATE)
                .orElseGet(() -> CourierRouteExecutionState.start(0L));
        this.workerPhase = WorkerPhase.byId(input.getStringOr("WorkerPhase", WorkerPhase.ACQUIRE_ORDER.id()));
        this.workerReason = input.getStringOr("WorkerReason", "ready");
        this.workerCooldownTicks = Math.max(0, input.getIntOr("WorkerCooldown", 0));
        this.lastCommanderCampaignGameTime = Math.max(0L, input.getLongOr("LastCommanderCampaignGameTime", 0L));
        if (input.getInt("ActiveWorkTargetX").isPresent()
                && input.getInt("ActiveWorkTargetY").isPresent()
                && input.getInt("ActiveWorkTargetZ").isPresent()) {
            this.activeWorkTarget = new BlockPos(
                    input.getIntOr("ActiveWorkTargetX", this.blockPosition().getX()),
                    input.getIntOr("ActiveWorkTargetY", this.blockPosition().getY()),
                    input.getIntOr("ActiveWorkTargetZ", this.blockPosition().getZ()));
        }
        this.blacklistedWorkTargetTicks = Math.max(0, input.getIntOr("BlacklistedWorkTargetTicks", 0));
        if (this.blacklistedWorkTargetTicks > 0
                && input.getInt("BlacklistedWorkTargetX").isPresent()
                && input.getInt("BlacklistedWorkTargetY").isPresent()
                && input.getInt("BlacklistedWorkTargetZ").isPresent()) {
            this.blacklistedWorkTarget = new BlockPos(
                    input.getIntOr("BlacklistedWorkTargetX", this.blockPosition().getX()),
                    input.getIntOr("BlacklistedWorkTargetY", this.blockPosition().getY()),
                    input.getIntOr("BlacklistedWorkTargetZ", this.blockPosition().getZ()));
        }
        this.setWorkRadius(input.getIntOr("WorkRadius", DEFAULT_WORK_RADIUS));
        if (input.getInt("WorkTargetX").isPresent()
                && input.getInt("WorkTargetY").isPresent()
                && input.getInt("WorkTargetZ").isPresent()) {
            this.setWorkTarget(new BlockPos(
                    input.getIntOr("WorkTargetX", this.blockPosition().getX()),
                    input.getIntOr("WorkTargetY", this.blockPosition().getY()),
                    input.getIntOr("WorkTargetZ", this.blockPosition().getZ())));
        }
        if (input.getInt("StorageTargetX").isPresent()
                && input.getInt("StorageTargetY").isPresent()
                && input.getInt("StorageTargetZ").isPresent()) {
            this.setStorageTarget(new BlockPos(
                    input.getIntOr("StorageTargetX", this.blockPosition().getX()),
                    input.getIntOr("StorageTargetY", this.blockPosition().getY()),
                    input.getIntOr("StorageTargetZ", this.blockPosition().getZ())));
        }
        if (input.getInt("BaseTargetX").isPresent()
                && input.getInt("BaseTargetY").isPresent()
                && input.getInt("BaseTargetZ").isPresent()) {
            this.setBaseTarget(new BlockPos(
                    input.getIntOr("BaseTargetX", this.blockPosition().getX()),
                    input.getIntOr("BaseTargetY", this.blockPosition().getY()),
                    input.getIntOr("BaseTargetZ", this.blockPosition().getZ())));
        }
        if (input.getInt("MoveTargetX").isPresent()
                && input.getInt("MoveTargetY").isPresent()
                && input.getInt("MoveTargetZ").isPresent()) {
            this.moveTarget = new BlockPos(
                    input.getIntOr("MoveTargetX", this.blockPosition().getX()),
                    input.getIntOr("MoveTargetY", this.blockPosition().getY()),
                    input.getIntOr("MoveTargetZ", this.blockPosition().getZ()));
        }
        this.syncRecruitStatusState();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.pendingNaturalSpawnRemoval) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide()
                && this.pendingNaturalSpawnInitialization
                && this.level() instanceof ServerLevel serverLevel) {
            // Chunk generation serializes mobs after finalizeSpawn, and loaders may cancel a
            // natural spawn before it is added. Initializing only on the first live server tick
            // preserves a valid type id and prevents orphaned authoritative faction state.
            if (!this.initializeNaturalWorldSpawn(serverLevel)) {
                this.discard();
                return;
            }
            this.pendingNaturalSpawnInitialization = false;
        }
        super.tick();
        if (!this.level().isClientSide()
                && !this.factionOutpostSiteGenerationResolved
                && this.factionOutpostId != null
                && this.tickCount % 20 == 1
                && this.level() instanceof ServerLevel serverLevel) {
            // Planet ecology keeps its existing first-resident shelter lifecycle. Overworld
            // blueprint residents mark this resolved during atomic site initialization.
            this.tryGenerateFactionOutpostSite(serverLevel);
        }
        if (!this.level().isClientSide()
                && this.appliedGameplayDataGeneration != GameplayDataManager.generation()) {
            this.applyUnitDefinition();
        }
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.classProgressState = this.classProgressState.regenerate(2);
            this.tickClassAbilities();
            this.migrateLegacyKingdomLink();
            if (this.level() instanceof ServerLevel serverLevel) {
                this.reconcileWorkerAuthority(serverLevel);
                this.reconcileArmyGroupOrder(serverLevel);
                this.tickArmyVitals(serverLevel);
                NpcForceRuntimeService.tick(serverLevel, this);
            }
            this.tickCommanderCampaign();
            this.syncRecruitStatusState();
        }
        if (!this.level().isClientSide() && this.acknowledgeOrderTicks > 0) {
            this.acknowledgeOrderTicks--;
        }
        if (!this.level().isClientSide() && this.tickCount % 100 == 0
                && this.level() instanceof ServerLevel serverLevel) {
            if (this.isNaturalFactionNpc() && this.getNpcRole() == NpcRole.COMMANDER) {
                FactionOutpostSavedData.get(serverLevel).pruneExpired(
                        serverLevel.getGameTime());
            }
            this.syncArmySnapshot(serverLevel);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData finalized = super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.DISPENSER) {
            this.initializeFromSpawnEgg();
        } else if (reason == EntitySpawnReason.CHUNK_GENERATION
                || reason == EntitySpawnReason.NATURAL) {
            // The loader may still cancel the add after finalizeSpawn. Wait until the entity's
            // first live server tick before creating authoritative outpost or planet state.
            this.pendingNaturalSpawnInitialization = true;
        }
        return finalized;
    }

    private boolean initializeNaturalWorldSpawn(ServerLevel level) {
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString();
        if (this.isTame() || this.kingdomId != null || this.settlementId != null) {
            return false;
        }
        if (level.dimension().equals(Level.OVERWORLD)) {
            // Overworld faction residents are created only by initialized blueprint sites.
            return false;
        }

        PlanetFactionSpawnPolicy.Evaluation evaluation = PlanetFactionSpawnPolicy.evaluate(
                GameplayDataManager.snapshot(), level.dimension().identifier().toString(), entityTypeId);
        if (!evaluation.knownPlanetDimension() || !evaluation.allowed()) {
            return false;
        }
        return this.initializeNaturalPlanetNpc(level, evaluation);
    }

    private void tryGenerateFactionOutpostSite(ServerLevel level) {
        UUID outpostId = this.factionOutpostId;
        if (outpostId == null) {
            this.factionOutpostSiteGenerationResolved = true;
            return;
        }
        FactionOutpostSavedData data = FactionOutpostSavedData.get(level);
        FactionOutpostRecord outpost = data.outpost(outpostId).orElse(null);
        if (outpost == null
                || !outpost.dimensionId().equals(level.dimension().identifier().toString())) {
            this.factionOutpostSiteGenerationResolved = true;
            return;
        }
        if (data.siteGenerated(outpostId)) {
            this.synchronizeFactionOutpostHome(outpost);
            this.factionOutpostSiteGenerationResolved = true;
            return;
        }
        var attempt = data.claimSiteGenerationAttempt(outpostId, level.getGameTime()).orElse(null);
        if (attempt == null) {
            return;
        }
        BlockPos generatedCenter = FactionOutpostMarkerService.generateFirstViableLoadedSite(
                level, outpost, attempt).orElse(null);
        if (generatedCenter == null) {
            return;
        }
        FactionOutpostRecord completed = data.completeSiteGeneration(
                outpostId, generatedCenter, level.getGameTime()).orElse(null);
        if (completed != null) {
            this.synchronizeFactionOutpostHome(completed);
            this.factionOutpostSiteGenerationResolved = true;
        }
    }

    private void synchronizeFactionOutpostHome(FactionOutpostRecord outpost) {
        this.setHomeTo(FactionOutpostMarkerService.shelterCenter(outpost), outpost.radius());
    }

    /** Finalizes data-driven stats and prevents a spawn-egg recruit from despawning. */
    public void initializeFromSpawnEgg() {
        if (this.level().isClientSide() || this.spawnEggInitialized) {
            return;
        }
        this.spawnEggInitialized = true;
        this.setPersistenceRequired();
        this.applyUnitDefinition();
        BrainUtil.clearMemories(this, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
        this.setTarget(null);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
        float before = this.getHealth();
        super.actuallyHurt(level, damageSource, damageAmount);
        if (this.getHealth() < before) {
            this.morale = clampVital(this.morale - this.factionMoraleLoss(10));
            if (damageSource.getEntity() instanceof ServerPlayer attacker) {
                FactionReputationService.recordNaturalNpcDamage(level, this, attacker);
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.deathResourcesReleased && this.level() instanceof ServerLevel serverLevel) {
            if (damageSource.getEntity() instanceof ServerPlayer attacker) {
                FactionReputationService.recordNaturalNpcKill(serverLevel, this, attacker);
            }
            if (this.armyGroupId != null) {
                for (GalacticRecruitEntity recruit : serverLevel.getEntitiesOfClass(
                        GalacticRecruitEntity.class,
                        this.getBoundingBox().inflate(32.0D),
                        recruit -> recruit != this && this.armyGroupId.equals(recruit.armyGroupId))) {
                    recruit.morale = clampVital(recruit.morale - recruit.factionMoraleLoss(20));
                }
            }
            this.deathResourcesReleased = true;
            List<ItemStack> carriedItems = this.workerInventory.stream().map(ItemStack::copy).toList();
            this.workerInventory = NonNullList.withSize(9, ItemStack.EMPTY);
            RecruitLifecycleService.dropCarriedItems(serverLevel, this, carriedItems);
            EntityReference<LivingEntity> owner = this.getOwnerReference();
            if (owner != null) {
                UUID ownerId = owner.getUUID();
                Optional<KingdomRecord> kingdom = KingdomSavedData.get(serverLevel).kingdomForOwner(ownerId);
                boolean commander = this.getRecruitDuty() == RecruitDuty.COMMANDER;
                RecruitLifecycleService.releaseSettlementState(
                        serverLevel, ownerId, this.getUUID(), commander, this.armyLocation());
                if (commander) {
                    kingdom.flatMap(record -> this.findCommandCenter(serverLevel, record))
                            .ifPresent(hall -> hall.settlePendingCampaignRefunds(serverLevel));
                }
            }
        }
        super.die(damageSource);
    }

    @Override
    public void onRemoval(Entity.RemovalReason reason) {
        if (reason.shouldDestroy() && this.factionOutpostId != null
                && this.level() instanceof ServerLevel serverLevel) {
            FactionOutpostSavedData.get(serverLevel).removeNpc(this.getUUID(), serverLevel.getGameTime());
            this.factionOutpostId = null;
        }
        super.onRemoval(reason);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        FoodProperties food = held.get(DataComponents.FOOD);
        if (food != null && this.isOwnedBy(player) && this.hunger < 100) {
            if (!this.level().isClientSide()) {
                this.hunger = clampVital(this.hunger + food.nutrition() * 4);
                if (!player.hasInfiniteMaterials()) {
                    held.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.isMerchant() && !player.isShiftKeyDown()) {
                NpcReactionDecision reaction = this.npcReactionTo(player);
                if (!reaction.tradeAllowed()) {
                    this.tryWarnPlayer(serverPlayer);
                    serverPlayer.sendSystemMessage(Component.translatable(
                            reaction.disposition() == NpcDisposition.HOSTILE
                                    ? "message.galacticwars.faction_ai.trade_hostile"
                                    : "message.galacticwars.faction_ai.trade_wary"));
                    if (reaction.shouldRaiseAlert()) {
                        NpcFactionAiService.raiseAlert(this, player, "hostile_approach");
                    }
                    return InteractionResult.SUCCESS_SERVER;
                }
                MenuRegistry.openExtendedMenu(serverPlayer, new MerchantTradeMenuProvider(this));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (this.isNaturalFactionNpc()) {
                NpcReactionDecision reaction = this.npcReactionTo(player);
                if (reaction.disposition() == NpcDisposition.FRIENDLY) {
                    this.acknowledgeOrderTicks = Math.max(this.acknowledgeOrderTicks, 20);
                } else if (reaction.shouldWarn()) {
                    this.tryWarnPlayer(serverPlayer);
                    return InteractionResult.SUCCESS_SERVER;
                } else if (reaction.shouldRaiseAlert()) {
                    NpcFactionAiService.raiseAlert(this, player, "hostile_approach");
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            MenuRegistry.openExtendedMenu(serverPlayer, new RecruitCommandMenuProvider(this));
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    public RecruitmentAction getRecruitCommand() {
        int command = this.entityData.get(DATA_COMMAND);
        RecruitmentAction[] values = RecruitmentAction.values();
        if (command < 0 || command >= values.length) {
            return RecruitmentAction.FOLLOW_OWNER;
        }
        return values[command];
    }

    public RecruitDuty getRecruitDuty() {
        int duty = this.entityData.get(DATA_RECRUIT_DUTY);
        RecruitDuty[] values = RecruitDuty.values();
        return duty < 0 || duty >= values.length ? RecruitDuty.SOLDIER : values[duty];
    }

    public NpcServiceBranch getServiceBranch() {
        return this.serviceBranch;
    }

    public @Nullable UUID getKingdomId() {
        return kingdomId;
    }

    public @Nullable UUID getSettlementId() {
        return settlementId;
    }

    public @Nullable UUID getFactionOutpostId() {
        return factionOutpostId;
    }

    public void initializeNaturalFactionNpc(UUID outpostId, NpcServiceBranch branch) {
        initializeNaturalFactionNpc(
                outpostId,
                branch,
                branch == NpcServiceBranch.MILITARY ? NpcRole.TROOPER : NpcRole.CIVILIAN);
    }

    public void initializeNaturalFactionNpc(
            UUID outpostId,
            NpcServiceBranch branch,
            NpcRole role
    ) {
        if (this.isTame() || this.kingdomId != null || this.settlementId != null) {
            throw new IllegalStateException("owned recruit cannot join a natural faction outpost");
        }
        this.factionOutpostId = Objects.requireNonNull(outpostId, "outpostId");
        this.factionOutpostSiteGenerationResolved = false;
        this.serviceBranch = Objects.requireNonNull(branch, "branch");
        this.setRecruitCommand(RecruitmentAction.HOLD_POSITION);
        applyNaturalNpcRole(Objects.requireNonNull(role, "role"));
    }

    public void initializeNaturalFactionNpc(
            UUID outpostId,
            NpcServiceBranch branch,
            BlockPos outpostCenter,
            int outpostRadius
    ) {
        initializeNaturalFactionNpc(outpostId, branch);
        this.setHomeTo(Objects.requireNonNull(outpostCenter, "outpostCenter"), outpostRadius);
    }

    public void initializeNaturalFactionNpc(
            UUID outpostId,
            NpcServiceBranch branch,
            NpcRole role,
            BlockPos outpostCenter,
            int outpostRadius
    ) {
        initializeNaturalFactionNpc(outpostId, branch, role);
        this.setHomeTo(Objects.requireNonNull(outpostCenter, "outpostCenter"), outpostRadius);
    }

    public void initializeBlueprintSiteResident(
            UUID outpostId,
            NpcServiceBranch branch,
            BlockPos outpostCenter,
            int outpostRadius
    ) {
        initializeNaturalFactionNpc(outpostId, branch, outpostCenter, outpostRadius);
        this.factionOutpostSiteGenerationResolved = true;
    }

    public void initializeBlueprintSiteResident(
            UUID outpostId,
            NpcServiceBranch branch,
            NpcRole role,
            BlockPos outpostCenter,
            int outpostRadius
    ) {
        initializeNaturalFactionNpc(outpostId, branch, role, outpostCenter, outpostRadius);
        this.factionOutpostSiteGenerationResolved = true;
    }

    public boolean isNaturalPlanetNpcInitialized() {
        return this.naturalPlanetNpcInitialized;
    }

    public boolean isPendingNaturalSpawnInitialization() {
        return this.pendingNaturalSpawnInitialization;
    }

    public boolean isPendingNaturalSpawnRemoval() {
        return this.pendingNaturalSpawnRemoval;
    }

    /** Applies the authoritative unit or civilian archetype to a naturally despawning planet NPC. */
    public void initializeNaturalPlanetNpc() {
        if (this.level().isClientSide() || this.naturalPlanetNpcInitialized) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("natural planet NPC initialization requires a server level");
        }
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString();
        PlanetFactionSpawnPolicy.Evaluation evaluation = PlanetFactionSpawnPolicy.evaluate(
                GameplayDataManager.snapshot(), this.level().dimension().identifier().toString(), entityTypeId);
        if (!evaluation.allowed() || !this.initializeNaturalPlanetNpc(serverLevel, evaluation)) {
            throw new IllegalStateException("entity type " + entityTypeId
                    + " could not join a natural faction outpost in "
                    + this.level().dimension().identifier());
        }
    }

    private boolean initializeNaturalPlanetNpc(
            ServerLevel level,
            PlanetFactionSpawnPolicy.Evaluation evaluation
    ) {
        if (this.naturalPlanetNpcInitialized) {
            return this.factionOutpostId != null && this.hasHome();
        }
        if (this.isTame() || this.kingdomId != null || this.settlementId != null
                || this.factionOutpostId != null || evaluation.serviceBranch() == null) {
            return false;
        }
        OverworldFactionSpawnProfile profile = GameplayDataManager.snapshot()
                .overworldSpawnProfiles().get(evaluation.factionId());
        if (profile == null || !profile.factionId().equals(evaluation.factionId())) {
            return false;
        }
        NpcServiceBranch branch = evaluation.serviceBranch();
        FactionOutpostRecord outpost = FactionOutpostSavedData.get(level).assignNaturalNpc(
                this.getUUID(),
                profile,
                branch,
                level.dimension().identifier().toString(),
                this.blockPosition(),
                level.getGameTime()).orElse(null);
        if (outpost == null) {
            return false;
        }
        this.applyUnitDefinition();
        NpcRole role = branch == NpcServiceBranch.MILITARY
                && !outpost.militaryNpcIds().isEmpty()
                && outpost.militaryNpcIds().getFirst().equals(this.getUUID())
                ? NpcRole.COMMANDER
                : branch == NpcServiceBranch.CIVILIAN
                        && !outpost.civilianNpcIds().isEmpty()
                        && outpost.civilianNpcIds().getFirst().equals(this.getUUID())
                        ? NpcRole.TRADER
                        : branch == NpcServiceBranch.MILITARY
                                ? NpcRole.TROOPER : NpcRole.CIVILIAN;
        this.initializeNaturalFactionNpc(
                outpost.id(), branch, role,
                FactionOutpostMarkerService.shelterCenter(outpost), outpost.radius());
        BrainUtil.clearMemories(this, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
        this.setTarget(null);
        this.naturalPlanetNpcInitialized = true;
        return true;
    }

    public @Nullable UUID getArmyGroupId() {
        return this.armyGroupId;
    }

    public boolean hasAuthoritativeArmyGroup() {
        return this.armyGroupId != null;
    }

    public boolean canPlayerCommandArmy(Player player) {
        Objects.requireNonNull(player, "player");
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord recruitKingdom = data.kingdomForRecruit(this.getUUID()).orElse(null);
        if (recruitKingdom == null) {
            return this.isOwnedBy(player);
        }
        KingdomRecord actorKingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        if (actorKingdom == null
                || !recruitKingdom.id().equals(actorKingdom.id())
                || recruitKingdom.npc(this.getUUID()).isEmpty()
                || !actorKingdom.allows(
                        player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        if (this.armyGroupId == null) {
            return true;
        }
        ArmyGroupRecord group = data.armyGroup(this.armyGroupId).orElse(null);
        return group != null
                && group.contains(this.getUUID())
                && group.kingdomId().equals(actorKingdom.id())
                && group.simulation().lifecycleState()
                        == galacticwars.clonewars.army.ArmyGroupLifecycleState.LIVE;
    }

    public String getRecruitFactionId() {
        return this.recruitFactionId();
    }

    public boolean canNaturallyEngage(LivingEntity target) {
        return !this.isTame()
                && this.factionOutpostId != null
                && this.serviceBranch == NpcServiceBranch.MILITARY
                && this.canAttackTarget(target);
    }

    public boolean isHostileFactionRecruit(GalacticRecruitEntity target) {
        return target.getRecruitDuty() != RecruitDuty.WORKER
                && this.factionRelationTo(target) == FactionRelation.ENEMY;
    }

    public FactionRelation factionRelationTo(GalacticRecruitEntity target) {
        Objects.requireNonNull(target, "target");
        FactionId ownFaction = FactionId.of(this.recruitFactionId());
        FactionId targetFaction = FactionId.of(target.recruitFactionId());
        if (!(this.level() instanceof ServerLevel serverLevel) || target.level() != this.level()) {
            return GameplayDataManager.snapshot().factions().relation(ownFaction, targetFaction);
        }
        return KingdomFactionRelations.resolve(
                GameplayDataManager.snapshot().factions(),
                KingdomSavedData.get(serverLevel),
                this.kingdomId,
                ownFaction,
                target.kingdomId,
                targetFaction,
                serverLevel.getGameTime());
    }

    public FactionRelation factionRelationTo(Player player) {
        Objects.requireNonNull(player, "player");
        if (!(this.level() instanceof ServerLevel serverLevel) || player.level() != this.level()) {
            return FactionRelation.NEUTRAL;
        }
        KingdomSavedData kingdoms = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> playerKingdom = kingdoms.kingdomForPlayer(player.getUUID());
        String playerFaction = playerKingdom.map(KingdomRecord::factionId)
                .orElseGet(() -> ProgressionSavedData.get(serverLevel).state(player.getUUID()).factionId());
        if (playerFaction.isBlank()) {
            return FactionRelation.NEUTRAL;
        }
        return KingdomFactionRelations.resolve(
                GameplayDataManager.snapshot().factions(),
                kingdoms,
                this.kingdomId,
                FactionId.of(this.recruitFactionId()),
                playerKingdom.map(KingdomRecord::id).orElse(null),
                FactionId.of(playerFaction),
                serverLevel.getGameTime());
    }

    public boolean canAttackFactionPlayer(Player player) {
        return Config.ALLOW_BLASTER_PVP.getAsBoolean()
                && !player.isSpectator()
                && !player.hasInfiniteMaterials()
                && this.npcReactionTo(player).disposition() == NpcDisposition.HOSTILE;
    }

    public boolean isNaturalFactionNpc() {
        return !this.isTame() && this.factionOutpostId != null;
    }

    public boolean isNaturalFactionCivilian() {
        return this.isNaturalFactionNpc()
                && this.serviceBranch == NpcServiceBranch.CIVILIAN;
    }

    public NpcRole getNpcRole() {
        if (this.getRecruitDuty() == RecruitDuty.COMMANDER) {
            return NpcRole.COMMANDER;
        }
        if (this.isMerchant()) {
            return NpcRole.TRADER;
        }
        return this.serviceBranch == NpcServiceBranch.MILITARY
                ? NpcRole.TROOPER : NpcRole.CIVILIAN;
    }

    public NpcReactionDecision npcReactionTo(Player player) {
        return NpcFactionAiService.decision(this, player);
    }

    public boolean hasActiveOutpostAlert() {
        return this.factionOutpostId != null
                && this.level() instanceof ServerLevel serverLevel
                && !FactionOutpostSavedData.get(serverLevel)
                        .activeAlerts(this.factionOutpostId, serverLevel.getGameTime()).isEmpty();
    }

    public boolean acceptNaturalDefenseTarget(Player player) {
        if (!this.isNaturalFactionNpc()
                || this.serviceBranch != NpcServiceBranch.MILITARY
                || this.hasAuthoritativeArmyGroup()
                || !this.canAttackFactionPlayer(player)) {
            return false;
        }
        BrainUtil.setTargetOfEntity(this, player);
        this.setTarget(player);
        this.setAggressive(true);
        return true;
    }

    public boolean tryWarnPlayer(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        long nextAllowed = factionWarningCooldowns.getOrDefault(player.getUUID(), 0L);
        if (gameTime < nextAllowed) {
            return false;
        }
        boolean wasAbsent = !factionWarningCooldowns.containsKey(player.getUUID());
        if (wasAbsent && factionWarningCooldowns.size() >= 16) {
            factionWarningCooldowns.remove(factionWarningCooldowns.keySet().iterator().next());
        }
        factionWarningCooldowns.put(
                player.getUUID(),
                gameTime + NpcFactionAiService.profile(this).warningCooldownTicks());
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.faction_ai.warning",
                this.getDisplayName()));
        return true;
    }

    public BlockPos naturalWorkstationPosition() {
        BlockPos home = this.hasHome() ? this.getHomePosition() : this.blockPosition();
        return switch (this.getWorkerProfession().orElse(WorkerProfession.BUILDER)) {
            case COOK, FARMER, FISHERMAN, ANIMAL_FARMER -> home.offset(-2, 0, 2);
            case MERCHANT, COURIER -> home.offset(1, 0, 0);
            default -> home.offset(-1, 0, 0);
        };
    }

    public boolean tryProduceNaturalSettlementSupplies() {
        if (!isNaturalFactionCivilian() || !(this.level() instanceof ServerLevel serverLevel)
                || serverLevel.getGameTime() < this.nextNaturalProductionGameTime) {
            return false;
        }
        WorkerProfession profession = this.getWorkerProfession().orElse(null);
        ItemStack output = naturalCivilianOutput(profession);
        if (output.isEmpty()) {
            this.nextNaturalProductionGameTime = serverLevel.getGameTime() + 1200L;
            return false;
        }
        BlockPos storage = this.getHomePosition().offset(1, 0, 0);
        if (!(serverLevel.getBlockEntity(storage) instanceof Container container)
        ) {
            this.nextNaturalProductionGameTime = serverLevel.getGameTime() + 200L;
            return false;
        }
        int originalCount = output.getCount();
        ItemStack remainder = HopperBlockEntity.addItem(null, container, output, null);
        if (remainder.getCount() == originalCount) {
            this.nextNaturalProductionGameTime = serverLevel.getGameTime() + 200L;
            return false;
        }
        container.setChanged();
        this.nextNaturalProductionGameTime = serverLevel.getGameTime()
                + this.factionProductionCooldownTicks(1200);
        return true;
    }

    private void assignNaturalCivilianProfession() {
        CivilianArchetypeDefinition archetype = this.currentCivilianArchetype().orElse(null);
        if (archetype == null) {
            return;
        }
        List<WorkerProfession> eligible = archetype.professions().stream()
                .map(WorkerProfession::byId)
                .flatMap(Optional::stream)
                .toList();
        if (!eligible.isEmpty()) {
            this.setWorkerProfession(eligible.get(Math.floorMod(this.getUUID().hashCode(), eligible.size())));
        }
    }

    private void applyNaturalNpcRole(NpcRole role) {
        switch (role) {
            case COMMANDER -> {
                if (this.serviceBranch != NpcServiceBranch.MILITARY) {
                    throw new IllegalArgumentException("Commander must use the military branch");
                }
                this.entityData.set(DATA_WORKER_PROFESSION, -1);
                this.setRecruitDuty(RecruitDuty.COMMANDER);
            }
            case TROOPER -> {
                if (this.serviceBranch != NpcServiceBranch.MILITARY) {
                    throw new IllegalArgumentException("Trooper must use the military branch");
                }
                this.entityData.set(DATA_WORKER_PROFESSION, -1);
                this.setRecruitDuty(RecruitDuty.SOLDIER);
            }
            case TRADER -> {
                if (this.serviceBranch != NpcServiceBranch.CIVILIAN) {
                    throw new IllegalArgumentException("Trader must use the civilian branch");
                }
                this.setRecruitDuty(RecruitDuty.WORKER);
                this.setWorkerProfession(WorkerProfession.MERCHANT);
            }
            case CIVILIAN -> {
                if (this.serviceBranch != NpcServiceBranch.CIVILIAN) {
                    throw new IllegalArgumentException("Civilian must use the civilian branch");
                }
                this.setRecruitDuty(RecruitDuty.WORKER);
                assignNaturalCivilianProfession();
            }
        }
    }

    private ItemStack naturalCivilianOutput(@Nullable WorkerProfession profession) {
        if (profession == null) {
            return ItemStack.EMPTY;
        }
        return switch (profession) {
            case FARMER -> new ItemStack(Items.WHEAT, 2);
            case LUMBERJACK -> this.recruitFactionId().equals("galacticwars:nightsister")
                    ? new ItemStack(galacticwars.clonewars.registry.ModItems.NIGHTSISTER_WEAVE_LOG.get())
                    : new ItemStack(Items.OAK_LOG);
            case FISHERMAN -> new ItemStack(Items.COD);
            case ANIMAL_FARMER -> new ItemStack(Items.LEATHER);
            case MINER -> new ItemStack(Items.RAW_IRON);
            case BUILDER -> new ItemStack(galacticwars.clonewars.registry.ModItems.DURACRETE.get());
            case COOK -> new ItemStack(Items.BREAD);
            case MERCHANT, COURIER, TECHNICIAN -> ItemStack.EMPTY;
        };
    }

    public boolean canNaturallyEngagePlayer(Player player) {
        if (this.isTame() || this.factionOutpostId == null
                || this.serviceBranch != NpcServiceBranch.MILITARY
                || !(this.level() instanceof ServerLevel)) {
            return false;
        }
        return this.canAttackFactionPlayer(player);
    }

    public RecruitVitals getRecruitVitals() {
        return new RecruitVitals(
                Math.max(0, Math.round(this.getHealth())),
                Math.max(1, Math.round(this.getMaxHealth())),
                this.morale,
                this.hunger,
                this.unpaidTicks);
    }

    public ClassProgressState classProgressState() {
        return this.classProgressState;
    }

    public int npcForceEnergy() {
        return this.npcForceEnergy;
    }

    public int npcForceLoadoutCursor() {
        return this.npcForceLoadoutCursor;
    }

    public long npcForceCooldownEnd(int slot) {
        return slot >= 0 && slot < this.npcForceCooldownEnds.length
                ? this.npcForceCooldownEnds[slot] : Long.MAX_VALUE;
    }

    public void regenerateNpcForceEnergy(int amount) {
        if (amount > 0) this.npcForceEnergy = Math.min(100, this.npcForceEnergy + amount);
    }

    public boolean commitNpcForceCast(
            int slot, int energyCost, long cooldownEnd, int loadoutSize
    ) {
        if (slot < 0 || slot >= this.npcForceCooldownEnds.length
                || energyCost < 0 || energyCost > this.npcForceEnergy) return false;
        this.npcForceEnergy -= energyCost;
        this.npcForceCooldownEnds[slot] = Math.max(
                this.npcForceCooldownEnds[slot], cooldownEnd);
        this.npcForceLoadoutCursor = Math.floorMod(slot + 1, Math.max(1, loadoutSize));
        return true;
    }

    public Optional<UnitClassDefinition> unitClassDefinition() {
        if (this.classProgressState.classId().isBlank()) {
            return Optional.empty();
        }
        return GameplayDataManager.snapshot().unitClass(this.classProgressState.classId());
    }

    public ClassAbilityRuntimeService.ActivationDecision activateClassAbility(
            String abilityId,
            long gameTime,
            boolean targetPresent,
            double targetDistance,
            boolean targetsPlayer
    ) {
        return activateClassAbility(
                abilityId,
                gameTime,
                targetPresent,
                targetDistance,
                targetsPlayer,
                () -> true);
    }

    public ClassAbilityRuntimeService.ActivationDecision activateClassAbility(
            String abilityId,
            long gameTime,
            boolean targetPresent,
            double targetDistance,
            boolean targetsPlayer,
            BooleanSupplier effect
    ) {
        UnitClassDefinition unitClass = this.unitClassDefinition().orElse(null);
        var ability = GameplayDataManager.snapshot().ability(abilityId).orElse(null);
        if (unitClass == null || ability == null) {
            return ClassAbilityRuntimeService.ActivationDecision.rejected(
                    "unknown_class_ability", this.classProgressState);
        }
        ClassAbilityRuntimeService.ActivationDecision decision = ClassAbilityRuntimeService.activate(
                unitClass,
                ability,
                this.classProgressState,
                gameTime,
                targetPresent,
                targetDistance,
                targetsPlayer,
                Config.ALLOW_CLASS_PVP.getAsBoolean());
        if (!decision.accepted()) {
            return decision;
        }
        if (!Objects.requireNonNull(effect, "effect").getAsBoolean()) {
            return ClassAbilityRuntimeService.ActivationDecision.rejected(
                    "effect_failed", this.classProgressState);
        }
        this.classProgressState = decision.state();
        return decision;
    }

    public void grantClassExperience(long amount) {
        this.classProgressState = this.classProgressState.gainExperience(amount);
    }

    public Optional<WorkerAssignment> getWorkerAssignment() {
        if (this.workTarget == null || this.getWorkerProfession().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new WorkerAssignment(
                this.getWorkerProfession().orElseThrow(),
                this.level().dimension().identifier().toString(),
                this.workTarget.getX(),
                this.workTarget.getY(),
                this.workTarget.getZ(),
                this.workRadius,
                Optional.ofNullable(this.workOrderId)));
    }

    public WorkerStatus getWorkerStatus() {
        Optional<WorkerStatus.Target> target = Optional.ofNullable(this.activeWorkTarget)
                .map(pos -> new WorkerStatus.Target(
                        this.level().dimension().identifier().toString(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()));
        return new WorkerStatus(this.workerPhase, this.workerReason, target);
    }

    public int getWorkerCarriedItemCount() {
        return this.workerInventory.stream().mapToInt(ItemStack::getCount).sum();
    }

    public CourierRouteExecutionState courierRouteExecutionState() {
        return this.courierRouteState;
    }

    public int getWorkerStorageItemCount() {
        return this.storageItemCount();
    }

    public boolean resumeWorkFromCommandCenter(ServerPlayer actor) {
        if (!this.canManageWorkerFromCommandCenter(actor) || this.workTarget == null) {
            return false;
        }
        this.moveTarget = this.workTarget;
        this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "return_to_worksite", null);
        return true;
    }

    public boolean recallWorkerToCommandCenter(ServerPlayer actor, BlockPos commandCenterPos) {
        if (!this.canManageWorkerFromCommandCenter(actor)) {
            return false;
        }
        this.pauseWorkerNavigation();
        this.moveTarget = Objects.requireNonNull(commandCenterPos, "commandCenterPos").immutable();
        this.setRecruitCommand(RecruitmentAction.MOVE_TO_POSITION);
        this.transitionWorker(WorkerPhase.PAUSED, "recalled_to_command_center", null);
        return true;
    }

    public boolean pauseWorkerFromCommandCenter(ServerPlayer actor) {
        if (!this.canManageWorkerFromCommandCenter(actor)) {
            return false;
        }
        this.pauseWorkerNavigation();
        this.moveTarget = this.blockPosition();
        this.setRecruitCommand(RecruitmentAction.HOLD_POSITION);
        this.transitionWorker(WorkerPhase.PAUSED, "paused_by_command_center", null);
        return true;
    }

    private boolean canManageWorkerFromCommandCenter(ServerPlayer actor) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || actor.level() != serverLevel
                || !this.isAlive()
                || !this.isTame()
                || this.getRecruitDuty() != RecruitDuty.WORKER
                || this.getWorkerProfession().isEmpty()
                || this.kingdomId == null) {
            return false;
        }
        KingdomRecord kingdom = KingdomSavedData.get(serverLevel)
                .kingdomForPlayer(actor.getUUID()).orElse(null);
        return kingdom != null
                && kingdom.id().equals(this.kingdomId)
                && kingdom.npc(this.getUUID()).isPresent()
                && kingdom.allows(actor.getUUID(),
                galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES);
    }

    public @Nullable BlockPos getMoveTarget() {
        return this.moveTarget;
    }

    public @Nullable BlockPos getWorkTarget() {
        return this.workTarget;
    }

    public @Nullable BlockPos getStorageTarget() {
        return this.storageTarget;
    }

    public @Nullable BlockPos getBaseTarget() {
        return this.baseTarget;
    }

    public List<Component> recruitStatusLines() {
        ArrayList<Component> lines = new ArrayList<>();
        BlockPos displayedWorkTarget = this.displayedTarget(DATA_WORK_TARGET, this.workTarget);
        BlockPos displayedStorageTarget = this.displayedTarget(DATA_STORAGE_TARGET, this.storageTarget);
        BlockPos displayedBaseTarget = this.displayedTarget(DATA_BASE_TARGET, this.baseTarget);
        int displayedStorageCount = this.level().isClientSide()
                ? this.entityData.get(DATA_STORAGE_RESOURCE_COUNT)
                : this.storageItemCount();
        int displayedBaseProgress = this.level().isClientSide()
                ? this.entityData.get(DATA_BASE_PROGRESS)
                : this.starterBaseCompletedBlocks;
        int displayedWorkRadius = this.level().isClientSide()
                ? this.entityData.get(DATA_WORK_RADIUS)
                : this.workRadius;
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.command",
                Component.literal(this.getRecruitCommand().name().toLowerCase())));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.duty",
                Component.literal(this.getRecruitDuty().id())));
        this.unitClassDefinition().ifPresent(unitClass -> lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.class",
                Component.literal(unitClass.displayName()),
                this.classProgressState.rank())));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.profession",
                this.getWorkerProfession()
                        .map(profession -> Component.translatable(profession.translationKey()))
                        .orElseGet(() -> Component.translatable("screen.galacticwars.recruit.status.none"))));
        this.displayedResourceStatus().ifPresent(resourceStatus -> lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.resource_action",
                Component.translatable("screen.galacticwars.recruit.workaction."
                        + resourceStatus.action()),
                resourceStatus.itemId().isBlank() ? Component.literal("-") : Component.literal(resourceStatus.itemId()))));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.worksite",
                targetLabel(displayedWorkTarget)));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.work_radius",
                displayedWorkRadius));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.storage",
                targetLabel(displayedStorageTarget),
                displayedStorageCount));
        lines.add(Component.translatable(
                "screen.galacticwars.recruit.status.blueprint",
                Component.literal(this.selectedBlueprintDisplayName())));
        if (displayedBaseTarget != null) {
            lines.add(Component.translatable(
                    "screen.galacticwars.recruit.status.base_progress",
                    displayedBaseProgress,
                    this.selectedBlueprintPlacementCount()));
            if (!this.level().isClientSide()) {
                this.planKingdomWorkOrder().ifPresent(order -> lines.add(Component.translatable(
                        "screen.galacticwars.recruit.status.kingdom_order",
                        Component.translatable("screen.galacticwars.recruit.kingdom_order."
                                + order.type().name().toLowerCase()),
                        order.profession() == null
                                ? Component.translatable("screen.galacticwars.recruit.status.none")
                                : Component.translatable(order.profession().translationKey()),
                        order.itemId().isBlank() ? Component.literal("-") : Component.literal(order.itemId()))));
            }
        }
        return List.copyOf(lines);
    }

    public RecruitStatusSnapshot recruitStatusSnapshot() {
        return new RecruitStatusSnapshot(this.recruitStatusLines());
    }

    public boolean handleMenuButton(ServerPlayer player, int buttonId) {
        Optional<RecruitCommandAction> requestedAction = RecruitCommandAction.fromButtonId(buttonId);
        if (player.level() != this.level()
                || !this.isAlive()
                || player.distanceToSqr(this) > 64.0
                || requestedAction.isEmpty()) {
            return false;
        }
        RecruitCommandAction action = requestedAction.orElseThrow();
        if (action == RecruitCommandAction.HIRE) {
            return this.tryHire(player);
        }
        Optional<WorkerProfession> profession = RecruitCommandAction.workerProfession(buttonId);
        boolean authorized = isArmyCommandAction(action)
                ? this.canPlayerCommandArmy(player)
                : isWorkerManagementAction(action)
                        ? this.canPlayerManageWorksites(player)
                        : action == RecruitCommandAction.OPEN_LOADOUT
                                ? this.canPlayerManageLogistics(player)
                                : isRecruitManagementAction(action)
                                        ? this.canPlayerRecruit(player)
                                        : this.isOwnedBy(player) && !this.hasKingdomAuthority();
        if (!authorized) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.not_owner"));
            return false;
        }
        if (profession.isPresent()) {
            return this.tryAssignWorkerProfession(player, profession.get());
        }

        return switch (action) {
            case PROMOTE_COMMANDER -> this.tryPromoteCommander(player);
            case TOGGLE_AUTO_RECRUITMENT -> this.tryToggleAutomaticRecruitment(player);
            case START_RECRUITMENT -> this.tryStartCommanderCampaign(player);
            case CYCLE_FORMATION -> this.cycleArmyFormation(player);
            case PATROL -> this.startArmyPatrol(player);
            case ROTATE_BLUEPRINT -> this.rotateSelectedBlueprint(player);
            case NEXT_BLUEPRINT -> this.cycleSelectedBlueprint(player);
            case RETURN_TO_SOLDIER -> this.tryReturnToSoldier(player);
            case CANCEL_BUILD -> this.tryCancelBuilding(player);
            case OPEN_LOADOUT -> {
                if (!this.canPlayerManageLogistics(player)) {
                    yield false;
                }
                MenuRegistry.openExtendedMenu(player, new RecruitLoadoutMenuProvider(this));
                yield true;
            }
            case FOLLOW -> {
                if (!this.applyMenuArmyOrder(player, RecruitmentAction.FOLLOW_OWNER, null)) {
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.follow"));
                yield true;
            }
            case HOLD -> {
                if (!this.applyMenuArmyOrder(player, RecruitmentAction.HOLD_POSITION, null)) {
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.hold"));
                yield true;
            }
            case MOVE -> {
                BlockPos moveTarget = CommandTargetSelection.blockFromInventory(player)
                        .orElse(player.blockPosition());
                if (!this.applyMenuArmyOrder(player, RecruitmentAction.MOVE_TO_POSITION, moveTarget)) {
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.move"));
                yield true;
            }
            case SET_WORKSITE -> {
                if (this.getWorkerProfession().isEmpty()) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.missing_profession"));
                    yield false;
                }
                Optional<BlockPos> targetedWorksite = targetedBlock(player);
                if (targetedWorksite.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.invalid_target"));
                    yield false;
                }
                if (!this.isInsideSettlementClaim(targetedWorksite.get())) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.invalid_target"));
                    yield false;
                }
                Optional<WorksiteRecord> configuredWorksite = this.configureOrValidateWorksite(
                        targetedWorksite.orElseThrow());
                if (configuredWorksite.isEmpty()) {
                    player.sendSystemMessage(Component.translatable(
                            "message.galacticwars.recruit.worksite.invalid_target"));
                    yield false;
                }
                WorksiteRecord worksite = configuredWorksite.orElseThrow();
                this.releaseCurrentWorkOrder(false);
                this.setWorkTarget(new BlockPos(worksite.x(), worksite.y(), worksite.z()));
                this.setWorkRadius(worksite.radius());
                this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "worksite_assigned", null);
                WorkerTaskDecision decision = this.planWorkerTask().orElseThrow();
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.recruit.worksite.set",
                        Component.translatable("screen.galacticwars.recruit.worktask."
                                + decision.taskType().name().toLowerCase())));
                yield true;
            }
            case RETURN_WORKSITE -> {
                if (this.workTarget == null) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.missing"));
                    yield false;
                }
                this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "return_to_worksite", null);
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.return"));
                yield true;
            }
            case CLEAR_WORKSITE -> {
                this.releaseCurrentWorkOrder(false);
                this.setWorkTarget(null);
                this.pauseWorkerNavigation();
                this.transitionWorker(WorkerPhase.BLOCKED, "worksite_missing", null);
                if (this.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE) {
                    this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.worksite.clear"));
                yield true;
            }
            case WORK_RADIUS_DECREASE -> {
                this.adjustAuthoritativeWorkRadius(-WORK_RADIUS_STEP);
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.recruit.worksite.radius",
                        this.workRadius));
                yield true;
            }
            case WORK_RADIUS_INCREASE -> {
                this.adjustAuthoritativeWorkRadius(WORK_RADIUS_STEP);
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.recruit.worksite.radius",
                        this.workRadius));
                yield true;
            }
            case SET_STORAGE -> {
                Optional<BlockPos> targetedStorage = targetedBlock(player);
                if (targetedStorage.isEmpty() || this.findContainer(targetedStorage.get()).isEmpty()) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.storage.invalid"));
                    yield false;
                }
                if (!this.isRegisteredStorageTarget(targetedStorage.orElseThrow())) {
                    player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.storage.invalid"));
                    yield false;
                }
                this.releaseCurrentWorkOrder(false);
                this.setStorageTarget(targetedStorage.get());
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "storage_assigned", null);
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.storage.set"));
                yield true;
            }
            case BUILD_STARTER_KEEP -> {
                KingdomSavedData buildData = KingdomSavedData.get((ServerLevel) this.level());
                KingdomRecord actorKingdom = buildData.kingdomForPlayer(player.getUUID()).orElse(null);
                if (actorKingdom == null) {
                    yield false;
                }
                KingdomBaseBlueprint blueprint = this.selectedBlueprint();
                ItemStack projector = ItemStack.EMPTY;
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack candidate = player.getInventory().getItem(slot);
                    if (candidate.is(ModItems.BLUEPRINT_PROJECTOR.get())) {
                        projector = candidate;
                        break;
                    }
                }
                ConstructionPlan plan = new ConstructionPlan(
                        blueprint.id(), this.buildRotationSteps, this.getUUID(), actorKingdom.id());
                if (projector.isEmpty()) {
                    projector = new ItemStack(ModItems.BLUEPRINT_PROJECTOR.get());
                    projector.set(ModDataComponents.CONSTRUCTION_PLAN.get(), plan);
                    if (!player.getInventory().add(projector)) {
                        player.drop(projector, false);
                    }
                } else {
                    projector.set(ModDataComponents.CONSTRUCTION_PLAN.get(), plan);
                }
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.recruit.projector_prepared",
                        Component.literal(blueprint.displayName()), this.buildRotationSteps * 90));
                yield true;
            }
            case PROTECT -> {
                if (!this.applyMenuArmyOrder(player, RecruitmentAction.PROTECT_OWNER, null)) {
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.protect"));
                yield true;
            }
            case ATTACK -> {
                Optional<LivingEntity> explicitTarget = targetedLivingEntity(player);
                if (explicitTarget.isEmpty()) {
                    player.sendSystemMessage(Component.translatable(
                            "message.galacticwars.recruit.attack.missing_target"));
                    yield false;
                }
                if (!this.applyMenuArmyAttack(player, explicitTarget.orElseThrow())) {
                    player.sendSystemMessage(Component.translatable(
                            "message.galacticwars.recruit.attack.missing_target"));
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.attack"));
                yield true;
            }
            case CLEAR -> {
                if (!this.applyMenuArmyOrder(player, RecruitmentAction.CLEAR_TARGET, null)) {
                    yield false;
                }
                player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.clear"));
                yield true;
            }
            case HIRE, ASSIGN_WORKER_PROFESSION -> false;
        };
    }

    public boolean shouldMoveToCommandTarget() {
        return this.isTame()
                && !this.hasAuthoritativeArmyGroup()
                && this.getRecruitCommand() == RecruitmentAction.MOVE_TO_POSITION
                && this.moveTarget != null
                && this.distanceToMoveTargetSqr() > 4.0;
    }

    public double distanceToMoveTargetSqr() {
        if (this.moveTarget == null) {
            return 0.0;
        }
        return this.distanceToSqr(
                this.moveTarget.getX() + 0.5,
                this.moveTarget.getY(),
                this.moveTarget.getZ() + 0.5);
    }

    public boolean shouldUseCompanionAi() {
        RecruitmentAction command = this.getRecruitCommand();
        return this.isTame()
                && !this.hasAuthoritativeArmyGroup()
                && !this.isOrderedToSit()
                && (command == RecruitmentAction.FOLLOW_OWNER || command == RecruitmentAction.PROTECT_OWNER)
                && this.getRecruitOwner().isPresent();
    }

    private boolean shouldProtectOwnerLocally() {
        return this.isTame()
                && !this.hasAuthoritativeArmyGroup()
                && !this.isOrderedToSit()
                && this.getRecruitCommand() == RecruitmentAction.PROTECT_OWNER
                && this.getRecruitOwner().isPresent();
    }

    public boolean canUseLocalAttackTarget(@Nullable LivingEntity target) {
        if (target == null || this.hasAuthoritativeArmyGroup() || !this.canAttackTarget(target)) {
            return false;
        }
        if (!this.isTame()) {
            return this.canNaturallyEngage(target);
        }
        if (this.getRecruitCommand() == RecruitmentAction.ATTACK_TARGET
                && target == this.getTarget()) {
            return true;
        }
        LivingEntity owner = this.getRecruitOwner().orElse(null);
        return this.shouldProtectOwnerLocally()
                && owner != null
                && target == owner.getLastHurtByMob();
    }

    public @Nullable LivingEntity selectLocalAttackTarget(@Nullable LivingEntity sensedTarget) {
        LivingEntity currentTarget = this.getTarget();
        if (this.canUseLocalAttackTarget(currentTarget)) {
            return currentTarget;
        }
        LivingEntity owner = this.getRecruitOwner().orElse(null);
        LivingEntity ownerAttacker = owner == null ? null : owner.getLastHurtByMob();
        if (this.canUseLocalAttackTarget(ownerAttacker)) {
            return ownerAttacker;
        }
        return this.canUseLocalAttackTarget(sensedTarget) ? sensedTarget : null;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return this.canAttackTarget(target);
    }

    public Optional<LivingEntity> getRecruitOwner() {
        EntityReference<LivingEntity> ownerReference = this.getOwnerReference();
        if (ownerReference == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(EntityReference.getLivingEntity(ownerReference, this.level()));
    }

    public Optional<WorkerProfession> getWorkerProfession() {
        int profession = this.entityData.get(DATA_WORKER_PROFESSION);
        WorkerProfession[] values = WorkerProfession.values();
        if (profession < 0 || profession >= values.length) {
            return Optional.empty();
        }
        return Optional.of(values[profession]);
    }

    private void tickClassAbilities() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        UnitClassDefinition unitClass = this.unitClassDefinition().orElse(null);
        if (unitClass == null) return;
        LivingEntity target = this.getTarget();
        for (var abilityId : unitClass.abilityIds()) {
            var ability = GameplayDataManager.snapshot().ability(abilityId.toString()).orElse(null);
            if (ability == null || !ability.enabled()
                    || !ClassAbilityEffectRegistry.registered(ability.id().toString())) continue;
            if (ability.activation() == galacticwars.clonewars.ability.AbilityActivation.PASSIVE) {
                ClassAbilityEffectRegistry.execute(serverLevel, this, ability, null);
                continue;
            }
            if (!ClassAbilityRuntimeService.shouldEvaluateNpc(ability, this.getUUID().hashCode(),
                    serverLevel.getGameTime())) continue;
            boolean selfAbility = ability.activation()
                    == galacticwars.clonewars.ability.AbilityActivation.SELF;
            LivingEntity executionTarget = selfAbility ? null : target;
            boolean targetPresent = executionTarget != null && executionTarget.isAlive();
            double distance = targetPresent ? this.distanceTo(target) : 0.0D;
            var decision = this.activateClassAbility(
                    ability.id().toString(),
                    serverLevel.getGameTime(),
                    targetPresent,
                    distance,
                    executionTarget instanceof Player,
                    () -> ClassAbilityEffectRegistry.execute(
                            serverLevel, this, ability, executionTarget));
            if (decision.accepted()) {
                break;
            }
        }
    }

    public boolean isMerchant() {
        return this.serviceBranch == NpcServiceBranch.CIVILIAN
                && this.getWorkerProfession().filter(profession -> profession == WorkerProfession.MERCHANT).isPresent();
    }

    public ItemStack getMilitaryMainHandItem() {
        return this.serviceBranch == NpcServiceBranch.MILITARY
                ? this.getMainHandItem()
                : this.inactiveDutyMainHand;
    }

    public ItemStack getWorkerMainHandItem() {
        return this.serviceBranch == NpcServiceBranch.CIVILIAN
                ? this.getMainHandItem()
                : this.inactiveDutyMainHand;
    }

    public boolean isMilitaryDutyActive() {
        return this.serviceBranch == NpcServiceBranch.MILITARY;
    }

    public void setMilitaryMainHandItem(ItemStack stack) {
        this.setDutyMainHand(NpcServiceBranch.MILITARY, stack);
    }

    public void setWorkerMainHandItem(ItemStack stack) {
        this.setDutyMainHand(NpcServiceBranch.CIVILIAN, stack);
    }

    private void setDutyMainHand(NpcServiceBranch branch, ItemStack stack) {
        ItemStack normalized = stack == null ? ItemStack.EMPTY : stack;
        if (this.serviceBranch == branch) {
            this.setItemSlot(EquipmentSlot.MAINHAND, normalized);
        } else {
            this.inactiveDutyMainHand = normalized;
        }
    }

    private void switchDutyBranch(NpcServiceBranch nextBranch) {
        if (this.serviceBranch == nextBranch) {
            return;
        }
        ItemStack previouslyActive = this.getMainHandItem();
        this.setItemSlot(EquipmentSlot.MAINHAND, this.inactiveDutyMainHand);
        this.inactiveDutyMainHand = previouslyActive;
        this.serviceBranch = nextBranch;
    }

    public void setWorkerProfession(WorkerProfession profession) {
        if (this.getWorkerMainHandItem().isEmpty()) {
            this.setWorkerMainHandItem(WorkerDutyLoadoutPolicy.defaultTool(profession));
        }
        this.switchDutyBranch(NpcServiceBranch.CIVILIAN);
        this.entityData.set(DATA_WORKER_PROFESSION, profession.ordinal());
        if (this.getRecruitDuty() != RecruitDuty.COMMANDER) {
            this.setRecruitDuty(RecruitDuty.WORKER);
        }
        if (this.level() instanceof ServerLevel serverLevel && this.getOwnerReference() != null) {
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            data.armyGroupForRecruit(this.getUUID()).ifPresent(group -> data.releaseArmyMember(
                    group.ownerId(), this.getUUID(), false, this.armyLocation()));
            this.armyGroupId = null;
        }
        this.syncRecruitStatusState();
    }

    private void clearWorkerProfession() {
        UUID actorId = this.getOwnerReference() == null
                ? null : this.getOwnerReference().getUUID();
        this.clearWorkerProfession(actorId);
    }

    private void clearWorkerProfession(@Nullable UUID actorId) {
        if (this.level() instanceof ServerLevel serverLevel && actorId != null) {
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            data.releaseWorkerAssignments(actorId, this.getUUID());
            data.setNpcServiceBranch(actorId, this.getUUID(), NpcServiceBranch.MILITARY);
        }
        this.entityData.set(DATA_WORKER_PROFESSION, -1);
        this.switchDutyBranch(NpcServiceBranch.MILITARY);
        this.setRecruitDuty(RecruitDuty.SOLDIER);
        this.workerPhase = WorkerPhase.ACQUIRE_ORDER;
        this.workerReason = "soldier_duty";
        this.workerCooldownTicks = 0;
        this.activeWorkTarget = null;
        this.blacklistedWorkTarget = null;
        this.blacklistedWorkTargetTicks = 0;
        this.workOrderId = null;
        this.setWorkTarget(null);
        this.setStorageTarget(null);
        this.setBaseTarget(null);
        this.starterBaseCompletedBlocks = 0;
        boolean joinedArmy = false;
        if (this.level() instanceof ServerLevel serverLevel && actorId != null) {
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            joinedArmy = data.addRecruitToArmy(actorId, this.getUUID());
            this.armyGroupId = data.armyGroupForRecruit(this.getUUID())
                    .map(ArmyGroupRecord::id)
                    .orElse(null);
        }
        this.setRecruitCommand(joinedArmy
                ? RecruitmentAction.FOLLOW_OWNER : RecruitmentAction.HOLD_POSITION);
        this.syncRecruitStatusState();
    }

    public Optional<WorkerTaskDecision> planWorkerTask() {
        return this.getWorkerProfession()
                .map(profession -> WorkerTaskPlanner.plan(profession, this.createWorksite(profession).orElse(null)));
    }

    public Optional<WorkerResourceDecision> planResourceDecision() {
        return this.getWorkerProfession().map(profession -> {
            if (profession == WorkerProfession.COURIER) {
                return this.planCourierLogistics()
                        .map(WorkerLogisticsDecision::asResourceDecision)
                        .orElseGet(() -> WorkerResourceDecision.idle("missing_courier_route"));
            }
            return WorkerResourcePlanner.plan(
                    profession,
                    this.createWorksite(profession).orElse(null),
                    this.workerInventorySnapshot(),
                    this.storageInventorySnapshot(),
                    128);
        });
    }

    public Optional<WorkerLogisticsDecision> planCourierLogistics() {
        if (this.getWorkerProfession().filter(profession -> profession == WorkerProfession.COURIER).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(WorkerLogisticsPlanner.planAnyAvailableSupply(
                this.createCourierRoute().orElse(null),
                this.workerInventorySnapshot(),
                this.storageInventorySnapshot(),
                128));
    }

    public Optional<KingdomBaseBuildDecision> planStarterBaseBuild() {
        if (this.baseTarget == null) {
            return Optional.empty();
        }
        return Optional.of(KingdomBaseBuildPlanner.planNext(
                this.selectedBlueprint(),
                this.storageInventorySnapshot(),
                this.starterBaseCompletedBlocks));
    }

    public Optional<KingdomWorkOrder> planKingdomWorkOrder() {
        if (this.baseTarget == null) {
            return Optional.empty();
        }
        Map<WorkerProfession, Integer> workers;
        Optional<WorkerProfession> profession = this.getWorkerProfession();
        if (profession.isPresent()) {
            workers = Map.of(profession.get(), 1);
        } else {
            workers = Map.of();
        }
        int population = this.isTame() ? 1 : 0;
        return Optional.of(KingdomSettlementPlanner.planNextWorkOrder(
                new KingdomSettlementState(
                        this.storageInventorySnapshot(),
                        workers,
                        population,
                        Math.max(1, population + 1),
                        this.starterBaseCompletedBlocks,
                        true),
                this.selectedBlueprint()));
    }

    public boolean shouldRunWorkerCycle() {
        return this.isTame()
                && this.getRecruitDuty() == RecruitDuty.WORKER
                && this.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE
                && this.getWorkerProfession().filter(WorkerProfessionCatalog::isEnabled).isPresent()
                && this.workTarget != null
                && this.hasAuthoritativeWorkerAssignment()
                && this.hasPaidSettlementUpkeep();
    }

    private boolean hasAuthoritativeWorkerAssignment() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return false;
        }
        WorkerProfession profession = this.getWorkerProfession().orElse(null);
        if (profession == null) {
            return false;
        }
        if (profession == WorkerProfession.TECHNICIAN) {
            return this.authoritativeResearchCommandCenter(serverLevel) != null;
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        return KingdomSavedData.get(serverLevel)
                .assignedWorksite(this.getOwnerReference().getUUID(), this.getUUID())
                .filter(worksite -> worksite.accepts(profession))
                .filter(worksite -> worksite.dimensionId().equals(dimensionId))
                .isPresent();
    }

    private @Nullable BlockPos authoritativeResearchCommandCenter(ServerLevel serverLevel) {
        if (this.getWorkerProfession().filter(WorkerProfession.TECHNICIAN::equals).isEmpty()) {
            return null;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord kingdom = data.kingdomForRecruit(this.getUUID())
                .filter(candidate -> data.isHallActive(candidate.ownerId()))
                .orElse(null);
        if (kingdom == null
                || !kingdom.settlement().dimensionId()
                        .equals(serverLevel.dimension().identifier().toString())
                || data.technologyStateOrDefault(kingdom.id()).activeProject()
                        .flatMap(project -> project.technicianId())
                        .filter(this.getUUID()::equals)
                        .isEmpty()) {
            return null;
        }
        BlockPos commandCenterPos = new BlockPos(
                kingdom.settlement().hallX(),
                kingdom.settlement().hallY(),
                kingdom.settlement().hallZ());
        if (!serverLevel.isLoaded(commandCenterPos)
                || !(serverLevel.getBlockEntity(commandCenterPos) instanceof CommandCenterBlockEntity hall)
                || !kingdom.ownerId().equals(hall.ownerId())) {
            return null;
        }
        return commandCenterPos;
    }

    private void reconcileWorkerAuthority(ServerLevel serverLevel) {
        if (!this.isTame() || this.getRecruitDuty() != RecruitDuty.WORKER
                || this.getOwnerReference() == null) {
            return;
        }
        WorkerProfession profession = this.getWorkerProfession()
                .filter(WorkerProfessionCatalog::isEnabled)
                .orElse(null);
        if (profession == null) {
            return;
        }
        if (!this.hasPaidSettlementUpkeep()) {
            this.pauseWorkerNavigation();
            if (this.workerPhase != WorkerPhase.PAUSED
                    || !this.workerReason.equals("upkeep_unpaid")) {
                this.transitionWorker(WorkerPhase.PAUSED, "upkeep_unpaid", null);
            }
            return;
        }
        if (this.workerPhase == WorkerPhase.PAUSED
                && this.workerReason.equals("upkeep_unpaid")) {
            this.workerCooldownTicks = 0;
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "upkeep_restored", null);
        }
        if (this.workerReason.equals("threat_retreat")) {
            return;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        if (profession == WorkerProfession.TECHNICIAN) {
            BlockPos commandCenterPos = this.authoritativeResearchCommandCenter(serverLevel);
            if (commandCenterPos == null) {
                if (this.workerReason.equals("researching")
                        || this.workerReason.equals("travel_to_command_center")
                        || this.workerReason.equals("research_assigned")
                        || this.workerReason.startsWith("command_center_")) {
                    this.pauseWorkerNavigation();
                    this.setWorkTarget(null);
                    this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
                    this.workerCooldownTicks = 20;
                    this.transitionWorker(WorkerPhase.BLOCKED, "awaiting_research", null);
                }
                return;
            }
            boolean targetChanged = !commandCenterPos.equals(this.workTarget);
            this.setWorkRadius(8);
            this.setWorkTarget(commandCenterPos);
            this.setStorageTarget(null);
            this.moveTarget = commandCenterPos;
            this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
            if (targetChanged) {
                this.workerCooldownTicks = 0;
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "research_assigned", commandCenterPos);
            }
            return;
        }
        if (profession == WorkerProfession.BUILDER && this.activeBuildProjectId != null) {
            BuildProject linkedProject = this.activeBuildProject().orElse(null);
            if (linkedProject == null
                    || linkedProject.state() == galacticwars.clonewars.kingdom.BuildProjectState.CANCELLED
                    || linkedProject.state() == galacticwars.clonewars.kingdom.BuildProjectState.COMPLETED) {
                this.pauseWorkerNavigation();
                this.releaseCurrentWorkOrder(false);
                this.activeBuildProjectId = null;
                this.setBaseTarget(null);
                this.setWorkTarget(null);
                this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
                this.transitionWorker(WorkerPhase.BLOCKED, "build_cancelled", null);
                return;
            }
        }
        Optional<UUID> preferredProject = profession == WorkerProfession.BUILDER
                ? Optional.ofNullable(this.activeBuildProjectId)
                : Optional.empty();
        WorksiteRecord worksite = data.assignedWorksite(ownerId, this.getUUID())
                .filter(candidate -> candidate.accepts(profession))
                .filter(candidate -> preferredProject.isEmpty()
                        || candidate.sourceProjectId().equals(preferredProject))
                .orElse(null);
        if (worksite == null && data.reserveWorksite(ownerId, this.getUUID(), profession, preferredProject)) {
            worksite = data.assignedWorksite(ownerId, this.getUUID()).orElse(null);
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        if (worksite == null || !worksite.dimensionId().equals(dimensionId)) {
            if (!this.workerReason.equals("worksite_capacity_unavailable")) {
                this.blockWorker("worksite_capacity_unavailable");
            }
            return;
        }

        this.setWorkRadius(worksite.radius());
        BlockPos worksiteCenter = new BlockPos(worksite.x(), worksite.y(), worksite.z());
        List<BlockPos> availableStorage = this.availableRegisteredStorage(serverLevel, data, ownerId);
        if (profession == WorkerProfession.COURIER) {
            WorksiteRecord authoritativeWorksite = worksite;
            BlockPos destination = authoritativeWorksite.type().equals("frontier")
                    && this.findContainer(worksiteCenter).isPresent()
                    ? worksiteCenter
                    : availableStorage.stream()
                            .filter(pos -> Math.abs(pos.getX() - authoritativeWorksite.x())
                                    <= authoritativeWorksite.radius())
                            .filter(pos -> Math.abs(pos.getZ() - authoritativeWorksite.z())
                                    <= authoritativeWorksite.radius())
                            .filter(pos -> Math.abs(pos.getY() - authoritativeWorksite.y()) <= 4)
                            .findFirst().orElse(null);
            BlockPos source = availableStorage.stream()
                    .filter(pos -> !pos.equals(destination))
                    .findFirst().orElse(null);
            this.setStorageTarget(source);
            this.setWorkTarget(destination == null ? worksiteCenter : destination);
        } else {
            this.setWorkTarget(worksiteCenter);
            if (this.storageTarget == null
                    || !this.isRegisteredStorageTarget(this.storageTarget)
                    || this.findContainer(this.storageTarget).isEmpty()) {
                this.setStorageTarget(availableStorage.isEmpty() ? null : availableStorage.getFirst());
            }
        }
        if (profession == WorkerProfession.BUILDER && worksite.sourceProjectId().isPresent()) {
            this.activeBuildProjectId = worksite.sourceProjectId().orElseThrow();
            this.activeBuildProject().ifPresent(project -> {
                BlockPos origin = new BlockPos(project.originX(), project.originY(), project.originZ());
                this.setBaseTarget(origin);
                this.setWorkTarget(origin);
            });
        }
    }

    private List<BlockPos> availableRegisteredStorage(
            ServerLevel serverLevel,
            KingdomSavedData data,
            UUID ownerId
    ) {
        String dimensionId = serverLevel.dimension().identifier().toString();
        return data.registeredStorageEndpoints(ownerId).stream()
                .filter(endpoint -> endpoint.dimensionId().equals(dimensionId))
                .map(endpoint -> new BlockPos(endpoint.x(), endpoint.y(), endpoint.z()))
                .filter(serverLevel::isLoaded)
                .filter(pos -> this.findContainer(pos).isPresent())
                .sorted(java.util.Comparator.comparingDouble(this::distanceToStorageSqr))
                .toList();
    }

    private double distanceToStorageSqr(BlockPos pos) {
        return this.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private Optional<WorksiteRecord> configureOrValidateWorksite(BlockPos target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return Optional.empty();
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        WorksiteRecord assigned = data.assignedWorksite(ownerId, this.getUUID()).orElse(null);
        if (assigned == null) {
            return Optional.empty();
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        if (assigned.type().equals("frontier")) {
            return data.configureAssignedFrontierWorksite(
                    ownerId, this.getUUID(), dimensionId, target, this.worksiteScanRadius());
        }
        boolean inside = assigned.dimensionId().equals(dimensionId)
                && Math.abs(target.getX() - assigned.x()) <= assigned.radius()
                && Math.abs(target.getZ() - assigned.z()) <= assigned.radius()
                && Math.abs(target.getY() - assigned.y()) <= 4;
        return inside ? Optional.of(assigned) : Optional.empty();
    }

    private void adjustAuthoritativeWorkRadius(int delta) {
        int requestedRadius = Math.max(MIN_WORK_RADIUS, Math.min(MAX_WORK_RADIUS, this.workRadius + delta));
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            this.setWorkRadius(requestedRadius);
            return;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        WorksiteRecord assigned = data.assignedWorksite(ownerId, this.getUUID()).orElse(null);
        if (assigned == null || !assigned.type().equals("frontier")) {
            this.setWorkRadius(assigned == null ? requestedRadius : assigned.radius());
            return;
        }
        data.configureAssignedFrontierWorksite(
                        ownerId, this.getUUID(), assigned.dimensionId(),
                        new BlockPos(assigned.x(), assigned.y(), assigned.z()), requestedRadius)
                .ifPresent(updated -> this.setWorkRadius(updated.radius()));
    }

    private boolean isRegisteredStorageTarget(BlockPos target) {
        return this.registeredStorageSlots(target) > 0;
    }

    private int registeredStorageSlots(BlockPos target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return 0;
        }
        return KingdomSavedData.get(serverLevel).registeredStorageEndpoint(
                        this.getOwnerReference().getUUID(),
                        serverLevel.dimension().identifier().toString(),
                        target)
                .map(StorageEndpoint::slots)
                .orElse(0);
    }

    public boolean performWorkerCycle() {
        if (!this.shouldRunWorkerCycle() || this.level().isClientSide()) {
            return false;
        }
        WorkerPhase previousPhase = this.workerPhase;
        String previousReason = this.workerReason;
        this.tickWorkerController();
        return previousPhase != this.workerPhase || !previousReason.equals(this.workerReason);
    }

    public boolean beginTechnologyResearch(BlockPos commandCenterPos) {
        Objects.requireNonNull(commandCenterPos, "commandCenterPos");
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getWorkerProfession().filter(WorkerProfession.TECHNICIAN::equals).isEmpty()
                || !commandCenterPos.equals(this.authoritativeResearchCommandCenter(serverLevel))) {
            return false;
        }
        this.setWorkRadius(8);
        this.setWorkTarget(commandCenterPos);
        this.setStorageTarget(null);
        this.moveTarget = commandCenterPos.immutable();
        this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
        this.workerCooldownTicks = 0;
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "research_assigned", commandCenterPos);
        this.tickTechnologyResearch(serverLevel);
        return true;
    }

    public void stopTechnologyResearch(BlockPos commandCenterPos) {
        Objects.requireNonNull(commandCenterPos, "commandCenterPos");
        if (this.getWorkerProfession().filter(WorkerProfession.TECHNICIAN::equals).isEmpty()
                || !commandCenterPos.equals(this.workTarget)) {
            return;
        }
        this.pauseWorkerNavigation();
        this.setWorkTarget(null);
        this.moveTarget = null;
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        this.workerCooldownTicks = 20;
        this.transitionWorker(WorkerPhase.BLOCKED, "awaiting_research", null);
    }

    public boolean isActivelyResearchingAt(BlockPos commandCenterPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || !this.shouldRunWorkerCycle()
                || !this.workerReason.equals("researching")
                || this.getTarget() != null
                || this.hurtTime > 0
                || !commandCenterPos.equals(this.workTarget)
                || !commandCenterPos.equals(this.authoritativeResearchCommandCenter(serverLevel))) {
            return false;
        }
        return this.distanceToSqr(
                commandCenterPos.getX() + 0.5D,
                commandCenterPos.getY() + 0.5D,
                commandCenterPos.getZ() + 0.5D) <= 64.0D;
    }

    public void tickWorkerController() {
        if (!this.shouldRunWorkerCycle() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.getTarget() != null || this.hurtTime > 0) {
            this.workerCooldownTicks = 40;
            this.transitionWorker(WorkerPhase.COOLDOWN, "combat_interrupted", null);
            this.pauseWorkerNavigation();
            return;
        }
        if (this.getWorkerProfession().filter(WorkerProfession.TECHNICIAN::equals).isPresent()) {
            this.tickTechnologyResearch(serverLevel);
            return;
        }
        if (this.workerPhase == WorkerPhase.FIND_TARGET
                && Math.floorMod(this.getUUID().hashCode(), 4) != Math.floorMod(this.tickCount, 4)) {
            return;
        }
        if (this.blacklistedWorkTargetTicks > 0 && --this.blacklistedWorkTargetTicks == 0) {
            this.blacklistedWorkTarget = null;
        }

        switch (this.workerPhase) {
            case ACQUIRE_ORDER -> this.tickAcquireOrder();
            case FIND_TARGET -> this.tickFindTarget();
            case NAVIGATE_SOURCE -> this.tickWorkerNavigation(WorkerPhase.INTERACT);
            case INTERACT -> this.tickWorkerInteraction(serverLevel);
            case COLLECT -> this.transitionAfterCollection();
            case NAVIGATE_STORAGE -> this.tickWorkerNavigation(WorkerPhase.DEPOSIT);
            case DEPOSIT -> this.tickWorkerDeposit();
            case COOLDOWN, BLOCKED -> {
                if (this.workerCooldownTicks > 0) {
                    this.workerCooldownTicks--;
                } else {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "ready", null);
                }
            }
            case PAUSED -> this.pauseWorkerNavigation();
        }
    }

    public void pauseWorkerNavigation() {
        BrainUtil.clearMemories(this, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
    }

    public boolean shouldUseWorkerSafety(@Nullable LivingEntity threat) {
        return this.isTame()
                && this.getRecruitDuty() == RecruitDuty.WORKER
                && this.getRecruitCommand() == RecruitmentAction.WORK_AT_SITE
                && this.getWorkerProfession().filter(WorkerProfessionCatalog::isEnabled).isPresent()
                && (this.isWorkerSafetyRetreating()
                || threat != null && threat.isAlive() && threat.level() == this.level());
    }

    public boolean isWorkerSafetyRetreating() {
        return this.workerPhase == WorkerPhase.PAUSED && this.workerReason.equals("threat_retreat");
    }

    public void beginWorkerSafetyRetreat(@Nullable LivingEntity threat) {
        this.clearAuthorityMemories();
        this.pauseWorkerNavigation();
        if (!this.isWorkerSafetyRetreating()
                || this.activeWorkTarget == null
                || !this.level().isLoaded(this.activeWorkTarget)) {
            this.transitionWorker(
                    WorkerPhase.PAUSED, "threat_retreat", this.workerSafetyRetreatTarget(threat));
        }
        this.maintainWorkerSafetyRetreat();
    }

    public void maintainWorkerSafetyRetreat() {
        if (!this.isWorkerSafetyRetreating() || this.activeWorkTarget == null) {
            return;
        }
        if (!this.level().isLoaded(this.activeWorkTarget)) {
            this.transitionWorker(
                    WorkerPhase.PAUSED, "threat_retreat", this.workerSafetyRetreatTarget(null));
        }
        if (this.activeWorkTarget == null) {
            return;
        }
        if (this.distanceToSqr(
                this.activeWorkTarget.getX() + 0.5D,
                this.activeWorkTarget.getY(),
                this.activeWorkTarget.getZ() + 0.5D) <= 4.0D) {
            this.pauseWorkerNavigation();
            return;
        }
        BrainUtil.setMemory(this, MemoryModuleType.WALK_TARGET,
                new WalkTarget(this.activeWorkTarget, 1.15F, 2));
    }

    public void resumeWorkerAfterSafety() {
        if (!this.isWorkerSafetyRetreating()) {
            return;
        }
        BrainUtil.clearMemories(
                this,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.PATH);
        this.setTarget(null);
        this.setAggressive(false);
        this.workerNavigationFailures = 0;
        this.workerCooldownTicks = 0;
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "threat_cleared", null);
    }

    private BlockPos workerSafetyRetreatTarget(@Nullable LivingEntity threat) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return this.blockPosition();
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord kingdom = data.kingdomForRecruit(this.getUUID()).orElse(null);
        if (kingdom != null) {
            CommandCenterBlockEntity hall = this.findCommandCenter(serverLevel, kingdom).orElse(null);
            if (hall != null) {
                return hall.getBlockPos().immutable();
            }
            String dimensionId = serverLevel.dimension().identifier().toString();
            BlockPos storage = data.registeredStorageEndpoints(kingdom.ownerId()).stream()
                    .filter(endpoint -> endpoint.dimensionId().equals(dimensionId))
                    .map(endpoint -> new BlockPos(endpoint.x(), endpoint.y(), endpoint.z()))
                    .filter(serverLevel::isLoaded)
                    .filter(pos -> this.findContainer(pos).isPresent())
                    .min(java.util.Comparator.comparingDouble(pos -> this.distanceToSqr(
                            pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D)))
                    .orElse(null);
            if (storage != null) {
                return storage;
            }
        }
        Vec3 away = threat == null
                ? new Vec3((this.getUUID().hashCode() & 1) == 0 ? 1.0D : -1.0D, 0.0D, 1.0D)
                : this.position().subtract(threat.position());
        away = new Vec3(away.x(), 0.0D, away.z());
        if (away.lengthSqr() < 1.0E-4D) {
            away = new Vec3(1.0D, 0.0D, 0.0D);
        }
        away = away.normalize();
        for (int distance : new int[] {12, 8, 4}) {
            BlockPos candidate = BlockPos.containing(this.position().add(away.scale(distance)));
            if (serverLevel.isLoaded(candidate)) {
                return candidate;
            }
        }
        return this.blockPosition();
    }

    private void tickTechnologyResearch(ServerLevel serverLevel) {
        BlockPos commandCenterPos = this.authoritativeResearchCommandCenter(serverLevel);
        if (commandCenterPos == null || !commandCenterPos.equals(this.workTarget)) {
            if (this.workTarget != null) {
                this.stopTechnologyResearch(this.workTarget);
            }
            return;
        }
        if (!serverLevel.isLoaded(commandCenterPos)
                || !(serverLevel.getBlockEntity(commandCenterPos) instanceof CommandCenterBlockEntity)) {
            this.workerCooldownTicks = 20;
            this.pauseWorkerNavigation();
            this.transitionWorker(WorkerPhase.BLOCKED, "command_center_unavailable", commandCenterPos);
            return;
        }
        double distance = this.distanceToSqr(
                commandCenterPos.getX() + 0.5D,
                commandCenterPos.getY() + 0.5D,
                commandCenterPos.getZ() + 0.5D);
        if (distance <= 64.0D) {
            this.workerNavigationFailures = 0;
            this.pauseWorkerNavigation();
            if (this.workerPhase != WorkerPhase.COOLDOWN
                    || !this.workerReason.equals("researching")
                    || !commandCenterPos.equals(this.activeWorkTarget)) {
                this.transitionWorker(WorkerPhase.COOLDOWN, "researching", commandCenterPos);
            }
            return;
        }
        if (this.workerPhase != WorkerPhase.NAVIGATE_SOURCE
                || !this.workerReason.equals("travel_to_command_center")
                || !commandCenterPos.equals(this.activeWorkTarget)) {
            this.transitionWorker(
                    WorkerPhase.NAVIGATE_SOURCE, "travel_to_command_center", commandCenterPos);
        }
        if (!this.publishWorkerWalkTarget(commandCenterPos, 1.0F, 2)) {
            this.workerCooldownTicks = 100;
            this.pauseWorkerNavigation();
            this.transitionWorker(
                    WorkerPhase.BLOCKED, "command_center_unreachable", commandCenterPos);
        }
    }

    private void tickAcquireOrder() {
        WorkerProfession profession = this.getWorkerProfession().orElseThrow();
        if (!this.ensureActiveWorkOrder(profession)) {
            this.blockWorker("work_order_persistence_failed");
            return;
        }
        if (!this.workerInventoryIsEmpty()
                && profession != WorkerProfession.BUILDER
                && profession != WorkerProfession.COURIER
                && profession != WorkerProfession.ANIMAL_FARMER
                && profession != WorkerProfession.COOK) {
            if (this.storageTarget == null
                    || !this.isRegisteredStorageTarget(this.storageTarget)
                    || this.findContainer(this.storageTarget).isEmpty()) {
                this.blockWorker("storage_container_required");
                return;
            }
            this.transitionWorker(WorkerPhase.NAVIGATE_STORAGE, "deposit_inventory", this.storageTarget);
            return;
        }

        switch (profession) {
            case FARMER, FISHERMAN, MINER ->
                    this.transitionWorker(WorkerPhase.FIND_TARGET, "scan_worksite", null);
            case LUMBERJACK -> {
                if (!this.workerInventoryContains(Items.OAK_SAPLING)) {
                    if (this.storageTarget == null || !this.containerContains(this.storageTarget, Items.OAK_SAPLING)) {
                        this.blockWorker("matching_sapling_required");
                    } else {
                        this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "withdraw_sapling", this.storageTarget);
                    }
                } else {
                    this.transitionWorker(WorkerPhase.FIND_TARGET, "scan_worksite", null);
                }
            }
            case ANIMAL_FARMER -> this.acquireAnimalFarmerOrder();
            case BUILDER -> this.acquireBuilderOrder();
            case COOK -> this.acquireCookOrder();
            case MERCHANT -> {
                this.workerCooldownTicks = 100;
                this.transitionWorker(WorkerPhase.COOLDOWN, "market_open", this.workTarget);
            }
            case COURIER -> this.acquireCourierOrder();
        }
    }

    private boolean ensureActiveWorkOrder(WorkerProfession profession) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || this.workTarget == null) {
            return false;
        }
        Optional<WorkOrderType> typeOptional = WorkOrderType.forProfession(profession);
        if (typeOptional.isEmpty()) {
            return false;
        }
        WorkOrderType type = typeOptional.orElseThrow();
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        WorksiteRecord worksite = data.assignedWorksite(ownerId, this.getUUID())
                .filter(candidate -> candidate.accepts(profession))
                .orElse(null);
        if (worksite == null) {
            return false;
        }

        Optional<UUID> projectId = Optional.empty();
        BlockPos orderTarget = this.workTarget;
        String resourceId = "";
        int quantity = 1;
        int completedQuantity = 0;
        if (profession == WorkerProfession.BUILDER) {
            BuildProject project = this.activeBuildProject().orElse(null);
            KingdomBaseBlueprint blueprint = project == null
                    ? null
                    : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
            if (project == null) {
                return false;
            }
            if (blueprint == null) {
                this.persistBlockedBuildProject(project, "blueprint_definition_missing");
                return false;
            }
            if (!blueprint.matchesDefinitionHash(project.definitionHash())) {
                this.persistBlockedBuildProject(project, "blueprint_definition_changed");
                return false;
            }
            if (worksite.sourceProjectId().filter(project.id()::equals).isEmpty()) {
                return false;
            }
            projectId = Optional.of(project.id());
            orderTarget = new BlockPos(project.originX(), project.originY(), project.originZ());
            quantity = blueprint.placements().size();
            completedQuantity = project.completedPlacements().size();
            if (completedQuantity >= quantity) {
                return this.workOrderId == null
                        || this.syncBuilderWorkOrderProgress(completedQuantity);
            }
            resourceId = blueprint.rotatedPlacement(completedQuantity, project.rotationSteps()).itemId();
        }

        WorkOrder current = this.currentWorkOrder(data, ownerId).orElse(null);
        if (current == null) {
            WorkOrder assigned = data.assignedWorkOrder(ownerId, this.getUUID()).orElse(null);
            if (assigned != null) {
                this.workOrderId = assigned.id();
                current = assigned;
            }
        }
        if (current != null) {
            boolean sameAuthority = current.assignedRecruitId().filter(this.getUUID()::equals).isPresent()
                    && current.type() == type
                    && current.worksiteId().filter(worksite.id()::equals).isPresent()
                    && current.projectId().equals(projectId)
                    && current.dimensionId().equals(serverLevel.dimension().identifier().toString())
                    && current.targetX() == orderTarget.getX()
                    && current.targetY() == orderTarget.getY()
                    && current.targetZ() == orderTarget.getZ();
            if (!sameAuthority || current.state() == WorkOrderState.CANCELLED) {
                this.releaseCurrentWorkOrder(false);
                current = null;
            } else if (current.state() == WorkOrderState.COMPLETED) {
                this.workOrderId = null;
                current = null;
            } else {
                if (current.state() == WorkOrderState.BLOCKED) {
                    current = data.resumeWorkOrder(
                                    ownerId, current.id(), this.getUUID(), current.revision())
                            .orElse(null);
                    if (current == null) {
                        return false;
                    }
                }
                if (profession == WorkerProfession.BUILDER
                        && !this.syncBuilderWorkOrderProgress(completedQuantity)) {
                    return false;
                }
                return true;
            }
        }

        WorkOrder queued = new WorkOrder(
                UUID.randomUUID(), type, Optional.empty(), WorkOrderState.QUEUED,
                Optional.of(worksite.id()), projectId,
                serverLevel.dimension().identifier().toString(),
                orderTarget.getX(), orderTarget.getY(), orderTarget.getZ(),
                resourceId, quantity, completedQuantity, "", 0);
        WorkOrder claimed = data.queueAndClaimWorkOrder(ownerId, this.getUUID(), queued).orElse(null);
        if (claimed == null) {
            return false;
        }
        this.workOrderId = claimed.id();
        return true;
    }

    private Optional<WorkOrder> currentWorkOrder(KingdomSavedData data, UUID ownerId) {
        return this.workOrderId == null ? Optional.empty() : data.workOrder(ownerId, this.workOrderId);
    }

    private boolean progressCurrentWorkOrder(int amount) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || this.workOrderId == null) {
            return false;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        WorkOrder current = data.workOrder(ownerId, this.workOrderId).orElse(null);
        if (current == null || current.state() == WorkOrderState.CANCELLED) {
            return false;
        }
        if (current.state() == WorkOrderState.COMPLETED) {
            this.workOrderId = null;
            return true;
        }
        if (current.state() == WorkOrderState.BLOCKED) {
            current = data.resumeWorkOrder(ownerId, current.id(), this.getUUID(), current.revision())
                    .orElse(null);
            if (current == null) {
                return false;
            }
        }
        boolean courierCompletion = current.type() == WorkOrderType.COURIER
                && (long) current.completedQuantity() + amount >= current.quantity();
        ProgressionSavedData progression = null;
        galacticwars.clonewars.progression.ProgressionState progressionBefore = null;
        galacticwars.clonewars.progression.ProgressionState progressionAfter = null;
        boolean progressionWasStored = false;
        KingdomGameplayAction completionAction = null;
        boolean progressionCommitted = false;
        if (courierCompletion) {
            completionAction = new KingdomGameplayAction(
                    KingdomActionId.of("delivery_complete", current.id()),
                    ownerId, ProgressionEventType.DELIVERY_COMPLETED,
                    "courier/" + current.id(), 1);
            progression = ProgressionSavedData.get(serverLevel);
            progressionBefore = progression.state(ownerId);
            progressionWasStored = progression.hasStoredState(ownerId);
            KingdomGameplayResult evaluation = KingdomGameplayTransactionService.evaluate(
                    progressionBefore, completionAction);
            if (!evaluation.accepted()) {
                return false;
            }
            if (evaluation.changed()) {
                KingdomGameplayResult committed = KingdomGameplayRuntimeService.applyProgression(
                        progression, completionAction);
                if (!committed.accepted() || !committed.changed()) {
                    return false;
                }
                progressionAfter = progression.state(ownerId);
                progressionCommitted = true;
            }
        }
        WorkOrder progressed = data.progressWorkOrder(
                ownerId, current.id(), current.revision(), amount).orElse(null);
        if (progressed == null) {
            if (progressionCommitted) {
                progression.restoreAfterFailedTransaction(
                        ownerId, progressionAfter, progressionBefore, progressionWasStored);
            }
            return false;
        }
        if (progressed.state() == WorkOrderState.COMPLETED) {
            this.grantClassExperience(10L);
            this.workOrderId = null;
            if (courierCompletion) {
                FactionReputationService.record(
                        serverLevel,
                        ownerId,
                        FactionReputationService.deterministicId(
                                "delivery-completed", ownerId, current.id(), 0L),
                        this.recruitFactionId(),
                        FactionReputationEvent.DELIVERY_COMPLETED);
            }
        } else if (progressionCommitted) {
            progression.restoreAfterFailedTransaction(
                    ownerId, progressionAfter, progressionBefore, progressionWasStored);
            return false;
        }
        return true;
    }

    private boolean syncBuilderWorkOrderProgress(int completedPlacements) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || this.workOrderId == null) {
            return false;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        WorkOrder current = data.workOrder(ownerId, this.workOrderId)
                .filter(order -> order.type() == WorkOrderType.BUILD)
                .orElse(null);
        if (current == null || completedPlacements < current.completedQuantity()) {
            return false;
        }
        if (current.state() == WorkOrderState.COMPLETED) {
            this.workOrderId = null;
            return true;
        }
        if (current.state() == WorkOrderState.BLOCKED) {
            current = data.resumeWorkOrder(ownerId, current.id(), this.getUUID(), current.revision())
                    .orElse(null);
            if (current == null) {
                return false;
            }
        }
        int difference = completedPlacements - current.completedQuantity();
        if (difference == 0) {
            return completedPlacements < current.quantity();
        }
        WorkOrder progressed = data.progressWorkOrder(
                ownerId, current.id(), current.revision(), difference).orElse(null);
        if (progressed == null) {
            return false;
        }
        if (progressed.state() == WorkOrderState.COMPLETED) {
            this.grantClassExperience(15L);
            this.workOrderId = null;
        }
        return true;
    }

    private void blockCurrentWorkOrder(String reason) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || this.workOrderId == null) {
            return;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        data.workOrder(ownerId, this.workOrderId)
                .filter(order -> !order.state().terminal() && order.state() != WorkOrderState.BLOCKED)
                .ifPresent(order -> data.blockWorkOrder(
                        ownerId, order.id(), order.revision(), reason));
    }

    private void releaseCurrentWorkOrder(boolean cancel) {
        if (this.level() instanceof ServerLevel serverLevel
                && this.getOwnerReference() != null
                && this.workOrderId != null) {
            UUID ownerId = this.getOwnerReference().getUUID();
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            data.workOrder(ownerId, this.workOrderId).ifPresent(order -> {
                if (cancel) {
                    data.cancelWorkOrder(ownerId, order.id(), order.revision());
                } else {
                    data.releaseWorkOrder(ownerId, order.id(), order.revision());
                }
            });
        }
        this.workOrderId = null;
    }

    private void tickFindTarget() {
        if (this.workTarget == null) {
            this.blockWorker("worksite_missing");
            return;
        }
        WorkerProfession profession = this.getWorkerProfession().orElseThrow();
        int radius = this.worksiteScanRadius();
        int side = radius * 2 + 1;
        int layers = 5;
        int totalPositions = side * side * layers;
        int scanBudget = Math.min(128, totalPositions);

        for (int checked = 0; checked < scanBudget; checked++) {
            int index = this.workerScanCursor;
            this.workerScanCursor = (this.workerScanCursor + 1) % totalPositions;
            int xOffset = index % side - radius;
            int zOffset = index / side % side - radius;
            int yOffset = index / (side * side) - 1;
            BlockPos candidate = this.workTarget.offset(xOffset, yOffset, zOffset);
            if (candidate.equals(this.blacklistedWorkTarget)) {
                continue;
            }
            BlockState state = this.level().getBlockState(candidate);
            if (this.isWorkerTarget(profession, state)) {
                this.workerScanCursor = 0;
                this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "navigate_work_target", candidate.immutable());
                return;
            }
        }

        if (this.workerScanCursor == 0) {
            this.blockWorker("no_valid_target");
        }
    }

    private void tickWorkerNavigation(WorkerPhase arrivalPhase) {
        if (this.activeWorkTarget == null || !this.level().isLoaded(this.activeWorkTarget)) {
            this.blockWorker("target_unloaded");
            return;
        }
        if (this.distanceToSqr(
                this.activeWorkTarget.getX() + 0.5,
                this.activeWorkTarget.getY() + 0.5,
                this.activeWorkTarget.getZ() + 0.5) <= 4.0) {
            this.workerNavigationFailures = 0;
            this.pauseWorkerNavigation();
            this.transitionWorker(arrivalPhase, this.workerReason, this.activeWorkTarget);
            return;
        }
        if (!this.publishWorkerWalkTarget(this.activeWorkTarget, 1.0F, 1)) {
            this.blockWorker("target_unreachable");
        }
    }

    private boolean publishWorkerWalkTarget(BlockPos target, float speed, int closeEnoughDistance) {
        RecruitNavigationResult result =
                BrainUtil.getMemory(this, ArmyBrainMemoryTypes.NAVIGATION_RESULT);
        if (result != null && result.target().equals(target)) {
            if (result.state() == RecruitNavigationResult.State.UNREACHABLE) {
                this.workerNavigationFailures++;
                BrainUtil.clearMemory(this, ArmyBrainMemoryTypes.NAVIGATION_RESULT);
                if (this.workerNavigationFailures >= 3) {
                    return false;
                }
            } else if (result.state() == RecruitNavigationResult.State.MOVING
                    || result.state() == RecruitNavigationResult.State.ARRIVED) {
                this.workerNavigationFailures = 0;
            }
        }
        BrainUtil.setMemory(this, MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, speed, closeEnoughDistance));
        return true;
    }

    private void tickWorkerInteraction(ServerLevel level) {
        if (this.activeWorkTarget == null || this.distanceToSqr(
                this.activeWorkTarget.getX() + 0.5,
                this.activeWorkTarget.getY() + 0.5,
                this.activeWorkTarget.getZ() + 0.5) > 4.0) {
            this.blockWorker("interaction_out_of_reach");
            return;
        }
        switch (this.workerReason) {
            case "withdraw_sapling" -> {
                if (this.withdrawSpecificItem(this.activeWorkTarget, Items.OAK_SAPLING, 1)) {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "sapling_ready", null);
                } else {
                    this.blockWorker("matching_sapling_required");
                }
            }
            case "withdraw_build_material" -> {
                if (this.withdrawCurrentBuildMaterial()) {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "build_material_ready", null);
                } else {
                    this.blockWorker("build_material_missing");
                }
            }
            case "courier_withdraw" -> {
                if (this.withdrawFirstStack(this.activeWorkTarget)) {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "courier_loaded", null);
                } else {
                    this.blockWorker("courier_source_empty");
                }
            }
            case "courier_route_action" -> this.executeCourierRouteAction();
            case "withdraw_animal_feed" -> {
                net.minecraft.world.item.Item feed = this.requiredAnimalFeed();
                if (feed != null && this.withdrawSpecificItem(this.activeWorkTarget, feed, 2)) {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "animal_feed_ready", null);
                } else {
                    this.blockWorker("animal_feed_missing");
                }
            }
            case "withdraw_cooking_ingredient" -> {
                net.minecraft.world.item.Item ingredient = this.missingCookingIngredient();
                if (ingredient != null && this.withdrawSpecificItem(this.activeWorkTarget, ingredient, 1)) {
                    this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "cooking_ingredient_ready", null);
                } else {
                    this.blockWorker("cooking_ingredient_missing");
                }
            }
            case "feed_animals" -> this.feedAnimalPair(level);
            case "harvest_animal" -> this.harvestExcessAnimal(level);
            case "cook_food" -> this.cookStoredFood();
            case "build_place" -> this.placeCurrentBuildBlock();
            case "navigate_work_target" -> this.performGatheringInteraction(level);
            default -> this.blockWorker("unknown_worker_action");
        }
    }

    private void transitionAfterCollection() {
        if (this.workerInventoryIsEmpty()) {
            if (!this.progressCurrentWorkOrder(1)) {
                this.blockWorker("work_order_persistence_failed");
                return;
            }
            this.workerCooldownTicks = this.factionProductionCooldownTicks(20);
            this.transitionWorker(WorkerPhase.COOLDOWN, "work_complete", null);
        } else if (this.storageTarget != null
                && this.isRegisteredStorageTarget(this.storageTarget)
                && this.findContainer(this.storageTarget).isPresent()) {
            this.transitionWorker(WorkerPhase.NAVIGATE_STORAGE, "deposit_inventory", this.storageTarget);
        } else {
            this.blockWorker("storage_container_required");
        }
    }

    private void tickWorkerDeposit() {
        if (this.activeWorkTarget == null
                || !this.isAuthorizedWorkerDepositTarget(this.activeWorkTarget)
                || !this.insertWorkerInventory(this.activeWorkTarget)) {
            this.blockWorker("storage_full_or_missing");
            return;
        }
        if (!this.progressCurrentWorkOrder(1)) {
            this.blockWorker("work_order_persistence_failed");
            return;
        }
        this.workerCooldownTicks = this.factionProductionCooldownTicks(40);
        this.transitionWorker(WorkerPhase.COOLDOWN, "deposit_complete", null);
    }

    private boolean isAuthorizedWorkerDepositTarget(BlockPos target) {
        if (this.isRegisteredStorageTarget(target)) {
            return true;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || this.getWorkerProfession().filter(WorkerProfession.COURIER::equals).isEmpty()
                || this.workTarget == null
                || !this.workTarget.equals(target)
                || !this.isInsideSettlementClaim(target)) {
            return false;
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        return KingdomSavedData.get(serverLevel)
                .assignedWorksite(this.getOwnerReference().getUUID(), this.getUUID())
                .filter(worksite -> worksite.accepts(WorkerProfession.COURIER))
                .filter(worksite -> worksite.dimensionId().equals(dimensionId))
                .filter(worksite -> worksite.x() == target.getX()
                        && worksite.y() == target.getY()
                        && worksite.z() == target.getZ())
                .isPresent();
    }

    private void performGatheringInteraction(ServerLevel level) {
        WorkerProfession profession = this.getWorkerProfession().orElseThrow();
        BlockPos target = this.activeWorkTarget;
        if (target == null || !this.canModifyWorkerTarget(target)) {
            this.blockWorker("target_outside_claim");
            return;
        }
        BlockState state = level.getBlockState(target);
        if (!this.isWorkerTarget(profession, state)) {
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "target_changed", null);
            return;
        }

        if (profession == WorkerProfession.FISHERMAN) {
            NonNullList<ItemStack> nextInventory = this.copyWorkerInventory();
            ItemStack catchStack = new ItemStack(
                    Math.floorMod(this.getUUID().hashCode() + this.tickCount, 4) == 0
                            ? Items.SALMON
                            : Items.COD);
            if (!mergeAll(nextInventory, List.of(catchStack))) {
                this.blockWorker("worker_inventory_full");
                return;
            }
            ItemStack rod = this.getMainHandItem();
            if (!rod.is(Items.FISHING_ROD)) {
                this.blockWorker("fishing_rod_required");
                return;
            }
            rod.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            this.workerInventory = nextInventory;
            this.transitionWorker(WorkerPhase.COLLECT, "fish_caught", null);
            return;
        }

        ItemStack tool = this.getMainHandItem();
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(
                state,
                level,
                target,
                level.getBlockEntity(target),
                this,
                tool));
        NonNullList<ItemStack> nextInventory = this.copyWorkerInventory();

        if (profession == WorkerProfession.FARMER) {
            if (!removeOneFromStacks(drops, Items.WHEAT_SEEDS)
                    && !removeOneFromStacks(nextInventory, Items.WHEAT_SEEDS)) {
                this.blockWorker("replant_seed_missing");
                return;
            }
        } else if (profession == WorkerProfession.LUMBERJACK
                && !removeOneFromStacks(nextInventory, Items.OAK_SAPLING)) {
            this.blockWorker("matching_sapling_required");
            return;
        }

        if (!mergeAll(nextInventory, drops)) {
            this.blockWorker("worker_inventory_full");
            return;
        }
        if (!level.destroyBlock(target, false, this, 16)) {
            this.blockWorker("world_change_rejected");
            return;
        }
        if (profession == WorkerProfession.FARMER && state.getBlock() instanceof CropBlock cropBlock) {
            level.setBlock(target, cropBlock.getStateForAge(0), 3);
        } else if (profession == WorkerProfession.LUMBERJACK) {
            level.setBlock(target, Blocks.OAK_SAPLING.defaultBlockState(), 3);
        }
        this.workerInventory = nextInventory;
        if (!tool.isEmpty()) {
            tool.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
        }
        this.transitionWorker(WorkerPhase.COLLECT, "resource_collected", null);
    }

    private void acquireBuilderOrder() {
        if (this.baseTarget == null) {
            this.blockWorker("base_target_missing");
            return;
        }
        BuildProject project = this.activeBuildProject().orElse(null);
        KingdomBaseBlueprint blueprint = project == null
                ? null
                : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
        if (project == null || blueprint == null) {
            this.blockWorker("blueprint_definition_missing");
            return;
        }
        if (!blueprint.matchesDefinitionHash(project.definitionHash())) {
            this.persistBlockedBuildProject(project, "blueprint_definition_changed");
            this.blockWorker("blueprint_definition_changed");
            return;
        }
        this.starterBaseCompletedBlocks = project.completedPlacements().size();
        List<BaseBlockPlacement> placements = blueprint.placements();
        while (this.starterBaseCompletedBlocks < placements.size()) {
            BaseBlockPlacement placement = blueprint.rotatedPlacement(
                    this.starterBaseCompletedBlocks, project.rotationSteps());
            Optional<Block> block = blockForPlacement(placement.blockId());
            if (block.isEmpty()) {
                this.blockWorker("unknown_blueprint_block");
                return;
            }
            BlockPos placementPos = this.baseTarget.offset(placement.x(), placement.y(), placement.z());
            BlockState state = this.level().getBlockState(placementPos);
            if (state.is(block.get())) {
                if (!this.persistBuildPlacement(project, this.starterBaseCompletedBlocks)) {
                    this.blockWorker("project_persistence_failed");
                    return;
                }
                this.starterBaseCompletedBlocks++;
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "placement_reconciled", null);
                return;
            }
            if (!state.isAir() && !state.canBeReplaced()) {
                this.blockWorker("blueprint_position_blocked");
                return;
            }
            if (this.workerInventoryContains(block.get().asItem())) {
                this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "build_place", placementPos);
                return;
            }
            if (this.storageTarget == null || !this.containerContains(this.storageTarget, block.get().asItem())) {
                this.blockWorker("build_material_missing");
                return;
            }
            this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "withdraw_build_material", this.storageTarget);
            return;
        }
        this.finishCompletedBlueprint();
    }

    private void acquireCourierOrder() {
        Optional<CourierRoutePlan> configuredRoute = this.authoritativeCourierRoute();
        if (configuredRoute.isPresent()) {
            CourierRoutePlan route = configuredRoute.orElseThrow();
            this.courierRouteState = CourierRoutePlanner.reconcile(route, this.courierRouteState);
            CourierWaypoint waypoint = CourierRoutePlanner.currentWaypoint(route, this.courierRouteState);
            if (!waypoint.dimensionId().equals(this.level().dimension().identifier().toString())) {
                this.blockWorker("courier_waypoint_wrong_dimension");
                return;
            }
            BlockPos target = new BlockPos(waypoint.x(), waypoint.y(), waypoint.z());
            if (!this.level().isLoaded(target)) {
                this.blockWorker("courier_waypoint_unloaded");
                return;
            }
            if (CourierRoutePlanner.currentAction(route, this.courierRouteState).isEmpty()) {
                CourierRouteExecutionState previous = this.courierRouteState;
                this.courierRouteState = CourierRoutePlanner.completeEmptyWaypoint(
                        route, this.courierRouteState);
                this.completeCourierRouteCycle(previous, this.courierRouteState);
                this.workerCooldownTicks = 5;
                this.transitionWorker(WorkerPhase.COOLDOWN, "courier_waypoint_complete", null);
                return;
            }
            this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "courier_route_action", target);
            return;
        }
        if (this.storageTarget == null || this.workTarget == null
                || this.storageTarget.equals(this.workTarget)
                || this.findContainer(this.storageTarget).isEmpty()
                || this.findContainer(this.workTarget).isEmpty()) {
            this.blockWorker("courier_route_requires_two_containers");
            return;
        }
        if (this.workerInventoryIsEmpty()) {
            this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "courier_withdraw", this.storageTarget);
        } else {
            this.transitionWorker(WorkerPhase.NAVIGATE_STORAGE, "courier_deliver", this.workTarget);
        }
    }

    private void executeCourierRouteAction() {
        CourierRoutePlan route = this.authoritativeCourierRoute().orElse(null);
        if (route == null || this.activeWorkTarget == null) {
            this.blockWorker("courier_route_missing");
            return;
        }
        this.courierRouteState = CourierRoutePlanner.reconcile(route, this.courierRouteState);
        CourierWaypoint waypoint = CourierRoutePlanner.currentWaypoint(route, this.courierRouteState);
        String dimensionId = this.level().dimension().identifier().toString();
        if (!waypoint.dimensionId().equals(dimensionId)) {
            this.blockWorker("courier_waypoint_wrong_dimension");
            return;
        }
        BlockPos waypointPos = new BlockPos(waypoint.x(), waypoint.y(), waypoint.z());
        if (!waypointPos.equals(this.activeWorkTarget)) {
            this.transitionWorker(
                    WorkerPhase.NAVIGATE_SOURCE, "courier_route_action", waypointPos);
            return;
        }
        CourierTransferAction action = CourierRoutePlanner.currentAction(route, this.courierRouteState)
                .orElse(null);
        if (action == null) {
            CourierRouteExecutionState previous = this.courierRouteState;
            this.courierRouteState = CourierRoutePlanner.completeEmptyWaypoint(
                    route, this.courierRouteState);
            this.completeCourierRouteCycle(previous, this.courierRouteState);
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "courier_waypoint_complete", null);
            return;
        }
        if (action.effectiveType().waitsAtWaypoint()) {
            if (this.courierRouteState.dwellTicksRemaining() == 0) {
                this.courierRouteState = CourierRoutePlanner.startDwell(route, this.courierRouteState);
            }
            CourierRouteExecutionState previous = this.courierRouteState;
            this.courierRouteState = CourierRoutePlanner.elapseDwell(
                    route, this.courierRouteState, 1);
            if (this.courierRouteState.dwellTicksRemaining() == 0) {
                this.completeCourierRouteCycle(previous, this.courierRouteState);
                this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "courier_wait_complete", null);
            }
            return;
        }

        Container container = this.findContainer(waypointPos).orElse(null);
        CourierTransferOutcome outcome = container == null
                ? CourierTransferOutcome.retry()
                : this.executeCourierTransfer(waypointPos, container, action);
        if (!outcome.complete()) {
            this.workerCooldownTicks = 20;
            this.transitionWorker(WorkerPhase.COOLDOWN, "courier_transfer_retry", null);
            return;
        }
        CourierRouteExecutionState previous = this.courierRouteState;
        this.courierRouteState = CourierRoutePlanner.completeCurrentAction(
                route, this.courierRouteState);
        this.completeCourierRouteCycle(previous, this.courierRouteState);
        this.workerCooldownTicks = 20;
        this.transitionWorker(
                WorkerPhase.COOLDOWN,
                outcome.transferred() > 0 ? "courier_transfer_complete" : "courier_target_satisfied",
                null);
    }

    private CourierTransferOutcome executeCourierTransfer(
            BlockPos waypointPos,
            Container container,
            CourierTransferAction action
    ) {
        int authorizedSlots = this.authorizedPhysicalStorageSlots(waypointPos, container);
        if (authorizedSlots <= 0) {
            return CourierTransferOutcome.retry();
        }
        CourierTransferType actionType = action.effectiveType();
        net.minecraft.world.item.Item filteredItem = actionType.hasItemFilter()
                ? resolveItem(action.itemId()) : null;
        if (actionType.hasItemFilter() && filteredItem == null) {
            return CourierTransferOutcome.retry();
        }
        boolean withdraw = actionType.takesFromWaypoint();
        int limit = switch (actionType) {
            case TAKE, PUT -> action.quantity();
            case TAKE_FILL -> Math.max(0, action.quantity() - this.workerInventoryCount(filteredItem));
            case PUT_FILL -> Math.max(0, action.quantity()
                    - countItem(container, filteredItem, authorizedSlots));
            case TAKE_ANY, PUT_ANY, TAKE_ALL, PUT_ALL -> Integer.MAX_VALUE;
            default -> 0;
        };
        if (limit <= 0) {
            int targetQuantity = courierTargetQuantity(actionType, container, filteredItem, authorizedSlots);
            return new CourierTransferOutcome(
                    0, CourierRoutePlanner.transferSatisfied(action, 0, targetQuantity));
        }

        List<ItemStack> sourceStacks = new ArrayList<>();
        if (withdraw) {
            for (int slot = 0; slot < authorizedSlots; slot++) {
                sourceStacks.add(container.getItem(slot).copy());
            }
        } else {
            this.workerInventory.forEach(stack -> sourceStacks.add(stack.copy()));
        }

        if (actionType == CourierTransferType.TAKE || actionType == CourierTransferType.PUT) {
            List<ItemStack> attemptedTemplates = new ArrayList<>();
            for (ItemStack stack : sourceStacks) {
                if (stack.isEmpty()
                        || !stack.is(filteredItem)
                        || attemptedTemplates.stream().anyMatch(candidate ->
                        ItemStack.isSameItemSameComponents(candidate, stack))) {
                    continue;
                }
                attemptedTemplates.add(stack.copyWithCount(1));
                int transferred = this.transferPhysicalQuantity(
                        waypointPos,
                        container,
                        authorizedSlots,
                        stack,
                        action.quantity(),
                        withdraw,
                        LogisticsTransferRequest.Fulfillment.REQUIRE_EXACT);
                if (CourierRoutePlanner.transferSatisfied(action, transferred, 0)) {
                    return new CourierTransferOutcome(transferred, true);
                }
            }
            return CourierTransferOutcome.retry();
        }

        int transferred = 0;
        for (ItemStack stack : sourceStacks) {
            if (stack.isEmpty()
                    || filteredItem != null && !stack.is(filteredItem)
                    || transferred >= limit) {
                continue;
            }
            int requested = limit == Integer.MAX_VALUE
                    ? stack.getCount()
                    : Math.min(stack.getCount(), limit - transferred);
            transferred += this.transferPhysicalQuantity(
                    waypointPos,
                    container,
                    authorizedSlots,
                    stack,
                    requested,
                    withdraw,
                    LogisticsTransferRequest.Fulfillment.ALLOW_PARTIAL);
        }
        int targetQuantity = courierTargetQuantity(actionType, container, filteredItem, authorizedSlots);
        return new CourierTransferOutcome(
                transferred, CourierRoutePlanner.transferSatisfied(action, transferred, targetQuantity));
    }

    private int courierTargetQuantity(
            CourierTransferType actionType,
            Container container,
            @Nullable Item filteredItem,
            int authorizedSlots
    ) {
        return switch (actionType) {
            case TAKE_FILL -> this.workerInventoryCount(filteredItem);
            case PUT_FILL -> countItem(container, filteredItem, authorizedSlots);
            default -> 0;
        };
    }

    private record CourierTransferOutcome(int transferred, boolean complete) {
        private static CourierTransferOutcome retry() {
            return new CourierTransferOutcome(0, false);
        }
    }

    private void completeCourierRouteCycle(
            CourierRouteExecutionState previous,
            CourierRouteExecutionState current
    ) {
        if (previous.waypointCursor() != 0 && current.waypointCursor() == 0) {
            this.progressCurrentWorkOrder(1);
        }
    }

    private Optional<CourierRoutePlan> authoritativeCourierRoute() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return Optional.empty();
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        return KingdomSavedData.get(serverLevel)
                .assignedWorksite(this.getOwnerReference().getUUID(), this.getUUID())
                .filter(worksite -> worksite.accepts(WorkerProfession.COURIER))
                .filter(worksite -> worksite.dimensionId().equals(dimensionId))
                .map(WorksiteRecord::configuration)
                .flatMap(configuration -> configuration.courierRoutePlan());
    }

    private boolean isAuthoritativeCourierWaypoint(BlockPos pos) {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return false;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord kingdom = data.kingdomForOwner(ownerId).orElse(null);
        if (kingdom == null || !data.isHallActive(ownerId)) {
            return false;
        }
        String dimensionId = this.level().dimension().identifier().toString();
        boolean claimedByKingdom = data.claimAt(
                        dimensionId,
                        new net.minecraft.world.level.ChunkPos(pos.getX() >> 4, pos.getZ() >> 4))
                .filter(claim -> claim.kingdomId().equals(kingdom.id()))
                .isPresent();
        return claimedByKingdom && this.authoritativeCourierRoute().stream()
                .flatMap(route -> route.waypoints().stream())
                .anyMatch(waypoint -> waypoint.dimensionId().equals(dimensionId)
                        && waypoint.x() == pos.getX()
                        && waypoint.y() == pos.getY()
                        && waypoint.z() == pos.getZ());
    }

    private static int countItem(
            Container container,
            net.minecraft.world.item.Item item,
            int slotLimit
    ) {
        int count = 0;
        for (int slot = 0; slot < Math.min(slotLimit, container.getContainerSize()); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static @Nullable Item resolveItem(String itemId) {
        try {
            Identifier identifier = Identifier.parse(itemId);
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(identifier);
            return item != null
                    && item != Items.AIR
                    && identifier.equals(BuiltInRegistries.ITEM.getKey(item)) ? item : null;
        } catch (RuntimeException invalidIdentifier) {
            return null;
        }
    }

    private void acquireAnimalFarmerOrder() {
        List<Animal> livestock = this.livestockInWorkArea();
        if (livestock.size() > 12) {
            Animal target = livestock.stream().filter(animal -> !animal.isBaby()).findFirst().orElse(null);
            if (target == null) {
                this.blockWorker("adult_livestock_required");
                return;
            }
            this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "harvest_animal", target.blockPosition());
            return;
        }
        net.minecraft.world.item.Item feed = this.requiredAnimalFeed(livestock);
        if (feed == null) {
            this.blockWorker("breeding_pair_required");
            return;
        }
        if (this.workerInventoryCount(feed) < 2) {
            if (this.storageTarget == null || !this.containerContains(this.storageTarget, feed)) {
                this.blockWorker("animal_feed_missing");
            } else {
                this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "withdraw_animal_feed", this.storageTarget);
            }
            return;
        }
        Animal target = this.breedingPair(livestock, feed).getFirst();
        this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "feed_animals", target.blockPosition());
    }

    private void acquireCookOrder() {
        net.minecraft.world.item.Item rawFood = this.availableRawCookingInput();
        net.minecraft.world.item.Item missing = rawFood == null ? this.availableStoredRawCookingInput() : null;
        if (rawFood == null && missing == null) {
            this.blockWorker("raw_food_missing");
            return;
        }
        if (rawFood == null) {
            this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "withdraw_cooking_ingredient", this.storageTarget);
            return;
        }
        if (!this.workerInventoryContains(Items.COAL)) {
            if (this.storageTarget == null || !this.containerContains(this.storageTarget, Items.COAL)) {
                this.blockWorker("cooking_fuel_missing");
            } else {
                this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "withdraw_cooking_ingredient", this.storageTarget);
            }
            return;
        }
        this.transitionWorker(WorkerPhase.NAVIGATE_SOURCE, "cook_food", this.workTarget);
    }

    private void feedAnimalPair(ServerLevel level) {
        List<Animal> livestock = this.livestockNearRecruit(5.0D);
        net.minecraft.world.item.Item feed = this.requiredAnimalFeed(livestock);
        if (feed == null || this.workerInventoryCount(feed) < 2) {
            this.blockWorker("breeding_pair_required");
            return;
        }
        List<Animal> pair = this.breedingPair(livestock, feed);
        if (pair.isEmpty()) {
            this.blockWorker("breeding_pair_required");
            return;
        }
        if (!removeOneFromStacks(this.workerInventory, feed)
                || !removeOneFromStacks(this.workerInventory, feed)) {
            this.blockWorker("animal_feed_missing");
            return;
        }
        pair.forEach(animal -> animal.setInLove(null));
        this.transitionWorker(WorkerPhase.COLLECT, "animals_fed", null);
    }

    private void harvestExcessAnimal(ServerLevel level) {
        Animal animal = this.livestockNearRecruit(4.0D).stream()
                .filter(candidate -> !candidate.isBaby())
                .findFirst().orElse(null);
        if (animal == null || this.livestockInWorkArea().size() <= 12) {
            this.blockWorker("population_within_limit");
            return;
        }
        List<ItemStack> products = new ArrayList<>();
        EntityType<?> livestockType = animal.getType();
        if (livestockType == net.minecraft.world.entity.EntityTypes.SHEEP) {
            products.add(new ItemStack(Items.MUTTON, 2));
            products.add(new ItemStack(Items.STRING, 2));
        } else if (livestockType == net.minecraft.world.entity.EntityTypes.PIG) {
            products.add(new ItemStack(Items.PORKCHOP, 2));
        } else if (livestockType == net.minecraft.world.entity.EntityTypes.CHICKEN) {
            products.add(new ItemStack(Items.CHICKEN));
            products.add(new ItemStack(Items.FEATHER, 2));
        } else if (livestockType == net.minecraft.world.entity.EntityTypes.COW
                || livestockType == net.minecraft.world.entity.EntityTypes.MOOSHROOM) {
            products.add(new ItemStack(Items.BEEF, 2));
            products.add(new ItemStack(Items.LEATHER));
        } else {
            this.blockWorker("unsupported_livestock_type");
            return;
        }
        NonNullList<ItemStack> nextInventory = this.copyWorkerInventory();
        if (!mergeAll(nextInventory, products)) {
            this.blockWorker("worker_inventory_full");
            return;
        }
        animal.discard();
        this.workerInventory = nextInventory;
        this.transitionWorker(WorkerPhase.COLLECT, "livestock_harvested", null);
    }

    private void cookStoredFood() {
        net.minecraft.world.item.Item raw = this.availableRawCookingInput();
        net.minecraft.world.item.Item cooked = cookedFoodFor(raw);
        if (raw == null || cooked == null || !this.workerInventoryContains(Items.COAL)) {
            this.blockWorker("cooking_ingredient_missing");
            return;
        }
        NonNullList<ItemStack> nextInventory = this.copyWorkerInventory();
        if (!removeOneFromStacks(nextInventory, raw)
                || !removeOneFromStacks(nextInventory, Items.COAL)
                || !mergeAll(nextInventory, List.of(new ItemStack(cooked)))) {
            this.blockWorker("worker_inventory_full");
            return;
        }
        this.workerInventory = nextInventory;
        this.transitionWorker(WorkerPhase.COLLECT, "food_cooked", null);
    }

    private void finishCompletedBlueprint() {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.baseTarget == null
                || this.getOwnerReference() == null) {
            this.blockWorker("kingdom_link_missing");
            return;
        }
        BuildProject project = this.activeBuildProject().orElse(null);
        KingdomBaseBlueprint blueprint = project == null
                ? null
                : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
        if (project == null || blueprint == null || project.completedPlacements().size() < blueprint.placements().size()) {
            this.blockWorker("project_persistence_incomplete");
            return;
        }
        if (this.workOrderId != null
                && !this.syncBuilderWorkOrderProgress(project.completedPlacements().size())) {
            this.blockWorker("work_order_persistence_failed");
            return;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        KingdomGameplayAction completionAction = new KingdomGameplayAction(
                KingdomActionId.of("build_project_complete", project.id()),
                ownerId, ProgressionEventType.BUILDING_COMPLETED,
                KingdomBaseBlueprint.path(blueprint.id()), 1);
        ProgressionSavedData progression = ProgressionSavedData.get(serverLevel);
        var progressionBefore = progression.state(ownerId);
        boolean progressionWasStored = progression.hasStoredState(ownerId);
        KingdomGameplayResult evaluation = KingdomGameplayTransactionService.evaluate(
                progressionBefore, completionAction);
        if (!evaluation.accepted()) {
            this.blockWorker("progression_rejected");
            return;
        }
        var progressionAfter = progressionBefore;
        if (evaluation.changed()) {
            KingdomGameplayResult committed = KingdomGameplayRuntimeService.applyProgression(
                    progression, completionAction);
            if (!committed.accepted() || !committed.changed()) {
                this.blockWorker("progression_commit_failed");
                return;
            }
            progressionAfter = progression.state(ownerId);
        }
        boolean success = KingdomSavedData.get(serverLevel).completeBuildProject(
                ownerId, project, blueprint);
        if (!success) {
            boolean progressionRestored = !evaluation.changed()
                    || progression.restoreAfterFailedTransaction(
                            ownerId, progressionAfter, progressionBefore, progressionWasStored);
            this.blockWorker(progressionRestored
                    ? "kingdom_link_missing" : "progression_reconciliation_required");
            return;
        }
        if (KingdomBaseBlueprint.STARTER_CAMP_ID.equals(blueprint.id())) {
            this.clearWorkerProfession();
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            data.addRecruitToArmy(ownerId, this.getUUID());
            this.armyGroupId = data.armyGroupForRecruit(this.getUUID())
                    .map(ArmyGroupRecord::id).orElse(null);
        } else {
            KingdomSavedData.get(serverLevel).reserveWorksite(
                    this.getOwnerReference().getUUID(), this.getUUID(), WorkerProfession.BUILDER);
        }
        this.workerCooldownTicks = this.factionProductionCooldownTicks(100);
        this.setBaseTarget(null);
        this.setWorkTarget(null);
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        this.activeBuildProjectId = null;
        this.transitionWorker(WorkerPhase.COOLDOWN, "blueprint_complete", null);
    }

    private void placeCurrentBuildBlock() {
        if (this.baseTarget == null || this.activeWorkTarget == null || !this.canModifyWorkerTarget(this.activeWorkTarget)) {
            this.blockWorker("target_outside_claim");
            return;
        }
        BuildProject project = this.activeBuildProject().orElse(null);
        KingdomBaseBlueprint blueprint = project == null
                ? null
                : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
        if (project == null || blueprint == null) {
            this.blockWorker("blueprint_definition_missing");
            return;
        }
        List<BaseBlockPlacement> placements = blueprint.placements();
        if (this.starterBaseCompletedBlocks >= placements.size()) {
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "blueprint_complete", null);
            return;
        }
        BaseBlockPlacement placement = blueprint.rotatedPlacement(
                this.starterBaseCompletedBlocks, project.rotationSteps());
        Optional<Block> block = blockForPlacement(placement.blockId());
        if (block.isEmpty() || !this.workerInventoryContains(block.get().asItem())) {
            this.blockWorker("build_material_missing");
            return;
        }
        BlockPos expected = this.baseTarget.offset(placement.x(), placement.y(), placement.z());
        if (!expected.equals(this.activeWorkTarget)) {
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "project_advanced", null);
            return;
        }
        BlockState current = this.level().getBlockState(expected);
        if (current.is(block.get())) {
            if (!this.persistBuildPlacement(project, this.starterBaseCompletedBlocks)) {
                this.blockWorker("project_persistence_failed");
                return;
            }
            this.starterBaseCompletedBlocks++;
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "placement_reconciled", null);
            return;
        }
        if (!current.isAir() && !current.canBeReplaced()) {
            this.blockWorker("blueprint_position_blocked");
            return;
        }
        if (!this.level().setBlock(expected, safeConstructionState(placement.blockState()), 3)) {
            this.blockWorker("world_change_rejected");
            return;
        }
        removeOneFromStacks(this.workerInventory, block.get().asItem());
        if (!this.persistBuildPlacement(project, this.starterBaseCompletedBlocks)) {
            this.blockWorker("project_persistence_failed");
            return;
        }
        this.starterBaseCompletedBlocks++;
        this.workerCooldownTicks = this.factionProductionCooldownTicks(20);
        this.transitionWorker(WorkerPhase.COOLDOWN, "placement_complete", null);
    }

    /**
     * A builder must be able to leave a newly placed work block safely. Campfires are therefore
     * installed unlit; the player can light the completed camp once no worker is pathing across it.
     */
    private static BlockState safeConstructionState(BlockState state) {
        return state.hasProperty(BlockStateProperties.LIT)
                ? state.setValue(BlockStateProperties.LIT, false)
                : state;
    }

    private boolean withdrawCurrentBuildMaterial() {
        if (this.baseTarget == null || this.activeWorkTarget == null) {
            return false;
        }
        BuildProject project = this.activeBuildProject().orElse(null);
        KingdomBaseBlueprint blueprint = project == null
                ? null
                : GameplayDataManager.snapshot().blueprint(project.blueprintId()).orElse(null);
        if (project == null || blueprint == null) {
            return false;
        }
        List<BaseBlockPlacement> placements = blueprint.placements();
        if (this.starterBaseCompletedBlocks >= placements.size()) {
            return false;
        }
        return blockForPlacement(blueprint.rotatedPlacement(
                        this.starterBaseCompletedBlocks, project.rotationSteps()).blockId())
                .map(block -> this.withdrawSpecificItem(this.activeWorkTarget, block.asItem(), 1))
                .orElse(false);
    }

    private boolean isWorkerTarget(WorkerProfession profession, BlockState state) {
        return switch (profession) {
            case FARMER -> state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state);
            case LUMBERJACK -> state.is(ModBlockTags.WORKER_LOGS);
            case FISHERMAN -> state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource();
            case MINER -> state.is(ModBlockTags.WORKER_MINEABLE);
            default -> false;
        };
    }

    private List<Animal> livestockInWorkArea() {
        return this.livestockNear(this.workTarget, this.worksiteScanRadius());
    }

    private List<Animal> livestockNear(@Nullable BlockPos center, double radius) {
        if (center == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        AABB bounds = new AABB(center).inflate(radius, 4.0D, radius);
        return serverLevel.getEntitiesOfClass(Animal.class, bounds, animal ->
                animal.isAlive() && !(animal instanceof GalacticRecruitEntity));
    }

    private List<Animal> livestockNearRecruit(double radius) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        return serverLevel.getEntitiesOfClass(Animal.class, this.getBoundingBox().inflate(radius), animal ->
                animal.isAlive() && !(animal instanceof GalacticRecruitEntity));
    }

    private net.minecraft.world.item.Item requiredAnimalFeed() {
        return this.requiredAnimalFeed(this.livestockInWorkArea());
    }

    private net.minecraft.world.item.Item requiredAnimalFeed(List<Animal> livestock) {
        for (net.minecraft.world.item.Item feed : List.of(Items.WHEAT, Items.CARROT, Items.WHEAT_SEEDS)) {
            if (!this.breedingPair(livestock, feed).isEmpty()) {
                return feed;
            }
        }
        return null;
    }

    private List<Animal> breedingPair(List<Animal> livestock, net.minecraft.world.item.Item feed) {
        ItemStack feedStack = new ItemStack(feed);
        for (int firstIndex = 0; firstIndex < livestock.size(); firstIndex++) {
            Animal first = livestock.get(firstIndex);
            if (first.isBaby() || !first.canFallInLove() || !first.isFood(feedStack)) {
                continue;
            }
            for (int secondIndex = firstIndex + 1; secondIndex < livestock.size(); secondIndex++) {
                Animal second = livestock.get(secondIndex);
                if (second.getType() == first.getType()
                        && !second.isBaby()
                        && second.canFallInLove()
                        && second.isFood(feedStack)) {
                    return List.of(first, second);
                }
            }
        }
        return List.of();
    }

    private net.minecraft.world.item.Item availableStoredRawCookingInput() {
        if (this.storageTarget == null) {
            return null;
        }
        return rawCookingInputs().stream()
                .filter(item -> this.containerContains(this.storageTarget, item))
                .findFirst().orElse(null);
    }

    private net.minecraft.world.item.Item availableRawCookingInput() {
        return rawCookingInputs().stream()
                .filter(this::workerInventoryContains)
                .findFirst().orElse(null);
    }

    private net.minecraft.world.item.Item missingCookingIngredient() {
        net.minecraft.world.item.Item carriedRaw = this.availableRawCookingInput();
        if (carriedRaw == null) {
            return this.availableStoredRawCookingInput();
        }
        return this.workerInventoryContains(Items.COAL) ? null : Items.COAL;
    }

    private static List<net.minecraft.world.item.Item> rawCookingInputs() {
        return List.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.COD, Items.SALMON, Items.POTATO);
    }

    private static net.minecraft.world.item.Item cookedFoodFor(net.minecraft.world.item.Item raw) {
        if (raw == Items.BEEF) return Items.COOKED_BEEF;
        if (raw == Items.PORKCHOP) return Items.COOKED_PORKCHOP;
        if (raw == Items.CHICKEN) return Items.COOKED_CHICKEN;
        if (raw == Items.COD) return Items.COOKED_COD;
        if (raw == Items.SALMON) return Items.COOKED_SALMON;
        if (raw == Items.POTATO) return Items.BAKED_POTATO;
        return null;
    }

    private boolean canModifyWorkerTarget(BlockPos target) {
        if (this.workTarget == null || !this.level().isLoaded(target)) {
            return false;
        }
        int radius = this.worksiteScanRadius();
        if (Math.abs(target.getX() - this.workTarget.getX()) > radius
                || Math.abs(target.getZ() - this.workTarget.getZ()) > radius
                || Math.abs(target.getY() - this.workTarget.getY()) > 4) {
            return false;
        }
        return this.isInsideSettlementClaim(target);
    }

    private boolean isInsideSettlementClaim(BlockPos target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.level().isLoaded(target)) {
            return false;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        if (!data.isHallActive(owner.getUUID())) {
            return false;
        }
        return data.kingdomForOwner(owner.getUUID())
                .map(KingdomRecord::settlement)
                .filter(settlement -> settlement.dimensionId().equals(serverLevel.dimension().identifier().toString()))
                .filter(settlement -> Math.abs(target.getX() - settlement.hallX()) <= settlement.claimRadius())
                .filter(settlement -> Math.abs(target.getZ() - settlement.hallZ()) <= settlement.claimRadius())
                .isPresent();
    }

    private Optional<Container> findContainer(BlockPos pos) {
        if (!this.level().isLoaded(pos)) {
            return Optional.empty();
        }
        BlockEntity blockEntity = this.level().getBlockEntity(pos);
        return blockEntity instanceof Container container ? Optional.of(container) : Optional.empty();
    }

    private boolean containerContains(BlockPos pos, net.minecraft.world.item.Item item) {
        int authorizedSlots = this.registeredStorageSlots(pos);
        return authorizedSlots > 0 && this.findContainer(pos).stream()
                .flatMap(container -> java.util.stream.IntStream.range(
                                0, Math.min(authorizedSlots, container.getContainerSize()))
                        .mapToObj(container::getItem))
                .anyMatch(stack -> stack.is(item));
    }

    private boolean withdrawSpecificItem(BlockPos pos, net.minecraft.world.item.Item item, int amount) {
        int authorizedSlots = this.registeredStorageSlots(pos);
        if (authorizedSlots <= 0) {
            return false;
        }
        Optional<Container> containerOptional = this.findContainer(pos);
        if (containerOptional.isEmpty()) {
            return false;
        }
        Container container = containerOptional.get();
        int slotLimit = Math.min(authorizedSlots, container.getContainerSize());
        for (int slot = 0; slot < slotLimit; slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.is(item)) {
                continue;
            }
            if (this.transferPhysical(
                    pos,
                    container,
                    authorizedSlots,
                    stored,
                    amount,
                    true,
                    LogisticsTransferRequest.Fulfillment.REQUIRE_EXACT)) {
                return true;
            }
        }
        return false;
    }

    private boolean withdrawFirstStack(BlockPos pos) {
        int authorizedSlots = this.registeredStorageSlots(pos);
        if (authorizedSlots <= 0) {
            return false;
        }
        Optional<Container> containerOptional = this.findContainer(pos);
        if (containerOptional.isEmpty()) {
            return false;
        }
        Container container = containerOptional.get();
        for (int slot = 0; slot < Math.min(authorizedSlots, container.getContainerSize()); slot++) {
            ItemStack stored = container.getItem(slot);
            if (stored.isEmpty()) {
                continue;
            }
            int transfer = Math.min(stored.getCount(), stored.getMaxStackSize());
            if (this.transferPhysical(
                    pos,
                    container,
                    authorizedSlots,
                    stored,
                    transfer,
                    true,
                    LogisticsTransferRequest.Fulfillment.REQUIRE_EXACT)) {
                return true;
            }
        }
        return false;
    }

    private boolean insertWorkerInventory(BlockPos pos) {
        int authorizedSlots = this.registeredStorageSlots(pos);
        Optional<Container> containerOptional = this.findContainer(pos);
        if (containerOptional.isEmpty()
                || authorizedSlots <= 0 && !this.isAuthorizedWorkerDepositTarget(pos)) {
            return false;
        }
        Container container = containerOptional.get();
        if (authorizedSlots <= 0) {
            authorizedSlots = container.getContainerSize();
        }
        int slotLimit = Math.min(authorizedSlots, container.getContainerSize());
        NonNullList<ItemStack> simulated = NonNullList.withSize(slotLimit, ItemStack.EMPTY);
        for (int slot = 0; slot < slotLimit; slot++) {
            simulated.set(slot, container.getItem(slot).copy());
        }
        List<ItemStack> contents = this.workerInventory.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        if (!mergeAll(simulated, contents)) {
            return false;
        }
        for (ItemStack stack : contents) {
            if (!this.transferPhysical(
                    pos,
                    container,
                    authorizedSlots,
                    stack,
                    stack.getCount(),
                    false,
                    LogisticsTransferRequest.Fulfillment.REQUIRE_EXACT)) {
                return false;
            }
        }
        return this.workerInventoryIsEmpty();
    }

    private boolean transferPhysical(
            BlockPos storagePos,
            Container storage,
            int authorizedStorageSlots,
            ItemStack template,
            int quantity,
            boolean withdraw,
            LogisticsTransferRequest.Fulfillment fulfillment
    ) {
        return this.transferPhysicalQuantity(
                storagePos, storage, authorizedStorageSlots, template, quantity, withdraw, fulfillment) > 0;
    }

    private int transferPhysicalQuantity(
            BlockPos storagePos,
            Container storage,
            int authorizedStorageSlots,
            ItemStack template,
            int quantity,
            boolean withdraw,
            LogisticsTransferRequest.Fulfillment fulfillment
    ) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null
                || template.isEmpty()
                || quantity <= 0) {
            return 0;
        }
        int slotLimit = Math.min(authorizedStorageSlots, storage.getContainerSize());
        if (slotLimit <= 0) {
            return 0;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        LogisticsEndpointIdentity storageIdentity = this.storageEndpointIdentity(serverLevel, storagePos);
        LogisticsEndpointIdentity cargoIdentity = new LogisticsEndpointIdentity(
                "recruit:" + this.getUUID() + ":cargo");
        LogisticsAccessPolicy policy = (actorId, endpoint, counterpart, operation, slot, stack) -> {
            if (!actorId.equals(ownerId)) {
                return false;
            }
            if (endpoint.equals(cargoIdentity)) {
                return this.isAlive() && this.level() == serverLevel;
            }
            if (!endpoint.equals(storageIdentity) || !serverLevel.isLoaded(storagePos)) {
                return false;
            }
            int liveSlots = this.authorizedPhysicalStorageSlots(storagePos, storage);
            return Math.min(liveSlots, storage.getContainerSize()) == slotLimit;
        };
        LogisticsEndpoint storageEndpoint = LogisticsEndpoint.container(
                storageIdentity, storage, slotLimit, policy);
        LogisticsEndpoint cargoEndpoint = LogisticsEndpoint.container(
                cargoIdentity, this.createCargoContainer(), ArmyMemberSnapshot.CARGO_SLOT_COUNT, policy);
        LogisticsEndpoint source = withdraw ? storageEndpoint : cargoEndpoint;
        LogisticsEndpoint destination = withdraw ? cargoEndpoint : storageEndpoint;
        PhysicalLogisticsTransaction.Result result = PhysicalLogisticsTransaction.transfer(
                source,
                destination,
                new LogisticsTransferAuthority(
                        ownerId, source.identity(), destination.identity()),
                new LogisticsTransferRequest(template, quantity, fulfillment));
        return result.committed() ? result.transferredQuantity() : 0;
    }

    private int authorizedPhysicalStorageSlots(BlockPos pos, Container container) {
        int registeredSlots = this.registeredStorageSlots(pos);
        if (registeredSlots > 0) {
            return Math.min(registeredSlots, container.getContainerSize());
        }
        return this.isAuthorizedWorkerDepositTarget(pos)
                || this.isAuthoritativeCourierWaypoint(pos)
                ? container.getContainerSize()
                : 0;
    }

    private LogisticsEndpointIdentity storageEndpointIdentity(ServerLevel level, BlockPos pos) {
        return new LogisticsEndpointIdentity("storage:"
                + level.dimension().identifier() + ":"
                + pos.getX() + "," + pos.getY() + "," + pos.getZ());
    }

    private boolean workerInventoryIsEmpty() {
        return this.workerInventory.stream().allMatch(ItemStack::isEmpty);
    }

    private boolean workerInventoryContains(net.minecraft.world.item.Item item) {
        return this.workerInventory.stream().anyMatch(stack -> stack.is(item));
    }

    private int workerInventoryCount(net.minecraft.world.item.Item item) {
        return this.workerInventory.stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private NonNullList<ItemStack> copyWorkerInventory() {
        NonNullList<ItemStack> copy = NonNullList.withSize(this.workerInventory.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < this.workerInventory.size(); slot++) {
            copy.set(slot, this.workerInventory.get(slot).copy());
        }
        return copy;
    }

    public boolean canPlayerManageLogistics(Player player) {
        Objects.requireNonNull(player, "player");
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord recruitKingdom = data.kingdomForRecruit(this.getUUID()).orElse(null);
        if (recruitKingdom == null) {
            return this.isOwnedBy(player);
        }
        KingdomRecord actorKingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        return recruitKingdom != null
                && actorKingdom != null
                && recruitKingdom.id().equals(actorKingdom.id())
                && recruitKingdom.npc(this.getUUID()).isPresent()
                && actorKingdom.allows(
                        player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_LOGISTICS);
    }

    public boolean canPlayerManageWorksites(Player player) {
        Objects.requireNonNull(player, "player");
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord recruitKingdom = data.kingdomForRecruit(this.getUUID()).orElse(null);
        if (recruitKingdom == null) {
            return this.isOwnedBy(player);
        }
        KingdomRecord actorKingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        return actorKingdom != null
                && recruitKingdom.id().equals(actorKingdom.id())
                && recruitKingdom.npc(this.getUUID()).isPresent()
                && actorKingdom.allows(
                        player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES);
    }

    private boolean canPlayerRecruit(Player player) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord recruitKingdom = data.kingdomForRecruit(this.getUUID()).orElse(null);
        KingdomRecord actorKingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        return recruitKingdom != null
                && actorKingdom != null
                && recruitKingdom.id().equals(actorKingdom.id())
                && actorKingdom.allows(
                        player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.RECRUIT);
    }

    private boolean hasKingdomAuthority() {
        return this.level() instanceof ServerLevel serverLevel
                && KingdomSavedData.get(serverLevel).kingdomForRecruit(this.getUUID()).isPresent();
    }

    /**
     * Returns a server-only container view over this recruit's shared cargo slots.
     * The view delegates directly to {@code workerInventory}; it never creates a
     * second inventory that can drift from worker automation or army snapshots.
     */
    public Container createCargoContainer() {
        if (this.level().isClientSide()) {
            throw new IllegalStateException("recruit cargo can only be edited on the server");
        }
        return new RecruitCargoContainer();
    }

    public void markLoadoutChanged() {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.syncArmySnapshot(serverLevel);
        }
    }

    private final class RecruitCargoContainer implements Container {
        @Override
        public int getContainerSize() {
            return ArmyMemberSnapshot.CARGO_SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return GalacticRecruitEntity.this.workerInventoryIsEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return validSlot(slot)
                    ? GalacticRecruitEntity.this.workerInventory.get(slot)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (!validSlot(slot) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack removed = ContainerHelper.removeItem(
                    GalacticRecruitEntity.this.workerInventory, slot, amount);
            if (!removed.isEmpty()) {
                this.setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return validSlot(slot)
                    ? ContainerHelper.takeItem(GalacticRecruitEntity.this.workerInventory, slot)
                    : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (!validSlot(slot)) {
                throw new IndexOutOfBoundsException("cargo slot " + slot);
            }
            ItemStack stored = stack == null || stack.isEmpty()
                    ? ItemStack.EMPTY
                    : stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
            GalacticRecruitEntity.this.workerInventory.set(slot, stored);
            this.setChanged();
        }

        @Override
        public void setChanged() {
            if (GalacticRecruitEntity.this.level() instanceof ServerLevel serverLevel) {
                GalacticRecruitEntity.this.syncArmySnapshot(serverLevel);
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return GalacticRecruitEntity.this.isAlive()
                    && !GalacticRecruitEntity.this.level().isClientSide()
                    && player.level() == GalacticRecruitEntity.this.level()
                    && player.distanceToSqr(GalacticRecruitEntity.this) <= 64.0D;
        }

        @Override
        public void clearContent() {
            GalacticRecruitEntity.this.workerInventory = NonNullList.withSize(
                    ArmyMemberSnapshot.CARGO_SLOT_COUNT, ItemStack.EMPTY);
            this.setChanged();
        }

        private boolean validSlot(int slot) {
            return slot >= 0 && slot < this.getContainerSize();
        }
    }

    private static boolean mergeAll(List<ItemStack> inventory, List<ItemStack> incoming) {
        for (ItemStack original : incoming) {
            ItemStack remaining = original.copy();
            for (ItemStack existing : inventory) {
                if (!remaining.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)
                        && existing.getCount() < existing.getMaxStackSize()) {
                    int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
            for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
                if (inventory.get(slot).isEmpty()) {
                    int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    inventory.set(slot, remaining.copyWithCount(moved));
                    remaining.shrink(moved);
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean removeOneFromStacks(List<ItemStack> stacks, net.minecraft.world.item.Item item) {
        for (ItemStack stack : stacks) {
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void transitionWorker(WorkerPhase phase, String reason, @Nullable BlockPos target) {
        this.workerPhase = phase;
        this.workerReason = reason;
        this.activeWorkTarget = target == null ? null : target.immutable();
        this.entityData.set(DATA_WORKER_PHASE, phase.id());
        this.entityData.set(DATA_WORKER_REASON, reason);
        this.entityData.set(DATA_ACTIVE_WORK_TARGET, Optional.ofNullable(this.activeWorkTarget));
        this.syncRecruitStatusState();
    }

    private void blockWorker(String reason) {
        if (reason.equals("target_unreachable") && this.activeWorkTarget != null) {
            this.blacklistedWorkTarget = this.activeWorkTarget.immutable();
            this.blacklistedWorkTargetTicks = 600;
        }
        this.workerCooldownTicks = 100;
        this.workerNavigationFailures = 0;
        this.blockCurrentWorkOrder(reason);
        this.pauseWorkerNavigation();
        this.transitionWorker(WorkerPhase.BLOCKED, reason, null);
    }

    private int worksiteScanRadius() {
        return Math.max(MIN_WORK_RADIUS, Math.min(MAX_WORK_RADIUS, this.workRadius));
    }

    private boolean tryHire(ServerPlayer player) {
        if (this.isTame()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.already_hired"));
            return false;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData kingdomData = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> kingdom = kingdomData.kingdomForPlayer(player.getUUID());
        if (kingdom.isEmpty()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.command_center_required"));
            return false;
        }
        if (!kingdom.orElseThrow().allows(
                player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.RECRUIT)) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.permission_denied"));
            return false;
        }
        Optional<ArmyUnitDefinition> unitOptional = this.currentUnitDefinition();
        Optional<CivilianArchetypeDefinition> civilianOptional = this.currentCivilianArchetype();
        if (unitOptional.isEmpty() && civilianOptional.isEmpty()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.data_missing"));
            return false;
        }
        boolean civilianContract = civilianOptional.isPresent();
        FactionDefinition faction = GameplayDataManager.snapshot().factions()
                .definition(galacticwars.clonewars.faction.FactionId.of(this.recruitFactionId()))
                .orElse(null);
        if (faction == null) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.data_missing"));
            return false;
        }
        FactionAlignment alignment = FactionAlignmentSavedData.get(serverLevel).alignment(player.getUUID());
        if (alignment.score(faction.id()) < faction.minimumHiringAlignment()) {
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.alignment_too_low",
                    faction.minimumHiringAlignment()));
            return false;
        }
        int hireCost = this.currentHireCost();
        Optional<CommandCenterBlockEntity> hall = this.findCommandCenter(serverLevel, kingdom.get());
        RecruitmentEligibility eligibility = RecruitmentService.evaluateDirectHire(
                kingdom.get(),
                this.recruitFactionId(),
                hireCost,
                player.hasInfiniteMaterials()
                        ? Integer.MAX_VALUE
                        : RecruitmentPaymentService.creditCount(player),
                hall.map(CommandCenterBlockEntity::upkeepPaid).orElse(false),
                alliedFactionsFor(kingdom.get().factionId()));
        if (!eligibility.accepted()) {
            String translationKey = switch (eligibility.reasonCode()) {
                case "insufficient_funds" -> "message.galacticwars.recruit.need_credits";
                case "housing_full" -> "message.galacticwars.recruit.housing_full";
                case "recruit_limit_reached" -> "message.galacticwars.recruit.faction_limit";
                case "hostile_faction" -> "message.galacticwars.recruit.hostile_faction";
                default -> "message.galacticwars.recruit.upkeep_unpaid";
            };
            sendFeedback(player, eligibility.reasonCode().equals("insufficient_funds")
                    ? Component.translatable(translationKey, hireCost)
                    : Component.translatable(translationKey));
            return false;
        }
        ProgressionSavedData progression = ProgressionSavedData.get(serverLevel);
        KingdomGameplayAction hireAction = new KingdomGameplayAction(
                KingdomActionId.of("recruit_hired", this.getUUID()),
                player.getUUID(), ProgressionEventType.RECRUIT_HIRED,
                this.recruitUnitId().substring(this.recruitUnitId().indexOf(':') + 1), 1);
        KingdomGameplayResult hireEvaluation = KingdomGameplayTransactionService.evaluate(
                progression.state(player.getUUID()), hireAction);
        if (!hireEvaluation.accepted() || !hireEvaluation.changed()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.data_missing"));
            return false;
        }
        if (!kingdomData.registerRecruit(
                player.getUUID(), this.getUUID(),
                civilianContract ? NpcServiceBranch.CIVILIAN : NpcServiceBranch.MILITARY)) {
            KingdomRecord currentKingdom = kingdomData.kingdomForPlayer(player.getUUID())
                    .orElse(kingdom.orElseThrow());
            String rejectionKey = currentKingdom.settlement().recruitIds().size()
                    >= FactionBalanceService.effectiveRecruitLimit(currentKingdom.factionId())
                    ? "message.galacticwars.recruit.faction_limit"
                    : "message.galacticwars.recruit.housing_full";
            sendFeedback(player, Component.translatable(rejectionKey));
            return false;
        }
        if (!RecruitmentPaymentService.withdrawCredits(player, hireCost)) {
            kingdomData.unregisterRecruit(player.getUUID(), this.getUUID());
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.payment_changed"));
            return false;
        }
        KingdomGameplayResult hireProgress = KingdomGameplayRuntimeService.applyProgression(
                progression, hireAction);
        if (!hireProgress.accepted() || !hireProgress.changed()) {
            RecruitmentPaymentService.refundCredits(player, hireCost);
            kingdomData.unregisterRecruit(player.getUUID(), this.getUUID());
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.payment_changed"));
            return false;
        }

        KingdomRecord registeredKingdom = kingdomData.kingdomForPlayer(player.getUUID()).orElseThrow();
        this.tameForContract(registeredKingdom.ownerId());
        if (this.factionOutpostId != null) {
            FactionOutpostSavedData.get(serverLevel).removeNpc(this.getUUID(), serverLevel.getGameTime());
            this.factionOutpostId = null;
            this.naturalPlanetNpcInitialized = false;
            this.clearHome();
        }
        this.kingdomId = registeredKingdom.id();
        this.settlementId = registeredKingdom.settlement().id();
        this.clearAuthorityMemories();
        if (civilianContract) {
            this.armyGroupId = null;
            this.switchDutyBranch(NpcServiceBranch.CIVILIAN);
            this.setRecruitDuty(RecruitDuty.WORKER);
            this.setRecruitCommand(RecruitmentAction.HOLD_POSITION);
        } else {
            this.switchDutyBranch(NpcServiceBranch.MILITARY);
            this.setRecruitDuty(RecruitDuty.SOLDIER);
            if (registeredKingdom.allows(
                    player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.COMMAND_ARMY)
                    && kingdomData.addRecruitToArmy(player.getUUID(), this.getUUID())) {
                this.armyGroupId = kingdomData.armyGroupForRecruit(this.getUUID())
                        .map(ArmyGroupRecord::id)
                        .orElse(null);
                this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
            } else {
                this.armyGroupId = null;
                this.setRecruitCommand(RecruitmentAction.HOLD_POSITION);
            }
        }
        this.level().broadcastEntityEvent(this, (byte) 7);
        sendFeedback(player, Component.translatable("message.galacticwars.recruit.hired"));
        return true;
    }

    private boolean tryAssignWorkerProfession(ServerPlayer player, WorkerProfession profession) {
        if (!WorkerProfessionCatalog.isEnabled(profession)) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.profession.disabled"));
            return false;
        }
        if (this.getRecruitDuty() == RecruitDuty.COMMANDER) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.worker"));
            return false;
        }
        if (!WorkerDutyLoadoutPolicy.isCompatible(profession, this.getWorkerMainHandItem())) {
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.profession.tool_incompatible",
                    Component.translatable(profession.translationKey())));
            return false;
        }
        WorkerProfessionDefinition definition = WorkerProfessionCatalog.definition(profession).orElseThrow();
        int cost = definition.hireCostCredits();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.worksite.missing"));
            return false;
        }
        WorkerProfession previousProfession = this.getWorkerProfession().orElse(null);
        KingdomSavedData kingdomData = KingdomSavedData.get(serverLevel);
        ProgressionSavedData progression = ProgressionSavedData.get(serverLevel);
        KingdomGameplayAction professionAction = new KingdomGameplayAction(
                KingdomActionId.of("profession_assigned", this.getUUID(), profession.id()),
                player.getUUID(), ProgressionEventType.PROFESSION_ASSIGNED,
                profession.id(), 1);
        KingdomGameplayResult professionEvaluation = KingdomGameplayTransactionService.evaluate(
                progression.state(player.getUUID()), professionAction);
        if (!professionEvaluation.accepted()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.data_missing"));
            return false;
        }
        Optional<UUID> preferredProject = profession == WorkerProfession.BUILDER
                ? Optional.ofNullable(this.activeBuildProjectId)
                : Optional.empty();
        UUID previousArmyGroupId = kingdomData.armyGroupForRecruit(this.getUUID())
                .map(ArmyGroupRecord::id).orElse(null);
        if (previousArmyGroupId != null
                && !kingdomData.releaseArmyMember(
                        player.getUUID(), this.getUUID(), false, this.armyLocation())) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.worksite.missing"));
            return false;
        }
        if (!kingdomData.reserveWorksite(
                player.getUUID(), this.getUUID(), profession, preferredProject)) {
            if (previousArmyGroupId != null) {
                kingdomData.addRecruitToArmy(player.getUUID(), previousArmyGroupId, this.getUUID());
            }
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.worksite.full"));
            return false;
        }
        if (previousProfession == profession) {
            if (professionEvaluation.changed()) {
                KingdomGameplayResult reconciliation = KingdomGameplayRuntimeService.applyProgression(
                        progression, professionAction);
                if (!reconciliation.accepted()) {
                    sendFeedback(player, Component.translatable(
                            "message.galacticwars.recruit.data_missing"));
                    return false;
                }
            }
            this.reconcileWorkerAuthority(serverLevel);
            this.resumeWorkAfterProfessionAssignment();
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.profession",
                    Component.translatable(profession.translationKey())));
            return true;
        }
        if (!player.hasInfiniteMaterials() && RecruitmentPaymentService.creditCount(player) < cost) {
            this.restorePreviousAssignment(
                    kingdomData, player.getUUID(), previousProfession, previousArmyGroupId);
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.profession.need_credits",
                    cost,
                    Component.translatable(profession.translationKey())));
            return false;
        }
        if (!RecruitmentPaymentService.withdrawCredits(player, cost)) {
            this.restorePreviousAssignment(
                    kingdomData, player.getUUID(), previousProfession, previousArmyGroupId);
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.payment_changed"));
            return false;
        }
        KingdomGameplayResult professionProgress = KingdomGameplayRuntimeService.applyProgression(
                progression, professionAction);
        if (!professionProgress.accepted()) {
            RecruitmentPaymentService.refundCredits(player, cost);
            this.restorePreviousAssignment(
                    kingdomData, player.getUUID(), previousProfession, previousArmyGroupId);
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.payment_changed"));
            return false;
        }
        this.setWorkerProfession(profession);
        this.reconcileWorkerAuthority(serverLevel);
        this.resumeWorkAfterProfessionAssignment();
        sendFeedback(player, Component.translatable(
                "message.galacticwars.recruit.profession.contract",
                Component.translatable(profession.translationKey()),
                cost));
        return true;
    }

    /**
     * Connects an authoritative persisted project to this recruit's existing builder work loop.
     * Project creation and site preflight belong to {@code ConstructionProjectService}; this method
     * owns the recruit-side assignment and rolls its local selection back when the contract fails.
     */
    public boolean assignConstructionProject(
            ServerPlayer actor,
            BuildProject project,
            KingdomBaseBlueprint blueprint
    ) {
        return assignConstructionProjectInternal(actor, project, blueprint, false);
    }

    /** Assigns the one onboarding build without charging or requiring the later workforce unlock. */
    public boolean assignStarterConstructionProject(
            ServerPlayer actor,
            BuildProject project,
            KingdomBaseBlueprint blueprint
    ) {
        if (!KingdomBaseBlueprint.STARTER_CAMP_ID.equals(blueprint.id())) {
            return false;
        }
        return assignConstructionProjectInternal(actor, project, blueprint, true);
    }

    public boolean packUpStarterConstruction() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return false;
        }
        UUID ownerId = this.getOwnerReference().getUUID();
        if (!this.workerInventoryIsEmpty()) {
            KingdomSavedData data = KingdomSavedData.get(serverLevel);
            boolean returned = this.availableRegisteredStorage(serverLevel, data, ownerId).stream()
                    .anyMatch(this::insertWorkerInventory);
            if (!returned) {
                this.blockWorker("starter_material_return_failed");
                return false;
            }
        }
        this.pauseWorkerNavigation();
        this.releaseCurrentWorkOrder(true);
        this.activeBuildProjectId = null;
        this.setBaseTarget(null);
        this.setWorkTarget(null);
        this.clearWorkerProfession();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        data.addRecruitToArmy(ownerId, this.getUUID());
        this.armyGroupId = data.armyGroupForRecruit(this.getUUID()).map(ArmyGroupRecord::id).orElse(null);
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        this.syncRecruitStatusState();
        return true;
    }

    private boolean assignConstructionProjectInternal(
            ServerPlayer actor,
            BuildProject project,
            KingdomBaseBlueprint blueprint,
            boolean starterContract
    ) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(project, "project");
        Objects.requireNonNull(blueprint, "blueprint");
        if (!(this.level() instanceof ServerLevel serverLevel)
                || actor.level() != serverLevel
                || this.getOwnerReference() == null
                || this.kingdomId == null
                || !this.kingdomId.equals(KingdomSavedData.get(serverLevel)
                        .kingdomForPlayer(actor.getUUID()).map(KingdomRecord::id).orElse(null))
                || !project.blueprintId().equals(blueprint.id())
                || !blueprint.matchesDefinitionHash(project.definitionHash())
                || !project.dimensionId().equals(serverLevel.dimension().identifier().toString())
                || !blueprint.supportsRotationSteps(project.rotationSteps())) {
            sendFeedback(actor, Component.translatable(
                    "message.galacticwars.recruit.worksite.missing"));
            return false;
        }
        Optional<BuildProject> currentProject = this.activeBuildProject();
        if (currentProject.filter(current -> !current.id().equals(project.id()))
                .filter(current -> current.state()
                        != galacticwars.clonewars.kingdom.BuildProjectState.COMPLETED)
                .filter(current -> current.state()
                        != galacticwars.clonewars.kingdom.BuildProjectState.CANCELLED)
                .isPresent()) {
            sendFeedback(actor, Component.translatable(
                    "message.galacticwars.recruit.base.blueprint_locked"));
            return false;
        }
        BlockPos previousBaseTarget = this.baseTarget;
        BlockPos previousWorkTarget = this.workTarget;
        UUID previousProjectId = this.activeBuildProjectId;
        String previousBlueprintId = this.selectedBlueprintId;
        int previousRotation = this.buildRotationSteps;
        int previousProgress = this.starterBaseCompletedBlocks;
        BlockPos origin = new BlockPos(project.originX(), project.originY(), project.originZ());
        this.activeBuildProjectId = project.id();
        this.selectedBlueprintId = blueprint.id();
        this.buildRotationSteps = project.rotationSteps();
        this.starterBaseCompletedBlocks = project.completedPlacements().size();
        this.setBaseTarget(origin);
        this.setWorkTarget(origin);
        boolean assigned = starterContract
                ? this.assignStarterBuilderDuty(actor, project)
                : this.tryAssignWorkerProfession(actor, WorkerProfession.BUILDER);
        if (!assigned) {
            this.activeBuildProjectId = previousProjectId;
            this.selectedBlueprintId = previousBlueprintId;
            this.buildRotationSteps = previousRotation;
            this.starterBaseCompletedBlocks = previousProgress;
            this.setBaseTarget(previousBaseTarget);
            this.setWorkTarget(previousWorkTarget);
            this.syncRecruitStatusState();
            return false;
        }
        this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "blueprint_queued", null);
        this.syncRecruitStatusState();
        return true;
    }

    private boolean assignStarterBuilderDuty(ServerPlayer actor, BuildProject project) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.getRecruitDuty() == RecruitDuty.COMMANDER
                || this.getOwnerReference() == null
                || !this.getOwnerReference().getUUID().equals(actor.getUUID())) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        this.setWorkerProfession(WorkerProfession.BUILDER);
        if (!data.reserveWorksite(
                actor.getUUID(), this.getUUID(), WorkerProfession.BUILDER, Optional.of(project.id()))) {
            this.clearWorkerProfession();
            data.addRecruitToArmy(actor.getUUID(), this.getUUID());
            return false;
        }
        this.reconcileWorkerAuthority(serverLevel);
        this.resumeWorkAfterProfessionAssignment();
        return true;
    }

    private void restorePreviousAssignment(
            KingdomSavedData kingdomData,
            UUID actorId,
            @Nullable WorkerProfession previousProfession,
            @Nullable UUID previousArmyGroupId
    ) {
        kingdomData.releaseWorksite(actorId, this.getUUID());
        if (previousProfession != null) {
            kingdomData.reserveWorksite(actorId, this.getUUID(), previousProfession);
            return;
        }
        kingdomData.setNpcServiceBranch(actorId, this.getUUID(), NpcServiceBranch.MILITARY);
        if (previousArmyGroupId != null) {
            kingdomData.addRecruitToArmy(actorId, previousArmyGroupId, this.getUUID());
        }
    }

    private boolean cycleSelectedBlueprint(ServerPlayer player) {
        KingdomBaseBlueprint current = this.selectedBlueprint();
        if (this.baseTarget != null && this.starterBaseCompletedBlocks < current.placements().size()) {
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.recruit.base.blueprint_locked"));
            return false;
        }
        List<KingdomBaseBlueprint> blueprints = List.copyOf(GameplayDataManager.snapshot().blueprints().values());
        if (blueprints.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.faction_selection.data_missing"));
            return false;
        }
        int currentIndex = Math.max(0, blueprints.indexOf(current));
        KingdomBaseBlueprint next = blueprints.get((currentIndex + 1) % blueprints.size());
        this.selectedBlueprintId = next.id();
        this.entityData.set(DATA_SELECTED_BLUEPRINT, this.selectedBlueprintId);
        this.starterBaseCompletedBlocks = 0;
        this.setBaseTarget(null);
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.recruit.base.blueprint",
                Component.literal(next.displayName())));
        return true;
    }

    private boolean tryReturnToSoldier(ServerPlayer player) {
        WorkerContractService.ExitDecision decision = WorkerContractService.evaluateExit(
                this.getRecruitDuty(),
                this.getWorkerProfession().isPresent(),
                this.workerInventoryIsEmpty(),
                this.baseTarget != null);
        if (decision != WorkerContractService.ExitDecision.ACCEPTED) {
            String translationKey = switch (decision) {
                case NOT_WORKER -> "message.galacticwars.recruit.soldier.rejected";
                case CARRIED_ITEMS -> "message.galacticwars.recruit.soldier.inventory";
                case ACTIVE_BUILD -> "message.galacticwars.recruit.soldier.build_active";
                case ACCEPTED -> throw new IllegalStateException("accepted exit handled below");
            };
            sendFeedback(player, Component.translatable(translationKey));
            return false;
        }
        this.pauseWorkerNavigation();
        this.clearWorkerProfession(player.getUUID());
        sendFeedback(player, Component.translatable(
                "message.galacticwars.recruit.soldier.returned"));
        return true;
    }

    private boolean tryCancelBuilding(ServerPlayer player) {
        if (this.activeBuildProjectId == null) {
            if (this.baseTarget == null) {
                sendFeedback(player, Component.translatable(
                        "message.galacticwars.recruit.base.cancel_missing"));
                return false;
            }
            this.pauseWorkerNavigation();
            this.releaseCurrentWorkOrder(true);
            this.setBaseTarget(null);
            this.setWorkTarget(null);
            this.starterBaseCompletedBlocks = 0;
            this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
            this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "build_cancelled", null);
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.base.cancelled"));
            return true;
        }
        return this.cancelConstructionProject(player, this.activeBuildProjectId);
    }

    public boolean cancelConstructionProject(ServerPlayer actor, UUID projectId) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(projectId, "projectId");
        if (!(this.level() instanceof ServerLevel serverLevel)
                || actor.level() != serverLevel
                || this.activeBuildProjectId == null
                || !this.activeBuildProjectId.equals(projectId)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        KingdomRecord kingdom = data.kingdomForPlayer(actor.getUUID()).orElse(null);
        BuildProject project = this.activeBuildProject().filter(candidate ->
                candidate.id().equals(projectId)).orElse(null);
        if (kingdom == null || project == null
                || !kingdom.id().equals(this.kingdomId)
                || !kingdom.allows(actor.getUUID(),
                galacticwars.clonewars.kingdom.KingdomPermission.BUILD)
                || !kingdom.allows(actor.getUUID(),
                galacticwars.clonewars.kingdom.KingdomPermission.MANAGE_WORKSITES)) {
            return false;
        }
        this.pauseWorkerNavigation();
        this.releaseCurrentWorkOrder(true);
        if (!data.replaceBuildProject(kingdom.ownerId(), project.cancel())) {
            return false;
        }
        this.setBaseTarget(null);
        this.setWorkTarget(null);
        this.starterBaseCompletedBlocks = 0;
        this.activeBuildProjectId = null;
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        this.transitionWorker(WorkerPhase.ACQUIRE_ORDER, "build_cancelled", null);
        sendFeedback(actor, Component.translatable(
                "message.galacticwars.recruit.base.cancelled"));
        return true;
    }

    private KingdomBaseBlueprint selectedBlueprint() {
        if (this.level().isClientSide()) {
            throw new IllegalStateException("Full gameplay blueprints are server-owned");
        }
        String blueprintId = this.selectedBlueprintId;
        GameplayDataSnapshot content = GameplayDataManager.snapshot();
        var loaded = content.blueprint(blueprintId)
                .or(() -> content.blueprint(KingdomBaseBlueprint.STARTER_KEEP_ID));
        if (loaded.isPresent()) {
            return loaded.orElseThrow();
        }
        throw new IllegalStateException("No validated gameplay blueprint is available for " + blueprintId);
    }

    private String selectedBlueprintDisplayName() {
        if (!this.level().isClientSide()) {
            return this.selectedBlueprint().displayName();
        }
        String blueprintId = this.entityData.get(DATA_SELECTED_BLUEPRINT);
        return ClientGameplayCatalog.snapshot().blueprint(blueprintId)
                .map(galacticwars.clonewars.network.GameplayCatalogPayload.BlueprintEntry::displayName)
                .orElse(blueprintId.isBlank() ? "unknown" : blueprintId);
    }

    private int selectedBlueprintPlacementCount() {
        if (!this.level().isClientSide()) {
            return this.selectedBlueprint().placements().size();
        }
        String blueprintId = this.entityData.get(DATA_SELECTED_BLUEPRINT);
        return ClientGameplayCatalog.snapshot().blueprint(blueprintId)
                .map(galacticwars.clonewars.network.GameplayCatalogPayload.BlueprintEntry::placementCount)
                .orElse(Math.max(0, this.entityData.get(DATA_BASE_PROGRESS)));
    }

    private boolean tryPromoteCommander(ServerPlayer player) {
        if (this.serviceBranch != NpcServiceBranch.MILITARY
                || this.getRecruitDuty() != RecruitDuty.SOLDIER || this.getWorkerProfession().isPresent()) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.worker"));
            return false;
        }
        this.migrateLegacyKingdomLink();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.kingdomId == null) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.rejected"));
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        if (!data.promoteCommander(player.getUUID(), this.getUUID())) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.rejected"));
            return false;
        }
        this.setRecruitDuty(RecruitDuty.COMMANDER);
        ArmyFormation formation = this.currentUnitDefinition()
                .map(ArmyUnitDefinition::defaultFormation)
                .orElse(ArmyFormation.LINE);
        Optional<ArmyGroupRecord> group = data.createOrReclaimArmyGroup(
                player.getUUID(),
                this.getUUID(),
                formation,
                this.armyLocation(),
                serverLevel.getGameTime());
        if (group.isEmpty()) {
            data.clearCommander(player.getUUID(), this.getUUID());
            this.setRecruitDuty(RecruitDuty.SOLDIER);
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.rejected"));
            return false;
        }
        this.armyGroupId = group.orElseThrow().id();
        this.linkLoadedSoldiersToCommander(serverLevel, player.getUUID());
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.promoted"));
        return true;
    }

    private boolean tryToggleAutomaticRecruitment(ServerPlayer player) {
        if (this.getRecruitDuty() != RecruitDuty.COMMANDER || !(this.level() instanceof ServerLevel serverLevel)) {
            player.sendSystemMessage(Component.translatable("message.galacticwars.recruit.commander.rejected"));
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> kingdomOptional = data.kingdomForOwner(player.getUUID());
        if (kingdomOptional.isEmpty()) {
            return false;
        }
        KingdomRecord kingdom = kingdomOptional.get();
        this.linkLoadedSoldiersToCommander(serverLevel, player.getUUID());
        CommanderPolicy current = kingdom.settlement().commanderPolicy();
        CommanderPolicy updated = new CommanderPolicy(
                !current.automaticRecruitment(),
                Math.max(current.targetRecruitCount(), 4),
                current.maximumCampaignSpend(),
                current.minimumTreasuryReserve(),
                current.campaignDelayTicks());
        if (!data.updateCommanderPolicy(player.getUUID(), kingdom.settlement().revision(), updated)) {
            return false;
        }
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.recruit.commander.auto",
                Boolean.toString(updated.automaticRecruitment())));
        return true;
    }

    private boolean tryStartCommanderCampaign(ServerPlayer player) {
        return this.startCommanderCampaign(player);
    }

    private boolean startCommanderCampaign(@Nullable ServerPlayer feedbackPlayer) {
        if (this.getRecruitDuty() != RecruitDuty.COMMANDER || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return false;
        }
        UUID ownerId = owner.getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> kingdomOptional = data.kingdomForOwner(ownerId);
        if (kingdomOptional.isEmpty()) {
            return false;
        }
        KingdomRecord kingdom = kingdomOptional.get();
        Optional<CommandCenterBlockEntity> hallOptional = this.findCommandCenter(serverLevel, kingdom);
        if (hallOptional.isEmpty()) {
            this.sendCampaignRejection(feedbackPlayer, "hall_unloaded");
            return false;
        }
        CommandCenterBlockEntity hall = hallOptional.get();
        KingdomRecord evaluatedKingdom = kingdom;
        if (feedbackPlayer != null && !kingdom.settlement().commanderPolicy().automaticRecruitment()) {
            CommanderPolicy currentPolicy = kingdom.settlement().commanderPolicy();
            CommanderPolicy manualPolicy = new CommanderPolicy(
                    true,
                    currentPolicy.targetRecruitCount(),
                    currentPolicy.maximumCampaignSpend(),
                    currentPolicy.minimumTreasuryReserve(),
                    currentPolicy.campaignDelayTicks());
            evaluatedKingdom = kingdom.withSettlement(kingdom.settlement().withCommanderPolicy(manualPolicy));
        }
        RecruitmentCampaignDecision decision = RecruitmentService.evaluateCommanderCampaign(
                evaluatedKingdom,
                this.getUUID(),
                this.recruitUnitId(),
                this.recruitFactionId(),
                "",
                this.currentHireCost(),
                hall.treasuryCredits(),
                serverLevel.getGameTime(),
                alliedFactionsFor(kingdom.factionId()));
        if (!decision.accepted()) {
            this.sendCampaignRejection(feedbackPlayer, decision.reasonCode());
            return false;
        }
        RecruitmentCampaign campaign = decision.campaign().orElseThrow();
        if (!hall.reserveCredits(campaign.reservedCost())) {
            this.sendCampaignRejection(feedbackPlayer, "treasury_changed");
            return false;
        }
        if (!data.beginCampaign(ownerId, decision)) {
            hall.refundCredits(campaign.reservedCost());
            this.sendCampaignRejection(feedbackPlayer, "campaign_changed");
            return false;
        }
        if (feedbackPlayer != null) {
            feedbackPlayer.sendSystemMessage(Component.translatable(
                    "message.galacticwars.recruit.commander.campaign",
                    campaign.reservedCost()));
        }
        return true;
    }

    private void tickCommanderCampaign() {
        if (this.getRecruitDuty() != RecruitDuty.COMMANDER || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return;
        }
        UUID ownerId = owner.getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> kingdomOptional = data.kingdomForOwner(ownerId);
        if (kingdomOptional.isEmpty()
                || kingdomOptional.get().settlement().commanderId().filter(this.getUUID()::equals).isEmpty()) {
            return;
        }
        KingdomRecord kingdom = kingdomOptional.get();
        long currentGameTime = serverLevel.getGameTime();
        long elapsedGameTime = this.lastCommanderCampaignGameTime == 0L
                ? 20L
                : Math.max(1L, currentGameTime - this.lastCommanderCampaignGameTime);
        this.lastCommanderCampaignGameTime = currentGameTime;
        Optional<RecruitmentCampaign> activeCampaign = kingdom.settlement().recruitmentCampaigns().stream()
                .filter(RecruitmentCampaign::active)
                .findFirst();
        long campaignDelayApplied = 0L;
        if (activeCampaign.isPresent() && elapsedGameTime > 40L) {
            campaignDelayApplied = elapsedGameTime - 20L;
            data.replaceCampaign(ownerId, activeCampaign.get().delay(campaignDelayApplied));
            kingdom = data.kingdomForOwner(ownerId).orElse(kingdom);
            activeCampaign = kingdom.settlement().recruitmentCampaigns().stream()
                    .filter(RecruitmentCampaign::active)
                    .findFirst();
        }
        Optional<CommandCenterBlockEntity> hallOptional = this.findCommandCenter(serverLevel, kingdom);
        if (hallOptional.isEmpty()) {
            long remainingDelay = elapsedGameTime - campaignDelayApplied;
            if (activeCampaign.isPresent() && remainingDelay > 0L) {
                data.replaceCampaign(ownerId, activeCampaign.get().delay(remainingDelay));
            }
            return;
        }
        CommandCenterBlockEntity hall = hallOptional.get();
        if (!hall.chargeDailyUpkeep(serverLevel.getGameTime(), kingdom.settlement().recruitIds().size())) {
            return;
        }
        if (activeCampaign.isPresent()) {
            if (currentGameTime >= activeCampaign.get().readyGameTime()) {
                this.completeCommanderCampaign(serverLevel, kingdom, hall, activeCampaign.get());
            }
            return;
        }
        if (kingdom.settlement().commanderPolicy().automaticRecruitment()) {
            this.startCommanderCampaign(null);
        }
    }

    private void completeCommanderCampaign(
            ServerLevel level,
            KingdomRecord kingdom,
            CommandCenterBlockEntity hall,
            RecruitmentCampaign campaign
    ) {
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomRecord current = data.kingdomForOwner(kingdom.ownerId()).orElse(kingdom);
        if (current.settlement().recruitIds().size()
                >= FactionBalanceService.effectiveRecruitLimit(current.factionId())) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "recruit_limit_reached");
            return;
        }
        if (!current.settlement().hasHousingSpace()) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "housing_full");
            return;
        }
        Optional<BlockPos> rally = this.findSafeRallyPosition(level, current);
        if (rally.isEmpty()) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "no_safe_rally_position");
            return;
        }

        @SuppressWarnings("unchecked")
        EntityType<? extends GalacticRecruitEntity> recruitType =
                (EntityType<? extends GalacticRecruitEntity>) this.getType();
        GalacticRecruitEntity recruit = recruitType.create(level, EntitySpawnReason.EVENT);
        if (recruit == null) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "spawn_failed");
            return;
        }
        BlockPos spawnPos = rally.get();
        recruit.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        recruit.setOwnerReference(EntityReference.of(kingdom.ownerId()));
        recruit.setTame(true, true);
        recruit.kingdomId = current.id();
        recruit.settlementId = current.settlement().id();
        recruit.armyGroupId = this.armyGroupId;
        recruit.setRecruitDuty(RecruitDuty.SOLDIER);
        recruit.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        ProgressionSavedData progression = ProgressionSavedData.get(level);
        KingdomGameplayAction hireAction = new KingdomGameplayAction(
                KingdomActionId.of("recruit_hired", recruit.getUUID()),
                kingdom.ownerId(), ProgressionEventType.RECRUIT_HIRED,
                recruit.recruitUnitId().substring(recruit.recruitUnitId().indexOf(':') + 1), 1);
        KingdomGameplayResult evaluation = KingdomGameplayTransactionService.evaluate(
                progression.state(kingdom.ownerId()), hireAction);
        if (!evaluation.accepted() || !evaluation.changed()) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "progression_rejected");
            return;
        }
        if (!level.addFreshEntity(recruit)) {
            this.cancelCommanderCampaign(level, current, hall, campaign, "spawn_rejected");
            return;
        }
        if (!data.registerRecruit(kingdom.ownerId(), recruit.getUUID())) {
            recruit.discard();
            this.cancelCommanderCampaign(level, current, hall, campaign, "housing_changed");
            return;
        }
        KingdomGameplayResult committed = KingdomGameplayRuntimeService.applyProgression(
                progression, hireAction);
        if (!committed.accepted() || !committed.changed()) {
            data.unregisterRecruit(kingdom.ownerId(), recruit.getUUID());
            recruit.discard();
            this.cancelCommanderCampaign(level, current, hall, campaign, "progression_commit_failed");
            return;
        }
        data.addRecruitToArmy(kingdom.ownerId(), recruit.getUUID());
        data.replaceCampaign(kingdom.ownerId(), campaign.complete());
    }

    private Optional<BlockPos> findSafeRallyPosition(ServerLevel level, KingdomRecord kingdom) {
        BlockPos hall = new BlockPos(
                kingdom.settlement().hallX(),
                kingdom.settlement().hallY(),
                kingdom.settlement().hallZ());
        for (int radius = 2; radius <= 5; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos candidate = hall.offset(x, 1, z);
                    if (level.isLoaded(candidate)
                            && level.getBlockState(candidate).isAir()
                            && level.getBlockState(candidate.above()).isAir()
                            && level.getBlockState(candidate.below()).blocksMotion()) {
                        return Optional.of(candidate.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void cancelActiveCommanderCampaign(String reason) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        data.kingdomForOwner(owner.getUUID()).ifPresent(kingdom -> {
            kingdom.settlement().recruitmentCampaigns().stream()
                    .filter(RecruitmentCampaign::active)
                    .findFirst()
                    .ifPresent(campaign -> {
                        if (data.replaceCampaign(kingdom.ownerId(), campaign.cancel(reason))) {
                            this.findCommandCenter(serverLevel, kingdom).ifPresent(
                                    hall -> data.applyPendingCampaignRefunds(kingdom.ownerId(), hall::refundCredits));
                        }
                    });
        });
    }

    private void cancelCommanderCampaign(
            ServerLevel level,
            KingdomRecord kingdom,
            CommandCenterBlockEntity hall,
            RecruitmentCampaign campaign,
            String reason
    ) {
        KingdomSavedData data = KingdomSavedData.get(level);
        if (data.replaceCampaign(kingdom.ownerId(), campaign.cancel(reason))) {
            data.applyPendingCampaignRefunds(kingdom.ownerId(), hall::refundCredits);
        }
    }

    private Optional<CommandCenterBlockEntity> findCommandCenter(ServerLevel level, KingdomRecord kingdom) {
        if (!KingdomSavedData.get(level).isHallActive(kingdom.ownerId())) {
            return Optional.empty();
        }
        if (!kingdom.settlement().dimensionId().equals(level.dimension().identifier().toString())) {
            return Optional.empty();
        }
        BlockPos hallPos = new BlockPos(
                kingdom.settlement().hallX(),
                kingdom.settlement().hallY(),
                kingdom.settlement().hallZ());
        if (!level.isLoaded(hallPos)) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(hallPos);
        return blockEntity instanceof CommandCenterBlockEntity hall ? Optional.of(hall) : Optional.empty();
    }

    private boolean hasPaidSettlementUpkeep() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return true;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return false;
        }
        return KingdomSavedData.get(serverLevel).kingdomForOwner(owner.getUUID())
                .flatMap(kingdom -> this.findCommandCenter(serverLevel, kingdom))
                .map(CommandCenterBlockEntity::upkeepPaid)
                .orElse(false);
    }

    private String recruitFactionId() {
        return this.currentUnitDefinition()
                .map(definition -> definition.factionId().toString())
                .or(() -> this.currentCivilianArchetype().map(CivilianArchetypeDefinition::factionId))
                .orElse("galacticwars:republic");
    }

    public String factionIdForGameplay() {
        return this.recruitFactionId();
    }

    private Optional<BuildProject> activeBuildProject() {
        if (this.activeBuildProjectId == null
                || !(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null) {
            return Optional.empty();
        }
        return KingdomSavedData.get(serverLevel).kingdomForOwner(this.getOwnerReference().getUUID())
                .stream()
                .flatMap(kingdom -> kingdom.settlement().buildProjects().stream())
                .filter(project -> project.id().equals(this.activeBuildProjectId))
                .findFirst();
    }

    private boolean persistBuildPlacement(BuildProject project, int placementIndex) {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.getOwnerReference() == null) {
            return false;
        }
        BuildProject updatedProject = project.markCompleted(placementIndex);
        if (!KingdomSavedData.get(serverLevel).replaceBuildProject(
                this.getOwnerReference().getUUID(), updatedProject)) {
            return false;
        }
        return this.syncBuilderWorkOrderProgress(updatedProject.completedPlacements().size());
    }

    private void persistBlockedBuildProject(BuildProject project, String reason) {
        if (this.level() instanceof ServerLevel serverLevel && this.getOwnerReference() != null) {
            KingdomSavedData.get(serverLevel).replaceBuildProject(
                    this.getOwnerReference().getUUID(), project.block(reason));
        }
    }

    private String recruitUnitId() {
        return this.currentUnitDefinition()
                .map(definition -> definition.id().toString())
                .or(() -> this.currentCivilianArchetype().map(CivilianArchetypeDefinition::id))
                .orElse("galacticwars:clone_trooper");
    }

    private int currentHireCost() {
        Optional<ArmyUnitDefinition> unit = this.currentUnitDefinition();
        if (unit.isPresent()) {
            return unit.orElseThrow().hireCost();
        }
        Optional<CivilianArchetypeDefinition> civilian = this.currentCivilianArchetype();
        if (civilian.isPresent()) {
            return Math.max(1, GameplayDataManager.snapshot().faction(civilian.orElseThrow().factionId())
                    .map(definition -> definition.hireCost() / 2)
                    .orElse(1));
        }
        return GameplayDataManager.snapshot().faction(this.recruitFactionId())
                .map(FactionDefinition::hireCost)
                .orElse(0);
    }

    private static Set<String> alliedFactionsFor(String factionId) {
        return GameplayDataManager.snapshot().faction(factionId)
                .map(definition -> definition.allies().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                .orElse(Set.of());
    }

    private Optional<ArmyUnitDefinition> currentUnitDefinition() {
        if (!this.unitId.isBlank()) {
            Optional<ArmyUnitDefinition> saved = GameplayDataManager.snapshot().unit(this.unitId);
            if (saved.isPresent()) {
                return saved;
            }
        }
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString();
        return GameplayDataManager.snapshot().unitForEntityType(entityTypeId);
    }

    private Optional<CivilianArchetypeDefinition> currentCivilianArchetype() {
        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString();
        return GameplayDataManager.snapshot().civilianArchetypeForEntity(entityTypeId);
    }

    private void applyUnitDefinition() {
        this.appliedGameplayDataGeneration = GameplayDataManager.generation();
        Optional<ArmyUnitDefinition> unitOptional = this.currentUnitDefinition();
        if (unitOptional.isEmpty()) {
            this.applyCivilianArchetype();
            return;
        }
        ArmyUnitDefinition unit = unitOptional.orElseThrow();
        this.unitId = unit.id().toString();
        GameplayDataManager.snapshot().unitClassForUnit(unit.id()).ifPresent(unitClass -> {
            if (!this.classProgressState.classId().equals(unitClass.id().toString())) {
                this.classProgressState = this.classProgressState.assign(unitClass.id());
            }
        });
        float healthRatio = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(unit.maxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                this.factionMovementSpeed(unit.movementSpeed()));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(unit.attackDamage());
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(unit.followRange());
        this.getAttribute(Attributes.ARMOR).setBaseValue(unit.armor());
        this.setHealth(Math.max(1.0F, Math.min(this.getMaxHealth(), this.getMaxHealth() * healthRatio)));
        if (!this.defaultLoadoutInitialized) {
            this.applyUnitEquipment(unit.equipment());
            this.defaultLoadoutInitialized = true;
        }
    }

    private void applyCivilianArchetype() {
        Optional<CivilianArchetypeDefinition> civilianOptional = this.currentCivilianArchetype();
        if (civilianOptional.isEmpty()) {
            return;
        }
        CivilianArchetypeDefinition civilian = civilianOptional.orElseThrow();
        boolean firstDefinition = this.unitId.isBlank();
        this.unitId = civilian.id();
        float healthRatio = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(civilian.maxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                this.factionMovementSpeed(civilian.movementSpeed()));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(20.0D);
        this.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
        this.setHealth(Math.max(1.0F, Math.min(this.getMaxHealth(), this.getMaxHealth() * healthRatio)));
        if (firstDefinition) {
            this.morale = civilian.baseMorale();
        }
        if (!this.defaultLoadoutInitialized) {
            if (!this.isTame()) {
                this.serviceBranch = NpcServiceBranch.CIVILIAN;
            }
            for (EquipmentSlot slot : List.of(
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
            this.defaultLoadoutInitialized = true;
            if (this.isNaturalFactionCivilian()) {
                assignNaturalCivilianProfession();
            } else {
                this.getWorkerProfession().ifPresent(this::applyWorkerEquipment);
            }
        }
    }

    private void applyUnitEquipment(ArmyEquipmentLoadout equipment) {
        this.setEquipmentFromData(EquipmentSlot.MAINHAND, equipment.mainHandItemId());
        this.setEquipmentFromData(EquipmentSlot.HEAD, equipment.headItemId());
        this.setEquipmentFromData(EquipmentSlot.CHEST, equipment.chestItemId());
        this.setEquipmentFromData(EquipmentSlot.LEGS, equipment.legsItemId());
        this.setEquipmentFromData(EquipmentSlot.FEET, equipment.feetItemId());
    }

    private void setEquipmentFromData(EquipmentSlot slot, String itemId) {
        ItemStack stack = equipmentStackFromData(itemId);
        this.setItemSlot(slot, stack);
        if (!stack.isEmpty()) {
            this.setDropChance(slot, 0.0F);
        }
    }

    private ItemStack defaultMilitaryMainHand() {
        return this.currentUnitDefinition()
                .map(ArmyUnitDefinition::equipment)
                .map(ArmyEquipmentLoadout::mainHandItemId)
                .map(this::equipmentStackFromData)
                .orElse(ItemStack.EMPTY);
    }

    private void migrateLegacyIronSwordLoadout() {
        ItemStack configuredWeapon = this.defaultMilitaryMainHand();
        if (this.getMainHandItem().is(Items.IRON_SWORD)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, configuredWeapon.copy());
            if (!configuredWeapon.isEmpty()) {
                this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            }
        }
        if (this.serviceBranch == NpcServiceBranch.CIVILIAN
                && this.inactiveDutyMainHand.is(Items.IRON_SWORD)) {
            this.inactiveDutyMainHand = configuredWeapon.copy();
        }
    }

    private ItemStack equipmentStackFromData(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.parse(itemId);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            GalacticWars.LOGGER.error("Validated unit equipment item disappeared from registry: {}", itemId);
            return ItemStack.EMPTY;
        }
        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public Optional<ArmyMemberSnapshot> createArmySnapshot(long generation) {
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (!this.isAlive() || owner == null || this.kingdomId == null || this.unitId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ArmyMemberSnapshot(
                this.getUUID(),
                BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString(),
                this.unitId,
                owner.getUUID(),
                this.kingdomId,
                this.getRecruitDuty(),
                Math.max(1.0F, this.getHealth()),
                this.morale,
                this.hunger,
                this.unpaidTicks,
                generation,
                new ArmySnapshotEquipment(
                        this.getItemBySlot(EquipmentSlot.MAINHAND),
                        this.getItemBySlot(EquipmentSlot.OFFHAND),
                        this.getItemBySlot(EquipmentSlot.HEAD),
                        this.getItemBySlot(EquipmentSlot.CHEST),
                        this.getItemBySlot(EquipmentSlot.LEGS),
                        this.getItemBySlot(EquipmentSlot.FEET)),
                this.copyWorkerInventory(),
                this.getCustomName() == null ? "" : this.getCustomName().getString()));
    }

    public void restoreArmySnapshot(ArmyMemberSnapshot snapshot, UUID groupId) {
        this.setUUID(snapshot.recruitId());
        this.setOwnerReference(EntityReference.of(snapshot.ownerId()));
        this.setTame(true, true);
        this.kingdomId = snapshot.kingdomId();
        this.armyGroupId = groupId;
        this.unitId = snapshot.unitId();
        this.morale = clampVital(snapshot.morale());
        this.hunger = clampVital(snapshot.hunger());
        this.unpaidTicks = Math.max(0, snapshot.unpaidTicks());
        this.armySnapshotGeneration = snapshot.generation();
        this.setRecruitDuty(snapshot.duty());
        this.applyUnitDefinition();
        this.setHealth(Math.min(this.getMaxHealth(), Math.max(1.0F, snapshot.health())));
        this.restoreArmyEquipment(EquipmentSlot.MAINHAND, snapshot.equipment().mainHand());
        this.restoreArmyEquipment(EquipmentSlot.OFFHAND, snapshot.equipment().offHand());
        this.restoreArmyEquipment(EquipmentSlot.HEAD, snapshot.equipment().head());
        this.restoreArmyEquipment(EquipmentSlot.CHEST, snapshot.equipment().chest());
        this.restoreArmyEquipment(EquipmentSlot.LEGS, snapshot.equipment().legs());
        this.restoreArmyEquipment(EquipmentSlot.FEET, snapshot.equipment().feet());
        List<ItemStack> cargo = snapshot.cargo();
        this.workerInventory = NonNullList.withSize(ArmyMemberSnapshot.CARGO_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < cargo.size(); slot++) {
            this.workerInventory.set(slot, cargo.get(slot).copy());
        }
        if (!snapshot.customName().isBlank()) {
            this.setCustomName(Component.literal(snapshot.customName()));
        }
    }

    public boolean initializeStarterContract(ServerPlayer owner, KingdomRecord kingdom) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(kingdom, "kingdom");
        if (!(this.level() instanceof ServerLevel serverLevel)
                || owner.level() != serverLevel
                || !owner.getUUID().equals(kingdom.ownerId())
                || !this.recruitFactionId().equals(kingdom.factionId())) {
            return false;
        }
        this.setOwnerReference(EntityReference.of(owner));
        this.setTame(true, true);
        this.kingdomId = kingdom.id();
        this.settlementId = kingdom.settlement().id();
        this.factionOutpostId = null;
        this.armyGroupId = null;
        this.serviceBranch = NpcServiceBranch.MILITARY;
        this.setRecruitDuty(RecruitDuty.SOLDIER);
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
        this.applyUnitDefinition();
        this.syncRecruitStatusState();
        return true;
    }

    public ArmyFormationRole armyFormationRole() {
        if (this.getRecruitDuty() == RecruitDuty.COMMANDER) {
            return ArmyFormationRole.LEADER;
        }
        return GameplayDataManager.snapshot().unit(this.recruitUnitId())
                .map(ArmyUnitDefinition::role)
                .map(ArmyFormationRole::fromUnitRole)
                .orElse(ArmyFormationRole.FRONTLINE);
    }

    private void restoreArmyEquipment(EquipmentSlot slot, ItemStack stack) {
        this.setItemSlot(slot, stack.copy());
        this.setDropChance(slot, 0.0F);
    }

    public long getArmySnapshotGeneration() {
        return this.armySnapshotGeneration;
    }

    private void syncArmySnapshot(ServerLevel level) {
        if (this.armyGroupId == null || this.getRecruitDuty() == RecruitDuty.WORKER) {
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        ArmyGroupRecord group = data.armyGroup(this.armyGroupId).orElse(null);
        if (group == null || group.simulation().lifecycleState()
                != galacticwars.clonewars.army.ArmyGroupLifecycleState.LIVE) {
            return;
        }
        this.armySnapshotGeneration = group.simulation().snapshotGeneration();
        this.createArmySnapshot(this.armySnapshotGeneration)
                .ifPresent(snapshot -> data.upsertArmySnapshot(group.id(), snapshot));
    }

    private void tickArmyVitals(ServerLevel level) {
        if (!this.isTame() || this.getRecruitDuty() == RecruitDuty.WORKER) {
            return;
        }
        boolean upkeepPaid = this.hasPaidSettlementUpkeep();
        this.unpaidTicks = upkeepPaid ? 0 : Math.min(Integer.MAX_VALUE - 20, this.unpaidTicks) + 20;
        if (this.tickCount % 1200 == 0) {
            this.hunger = clampVital(this.hunger - 1);
        }
        if (this.tickCount % 200 == 0 && upkeepPaid && this.getTarget() == null && this.hurtTime == 0
                && this.nearCommanderOrHall(level)) {
            this.morale = clampVital(this.morale + this.factionMoraleRecovery());
        }
    }

    private boolean nearCommanderOrHall(ServerLevel level) {
        if (this.armyGroupId != null) {
            ArmyGroupRecord group = KingdomSavedData.get(level).armyGroup(this.armyGroupId).orElse(null);
            if (group != null && group.commanderId()
                    .map(level::getEntity)
                    .filter(Objects::nonNull)
                    .filter(entity -> entity.distanceToSqr(this) <= 1024.0D)
                    .isPresent()) {
                return true;
            }
        }
        if (this.getOwnerReference() == null) {
            return false;
        }
        return KingdomSavedData.get(level).kingdomForOwner(this.getOwnerReference().getUUID())
                .map(KingdomRecord::settlement)
                .filter(settlement -> settlement.dimensionId().equals(level.dimension().identifier().toString()))
                .filter(settlement -> this.distanceToSqr(
                        settlement.hallX() + 0.5D,
                        settlement.hallY() + 0.5D,
                        settlement.hallZ() + 0.5D) <= 1024.0D)
                .isPresent();
    }

    private static int clampVital(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private FactionBalanceService.ResolvedBalance factionBalance() {
        return FactionBalanceService.resolve(this.recruitFactionId());
    }

    private int factionProductionCooldownTicks(int baseTicks) {
        if (baseTicks <= 0) {
            throw new IllegalArgumentException("baseTicks must be positive");
        }
        int productionPercent = Math.max(1, this.factionBalance().productionPercent());
        long numerator = (long) baseTicks * 100L;
        return (int) Math.max(1L, Math.min(
                Integer.MAX_VALUE, (numerator + productionPercent - 1L) / productionPercent));
    }

    private double factionMovementSpeed(double baseSpeed) {
        int mobilityPercent = Math.max(1, this.factionBalance().mobilityPercent());
        return Math.max(0.01D, Math.min(4.0D, baseSpeed * mobilityPercent / 100.0D));
    }

    private int factionMoraleStabilityPercent() {
        FactionBalanceService.ResolvedBalance balance = this.factionBalance();
        int moraleWithStrategy = Math.max(1, balance.moralePercent() + balance.moraleBonus());
        int withLoyalty = FactionBalanceService.applyPercentFloor(
                moraleWithStrategy, balance.loyaltyPercent());
        return Math.max(1, Math.min(FactionBalanceService.MAX_PERCENT, withLoyalty));
    }

    private int factionMoraleLoss(int baseLoss) {
        if (baseLoss <= 0) {
            return 0;
        }
        int stabilityPercent = this.factionMoraleStabilityPercent();
        long numerator = (long) baseLoss * 100L;
        return (int) Math.max(1L, Math.min(
                Integer.MAX_VALUE, (numerator + stabilityPercent - 1L) / stabilityPercent));
    }

    private int factionMoraleRecovery() {
        return Math.max(1, FactionBalanceService.applyPercentCeil(
                1, this.factionMoraleStabilityPercent()));
    }

    private void tameForContract(UUID kingdomOwnerId) {
        this.setOwnerReference(EntityReference.of(kingdomOwnerId));
        this.setTame(true, true);
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        if (player.connection != null) {
            player.sendSystemMessage(message);
        }
    }

    private void sendCampaignRejection(@Nullable ServerPlayer player, String reason) {
        if (player != null) {
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.commander.campaign_rejected",
                    reason));
        }
    }

    private void setRecruitDuty(RecruitDuty duty) {
        this.entityData.set(DATA_RECRUIT_DUTY, duty.ordinal());
    }

    private static boolean isArmyCommandAction(RecruitCommandAction action) {
        return switch (action) {
            case FOLLOW, HOLD, MOVE, PROTECT, ATTACK, CLEAR, CYCLE_FORMATION, PATROL -> true;
            default -> false;
        };
    }

    private static boolean isWorkerManagementAction(RecruitCommandAction action) {
        return switch (action) {
            case SET_WORKSITE, RETURN_WORKSITE, CLEAR_WORKSITE, SET_STORAGE,
                    BUILD_STARTER_KEEP, WORK_RADIUS_DECREASE, WORK_RADIUS_INCREASE,
                    NEXT_BLUEPRINT, RETURN_TO_SOLDIER, CANCEL_BUILD,
                    ASSIGN_WORKER_PROFESSION, ROTATE_BLUEPRINT -> true;
            default -> false;
        };
    }

    private static boolean isRecruitManagementAction(RecruitCommandAction action) {
        return switch (action) {
            case PROMOTE_COMMANDER, TOGGLE_AUTO_RECRUITMENT, START_RECRUITMENT -> true;
            default -> false;
        };
    }

    private void clearAuthorityMemories() {
        this.setTarget(null);
        this.setAggressive(false);
        BrainUtil.clearMemories(
                this,
                ArmyBrainMemoryTypes.ARMY_STATE,
                ArmyBrainMemoryTypes.PATH_STATUS,
                ArmyBrainMemoryTypes.MARCH_STATE,
                ArmyBrainMemoryTypes.NAVIGATION_RESULT,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.PATH,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY);
    }

    private boolean applyMenuArmyOrder(
            ServerPlayer actor,
            RecruitmentAction action,
            @Nullable BlockPos target
    ) {
        if (this.hasAuthoritativeArmyGroup()) {
            return this.persistArmyGroupOrder(actor.getUUID(), action, target);
        }
        if (!this.canPlayerCommandArmy(actor)) {
            return false;
        }
        switch (action) {
            case HOLD_POSITION -> this.moveTarget = this.blockPosition();
            case MOVE_TO_POSITION -> this.moveTarget = Objects.requireNonNull(target, "target").immutable();
            case FOLLOW_OWNER, PROTECT_OWNER, CLEAR_TARGET -> {
                // No additional local payload.
            }
            default -> {
                return false;
            }
        }
        this.setRecruitCommand(action);
        return true;
    }

    private boolean applyMenuArmyAttack(ServerPlayer actor, LivingEntity target) {
        if (!this.canAttackTarget(target)) {
            return false;
        }
        if (this.hasAuthoritativeArmyGroup()) {
            return this.persistArmyGroupAttack(actor.getUUID(), target);
        }
        if (!this.canPlayerCommandArmy(actor)) {
            return false;
        }
        this.installAuthorizedAttackTarget(target);
        this.setRecruitCommand(RecruitmentAction.ATTACK_TARGET);
        return true;
    }

    private void linkLoadedSoldiersToCommander(ServerLevel level, UUID ownerId) {
        if (this.armyGroupId == null) {
            return;
        }
        for (GalacticRecruitEntity recruit : level.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                this.getBoundingBox().inflate(128.0),
                recruit -> recruit != this
                        && recruit.serviceBranch == NpcServiceBranch.MILITARY
                        && recruit.getRecruitDuty() == RecruitDuty.SOLDIER)) {
            EntityReference<LivingEntity> recruitOwner = recruit.getOwnerReference();
            if (recruitOwner != null
                    && ownerId.equals(recruitOwner.getUUID())
                    && (this.kingdomId == null || this.kingdomId.equals(recruit.kingdomId))) {
                recruit.armyGroupId = this.armyGroupId;
            }
        }
    }

    private boolean persistArmyGroupOrder(
            UUID actorId,
            RecruitmentAction action,
            @Nullable BlockPos target
    ) {
        if (this.armyGroupId == null
                || !(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null) {
            return false;
        }
        ArmyGroupRecord group = KingdomSavedData.get(serverLevel).armyGroup(this.armyGroupId).orElse(null);
        if (group == null) {
            return false;
        }
        UUID ownerId = group.ownerId();
        ArmyGroupOrder order = switch (action) {
            case FOLLOW_OWNER -> new ArmyGroupOrder(
                    ArmyCommandType.FOLLOW_OWNER, Optional.empty(), Optional.empty(),
                    group.order().formation(), group.order().spacing());
            case HOLD_POSITION -> new ArmyGroupOrder(
                    ArmyCommandType.HOLD_POSITION, Optional.of(this.armyLocation()), Optional.empty(),
                    group.order().formation(), group.order().spacing());
            case MOVE_TO_POSITION -> new ArmyGroupOrder(
                    ArmyCommandType.MOVE_TO_POSITION,
                    Optional.of(this.armyLocation(Objects.requireNonNull(target, "target"))), Optional.empty(),
                    group.order().formation(), group.order().spacing());
            case PROTECT_OWNER -> new ArmyGroupOrder(
                    ArmyCommandType.PROTECT_OWNER, Optional.empty(), Optional.empty(),
                    group.order().formation(), group.order().spacing());
            case CLEAR_TARGET -> new ArmyGroupOrder(
                    ArmyCommandType.CLEAR_TARGET, Optional.empty(), Optional.empty(),
                    group.order().formation(), group.order().spacing());
            default -> null;
        };
        if (order == null
                || !ArmyCommandPolicy.canIssue(
                        order.toCommand(ownerId, group.id()), group.commandValidationState()).accepted()
                || !KingdomSavedData.get(serverLevel).issueArmyOrder(actorId, group.id(), order)) {
            return false;
        }
        this.linkLoadedSoldiersToCommander(serverLevel, ownerId);
        this.reconcileArmyGroupOrder(serverLevel);
        for (GalacticRecruitEntity recruit : serverLevel.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                this.getBoundingBox().inflate(128.0),
                recruit -> recruit != this
                        && recruit.getRecruitDuty() == RecruitDuty.SOLDIER
                        && this.armyGroupId.equals(recruit.armyGroupId))) {
            EntityReference<LivingEntity> recruitOwner = recruit.getOwnerReference();
            if (recruitOwner == null || !ownerId.equals(recruitOwner.getUUID())) {
                continue;
            }
            recruit.reconcileArmyGroupOrder(serverLevel);
        }
        return true;
    }

    private boolean persistArmyGroupAttack(UUID actorId, LivingEntity target) {
        if (this.armyGroupId == null
                || !(this.level() instanceof ServerLevel serverLevel)
                || this.getOwnerReference() == null) {
            return false;
        }
        if (!this.canAttackTarget(target)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        ArmyGroupRecord group = data.armyGroup(this.armyGroupId).orElse(null);
        if (group == null) {
            return false;
        }
        UUID ownerId = group.ownerId();
        ArmyGroupOrder order = new ArmyGroupOrder(
                ArmyCommandType.ATTACK_TARGET,
                Optional.of(this.armyLocation(target.blockPosition())),
                Optional.of(target.getUUID()),
                group.order().formation(),
                group.order().spacing());
        if (!ArmyCommandPolicy.canIssue(
                order.toCommand(ownerId, group.id()), group.commandValidationState()).accepted()
                || !data.issueArmyOrder(actorId, group.id(), order)) {
            return false;
        }
        this.linkLoadedSoldiersToCommander(serverLevel, ownerId);
        this.reconcileArmyGroupOrder(serverLevel);
        for (GalacticRecruitEntity recruit : serverLevel.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                this.getBoundingBox().inflate(128.0),
                recruit -> recruit != this && group.contains(recruit.getUUID()))) {
            recruit.reconcileArmyGroupOrder(serverLevel);
        }
        return true;
    }

    private boolean canAttackTarget(LivingEntity target) {
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (!target.isAlive() || target == this
                || owner != null && target.getUUID().equals(owner.getUUID())) {
            return false;
        }
        if (target instanceof Player player) {
            return this.canAttackFactionPlayer(player);
        }
        if (target instanceof GalacticRecruitEntity recruit) {
            EntityReference<LivingEntity> targetOwner = recruit.getOwnerReference();
            boolean sameOwner = owner != null && targetOwner != null
                    && targetOwner.getUUID().equals(owner.getUUID());
            return !sameOwner
                    && recruit.getRecruitDuty() != RecruitDuty.WORKER
                    && this.factionRelationTo(recruit) == FactionRelation.ENEMY;
        }
        return target instanceof Monster;
    }

    private boolean cycleArmyFormation(ServerPlayer player) {
        if (this.getRecruitDuty() != RecruitDuty.COMMANDER
                || this.armyGroupId == null
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        ArmyGroupRecord group = data.armyGroup(this.armyGroupId).orElse(null);
        KingdomRecord actorKingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
        if (group == null || actorKingdom == null
                || !group.kingdomId().equals(actorKingdom.id())
                || !actorKingdom.allows(
                        player.getUUID(), galacticwars.clonewars.kingdom.KingdomPermission.COMMAND_ARMY)) {
            return false;
        }
        ArmyFormation[] formations = ArmyFormation.values();
        ArmyFormation next = formations[(group.order().formation().ordinal() + 1) % formations.length];
        if (!data.issueArmyOrder(player.getUUID(), group.id(), group.order().withFormation(next))) {
            return false;
        }
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.recruit.commander.formation",
                Component.literal(next.name().toLowerCase())));
        return true;
    }

    private boolean startArmyPatrol(ServerPlayer player) {
        if (this.armyGroupId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.patrol.invalid"));
            return false;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        ArmyGroupRecord group = data.armyGroup(this.armyGroupId).orElse(null);
        if (group == null) {
            return false;
        }
        String dimensionId = serverLevel.dimension().identifier().toString();
        ArmyLocation rallyPoint = group.rallyPoint()
                .filter(location -> location.dimensionId().equals(dimensionId))
                .orElseGet(this::armyLocation);
        ArmyLocation endpoint = this.armyLocation(player.blockPosition());
        if (rallyPoint.blockPosition().equals(endpoint.blockPosition())
                || !data.startArmyPatrol(
                        player.getUUID(), group.id(), rallyPoint, List.of(rallyPoint, endpoint))) {
            sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.patrol.invalid"));
            return false;
        }
        this.reconcileArmyGroupOrder(serverLevel);
        for (GalacticRecruitEntity recruit : serverLevel.getEntitiesOfClass(
                GalacticRecruitEntity.class,
                this.getBoundingBox().inflate(128.0),
                recruit -> recruit != this && group.contains(recruit.getUUID()))) {
            recruit.reconcileArmyGroupOrder(serverLevel);
        }
        sendFeedback(player, Component.translatable("message.galacticwars.recruit.commander.patrol"));
        return true;
    }

    private boolean rotateSelectedBlueprint(ServerPlayer player) {
        if (this.baseTarget != null || this.activeBuildProjectId != null) {
            sendFeedback(player, Component.translatable(
                    "message.galacticwars.recruit.base.blueprint_locked"));
            return false;
        }
        this.buildRotationSteps = Math.floorMod(this.buildRotationSteps + 1, 4);
        sendFeedback(player, Component.translatable(
                "message.galacticwars.recruit.base.rotation", this.buildRotationSteps * 90));
        return true;
    }

    private void reconcileArmyGroupOrder(ServerLevel level) {
        if (!this.isTame() || this.getOwnerReference() == null) {
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        Optional<ArmyGroupRecord> groupOptional = data.armyGroupForRecruit(this.getUUID());
        if (this.getRecruitDuty() == RecruitDuty.WORKER) {
            groupOptional.ifPresent(group -> data.releaseArmyMember(
                    group.ownerId(), this.getUUID(), false, this.armyLocation()));
            this.armyGroupId = null;
            return;
        }
        if (groupOptional.isEmpty()) {
            if (this.armyGroupId != null) {
                this.armyGroupId = null;
                this.setTarget(null);
                this.setAggressive(false);
                BrainUtil.clearMemories(
                        this, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
                this.setRecruitCommand(RecruitmentAction.HOLD_POSITION);
            }
            return;
        }
        ArmyGroupRecord group = groupOptional.orElseThrow();
        this.armyGroupId = group.id();
        RecruitmentAction action = recruitmentAction(group.order().type());
        Optional<ArmyLocation> commandPosition = group.order().type() == ArmyCommandType.RETURN_TO_RALLY
                ? group.rallyPoint()
                : group.order().targetPosition();
        LivingEntity persistedTarget = group.order().type() == ArmyCommandType.ATTACK_TARGET
                ? group.order().targetEntityId()
                        .map(level::getEntity)
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .filter(this::canAttackTarget)
                        .orElse(null)
                : null;
        if (this.getRecruitDuty() == RecruitDuty.COMMANDER) {
            ArmyPosition executionPosition = ArmyGroupOrderPlanner.executionPosition(group);
            this.moveTarget = executionPosition == null ? null : new BlockPos(
                    executionPosition.x(), executionPosition.y(), executionPosition.z());
            this.setRecruitCommand(action);
            if (action == RecruitmentAction.ATTACK_TARGET) {
                this.installAuthorizedAttackTarget(persistedTarget);
            }
            return;
        }
        List<ArmyGroupOrderAssignment> assignments = ArmyGroupOrderPlanner.plan(group, null);
        assignments.stream()
                .filter(assignment -> assignment.recruitId().equals(this.getUUID()))
                .findFirst()
                .ifPresent(assignment -> {
                    if (assignment.assignedPosition() != null) {
                        this.moveTarget = new BlockPos(
                                assignment.assignedPosition().x(),
                                assignment.assignedPosition().y(),
                                assignment.assignedPosition().z());
                    } else {
                        this.moveTarget = commandPosition
                                .map(ArmyLocation::blockPosition)
                                .map(position -> new BlockPos(position.x(), position.y(), position.z()))
                                .orElse(null);
                    }
                    this.setRecruitCommand(action);
                    if (action == RecruitmentAction.ATTACK_TARGET) {
                        this.installAuthorizedAttackTarget(persistedTarget);
                    }
                });
    }

    private static RecruitmentAction recruitmentAction(ArmyCommandType type) {
        return switch (type) {
            case FOLLOW_OWNER -> RecruitmentAction.FOLLOW_OWNER;
            case HOLD_POSITION -> RecruitmentAction.HOLD_POSITION;
            case MOVE_TO_POSITION -> RecruitmentAction.MOVE_TO_POSITION;
            case PROTECT_OWNER -> RecruitmentAction.PROTECT_OWNER;
            case PROTECT_ENTITY -> RecruitmentAction.PROTECT_OWNER;
            case ATTACK_TARGET -> RecruitmentAction.ATTACK_TARGET;
            case CLEAR_TARGET -> RecruitmentAction.CLEAR_TARGET;
            case RETURN_TO_RALLY -> RecruitmentAction.MOVE_TO_POSITION;
            case PATROL_ROUTE -> RecruitmentAction.PATROL_ROUTE;
        };
    }

    private void migrateLegacyKingdomLink() {
        if (this.kingdomId != null || !this.isTame() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        if (owner == null) {
            return;
        }
        UUID ownerId = owner.getUUID();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> kingdom = data.kingdomForOwner(ownerId);
        if (kingdom.isEmpty()) {
            return;
        }
        if (!kingdom.get().settlement().containsRecruit(this.getUUID())
                && !data.registerRecruit(ownerId, this.getUUID())) {
            return;
        }
        KingdomRecord linked = data.kingdomForOwner(ownerId).orElseThrow();
        this.kingdomId = linked.id();
        this.settlementId = linked.settlement().id();
    }

    private void setRecruitCommand(RecruitmentAction command) {
        RecruitmentAction previous = this.getRecruitCommand();
        this.entityData.set(DATA_COMMAND, command.ordinal());
        if (previous != command) {
            this.acknowledgeOrderTicks = 10;
        }
        this.setOrderedToSit(this.isTame() && command == RecruitmentAction.HOLD_POSITION);
        if (command != RecruitmentAction.MOVE_TO_POSITION
                && command != RecruitmentAction.WORK_AT_SITE
                && command != RecruitmentAction.HOLD_POSITION
                && command != RecruitmentAction.PATROL_ROUTE) {
            this.moveTarget = null;
        }
        if (command != RecruitmentAction.ATTACK_TARGET) {
            BrainUtil.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
            this.setTarget(null);
        }
        this.syncRecruitStatusState();
    }

    private void installAuthorizedAttackTarget(@Nullable LivingEntity target) {
        if (target == null) {
            BrainUtil.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
            this.setTarget(null);
            this.setAggressive(false);
            return;
        }
        BrainUtil.setTargetOfEntity(this, target);
        this.setTarget(target);
        this.setAggressive(true);
    }

    private void resumeWorkAfterProfessionAssignment() {
        if (this.workTarget != null) {
            this.moveTarget = this.workTarget;
            this.setRecruitCommand(RecruitmentAction.WORK_AT_SITE);
            return;
        }
        this.setRecruitCommand(RecruitmentAction.FOLLOW_OWNER);
    }

    private void setWorkTarget(@Nullable BlockPos target) {
        this.workTarget = target;
        this.entityData.set(DATA_WORK_TARGET, Optional.ofNullable(target));
        this.syncRecruitStatusState();
    }

    private void setStorageTarget(@Nullable BlockPos target) {
        this.storageTarget = target;
        this.entityData.set(DATA_STORAGE_TARGET, Optional.ofNullable(target));
        this.syncRecruitStatusState();
    }

    private void setBaseTarget(@Nullable BlockPos target) {
        this.baseTarget = target;
        this.entityData.set(DATA_BASE_TARGET, Optional.ofNullable(target));
        this.syncRecruitStatusState();
    }

    private void adjustWorkRadius(int delta) {
        this.setWorkRadius(this.workRadius + delta);
    }

    private void setWorkRadius(int radius) {
        this.workRadius = Math.max(MIN_WORK_RADIUS, Math.min(MAX_WORK_RADIUS, radius));
        this.entityData.set(DATA_WORK_RADIUS, this.workRadius);
        this.syncRecruitStatusState();
    }

    private void syncRecruitStatusState() {
        this.entityData.set(DATA_WORK_TARGET, Optional.ofNullable(this.workTarget));
        this.entityData.set(DATA_STORAGE_TARGET, Optional.ofNullable(this.storageTarget));
        this.entityData.set(DATA_BASE_TARGET, Optional.ofNullable(this.baseTarget));
        this.entityData.set(DATA_STORAGE_RESOURCE_COUNT, this.storageItemCount());
        this.entityData.set(DATA_CARRIED_RESOURCE_COUNT,
                this.workerInventory.stream().mapToInt(ItemStack::getCount).sum());
        this.entityData.set(DATA_BASE_PROGRESS, this.starterBaseCompletedBlocks);
        this.entityData.set(DATA_WORK_RADIUS, this.workRadius);
        this.entityData.set(DATA_WORKER_PHASE, this.workerPhase.id());
        this.entityData.set(DATA_WORKER_REASON, this.workerReason);
        this.entityData.set(DATA_ACTIVE_WORK_TARGET, Optional.ofNullable(this.activeWorkTarget));
        this.entityData.set(DATA_SELECTED_BLUEPRINT, this.selectedBlueprintId);
        this.entityData.set(DATA_RESOURCE_ACTION, this.getWorkerProfession().isPresent() ? this.workerPhase.id() : "");
        this.entityData.set(DATA_RESOURCE_ITEM, this.getWorkerProfession().isPresent() ? this.workerReason : "");
        this.entityData.set(DATA_VISUAL_ACTION, this.resolveVisualAction().id());
    }

    private RecruitVisualAction resolveVisualAction() {
        if (this.acknowledgeOrderTicks > 0) {
            return RecruitVisualAction.ACKNOWLEDGE_ORDER;
        }
        if (this.getHealth() <= this.getMaxHealth() * 0.35F) {
            return RecruitVisualAction.INJURED;
        }
        if (this.morale < 25) {
            return RecruitVisualAction.LOW_MORALE;
        }
        if (this.getTarget() != null) {
            return FactionRangedWeaponService.supportsRecruitRangedCombat(this.getMainHandItem())
                    ? RecruitVisualAction.RANGED_COMBAT
                    : RecruitVisualAction.MELEE_COMBAT;
        }
        if (this.getWorkerProfession().filter(profession -> profession == WorkerProfession.BUILDER).isPresent()
                && this.activeBuildProjectId != null) {
            return RecruitVisualAction.BUILD;
        }
        if (this.level() instanceof ServerLevel serverLevel && this.armyGroupId != null) {
            ArmyGroupRecord group = KingdomSavedData.get(serverLevel).armyGroup(this.armyGroupId).orElse(null);
            if (group != null) {
                if (group.order().type() == ArmyCommandType.RETURN_TO_RALLY) {
                    return RecruitVisualAction.RETREAT;
                }
                return switch (group.simulation().marchState().phase()) {
                    case FORMING, REFORMING -> RecruitVisualAction.FORM_UP;
                    case MARCHING, COMPRESSED -> RecruitVisualAction.MARCH;
                    case HALTED -> RecruitVisualAction.HALT;
                    case ENGAGED -> FactionRangedWeaponService.supportsRecruitRangedCombat(this.getMainHandItem())
                            ? RecruitVisualAction.RANGED_COMBAT : RecruitVisualAction.MELEE_COMBAT;
                };
            }
        }
        return this.getRecruitCommand() == RecruitmentAction.HOLD_POSITION
                ? RecruitVisualAction.HALT : RecruitVisualAction.IDLE;
    }

    private @Nullable BlockPos displayedTarget(
            EntityDataAccessor<Optional<BlockPos>> accessor,
            @Nullable BlockPos serverValue
    ) {
        if (!this.level().isClientSide()) {
            return serverValue;
        }
        return this.entityData.get(accessor).orElse(null);
    }

    private Optional<ResourceStatus> displayedResourceStatus() {
        if (this.level().isClientSide()) {
            String action = this.entityData.get(DATA_RESOURCE_ACTION);
            if (action.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new ResourceStatus(action, this.entityData.get(DATA_RESOURCE_ITEM)));
        }
        return this.getWorkerProfession().isEmpty()
                ? Optional.empty()
                : Optional.of(new ResourceStatus(this.workerPhase.id(), this.workerReason));
    }

    private int storageItemCount() {
        if (this.storageTarget == null) {
            return 0;
        }
        int authorizedSlots = this.registeredStorageSlots(this.storageTarget);
        return this.findContainer(this.storageTarget)
                .map(container -> java.util.stream.IntStream.range(
                                0, Math.min(authorizedSlots, container.getContainerSize()))
                        .map(slot -> container.getItem(slot).getCount())
                        .sum())
                .orElse(0);
    }

    private ResourceInventory workerInventorySnapshot() {
        return resourceSnapshot(this.workerInventory);
    }

    private ResourceInventory storageInventorySnapshot() {
        if (this.storageTarget == null) {
            return ResourceInventory.empty();
        }
        int authorizedSlots = this.registeredStorageSlots(this.storageTarget);
        return this.findContainer(this.storageTarget)
                .map(container -> {
                    int slotLimit = Math.min(authorizedSlots, container.getContainerSize());
                    ArrayList<ItemStack> stacks = new ArrayList<>(slotLimit);
                    for (int slot = 0; slot < slotLimit; slot++) {
                        stacks.add(container.getItem(slot));
                    }
                    return resourceSnapshot(stacks);
                })
                .orElseGet(ResourceInventory::empty);
    }

    private static ResourceInventory resourceSnapshot(Iterable<ItemStack> stacks) {
        ResourceInventory resources = ResourceInventory.empty();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                resources = resources.withAdded(
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                        stack.getCount());
            }
        }
        return resources;
    }

    private static Optional<BlockPos> targetedBlock(ServerPlayer player) {
        Optional<BlockPos> selected = CommandTargetSelection.blockFromInventory(player);
        if (selected.isPresent()) {
            return selected;
        }
        HitResult hit = player.pick(8.0, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
            return Optional.empty();
        }
        return Optional.of(blockHit.getBlockPos().immutable());
    }

    private static Optional<LivingEntity> targetedLivingEntity(ServerPlayer player) {
        Optional<LivingEntity> selected = CommandTargetSelection.entityFromInventory(player);
        if (selected.isPresent()) {
            return selected;
        }
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F).scale(32.0D);
        Vec3 end = start.add(direction);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(direction).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living.isAlive() && entity != player,
                32.0D * 32.0D);
        return hit != null && hit.getEntity() instanceof LivingEntity living
                ? Optional.of(living)
                : Optional.empty();
    }

    private ArmyLocation armyLocation() {
        return new ArmyLocation(
                this.level().dimension().identifier().toString(),
                this.getX(), this.getY(), this.getZ());
    }

    private ArmyLocation armyLocation(BlockPos position) {
        return new ArmyLocation(
                this.level().dimension().identifier().toString(),
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D);
    }

    private Optional<WorkerWorksite> createWorksite(WorkerProfession profession) {
        if (this.workTarget == null) {
            return Optional.empty();
        }
        return WorkerProfessionCatalog.definition(profession)
                .map(WorkerProfessionDefinition::workAreaType)
                .map(area -> new WorkerWorksite(
                        area,
                        this.workTarget.getX(),
                        this.workTarget.getY(),
                        this.workTarget.getZ(),
                        this.workRadius));
    }

    private Optional<WorkerLogisticsRoute> createCourierRoute() {
        if (this.storageTarget == null || this.workTarget == null) {
            return Optional.empty();
        }
        return Optional.of(new WorkerLogisticsRoute(
                new WorkerWorksite(
                        WorkAreaType.STORAGE,
                        this.storageTarget.getX(),
                        this.storageTarget.getY(),
                        this.storageTarget.getZ(),
                        8),
                new WorkerWorksite(
                        WorkAreaType.COURIER_ROUTE,
                        this.workTarget.getX(),
                        this.workTarget.getY(),
                        this.workTarget.getZ(),
                        this.workRadius)));
    }

    private void applyWorkerEquipment(WorkerProfession profession) {
        if (this.getWorkerMainHandItem().isEmpty()) {
            this.setWorkerMainHandItem(WorkerDutyLoadoutPolicy.defaultTool(profession));
        }
    }

    private static RecruitmentAction parseCommand(String value) {
        try {
            return RecruitmentAction.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return RecruitmentAction.FOLLOW_OWNER;
        }
    }

    private static Component targetLabel(@Nullable BlockPos target) {
        if (target == null) {
            return Component.translatable("screen.galacticwars.recruit.status.none");
        }
        return Component.literal(target.getX() + ", " + target.getY() + ", " + target.getZ());
    }

    private static Optional<Block> blockForPlacement(String blockId) {
        return switch (blockId) {
            case "galacticwars:duracrete" -> Optional.of(ModBlocks.DURACRETE.get());
            case "galacticwars:nightsister_weave_log" -> Optional.of(ModBlocks.NIGHTSISTER_WEAVE_LOG.get());
            case "minecraft:oak_planks" -> Optional.of(Blocks.OAK_PLANKS);
            case "minecraft:oak_log" -> Optional.of(Blocks.OAK_LOG);
            case "minecraft:dirt" -> Optional.of(Blocks.DIRT);
            case "minecraft:chest" -> Optional.of(Blocks.CHEST);
            case "minecraft:campfire" -> Optional.of(Blocks.CAMPFIRE);
            case "minecraft:crafting_table" -> Optional.of(Blocks.CRAFTING_TABLE);
            default -> Optional.empty();
        };
    }

    private record ResourceStatus(String action, String itemId) {
    }
}
