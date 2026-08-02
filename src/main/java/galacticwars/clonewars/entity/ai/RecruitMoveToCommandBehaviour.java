package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

public final class RecruitMoveToCommandBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final double ARRIVAL_DISTANCE_SQUARED = 4.0D;
    private static final double MIN_PROGRESS_SQUARED = 0.25D;
    private static final int REPATH_INTERVAL = 20;
    private static final int STALL_TIMEOUT = 200;
    private static final int RETRY_BACKOFF = 40;

    private final double speedModifier;
    private BlockPos trackedTarget;
    private double bestDistanceSqr;
    private int stalledTicks;
    private int retryTicks;
    private boolean waitingToRetry;

    public RecruitMoveToCommandBehaviour(double speedModifier) {
        this.speedModifier = speedModifier;
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        if (recruit.isHazardAvoidanceActive()) {
            return false;
        }
        BlockPos currentTarget = recruit.getMoveTarget();
        if (retryTicks > 0 && (currentTarget == null || currentTarget.equals(trackedTarget))) {
            retryTicks--;
            return false;
        }
        retryTicks = 0;
        return recruit.shouldMoveToCommandTarget();
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return !recruit.isHazardAvoidanceActive()
                && recruit.shouldMoveToCommandTarget()
                && !waitingToRetry
                && distanceToTargetSqr(recruit) > ARRIVAL_DISTANCE_SQUARED;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        waitingToRetry = false;
        resetProgress(recruit, recruit.getMoveTarget());
        setWalkTarget(recruit);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        BlockPos target = recruit.getMoveTarget();
        if (target == null) {
            waitingToRetry = true;
            return;
        }
        if (!target.equals(trackedTarget)) {
            resetProgress(recruit, target);
            setWalkTarget(recruit);
        }
        double distanceSqr = distanceToTargetSqr(recruit);
        if (distanceSqr < bestDistanceSqr - MIN_PROGRESS_SQUARED) {
            bestDistanceSqr = distanceSqr;
            stalledTicks = 0;
        } else {
            stalledTicks++;
        }
        if (stalledTicks >= STALL_TIMEOUT) {
            BrainUtil.clearMemories(
                    recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
            retryTicks = RETRY_BACKOFF;
            waitingToRetry = true;
            return;
        }
        if (recruit.getNavigation().isDone()
                || recruit.tickCount % REPATH_INTERVAL == 0) {
            setWalkTarget(recruit);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(
                recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
        if (retryTicks == 0) {
            trackedTarget = null;
        }
        waitingToRetry = false;
    }

    private void setWalkTarget(GalacticRecruitEntity recruit) {
        BlockPos target = recruit.getMoveTarget();
        if (target != null) {
            BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(target, (float)speedModifier, 2));
        }
    }

    private static double distanceToTargetSqr(GalacticRecruitEntity recruit) {
        BlockPos target = recruit.getMoveTarget();
        if (target == null) {
            return 0.0D;
        }
        return recruit.distanceToSqr(
                target.getX() + 0.5D,
                target.getY(),
                target.getZ() + 0.5D);
    }

    private void resetProgress(GalacticRecruitEntity recruit, BlockPos target) {
        trackedTarget = target == null ? null : target.immutable();
        bestDistanceSqr = distanceToTargetSqr(recruit);
        stalledTicks = 0;
    }
}
