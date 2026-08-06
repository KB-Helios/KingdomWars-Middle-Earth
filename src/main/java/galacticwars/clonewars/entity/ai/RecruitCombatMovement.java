package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Cover-first, deterministic lateral repositioning for ranged recruit combat. */
final class RecruitCombatMovement {
    private RecruitCombatMovement() {
    }

    static BlockPos coverOrDodge(
            GalacticRecruitEntity recruit,
            LivingEntity target
    ) {
        if (!(recruit.level() instanceof ServerLevel level)) {
            return recruit.blockPosition();
        }
        BlockPos origin = recruit.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (int radius = 2; radius <= 5; radius++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = origin.relative(direction, radius).immutable();
                if (!candidates.contains(pos)) {
                    candidates.add(pos);
                }
                BlockPos diag = origin.relative(direction, radius)
                        .relative(direction.getClockWise(), radius / 2).immutable();
                if (!candidates.contains(diag)) {
                    candidates.add(diag);
                }
            }
        }
        List<BlockPos> ranked = rankCandidates(
                candidates,
                origin,
                candidate -> safeStand(level, candidate),
                candidate -> hasCover(level, candidate, target));
        if (!ranked.isEmpty()) {
            return ranked.getFirst();
        }
        Vec3 away = recruit.position().subtract(target.position());
        Vec3 lateral = new Vec3(-away.z, 0.0D, away.x);
        if (lateral.lengthSqr() < 1.0E-4D) {
            lateral = new Vec3(1.0D, 0.0D, 0.0D);
        }
        if ((recruit.getId() + recruit.tickCount / 20 & 1) != 0) {
            lateral = lateral.scale(-1.0D);
        }
        return BlockPos.containing(recruit.position().add(lateral.normalize().scale(3.0D)));
    }

    static List<BlockPos> rankCandidates(
            List<BlockPos> candidates,
            BlockPos origin,
            Predicate<BlockPos> safeStand,
            Predicate<BlockPos> hasCover
    ) {
        LinkedHashSet<BlockPos> uniqueCandidates = new LinkedHashSet<>();
        for (BlockPos candidate : candidates) {
            uniqueCandidates.add(candidate.immutable());
        }
        List<RankedCandidate> ranked = new ArrayList<>();
        for (BlockPos candidate : uniqueCandidates) {
            if (safeStand.test(candidate)) {
                ranked.add(new RankedCandidate(
                        candidate,
                        hasCover.test(candidate),
                        candidate.distSqr(origin)));
            }
        }
        ranked.sort(Comparator
                .comparing(RankedCandidate::covered)
                .reversed()
                .thenComparingDouble(RankedCandidate::distanceSquared)
                .thenComparingLong(candidate -> candidate.position().asLong()));
        return ranked.stream()
                .map(RankedCandidate::position)
                .toList();
    }

    private static boolean safeStand(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos).getFluidState().isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(
                        level, pos.below(), Direction.UP);
    }

    private static boolean hasCover(
            ServerLevel level,
            BlockPos candidate,
            LivingEntity target
    ) {
        Vec3 start = Vec3.atBottomCenterOf(candidate).add(0.0D, 1.4D, 0.0D);
        return level.clip(new ClipContext(
                start,
                target.getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                target)).getType() == HitResult.Type.BLOCK;
    }

    private record RankedCandidate(
            BlockPos position,
            boolean covered,
            double distanceSquared
    ) {
    }
}
