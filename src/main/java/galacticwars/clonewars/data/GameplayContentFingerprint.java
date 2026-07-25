package galacticwars.clonewars.data;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class GameplayContentFingerprint {
    private GameplayContentFingerprint() {
    }

    static String compute(GameplayDataSnapshot snapshot) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "galactic-wars-gameplay-content-v2");
            updateMap(digest, "faction", snapshot.factions().definitions(), GameplayContentFingerprint::canonical);
            updateMap(digest, "unit", snapshot.units().definitions(), GameplayContentFingerprint::canonical);
            updateMap(digest, "unit_alias", snapshot.unitAliases(), GameplayContentFingerprint::canonical);
            updateMap(digest, "blueprint", snapshot.blueprints(), GameplayContentFingerprint::canonical);
            updateMap(digest, "spawn_profile", snapshot.overworldSpawnProfiles(), GameplayContentFingerprint::canonical);
            updateMap(digest, "civilian", snapshot.civilianArchetypesByEntityType(), GameplayContentFingerprint::canonical);
            updateMap(digest, "ability", snapshot.abilities(), GameplayContentFingerprint::canonical);
            updateMap(digest, "class", snapshot.unitClasses(), GameplayContentFingerprint::canonical);
            updateMap(digest, "faction_policy", snapshot.factionPolicies(), GameplayContentFingerprint::canonical);
            updateMap(digest, "npc_ai_profile", snapshot.npcAiProfiles(), GameplayContentFingerprint::canonical);
            LaunchContentDefinitions launch = snapshot.launchContent();
            updateMap(digest, "planet", launch.planets(), GameplayContentFingerprint::canonical);
            updateMap(digest, "vehicle", launch.vehicles(), GameplayContentFingerprint::canonical);
            updateMap(digest, "force", launch.forceAbilities(), GameplayContentFingerprint::canonical);
            updateMap(digest, "force_tradition", launch.forceTraditions(), GameplayContentFingerprint::canonical);
            updateMap(digest, "force_node", launch.forceNodes(), GameplayContentFingerprint::canonical);
            updateMap(digest, "quest", launch.quests(), GameplayContentFingerprint::canonical);
            updateMap(digest, "mission", launch.missions(), GameplayContentFingerprint::canonical);
            updateMap(digest, "trade", launch.trades(), GameplayContentFingerprint::canonical);
            updateMap(digest, "region", launch.conquestRegions(), GameplayContentFingerprint::canonical);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static <K, V> void updateMap(
            MessageDigest digest,
            String label,
            Map<K, V> values,
            Function<V, String> formatter
    ) {
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((left, right) -> left.toString().compareTo(right.toString())))
                .forEach(entry -> update(digest,
                        label + "\u0000" + canonical(entry.getKey()) + "\u0000" + formatter.apply(entry.getValue())));
    }

    private static String canonical(Object value) {
        StringBuilder result = new StringBuilder();
        appendCanonical(result, value);
        return result.toString();
    }

    private static void appendCanonical(StringBuilder result, Object value) {
        if (value == null) {
            result.append("null;");
            return;
        }
        if (value instanceof CharSequence || value instanceof Character
                || value instanceof Number || value instanceof Boolean) {
            appendToken(result, value.getClass().getName());
            appendToken(result, value.toString());
            return;
        }
        if (value instanceof Enum<?> enumValue) {
            appendToken(result, enumValue.getDeclaringClass().getName());
            appendToken(result, enumValue.name());
            return;
        }
        if (value instanceof Optional<?> optional) {
            result.append("optional[");
            optional.ifPresent(item -> appendCanonical(result, item));
            result.append("];");
            return;
        }
        if (value instanceof Map<?, ?> map) {
            ArrayList<String> entries = new ArrayList<>(map.size());
            map.forEach((key, item) -> entries.add(canonical(key) + canonical(item)));
            entries.sort(Comparator.naturalOrder());
            result.append("map[");
            entries.forEach(entry -> appendToken(result, entry));
            result.append("];");
            return;
        }
        if (value instanceof Set<?> set) {
            ArrayList<String> entries = new ArrayList<>(set.size());
            set.forEach(item -> entries.add(canonical(item)));
            entries.sort(Comparator.naturalOrder());
            result.append("set[");
            entries.forEach(entry -> appendToken(result, entry));
            result.append("];");
            return;
        }
        if (value instanceof Collection<?> collection) {
            result.append("collection[");
            collection.forEach(item -> appendCanonical(result, item));
            result.append("];");
            return;
        }
        if (value.getClass().isArray()) {
            result.append("array[");
            for (int index = 0; index < Array.getLength(value); index++) {
                appendCanonical(result, Array.get(value, index));
            }
            result.append("];");
            return;
        }
        if (value.getClass().isRecord()) {
            appendToken(result, value.getClass().getName());
            result.append('{');
            for (RecordComponent component : value.getClass().getRecordComponents()) {
                appendToken(result, component.getName());
                try {
                    appendCanonical(result, component.getAccessor().invoke(value));
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Cannot fingerprint " + value.getClass().getName(), exception);
                }
            }
            result.append("};");
            return;
        }
        appendToken(result, value.getClass().getName());
        appendToken(result, value.toString());
    }

    private static void appendToken(StringBuilder result, String value) {
        result.append(value.length()).append(':').append(value).append(';');
    }

    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update((byte) (bytes.length >>> 24));
        digest.update((byte) (bytes.length >>> 16));
        digest.update((byte) (bytes.length >>> 8));
        digest.update((byte) bytes.length);
        digest.update(bytes);
    }
}
