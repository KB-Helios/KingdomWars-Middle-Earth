package galacticwars.clonewars.technology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.UUIDUtil;

public final class TechnologyCodecs {
    private static final Codec<Map<String, Integer>> INPUTS =
            Codec.unboundedMap(Codec.STRING, Codec.intRange(1, TechnologyNodeDefinition.MAX_INPUT_COUNT));

    public static final Codec<KingdomResearchProject> RESEARCH_PROJECT =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("node_id").forGetter(KingdomResearchProject::nodeId),
                    Codec.STRING.fieldOf("definition_hash").forGetter(KingdomResearchProject::definitionHash),
                    INPUTS.fieldOf("required_inputs").forGetter(KingdomResearchProject::requiredInputs),
                    INPUTS.optionalFieldOf("delivered_inputs", Map.of())
                            .forGetter(KingdomResearchProject::deliveredInputs),
                    UUIDUtil.CODEC.optionalFieldOf("technician_id")
                            .forGetter(KingdomResearchProject::technicianId),
                    Codec.intRange(0, TechnologyNodeDefinition.MAX_REQUIRED_WORK)
                            .optionalFieldOf("work_progress", 0)
                            .forGetter(KingdomResearchProject::workProgress),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0)
                            .forGetter(KingdomResearchProject::revision),
                    UUIDUtil.CODEC.listOf(0, KingdomResearchProject.MAX_REPLAY_IDS)
                            .optionalFieldOf("replay_ids", List.of())
                            .forGetter(KingdomResearchProject::replayIds)
            ).apply(instance, KingdomResearchProject::new));

    public static final Codec<KingdomTechnologyState> KINGDOM_STATE =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("kingdom_id").forGetter(KingdomTechnologyState::kingdomId),
                    Codec.STRING.fieldOf("faction_id").forGetter(KingdomTechnologyState::factionId),
                    Codec.STRING.listOf(0, KingdomTechnologyState.MAX_COMPLETED_NODES)
                            .optionalFieldOf("completed_nodes", List.of())
                            .forGetter(KingdomTechnologyState::completedNodes),
                    RESEARCH_PROJECT.optionalFieldOf("active_project")
                            .forGetter(KingdomTechnologyState::activeProject),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("revision", 0)
                            .forGetter(KingdomTechnologyState::revision),
                    Codec.BOOL.optionalFieldOf("legacy_migration_pending", false)
                            .forGetter(KingdomTechnologyState::legacyMigrationPending)
            ).apply(instance, KingdomTechnologyState::new));

    private TechnologyCodecs() {
    }
}
