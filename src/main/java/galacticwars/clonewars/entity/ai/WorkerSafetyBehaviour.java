package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jspecify.annotations.Nullable;

/** Interrupts worker automation and retreats without mutating carried resources. */
public final class WorkerSafetyBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int SAFE_TICKS_TO_RESUME = 100;
    private static final double ACTIVE_THREAT_DISTANCE_SQUARED = 24.0D * 24.0D;

    private @Nullable LivingEntity threat;
    private int threatFreeTicks;

    public WorkerSafetyBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(
            ServerLevel level, GalacticRecruitEntity recruit
    ) {
        threat = BrainUtil.getMemory(recruit, MemoryModuleType.HURT_BY_ENTITY);
        return recruit.shouldUseWorkerSafety(threat);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return recruit.isWorkerSafetyRetreating() && threatFreeTicks < SAFE_TICKS_TO_RESUME;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        threatFreeTicks = 0;
        recruit.beginWorkerSafetyRetreat(threat);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        LivingEntity remembered = BrainUtil.getMemory(recruit, MemoryModuleType.HURT_BY_ENTITY);
        if (remembered != null && remembered.isAlive() && remembered.level() == recruit.level()) {
            threat = remembered;
        }
        boolean activeThreat = recruit.hurtTime > 0
                || threat != null
                && threat.isAlive()
                && threat.level() == recruit.level()
                && recruit.distanceToSqr(threat) <= ACTIVE_THREAT_DISTANCE_SQUARED;
        threatFreeTicks = activeThreat ? 0 : threatFreeTicks + 1;
        recruit.maintainWorkerSafetyRetreat();
        if (threatFreeTicks >= SAFE_TICKS_TO_RESUME) {
            recruit.resumeWorkerAfterSafety();
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        if (recruit.isWorkerSafetyRetreating() && threatFreeTicks >= SAFE_TICKS_TO_RESUME) {
            recruit.resumeWorkerAfterSafety();
        }
        threat = null;
        threatFreeTicks = 0;
    }
}
