package galacticwars.clonewars.technology;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.progression.ProgressionState;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;

public final class TechnologyMigrationService {
    private TechnologyMigrationService() {
    }

    public static boolean migrateKingdom(ServerLevel level, KingdomRecord kingdom) {
        ProgressionState progression = ProgressionSavedData.get(level).state(kingdom.ownerId());
        KingdomSavedData data = KingdomSavedData.get(level);
        KingdomTechnologyState state = data.technologyStateOrDefault(kingdom.id());
        if (!state.legacyMigrationPending()) {
            return false;
        }
        LinkedHashSet<String> grants = new LinkedHashSet<>();
        if (progression.unlocks().contains("advanced_trading")) {
            grants.add("galacticwars:field_fabrication");
            grants.add(firstFactionNode(kingdom.factionId()));
            grants.add(armamentNode(kingdom.factionId()));
        }
        if (progression.unlocks().contains("vehicle_crafting")) {
            grants.addAll(Set.of(
                    "galacticwars:field_fabrication",
                    "galacticwars:industrial_tooling",
                    "galacticwars:hyperspace_navigation",
                    "galacticwars:vehicle_engineering"));
            GameplayDataManager.snapshot().technology().visibleToFaction(kingdom.factionId()).stream()
                    .map(TechnologyNodeDefinition::id)
                    .forEach(grants::add);
        }
        grants.removeIf(id -> GameplayDataManager.snapshot().technology().node(id).isEmpty());
        KingdomTechnologyState migrated = state.grantMigrationNodes(grants);
        return data.storeTechnologyState(migrated, state.revision());
    }

    private static String firstFactionNode(String factionId) {
        return switch (factionId) {
            case "galacticwars:republic" -> "galacticwars:plastoid_processing";
            case "galacticwars:separatist" -> "galacticwars:alloy_reclamation";
            case "galacticwars:mandalorian" -> "galacticwars:alloy_forging";
            case "galacticwars:hutt_cartel" -> "galacticwars:salvage_fabrication";
            case "galacticwars:nightsister" -> "galacticwars:weave_processing";
            default -> "";
        };
    }

    private static String armamentNode(String factionId) {
        return switch (factionId) {
            case "galacticwars:republic" -> "galacticwars:clone_field_arms";
            case "galacticwars:separatist" -> "galacticwars:droid_field_arms";
            case "galacticwars:mandalorian" -> "galacticwars:clan_armaments";
            case "galacticwars:hutt_cartel" -> "galacticwars:mercenary_armaments";
            case "galacticwars:nightsister" -> "galacticwars:weave_armaments";
            default -> "";
        };
    }
}
