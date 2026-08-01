package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.army.ArmyBehaviorDecision;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyTacticalDecision;
import galacticwars.clonewars.army.ArmyTacticalIntent;
import galacticwars.clonewars.army.ArmyTacticalPlanner;
import galacticwars.clonewars.combat.BlasterHeatPolicy;
import galacticwars.clonewars.combat.BlasterItem;
import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.recruitment.RecruitDuty;
import galacticwars.clonewars.registry.ModItems;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
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

/** Executes live squad combat after the threat sensor has populated attack memory. */
public final class ArmyCombatBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int MELEE_ATTACK_COOLDOWN_TICKS = 20;
    private static final int BOW_ATTACK_COOLDOWN_TICKS = 24;
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2)
            .hasMemory(ArmyBrainMemoryTypes.ARMY_STATE)
            .hasMemory(MemoryModuleType.ATTACK_TARGET);

    private BlasterHeatPolicy.BlasterHeatState blasterHeat = BlasterHeatPolicy.BlasterHeatState.ready();
    private int attackCooldownTicks;

    public ArmyCombatBehaviour() {
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
        BrainUtil.clearMemories(recruit,
                ArmyBrainMemoryTypes.PATH_STATUS,
                MemoryModuleType.WALK_TARGET);
        recruit.setAggressive(true);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        if (!(recruit.level() instanceof ServerLevel level)) {
            return;
        }
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        LivingEntity target = BrainUtil.getMemory(recruit, MemoryModuleType.ATTACK_TARGET);
        if (state == null || !ArmyBrainSupport.canEngageGroupTarget(recruit, state, target)) {
            clearCombatTarget(recruit);
            return;
        }
        ArmyTacticalDecision tactical = ArmyTacticalPlanner.plan(
                ArmyBehaviorDecision.attack(target.getUUID(), "active_group_target"),
                recruit.getRecruitVitals(),
                state.fallbackPosition());
        if (tactical.intent() != ArmyTacticalIntent.EXECUTE_ORDER) {
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE, state.withSelectedTarget(null));
            clearCombatTarget(recruit);
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        ArmyGroupRecord group = data.armyGroup(state.group().id()).orElse(null);
        if (group == null) {
            ArmyBrainSupport.clearGroupExecution(recruit);
            return;
        }

        attackCooldownTicks = Math.max(0, attackCooldownTicks - 1);
        blasterHeat = BlasterHeatPolicy.tick(blasterHeat);
        recruit.setTarget(target);
        recruit.setAggressive(true);
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));

        ItemStack weapon = recruit.getMainHandItem();
        double range = Math.max(4.0D, recruit.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (FactionRangedWeaponService.supportsRecruitRangedCombat(weapon)) {
            boolean hasShot = recruit.distanceToSqr(target) <= range * range
                    && recruit.getSensing().hasLineOfSight(target)
                    && ArmyBrainSupport.canUseRangedFire(recruit, level, group, target);
            if (!hasShot) {
                holdOrRepositionRanged(recruit, state, target);
                return;
            }
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            if (weapon.getItem() instanceof BlasterItem blaster) {
                if (attackCooldownTicks == 0 && BlasterHeatPolicy.canFire(blasterHeat)) {
                    blaster.fireAt(level, recruit, target, weapon);
                    blasterHeat = BlasterHeatPolicy.afterShot(blasterHeat);
                    attackCooldownTicks = ArmyBrainSupport.coordinatedCooldownTicks(
                            recruit, level, data, group, BlasterHeatPolicy.SHOT_COOLDOWN_TICKS);
                } else {
                    holdOrRepositionRanged(recruit, state, target);
                }
                return;
            }
            if (weapon.is(ModItems.NIGHTSISTER_BOW.get()) && attackCooldownTicks == 0) {
                FactionRangedWeaponService.fireNightsisterBow(level, recruit, target, weapon);
                attackCooldownTicks = ArmyBrainSupport.coordinatedCooldownTicks(
                        recruit, level, data, group, BOW_ATTACK_COOLDOWN_TICKS);
            }
            return;
        }
        meleeOrClose(recruit, level, data, group, state, target);
    }

    private static void holdOrRepositionRanged(
            GalacticRecruitEntity recruit,
            ArmyBrainState state,
            LivingEntity target
    ) {
        if (ArmyBrainSupport.shouldMaintainFormation(state, target)) {
            var anchor = ArmyBrainSupport.formationAnchor(state);
            BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new BlockPos(anchor.x(), anchor.y(), anchor.z()), 1.0F,
                            state.group().effectiveTactics().tightFormation() ? 1 : 2));
            return;
        }
        double range = Math.max(
                4.0D,
                recruit.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (recruit.distanceToSqr(target) <= range * range
                && recruit.getSensing().hasLineOfSight(target)) {
            if (!RecruitAiCadence.shouldRecomputeArmyCover(recruit.tickCount)) {
                return;
            }
            BrainUtil.setMemory(
                    recruit,
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(
                            RecruitCombatMovement.coverOrDodge(recruit, target),
                            1.1F,
                            0));
            return;
        }
        int preferredRange = Math.max(4,
                (int) Math.floor(recruit.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.7D));
        BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, 0.95F, preferredRange));
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                ArmyBrainMemoryTypes.PATH_STATUS,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        recruit.setTarget(null);
        recruit.setAggressive(false);
        blasterHeat = BlasterHeatPolicy.BlasterHeatState.ready();
        attackCooldownTicks = 0;
    }

    private void meleeOrClose(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            KingdomSavedData data,
            ArmyGroupRecord group,
            ArmyBrainState state,
            LivingEntity target
    ) {
        if (recruit.isWithinMeleeAttackRange(target)
                && recruit.getSensing().hasLineOfSight(target)) {
            BrainUtil.clearMemory(recruit, MemoryModuleType.WALK_TARGET);
            if (attackCooldownTicks == 0) {
                recruit.swing(InteractionHand.MAIN_HAND);
                recruit.doHurtTarget(level, target);
                attackCooldownTicks = ArmyBrainSupport.coordinatedCooldownTicks(
                        recruit, level, data, group, MELEE_ATTACK_COOLDOWN_TICKS);
            }
            return;
        }
        if (ArmyBrainSupport.shouldMaintainFormation(state, target)) {
            var anchor = ArmyBrainSupport.formationAnchor(state);
            BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new BlockPos(anchor.x(), anchor.y(), anchor.z()), 1.0F,
                            state.group().effectiveTactics().tightFormation() ? 1 : 2));
            return;
        }
        BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, 1.05F, 1));
    }

    private static boolean canFight(GalacticRecruitEntity recruit) {
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        LivingEntity target = BrainUtil.getMemory(recruit, MemoryModuleType.ATTACK_TARGET);
        return state != null
                && recruit.getRecruitDuty() != RecruitDuty.WORKER
                && !recruit.isHazardAvoidanceActive()
                && ArmyBrainSupport.canEngageGroupTarget(recruit, state, target);
    }

    private static void clearCombatTarget(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET);
        recruit.setTarget(null);
        recruit.setAggressive(false);
    }
}
