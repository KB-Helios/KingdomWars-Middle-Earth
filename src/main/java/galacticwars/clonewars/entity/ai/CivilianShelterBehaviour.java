package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Returns natural civilians to their outpost home at night or when hostile troops approach. */
public final class CivilianShelterBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final double DANGER_RADIUS = 18.0D;

    public CivilianShelterBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity civilian) {
        return shouldShelter(civilian);
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity civilian) {
        return shouldShelter(civilian);
    }

    @Override
    protected void start(GalacticRecruitEntity civilian) {
        BrainUtil.clearMemories(civilian,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        civilian.setTarget(null);
        setShelterTargets(civilian);
    }

    @Override
    protected void tick(GalacticRecruitEntity civilian) {
        BrainUtil.setMemory(civilian, MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(civilian.getHomePosition()));
        if (civilian.getNavigation().isDone()) {
            setShelterTargets(civilian);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity civilian) {
        BrainUtil.clearMemories(civilian,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
    }

    private static boolean shouldShelter(GalacticRecruitEntity civilian) {
        return civilian.isNaturalFactionCivilian()
                && civilian.hasHome()
                && !civilian.blockPosition().closerThan(civilian.getHomePosition(), 3.0D)
                && (civilian.level().isDarkOutside()
                || civilian.hasActiveOutpostAlert()
                || dangerNearby(civilian));
    }

    private static void setShelterTargets(GalacticRecruitEntity civilian) {
        var home = civilian.getHomePosition();
        BrainUtil.setMemory(civilian, MemoryModuleType.LOOK_TARGET,
                new BlockPosTracker(home));
        BrainUtil.setMemory(civilian, MemoryModuleType.WALK_TARGET,
                new WalkTarget(home, 1.1F, 2));
    }

    private static boolean dangerNearby(GalacticRecruitEntity civilian) {
        return !civilian.level().getEntitiesOfClass(
                GalacticRecruitEntity.class,
                civilian.getBoundingBox().inflate(DANGER_RADIUS),
                candidate -> candidate != civilian
                        && candidate.getServiceBranch() == NpcServiceBranch.MILITARY
                        && civilian.isHostileFactionRecruit(candidate)).isEmpty();
    }
}
