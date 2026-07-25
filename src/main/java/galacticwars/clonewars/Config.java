package galacticwars.clonewars;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Authoritative gameplay policy loaded only by the logical server. */
public final class Config {
    private static final Map<String, BooleanValue> BOOLEAN_VALUES = new LinkedHashMap<>();
    private static final Map<String, IntValue> INT_VALUES = new LinkedHashMap<>();
    private static boolean loaded;

    public static final BooleanValue LOG_STARTUP = define("logStartup", true);
    public static final BooleanValue ALLOW_BLASTER_FRIENDLY_FIRE =
            define("allowBlasterFriendlyFire", false);
    public static final BooleanValue ALLOW_BLASTER_PVP = define("allowBlasterPvp", true);
    public static final BooleanValue ALLOW_FORCE_PVP = define("allowForcePvp", true);
    public static final BooleanValue ALLOW_FORCE_BLOCK_PHYSICS =
            define("allowForceBlockPhysics", true);
    public static final BooleanValue ALLOW_FORCE_VEHICLE_PHYSICS =
            define("allowForceVehiclePhysics", true);
    public static final BooleanValue ALLOW_CLASS_PVP = define("allowClassPvp", false);
    public static final BooleanValue ENABLE_DYNAMIC_FACTION_AI =
            define("enableDynamicFactionAi", true);
    public static final IntValue NPC_AI_MAX_SCAN_RADIUS =
            define("npcAiMaxScanRadius", 48, 8, 64);
    public static final IntValue NPC_AI_MAX_RESPONDERS =
            define("npcAiMaxResponders", 12, 1, 32);

    private Config() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path configPath = configPath();
        Path legacyConfigPath = legacyConfigPath();
        Path source = Files.isRegularFile(configPath) ? configPath
                : Files.isRegularFile(legacyConfigPath) ? legacyConfigPath : null;
        if (source == null) {
            save();
            return;
        }

        if (read(source) && source.equals(legacyConfigPath)) {
            save();
            GalacticWars.LOGGER.info("Migrated server policy from {} to {}", source, configPath);
        }
    }

    /** Reloads the authoritative policy from disk for the operator command. */
    public static synchronized boolean reload() {
        Path configPath = configPath();
        if (!Files.isRegularFile(configPath)) {
            BOOLEAN_VALUES.values().forEach(BooleanValue::reset);
            INT_VALUES.values().forEach(IntValue::reset);
            save();
            return true;
        }
        Map<String, Boolean> booleanSnapshot = new LinkedHashMap<>();
        Map<String, Integer> intSnapshot = new LinkedHashMap<>();
        BOOLEAN_VALUES.forEach((key, value) -> booleanSnapshot.put(key, value.get()));
        INT_VALUES.forEach((key, value) -> intSnapshot.put(key, value.get()));
        if (!read(configPath)) {
            booleanSnapshot.forEach((key, value) -> BOOLEAN_VALUES.get(key).set(value));
            intSnapshot.forEach((key, value) -> INT_VALUES.get(key).set(value));
            return false;
        }
        return true;
    }

    private static boolean read(Path source) {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(source)) {
            properties.load(input);
        } catch (IOException exception) {
            GalacticWars.LOGGER.error("Unable to read {}", source, exception);
            return false;
        }

        Map<String, Boolean> parsedBooleans = new LinkedHashMap<>();
        for (Map.Entry<String, BooleanValue> entry : BOOLEAN_VALUES.entrySet()) {
            String key = entry.getKey();
            String encoded = properties.getProperty(key);
            if (encoded == null) {
                parsedBooleans.put(key, entry.getValue().getDefault());
            } else {
                String trimmed = encoded.trim();
                if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false")) {
                    parsedBooleans.put(key, Boolean.parseBoolean(trimmed));
                } else {
                    GalacticWars.LOGGER.warn("Rejected invalid boolean {}={} in {}", key, encoded, source);
                    return false;
                }
            }
        }

        Map<String, Integer> parsedInts = new LinkedHashMap<>();
        for (Map.Entry<String, IntValue> entry : INT_VALUES.entrySet()) {
            String key = entry.getKey();
            String encoded = properties.getProperty(key);
            if (encoded == null) {
                parsedInts.put(key, entry.getValue().getDefault());
                continue;
            }
            try {
                int value = Integer.parseInt(encoded.trim());
                if (!entry.getValue().accepts(value)) {
                    GalacticWars.LOGGER.warn(
                            "Rejected out-of-range integer {}={} in {}", key, encoded, source);
                    return false;
                }
                parsedInts.put(key, value);
            } catch (NumberFormatException exception) {
                GalacticWars.LOGGER.warn("Rejected invalid integer {}={} in {}", key, encoded, source);
                return false;
            }
        }

        parsedBooleans.forEach((key, value) -> BOOLEAN_VALUES.get(key).set(value));
        parsedInts.forEach((key, value) -> INT_VALUES.get(key).set(value));
        return true;
    }

    public static synchronized void save() {
        Properties properties = new Properties();
        BOOLEAN_VALUES.forEach(
                (key, value) -> properties.setProperty(key, Boolean.toString(value.get())));
        INT_VALUES.forEach(
                (key, value) -> properties.setProperty(key, Integer.toString(value.get())));

        Path configPath = configPath();
        Path parent = configPath.getParent();
        Path temporary = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        try {
            Files.createDirectories(parent);
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "Galactic Wars common configuration");
            }
            try {
                Files.move(temporary, configPath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            GalacticWars.LOGGER.error("Unable to save {}", configPath, exception);
        }
    }

    private static Path configPath() {
        return Platform.getConfigFolder().resolve("galacticwars-server.properties");
    }

    private static Path legacyConfigPath() {
        return Platform.getConfigFolder().resolve("galacticwars.properties");
    }

    private static BooleanValue define(String key, boolean defaultValue) {
        BooleanValue value = new BooleanValue(key, defaultValue);
        if (BOOLEAN_VALUES.containsKey(key) || INT_VALUES.containsKey(key)) {
            throw new IllegalStateException("Duplicate configuration key " + key);
        }
        BOOLEAN_VALUES.put(key, value);
        return value;
    }

    private static IntValue define(String key, int defaultValue, int minimum, int maximum) {
        IntValue value = new IntValue(key, defaultValue, minimum, maximum);
        if (BOOLEAN_VALUES.containsKey(key) || INT_VALUES.containsKey(key)) {
            throw new IllegalStateException("Duplicate configuration key " + key);
        }
        INT_VALUES.put(key, value);
        return value;
    }

    public static final class BooleanValue {
        private final String key;
        private final boolean defaultValue;
        private final AtomicBoolean value;

        private BooleanValue(String key, boolean defaultValue) {
            this.key = Objects.requireNonNull(key, "key");
            this.defaultValue = defaultValue;
            this.value = new AtomicBoolean(defaultValue);
        }

        public String key() {
            return key;
        }

        public boolean getDefault() {
            return defaultValue;
        }

        public boolean get() {
            return value.get();
        }

        public boolean getAsBoolean() {
            return get();
        }

        public void set(boolean nextValue) {
            value.set(nextValue);
        }

        private void reset() {
            value.set(defaultValue);
        }
    }

    public static final class IntValue {
        private final String key;
        private final int defaultValue;
        private final int minimum;
        private final int maximum;
        private final AtomicInteger value;

        private IntValue(String key, int defaultValue, int minimum, int maximum) {
            this.key = Objects.requireNonNull(key, "key");
            if (minimum > maximum || defaultValue < minimum || defaultValue > maximum) {
                throw new IllegalArgumentException("Invalid integer configuration bounds for " + key);
            }
            this.defaultValue = defaultValue;
            this.minimum = minimum;
            this.maximum = maximum;
            this.value = new AtomicInteger(defaultValue);
        }

        public String key() {
            return key;
        }

        public int getDefault() {
            return defaultValue;
        }

        public int minimum() {
            return minimum;
        }

        public int maximum() {
            return maximum;
        }

        public int get() {
            return value.get();
        }

        public void set(int nextValue) {
            if (!accepts(nextValue)) {
                throw new IllegalArgumentException(
                        key + " must be between " + minimum + " and " + maximum);
            }
            value.set(nextValue);
        }

        private boolean accepts(int nextValue) {
            return nextValue >= minimum && nextValue <= maximum;
        }

        private void reset() {
            value.set(defaultValue);
        }
    }
}
