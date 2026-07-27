package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.combat.BlasterHeatPolicy;
import galacticwars.clonewars.combat.BlasterItem;
import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.recruitment.RecruitDuty;
import galacticwars.clonewars.registry.ModItems;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.library.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Executes local attack orders for ungrouped ranged recruits. */
public final class RecruitRangedCombatBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
            .hasMemory(MemoryModuleType.ATTACK_TARGET);
    private BlasterHeatPolicy.BlasterHeatState heat =
            BlasterHeatPolicy.BlasterHeatState.ready();
    private int bowCooldownTicks;

    public RecruitRangedCombatBehaviour() {
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
        heat = BlasterHeatPolicy.tick(heat);
        bowCooldownTicks = Math.max(0, bowCooldownTicks - 1);
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET,
                new EntityTracker(target, true));

        double range = Math.max(4.0D, recruit.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (recruit.distanceToSqr(target) > range * range
                || !recruit.getSensing().hasLineOfSight(target)) {
            BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(target, 1.05F, 1));
            return;
        }

        BrainUtil.clearMemories(
                recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
        ItemStack weapon = recruit.getMainHandItem();
        if (weapon.getItem() instanceof BlasterItem blaster && BlasterHeatPolicy.canFire(heat)) {
            blaster.fireAt(level, recruit, target, weapon);
            heat = BlasterHeatPolicy.afterShot(heat);
        } else if (weapon.is(ModItems.NIGHTSISTER_BOW.get()) && bowCooldownTicks == 0) {
            FactionRangedWeaponService.fireNightsisterBow(level, recruit, target, weapon);
            bowCooldownTicks = 24;
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
        recruit.setAggressive(false);
        heat = BlasterHeatPolicy.BlasterHeatState.ready();
        bowCooldownTicks = 0;
    }

    private static boolean canFight(GalacticRecruitEntity recruit) {
        LivingEntity target = target(recruit);
        return recruit.getRecruitDuty() == RecruitDuty.SOLDIER
                && !recruit.hasAuthoritativeArmyGroup()
                && recruit.canUseLocalAttackTarget(target)
                && FactionRangedWeaponService.supportsRecruitRangedCombat(
                        recruit.getMainHandItem());
    }

    private static LivingEntity target(GalacticRecruitEntity recruit) {
        return BrainUtil.getMemory(recruit, MemoryModuleType.ATTACK_TARGET);
    }
}
