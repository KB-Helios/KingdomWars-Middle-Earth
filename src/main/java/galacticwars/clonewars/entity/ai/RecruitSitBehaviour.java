package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

public final class RecruitSitBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    public RecruitSitBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        return shouldSit(recruit);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return shouldSit(recruit);
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
        recruit.getMoveControl().setWait();
        recruit.setInSittingPose(true);
        stopHorizontalMovement(recruit);
        recruit.setAggressive(false);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(
                recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
        recruit.getMoveControl().setWait();
        recruit.setInSittingPose(true);
        stopHorizontalMovement(recruit);
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        recruit.setInSittingPose(false);
    }

    private static boolean shouldSit(GalacticRecruitEntity recruit) {
        return recruit.isTame()
                && recruit.isOrderedToSit()
                && !recruit.hasAuthoritativeArmyGroup();
    }

    private static void stopHorizontalMovement(GalacticRecruitEntity recruit) {
        recruit.setDeltaMovement(
                0.0D,
                recruit.onGround() ? 0.0D : recruit.getDeltaMovement().y(),
                0.0D);
    }
}
