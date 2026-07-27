package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.army.ArmyAttackTargetPolicy;
import galacticwars.clonewars.army.ArmyCommand;
import galacticwars.clonewars.army.ArmyCommandType;
import galacticwars.clonewars.army.ArmyEngagementDecision;
import galacticwars.clonewars.army.ArmyEngagementPlanner;
import galacticwars.clonewars.army.ArmyEngagementStance;
import galacticwars.clonewars.army.ArmyGroupLifecycleState;
import galacticwars.clonewars.army.ArmyGroupOrderAssignment;
import galacticwars.clonewars.army.ArmyGroupOrderPlanner;
import galacticwars.clonewars.army.ArmyGroupRecord;
import galacticwars.clonewars.army.ArmyGroupTactics;
import galacticwars.clonewars.army.ArmyPatrolEnemyPolicy;
import galacticwars.clonewars.army.ArmyPosition;
import galacticwars.clonewars.army.ArmyTargetCandidate;
import galacticwars.clonewars.army.ArmyTargetPriority;
import galacticwars.clonewars.army.ArmyTargetSelection;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.FactionRelation;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.recruitment.RecruitDuty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jspecify.annotations.Nullable;

/** Shared, server-only resolution and targeting helpers for squad brain components. */
final class ArmyBrainSupport {
    private static final int MAX_COORDINATION_RADIUS = 64;

    private ArmyBrainSupport() {
    }

    static @Nullable ArmyBrainState resolveState(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            @Nullable ArmyBrainState previous
    ) {
        if (!recruit.isTame() || recruit.getRecruitDuty() == RecruitDuty.WORKER) {
            return null;
        }
        UUID groupId = recruit.getArmyGroupId();
        if (groupId == null) {
            return null;
        }
        ArmyGroupRecord group = KingdomSavedData.get(level).armyGroup(groupId).orElse(null);
        if (group == null
                || group.simulation().lifecycleState() != ArmyGroupLifecycleState.LIVE
                || !group.contains(recruit.getUUID())) {
            return null;
        }

        LivingEntity owner = living(level.getEntity(group.ownerId()));
        ArmyPosition selfPosition = position(recruit);
        ArmyPosition ownerPosition = owner == null ? selfPosition : position(owner);
        MemberOrder memberOrder = memberOrder(group, recruit, ownerPosition);
        ArmyPosition fallbackPosition = commanderOrAnchor(level, group);
        int followRange = Math.max(4, (int)Math.round(
                recruit.getAttributeValue(Attributes.FOLLOW_RANGE)));
        boolean retainPerception = previous != null
                && previous.group().id().equals(group.id())
                && previous.group().simulation().revision() == group.simulation().revision();
        UUID selectedTarget = retainPerception ? previous.selectedTargetId() : null;
        UUID retreatThreat = retainPerception ? previous.retreatThreatId() : null;
        return new ArmyBrainState(
                group,
                memberOrder.command(),
                memberOrder.behaviorAnchor(),
                fallbackPosition,
                followRange,
                group.commanderId().filter(recruit.getUUID()::equals).isPresent(),
                selectedTarget,
                retreatThreat);
    }

    static void clearGroupExecution(GalacticRecruitEntity recruit) {
        BrainUtil.clearMemories(recruit,
                ArmyBrainMemoryTypes.ARMY_STATE,
                ArmyBrainMemoryTypes.PATH_STATUS,
                ArmyBrainMemoryTypes.MARCH_STATE,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryModuleType.PATH);
        recruit.setTarget(null);
        recruit.setAggressive(false);
    }

    static @Nullable LivingEntity selectTarget(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            ArmyBrainState state
    ) {
        ArmyGroupRecord group = state.group();
        ArmyCommandType commandType = group.order().type();
        if (commandType == ArmyCommandType.CLEAR_TARGET
                || isPatrolWithPolicy(group, ArmyPatrolEnemyPolicy.IGNORE_HOSTILES)
                || isPatrolWithPolicy(group, ArmyPatrolEnemyPolicy.RETREAT_FROM_HOSTILES)) {
            return null;
        }

        if (commandType == ArmyCommandType.ATTACK_TARGET) {
            LivingEntity explicit = group.order().targetEntityId()
                    .map(level::getEntity)
                    .map(ArmyBrainSupport::living)
                    .orElse(null);
            return validTarget(recruit, group, explicit, false) ? explicit : null;
        }

        ArmyGroupTactics tactics = group.effectiveTactics();
        if (tactics.engagementStance() == ArmyEngagementStance.PASSIVE) {
            return null;
        }

        UUID protectedEntityId = commandType == ArmyCommandType.PROTECT_ENTITY
                ? group.order().targetEntityId().orElse(null)
                : null;
        if (commandType == ArmyCommandType.PROTECT_OWNER || protectedEntityId != null) {
            LivingEntity protectedEntity = protectedEntityId == null
                    ? living(level.getEntity(group.ownerId()))
                    : living(level.getEntity(protectedEntityId));
            LivingEntity attacker = protectedEntity == null ? null : protectedEntity.getLastHurtByMob();
            if (isDefensiveTarget(recruit, group, attacker, state.followRange())) {
                return attacker;
            }
        }

        List<LiveTargetCandidate> candidates = new ArrayList<>();
        AABB scanBox = recruit.getBoundingBox().inflate(state.followRange());
        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class, scanBox, entity -> entity != recruit && entity.isAlive())) {
            ArmyTargetCandidate mapped = candidate(recruit, group, candidate, protectedEntityId);
            if (mapped != null) {
                candidates.add(new LiveTargetCandidate(candidate, mapped));
            }
        }
        LivingEntity prioritized = selectPrioritizedTarget(
                recruit, group, state, tactics, candidates);
        if (prioritized != null) {
            return prioritized;
        }
        LivingEntity attacker = recruit.getLastHurtByMob();
        return isDefensiveTarget(recruit, group, attacker, state.followRange()) ? attacker : null;
    }

    /** Finds a nearby hostile for a patrol that must retreat rather than fight. */
    static @Nullable LivingEntity selectRetreatThreat(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            ArmyBrainState state
    ) {
        ArmyGroupRecord group = state.group();
        if (!isPatrolWithPolicy(group, ArmyPatrolEnemyPolicy.RETREAT_FROM_HOSTILES)) {
            return null;
        }
        AABB scanBox = recruit.getBoundingBox().inflate(state.followRange());
        return level.getEntitiesOfClass(LivingEntity.class, scanBox,
                        entity -> entity != recruit && entity.isAlive())
                .stream()
                .filter(entity -> entity instanceof Monster
                        || candidate(recruit, group, entity, null) != null)
                .min(Comparator.comparingDouble(recruit::distanceToSqr))
                .orElse(null);
    }

    static boolean isCurrentGroupTarget(
            GalacticRecruitEntity recruit,
            @Nullable LivingEntity target
    ) {
        ArmyBrainState state = BrainUtil.getMemory(recruit, ArmyBrainMemoryTypes.ARMY_STATE);
        if (state == null || target == null) {
            return false;
        }
        UUID selectedTarget = state.selectedTargetId();
        if (selectedTarget != null && selectedTarget.equals(target.getUUID())) {
            return validTarget(recruit, state.group(), target, true);
        }
        if (state.group().order().type() == ArmyCommandType.ATTACK_TARGET
                && state.group().order().targetEntityId().filter(target.getUUID()::equals).isPresent()) {
            return validTarget(recruit, state.group(), target, false);
        }
        return target == recruit.getLastHurtByMob()
                && validTarget(recruit, state.group(), target, true);
    }

    static boolean canEngageGroupTarget(
            GalacticRecruitEntity recruit,
            ArmyBrainState state,
            @Nullable LivingEntity target
    ) {
        return isCurrentGroupTarget(recruit, target)
                || state.group().order().type() == ArmyCommandType.ATTACK_TARGET
                        && validTarget(recruit, state.group(), target, false);
    }

    /**
     * A held formation may fire or fight an enemy that reaches its control
     * radius, but does not let target acquisition pull the squad away from its
     * assigned slots.
     */
    static boolean shouldMaintainFormation(ArmyBrainState state, LivingEntity target) {
        if (!state.group().effectiveTactics().holdFormation()) {
            return false;
        }
        long formationRangeSquared = (long) state.followRange() * state.followRange();
        return distanceSquared(formationAnchor(state), position(target)) > formationRangeSquared;
    }

    /** Returns the member slot when present, otherwise the order behaviour anchor. */
    static ArmyPosition formationAnchor(ArmyBrainState state) {
        ArmyPosition slot = state.memberCommand().targetPosition();
        return slot == null ? state.behaviorAnchor() : slot;
    }

    /**
     * Resolves the persisted ranged policy at the server-side combat boundary.
     * Melee fallback remains available when a policy rejects a shot.
     */
    static boolean canUseRangedFire(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            ArmyGroupRecord group,
            LivingEntity target
    ) {
        boolean commandTarget = group.order().type() == ArmyCommandType.ATTACK_TARGET
                && group.order().targetEntityId().filter(target.getUUID()::equals).isPresent();
        boolean returnFireTarget = wasAttackedBy(recruit, target)
                || wasAttackedBy(living(level.getEntity(group.ownerId())), target);
        if (group.order().type() == ArmyCommandType.PROTECT_ENTITY) {
            returnFireTarget |= group.order().targetEntityId()
                    .map(level::getEntity)
                    .map(ArmyBrainSupport::living)
                    .map(defended -> wasAttackedBy(defended, target))
                    .orElse(false);
        }
        return group.effectiveTactics().rangedFirePolicy()
                .allowsRangedFire(returnFireTarget, commandTarget);
    }

    static boolean validTarget(
            GalacticRecruitEntity recruit,
            ArmyGroupRecord group,
            @Nullable LivingEntity target,
            boolean defendingOwner
    ) {
        if (target == null || !target.isAlive() || target == recruit || group.contains(target.getUUID())) {
            return false;
        }
        LivingEntity owner = recruit.getOwner();
        if (owner != null && owner.getUUID().equals(target.getUUID())) {
            return false;
        }
        if (target instanceof Player player) {
            return recruit.canAttackFactionPlayer(player);
        }
        if (target instanceof GalacticRecruitEntity other) {
            if (other.getRecruitDuty() == RecruitDuty.WORKER || sameOwner(recruit, other)) {
                return false;
            }
            return recruit.factionRelationTo(other) == FactionRelation.ENEMY;
        }
        if (target instanceof Monster) {
            boolean explicitOrderTarget = group.order().type()
                    == galacticwars.clonewars.army.ArmyCommandType.ATTACK_TARGET
                    && group.order().targetEntityId().filter(target.getUUID()::equals).isPresent();
            return defendingOwner || ArmyAttackTargetPolicy.canAttackMonster(
                    explicitOrderTarget, target == recruit.getLastHurtByMob());
        }
        return target == recruit.getLastHurtByMob();
    }

    static int coordinatedCooldownTicks(
            GalacticRecruitEntity recruit,
            ServerLevel level,
            KingdomSavedData data,
            ArmyGroupRecord group,
            int baseCooldownTicks
    ) {
        if (baseCooldownTicks <= 0) {
            throw new IllegalArgumentException("baseCooldownTicks must be positive");
        }
        LivingEntity commander = group.commanderId().map(level::getEntity)
                .map(ArmyBrainSupport::living)
                .orElse(null);
        int coordinationRadius = Math.max(4, Math.min(MAX_COORDINATION_RADIUS,
                (int)Math.round(recruit.getAttributeValue(Attributes.FOLLOW_RANGE))));
        if (commander == null
                || recruit.distanceToSqr(commander)
                        > (double)coordinationRadius * coordinationRadius) {
            return baseCooldownTicks;
        }
        KingdomRecord kingdom = data.kingdom(group.kingdomId()).orElse(null);
        if (kingdom == null) {
            return baseCooldownTicks;
        }
        FactionBalanceService.ResolvedBalance balance = FactionBalanceService.resolve(kingdom.factionId());
        int effectivePercent = Math.max(1, Math.min(FactionBalanceService.MAX_PERCENT,
                balance.coordinationPercent()));
        if (!data.isHallActive(kingdom.ownerId())) {
            effectivePercent = Math.max(1, Math.min(FactionBalanceService.MAX_PERCENT,
                    FactionBalanceService.applyPercentCeil(
                            effectivePercent, balance.withoutCommandNodePercent())));
        }
        long numerator = (long)baseCooldownTicks * 100L;
        return (int)Math.max(1L, Math.min(Integer.MAX_VALUE,
                (numerator + effectivePercent - 1L) / effectivePercent));
    }

    static ArmyPosition position(Entity entity) {
        return new ArmyPosition(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
    }

    static long distanceSquared(ArmyPosition first, ArmyPosition second) {
        long x = (long)first.x() - second.x();
        long y = (long)first.y() - second.y();
        long z = (long)first.z() - second.z();
        return x * x + y * y + z * z;
    }

    static @Nullable LivingEntity living(@Nullable Entity entity) {
        return entity instanceof LivingEntity living ? living : null;
    }

    private static @Nullable ArmyTargetCandidate candidate(
            GalacticRecruitEntity recruit,
            ArmyGroupRecord group,
            LivingEntity candidate,
            @Nullable UUID protectedEntityId
    ) {
        if (!(candidate instanceof GalacticRecruitEntity other)
                || other.getRecruitDuty() == RecruitDuty.WORKER
                || group.contains(other.getUUID())
                || sameOwner(recruit, other)
                || recruit.factionRelationTo(other) != FactionRelation.ENEMY) {
            return null;
        }
        int threat = (int)Math.round(Math.min(100.0D,
                other.getAttributeValue(Attributes.ATTACK_DAMAGE) * 5.0D));
        LivingEntity target = other.getTarget();
        return new ArmyTargetCandidate(
                other.getUUID(),
                FactionId.of(other.getRecruitFactionId()),
                position(other),
                target != null && (target.getUUID().equals(group.ownerId())
                        || target.getUUID().equals(protectedEntityId)),
                target != null && group.contains(target.getUUID()),
                Math.max(0, threat),
                Optional.of(recruit.factionRelationTo(other)));
    }

    private static MemberOrder memberOrder(
            ArmyGroupRecord group,
            GalacticRecruitEntity recruit,
            ArmyPosition ownerPosition
    ) {
        if (group.commanderId().filter(recruit.getUUID()::equals).isPresent()) {
            if (group.order().type() == ArmyCommandType.RETURN_TO_RALLY
                    && group.rallyPoint().isPresent()) {
                ArmyPosition rally = group.rallyPoint().orElseThrow().blockPosition();
                return new MemberOrder(
                        ArmyCommand.moveToPosition(group.ownerId(), group.id(), rally), rally);
            }
            ArmyPosition executionPosition = ArmyGroupOrderPlanner.executionPosition(group);
            if (executionPosition != null
                    && group.simulation().marchState().phase()
                            != galacticwars.clonewars.army.ArmyMarchPhase.HALTED) {
                return new MemberOrder(
                        ArmyCommand.moveToPosition(group.ownerId(), group.id(), executionPosition),
                        executionPosition);
            }
            return new MemberOrder(group.order().toCommand(group.ownerId(), group.id()), ownerPosition);
        }
        List<ArmyGroupOrderAssignment> assignments = ArmyGroupOrderPlanner.plan(group, ownerPosition);
        for (ArmyGroupOrderAssignment assignment : assignments) {
            if (!assignment.recruitId().equals(recruit.getUUID())) {
                continue;
            }
            ArmyPosition anchor = assignment.assignedPosition() == null
                    ? ownerPosition
                    : assignment.assignedPosition();
            return new MemberOrder(assignment.command(), anchor);
        }
        return new MemberOrder(group.order().toCommand(group.ownerId(), group.id()), ownerPosition);
    }

    private static ArmyPosition commanderOrAnchor(ServerLevel level, ArmyGroupRecord group) {
        LivingEntity commander = group.commanderId().map(level::getEntity)
                .map(ArmyBrainSupport::living)
                .orElse(null);
        return commander == null ? group.simulation().anchor().blockPosition() : position(commander);
    }

    private static boolean sameOwner(GalacticRecruitEntity first, GalacticRecruitEntity second) {
        LivingEntity firstOwner = first.getOwner();
        LivingEntity secondOwner = second.getOwner();
        return firstOwner != null && secondOwner != null
                && firstOwner.getUUID().equals(secondOwner.getUUID());
    }

    private static boolean wasAttackedBy(@Nullable LivingEntity defender, LivingEntity target) {
        return defender != null
                && defender.getLastHurtByMob() != null
                && defender.getLastHurtByMob().getUUID().equals(target.getUUID());
    }

    private static boolean isPatrolWithPolicy(ArmyGroupRecord group, ArmyPatrolEnemyPolicy policy) {
        return group.order().type() == ArmyCommandType.PATROL_ROUTE
                && group.effectivePatrolPlan()
                        .map(plan -> plan.enemyPolicy() == policy)
                        .orElse(false);
    }

    private static boolean isDefensiveTarget(
            GalacticRecruitEntity recruit,
            ArmyGroupRecord group,
            @Nullable LivingEntity target,
            int followRange
    ) {
        return validTarget(recruit, group, target, true)
                && target != null
                && recruit.distanceToSqr(target) <= (double)followRange * followRange;
    }

    private static @Nullable LivingEntity selectPrioritizedTarget(
            GalacticRecruitEntity recruit,
            ArmyGroupRecord group,
            ArmyBrainState state,
            ArmyGroupTactics tactics,
            List<LiveTargetCandidate> candidates
    ) {
        List<LiveTargetCandidate> eligible = switch (tactics.engagementStance()) {
            case PASSIVE -> List.of();
            case DEFENSIVE -> candidates.stream()
                    .filter(candidate -> candidate.target().attackingOwner()
                            || candidate.target().attackingRecruit())
                    .toList();
            case AGGRESSIVE -> candidates;
        };
        if (eligible.isEmpty()) {
            return null;
        }
        return switch (tactics.targetPriority()) {
            case OWNER_THREAT -> eligible.stream()
                    .filter(candidate -> candidate.target().attackingOwner())
                    .min(Comparator.comparingDouble(candidate -> recruit.distanceToSqr(candidate.entity())))
                    .or(() -> selectWithEngagementPlanner(recruit, state, tactics.engagementStance(), eligible))
                    .map(LiveTargetCandidate::entity)
                    .orElse(null);
            case NEAREST_HOSTILE -> eligible.stream()
                    .min(Comparator.comparingDouble(candidate -> recruit.distanceToSqr(candidate.entity())))
                    .map(LiveTargetCandidate::entity)
                    .orElse(null);
            case LOWEST_HEALTH -> eligible.stream()
                    .min(Comparator.comparingDouble(candidate -> candidate.entity().getHealth()))
                    .map(LiveTargetCandidate::entity)
                    .orElse(null);
            case COMMAND_TARGET -> selectWithEngagementPlanner(
                    recruit, state, tactics.engagementStance(), eligible)
                    .map(LiveTargetCandidate::entity)
                    .orElse(null);
        };
    }

    private static Optional<LiveTargetCandidate> selectWithEngagementPlanner(
            GalacticRecruitEntity recruit,
            ArmyBrainState state,
            ArmyEngagementStance stance,
            List<LiveTargetCandidate> candidates
    ) {
        ArmyEngagementDecision engagement = ArmyEngagementPlanner.plan(
                stance,
                FactionId.of(recruit.getRecruitFactionId()),
                position(recruit),
                candidates.stream().map(LiveTargetCandidate::target).toList(),
                GameplayDataManager.snapshot().factions(),
                state.followRange());
        UUID selectedId = Optional.ofNullable(engagement.targetSelection())
                .map(ArmyTargetSelection::targetId)
                .orElse(null);
        if (selectedId == null) {
            return Optional.empty();
        }
        return candidates.stream().filter(candidate -> candidate.entity().getUUID().equals(selectedId)).findFirst();
    }

    private record MemberOrder(ArmyCommand command, ArmyPosition behaviorAnchor) {
    }

    private record LiveTargetCandidate(LivingEntity entity, ArmyTargetCandidate target) {
    }
}
