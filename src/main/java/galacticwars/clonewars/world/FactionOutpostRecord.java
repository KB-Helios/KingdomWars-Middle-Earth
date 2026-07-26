package galacticwars.clonewars.world;

import galacticwars.clonewars.recruitment.NpcServiceBranch;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public record FactionOutpostRecord(
        UUID id,
        String factionId,
        String dimensionId,
        int x,
        int y,
        int z,
        int radius,
        List<UUID> militaryNpcIds,
        List<UUID> civilianNpcIds,
        long lastActivityGameTime,
        BlueprintSiteKind siteKind,
        Optional<BlockPos> commandPostPosition
) {
    public FactionOutpostRecord {
        Objects.requireNonNull(id, "id");
        factionId = required(factionId, "factionId");
        dimensionId = required(dimensionId, "dimensionId");
        if (radius < 16 || lastActivityGameTime < 0) throw new IllegalArgumentException("invalid outpost bounds");
        militaryNpcIds = List.copyOf(new LinkedHashSet<>(Objects.requireNonNull(militaryNpcIds, "militaryNpcIds")));
        civilianNpcIds = List.copyOf(new LinkedHashSet<>(Objects.requireNonNull(civilianNpcIds, "civilianNpcIds")));
        if (militaryNpcIds.stream().anyMatch(civilianNpcIds::contains)) {
            throw new IllegalArgumentException("NPC cannot be both military and civilian");
        }
        siteKind = Objects.requireNonNull(siteKind, "siteKind");
        commandPostPosition = commandPostPosition == null ? Optional.empty() : commandPostPosition;
        if ((siteKind == BlueprintSiteKind.COMMAND_CENTER) != commandPostPosition.isPresent()) {
            throw new IllegalArgumentException("command center site kind and command post must be defined together");
        }
    }

    public FactionOutpostRecord(
            UUID id,
            String factionId,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius,
            List<UUID> militaryNpcIds,
            List<UUID> civilianNpcIds,
            long lastActivityGameTime
    ) {
        this(id, factionId, dimensionId, x, y, z, radius, militaryNpcIds, civilianNpcIds,
                lastActivityGameTime, BlueprintSiteKind.OUTPOST, Optional.empty());
    }

    public static FactionOutpostRecord create(
            String factionId, String dimensionId, int x, int y, int z, int radius, long gameTime
    ) {
        return new FactionOutpostRecord(UUID.randomUUID(), factionId, dimensionId,
                x, y, z, radius, List.of(), List.of(), gameTime);
    }

    public boolean contains(UUID npcId) {
        return militaryNpcIds.contains(npcId) || civilianNpcIds.contains(npcId);
    }

    public long distanceSquared(int targetX, int targetZ) {
        long dx = (long) x - targetX;
        long dz = (long) z - targetZ;
        return dx * dx + dz * dz;
    }

    public FactionOutpostRecord withNpc(UUID npcId, NpcServiceBranch branch, long gameTime) {
        Objects.requireNonNull(npcId, "npcId");
        Objects.requireNonNull(branch, "branch");
        LinkedHashSet<UUID> military = new LinkedHashSet<>(militaryNpcIds);
        LinkedHashSet<UUID> civilians = new LinkedHashSet<>(civilianNpcIds);
        military.remove(npcId);
        civilians.remove(npcId);
        (branch == NpcServiceBranch.MILITARY ? military : civilians).add(npcId);
        return new FactionOutpostRecord(id, factionId, dimensionId, x, y, z, radius,
                List.copyOf(military), List.copyOf(civilians), gameTime, siteKind, commandPostPosition);
    }

    public FactionOutpostRecord withoutNpc(UUID npcId, long gameTime) {
        if (!contains(npcId)) return this;
        return new FactionOutpostRecord(id, factionId, dimensionId, x, y, z, radius,
                militaryNpcIds.stream().filter(existingNpcId -> !existingNpcId.equals(npcId)).toList(),
                civilianNpcIds.stream().filter(existingNpcId -> !existingNpcId.equals(npcId)).toList(), gameTime,
                siteKind, commandPostPosition);
    }

    public FactionOutpostRecord relocatedTo(int targetX, int targetY, int targetZ, long gameTime) {
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        return new FactionOutpostRecord(
                id,
                factionId,
                dimensionId,
                targetX,
                targetY,
                targetZ,
                radius,
                militaryNpcIds,
                civilianNpcIds,
                Math.max(lastActivityGameTime, gameTime),
                siteKind,
                commandPostPosition.map(position -> position.offset(
                        targetX - x, targetY - y, targetZ - z)));
    }

    private static String required(String value, String label) {
        value = Objects.requireNonNull(value, label).trim().toLowerCase(java.util.Locale.ROOT);
        if (value.isEmpty()) throw new IllegalArgumentException(label + " cannot be blank");
        return value;
    }
}
