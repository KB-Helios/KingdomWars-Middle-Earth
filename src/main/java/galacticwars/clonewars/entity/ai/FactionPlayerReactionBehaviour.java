package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.Config;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.NpcDisposition;
import galacticwars.clonewars.faction.ai.NpcFactionAiService;
import galacticwars.clonewars.faction.ai.NpcReactionDecision;
import galacticwars.clonewars.faction.ai.NpcRole;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.base.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

/** Converts sensed players into embodied warnings, alarms, or authorized local targets. */
public final class FactionPlayerReactionBehaviour
        extends ExtendedBehaviour<GalacticRecruitEntity> {
    @Override
    public Set<MemoryCondition<?, ?>> getMemoryRequirements() {
        return Set.of();
    }

    @Override
    protected boolean checkExtraStartConditions(
            ServerLevel level,
            GalacticRecruitEntity recruit
    ) {
        return Config.ENABLE_DYNAMIC_FACTION_AI.getAsBoolean()
                && recruit.isNaturalFactionNpc()
                && nearbyPlayer(recruit) != null;
    }

    @Override
    protected boolean shouldKeepRunning(GalacticRecruitEntity recruit) {
        return false;
    }

    @Override
    protected void start(GalacticRecruitEntity recruit) {
        Player player = nearbyPlayer(recruit);
        if (player == null) {
            return;
        }
        NpcReactionDecision decision = recruit.npcReactionTo(player);
        BrainUtil.setMemory(
                recruit,
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(player, true));
        if (decision.disposition() == NpcDisposition.WARY) {
            if (player instanceof ServerPlayer serverPlayer) {
                recruit.tryWarnPlayer(serverPlayer);
            }
            if ((recruit.getNpcRole() == NpcRole.COMMANDER
                    || recruit.getNpcRole() == NpcRole.TROOPER)
                    && recruit.distanceToSqr(player) > 36.0D) {
                BrainUtil.setMemory(
                        recruit,
                        MemoryModuleType.WALK_TARGET,
                        new WalkTarget(player, 0.85F, 6));
            } else if ((recruit.getNpcRole() == NpcRole.TRADER
                    || recruit.getNpcRole() == NpcRole.CIVILIAN)
                    && recruit.hasHome()
                    && recruit.distanceToSqr(
                    net.minecraft.world.phys.Vec3.atCenterOf(recruit.getHomePosition())) > 9.0D) {
                BrainUtil.setMemory(
                        recruit,
                        MemoryModuleType.WALK_TARGET,
                        new WalkTarget(recruit.getHomePosition(), 0.9F, 2));
            }
            return;
        }
        if (decision.shouldRaiseAlert()) {
            NpcFactionAiService.raiseAlert(recruit, player, "hostile_approach");
            if (recruit.getNpcRole() == NpcRole.COMMANDER
                    || recruit.getNpcRole() == NpcRole.TROOPER) {
                recruit.acceptNaturalDefenseTarget(player);
            }
        }
    }

    private static Player nearbyPlayer(GalacticRecruitEntity recruit) {
        Player player = BrainUtil.getMemory(
                recruit, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return null;
        }
        int radius = NpcFactionAiService.scanRadius(recruit);
        return recruit.distanceToSqr(player) <= (double) radius * radius ? player : null;
    }
}
