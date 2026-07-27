package galacticwars.clonewars.data;

import com.google.gson.JsonObject;
import galacticwars.clonewars.army.ArmyEquipmentLoadout;

public final class GameplayDataEquipmentDefaultsTest {
    private GameplayDataEquipmentDefaultsTest() {
    }

    public static void main(String[] args) {
        missingMainHandDefaultsToEmpty();
        explicitEquipmentIsPreserved();
        System.out.println("GameplayDataEquipmentDefaultsTest passed");
    }

    private static void missingMainHandDefaultsToEmpty() {
        ArmyEquipmentLoadout equipment = GameplayDataManager.parseUnitEquipment(new JsonObject());

        assertTrue(equipment.mainHandItemId().isEmpty(), "missing main hand is empty");
        assertTrue(equipment.headItemId().isEmpty(), "missing helmet is empty");
        assertTrue(equipment.chestItemId().isEmpty(), "missing chest armor is empty");
        assertTrue(equipment.legsItemId().isEmpty(), "missing leg armor is empty");
        assertTrue(equipment.feetItemId().isEmpty(), "missing boots are empty");
    }

    private static void explicitEquipmentIsPreserved() {
        JsonObject json = new JsonObject();
        json.addProperty("main_hand", "galacticwars:vibroblade");
        json.addProperty("head", "galacticwars:nightsister_weave_helmet");

        ArmyEquipmentLoadout equipment = GameplayDataManager.parseUnitEquipment(json);

        assertTrue(
                equipment.mainHandItemId().equals("galacticwars:vibroblade"),
                "explicit main hand is preserved");
        assertTrue(
                equipment.headItemId().equals("galacticwars:nightsister_weave_helmet"),
                "explicit helmet is preserved");
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
