package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;

/** Rate-limited owner/commander contact for actionable recruit states. */
public final class RecruitStatusAlertBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    private static final int MINIMUM_ALERT_INTERVAL = 600;

    private String lastAlertCode = "";
    private int lastAlertTick = Integer.MIN_VALUE / 2;

    public RecruitStatusAlertBehaviour() {
        this.noTimeout();
    }

    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(
            ServerLevel level,
            GalacticRecruitEntity recruit
    ) {
        return recruit.tickCount % 20 == 0;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return false;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        String code = recruit.recruitStatusAlertCode().orElse("");
        if (code.isEmpty()) {
            lastAlertCode = "";
            return;
        }
        boolean changed = !code.equals(lastAlertCode);
        if (changed || recruit.tickCount - lastAlertTick >= MINIMUM_ALERT_INTERVAL) {
            if (recruit.sendRecruitStatusAlert(code)) {
                lastAlertCode = code;
                lastAlertTick = recruit.tickCount;
            }
        }
    }
}
