package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

/**
 * Opens claim-authorized wooden doors and fence gates on the active path without
 * competing with the sole walk-target navigation controller.
 */
public final class RecruitDoorInteractionBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int MINIMUM_OPEN_TICKS = 10;
    private static final double CLOSE_DISTANCE_SQUARED = 9.0D;

    private final Map<BlockPos, Integer> openedEntries = new LinkedHashMap<>();

    public RecruitDoorInteractionBehaviour() {
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
        closePassedEntries(level, recruit);
        Path path = recruit.getNavigation().getPath();
        if (path == null || path.isDone()) {
            return;
        }
        BlockPos next = path.getNextNodePos();
        for (BlockPos candidate : ListCandidates.around(next)) {
            BlockState state = level.getBlockState(candidate);
            if (!isClosedSupportedEntry(state)
                    || !recruit.canInteractWithRecruitDoor(candidate)) {
                continue;
            }
            setOpen(level, recruit, candidate, state, true);
            openedEntries.putIfAbsent(candidate.immutable(), recruit.tickCount);
        }
    }

    @Override
    protected void stop(GalacticRecruitEntity recruit) {
        if (recruit.level() instanceof ServerLevel level) {
            closeAll(level, recruit);
        }
    }

    private void closePassedEntries(ServerLevel level, GalacticRecruitEntity recruit) {
        for (BlockPos pos : new ArrayList<>(openedEntries.keySet())) {
            int openedAt = openedEntries.get(pos);
            if (recruit.tickCount - openedAt < MINIMUM_OPEN_TICKS
                    || recruit.distanceToSqr(
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
                    <= CLOSE_DISTANCE_SQUARED
                    || hasEntityInEntry(level, recruit, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (isOpenSupportedEntry(state)) {
                setOpen(level, recruit, pos, state, false);
            }
            openedEntries.remove(pos);
        }
    }

    private void closeAll(ServerLevel level, GalacticRecruitEntity recruit) {
        for (BlockPos pos : new ArrayList<>(openedEntries.keySet())) {
            BlockState state = level.getBlockState(pos);
            if (isOpenSupportedEntry(state) && !hasEntityInEntry(level, recruit, pos)) {
                setOpen(level, recruit, pos, state, false);
            }
        }
        openedEntries.clear();
    }

    private static boolean hasEntityInEntry(
            ServerLevel level,
            GalacticRecruitEntity recruit,
            BlockPos pos
    ) {
        return !level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(pos).inflate(0.2D),
                entity -> entity != recruit && entity.isAlive()).isEmpty();
    }

    private static boolean isClosedSupportedEntry(BlockState state) {
        return state.hasProperty(BlockStateProperties.OPEN)
                && !state.getValue(BlockStateProperties.OPEN)
                && (state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS));
    }

    private static boolean isOpenSupportedEntry(BlockState state) {
        return state.hasProperty(BlockStateProperties.OPEN)
                && state.getValue(BlockStateProperties.OPEN)
                && (state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS));
    }

    private static void setOpen(
            ServerLevel level,
            GalacticRecruitEntity recruit,
            BlockPos pos,
            BlockState state,
            boolean open
    ) {
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(recruit, level, state, pos, open);
            return;
        }
        level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 10);
    }

    private static final class ListCandidates {
        private ListCandidates() {
        }

        private static java.util.List<BlockPos> around(BlockPos pos) {
            return java.util.List.of(pos, pos.above(), pos.below());
        }
    }
}
