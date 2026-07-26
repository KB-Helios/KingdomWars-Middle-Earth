package galacticwars.clonewars.progression

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.datafixers.util.Either
import java.util.Collections
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Locale
import java.util.Objects
import java.util.UUID
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

/**
 * Loader-neutral, bounded client read model for campaign and Force state.
 * Server-only replay ledgers remain exclusively in their authoritative SavedData.
 */
class PlayerCampaignAttachmentState(
    schemaVersion: Int,
    playerId: UUID,
    campaign: CampaignProjection,
    force: ForceProjection,
    gameplay: GameplayProjection,
) {
    constructor(
        schemaVersion: Int,
        playerId: UUID,
        campaign: CampaignProjection,
        force: ForceProjection,
    ) : this(schemaVersion, playerId, campaign, force, GameplayProjection.empty())

    val schemaVersion: Int = schemaVersion
    val playerId: UUID = playerId
    val campaign: CampaignProjection = campaign
    val force: ForceProjection = force
    val gameplay: GameplayProjection = gameplay

    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION) {
            "Unsupported player campaign attachment schema $schemaVersion"
        }
    }

    fun schemaVersion(): Int = schemaVersion

    fun playerId(): UUID = playerId

    fun campaign(): CampaignProjection = campaign

    fun force(): ForceProjection = force

    fun gameplay(): GameplayProjection = gameplay

    override fun equals(other: Any?): Boolean = this === other ||
        other is PlayerCampaignAttachmentState &&
        schemaVersion == other.schemaVersion &&
        playerId == other.playerId &&
        campaign == other.campaign &&
        force == other.force &&
        gameplay == other.gameplay

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + playerId.hashCode()
        result = 31 * result + campaign.hashCode()
        result = 31 * result + force.hashCode()
        return 31 * result + gameplay.hashCode()
    }

    override fun toString(): String = "PlayerCampaignAttachmentState[" +
        "schemaVersion=$schemaVersion, playerId=$playerId, campaign=$campaign, force=$force, gameplay=$gameplay]"

    class GameplayProjection(
        reputation: Map<String, Int>,
        completedTechnology: Set<String>,
        activeResearchNode: String?,
        requiredResearchInputs: Map<String, Int>,
        deliveredResearchInputs: Map<String, Int>,
        technicianId: String?,
        researchProgress: Int,
        researchRequired: Int,
        kingdomRevision: Int,
        effectiveCapabilities: Set<String>,
    ) {
        val reputation: Map<String, Int> = Collections.unmodifiableMap(LinkedHashMap(reputation))
        val completedTechnology: Set<String> = immutableSet(
            completedTechnology,
            MAX_COMPLETED_TECHNOLOGY,
            "completedTechnology",
        )
        val activeResearchNode: String = activeResearchNode.orEmpty()
        val requiredResearchInputs: Map<String, Int> =
            Collections.unmodifiableMap(LinkedHashMap(requiredResearchInputs))
        val deliveredResearchInputs: Map<String, Int> =
            Collections.unmodifiableMap(LinkedHashMap(deliveredResearchInputs))
        val technicianId: String = technicianId.orEmpty()
        val researchProgress: Int = researchProgress
        val researchRequired: Int = researchRequired
        val kingdomRevision: Int = kingdomRevision
        val effectiveCapabilities: Set<String> = immutableSet(
            effectiveCapabilities,
            MAX_EFFECTIVE_CAPABILITIES,
            "effectiveCapabilities",
        )

        init {
            require(this.reputation.size <= MAX_REPUTATION_FACTIONS)
            require(this.reputation.keys.all(::validIdentifier))
            require(this.reputation.values.all { it in -100..100 })
            require(this.activeResearchNode.isEmpty() || validIdentifier(this.activeResearchNode))
            require(this.requiredResearchInputs.size <= 16)
            require(this.requiredResearchInputs.keys.all(::validIdentifier))
            require(this.requiredResearchInputs.values.all { it in 1..4_096 })
            require(this.deliveredResearchInputs.size <= 16)
            require(this.deliveredResearchInputs.keys.all { it in this.requiredResearchInputs })
            require(this.deliveredResearchInputs.all { (key, value) ->
                value in 1..(this.requiredResearchInputs[key] ?: 0)
            })
            require(this.technicianId.isEmpty() || this.technicianId.length <= 64)
            require(researchProgress >= 0 && researchRequired >= 0 && researchProgress <= researchRequired)
            require(kingdomRevision >= 0)
        }

        override fun equals(other: Any?): Boolean = this === other ||
            other is GameplayProjection &&
            reputation == other.reputation &&
            completedTechnology == other.completedTechnology &&
            activeResearchNode == other.activeResearchNode &&
            requiredResearchInputs == other.requiredResearchInputs &&
            deliveredResearchInputs == other.deliveredResearchInputs &&
            technicianId == other.technicianId &&
            researchProgress == other.researchProgress &&
            researchRequired == other.researchRequired &&
            kingdomRevision == other.kingdomRevision &&
            effectiveCapabilities == other.effectiveCapabilities

        override fun hashCode(): Int = Objects.hash(
            reputation,
            completedTechnology,
            activeResearchNode,
            requiredResearchInputs,
            deliveredResearchInputs,
            technicianId,
            researchProgress,
            researchRequired,
            kingdomRevision,
            effectiveCapabilities,
        )

        companion object {
            @JvmStatic
            fun empty(): GameplayProjection = GameplayProjection(
                emptyMap(), emptySet(), "", emptyMap(), emptyMap(), "", 0, 0, 0, emptySet(),
            )
        }
    }

    class CampaignProjection(
        factionId: String?,
        pendingCreditRewards: Int,
        eventTotals: Map<ProgressionEventType, Int>,
        eventSubjects: Map<ProgressionEventType, Set<String>>,
        unlocks: Set<String>,
    ) {
        val factionId: String = factionId.orEmpty()
        val pendingCreditRewards: Int = pendingCreditRewards
        val eventTotals: Map<ProgressionEventType, Int> = immutableMap(
            eventTotals,
            ProgressionEventType.values().size,
            "eventTotals",
        )
        val eventSubjects: Map<ProgressionEventType, Set<String>> = immutableSubjectMap(eventSubjects)
        val unlocks: Set<String> = immutableSet(unlocks, MAX_UNLOCKS, "unlocks")

        init {
            require(this.factionId.length <= MAX_IDENTIFIER_LENGTH) { "factionId is too long" }
            require(pendingCreditRewards >= 0) { "pendingCreditRewards cannot be negative" }
            require(eventTotals.values.all { it >= 0 }) { "event totals cannot be negative" }
            require(eventSubjects.keys.none { it in SERVER_ONLY_EVENT_SUBJECT_TYPES }) {
                "replay and mission-lifecycle subjects are server-only"
            }
            require(eventSubjects.values.all { it.size <= MAX_SUBJECTS_PER_TYPE }) {
                "event subject count exceeds protocol limit"
            }
            require(eventSubjects.values.flatten().all(::validIdentifier)) {
                "event subjects must be non-blank bounded identifiers"
            }
            require(unlocks.all(::validIdentifier)) {
                "unlocks must be non-blank bounded identifiers"
            }
        }

        fun factionId(): String = factionId

        fun pendingCreditRewards(): Int = pendingCreditRewards

        fun eventTotals(): Map<ProgressionEventType, Int> = eventTotals

        fun eventSubjects(): Map<ProgressionEventType, Set<String>> = eventSubjects

        fun unlocks(): Set<String> = unlocks

        override fun equals(other: Any?): Boolean = this === other ||
            other is CampaignProjection &&
            factionId == other.factionId &&
            pendingCreditRewards == other.pendingCreditRewards &&
            eventTotals == other.eventTotals &&
            eventSubjects == other.eventSubjects &&
            unlocks == other.unlocks

        override fun hashCode(): Int {
            var result = factionId.hashCode()
            result = 31 * result + pendingCreditRewards
            result = 31 * result + eventTotals.hashCode()
            result = 31 * result + eventSubjects.hashCode()
            return 31 * result + unlocks.hashCode()
        }

        override fun toString(): String = "CampaignProjection[" +
            "factionId=$factionId, pendingCreditRewards=$pendingCreditRewards, " +
            "eventTotals=$eventTotals, eventSubjects=$eventSubjects, unlocks=$unlocks]"
    }

    class ForceProjection(
        traditionOrPath: String?,
        energy: Int,
        cooldownEnds: Map<String, Long>,
    ) {
        val tradition: String = ForceRuntimeState.normalizeTradition(traditionOrPath)
        val path: String = when (tradition) {
            "jedi" -> "light"
            "sith", "nightsister" -> "dark"
            else -> ""
        }
        val energy: Int = energy
        val cooldownEnds: Map<String, Long> = immutableMap(
            cooldownEnds,
            MAX_FORCE_COOLDOWNS,
            "force cooldowns",
        )
        init {
            require(this.tradition.isEmpty() || this.tradition in setOf("jedi", "sith", "nightsister")) {
                "Unknown Force tradition ${this.tradition}"
            }
            require(energy in 0..ForceRuntimeState.MAX_ENERGY) {
                "Force energy must be between 0 and ${ForceRuntimeState.MAX_ENERGY}"
            }
            require(cooldownEnds.keys.all(::validIdentifier)) {
                "Force cooldown ids must be non-blank bounded identifiers"
            }
        }

        fun path(): String = path

        fun tradition(): String = tradition

        fun energy(): Int = energy

        fun cooldownEnds(): Map<String, Long> = cooldownEnds

        override fun equals(other: Any?): Boolean = this === other ||
            other is ForceProjection &&
            tradition == other.tradition &&
            energy == other.energy &&
            cooldownEnds == other.cooldownEnds

        override fun hashCode(): Int {
            var result = tradition.hashCode()
            result = 31 * result + energy
            return 31 * result + cooldownEnds.hashCode()
        }

        override fun toString(): String = "ForceProjection[" +
            "tradition=$tradition, energy=$energy, cooldownEnds=$cooldownEnds]"
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 2
        const val MAX_IDENTIFIER_LENGTH: Int = 256
        const val MAX_SUBJECTS_PER_TYPE: Int = 64
        const val MAX_UNLOCKS: Int = 256
        const val MAX_FORCE_COOLDOWNS: Int = 32
        const val MAX_REPUTATION_FACTIONS: Int = 8
        const val MAX_COMPLETED_TECHNOLOGY: Int = 256
        const val MAX_EFFECTIVE_CAPABILITIES: Int = 256
        const val MAX_SYNC_PAYLOAD_BYTES: Int = 1_048_576

        @JvmField
        val SERVER_ONLY_EVENT_SUBJECT_TYPES: Set<ProgressionEventType> = setOf(
            ProgressionEventType.DELIVERY_COMPLETED,
            ProgressionEventType.MISSION_STARTED,
            ProgressionEventType.MISSION_FAILED,
            ProgressionEventType.MISSION_OBJECTIVE_COMPLETED,
        )

        private val EVENT_TYPE_CODEC: Codec<ProgressionEventType> = Codec.STRING.comapFlatMap(
            { value ->
                try {
                    DataResult.success(ProgressionEventType.valueOf(value.uppercase(Locale.ROOT)))
                } catch (_: IllegalArgumentException) {
                    DataResult.error { "Unknown progression event type $value" }
                }
            },
            { type -> type.name.lowercase(Locale.ROOT) },
        )
        private val STRING_SET_CODEC: Codec<Set<String>> = Codec.STRING.listOf().xmap(
            { values -> LinkedHashSet(values) },
            { values -> values.sorted() },
        )
        private val EVENT_TOTALS_CODEC: Codec<Map<ProgressionEventType, Int>> = Codec.unboundedMap(
            EVENT_TYPE_CODEC,
            Codec.intRange(0, Int.MAX_VALUE),
        )
        private val EVENT_SUBJECTS_CODEC: Codec<Map<ProgressionEventType, Set<String>>> =
            Codec.unboundedMap(EVENT_TYPE_CODEC, STRING_SET_CODEC)

        private val CAMPAIGN_CODEC: Codec<CampaignProjection> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("faction_id", "")
                    .forGetter { value: CampaignProjection -> value.factionId },
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("pending_credit_rewards", 0)
                    .forGetter { value: CampaignProjection -> value.pendingCreditRewards },
                EVENT_TOTALS_CODEC.optionalFieldOf("event_totals", emptyMap())
                    .forGetter { value: CampaignProjection -> value.eventTotals },
                EVENT_SUBJECTS_CODEC.optionalFieldOf("event_subjects", emptyMap())
                    .forGetter { value: CampaignProjection -> value.eventSubjects },
                STRING_SET_CODEC.optionalFieldOf("unlocks", emptySet())
                    .forGetter { value: CampaignProjection -> value.unlocks },
            ).apply(instance, ::CampaignProjection)
        }

        private val FORCE_CODEC: Codec<ForceProjection> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("path", "")
                    .forGetter { value: ForceProjection -> value.tradition },
                Codec.intRange(0, ForceRuntimeState.MAX_ENERGY)
                    .optionalFieldOf("energy", ForceRuntimeState.MAX_ENERGY)
                    .forGetter { value: ForceProjection -> value.energy },
                Codec.unboundedMap(Codec.STRING, Codec.LONG)
                    .optionalFieldOf("cooldown_ends", emptyMap())
                    .forGetter { value: ForceProjection -> value.cooldownEnds },
            ).apply(instance, ::ForceProjection)
        }

        private val GAMEPLAY_CODEC: Codec<GameplayProjection> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.intRange(-100, 100))
                    .optionalFieldOf("reputation", emptyMap())
                    .forGetter { value: GameplayProjection -> value.reputation },
                STRING_SET_CODEC.optionalFieldOf("completed_technology", emptySet())
                    .forGetter { value: GameplayProjection -> value.completedTechnology },
                Codec.STRING.optionalFieldOf("active_research_node", "")
                    .forGetter { value: GameplayProjection -> value.activeResearchNode },
                Codec.unboundedMap(Codec.STRING, Codec.intRange(1, 4_096))
                    .optionalFieldOf("required_research_inputs", emptyMap())
                    .forGetter { value: GameplayProjection -> value.requiredResearchInputs },
                Codec.unboundedMap(Codec.STRING, Codec.intRange(1, 4_096))
                    .optionalFieldOf("delivered_research_inputs", emptyMap())
                    .forGetter { value: GameplayProjection -> value.deliveredResearchInputs },
                Codec.STRING.optionalFieldOf("technician_id", "")
                    .forGetter { value: GameplayProjection -> value.technicianId },
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("research_progress", 0)
                    .forGetter { value: GameplayProjection -> value.researchProgress },
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("research_required", 0)
                    .forGetter { value: GameplayProjection -> value.researchRequired },
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("kingdom_revision", 0)
                    .forGetter { value: GameplayProjection -> value.kingdomRevision },
                STRING_SET_CODEC.optionalFieldOf("effective_capabilities", emptySet())
                    .forGetter { value: GameplayProjection -> value.effectiveCapabilities },
            ).apply(instance, ::GameplayProjection)
        }

        private val CURRENT_CODEC: Codec<PlayerCampaignAttachmentState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(CURRENT_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION)
                    .fieldOf("schema_version")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.schemaVersion },
                UUIDUtil.CODEC.fieldOf("player_id")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.playerId },
                CAMPAIGN_CODEC.fieldOf("progression")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.campaign },
                FORCE_CODEC.fieldOf("force")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.force },
                GAMEPLAY_CODEC.optionalFieldOf("gameplay", GameplayProjection.empty())
                    .forGetter { value: PlayerCampaignAttachmentState -> value.gameplay },
            ).apply(instance, ::PlayerCampaignAttachmentState)
        }

        private val LEGACY_CODEC: Codec<PlayerCampaignAttachmentState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(1, 1).fieldOf("schema_version")
                    .forGetter { _: PlayerCampaignAttachmentState -> 1 },
                UUIDUtil.CODEC.fieldOf("player_id")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.playerId },
                CAMPAIGN_CODEC.fieldOf("progression")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.campaign },
                FORCE_CODEC.fieldOf("force")
                    .forGetter { value: PlayerCampaignAttachmentState -> value.force },
            ).apply(instance) { _, playerId, campaign, force ->
                PlayerCampaignAttachmentState(
                    CURRENT_SCHEMA_VERSION,
                    playerId,
                    campaign,
                    force,
                    GameplayProjection.empty(),
                )
            }
        }

        @JvmField
        val CODEC: Codec<PlayerCampaignAttachmentState> =
            Codec.either(CURRENT_CODEC, LEGACY_CODEC).xmap(
                { either -> either.map({ it }, { it }) },
                { value -> Either.left(value) },
            )

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerCampaignAttachmentState> =
            StreamCodec.of(
                { buffer, value -> write(buffer, value) },
                { buffer -> read(buffer) },
            )

        @JvmStatic
        fun initial(playerId: UUID): PlayerCampaignAttachmentState =
            PlayerCampaignAttachmentService.fromAuthoritative(
                ProgressionState.create(playerId),
                ForceRuntimeState.full(),
            )

        private fun write(buffer: RegistryFriendlyByteBuf, value: PlayerCampaignAttachmentState) {
            buffer.writeVarInt(value.schemaVersion)
            buffer.writeUUID(value.playerId)
            writeString(buffer, value.campaign.factionId)
            buffer.writeVarInt(value.campaign.pendingCreditRewards)
            writeEventTotals(buffer, value.campaign.eventTotals)
            writeEventSubjects(buffer, value.campaign.eventSubjects)
            writeStringSet(buffer, value.campaign.unlocks, MAX_UNLOCKS)
            writeString(buffer, value.force.tradition)
            buffer.writeVarInt(value.force.energy)
            writeCooldowns(buffer, value.force.cooldownEnds)
            writeStringIntMap(buffer, value.gameplay.reputation, MAX_REPUTATION_FACTIONS, "reputation")
            writeStringSet(
                buffer,
                value.gameplay.completedTechnology,
                MAX_COMPLETED_TECHNOLOGY,
            )
            writeString(buffer, value.gameplay.activeResearchNode)
            writeStringIntMap(buffer, value.gameplay.requiredResearchInputs, 16, "requiredResearchInputs")
            writeStringIntMap(buffer, value.gameplay.deliveredResearchInputs, 16, "deliveredResearchInputs")
            writeString(buffer, value.gameplay.technicianId)
            buffer.writeVarInt(value.gameplay.researchProgress)
            buffer.writeVarInt(value.gameplay.researchRequired)
            buffer.writeVarInt(value.gameplay.kingdomRevision)
            writeStringSet(
                buffer,
                value.gameplay.effectiveCapabilities,
                MAX_EFFECTIVE_CAPABILITIES,
            )
        }

        private fun read(buffer: RegistryFriendlyByteBuf): PlayerCampaignAttachmentState {
            val schemaVersion = buffer.readVarInt()
            require(schemaVersion in 1..CURRENT_SCHEMA_VERSION) {
                "Unsupported player campaign attachment schema $schemaVersion"
            }
            val playerId = buffer.readUUID()
            val campaign = CampaignProjection(
                readString(buffer),
                nonNegative(buffer.readVarInt(), "pendingCreditRewards"),
                readEventTotals(buffer),
                readEventSubjects(buffer),
                readStringSet(buffer, MAX_UNLOCKS, "unlocks"),
            )
            val force = ForceProjection(
                readString(buffer),
                buffer.readVarInt(),
                readCooldowns(buffer),
            )
            val gameplay = if (schemaVersion >= 2) {
                GameplayProjection(
                    readStringIntMap(buffer, MAX_REPUTATION_FACTIONS, "reputation"),
                    readStringSet(buffer, MAX_COMPLETED_TECHNOLOGY, "completedTechnology"),
                    readString(buffer),
                    readStringIntMap(buffer, 16, "requiredResearchInputs", 1, 4_096),
                    readStringIntMap(buffer, 16, "deliveredResearchInputs", 1, 4_096),
                    readString(buffer),
                    nonNegative(buffer.readVarInt(), "researchProgress"),
                    nonNegative(buffer.readVarInt(), "researchRequired"),
                    nonNegative(buffer.readVarInt(), "kingdomRevision"),
                    readStringSet(buffer, MAX_EFFECTIVE_CAPABILITIES, "effectiveCapabilities"),
                )
            } else {
                GameplayProjection.empty()
            }
            return PlayerCampaignAttachmentState(
                CURRENT_SCHEMA_VERSION,
                playerId,
                campaign,
                force,
                gameplay,
            )
        }

        private fun writeStringIntMap(
            buffer: RegistryFriendlyByteBuf,
            values: Map<String, Int>,
            maximum: Int,
            label: String,
        ) {
            writeSize(buffer, values.size, maximum, label)
            values.toSortedMap().forEach { (key, value) ->
                writeString(buffer, key)
                buffer.writeVarInt(value)
            }
        }

        private fun readStringIntMap(
            buffer: RegistryFriendlyByteBuf,
            maximum: Int,
            label: String,
            minimumValue: Int = -100,
            maximumValue: Int = 100,
        ): Map<String, Int> {
            val size = readSize(buffer, maximum, label)
            val values = LinkedHashMap<String, Int>(size)
            repeat(size) {
                val key = readString(buffer)
                val value = buffer.readVarInt()
                require(value in minimumValue..maximumValue && values.put(key, value) == null) {
                    "Invalid or duplicate value in $label"
                }
            }
            return values
        }

        private fun writeEventTotals(
            buffer: RegistryFriendlyByteBuf,
            values: Map<ProgressionEventType, Int>,
        ) {
            writeSize(buffer, values.size, ProgressionEventType.values().size, "eventTotals")
            values.toSortedMap(compareBy { it.ordinal }).forEach { (type, total) ->
                writeString(buffer, type.name.lowercase(Locale.ROOT))
                buffer.writeVarInt(total)
            }
        }

        private fun readEventTotals(buffer: RegistryFriendlyByteBuf): Map<ProgressionEventType, Int> {
            val size = readSize(buffer, ProgressionEventType.values().size, "eventTotals")
            val values = LinkedHashMap<ProgressionEventType, Int>(size)
            repeat(size) {
                val type = readEventType(buffer)
                require(values.put(type, nonNegative(buffer.readVarInt(), "event total")) == null) {
                    "Duplicate event total for $type"
                }
            }
            return values
        }

        private fun writeEventSubjects(
            buffer: RegistryFriendlyByteBuf,
            values: Map<ProgressionEventType, Set<String>>,
        ) {
            writeSize(buffer, values.size, ProgressionEventType.values().size, "eventSubjects")
            values.toSortedMap(compareBy { it.ordinal }).forEach { (type, subjects) ->
                writeString(buffer, type.name.lowercase(Locale.ROOT))
                writeStringSet(buffer, subjects, MAX_SUBJECTS_PER_TYPE)
            }
        }

        private fun readEventSubjects(
            buffer: RegistryFriendlyByteBuf,
        ): Map<ProgressionEventType, Set<String>> {
            val size = readSize(buffer, ProgressionEventType.values().size, "eventSubjects")
            val values = LinkedHashMap<ProgressionEventType, Set<String>>(size)
            repeat(size) {
                val type = readEventType(buffer)
                require(type != ProgressionEventType.DELIVERY_COMPLETED) {
                    "delivery replay subjects are server-only"
                }
                require(values.put(
                    type,
                    readStringSet(buffer, MAX_SUBJECTS_PER_TYPE, "event subjects"),
                ) == null) { "Duplicate event subjects for $type" }
            }
            return values
        }

        private fun writeCooldowns(buffer: RegistryFriendlyByteBuf, values: Map<String, Long>) {
            writeSize(buffer, values.size, MAX_FORCE_COOLDOWNS, "force cooldowns")
            values.toSortedMap().forEach { (abilityId, cooldownEnd) ->
                writeString(buffer, abilityId)
                buffer.writeLong(cooldownEnd)
            }
        }

        private fun readCooldowns(buffer: RegistryFriendlyByteBuf): Map<String, Long> {
            val size = readSize(buffer, MAX_FORCE_COOLDOWNS, "force cooldowns")
            val values = LinkedHashMap<String, Long>(size)
            repeat(size) {
                val abilityId = readString(buffer)
                require(!values.containsKey(abilityId)) { "Duplicate Force cooldown for $abilityId" }
                values[abilityId] = buffer.readLong()
            }
            return values
        }

        private fun writeStringSet(
            buffer: RegistryFriendlyByteBuf,
            values: Set<String>,
            maximum: Int,
        ) {
            writeSize(buffer, values.size, maximum, "string set")
            values.sorted().forEach { writeString(buffer, it) }
        }

        private fun readStringSet(
            buffer: RegistryFriendlyByteBuf,
            maximum: Int,
            label: String,
        ): Set<String> {
            val size = readSize(buffer, maximum, label)
            val values = LinkedHashSet<String>(size)
            repeat(size) {
                require(values.add(readString(buffer))) { "Duplicate string in $label" }
            }
            return values
        }

        private fun readEventType(buffer: RegistryFriendlyByteBuf): ProgressionEventType {
            val value = readString(buffer)
            return try {
                ProgressionEventType.valueOf(value.uppercase(Locale.ROOT))
            } catch (exception: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown progression event type $value", exception)
            }
        }

        private fun writeString(buffer: RegistryFriendlyByteBuf, value: String) {
            require(validIdentifier(value) || value.isEmpty()) { "Invalid attachment string $value" }
            buffer.writeUtf(value, MAX_IDENTIFIER_LENGTH)
        }

        private fun readString(buffer: RegistryFriendlyByteBuf): String =
            buffer.readUtf(MAX_IDENTIFIER_LENGTH)

        private fun writeSize(buffer: RegistryFriendlyByteBuf, size: Int, maximum: Int, label: String) {
            require(size in 0..maximum) { "$label size $size exceeds $maximum" }
            buffer.writeVarInt(size)
        }

        private fun readSize(buffer: RegistryFriendlyByteBuf, maximum: Int, label: String): Int {
            val size = buffer.readVarInt()
            require(size in 0..maximum) { "$label size $size exceeds $maximum" }
            return size
        }

        private fun nonNegative(value: Int, label: String): Int {
            require(value >= 0) { "$label cannot be negative" }
            return value
        }

        private fun validIdentifier(value: String): Boolean =
            value.isNotBlank() && value.length <= MAX_IDENTIFIER_LENGTH

        private fun <T : Any> immutableSet(source: Set<T>, maximum: Int, label: String): Set<T> {
            require(source.size <= maximum) { "$label size ${source.size} exceeds $maximum" }
            val copy = LinkedHashSet<T>(source.size)
            source.forEach { copy.add(Objects.requireNonNull(it, label)) }
            return Collections.unmodifiableSet(copy)
        }

        private fun <K : Any, V : Any> immutableMap(
            source: Map<K, V>,
            maximum: Int,
            label: String,
        ): Map<K, V> {
            require(source.size <= maximum) { "$label size ${source.size} exceeds $maximum" }
            val copy = LinkedHashMap<K, V>(source.size)
            source.forEach { (key, value) ->
                copy[Objects.requireNonNull(key, "$label key")] =
                    Objects.requireNonNull(value, "$label value")
            }
            return Collections.unmodifiableMap(copy)
        }

        private fun immutableSubjectMap(
            source: Map<ProgressionEventType, Set<String>>,
        ): Map<ProgressionEventType, Set<String>> {
            require(source.size <= ProgressionEventType.values().size) {
                "eventSubjects contains too many event types"
            }
            val copy = LinkedHashMap<ProgressionEventType, Set<String>>(source.size)
            source.forEach { (type, subjects) ->
                copy[Objects.requireNonNull(type, "eventSubjects type")] = immutableSet(
                    Objects.requireNonNull(subjects, "eventSubjects values"),
                    MAX_SUBJECTS_PER_TYPE,
                    "eventSubjects subject",
                )
            }
            return Collections.unmodifiableMap(copy)
        }
    }
}
