package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jspecify.annotations.Nullable;

/** Collects authorized nearby item entities into physical recruit cargo. */
public final class RecruitItemPickupBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private @Nullable ItemEntity target;

    public RecruitItemPickupBehaviour() {
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
        if (recruit.isHazardAvoidanceActive()) {
            return false;
        }
        target = recruit.nearbyRecruitPickupTarget().orElse(null);
        return target != null;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return !recruit.isHazardAvoidanceActive()
                && target != null
                && target.isAlive()
                && recruit.canCollectRecruitItem(target);
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        publishTarget(recruit);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        if (target == null) {
            return;
        }
        if (recruit.distanceToSqr(target) <= 2.25D) {
            recruit.collectRecruitItem(target);
            return;
        }
        if (recruit.tickCount % 10 == 0) {
            publishTarget(recruit);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(
                recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        target = null;
    }

    private void publishTarget(GalacticRecruitEntity recruit) {
        if (target == null) {
            return;
        }
        BrainUtil.setMemory(
                recruit,
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(target, true));
        BrainUtil.setMemory(
                recruit,
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, 0.9F, 1));
    }
}
