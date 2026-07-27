package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.registry.ModSensorTypes;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyPatrolEnemyPolicy;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Selects bounded, server-authoritative combat targets for live squad members. */
public final class ArmyThreatSensor extends ExtendedSensor<GalacticRecruitEntity> {
    public ArmyThreatSensor() {
        scanRate(10);
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.ARMY_THREAT.get();
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return List.of(
                ArmyBrainMemoryTypes.ARMY_STATE,
                MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void doTick(ServerLevel level, GalacticRecruitEntity recruit) {
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        if (state == null) {
            return;
        }
        refreshAuthorizedThreat(level, recruit, state);
    }

    /**
     * Installs combat memory at the same authority boundary that projects a new group/order.
     *
     * <p>The sensor continues to call this on its normal scan cadence for retargeting. The
     * group-state sensor also calls it once when authority changes so a freshly authorized
     * attack cannot spend a sensor interval with only the legacy command state installed.</p>
     */
    static void refreshAuthorizedThreat(
            ServerLevel level,
            GalacticRecruitEntity recruit,
            ArmyBrainState state
    ) {
        if (state.group().order().type() == ArmyCommandType.PATROL_ROUTE
                && state.group().effectivePatrolPlan()
                        .map(plan -> plan.enemyPolicy() == ArmyPatrolEnemyPolicy.RETREAT_FROM_HOSTILES)
                        .orElse(false)) {
            LivingEntity threat = ArmyBrainSupport.selectRetreatThreat(recruit, level, state);
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE,
                    state.withRetreatThreat(threat == null ? null : threat.getUUID()));
            BrainUtil.clearMemory(recruit, MemoryModuleType.ATTACK_TARGET);
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }
        LivingEntity target = ArmyBrainSupport.selectTarget(recruit, level, state);
        if (target == null) {
            BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE,
                    state.withSelectedTarget(null));
            BrainUtil.clearMemory(recruit, MemoryModuleType.ATTACK_TARGET);
            recruit.setTarget(null);
            recruit.setAggressive(false);
            return;
        }
        BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE,
                state.withSelectedTarget(target.getUUID()));
        BrainUtil.setTargetOfEntity(recruit, target);
        recruit.setTarget(target);
    }
}
