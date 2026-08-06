package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

/** Eats physical cargo food or publishes a settlement demand while off duty. */
public final class RecruitSelfCareBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int FOOD_SUPPLY_REQUEST_COOLDOWN = 100;
    private int cooldownTicks;

    public RecruitSelfCareBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }
        return RecruitAiCadence.shouldCheckSelfCare(recruit.tickCount)
                && recruit.shouldUseRecruitSelfCare();
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return false;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        recruit.performRecruitSelfCare();
        cooldownTicks = FOOD_SUPPLY_REQUEST_COOLDOWN;
    }
}
