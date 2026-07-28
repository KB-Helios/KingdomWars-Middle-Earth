package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.army.ArmyBehaviorContext;
import galacticwars.clonewars.army.ArmyBehaviorDecision;
import galacticwars.clonewars.army.ArmyBehaviorIntent;
import galacticwars.clonewars.army.ArmyBehaviorPlanner;
import galacticwars.clonewars.army.ArmyCommand;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyPatrolRetreatPlanner;
import galacticwars.clonewars.army.ArmyPatrolPlan;
import galacticwars.clonewars.army.ArmyPatrolStatus;
import galacticwars.clonewars.army.ArmyPosition;
import galacticwars.clonewars.army.ArmyTacticalDecision;
import galacticwars.clonewars.army.ArmyTacticalIntent;
import galacticwars.clonewars.army.ArmyTacticalPlanner;
import galacticwars.clonewars.army.RecruitState;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Publishes formation, guard, retreat, and patrol movement through SmartBrain memories. */
public final class ArmyOrderBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int STALL_TIMEOUT = 200;
    private static final int RETRY_BACKOFF = 40;
    private static final int SAFE_TARGET_SEARCH_RADIUS = 2;
    private static final double MIN_PROGRESS_SQUARED = 0.25D;
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
            .hasMemory(ArmyBrainMemoryTypes.ARMY_STATE);

    public ArmyOrderBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        return !BrainUtil.hasMemory(recruit, MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return BrainUtil.hasMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE)
                && !BrainUtil.hasMemory(recruit, MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                ArmyBrainMemoryTypes.PATH_STATUS);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        if (!(recruit.level() instanceof ServerLevel level)) {
            return;
        }
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        if (state == null) {
            return;
        }

        if (state.group().order().type() == ArmyCommandType.PATROL_ROUTE
                && state.group().effectivePatrolPlan().map(ArmyPatrolPlan::state)
                        .filter(patrol -> patrol.status() != ArmyPatrolStatus.ACTIVE
                                || patrol.waitTicksRemaining() > 0)
                        .isPresent()) {
            clearMoveTarget(recruit);
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }

        if (state.retreatThreatId() != null) {
            publishMoveTarget(recruit, patrolRetreatTarget(recruit, level, state),
                    1.1F, formationCloseEnough(state));
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }

        ArmyTacticalDecision tactical = tacticalDecision(recruit, level, state);
        if (tactical.intent() != ArmyTacticalIntent.EXECUTE_ORDER) {
            BrainUtil.clearMemory(recruit, MemoryModuleType.ATTACK_TARGET);
            publishMoveTarget(recruit, tactical.tacticalTarget(),
                    tactical.intent() == ArmyTacticalIntent.HOLD_POSITION ? 0.8F : 1.05F,
                    formationCloseEnough(state));
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }

        ArmyBehaviorDecision behavior = tactical.behaviorDecision();
        if (behavior.intent() == ArmyBehaviorIntent.ATTACK_TARGET) {
            LivingEntity target = behavior.attackTargetId() == null
                    ? null
                    : ArmyBrainSupport.living(level.getEntity(behavior.attackTargetId()));
            if (target != null && ArmyBrainSupport.canEngageGroupTarget(recruit, state, target)) {
                if (ArmyBrainSupport.shouldMaintainFormation(state, target)) {
                    maintainFormation(recruit, state);
                    return;
                }
                BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE,
                        state.withSelectedTarget(target.getUUID()));
                BrainUtil.setTargetOfEntity(recruit, target);
            } else {
                clearMoveTarget(recruit);
            }
            return;
        }

        switch (behavior.intent()) {
            case MOVE_TO_POSITION, FOLLOW_OWNER, PROTECT_OWNER -> {
                publishMoveTarget(recruit, behavior.moveTarget(), movementSpeed(recruit, state),
                        formationCloseEnough(state));
                recruit.setTarget(null);
                recruit.setAggressive(false);
            }
            case HOLD_POSITION -> {
                if (ArmyBrainSupport.distanceSquared(
                        ArmyBrainSupport.position(recruit), behavior.moveTarget()) > 4L) {
                    publishMoveTarget(recruit, behavior.moveTarget(), 0.9F, formationCloseEnough(state));
                } else {
                    clearMoveTarget(recruit);
                }
                recruit.setTarget(null);
                recruit.setAggressive(false);
            }
            case IDLE -> {
                clearMoveTarget(recruit);
                recruit.setTarget(null);
                recruit.setAggressive(false);
            }
            case ATTACK_TARGET -> {
                // Handled before the switch so the attack memory drives the fighting activity.
            }
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        if (!BrainUtil.hasMemory(recruit, MemoryModuleType.ATTACK_TARGET)) {
            clearMoveTarget(recruit);
        }
    }

    private static ArmyTacticalDecision tacticalDecision(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            ArmyBrainState state
    ) {
        ArmyCommand command = state.memberCommand();
        LivingEntity selectedTarget = state.selectedTargetId() == null
                ? null
                : ArmyBrainSupport.living(level.getEntity(state.selectedTargetId()));
        if (selectedTarget != null
                && command.type() != ArmyCommandType.CLEAR_TARGET
                && ArmyBrainSupport.canEngageGroupTarget(recruit, state, selectedTarget)) {
            return ArmyTacticalPlanner.plan(
                    ArmyBehaviorDecision.attack(selectedTarget.getUUID(), "selected_group_target"),
                    recruit.getRecruitVitals(),
                    state.fallbackPosition(),
                    state.group().effectiveTactics());
        }
        if (command.type() == ArmyCommandType.PROTECT_ENTITY) {
            LivingEntity protectedEntity = command.targetEntityId() == null
                    ? null
                    : ArmyBrainSupport.living(level.getEntity(command.targetEntityId()));
            ArmyBehaviorDecision behavior = protectedEntity == null || !protectedEntity.isAlive()
                    ? ArmyBehaviorDecision.idle("protected_entity_unavailable")
                    : ArmyBehaviorDecision.protect(
                            ArmyBrainSupport.position(protectedEntity), "protect_entity");
            return ArmyTacticalPlanner.plan(
                    behavior, recruit.getRecruitVitals(), state.fallbackPosition(),
                    state.group().effectiveTactics());
        }
        boolean commandTargetAlive = command.targetEntityId() != null
                && ArmyBrainSupport.living(level.getEntity(command.targetEntityId())) != null;
        ArmyBehaviorDecision behavior = ArmyBehaviorPlanner.plan(
                new RecruitState(recruit.getUUID(), state.group().ownerId(), state.group().id(), command),
                new ArmyBehaviorContext(
                        ArmyBrainSupport.position(recruit),
                        state.behaviorAnchor(),
                        state.selectedTargetId(),
                        commandTargetAlive,
                        state.followRange()));
        return ArmyTacticalPlanner.plan(
                behavior, recruit.getRecruitVitals(), state.fallbackPosition(),
                state.group().effectiveTactics());
    }

    private static void publishMoveTarget(
            GalacticRecruitEntity recruit,
            ArmyPosition target,
            float speed,
            int closeEnoughDistance
    ) {
        if (target == null) {
            clearMoveTarget(recruit);
            return;
        }
        ArmyPosition resolvedTarget = resolveSafeTarget(recruit, target);
        BlockPos targetPos = new BlockPos(resolvedTarget.x(), resolvedTarget.y(), resolvedTarget.z());
        if (!recruit.level().isLoaded(targetPos)) {
            clearMoveTarget(recruit);
            return;
        }
        double distanceSquared = recruit.distanceToSqr(
                resolvedTarget.x() + 0.5D, resolvedTarget.y(), resolvedTarget.z() + 0.5D);
        ArmyPathStatus status = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.PATH_STATUS);
        if (status == null || !status.target().equals(resolvedTarget)) {
            status = ArmyPathStatus.tracking(resolvedTarget, distanceSquared);
        } else if (status.retryAfterTick() > recruit.tickCount) {
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.PATH_STATUS, status);
            return;
        } else if (distanceSquared < status.bestDistanceSquared() - MIN_PROGRESS_SQUARED) {
            status = status.progressed(distanceSquared);
        } else {
            status = status.stalled(distanceSquared);
        }
        if (status.stalledTicks() >= STALL_TIMEOUT) {
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.PATH_STATUS,
                    status.retryAfter(recruit.tickCount + RETRY_BACKOFF, distanceSquared));
            return;
        }
        BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.PATH_STATUS, status);
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
        BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                new WalkTarget(targetPos, speed, closeEnoughDistance));
    }

    private static void clearMoveTarget(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                ArmyBrainMemoryTypes.PATH_STATUS,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
    }

    private static void maintainFormation(GalacticRecruitEntity recruit, ArmyBrainState state) {
        publishMoveTarget(recruit, ArmyBrainSupport.formationAnchor(state), movementSpeed(recruit, state),
                formationCloseEnough(state));
        recruit.setTarget(null);
        recruit.setAggressive(false);
    }

    private static int formationCloseEnough(ArmyBrainState state) {
        return state.group().effectiveTactics().tightFormation() ? 1 : 2;
    }

    private static float movementSpeed(GalacticRecruitEntity recruit, ArmyBrainState state) {
        float base = state.group().order().type() == ArmyCommandType.PATROL_ROUTE
                ? state.group().effectivePatrolPlan()
                .map(ArmyPatrolPlan::loadedMovementSpeed)
                .orElse(1.0F)
                : 1.0F;
        ArmyMarchMemory march = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.MARCH_STATE);
        if (march == null || march.cohesionPercent() >= 70
                || march.phase() == galacticwars.clonewars.army.ArmyMarchPhase.ENGAGED) {
            return base;
        }
        return march.memberSlot() < 0
                ? Math.min(base, 0.8F)
                : Math.min(1.15F, base + 0.15F);
    }

    /**
     * Retreats every member around one commander-relative anchor rather than
     * collapsing the patrol into the commander's current block. The target is
     * intentionally derived from live state and never persisted as an order.
     */
    private static ArmyPosition patrolRetreatTarget(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            ArmyBrainState state
    ) {
        LivingEntity threat = ArmyBrainSupport.living(level.getEntity(state.retreatThreatId()));
        if (threat == null) {
            return state.fallbackPosition();
        }
        return ArmyPatrolRetreatPlanner.retreatPosition(
                state.group(),
                recruit.getUUID(),
                state.fallbackPosition(),
                ArmyBrainSupport.position(threat));
    }

    /**
     * The formation planner is intentionally world-independent. Resolve its
     * anchor to a nearby loaded, standable surface before publishing the
     * Minecraft walk memory, while leaving the persisted formation slot intact.
     */
    private static ArmyPosition resolveSafeTarget(GalacticRecruitEntity recruit, ArmyPosition target) {
        if (!(recruit.level() instanceof ServerLevel level)) {
            return target;
        }
        for (int radius = 0; radius <= SAFE_TARGET_SEARCH_RADIUS; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (Math.max(Math.abs(xOffset), Math.abs(zOffset)) != radius) {
                        continue;
                    }
                    int x = target.x() + xOffset;
                    int z = target.z() + zOffset;
                    BlockPos horizontal = new BlockPos(x, target.y(), z);
                    if (!level.isLoaded(horizontal)) {
                        continue;
                    }
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos surface = new BlockPos(x, y, z);
                    if (level.getBlockState(surface).isAir() && level.getBlockState(surface.above()).isAir()) {
                        return new ArmyPosition(x, y, z);
                    }
                }
            }
        }
        return target;
    }
}
