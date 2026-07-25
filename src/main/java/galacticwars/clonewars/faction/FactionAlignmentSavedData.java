package galacticwars.clonewars.faction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import galacticwars.clonewars.GalacticWars;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class FactionAlignmentSavedData extends SavedData {
    public static final int CURRENT_SCHEMA_VERSION = 2;
    public static final int MAX_PROCESSED_EVENTS_PER_PLAYER = 256;
    private static final int MIN_ALIGNMENT = -100;
    private static final int MAX_ALIGNMENT = 100;

    private static final Codec<PlayerScores> PLAYER_SCORES_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(PlayerScores::playerId),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("scores", Map.of())
                    .forGetter(PlayerScores::scores),
            UUIDUtil.CODEC.listOf().optionalFieldOf("processed_events", List.of())
                    .forGetter(PlayerScores::processedEvents)
    ).apply(instance, PlayerScores::new));

    public static final Codec<FactionAlignmentSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA_VERSION)
                    .forGetter(data -> data.schemaVersion),
            PLAYER_SCORES_CODEC.listOf().optionalFieldOf("players", List.of())
                    .forGetter(FactionAlignmentSavedData::serializedPlayers)
    ).apply(instance, FactionAlignmentSavedData::new));

    public static final SavedDataType<FactionAlignmentSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "faction_alignments"),
            FactionAlignmentSavedData::new,
            CODEC,
            null);

    private final int schemaVersion;
    private final Map<UUID, FactionAlignment> alignments = new LinkedHashMap<>();
    private final Map<UUID, LinkedHashSet<UUID>> processedEvents = new LinkedHashMap<>();

    public FactionAlignmentSavedData() {
        this(CURRENT_SCHEMA_VERSION, List.of());
    }

    private FactionAlignmentSavedData(int schemaVersion, List<PlayerScores> players) {
        this.schemaVersion = Math.max(CURRENT_SCHEMA_VERSION, schemaVersion);
        for (PlayerScores player : players) {
            LinkedHashMap<FactionId, Integer> scores = new LinkedHashMap<>();
            player.scores().forEach((id, score) -> scores.put(FactionId.of(id), clamp(score)));
            alignments.put(player.playerId(), new FactionAlignment(player.playerId(), scores));
            LinkedHashSet<UUID> events = new LinkedHashSet<>();
            player.processedEvents().stream()
                    .skip(Math.max(0, player.processedEvents().size()
                            - MAX_PROCESSED_EVENTS_PER_PLAYER))
                    .forEach(events::add);
            if (!events.isEmpty()) {
                processedEvents.put(player.playerId(), events);
            }
        }
    }

    public static FactionAlignmentSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public FactionAlignment alignment(UUID playerId) {
        return alignments.getOrDefault(playerId, FactionAlignment.empty(playerId));
    }

    public boolean hasStoredAlignment(UUID playerId) {
        return alignments.containsKey(playerId);
    }

    /** Compare-and-restore compensation for a failed server-tick gameplay transaction. */
    public boolean restoreAfterFailedTransaction(
            UUID playerId,
            FactionAlignment expectedCurrent,
            FactionAlignment previous,
            boolean previousWasStored
    ) {
        if (!alignment(playerId).equals(expectedCurrent) || !previous.playerId().equals(playerId)) {
            return false;
        }
        if (previousWasStored) {
            alignments.put(playerId, previous);
        } else {
            alignments.remove(playerId);
        }
        this.setDirty();
        return true;
    }

    public FactionAlignmentUpdateResult applyPledge(
            UUID playerId,
            FactionDefinition faction,
            FactionCatalog catalog
    ) {
        return applyRule(
                playerId,
                catalog,
                faction.id(),
                new FactionAlignmentRule(
                        faction.pledgeDirectDelta(),
                        faction.pledgeAllyDelta(),
                        faction.pledgeEnemyDelta(),
                        "faction_pledge"));
    }

    /**
     * Applies an authoritative event once. The bounded ledger is persisted with the
     * score update so transaction replays cannot farm reputation.
     */
    public FactionAlignmentEventResult applyEvent(
            UUID playerId,
            UUID eventId,
            FactionCatalog catalog,
            FactionId sourceFaction,
            FactionAlignmentRule rule
    ) {
        LinkedHashSet<UUID> events = processedEvents.computeIfAbsent(
                playerId, ignored -> new LinkedHashSet<>());
        if (events.contains(eventId)) {
            return FactionAlignmentEventResult.duplicate(alignment(playerId));
        }
        FactionAlignmentUpdateResult update = applyRule(
                playerId, catalog, sourceFaction, rule);
        events.add(eventId);
        while (events.size() > MAX_PROCESSED_EVENTS_PER_PLAYER) {
            events.remove(events.iterator().next());
        }
        this.setDirty();
        return new FactionAlignmentEventResult(false, update);
    }

    public boolean processed(UUID playerId, UUID eventId) {
        return processedEvents.getOrDefault(playerId, new LinkedHashSet<>()).contains(eventId);
    }

    public int processedEventCount(UUID playerId) {
        return processedEvents.getOrDefault(playerId, new LinkedHashSet<>()).size();
    }

    private FactionAlignmentUpdateResult applyRule(
            UUID playerId,
            FactionCatalog catalog,
            FactionId sourceFaction,
            FactionAlignmentRule rule
    ) {
        FactionAlignment beforeAlignment = alignment(playerId);
        FactionAlignmentUpdateResult raw = FactionAlignmentUpdater.apply(
                beforeAlignment,
                catalog,
                sourceFaction,
                rule);
        LinkedHashMap<FactionId, Integer> clampedScores = new LinkedHashMap<>();
        raw.alignment().scores().forEach((id, score) -> clampedScores.put(id, clamp(score)));
        FactionAlignment updated = new FactionAlignment(playerId, clampedScores);

        ArrayList<FactionAlignmentChange> changes = new ArrayList<>();
        for (FactionAlignmentChange change : raw.changes()) {
            int before = alignmentFromChanges(raw, change.factionId(), change.beforeScore());
            int after = updated.score(change.factionId());
            if (after != before) {
                changes.add(new FactionAlignmentChange(
                        change.factionId(), before, after - before, after, change.reasonCode()));
            }
        }
        alignments.put(playerId, updated);
        this.setDirty();
        return new FactionAlignmentUpdateResult(updated, List.copyOf(changes));
    }

    public void setScore(UUID playerId, FactionId factionId, int score) {
        FactionAlignment current = alignment(playerId);
        LinkedHashMap<FactionId, Integer> scores = new LinkedHashMap<>(current.scores());
        scores.put(factionId, clamp(score));
        alignments.put(playerId, new FactionAlignment(playerId, scores));
        this.setDirty();
    }

    private List<PlayerScores> serializedPlayers() {
        ArrayList<PlayerScores> players = new ArrayList<>();
        LinkedHashSet<UUID> playerIds = new LinkedHashSet<>(alignments.keySet());
        playerIds.addAll(processedEvents.keySet());
        for (UUID playerId : playerIds) {
            FactionAlignment alignment = alignment(playerId);
            LinkedHashMap<String, Integer> scores = new LinkedHashMap<>();
            alignment.scores().forEach((id, score) -> scores.put(id.toString(), clamp(score)));
            players.add(new PlayerScores(
                    alignment.playerId(),
                    scores,
                    List.copyOf(processedEvents.getOrDefault(playerId, new LinkedHashSet<>()))));
        }
        return List.copyOf(players);
    }

    private static int alignmentFromChanges(
            FactionAlignmentUpdateResult raw,
            FactionId factionId,
            int fallback
    ) {
        return raw.changes().stream()
                .filter(change -> change.factionId().equals(factionId))
                .map(FactionAlignmentChange::beforeScore)
                .findFirst()
                .orElse(fallback);
    }

    private static int clamp(int score) {
        return Math.max(MIN_ALIGNMENT, Math.min(MAX_ALIGNMENT, score));
    }

    private record PlayerScores(
            UUID playerId,
            Map<String, Integer> scores,
            List<UUID> processedEvents
    ) {
        private PlayerScores {
            scores = Map.copyOf(scores);
            processedEvents = List.copyOf(processedEvents);
        }
    }
}
