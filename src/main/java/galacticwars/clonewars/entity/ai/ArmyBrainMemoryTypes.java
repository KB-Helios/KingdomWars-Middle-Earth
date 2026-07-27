package galacticwars.clonewars.entity.ai;

import java.util.Optional;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * Recruit-local squad memories.
 *
 * <p>These memory types deliberately have no codec and are registered by the
 * SmartBrain builder from the squad sensors. SavedData remains the durable
 * authority for every army order; the brain only keeps a bounded live-world
 * projection of it.</p>
 */
public final class ArmyBrainMemoryTypes {
    public static final MemoryModuleType<ArmyBrainState> ARMY_STATE =
            new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<ArmyPathStatus> PATH_STATUS =
            new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<ArmyMarchMemory> MARCH_STATE =
            new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<RecruitNavigationResult> NAVIGATION_RESULT =
            new MemoryModuleType<>(Optional.empty());

    private ArmyBrainMemoryTypes() {
    }
}
