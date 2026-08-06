package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

/**
 * Consumes SmartBrain walk memories as the sole recruit navigation authority.
 *
 * <p>SmartBrainLib's stock mover does not start its computed path on Minecraft 26.2, so this
 * behaviour dispatches the published memory through Minecraft navigation without moving
 * command-specific state out of the behaviours that publish the target.</p>
 */
public final class RecruitWalkTargetBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final double MOVED_TARGET_DISTANCE_SQUARED = 4.0D;

    private BlockPos lastTargetPos;
    private boolean controlsNavigation;

    public RecruitWalkTargetBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        return true;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return true;
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        // Group orders reuse the legacy HOLD_POSITION command for UI/state
        // compatibility, which marks the recruit as sitting. The group brain
        // still needs to publish its formation target before it can hold it.
        if (recruit.isTame() && recruit.isOrderedToSit()
                && !recruit.isHazardAvoidanceActive()
                && !recruit.hasAuthoritativeArmyGroup()) {
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            stopOwnedNavigation(recruit);
            return;
        }

        WalkTarget walkTarget = BrainUtil.getMemory(recruit, MemoryModuleType.WALK_TARGET);
        if (walkTarget == null) {
            stopOwnedNavigation(recruit);
            return;
        }

        BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
        if (targetPos.distManhattan(recruit.blockPosition())
                <= walkTarget.getCloseEnoughDist()) {
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.NAVIGATION_RESULT,
                    new RecruitNavigationResult(
                            targetPos, RecruitNavigationResult.State.ARRIVED, recruit.tickCount));
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            stopOwnedNavigation(recruit);
            return;
        }

        boolean targetMoved = lastTargetPos == null
                || lastTargetPos.distSqr(targetPos) > MOVED_TARGET_DISTANCE_SQUARED;
        if (!controlsNavigation
                || recruit.getNavigation().isDone()
                || targetMoved) {
            startPath(recruit, walkTarget, targetPos);
        } else {
            BrainUtil.setOrClearMemory(recruit.getBrain(), MemoryModuleType.PATH,
                    recruit.getNavigation().getPath());
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        stopOwnedNavigation(recruit);
    }

    private void startPath(
            GalacticRecruitEntity recruit,
            WalkTarget walkTarget,
            BlockPos targetPos
    ) {
        lastTargetPos = targetPos.immutable();
        boolean wasControllingNavigation = controlsNavigation;
        // The coordinate overload owns path construction and installs its move-control state in
        // one operation. Re-submitting a precomputed Path can leave that state idle on 26.2.
        controlsNavigation = recruit.getNavigation().moveTo(
                targetPos.getX() + 0.5D,
                targetPos.getY(),
                targetPos.getZ() + 0.5D,
                walkTarget.getSpeedModifier());
        Path path = controlsNavigation ? recruit.getNavigation().getPath() : null;
        if (path == null) {
            if (wasControllingNavigation) {
                recruit.getNavigation().stop();
            }
            controlsNavigation = false;
            BrainUtil.clearMemory(recruit, MemoryModuleType.PATH);
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.NAVIGATION_RESULT,
                    new RecruitNavigationResult(
                            targetPos, RecruitNavigationResult.State.UNREACHABLE, recruit.tickCount));
            return;
        }
        BrainUtil.setOrClearMemory(recruit.getBrain(), MemoryModuleType.PATH,
                path);
        BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.NAVIGATION_RESULT,
                new RecruitNavigationResult(
                        targetPos, RecruitNavigationResult.State.MOVING, recruit.tickCount));
    }

    private void stopOwnedNavigation(GalacticRecruitEntity recruit) {
        if (controlsNavigation) {
            recruit.getNavigation().stop();
        }
        releaseWithoutStopping(recruit);
    }

    private void releaseWithoutStopping(GalacticRecruitEntity recruit) {
        controlsNavigation = false;
        lastTargetPos = null;
        BrainUtil.clearMemory(recruit, MemoryModuleType.PATH);
    }
}
