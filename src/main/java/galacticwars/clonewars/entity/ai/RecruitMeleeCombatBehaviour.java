package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

public final class RecruitMeleeCombatBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
            .hasMemory(MemoryModuleType.ATTACK_TARGET);
    private int attackCooldownTicks;

    public RecruitMeleeCombatBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        return canFight(recruit);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return canFight(recruit);
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
        recruit.setAggressive(true);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        LivingEntity target = target(recruit);
        if (target == null || !(recruit.level() instanceof ServerLevel level)) {
            return;
        }
        attackCooldownTicks = Math.max(0, attackCooldownTicks - 1);
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET,
                new EntityTracker(target, true));
        if (recruit.isWithinMeleeAttackRange(target)
                && recruit.getSensing().hasLineOfSight(target)) {
            BrainUtil.clearMemories(
                    recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
            if (attackCooldownTicks == 0) {
                recruit.swing(InteractionHand.MAIN_HAND);
                recruit.doHurtTarget(level, target);
                attackCooldownTicks = 20;
            }
            return;
        }
        BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, 1.05F, 1));
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
        recruit.setAggressive(false);
    }

    private static boolean canFight(GalacticRecruitEntity recruit) {
        LivingEntity target = target(recruit);
        return recruit.getRecruitDuty() == galacticwars.clonewars.recruitment.RecruitDuty.SOLDIER
                && !recruit.hasAuthoritativeArmyGroup()
                && recruit.canUseLocalAttackTarget(target)
                && !FactionRangedWeaponService.supportsRecruitRangedCombat(
                        recruit.getMainHandItem());
    }

    private static LivingEntity target(GalacticRecruitEntity recruit) {
        return BrainUtil.getMemory(recruit, MemoryModuleType.ATTACK_TARGET);
    }
}
