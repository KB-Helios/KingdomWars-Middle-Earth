package galacticwars.clonewars.faction.ai;

import galacticwars.clonewars.faction.FactionAlignmentRule;
import galacticwars.clonewars.faction.FactionId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Validated, reloadable policy for one faction's embodied NPC reactions. */
public record NpcAiProfile(
        FactionId factionId,
        Map<NpcRole, RoleScanSettings> roleSettings,
        int coordinationRadius,
        int maxResponders,
        int warningCooldownTicks,
        int alertDurationTicks,
        int friendlyThreshold,
        int neutralThreshold,
        int waryThreshold,
        int friendlyTradePricePercent,
        Map<FactionReputationEvent, FactionAlignmentRule> reputationRules
) {
    public static final int SCHEMA_VERSION = 1;

    public NpcAiProfile {
        Objects.requireNonNull(factionId, "factionId");
        roleSettings = immutableEnumMap(roleSettings, NpcRole.class, "roleSettings");
        reputationRules = immutableEnumMap(
                reputationRules, FactionReputationEvent.class, "reputationRules");
        for (NpcRole role : NpcRole.values()) {
            if (!roleSettings.containsKey(role)) {
                throw new IllegalArgumentException("Missing scan settings for role " + role.id());
            }
        }
        for (FactionReputationEvent event : FactionReputationEvent.values()) {
            if (!reputationRules.containsKey(event)) {
                throw new IllegalArgumentException("Missing reputation rule for event " + event.id());
            }
        }
        if (coordinationRadius < 8 || coordinationRadius > 64) {
            throw new IllegalArgumentException("coordinationRadius must be between 8 and 64");
        }
        if (maxResponders < 1 || maxResponders > 32) {
            throw new IllegalArgumentException("maxResponders must be between 1 and 32");
        }
        if (warningCooldownTicks < 20 || warningCooldownTicks > 12_000) {
            throw new IllegalArgumentException("warningCooldownTicks is outside safe bounds");
        }
        if (alertDurationTicks < 20 || alertDurationTicks > 72_000) {
            throw new IllegalArgumentException("alertDurationTicks is outside safe bounds");
        }
        if (friendlyThreshold > 100 || waryThreshold < -100
                || friendlyThreshold <= neutralThreshold
                || neutralThreshold <= waryThreshold) {
            throw new IllegalArgumentException(
                    "Disposition thresholds must descend from friendly to wary");
        }
        if (friendlyTradePricePercent < 1 || friendlyTradePricePercent > 100) {
            throw new IllegalArgumentException(
                    "friendlyTradePricePercent must be between 1 and 100");
        }
    }

    public RoleScanSettings settings(NpcRole role) {
        return roleSettings.get(Objects.requireNonNull(role, "role"));
    }

    public FactionAlignmentRule rule(FactionReputationEvent event) {
        return reputationRules.get(Objects.requireNonNull(event, "event"));
    }

    public static NpcAiProfile defaults(FactionId factionId) {
        EnumMap<NpcRole, RoleScanSettings> roles = new EnumMap<>(NpcRole.class);
        roles.put(NpcRole.COMMANDER, new RoleScanSettings(10, 32));
        roles.put(NpcRole.TROOPER, new RoleScanSettings(10, 32));
        roles.put(NpcRole.TRADER, new RoleScanSettings(20, 32));
        roles.put(NpcRole.CIVILIAN, new RoleScanSettings(20, 32));

        EnumMap<FactionReputationEvent, FactionAlignmentRule> rules =
                new EnumMap<>(FactionReputationEvent.class);
        rules.put(FactionReputationEvent.NPC_DAMAGED,
                new FactionAlignmentRule(-5, -2, 1, "npc_damaged"));
        rules.put(FactionReputationEvent.NPC_KILLED,
                new FactionAlignmentRule(-20, -5, 5, "npc_killed"));
        rules.put(FactionReputationEvent.TRADE_COMPLETED,
                new FactionAlignmentRule(1, 0, 0, "trade_completed"));
        rules.put(FactionReputationEvent.DELIVERY_COMPLETED,
                new FactionAlignmentRule(3, 1, -1, "delivery_completed"));
        rules.put(FactionReputationEvent.MISSION_COMPLETED,
                new FactionAlignmentRule(10, 3, -3, "mission_completed"));
        rules.put(FactionReputationEvent.OUTPOST_DEFENDED,
                new FactionAlignmentRule(8, 2, -2, "outpost_defended"));
        return new NpcAiProfile(
                factionId, roles, 48, 12, 100, 600,
                10, 0, -20, 90, rules);
    }

    private static <K extends Enum<K>, V> Map<K, V> immutableEnumMap(
            Map<K, V> source,
            Class<K> keyType,
            String label
    ) {
        Objects.requireNonNull(source, label);
        EnumMap<K, V> copy = new EnumMap<>(keyType);
        source.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, label + " key"),
                Objects.requireNonNull(value, label + " value")));
        return Collections.unmodifiableMap(copy);
    }

    public record RoleScanSettings(int scanIntervalTicks, int scanRadius) {
        public RoleScanSettings {
            if (scanIntervalTicks < 1 || scanIntervalTicks > 1_200) {
                throw new IllegalArgumentException("scanIntervalTicks is outside safe bounds");
            }
            if (scanRadius < 4 || scanRadius > 64) {
                throw new IllegalArgumentException("scanRadius must be between 4 and 64");
            }
        }
    }
}
