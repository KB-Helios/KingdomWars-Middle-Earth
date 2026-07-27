package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.registry.ModSensorTypes;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Projects the authoritative SavedData squad state into a loaded recruit brain. */
public final class ArmyGroupStateSensor extends ExtendedSensor<GalacticRecruitEntity> {
    private boolean cleanedInvalidGroup;

    public ArmyGroupStateSensor() {
        scanRate(2);
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.ARMY_GROUP_STATE.get();
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return List.of(
                ArmyBrainMemoryTypes.ARMY_STATE,
                ArmyBrainMemoryTypes.PATH_STATUS,
                ArmyBrainMemoryTypes.MARCH_STATE,
                ArmyBrainMemoryTypes.NAVIGATION_RESULT);
    }

    @Override
    protected void doTick(ServerLevel level, GalacticRecruitEntity recruit) {
        ArmyBrainState previous = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        ArmyBrainState next = ArmyBrainSupport.resolveState(recruit, level, previous);
        if (next == null) {
            if (!cleanedInvalidGroup && (previous != null || recruit.hasAuthoritativeArmyGroup())) {
                ArmyBrainSupport.clearGroupExecution(recruit);
            }
            cleanedInvalidGroup = recruit.hasAuthoritativeArmyGroup();
            return;
        }

        boolean changedGroup = previous == null || !previous.group().id().equals(next.group().id());
        boolean changedOrder = previous == null
                || !previous.group().order().equals(next.group().order());
        if (changedGroup) {
            ArmyBrainSupport.clearGroupExecution(recruit);
        }
        cleanedInvalidGroup = false;
        BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE, next);
        int slot = next.group().effectiveFormationSlotAssignments().stream()
                .filter(assignment -> assignment.memberId().equals(recruit.getUUID()))
                .mapToInt(assignment -> assignment.slotIndex())
                .findFirst().orElse(-1);
        var march = next.group().simulation().marchState();
        BrainUtil.setMemory(recruit, ArmyBrainMemoryTypes.MARCH_STATE, new ArmyMarchMemory(
                next.group().id(),
                next.group().order().type(),
                slot,
                next.group().simulation().anchor().blockPosition(),
                march.phase(),
                march.cohesionPercent(),
                next.group().order().targetPosition().map(location -> location.blockPosition()).orElse(null),
                next.group().order().targetEntityId().orElse(null)));
        if (changedGroup || changedOrder) {
            ArmyThreatSensor.refreshAuthorizedThreat(level, recruit, next);
        }
    }
}
