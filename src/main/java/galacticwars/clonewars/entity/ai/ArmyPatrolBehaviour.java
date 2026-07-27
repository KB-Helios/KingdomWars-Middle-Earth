package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyPatrolOrderDecision;
import galacticwars.clonewars.army.ArmyPatrolOrderPlanner;
import galacticwars.clonewars.army.ArmyPatrolPlan;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Advances a persisted route only from its loaded commander; members consume the updated order. */
public final class ArmyPatrolBehaviour extends ExtendedBehaviour<GalacticRecruitEntity> {
    public ArmyPatrolBehaviour() {
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
        if (!(recruit.level() instanceof ServerLevel level)) {
            return;
        }
        if (BrainUtil.hasMemory(recruit, MemoryModuleType.ATTACK_TARGET)) {
            return;
        }
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        if (state == null || !state.commander()) {
            return;
        }
        ArmyGroupRecord group = state.group();
        if (group.order().type() != ArmyCommandType.PATROL_ROUTE
                || group.patrolRoute().size() < 2
                || group.patrolRoute().stream().anyMatch(waypoint -> !waypoint.dimensionId()
                        .equals(level.dimension().identifier().toString()))) {
            return;
        }
        Optional<ArmyPatrolOrderDecision> next = ArmyPatrolOrderPlanner.advance(
                group, ArmyBrainSupport.position(recruit));
        if (next.isEmpty()) {
            return;
        }
        ArmyPatrolOrderDecision decision = next.orElseThrow();
        ArmyPatrolPlan currentPlan = group.effectivePatrolPlan().orElse(null);
        if (decision.nextOrder().equals(group.order()) && decision.nextPlan().equals(currentPlan)) {
            return;
        }
        ArmyGroupRecord updated = group.withPatrolProgressAndOrder(decision.nextPlan(), decision.nextOrder());
        if (KingdomSavedData.get(level).replaceArmyGroup(updated, group.simulation().revision())) {
            ArmyBrainState refreshed = ArmyBrainSupport.resolveState(recruit, level, state);
            if (refreshed != null) {
                BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE, refreshed);
            }
        }
    }
}
