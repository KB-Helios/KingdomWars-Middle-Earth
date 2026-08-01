package galacticwars.clonewars.entity.ai;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.NpcFactionAiService;
import galacticwars.clonewars.faction.ai.NpcRole;
import java.util.List;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.tslat.smartbrainlib.api.SmartBrainBuilder;
import net.tslat.smartbrainlib.api.core.behaviour.base.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.GenericAttackTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.library.object.SquareRadius;

/** Stateless SmartBrainLib declaration for every Galactic recruit instance. */
public final class RecruitBrain implements SmartBrainBuilder<GalacticRecruitEntity> {
    public static final RecruitBrain INSTANCE = new RecruitBrain();

    private RecruitBrain() {
    }

    @Override
    public List<? extends ExtendedSensor<?>> getSensors(GalacticRecruitEntity owner) {
        return List.of(
                new ArmyGroupStateSensor(),
                new ArmyThreatSensor(),
                new HurtBySensor<GalacticRecruitEntity>()
                        .setPredicate((recruit, source) -> recruit.hurtTime > 0),
                new NearbyPlayersSensor<GalacticRecruitEntity>()
                        .setPredicate((recruit, player) -> !player.isSpectator())
                        .setRadius(recruit -> new SquareRadius(
                                NpcFactionAiService.scanRadius(recruit)))
                        .scanRate(NpcFactionAiService::scanInterval),
                new NearbyLivingEntitySensor<GalacticRecruitEntity>()
                        .setPredicate(GalacticRecruitEntity::canUseLocalAttackTarget)
                        .scanRate(NpcFactionAiService::scanInterval),
                new GenericAttackTargetSensor<GalacticRecruitEntity>()
                        .onlyTargetIf(GalacticRecruitEntity::canUseLocalAttackTarget)
                        .onlyScanIf(recruit -> !recruit.hasAuthoritativeArmyGroup()
                                && (recruit.isTame()
                                || recruit.getNpcRole() == NpcRole.COMMANDER
                                || recruit.getNpcRole() == NpcRole.TROOPER))
                        .scanRate(NpcFactionAiService::scanInterval));
    }

    @Override
    public List<? extends BehaviorControl<?>> getAlwaysRunningBehaviours(
            GalacticRecruitEntity owner
    ) {
        return List.of(
                new RecruitHazardAvoidanceBehaviour(),
                new WorkerSafetyBehaviour(),
                new ArmyPatrolBehaviour(),
                new FloatToSurfaceOfFluid<GalacticRecruitEntity>(),
                new LookAtTarget<GalacticRecruitEntity>(),
                new FactionPlayerReactionBehaviour(),
                new NaturalCommanderCoordinationBehaviour(),
                new RecruitSelfCareBehaviour(),
                new RecruitStatusAlertBehaviour(),
                new RecruitDoorInteractionBehaviour(),
                new RecruitWalkTargetBehaviour());
    }

    @Override
    public List<? extends BehaviorControl<?>> getIdleBehaviours(GalacticRecruitEntity owner) {
        return List.of(
                new RecruitAcquireAttackTargetBehaviour(),
                new FirstApplicableBehaviour<GalacticRecruitEntity>(
                        new RecruitSitBehaviour(),
                        new ArmyOrderBehaviour(),
                        new RecruitMoveToCommandBehaviour(1.05D),
                        new RecruitCompanionBehaviour(1.0D),
                        new CivilianShelterBehaviour(),
                        new NaturalCivilianWorkBehaviour(),
                        new RecruitWorkerBehaviour(),
                        new RecruitItemPickupBehaviour(),
                        new OneRandomBehaviour<GalacticRecruitEntity>(
                                new SetRandomWalkTarget<GalacticRecruitEntity>()
                                        .speedModifier(0.8F)
                                        .startCondition(RecruitBrain::canIdleWander)
                                        .stopIf(recruit -> !canIdleWander(recruit))
                                        .cooldownFor(20, 60),
                                new SetRandomLookTarget<GalacticRecruitEntity>()
                                        .lookTime(30, 60)
                                        .startCondition(RecruitBrain::canIdleWander)
                                        .stopIf(recruit -> !canIdleWander(recruit)),
                                new Idle<GalacticRecruitEntity>()
                                        .runFor(30, 60)
                                        .startCondition(RecruitBrain::canIdleWander)
                                        .stopIf(recruit -> !canIdleWander(recruit)))));
    }

    @Override
    public List<? extends BehaviorControl<?>> getFightingBehaviours(
            GalacticRecruitEntity owner
    ) {
        return List.of(
                new RecruitInvalidateAttackTargetBehaviour(),
                new FirstApplicableBehaviour<GalacticRecruitEntity>(
                        new ArmyCombatBehaviour(),
                        new RecruitRangedCombatBehaviour(),
                        new RecruitMeleeCombatBehaviour()));
    }

    private static boolean canIdleWander(GalacticRecruitEntity recruit) {
        return !recruit.hasAuthoritativeArmyGroup()
                && !recruit.isHazardAvoidanceActive()
                && !recruit.isOrderedToSit()
                && !recruit.shouldMoveToCommandTarget();
    }
}
