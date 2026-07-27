package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Keeps vanilla Mob target state synchronized with SmartBrainLib fight memory. */
public final class RecruitInvalidateAttackTargetBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
            .hasMemory(MemoryModuleType.ATTACK_TARGET);

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        LivingEntity target = BrainUtil.getMemory(recruit, MemoryModuleType.ATTACK_TARGET);
        if (!recruit.canUseLocalAttackTarget(target)
                && !ArmyBrainSupport.isCurrentGroupTarget(recruit, target)) {
            BrainUtil.clearMemories(recruit,
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.PATH);
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }
        recruit.setTarget(target);
    }
}
