package galacticwars.clonewars.entity.ai;

import java.util.Objects;
import net.minecraft.core.BlockPos;

/** Transient result published by the sole Minecraft navigation consumer. */
public record RecruitNavigationResult(
        BlockPos target,
        State state,
        int gameTick
) {
    public RecruitNavigationResult {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(state, "state");
        target = target.immutable();
    }

    public enum State {
        MOVING,
        ARRIVED,
        UNREACHABLE
    }
}
