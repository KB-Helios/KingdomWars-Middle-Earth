package galacticwars.clonewars.data

import galacticwars.clonewars.ability.AbilityDefinition
import galacticwars.clonewars.ability.AbilityId
import galacticwars.clonewars.army.ArmyUnitDefinition
import galacticwars.clonewars.classes.UnitClassDefinition
import galacticwars.clonewars.classes.UnitClassId
import galacticwars.clonewars.faction.FactionDefinition
import galacticwars.clonewars.faction.FactionId
import galacticwars.clonewars.faction.FactionRuntimePolicy
import galacticwars.clonewars.faction.ai.NpcAiProfile
import galacticwars.clonewars.settlement.KingdomBaseBlueprint
import galacticwars.clonewars.world.CivilianArchetypeDefinition
import galacticwars.clonewars.world.OverworldFactionSpawnProfile
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.server.packs.resources.ResourceManager

/** Immutable output of one all-or-nothing gameplay-data preparation pass. */
data class PreparedGameplayData(
    val factions: Map<FactionId, FactionDefinition>,
    val units: List<ArmyUnitDefinition>,
    val abilities: Map<AbilityId, AbilityDefinition>,
    val unitClasses: Map<UnitClassId, UnitClassDefinition>,
    val factionPolicies: Map<FactionId, FactionRuntimePolicy>,
    val npcAiProfiles: Map<FactionId, NpcAiProfile>,
    val blueprints: Map<String, KingdomBaseBlueprint>,
    val civilianArchetypes: Map<String, CivilianArchetypeDefinition>,
    val overworldSpawnProfiles: Map<String, OverworldFactionSpawnProfile>,
    val launchContent: LaunchContentDefinitions,
)

/**
 * Loads independent datapack domains concurrently under one structured scope.
 * A failure cancels every sibling load, and the caller only receives a fully
 * consistent collection of definitions.
 */
object GameplayDataReloadPipeline {
    @JvmStatic
    @Throws(IOException::class)
    fun load(manager: ResourceManager): PreparedGameplayData = runBlocking {
        withContext(Dispatchers.Default) {
            val factions = GameplayDataManager.loadFactions(manager)
            coroutineScope {
                val units = async { GameplayDataManager.loadUnits(manager, factions) }
                val abilities = async { GameplayDataManager.loadAbilities(manager) }
                val factionPolicies = async {
                    GameplayDataManager.loadFactionPolicies(manager, factions)
                }
                val npcAiProfiles = async {
                    GameplayDataManager.loadNpcAiProfiles(manager, factions)
                }
                val blueprints = async { GameplayDataManager.loadBlueprints(manager) }
                val civilianArchetypes = async {
                    GameplayDataManager.loadCivilianArchetypes(manager, factions)
                }
                val launchContent = async {
                    LaunchContentValidator.load(
                        manager,
                        factions.keys.mapTo(linkedSetOf()) { it.path() },
                    )
                }

                val loadedUnits = units.await()
                val loadedAbilities = abilities.await()
                val loadedCivilianArchetypes = civilianArchetypes.await()
                val unitClasses = async {
                    GameplayDataManager.loadUnitClasses(
                        manager,
                        factions,
                        loadedUnits,
                        loadedAbilities,
                    )
                }
                val spawnProfiles = async {
                    GameplayDataManager.loadOverworldSpawnProfiles(
                        manager,
                        factions,
                        loadedUnits,
                        loadedCivilianArchetypes,
                    )
                }

                PreparedGameplayData(
                    factions = factions,
                    units = loadedUnits,
                    abilities = loadedAbilities,
                    unitClasses = unitClasses.await(),
                    factionPolicies = factionPolicies.await(),
                    npcAiProfiles = npcAiProfiles.await(),
                    blueprints = blueprints.await(),
                    civilianArchetypes = loadedCivilianArchetypes,
                    overworldSpawnProfiles = spawnProfiles.await(),
                    launchContent = launchContent.await(),
                )
            }
        }
    }
}
