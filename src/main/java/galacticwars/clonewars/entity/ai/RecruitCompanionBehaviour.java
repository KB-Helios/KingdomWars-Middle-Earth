package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jspecify.annotations.Nullable;

public final class RecruitCompanionBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final double COMFORT_DISTANCE_SQUARED = 16.0D;
    private static final double TELEPORT_DISTANCE_SQUARED = 256.0D;

    private final double speedModifier;
    private @Nullable LivingEntity owner;

    public RecruitCompanionBehaviour(double speedModifier) {
        this.speedModifier = speedModifier;
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GalacticRecruitEntity recruit) {
        if (recruit.isHazardAvoidanceActive()) {
            return false;
        }
        owner = recruit.getRecruitOwner().orElse(null);
        return owner != null
                && recruit.shouldUseCompanionAi()
                && recruit.distanceToSqr(findCompanionAnchor(recruit, owner))
                        > COMFORT_DISTANCE_SQUARED;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return !recruit.isHazardAvoidanceActive()
                && owner != null
                && owner.isAlive()
                && recruit.shouldUseCompanionAi()
                && recruit.distanceToSqr(findCompanionAnchor(recruit, owner))
                        > COMFORT_DISTANCE_SQUARED;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET);
        setCompanionTargets(recruit);
    }

    @Override
    protected void tick(GalacticRecruitEntity recruit) {
        if (owner == null) {
            return;
        }
        Vec3 anchor = findCompanionAnchor(recruit, owner);
        double distanceToSqr = recruit.distanceToSqr(anchor);
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET,
                new EntityTracker(owner, true));
        if (distanceToSqr > TELEPORT_DISTANCE_SQUARED && !recruit.level().isClientSide()) {
            recruit.teleportTo(anchor.x(), anchor.y(), anchor.z());
            BrainUtil.clearMemories(
                    recruit, MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH);
            return;
        }
        if (recruit.tickCount % 10 == 0) {
            setCompanionTargets(recruit);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.PATH);
        owner = null;
    }

    private static Vec3 findCompanionAnchor(
            GalacticRecruitEntity recruit, LivingEntity owner
    ) {
        Vec3 look = owner.getLookAngle();
        Vec3 forward = new Vec3(look.x(), 0.0D, look.z());
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            forward = forward.normalize();
        }
        Vec3 right = new Vec3(-forward.z(), 0.0D, forward.x()).normalize();
        double sideOffset = (recruit.getId() & 1) == 0 ? 1.75D : -1.75D;
        return owner.position()
                .add(forward.scale(-2.5D))
                .add(right.scale(sideOffset));
    }

    private void setCompanionTargets(GalacticRecruitEntity recruit) {
        if (owner == null) {
            return;
        }
        BrainUtil.setMemory(recruit, MemoryModuleType.LOOK_TARGET,
                new EntityTracker(owner, true));
        BrainUtil.setMemory(recruit, MemoryModuleType.WALK_TARGET,
                new WalkTarget(findCompanionAnchor(recruit, owner),
                        (float)speedModifier, 2));
    }
}
