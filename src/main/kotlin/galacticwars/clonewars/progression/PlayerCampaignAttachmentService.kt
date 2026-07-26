package galacticwars.clonewars.progression

import java.util.LinkedHashMap
import java.util.LinkedHashSet

/** Pure projection/update boundary used by both loader-specific attachment adapters. */
object PlayerCampaignAttachmentService {
    @JvmStatic
    @JvmOverloads
    fun fromAuthoritative(
        progression: ProgressionState,
        force: ForceRuntimeState,
        gameplay: PlayerCampaignAttachmentState.GameplayProjection =
            PlayerCampaignAttachmentState.GameplayProjection.empty(),
    ): PlayerCampaignAttachmentState = PlayerCampaignAttachmentState(
        PlayerCampaignAttachmentState.CURRENT_SCHEMA_VERSION,
        progression.playerId,
        campaignProjection(progression),
        forceProjection(force),
        gameplay,
    )

    @JvmStatic
    fun updateProgression(
        current: PlayerCampaignAttachmentState,
        progression: ProgressionState,
    ): PlayerCampaignAttachmentState {
        require(current.playerId == progression.playerId) {
            "Progression state belongs to a different attachment player"
        }
        val updated = campaignProjection(progression)
        return if (updated == current.campaign) current else PlayerCampaignAttachmentState(
            current.schemaVersion,
            current.playerId,
            updated,
            current.force,
            current.gameplay,
        )
    }

    @JvmStatic
    fun updateForce(
        current: PlayerCampaignAttachmentState,
        force: ForceRuntimeState,
    ): PlayerCampaignAttachmentState {
        val updated = forceProjection(force)
        return if (updated == current.force) current else PlayerCampaignAttachmentState(
            current.schemaVersion,
            current.playerId,
            current.campaign,
            updated,
            current.gameplay,
        )
    }

    private fun campaignProjection(
        progression: ProgressionState,
    ): PlayerCampaignAttachmentState.CampaignProjection =
        PlayerCampaignAttachmentState.CampaignProjection(
            progression.factionId,
            progression.pendingCreditRewards(),
            progression.eventTotals,
            progression.eventSubjects.entries
                .asSequence()
                .filter { (type, _) ->
                    type !in PlayerCampaignAttachmentState.SERVER_ONLY_EVENT_SUBJECT_TYPES
                }
                .sortedBy { (type, _) -> type.ordinal }
                .associateTo(LinkedHashMap()) { (type, subjects) ->
                    type to boundedIdentifiers(
                        subjects,
                        PlayerCampaignAttachmentState.MAX_SUBJECTS_PER_TYPE,
                    )
                },
            boundedIdentifiers(
                progression.unlocks,
                PlayerCampaignAttachmentState.MAX_UNLOCKS,
            ),
        )

    private fun forceProjection(force: ForceRuntimeState): PlayerCampaignAttachmentState.ForceProjection =
        PlayerCampaignAttachmentState.ForceProjection(
            force.traditionId,
            force.energy,
            force.cooldownEnds.entries
                .asSequence()
                .filter { (abilityId, _) -> validIdentifier(abilityId) }
                .sortedBy { (abilityId, _) -> abilityId }
                .take(PlayerCampaignAttachmentState.MAX_FORCE_COOLDOWNS)
                .associateTo(LinkedHashMap()) { (abilityId, cooldownEnd) ->
                    abilityId to cooldownEnd
                },
        )

    private fun boundedIdentifiers(values: Set<String>, maximum: Int): Set<String> = values
        .asSequence()
        .filter(::validIdentifier)
        .sorted()
        .take(maximum)
        .toCollection(LinkedHashSet())

    private fun validIdentifier(value: String): Boolean =
        value.isNotBlank() && value.length <= PlayerCampaignAttachmentState.MAX_IDENTIFIER_LENGTH
}
