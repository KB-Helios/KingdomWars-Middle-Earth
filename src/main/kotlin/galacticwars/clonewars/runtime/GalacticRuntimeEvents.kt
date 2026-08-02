package galacticwars.clonewars.runtime

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.TickEvent
import galacticwars.clonewars.army.ArmyRuntimeEvents
import galacticwars.clonewars.combat.BlasterCombatEvents
import galacticwars.clonewars.classes.PlayerClassRuntime
import galacticwars.clonewars.force.ForceRuntimeEvents
import galacticwars.clonewars.kingdom.KingdomClaimEvents
import galacticwars.clonewars.kingdom.KingdomSimulationEvents
import galacticwars.clonewars.kingdom.SiegeRuntimeEvents
import galacticwars.clonewars.progression.MissionRuntimeEvents
import galacticwars.clonewars.settlement.CommandCenterEvents
import galacticwars.clonewars.survival.MountFiberRecoveryEvents
import galacticwars.clonewars.workforce.WorksiteOverlayService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Installs gameplay event handlers once through Architectury so the server
 * simulation and permission rules are identical on Fabric and NeoForge.
 */
object GalacticRuntimeEvents {
    private val registered = AtomicBoolean()

    @JvmStatic
    fun register() {
        if (!registered.compareAndSet(false, true)) {
            return
        }

        TickEvent.SERVER_POST.register(ArmyRuntimeEvents::onServerTick)
        TickEvent.SERVER_POST.register(ForceRuntimeEvents::onServerTick)
        TickEvent.SERVER_POST.register(KingdomSimulationEvents::onServerTick)
        TickEvent.SERVER_POST.register(SiegeRuntimeEvents::onServerTick)
        TickEvent.SERVER_POST.register(WorksiteOverlayService::onServerTick)
        MissionRuntimeEvents.register()
        PlayerClassRuntime.register()

        EntityEvent.ADD.register { entity, level ->
            if (ArmyRuntimeEvents.allowEntityAddition(entity, level)) {
                EventResult.pass()
            } else {
                EventResult.interruptFalse()
            }
        }
        EntityEvent.LIVING_HURT.register { target, source, _ ->
            if (BlasterCombatEvents.blocksIncomingDamage(target, source)) {
                EventResult.interruptFalse()
            } else {
                EventResult.pass()
            }
        }

        BlockEvent.BREAK.register { level, pos, _, player ->
            val allowed = CommandCenterEvents.allowBlockBreak(level, pos, player) &&
                KingdomClaimEvents.allowBreak(level, player, pos)
            if (allowed) EventResult.pass() else EventResult.interruptFalse()
        }
        BlockEvent.PLACE.register { level, pos, _, entity ->
            if (KingdomClaimEvents.allowPlace(level, entity, pos)) {
                EventResult.pass()
            } else {
                EventResult.interruptFalse()
            }
        }
        InteractionEvent.RIGHT_CLICK_BLOCK.register { player, _, pos, _ ->
            if (KingdomClaimEvents.allowStorageInteraction(player, pos)) {
                EventResult.pass()
            } else {
                EventResult.interruptFalse()
            }
        }
        InteractionEvent.INTERACT_ENTITY.register { player, target, hand ->
            MountFiberRecoveryEvents.onHorseBrushed(player, target, hand)
                .map(EventResult::fromMinecraft)
                .orElseGet(EventResult::pass)
        }
    }
}
