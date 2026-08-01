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
import org.jspecify.annotations.Nullable;

/** Safety-first bounded escape that publishes a walk target but never navigates directly. */
public final class RecruitHazardAvoidanceBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int SAFE_TICKS_TO_STOP = 20;

    private @Nullable BlockPos escapeTarget;
    private int safeTicks;

    public RecruitHazardAvoidanceBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(
            ServerLevel level,
            GalacticRecruitEntity recruit
    ) {
        escapeTarget = recruit.findRecruitHazardEscapeTarget().orElse(null);
        return escapeTarget != null;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return recruit.isHazardAvoidanceActive()
                && escapeTarget != null
                && safeTicks < SAFE_TICKS_TO_STOP;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        safeTicks = 0;
        recruit.setHazardAvoidanceActive(true);
        publishEscapeTarget(recruit);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        if (recruit.isInRecruitHazard()) {
            safeTicks = 0;
            escapeTarget = recruit.findRecruitHazardEscapeTarget()
                    .orElse(escapeTarget);
        } else {
            safeTicks++;
        }
        if (escapeTarget != null && recruit.tickCount % 5 == 0) {
            publishEscapeTarget(recruit);
        }
        if (safeTicks >= SAFE_TICKS_TO_STOP) {
            recruit.setHazardAvoidanceActive(false);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        recruit.setHazardAvoidanceActive(false);
        BrainUtil.clearMemories(
                recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.PATH);
        escapeTarget = null;
        safeTicks = 0;
    }

    private void publishEscapeTarget(GalacticRecruitEntity recruit) {
        BrainUtil.setMemory(
                recruit,
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(escapeTarget, 1.2F, 0));
    }
}
