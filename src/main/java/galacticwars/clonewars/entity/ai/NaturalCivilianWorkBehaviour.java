package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Keeps natural faction civilians visibly occupied at their settlement during daylight. */
public final class NaturalCivilianWorkBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    public NaturalCivilianWorkBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity civilian) {
        return canWork(civilian);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity civilian) {
        return canWork(civilian);
    }

    @Override
    protected void start(GalacticRecruitEntity civilian) {
        BrainUtil.clearMemories(civilian,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        var workstation = civilian.naturalWorkstationPosition();
        if (civilian.blockPosition().closerThan(workstation, 2.5D)) {
            civilian.tryProduceNaturalSettlementSupplies();
        } else {
            setWalkTarget(civilian);
        }
    }

    @Override
    protected void tick(GalacticRecruitEntity civilian) {
        var workstation = civilian.naturalWorkstationPosition();
        BrainUtil.setMemory(civilian, MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(workstation));
        if (civilian.blockPosition().closerThan(workstation, 2.5D)) {
            BrainUtil.clearMemories(
                    civilian, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
            civilian.tryProduceNaturalSettlementSupplies();
        } else if (civilian.getNavigation().isDone()) {
            setWalkTarget(civilian);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity civilian) {
        BrainUtil.clearMemories(civilian,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
    }

    private static boolean canWork(GalacticRecruitEntity civilian) {
        return civilian.isNaturalFactionCivilian()
                && civilian.hasHome()
                && civilian.getWorkerProfession().isPresent()
                && civilian.level().isBrightOutside();
    }

    private static void setWalkTarget(GalacticRecruitEntity civilian) {
        var target = civilian.naturalWorkstationPosition();
        BrainUtil.setMemory(civilian, MemoryModuleType.WALK_TARGET,
                new WalkTarget(target, 0.85F, 2));
    }
}
