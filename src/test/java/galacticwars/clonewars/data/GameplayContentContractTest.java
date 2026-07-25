package galacticwars.clonewars.data;

import galacticwars.clonewars.army.ArmyUnitCatalog;
import galacticwars.clonewars.faction.FactionCatalog;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameplayContentContractTest {
    private GameplayContentContractTest() {
    }

    public static void main(String[] args) {
        exactCoreIdsAreRequired();
        contentStateDistinguishesReadinessFromRejectedReloads();
        contentFingerprintIsStableAcrossMapOrder();
        System.out.println("GameplayContentContractTest passed");
    }

    private static void exactCoreIdsAreRequired() {
        CoreContentBindings.requireExactIds(
                "test", Set.of("alpha", "beta"), Set.of("beta", "alpha"));
        assertRejected(() -> CoreContentBindings.requireExactIds(
                "test", Set.of("alpha", "beta"), Set.of("alpha")), "missing id");
        assertRejected(() -> CoreContentBindings.requireExactIds(
                "test", Set.of("alpha"), Set.of("alpha", "gamma")), "unexpected id");
    }

    private static void contentStateDistinguishesReadinessFromRejectedReloads() {
        GameplayContentState initial = GameplayContentState.uninitialized();
        assertFalse(initial.hasUsableSnapshot(), "uninitialized state");

        String hash = "a".repeat(64);
        GameplayContentState ready = GameplayContentState.ready(1, hash);
        assertTrue(ready.hasUsableSnapshot(), "ready state");
        assertEquals(GameplayContentState.Status.READY, ready.status(), "ready status");

        GameplayContentState rejected = GameplayContentState.rejectedReload(
                ready.generation(), ready.contentHash(), "invalid datapack");
        assertTrue(rejected.hasUsableSnapshot(), "rejected reload retains snapshot");
        assertEquals(ready.contentHash(), rejected.contentHash(), "retained content hash");
        assertEquals(GameplayContentState.Status.RELOAD_REJECTED, rejected.status(), "reload status");

        GameplayContentState failure = GameplayContentState.initialFailure("missing factions");
        assertFalse(failure.hasUsableSnapshot(), "initial failure has no snapshot");
    }

    private static void contentFingerprintIsStableAcrossMapOrder() {
        KingdomBaseBlueprint forwardBase = KingdomBaseBlueprint.starterKeep();
        KingdomBaseBlueprint barracks = KingdomBaseBlueprint.barracks();
        LinkedHashMap<String, KingdomBaseBlueprint> first = new LinkedHashMap<>();
        first.put(forwardBase.id(), forwardBase);
        first.put(barracks.id(), barracks);
        LinkedHashMap<String, KingdomBaseBlueprint> second = new LinkedHashMap<>();
        second.put(barracks.id(), barracks);
        second.put(forwardBase.id(), forwardBase);

        String firstHash = GameplayContentFingerprint.compute(snapshot(first));
        String secondHash = GameplayContentFingerprint.compute(snapshot(second));
        assertEquals(firstHash, secondHash, "map-order-independent fingerprint");
        assertFalse(firstHash.equals(GameplayContentFingerprint.compute(snapshot(Map.of(forwardBase.id(), forwardBase)))),
                "content change alters fingerprint");
        KingdomBaseBlueprint renamed = new KingdomBaseBlueprint(
                forwardBase.id(), "Renamed Forward Base", forwardBase.anchor(),
                forwardBase.allowedRotations(), forwardBase.placements(),
                forwardBase.housingReward(), forwardBase.storageSlotReward(),
                forwardBase.worksiteType(), forwardBase.worksiteCapacity(),
                forwardBase.commanderSlotReward());
        assertFalse(firstHash.equals(GameplayContentFingerprint.compute(snapshot(Map.of(
                        renamed.id(), renamed, barracks.id(), barracks)))),
                "display content alters fingerprint");

        LaunchContentDefinitions.VehicleDefinition firstVehicle = vehicle(
                linkedMap("minecraft:iron_ingot", 4, "minecraft:redstone", 2),
                linkedSet("vehicle_crafting", "factory_access"));
        LaunchContentDefinitions.VehicleDefinition secondVehicle = vehicle(
                linkedMap("minecraft:redstone", 2, "minecraft:iron_ingot", 4),
                linkedSet("factory_access", "vehicle_crafting"));
        assertEquals(
                GameplayContentFingerprint.compute(snapshot(first, launch(firstVehicle))),
                GameplayContentFingerprint.compute(snapshot(second, launch(secondVehicle))),
                "nested map-and-set-order-independent fingerprint");
    }

    private static GameplayDataSnapshot snapshot(Map<String, KingdomBaseBlueprint> blueprints) {
        return snapshot(blueprints, LaunchContentDefinitions.empty());
    }

    private static GameplayDataSnapshot snapshot(
            Map<String, KingdomBaseBlueprint> blueprints,
            LaunchContentDefinitions launch
    ) {
        return new GameplayDataSnapshot(
                new FactionCatalog(Map.of()),
                new ArmyUnitCatalog(List.of()),
                Map.of(),
                Map.of(),
                blueprints,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                launch);
    }

    private static LaunchContentDefinitions launch(LaunchContentDefinitions.VehicleDefinition vehicle) {
        return new LaunchContentDefinitions(
                Map.of(), Map.of(vehicle.id(), vehicle), Map.of(), Map.of(), Map.of(), Map.of());
    }

    private static LaunchContentDefinitions.VehicleDefinition vehicle(
            Map<String, Integer> inputs,
            Set<String> requirements
    ) {
        return new LaunchContentDefinitions.VehicleDefinition(
                "test_speeder", "hover", 1, 20, 100, "vehicle_crafting",
                0.4D, 0.5D, 0.0D, 5, "blaster", List.of("driver"),
                25, inputs, requirements);
    }

    private static Map<String, Integer> linkedMap(String firstKey, int firstValue, String secondKey, int secondValue) {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        values.put(firstKey, firstValue);
        values.put(secondKey, secondValue);
        return values;
    }

    private static Set<String> linkedSet(String first, String second) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        values.add(first);
        values.add(second);
        return values;
    }

    private static void assertRejected(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " was accepted");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean value, String label) {
        assertTrue(!value, label);
    }
}
