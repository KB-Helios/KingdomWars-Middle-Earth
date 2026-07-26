package galacticwars.clonewars.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class FactionOutpostSavedData extends SavedData {
    public static final int SCHEMA_VERSION = 4;
    public static final int MAX_PERSISTED_ALERTS = 256;
    private static final Codec<FactionOutpostRecord> OUTPOST_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(FactionOutpostRecord::id),
            Codec.STRING.fieldOf("faction_id").forGetter(FactionOutpostRecord::factionId),
            Codec.STRING.fieldOf("dimension").forGetter(FactionOutpostRecord::dimensionId),
            Codec.INT.fieldOf("x").forGetter(FactionOutpostRecord::x),
            Codec.INT.fieldOf("y").forGetter(FactionOutpostRecord::y),
            Codec.INT.fieldOf("z").forGetter(FactionOutpostRecord::z),
            Codec.intRange(8, 512).fieldOf("radius").forGetter(FactionOutpostRecord::radius),
            UUIDUtil.CODEC.listOf().optionalFieldOf("military_npcs", List.of())
                    .forGetter(FactionOutpostRecord::militaryNpcIds),
            UUIDUtil.CODEC.listOf().optionalFieldOf("civilian_npcs", List.of())
                    .forGetter(FactionOutpostRecord::civilianNpcIds),
            Codec.LONG.optionalFieldOf("last_activity", 0L).forGetter(FactionOutpostRecord::lastActivityGameTime),
            BlueprintSiteKind.CODEC.optionalFieldOf("site_kind", BlueprintSiteKind.OUTPOST)
                    .forGetter(FactionOutpostRecord::siteKind),
            BlockPos.CODEC.optionalFieldOf("command_post")
                    .forGetter(FactionOutpostRecord::commandPostPosition)
    ).apply(instance, FactionOutpostRecord::new));

    private static final Codec<FactionOutpostSiteProgress> SITE_PROGRESS_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("outpost_id").forGetter(FactionOutpostSiteProgress::outpostId),
                    Codec.intRange(0, FactionOutpostSitePlan.candidateCount() - 1)
                            .fieldOf("next_candidate")
                            .forGetter(FactionOutpostSiteProgress::nextCandidateIndex),
                    Codec.LONG.fieldOf("next_attempt_game_time")
                            .forGetter(FactionOutpostSiteProgress::nextAttemptGameTime)
            ).apply(instance, FactionOutpostSiteProgress::new));

    private static final Codec<OutpostAlert> ALERT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("outpost_id").forGetter(OutpostAlert::outpostId),
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(OutpostAlert::playerId),
            Codec.LONG.fieldOf("raised_at").forGetter(OutpostAlert::raisedAt),
            Codec.LONG.fieldOf("expires_at").forGetter(OutpostAlert::expiresAt),
            Codec.STRING.fieldOf("reason").forGetter(OutpostAlert::reason)
    ).apply(instance, OutpostAlert::new));

    public static final Codec<FactionOutpostSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", 1).forGetter(data -> SCHEMA_VERSION),
            OUTPOST_CODEC.listOf().optionalFieldOf("outposts", List.of()).forGetter(FactionOutpostSavedData::outposts),
            UUIDUtil.CODEC.listOf().optionalFieldOf("generated_sites", List.of())
                    .forGetter(data -> List.copyOf(data.generatedSiteIds)),
            SITE_PROGRESS_CODEC.listOf().optionalFieldOf("site_progress", List.of())
                    .forGetter(data -> List.copyOf(data.siteProgressByOutpost.values())),
            ALERT_CODEC.listOf().optionalFieldOf("alerts", List.of())
                    .forGetter(data -> List.copyOf(data.alertsByTarget.values()))
    ).apply(instance, FactionOutpostSavedData::new));

    public static final SavedDataType<FactionOutpostSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "overworld_faction_outposts"),
            FactionOutpostSavedData::new,
            CODEC,
            null);

    private final Map<UUID, FactionOutpostRecord> outpostsById = new LinkedHashMap<>();
    private final Map<UUID, UUID> outpostIdsByNpc = new LinkedHashMap<>();
    private final LinkedHashSet<UUID> generatedSiteIds = new LinkedHashSet<>();
    private final Map<UUID, FactionOutpostSiteProgress> siteProgressByOutpost = new LinkedHashMap<>();
    private final Map<AlertTarget, OutpostAlert> alertsByTarget = new LinkedHashMap<>();

    public FactionOutpostSavedData() {
    }

    private FactionOutpostSavedData(
            int schemaVersion,
            List<FactionOutpostRecord> outposts,
            List<UUID> generatedSiteIds,
            List<FactionOutpostSiteProgress> siteProgress,
            List<OutpostAlert> alerts
    ) {
        if (schemaVersion > SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported faction outpost schema " + schemaVersion);
        }
        outposts.forEach(this::index);
        this.generatedSiteIds.addAll(generatedSiteIds);
        this.generatedSiteIds.retainAll(outpostsById.keySet());
        for (FactionOutpostSiteProgress progress : siteProgress) {
            if (!outpostsById.containsKey(progress.outpostId())
                    || this.generatedSiteIds.contains(progress.outpostId())) {
                continue;
            }
            if (siteProgressByOutpost.putIfAbsent(progress.outpostId(), progress) != null) {
                throw new IllegalArgumentException(
                        "duplicate faction outpost site progress " + progress.outpostId());
            }
        }
        for (OutpostAlert alert : alerts.stream()
                .skip(Math.max(0, alerts.size() - MAX_PERSISTED_ALERTS)).toList()) {
            if (!outpostsById.containsKey(alert.outpostId())) {
                continue;
            }
            AlertTarget key = new AlertTarget(alert.outpostId(), alert.playerId());
            OutpostAlert previous = alertsByTarget.putIfAbsent(key, alert);
            if (previous != null && previous.expiresAt() < alert.expiresAt()) {
                alertsByTarget.put(key, alert);
            }
        }
    }

    public static FactionOutpostSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<FactionOutpostRecord> outposts() {
        return List.copyOf(outpostsById.values());
    }

    public Optional<FactionOutpostRecord> outpost(UUID id) {
        return Optional.ofNullable(outpostsById.get(id));
    }

    public Optional<FactionOutpostRecord> outpostForNpc(UUID npcId) {
        return Optional.ofNullable(outpostIdsByNpc.get(npcId)).flatMap(this::outpost);
    }

    public Optional<OutpostAlert> activeAlert(
            UUID outpostId,
            UUID playerId,
            long gameTime
    ) {
        OutpostAlert alert = alertsByTarget.get(new AlertTarget(outpostId, playerId));
        return alert != null && alert.activeAt(gameTime)
                ? Optional.of(alert)
                : Optional.empty();
    }

    public List<OutpostAlert> activeAlerts(UUID outpostId, long gameTime) {
        return alertsByTarget.values().stream()
                .filter(alert -> alert.outpostId().equals(outpostId) && alert.activeAt(gameTime))
                .sorted(java.util.Comparator.comparingLong(OutpostAlert::raisedAt))
                .toList();
    }

    public Optional<OutpostAlert> raiseAlert(
            UUID outpostId,
            UUID playerId,
            long gameTime,
            int durationTicks,
            String reason
    ) {
        if (!outpostsById.containsKey(outpostId)
                || gameTime < 0L
                || durationTicks < 1) {
            return Optional.empty();
        }
        pruneExpired(gameTime);
        AlertTarget key = new AlertTarget(outpostId, playerId);
        OutpostAlert previous = alertsByTarget.get(key);
        OutpostAlert alert = previous == null
                ? new OutpostAlert(
                        outpostId, playerId, gameTime,
                        Math.addExact(gameTime, durationTicks), reason)
                : previous.extend(gameTime, durationTicks, reason);
        alertsByTarget.put(key, alert);
        trimAlerts();
        this.setDirty();
        return Optional.of(alert);
    }

    public int pruneExpired(long gameTime) {
        int before = alertsByTarget.size();
        alertsByTarget.values().removeIf(alert -> !alert.activeAt(gameTime));
        int removed = before - alertsByTarget.size();
        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    public boolean siteGenerated(UUID outpostId) {
        return generatedSiteIds.contains(outpostId);
    }

    public void markSiteGenerated(UUID outpostId) {
        if (!outpostsById.containsKey(outpostId)) {
            return;
        }
        boolean changed = generatedSiteIds.add(outpostId);
        changed |= siteProgressByOutpost.remove(outpostId) != null;
        if (changed) {
            this.setDirty();
        }
    }

    /**
     * Claims the next bounded search window for an outpost. Advancing the persisted cursor before
     * returning makes the claim exclusive to a single resident for the configured retry interval.
     */
    public Optional<FactionOutpostSitePlan.AttemptWindow> claimSiteGenerationAttempt(
            UUID outpostId,
            long gameTime
    ) {
        if (!outpostsById.containsKey(outpostId) || generatedSiteIds.contains(outpostId)) {
            return Optional.empty();
        }
        FactionOutpostSiteProgress current = siteProgressByOutpost.getOrDefault(
                outpostId, FactionOutpostSiteProgress.initial(outpostId));
        Optional<FactionOutpostSiteProgress.Claim> claimed = current.claim(gameTime);
        if (claimed.isEmpty()) {
            return Optional.empty();
        }
        FactionOutpostSiteProgress.Claim claim = claimed.orElseThrow();
        siteProgressByOutpost.put(outpostId, claim.followingProgress());
        this.setDirty();
        return Optional.of(claim.window());
    }

    public Optional<FactionOutpostSiteProgress> siteGenerationProgress(UUID outpostId) {
        return Optional.ofNullable(siteProgressByOutpost.get(outpostId));
    }

    /** Atomically publishes the relocated logical center after the physical shelter is complete. */
    public Optional<FactionOutpostRecord> completeSiteGeneration(
            UUID outpostId,
            BlockPos generatedCenter,
            long gameTime
    ) {
        FactionOutpostRecord current = outpostsById.get(outpostId);
        if (current == null) {
            return Optional.empty();
        }
        if (generatedSiteIds.contains(outpostId)) {
            return Optional.of(current);
        }
        FactionOutpostRecord relocated = current.relocatedTo(
                generatedCenter.getX(), generatedCenter.getY(), generatedCenter.getZ(), gameTime);
        index(relocated);
        generatedSiteIds.add(outpostId);
        siteProgressByOutpost.remove(outpostId);
        this.setDirty();
        return Optional.of(relocated);
    }

    public Optional<FactionOutpostRecord> assignNaturalNpc(
            UUID npcId,
            OverworldFactionSpawnProfile profile,
            NpcServiceBranch branch,
            String dimensionId,
            BlockPos position,
            long gameTime
    ) {
        Optional<FactionOutpostRecord> existing = outpostForNpc(npcId);
        if (existing.isPresent()) return existing;
        FactionOutpostRecord nearest = outpostsById.values().stream()
                .filter(outpost -> outpost.dimensionId().equals(dimensionId)
                        && outpost.factionId().equals(profile.factionId()))
                .filter(outpost -> outpost.distanceSquared(position.getX(), position.getZ())
                        <= (long) outpost.radius() * outpost.radius())
                .min(java.util.Comparator.comparingLong(
                        outpost -> outpost.distanceSquared(position.getX(), position.getZ())))
                .orElse(null);
        if (nearest == null) {
            boolean overlaps = outpostsById.values().stream()
                    .filter(outpost -> outpost.dimensionId().equals(dimensionId))
                    .anyMatch(outpost -> outpost.distanceSquared(position.getX(), position.getZ())
                            < (long) profile.minimumOutpostSpacing() * profile.minimumOutpostSpacing());
            if (overlaps) return Optional.empty();
            nearest = FactionOutpostRecord.create(
                    profile.factionId(), dimensionId, position.getX(), position.getY(), position.getZ(),
                    profile.outpostRadius(), gameTime);
        }
        int population = branch == NpcServiceBranch.MILITARY
                ? nearest.militaryNpcIds().size() : nearest.civilianNpcIds().size();
        int capacity = branch == NpcServiceBranch.MILITARY
                ? profile.militaryCapacity() : profile.civilianCapacity();
        if (population >= capacity) return Optional.empty();
        FactionOutpostRecord updated = nearest.withNpc(npcId, branch, gameTime);
        index(updated);
        this.setDirty();
        return Optional.of(updated);
    }

    /** Publishes a generated site's complete identity before any residents become visible. */
    public FactionOutpostRecord registerGeneratedSite(
            UUID id,
            String factionId,
            String dimensionId,
            BlockPos position,
            int radius,
            List<UUID> militaryNpcIds,
            List<UUID> civilianNpcIds,
            long gameTime
    ) {
        FactionOutpostRecord outpost = publishGeneratedSiteRecord(
                id, factionId, dimensionId, position, radius, militaryNpcIds, civilianNpcIds,
                gameTime, BlueprintSiteKind.OUTPOST, Optional.empty());
        markSiteGenerated(id);
        return outpost;
    }

    public FactionOutpostRecord registerGeneratedSite(
            UUID id,
            String factionId,
            String dimensionId,
            BlockPos position,
            int radius,
            List<UUID> militaryNpcIds,
            List<UUID> civilianNpcIds,
            long gameTime,
            BlueprintSiteKind siteKind,
            Optional<BlockPos> commandPostPosition
    ) {
        FactionOutpostRecord outpost = publishGeneratedSiteRecord(
                id, factionId, dimensionId, position, radius, militaryNpcIds, civilianNpcIds,
                gameTime, siteKind, commandPostPosition);
        markSiteGenerated(id);
        return outpost;
    }

    /**
     * Publishes deterministic generated-site identity without marking initialization complete.
     * Callers can safely replay residents, command posts, and containers before
     * {@link #markSiteGenerated(UUID)} seals the transaction.
     */
    public FactionOutpostRecord publishGeneratedSiteRecord(
            UUID id,
            String factionId,
            String dimensionId,
            BlockPos position,
            int radius,
            List<UUID> militaryNpcIds,
            List<UUID> civilianNpcIds,
            long gameTime,
            BlueprintSiteKind siteKind,
            Optional<BlockPos> commandPostPosition
    ) {
        FactionOutpostRecord existing = outpostsById.get(id);
        if (existing != null) {
            if (!existing.factionId().equals(factionId)
                    || !existing.dimensionId().equals(dimensionId)
                    || existing.x() != position.getX()
                    || existing.y() != position.getY()
                    || existing.z() != position.getZ()) {
                return existing;
            }
            FactionOutpostRecord reconciled = new FactionOutpostRecord(
                    id, factionId, dimensionId,
                    position.getX(), position.getY(), position.getZ(), radius,
                    militaryNpcIds, civilianNpcIds,
                    Math.max(existing.lastActivityGameTime(), gameTime),
                    siteKind, commandPostPosition);
            if (!reconciled.equals(existing)) {
                index(reconciled);
                this.setDirty();
            }
            return reconciled;
        }
        FactionOutpostRecord created = new FactionOutpostRecord(id, factionId, dimensionId,
                position.getX(), position.getY(), position.getZ(), radius,
                militaryNpcIds, civilianNpcIds, gameTime, siteKind, commandPostPosition);
        index(created);
        this.setDirty();
        return created;
    }

    public boolean removeNpc(UUID npcId, long gameTime) {
        UUID outpostId = outpostIdsByNpc.remove(npcId);
        FactionOutpostRecord outpost = outpostId == null ? null : outpostsById.get(outpostId);
        if (outpost == null) return false;
        index(outpost.withoutNpc(npcId, gameTime));
        this.setDirty();
        return true;
    }

    private void trimAlerts() {
        while (alertsByTarget.size() > MAX_PERSISTED_ALERTS) {
            AlertTarget oldest = alertsByTarget.entrySet().stream()
                    .min(java.util.Comparator.comparingLong(
                            entry -> entry.getValue().expiresAt()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (oldest == null) {
                return;
            }
            alertsByTarget.remove(oldest);
        }
    }

    private void index(FactionOutpostRecord outpost) {
        FactionOutpostRecord previous = outpostsById.put(outpost.id(), outpost);
        if (previous != null) {
            previous.militaryNpcIds().forEach(id -> outpostIdsByNpc.remove(id, previous.id()));
            previous.civilianNpcIds().forEach(id -> outpostIdsByNpc.remove(id, previous.id()));
        }
        outpost.militaryNpcIds().forEach(id -> outpostIdsByNpc.put(id, outpost.id()));
        outpost.civilianNpcIds().forEach(id -> outpostIdsByNpc.put(id, outpost.id()));
    }

    private record AlertTarget(UUID outpostId, UUID playerId) {
    }
}
